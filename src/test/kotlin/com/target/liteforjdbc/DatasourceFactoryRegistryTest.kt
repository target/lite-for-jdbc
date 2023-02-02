package com.target.liteforjdbc

import com.zaxxer.hikari.HikariDataSource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import org.junit.jupiter.api.Test
import javax.sql.DataSource

class DataSourceFactoryRegistryTest {

    @Test
    fun `Test Postgres`() {
        val config = DbConfig(
            type = DbType.POSTGRES,
            host = "host",
            username = "user",
            password = "password",
            databaseName = "dbName"
        )

        val result = DataSourceFactoryRegistry.dataSource(config) as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:postgresql://host:5432/dbName"
    }

    @Test
    fun `Test H2 InMem`() {
        val config = DbConfig(
            type = DbType.H2_INMEM,
            host = "host",
            username = "user",
            password = "password",
            databaseName = "dbName"
        )

        val result = DataSourceFactoryRegistry.dataSource(config) as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:h2:mem:dbName;DB_CLOSE_DELAY=-1;PASSWORD=password;USER=user"
    }

    @Test
    fun `Test H2 File`() {
        val config = DbConfig(
            type = DbType.H2_FILE,
            host = "host",
            username = "user",
            password = "password",
            databaseName = "./dbDir/dbFile"
        )

        val result = DataSourceFactoryRegistry.dataSource(config) as HikariDataSource

        result.jdbcUrl shouldBe "jdbc:h2:file:./dbDir/dbFile;DB_CLOSE_DELAY=-1;PASSWORD=password;USER=user"
    }

    @Test
    fun `Test Register`() {
        val typeName = "custom"
        val config = DbConfig(
            type = typeName,
            username = "user",
            password = "pw"
        )
        val mockDataSource = mockk<DataSource>()

        DataSourceFactoryRegistry.registerDataSourceFactory(typeName) {
            mockDataSource
        }

        val result = DataSourceFactoryRegistry.dataSource(config)

        result shouldBeSameInstanceAs mockDataSource
    }

}
