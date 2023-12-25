package ru.dmitriyt.mappergenerator

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toKmClass
import kotlinx.metadata.isNullable
import ru.dmitriyt.mappergenerator.ext.fields
import ru.dmitriyt.mappergenerator.ext.getGenericFirstType
import ru.dmitriyt.mappergenerator.ext.isBoolean
import ru.dmitriyt.mappergenerator.ext.isDateOrTime
import ru.dmitriyt.mappergenerator.ext.isDouble
import ru.dmitriyt.mappergenerator.ext.isFloat
import ru.dmitriyt.mappergenerator.ext.isInt
import ru.dmitriyt.mappergenerator.ext.isList
import ru.dmitriyt.mappergenerator.ext.isLong
import ru.dmitriyt.mappergenerator.ext.isPrimitive
import ru.dmitriyt.mappergenerator.ext.isString
import ru.dmitriyt.mappergenerator.ext.packageName
import ru.dmitriyt.mappergenerator.model.MemberMaybeImport
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

/**
 * Процессор для создания мапперов
 * Поддерживает примитивы, java.time, List, кастомные классы
 * Поддерживает nullable поля и добавляет дефолтные значения
 * Классы java.time не дефолтит
 * Мапперы генерируются для нуллейбл классов
 * List не допускает внутри себя null элементы в любом случае путем фильтрации (не дефолта)
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@OptIn(KotlinPoetMetadataPreview::class, DelicateKotlinPoetApi::class)
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
                    members.filter { it.needImport }.forEachIndexed { index, memberMaybeImport ->
                        addAliasedImport(memberMaybeImport.memberName, "alias${index}ToDomain")
                    }
                }
                .addFunction(mapperExtensionMethod)
                .build()
                .writeTo(file)
        }

        return true
    }

    /**
     * @return тело конструктора результирующего класса маппинга
     */
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
     * @return строка в конструкторе для поля [element]
     */
    private fun getConstructorRow(element: Element): String {
        return """${element.simpleName} = this?.${element.simpleName}${getConstructorRowSuffix(element)}"""
    }

    /**
     * @param element Element.kind = FIELD
     * @return суффикс который нужно добавить к полю при его вставке в конструктор результирующего класса
     */
    private fun getConstructorRowSuffix(element: Element): String {
        val metadata = element.enclosingElement.getAnnotation(Metadata::class.java)
        val isNullable = metadata.toKmClass().properties
            .find { it.name == element.simpleName.toString() }
            ?.returnType
            ?.isNullable == true
        val type = element.asType()
        val isNotNull = !isNullable

        return getMapperFunctionValue(type, isNotNull)
    }

    /**
     * @return суффикс вызова маппера для поля с типом [type] и нуллабельностью [isNotNull]
     */
    private fun getMapperFunctionValue(type: TypeMirror, isNotNull: Boolean): String {
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
            type.isList() -> {
                val genericType = type.getGenericFirstType()
                val notNullPrefix = if (isNotNull) ".orEmpty()" else "?"
                "$notNullPrefix.mapNotNull { it${getMapperFunctionValue(genericType, isNotNull = false)} }"
            }

            else -> if (isNotNull) ".%M()" else "?.%M()"
        }
    }

    /**
     * @param element класс, из которого маппим
     * @return список [MemberName] для экстеншенов маппинга вложенных классов, в том же порядке,
     * в котором в [getConstructorRowSuffix] встречаются "%M"
     */
    private fun getMapperExtensionMembers(element: Element): List<MemberMaybeImport> {
        return element.fields().mapNotNull { field ->
            val type = field.asType()
            getMapperExtensionMemberForType(type, element.packageName)
        }
    }

    /**
     * @return [MemberName] для экстеншена маппера, если он нужен данному [type] поля
     */
    private fun getMapperExtensionMemberForType(
        type: TypeMirror,
        classPackageName: String,
    ): MemberMaybeImport? {
        return when {
            type.isPrimitive() -> null
            type.isDateOrTime() -> null
            type.isList() -> {
                val genericType = type.getGenericFirstType()
                getMapperExtensionMemberForType(genericType, classPackageName)
            }
            else -> {
                val fieldClassElement = processingEnv.typeUtils.asElement(type)
                val memberName = MemberName(fieldClassElement.packageName, "toDomain", true)
                processingEnv.messager.printNote("MEMBER: ${memberName.isExtension}")
                MemberMaybeImport(
                    memberName = memberName,
                    needImport = classPackageName != fieldClassElement.packageName,
                )
            }
        }
    }
}
