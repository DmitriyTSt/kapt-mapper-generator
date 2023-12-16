package ru.dmitriyt.mappergenerator

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Mapper(val targetClass: KClass<*>)
