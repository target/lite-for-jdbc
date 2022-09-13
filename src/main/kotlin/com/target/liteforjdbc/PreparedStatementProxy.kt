package com.target.liteforjdbc

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Date
import java.util.*

/**
 * This is a proxy wrapper which will pass every call through to the PreparedStatement provided. It is open
 * so it can be extended
 */
open class PreparedStatementProxy(
    val preparedStatement: PreparedStatement,
) : PreparedStatement {

    @Throws(SQLException::class)
    override fun executeQuery(): ResultSet {
        return preparedStatement.executeQuery()
    }

    @Throws(SQLException::class)
    override fun executeUpdate(): Int {
        return preparedStatement.executeUpdate()
    }

    @Throws(SQLException::class)
    override fun setNull(parameterIndex: Int, sqlType: Int) {
        preparedStatement.setNull(parameterIndex, sqlType)
    }

    @Throws(SQLException::class)
    override fun setBoolean(parameterIndex: Int, x: Boolean) {
        preparedStatement.setBoolean(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setByte(parameterIndex: Int, x: Byte) {
        preparedStatement.setByte(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setShort(parameterIndex: Int, x: Short) {
        preparedStatement.setShort(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setInt(parameterIndex: Int, x: Int) {
        preparedStatement.setInt(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setLong(parameterIndex: Int, x: Long) {
        preparedStatement.setLong(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setFloat(parameterIndex: Int, x: Float) {
        preparedStatement.setFloat(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setDouble(parameterIndex: Int, x: Double) {
        preparedStatement.setDouble(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setBigDecimal(parameterIndex: Int, x: BigDecimal?) {
        preparedStatement.setBigDecimal(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setString(parameterIndex: Int, x: String?) {
        preparedStatement.setString(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setBytes(parameterIndex: Int, x: ByteArray?) {
        preparedStatement.setBytes(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setDate(parameterIndex: Int, x: Date?) {
        preparedStatement.setDate(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setTime(parameterIndex: Int, x: Time?) {
        preparedStatement.setTime(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setTimestamp(parameterIndex: Int, x: Timestamp?) {
        preparedStatement.setTimestamp(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Int) {
        preparedStatement.setAsciiStream(parameterIndex, x, length)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in source interface", replaceWith = ReplaceWith("setCharacterStream(Int, Reader?, Int)"))
    @Throws(SQLException::class)
    override fun setUnicodeStream(parameterIndex: Int, x: InputStream?, length: Int) {
        preparedStatement.setUnicodeStream(parameterIndex, x, length)
    }

    @Throws(SQLException::class)
    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Int) {
        preparedStatement.setBinaryStream(parameterIndex, x, length)
    }

    @Throws(SQLException::class)
    override fun clearParameters() {
        preparedStatement.clearParameters()
    }

    @Throws(SQLException::class)
    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int) {
        preparedStatement.setObject(parameterIndex, x, targetSqlType)
    }

    @Throws(SQLException::class)
    override fun setObject(parameterIndex: Int, x: Any?) {
        preparedStatement.setObject(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun execute(): Boolean {
        return preparedStatement.execute()
    }

    @Throws(SQLException::class)
    override fun addBatch() {
        preparedStatement.addBatch()
    }

    @Throws(SQLException::class)
    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Int) {
        preparedStatement.setCharacterStream(parameterIndex, reader, length)
    }

    @Throws(SQLException::class)
    override fun setRef(parameterIndex: Int, x: Ref?) {
        preparedStatement.setRef(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setBlob(parameterIndex: Int, x: Blob?) {
        preparedStatement.setBlob(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setClob(parameterIndex: Int, x: Clob?) {
        preparedStatement.setClob(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setArray(parameterIndex: Int, x: java.sql.Array?) {
        preparedStatement.setArray(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun getMetaData(): ResultSetMetaData {
        return preparedStatement.metaData
    }

    @Throws(SQLException::class)
    override fun setDate(parameterIndex: Int, x: Date?, cal: Calendar?) {
        preparedStatement.setDate(parameterIndex, x, cal)
    }

    @Throws(SQLException::class)
    override fun setTime(parameterIndex: Int, x: Time?, cal: Calendar?) {
        preparedStatement.setTime(parameterIndex, x, cal)
    }

    @Throws(SQLException::class)
    override fun setTimestamp(parameterIndex: Int, x: Timestamp?, cal: Calendar?) {
        preparedStatement.setTimestamp(parameterIndex, x, cal)
    }

    @Throws(SQLException::class)
    override fun setNull(parameterIndex: Int, sqlType: Int, typeName: String?) {
        preparedStatement.setNull(parameterIndex, sqlType, typeName)
    }

    @Throws(SQLException::class)
    override fun setURL(parameterIndex: Int, x: URL?) {
        preparedStatement.setURL(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun getParameterMetaData(): ParameterMetaData {
        return preparedStatement.parameterMetaData
    }

    @Throws(SQLException::class)
    override fun setRowId(parameterIndex: Int, x: RowId?) {
        preparedStatement.setRowId(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setNString(parameterIndex: Int, value: String?) {
        preparedStatement.setNString(parameterIndex, value)
    }

    @Throws(SQLException::class)
    override fun setNCharacterStream(parameterIndex: Int, value: Reader?, length: Long) {
        preparedStatement.setNCharacterStream(parameterIndex, value, length)
    }

    @Throws(SQLException::class)
    override fun setNClob(parameterIndex: Int, value: NClob?) {
        preparedStatement.setNClob(parameterIndex, value)
    }

    @Throws(SQLException::class)
    override fun setClob(parameterIndex: Int, reader: Reader?, length: Long) {
        preparedStatement.setClob(parameterIndex, reader, length)
    }

    @Throws(SQLException::class)
    override fun setBlob(parameterIndex: Int, inputStream: InputStream?, length: Long) {
        preparedStatement.setBlob(parameterIndex, inputStream, length)
    }

    @Throws(SQLException::class)
    override fun setNClob(parameterIndex: Int, reader: Reader?, length: Long) {
        preparedStatement.setNClob(parameterIndex, reader, length)
    }

    @Throws(SQLException::class)
    override fun setSQLXML(parameterIndex: Int, xmlObject: SQLXML?) {
        preparedStatement.setSQLXML(parameterIndex, xmlObject)
    }

    @Throws(SQLException::class)
    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: Int, scaleOrLength: Int) {
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength)
    }

    @Throws(SQLException::class)
    override fun setAsciiStream(parameterIndex: Int, x: InputStream?, length: Long) {
        preparedStatement.setAsciiStream(parameterIndex, x, length)
    }

    @Throws(SQLException::class)
    override fun setBinaryStream(parameterIndex: Int, x: InputStream?, length: Long) {
        preparedStatement.setBinaryStream(parameterIndex, x, length)
    }

    @Throws(SQLException::class)
    override fun setCharacterStream(parameterIndex: Int, reader: Reader?, length: Long) {
        preparedStatement.setCharacterStream(parameterIndex, reader, length)
    }

    @Throws(SQLException::class)
    override fun setAsciiStream(parameterIndex: Int, x: InputStream?) {
        preparedStatement.setAsciiStream(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setBinaryStream(parameterIndex: Int, x: InputStream?) {
        preparedStatement.setBinaryStream(parameterIndex, x)
    }

    @Throws(SQLException::class)
    override fun setCharacterStream(parameterIndex: Int, reader: Reader?) {
        preparedStatement.setCharacterStream(parameterIndex, reader)
    }

    @Throws(SQLException::class)
    override fun setNCharacterStream(parameterIndex: Int, value: Reader?) {
        preparedStatement.setNCharacterStream(parameterIndex, value)
    }

    @Throws(SQLException::class)
    override fun setClob(parameterIndex: Int, reader: Reader?) {
        preparedStatement.setClob(parameterIndex, reader)
    }

    @Throws(SQLException::class)
    override fun setBlob(parameterIndex: Int, inputStream: InputStream?) {
        preparedStatement.setBlob(parameterIndex, inputStream)
    }

    @Throws(SQLException::class)
    override fun setNClob(parameterIndex: Int, reader: Reader?) {
        preparedStatement.setNClob(parameterIndex, reader)
    }

    @Throws(SQLException::class)
    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: SQLType?, scaleOrLength: Int) {
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength)
    }

    @Throws(SQLException::class)
    override fun setObject(parameterIndex: Int, x: Any?, targetSqlType: SQLType?) {
        preparedStatement.setObject(parameterIndex, x, targetSqlType)
    }

    @Throws(SQLException::class)
    override fun executeLargeUpdate(): Long {
        return preparedStatement.executeLargeUpdate()
    }

    @Throws(SQLException::class)
    override fun executeQuery(sql: String?): ResultSet {
        return preparedStatement.executeQuery(sql)
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String?): Int {
        return preparedStatement.executeUpdate(sql)
    }

    @Throws(SQLException::class)
    override fun close() {
        preparedStatement.close()
    }

    @Throws(SQLException::class)
    override fun getMaxFieldSize(): Int {
        return preparedStatement.maxFieldSize
    }

    @Throws(SQLException::class)
    override fun setMaxFieldSize(max: Int) {
        preparedStatement.maxFieldSize = max
    }

    @Throws(SQLException::class)
    override fun getMaxRows(): Int {
        return preparedStatement.maxRows
    }

    @Throws(SQLException::class)
    override fun setMaxRows(max: Int) {
        preparedStatement.maxRows = max
    }

    @Throws(SQLException::class)
    override fun setEscapeProcessing(enable: Boolean) {
        preparedStatement.setEscapeProcessing(enable)
    }

    @Throws(SQLException::class)
    override fun getQueryTimeout(): Int {
        return preparedStatement.queryTimeout
    }

    @Throws(SQLException::class)
    override fun setQueryTimeout(seconds: Int) {
        preparedStatement.queryTimeout = seconds
    }

    @Throws(SQLException::class)
    override fun cancel() {
        preparedStatement.cancel()
    }

    @Throws(SQLException::class)
    override fun getWarnings(): SQLWarning {
        return preparedStatement.warnings
    }

    @Throws(SQLException::class)
    override fun clearWarnings() {
        preparedStatement.clearWarnings()
    }

    @Throws(SQLException::class)
    override fun setCursorName(name: String?) {
        preparedStatement.setCursorName(name)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String?): Boolean {
        return preparedStatement.execute(sql)
    }

    @Throws(SQLException::class)
    override fun getResultSet(): ResultSet {
        return preparedStatement.resultSet
    }

    @Throws(SQLException::class)
    override fun getUpdateCount(): Int {
        return preparedStatement.updateCount
    }

    @Throws(SQLException::class)
    override fun getMoreResults(): Boolean {
        return preparedStatement.moreResults
    }

    @Throws(SQLException::class)
    override fun getFetchDirection(): Int {
        return preparedStatement.fetchDirection
    }

    @Throws(SQLException::class)
    override fun setFetchDirection(direction: Int) {
        preparedStatement.fetchDirection = direction
    }

    @Throws(SQLException::class)
    override fun getFetchSize(): Int {
        return preparedStatement.fetchSize
    }

    @Throws(SQLException::class)
    override fun setFetchSize(rows: Int) {
        preparedStatement.fetchSize = rows
    }

    @Throws(SQLException::class)
    override fun getResultSetConcurrency(): Int {
        return preparedStatement.resultSetConcurrency
    }

    @Throws(SQLException::class)
    override fun getResultSetType(): Int {
        return preparedStatement.resultSetType
    }

    @Throws(SQLException::class)
    override fun addBatch(sql: String?) {
        preparedStatement.addBatch(sql)
    }

    @Throws(SQLException::class)
    override fun clearBatch() {
        preparedStatement.clearBatch()
    }

    @Throws(SQLException::class)
    override fun executeBatch(): IntArray {
        return preparedStatement.executeBatch()
    }

    @Throws(SQLException::class)
    override fun getConnection(): Connection {
        return preparedStatement.connection
    }

    @Throws(SQLException::class)
    override fun getMoreResults(current: Int): Boolean {
        return preparedStatement.getMoreResults(current)
    }

    @Throws(SQLException::class)
    override fun getGeneratedKeys(): ResultSet {
        return preparedStatement.generatedKeys
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String?, autoGeneratedKeys: Int): Int {
        return preparedStatement.executeUpdate(sql, autoGeneratedKeys)
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String?, columnIndexes: IntArray?): Int {
        return preparedStatement.executeUpdate(sql, columnIndexes)
    }

    @Throws(SQLException::class)
    override fun executeUpdate(sql: String?, columnNames: Array<String?>?): Int {
        return preparedStatement.executeUpdate(sql, columnNames)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String?, autoGeneratedKeys: Int): Boolean {
        return preparedStatement.execute(sql, autoGeneratedKeys)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String?, columnIndexes: IntArray?): Boolean {
        return preparedStatement.execute(sql, columnIndexes)
    }

    @Throws(SQLException::class)
    override fun execute(sql: String?, columnNames: Array<String?>?): Boolean {
        return preparedStatement.execute(sql, columnNames)
    }

    @Throws(SQLException::class)
    override fun getResultSetHoldability(): Int {
        return preparedStatement.resultSetHoldability
    }

    @Throws(SQLException::class)
    override fun isClosed(): Boolean {
        return preparedStatement.isClosed
    }

    @Throws(SQLException::class)
    override fun isPoolable(): Boolean {
        return preparedStatement.isPoolable
    }

    @Throws(SQLException::class)
    override fun setPoolable(poolable: Boolean) {
        preparedStatement.isPoolable = poolable
    }

    @Throws(SQLException::class)
    override fun closeOnCompletion() {
        preparedStatement.closeOnCompletion()
    }

    @Throws(SQLException::class)
    override fun isCloseOnCompletion(): Boolean {
        return preparedStatement.isCloseOnCompletion
    }

    @Throws(SQLException::class)
    override fun getLargeUpdateCount(): Long {
        return preparedStatement.largeUpdateCount
    }

    @Throws(SQLException::class)
    override fun getLargeMaxRows(): Long {
        return preparedStatement.largeMaxRows
    }

    @Throws(SQLException::class)
    override fun setLargeMaxRows(max: Long) {
        preparedStatement.largeMaxRows = max
    }

    @Throws(SQLException::class)
    override fun executeLargeBatch(): LongArray {
        return preparedStatement.executeLargeBatch()
    }

    @Throws(SQLException::class)
    override fun executeLargeUpdate(sql: String?): Long {
        return preparedStatement.executeLargeUpdate(sql)
    }

    @Throws(SQLException::class)
    override fun executeLargeUpdate(sql: String?, autoGeneratedKeys: Int): Long {
        return preparedStatement.executeLargeUpdate(sql, autoGeneratedKeys)
    }

    @Throws(SQLException::class)
    override fun executeLargeUpdate(sql: String?, columnIndexes: IntArray?): Long {
        return preparedStatement.executeLargeUpdate(sql, columnIndexes)
    }

    @Throws(SQLException::class)
    override fun executeLargeUpdate(sql: String?, columnNames: Array<String?>?): Long {
        return preparedStatement.executeLargeUpdate(sql, columnNames)
    }

    @Throws(SQLException::class)
    override fun enquoteLiteral(`val`: String?): String {
        return preparedStatement.enquoteLiteral(`val`)
    }

    @Throws(SQLException::class)
    override fun enquoteIdentifier(identifier: String?, alwaysQuote: Boolean): String {
        return preparedStatement.enquoteIdentifier(identifier, alwaysQuote)
    }

    @Throws(SQLException::class)
    override fun isSimpleIdentifier(identifier: String?): Boolean {
        return preparedStatement.isSimpleIdentifier(identifier)
    }

    @Throws(SQLException::class)
    override fun enquoteNCharLiteral(`val`: String?): String {
        return preparedStatement.enquoteNCharLiteral(`val`)
    }

    @Throws(SQLException::class)
    override fun <T> unwrap(iface: Class<T>): T {
        return preparedStatement.unwrap(iface)
    }

    @Throws(SQLException::class)
    override fun isWrapperFor(iface: Class<*>?): Boolean {
        return preparedStatement.isWrapperFor(iface)
    }

    override fun toString(): String {
        return preparedStatement.toString()
    }
}
