package com.target.liteforjdbc.ext

import java.lang.UnsupportedOperationException
import java.sql.ResultSet

/**
 * Reads a value from a given column name and invokes the `get***` from the result to match the
 * input type.
 *
 * Time and Timestamps will be returned as [java.time.Instant] instances.
 *
 * Note, the following types are not supported:
 *  - STRUCT
 *  - DATALINK
 *  - REF_CURSOR
 *  - ROW_ID
 *
 *  @throws UnsupportedOperationException if any of the unsupported types are requested.
 */
fun ResultSet.readValue(type: Int, column: String): Any? = when (SqlType.typeOf(type)) {
  SqlType.BIT, SqlType.BOOLEAN ->  getBoolean(column)
  SqlType.TINYINT, SqlType.SMALLINT, SqlType.INTEGER -> getInt(column)
  SqlType.BIGINT -> getLong(column)
  SqlType.REAL ->  getFloat(column)
  SqlType.FLOAT, SqlType.DOUBLE -> getDouble(column)
  SqlType.NUMERIC, SqlType.DECIMAL -> getBigDecimal(column)
  SqlType.CHAR, SqlType.VARCHAR, SqlType.LONGVARCHAR -> getString(column)
  SqlType.DATE -> getDate(column)
  SqlType.TIME -> getTime(column).toInstant()
  SqlType.TIMESTAMP ->  getTimestamp(column).toInstant()
  SqlType.TIMESTAMP_WITH_TIMEZONE -> getTimestamp(column).toInstant()
  SqlType.BINARY, SqlType.VARBINARY, SqlType.LONGVARBINARY ->  getBytes(column)
  SqlType.NULL -> null
  SqlType.JAVA_OBJECT, SqlType.DISTINCT, SqlType.OTHER -> getObject(column)
  SqlType.STRUCT -> throw UnsupportedOperationException("java.sql.Types.STRUCT is currently unsupported.")
  SqlType.ARRAY -> getArray(column)
  SqlType.BLOB -> getBlob(column)
  SqlType.CLOB -> getString(column)
  SqlType.REF -> getRef(column)
  SqlType.DATALINK -> throw UnsupportedOperationException("java.sql.Types.DATALINK is currently unsupported.")
  SqlType.ROWID -> throw UnsupportedOperationException("java.sql.Types.ROWID is currently unsupported.")
  SqlType.NCHAR, SqlType.NVARCHAR, SqlType.LONGNVARCHAR -> getNString(column)
  SqlType.NCLOB -> getNClob(column)
  SqlType.SQLXML -> getSQLXML(column)
  SqlType.REF_CURSOR -> throw UnsupportedOperationException("java.sql.Types.REF_CURSOR is currently unsupported.")
  SqlType.TIME_WITH_TIMEZONE -> getTime(column).toInstant()
}
