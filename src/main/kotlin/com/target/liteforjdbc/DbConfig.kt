package com.target.liteforjdbc

object DbType {
    const val POSTGRES = "POSTGRES"
    const val H2_INMEM = "H2_INMEM"
    const val H2_FILE = "H2_FILE"
}

data class DbConfig(
    val type: String = DbType.POSTGRES,
    val host: String = System.getenv(DATABASE_HOST_ENV_NAME) ?: "127.0.0.1",
    val port: Int = 5432,
    val username: String,
    val password: String,
    val databaseName: String? = null,
    val jdbcUrl: String? = null,
    val ssl: Boolean = false,
    val connectionTimeoutMillis: Long = 10_000,
    val idleTimeoutMillis: Long = 120_000,
    val keepAliveTime: Long = 180_000,
    val maxLifetime: Long = 300_000,
    val minimumIdle: Int = 1,
    val maximumPoolSize: Int = 5,
    val dataSourceProperties: List<DataSourceProperty> = listOf(),
)
