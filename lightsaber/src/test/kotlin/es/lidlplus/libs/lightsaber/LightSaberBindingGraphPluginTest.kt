package es.lidlplus.libs.lightsaber

import com.google.testing.compile.CompilationSubject
import org.junit.jupiter.api.Test

class LightSaberBindingGraphPluginTest {

    private val compiler = createCompiler(
        unusedBindInstance = ReportType.Error,
        unusedBindsAndProvides = ReportType.Error,
        unusedDependencies = ReportType.Error,
        unusedModules = ReportType.Error,
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

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()
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

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()
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

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()
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

        CompilationSubject.assertThat(compilation).succeededWithoutWarnings()
    }
}
