package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.KspKotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.extension

internal class UnusedScopeKtTest {

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun singletonWithInject_NoErrors(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val foo = createSource(
            """
                package test

                import javax.inject.Inject
                import javax.inject.Singleton

                @Singleton
                class Foo @Inject constructor()
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,5,14", "ksp,6,")
    fun singletonNoInject_Errors(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val foo = createSource(
            """
                package test

                import javax.inject.Singleton

                @Singleton
                class Foo
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertUnusedScope(
            message = "The `@javax.inject.Singleton` scope is unused because `test.Foo` doesn't contain any constructor annotated with `@Inject`.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,5,14", "ksp,6,")
    fun withSuppress(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val foo = createSource(
            """
                package test

                import javax.inject.Singleton

                @Singleton 
                @Suppress("UnusedScope")
                class Foo 
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun customScopeWithInject_NoErrors(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {

        val foo = createSource(
            """
                package test

                import javax.inject.Inject

                @MyAnnotation
                class Foo @Inject constructor()
            """.trimIndent(),
        )

        val annotation = createSource(
            """
                package test

                import javax.inject.Scope
                import javax.inject.Singleton

                @Scope
                annotation class MyAnnotation
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo, annotation)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,5,14", "ksp,6,")
    fun customScopeNoInject_Error(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {

        val foo = createSource(
            """
                package test

                import javax.inject.Inject

                @MyAnnotation
                class Foo
            """.trimIndent(),
        )

        val annotation = createSource(
            """
                package test

                import javax.inject.Scope
                import javax.inject.Singleton

                @Scope
                annotation class MyAnnotation
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo, annotation)

        compilation.assertUnusedScope(
            message = "The `@test.MyAnnotation` scope is unused because `test.Foo` doesn't contain any constructor annotated with `@Inject`.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun noScopeWithInject_NoError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val foo = createSource(
            """
                package test

                import javax.inject.Inject

                class Foo @Inject constructor()
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun noScopeNoInject_NoError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val foo = createSource(
            """
                package test

                class Foo
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertNoFindings()
    }

    private class CompilerArgumentConverter : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            source as String
            return when (source) {
                "kapt" -> KaptKotlinCompiler(Rule.UnusedScope)
                "ksp" -> KspKotlinCompiler(Rule.UnusedScope)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertUnusedScope(message: String, line: Int, column: Int?) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedScope",
        fileName = sourcesDir.resolve("test/Foo.${type.extension}").toString(),
    )
}
