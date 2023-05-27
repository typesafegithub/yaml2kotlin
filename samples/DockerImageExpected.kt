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
import kotlin.collections.linkedMapOf
import kotlin.collections.mapOf

public val workflowDockerimagegeneratedYml: Workflow = workflow(
      name = "Docker Image",
      on = listOf(
        PullRequest(
          branches = listOf("main"),
        ),
        Push(
          branches = listOf("main"),
        ),
        ),
      sourceFile = Paths.get(".github/workflows/dockerimagegenerated.yml.main.kts"),
      env = linkedMapOf(
        "REGISTRY" to "ghcr.io",
        "IMAGE_NAME" to expr("github.repository"),
      ),
    ) {
      job(
        id = "build",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "Log in to the Container registry",
          action = CustomAction(
            actionOwner = "docker",
            actionName = "login-action",
            actionVersion = "f054a8b539a109f9f41c372932f1ae047eff08c9",
            inputs = mapOf(
              "registry" to "${'$'}{{ env.REGISTRY }}",
              "username" to "${'$'}{{ github.actor }}",
              "password" to "${'$'}{{ secrets.GITHUB_TOKEN }}",
            )
          ),
        )
        uses(
          name = "CheckoutV2",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v2",
            inputs = emptyMap()),
        )
        uses(
          name = "Extract metadata (tags, labels) for Docker",
          action = CustomAction(
            actionOwner = "docker",
            actionName = "metadata-action",
            actionVersion = "98669ae865ea3cffbcbaa878cf57c20bbf1c6c38",
            inputs = mapOf(
              "images" to "${'$'}{{ env.REGISTRY }}/${'$'}{{ env.IMAGE_NAME }}",
            )
          ),
        )
        uses(
          name = "Build and push Docker image",
          action = CustomAction(
            actionOwner = "docker",
            actionName = "build-push-action",
            actionVersion = "ad44023a93711e3deb337508980b4b5e9bcdc5dc",
            inputs = mapOf(
              "context" to ".",
              "push" to "true",
              "tags" to "${'$'}{{ steps.meta.outputs.tags }}",
              "labels" to "${'$'}{{ steps.meta.outputs.labels }}",
            )
          ),
        )
      }

    }
