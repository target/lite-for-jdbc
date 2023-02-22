package com.target.liteforjdbc.h2

import com.target.liteforjdbc.*
import com.target.liteforjdbc.integration.AnnoyedParent
import com.target.liteforjdbc.integration.Model
import com.target.liteforjdbc.integration.countResultSetMap
import com.target.liteforjdbc.integration.modelResultSetMap
import com.target.liteforjdbc.postgres.PostgresSqlIntegrationTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.time.Instant
import java.util.*

class H2IntegrationTest {
    lateinit var db: Db
    val knownUUIDList = mutableListOf<UUID>()

    @BeforeEach
    fun setupClass() {
        val dbConfig = DbConfig(
            type = DbType.H2_INMEM,
            username = "user",
            password = "password",
            databaseName = "H2IntegrationTest"
        )

        db = Db(dbConfig)

        // Setup
        db.executeUpdate("CREATE TABLE T ( id UUID default random_uuid(), field1 VARCHAR(255), field2 INT, field3 TIMESTAMP, annoyed_parent ENUM('ONE', 'TWO', 'TWO_AND_A_HALF', 'THREE') )")

        db.executeUpdate("CREATE TABLE KEY_GEN_T ( id UUID default random_uuid(), field1 VARCHAR(255), field2 INT, field3 TIMESTAMP DEFAULT CURRENT_TIMESTAMP, annoyed_parent ENUM('ONE', 'TWO', 'TWO_AND_A_HALF', 'THREE') )")
        // Setup
        val firstUUID = UUID.randomUUID()
        knownUUIDList.add(firstUUID)
        db.executeUpdate("INSERT INTO T (id, field1, field2, field3, annoyed_parent) VALUES ('$firstUUID', 'First', 10, TIMESTAMP '1970-01-01 00:00:00', 'ONE')")
        val secondUUID = UUID.randomUUID()
        knownUUIDList.add(secondUUID)
        db.executeUpdate("INSERT INTO T (id, field1, field2, field3, annoyed_parent) VALUES ('$secondUUID', 'Second', 20, TIMESTAMP '1970-01-01 00:00:01', 'TWO')")
        val thirdUUID = UUID.randomUUID()
        knownUUIDList.add(thirdUUID)
        db.executeUpdate("INSERT INTO T (id, field1, field2, field3, annoyed_parent) VALUES ('$thirdUUID', 'Third', 30, TIMESTAMP '1970-01-01 00:00:02', 'TWO_AND_A_HALF')")
    }

