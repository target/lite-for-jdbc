package com.target.liteforjdbc

import java.sql.PreparedStatement
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
 * Will set a paraemter
 */
fun PreparedStatement.setParameter(index: Int, arg: Any?) {
    when (arg) {
        is ZonedDateTime -> this.setZonedDateTime(index, arg)
        is Instant -> this.setInstant(index, arg)

        else -> this.setObject(index, arg)
    }
}
