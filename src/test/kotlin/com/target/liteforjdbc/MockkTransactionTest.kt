package com.target.liteforjdbc

import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MockkTransactionTest {
    @MockK(relaxed = true)
    lateinit var mockDb: Db

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun mockkTransaction() {
        val mockTransaction: Transaction = mockkTransaction(mockDb, relaxed = true)

        mockDb.withTransaction { t: Transaction ->
            // This code is actually executed because mockkTransaction has done the necessary setup to make that happen
            t shouldBeSameInstanceAs mockTransaction
            t.rollback()
            t.commit()
        }

        // Verify the two calls were made. Because the mock transaction is relaxed, these didn't need to be setup
        // before the call
        verify {
            mockTransaction.rollback()
            mockTransaction.commit()
        }

        // Those are the only two calls that were made
        confirmVerified(mockTransaction)
    }
}
