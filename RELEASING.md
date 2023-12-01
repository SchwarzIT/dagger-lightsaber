# RELEASING

1. Update version in gradle.properties
2. Update CHANGELOG.md
3. Update README.md
4. `git checkout -b release`
5. `git commit -am "Prepare for release X.Y.Z"` (where X.Y.Z is the new version)
6. `./gradlew publish -PRELEASE_SIGNING_ENABLED=true`
7. Visit [Sonatype Nexus](https://s01.oss.sonatype.org/) and promote the artifact.
8. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
9. `git push --follow-tags`
10. [Create PR release to main](https://github.com/SchwarzIT/dagger-lightsaber/compare/release?expand=1)

If step 6 or 7 fails, drop the Sonatype repo, fix the problem, commit, and start again at step 5.
