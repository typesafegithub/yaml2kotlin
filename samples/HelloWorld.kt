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

public val workflowHelloworld: Workflow = workflow(
      name = "HelloWorld",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/workflows/helloworld.main.kts"),
    ) {

        job(
          id = "build",
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
            name = "Run a one-line script",
            command = "echo \"Hello, world!\"",
          )
        }

    }