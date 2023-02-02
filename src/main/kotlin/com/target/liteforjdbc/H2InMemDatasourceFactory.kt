package com.target.liteforjdbc

import javax.sql.DataSource

fun buildH2InMemDatasource(config: DbConfig): DataSource {
    checkEqual(config.type, DbType.H2_INMEM, "type")
    val fixedConfig = config.copy(
        jdbcUrl = buildH2JdbcUrl(config, "mem")
    )
    return hikariDataSource(fixedConfig)
}
