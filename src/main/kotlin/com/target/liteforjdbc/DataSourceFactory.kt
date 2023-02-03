package com.target.liteforjdbc

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
