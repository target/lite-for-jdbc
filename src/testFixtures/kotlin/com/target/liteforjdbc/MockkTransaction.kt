package com.target.liteforjdbc

import io.mockk.every
import io.mockk.mockk

fun mockkTransaction(
    mockDb: Db,
    name: String? = null,
    relaxed: Boolean = false,
    relaxUnitFun: Boolean = false,
): Transaction {
    val mockTransaction: Transaction = mockk(name = name, relaxed = relaxed, relaxUnitFun = relaxUnitFun)

    every {
        mockDb.withTransaction(any() as ((Transaction) -> Any?))
    } answers {
        val method = firstArg() as ((Transaction) -> Any?)
        method(mockTransaction)
    }
    return mockTransaction
}
