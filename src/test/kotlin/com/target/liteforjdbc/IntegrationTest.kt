package com.target.liteforjdbc

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.h2.jdbcx.JdbcDataSource
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.ResultSet
import java.time.Instant


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTest {
    private lateinit var db: Db

    private val countResultSetMap = { resultSet: ResultSet -> resultSet.getInt("cnt") }
    @BeforeAll
    fun setupClass() {
        val dataSource = JdbcDataSource()
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        dataSource.user = "sa"
        dataSource.password = ""

        db = Db(dataSource)

        // Setup
        db.executeUpdate("CREATE TABLE T ( id INT, field1 VARCHAR(255), field2 INT, field3 TIMESTAMP )")
        db.executeUpdate("INSERT INTO T (id, field1, field2, field3) VALUES (1, 'First', 10, TIMESTAMP '1970-01-01 00:00:00')")
        db.executeUpdate("INSERT INTO T (id, field1, field2, field3) VALUES (2, 'Second', 20, TIMESTAMP '1970-01-01 00:00:01')")
        db.executeUpdate("INSERT INTO T (id, field1, field2, field3) VALUES (3, 'Third', 30, TIMESTAMP '1970-01-01 00:00:02')")

        db.executeUpdate("CREATE TABLE KEY_GEN_T ( id INT AUTO_INCREMENT, field1 VARCHAR(255), field2 INT, field3 TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
    }

    @Test
    fun testUseConnection() {
        db.useConnection { conn ->
            val ps = conn.prepareStatement("SHOW TABLES;")
            val rs = ps.executeQuery()
            rs.next() shouldBe true
        }
    }

    @Test
    fun testUsePreparedStatement() {
        db.usePreparedStatement("SHOW TABLES;") { ps ->
            val rs = ps.executeQuery()
            rs.next() shouldBe true
        }
    }

    @Test
    fun testUseNamedParamPreparedStatement() {
        db.useNamedParamPreparedStatement("SELECT * FROM T WHERE id = :id") { ps ->
            ps.setInt("id", 1)
            val rs = ps.executeQuery()
            rs.next() shouldBe true
            val field1Val = rs.getString("field1")
            field1Val shouldBe "First"
        }
    }

    @Test
    fun testExecuteUpdate() {
        db.executeUpdate("INSERT INTO T (id, field1, field2) VALUES (123, 'Temp', 10)")
        var count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = 123", rowMapper = countResultSetMap)
        count shouldBe 1
        db.executeUpdate("DELETE FROM T WHERE id = 123")
        count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = 123", rowMapper = countResultSetMap)
        count shouldBe 0
    }

    @Test
    fun `a reused named parameter should populate all ordinal parameters`() {
        val count = db.executeQuery(
            sql = "SELECT COUNT(*) cnt FROM T WHERE id = :id and id = :id",
            args = mapOf("id" to 1),
            rowMapper = countResultSetMap
        )
        count shouldBe 1
    }

    @Test
    fun testExecuteUpdatePositionalParams() {
        db.executeUpdatePositionalParams(
            "INSERT INTO T (id, field1, field2, field3) VALUES (?, ?, ?, ?)",
            123,
            "Temp",
            10,
            Instant.ofEpochMilli(0)
        )
        var count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = 123", rowMapper = countResultSetMap)
        count shouldBe 1
        db.executeUpdatePositionalParams("DELETE FROM T WHERE id = ?", 123)
        count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = 123", rowMapper = countResultSetMap)
        count shouldBe 0
    }

    @Test
    fun testExecuteWithParams() {
        db.executeUpdate(
            "INSERT INTO T (id, field1, field2, field3) VALUES (:id, :field1, :field2, :field3)",
            mapOf("id" to 123, "field1" to "Temp", "field2" to 10, "field3" to Instant.ofEpochMilli(0))
        )
        var count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = 123", rowMapper = countResultSetMap)
        count shouldBe 1
        db.executeUpdate(
            "DELETE FROM T WHERE id = :id",
            mapOf("id" to 123)
        )
        count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = 123", rowMapper = countResultSetMap)
        count shouldBe 0
    }

    @Test
    fun testExecuteQuery() {
        val result = db.executeQuery(
            "SELECT COUNT(*) cnt FROM T",
            rowMapper = countResultSetMap
        )


        checkNotNull(result)
        result::class shouldBe Int::class
        result shouldBe 3
    }

    @Test
    fun testExecuteWithGeneratedKeys() {
        clearKeyGenTable()

        val ids = db.executeWithGeneratedKeys(
            "INSERT INTO KEY_GEN_T (field1, field2, field3) VALUES (:field1, :field2, :field3)",
            mapOf("field1" to "Temp", "field2" to 10, "field3" to Instant.ofEpochMilli(0))
        ) { resultSet: ResultSet -> resultSet.getInt("id") }

        val finalCount = tableKeyGenCount()

        val expectedId = tableKeyGenIdByField1("Temp")
        clearKeyGenTable()

        ids.size shouldBe 1
        ids[0] shouldBe expectedId
        finalCount shouldBe 1
    }

    @Test
    fun testExecuteWithGeneratedKeysPositionalParams() {
        clearKeyGenTable()

        val ids = db.executeWithGeneratedKeysPositionalParams(
            "INSERT INTO KEY_GEN_T (field1, field2, field3) VALUES (?, ?, ?)",
            { resultSet: ResultSet -> resultSet.getInt("id") },
            "Temp", 10, Instant.ofEpochMilli(0)
        )

        val finalCount = tableKeyGenCount()

        val expectedId = tableKeyGenIdByField1("Temp")
        clearKeyGenTable()

        ids.size shouldBe 1
        ids[0] shouldBe expectedId
        finalCount shouldBe 1
    }

    @Test
    fun testExecuteBatch() {
        clearKeyGenTable()

        val result = db.executeBatch(
            "INSERT INTO KEY_GEN_T (field1, field2, field3) VALUES (:field1, :field2, :field3)",
            listOf(
                mapOf("field1" to "Temp", "field2" to 10, "field3" to Instant.ofEpochMilli(0)),
                mapOf("field1" to "Temp2", "field2" to 11, "field3" to Instant.ofEpochMilli(1)),
            )
        )

        val finalCount = tableKeyGenCount()
        clearKeyGenTable()

        result.size shouldBe 2
        result[0] shouldBe 1
        result[1] shouldBe 1
        finalCount shouldBe 2
    }

    @Test
    fun testExecuteBatchWithGeneratedKeys() {
        clearKeyGenTable()

        val result = db.executeBatch(
            "INSERT INTO KEY_GEN_T (field1, field2, field3) VALUES (:field1, :field2, :field3)",
            listOf(
                mapOf("field1" to "Temp", "field2" to 10, "field3" to Instant.ofEpochMilli(0)),
                mapOf("field1" to "Temp2", "field2" to 11, "field3" to Instant.ofEpochMilli(1)),
            )
        ) { resultSet: ResultSet -> resultSet.getInt("id") }

        val finalCount = tableKeyGenCount()
        val expectedId1 = tableKeyGenIdByField1("Temp")
        val expectedId2 = tableKeyGenIdByField1("Temp2")
        clearKeyGenTable()

        result.size shouldBe 2
        result[0] shouldBe expectedId1
        result[1] shouldBe expectedId2
        finalCount shouldBe 2
    }

    @Test
    fun testExecuteBatchPositionalParams() {
        clearKeyGenTable()

        val rowCounts = db.executeBatchPositionalParams(
            "INSERT INTO KEY_GEN_T (field1, field2, field3) VALUES (?, ?, ?)",
            listOf(
                listOf("Temp", 10, Instant.ofEpochMilli(0)),
                listOf("Temp2", 11, Instant.ofEpochMilli(1)),
            )
        )

        val finalCount = tableKeyGenCount()
        clearKeyGenTable()

        rowCounts.size shouldBe 2
        rowCounts[0] shouldBe 1
        rowCounts[0] shouldBe 1
        finalCount shouldBe 2
    }

    @Test
    fun testExecuteBatchPositionalParamsWithGeneratedKeys() {
        clearKeyGenTable()

        val result = db.executeBatchPositionalParams(
            "INSERT INTO KEY_GEN_T (field1, field2, field3) VALUES (?, ?, ?)",
            listOf(
                listOf("Temp", 10, Instant.ofEpochMilli(0)),
                listOf("Temp2", 11, Instant.ofEpochMilli(1)),
            )
        ) { resultSet: ResultSet -> resultSet.getInt("id") }

        val finalCount = tableKeyGenCount()
        val expectedId1 = tableKeyGenIdByField1("Temp")
        val expectedId2 = tableKeyGenIdByField1("Temp2")
        clearKeyGenTable()

        result.size shouldBe 2
        result[0] shouldBe expectedId1
        result[1] shouldBe expectedId2
        finalCount shouldBe 2
    }

    @Test
    fun testExecuteQueryPositionalParams() {
        val result = db.executeQueryPositionalParams(
            "SELECT COUNT(*) cnt FROM T WHERE field2 > ?",
            countResultSetMap,
            15
        )

        checkNotNull(result)
        result::class shouldBe Int::class
        result shouldBe 2
    }

    @Test
    fun testExecuteQueryWithParams() {
        val result = db.executeQuery(
            "SELECT COUNT(*) cnt FROM T WHERE field2 > :field2Min",
            mapOf("field2Min" to 15),
            countResultSetMap
        )

        checkNotNull(result)
        result::class shouldBe Int::class
        result shouldBe 2
    }

    @Test
    fun testFindAll() {
        val result = db.findAll(
            "SELECT * FROM T ORDER BY field3",
        ) { resultSet -> resultSet.toModel() }

        result.size shouldBe 3
        result[0]::class shouldBe Model::class
        result[0].id shouldBe 1
        result[0].field3 shouldBe Instant.ofEpochMilli(0)
        result[1]::class shouldBe Model::class
        result[1].id shouldBe 2
        result[1].field3 shouldBe Instant.ofEpochMilli(1000)

    }

    @Test
    fun testFindAllPositionalParams() {
        val result = db.findAllPositionalParams(
            "SELECT * FROM T WHERE field2 > ?",
            { resultSet -> resultSet.toModel() },
            15
        )

        result.size shouldBe 2
        result[0]::class shouldBe Model::class
    }

    @Test
    fun testFindAllWithParams() {
        val result = db.findAll(
            "SELECT * FROM T WHERE field2 > :field2Min",
            mapOf("field2Min" to 15)
        ) { resultSet -> resultSet.toModel() }

        result.size shouldBe 2
        result[0]::class shouldBe Model::class

    }

    @Test
    fun testSaveAndDelete() {
        val originalCount = tableCount()

        val model = Model(100, "testName", 1000, Instant.ofEpochMilli(0))
        db.executeUpdate(
            "INSERT INTO T (id, field1, field2, field3) VALUES (:id, :field1, :field2, :field3)",
            model.toMap()
        )

        val newCount = tableCount()

        newCount shouldBe originalCount + 1

        val result = checkNotNull(db.executeQuery(
            "SELECT * FROM T WHERE id = :id",
            mapOf("id" to 100)
        ) { resultSet -> resultSet.toModel() })

        result shouldBe model
        result.field3 shouldBe Instant.ofEpochMilli(0)

        db.executeUpdate(
            "DELETE FROM T WHERE id = :id",
            model.toMap()
        )

        val finalCount = tableCount()

        finalCount shouldBe originalCount
    }

    private fun tableCount(): Int {
        return checkNotNull(db.executeQuery(
            "SELECT COUNT(*) cnt FROM T"
        ) { resultSet -> resultSet.getInt("cnt") })
    }

    private fun tableKeyGenCount(): Int {
        return checkNotNull(db.executeQuery(
            "SELECT COUNT(*) cnt FROM KEY_GEN_T"
        ) { resultSet -> resultSet.getInt("cnt") })
    }

    private fun tableKeyGenIdByField1(field1Val: String): Int {
        return checkNotNull(db.executeQuery(
            "SELECT id FROM KEY_GEN_T WHERE field1 = :field1Val",
            mapOf( "field1Val" to field1Val )
        ) { resultSet -> resultSet.getInt("id") })
    }

    private fun clearKeyGenTable() {
        db.executeUpdate("DELETE FROM KEY_GEN_T")
    }

    data class Model(
        val id: Int,
        val field1: String,
        val field2: Int,
        val field3: Instant,
    ) {
        fun toMap(): Map<String, Any> {
            return mapOf("id" to id, "field1" to field1, "field2" to field2, "field3" to field3)
        }
    }

    private fun ResultSet.toModel(): Model = Model(
        id = getInt("id"),
        field1 = getString("field1"),
        field2 = getInt("field2"),
        field3 = checkNotNull(getInstant("field3"))
    )
}
