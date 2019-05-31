import com.github.spotbugs.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
    id("org.danilopianini.git-sensitive-semantic-versioning") version Versions.org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin
    `java-library`
    jacoco
    id("com.github.spotbugs") version Versions.com_github_spotbugs_gradle_plugin
    pmd
    checkstyle
}

repositories {
    mavenCentral()
}

gitSemVer {
    version = computeGitSemVer()
}

dependencies {
    implementation(Libs.guava)
    implementation(Libs.commons_lang3)
    implementation(Libs.boilerplate)
    implementation(Libs.jgrapht_core)
    testImplementation(Libs.junit)
}

tasks.withType<Test> {
    failFast = true
    testLogging {
        events("passed", "skipped", "failed", "standardError")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

spotbugs {
    effort = "max"
    reportLevel = "low"
    val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
    if (excludeFile.exists()) {
        excludeFilterConfig = project.resources.text.fromFile(excludeFile)
    }
}
tasks.withType<SpotBugsTask> {
    reports {
        xml.setEnabled(false)
        html.setEnabled(true)
    }
}

pmd {
    ruleSets = listOf()
    ruleSetConfig = resources.text.fromFile("${project.rootProject.projectDir}/config/pmd/pmd.xml")
}
checkstyle {
    maxErrors = 0
    maxWarnings = 0
}