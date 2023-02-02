package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class H2DatasourceFactoryTest {

    @Test
    fun `Test buildH2JdbcUrl valid`() {
        val config = DbConfig(
                type = DbType.H2_FILE,
                host = "host",
                username = "user",
                password = "password",
                databaseName = "/file/location",
            )

        val result = buildH2JdbcUrl(config, "file")

        result shouldBe "jdbc:h2:file:/file/location;DB_CLOSE_DELAY=-1;PASSWORD=password;USER=user"
    }

    @Test
    fun `Test buildH2JdbcUrl `() {
        val config = DbConfig(
            type = DbType.H2_FILE,
            host = "host",
            username = "user",
            password = "password",
            databaseName = "/file/location",
        )

        val result = buildH2JdbcUrl(config, "file")

        result shouldBe "jdbc:h2:file:/file/location;DB_CLOSE_DELAY=-1;PASSWORD=password;USER=user"
    }

}
