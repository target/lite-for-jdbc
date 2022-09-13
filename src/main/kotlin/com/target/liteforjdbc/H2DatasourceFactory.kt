package com.target.liteforjdbc

import javax.sql.DataSource

/**
 * Datasource factory for Postgres connection via a Hikari pool.
 *
 * See the PostgresDatasourceFactory.Config implementation for default values
 */
class H2InMemDatasourceFactory(private val config: DbConfig) {

    private val hikariDatasourceFactory = HikariDatasourceFactory(config)

    fun dataSource(): DataSource = hikariDatasourceFactory.dataSource("jdbc:h2:mem:${config.databaseName};DB_CLOSE_DELAY=-1")

}