package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource

const val DATABASE_HOST_ENV_NAME = "DATABASE_HOST"

/**
 * Datasource factory for Postgres connection via a Hikari pool.
 *
 * See the PostgresDatasourceFactory.Config implementation for default values
 */

fun hikariDbConfigValidate(config: DbConfig) {
    checkNotNull(config.jdbcUrl) { "JDBCUrl is required for Hikari Data Sources, but was null"}
}
fun hikariDataSource(config: DbConfig): HikariDataSource {
    hikariDbConfigValidate(config)

    val dataSource = HikariDataSource()
    dataSource.jdbcUrl = config.jdbcUrl
    dataSource.connectionTimeout = config.connectionTimeoutMillis
    dataSource.keepaliveTime = config.keepAliveTime
    dataSource.maxLifetime = config.maxLifetime
    dataSource.idleTimeout = config.idleTimeoutMillis
    dataSource.minimumIdle = config.minimumIdle
    dataSource.maximumPoolSize = config.maximumPoolSize

    dataSource.username = config.username
    dataSource.password = config.password

    return dataSource
}
