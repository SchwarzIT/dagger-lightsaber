# RELEASING

1. Update version in gradle.properties
2. Update CHANGELOG.md
3. Update README.md
4. `git commit -am "Prepare for release X.Y.Z"` (where X.Y.Z is the new version)
5. `./gradlew publish`
6. Visit [Sonatype Nexus](https://s01.oss.sonatype.org/) and promote the artifact.
7. `git tag -a X.Y.X -m "Version X.Y.Z"` (where X.Y.Z is the new version)
8. `git push --follow-tags`

If step 5 or 6 fails, drop the Sonatype repo, fix the problem, commit, and start again at step 5.
