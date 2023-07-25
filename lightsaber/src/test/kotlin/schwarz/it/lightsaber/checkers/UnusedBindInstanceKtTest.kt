package schwarz.it.lightsaber.checkers

import com.google.testing.compile.CompilationSubject.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.ReportType
import schwarz.it.lightsaber.createCompiler
import schwarz.it.lightsaber.createSource

internal class UnusedBindInstanceKtTest {

    private val compiler = createCompiler(unusedBindInstance = ReportType.Error)

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

        assertThat(compilation)
            .hadErrorCount(1)
        assertThat(compilation)
            .hadErrorContaining("The @BindsInstance `myInt` is not used.")
            .inFile(component)
            .onLineContaining("interface MyComponent")
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

        assertThat(compilation)
            .hadErrorCount(1)
        assertThat(compilation)
            .hadErrorContaining("The @BindsInstance `myInt` is not used.")
            .inFile(component)
            .onLineContaining("interface MyComponent")
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

        assertThat(compilation)
            .succeededWithoutWarnings()
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

        assertThat(compilation)
            .hadErrorCount(1)
        assertThat(compilation)
            .hadErrorContaining("The @BindsInstance `secondInt` is not used.")
            .inFile(component)
            .onLineContaining("interface MyComponent")
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

        assertThat(compilation)
            .succeededWithoutWarnings()
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

        assertThat(compilation)
            .succeededWithoutWarnings()
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

        assertThat(compilation)
            .hadErrorCount(1)

        assertThat(compilation)
            .hadErrorContaining("The @BindsInstance `myInt` is not used. [test.MyComponent → test.MySubcomponent]")
            .inFile(component)
            .onLineContaining("interface MyComponent")
    }

    @Nested
    internal inner class ReportTypes {
        private val component = createSource(
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

        @Test
        fun testError() {
            val compilation = createCompiler(unusedBindInstance = ReportType.Error)
                .compile(component)

            assertThat(compilation)
                .hadErrorCount(1)
        }

        @Test
        fun testWarning() {
            val compilation = createCompiler(unusedBindInstance = ReportType.Warning)
                .compile(component)

            assertThat(compilation)
                .hadWarningCount(1)
        }

        @Test
        fun testIgnore() {
            val compilation = createCompiler(unusedBindInstance = ReportType.Ignore)
                .compile(component)

            assertThat(compilation)
                .succeededWithoutWarnings()
        }
    }
}
