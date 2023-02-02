package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource

fun buildPostgresJdbcUrl(config: DbConfig): String {
    checkNotBlank(config.host, "host")
    checkNotBlank(config.databaseName, "databaseName")
    return "jdbc:postgresql://${config.host}:${config.port}/${config.databaseName}"
}

fun buildPostgresDatasource(config: DbConfig): HikariDataSource {
    checkEqual(config.type, DbType.POSTGRES, "type")
    val fullConfig = config.copy(
        jdbcUrl = buildPostgresJdbcUrl(config)
    )

    val dataSource = hikariDataSource(fullConfig)
    dataSource.addDataSourceProperty("reWriteBatchedInserts", "true")

    if (config.ssl) {
        dataSource.addDataSourceProperty("ssl", true)
        dataSource.addDataSourceProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory")
        dataSource.addDataSourceProperty("sslmode", "require")
    }

    return dataSource
}
