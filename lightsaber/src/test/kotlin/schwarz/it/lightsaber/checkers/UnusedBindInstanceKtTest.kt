package schwarz.it.lightsaber.checkers

import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.KaptKotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertNoFindings

internal class UnusedBindInstanceKtTest {

    private val compiler = KaptKotlinCompiler(Rule.UnusedBindInstance)

    @Test
    fun bindInstanceNotUsed_Factory() {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {
                    
                    @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` is not used.",
            line = 15,
            column = 13,
        )
    }

    @Test
    fun bindInstanceNotUsed_Builder() {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {
                    
                    @Component.Builder
                    interface Builder {
                        fun build(): MyComponent
                        fun myInt(@BindsInstance myInt: Int): Builder
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` is not used.",
            line = 18,
            column = 13,
        )
    }

    @Test
    fun bindInstanceIsUsed() {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {
                    
                    fun dependency(): Int
                    
                    @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertNoFindings()
    }

    @Test
    fun bindInstanceNamedIsNotUsed() {
        val component = createSource(
            """
                package test
                
                import javax.inject.Named
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {
                    
                    fun dependency(): Int
                    
                    @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int,
                            @BindsInstance @Named("secondInt") secondInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `secondInt` is not used.",
            line = 19,
            column = 13,
        )
    }

    @Test
    fun componentWithBindInstanceAndSubcomponentIsUsed() {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {
                    
                    fun subcomponent(): MySubcomponent
                    
                    @Component.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MyComponent
                    }
                }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
                package test                

                import dagger.Subcomponent
                
                @Subcomponent
                interface MySubcomponent {
                    fun dependency(): Int
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent)

        compilation.assertNoFindings()
    }

    @Test
    fun bindInstanceInSubcomponentIsUsed() {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {
                    
                    fun subcomponentFactory(): MySubcomponent.Factory
                }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
                package test                
                
                import dagger.BindsInstance
                import dagger.Subcomponent
                
                @Subcomponent
                interface MySubcomponent {
                    fun dependency(): Int
                                        
                    @Subcomponent.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MySubcomponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent)

        compilation.assertNoFindings()
    }

    @Test
    fun bindInstanceInSubcomponentIsNoUsed() {
        val component = createSource(
            """
                package test
                
                import dagger.BindsInstance
                import dagger.Component
                
                @Component
                interface MyComponent {
                    
                    fun subcomponentFactory(): MySubcomponent.Factory
                }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
                package test                
                
                import dagger.BindsInstance
                import dagger.Subcomponent
                
                @Subcomponent
                interface MySubcomponent {
                
                    @Subcomponent.Factory
                    interface Factory {
                        fun create(
                            @BindsInstance myInt: Int
                        ): MySubcomponent
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` is not used.",
            line = 15,
            column = 13,
            fileName = "test/MySubcomponent.java",
        )
    }
}

private fun CompilationResult.assertUnusedBindInstance(
    message: String,
    line: Int,
    column: Int,
    fileName: String = "test/MyComponent.java",
) {
    assertHasFinding(message, line, column, sourcesDir.resolve(fileName).toString(), "UnusedBindInstance")
}
