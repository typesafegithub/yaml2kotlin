package expected
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.mapOf

public val workflowIntegration06: Workflow = workflow(
      name = "Test workflow",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/workflows/integration06.main.kts"),
      concurrency = Concurrency(group = "workflow_staging_environment", cancelInProgress = false),
    ) {
      job(
        id = "test_job",
        runsOn = RunnerType.UbuntuLatest,
        concurrency = Concurrency(group = "job_staging_environment", cancelInProgress = false),
      ) {
        uses(
          name = "AddAndCommitV9",
          action = CustomAction(
            actionOwner = "EndBug",
            actionName = "add-and-commit",
            actionVersion = "v9",
            inputs = emptyMap()),
        )
        uses(
          name = "Some step consuming other step's output",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v3",
            inputs = mapOf(
              "repository" to "${'$'}{{ step-0 }}",
              "ref" to "${'$'}{{ steps.step-0.outputs.commit_sha }}",
              "token" to "${'$'}{{ steps.step-0.outputs.my-unsafe-output }}",
            )
          ),
        )
      }

    }