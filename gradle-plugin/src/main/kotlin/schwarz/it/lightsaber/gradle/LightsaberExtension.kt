package schwarz.it.lightsaber.gradle

import org.gradle.api.provider.Property

interface LightsaberExtension {
    val emptyComponent: Property<Severity>
    val unusedBindInstance: Property<Severity>
    val unusedBindsAndProvides: Property<Severity>
    val unusedDependencies: Property<Severity>
    val unusedMembersInjectionMethods: Property<Severity>
    val unusedModules: Property<Severity>
}

enum class Severity {
    Error,
    Warning,
    Ignore,
}

internal fun LightsaberExtension.getArguments() = mapOf(
    "Lightsaber.CheckEmptyComponent" to emptyComponent.toArgument(),
    "Lightsaber.CheckUnusedBindInstance" to unusedBindInstance.toArgument(),
    "Lightsaber.CheckUnusedBindsAndProvides" to unusedBindsAndProvides.toArgument(),
    "Lightsaber.CheckUnusedDependencies" to unusedDependencies.toArgument(),
    "Lightsaber.CheckUnusedMembersInjectionMethods" to unusedMembersInjectionMethods.toArgument(),
    "Lightsaber.CheckUnusedModules" to unusedModules.toArgument(),
)

private fun Property<Severity>.toArgument(): Boolean {
    return map { severity: Severity ->
        when (severity) {
            Severity.Error -> true
            Severity.Warning -> true
            Severity.Ignore -> false
        }
    }.get()
}
