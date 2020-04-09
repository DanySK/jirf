import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.danilopianini.git-sensitive-semantic-versioning")
    `java-library`
    jacoco
    id("com.github.spotbugs")
    pmd
    checkstyle
    id("org.jlleitschuh.gradle.ktlint")
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central")
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
    setEffort("max")
    setReportLevel("low")
    showProgress.set(true)
    val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
    if (excludeFile.exists()) {
        excludeFilter.set(excludeFile)
    }
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports {
        create("html") { enabled = true }
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
    projectDescription.set("An advanced factory supporting implicit type conversions")
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
