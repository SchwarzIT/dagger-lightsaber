package schwarz.it.lightsaber.sample

import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(modules = [MyModule::class])
interface MyComponent {
    fun myInt(): Int
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
    }
}
