plugins {
    id("de.fayard.buildSrcVersions") version "0.3.2"
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:[7.0, 22[")
    implementation("org.apache.commons:commons-lang3:[3.0, 4[")
    implementation("org.danilopianini:boilerplate:[0.1.2, 0.2.0[")
    implementation("org.jgrapht:jgrapht-core:1.1.0")
    testImplementation("junit:junit:4.12")
}
