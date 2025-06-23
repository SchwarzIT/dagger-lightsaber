package com.example

import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Component(modules = [MyModule::class])
interface MyComponent {
    fun myInt(): Int
    fun foo(): Foo
}

@Module
abstract class MyModule {

    companion object {
        @Provides
        fun myInt(): Int {
            return 42
        }

        @Provides
        fun myLong(): Long {
            return 42L
        }

        @Provides
        fun provideFoo(): Foo = Foo()
    }
}

class Foo @Inject constructor()
