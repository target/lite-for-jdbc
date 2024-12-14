package com.target.liteforjdbc.h2

import com.target.liteforjdbc.DbConfig
import com.target.liteforjdbc.DbType
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class H2InMemDataSourceBuilderTest {

    @Test
    fun `Test calculateH2InMemConfig valid`() {
        val config = DbConfig(
            type = DbType.H2_INMEM,
            databaseName = "dbName",
            username = "user",
            password = "password",
        )

        val result = buildH2InMemDataSource(config) as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:h2:mem:dbName;DB_CLOSE_DELAY=-1;PASSWORD=password;USER=user"
        result.username shouldBe "user"
        result.password shouldBe "password"
    }

    @Test
    fun `Test calculateH2InMemConfig wrong type`() {
        val config = DbConfig(
            type = DbType.H2_FILE,
            databaseName = "dbName",
            username = "user",
            password = "password",
        )

        shouldThrowWithMessage<IllegalStateException>(
            "type was expected to be \"H2_INMEM\" but was \"H2_FILE\""
        ) {
            buildH2InMemDataSource(config)
        }
    }

}
