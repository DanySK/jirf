const publishCmd = `
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md
git push --force origin \${nextRelease.version}
./gradlew uploadJava release || exit 1
./gradlew publishJavaOSSRHPublicationToGithubRepository || true
`
import config from 'semantic-release-preconfigured-conventional-commits' with { type: "json" };
config.plugins.push(
    ["@semantic-release/exec", {
        "publishCmd": publishCmd,
    }],
    "@semantic-release/github",
    "@semantic-release/git",
)
export default config
