package ru.dmitriyt.mappergenerator.domain.model

import ru.dmitriyt.mappergenerator.domain.model.product.City
import ru.dmitriyt.mappergenerator.domain.model.product.Product
import java.time.LocalDate

data class User(
    val id: String,
    val name: String,
    val age: Int,
    val parent: User,
    val street: Street,
    val birthday: LocalDate?,
    val product: Product,
    val city: City,
)
