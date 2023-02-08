package com.target.liteforjdbc

import com.target.liteforjdbc.h2.buildH2FileDataSource
import com.target.liteforjdbc.h2.buildH2InMemDataSource
import com.target.liteforjdbc.postgres.buildPostgresDataSource
import javax.sql.DataSource


typealias DataSourceBuilder = (DbConfig) -> DataSource

object DataSourceFactory {
    private val registry = mutableMapOf(
        DbType.H2_INMEM to ::buildH2InMemDataSource as DataSourceBuilder,
        DbType.H2_FILE to ::buildH2FileDataSource as DataSourceBuilder,
        DbType.POSTGRES to ::buildPostgresDataSource as DataSourceBuilder,
    )

    private fun getDataSourceFactory(config: DbConfig): DataSourceBuilder {
        val key = config.type
        val entry = registry[key]
        checkNotNull(entry) { "$key isn't registered with DataSourceFactory. Registered List : ${registry.keys.joinToString(",")}"}
        return entry
    }

    fun registerDataSourceBuilder(type: String, entry: DataSourceBuilder) {
        registry[type] = entry
    }

    fun dataSource(config: DbConfig): DataSource {
        val factoryFunction = getDataSourceFactory(config)
        return factoryFunction(config)
    }
}
