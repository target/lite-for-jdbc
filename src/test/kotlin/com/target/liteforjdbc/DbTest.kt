package com.target.liteforjdbc

import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

class DbTest {

    @MockK(relaxed = true)
    lateinit var mockDataSource: DataSource

    @MockK(relaxed = true)
    lateinit var mockConnection: Connection

    @MockK(relaxed = true)
    lateinit var mockAutoCommit: AutoCommit

    private lateinit var db: Db
    private lateinit var partialDb: Db

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        db = Db(mockDataSource)

        partialDb = object : Db(mockDataSource) {
            override fun <T> withAutoCommit(block: (AutoCommit) -> T): T = block(mockAutoCommit)
        }

        every { mockDataSource.connection } returns mockConnection
        every { mockConnection.autoCommit } returns true
    }

    @Test
    fun executeUpdatePositionalParams() {
        val sql = "sql"
        val arg1 = 1
        val arg2 = "arg2"

        val expected = 2
        every { mockAutoCommit.executeUpdatePositionalParams(sql, arg1, arg2) } returns expected
        val result = partialDb.executeUpdatePositionalParams(sql, arg1, arg2)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeUpdatePositionalParams(sql, arg1, arg2) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun executeUpdate() {
        val sql = "sql"
        val mapArgs = emptyMap<String, Any?>()

        val expected = 2
        every { mockAutoCommit.executeUpdate(sql, mapArgs) } returns expected
        val result = partialDb.executeUpdate(sql, mapArgs)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeUpdate(sql, mapArgs) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun executeWithGeneratedKeysPositionalParams() {
        val sql = "sql"
        val arg1 = 1
        val arg2 = "arg2"
        val rowMapper = { resultSet: ResultSet -> resultSet.getString("test") }

        val expected = listOf("string")

        every { mockAutoCommit.executeWithGeneratedKeysPositionalParams(sql, rowMapper, arg1, arg2) } returns expected
        val result = partialDb.executeWithGeneratedKeysPositionalParams(sql, rowMapper, arg1, arg2)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeWithGeneratedKeysPositionalParams(sql, rowMapper, arg1, arg2) }
        confirmVerified(mockAutoCommit)

    }

    @Test
    fun executeWithGeneratedKeys() {
        val sql = "sql"
        val mapArgs = emptyMap<String, Any?>()
        val rowMapper = { resultSet: ResultSet -> resultSet.getString("test") }

        val expected = listOf("string")

        every { mockAutoCommit.executeWithGeneratedKeys(sql, mapArgs, rowMapper) } returns expected
        val result = partialDb.executeWithGeneratedKeys(sql, mapArgs, rowMapper)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeWithGeneratedKeys(sql, mapArgs, rowMapper) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun executeBatchPositionalParams() {
        val sql = "sql"
        val args = listOf(listOf(1, "arg2"))

        val expected = listOf(1)

        every { mockAutoCommit.executeBatchPositionalParams(sql, args) } returns expected
        val result = partialDb.executeBatchPositionalParams(sql, args)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeBatchPositionalParams(sql, args) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun executeBatch() {
        val sql = "sql"
        val args = listOf(mapOf("arg1" to 1, "arg2" to "arg2"))

        val expected = listOf(1)

        every { mockAutoCommit.executeBatch(sql, args) } returns expected
        val result = partialDb.executeBatch(sql, args)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeBatch(sql, args) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun executeQueryPositionalParams() {
        val sql = "sql"
        val arg1 = 1
        val arg2 = "arg2"
        val rowMapper = { resultSet: ResultSet -> resultSet.getString("test") }

        val expected = "string"

        every { mockAutoCommit.executeQueryPositionalParams(sql, rowMapper, arg1, arg2) } returns expected
        val result = partialDb.executeQueryPositionalParams(sql, rowMapper, arg1, arg2)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeQueryPositionalParams(sql, rowMapper, arg1, arg2) }
        confirmVerified(mockAutoCommit)

    }

    @Test
    fun executeQuery() {
        val sql = "sql"
        val mapArgs = emptyMap<String, Any?>()
        val rowMapper = { resultSet: ResultSet -> resultSet.getString("test") }

        val expected = "string"

        every { mockAutoCommit.executeQuery(sql, mapArgs, rowMapper) } returns expected
        val result = partialDb.executeQuery(sql, mapArgs, rowMapper)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.executeQuery(sql, mapArgs, rowMapper) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun findAllPositionalParams() {
        val sql = "sql"
        val arg1 = 1
        val arg2 = "arg2"
        val rowMapper = { resultSet: ResultSet -> resultSet.getString("test") }

        val expected = listOf("string")

        every { mockAutoCommit.findAllPositionalParams(sql, rowMapper, arg1, arg2) } returns expected
        val result = partialDb.findAllPositionalParams(sql, rowMapper, arg1, arg2)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.findAllPositionalParams(sql, rowMapper, arg1, arg2) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun findAll() {
        val sql = "sql"
        val mapArgs = emptyMap<String, Any?>()
        val rowMapper = { resultSet: ResultSet -> resultSet.getString("test") }

        val expected = listOf("string")

        every { mockAutoCommit.findAll(sql, mapArgs, rowMapper) } returns expected
        val result = partialDb.findAll(sql, mapArgs, rowMapper)

        result shouldBeSameInstanceAs expected
        verify { mockAutoCommit.findAll(sql, mapArgs, rowMapper) }
        confirmVerified(mockAutoCommit)
    }

    @Test
    fun useConnection() {
        var result: Connection? = null

        db.useConnection {
            result = it
        }

        result shouldBeSameInstanceAs mockConnection
        verify {
            mockConnection.close()
        }

        confirmVerified(mockConnection)
    }

    private data class Model(
        val name: String,
    )

}
