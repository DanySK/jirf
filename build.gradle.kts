import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.danilopianini.git-sensitive-semantic-versioning")
    `java-library`
    jacoco
    id("com.github.spotbugs")
    pmd
    checkstyle
    id("org.jlleitschuh.gradle.ktlint")
//    alias(libs.plugins.java.qa)
//    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.taskTree)
}

repositories {
    mavenCentral()
}

multiJvm {
    jvmVersionForCompilation.set(11)
}

dependencies {
    compileOnly("com.github.spotbugs:spotbugs-annotations:_")
    implementation("com.google.guava:guava:_")
    implementation("org.apache.commons:commons-lang3:_")
    implementation("org.danilopianini:boilerplate:_")
    implementation("org.jgrapht:jgrapht-core:_")
    testImplementation("junit:junit:_")
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
    maxFailures.set(2)
}
checkstyle {
    maxErrors = 0
    maxWarnings = 0
}

group = "org.danilopianini"
publishOnCentral {
    projectDescription.set("An advanced factory supporting implicit type conversions")
    projectLongName.set("Java Implicit Reflective Factory")
    repository("https://maven.pkg.github.com/danysk/jirf") {
        user.set("DanySK")
        password.set(System.getenv("GITHUB_TOKEN"))
    }
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
