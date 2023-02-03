package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource

fun buildPostgresDataSource(config: DbConfig): HikariDataSource {
    checkEqual(config.type, DbType.POSTGRES, "type")
    checkNotBlank(config.host, "host")
    checkNotBlank(config.databaseName, "databaseName")

    val fullConfig = config.copy(
        jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.databaseName}"
    )

    val dataSource = buildHikariDataSource(fullConfig)
    dataSource.addDataSourceProperty("reWriteBatchedInserts", "true")

    if (config.ssl) {
        dataSource.addDataSourceProperty("ssl", true)
        dataSource.addDataSourceProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory")
        dataSource.addDataSourceProperty("sslmode", "require")
    }

    return dataSource
}
