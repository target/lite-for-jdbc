package com.target.liteforjdbc

import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


internal class ParsedQueryTest {

    @Test
    fun parsedQueryNoParams() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = 'value'"
        val parsedQuery = ParsedQuery(sql)
        parsedQuery.toOriginalSql() shouldBe sql
        parsedQuery.toSql() shouldBe sql
    }

    @Test
    fun parsedQueryPositionalOnly() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = ? AND T.field2 = ?"
        val parsedQuery = ParsedQuery(sql)
        parsedQuery.toOriginalSql() shouldBe sql

        parsedQuery.getPositionalParameterSize() shouldBe 2
        parsedQuery.getNamedParametersSize() shouldBe 0

        parsedQuery.toSql() shouldBe sql
    }

    @Test
    fun parsedQueryNamedOnly() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = :field_1 AND T.field2 =:field-2"
        val escapedSql = "SELECT * FROM \"TABLE\" T WHERE T.field = ? AND T.field2 =?"
        val parsedQuery = ParsedQuery(sql)
        parsedQuery.toOriginalSql() shouldBe sql
        parsedQuery.getPositionalParameterSize() shouldBe 0
        parsedQuery.getNamedParametersSize() shouldBe 2
        parsedQuery.getNamedParameters()[0].parameterName shouldBe "field_1"
        parsedQuery.getNamedParameters()[1].parameterName shouldBe "field-2"

        parsedQuery.toSql() shouldBe escapedSql
    }

    @Test
    fun parsedQueryDoubleColonReplace() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = ::oracle_system_param"
        val parsedQuery = ParsedQuery(sql)
        val escapedSql = "SELECT * FROM \"TABLE\" T WHERE T.field = :oracle_system_param"
        parsedQuery.toOriginalSql() shouldBe escapedSql
        parsedQuery.toSql() shouldBe escapedSql

        parsedQuery.getPositionalParameterSize() shouldBe 0
        parsedQuery.getNamedParametersSize() shouldBe 0
    }

    @Test
    fun parsedQueryColonInsideQuotes() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = ':literal'"
        val parsedQuery = ParsedQuery(sql)
        parsedQuery.toOriginalSql() shouldBe sql
        parsedQuery.toSql() shouldBe sql

        parsedQuery.getPositionalParameterSize() shouldBe 0
        parsedQuery.getNamedParametersSize() shouldBe 0
    }

    @Test
    fun parsedQueryColonInsideDoubleQuotes() {
        val sql = "SELECT * FROM \"TABLE:COLON\" T WHERE T.field = 'value'"
        val parsedQuery = ParsedQuery(sql)
        parsedQuery.toOriginalSql() shouldBe sql
        parsedQuery.toSql() shouldBe sql

        parsedQuery.getPositionalParameterSize() shouldBe 0
        parsedQuery.getNamedParametersSize() shouldBe 0
    }

    @Test
    fun parsedQueryEscapedSingQuotes() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = 'value''s'"
        val parsedQuery = ParsedQuery(sql)
        parsedQuery.toOriginalSql() shouldBe sql
        parsedQuery.toSql() shouldBe sql

        parsedQuery.getPositionalParameterSize() shouldBe 0
        parsedQuery.getNamedParametersSize() shouldBe 0
    }

    @Test
    fun parsedQueryEscapedQuestionMark() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = ??"
        val parsedQuery = ParsedQuery(sql)
        parsedQuery.toOriginalSql() shouldBe sql
        parsedQuery.toSql() shouldBe sql

        parsedQuery.getPositionalParameterSize() shouldBe 0
        parsedQuery.getNamedParametersSize() shouldBe 0
    }

    @Test
    fun parsedQueryWithIllegalNamedParameter() {
        val sql = "SELECT * FROM \"TABLE\" T WHERE T.field = :~bad_name"
        shouldThrowMessage(
            ": was followed by an illegal character \"~\". It must be followed by a legal " +
                    "parameter character, which includes a letter, number, dash or underscore, " +
                    "or another colon to escape a literal colon"
        ) {
            ParsedQuery(sql)
        }
    }
}
