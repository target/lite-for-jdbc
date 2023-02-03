package com.target.liteforjdbc

import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DbConfigTest {

    @Test
    fun `Test Environment host`() {
        val expectedHost = "expectedHost"
        val config = withEnvironment(DATABASE_HOST_ENV_NAME, expectedHost) {
            DbConfig(
                username = "user",
                password = "password",
                databaseName = "dbName"
            )
        }

        config.host shouldBe expectedHost
    }

}