package com.target.liteforjdbc

import javax.sql.DataSource


typealias GenerateDataSource = () -> DataSource

class DatasourceFactory(config: DbConfig) {

    val generateDataSource: GenerateDataSource =
        when (config.type) {
            DbType.POSTGRES -> PostgresDatasourceFactory(config)::dataSource
            DbType.H2_INMEM -> H2InMemDatasourceFactory(config)::dataSource
        }


    fun dataSource(): DataSource = generateDataSource()


}
