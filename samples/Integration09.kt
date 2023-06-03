package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.listOf
import kotlin.collections.mapOf

public val workflowIntegration09: Workflow = workflow(
      name = "Test workflow",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/workflows/integration09.main.kts"),
    ) {

        job(
          id = "check_yaml_consistency",
          name = "Check YAML consistency",
          runsOn = RunnerType.UbuntuLatest,
          condition = expr { "always()" },
        ) {
          uses(
            name = "Check out",
            action = CustomAction(
              actionOwner = "actions",
              actionName = "checkout",
              actionVersion = "v3",
              inputs = emptyMap()),
          )
          run(
            name = "Consistency check",
            command =
                "diff -u '.github/workflows/some_workflow.yaml' <('.github/workflows/some_workflow.main.kts')",
          )
        }

        job(
          id = "test_job",
          name = "Test Job",
          runsOn = RunnerType.UbuntuLatest,
          _customArguments = mapOf(
          "needs" to listOf("check_yaml_consistency"),
          )
        ) {
          uses(
            name = "CheckoutV3",
            action = CustomAction(
              actionOwner = "actions",
              actionName = "checkout",
              actionVersion = "v3",
              inputs = emptyMap()),
          )
          run(
            name = "echo 'hello!'",
            command = "echo 'hello!'",
          )
        }

    }