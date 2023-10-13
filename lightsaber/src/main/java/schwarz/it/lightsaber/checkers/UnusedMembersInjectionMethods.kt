package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.DependencyRequest
import dagger.spi.model.RequestKind
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getElements
import schwarz.it.lightsaber.utils.getFullQualifiedName

internal fun checkUnusedMembersInjectionMethods(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    return bindingGraph.componentNodes().flatMap { component ->
        component.entryPoints().filter {
            it.kind() == RequestKind.MEMBERS_INJECTION
        }.mapNotNull {
            Finding(
                "The members-injection method `${it.getMethodName()}` declared in `${component.getFullQualifiedName()}` is not used. " +
                    "`${it.key()}` doesn't have any variable or method annotated with @Inject.",
                it.requestElement().get().fold(
                    { daggerProcessingEnv.getElements().getCodePosition(it) },
                    { it.location.toCodePosition() },
                ),
            )
        }
    }
}

private fun DependencyRequest.getMethodName(): String {
    return this.requestElement().get().fold(
        { it.simpleName.toString() },
        { (it as KSFunctionDeclaration).simpleName.asString() }
    )
}
