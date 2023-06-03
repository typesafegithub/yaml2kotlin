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
import kotlin.collections.listOf
import kotlin.collections.mapOf

public val workflowGeneratewrappers: Workflow = workflow(
      name = "Generate wrappers",
      on = listOf(
        Push(
          branchesIgnore = listOf("main"),
        ),
        ),
      sourceFile = Paths.get(".github/workflows/generatewrappers.main.kts"),
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
            command =
                "diff -u '.github/workflows/generate-wrappers.yaml' <('.github/workflows/generate-wrappers.main.kts')",
          )
        }

        job(
          id = "generate-wrappers",
          runsOn = RunnerType.UbuntuLatest,
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
          )
          uses(
            name = "Generate wrappers",
            action = CustomAction(
              actionOwner = "gradle",
              actionName = "gradle-build-action",
              actionVersion = "v1",
              inputs = mapOf(
                "arguments" to ":wrapper-generator:run",
              )
            ),
          )
          uses(
            name = "Check that the library builds fine with newly generated wrappers",
            action = CustomAction(
              actionOwner = "gradle",
              actionName = "gradle-build-action",
              actionVersion = "v2",
              inputs = mapOf(
                "arguments" to "build",
              )
            ),
          )
          run(
            name = "Commit and push",
            command = """
            |git config --global user.email "<>"
            |git config --global user.name "GitHub Actions Bot"
            |git add .
            |git commit --allow-empty -m "Regenerate wrappers (${'$'}GITHUB_SHA)"  # an empty commit explicitly shows that the wrappers are up-to-date
            |git push
            |""".trimMargin(),
          )
        }

    }