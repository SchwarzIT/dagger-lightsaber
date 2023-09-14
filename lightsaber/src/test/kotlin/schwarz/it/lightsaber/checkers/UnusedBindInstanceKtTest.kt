package schwarz.it.lightsaber.checkers

import com.google.testing.compile.Compilation
import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.assertHasFinding
import schwarz.it.lightsaber.assertNoFindings
import schwarz.it.lightsaber.createCompiler
import schwarz.it.lightsaber.createSource

internal class UnusedBindInstanceKtTest {

    private val compiler = createCompiler(checkUnusedBindInstance = true)

    @Test
    fun bindInstanceNotUsed_Factory() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                
                @Component
                public interface MyComponent {
                    
                    @Component.Factory
                    public interface Factory {
                        MyComponent create(
                            @BindsInstance int myInt
                        );
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` is not used.",
            line = 12,
            column = 32,
        )
    }

    @Test
    fun bindInstanceNotUsed_Builder() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                
                @Component
                public interface MyComponent {
                    
                    @Component.Builder
                    public interface Builder {
                        MyComponent build();
                        Builder myInt(@BindsInstance int myInt);
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` is not used.",
            line = 12,
            column = 42,
        )
    }

    @Test
    fun bindInstanceIsUsed() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                
                @Component
                public interface MyComponent {
                    
                    int dependency();
                    
                    @Component.Factory
                    public interface Factory {
                        MyComponent create(
                            @BindsInstance int myInt
                        );
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
                package test;
                
                import javax.inject.Named;
                import dagger.BindsInstance;
                import dagger.Component;
                
                @Component
                public interface MyComponent {
                    
                    int dependency();
                    
                    @Component.Factory
                    public interface Factory {
                        MyComponent create(
                            @BindsInstance int myInt,
                            @BindsInstance @Named("secondInt") int secondInt
                        );
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `secondInt` is not used.",
            line = 16,
            column = 52,
        )
    }

    @Test
    fun componentWithBindInstanceAndSubcomponentIsUsed() {
        val component = createSource(
            """
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                
                @Component
                public interface MyComponent {
                    
                    MySubcomponent subcomponent();
                    
                    @Component.Factory
                    public interface Factory {
                        MyComponent create(
                            @BindsInstance int myInt
                        );
                    }
                }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
                package test;                

                import dagger.Subcomponent;
                
                @Subcomponent
                public interface MySubcomponent {
                    int dependency();
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
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                
                @Component
                public interface MyComponent {
                    
                    MySubcomponent.Factory subcomponentFactory();
                }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
                package test;                
                
                import dagger.BindsInstance;
                import dagger.Subcomponent;
                
                @Subcomponent
                public interface MySubcomponent {
                    int dependency();
                                        
                    @Subcomponent.Factory
                    public interface Factory {
                        MySubcomponent create(
                            @BindsInstance int myInt
                        );
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
                package test;
                
                import dagger.BindsInstance;
                import dagger.Component;
                
                @Component
                public interface MyComponent {
                    
                    MySubcomponent.Factory subcomponentFactory();
                }
            """.trimIndent(),
        )

        val subcomponent = createSource(
            """
                package test;                
                
                import dagger.BindsInstance;
                import dagger.Subcomponent;
                
                @Subcomponent
                public interface MySubcomponent {
                
                    @Subcomponent.Factory
                    public interface Factory {
                        MySubcomponent create(
                            @BindsInstance int myInt
                        );
                    }
                }
            """.trimIndent(),
        )

        val compilation = compiler.compile(component, subcomponent)

        compilation.assertUnusedBindInstance(
            message = "The @BindsInstance `myInt` is not used.",
            line = 12,
            column = 32,
            fileName = "test/MySubcomponent.java",
        )
    }
}

private fun Compilation.assertUnusedBindInstance(
    message: String,
    line: Int,
    column: Int,
    fileName: String = "test/MyComponent.java",
) {
    assertHasFinding(message, line, column, fileName, "UnusedBindInstance")
}
