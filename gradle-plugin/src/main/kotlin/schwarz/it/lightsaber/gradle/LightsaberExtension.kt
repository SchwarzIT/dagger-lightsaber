package schwarz.it.lightsaber.gradle

import org.gradle.api.provider.Property

interface LightsaberExtension {
    val emptyComponents: Property<Severity>
    val unusedBindsInstances: Property<Severity>
    val unusedBindsAndProvides: Property<Severity>
    val unusedDependencies: Property<Severity>
    val unusedInject: Property<Severity>
    val unusedMembersInjectionMethods: Property<Severity>
    val unusedModules: Property<Severity>
    val unusedScopes: Property<Severity>
}

enum class Severity {
    Error,
    Warning,
    Ignore,
}

internal fun LightsaberExtension.getArguments() = mapOf(
    "Lightsaber.CheckEmptyComponents" to emptyComponents.toArgument(),
    "Lightsaber.CheckUnusedBindsInstances" to unusedBindsInstances.toArgument(),
    "Lightsaber.CheckUnusedBindsAndProvides" to unusedBindsAndProvides.toArgument(),
    "Lightsaber.CheckUnusedDependencies" to unusedDependencies.toArgument(),
    "Lightsaber.CheckUnusedInject" to unusedInject.toArgument(),
    "Lightsaber.CheckUnusedMembersInjectionMethods" to unusedMembersInjectionMethods.toArgument(),
    "Lightsaber.CheckUnusedModules" to unusedModules.toArgument(),
    "Lightsaber.CheckUnusedScopes" to unusedScopes.toArgument(),
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
