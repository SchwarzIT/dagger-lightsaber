package schwarz.it.lightsaber

import schwarz.it.lightsaber.checkers.UnusedInjectJavac
import schwarz.it.lightsaber.checkers.UnusedScopesJavac
import schwarz.it.lightsaber.utils.FileGenerator
import schwarz.it.lightsaber.utils.writeFile
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class LightsaberJavacProcessor : AbstractProcessor() {
    private lateinit var fileGenerator: FileGenerator
    private lateinit var rules: Set<Pair<String, LightsaberJavacRule>>

    override fun init(processingEnv: ProcessingEnvironment) {
        fileGenerator = FileGenerator(processingEnv.filer)
        val elements = processingEnv.elementUtils
        rules = buildSet {
            if (processingEnv.options["Lightsaber.CheckUnusedInject"] != "false") {
                add("UnusedInject" to UnusedInjectJavac(elements))
            }
            if (processingEnv.options["Lightsaber.CheckUnusedScopes"] != "false") {
                add("UnusedScopes" to UnusedScopesJavac(elements))
            }
        }
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        rules.forEach { (_, rule) -> rule.process(roundEnv) }

        if (roundEnv.processingOver()) {
            val issues = rules
                .flatMap { (name, rule) ->
                    rule.computeFindings()
                        .filterNot { it.suppression.hasSuppress(name) }
                        .map { Issue(it.codePosition, it.message, name) }
                }

            if (issues.isNotEmpty()) {
                fileGenerator.writeFile("javac", issues)
            }
        }

        return false
    }

    override fun getSupportedOptions() = setOf("Lightsaber.CheckUnusedInject", "Lightsaber.CheckUnusedScopes")

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes() = setOf("*")
}

interface LightsaberJavacRule {
    fun process(roundEnv: RoundEnvironment)

    fun computeFindings(): List<Finding>
}
