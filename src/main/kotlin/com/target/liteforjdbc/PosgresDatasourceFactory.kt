package com.target.liteforjdbc

import javax.sql.DataSource

/**
 * Datasource factory for Postgres connection via a Hikari pool.
 *
 * See the PostgresDatasourceFactory.Config implementation for default values
 */
class PostgresDatasourceFactory(private val config: DbConfig) {

    @Suppress("MemberVisibilityCanBePrivate")
    val hikariDatasourceFactory = HikariDatasourceFactory(config)

    fun dataSource(): DataSource {
        val dataSource = hikariDatasourceFactory.dataSource("jdbc:postgresql://${config.host}:${config.port}/${config.databaseName}")
        dataSource.addDataSourceProperty("reWriteBatchedInserts", "true")

        if (config.ssl) {
            dataSource.addDataSourceProperty("ssl", true)
            dataSource.addDataSourceProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory")
            dataSource.addDataSourceProperty("sslmode", "require")
        }

        return dataSource
    }
}