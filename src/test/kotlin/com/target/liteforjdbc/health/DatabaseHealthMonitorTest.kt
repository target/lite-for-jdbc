package com.target.liteforjdbc.health

import com.target.liteforjdbc.Db
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DatabaseHealthMonitorTest {

    @MockK(relaxed = true)
    lateinit var mockDb: Db

    lateinit var healthMonitor: DatabaseHealthMonitor


    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `validate healthy`() {
        // SETUP
        healthMonitor = DatabaseHealthMonitor(mockDb)
        every { mockDb.executeQuery(sql = DEFAULT_CHECK_QUERY, rowMapper = ::healthRowMapper) } returns true

        // TEST
        val result = healthMonitor.check()

        // VERIFY
        assertSoftly(result) {
            name shouldBe DEFAULT_MONITOR_NAME
            details shouldBe ""
            isHealthy shouldBe true
        }

    }

    @Test
    fun `validate healthy custom name and query`() {
        // SETUP
        healthMonitor = DatabaseHealthMonitor(mockDb, "customName", "CUSTOM QUERY")
        every { mockDb.executeQuery(sql = "CUSTOM QUERY", rowMapper = ::healthRowMapper) } returns true

        // TEST
        val result = healthMonitor.check()

        // VERIFY
        assertSoftly(result) {
            name shouldBe "customName"
            details shouldBe ""
            isHealthy shouldBe true
        }
    }


    @Test
    fun `validate unhealthy`() {
        // SETUP
        healthMonitor = DatabaseHealthMonitor(mockDb)
        every { mockDb.executeQuery(sql = DEFAULT_CHECK_QUERY, rowMapper = ::healthRowMapper) } throws RuntimeException("Exception message")

        // TEST
        val result = healthMonitor.check()

        // VERIFY
        assertSoftly(result) {
            name shouldBe DEFAULT_MONITOR_NAME
            details shouldBe "Exception message"
            isHealthy shouldBe false
        }
    }
}
