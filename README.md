# Lightsaber

Lightsaber is a [Dagger 2][dagger] plugin that flags the unused dependencies declared in your `Module`s and `Component`s.

## How to use it

There are two ways to use the plugin:

### as a dependency
```kotlin
// build.gradle.kts
dependencies {
    kapt("com.schwarzit.lightsaber:lightsaber:<version>")
}
```

### as a gradle plugin
```kotlin
// build.gradle.kts
plugins {
    id("com.schwarzit.lightsaber") version "<version>"
}
```

## What to expect
This plugin contains 4 rules:
- Unused `@BindInstance`
- Unused `@Provides` or `@Binds` inside `@Module`s
- Unused `@Component(dependencies)`
- Unused `@Module`s

By default, anything flagged by those rules is treated as a compilation error.

You can change that default behaviour from compilation error to warnings or even ignore them completely.

### as a dependency
```kotlin
kapt {
    arguments {
        arg("LightSaber.UnusedBindInstance", "error") // "warning" or "ignore"
        arg("LightSaber.UnusedBindsAndProvides", "error") // "warning" or "ignore"
        arg("LightSaber.UnusedDependencies", "error") // "warning" or "ignore"
        arg("LightSaber.UnusedModules", "error") // "warning" or "ignore"
    }
}
```

### as a gradle plugin
```kotlin
import es.lidlplus.libs.lightsaber.plugin.Severity

lightsaber {
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
