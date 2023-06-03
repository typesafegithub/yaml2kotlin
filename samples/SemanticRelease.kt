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
import kotlin.collections.mapOf

public val workflowSemanticrelease: Workflow = workflow(
      name = "Semantic release",
      on = listOf(
        Push(
          branches = listOf("master"),
        ),
        ),
      sourceFile = Paths.get(".github/workflows/semanticrelease.main.kts"),
    ) {

        job(
          id = "publish",
          runsOn = RunnerType.UbuntuLatest,
        ) {
          uses(
            name = "CheckoutV2",
            action = CustomAction(
              actionOwner = "actions",
              actionName = "checkout",
              actionVersion = "v2",
              inputs = emptyMap()),
          )
          uses(
            name = "Setup Node.js",
            action = CustomAction(
              actionOwner = "actions",
              actionName = "setup-node",
              actionVersion = "v1",
              inputs = mapOf(
                "node-version" to "12",
              )
            ),
          )
          run(
            name = "Install dependencies",
            command = "npm install",
          )
          run(
            name = "Build app",
            command = "npm run build",
          )
          run(
            name = "Semantic release",
            command = "npx semantic-release",
            env = linkedMapOf(
              "GITHUB_TOKEN" to expr { "secrets.GH_TOKEN" },
            ),
          )
        }

    }