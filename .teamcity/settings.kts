import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.jiraCloudIntegration
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.FTPUpload
import jetbrains.buildServer.configs.kotlin.buildSteps.ftpUpload
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.projectFeatures.jira
import jetbrains.buildServer.configs.kotlin.projectFeatures.spaceConnection
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.05"

project {

    buildType(Build)
    buildType(DeployToFtp)

    features {
        spaceConnection {
            id = "PROJECT_EXT_7"
            displayName = "JetBrains Space"
            serverUrl = "https://huidu.jetbrains.space"
            clientId = "tangww"
            clientSecret = "credentialsJSON:5368a957-452e-4b52-a2b7-260a42201c5a"
        }
        jira {
            id = "PROJECT_EXT_8"
            displayName = "issue"
            host = "https://huiduyzh.atlassian.net/"
            userName = "1245998600@qq.com"
            password = "credentialsJSON:1fec03b4-d20e-4fef-b85f-abfabaa0e14c"
            projectKeys = "SPRIN TES"
            useAutomaticKeys = true
            cloudClientID = "2si5iGOhI6P4J3VkTwgB0wUMNCD7oCCh"
            cloudSecret = "credentialsJSON:506dcadc-bb6e-42b2-bc23-40fee829775b"
        }
    }
}

object Build : BuildType({
    name = "Build"

    artifactRules = """wrapper\maven-wrapper.jar => tes.jar"""

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            jdkHome = "%env.JDK_20_0%"
        }
    }

    features {
        perfmon {
        }
        jiraCloudIntegration {
            issueTrackerConnectionId = "PROJECT_EXT_8"
        }
    }
})

object DeployToFtp : BuildType({
    name = "Deploy to FTP"

    enablePersonalBuilds = false
    artifactRules = """wrapper\maven-wrapper.jar"""
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        ftpUpload {
            name = "upload"
            targetUrl = "ftp://192.168.38.39"
            securityMode = FTPUpload.SecurityMode.NONE
            dataChannelProtection = FTPUpload.DataChannelProtectionMode.DISABLE
            authMethod = usernameAndPassword {
                username = "admin"
                password = "credentialsJSON:1ff30b95-a2ea-4819-a009-16140ad62262"
            }
            transferMode = FTPUpload.TransferMode.AUTO
            sourcePath = "tes.jar"
        }
    }

    triggers {
        vcs {
            watchChangesInDependencies = true
            perCheckinTriggering = true
            enableQueueOptimization = false
        }
    }

    features {
        perfmon {
        }
        jiraCloudIntegration {
            issueTrackerConnectionId = "PROJECT_EXT_8"
        }
        commitStatusPublisher {
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:7ee72c64-dd37-4b9d-aef8-65c705dab35c"
                }
            }
        }
    }

    dependencies {
        snapshot(Build) {
        }
    }
})
