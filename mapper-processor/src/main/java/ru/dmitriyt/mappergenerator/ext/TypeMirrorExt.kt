package ru.dmitriyt.mappergenerator.ext

import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

fun TypeMirror.isDateOrTime(): Boolean {
    return toString().let {
        it == "java.time.LocalDate" ||
            it == "java.time.LocalDateTime" ||
            it == "java.time.OffsetDateTime" ||
            it == "java.time.ZonedDateTime"
    }
}

fun TypeMirror.getGenericFirstType(): TypeMirror {
    return (this as DeclaredType).typeArguments.first()
}

fun TypeMirror.isList(): Boolean {
    return toString().let { it.startsWith("java.util.List<") && it.endsWith(">") }
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