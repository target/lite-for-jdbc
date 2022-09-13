package com.target.liteforjdbc.health

import com.target.health.HealthCheckResponse
import com.target.health.HealthMonitor
import com.target.liteforjdbc.Db
import java.sql.ResultSet

const val DEFAULT_MONITOR_NAME = "db"
const val DEFAULT_CHECK_QUERY = "SELECT 1"

class DatabaseHealthMonitor(
    private val db: Db,
    override val name: String = DEFAULT_MONITOR_NAME,
    private val checkQuery: String = DEFAULT_CHECK_QUERY,
) : HealthMonitor {

    override fun check(): HealthCheckResponse {
        return try {
            checkNotNull(db.executeQuery(sql = checkQuery, rowMapper = ::healthRowMapper))
            HealthCheckResponse(name = name, isHealthy = true, "")
        } catch (e: Exception) {
            HealthCheckResponse(name = name, isHealthy = false, e.localizedMessage)
        }
    }

}

@Suppress("UNUSED_PARAMETER")
fun healthRowMapper(rs: ResultSet): Boolean = true
