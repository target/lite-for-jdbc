package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource

const val DATABASE_HOST_ENV_NAME = "DATABASE_HOST"

/**
 * DataSource factory for connections via a Hikari pool.
 */

fun buildHikariDataSource(config: DbConfig): HikariDataSource {
    checkNotNull(config.jdbcUrl) { "JDBCUrl is required for Hikari Data Sources, but was null" }

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

    config.dataSourceProperties.forEach { dataSourceProperty ->
        dataSourceProperty.apply {
            dataSource.addDataSourceProperty(propertyName, propertyValue)
        }
    }

    return dataSource
}
