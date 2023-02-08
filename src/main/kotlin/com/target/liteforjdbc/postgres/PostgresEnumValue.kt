package com.target.liteforjdbc.postgres

import com.target.liteforjdbc.DbValue
import com.target.liteforjdbc.IntParameterType
import java.sql.Types

/**
 * Used to pass an Enum parameter into a parameter for a Postgres custom enum type
 */
fun <T: Enum<T>> postgresEnumValue(value: T): DbValue<T> {
    return DbValue(value, IntParameterType(Types.OTHER))
}