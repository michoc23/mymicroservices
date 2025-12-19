#!groovy
import jenkins.model.*
import hudson.model.*
import hudson.plugins.view.dashboard.*
import org.jenkinsci.plugins.buildmonitor.*

def instance = Jenkins.getInstance()

// Create Dashboard View for microservices
def dashboardView = new Dashboard("Microservices Dashboard")
dashboardView.setDescription("Transport Microservices Pipeline Dashboard")
dashboardView.setIncludeRegex("transport-.*")

// Add dashboard portlets
def buildStatisticsPortlet = new BuildStatisticsPortlet("buildStatistics")
buildStatisticsPortlet.setDisplayName("Build Statistics")

def testResultTrendPortlet = new TestTrendChart("testTrend")
testResultTrendPortlet.setDisplayName("Test Results Trend")

dashboardView.addTopPortlet(buildStatisticsPortlet)
dashboardView.addTopPortlet(testResultTrendPortlet)

// Create Build Monitor View
try {
    def buildMonitorView = new BuildMonitorView(
        "Pipeline Monitor",
        "Real-time pipeline status monitor"
    )
    buildMonitorView.setIncludeRegex("transport-.*")
    buildMonitorView.setDescription("Large screen view for pipeline monitoring")

    instance.addView(buildMonitorView)
    println "✅ Build Monitor View created"
} catch (Exception e) {
    println "⚠️ Build Monitor View not available: ${e.message}"
}

// Create All Pipelines view
def listView = new ListView("All Pipelines")
listView.setDescription("All pipeline jobs")
listView.setIncludeRegex(".*pipeline.*")
listView.setRecurse(true)

// Add views to Jenkins
instance.addView(dashboardView)
instance.addView(listView)

// Set default view
instance.setPrimaryView(dashboardView)

// Save configuration
instance.save()

println "✅ Pipeline views created:"
println "   - Microservices Dashboard (default)"
println "   - Pipeline Monitor"
println "   - All Pipelines"