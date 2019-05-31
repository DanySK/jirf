plugins {
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
    id("org.danilopianini.git-sensitive-semantic-versioning") version Versions.org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin
    `java-library`
    jacoco
    id("com.github.spotbugs") version Versions.com_github_spotbugs_gradle_plugin
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

spotbugs {
    effort = "max"
    reportLevel = "low"
    val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
    if (excludeFile.exists()) {
        excludeFilterConfig = project.resources.text.fromFile(excludeFile)
    }
}