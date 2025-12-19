import jenkins.model.*
import hudson.model.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import hudson.plugins.git.*

Jenkins j = Jenkins.instance
String jobName = 'transport-microservices'
String repoUrl = 'file:///workspace/mymicroservices'
String scriptPath = 'Jenkinsfile'

WorkflowJob job = j.getItem(jobName) as WorkflowJob
if (job == null) {
  job = new WorkflowJob(j, jobName)
  j.add(job, jobName)
}

def scm = new GitSCM(repoUrl)
scm.branches = [new BranchSpec('*/master'), new BranchSpec('*/main')]

def defn = new CpsScmFlowDefinition(scm, scriptPath)
defn.setLightweight(true)
job.setDefinition(defn)
job.save()

// Schedule first build
job.scheduleBuild2(0)
println("[init] Ensured pipeline job '${jobName}' exists and scheduled first build.")
