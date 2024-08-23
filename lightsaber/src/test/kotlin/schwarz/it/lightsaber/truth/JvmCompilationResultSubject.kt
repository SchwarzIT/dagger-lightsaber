@file:OptIn(ExperimentalCompilerApi::class)

package schwarz.it.lightsaber.truth

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

fun assertThat(actual: JvmCompilationResult?): JvmCompilationResultSubject {
    return Truth.assertAbout(JvmCompilationResultSubject.KotlinCompilationResultSubjectFactory).that(actual)
}

class JvmCompilationResultSubject(
    metadata: FailureMetadata,
    private val actual: JvmCompilationResult?,
) : Subject(metadata, actual) {

    object KotlinCompilationResultSubjectFactory : Factory<JvmCompilationResultSubject, JvmCompilationResult> {
        override fun createSubject(
            metadata: FailureMetadata,
            actual: JvmCompilationResult?,
        ): JvmCompilationResultSubject {
            return JvmCompilationResultSubject(metadata, actual)
        }
    }

    fun succeeded() {
        if (actual!!.exitCode != KotlinCompilation.ExitCode.OK) {
            failWithoutActual(Fact.simpleFact("The compilation failed with message:\n${actual.messages}"))
        }
    }
}
