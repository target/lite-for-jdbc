package com.target.liteforjdbc

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime

class KotlinClassToMapTest {


    @Test
    fun `Test propertiesToMap`() {
        val value = Domain1(
            i = 1,
            realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe = "string",
            `value's name has special chars` = 10L
        )

        val result = value.propertiesToMap()

        result shouldNotBe null
        result.size shouldBe 4
        result["i"] shouldBe 1
        result["realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe"] shouldBe "string"
        result["value's name has special chars"] shouldBe 10L
        result["nullable"] shouldBe null
    }

    @Test
    fun `Test propertiesToMap with exclude`() {
        val value = Domain1(
            i = 1,
            realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe = "string",
            `value's name has special chars` = 10L,
            nullable = "Not Null"
        )

        val result = value.propertiesToMap(exclude = listOf("i", "value's name has special chars"))

        result shouldNotBe null
        result.size shouldBe 2
        result["realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe"] shouldBe "string"
        result["nullable"] shouldBe "Not Null"
    }

    @Test
    fun `Test propertiesToMap with name transformer`() {
        val value = Domain1(
            i = 1,
            realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe = "string",
            `value's name has special chars` = 10L,
            nullable = "Not Null"
        )

        val result = value.propertiesToMap(nameTransformer = {
            it.replace(" ", "").replace("'", "").take(15)
        })

        result shouldNotBe null
        result.size shouldBe 4
        result["i"] shouldBe 1
        result["realyRealyLongP"] shouldBe "string"
        result["valuesnamehassp"] shouldBe 10L
        result["nullable"] shouldBe "Not Null"
    }

    @Test
    fun `Test propertiesToMap with camelToSnake transformer`() {
        val value = Domain1(
            i = 1,
            realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe = "string",
            `value's name has special chars` = 10L,
            nullable = "Not Null"
        )

        val result = value.propertiesToMap(nameTransformer = ::camelToSnakeCase)

        result shouldNotBe null
        result.size shouldBe 4
        result["i"] shouldBe 1
        result["realy_realy_long_property_name_that_shouldnt_exist_but_by_testing_this_we_should_be_safe"] shouldBe "string"
        result["value's name has special chars"] shouldBe 10L
        result["nullable"] shouldBe "Not Null"
    }

    @Test
    fun `Test propertiesToMap with override values`() {
        val value = Domain1(
            i = 1,
            realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe = "string",
            `value's name has special chars` = 10L,
            nullable = "Not Null"
        )

        val result = value.propertiesToMap(override = mapOf(
            "nullable" to "Null"
        ))

        result shouldNotBe null
        result.size shouldBe 4
        result["i"] shouldBe 1
        result["realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe"] shouldBe "string"
        result["value's name has special chars"] shouldBe 10L
        result["nullable"] shouldBe "Null"
    }

    @Test
    fun `propertiesToMap handles Instant`() {
        val value = TimedEvent(
            description = "Sample",
            occurredAt = Instant.ofEpochSecond(12L)
        )
        val result = value.propertiesToMap()
        result shouldNotBe null
        result.size shouldBe 2
        result["description"] shouldBe "Sample"
        result["occurredAt"] shouldBe LocalDateTime.of(1970, 1, 1, 0, 0, 12)
    }

    @Test
    fun `toJdbcValue handles null`() {
        toJdbcValue(null) shouldBe null
    }

    @Test
    fun `toJdbcValue keeps value as is if it's not Instant`() {
        toJdbcValue("Anything") shouldBe "Anything"
    }

    @Test
    fun `toJdbcValue converts Instant to LocalDateTime`() {
        toJdbcValue(Instant.ofEpochSecond(12L)) shouldBe LocalDateTime.of(1970, 1, 1, 0, 0, 12)
    }

    private data class Domain1(
        val i: Int,
        val realyRealyLongPropertyNameThatShouldntExistButByTestingThisWeShouldBeSafe: String,
        val `value's name has special chars`: Long,
        val nullable: Any? = null,
    )

    data class TimedEvent(
        val description: String,
        val occurredAt: Instant
    )
}
