package ru.dmitriyt.mappergenerator

import ru.dmitriyt.mappergenerator.data.model.ApiUser
import java.time.LocalDate

fun main() {
    val apiUser = ApiUser("id", "name", LocalDate.now())

    println(apiUser)
}