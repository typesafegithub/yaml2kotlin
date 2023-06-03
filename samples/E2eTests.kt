package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.mapOf

public val workflowE2etests: Workflow = workflow(
      name = "E2E tests",
      on = listOf(
        PullRequest(
          types = listOf(PullRequest.Type.Opened, PullRequest.Type.Synchronize,
              PullRequest.Type.Reopened),
          branches = listOf("develop"),
        ),
        Push(
          branches = listOf("develop"),
        ),
        ),
      sourceFile = Paths.get(".github/workflows/e2etests.main.kts"),
    ) {

        job(
          id = "cypress-run",
          runsOn = RunnerType.UbuntuLatest,
        ) {
          uses(
            name = "Checkout",
            action = CustomAction(
              actionOwner = "actions",
              actionName = "checkout",
              actionVersion = "v1",
              inputs = emptyMap()),
          )
          run(
            name = "Setup npm package",
            command = "npm install",
          )
          uses(
            name = "Run E2E test",
            action = CustomAction(
              actionOwner = "cypress-io",
              actionName = "github-action",
              actionVersion = "v1",
              inputs = mapOf(
                "config" to "baseUrl=https://seedxb.com",
              )
            ),
          )
        }

    }