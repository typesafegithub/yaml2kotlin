
package expected

import io.github.typesafegithub.workflows.domain.Workflow

val allWorkflows: List<Workflow> = listOf(
     workflowGetblogposts,
   workflowNodedeploy,
   workflowNodejspackage,
   workflowGeneratewrappers,
   workflowE2etests,
   workflowHelloworld,
   workflowUpdatearticles,
   workflowRefreshversionsbuild,
   workflowAlltriggers,
   workflowGeneratedsource,
   workflowRefreshversionspr,
   workflowRefreshversionswebsite,
   workflowUpdategradlewrapper,
   workflowDockerimage,
   workflowAutoassignpr,
   workflowDeployenvironments,
   workflowSemanticrelease
)
