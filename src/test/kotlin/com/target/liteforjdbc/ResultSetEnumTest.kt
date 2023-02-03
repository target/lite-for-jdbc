package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.ResultSet


class ResultSetEnumTest {

    enum class TestEnum {
        VALUE1,
        VALUE2,
    }

    @MockK(relaxed = true)
    lateinit var mockResultSet: ResultSet

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockResultSet.getString(1) }.answers { "VALUE1" }
        every { mockResultSet.getString("column1") }.answers { "VALUE1" }

        every { mockResultSet.getString(2) }.answers { "VALUE2" }
        every { mockResultSet.getString("column2") }.answers { "VALUE2" }
    }

    @Test
    fun `getEnum index`() {
        mockResultSet.getEnum<TestEnum>(1) shouldBe TestEnum.VALUE1
        mockResultSet.getEnum<TestEnum>(2) shouldBe TestEnum.VALUE2
    }

    @Test
    fun `getEnum column name`() {
        mockResultSet.getEnum<TestEnum>("column1") shouldBe TestEnum.VALUE1
        mockResultSet.getEnum<TestEnum>("column2") shouldBe TestEnum.VALUE2
    }

}
