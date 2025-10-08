This migration document seeks to give the simplest path to update a project from Lite For JDBC to JDBI.

## Dependencies
The project dependencies will need to be modified from lite-for-jdbc to jdbi. The changes if you are using versions.toml files, the dependencies will look like this

```toml
[versions]
lite-for-jdbc = "2.6.0"

[libraries]
jdbc-lite = { module = "com.target:lite-for-jdbc", version.ref = "lite-for-jdbc" }
```

```toml
[versions]
hikari = "6.2.1"
jdbi = "3.47.0"

[libraries]
jdbi-core = {module = "org.jdbi:jdbi3-core", version.ref = "jdbi"}
jdbi-kotlin = {module = "org.jdbi:jdbi3-kotlin", version.ref = "jdbi"}
jdbi-sqlobject = {module = "org.jdbi:jdbi3-sqlobject", version.ref = "jdbi"}
hikari-cp = {module = "com.zaxxer:HikariCP", version.ref = "hikari"}

[bundles]
jdbi = [ "jdbi-core", "jdbi-sqlobject", "jdbi-kotlin", "postgresql", "hikari-cp" ]
```
## Config

To minimize the changes with the migration, add the following code to your project.

```kotlin
const val DEFAULT_MAXIMUM_POOL_SIZE = 5
val DEFAULT_MAX_LIFETIME = 5.minutes
val DEFAULT_CONNECTION_TIMEOUT = 10.seconds

// Replaces com.target.liteforjdbc.DbConfig
data class JdbiConfig(
    val host: String = getDefaultHost(),
    val databaseName: String,
    val port: Int = 5432,
    val userName: String,
    val password: String,
    val poolName: String = "Pool-$databaseName",
    val maximumPoolSize: Int = DEFAULT_MAXIMUM_POOL_SIZE,
    val maxLifetime: Duration = DEFAULT_MAX_LIFETIME,
    val connectionTimeout: Duration = DEFAULT_CONNECTION_TIMEOUT,
    val schemaName: String = "public",
) {
    val hostName = "jdbc:postgresql://$host:$port/$databaseName"
}

fun getDefaultHost() = System.getenv("DATABASE_HOST") ?: "127.0.0.1"

// Replaces the constructor call on com.target.liteforjdbc.Db
fun createJdbiDataSource(jdbiConfig: JdbiConfig): Jdbi {
    val config =
        HikariConfig().apply {
            jdbcUrl = jdbiConfig.hostName
            username = jdbiConfig.userName
            password = jdbiConfig.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = jdbiConfig.maximumPoolSize
            maxLifetime = jdbiConfig.maxLifetime.inWholeMilliseconds
            connectionTimeout = jdbiConfig.connectionTimeout.inWholeMilliseconds
            poolName = jdbiConfig.poolName
            schema = jdbiConfig.schemaName
        }
    val jdbi = Jdbi.create(HikariDataSource(config))
    jdbi.installPlugin(KotlinPlugin())
    return jdbi
}
```

Now replace liteForJdbc's DbConfig with JdbiConfig

```kotlin
data class Config(
    databaseConfig: DbConfig
)

val db = Db(config.databaseConfig)
```

becomes 

```kotlin
data class Config(
    databaseConfig: JdbiConfig
)

val jdbi = createJdbiDataSource(config.databaseConfig)
```

The corresponding configuration files won't need to change
```conf
databaseConfig {
  username = "user_name"
  password = "a_good_password"
  databaseName = "the_database"
  host = "postgres"
}
```

The JDBI object should now replace the Db object everywhere.

## Queries

To use the new JDBI object, there will be a few small tweaks required in each query. As a generic example, 

```kotlin
db.findAll(
    sql = "SELECT * FROM Table WHERE field1 = :arg1 AND field2 = :arg2",
    args = mapOf("arg1" to value1, "arg2" to value2),
    rowMapper = ::customRowMapperFunction
)
```

Becomes

