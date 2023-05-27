package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch.Type.Boolean
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch.Type.Choice
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.collections.listOf
import kotlin.collections.mapOf

public val workflowRefreshversionsbuildgeneratedYml: Workflow = workflow(
      name = "RefreshVersions build",
      on = listOf(
        PullRequest(
          paths = listOf("plugins/**", "sample-kotlin/**", "sample-groovy/**", "!**.md",
              "!.editorconfig", "!**/.gitignore", "!**.adoc", "!docs/**"),
        ),
        Push(
          branches = listOf("main"),
          paths = listOf("plugins/**", "sample-kotlin/**", "sample-groovy/**", "!**.md",
              "!.editorconfig", "!**/.gitignore", "!**.adoc", "!docs/**"),
        ),
        WorkflowDispatch(mapOf(
          "run-refreshVersions-task" to WorkflowDispatch.Input(
            type = Boolean,
            description = "Run the refreshVersions task",
            default = "false",
            required = true,
          ),
          "sample-kotlin" to WorkflowDispatch.Input(
            type = Boolean,
            description = "Enable sample-kotlin",
            default = "true",
            required = true,
          ),
          "sample-groovy" to WorkflowDispatch.Input(
            type = Boolean,
            description = "Enable sample-groovy",
            default = "true",
            required = true,
          ),
          "sample-multi-modules" to WorkflowDispatch.Input(
            type = Boolean,
            description = "Enable sample-multi-modules",
            default = "true",
            required = true,
          ),
          "sample-kotlin-js" to WorkflowDispatch.Input(
            type = Boolean,
            description = "Enable sample-kotlin-js",
            default = "true",
            required = true,
          ),
          "sample-android" to WorkflowDispatch.Input(
            type = Boolean,
            description = "Enable sample-android",
            default = "false",
            required = true,
          ),
          "run-on" to WorkflowDispatch.Input(
            type = Choice,
            description = "Where to run this workflow",
            default = "ubuntu-latest",
            required = true,
            options = listOf("windows-2022", "windows-2019", "windows-latest", "macos-11",
                "macos-10.5", "macos-latest", "ubuntu-20.04", "ubuntu-18.04", "ubuntu-latest"),
          ),
        ))
        ),
      sourceFile = Paths.get(".github/workflows/refreshversionsbuildgenerated.yml.main.kts"),
    ) {
      job(
        id = "check-all",
        runsOn = RunnerType.Custom(expr("github.event.inputs.run-on || 'ubuntu-latest'")),
      ) {
        run(
          name = "Enable long paths for git Windows",
          command = "git config --global core.longpaths true",
          condition = expr("runner.os == 'Windows'"),
        )
        uses(
          name = "CheckoutV3",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v3",
            inputs = emptyMap()),
        )
        uses(
          name = "Configure JDK",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "setup-java",
            actionVersion = "v3",
            inputs = mapOf(
              "distribution" to "adopt",
              "java-version" to "11",
            )
          ),
        )
        uses(
          name = "Check plugins and publish them to MavenLocal",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "plugins/gradlew",
              "build-root-directory" to "plugins",
              "arguments" to "check publishToMavenLocal --stacktrace",
            )
          ),
        )
        uses(
          name = "Run refreshVersions on sample-kotlin",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-kotlin/gradlew",
              "build-root-directory" to "sample-kotlin",
              "arguments" to "refreshVersions --stacktrace",
            )
          ),
          condition =
              expr("github.event_name != 'workflow_dispatch' || github.event.inputs.sample-kotlin == 'true' && github.event.inputs.run-refreshVersions-task == 'true'"),
        )
        uses(
          name = "Check sample-kotlin",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-kotlin/gradlew",
              "build-root-directory" to "sample-kotlin",
              "arguments" to "check --stacktrace --configuration-cache",
            )
          ),
          condition =
              expr("github.event_name != 'workflow_dispatch' || github.event.inputs.sample-kotlin == 'true'"),
        )
        uses(
          name = "Run refreshVersions on sample-groovy",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-groovy/gradlew",
              "build-root-directory" to "sample-groovy",
              "arguments" to "refreshVersions --stacktrace",
            )
          ),
          condition =
              expr("github.event.inputs.sample-groovy == 'true' && github.event.inputs.run-refreshVersions-task == 'true'"),
        )
        uses(
          name = "Check sample-groovy",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-groovy/gradlew",
              "build-root-directory" to "sample-groovy",
              "arguments" to "check --stacktrace",
            )
          ),
          condition = expr("github.event.inputs.sample-groovy == 'true'"),
        )
        uses(
          name = "Check buildSrc of sample-groovy (simulates IDE Gradle sync)",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-groovy/gradlew",
              "build-root-directory" to "sample-groovy/buildSrc",
              "arguments" to "help --stacktrace",
            )
          ),
          condition = expr("github.event.inputs.sample-groovy == 'true'"),
        )
        uses(
          name = "Run refreshVersions on sample-multi-modules",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-multi-modules/gradlew",
              "build-root-directory" to "sample-multi-modules",
              "arguments" to "refreshVersions --stacktrace",
            )
          ),
          condition =
              expr("github.event.inputs.sample-multi-modules == 'true' && github.event.inputs.run-refreshVersions-task == 'true'"),
        )
        uses(
          name = "Check sample-multi-modules",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-multi-modules/gradlew",
              "build-root-directory" to "sample-multi-modules",
              "arguments" to "check --stacktrace",
            )
          ),
          condition = expr("github.event.inputs.sample-multi-modules == 'true'"),
        )
        uses(
          name = "Run refreshVersions on sample-kotlin-js",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-kotlin-js/gradlew",
              "build-root-directory" to "sample-kotlin-js",
              "arguments" to "refreshVersions --stacktrace",
            )
          ),
          condition =
              expr("github.event.inputs.sample-kotlin-js == 'true' && github.event.inputs.run-refreshVersions-task == 'true'"),
        )
        uses(
          name = "Check sample-kotlin-js",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-kotlin-js/gradlew",
              "build-root-directory" to "sample-kotlin-js",
              "arguments" to "check --stacktrace",
            )
          ),
          condition = expr("github.event.inputs.sample-kotlin-js == 'true'"),
        )
        uses(
          name = "Run refreshVersions on sample-android",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-android/gradlew",
              "build-root-directory" to "sample-android",
              "arguments" to "refreshVersions --stacktrace",
            )
          ),
          condition =
              expr("github.event.inputs.sample-android == 'true' && github.event.inputs.run-refreshVersions-task == 'true'"),
        )
        uses(
          name = "Check sample-android",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "gradle-executable" to "sample-android/gradlew",
              "build-root-directory" to "sample-android",
              "arguments" to "check --stacktrace",
            )
          ),
          condition = expr("github.event.inputs.sample-android == 'true'"),
        )
      }

    }
