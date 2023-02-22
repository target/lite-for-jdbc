package com.target.liteforjdbc

import java.sql.ResultSet
import java.util.UUID

fun ResultSet.getUUID(columnLabel: String): UUID? {
    return getObject(columnLabel, UUID::class.java)
}

fun ResultSet.getUUID(columnIndex: Int): UUID? {
    return getObject(columnIndex, UUID::class.java)
}