    @AfterEach
    fun teardown() {
        db.executeUpdate("DROP TABLE T")

        db.executeUpdate("DROP TABLE KEY_GEN_T")
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
            ps.setUUID("id", knownUUIDList[0])
            val rs = ps.executeQuery()
            rs.next() shouldBe true
            val field1Val = rs.getString("field1")
            field1Val shouldBe "First"
        }
    }

    @Test
    fun testExecuteUpdate() {
        val id = UUID.randomUUID()
        db.executeUpdate("INSERT INTO T (id, field1, field2, annoyed_parent) VALUES ('$id', 'Temp', 10, 'ONE')")
        var count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = :id", args = mapOf("id" to id), rowMapper = countResultSetMap)
        count shouldBe 1
        db.executeUpdate("DELETE FROM T WHERE id = :id", args = mapOf("id" to id))
        count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = :id", args = mapOf("id" to id), rowMapper = countResultSetMap)
        count shouldBe 0
    }

    @Test
    fun `a reused named parameter should populate all ordinal parameters`() {
        val count = db.executeQuery(
            sql = "SELECT COUNT(*) cnt FROM T WHERE id = :id and id = :id",
            args = mapOf("id" to knownUUIDList[0]),
            rowMapper = countResultSetMap
        )
        count shouldBe 1
    }

    @Test
    fun testExecuteUpdatePositionalParams() {
        val id = UUID.randomUUID()
        db.executeUpdatePositionalParams(
            "INSERT INTO T (id, field1, field2, field3, annoyed_parent) VALUES (?, ?, ?, ?, ?)",
            id,
            "Temp",
            10,
            Instant.ofEpochMilli(0),
            AnnoyedParent.TWO
        )
        var count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = :id",
            args = mapOf("id" to id),
            rowMapper = countResultSetMap)
        count shouldBe 1
        db.executeUpdatePositionalParams("DELETE FROM T WHERE id = ?", id)
        count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = :id",
            args = mapOf("id" to id),
            rowMapper = countResultSetMap)
        count shouldBe 0
    }

    @Test
    fun testExecuteWithParams() {
        val id = UUID.randomUUID()
        db.executeUpdate(
            "INSERT INTO T (id, field1, field2, field3, annoyed_parent) VALUES (:id, :field1, :field2, :field3, :annoyedParent)",
            mapOf(
                "id" to id,
                "field1" to "Temp",
                "field2" to 10,
                "field3" to Instant.ofEpochMilli(0),
                "annoyedParent" to AnnoyedParent.TWO
            )
        )
        var count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = :id",
            args = mapOf("id" to id),
            rowMapper = countResultSetMap)
        count shouldBe 1
        db.executeUpdate(
            "DELETE FROM T WHERE id = :id",
            mapOf("id" to id)
        )
        count = db.executeQuery("SELECT COUNT(*) cnt FROM T WHERE id = :id",
            args = mapOf("id" to id),
            rowMapper = countResultSetMap)
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
            "INSERT INTO KEY_GEN_T (field1, field2, field3, annoyed_parent) VALUES (:field1, :field2, :field3, :annoyedParent)",
            mapOf(
                "field1" to "Temp",
                "field2" to 10,
                "field3" to Instant.ofEpochMilli(0),
                "annoyedParent" to AnnoyedParent.THREE
            )
        ) { resultSet: ResultSet -> resultSet.getUUID("id") }

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
            "INSERT INTO KEY_GEN_T (field1, field2, field3, annoyed_parent) VALUES (?, ?, ?, ?)",
            { resultSet: ResultSet -> resultSet.getUUID("id") },
            "Temp", 10, Instant.ofEpochMilli(0), AnnoyedParent.TWO_AND_A_HALF
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
            "INSERT INTO KEY_GEN_T (field1, field2, field3, annoyed_parent) VALUES (:field1, :field2, :field3, :annoyedParent)",
            listOf(
                mapOf(
                    "field1" to "Temp",
                    "field2" to 10,
                    "field3" to Instant.ofEpochMilli(0),
                    "annoyedParent" to AnnoyedParent.ONE
                ),
                mapOf(
                    "field1" to "Temp2",
                    "field2" to 11,
                    "field3" to Instant.ofEpochMilli(1),
                    "annoyedParent" to AnnoyedParent.TWO
                ),
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
            "INSERT INTO KEY_GEN_T (field1, field2, field3, annoyed_parent) VALUES (:field1, :field2, :field3, :annoyedParent)",
            listOf(
                mapOf(
                    "field1" to "Temp",
                    "field2" to 10,
                    "field3" to Instant.ofEpochMilli(0),
                    "annoyedParent" to AnnoyedParent.ONE
                ),
                mapOf(
                    "field1" to "Temp2",
                    "field2" to 11,
                    "field3" to Instant.ofEpochMilli(1),
                    "annoyedParent" to AnnoyedParent.TWO_AND_A_HALF
                ),
            )
        ) { resultSet: ResultSet -> resultSet.getUUID("id") }

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
            "INSERT INTO KEY_GEN_T (field1, field2, field3, annoyed_parent) VALUES (?, ?, ?, ?)",
            listOf(
                listOf("Temp", 10, Instant.ofEpochMilli(0), AnnoyedParent.TWO),
                listOf("Temp2", 11, Instant.ofEpochMilli(1), AnnoyedParent.TWO_AND_A_HALF),
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
            "INSERT INTO KEY_GEN_T (field1, field2, field3, annoyed_parent) VALUES (?, ?, ?, ?)",
            listOf(
                listOf("Temp", 10, Instant.ofEpochMilli(0), AnnoyedParent.ONE),
                listOf("Temp2", 11, Instant.ofEpochMilli(1), AnnoyedParent.TWO),
            )
        ) { resultSet: ResultSet -> resultSet.getUUID("id") }

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
            sql = "SELECT * FROM T ORDER BY field3",
            rowMapper = modelResultSetMap
        )

        result.size shouldBe 3
        result[0]::class shouldBe Model::class
        result[0].id shouldBe knownUUIDList[0]
        result[0].field3 shouldBe Instant.ofEpochMilli(0)
        result[0].annoyedParent shouldBe AnnoyedParent.ONE
        result[1]::class shouldBe Model::class
        result[1].id shouldBe knownUUIDList[1]
        result[1].field3 shouldBe Instant.ofEpochMilli(1000)
        result[1].annoyedParent shouldBe AnnoyedParent.TWO
        result[2]::class shouldBe Model::class
        result[2].id shouldBe knownUUIDList[2]
        result[2].field3 shouldBe Instant.ofEpochMilli(2000)
        result[2].annoyedParent shouldBe AnnoyedParent.TWO_AND_A_HALF

    }

    @Test
    fun testFindAllPositionalParams() {
        val result = db.findAllPositionalParams(
            "SELECT * FROM T WHERE field2 > ?",
            rowMapper = modelResultSetMap,
            15
        )

        result.size shouldBe 2
        result[0]::class shouldBe Model::class
    }

    @Test
    fun testFindAllWithParams() {
        val result = db.findAll(
            "SELECT * FROM T WHERE field2 > :field2Min",
            mapOf("field2Min" to 15),
            rowMapper = modelResultSetMap,
        )

        result.size shouldBe 2
        result[0]::class shouldBe Model::class

    }

    @Test
    fun testSaveAndDelete() {
        val originalCount = tableCount()

        val model = Model(UUID.randomUUID(), "testName", 1000, Instant.ofEpochMilli(0), AnnoyedParent.TWO)
        db.executeUpdate(
            "INSERT INTO T (id, field1, field2, field3, annoyed_parent) VALUES (:id, :field1, :field2, :field3, :annoyedParent)",
            model.propertiesToMap()
        )

        val newCount = tableCount()

        newCount shouldBe originalCount + 1

        val result = checkNotNull(
            db.executeQuery(
                "SELECT * FROM T WHERE id = :id",
                mapOf("id" to model.id),
                modelResultSetMap
            )
        )

        result shouldBe model
        result.field3 shouldBe Instant.ofEpochMilli(0)

        db.executeUpdate(
            "DELETE FROM T WHERE id = :id",
            model.propertiesToMap()
        )

        val finalCount = tableCount()

        finalCount shouldBe originalCount
    }

    fun tableCount(): Int {
        return checkNotNull(db.executeQuery(
            "SELECT COUNT(*) cnt FROM T"
        ) { resultSet -> resultSet.getInt("cnt") })
    }

    fun tableKeyGenCount(): Int {
        return checkNotNull(db.executeQuery(
            "SELECT COUNT(*) cnt FROM KEY_GEN_T"
        ) { resultSet -> resultSet.getInt("cnt") })
    }

    fun tableKeyGenIdByField1(field1Val: String): UUID {
        return checkNotNull(db.executeQuery(
            "SELECT id FROM KEY_GEN_T WHERE field1 = :field1Val",
            mapOf("field1Val" to field1Val)
        ) { resultSet -> resultSet.getUUID("id") })
    }

    fun clearKeyGenTable() {
        db.executeUpdate("DELETE FROM KEY_GEN_T")
    }

}
