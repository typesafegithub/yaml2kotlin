package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Cron
import io.github.typesafegithub.workflows.domain.triggers.Schedule
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.mapOf

public val workflowGetblogposts: Workflow = workflow(
      name = "Get latest blog posts",
      on = listOf(
        Schedule(listOf(
          Cron("0 0 * * *"),
        )),
        WorkflowDispatch(),
        ),
      sourceFile = Paths.get(".github/workflows/getblogposts.main.kts"),
    ) {

        job(
          id = "update_profile_data",
          name = "Get latest blog posts",
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
            name = "SetupNodeV3",
            action = CustomAction(
              actionOwner = "actions",
              actionName = "setup-node",
              actionVersion = "v3",
              inputs = mapOf(
                "node-version" to "16.17.1",
              )
            ),
          )
          run(
            name = "Get blog posts",
            command = """
            |npm install
            |node --experimental-fetch bin/generateDevToPosts.js
            |node bin/generateHashnodeUrlMapping.js
            |""".trimMargin(),
            env = linkedMapOf(
              "DEV_API_KEY" to expr { "secrets.DEV_API_KEY" },
            ),
          )
          run(
            name = "Commit changes",
            command = """
            |git config user.name "GitHub Actions Bot"
            |git config user.email "<>"
            |git pull origin main
            |git add .
            |if [[ -n "${'$'}(git status --porcelain)" ]]; then
            |  git commit -m "chore (automated): update blog posts"
            |  git push origin main
            |fi
            |""".trimMargin(),
            env = linkedMapOf(
              "GITHUB_TOKEN" to expr { "secrets.GITHUB_TOKEN" },
            ),
          )
        }

    }