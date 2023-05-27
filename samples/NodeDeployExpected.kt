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

public val workflowNodedeploygeneratedYml: Workflow = workflow(
      name = "Node Deploy",
      on = listOf(
        Push(
          branches = listOf("master"),
        ),
        ),
      sourceFile = Paths.get(".github/workflows/nodedeploygenerated.yml.main.kts"),
    ) {
      job(
        id = "build-and-deploy",
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
        uses(
          name = "SetupNodeV1",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "setup-node",
            actionVersion = "v1",
            inputs = mapOf(
              "node-version" to "10.x",
            )
          ),
        )
        uses(
          name = "CacheV1",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "cache",
            actionVersion = "v1",
            inputs = mapOf(
              "path" to "~/.npm",
              "key" to "${'$'}{{ runner.os }}-node-${'$'}{{ hashFiles('**/package-lock.json') }}",
              "restore-keys" to """
              |${'$'}{{ runner.os }}-node-
              |""".trimMargin(),
            )
          ),
        )
        run(
          name = "Build",
          command = """
          |npm install
          |npm run-script deploy
          |""".trimMargin(),
        )
        uses(
          name = "Deploy",
          action = CustomAction(
            actionOwner = "JamesIves",
            actionName = "github-pages-deploy-action",
            actionVersion = "releases",
            inputs = mapOf(
              "GITHUB_TOKEN" to "${'$'}{{ secrets.GITHUB_TOKEN }}",
              "BRANCH" to "gh-pages",
              "FOLDER" to "dist/angular-ci-cd",
            )
          ),
        )
      }

    }
