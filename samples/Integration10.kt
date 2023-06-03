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

public val workflowIntegration10: Workflow = workflow(
      name = "Test workflow",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/workflows/integration10.main.kts"),
    ) {

        job(
          id = "test_job",
          runsOn = RunnerType.UbuntuLatest,
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
            name = "Hello world!",
            command = "echo 'hello!'",
          )
        }

    }