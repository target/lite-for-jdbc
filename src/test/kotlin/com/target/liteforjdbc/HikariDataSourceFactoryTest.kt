package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HikariDataSourceFactoryTest {

    @Test
    fun `Test dataSource`() {
        val config = DbConfig(
            type = DbType.POSTGRES,
            jdbcUrl = "JDBC:",
            username = "user",
            password = "password",
            databaseName = "dbName",
            ssl = true,
            connectionTimeoutMillis = 1000,
            idleTimeoutMillis = 2000,
            keepAliveTime = 3000,
            maxLifetime = 4000,
            minimumIdle = 1,
            maximumPoolSize = 10
        )

        val result = buildHikariDataSource(config)

        result.jdbcUrl shouldBe "JDBC:"
        result.connectionTimeout shouldBe 1000
        result.idleTimeout shouldBe 2000
        result.keepaliveTime shouldBe 3000
        result.maxLifetime shouldBe 4000
        result.minimumIdle shouldBe 1
        result.maximumPoolSize shouldBe 10

        result.username shouldBe "user"
        result.password shouldBe "password"
    }

}
