package com.target.liteforjdbc


internal class ParsedQuery(sql: String) {

    private val queryList: List<QueryPiece>

    private val NAMED_PARAMS_CHARS = ('a'..'z').toList() +
            ('A'..'Z').toList() +
            ('0'..'9').toList() +
            '-' + '_'

    init {
        val context = ParsingContext(ParseState.SCANNING, StringBuilder())
        for (c in sql) {
            parse(c, context)
        }
        when (context.state) {
            ParseState.SCANNING -> context.queryPieces.add(SqlSnippet(context.currentString.toString()))
            ParseState.START_POSITIONAL_PARAM -> context.queryPieces.add(PositionalParameter())
            ParseState.IN_NAMED_PARAM -> terminateNamedChar(context)
            else -> throw IllegalStateException("${context.state} is illegal")
        }
        queryList = context.queryPieces.toList()
    }

    fun toSql(): String {
        return queryList.joinToString(separator = "") { it.toSql() }
    }

    fun toOriginalSql(): String {
        return queryList.joinToString(separator = "") { it.toOriginalSql() }
    }

    private fun getPositionalParameters(): List<PositionalParameter> {
        return queryList.filterIsInstance<PositionalParameter>()
    }

    fun getPositionalParameterSize(): Int {
        return getPositionalParameters().size
    }

    fun getNamedParameters(): List<NamedParameter> {
        return queryList.filterIsInstance<NamedParameter>()
    }

    fun getNamedParametersSize(): Int {
        return getNamedParameters().size
    }

    private fun parse(c: Char, context: ParsingContext) {
        when (context.state) {
            ParseState.SCANNING -> parseScanning(c, context)
            ParseState.IN_SINGLE_QUOTE -> parseInSingleQuote(c, context)
            ParseState.IN_DOUBLE_QUOTE -> parseInDoubleQuote(c, context)
            ParseState.START_POSITIONAL_PARAM -> parseStartPositionalParam(c, context)
            ParseState.START_NAMED_PARAM -> parseStartNamedParam(c, context)
            ParseState.IN_NAMED_PARAM -> parseInNamedParam(c, context)
        }
    }

    private fun parseScanning(c: Char, context: ParsingContext) {
        val triggerChars = mapOf(
            '\'' to ParseState.IN_SINGLE_QUOTE,
            '"' to ParseState.IN_DOUBLE_QUOTE,
            ':' to ParseState.START_NAMED_PARAM,
            '?' to ParseState.START_POSITIONAL_PARAM
        )
        if (triggerChars.containsKey(c)) {
            context.state = checkNotNull(triggerChars[c])
            context.queryPieces.add(SqlSnippet(context.currentString.toString()))
            context.currentString = StringBuilder()
        }
        context.currentString.append(c)
    }

    private fun parseInSingleQuote(c: Char, context: ParsingContext) {
        parseQuotes(c, context, '\'')
    }

    private fun parseInDoubleQuote(c: Char, context: ParsingContext) {
        parseQuotes(c, context, '"')
    }

    private fun parseQuotes(c: Char, context: ParsingContext, q: Char) {
        if (c == q) {
            context.currentString.append(c)
            context.state = ParseState.SCANNING
            context.queryPieces.add(SqlSnippet(context.currentString.toString()))
            context.currentString = StringBuilder()
        } else {
            context.currentString.append(c)
        }
    }

    private fun parseStartPositionalParam(c: Char, context: ParsingContext) {
        if (c == '?') {
            context.currentString.append(c)
            context.state = ParseState.SCANNING
            context.queryPieces.add(SqlSnippet(context.currentString.toString()))
            context.currentString = StringBuilder()
        } else {
            context.queryPieces.add(PositionalParameter())
            context.state = ParseState.SCANNING
            context.currentString = StringBuilder()
            parseScanning(c, context)
        }
    }

    private fun parseStartNamedParam(c: Char, context: ParsingContext) {
        if (NAMED_PARAMS_CHARS.contains(c)) {
            context.state = ParseState.IN_NAMED_PARAM
            context.currentString.append(c)
        } else {
            if (c == ':') {
                context.state = ParseState.SCANNING
                context.queryPieces.add(SqlSnippet(":"))
                context.currentString = StringBuilder()
            } else {
                throw Exception(
                    ": was followed by an illegal character \"$c\". It must be followed by a legal " +
                            "parameter character, which includes a letter, number, dash or underscore, " +
                            "or another colon to escape a literal colon"
                )
            }
        }
    }

    private fun parseInNamedParam(c: Char, context: ParsingContext) {
        if (NAMED_PARAMS_CHARS.contains(c)) {
            context.currentString.append(c)
        } else {
            terminateNamedChar(context)
            context.state = ParseState.SCANNING
            context.currentString = StringBuilder()
            parseScanning(c, context)
        }
    }

    private fun terminateNamedChar(context: ParsingContext) {
        val name = context.currentString.toString().substring(1)
        context.queryPieces.add(NamedParameter(name))

    }

}

private enum class ParseState {
    SCANNING,
    START_NAMED_PARAM,
    START_POSITIONAL_PARAM,
    IN_NAMED_PARAM,
    IN_DOUBLE_QUOTE,
    IN_SINGLE_QUOTE
}

private data class ParsingContext(
    var state: ParseState,
    var currentString: StringBuilder,
    val queryPieces: MutableList<QueryPiece> = mutableListOf(),
)

private interface QueryPiece {
    fun toSql(): String
    fun toOriginalSql(): String
}

private data class SqlSnippet(
    val sql: String,
) : QueryPiece {
    override fun toSql(): String {
        return sql
    }

    override fun toOriginalSql(): String {
        return sql
    }
}

data class NamedParameter(
    val parameterName: String,
) : QueryPiece {
    override fun toSql(): String {
        return "?"
    }

    override fun toOriginalSql(): String {
        return ":$parameterName"
    }
}

class PositionalParameter : QueryPiece {
    override fun toSql(): String {
        return "?"
    }

    override fun toOriginalSql(): String {
        return "?"
    }
}
