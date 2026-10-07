# Lightsaber

Lightsaber is a [Dagger 2][dagger] plugin that detects unused code in your `Module`s, `Component`s and `Subcomponent`s

## Works with
- [Dagger 2][dagger] (kapt, ksp and javac annotation processor)
- [Anvil][anvil] (kapt)

## What to expect

```
/path/module/com/example/MyComponent.java:6:8: e: The @BindsInstance `myInt` declared in `test.MyComponent` is not used. [UnusedBindsInstances]
```

This plugin contains several rules:
- Empty `@Component` and `@Subcomponent`
- Unused `@BindsInstance`
- Unused `@Provides` or `@Binds` inside `@Module`s
- Unused `@Component(dependencies)`
- Unused `@Inject`
- Unused member injection methods (`Component.inject(Foo)`)
- Unused `@Module`s
- Unused Scopes (for example: `@Singleton`)

## How to use it

Add the plugin to your project:

```kotlin
// build.gradle.kts
plugins {
    id("io.github.schwarzit.lightsaber") version "0.0.22"
}
```

And run `./gradlew lightsaberCheck`. Lightsaber will check your code and fail if there is any issue.

### Configuration

You can change that default for each rule from error to warnings or even ignore it completely like this:

```kotlin
import schwarz.it.lightsaber.gradle.Severity

lightsaber {
  emptyComponents = Severity.Error
  unusedBindsInstances = Severity.Error
  unusedBindsAndProvides = Severity.Error
  unusedDependencies = Severity.Error
  unusedInjects = Severity.Error
  unusedMembersInjection = Severity.Error
  unusedModules = Severity.Error
  unusedScopes = Severity.Error
}
```

## Without the Gradle plugin

The Gradle plugin is a thin wrapper: it adds the `io.github.schwarzit:lightsaber` artifact to your annotation processor
classpath and reads the findings the processors write. Build systems without a Gradle plugin (Bazel, Maven, ...) can use
the artifact directly and let Lightsaber report its findings as regular compiler diagnostics, so there is no output
directory to collect:

| Option | Value |
| --- | --- |
| `Lightsaber.Report` | `diagnostics` |
| `Lightsaber.Severity.<Rule>` | `error` (default), `warning` or `ignore` |

`<Rule>` is one of `EmptyComponents`, `UnusedBindsInstances`, `UnusedBindsAndProvides`, `UnusedDependencies`,
`UnusedInject`, `UnusedMembersInjectionMethods`, `UnusedModules` or `UnusedScopes`. Pass the options to the processors
that run Lightsaber (Dagger's own processor for the graph rules, plus the KSP or javac processor for `UnusedInject` and
`UnusedScopes`), for example with KSP:

```kotlin
ksp {
    arg("Lightsaber.Report", "diagnostics")
    arg("Lightsaber.Severity.UnusedModules", "warning")
}
```

An `error` finding fails the compilation, a `warning` is printed and the compilation continues. The rules can still be
turned off with `Lightsaber.Check<Rule>=false`.

## Suppress

Lightsaber supports the `@Suppress` annotation. If you want to suppress an issue, you only have to add the `@Suppress` annotation above the reported element with the rule name.
Example: 
```kotlin
@Module
class MyModule {
    @Suppress("UnusedBindsAndProvides")
    @Provides
    fun providesPotatoes() = "potatoes"
}
```

## How to build it

Clone the repo and execute:

```bash
./gradlew build
```

  [dagger]: https://dagger.dev/
  [anvil]: https://github.com/square/anvil
