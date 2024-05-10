package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.symbol.KSName
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.getElements

internal fun checkUnusedInject(
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val ksName = daggerProcessingEnv.resolver().getKSNameFromString("test.Foo")
    val classByName = daggerProcessingEnv.resolver().getClassDeclarationByName(ksName)
    return listOf(Finding("This Inject is unused", classByName!!.location.toCodePosition()))
}
