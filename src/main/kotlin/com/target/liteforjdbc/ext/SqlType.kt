package com.target.liteforjdbc.ext

import java.sql.Types

/**
 * A Kotlin enum which simply wraps the [java.sql.Types] to make resolving and
 * compiler enforcement of values possible. [java.sql.Types] predates enums in Java, and
 * are difficult to follow as they are simply Int constants.
 */
enum class SqlType(val type: Int) {
  BIT(Types.BIT),
  BOOLEAN(Types.BIT),
  TINYINT(Types.TINYINT),
  SMALLINT(Types.TINYINT),
  INTEGER(Types.INTEGER),
  BIGINT(Types.BIGINT),
  REAL(Types.REAL),
  FLOAT(Types.FLOAT),
  DOUBLE(Types.DOUBLE),
  NUMERIC(Types.NUMERIC),
  DECIMAL(Types.DECIMAL),
  CHAR(Types.CHAR),
  VARCHAR(Types.VARCHAR),
  LONGVARCHAR(Types.LONGVARCHAR),
  DATE(Types.DATE),
  TIME(Types.TIME),
  TIMESTAMP(Types.TIMESTAMP),
  TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE),
  BINARY(Types.BINARY),
  VARBINARY(Types.VARBINARY),
  LONGVARBINARY(Types.LONGVARBINARY),
  NULL(Types.NULL),
  JAVA_OBJECT(Types.JAVA_OBJECT),
  DISTINCT(Types.DISTINCT),
  OTHER(Types.OTHER),
  STRUCT(Types.STRUCT),
  ARRAY(Types.ARRAY),
  BLOB(Types.BLOB),
  CLOB(Types.CLOB),
  REF(Types.REF),
  DATALINK(Types.DATALINK),
  ROWID(Types.ROWID),
  NCHAR(Types.NCHAR),
  NVARCHAR(Types.NVARCHAR),
  LONGNVARCHAR(Types.LONGNVARCHAR),
  NCLOB(Types.NCLOB),
  SQLXML(Types.SQLXML),
  REF_CURSOR(Types.REF_CURSOR),
  TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE);

  companion object {
    fun typeOf(int: Int) = when (int) {
      Types.BIT -> BIT
      Types.BOOLEAN -> BOOLEAN
      Types.TINYINT -> TINYINT
      Types.SMALLINT -> SMALLINT
      Types.BIGINT -> BIGINT
      Types.INTEGER -> INTEGER
      Types.REAL -> REAL
      Types.FLOAT -> FLOAT
      Types.DOUBLE -> DOUBLE
      Types.NUMERIC -> NUMERIC
      Types.DECIMAL -> DECIMAL
      Types.CHAR -> CHAR
      Types.VARCHAR -> VARCHAR
      Types.LONGVARCHAR -> LONGVARCHAR
      Types.DATE -> DATE
      Types.TIME -> TIME
      Types.TIMESTAMP -> TIMESTAMP
      Types.TIMESTAMP_WITH_TIMEZONE -> TIMESTAMP_WITH_TIMEZONE
      Types.BINARY -> BINARY
      Types.VARBINARY -> VARBINARY
      Types.LONGVARBINARY -> LONGVARBINARY
      Types.NULL -> NULL
      Types.JAVA_OBJECT -> JAVA_OBJECT
      Types.DISTINCT -> DISTINCT
      Types.OTHER -> OTHER
      Types.STRUCT -> STRUCT
      Types.ARRAY -> ARRAY
      Types.BLOB -> BLOB
      Types.CLOB -> CLOB
      Types.REF -> REF
      Types.DATALINK -> DATALINK
      Types.ROWID -> ROWID
      Types.NCHAR -> NCHAR
      Types.NVARCHAR -> NVARCHAR
      Types.LONGNVARCHAR -> LONGNVARCHAR
      Types.NCLOB -> NCLOB
      Types.SQLXML -> SQLXML
      Types.REF_CURSOR -> REF_CURSOR
      Types.TIME_WITH_TIMEZONE -> TIME_WITH_TIMEZONE
      else -> throw IllegalArgumentException("Unknown type: $int found in java.sql.Types.")
    }
  }
}