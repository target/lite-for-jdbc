package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*


@Suppress("UNCHECKED_CAST", "DEPRECATION")
class PreparedStatementProxyTest {

    @MockK
    lateinit var mockPreparedStatement: PreparedStatement

    private lateinit var preparedStatementProxy: PreparedStatementProxy

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        preparedStatementProxy = PreparedStatementProxy(mockPreparedStatement)
    }

    @Test
    fun testExecuteQuery() {
        val resultSet: ResultSet = mockk()
        every { mockPreparedStatement.executeQuery() } returns (resultSet)
        val result = preparedStatementProxy.executeQuery()

        result shouldBe resultSet
    }

    @Test
    fun testExecuteUpdate() {
        every { mockPreparedStatement.executeUpdate() } returns (1)
        val result = preparedStatementProxy.executeUpdate()

        result shouldBe 1
    }

    @Test
    fun testSetNull() {
        justRun { mockPreparedStatement.setNull(1, 2) }
        preparedStatementProxy.setNull(1, 2)

        verify { mockPreparedStatement.setNull(1, 2) }
    }

    @Test
    fun testSetBoolean() {
        justRun { mockPreparedStatement.setBoolean(1, true) }
        preparedStatementProxy.setBoolean(1, true)

        verify { mockPreparedStatement.setBoolean(1, true) }
    }

    @Test
    fun testSetByte() {
        val value: Byte = 63
        justRun { mockPreparedStatement.setByte(1, value) }
        preparedStatementProxy.setByte(1, value)

        verify { mockPreparedStatement.setByte(1, value) }
    }

    @Test
    fun testSetShort() {
        val value: Short = 63
        justRun { mockPreparedStatement.setShort(1, value) }
        preparedStatementProxy.setShort(1, value)

        verify { mockPreparedStatement.setShort(1, value) }
    }

    @Test
    fun testSetInt() {
        val value = 63
        justRun { mockPreparedStatement.setInt(1, value) }
        preparedStatementProxy.setInt(1, value)

        verify { mockPreparedStatement.setInt(1, value) }
    }

    @Test
    fun testSetLong() {
        val value: Long = 63
        justRun { mockPreparedStatement.setLong(1, value) }
        preparedStatementProxy.setLong(1, value)

        verify { mockPreparedStatement.setLong(1, value) }
    }

    @Test
    fun testSetFloat() {
        val value = 63F
        justRun { mockPreparedStatement.setFloat(1, value) }
        preparedStatementProxy.setFloat(1, value)

        verify { mockPreparedStatement.setFloat(1, value) }
    }

    @Test
    fun testSetDouble() {
        val value = 63.0
        justRun { mockPreparedStatement.setDouble(1, value) }
        preparedStatementProxy.setDouble(1, value)

        verify { mockPreparedStatement.setDouble(1, value) }
    }

    @Test
    fun testSetBigDecimal() {
        val value = (63.0).toBigDecimal()
        justRun { mockPreparedStatement.setBigDecimal(1, value) }
        preparedStatementProxy.setBigDecimal(1, value)

        verify { mockPreparedStatement.setBigDecimal(1, value) }

    }

    @Test
    fun testSetString() {
        val value = "test"
        justRun { mockPreparedStatement.setString(1, value) }
        preparedStatementProxy.setString(1, value)

        verify { mockPreparedStatement.setString(1, value) }
    }

    @Test
    fun testSetBytes() {
        val value: ByteArray = byteArrayOf(63)
        justRun { mockPreparedStatement.setBytes(1, value) }
        preparedStatementProxy.setBytes(1, value)

        verify { mockPreparedStatement.setBytes(1, value) }

    }

    @Test
    fun testSetDate() {
        val value = Date(System.currentTimeMillis())
        justRun { mockPreparedStatement.setDate(1, value) }
        preparedStatementProxy.setDate(1, value)

        verify { mockPreparedStatement.setDate(1, value) }
    }

    @Test
    fun testSetTime() {
        val value = Time(System.currentTimeMillis())
        justRun { mockPreparedStatement.setTime(1, value) }
        preparedStatementProxy.setTime(1, value)

        verify { mockPreparedStatement.setTime(1, value) }
    }

    @Test
    fun testSetTimestamp() {
        val value = Timestamp(System.currentTimeMillis())
        justRun { mockPreparedStatement.setTimestamp(1, value) }
        preparedStatementProxy.setTimestamp(1, value)

        verify { mockPreparedStatement.setTimestamp(1, value) }
    }

    @Test
    fun testSetAsciiStreamWithLength() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setAsciiStream(1, value, 1024) }
        preparedStatementProxy.setAsciiStream(1, value, 1024)

        verify { mockPreparedStatement.setAsciiStream(1, value, 1024) }
    }

    @Test
    fun testSetUnicodeStream() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setUnicodeStream(1, value, 1024) }
        preparedStatementProxy.setUnicodeStream(1, value, 1024)

        verify { mockPreparedStatement.setUnicodeStream(1, value, 1024) }
    }

    @Test
    fun testSetBinaryStreamWithLength() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setBinaryStream(1, value, 1024) }
        preparedStatementProxy.setBinaryStream(1, value, 1024)

        verify { mockPreparedStatement.setBinaryStream(1, value, 1024) }
    }

    @Test
    fun testClearParameters() {
        justRun { mockPreparedStatement.clearParameters() }
        preparedStatementProxy.clearParameters()

        verify { mockPreparedStatement.clearParameters() }
    }

    @Test
    fun testSetObjectWithType() {
        val value = 63
        justRun { mockPreparedStatement.setObject(1, value, 2) }
        preparedStatementProxy.setObject(1, value, 2)

        verify { mockPreparedStatement.setObject(1, value, 2) }
    }

    @Test
    fun testSetObject() {
        val value = 63
        justRun { mockPreparedStatement.setObject(1, value) }
        preparedStatementProxy.setObject(1, value)

        verify { mockPreparedStatement.setObject(1, value) }
    }

    @Test
    fun testExecute() {
        every { mockPreparedStatement.execute() } returns (true)
        val result = preparedStatementProxy.execute()

        result shouldBe true
    }

    @Test
    fun testAddBatch() {
        justRun { mockPreparedStatement.addBatch() }
        preparedStatementProxy.addBatch()

        verify { mockPreparedStatement.addBatch() }
    }

    @Test
    fun testSetCharacterStreamWithLength() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setCharacterStream(1, value, 1024) }
        preparedStatementProxy.setCharacterStream(1, value, 1024)

        verify { mockPreparedStatement.setCharacterStream(1, value, 1024) }
    }

    @Test
    fun testSetRef() {
        val value: Ref = mockk()
        justRun { mockPreparedStatement.setRef(1, value) }
        preparedStatementProxy.setRef(1, value)

        verify { mockPreparedStatement.setRef(1, value) }
    }

    @Test
    fun testSetBlob() {
        val value: Blob = mockk()
        justRun { mockPreparedStatement.setBlob(1, value) }
        preparedStatementProxy.setBlob(1, value)

        verify { mockPreparedStatement.setBlob(1, value) }
    }

    @Test
    fun testSetClob() {
        val value: Clob = mockk()
        justRun { mockPreparedStatement.setClob(1, value) }
        preparedStatementProxy.setClob(1, value)

        verify { mockPreparedStatement.setClob(1, value) }
    }

    @Test
    fun testSetArray() {
        val value: Array = mockk()
        justRun { mockPreparedStatement.setArray(1, value) }
        preparedStatementProxy.setArray(1, value)

        verify { mockPreparedStatement.setArray(1, value) }
    }

    @Test
    fun testGetMetaData() {
        val value: ResultSetMetaData = mockk()
        every { mockPreparedStatement.metaData } returns (value)
        val result = preparedStatementProxy.metaData

        result shouldBe value
    }

    @Test
    fun testSetDateWithCalendar() {
        val value: Date = mockk()
        val calendar = Calendar.getInstance()
        justRun { mockPreparedStatement.setDate(1, value, calendar) }
        preparedStatementProxy.setDate(1, value, calendar)

        verify { mockPreparedStatement.setDate(1, value, calendar) }
    }

    @Test
    fun testSetTimeWithCalendar() {
        val value: Time = mockk()
        val calendar = Calendar.getInstance()
        justRun { mockPreparedStatement.setTime(1, value, calendar) }
        preparedStatementProxy.setTime(1, value, calendar)

        verify { mockPreparedStatement.setTime(1, value, calendar) }
    }

    @Test
    fun testSetTimestampWithCalendar() {
        val value: Timestamp = mockk()
        val calendar = Calendar.getInstance()
        justRun { mockPreparedStatement.setTimestamp(1, value, calendar) }
        preparedStatementProxy.setTimestamp(1, value, calendar)

        verify { mockPreparedStatement.setTimestamp(1, value, calendar) }
    }

    @Test
    fun testSetNullWithTypeName() {
        justRun { mockPreparedStatement.setNull(1, 2, "string") }
        preparedStatementProxy.setNull(1, 2, "string")

        verify { mockPreparedStatement.setNull(1, 2, "string") }
    }

    @Test
    fun testSetURL() {
        val value: URL = mockk()
        justRun { mockPreparedStatement.setURL(1, value) }
        preparedStatementProxy.setURL(1, value)

        verify { mockPreparedStatement.setURL(1, value) }
    }

    @Test
    fun testGetParameterMetaData() {
        val value: ParameterMetaData = mockk()
        every { mockPreparedStatement.parameterMetaData } returns (value)
        val result = preparedStatementProxy.parameterMetaData

        result shouldBe value
    }

    @Test
    fun testSetRowId() {
        val value: RowId = mockk()
        justRun { mockPreparedStatement.setRowId(1, value) }
        preparedStatementProxy.setRowId(1, value)

        verify { mockPreparedStatement.setRowId(1, value) }
    }

    @Test
    fun testSetNString() {
        val value = "string"
        justRun { mockPreparedStatement.setNString(1, value) }
        preparedStatementProxy.setNString(1, value)

        verify { mockPreparedStatement.setNString(1, value) }
    }

    @Test
    fun testSetNCharacterStreamWithLength() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setNCharacterStream(1, value, 1024) }
        preparedStatementProxy.setNCharacterStream(1, value, 1024)

        verify { mockPreparedStatement.setNCharacterStream(1, value, 1024) }
    }

    @Test
    fun testSetNClob() {
        val value: NClob = mockk()
        justRun { mockPreparedStatement.setNClob(1, value) }
        preparedStatementProxy.setNClob(1, value)

        verify { mockPreparedStatement.setNClob(1, value) }
    }

    @Test
    fun testSetClobWithReaderAndLength() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setClob(1, value, 1024) }
        preparedStatementProxy.setClob(1, value, 1024)

        verify { mockPreparedStatement.setClob(1, value, 1024) }
    }

    @Test
    fun testSetBlobWithInputStreamAndLength() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setBlob(1, value, 1024) }
        preparedStatementProxy.setBlob(1, value, 1024)

        verify { mockPreparedStatement.setBlob(1, value, 1024) }
    }

    @Test
    fun testSetNClobWithReaderAndLength() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setNClob(1, value, 1024) }
        preparedStatementProxy.setNClob(1, value, 1024)

        verify { mockPreparedStatement.setNClob(1, value, 1024) }
    }

    @Test
    fun testSetSQLXML() {
        val value: SQLXML = mockk()
        justRun { mockPreparedStatement.setSQLXML(1, value) }
        preparedStatementProxy.setSQLXML(1, value)

        verify { mockPreparedStatement.setSQLXML(1, value) }
    }

    @Test
    fun testSetObjectWithTypeAndScale() {
        val value = "string"
        justRun { mockPreparedStatement.setObject(1, value, 2, 3) }
        preparedStatementProxy.setObject(1, value, 2, 3)

        verify { mockPreparedStatement.setObject(1, value, 2, 3) }
    }

    @Test
    fun testSetAsciiStreamWithlongLength() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setAsciiStream(1, value, 1024L) }
        preparedStatementProxy.setAsciiStream(1, value, 1024L)

        verify { mockPreparedStatement.setAsciiStream(1, value, 1024L) }
    }

    @Test
    fun testSetBinaryStreamWithLongLength() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setBinaryStream(1, value, 1024L) }
        preparedStatementProxy.setBinaryStream(1, value, 1024L)

        verify { mockPreparedStatement.setBinaryStream(1, value, 1024L) }
    }

    @Test
    fun testSetCharacterStreamWithLongLength() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setCharacterStream(1, value, 1024L) }
        preparedStatementProxy.setCharacterStream(1, value, 1024L)

        verify { mockPreparedStatement.setCharacterStream(1, value, 1024L) }
    }

    @Test
    fun testSetAsciiStream() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setAsciiStream(1, value) }
        preparedStatementProxy.setAsciiStream(1, value)

        verify { mockPreparedStatement.setAsciiStream(1, value) }
    }

    @Test
    fun testSetBinaryStream() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setBinaryStream(1, value) }
        preparedStatementProxy.setBinaryStream(1, value)

        verify { mockPreparedStatement.setBinaryStream(1, value) }
    }

    @Test
    fun testSetCharacterStream() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setCharacterStream(1, value) }
        preparedStatementProxy.setCharacterStream(1, value)

        verify { mockPreparedStatement.setCharacterStream(1, value) }
    }

    @Test
    fun testSetNCharacterStream() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setNCharacterStream(1, value) }
        preparedStatementProxy.setNCharacterStream(1, value)

        verify { mockPreparedStatement.setNCharacterStream(1, value) }
    }

    @Test
    fun testSetClobWithReader() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setClob(1, value) }
        preparedStatementProxy.setClob(1, value)

        verify { mockPreparedStatement.setClob(1, value) }
    }

    @Test
    fun testSetBlobWithInputStream() {
        val value: InputStream = mockk()
        justRun { mockPreparedStatement.setBlob(1, value) }
        preparedStatementProxy.setBlob(1, value)

        verify { mockPreparedStatement.setBlob(1, value) }
    }

    @Test
    fun testSetNClobWithReader() {
        val value: Reader = mockk()
        justRun { mockPreparedStatement.setNClob(1, value) }
        preparedStatementProxy.setNClob(1, value)

        verify { mockPreparedStatement.setNClob(1, value) }
    }

    @Test
    fun testSetObjectWithTypeEnumAndScale() {
        val value = "string"
        val type: SQLType = JDBCType.VARCHAR
        justRun { mockPreparedStatement.setObject(1, value, type, 3) }
        preparedStatementProxy.setObject(1, value, type, 3)

        verify { mockPreparedStatement.setObject(1, value, type, 3) }
    }

    @Test
    fun testSetObjectWithTypeEnum() {
        val value = "string"
        val type: SQLType = JDBCType.VARCHAR
        justRun { mockPreparedStatement.setObject(1, value, type) }
        preparedStatementProxy.setObject(1, value, type)

        verify { mockPreparedStatement.setObject(1, value, type) }
    }

    @Test
    fun testExecuteLargeUpdate() {
        every { mockPreparedStatement.executeLargeUpdate() } returns (1L)
        val result = preparedStatementProxy.executeLargeUpdate()

        result shouldBe 1L
    }

    @Test
    fun testExecuteQueryWithSql() {
        val resultSet: ResultSet = mockk()
        every { mockPreparedStatement.executeQuery("sql") } returns (resultSet)
        val result = preparedStatementProxy.executeQuery("sql")

        result shouldBe resultSet
    }

    @Test
    fun testExecuteUpdateWithSql() {
        every { mockPreparedStatement.executeUpdate("sql") } returns (1)
        val result = preparedStatementProxy.executeUpdate("sql")

        result shouldBe 1
    }

    @Test
    fun testClose() {
        justRun { mockPreparedStatement.close() }
        preparedStatementProxy.close()

        verify { mockPreparedStatement.close() }
    }

    @Test
    fun testGetMaxFieldSize() {
        every { mockPreparedStatement.maxFieldSize } returns 1
        val result = preparedStatementProxy.maxFieldSize

        result shouldBe 1
    }

    @Test
    fun testSetMaxFieldSize() {
        justRun { mockPreparedStatement.maxFieldSize = 1 }
        preparedStatementProxy.maxFieldSize = 1

        verify { mockPreparedStatement.maxFieldSize = 1 }
    }

    @Test
    fun testGetMaxRows() {
        every { mockPreparedStatement.maxRows } returns 1
        val result = preparedStatementProxy.maxRows

        result shouldBe 1
    }

    @Test
    fun testSetMaxRows() {
        justRun { mockPreparedStatement.maxRows = 1 }
        preparedStatementProxy.maxRows = 1

        verify { mockPreparedStatement.maxRows = 1 }
    }

    @Test
    fun testSetEscapeProcessing() {
        justRun { mockPreparedStatement.setEscapeProcessing(true) }
        preparedStatementProxy.setEscapeProcessing(true)

        verify { mockPreparedStatement.setEscapeProcessing(true) }
    }

    @Test
    fun testGetQueryTimeout() {
        every { mockPreparedStatement.queryTimeout } returns 1
        val result = preparedStatementProxy.queryTimeout

        result shouldBe 1
    }

    @Test
    fun testSetQueryTimeout() {
        justRun { mockPreparedStatement.queryTimeout = 1 }
        preparedStatementProxy.queryTimeout = 1

        verify { mockPreparedStatement.queryTimeout = 1 }
    }

    @Test
    fun testCancel() {
        justRun { mockPreparedStatement.cancel() }
        preparedStatementProxy.cancel()

        verify { mockPreparedStatement.cancel() }
    }

    @Test
    fun testGetWarnings() {
        val value: SQLWarning = mockk()
        every { mockPreparedStatement.warnings } returns value
        val result = preparedStatementProxy.warnings

        result shouldBe value
    }

    @Test
    fun testClearWarnings() {
        justRun { mockPreparedStatement.clearWarnings() }
        preparedStatementProxy.clearWarnings()

        verify { mockPreparedStatement.clearWarnings() }
    }

    @Test
    fun testSetCursorName() {
        justRun { mockPreparedStatement.setCursorName("name") }
        preparedStatementProxy.setCursorName("name")

        verify { mockPreparedStatement.setCursorName("name") }
    }

    @Test
    fun testExecuteWithSql() {
        every { mockPreparedStatement.execute("sql") } returns (true)
        val result = preparedStatementProxy.execute("sql")

        result shouldBe true
    }

    @Test
    fun testGetResultSet() {
        val value: ResultSet = mockk()
        every { mockPreparedStatement.resultSet } returns value
        val result = preparedStatementProxy.resultSet

        result shouldBe value
    }

    @Test
    fun testGetUpdateCount() {
        val value = 1
        every { mockPreparedStatement.updateCount } returns value
        val result = preparedStatementProxy.updateCount

        result shouldBe value
    }

    @Test
    fun testGetMoreResults() {
        val value = true
        every { mockPreparedStatement.moreResults } returns value
        val result = preparedStatementProxy.moreResults

        result shouldBe value
    }

    @Test
    fun testGetFetchDirection() {
        val value = 1
        every { mockPreparedStatement.fetchDirection } returns value
        val result = preparedStatementProxy.fetchDirection

        result shouldBe value
    }

    @Test
    fun testSetFetchDirection() {
        val value = 1
        justRun { mockPreparedStatement.fetchDirection = value }
        preparedStatementProxy.fetchDirection = value

        verify { mockPreparedStatement.fetchDirection = value }
    }

    @Test
    fun testGetFetchSize() {
        val value = 1
        every { mockPreparedStatement.fetchSize } returns value
        val result = preparedStatementProxy.fetchSize

        result shouldBe value
    }

    @Test
    fun testSetFetchSize() {
        val value = 1
        justRun { mockPreparedStatement.fetchSize = value }
        preparedStatementProxy.fetchSize = value

        verify { mockPreparedStatement.fetchSize = value }
    }

    @Test
    fun testGetResultSetConcurrency() {
        val value = 1
        every { mockPreparedStatement.resultSetConcurrency } returns value
        val result = preparedStatementProxy.resultSetConcurrency

        result shouldBe value
    }

    @Test
    fun testGetResultSetType() {
        val value = 1
        every { mockPreparedStatement.resultSetType } returns value
        val result = preparedStatementProxy.resultSetType

        result shouldBe value
    }

    @Test
    fun testAddBatchWithSql() {
        val value = "sql"
        justRun { mockPreparedStatement.addBatch(value) }
        preparedStatementProxy.addBatch(value)

        verify { mockPreparedStatement.addBatch(value) }
    }

    @Test
    fun testClearBatch() {
        justRun { mockPreparedStatement.clearBatch() }
        preparedStatementProxy.clearBatch()

        verify { mockPreparedStatement.clearBatch() }
    }

    @Test
    fun testExecuteBatch() {
        val value = intArrayOf(1)
        every { mockPreparedStatement.executeBatch() } returns value
        val result = preparedStatementProxy.executeBatch()

        result shouldBe value
    }

    @Test
    fun testGetConnection() {
        val value: Connection = mockk()
        every { mockPreparedStatement.connection } returns value
        val result = preparedStatementProxy.connection

        result shouldBe value
    }

    @Test
    fun testGetMoreResultsWithCurrent() {
        val value = true
        every { mockPreparedStatement.getMoreResults(1) } returns value
        val result = preparedStatementProxy.getMoreResults(1)

        result shouldBe value
    }

    @Test
    fun testGetGeneratedKeys() {
        val value: ResultSet = mockk()
        every { mockPreparedStatement.generatedKeys } returns value
        val result = preparedStatementProxy.generatedKeys

        result shouldBe value
    }

    @Test
    fun testExecuteUpdateWithSqlAndKeys() {
        every { mockPreparedStatement.executeUpdate("sql", 1) } returns (10)
        val result = preparedStatementProxy.executeUpdate("sql", 1)

        result shouldBe 10
    }

    @Test
    fun testExecuteUpdateWithSqlAndIndexes() {
        val indexes = intArrayOf(1)
        every { mockPreparedStatement.executeUpdate("sql", indexes) } returns (10)
        val result = preparedStatementProxy.executeUpdate("sql", indexes)

        result shouldBe 10
    }

    @Test
    fun testExecuteUpdateWithSqlAndColumnNames() {
        val columnNames = arrayOf("columnName") as kotlin.Array<String?>?
        every { mockPreparedStatement.executeUpdate("sql", columnNames) } returns (10)
        val result = preparedStatementProxy.executeUpdate("sql", columnNames)

        result shouldBe 10
    }


    @Test
    fun testExecuteWithSqlAndKeys() {
        val value = true
        every { mockPreparedStatement.execute("sql", 1) } returns (value)
        val result = preparedStatementProxy.execute("sql", 1)

        result shouldBe value
    }

    @Test
    fun testExecuteWithSqlAndIndexes() {
        val value = true
        val indexes = intArrayOf(1)
        every { mockPreparedStatement.execute("sql", indexes) } returns (value)
        val result = preparedStatementProxy.execute("sql", indexes)

        result shouldBe value
    }

    @Test
    fun testExecuteWithSqlAndColumnNames() {
        val value = true
        val columnNames = arrayOf("columnName") as kotlin.Array<String?>?
        every { mockPreparedStatement.execute("sql", columnNames) } returns (value)
        val result = preparedStatementProxy.execute("sql", columnNames)

        result shouldBe value
    }

    @Test
    fun testGetResultSetHoldability() {
        val value = 10
        every { mockPreparedStatement.resultSetHoldability } returns value
        val result = preparedStatementProxy.resultSetHoldability

        result shouldBe value
    }

    @Test
    fun testIsClosed() {
        val value = true
        every { mockPreparedStatement.isClosed } returns value
        val result = preparedStatementProxy.isClosed

        result shouldBe value
    }

    @Test
    fun testIsPoolable() {
        val value = true
        every { mockPreparedStatement.isPoolable } returns value
        val result = preparedStatementProxy.isPoolable

        result shouldBe value
    }

    @Test
    fun testSetPoolable() {
        val value = true
        justRun { mockPreparedStatement.isPoolable = value }
        preparedStatementProxy.isPoolable = value

        verify { mockPreparedStatement.isPoolable = value }
    }

    @Test
    fun testCloseOnCompletion() {
        justRun { mockPreparedStatement.closeOnCompletion() }
        preparedStatementProxy.closeOnCompletion()

        verify { mockPreparedStatement.closeOnCompletion() }
    }

    @Test
    fun testIsCloseOnCompletion() {
        val value = true
        every { mockPreparedStatement.isCloseOnCompletion } returns value
        val result = preparedStatementProxy.isCloseOnCompletion

        result shouldBe value
    }

    @Test
    fun testGetLargeUpdateCount() {
        val value = 10L
        every { mockPreparedStatement.largeUpdateCount } returns value
        val result = preparedStatementProxy.largeUpdateCount

        result shouldBe value
    }

    @Test
    fun testGetLargeMaxRows() {
        val value = 10L
        every { mockPreparedStatement.largeMaxRows } returns value
        val result = preparedStatementProxy.largeMaxRows

        result shouldBe value
    }

    @Test
    fun testSetLargeMaxRows() {
        val value = 10L
        justRun { mockPreparedStatement.largeMaxRows = value }
        preparedStatementProxy.largeMaxRows = value

        verify { mockPreparedStatement.largeMaxRows = value }
    }

    @Test
    fun testExecuteLargeBatch() {
        val value = longArrayOf(10L)
        every { mockPreparedStatement.executeLargeBatch() } returns value
        val result = preparedStatementProxy.executeLargeBatch()

        result shouldBe value
    }

    @Test
    fun testExecuteLargeUpdateWithSql() {
        val value = 10L
        val sql = "sql"
        every { mockPreparedStatement.executeLargeUpdate(sql) } returns value
        val result = preparedStatementProxy.executeLargeUpdate(sql)

        result shouldBe value
    }

    @Test
    fun testExecuteLargeUpdateWithSqlAndKeys() {
        val value = 10L
        val sql = "sql"
        val keys = 2
        every { mockPreparedStatement.executeLargeUpdate(sql, keys) } returns value
        val result = preparedStatementProxy.executeLargeUpdate(sql, keys)

        result shouldBe value
    }

    @Test
    fun testExecuteLargeUpdateWithSqlAndColumnIndexes() {
        val value = 10L
        val sql = "sql"
        val columnIndexes = intArrayOf(1)
        every { mockPreparedStatement.executeLargeUpdate(sql, columnIndexes) } returns value
        val result = preparedStatementProxy.executeLargeUpdate(sql, columnIndexes)

        result shouldBe value
    }

    @Test
    fun testExecuteLargeUpdateWithSqlAndColumnNames() {
        val value = 10L
        val sql = "sql"
        val columnIndexes = arrayOf("column") as kotlin.Array<String?>
        every { mockPreparedStatement.executeLargeUpdate(sql, columnIndexes) } returns value
        val result = preparedStatementProxy.executeLargeUpdate(sql, columnIndexes)

        result shouldBe value
    }

    @Test
    fun testEnquoteLiteral() {
        val value = "value"
        val string = "string"
        every { mockPreparedStatement.enquoteLiteral(string) } returns value
        val result = preparedStatementProxy.enquoteLiteral(string)

        result shouldBe value
    }

    @Test
    fun testEnquoteIdentifier() {
        val value = "value"
        val string = "string"
        every { mockPreparedStatement.enquoteIdentifier(string, true) } returns value
        val result = preparedStatementProxy.enquoteIdentifier(string, true)

        result shouldBe value
    }

    @Test
    fun testIsSimpleIdentifier() {
        val value = true
        val string = "string"
        every { mockPreparedStatement.isSimpleIdentifier(string) } returns value
        val result = preparedStatementProxy.isSimpleIdentifier(string)

        result shouldBe value
    }

    @Test
    fun testEnquoteNCharLiteral() {
        val value = "value"
        val string = "string"
        every { mockPreparedStatement.enquoteNCharLiteral(string) } returns value
        val result = preparedStatementProxy.enquoteNCharLiteral(string)

        result shouldBe value
    }

    @Test
    fun testUnwrap() {
        val value: PreparedStatement = mockk()
        val clazz = PreparedStatement::class.java

        // Hint is needed to overcome some issues with erasure in the generics
        every {
            hint(PreparedStatement::class)
            mockPreparedStatement.unwrap(clazz)
        } returns value
        val result = preparedStatementProxy.unwrap(clazz)

        result shouldBe value
    }

    @Test
    fun testIsWrapperFor() {
        val value = true
        val clazz = PreparedStatement::class.java

        every { mockPreparedStatement.isWrapperFor(clazz) } returns value
        val result = preparedStatementProxy.isWrapperFor(clazz)

        result shouldBe value
    }

    @Test
    fun testToString() {
        val value = "string"

        every { mockPreparedStatement.toString() } returns value
        val result = preparedStatementProxy.toString()

        result shouldBe value
    }
}
