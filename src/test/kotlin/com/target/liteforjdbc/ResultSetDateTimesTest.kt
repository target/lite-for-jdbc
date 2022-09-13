package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.time.*

class ResultSetDateTimesTest {
    @MockK(relaxed = true)
    lateinit var mockResultSet: ResultSet

    @MockK(relaxed = true)
    lateinit var mockMetaData: ResultSetMetaData

    private val localDateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
    private val localDate = LocalDate.of(1970, 1, 1)
    private val localTime = LocalTime.ofSecondOfDay(0)

    private val offsetDateTime = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        /*
        Interesting Programming Note:
        Because we are testing our get___ methods which are extension to ResultSet, we do not need to
        perform a partial mock using the "callOriginal" method in mockk (see mockk.io for more details).

        Instead, it will already execute our code on the mock class. Effectively acting like a partial mock.
         */
        every { mockResultSet.getObject(1, LocalDateTime::class.java) }.answers { localDateTime }
        every { mockResultSet.getObject("column", LocalDateTime::class.java) }.answers { localDateTime }
        every { mockResultSet.getObject(1, LocalDate::class.java) }.answers { localDate }
        every { mockResultSet.getObject("column", LocalDate::class.java) }.answers { localDate }
        every { mockResultSet.getObject(1, LocalTime::class.java) }.answers { localTime }
        every { mockResultSet.getObject("column", LocalTime::class.java) }.answers { localTime }

        every { mockResultSet.getObject(1, OffsetDateTime::class.java) }.answers { offsetDateTime }
        every { mockResultSet.getObject("column", OffsetDateTime::class.java) }.answers { offsetDateTime }
    }

    @Test
    fun `getInstant index`() {
        val result = mockResultSet.getInstant(1)

        result shouldNotBe null
        result shouldBe Instant.ofEpochMilli(0)
    }

    @Test
    fun `getInstant column name`() {
        val result = mockResultSet.getInstant("column")

        result shouldNotBe null
        result shouldBe Instant.ofEpochMilli(0)
    }


    @Test
    fun `getZonedDateTime index`() {
        val result = mockResultSet.getZonedDateTime(1)

        result shouldNotBe null
        result shouldBe ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `getZonedDateTime column name`() {
        val result = mockResultSet.getZonedDateTime("column")

        result shouldNotBe null
        result shouldBe ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
    }
}
