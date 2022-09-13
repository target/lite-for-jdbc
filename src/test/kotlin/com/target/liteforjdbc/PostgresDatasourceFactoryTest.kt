package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class PostgresDatasourceFactoryTest {

    @Test
    fun `Test dataSource with SSL and no defaults`() {
        val factory = PostgresDatasourceFactory(
            DbConfig(
                type = DbType.POSTGRES,
                host = "host",
                port = 1234,
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
        )

        val result = factory.dataSource() as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:postgresql://host:1234/dbName"
        result.connectionTimeout shouldBe 1000
        result.idleTimeout shouldBe 2000
        result.keepaliveTime shouldBe 3000
        result.maxLifetime shouldBe 4000
        result.minimumIdle shouldBe 1
        result.maximumPoolSize shouldBe 10
        result.dataSourceProperties["reWriteBatchedInserts"] shouldBe "true"

        result.dataSourceProperties["ssl"] shouldBe true
        result.dataSourceProperties["sslfactory"] shouldBe "org.postgresql.ssl.NonValidatingFactory"
        result.dataSourceProperties["sslmode"] shouldBe "require"

        result.username shouldBe "user"
        result.password shouldBe "password"
    }

    @Test
    fun `Test dataSource without SSL and no defaults`() {
        val factory = PostgresDatasourceFactory(
            DbConfig(
                type = DbType.POSTGRES,
                host = "host",
                port = 1234,
                username = "user",
                password = "password",
                databaseName = "dbName",
                ssl = false,
                connectionTimeoutMillis = 1000,
                idleTimeoutMillis = 2000,
                keepAliveTime = 3000,
                maxLifetime = 4000,
                minimumIdle = 1,
                maximumPoolSize = 10
            )
        )

        val result = factory.dataSource() as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:postgresql://host:1234/dbName"
        result.connectionTimeout shouldBe 1000
        result.idleTimeout shouldBe 2000
        result.keepaliveTime shouldBe 3000
        result.maxLifetime shouldBe 4000
        result.minimumIdle shouldBe 1
        result.maximumPoolSize shouldBe 10
        result.dataSourceProperties["reWriteBatchedInserts"] shouldBe "true"

        result.dataSourceProperties.keys shouldNotContain "ssl"
        result.dataSourceProperties.keys shouldNotContain "sslfactory"
        result.dataSourceProperties.keys shouldNotContain "sslmode"

        result.username shouldBe "user"
        result.password shouldBe "password"
    }

    @Test
    fun `Test dataSource host provided in parmaeter and system environment`() {
        val factory = withEnvironment(DATABASE_HOST_ENV_NAME, "ignoredDatabaseHost") {
            PostgresDatasourceFactory(
                DbConfig(
                    host = "host",
                    username = "user",
                    password = "password",
                    databaseName = "dbName"
                )
            )
        }

        val result = factory.dataSource() as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:postgresql://host:5432/dbName"
    }

    @Test
    fun `Test dataSource with maximal defaults`() {
        val factory = withEnvironment(DATABASE_HOST_ENV_NAME, null) {
            PostgresDatasourceFactory(
                DbConfig(
                    username = "user",
                    password = "password",
                    databaseName = "dbName"
                )
            )
        }

        val result = factory.dataSource() as HikariDataSource

        // Overridden values
        result.username shouldBe "user"
        result.password shouldBe "password"

        // Mixed (host and port default)
        result.jdbcUrl shouldBe "jdbc:postgresql://127.0.0.1:5432/dbName"

        // Default values
        result.connectionTimeout shouldBe 10_000
        result.idleTimeout shouldBe 120_000
        result.keepaliveTime shouldBe 180_000
        result.maxLifetime shouldBe 300_000
        result.minimumIdle shouldBe 1
        result.maximumPoolSize shouldBe 5
        result.dataSourceProperties["reWriteBatchedInserts"] shouldBe "true"

        result.dataSourceProperties.keys shouldNotContain "ssl"
        result.dataSourceProperties.keys shouldNotContain "sslfactory"
        result.dataSourceProperties.keys shouldNotContain "sslmode"
    }

    @Test
    fun `Test dataSource with Database Host in the Env`() {
        val factory = withEnvironment(DATABASE_HOST_ENV_NAME, "dbHost") {
            PostgresDatasourceFactory(
                DbConfig(
                    username = "user",
                    password = "password",
                    databaseName = "dbName"
                )
            )
        }

        val result = factory.dataSource() as HikariDataSource

        // Overridden values
        result.username shouldBe "user"
        result.password shouldBe "password"

        // Mixed (host and port default)
        result.jdbcUrl shouldBe "jdbc:postgresql://dbHost:5432/dbName"

        // Default values
        result.connectionTimeout shouldBe 10_000
        result.idleTimeout shouldBe 120_000
        result.keepaliveTime shouldBe 180_000
        result.maxLifetime shouldBe 300_000
        result.minimumIdle shouldBe 1
        result.maximumPoolSize shouldBe 5
        result.dataSourceProperties["reWriteBatchedInserts"] shouldBe "true"

        result.dataSourceProperties.keys shouldNotContain "ssl"
        result.dataSourceProperties.keys shouldNotContain "sslfactory"
        result.dataSourceProperties.keys shouldNotContain "sslmode"
    }
}
