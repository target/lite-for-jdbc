package com.target.liteforjdbc

import java.sql.SQLType

sealed class ParameterType {
    abstract val sqlType: SQLType?
    abstract val intType: Int?
}

data class SqlParameterType (
    override val sqlType: SQLType
):  ParameterType() {
    override val intType = null
}

data class IntParameterType (
    override val intType: Int
):  ParameterType() {
    override val sqlType = null
}
