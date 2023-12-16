package ru.dmitriyt.mappergenerator.data.model

import ru.dmitriyt.mappergenerator.domain.model.User
import ru.dmitriyt.mappergenerator.Mapper
import java.time.LocalDate

@Mapper(User::class)
class ApiUser(
    val id: String?,
    val name: String?,
    val birthday: LocalDate?,
)
