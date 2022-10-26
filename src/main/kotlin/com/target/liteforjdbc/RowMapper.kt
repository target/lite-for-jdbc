package com.target.liteforjdbc

import java.sql.ResultSet

typealias RowMapper<T> = (rs: ResultSet) -> T