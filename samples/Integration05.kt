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
import kotlin.collections.linkedMapOf

public val workflowIntegration05: Workflow = workflow(
      name = "Overridden name!",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/workflows/integration05.main.kts"),
      env = linkedMapOf(
        "SIMPLE_VAR" to "simple-value-workflow",
        "MULTILINE_VAR" to """
        |hey,
        |hi,
        |hello! workflow
        """.trimMargin(),
      ),
    ) {

        job(
          id = "test_job",
          runsOn = RunnerType.UbuntuLatest,
          condition = expr { "always()" },
          env = linkedMapOf(
            "SIMPLE_VAR" to "simple-value-job",
            "MULTILINE_VAR" to """
            |hey,
            |hi,
            |hello! job
            """.trimMargin(),
          ),

        ) {
          uses(
            name = "Check out",
            action = CustomAction(
              actionOwner = "actions",
              actionName = "checkout",
              actionVersion = "v3",
              inputs = emptyMap()),
            env = linkedMapOf(
              "SIMPLE_VAR" to "simple-value-uses",
              "MULTILINE_VAR" to """
              |hey,
              |hi,
              |hello! uses
              """.trimMargin(),
            ),
          )
          run(
            name = "Hello world!",
            command = "echo 'hello!'",
            env = linkedMapOf(
              "SIMPLE_VAR" to "simple-value-run",
              "MULTILINE_VAR" to """
              |hey,
              |hi,
              |hello! run
              """.trimMargin(),
            ),
          )
        }

    }