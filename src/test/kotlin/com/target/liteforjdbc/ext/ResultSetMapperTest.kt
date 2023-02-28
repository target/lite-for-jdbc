package com.target.liteforjdbc.ext

import com.target.liteforjdbc.RowMapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResultSetMapperTest {

  @MockK(relaxed = true)
  lateinit var mockExampleResultSet1: ResultSet

  @MockK(relaxed = true)
  lateinit var mockExampleResultSet2: ResultSet

  @BeforeEach
  fun setUp() {
    MockKAnnotations.init(this)

    every { mockExampleResultSet1.getInt("id") }.answers { 42 }
    every { mockExampleResultSet1.getString("name") }.answers { "Target" }
    every { mockExampleResultSet1.getTimestamp("instant") }.answers { Timestamp(0) }
    every { mockExampleResultSet1.getBoolean("enabled") }.answers { true }
    every { mockExampleResultSet1.getObject("global_id") }.answers { UUID.fromString("4ad9d3c5-e868-4f19-9a7a-6fced128d230") }
    every { mockExampleResultSet1.getString("ldap_name") }.answers { "TargetUser" }
    every { mockExampleResultSet1.getString("status") }.answers { "Offline" }
    every { mockExampleResultSet1.getString("no_default_mapping") }.answers { "I need remapping" }

    every { mockExampleResultSet2.getInt("id") }.answers { 23 }
    every { mockExampleResultSet2.getString("name") }.answers { "NotTarget" }
    every { mockExampleResultSet2.getTimestamp("instant") }.answers { Timestamp(1677593073027) }
    every { mockExampleResultSet2.getBoolean("enabled") }.answers { false }
    every { mockExampleResultSet2.getObject("global_id") }.answers { UUID.fromString("5ad9d3c5-e868-4f19-9a7a-6fced128d230") }
    every { mockExampleResultSet2.getString("ldap_name") }.answers { "NotTargetUser" }
    every { mockExampleResultSet2.getString("status") }.answers { "Online" }
    every { mockExampleResultSet2.getString("no_default_mapping") }.answers { "I have been remapped if you can read this" }

    every { mockExampleResultSet1.metaData }.answers { MockSuperSimpleMetaData }
    every { mockExampleResultSet2.metaData }.answers { MockSuperSimpleMetaData }
  }

  @Test
  fun `All values have been bound from result set`() {
    val statusTransformer: Transform = { x -> Status.valueOf(x.toString()) }
    val result: Example = mapRow(mockExampleResultSet2,
      transform = mapOf("status" to statusTransformer),
      remap = mapOf("remapMe" to "no_default_mapping")
    )
    val expected = Example(
      id=23, name = "NotTarget",
      instant = Timestamp(1677593073027).toInstant(),
      enabled = false,
      ldapName = "NotTargetUser",
      globalId = UUID.fromString("5ad9d3c5-e868-4f19-9a7a-6fced128d230"),
      status = Status.Online,
      remapMe = "I have been remapped if you can read this"
    )

    assertEquals(expected, result)
  }

  @Test
  fun `Option 'skip' ignores value`() {
    val simple: Example = mapRow(mockExampleResultSet1, skip = setOf("status"))
    assertEquals(Example(), simple)
  }

  @Test
  fun `Option 'transform' supplies new value`() {
    val statusTransformer: Transform = { x -> Status.valueOf(x.toString()) }

    val rowMapper: RowMapper<Example> = { rs ->
      mapRow(
        rs = rs,
        transform = mapOf("status" to statusTransformer)
      )
    }

    val simple = rowMapper(mockExampleResultSet1)

    assertEquals(Example(), simple)
  }


  @Test
  fun `Option 'valueFor' supplies value`() {
    val simple: Example = mapRow(
      rs = mockExampleResultSet1,
      valueFor = mapOf("name" to "Target Warehouse"),
      skip = setOf("status")
    )
    assertEquals(Example(name = "Target Warehouse"), simple)
  }


  @Test
  fun `Option 'remap' applies alternate value value`() {
    val simple: Example = mapRow(
      rs = mockExampleResultSet1,
      skip = setOf("status"),
      remap = mapOf("remapMe" to "no_default_mapping")
    )
    assertEquals(Example(remapMe = "I need remapping"), simple)
  }


  @Test
  fun `All options used`() {
    val simple: Example = mapRow(
      rs = mockExampleResultSet1,
      valueFor = mapOf("phone" to "xxx-xxx-xxxx"),
      transform = mapOf("username" to { x -> (x as? String)?.lowercase() }),
      skip = setOf("status"),
      remap = mapOf("someProperty" to "some_column_name")
    )
    assertEquals(Example(remapMe = "I need remapping"), simple)
  }
}

