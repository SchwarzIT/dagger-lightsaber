package schwarz.it.lightsaber.truth

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.tschuchort.compiletesting.KotlinCompilation

fun assertThat(result: KotlinCompilation.Result): KotlinCompilationResultSubject {
    return Truth.assertAbout(KotlinCompilationResultSubject.KotlinCompilationResultSubjectFactory).that(result)
}

class KotlinCompilationResultSubject(
    metadata: FailureMetadata,
    private val actual: KotlinCompilation.Result,
) : Subject(metadata, actual) {

    object KotlinCompilationResultSubjectFactory : Factory<KotlinCompilationResultSubject, KotlinCompilation.Result> {
        override fun createSubject(
            failureMetadata: FailureMetadata,
            that: KotlinCompilation.Result,
        ): KotlinCompilationResultSubject {
            return KotlinCompilationResultSubject(failureMetadata, that)
        }
    }

    fun succeeded() {
        if (actual.exitCode != KotlinCompilation.ExitCode.OK) {
            failWithoutActual(
                Fact.simpleFact("The compilation failed"),
            )
        }
    }
}
