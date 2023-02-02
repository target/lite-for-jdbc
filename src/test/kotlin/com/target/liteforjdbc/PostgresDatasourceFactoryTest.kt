package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class PostgresDatasourceFactoryTest {

    @Test
    fun `Test buildPostgresDatasource no database`() {
        val config = DbConfig(
            type = DbType.POSTGRES,
            host = "host",
            username = "user",
            password = "password",
        )

        shouldThrowWithMessage<IllegalStateException>(
            "databaseName is required but was blank"
        ) {
            buildPostgresDatasource(config)
        }
    }

    @Test
    fun `Test buildPostgresDatasource no host`() {
        val config = DbConfig(
            type = DbType.POSTGRES,
            host = "",
            databaseName = "dbName",
            username = "user",
            password = "password",
        )

        shouldThrowWithMessage<IllegalStateException>(
            "host is required but was blank"
        ) {
            buildPostgresDatasource(config)
        }
    }

    @Test
    fun `Test buildPostgresDatasource wrong type`() {
        val config = DbConfig(
            type = DbType.H2_FILE,
            databaseName = "dbName",
            username = "user",
            password = "password",
        )

        shouldThrowWithMessage<IllegalStateException>(
            "type was expected to be \"POSTGRES\" but was \"H2_FILE\""
        ) {
            buildPostgresDatasource(config)
        }
    }

    @Test
    fun `Test buildPostgresDatasource with SSL`() {
        val config = DbConfig(
            databaseName = "dbName",
            host = "host",
            port = 1234,
            username = "user",
            password = "password",
            ssl = true,
            connectionTimeoutMillis = 1000,
            idleTimeoutMillis = 2000,
            keepAliveTime = 3000,
            maxLifetime = 4000,
            minimumIdle = 1,
            maximumPoolSize = 10
        )

        val result = buildPostgresDatasource(config)

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
    fun `Test buildPostgresDatasource without SSL`() {
        val config = DbConfig(
            databaseName = "dbName",
            host = "host",
            port = 1234,
            username = "user",
            password = "password",
            ssl = false,
            connectionTimeoutMillis = 1000,
            idleTimeoutMillis = 2000,
            keepAliveTime = 3000,
            maxLifetime = 4000,
            minimumIdle = 1,
            maximumPoolSize = 10
        )

        val result = buildPostgresDatasource(config)

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

}
