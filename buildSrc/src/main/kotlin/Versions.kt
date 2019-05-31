import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val com_github_spotbugs_gradle_plugin: String = "1.6.9" // available: "2.0.0"

    const val guava: String = "27.1-jre"

    const val ktlint: String = "0.33.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" 

    const val junit: String = "4.12" 

    const val commons_lang3: String = "3.9" 

    const val org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin: String = "0.2.2" 

    const val org_danilopianini_publish_on_central_gradle_plugin: String = "0.1.1" 

    const val boilerplate: String = "0.1.8" // available: "0.2.1"

    const val jgrapht_core: String = "1.1.0" // available: "1.3.0"

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "8.0.0" 

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "5.4.1"

        const val currentVersion: String = "5.4.1"

        const val nightlyVersion: String = "5.6-20190531002906+0000"

        const val releaseCandidate: String = "5.5-rc-1"
    }
}
