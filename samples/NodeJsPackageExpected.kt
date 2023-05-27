package expected
#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:0.44.0-SNAPSHOT")

import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Release
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths
import kotlin.Any
import kotlin.collections.mapOf

public val workflowNodejspackagegeneratedYml: Workflow = workflow(
      name = "NodeJs Package",
      on = listOf(
        Release(
          _customArguments = mapOf(
            "types" to Any("created")
          ),
        ),
        ),
      sourceFile = Paths.get(".github/workflows/nodejspackagegenerated.yml.main.kts"),
      concurrency = Concurrency(group = "workflow_staging_environment", cancelInProgress = false),
    ) {
      job(
        id = "build",
        runsOn = RunnerType.UbuntuLatest,
        concurrency = Concurrency(group = "job_staging_environment", cancelInProgress = false),
      ) {
        uses(
          name = "CheckoutV3",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "checkout",
            actionVersion = "v3",
            inputs = emptyMap()),
        )
        uses(
          name = "SetupNodeV3",
          action = CustomAction(
            actionOwner = "actions",
            actionName = "setup-node",
            actionVersion = "v3",
            inputs = mapOf(
              "node-version" to "12.x",
              "registry-url" to "https://npm.pkg.github.com",
              "scope" to "octocat",
            )
          ),
        )
        run(
          name = "npm install",
          command = "npm install",
        )
        run(
          name = "npm publish",
          command = "npm publish",
          env = linkedMapOf(
            "NODE_AUTH_TOKEN" to "${'$'}",
          ),
        )
      }

    }

workflowNodeJsPackageGeneratedYml.writeToFile()