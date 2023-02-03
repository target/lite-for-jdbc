package com.target.liteforjdbc

fun buildH2JdbcUrl(config: DbConfig, jdbcTypePart: String): String {
    checkNotBlank(config.databaseName) { "config.databaseName is required for H2 dataSources" }
    return "jdbc:h2:${jdbcTypePart}:${config.databaseName};DB_CLOSE_DELAY=-1;PASSWORD=${config.password};USER=${config.username}"
}
