package com.target.liteforjdbc.postgres

import com.target.liteforjdbc.IntParameterType
import com.target.liteforjdbc.integration.AnnoyedParent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.sql.Types

class PostgresEnumValueTest {
    @Test
    fun `Test postgresEnumValue`() {
        val result = postgresEnumValue(AnnoyedParent.ONE)

        result.value shouldBe AnnoyedParent.ONE
        result.precision shouldBe null
        result.type::class shouldBe IntParameterType::class
        result.type.intType shouldBe Types.OTHER
    }
}