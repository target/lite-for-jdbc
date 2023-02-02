package com.target.liteforjdbc

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

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
open class Db(
    private val dataSource: DataSource,
) {

    constructor(config: DbConfig) : this(DataSourceFactoryRegistry.dataSource(config))

    /**
     * @see AutoCommit#executeUpdatePositionalParams
     */
    fun executeUpdatePositionalParams(sql: String, vararg args: Any?): Int = withAutoCommit { it.executeUpdatePositionalParams(sql, *args) }

    /**
     * @see AutoCommit#executeUpdate
     */
    fun executeUpdate(sql: String, args: Map<String, Any?> = mapOf()): Int = withAutoCommit { it.executeUpdate(sql, args) }


    /**
     * @see AutoCommit#executeWithGeneratedKeysPositionalParams
     */
    fun <T> executeWithGeneratedKeysPositionalParams(sql: String, rowMapper: RowMapper<T>, vararg args: Any?): List<T> =
        withAutoCommit { it.executeWithGeneratedKeysPositionalParams(sql, rowMapper, *args) }

    /**
     * @see AutoCommit#executeWithGeneratedKeys
     */
    fun <T> executeWithGeneratedKeys(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: RowMapper<T>): List<T> =
        withAutoCommit { it.executeWithGeneratedKeys(sql, args, rowMapper) }

    /**
     * @see AutoCommit#executeBatchPositionalParams
     */
    fun executeBatchPositionalParams(sql: String, args: List<List<Any?>>): List<Int> =
        withAutoCommit { it.executeBatchPositionalParams(sql, args) }

    /**
     * @see AutoCommit#executeBatchPositionalParams
     */
    fun <T> executeBatchPositionalParams(sql: String, args: List<List<Any?>>, rowMapper: RowMapper<T>): List<T> =
        withAutoCommit { it.executeBatchPositionalParams(sql, args, rowMapper) }

    /**
     * @see AutoCommit#executeBatch
     */
    fun executeBatch(sql: String, args: List<Map<String, Any?>>): List<Int> =
        withAutoCommit { it.executeBatch(sql, args) }

    /**
     * @see AutoCommit#executeBatch
     */
    fun <T> executeBatch(sql: String, args: List<Map<String, Any?>>, rowMapper: RowMapper<T>): List<T> =
        withAutoCommit { it.executeBatch(sql, args, rowMapper) }

    /**
     * @see AutoCommit#executeQueryPositionalParams
     */
    fun <T> executeQueryPositionalParams(sql: String, rowMapper: RowMapper<T>, vararg args: Any?): T? =
        withAutoCommit { it.executeQueryPositionalParams(sql, rowMapper, *args) }

    /**
     * @see AutoCommit#executeQuery
     */
    fun <T> executeQuery(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: RowMapper<T>): T? =
        withAutoCommit { it.executeQuery(sql, args, rowMapper) }

    /**
     * @see AutoCommit#findAllPositionalParams
     */
    fun <T> findAllPositionalParams(sql: String, rowMapper: RowMapper<T>, vararg args: Any?): List<T> =
        withAutoCommit { it.findAllPositionalParams(sql, rowMapper, *args) }

    /**
     * @see AutoCommit#findAll
     */
    fun <T> findAll(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: RowMapper<T>): List<T> =
        withAutoCommit { it.findAll(sql, args, rowMapper) }

    /**
     * Uses the JDBC connection, and closes it once the block is executed. This can be useful to perform functions that
     * aren't provided by the existing methods on this class
     */
    fun <T> useConnection(block: (Connection) -> T): T = dataSource.connection.use(block)

    /**
     * Uses a com.target.liteforjdbc.Transaction, and commits it once to the block is executed successfully,
     * or rolls back if it throws an exception. This is required to perform any DB interactions that need transaction
     * support.
     */
    fun <T> withTransaction(block: (Transaction) -> T): T {
        val transaction = Transaction(connection = dataSource.connection)
        transaction.use {
            try {
                val result = block(transaction)
                transaction.commit()
                return result
            } catch (t: Throwable) {
                transaction.rollback()
                throw t
            }
        }
    }

    /**
     * Uses a com.target.liteforjdbc.AutoCommit and closes it once teh block is executed. This can be useful to use a
     * single connection from the DataSource for a series of actions. Using other convenience query methods on this
     * class will use a new AutoCommit object per call, which will be less efficient for multiple calls.
     */
    open fun <T> withAutoCommit(block: (AutoCommit) -> T): T = AutoCommit(connection = dataSource.connection).use(block)

    /**
     * @see AutoCommit#usePreparedStatement
     */
    fun <T> usePreparedStatement(sql: String, block: (PreparedStatement) -> T): T = withAutoCommit { it.usePreparedStatement(sql, block) }

    /**
     * @see AutoCommit#useNamedParamPreparedStatement
     */
    fun <T> useNamedParamPreparedStatement(sql: String, block: (NamedParamPreparedStatement) -> T): T =
        withAutoCommit { it.useNamedParamPreparedStatement(sql, block) }

    /**
     * @see AutoCommit#usePreparedStatementWithAutoGenKeys
     */
    fun <T> usePreparedStatementWithAutoGenKeys(sql: String, block: (PreparedStatement) -> T): T =
        withAutoCommit { it.usePreparedStatementWithAutoGenKeys(sql, block) }

    /**
     * @see AutoCommit#useNamedParamPreparedStatementWithAutoGenKeys
     */
    fun <T> useNamedParamPreparedStatementWithAutoGenKeys(sql: String, block: (NamedParamPreparedStatement) -> T): T =
        withAutoCommit { it.useNamedParamPreparedStatementWithAutoGenKeys(sql, block) }

    /**
     * Checks for the health of the underlying DataSource
     *
     * @return true if the datasource returns a functioning connection
     */
    fun isDataSourceHealthy() = useConnection { !it.isClosed }

}

