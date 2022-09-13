package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class DatasourceFactoryTest {

    @Test
    fun `Test Postgres`() {
        val factory = DatasourceFactory(
            DbConfig(
                type = DbType.POSTGRES,
                host = "host",
                username = "user",
                password = "password",
                databaseName = "dbName"
            )
        )

        val result = factory.dataSource() as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:postgresql://host:5432/dbName"
    }

    @Test
    fun `Test H2 InMem`() {
        val factory = DatasourceFactory(
            DbConfig(
                type = DbType.H2_INMEM,
                host = "host",
                username = "user",
                password = "password",
                databaseName = "dbName"
            )
        )

        val result = factory.dataSource() as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:h2:mem:dbName;DB_CLOSE_DELAY=-1"
    }

}
