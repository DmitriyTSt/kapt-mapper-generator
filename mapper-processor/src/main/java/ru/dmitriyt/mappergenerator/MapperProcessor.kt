package ru.dmitriyt.mappergenerator

import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

//@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@SupportedOptions(MapperProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class MapperProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Mapper::class.java.canonicalName)
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        roundEnv?.getElementsAnnotatedWith(Mapper::class.java)?.forEach { apiClass ->
            processingEnv.messager.printNote(apiClass)
        }

        val file = File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()).apply { mkdir() }
        FileSpec.builder("ru.dmitriyt.mappergenerator.data.mapper", "ApiUserMapper")
            .build()
            .writeTo(file)
        return true
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}