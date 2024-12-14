package com.target.liteforjdbc.h2

import com.target.liteforjdbc.*
import javax.sql.DataSource

fun buildH2InMemDataSource(config: DbConfig): DataSource {
    checkEqual(config.type, DbType.H2_INMEM, "type")
    val fixedConfig = config.copy(
        jdbcUrl = buildH2JdbcUrl(config, "mem")
    )
    return buildHikariDataSource(fixedConfig)
}
