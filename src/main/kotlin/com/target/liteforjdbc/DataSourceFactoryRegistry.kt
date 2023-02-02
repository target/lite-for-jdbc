package com.target.liteforjdbc

import javax.sql.DataSource


typealias DataSourceFactory = (DbConfig) -> DataSource

object DataSourceFactoryRegistry {
    private val registry = mutableMapOf(
        DbType.H2_INMEM to ::buildH2InMemDatasource as DataSourceFactory,
        DbType.H2_FILE to ::buildH2FileDatasource as DataSourceFactory,
        DbType.POSTGRES to ::buildPostgresDatasource as DataSourceFactory,
    )

    private fun getDataSourceFactory(config: DbConfig): DataSourceFactory {
        val key = config.type
        val entry = registry[key]
        checkNotNull(entry) { "$key isn't registered with DataSourceFactory. Registered List : ${registry.keys.joinToString(",")}"}
        return entry
    }

    fun registerDataSourceFactory(type: String, entry: DataSourceFactory) {
        registry[type] = entry
    }

    fun dataSource(config: DbConfig): DataSource {
        val factoryFunction = getDataSourceFactory(config)
        return factoryFunction(config)
    }
}
