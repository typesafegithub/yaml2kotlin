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

public val workflowUpdatearticles: Workflow = workflow(
      name = "Update Articles",
      on = listOf(
        Schedule(listOf(
          Cron("30 23 * * *"),
        )),
        WorkflowDispatch(),
        ),
      sourceFile = Paths.get(".github/workflows/updatearticles.main.kts"),
    ) {

        job(
          id = "run-script",
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
          run(
            name = "Run script to get articles from dev.to",
            command = """
            |./scripts/post_files_from_GET_json.sh
            |""".trimMargin(),
          )
          run(
            name = "Commit to the repo",
            command = "git config --global user.name 'Bruno Drugowick'",
          )
        }

    }