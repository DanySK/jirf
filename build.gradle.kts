import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.java.qa)
    alias(libs.plugins.kotlin.qa)
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
    compileOnly(libs.spotbugs.annotations)
    implementation(libs.boilerplate)
    implementation(libs.guava)
    implementation(libs.commons.lang3)
    implementation(libs.jgrapht.core)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    failFast = true
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed", "standardError")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<Checkstyle>().configureEach {
    javaLauncher = javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) }
}

group = "org.danilopianini"
publishOnCentral {
    repoOwner = "DanySK"
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
