package expected
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.Workflow
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.yaml.writeToFile
import java.nio.`file`.Paths

public val workflowIntegration13: Workflow = workflow(
      name = "test",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/workflows/integration13.main.kts"),
    ) {
      job(
        id = "test",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        run(
          name = "echo 'Hello!'",
          command = "echo 'Hello!'",
        )
      }

    }