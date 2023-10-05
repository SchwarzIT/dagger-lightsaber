package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.FindingInfo
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.KspKotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertHasFindings
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.extension

internal class UnusedBindsAndProvidesKtTest {

    @ParameterizedTest
    @CsvSource("kapt,17,55", "ksp,10,")
    fun bindsNotUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component(modules = [MyModule::class])
                interface MyComponent {
                    fun myInts(): ArrayList<Int>
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                
                @Module
                abstract class MyModule {
                    @Binds
                    abstract fun bindsMyInts(impl: ArrayList<Int>): List<Int>
                    
                    companion object {
                        @Provides
                        fun providesMyInts(): ArrayList<Int> {
                            return ArrayList()
                        }
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertUnusedBindsAndProvides(
            message = "The @Binds `bindsMyInts` declared on `test.MyModule` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,23,35", "ksp,15,")
    fun providesNotUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component(modules = [MyModule::class])
                interface MyComponent {
                    fun myInts(): ArrayList<Int>
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                
                @Module
                object MyModule {
                    @Provides
                    fun providesMyInts(): ArrayList<Int> {
                        return ArrayList()
                    }
                    
                    @Provides
                    fun providesMyString(): String {
                        return "Hello there!"
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, module)

        compilation.assertUnusedBindsAndProvides(
            message = "The @Provides `providesMyString` declared on `test.MyModule` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,23,35", "ksp,15,")
    fun providesNotUsedReportedOnSubcomponent(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {                
                    fun subcomponent(): MySubcomponent
                }
            """.trimIndent(),
        )
        val subcomponent = createSource(
            """
                package test

                import dagger.Subcomponent

                @Subcomponent(modules = [MyModule::class])
                interface MySubcomponent {
                    fun myInts(): ArrayList<Int>
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                
                @Module
                object MyModule {
                    @Provides
                    fun providesMyInts(): ArrayList<Int> {
                        return ArrayList()
                    }
                    
                    @Provides
                    fun providesMyString(): String {
                        return "Hello there!"
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, module)

        compilation.assertUnusedBindsAndProvides(
            message = "The @Provides `providesMyString` declared on `test.MyModule` is not used.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt,17,57,23,35", "ksp,10,,15,")
    fun providesNotUsedReportedOnSubcomponent2(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line1: Int,
        column1: Int?,
        line2: Int,
        column2: Int?,
    ) {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {                
                    fun subcomponent(): MySubcomponent
                }
            """.trimIndent(),
        )
        val subcomponent = createSource(
            """
                package test

                import dagger.Subcomponent

                @Subcomponent(modules = [MyModule::class])
                interface MySubcomponent {
                    fun myInts(): ArrayList<Int>
                }
            """.trimIndent(),
        )
        val component2 = createSource(
            """
                package test
                
                import dagger.Component
                
                @Component(modules = [MyModule::class])
                interface MyComponent2 { 
                    fun myString(): String
                }
            """.trimIndent(),
        )
        val module = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                
                @Module
                object MyModule {
                    @Provides
                    fun providesMyInts(): ArrayList<Int> {
                        return ArrayList()
                    }
                    
                    @Provides
                    fun providesMyString(): String {
                        return "Hello there!"
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent, component2, module)

        compilation.assertHasFindings(
            FindingInfo(
                message = "The @Provides `providesMyInts` declared on `test.MyModule` is not used.",
                line = line1,
                column = column1,
                ruleName = "UnusedBindsAndProvides",
                fileName = compilation.sourcesDir.resolve("test/MyModule.${compilation.type.extension}").toString(),
            ),
            FindingInfo(
                message = "The @Provides `providesMyString` declared on `test.MyModule` is not used.",
                line = line2,
                column = column2,
                ruleName = "UnusedBindsAndProvides",
                fileName = compilation.sourcesDir.resolve("test/MyModule.${compilation.type.extension}").toString(),
            ),
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun componentWithInterface(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test
                
                import dagger.Component
                
                @Component(modules = [MyModule::class])
                interface MyComponent : MyComponentInterface
            """.trimIndent(),
        )

        val componentInterface = createSource(
            """
                package test
                
                
                interface MyComponentInterface {
                    fun myInts(): ArrayList<Int>
                }
            """.trimIndent(),
        )

        val module = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                
                @Module
                object MyModule {
                    @Provides
                    fun providesMyInts(): ArrayList<Int> {
                        return ArrayList()
                    }                
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, componentInterface, module)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt,17,55", "ksp,10,")
    fun bindsNotUsedInAChildModule(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component(modules = [MyModuleA::class])
                interface MyComponent {
                    fun myInts(): ArrayList<Int>
                }
            """.trimIndent(),
        )
        val moduleB = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                
                @Module
                abstract class MyModuleB {
                    @Binds
                    abstract fun bindsMyInts(impl: ArrayList<Int>): List<Int>

                    companion object {                    
                        @Provides
                        fun providesMyInts(): ArrayList<Int> {
                            return ArrayList()
                        }
                    }
                }
            """.trimIndent(),
        )

        val moduleA = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                
                @Module(includes = [MyModuleB::class])
                abstract class MyModuleA
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA, moduleB)

        compilation.assertUnusedBindsAndProvides(
            message = "The @Binds `bindsMyInts` declared on `test.MyModuleB` is not used.",
            line = line,
            column = column,
            fileName = "test/MyModuleB",
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun testMultibinding(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val component = createSource(
            """
                package test
                
                import dagger.Component
                
                @Component(modules = [MyModuleA::class])
                interface MyComponent {
                    fun myInts(): Set<Number>
                }
            """.trimIndent(),
        )
        val moduleA = createSource(
            """
                package test
                
                import dagger.Binds
                import dagger.Module
                import dagger.Provides
                import dagger.multibindings.IntoSet
                                
                @Module
                abstract class MyModuleA {
                        
                    @Binds
                    @IntoSet
                    abstract fun bindLong(impl: Int): Number
                    
                    @Binds
                    @IntoSet
                    abstract fun bindLong2(impl: Long): Number
                    
                    companion object {
                        @Provides
                        fun providesMyInt(): Int {
                            return 1
                        }
                        
                        @Provides
                        fun providesMyLong2(): Long {
                            return 2L
                        }
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, moduleA)

        compilation.assertNoFindings()
    }

    private class CompilerArgumentConverter : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            source as String
            return when (source) {
                "kapt" -> KaptKotlinCompiler(Rule.UnusedBindAndProvides)
                "ksp" -> KspKotlinCompiler(Rule.UnusedBindAndProvides)
                else -> error("Unknown compiler of type $source")
            }
        }
    }
}

private fun CompilationResult.assertUnusedBindsAndProvides(
    message: String,
    line: Int,
    column: Int?,
    fileName: String = "test/MyModule",
) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        fileName = sourcesDir.resolve("$fileName.${type.extension}").toString(),
        ruleName = "UnusedBindsAndProvides",
    )
}
