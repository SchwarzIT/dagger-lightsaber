# RELEASING

1. Update version in gradle.properties
2. Update CHANGELOG.md
3. Update README.md
4. `git commit -am "Prepare for release X.Y.Z"` (where X.Y.Z is the new version)
5. `./gradlew publish`
6. Visit [Sonatype Nexus](https://s01.oss.sonatype.org/) and promote the artifact.
