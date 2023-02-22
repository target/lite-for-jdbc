package com.target.liteforjdbc

import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.time.*
import java.util.*


class NamedParamPreparedStatementTest {

    @MockK(relaxed = true)
    lateinit var mockPreparedStatement: PreparedStatement

    private lateinit var preparedStatement: NamedParamPreparedStatement

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        val params = mapOf(
            "field-1" to listOf(1), "field-2" to listOf(2), "field-3" to listOf(3),
            "field-4" to listOf(4), "field-5" to listOf(5), "field-6" to listOf(6)
        )
        preparedStatement = NamedParamPreparedStatement(mockPreparedStatement, params)
    }

    @Test
    fun testBuilder() {
        val conn: Connection = mockk()
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = :field_1 AND T.field2 = :field-2"
        val builtSql = "SELECT * FROM \"TABLE\" T WHERE T.field = ? AND T.field2 = ?"

        every { conn.prepareStatement(builtSql) } returns (mockPreparedStatement)
        val builtNamedParam = NamedParamPreparedStatement.Builder(conn, sql).build()

        builtNamedParam.namedParamOrdinalIndexes.size shouldBe 2
        builtNamedParam.namedParamOrdinalIndexes["field_1"] shouldBe listOf(1)
        builtNamedParam.namedParamOrdinalIndexes["field-2"] shouldBe listOf(2)
        builtNamedParam.preparedStatement shouldBe mockPreparedStatement
    }

    @Test
    fun `a reused named parameter should populate all ordinal parameters`() {
        val conn: Connection = mockk()
        val sql = """SELECT * FROM "TABLE" T WHERE T.field = :field_1 AND T.field2 = :field_1"""
        val builtSql = """SELECT * FROM "TABLE" T WHERE T.field = ? AND T.field2 = ?"""

        every { conn.prepareStatement(builtSql) } returns (mockPreparedStatement)
        val builtNamedParam = NamedParamPreparedStatement.Builder(conn, sql).build()

        builtNamedParam.preparedStatement shouldBe mockPreparedStatement

        builtNamedParam.namedParamOrdinalIndexes.size shouldBe 1
        builtNamedParam.namedParamOrdinalIndexes["field_1"] shouldBe listOf(1, 2)
    }

    @Test
    fun testBuilderWithGeneratedKeys() {
        val conn: Connection = mockk()
        val sql = "INSERT INTO T (field, field2) VALUES(:field_1, :field-2)"
        val builtSql = "INSERT INTO T (field, field2) VALUES(?, ?)"

        every { conn.prepareStatement(builtSql, Statement.RETURN_GENERATED_KEYS) } returns (mockPreparedStatement)
        val builtNamedParam = NamedParamPreparedStatement.Builder(conn, sql, true).build()

        builtNamedParam.namedParamOrdinalIndexes.size shouldBe 2
        builtNamedParam.namedParamOrdinalIndexes["field_1"] shouldBe listOf(1)
        builtNamedParam.namedParamOrdinalIndexes["field-2"] shouldBe listOf(2)
        builtNamedParam.preparedStatement shouldBe mockPreparedStatement
    }

    @Test
    fun testBuilderWithMixedParameters() {
        val conn: Connection = mockk()
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = :field_1 AND T.field2 = ?"

        shouldThrowMessage("Named parameters cannot have positional parameters as well. But 1 positional parameter(s) were found") {
            NamedParamPreparedStatement.Builder(conn, sql).build()
        }
    }

    @Test
    fun testSetParameters() {
        val stringVal = "string"
        val longVal = 10L
        val doubleVal = 2.3
        val instantVal = Instant.now()
        val timeVal = ZonedDateTime.now()
        val objectVal = BigDecimal("10.123456")

        val mapParams = mapOf(
            "field-1" to stringVal, "field-2" to longVal, "field-3" to doubleVal, "field-4" to instantVal,
            "field-5" to timeVal, "field-6" to objectVal
        )
        preparedStatement.setParameters(mapParams)

        verify { mockPreparedStatement.setObject(1, stringVal) }
        verify { mockPreparedStatement.setObject(2, longVal) }
        verify { mockPreparedStatement.setObject(3, doubleVal) }
        verify { mockPreparedStatement.setObject(4, LocalDateTime.ofInstant(instantVal, ZoneOffset.UTC)) }
        verify { mockPreparedStatement.setObject(5, timeVal.toOffsetDateTime()) }
        verify { mockPreparedStatement.setObject(6, objectVal) }
    }

    @Test
    fun testSetParameterForTime() {
        val value = ZonedDateTime.now()

        preparedStatement.setParameter("field-1", value)

        verify { mockPreparedStatement.setObject(1, value.toOffsetDateTime()) }
    }

    @Test
    fun testSetParameterForInstant() {
        val value = Instant.now()

        preparedStatement.setParameter("field-1", value)

        verify { mockPreparedStatement.setObject(1, LocalDateTime.ofInstant(value, ZoneOffset.UTC)) }
    }

    @Test
    fun testSetParameterForObject() {
        val value = BigDecimal("10.123456")

        preparedStatement.setParameter("field-1", value)

        verify { mockPreparedStatement.setObject(1, value) }
    }

    @Test
    fun testGetIndex() {
        val result = preparedStatement.getIndexes("field-1")

        result shouldBe listOf(1)
    }

    @Test
    fun testGetIndexBadName() {
        shouldThrowMessage(
            "Unable to find a parameter named badName in available keys " +
                    "(field-1, field-2, field-3, field-4, field-5, field-6)"
        ) {
            preparedStatement.getIndexes("badName")
        }
    }

    @Test
    fun testSetNull() {
        preparedStatement.setNull("field-1", 2)

        verify { mockPreparedStatement.setNull(1, 2) }
    }

    @Test
    fun testSetBoolean() {
        val value = true
        preparedStatement.setBoolean("field-1", value)

        verify { mockPreparedStatement.setBoolean(1, value) }
    }

    @Test
    fun testSetByte() {
        val value: Byte = 63
        preparedStatement.setByte("field-1", value)

        verify { mockPreparedStatement.setByte(1, value) }
    }

    @Test
    fun testSetShort() {
        val value: Short = 63
        preparedStatement.setShort("field-1", value)

        verify { mockPreparedStatement.setShort(1, value) }
    }

    @Test
    fun testSetInt() {
        val value = 63
        preparedStatement.setInt("field-1", value)

        verify { mockPreparedStatement.setInt(1, value) }
    }

    @Test
    fun testSetLong() {
        val value: Long = 63
        preparedStatement.setLong("field-1", value)

        verify { mockPreparedStatement.setLong(1, value) }
    }

    @Test
    fun testSetFloat() {
        val value = 63F
        preparedStatement.setFloat("field-1", value)

        verify { mockPreparedStatement.setFloat(1, value) }
    }

    @Test
    fun testSetDouble() {
        val value = 63.0
        preparedStatement.setDouble("field-1", value)

        verify { mockPreparedStatement.setDouble(1, value) }
    }

    @Test
    fun testSetBigDecimal() {
        val value = BigDecimal("63.0")
        preparedStatement.setBigDecimal("field-1", value)

        verify { mockPreparedStatement.setBigDecimal(1, value) }

    }

    @Test
    fun testSetString() {
        val value = "test"
        preparedStatement.setString("field-1", value)

        verify { mockPreparedStatement.setString(1, value) }
    }

    @Test
    fun testSetBytes() {
        val value: ByteArray = byteArrayOf(63)
        preparedStatement.setBytes("field-1", value)

        verify { mockPreparedStatement.setBytes(1, value) }

    }

    @Test
    fun testSetDate() {
        val value = Date(System.currentTimeMillis())
        preparedStatement.setDate("field-1", value)

        verify { mockPreparedStatement.setDate(1, value) }
    }

    @Test
    fun testSetTime() {
        val value = Time(System.currentTimeMillis())
        preparedStatement.setTime("field-1", value)

        verify { mockPreparedStatement.setTime(1, value) }
    }

    @Test
    fun testSetTimestamp() {
        val value = Timestamp(System.currentTimeMillis())
        preparedStatement.setTimestamp("field-1", value)

        verify { mockPreparedStatement.setTimestamp(1, value) }
    }

    @Test
    fun testSetNullWithSqlTypeAndTypeName() {
        preparedStatement.setNull("field-1", 2, "type")

        verify { mockPreparedStatement.setNull(1, 2, "type") }
    }

    @Test
    fun testSetAsciiStreamWithLength() {
        val value: InputStream = mockk()
        preparedStatement.setAsciiStream("field-1", value, 1024)

        verify { mockPreparedStatement.setAsciiStream(1, value, 1024) }
    }

    @Test
    fun testSetBinaryStreamWithLength() {
        val value: InputStream = mockk()
        preparedStatement.setBinaryStream("field-1", value, 1024)

        verify { mockPreparedStatement.setBinaryStream(1, value, 1024) }
    }

    @Test
    fun testSetCharacterStreamWithLength() {
        val value: Reader = mockk()
        preparedStatement.setCharacterStream("field-1", value, 1024)

        verify { mockPreparedStatement.setCharacterStream(1, value, 1024) }
    }

    @Test
    fun testSetRef() {
        val value: Ref = mockk()
        preparedStatement.setRef("field-1", value)

        verify { mockPreparedStatement.setRef(1, value) }
    }

    @Test
    fun testSetBlob() {
        val value: Blob = mockk()
        preparedStatement.setBlob("field-1", value)

        verify { mockPreparedStatement.setBlob(1, value) }
    }

    @Test
    fun testSetClob() {
        val value: Clob = mockk()
        preparedStatement.setClob("field-1", value)

        verify { mockPreparedStatement.setClob(1, value) }
    }

    @Test
    fun testSetArray() {
        val value: Array = mockk()
        preparedStatement.setArray("field-1", value)

        verify { mockPreparedStatement.setArray(1, value) }
    }

    // TODO HERE
    @Test
    fun testSetDateWithCalendar() {
        val value: Date = mockk()
        val calendar = Calendar.getInstance()
        preparedStatement.setDate("field-1", value, calendar)

        verify { mockPreparedStatement.setDate(1, value, calendar) }
    }

    @Test
    fun testSetTimeWithCalendar() {
        val value: Time = mockk()
        val calendar = Calendar.getInstance()
        preparedStatement.setTime("field-1", value, calendar)

        verify { mockPreparedStatement.setTime(1, value, calendar) }
    }

    @Test
    fun testSetTimestampWithCalendar() {
        val value: Timestamp = mockk()
        val calendar = Calendar.getInstance()
        preparedStatement.setTimestamp("field-1", value, calendar)

        verify { mockPreparedStatement.setTimestamp(1, value, calendar) }
    }

    @Test
    fun testSetNullWithTypeName() {
        val sqlType = 2
        val typeName = "string"
        preparedStatement.setNull(1, sqlType, typeName)

        verify { mockPreparedStatement.setNull(1, sqlType, typeName) }
    }

    @Test
    fun testSetURL() {
        val value: URL = mockk()
        preparedStatement.setURL("field-1", value)

        verify { mockPreparedStatement.setURL(1, value) }
    }

    @Test
    fun testSetRowId() {
        val value: RowId = mockk()
        preparedStatement.setRowId("field-1", value)

        verify { mockPreparedStatement.setRowId(1, value) }
    }

    @Test
    fun testSetNString() {
        val value = "string"
        preparedStatement.setNString("field-1", value)

        verify { mockPreparedStatement.setNString(1, value) }
    }

    @Test
    fun testSetNCharacterStreamWithLength() {
        val value: Reader = mockk()
        val length = 1024L
        preparedStatement.setNCharacterStream("field-1", value, length)

        verify { mockPreparedStatement.setNCharacterStream(1, value, length) }
    }

    @Test
    fun testSetNClob() {
        val value: NClob = mockk()
        preparedStatement.setNClob("field-1", value)

        verify { mockPreparedStatement.setNClob(1, value) }
    }

    @Test
    fun testSetClobWithReaderAndLength() {
        val value: Reader = mockk()
        val length = 1024L
        preparedStatement.setClob("field-1", value, length)

        verify { mockPreparedStatement.setClob(1, value, length) }
    }

    @Test
    fun testSetBlobWithInputStreamAndLength() {
        val value: InputStream = mockk()
        val length = 1024L
        preparedStatement.setBlob("field-1", value, length)

        verify { mockPreparedStatement.setBlob(1, value, length) }
    }

    @Test
    fun testSetNClobWithReaderAndLength() {
        val value: Reader = mockk()
        val length = 1024L
        preparedStatement.setNClob("field-1", value, length)

        verify { mockPreparedStatement.setNClob(1, value, length) }
    }

    @Test
    fun testSetSQLXML() {
        val value: SQLXML = mockk()
        preparedStatement.setSQLXML("field-1", value)

        verify { mockPreparedStatement.setSQLXML(1, value) }
    }

    @Test
    fun testSetObjectWithTypeAndScale() {
        val value = "string"
        val sqlType = 2
        val scaleOrLength = 3
        preparedStatement.setObject("field-1", value, sqlType, scaleOrLength)

        verify { mockPreparedStatement.setObject(1, value, sqlType, scaleOrLength) }
    }

    @Test
    fun testSetAsciiStreamWithlongLength() {
        val value: InputStream = mockk()
        val length = 1024L
        preparedStatement.setAsciiStream("field-1", value, length)

        verify { mockPreparedStatement.setAsciiStream(1, value, length) }
    }

    @Test
    fun testSetBinaryStreamWithLongLength() {
        val value: InputStream = mockk()
        val length = 1024L
        preparedStatement.setBinaryStream("field-1", value, length)

        verify { mockPreparedStatement.setBinaryStream(1, value, length) }
    }

    @Test
    fun testSetCharacterStreamWithLongLength() {
        val value: Reader = mockk()
        val length = 1024L
        preparedStatement.setCharacterStream("field-1", value, length)

        verify { mockPreparedStatement.setCharacterStream(1, value, length) }
    }

    @Test
    fun testSetAsciiStream() {
        val value: InputStream = mockk()
        preparedStatement.setAsciiStream("field-1", value)

        verify { mockPreparedStatement.setAsciiStream(1, value) }
    }

    @Test
    fun testSetBinaryStream() {
        val value: InputStream = mockk()
        preparedStatement.setBinaryStream("field-1", value)

        verify { mockPreparedStatement.setBinaryStream(1, value) }
    }

    @Test
    fun testSetCharacterStream() {
        val value: Reader = mockk()
        preparedStatement.setCharacterStream("field-1", value)

        verify { mockPreparedStatement.setCharacterStream(1, value) }
    }

    @Test
    fun testSetNCharacterStream() {
        val value: Reader = mockk()
        preparedStatement.setNCharacterStream("field-1", value)

        verify { mockPreparedStatement.setNCharacterStream(1, value) }
    }

    @Test
    fun testSetClobWithReader() {
        val value: Reader = mockk()
        preparedStatement.setClob("field-1", value)

        verify { mockPreparedStatement.setClob(1, value) }
    }

    @Test
    fun testSetBlobWithInputStream() {
        val value: InputStream = mockk()
        preparedStatement.setBlob("field-1", value)

        verify { mockPreparedStatement.setBlob(1, value) }
    }

    @Test
    fun testSetNClobWithReader() {
        val value: Reader = mockk()
        preparedStatement.setNClob("field-1", value)

        verify { mockPreparedStatement.setNClob(1, value) }
    }

    @Test
    fun testSetObjectWithTypeEnumAndScale() {
        val value = "string"
        val type: SQLType = JDBCType.VARCHAR
        val scaleOrLength = 3
        preparedStatement.setObject("field-1", value, type, scaleOrLength)

        verify { mockPreparedStatement.setObject(1, value, type, scaleOrLength) }
    }

    @Test
    fun testSetObjectWithTypeEnum() {
        val value = "string"
        val type: SQLType = JDBCType.VARCHAR
        preparedStatement.setObject("field-1", value, type)

        verify { mockPreparedStatement.setObject(1, value, type) }
    }

    @Test
    fun testSetObjectWithType() {
        val value = "string"
        val type = 2
        preparedStatement.setObject("field-1", value, type)

        verify { mockPreparedStatement.setObject(1, value, type) }
    }

    @Test
    fun testSetUUID() {
        val value = UUID.randomUUID()
        preparedStatement.setUUID("field-1", value)

        verify { mockPreparedStatement.setUUID(1, value) }
    }

    @Test
    fun testSetZonedDateTime() {
        val value = ZonedDateTime.now()
        preparedStatement.setZonedDateTime("field-1", value)

        verify { mockPreparedStatement.setZonedDateTime(1, value) }
    }

    @Test
    fun testSetInstant() {
        val value = Instant.now()
        preparedStatement.setInstant("field-1", value)

        verify { mockPreparedStatement.setInstant(1, value) }
    }

    @Test
    fun testSetLocalDateTime() {
        val value = LocalDateTime.now()
        preparedStatement.setLocalDateTime("field-1", value)

        verify { mockPreparedStatement.setLocalDateTime(1, value) }
    }

    @Test
    fun testSetLocalDate() {
        val value = LocalDate.now()
        preparedStatement.setLocalDate("field-1", value)

        verify { mockPreparedStatement.setLocalDate(1, value) }
    }

    @Test
    fun testSetLocalTime() {
        val value = LocalTime.now()
        preparedStatement.setLocalTime("field-1", value)

        verify { mockPreparedStatement.setLocalTime(1, value) }
    }

    @Test
    fun testSetOffsetDateTime() {
        val value = OffsetDateTime.now()
        preparedStatement.setOffsetDateTime("field-1", value)

        verify { mockPreparedStatement.setOffsetDateTime(1, value) }
    }

    @Test
    fun testSetOffsetTime() {
        val value = OffsetTime.now()
        preparedStatement.setOffsetTime("field-1", value)

        verify { mockPreparedStatement.setOffsetTime(1, value) }
    }

    @Test
    fun testSetDbValue() {
        val value = DbValue("value", SqlParameterType(JDBCType.VARCHAR))
        preparedStatement.setDbValue("field-1", value)

        verify { mockPreparedStatement.setDbValue(1, value) }
    }
}
