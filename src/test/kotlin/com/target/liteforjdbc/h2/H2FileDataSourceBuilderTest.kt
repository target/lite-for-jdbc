package com.target.liteforjdbc.h2

import com.target.liteforjdbc.DbConfig
import com.target.liteforjdbc.DbType
import com.target.liteforjdbc.buildH2FileDataSource
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class H2FileDataSourceBuilderTest {

    @Test
    fun `Test calculateH2FileConfig valid`() {
        val config = DbConfig(
            type = DbType.H2_FILE,
            databaseName = "dbName",
            username = "user",
            password = "password",
        )

        val result = buildH2FileDataSource(config) as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:h2:file:dbName;DB_CLOSE_DELAY=-1;PASSWORD=password;USER=user"
        result.username shouldBe "user"
        result.password shouldBe "password"
    }

    @Test
    fun `Test calculateH2FileConfig wrong type`() {
        val config = DbConfig(
            type = DbType.H2_INMEM,
            databaseName = "dbName",
            username = "user",
            password = "password",
        )

        shouldThrowWithMessage<IllegalStateException>(
            "type was expected to be \"H2_FILE\" but was \"H2_INMEM\""
        ) {
            buildH2FileDataSource(config)
        }
    }

}
