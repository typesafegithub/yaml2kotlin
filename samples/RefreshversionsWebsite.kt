package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.mapOf

public val workflowRefreshversionswebsite: Workflow = workflow(
      name = "RefreshVersions Website",
      on = listOf(
        Push(
          branches = listOf("release"),
        ),
        WorkflowDispatch(),
        ),
      sourceFile = Paths.get(".github/workflows/refreshversionswebsite.main.kts"),
    ) {
      job(
        id = "deploy",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "CheckoutV3",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v3",
            inputs = emptyMap()),
        )
        run(
          name = "./docs/DocsCopier.main.kts",
          command = "./docs/DocsCopier.main.kts",
        )
        uses(
          name = "SetupPythonV2",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "setup-python",
            actionVersion = "v2",
            inputs = mapOf(
              "python-version" to "3.x",
            )
          ),
        )
        run(
          name = "pip install -r docs/requirements.txt",
          command = "pip install -r docs/requirements.txt",
        )
        run(
          name = "mkdocs gh-deploy --force",
          command = "mkdocs gh-deploy --force",
        )
      }

    }