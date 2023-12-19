package ru.dmitriyt.mappergenerator.data.model.product

import ru.dmitriyt.mappergenerator.Mapper
import ru.dmitriyt.mappergenerator.domain.model.product.Product

@Mapper(targetClass = Product::class)
class ApiProduct(
    val hasId: Boolean?,
)