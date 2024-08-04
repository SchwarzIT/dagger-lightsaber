package schwarz.it.lightsaber.checkers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.AbstractCompilerArgumentConverter
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.FindingInfo
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertHasFindings
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.extension

internal class UnusedScopesKtTest {

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
    @CsvSource("kapt,5,14", "ksp,5,")
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

        compilation.assertUnusedScopes(
            message = "The `@javax.inject.Singleton` scope is unused because `test.Foo` doesn't contain any constructor annotated with `@Inject`.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun withSuppress(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val foo = createSource(
            """
                package test

                import javax.inject.Singleton

                @Singleton 
                @Suppress("UnusedScopes")
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
    @CsvSource("kapt,5,14", "ksp,5,")
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

        compilation.assertUnusedScopes(
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

    @ParameterizedTest
    @CsvSource("kapt,Component", "kapt,Subcomponent", "ksp,Component", "ksp,Subcomponent")
    fun noReportAComponentNorSubcomponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        type: String,
    ) {
        val foo = createSource(
            """
                package test

                import dagger.Component
                import dagger.Subcomponent
                import javax.inject.Inject
                import javax.inject.Singleton

                @$type
                @Singleton
                interface MyComponent {
                    val bar: Bar
                }

                @Singleton class Bar @Inject constructor()
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(foo)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,3,1", "ksp,7,")
    fun noScopeNeededOnComponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val foo = createSource(
            """
                package test

                import dagger.Component
                import javax.inject.Inject
                import javax.inject.Singleton

                @Singleton
                @Component
                interface MyComponent {
                    val foo: Foo
                }

                class Foo @Inject constructor(bar: Bar)

                class Bar @Inject constructor()
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertUnusedScopes(
            "The scope `@javax.inject.Singleton` on component `test.MyComponent` is not used.",
            line,
            column,
            "MyComponent",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,1", "ksp,8,")
    fun noCustomScopeNeededOnComponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val foo = createSource(
            """
                package test

                import dagger.Component
                import javax.inject.Inject
                import javax.inject.Scope
                import javax.inject.Singleton

                @MyAnnotation
                @Component
                interface MyComponent {
                    val foo: Foo
                }

                class Foo @Inject constructor(bar: Bar)

                class Bar @Inject constructor()

                @Scope
                annotation class MyAnnotation
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertUnusedScopes(
            "The scope `@test.MyAnnotation` on component `test.MyComponent` is not used.",
            line,
            column,
            "MyComponent",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,3,1", "ksp,8,")
    fun noMultipleScopeNeededOnComponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val foo = createSource(
            """
                package test

                import dagger.Component
                import javax.inject.Inject
                import javax.inject.Scope
                import javax.inject.Singleton

                @Singleton
                @MyAnnotation
                @Component
                interface MyComponent {
                    val foo: Foo
                }

                class Foo @Inject constructor(bar: Bar)

                class Bar @Inject constructor()

                @Scope
                annotation class MyAnnotation
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertHasFindings(
            compilation.finding(
                "The scope `@javax.inject.Singleton` on component `test.MyComponent` is not used.",
                line,
                column,
            ),
            compilation.finding(
                "The scope `@test.MyAnnotation` on component `test.MyComponent` is not used.",
                line + 1,
                column,
            ),
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun noFindingsOnComponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val foo = createSource(
            """
                package test

                import dagger.Component
                import javax.inject.Inject
                import javax.inject.Scope
                import javax.inject.Singleton

                @Singleton
                @MyAnnotation
                @Component
                interface MyComponent {
                    val foo: Foo
                }

                @Singleton
                class Foo @Inject constructor(bar: Bar)

                @MyAnnotation
                class Bar @Inject constructor()

                @Scope
                annotation class MyAnnotation
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,3,1", "ksp,8,")
    fun onlyOneFinding(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val foo = createSource(
            """
                package test

                import dagger.Component
                import javax.inject.Inject
                import javax.inject.Scope
                import javax.inject.Singleton

                @Singleton
                @MyAnnotation
                @Component
                interface MyComponent {
                    val foo: Foo
                }

                class Foo @Inject constructor(bar: Bar)

                @MyAnnotation
                class Bar @Inject constructor()

                @Scope
                annotation class MyAnnotation
            """.trimIndent(),
        )

        val compilation = compiler.compile(foo)

        compilation.assertUnusedScopes(
            "The scope `@javax.inject.Singleton` on component `test.MyComponent` is not used.",
            line,
            column,
            "MyComponent",
        )
    }

    private class CompilerArgumentConverter : AbstractCompilerArgumentConverter(Rule.UnusedScopes)
}

private fun CompilationResult.assertUnusedScopes(message: String, line: Int, column: Int?, filename: String = "Foo") {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedScopes",
        fileName = sourcesDir.resolve("test/$filename.${type.extension}").toString(),
    )
}

private fun CompilationResult.finding(message: String, line: Int, column: Int?) =
    FindingInfo(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedScopes",
        fileName = sourcesDir.resolve("test/MyComponent.${type.extension}").toString(),
    )
