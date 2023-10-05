package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.FindingInfo
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertHasFindings
import schwarz.it.lightsaber.utils.assertNoFindings

class UnusedBindsAndProvidesKtTest {

    private val compiler = KaptKotlinCompiler(Rule.UnusedBindAndProvides)

    @Test
    fun bindsNotUsed() {
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
            line = 17,
            column = 55,
        )
    }

    @Test
    fun providesNotUsed() {
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
            line = 23,
            column = 35,
        )
    }

    @Test
    fun providesNotUsedReportedOnSubcomponent() {
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
            line = 23,
            column = 35,
        )
    }

    @Test
    fun providesNotUsedReportedOnSubcomponent2() {
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
                line = 17,
                column = 57,
                ruleName = "UnusedBindsAndProvides",
                fileName = compilation.sourcesDir.resolve("test/MyModule.java").toString(),
            ),
            FindingInfo(
                message = "The @Provides `providesMyString` declared on `test.MyModule` is not used.",
                line = 23,
                column = 35,
                ruleName = "UnusedBindsAndProvides",
                fileName = compilation.sourcesDir.resolve("test/MyModule.java").toString(),
            ),
        )
    }

    @Test
    fun componentWithInterface() {
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

    @Test
    fun bindsNotUsedInAChildModule() {
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
            line = 17,
            column = 55,
            fileName = "test/MyModuleB.java",
        )
    }

    @Test
    fun testMultibinding() {
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
}

private fun CompilationResult.assertUnusedBindsAndProvides(
    message: String,
    line: Int,
    column: Int,
    fileName: String = "test/MyModule.java",
) {
    assertHasFinding(message, line, column, sourcesDir.resolve(fileName).toString(), "UnusedBindsAndProvides")
}
