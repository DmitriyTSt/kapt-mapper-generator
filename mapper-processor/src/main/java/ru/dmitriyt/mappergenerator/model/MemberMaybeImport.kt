package ru.dmitriyt.mappergenerator.model

import com.squareup.kotlinpoet.MemberName

/**
 * [MemberName] с информацией, нужно ли его импортить (импортить нужно, если он из другого пакета)
 * MemberName нужен для корректной подставновки экстеншенов маппинга
 */
class MemberMaybeImport(
    val memberName: MemberName,
    val needImport: Boolean,
)