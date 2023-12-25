package ru.dmitriyt.mappergenerator

import ru.dmitriyt.mappergenerator.data.model.ApiStreet
import ru.dmitriyt.mappergenerator.data.model.ApiUser
import ru.dmitriyt.mappergenerator.data.model.city.ApiCity
import ru.dmitriyt.mappergenerator.data.model.product.ApiProduct
import ru.dmitriyt.mappergenerator.data.model.toDomain
import java.time.LocalDate

fun main() {
    val apiUser = ApiUser(
        id = "id",
        name = "name",
        age = 12,
        parent = null,
        street = ApiStreet(null, 1, 1f, 1.0),
        birthday = LocalDate.now(),
        product = listOf(ApiProduct(false)),
        points = listOf(1, null, 3),
        city = ApiCity(null)
    )

    println(apiUser.toDomain())
}