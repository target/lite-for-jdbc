package com.target.liteforjdbc

data class DbValue<T>(
    val value: T,
    val type: ParameterType,
    val precision: Int? = null,
)
