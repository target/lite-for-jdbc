package com.target.liteforjdbc

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime


class PreparedStatementSetParametersTest {

    @MockK(relaxed = true)
    lateinit var mockPreparedStatement: PreparedStatement

    private lateinit var preparedStatementProxy: PreparedStatementProxy

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        preparedStatementProxy = PreparedStatementProxy(mockPreparedStatement)
    }

    @Test
    fun testSetParameterForString() {
        val value = "test"
        preparedStatementProxy.setParameter(1, value)

        verify { mockPreparedStatement.setObject(1, value) }
    }

    @Test
    fun testSetParameterForLong() {
        val value = 10L
        preparedStatementProxy.setParameter(1, value)

        verify { mockPreparedStatement.setObject(1, value) }
    }

    @Test
    fun testSetParameterForInt() {
        val value = 10
        preparedStatementProxy.setParameter(1, value)

        verify { mockPreparedStatement.setObject(1, value) }
    }

    @Test
    fun testSetParameterForDouble() {
        val value = 10.0
        preparedStatementProxy.setParameter(1, value)

        verify { mockPreparedStatement.setObject(1, value) }
    }

    @Test
    fun testSetParameterForZonedDateTime() {
        val value = ZonedDateTime.now()
        preparedStatementProxy.setParameter(1, value)

        verify { mockPreparedStatement.setObject(1, value.toOffsetDateTime()) }
    }

    @Test
    fun testSetParameterForInstant() {
        val value = Instant.now()
        preparedStatementProxy.setParameter(1, value)

        verify { mockPreparedStatement.setObject(1, LocalDateTime.ofInstant(value, ZoneOffset.UTC)) }
    }

    @Test
    fun testSetParameterForObject() {
        val value = BigDecimal("10")
        preparedStatementProxy.setParameter(1, value)

        verify { mockPreparedStatement.setObject(1, value) }
    }

    @Test
    fun testSetParameters() {
        val stringVal = "string"
        val longVal = 10L
        val doubleVal = 10.1
        val instantVal = Instant.now()
        val timeVal = ZonedDateTime.now()
        val objectVal = BigDecimal("10.123456")

        preparedStatementProxy.setParameters(stringVal, longVal, doubleVal, instantVal, timeVal, objectVal)

        verify { mockPreparedStatement.setObject(1, stringVal) }
        verify { mockPreparedStatement.setObject(2, longVal) }
        verify { mockPreparedStatement.setObject(3, doubleVal) }
        verify { mockPreparedStatement.setObject(4, LocalDateTime.ofInstant(instantVal, ZoneOffset.UTC)) }
        verify { mockPreparedStatement.setObject(5, timeVal.toOffsetDateTime()) }
        verify { mockPreparedStatement.setObject(6, objectVal) }
    }

}
