# Lightsaber

Lightsaber is a [Dagger 2][dagger] plugin that detects unused code in your `Module`s, `Component`s and `Subcomponent`s

## What to expect

```
/path/module/com/example/MyComponent.java:6:8: e: The @BindsInstance `myInt` declared in `test.MyComponent` is not used. [UnusedBindInstance]
```

This plugin contains 5 rules:
- Empty `@Component` and `@Subcomponent`
- Unused `@BindInstance`
- Unused `@Provides` or `@Binds` inside `@Module`s
- Unused `@Component(dependencies)`
- Unused `@Module`s

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
  emptyComponent = Severity.Error
  unusedBindInstance = Severity.Error
  unusedBindsAndProvides = Severity.Error
  unusedDependencies = Severity.Error
  unusedModules = Severity.Error
}
```

## How to build it

Clone the repo and execute:

```bash
./gradlew build
```

  [dagger]: https://dagger.dev/
