package ru.dmitriyt.mappergenerator.ext

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

/**
 * @return список вложенных элементов с типом [ElementKind.FIELD]
 */
fun Element.fields(): List<Element> {
    return enclosedElements.filter { it.kind == ElementKind.FIELD }
}

/**
 * Возвращает пакет для [Element], если он у него есть как родитель, иначе кидает ошибку
 */
val Element.packageName: String
    get() = enclosingElement.let { packageElement ->
        packageElement.toString().takeIf { packageElement.kind == ElementKind.PACKAGE && it != "unnamed package" }
    } ?: error("${toString()} packageName is null")