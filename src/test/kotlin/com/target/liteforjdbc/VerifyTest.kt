package com.target.liteforjdbc

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class VerifyTest {
    @Test
    fun `Test checkNotBlank with field name`() {
        checkNotBlank("test", "fieldName") shouldBe "test"
        shouldThrowWithMessage<IllegalStateException>("fieldName is required but was blank") {
            checkNotBlank(null, "fieldName")
        }
        shouldThrowWithMessage<IllegalStateException>("fieldName is required but was blank") {
            checkNotBlank("", "fieldName")
        }
        shouldThrowWithMessage<IllegalStateException>("fieldName is required but was blank") {
            checkNotBlank("  ", "fieldName")
        }
    }

    @Test
    fun `Test checkNotBlank with custom message`() {
        val customMessage = fun(): String { return "Custom message" }

        checkNotBlank("test", customMessage) shouldBe "test"
        shouldThrowWithMessage<IllegalStateException>("Custom message") {
            checkNotBlank(null, customMessage)
        }
        shouldThrowWithMessage<IllegalStateException>("Custom message") {
            checkNotBlank("", customMessage)
        }
        shouldThrowWithMessage<IllegalStateException>("Custom message") {
            checkNotBlank("  ", customMessage)
        }
    }

    @Test
    fun `Test checkEqual with field name`() {
        checkEqual("expected", "expected", "fieldName") shouldBe "expected"
        checkEqual(null as String?, null, "fieldName") shouldBe null
        shouldThrowWithMessage<IllegalStateException>("fieldName was expected to be \"expected\" but was \"bad\"") {
            checkEqual("bad", "expected", "fieldName")
        }
    }

    @Test
    fun `Test checkEqual with custom message`() {
        val customMessage = fun(): String { return "Custom message" }

        checkEqual("expected", "expected", customMessage) shouldBe "expected"
        checkEqual(null as String?, null, customMessage) shouldBe null
        shouldThrowWithMessage<IllegalStateException>("Custom message") {
            checkEqual("bad", "expected", customMessage)
        }
    }
}