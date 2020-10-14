/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package lt.petuska.npm.publish

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.kotest.matchers.string.shouldNotContainIgnoringCase
import lt.petuska.npm.publish.util.assembleTaskName
import lt.petuska.npm.publish.util.defaultRepoName
import lt.petuska.npm.publish.util.gradleExec
import lt.petuska.npm.publish.util.kotlinJs
import lt.petuska.npm.publish.util.kotlinMpp
import lt.petuska.npm.publish.util.npmRepository
import lt.petuska.npm.publish.util.publishTaskName
import org.gradle.testkit.runner.BuildResult

private fun taskCreationTest(
  kotlinPlugin: String,
  jsTargets: List<String> = listOf(),
  jvmTargets: List<String> = listOf(),
  repositories: List<String> = listOf(defaultRepoName),
  expectedMissingTasks: List<String> = listOf()
) {
  val jsPlugin = kotlinPlugin.equals("js", true)
  fun execute(): BuildResult {
    return gradleExec(
      {
        if (kotlinPlugin.equals("multiplatform", true)) {
          kotlinMpp {
            jsTargets.forEach { target ->
              "js"(target) {
                "browser"()
              }
              "sourceSets" {
                "named"("${target}Main") {
                  "dependencies" {
                    "implementation"(arg { "devNpm"("axios", "*") })
                    "api"(arg { "npm"("snabbdom", "*") })
                  }
                }
              }
            }
            jvmTargets.forEach { target ->
              "jvm"(target)
            }
          }
        } else if (kotlinPlugin.equals("js", true)) {
          kotlinJs {
            "js" {
              "browser"()
            }
            "sourceSets" {
              "named"("main") {
                "dependencies" {
                  "implementation"(arg { "npm"("axios", "*") })
                  "api"(arg { "devNpm"("snabbdom", "*") })
                }
              }
            }
          }
        }
        repositories.forEach {
          npmRepository(it)
        }
      },
      "tasks",
      "--all",
      "--stacktrace"
    )
  }

  val result = execute()

  // Verify the result
  fun verifyRepos(pub: String, contains: Boolean) {
    repositories.forEach {
      if (contains) {
        result.output shouldContainIgnoringCase publishTaskName(pub, it)
      } else {
        result.output shouldNotContainIgnoringCase publishTaskName(pub, it)
      }
    }
  }
  jsTargets.forEach {
    val name = if (jsPlugin) "js" else it
    result.output shouldContainIgnoringCase assembleTaskName(name)
    verifyRepos(name, true)
  }
  jvmTargets.forEach {
    result.output shouldNotContainIgnoringCase assembleTaskName(it)
    verifyRepos(it, false)
  }
  expectedMissingTasks.forEach {
    result.output shouldNotContainIgnoringCase it
  }
}

class NpmPublishPluginFunctionalTest : WordSpec(
  {
    "Applying Plugin" should {
      "Create tasks for Kotlin/multiplatform given [single JS target]" {
        taskCreationTest(
          "multiplatform",
          jsTargets = listOf("js")
        )
      }

      "Not create tasks for Kotlin/multiplatform given [single JVM target]" {
        taskCreationTest(
          "multiplatform",
          jvmTargets = listOf("jvm"),
          expectedMissingTasks = listOf(
            assembleTaskName("js"),
            publishTaskName("js")
          )
        )
      }
      "Not create tasks for Kotlin/multiplatform given [multiple JS targets and single JVM target]" {
        taskCreationTest(
          "multiplatform",
          jsTargets = listOf("jsOne", "jsTwo"),
          jvmTargets = listOf("jvm"),
          expectedMissingTasks = listOf(
            assembleTaskName("js"),
            publishTaskName("js")
          )
        )
      }
      "Create tasks for Kotlin/JS given [default JS target]" {
        taskCreationTest(
          "js",
          jsTargets = listOf("jsOne")
        )
      }
      "Not create tasks for no kotlin plugin" {
        taskCreationTest(
          "",
          repositories = listOf(),
          expectedMissingTasks = listOf(
            assembleTaskName("js"),
            publishTaskName("js")
          )
        )
      }
    }
  }
)
