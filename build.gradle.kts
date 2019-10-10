import com.github.spotbugs.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    buildSrcVersions
    id("org.danilopianini.git-sensitive-semantic-versioning") version Versions.org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin
    `java-library`
    jacoco
    id("com.github.spotbugs") version Versions.com_github_spotbugs_gradle_plugin
    pmd
    checkstyle
    id("org.jlleitschuh.gradle.ktlint") version Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central") version Versions.org_danilopianini_publish_on_central_gradle_plugin
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

group = "org.danilopianini"
publishOnCentral {
    projectDescription.set("An advanced factory supporting implicit type conversions")"
    projectLongName.set("Java Implicit Reflective Factory")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Danilo Pianini")
                        email.set("danilo.pianini@gmail.com")
                        url.set("http://www.danilopianini.org/")
                    }
                }
            }
        }
    }
}

if (System.getenv("CI") == true.toString()) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}
