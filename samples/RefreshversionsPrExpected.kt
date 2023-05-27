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

public val workflowRefreshversionsprgeneratedYml: Workflow = workflow(
      name = "RefreshVersions Pr",
      on = listOf(
        Schedule(listOf(
          Cron("0 7 * * 1"),
        )),
        WorkflowDispatch(),
        ),
      sourceFile = Paths.get(".github/workflows/refreshversionsprgenerated.yml.main.kts"),
    ) {
      job(
        id = "Refresh-Versions",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "check-out",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v3",
            inputs = mapOf(
              "ref" to "main",
            )
          ),
        )
        uses(
          name = "setup-java",
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
          name = "create-branch",
          action = CustomAction(
            actionOwner = "peterjgrainger",
            actionName = "action-create-branch",
            actionVersion = "v2.1.0",
            inputs = mapOf(
              "branch" to "dependency-update",
            )
          ),
          env = linkedMapOf(
            "GITHUB_TOKEN" to expr("secrets.GITHUB_TOKEN"),
          ),
        )
        uses(
          name = "gradle refreshVersions",
          action = CustomAction(
            actionOwner = "gradle",
            actionName = "gradle-build-action",
            actionVersion = "v2",
            inputs = mapOf(
              "arguments" to "refreshVersions",
            )
          ),
        )
        uses(
          name = "Commit",
          action = CustomAction(
            actionOwner = "EndBug",
            actionName = "add-and-commit",
            actionVersion = "v9",
            inputs = mapOf(
              "author_name" to "GitHub Actions",
              "author_email" to "noreply@github.com",
              "message" to "Refresh versions.properties",
              "new_branch" to "dependency-update",
              "push" to "--force --set-upstream origin dependency-update",
            )
          ),
        )
        uses(
          name = "Pull Request",
          action = CustomAction(
            actionOwner = "repo-sync",
            actionName = "pull-request",
            actionVersion = "v2",
            inputs = mapOf(
              "source_branch" to "dependency-update",
              "destination_branch" to "main",
              "pr_title" to "Upgrade gradle dependencies",
              "pr_body" to
                  "[refreshVersions](https://github.com/jmfayard/refreshVersions) has found those library updates!",
              "pr_draft" to "true",
              "github_token" to "${'$'}{{ secrets.GITHUB_TOKEN }}",
            )
          ),
        )
      }

    }

workflowRefreshversionsPrGeneratedYml.writeToFile()