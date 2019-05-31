import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val guava: String = "21.0" // available: "27.1-jre"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" 

    const val junit: String = "4.12" 

    const val commons_lang3: String = "3.9" 

    const val boilerplate: String = "0.1.8" // available: "0.2.1"

    const val jgrapht_core: String = "1.1.0" // available: "1.3.0"

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
