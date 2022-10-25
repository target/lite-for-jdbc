package com.target.liteforjdbc

import mu.KotlinLogging
import java.io.Closeable
import java.sql.*

/**
 * Utility to remove necessary database boilerplate code and perform the proper cleanup.
 *
 * NAMED PARAMETERS
 * These parameters use a colon prefix to provide a label for the query parameter. For example
 *
 * SELECT * FROM table WHERE field = :value
 *
 * To use this query syntax, call the methods which accept map for the arg values, and use the parameter name as the key.
 *
 * Colons inside of quotes or double quotes will be left in the query as is. If you need to escape a colon outside of
 * quotes, you can use a double colon :: which will translate to a single colon.
 *
 * POSITIONAL PARAMETERS
 * This is the normal JDBC syntax, where question marks represent parameters, and the values are provided based on the
 * position in the query. For example
 *
 * SELECT * FROM table WHERE field = ?
 *
 * To use this query syntax call the methods with the "PositionalParams" suffix. The parameters are then passed into the
 * varargs in the order they appear in the query.
 *
 * RECOMMENDED USAGE
 * It is recommended to use the named parameters to help make your code easier to read and maintain.
 *
 */

private val log = KotlinLogging.logger {}

sealed class ConnectionSession(
    val connection: Connection,
    autoCommitNeeded: Boolean,
) : Closeable {

    init {
        if (autoCommitNeeded != connection.autoCommit) {
            log.debug { "Overriding the autoCommit value to $autoCommitNeeded" }
            connection.autoCommit = autoCommitNeeded
        }
    }

    /**
     * Executes a query using PreparedStatement.executeUpdate(). The varargs provided will be set on the PreparedStatement as
     * parameters
     *
     * The query should use positional parameters indicated by '?', as per the JDBC spec. Pass the args in query order
     */
    fun executeUpdatePositionalParams(sql: String, vararg args: Any?): Int =
        callInternalPositionalParams(sql, { it.executeUpdate() }, *args)

    /**
     * Executes a query using PreparedStatement.executeUpdate(). The map provided will be set on the PreparedStatement as
     * parameters
     *
     * The query should use named parameters indicated by a colon followed by the parameter name, such as :value.
     * The args are provided in a map with the parameter name as the key. So if the parameter named value should be 1,
     * mapOf("value" to 1) can be sent.
     */
    fun executeUpdate(sql: String, args: Map<String, Any?> = mapOf()): Int = callInternal(sql, args) { it.executeUpdate() }

    /**
     * Executes a query using PreparedStatement.execute(), and getGeneratedKeys(). The varargs provided will be set
     * on the PreparedStatement as parameters. The generated keys will be returned to the result set,
     * which can be mapped using the rowMapper.
     *
     * The query should use positional parameters indicated by '?', as per the JDBC spec. Pass the args in query order
     */
    fun <T> executeWithGeneratedKeysPositionalParams(sql: String, rowMapper: (rs: ResultSet) -> T, vararg args: Any?): List<T> =
        callKeyGenInternalPositionalParams(sql, mapGeneratedKeys(rowMapper), *args)

    /**
     * Executes a query using PreparedStatement.execute(), and getGeneratedKeys(). The varargs provided will be set
     * on the PreparedStatement as parameters. The generated keys will be returned to the result set,
     * which can be mapped using the rowMapper.
     *
     * The query should use named parameters indicated by a colon followed by the parameter name, such as :value.
     * The args are provided in a map with the parameter name as the key. So if the parameter named value should be 1,
     * mapOf("value" to 1) can be sent.
     */
    fun <T> executeWithGeneratedKeys(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: (rs: ResultSet) -> T): List<T> =
        callKeyGenInternal(sql, args, mapGeneratedKeys(rowMapper))

    /**
     * Executes a query using PreparedStatement.addBatch() and executeBatch() and returns the list of Int. The args are
     * in a List of Lists. The outer list represents batches, the inner list is positional parameters for each query
     * in the batch.
     *
     * So args.size should be the same as the returned result's size. And each nested list should all have a size that
     * matches the number of query parameters.
     *
     * The returned list will provide the corresponding count of the rows affected for each query in the batch.
     *
     * The query should use positional parameters indicated by '?', as per the JDBC spec. Pass the args in query order
     */
    fun executeBatchPositionalParams(sql: String, args: List<List<Any?>>): List<Int> =
        callBatchInternalPositionalParams(sql, mapBatch(), args)

    /**
     * Executes a query using PreparedStatement.addBatch() and executeBatch() and returns the list of Int. The args are
     * in a List of Maps. The outer list represents batches, the maps are named parameters for each query in the batch.
     *
     * So args.size should be the same as the returned result's size. And each map should have an entry for all the
     * named parameters in the query.
     *
     * The returned list will provide the corresponding count of the rows affected for each query in the batch.
     *
     * The query should use named parameters indicated by a colon followed by the parameter name, such as :value.
     * The args are provided in a map with the parameter name as the key. So if the parameter named value should be 1,
     * mapOf("value" to 1) can be sent.
     */
    fun executeBatch(sql: String, args: List<Map<String, Any?>>): List<Int> =
        callBatchInternal(sql, mapBatch(), args)

    /**
     * Executes a query using PreparedStatement.executeQuery(). The  varargs provided will be set on the PreparedStatement as
     * parameters, and the first result will be mapped using the rowMapper provided.
     *
     * The query should use positional parameters indicated by '?', as per the JDBC spec. Pass the args in query order
     */
    fun <T> executeQueryPositionalParams(sql: String, rowMapper: (rs: ResultSet) -> T, vararg args: Any?): T? =
        callInternalPositionalParams(sql, mapSingleResult(rowMapper), *args)

    /**
     * Executes a query using PreparedStatement.executeQuery(). The map provided will be set on the PreparedStatement as
     * parameters, and the first result will be mapped using the rowMapper provided.
     *
     * The query should use named parameters indicated by a colon followed by the parameter name, such as :value.
     * The args are provided in a map with the parameter name as the key. So if the parameter named value should be 1,
     * mapOf("value" to 1) can be sent.
     */
    fun <T> executeQuery(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: (rs: ResultSet) -> T): T? =
        callInternal(sql, args, mapSingleResult(rowMapper))

    /**
     * Executes a query using PreparedStatement.executeQuery(). The varargs provided will be set on the PreparedStatement as
     * parameters, and all results will be mapped using the rowMapper provided.
     *
     * The query should use positional parameters indicated by '?', as per the JDBC spec. Pass the args in query order
     */
    fun <T> findAllPositionalParams(sql: String, rowMapper: (rs: ResultSet) -> T, vararg args: Any?): List<T> =
        callInternalPositionalParams(sql, mapMultipleResults(rowMapper), *args)

    /**
     * Executes a query using PreparedStatement.executeQuery(). The map provided will be set on the PreparedStatement as
     * parameters, and all results will be mapped using the rowMapper provided.
     *
     * The query should use named parameters indicated by a colon followed by the parameter name, such as :value.
     * The args are provided in a map with the parameter name as the key. So if the parameter named value should be 1,
     * mapOf("value" to 1) can be sent.
     */
    fun <T> findAll(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: (rs: ResultSet) -> T): List<T> =
        callInternal(sql, args, mapMultipleResults(rowMapper))

    /**
     * Uses the JDBC PreparedStatement, and closes it once the block is executed. This can be useful to perform
     * functions that aren't provided by the existing methods on this class
     */
    fun <T> usePreparedStatement(sql: String, block: (PreparedStatement) -> T): T = connection.prepareStatement(sql).use(block)

    /**
     * Uses the JDBC NamedParamPreparedStatement, and closes it once the block is executed. This can be useful to perform
     * functions that aren't provided by the existing methods on this class.
     *
     * The query must use named params, not positional params
     */
    fun <T> useNamedParamPreparedStatement(sql: String, block: (NamedParamPreparedStatement) -> T): T =
        NamedParamPreparedStatement.Builder(connection, sql).build().use(block)

    /**
     * Uses the JDBC PreparedStatement, and closes it once the block is executed. This can be useful to perform
     * functions that aren't provided by the existing methods on this class.
     *
     * This usage will set the statement to return autogenerated keys
     */
    fun <T> usePreparedStatementWithAutoGenKeys(sql: String, block: (PreparedStatement) -> T): T =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use(block)

    /**
     * Uses the JDBC NamedParamPreparedStatement, and closes it once the block is executed. This can be useful to perform
     * functions that aren't provided by the existing methods on this class.
     *
     * The query must use named params, not positional params
     *
     * This usage will set the statement to return autogenerated keys
     */
    fun <T> useNamedParamPreparedStatementWithAutoGenKeys(sql: String, block: (NamedParamPreparedStatement) -> T): T =
        NamedParamPreparedStatement.Builder(connection, sql, true).build().use(block)

    private fun <T> mapSingleResult(rowMapper: (rs: ResultSet) -> T): (preparedStatement: PreparedStatement) -> T? =
        { preparedStatement: PreparedStatement ->
            preparedStatement.executeQuery().takeIf { it.next() }?.run { rowMapper.invoke(this) }
        }

    private fun <T> mapMultipleResults(rowMapper: (rs: ResultSet) -> T): (preparedStatement: PreparedStatement) -> List<T> =
        { preparedStatement: PreparedStatement ->
            val result = mutableListOf<T>()
            val resultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                result.add(rowMapper.invoke(resultSet))
            }
            result
        }

    private fun <T> mapGeneratedKeys(rowMapper: (rs: ResultSet) -> T): (preparedStatement: PreparedStatement) -> List<T> =
        { preparedStatement: PreparedStatement ->
            val result = mutableListOf<T>()
            preparedStatement.execute()
            val resultSet = preparedStatement.generatedKeys
            while (resultSet.next()) {
                result.add(rowMapper.invoke(resultSet))
            }
            result
        }

    private fun mapBatch(): (preparedStatement: PreparedStatement) -> List<Int> = { preparedStatement: PreparedStatement ->
        val results = preparedStatement.executeBatch()
        results.toList()
    }

    private fun <T> callInternalPositionalParams(sql: String, action: (pstmt: PreparedStatement) -> T, vararg args: Any?): T =
        usePreparedStatement(sql) { pstmt ->
            pstmt.setParameters(*args)
            action(pstmt)
        }

    private fun <T> callInternal(sql: String, args: Map<String, Any?>, action: (pstmt: PreparedStatement) -> T): T =
        useNamedParamPreparedStatement(sql) { pstmt ->
            pstmt.setParameters(args)
            action(pstmt)
        }

    private fun <T> callBatchInternalPositionalParams(sql: String, action: (pstmt: PreparedStatement) -> T, args: List<List<Any?>>): T =
        usePreparedStatement(sql) { pstmt ->
            args.forEach { arg ->
                pstmt.setParameters(*arg.toTypedArray())
                pstmt.addBatch()
            }
            action(pstmt)
        }

    private fun <T> callBatchInternal(sql: String, action: (pstmt: PreparedStatement) -> T, args: List<Map<String, Any?>>): T =
        useNamedParamPreparedStatement(sql) { pstmt ->
            args.forEach { arg ->
                pstmt.setParameters(arg)
                pstmt.addBatch()
            }
            action(pstmt)
        }

    private fun <T> callKeyGenInternalPositionalParams(sql: String, action: (pstmt: PreparedStatement) -> T, vararg args: Any?): T =
        usePreparedStatementWithAutoGenKeys(sql) { pstmt ->
            pstmt.setParameters(*args)
            action(pstmt)
        }

    private fun <T> callKeyGenInternal(sql: String, args: Map<String, Any?>, action: (pstmt: PreparedStatement) -> T): T =
        useNamedParamPreparedStatementWithAutoGenKeys(sql) { pstmt ->
            pstmt.setParameters(args)
            action(pstmt)
        }

    /**
     * Checks for the health of the underlying Connection
     *
     * @return true if the connection is still open
     */
    fun isHealthy() = !connection.isClosed

    /**
     * Closes this ConnectionSession by closing the connection
     */
    override fun close() {
        connection.close()
    }
}

