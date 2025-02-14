@file:OptIn(ExperimentalCompilerApi::class)

package schwarz.it.lightsaber

import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.truth.assertThat
import schwarz.it.lightsaber.utils.AbstractCompilerArgumentConverter
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.Rule

internal class LightsaberDaggerProcessorTest {

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun emptyComponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
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

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun emptyComponent_emptyDependencies(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
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

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun emptyComponent_emptyModule(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
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

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun emptyComponent_emptyModuleAndDependencies(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
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

    private class CompilerArgumentConverter : AbstractCompilerArgumentConverter(Rule.UnusedModules)
}
