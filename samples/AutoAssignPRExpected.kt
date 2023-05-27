package expected
#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:0.44.0-SNAPSHOT")

import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Issues
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.Any
import kotlin.collections.mapOf

public val workflowAutoassignprgeneratedYml: Workflow = workflow(
      name = "auto-assign pr",
      on = listOf(
        Issues(
          _customArguments = mapOf(
            "types" to Any("opened")
          ),
        ),
        ),
      sourceFile = Paths.get(".github/workflows/autoassignprgenerated.yml.main.kts"),
    ) {
      job(
        id = "auto-assign",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "Auto-assign PR",
          action = CustomAction(
            actionOwner = "pozil",
            actionName = "auto-assign-issue",
            actionVersion = "v1",
            inputs = mapOf(
              "assignees" to "deraowl",
              "repo-token" to "${'$'}{{ secrets.GITHUB_TOKEN }}",
            )
          ),
        )
      }

    }

workflowAutoAssignPRGeneratedYml.writeToFile()