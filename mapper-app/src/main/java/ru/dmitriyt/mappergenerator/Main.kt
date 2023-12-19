package ru.dmitriyt.mappergenerator

import ru.dmitriyt.mappergenerator.data.model.ApiStreet
import ru.dmitriyt.mappergenerator.data.model.ApiUser
import ru.dmitriyt.mappergenerator.data.model.product.ApiCity
import ru.dmitriyt.mappergenerator.data.model.product.ApiProduct
import java.time.LocalDate

fun main() {
    val apiUser = ApiUser(
        id = "id",
        name = "name",
        age = 12,
        parent = null,
        street = ApiStreet(null, 1, 1f, 1.0),
        birthday = LocalDate.now(),
        product = ApiProduct(false),
        city = ApiCity(null)
    )

    println(apiUser)
}