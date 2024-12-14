package com.target.liteforjdbc

import java.sql.ResultSet

inline fun <reified T : Enum<T>> ResultSet.getEnum(columnLabel: String): T? {
    val enumName = getString(columnLabel) ?: return null
    return enumValueOf<T>(enumName)
}

inline fun <reified T : Enum<T>> ResultSet.getEnum(columnIndex: Int): T? {
    val enumName = getString(columnIndex) ?: return null
    return enumValueOf<T>(enumName)
}
