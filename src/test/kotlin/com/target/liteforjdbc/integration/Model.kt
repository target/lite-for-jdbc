package com.target.liteforjdbc.integration

import com.target.liteforjdbc.getEnum
import com.target.liteforjdbc.getInstant
import java.sql.ResultSet
import java.time.Instant

data class Model(
    val id: Int,
    val field1: String,
    val field2: Int,
    val field3: Instant,
    val annoyedParent: AnnoyedParent
)

enum class AnnoyedParent {
    ONE,
    TWO,
    TWO_AND_A_HALF,
    THREE
}

val modelResultSetMap = { resultSet: ResultSet ->
    resultSet.run {
        Model(
            id = getInt("id"),
            field1 = getString("field1"),
            field2 = getInt("field2"),
            field3 = checkNotNull(getInstant("field3")),
            annoyedParent = checkNotNull(getEnum<AnnoyedParent>("annoyed_parent"))
        )
    }
}

val countResultSetMap = { resultSet: ResultSet -> resultSet.getInt("cnt") }
