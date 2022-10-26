package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.*

class ConnectionSessionTest {

    @MockK(relaxed = true)
    lateinit var mockAutoCommitConnection: Connection

    @MockK(relaxed = true)
    lateinit var mockPreparedStatement: PreparedStatement

    @MockK(relaxed = true)
    lateinit var mockResultSet: ResultSet

    @MockK(relaxed = true)
    lateinit var mockInsertPreparedStatement: PreparedStatement

    @MockK(relaxed = true)
    lateinit var mockGeneratedKeysResultSet: ResultSet

    lateinit var autoCommit: AutoCommit

    @MockK(relaxed = true)
    lateinit var mockNotAutoCommitConnection: Connection
    lateinit var transaction: Transaction

    private val sql = "SELECT * FROM TABLE WHERE FIELD = ? OR FIELD2 = ?"
    private val insertSql = "INSERT INTO TABLE (FIELD, FIELD2) VALUES (?, ?)"

    private val name1 = "name1"
    private val name2 = "name2"

    private val generatedKey1 = 10
    private val generatedKey2 = 11

    private val batchRowCount1 = 1
    private val batchRowCount2 = 2

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockAutoCommitConnection.autoCommit } returns true
        autoCommit = AutoCommit(mockAutoCommitConnection)

        every { mockNotAutoCommitConnection.autoCommit } returns false
        transaction = Transaction(mockNotAutoCommitConnection)

        every { mockAutoCommitConnection.prepareStatement(sql) } returns mockPreparedStatement
        every { mockAutoCommitConnection.prepareStatement(insertSql) } returns mockInsertPreparedStatement
        every {
            mockAutoCommitConnection.prepareStatement(
                insertSql,
                Statement.RETURN_GENERATED_KEYS
            )
        } returns mockInsertPreparedStatement

        every { mockPreparedStatement.executeQuery() } returns mockResultSet
        every { mockResultSet.next() } returnsMany listOf(true, true, false)
        every { mockResultSet.getString("name") } returnsMany listOf(name1, name2)

        every { mockInsertPreparedStatement.executeUpdate() } returns 2
        every { mockInsertPreparedStatement.generatedKeys } returns mockGeneratedKeysResultSet
        every { mockGeneratedKeysResultSet.next() } returnsMany listOf(true, true, false)
        every { mockGeneratedKeysResultSet.getInt("id") } returnsMany listOf(generatedKey1, generatedKey2)

        every { mockInsertPreparedStatement.addBatch() } returns Unit
        every { mockInsertPreparedStatement.executeBatch() } returns intArrayOf(batchRowCount1, batchRowCount2)
    }

    @Test
    fun `Test initialize AutoCommit with auto commit already on`() {
        val mockConnection: Connection = mockk()
        every { mockConnection.autoCommit } returns true

        AutoCommit(mockConnection)

        verify(exactly = 0) { mockConnection.autoCommit = any() }
    }

    @Test
    fun `Test initialize AutoCommit with auto commit off`() {
        val mockConnection: Connection = mockk()

        every { mockConnection.autoCommit } returns false
        every { mockConnection.autoCommit = true } answers {}

        AutoCommit(mockConnection)

        verify(exactly = 1) { mockConnection.autoCommit = true }
    }

    @Test
    fun `Test initialize Transaction with auto commit on`() {
        val mockConnection: Connection = mockk()

        every { mockConnection.autoCommit } returns true
        every { mockConnection.autoCommit = false } answers {}

        Transaction(mockConnection)

        verify(exactly = 1) { mockConnection.autoCommit = false }
    }

    @Test
    fun `Test initialize Transaction with auto commit already off`() {
        val mockConnection: Connection = mockk()

        every { mockConnection.autoCommit } returns false

        Transaction(mockConnection)

        verify(exactly = 0) { mockConnection.autoCommit = any() }
    }

    @Test
    fun testUsePreparedStatement() {
        var result: PreparedStatement? = null

        every { mockAutoCommitConnection.autoCommit } returns true
        val autoCommit = AutoCommit(mockAutoCommitConnection)
        autoCommit.usePreparedStatement(sql) { innerStmt ->
            result = innerStmt
        }

        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)
            mockPreparedStatement.close()
        }
        result shouldBe mockPreparedStatement

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }

    @Test
    fun testUseNamedParamPreparedStatement() {

        val namedSql = "SELECT * FROM TABLE WHERE FIELD = :parameter OR FIELD2 = :other-parameter"

        var result: NamedParamPreparedStatement? = null

        autoCommit.useNamedParamPreparedStatement(namedSql) { innerStmt ->
            result = innerStmt
        }

        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)
            mockPreparedStatement.close()
        }
        result?.preparedStatement shouldBe mockPreparedStatement

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }

    @Test
    fun testExecuteUpdatePositionalParams() {
        val result = autoCommit.executeUpdatePositionalParams(insertSql)

        result shouldBe 2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql)
            mockInsertPreparedStatement.executeUpdate()
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteUpdatePositionalParamsWithArgs() {
        val result = autoCommit.executeUpdatePositionalParams(insertSql, "value", 1)

        result shouldBe 2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql)
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.executeUpdate()
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteUpdate() {
        val namedSql = "INSERT INTO TABLE (FIELD, FIELD2) VALUES (:parameter, :other-parameter)"

        val result = autoCommit.executeUpdate(namedSql, mapOf("parameter" to "value", "other-parameter" to 1))

        result shouldBe 2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql)
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.executeUpdate()
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteWithGeneratedKeysPositionalParams() {
        val result = autoCommit.executeWithGeneratedKeysPositionalParams(
            insertSql,
            { resultSet -> resultSet.getInt("id") },
            "value", 1
        )

        result.size shouldBe 2
        result[0] shouldBe generatedKey1
        result[1] shouldBe generatedKey2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.execute()
            mockInsertPreparedStatement.generatedKeys
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteWithGeneratedKeys() {
        val namedSql = "INSERT INTO TABLE (FIELD, FIELD2) VALUES (:fieldVal, :field2Val)"

        val result = autoCommit.executeWithGeneratedKeys(
            namedSql,
            mapOf("fieldVal" to "value", "field2Val" to 1)
        ) { resultSet -> resultSet.getInt("id") }

        result.size shouldBe 2
        result[0] shouldBe generatedKey1
        result[1] shouldBe generatedKey2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.execute()
            mockInsertPreparedStatement.generatedKeys
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteBatchPositionalParams() {
        val result = autoCommit.executeBatchPositionalParams(
            insertSql,
            listOf(
                listOf("value", 1),
                listOf("value2", 2)
            )
        )

        result.size shouldBe 2
        result[0] shouldBe batchRowCount1
        result[1] shouldBe batchRowCount2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value2")
            mockInsertPreparedStatement.setObject(2, 2)
            mockInsertPreparedStatement.executeBatch()
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteBatch() {
        val namedSql = "INSERT INTO TABLE (FIELD, FIELD2) VALUES (:fieldVal, :field2Val)"

        val result = autoCommit.executeBatch(
            namedSql,
            listOf(
                mapOf("fieldVal" to "value", "field2Val" to 1),
                mapOf("fieldVal" to "value2", "field2Val" to 2)
            )
        )

        result.size shouldBe 2
        result[0] shouldBe batchRowCount1
        result[1] shouldBe batchRowCount2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value2")
            mockInsertPreparedStatement.setObject(2, 2)
            mockInsertPreparedStatement.executeBatch()
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }


    @Test
    fun testExecuteBatchWithGeneratedKeys() {
        val namedSql = "INSERT INTO TABLE (FIELD, FIELD2) VALUES (:fieldVal, :field2Val)"

        val result = autoCommit.executeBatch(
            namedSql,
            listOf(
                mapOf("fieldVal" to "value", "field2Val" to 1),
                mapOf("fieldVal" to "value2", "field2Val" to 2)
            )
        ) { resultSet -> resultSet.getInt("id") }

        result.size shouldBe 2
        result[0] shouldBe generatedKey1
        result[1] shouldBe generatedKey2

        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value2")
            mockInsertPreparedStatement.setObject(2, 2)
            mockInsertPreparedStatement.executeBatch()
            mockInsertPreparedStatement.generatedKeys
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteBatchPositionalParamsWithGeneratedKeys() {
        val result = autoCommit.executeBatchPositionalParams(
            insertSql,
            listOf(
                listOf("value", 1),
                listOf("value2", 2)
            )
        ) { resultSet -> resultSet.getInt("id") }

        result.size shouldBe 2
        result[0] shouldBe generatedKey1
        result[1] shouldBe generatedKey2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value")
            mockInsertPreparedStatement.setObject(2, 1)
            mockInsertPreparedStatement.addBatch()
            mockInsertPreparedStatement.setObject(1, "value2")
            mockInsertPreparedStatement.setObject(2, 2)
            mockInsertPreparedStatement.executeBatch()
            mockInsertPreparedStatement.generatedKeys
            mockInsertPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockInsertPreparedStatement)
    }

    @Test
    fun testExecuteQueryPositionalParams() {
        val result = autoCommit.executeQueryPositionalParams(sql, { resultSet -> Model(resultSet.getString("name")) })

        checkNotNull(result)
        result.name shouldBe name1
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)
            mockPreparedStatement.executeQuery()
            mockPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }

    @Test
    fun testExecuteQueryPositionalParamsWithArgs() {
        val result = autoCommit.executeQueryPositionalParams(
            sql,
            { resultSet -> Model(resultSet.getString("name")) },
            "value", 1
        )

        checkNotNull(result)
        result.name shouldBe name1
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)
            mockPreparedStatement.setObject(1, "value")
            mockPreparedStatement.setObject(2, 1)
            mockPreparedStatement.executeQuery()
            mockPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }

    @Test
    fun testExecuteQuery() {
        val namedSql = "SELECT * FROM TABLE WHERE FIELD = :parameter OR FIELD2 = :other-parameter"

        val result = autoCommit.executeQuery(
            namedSql,
            mapOf("parameter" to "value", "other-parameter" to 1)
        ) { resultSet -> Model(resultSet.getString("name")) }

        checkNotNull(result)
        result.name shouldBe name1
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)
            mockPreparedStatement.setObject(1, "value")
            mockPreparedStatement.setObject(2, 1)
            mockPreparedStatement.executeQuery()
            mockPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }


    @Test
    fun testFindAllPositionalParams() {
        val result = autoCommit.findAllPositionalParams(sql, { resultSet -> Model(resultSet.getString("name")) })

        result.size shouldBe 2
        result[0].name shouldBe name1
        result[1].name shouldBe name2

        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)
            mockPreparedStatement.executeQuery()
            mockPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }

    @Test
    fun testFindAllPositionalParamsWithArgs() {
        val result = autoCommit.findAllPositionalParams(
            sql,
            { resultSet -> Model(resultSet.getString("name")) },
            "value", 1
        )

        result.size shouldBe 2
        result[0].name shouldBe name1
        result[1].name shouldBe name2
        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)

            mockPreparedStatement.setObject(1, "value")
            mockPreparedStatement.setObject(2, 1)
            mockPreparedStatement.executeQuery()
            mockPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }

    @Test
    fun testFindAll() {
        val namedSql = "SELECT * FROM TABLE WHERE FIELD = :parameter OR FIELD2 = :other-parameter"

        val result = autoCommit.findAll(
            namedSql,
            mapOf("parameter" to "value", "other-parameter" to 1)
        ) { resultSet -> Model(resultSet.getString("name")) }

        result.size shouldBe 2
        result[0].name shouldBe name1
        result[1].name shouldBe name2

        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.prepareStatement(sql)

            mockPreparedStatement.setObject(1, "value")
            mockPreparedStatement.setObject(2, 1)
            mockPreparedStatement.executeQuery()
            mockPreparedStatement.close()
        }

        confirmVerified(mockAutoCommitConnection, mockPreparedStatement)
    }

    @Test
    fun `test isHealthy`() {
        every { mockAutoCommitConnection.isClosed } answers { false }

        val result = autoCommit.isHealthy()

        result shouldBe true
    }

    @Test
    fun `test close`() {
        every { mockAutoCommitConnection.close() } answers {}

        autoCommit.close()

        verifyAll {
            mockAutoCommitConnection.autoCommit
            mockAutoCommitConnection.close()
        }

        confirmVerified(mockAutoCommitConnection)
    }

    @Test
    fun `test commit`() {
        every { mockNotAutoCommitConnection.commit() } answers { }

        transaction.commit()

        verifyAll {
            mockNotAutoCommitConnection.autoCommit
            mockNotAutoCommitConnection.commit()
        }

        confirmVerified(mockNotAutoCommitConnection)
    }

    @Test
    fun `test savepoint`() {
        val mockSavepoint: Savepoint = mockk()
        every { mockNotAutoCommitConnection.setSavepoint() } answers { mockSavepoint }

        val result = transaction.savepoint()

        result shouldBeSameInstanceAs mockSavepoint
        verifyAll {
            mockNotAutoCommitConnection.autoCommit
            mockNotAutoCommitConnection.setSavepoint()
        }

        confirmVerified(mockNotAutoCommitConnection)
    }

    @Test
    fun `test savepoint with name`() {
        val mockSavepoint: Savepoint = mockk()
        every { mockNotAutoCommitConnection.setSavepoint("name") } answers { mockSavepoint }

        val result = transaction.savepoint("name")

        result shouldBeSameInstanceAs mockSavepoint
        verifyAll {
            mockNotAutoCommitConnection.autoCommit
            mockNotAutoCommitConnection.setSavepoint("name")
        }

        confirmVerified(mockNotAutoCommitConnection)
    }

    @Test
    fun `test rollback`() {
        every { mockNotAutoCommitConnection.rollback() } answers { }

        transaction.rollback()

        verifyAll {
            mockNotAutoCommitConnection.autoCommit
            mockNotAutoCommitConnection.rollback()
        }

        confirmVerified(mockNotAutoCommitConnection)
    }

    @Test
    fun `test rollback to savepoint`() {
        val mockSavepoint: Savepoint = mockk()
        every { mockNotAutoCommitConnection.rollback(mockSavepoint) } answers { }

        transaction.rollback(mockSavepoint)

        verifyAll {
            mockNotAutoCommitConnection.autoCommit
            mockNotAutoCommitConnection.rollback(mockSavepoint)
        }

        confirmVerified(mockNotAutoCommitConnection)
    }

    private data class Model(
        val name: String,
    )

}
