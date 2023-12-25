package ru.dmitriyt.mappergenerator.data.model.city

import ru.dmitriyt.mappergenerator.Mapper
import ru.dmitriyt.mappergenerator.domain.model.product.City

@Mapper(targetClass = City::class)
class ApiCity(
    val cityName: String?,
)