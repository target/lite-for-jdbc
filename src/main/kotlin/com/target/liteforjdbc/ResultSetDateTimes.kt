package com.target.liteforjdbc

import java.sql.ResultSet
import java.time.*

fun ResultSet.getInstant(columnLabel: String): Instant? {
    val localDateTime = getLocalDateTime(columnLabel)
    return localDateTime?.toInstant(ZoneOffset.UTC)
}

fun ResultSet.getInstant(columnIndex: Int): Instant? {
    val localDateTime = getLocalDateTime(columnIndex)
    return localDateTime?.toInstant(ZoneOffset.UTC)
}

fun ResultSet.getLocalDateTime(columnLabel: String): LocalDateTime? = getObject(columnLabel, LocalDateTime::class.java)

fun ResultSet.getLocalDateTime(columnIndex: Int): LocalDateTime? = getObject(columnIndex, LocalDateTime::class.java)

fun ResultSet.getLocalDate(columnLabel: String): LocalDate? = getObject(columnLabel, LocalDate::class.java)

fun ResultSet.getLocalDate(columnIndex: Int): LocalDate? = getObject(columnIndex, LocalDate::class.java)

fun ResultSet.getLocalTime(columnLabel: String): LocalTime? = getObject(columnLabel, LocalTime::class.java)

fun ResultSet.getLocalTime(columnIndex: Int): LocalTime? = getObject(columnIndex, LocalTime::class.java)

fun ResultSet.getOffsetDateTime(columnLabel: String): OffsetDateTime? = getObject(columnLabel, OffsetDateTime::class.java)

fun ResultSet.getOffsetDateTime(columnIndex: Int): OffsetDateTime? = getObject(columnIndex, OffsetDateTime::class.java)

fun ResultSet.getOffsetTime(columnLabel: String): OffsetTime? = getObject(columnLabel, OffsetTime::class.java)

fun ResultSet.getOffsetTime(columnIndex: Int): OffsetTime? = getObject(columnIndex, OffsetTime::class.java)

fun ResultSet.getZonedDateTime(columnLabel: String): ZonedDateTime? {
    var offsetDateTime = getOffsetDateTime(columnLabel)
    return offsetDateTime?.toZonedDateTime()
}

fun ResultSet.getZonedDateTime(columnIndex: Int): ZonedDateTime? {
    var offsetDateTime = getOffsetDateTime(columnIndex)
    return offsetDateTime?.toZonedDateTime()
}
