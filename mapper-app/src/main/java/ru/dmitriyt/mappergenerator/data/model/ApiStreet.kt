package ru.dmitriyt.mappergenerator.data.model

import ru.dmitriyt.mappergenerator.Mapper
import ru.dmitriyt.mappergenerator.domain.model.Street

@Mapper(Street::class)
class ApiStreet(
    val name: String?,
    val house: Int?,
    val floor: Float?,
    val coord: Double?,
)