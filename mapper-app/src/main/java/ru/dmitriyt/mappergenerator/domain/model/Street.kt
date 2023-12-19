package ru.dmitriyt.mappergenerator.domain.model

data class Street(
    val name: String,
    val house: Int,
    val floor: Float,
    val coord: Double,
)