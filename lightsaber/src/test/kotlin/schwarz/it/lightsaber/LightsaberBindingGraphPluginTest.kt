package schwarz.it.lightsaber

import com.google.testing.compile.CompilationSubject.assertThat
import org.junit.jupiter.api.Test

class LightsaberBindingGraphPluginTest {

    private val compiler = createCompiler(
        checkUnusedBindInstance = true,
        checkUnusedBindsAndProvides = true,
        checkUnusedDependencies = true,
        checkUnusedModules = true,
    )

    @Test
    fun emptyComponent() {
        val component = createSource(
            """
                import dagger.Component;

                @Component
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation).succeededWithoutWarnings()
    }

    @Test
    fun emptyComponent_emptyDependencies() {
        val component = createSource(
            """
                import dagger.Component;

                @Component(dependencies = {})
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation).succeededWithoutWarnings()
    }

    @Test
    fun emptyComponent_emptyModule() {
        val component = createSource(
            """
                import dagger.Component;

                @Component(modules = {})
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation).succeededWithoutWarnings()
    }

    @Test
    fun emptyComponent_emptyModuleAndDependencies() {
        val component = createSource(
            """
                import dagger.Component;

                @Component(modules = {}, dependencies = {})
                public interface MyComponent {
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        assertThat(compilation).succeededWithoutWarnings()
    }
}
