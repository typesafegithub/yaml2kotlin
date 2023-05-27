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
import kotlin.collections.mapOf

public val workflowIntegration08: Workflow = workflow(
      name = "Test workflow",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/workflows/integration08.main.kts"),
    ) {
      job(
        id = "deploy-dev",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "ConfigureAwsCredentialsV2",
          action = CustomAction(
            actionOwner = "aws-actions",
            actionName = "configure-aws-credentials",
            actionVersion = "v2",
            inputs = mapOf(
              "aws-region" to "us-west-1",
              "role-to-assume" to
                  "arn:aws:iam::12345678901234567890:role/github-actions-role/123456789012345678901234567890",
            )
          ),
        )
        uses(
          name = "ConfigureAwsCredentialsV2",
          action = CustomAction(
            actionOwner = "aws-actions",
            actionName = "configure-aws-credentials",
            actionVersion = "v2",
            inputs = mapOf(
              "aws-region" to "us-west-1",
              "role-to-assume" to "arn:aws:iam:::role/github-actions-role/${'$'}{{ github.token }}",
            )
          ),
        )
        uses(
          name = "ConfigureAwsCredentialsV2",
          action = CustomAction(
            actionOwner = "aws-actions",
            actionName = "configure-aws-credentials",
            actionVersion = "v2",
            inputs = mapOf(
              "aws-region" to "us-west-1",
              "role-to-assume" to
                  "arn:aws:iam::1234567890:role/github-actions-role/${'$'}{{ github.token }}",
            )
          ),
        )
        uses(
          name = "ConfigureAwsCredentialsV2",
          action = CustomAction(
            actionOwner = "aws-actions",
            actionName = "configure-aws-credentials",
            actionVersion = "v2",
            inputs = mapOf(
              "aws-region" to "us-west-1",
              "role-to-assume" to
                  "arn:aws:iam::12345678901234567890:role/github-actions-role/${'$'}{{ github.token }}",
            )
          ),
        )
      }

    }