data class Example(
  val id: Int = 42,
  val name: String = "Target",
  val instant: Instant = Timestamp(0).toInstant(),
  val enabled: Boolean = true,
  val ldapName: String = "TargetUser",
  val globalId: UUID = UUID.fromString("4ad9d3c5-e868-4f19-9a7a-6fced128d230"),
  val status: Status = Status.Offline,
  val remapMe: String = "I need remapping"
)

enum class Status {
  Online,
  Offline
}

private object MockSuperSimpleMetaData : MockResultSetMetaData() {
  override fun getColumnName(column: Int): String = when (column) {
    1 -> "id"
    2 -> "name"
    3 -> "instant"
    4 -> "enabled"
    5 -> "global_id"
    6 -> "ldap_name"
    7 -> "status"
    8 -> "no_default_mapping"
    else -> error("Column $column not mapped for test case")
  }

  override fun getColumnType(column: Int): Int = when (column) {
    1 -> SqlType.INTEGER.type
    2, 6, 7, 8 -> SqlType.VARCHAR.type
    3 -> SqlType.TIMESTAMP_WITH_TIMEZONE.type
    4 -> SqlType.BOOLEAN.type
    5 -> SqlType.JAVA_OBJECT.type
    else -> error("Column $column not mapped for test case")
  }

  override fun getColumnCount(): Int = 8
}

private open class MockResultSetMetaData : ResultSetMetaData {
  override fun <T : Any?> unwrap(iface: Class<T>?): T = TODO("Not implemented")
  override fun isWrapperFor(iface: Class<*>?): Boolean = TODO("Not implemented")
  override fun getColumnCount(): Int = TODO("Not implemented")
  override fun isAutoIncrement(column: Int): Boolean = TODO("Not implemented")
  override fun isCaseSensitive(column: Int): Boolean = TODO("Not implemented")
  override fun isSearchable(column: Int): Boolean = TODO("Not implemented")
  override fun isCurrency(column: Int): Boolean = TODO("Not implemented")
  override fun isNullable(column: Int): Int = TODO("Not implemented")
  override fun isSigned(column: Int): Boolean = TODO("Not implemented")
  override fun getColumnDisplaySize(column: Int): Int = TODO("Not implemented")
  override fun getColumnLabel(column: Int): String = TODO("Not implemented")
  override fun getColumnName(column: Int): String = TODO("Not implemented")
  override fun getSchemaName(column: Int): String = TODO("Not implemented")
  override fun getPrecision(column: Int): Int = TODO("Not implemented")
  override fun getScale(column: Int): Int = TODO("Not implemented")
  override fun getTableName(column: Int): String = TODO("Not implemented")
  override fun getCatalogName(column: Int): String = TODO("Not implemented")
  override fun getColumnType(column: Int): Int = TODO("Not implemented")
  override fun getColumnTypeName(column: Int): String = TODO("Not implemented")
  override fun isReadOnly(column: Int): Boolean = TODO("Not implemented")
  override fun isWritable(column: Int): Boolean = TODO("Not implemented")
  override fun isDefinitelyWritable(column: Int): Boolean = TODO("Not implemented")
  override fun getColumnClassName(column: Int): String = TODO("Not implemented")
}

