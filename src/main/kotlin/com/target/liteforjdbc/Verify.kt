package com.target.liteforjdbc

typealias MessageGen = () -> Any

fun <T> checkEqual(value: T?, expectedValue: Any?, fieldName: String): T? {
    return checkEqual(value, expectedValue) {
        "$fieldName was expected to be \"$expectedValue\" but was \"$value\""
    }
}

inline fun <T> checkEqual(value: T?, expectedValue: Any?, lazyMessage: MessageGen = {
    "Expected $expectedValue but was $value"
}): T? {
    if (value != expectedValue) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
    return value
}

fun checkNotBlank(value: String?, fieldName: String): String {
    return checkNotBlank(value) {
        "$fieldName is required but was blank"
    }
}

inline fun checkNotBlank(value: String?, lazyMessage: MessageGen = {
    "Required value was blank."
}): String {
    checkNotNull(value, lazyMessage)
    if (value.isBlank()) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
    return value
}
