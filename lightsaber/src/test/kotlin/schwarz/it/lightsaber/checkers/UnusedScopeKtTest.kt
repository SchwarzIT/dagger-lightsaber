package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.*
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.KspKotlinCompiler

internal class UnusedScopeKtTest {

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun scopeUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
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

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,4,14", "ksp,6,")
    fun scopeNotUsed(
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

        val module = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class Module {
                    @Provides
                    fun dependency(): Foo {
                        return Foo()
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo, module)

        compilation.assertUnusedScope(
            message = "The `@javax.inject.Singleton` scope is unused.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,4,14", "ksp,4,")
    fun scopeNotUsed2(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {

        val foo = createSource(
            """
                package test

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

        val module = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class Module {
                    @Provides
                    fun dependency(): Foo {
                        return Foo()
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo, annotation, module)

        compilation.assertUnusedScope(
            message = "The `@test.MyAnnotation` scope is unused.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @Disabled("Not yet supported")
    @CsvSource("kapt,4,14", "ksp,6,")
    fun scopeNotUsed3(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val annotation = createSource(
            """
                package test
                
                import javax.inject.Scope

                @Scope
                annotation class MyAnnotation
            """.trimIndent(),
        )

        val fooImpl = createSource(
            """
                package test

                @MyAnnotation
                class FooImpl : Foo
            """.trimIndent(),
        )


        val foo = createSource(
            """
                package test

                interface Foo
            """.trimIndent(),
        )

        val module = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class Module {
                    @Provides
                    fun dependency(): Foo {
                        return FooImpl()
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(annotation, fooImpl, foo, module)

        compilation.assertUnusedScope(
            message = "The `@test.MyAnnotation` scope is unused.",
            line = line,
            column = column,
        )
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
