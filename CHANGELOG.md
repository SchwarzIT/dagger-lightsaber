# CHANGELOG

## Version 0.0.20 (2025-01-17)
- FIX: Don't add `.lightsaber` files inside the classpath (#288)


## Version 0.0.19 (2024-11-22)
- FIX: crash with `NoClassDefFoundError` on projects that doesn't apply the android gradle plugin (#270)


## Version 0.0.18 (2024-09-06)
- ADD: Initial support for Anvil. (If you find any issue with Anvil please report it)
- CHANGE: Improve code position in error messages


## Version 0.0.17 (2024-07-05)
- FIX: UnusedScopes false positives on components with scopes
- ADD: Flag scopes not used on components


## Version 0.0.16 (2024-07-05)
- ADD: New rule UnusedInject
- ADD: New rule UnusedScopes


## Version 0.0.14 (2024-05-24)
- CHANGE: Rename EmptyComponent to EmptyComponents and UnusedBindInstance to UnusedBindsInstances
- ADD: Allow to suppress issues with the annotation `@Suppress`
- FIX: Don't crash when lightsaber can't find the location of an issue


## Version 0.0.13 (2023-12-01)
- ADD: Support android build types and variants on the Gradle Plugin
- FIX: Don't crash on Java 17.


## Version 0.0.12 (2023-11-17)
- FIX: Don't crash if KAPT and KSP Gradle plugins are applied at the same time
- ADD: Support `annotationProcessor` on the Gradle plugin


## Version 0.0.11 (2023-10-20)
Initial release.
