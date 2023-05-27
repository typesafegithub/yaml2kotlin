package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Cron
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.PullRequestTarget
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.Schedule
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch.Type.Choice
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.linkedMapOf
import kotlin.collections.listOf
import kotlin.collections.mapOf

public val workflowGeneratedsource: Workflow = workflow(
      name = "generated",
      on = listOf(
        PullRequest(
          types = listOf(PullRequest.Type.AutoMergeDisabled, PullRequest.Type.Opened),
          branches = listOf("branch1", "branch2"),
          paths = listOf("path1", "path2"),
        ),
        Push(
          branches = listOf("branch1", "branch2"),
          paths = listOf("path1", "path2"),
          tags = listOf("tag1", "tag2"),
        ),
        PullRequestTarget(),
        Schedule(listOf(
          Cron("0 0 * * *"),
        )),
        WorkflowDispatch(mapOf(
          "logLevel" to WorkflowDispatch.Input(
            type = Choice,
            description = "Log level",
            default = "warning",
            required = true,
            options = listOf("info", "warning", "debug"),
          ),
        ))
        ),
      sourceFile = Paths.get(".github/workflows/generatedsource.main.kts"),
      env = linkedMapOf(
        "GRADLE_ENTERPRISE_ACCESS_KEY" to expr("secrets.GRADLE_ENTERPRISE_ACCESS_KEY"),
        "GRADLE_BUILD_ACTION_CACHE_DEBUG_ENABLED" to "true",
      ),
    ) {
      job(
        id = "check_yaml_consistency",
        name = "Check YAML consistency",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "Check out",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v3",
            inputs = emptyMap()),
        )
        run(
          name = "Install Kotlin",
          command = "sudo snap install --classic kotlin",
        )
        run(
          name = "Consistency check",
          command = "diff -u '.github/workflows/build.yaml' <('.github/workflows/build.main.kts')",
          env = linkedMapOf(
            "HELLO" to "ok",
            "PAT" to "rick",
          ),
          condition = "true",
        )
      }

      job(
        id = "build_for_UbuntuLatest",
        runsOn = RunnerType.UbuntuLatest,
        env = linkedMapOf(
          "COLOR" to "blue",
          "SIZE" to "XXL",
        ),

        _customArguments = mapOf(
        "needs" to listOf("check_yaml_consistency"),
        )
      ) {
        uses(
          name = "Checkout",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v3",
            inputs = emptyMap()),
          env = linkedMapOf(
            "HELLO" to "ok",
            "PAT" to "rick",
          ),
        )
        uses(
          name = "Set up JDK",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "setup-java",
            actionVersion = "v3",
            inputs = mapOf(
              "java-version" to "11",
              "distribution" to "adopt",
            )
          ),
          env = linkedMapOf(
            "HELLO" to "ok",
            "PAT" to "rick",
          ),
        )
        uses(
          name = "Build",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "arguments" to "build",
            )
          ),
        )
        uses(
          name = "setup",
          action = CustomAction(
            actionOwner = "docker",
            actionName = "setup-buildx-action",
            actionVersion = "v1",
            inputs = mapOf(
              "driver-opts" to """
              |hello
              |world
              |""".trimMargin(),
              "install" to "true",
            )
          ),
        )
      }

    }