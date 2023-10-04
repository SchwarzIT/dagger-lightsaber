package schwarz.it.lightsaber

import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.truth.assertThat
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.Rule

class LightsaberBindingGraphPluginTest {

    private val compiler = KaptKotlinCompiler(*Rule.entries.toTypedArray())

    @Test
    fun emptyComponent() {
        val component = createSource(
            """
                import dagger.Component

                @Component
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation.result).succeeded()
    }

    @Test
    fun emptyComponent_emptyDependencies() {
        val component = createSource(
            """
                import dagger.Component

                @Component(dependencies = [])
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation.result).succeeded()
    }

    @Test
    fun emptyComponent_emptyModule() {
        val component = createSource(
            """
                import dagger.Component

                @Component(modules = [])
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation.result).succeeded()
    }

    @Test
    fun emptyComponent_emptyModuleAndDependencies() {
        val component = createSource(
            """
                import dagger.Component

                @Component(modules = [], dependencies = [])
                interface MyComponent
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation.result).succeeded()
    }
}