```kotlin
jdbi.withHandleUnchecked { handle ->
    handle.createQuery("SELECT * FROM Table WHERE field1 = :arg1 AND field2 = :arg2")
        .bind("arg1", value1)
        .bind("arg2", value2)
        .mapTo(Domain::class.java)
        .list()
}
```


### Paremeters

Lite for JDBC and JDBI use the same parameter replacement syntax. A colon is used to mark named parameters, and a double colon is used to render a single colon into the query. So no changes are needed

`SELECT * FROM TABLE T WHERE T.field = ::oracle_system_param AND T.field_2 = :queryParameter`

#### Individual paramters
```
    args = mapOf("arg1" to value1, "arg2" to value2),
```

With jdbi you can replace this with

```
    .bind("arg1", value1)
    .bind("arg2", value2)
```

or if the map is defined elsewhere, bindMap is used

```
    .bindMap(mapOfNameToValue)
```


#### Domain object paramters

```
    args = domain.parametersToMap(),
```

With JDBI, this becomes

```
    .bindKotlin(domain)
```

### Mapping return objects

Mapping a resultset is very manual in Lite for JDBC a mapping function is needed
```kotlin
    private fun mapToDomain(resultSet: ResultSet) = with(resultSet) {
        Domain(
            id = getUUID("id"),
            field1 = getString("field_1"),
        )
    }
```

And then that function is used as the rowMapper in each query

```kotlin
    rowMapper = ::mapToDomain,
```

JDBI replaces that with a mapper that uses name matching

```kotlin
    .mapTo(Domain::class.java)
```

If custom mapping is needed, that is beyond the scope of this document. Look into the [JDBI Mappers](https://jdbi.org/#_mappers)

### Transactions

Transactions are handled in a very similar way between both libraries. It is possible that no additional changes will be needed for those transactions.

```
db.withTransaction { tx ->
    db.executeUpdate(
        sql = "INSERT INTO domain (id, field) VALUES(:id, :field)",
        args = domain.propertiesToMap(),
    )
    tx.commit()
}
```

```
jdbi.withTransactionUnchecked { tx ->
    tx.createUpdate("INSERT INTO domain (id, field) VALUES(:id, :field)")
        .bindKotlin(domain)
        .execute()
    tx.commit()
}
```

### Query Types

Here are a few common query types written in Lite for JDBC and JDBI for comparison.

#### Select a list

Lite For JDBC
```kotlin
db.findAll(
    sql = "SELECT * FROM domain",
    rowMapper = ::mapToDomain,
)
```

JDBI
```kotlin
jdbi.withHandleUnchecked { handle ->
    handle.createQuery("SELECT * FROM domain")
        .mapTo(Domain::class.java)
        .list()
}
```

#### Select a single row

Lite For JDBC
```kotlin
db.executeQuery(
    sql = "SELECT * FROM domain WHERE id = :id",
    args = mapOf( "id", id )
    rowMapper = ::mapToDomain,
)
```

JDBI
```kotlin
jdbi.withHandleUnchecked { handle ->
    handle.createQuery("SELECT * FROM domain WHERE id = :id")
        .bind("id", id)
        .mapTo(Domain::class.java)
        .findFirst().orElse(null)
}
```

However, JDBI has multiple options to enforce the expected result size. 

| Function  | Ensures 1 result | Ensures ONLY 1 result |
| findFirst | No (Optional)    | No                    |
| first     | Yes              | No                    |
| findOne   | No (Optional)    | Yes                   |
| one       | Yes              | Yes                   |

For the "find" options the result will be returned as an Optional wrapper. Adding `.orElse(null)` as seen the JDBI example above will result in a Kotlin nullable object.

#### Batches
Lite For JDBC
```kotlin
db.executeBatch(
    sql = "INSERT INTO domain (id, field) VALUES(:id, :field)",
    args = domainList.map { it.propertiesToMap() },
)
```

JDBI
```kotlin
jdbi.withHandleUnchecked {
    val batch = handle.prepareBatch("INSERT INTO domain (id, field) VALUES(:id, :field)")
    domainList.forEach { domain ->
        batch.bindKotlin(domain).add()
    }
    val counts = batch.execute().asList()
}
```