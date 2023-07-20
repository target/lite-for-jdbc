package com.target.liteforjdbc

import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DbConfigTest {

    @Test
    fun `Test TargetServerType jdbc parameter values`() {
        TargetServerType.ANY.jdbcParameterValue shouldBe "any"

        TargetServerType.PRIMARY.jdbcParameterValue shouldBe "primary"
        TargetServerType.PREFER_PRIMARY.jdbcParameterValue shouldBe "preferPrimary"

        TargetServerType.SECONDARY.jdbcParameterValue shouldBe "secondary"
        TargetServerType.PREFER_SECONDARY.jdbcParameterValue shouldBe "preferSecondary"
    }


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