package schwarz.it.lightsaber

import com.google.testing.compile.Compiler
import dagger.internal.codegen.ComponentProcessor

internal fun createCompiler(
    unusedBindInstance: ReportType = ReportType.Ignore,
    unusedBindsAndProvides: ReportType = ReportType.Ignore,
    unusedDependencies: ReportType = ReportType.Ignore,
    unusedModules: ReportType = ReportType.Ignore,
): Compiler {
    return Compiler.javac()
        .withProcessors(ComponentProcessor.forTesting(LightsaberBindingGraphPlugin()))
        .withOptions(
            createOptions(
                unusedBindInstance = unusedBindInstance,
                unusedBindsAndProvides = unusedBindsAndProvides,
                unusedDependencies = unusedDependencies,
                unusedModules = unusedModules,
            ),
        )
}

internal fun createOptions(
    unusedBindInstance: ReportType = ReportType.Ignore,
    unusedBindsAndProvides: ReportType = ReportType.Ignore,
    unusedDependencies: ReportType = ReportType.Ignore,
    unusedModules: ReportType = ReportType.Ignore,
): List<String> {
    return listOf(
        "Lightsaber.UnusedBindInstance" to unusedBindInstance.toOption(),
        "Lightsaber.UnusedBindsAndProvides" to unusedBindsAndProvides.toOption(),
        "Lightsaber.UnusedDependencies" to unusedDependencies.toOption(),
        "Lightsaber.UnusedModules" to unusedModules.toOption(),
    ).map { (first, second) -> "-A$first=$second" } // https://docs.oracle.com/javase/7/docs/technotes/tools/solaris/javac.html
}

private fun ReportType.toOption(): String {
    return when (this) {
        ReportType.Ignore -> "ignore"
        ReportType.Warning -> "warning"
        ReportType.Error -> "error"
    }
}
