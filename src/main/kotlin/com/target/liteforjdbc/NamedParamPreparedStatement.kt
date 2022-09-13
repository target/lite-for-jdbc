package com.target.liteforjdbc

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Date
import java.util.*

/**
 * A wrapper for a prepared statement, which includes a name to parameter index map for name based parameters
 *
 * This extends PreparedStatement to offer name-based options to set parameters.
 */
class NamedParamPreparedStatement(
    ps: PreparedStatement,
    val namedParamOrdinalIndexes: Map<String, List<Int>>,
) : PreparedStatementProxy(ps) {

    data class Builder(
        val conn: Connection,
        var sql: String,
        val autoGenKeys: Boolean = false,
    ) {

        fun build(): NamedParamPreparedStatement {
            val parsedQuery = ParsedQuery(sql)
            if (parsedQuery.getPositionalParameterSize() > 0) {
                throw Exception("Named parameters cannot have positional parameters as well. But ${parsedQuery.getPositionalParameterSize()} positional parameter(s) were found")
            }

            // transform the ordered list of parameter names to a map of the parameter names and their list of 1-based ordinal parameter positions
            val namedParameters = parsedQuery.getNamedParameters()
            val namedParameterIndexes: MutableMap<String, MutableList<Int>> = mutableMapOf()
            namedParameters.forEachIndexed { index, namedParameter ->
                namedParameterIndexes.getOrPut(namedParameter.parameterName) { mutableListOf() }.add(index + 1)
            }

            val preparedStatement =
                if (autoGenKeys) conn.prepareStatement(parsedQuery.toSql(), Statement.RETURN_GENERATED_KEYS) else conn.prepareStatement(
                    parsedQuery.toSql()
                )
            return NamedParamPreparedStatement(preparedStatement, namedParameterIndexes)
        }
    }

    fun setParameters(args: Map<String, Any?>) {
        for ((name, arg) in args) {
            if (namedParamOrdinalIndexes[name] != null) {
                setParameter(name, arg)
            }
        }
    }

    internal fun setParameter(name: String, arg: Any?) {
        getIndexes(name).forEach {
            setParameter(it, arg)
        }
    }

    internal fun getIndexes(parameterKey: String): List<Int> {
        return checkNotNull(namedParamOrdinalIndexes[parameterKey]) {
            "Unable to find a parameter named $parameterKey in available keys (${namedParamOrdinalIndexes.keys.joinToString(", ")})"
        }
    }

    @Throws(SQLException::class)
    fun setNull(parameterKey: String, value: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNull(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setBoolean(parameterKey: String, value: Boolean) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBoolean(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setByte(parameterKey: String, value: Byte) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setByte(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setShort(parameterKey: String, value: Short) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setShort(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setInt(parameterKey: String, value: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setInt(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setLong(parameterKey: String, value: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setLong(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setFloat(parameterKey: String, value: Float) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setFloat(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setDouble(parameterKey: String, value: Double) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setDouble(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setBigDecimal(parameterKey: String, value: BigDecimal?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBigDecimal(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setString(parameterKey: String, value: String?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setString(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setBytes(parameterKey: String, value: ByteArray?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBytes(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setDate(parameterKey: String, value: Date?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setDate(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setTime(parameterKey: String, value: Time?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setTime(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setTimestamp(parameterKey: String, value: Timestamp?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setTimestamp(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setAsciiStream(parameterKey: String, value: InputStream?, length: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setAsciiStream(it, value, length)
        }
    }

    @Throws(SQLException::class)
    fun setBinaryStream(parameterKey: String, value: InputStream?, length: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBinaryStream(it, value, length)
        }
    }

    @Throws(SQLException::class)
    fun setObject(parameterKey: String, value: Any?, targetSqlType: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setObject(it, value, targetSqlType)
        }
    }

    @Throws(SQLException::class)
    fun setObject(parameterKey: String, value: Any?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setObject(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setCharacterStream(parameterKey: String, reader: Reader?, length: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setCharacterStream(it, reader, length)
        }
    }

    @Throws(SQLException::class)
    fun setRef(parameterKey: String, value: Ref?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setRef(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setBlob(parameterKey: String, value: Blob?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBlob(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setClob(parameterKey: String, value: Clob?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setClob(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setArray(parameterKey: String, value: java.sql.Array?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setArray(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setDate(parameterKey: String, value: Date?, cal: Calendar?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setDate(it, value, cal)
        }
    }

    @Throws(SQLException::class)
    fun setTime(parameterKey: String, value: Time?, cal: Calendar?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setTime(it, value, cal)
        }
    }

    @Throws(SQLException::class)
    fun setTimestamp(parameterKey: String, value: Timestamp?, cal: Calendar?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setTimestamp(it, value, cal)
        }
    }

    @Throws(SQLException::class)
    fun setNull(parameterKey: String, sqlType: Int, typeName: String?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNull(it, sqlType, typeName)
        }
    }

    @Throws(SQLException::class)
    fun setURL(parameterKey: String, value: URL?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setURL(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setRowId(parameterKey: String, value: RowId?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setRowId(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setNString(parameterKey: String, value: String?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNString(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setNCharacterStream(parameterKey: String, value: Reader?, length: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNCharacterStream(it, value, length)
        }
    }

    @Throws(SQLException::class)
    fun setNClob(parameterKey: String, value: NClob?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNClob(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setClob(parameterKey: String, reader: Reader?, length: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setClob(it, reader, length)
        }
    }

    @Throws(SQLException::class)
    fun setBlob(parameterKey: String, inputStream: InputStream?, length: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBlob(it, inputStream, length)
        }
    }

    @Throws(SQLException::class)
    fun setNClob(parameterKey: String, reader: Reader?, length: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNClob(it, reader, length)
        }
    }

    @Throws(SQLException::class)
    fun setSQLXML(parameterKey: String, xmlObject: SQLXML?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setSQLXML(it, xmlObject)
        }
    }

    @Throws(SQLException::class)
    fun setObject(parameterKey: String, value: Any?, targetSqlType: Int, scaleOrLength: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setObject(it, value, targetSqlType, scaleOrLength)
        }
    }

    @Throws(SQLException::class)
    fun setAsciiStream(parameterKey: String, value: InputStream?, length: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setAsciiStream(it, value, length)
        }
    }

    @Throws(SQLException::class)
    fun setBinaryStream(parameterKey: String, value: InputStream?, length: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBinaryStream(it, value, length)
        }
    }

    @Throws(SQLException::class)
    fun setCharacterStream(parameterKey: String, reader: Reader?, length: Long) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setCharacterStream(it, reader, length)
        }
    }

    @Throws(SQLException::class)
    fun setAsciiStream(parameterKey: String, value: InputStream?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setAsciiStream(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setBinaryStream(parameterKey: String, value: InputStream?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBinaryStream(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setCharacterStream(parameterKey: String, reader: Reader?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setCharacterStream(it, reader)
        }
    }

    @Throws(SQLException::class)
    fun setNCharacterStream(parameterKey: String, value: Reader?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNCharacterStream(it, value)
        }
    }

    @Throws(SQLException::class)
    fun setClob(parameterKey: String, reader: Reader?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setClob(it, reader)
        }
    }

    @Throws(SQLException::class)
    fun setBlob(parameterKey: String, inputStream: InputStream?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setBlob(it, inputStream)
        }
    }

    @Throws(SQLException::class)
    fun setNClob(parameterKey: String, reader: Reader?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setNClob(it, reader)
        }
    }

    @Throws(SQLException::class)
    fun setObject(parameterKey: String, value: Any?, targetSqlType: SQLType?, scaleOrLength: Int) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setObject(it, value, targetSqlType, scaleOrLength)
        }
    }

    @Throws(SQLException::class)
    fun setObject(parameterKey: String, value: Any?, targetSqlType: SQLType?) {
        getIndexes(parameterKey).forEach {
            preparedStatement.setObject(it, value, targetSqlType)
        }
    }

}
