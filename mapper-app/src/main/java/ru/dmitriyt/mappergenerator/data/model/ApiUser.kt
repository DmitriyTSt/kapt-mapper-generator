package ru.dmitriyt.mappergenerator.data.model

import ru.dmitriyt.mappergenerator.Mapper
import ru.dmitriyt.mappergenerator.data.model.city.ApiCity
import ru.dmitriyt.mappergenerator.data.model.product.ApiProduct
import ru.dmitriyt.mappergenerator.domain.model.User
import java.time.LocalDate

@Mapper(User::class)
data class ApiUser(
    val id: String?,
    val name: String?,
    val age: Int?,
    val parent: ApiUser?,
    val street: ApiStreet?,
    val birthday: LocalDate?,
    val product: List<ApiProduct?>?,
    val points: List<Int?>?,
    val city: ApiCity?,
)