/**
 * A Transaction {@link ConnectionSession}. If the connection provided isn't set with autocommit as false, it will be
 * forced to be autocommit disabled to ensure proper operation.
 *
 * Any code using this will be responsible for managing its own transaction. Calling close with uncommitted changes will
 * behave differently depending on the JDBC driver, so it should be avoided. Using {@link com.target.liteforjdbc.Db} will
 * ensure this is done, so it is the recommended pattern.
 */
class Transaction(connection: Connection) : ConnectionSession(connection, false) {
    /**
     * Commits database changes
     *
     * @see java.sql.Connection#commit()
     */
    fun commit() = connection.commit()

    /**
     * Creates a savepoint that can be used to perform a partial rollback
     *
     * @see java.sql.Connection#savepoint()
     */
    fun savepoint(): Savepoint = connection.setSavepoint()

    /**
     * Creates a named savepoint that can be used to perform a partial rollback
     *
     * @see java.sql.Connection#savepoint(String)
     */
    fun savepoint(name: String): Savepoint = connection.setSavepoint(name)

    /**
     * Rollback to last commit or beginning of transaction if no commit has been done.
     *
     * @see java.sql.Connection#rollback()
     */
    fun rollback() = connection.rollback()

    /**
     * Rollback to last savepoint provided. If the savepoint has already been committed, this will throw an exception
     *
     * @see java.sql.Connection#rollback
     */
    fun rollback(savepoint: Savepoint) = connection.rollback(savepoint)
}

/**
 * An AutoCommit {@link ConnectionSession}. If the connection provided isn't set for AutoCommit, it will be forced to be
 * AutoCommit enabled to ensure proper operation.
 */
class AutoCommit(connection: Connection) : ConnectionSession(connection, true)
