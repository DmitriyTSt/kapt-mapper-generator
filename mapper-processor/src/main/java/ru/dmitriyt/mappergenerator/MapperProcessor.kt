package ru.dmitriyt.mappergenerator

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toKmClass
import kotlinx.metadata.isNullable
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@OptIn(KotlinPoetMetadataPreview::class)
class MapperProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Mapper::class.java.canonicalName)
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        roundEnv?.getElementsAnnotatedWith(Mapper::class.java)?.forEach { outer ->
            val targetClassTypeMirror: TypeMirror = try {
                outer.getAnnotation(Mapper::class.java).targetClass
                null
            } catch (e: MirroredTypeException) {
                e.typeMirror
            } ?: error("${outer.simpleName} target class get error")
            val targetClassElement = processingEnv.typeUtils.asElement(targetClassTypeMirror)

            val file = File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()).apply { mkdir() }
            val members = getMapperExtensionMembers(outer)
            val mapperExtensionMethod = FunSpec.builder("toDomain")
                .receiver(outer.asType().asTypeName().copy(nullable = true))
                .returns(targetClassTypeMirror.asTypeName())
                .addStatement(
                    """
                        return %T(
                            ${getConstructorBody(outer, targetClassElement)}
                        )
                    """.trimIndent(),
                    targetClassTypeMirror,
                    *members.map { it.memberName }.toTypedArray(),
                )
                .build()
            FileSpec.builder(outer.packageName, "${outer.simpleName}Mapper")
                .apply {
                    members.filter { it.needImport }.forEach { memberMaybeImport ->
                        addAliasedImport(memberMaybeImport.memberName, "aliasToDomain")
                    }
                }
                .addFunction(mapperExtensionMethod)
                .build()
                .writeTo(file)
        }

        return true
    }

    private fun getConstructorBody(element: Element, targetElement: Element): String {
        val elementFields = element.fields()
        val targetElementFields = targetElement.fields()
        elementFields.forEachIndexed { index, field ->
            if (field.simpleName != targetElementFields[index].simpleName) {
                error("$element has different fields with $targetElement")
            }
        }
        return targetElementFields.joinToString(",\n                            ") { getConstructorRow(it) }
    }

    /**
     * @param element Element.kind = FIELD
     */
    private fun getConstructorRow(element: Element): String {
        return """${element.simpleName} = this?.${element.simpleName}${getConstructorRowSuffix(element)}"""
    }

    /**
     * Возвращает суффикс который нужно добавить к полю при его вставке в конструктор результирующего класса
     * @param element Element.kind = FIELD
     */
    private fun getConstructorRowSuffix(element: Element): String {
        val metadata = element.enclosingElement.getAnnotation(Metadata::class.java)
        val isNullable = metadata.toKmClass().properties
            .find { it.name == element.simpleName.toString() }
            ?.returnType
            ?.isNullable == true
        val type = element.asType()
        val isNotNull = !isNullable

        val ifNotNull: (String) -> String = { defaultSuffix ->
            if (isNotNull) defaultSuffix else ""
        }
        return when {
            type.isString() -> ifNotNull(" ?: \"\"")
            type.isInt() -> ifNotNull(" ?: 0")
            type.isLong() -> ifNotNull(" ?: 0L")
            type.isFloat() -> ifNotNull(" ?: 0f")
            type.isDouble() -> ifNotNull(" ?: 0.0")
            type.isBoolean() -> ifNotNull(" ?: false")
            type.isDateOrTime() -> ""
            else -> if (isNotNull) ".%M()" else "?.%M()"
        }
    }

    /**
     * Получает список [MemberName] для экстеншенов маппинга вложенных классов, в том же порядке,
     * в котором в [getConstructorRowSuffix] встречаются "%M"
     * @param element source fields
     */
    private fun getMapperExtensionMembers(element: Element): List<MemberMaybeImport> {
        return element.fields().mapNotNull { field ->
            val type = field.asType()
            when {
                type.isPrimitive() -> null
                type.isDateOrTime() -> null
                else -> {
                    val fieldClassElement = processingEnv.typeUtils.asElement(field.asType())
                    val memberName = MemberName(fieldClassElement.packageName, "toDomain", true)
                    processingEnv.messager.printNote("MEMBER: ${memberName.isExtension}")
                    MemberMaybeImport(
                        memberName = memberName,
                        needImport = element.packageName != fieldClassElement.packageName,
                    )
                }
            }
        }
    }

    class MemberMaybeImport(
        val memberName: MemberName,
        val needImport: Boolean,
    )

    fun Element.fields(): List<Element> {
        return enclosedElements.filter { it.kind == ElementKind.FIELD }
    }

    fun TypeMirror.isDateOrTime(): Boolean {
        return toString().let {
            it == "java.time.LocalDate" ||
                it == "java.time.LocalDateTime" ||
                it == "java.time.OffsetDateTime" ||
                it == "java.time.ZonedDateTime"
        }
    }

    fun TypeMirror.isPrimitive(): Boolean {
        return isString() || isInt() || isLong() || isBoolean() || isFloat() || isDouble()
    }

    fun TypeMirror.isString(): Boolean {
        return toString() == "java.lang.String"
    }

    fun TypeMirror.isInt(): Boolean {
        return toString().let { it == "java.lang.Integer" || it == "int" }
    }

    fun TypeMirror.isBoolean(): Boolean {
        return toString().let { it == "java.lang.Boolean" || it == "boolean" }
    }

    fun TypeMirror.isFloat(): Boolean {
        return toString().let { it == "java.lang.Float" || it == "float" }
    }

    fun TypeMirror.isDouble(): Boolean {
        return toString().let { it == "java.lang.Double" || it == "double" }
    }

    fun TypeMirror.isLong(): Boolean {
        return toString().let { it == "java.lang.Long" || it == "long" }
    }

    val Element.packageName: String
        get() = enclosingElement.let { packageElement ->
            packageElement.toString().takeIf { packageElement.kind == ElementKind.PACKAGE && it != "unnamed package" }
        } ?: error("$simpleName packageName is null")

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}