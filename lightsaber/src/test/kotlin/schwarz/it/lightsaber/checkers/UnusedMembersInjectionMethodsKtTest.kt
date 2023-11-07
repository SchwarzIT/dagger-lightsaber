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

internal class UnusedMembersInjectionMethodsKtTest {

    @ParameterizedTest
    @CsvSource("kapt,7,26", "ksp,7,")
    fun UnusedMembersInjectionMethodsReportsError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
            package test

            import dagger.Component

            @Component
            interface MyComponent {
                fun inject(str: String)
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedMembersInjectionMethods(
            message = "The members-injection method `inject` declared in `test.MyComponent` is not used. " +
                "`java.lang.String` doesn't have any variable or method annotated with @Inject.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun UnusedMembersInjectionMethodsDoesNotReportError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
            package test

            import dagger.Component
            import dagger.BindsInstance
            import javax.inject.Inject

            @Component
            interface MyComponent {
                fun inject(foo: Foo)
                
                @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myString: String
                        ): MyComponent
                    }
            }

            class Foo {
                @Inject
                lateinit var myInject : String
            }
            """.trimIndent(),

        )

        val compilation = compiler.compile(component)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,10,26", "ksp,10,")
    fun UnusedMembersInjectionWithUsedAndUnusedMethodsReportsError(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
            package test

            import dagger.Component
            import dagger.BindsInstance
            import javax.inject.Inject

            @Component
            interface MyComponent {
                fun inject(foo: Foo)
                fun inject(str: String)
                
                @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myString: String
                        ): MyComponent
                    }
            }

            class Foo {
                @Inject
                lateinit var myInject : String
            }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedMembersInjectionMethods(
            message = "The members-injection method `inject` declared in `test.MyComponent` is not used. " +
                "`java.lang.String` doesn't have any variable or method annotated with @Inject.",
            line = line,
            column = column,
        )
    }

    private class CompilerArgumentConverter : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            source as String
            return when (source) {
                "kapt" -> KaptKotlinCompiler(Rule.UnusedMembersInjectionMethods)
                "ksp" -> KspKotlinCompiler(Rule.UnusedMembersInjectionMethods)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertUnusedMembersInjectionMethods(
    message: String,
    line: Int,
    column: Int?,
    fileName: String = "test/MyComponent",
) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        fileName = sourcesDir.resolve("$fileName.${type.extension}").toString(),
        ruleName = "UnusedMembersInjectionMethods",
    )
}
