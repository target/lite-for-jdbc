package com.target.liteforjdbc

import java.sql.PreparedStatement
import java.time.*

/**
 * Sets all parameters provided in order, using setObject
 *
 * @param args vararg list of all parameters to be set in order
 */
fun PreparedStatement.setParameters(vararg args: Any?) {
    for ((i, arg) in args.withIndex()) {
        val statementIndex = i + 1
        setParameter(statementIndex, arg)
    }
}

/**
 * Set the parameter as an OffsetDateTime as per the JDBC recommendation.
 */
fun PreparedStatement.setZonedDateTime(index: Int, dateTime: ZonedDateTime) {
    setObject(index, dateTime.toOffsetDateTime())
}

/**
 * Sets instant by converting it to LocalDateTime, assuming the timezone is UTC.
 */
fun PreparedStatement.setInstant(index: Int, instant: Instant) {
    setObject(index, LocalDateTime.ofInstant(instant, ZoneOffset.UTC))
}

/**
 * Sets LocalDateTime parameter
 */
fun PreparedStatement.setLocalDateTime(index:Int, value: LocalDateTime) {
    setObject(index, value)
}

/**
 * Sets LocalDate parameter
 */
fun PreparedStatement.setLocalDate(index:Int, value: LocalDate) {
    setObject(index, value)
}

/**
 * Sets LocalTime parameter
 */
fun PreparedStatement.setLocalTime(index:Int, value: LocalTime) {
    setObject(index, value)
}

/**
 * Sets OffsetDateTime parameter
 */
fun PreparedStatement.setOffsetDateTime(index:Int, value: OffsetDateTime) {
    setObject(index, value)
}

/**
 * Sets OffsetTime parameter
 */
fun PreparedStatement.setOffsetTime(index:Int, value: OffsetTime) {
    setObject(index, value)
}

/**
 * Sets a DbValue on the by using setObject and the corresponding values from DbValue
 */
fun <T> PreparedStatement.setDbValue(index: Int, dbValue: DbValue<T>) {
    when (dbValue.type) {
        is SqlParameterType -> {
            if (dbValue.precision == null) {
                setObject(index, dbValue.value, dbValue.type.sqlType)
            } else {
                setObject(index, dbValue.value, dbValue.type.sqlType, dbValue.precision)
            }
        }
        is IntParameterType -> {
            if (dbValue.precision == null) {
                setObject(index, dbValue.value, dbValue.type.intType)
            } else {
                setObject(index, dbValue.value, dbValue.type.intType, dbValue.precision)
            }
        }
    }
}


/**
 * Will set a paraemter
 */
fun PreparedStatement.setParameter(index: Int, arg: Any?) {
    when (arg) {
        is ZonedDateTime -> this.setZonedDateTime(index, arg)
        is Instant -> this.setInstant(index, arg)
        is Enum<*> -> this.setString(index, arg.name)
        is DbValue<*> -> this.setDbValue(index, arg)

        else -> this.setObject(index, arg)
    }
}
