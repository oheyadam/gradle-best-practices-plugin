package com.autonomousapps

import com.autonomousapps.internal.logging.ConfigurableLogger
import com.autonomousapps.task.CheckBestPracticesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

@Suppress("unused")
class GradleBestPracticesPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    pluginManager.withPlugin("java-gradle-plugin") {
      val mainOutput = extensions.getByType(SourceSetContainer::class.java)
        .findByName(SourceSet.MAIN_SOURCE_SET_NAME)
        ?.output
        ?.classesDirs
        ?: files()

      val bestPractices = tasks.register("checkBestPractices", CheckBestPracticesTask::class.java) {
        with(it) {
          classesDirs.setFrom(mainOutput)
          logLevel.set(logLevel())
          output.set(layout.buildDirectory.file("reports/best-practices/check.txt"))
        }
      }

      tasks.named("check").configure {
        it.dependsOn(bestPractices)
      }
    }
  }

  /**
   * `-Dbest-practices-logging=<reporting|debug>` will trigger additional logging and console output.
   *
   * TODO: use an extension instead of a system property.
   */
  private fun Project.logLevel() = providers
    .systemProperty("best-practices-logging")
    .map { logging -> ConfigurableLogger.Level.of(logging) }
    .orElse(ConfigurableLogger.Level.NORMAL)
}
