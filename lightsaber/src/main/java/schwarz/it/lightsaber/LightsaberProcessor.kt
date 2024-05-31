package schwarz.it.lightsaber

import schwarz.it.lightsaber.checkers.UnusedInjectJavac
import schwarz.it.lightsaber.utils.FileGenerator
import java.io.PrintWriter
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
class LightsaberProcessor : AbstractProcessor() {
    private lateinit var config: LightsaberConfig2
    private lateinit var elements: Elements
    private lateinit var fileGenerator: FileGenerator

    private val rules: Set<Pair<String, LightsaberJavacRule>> by lazy {
        buildSet {
            if (config.checkUnusedInject) {
                add("UnusedInject" to UnusedInjectJavac(elements))
            }
        }
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        config = LightsaberConfig2(
            checkUnusedInject = processingEnv.options["Lightsaber.CheckUnusedInject"] != "false",
        )
        println(processingEnv.options["Lightsaber.CheckUnusedInject"])
        elements = processingEnv.elementUtils
        fileGenerator = FileGenerator(processingEnv.filer)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        rules.forEach { (_, rule) -> rule.process(roundEnv) }

        if (roundEnv.processingOver()) {
            val issues = rules
                .flatMap { (name, rule) -> rule.computeFindings().map { Issue(it.codePosition, it.message, name) } }

            if (issues.isNotEmpty()) {
                fileGenerator.createFile("schwarz.it.lightsaber", "javac", "lightsaber")
                    .let(::PrintWriter)
                    .use { writer -> issues.forEach { writer.println(it.getMessage()) } }
            }
        }

        return false
    }
}

interface LightsaberJavacRule {
    fun process(roundEnv: RoundEnvironment)

    fun computeFindings(): List<Finding>
}
