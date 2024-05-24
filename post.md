# Give me some light...
The Lidl Plus application is composed of over 250 modules which use Dagger for their internal dependency injection and connection between them. By Daggers nature, it is easy to forget to delete an unused `@Provides` inside a `@Module`, and Dagger will never complain about it.

This is a cause of dead code in our project. We did not have any tool (`ktlint`, `detekt`, gradle dependency analysis and checks from IntelliJ) which could help us with that. Also when we are providing an Use Case with `@Provides`, the Use Case and its dependencies are not flagged as dead code, which ends up generating "sub-trees" of dead code on our dependencies graph. Also, as we use Dagger to connect our different modules between them, a lot of times those `@Provides` make us think that two modules depend between them, while in reality it is not necessary.

We want to find a solution for these problems. We started searching for different options, but could not find any tool which was helpful on spotting those problems. So we decided to implement it ourselves! At SCRM we have our Fridays to work on tech innovations and improvements, so we decided to get into work. We researched about Dagger internals, created a Gradle plugin, connected it to different compilation plugins (`ksp`, `kapt`, `javac` annotator processor)... It was an interesting challenge!

And so, Lightsaber was born!  🎉

## Lightsaber
If you know Dagger2 you would also know that many tools around it uses names related with "dagger": `Anvil`, `Scabbard`, etc. So, a tool that finds dead code.. How can we name it? A tool that throw light to dead code.. light... lightsaber! Done! We got the name, one of the most difficult things to do in software development.

Lightsaber is a Dagger 2 plugin that detects unused code in your Modules, Components and Subcomponents.

Currently it has 7 rules that detects the following problems:
- EmptyComponent
- UnusedBindInstance
- UnusedBindsAndProvides
- UnusedDependencies
- UnusedInject
- UnusedMembersInjectionMethods
- UnusedModules

We spent the major part of the development time creating clear and helpful reporting messages (but compared with Dagger it was an easy achievement)

## How can you use it in your project?

It's very easy! Lightsaber comes with a Gradle plugin which configures everything. You just need to add this plugin into each module where you want to use it.

```kotlin
// build.gradle.kts
plugins {
    id("io.github.schwarzit.lightsaber") version "<version>"
}
```

And run the task  `./gradlew lightsaberCheck`.

When you execute it, if Lightsaber detects any kind of dead code in your Dagger configuration, the task will fail showing you an error like:

```
/path/module/com/example/MyComponent.java:6:8: e: The @BindsInstance `myInt` declared in `test.MyComponent` is not used. [UnusedBindInstance]
```

On this example we can see that Lightsaber has detected an unused `@BindsInstance` on line 6, colum 8 in `MyComponent.kt` file which can be removed.

Nowadays, Lightsaber doesn't have the option of automatically fixing the errors, it only generates reports with the errors detected.

Also, if any rule is not detected or you prefer to treat the issues as a warnings instead of errors, you can do it like this:

```
// build.gradle.kts
lightsaber {
  emptyComponent = Severity.Error // Warning or Ignore
  unusedBindInstance = Severity.Error
  unusedBindsAndProvides = Severity.Error
  unusedDependencies = Severity.Error
  unusedInject = Severity.Error
  unusedMembersInjection = Severity.Error
  unusedModules = Severity.Error
}
```
By default all rules are active in error mode.

## Results
By applying Lightsaber in our project, we have obtained these results:

- ~2.000 code lines removed.
- ~5.000 code lines detected after removing all the dead code spotted by Lightsaber. 
- ~70 dependencies between modules (This has helped us to improve our project parallelization and reduce unnecessary recompilations)
- Tool added to our `CI` to avoid new cases.
- Lightsaber is a tool which help us to remove code. We all love to remove code :)
