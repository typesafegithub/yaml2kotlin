package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.listOf
import kotlin.collections.mapOf

public val workflowDeployenvironments: Workflow = workflow(
      name = "Deploy Environments",
      on = listOf(
        PullRequest(),
        Push(),
        WorkflowDispatch(),
        ),
      sourceFile = Paths.get(".github/workflows/deployenvironments.main.kts"),
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
          command = "echo Hello, world!",
        )
        run(
          name = "Run a multi-line script",
          command = """
          |echo Add other actions to build,
          |echo test, and deploy your project.
          |""".trimMargin(),
        )
      }

      job(
        id = "deploy-staging",
        runsOn = RunnerType.UbuntuLatest,
        _customArguments = mapOf(
        "needs" to listOf("build"),
        )
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
          command = "echo Hello, world!",
        )
        run(
          name = "Run a multi-line script",
          command = """
          |echo Add other actions to build,
          |echo test, and deploy your project.
          |""".trimMargin(),
        )
      }

      job(
        id = "test-site",
        name = "Test on ${'$'}{{ matrix.browser }}",
        runsOn = RunnerType.UbuntuLatest,
        _customArguments = mapOf(
        "needs" to listOf("deploy-staging"),
        )
      ) {
        run(
          name = "Run a one-line script",
          command = "echo Hello, world!",
        )
        run(
          name = "Run a multi-line script",
          command = """
          |echo Add other actions to build,
          |echo test, and deploy your project.
          |""".trimMargin(),
        )
      }

      job(
        id = "deploy-production",
        runsOn = RunnerType.UbuntuLatest,
        _customArguments = mapOf(
        "needs" to listOf("test-site"),
        )
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
          command = "echo Hello, world!",
        )
        run(
          name = "Run a multi-line script",
          command = """
          |echo Add other actions to build,
          |echo test, and deploy your project.
          |""".trimMargin(),
        )
      }

      job(
        id = "deploy-review",
        runsOn = RunnerType.UbuntuLatest,
        _customArguments = mapOf(
        "needs" to listOf("build"),
        )
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
          command = "echo Hello, world!",
        )
        run(
          name = "Run a multi-line script",
          command = """
          |echo Add other actions to build,
          |echo test, and deploy your project.
          |""".trimMargin(),
        )
      }

    }