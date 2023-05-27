package expected
#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:0.44.0-SNAPSHOT")

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

public val workflowUpdategradlewrappergeneratedYml: Workflow = workflow(
      name = "Update Gradle Wrapper",
      on = listOf(
        Schedule(listOf(
          Cron("0 0 * * *"),
        )),
        WorkflowDispatch(),
        ),
      sourceFile = Paths.get(".github/workflows/updategradlewrappergenerated.yml.main.kts"),
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
          condition = "true",
        )
        run(
          name = "Install Kotlin",
          command = "sudo snap install --classic kotlin",
        )
        run(
          name = "Consistency check",
          command =
              "diff -u '.github/workflows/update-gradle-wrapper.yml' <('.github/workflows/update-gradle-wrapper.main.kts')",
        )
      }

      job(
        id = "update-gradle-wrapper",
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
          name = "Update Gradle Wrapper",
          action = CustomAction(
            actionOwner = "gradle-update",
            actionName = "update-gradle-wrapper-action",
            actionVersion = "v1",
            inputs = emptyMap()),
        )
        uses(
          name = "Latex",
          action = CustomAction(
            actionOwner = "xu-cheng",
            actionName = "latex-action",
            actionVersion = "v2",
            inputs = mapOf(
              "root_file" to "report.tex",
              "compiler" to "latexmk",
            )
          ),
        )
      }

    }

workflowUpdateGradleWrapperGeneratedYml.writeToFile()