# Lightsaber

Lightsaber is a [Dagger 2][dagger] plugin that flags the unused dependencies declared in your `Module`s and `Component`s.

## What to expect

```
/path/module/build/tmp/kapt3/stubs/release/com/example/MyComponent.java:6: error: [Lightsaber] The @BindsInstance `context` is not used.
public abstract interface MyComponent {
                ^
```

This plugin contains 4 rules:
- Unused `@BindInstance`
- Unused `@Provides` or `@Binds` inside `@Module`s
- Unused `@Component(dependencies)`
- Unused `@Module`s

By default, anything flagged by those rules is treated as a compilation error.


## How to use it

Add the plugin to your project:

```kotlin
// build.gradle.kts
plugins {
    id("schwarz.it.lightsaber") version "<version>"
}
```

And run `./gradlew lightsaberCheck`. Lightsaber will check your code and fail if there is any issue.

### Configuration

You can change that default for each rule from error to warnings or even ignore it completely like this:

```kotlin
import schwarz.it.lightsaber.gradle.Severity

lightsaber {
  unusedBindInstance = Severity.Error
  unusedBindsAndProvides = Severity.Error
  unusedDependencies = Severity.Error
  unusedModules = Severity.Error
}
```

*Caution*: The `kapt` gradle plugin has a bug [KT-58326] that if you change its arguments it doesn't re-execute the task. This bug impacts Lightsaber so if you change the configuration for the rules you should execute the compilation with `--rerun-tasks` to ensure that the current configuration is applied.

## How to build it

Clone the repo and execute:

```bash
./gradlew build
```

  [dagger]: https://dagger.dev/
  [KT-58326]: https://youtrack.jetbrains.com/issue/KT-58326
