package com.target.liteforjdbc

import javax.sql.DataSource

fun buildH2FileDatasource(config: DbConfig): DataSource {
    checkEqual(config.type, DbType.H2_FILE, "type")
    val fixedConfig = config.copy(
        jdbcUrl = buildH2JdbcUrl(config, "file")
    )
    return hikariDataSource(fixedConfig)
}
