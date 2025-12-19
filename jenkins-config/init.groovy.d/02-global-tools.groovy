#!groovy
import jenkins.model.*
import hudson.tools.*
import hudson.plugins.nodejs.*
import hudson.plugins.nodejs.tools.*

def instance = Jenkins.getInstance()

// Configure Maven
def mavenDescriptor = instance.getDescriptorByType(hudson.tasks.Maven.MavenInstallation.DescriptorImpl.class)
def mavenInstaller = new hudson.tasks.Maven.MavenInstaller("3.9.0")
def mavenInstallSourceProperty = new InstallSourceProperty([mavenInstaller])
def mavenInstallation = new hudson.tasks.Maven.MavenInstallation(
    "Maven-3.9.0",
    null,
    [mavenInstallSourceProperty]
)
mavenDescriptor.setInstallations(mavenInstallation)

// Configure Node.js
def nodeDescriptor = instance.getDescriptorByType(NodeJSPlugin.DescriptorImpl.class)
def nodeInstaller = new NodeJSInstaller("18.18.0", "", 100)
def nodeInstallSourceProperty = new InstallSourceProperty([nodeInstaller])
def nodeInstallation = new nodejs.tools.NodeJSInstallation(
    "NodeJS-18",
    null,
    [nodeInstallSourceProperty]
)
nodeDescriptor.setInstallations(nodeInstallation)

// Configure Docker (if docker plugin is available)
try {
    def dockerDescriptor = instance.getDescriptorByType(org.jenkinsci.plugins.docker.commons.tools.DockerTool.DescriptorImpl.class)
    def dockerInstaller = new org.jenkinsci.plugins.docker.commons.tools.DockerTool.DockerInstaller("latest")
    def dockerInstallSourceProperty = new InstallSourceProperty([dockerInstaller])
    def dockerInstallation = new org.jenkinsci.plugins.docker.commons.tools.DockerTool(
        "Docker",
        null,
        [dockerInstallSourceProperty]
    )
    dockerDescriptor.setInstallations(dockerInstallation)
    println "✅ Docker tool configured"
} catch (Exception e) {
    println "⚠️ Docker plugin not available: ${e.message}"
}

// Save configuration
instance.save()

println "✅ Global tools configured:"
println "   - Maven-3.9.0"
println "   - NodeJS-18"
println "   - Docker (latest)"