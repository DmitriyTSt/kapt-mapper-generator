package ru.dmitriyt.mappergenerator.domain.model

import java.time.LocalDate

class User(
    val id: String,
    val name: String,
    val birthday: LocalDate?,
)
