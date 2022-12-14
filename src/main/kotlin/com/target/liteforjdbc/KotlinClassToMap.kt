package com.target.liteforjdbc

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

typealias NameTransformer = (name: String) -> String

private fun NO_OP(name: String) = name

private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
private val snakeRegex = "_[a-zA-Z]".toRegex()

fun camelToSnakeCase(name: String) = camelRegex.replace(name) {
    "_${it.value}"
}.lowercase()

fun Any.propertiesToMap(exclude: Collection<String> = emptyList(), nameTransformer: NameTransformer = ::NO_OP): Map<String, Any?> {
    val props = this::class.memberProperties
        .filter {
            it.name !in exclude
        }.map {
            it.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            it as KProperty1<Any, Any?>
        }
    return props.associateBy({ nameTransformer(it.name) }, { it.get(this) })
}
