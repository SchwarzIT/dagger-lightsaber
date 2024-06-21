package schwarz.it.lightsaber

import schwarz.it.lightsaber.checkers.UnusedInjectJavac
import schwarz.it.lightsaber.checkers.UnusedScopeJavac
import schwarz.it.lightsaber.utils.FileGenerator
import schwarz.it.lightsaber.utils.writeFile
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
class LightsaberJavacProcessor : AbstractProcessor() {
    private lateinit var config: AnnotationProcessorConfig
    private lateinit var elements: Elements
    private lateinit var fileGenerator: FileGenerator

    private val rules: Set<Pair<String, LightsaberJavacRule>> by lazy {
        buildSet {
            if (config.checkUnusedInject) {
                add("UnusedInject" to UnusedInjectJavac(elements))
            }
            if (config.checkUnusedScope) {
                add("UnusedScope" to UnusedScopeJavac(elements))
            }
        }
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        config = AnnotationProcessorConfig(
            checkUnusedInject = processingEnv.options["Lightsaber.CheckUnusedInject"] != "false",
            checkUnusedScope = processingEnv.options["Lightsaber.CheckUnusedScope"] != "false",
        )
        elements = processingEnv.elementUtils
        fileGenerator = FileGenerator(processingEnv.filer)
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
}

interface LightsaberJavacRule {
    fun process(roundEnv: RoundEnvironment)

    fun computeFindings(): List<Finding>
}
