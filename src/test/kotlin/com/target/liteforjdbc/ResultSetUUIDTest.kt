package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.util.*


class ResultSetUUIDTest {

    @MockK(relaxed = true)
    lateinit var mockResultSet: ResultSet

    private val id1: UUID = UUID.randomUUID()
    private val id2: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockResultSet.getObject(1, UUID::class.java) }.answers { id1 }
        every { mockResultSet.getObject("column1", UUID::class.java) }.answers { id1 }

        every { mockResultSet.getObject(2, UUID::class.java) }.answers { id2 }
        every { mockResultSet.getObject("column2", UUID::class.java) }.answers { id2 }
    }

    @Test
    fun `getUUID index`() {
        mockResultSet.getUUID(1) shouldBe id1
        mockResultSet.getUUID(2) shouldBe id2
    }

    @Test
    fun `getUUID column name`() {
        mockResultSet.getUUID("column1") shouldBe id1
        mockResultSet.getUUID("column2") shouldBe id2
    }

}
