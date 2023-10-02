# lite-for-jdbc

Lightweight library to help simplify JDBC database access. Main features:

- Lets you use SQL statements with named parameters
- Automates resource cleanup
- Provides a functions for common database interaction patterns like individual and list result
  handling, updates, and batch statements

<!-- TOC -->
* [lite-for-jdbc](#lite-for-jdbc)
* [Gradle Setup](#gradle-setup)
* [Db Setup](#db-setup)
  * [Custom Database Types](#custom-database-types)
* [Methods](#methods)
  * [executeQuery](#executequery)
  * [findAll](#findall)
  * [executeUpdate](#executeupdate)
  * [executeWithGeneratedKeys](#executewithgeneratedkeys)
  * [executeBatch](#executebatch)
  * [executeBatch Counts only](#executebatch-counts-only)
  * [useNamedParamPreparedStatement](#usenamedparampreparedstatement)
  * [useNamedParamPreparedStatementWithAutoGenKeys](#usenamedparampreparedstatementwithautogenkeys)
  * [useConnection](#useconnection)
* [Query Parameters](#query-parameters)
  * [Named Parameters](#named-parameters)
  * [Positional Params](#positional-params)
* [Row Mapping](#row-mapping)
  * [rowMapper](#rowmapper)
  * [ResultSet/PreparedStatement extensions](#resultsetpreparedstatement-extensions)
  * [Java type to Postgresql column type mapping requirements](#java-type-to-postgresql-column-type-mapping-requirements)
    * [Storing timestamps with timezone](#storing-timestamps-with-timezone)
  * [propertiesToMap](#propertiestomap)
* [Transactions & Autocommit](#transactions--autocommit)
  * [withAutoCommit](#withautocommit)
  * [withTransaction](#withtransaction)
  * [DataSource configuration & AutoCommit](#datasource-configuration--autocommit)
  * [DataSource settings](#datasource-settings)
  * [Testing with mockkTransaction](#testing-with-mockktransaction)
* [IntelliJ SQL language integration](#intellij-sql-language-integration)
* [Development](#development)
  * [Building](#building)
  * [Issues](#issues)
  * [Contributing](#contributing)
    * [Code review standards](#code-review-standards)
    * [Testing standards](#testing-standards)
* [Breaking version changes](#breaking-version-changes)
    * [`1.9.2` -> `2.0.0`](#192---200)
<!-- TOC -->

# Gradle Setup

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    api("com.target:lite-for-jdbc:2.1.1")
}
```

# Db Setup

The core of lite-for-jdbc is the Db class. A Db object is intended to be used as a singleton and
injected as a dependency in repository classes. It requires a DataSource constructor argument,  
and there is a DataSourceFactory to help with that.
The typical recommendation is to use Hikari, which is configured with reasonable defaults, but you can customize
it to any DataSource.
Examples:

Using DataSourceFactory:

```kotlin
val config = DbConfig(
  type = "H2_INMEM",
  username = "user",
  password = "password",
  databaseName = "dbName"
)
val dataSource = DataSourceFactory.dataSource(config)

val db = Db(dataSource)
```

Or you can use the Db constructor that accepts a DbConfig directly. Db will use DataSourceFactory under the covers for you.

```kotlin
val db = Db(DbConfig(
  type = "H2_INMEM",
  username = "user",
  password = "password",
  databaseName = "dbName"
))
```

See `DbConfig` for a full list of configuration options available.

## Custom Database Types

If another implementation of DataSource is required, you can register a custom "Type" to be set on `DbConfig` and build the
respective `DataSource` in a `DataSourceBuilder` lambda as shown below.

```kotlin
DataSourceFactory.registerDataSourceBuilder("custom") {config: DbConfig ->
  val fullConfig = config.copy(
    jdbcUrl = "jdbc:custom:server//${config.host}:${config.port}/${config.databaseName}"
  )
  hikariDataSource(fullConfig)
}
```

Or if you don't wish to use the `DbConfig` configuration class, a dataSource can be constructed directly and injected into the
`Db` instance.

```kotlin
val dataSource = JdbcDataSource()
dataSource.setURL("jdbc:oracle:thin@localhost:5221:dbName")
dataSource.user = "sa"
dataSource.password = ""

val db = Db(dataSource)
```

# Methods

## executeQuery

```
executeQuery(
    sql: String, 
    args: Map<String, Any?> = mapOf(), 
    rowMapper: RowMapper<T>
): T?
```

executeQuery is used to for queries intended to return a single result. Example:

```kotlin
val user: User? = db.executeQuery(
    sql = "SELECT * FROM USERS WHERE id = :id",
    args = mapOf("id" to 86753)
) { resultSet ->
    User(
        id = getLong("id"),
        userName = getString("username"),
    )
}
```

If you have more than one method in your repository that needs to map a resultSet into the same domain object,
it's typical to extract the mapper into a standalone function.

```kotlin
val user: User?  = db.executeQuery(sql = "SELECT * FROM USERS WHERE id = :id",
  args = mapOf("id" to 86753),
  rowMapper = ::mapToUser
)

private fun mapToUser(resultSet: ResultSet) = with(resultSet) {
    User(
        id = getLong("id"),
        userName = getString("username"),
    )
}
```

`executeQuery` returns a nullable object. If you expect the query to never be null, a common idiom is
to wrap the call with `checkNotNull`. e.g.

```kotlin
val user: User = checkNotNull(
    db.executeQuery(
        sql = "SELECT * FROM USERS WHERE id = :id",
        args = mapOf("id" to 86753),
        rowMapper = ::mapToUser
    )
) { "Unexpected state: Query didn't return a result." }
```

## findAll

```
findAll(
    sql: String, 
    args: Map<String, Any?> = mapOf(), 
    rowMapper: RowMapper<T>
): List<T>
```

findAll is used to query for a list of results. e.g.

```kotlin
val adminUsers: List<User> = db.findAll(
  sql = "SELECT id, username FROM USERS WHERE is_admin = :isAdmin",
  args = mapOf("isAdmin" to true),
  rowMapper = ::mapToUser
)
```

## executeUpdate

```
executeUpdate(
    sql: String, 
    args: Map<String, Any?> = mapOf()
): Int
```

executeUpdate is used for statements that do not require a resultSet response. For example updates
and DDL. It returns the number of rows affected by the query.

```kotlin
val count = db.executeUpdate(sql = "INSERT INTO T (id, field1, field2) VALUES (:id, :field1, :field2)",
  args = model.propertiesToMap()
)
println("$count row(s) inserted")
```

Docs on the helper function [propertiesToMap](#propertiestomap)

## executeWithGeneratedKeys

```
executeWithGeneratedKeys(
    sql: String, 
    args: Map<String, Any?> = mapOf(), 
    rowMapper: RowMapper<T>
): List<T>

```

executeWithGeneratedKeys is used for queries that generate a default value, using something like a sequence or a
random UUID. These results will need to be mapped since multiple columns can be populated by defaults in a single
insert.

```kotlin
// Table T has an auto-generated value for the ID column in this example
val model = Model(field1 = "testName1", field2 = 1001)
val results = db.executeWithGeneratedKeys(sql = "INSERT INTO T (field1, field2) VALUES (:field1, :field2)",
  args = listOf(model.propertiesToMap(), model2.propertiesToMap()),
  rowMapper = { resultSet -> resultSet.get("id") }
)

val newModel = model.copy(id = results.first())
```

## executeBatch

```
executeBatch(
    sql: String, 
    args: List<Map<String, Any?>>,
    rowMapper: RowMapper<T>
): List<T>
```

executeBatch is used to run the same SQL statement with different parameters in batch mode.
This can give you significant performance improvements.

Args is a list of maps. Each item in the list will be a query execution in a batch. The Map will provide the parameters
for that execution. In the following example there will be two queries executed in a single batch. The first will
insert model1, and the second will insert model2.

RowMapper maps the results to the specified result type.

The response is a list of Objects of type `T`. Each object represents a batch query result. Most likely there will
be one result per query execution. In the following example the results list has 2 elements. The first element  
provides the generated ID of the model1 object, and the second element provides the generated ID of the model2 object.

```kotlin
val models = listOf(
    Model(field1 = "testName1", field2 = 1001),
    Model(field1 = "testName2", field2 = 1002)
)

val insertedIds = db.executeBatch(
    sql = "INSERT INTO T (field1, field2) VALUES (:field1, :field2)",
    args = models.map { it.propertiesToMap() },
    rowMapper = { resultSet -> resultSet.get("id") }
)
```

## executeBatch Counts only

```
executeBatch(
    sql: String, 
    args: List<Map<String, Any?>>
): List<Int>
```

executeBatch is used to run the same SQL statement with different parameters in batch mode.
This can give you significant performance improvements.

Args is a list of maps. Each item in the list is a query execution in a batch. The Map provides the parameters
for that execution. In the following example there are two queries executed in a single bath. The first
inserts model1, and the second inserts model2.

The response is a list of Int. Each Int indicates the rows affected by the respective query execution. In the following
example the results list has 2 elements. The first element indicates how many rows were affected by the
model1 insert (it should be 1), and the second element indicates how many rows were affected by the model2 insert.

```kotlin
val model1 = Model(field1 = "testName1", field2 = 1001)
val model2 = Model(field1 = "testName2", field2 = 1002)
val results = db.executeBatch(sql = "INSERT INTO T (field1, field2) VALUES (:field1, :field2)",
  args = listOf(model1.propertiesToMap(), model2.propertiesToMap())
)

results.forEach { println("$it row(s) inserted")}
```

## useNamedParamPreparedStatement

```
useNamedParamPreparedStatement(
    sql: String, 
    block: (NamedParamPreparedStatement) -> T
): T
```

usePreparedStatement is used to run blocks of code against a prepared statement that is created for you, and clean up
is done automatically. This should only be used if none of the above methods meet your needs and you need access to the
raw NamedParamPreparedStatement.

This method will NOT return generated keys.

Unlike the other methods listed here, the PositionalParam option is simply usePreparedStatement (since the vanilla
PreparedStatement is what will be provided to you)

## useNamedParamPreparedStatementWithAutoGenKeys

```
useNamedParamPreparedStatementWithAutoGenKeys(
    sql: String, 
    block: (NamedParamPreparedStatement) -> T
): T
```

useNamedParamPreparedStatementWithAutoGenKeys is used to run blocks of code against a prepared statement that is created
for you, and clean up is done automatically. This should only be used if none of the above methods meet your needs, and
you need access to the raw NamedParamPreparedStatement.

This method will set the PreparedStatement to return generated keys.

Unlike the other methods listed here, the PositionalParam option is simply usePreparedStatement (since the vanilla
PreparedStatement is what will be provided to you)

## useConnection

```
useConnection(block:(Connection) -> T): T
```

useConnection is the lowest level method, and should only be used if you require direct access to the
JDBC Connection. The connection will be created and cleaned up for you.

# Query Parameters

lite-for-jdbc supports named parameters in your query. The named parameter syntax is the recommended pattern
for ease of maintenance and readability. All the examples use named parameters.
Positional parameters are also supported for backword compatability. The positional parameter
version of each method is available by adding `PositionalParams` to the method name.
For example, to query using named parameters, call `executeQuery`, and to query using positional
parameters, call `executeQueryPositionalParams`.

## Named Parameters

In your query, use a colon to indicate a named parameter.

```sql
SELECT * FROM T WHERE field = :value1 OR field2 = :value2
```

In the above example, invoking it would require a map defind like this

```kotlin
mapOf("value1" to "string value", "value2" to 123)
```

Named Parameters can NOT be mixed with positional parameters - doing so will result in an exception.

```sql
-- ILLEGAL
SELECT * FROM T WHERE field = :value1 OR field2 = ?
```

Colons inside of quotes or double quotes will be ignored.

```sql
SELECT * FROM T WHERE field = 'This will ignore the : in the string'
```

If you need a colon in the SQL, escape it with a double colon.

```sql
SELECT * FROM T WHERE field = ::systemVariableInOracle
```

The above query will have no named paraemters, and the sql will translate INTO the following

```sql
SELECT * FROM T WHERE field = :systemVariableInOracle
```

## Positional Params

Favor named params if you can - they make the code easier to understand, and aren't at risk
of parameter order bugs that can happen with positional params. This library also supports
positional params if you want or need them for some reason. Positional Parameters pass the SQL
directly to the JDBC Connection to prepare a statement. See the Java JDBC reference documentation
for more details on the syntax.

The Positional Parameter methods accept varargs, and the order of the arguments will dictate the position in the query

There is one exception to the use of varargs, and that's the executeBatchPositionalParams. That accepts a List of Lists.

# Row Mapping

On calls on Db that will return a ResultSet, a row mapping function must be provided to the map each row to an object.
If the function returns a list of objects, the rowMapper will be called once per row.

## rowMapper

The rowMapper takes a ResultSet and maps the current row to the returned object. It will handle looping on the
ResultSet for you where necessary. This mapper can interact directly with the ResultSet as seen in the following example:

```kotlin
import java.time.Instant
 
data class Model (
  val field1: String,
  val field2: Int,
  val field3: Instant
)

val results = db.findAll(sql = "SELECT * FROM model", 
  rowMapper = { resultSet ->
    Model(
      field1 = resultSet.getString("field_1"),
      field2 = resultSet.getInt("field_2"),
      field3 = resultSet.getInstant("field_3")
    )
  }
)
```

## ResultSet/PreparedStatement extensions

To facilitate mapping, ResultSet.get and PreparedStatement.set extensions have been added.

| Extension methods                   | Behavior of ResultSet.get              | Behavior of PreparedStatement.set                               |
|-------------------------------------|----------------------------------------|-----------------------------------------------------------------|
| getInstant/setInstant               | getLocalDateTime(c).toInstant(UTC)     | setObject(c, LocalDateTime.ofInstant(instant, ZoneOffset.UTC))  |
| getLocalDateTime/setLocalDateTime   | getObject(c, LocalDateTime)            | setObject(c, LocalDateTime)                                     |
| getLocalDate/setLocalDate           | getObject(c, LocalDate)                | setObject(c, LocalDate)                                         |
| getLocalTime/setLocalTime           | getObject(c, LocalTime)                | setObject(c, LocalTime)                                         |
| getOffsetDateTime/setOffsetDateTime | getObject(c, OffsetDateTime)           | setObject(c, OffsetDateTime)                                    |
| getOffsetTime/setOffsetTime         | getObject(c, OffsetTime)               | setObject(c, OffsetTime)                                        |                                      
| getZonedDateTime/setZonedDateTime   | getOffsetDateTime(c).toZonedDateTime() | setObject(c, zonedDateTime.toOffsetDateTime())                  |
| getUUID/setUUID                     | getObject(c, UUID)                     | setObject(c, UUID)                                              |
| setDbValue                          |                                        | setObject(c, DbValue.value, DbValue.type, [DbValue.percission]) |


## Java type to Postgresql column type mapping requirements

The following table shows the Java type to Postgresql column type pairing that should be used with lite-for-jdbc.

| Java Type      | Postgresql Type | Description                                         | Example fields             |
|----------------|-----------------|-----------------------------------------------------|----------------------------|
| Instant        | Timestamp       | A moment in time without a time zone                | created_timestamp          |
| LocalDateTime  | Timestamp       | A date/time without considering time zones          | movie_opening              |
| LocalDate      | Date            | A day with no time or time zone information         | product_launch_date        |
| LocalTime      | Time            | A time of day without date or time zone information | mcdonalds_lunch_start_time |
| OffsetDateTime | TimestampTZ     | A date/time with a set offset (-/+hh:mm)            | flight_depart_timestamp    |
| OffsetTime     | TimeTZ          | A time with a set offset (-/+hh:mm)                 | store_open_time            |
| ZonedDateTime  | TimestampTZ     | A date/time with a time zone code                   | meeting_start_timestamp    |

### Storing timestamps with timezone

Postgres stores ALL Timestamps as UTC. TimestampTZ adds support for time zone offsets in the value when it is being
inserted, but it is stored in UTC with NO KNOWLEDGE of the Time Zone offset provided. Because of this, when Offset
or Zoned DateTimes are read back from Postgres, you will always get the data with a Zero offset. If you need to save
a timestamp AND a timezone, you will need to have two fields: one for the timestamp and one for the timezone.

We find the simplest solution is to have a pair of fields with a Instant/Timestamp for the date/time, and a String/Text
field for a Zone ID along with convenience methods. See the below example code

```postgresql
-- DDL
CREATE TABLE flight
(
  id                      INTEGER   GENERATED ALWAYS AS IDENTITY,
  flight_name             TEXT      NOT NULL,
  flight_depart_timestamp TIMESTAMP NOT NULL,
  flight_depart_timezone  TEXT      NOT NULL
)
```

```kotlin
// Domain Class
 import java.time.ZoneIdconst 
 import java.time.Instant
 import java.time.ZonedDateTime
 
 val UNSET: Long = -1

data class Delivery (
  val id: Long = UNSET,
  val deliveryAddress: String,
  val deliveryTimestamp: Instant, 
  val deliveryTimezone: ZoneId
) {
    fun getFlightDepartDateTime(): ZonedDateTime {
        return ZonedDateTime.ofInstant(deliveryTimestamp, deliveryTimezone)
    }
    fun setFlightDepartDateTime(value: ZonedDateTime) {
      this.deliveryTimestamp = value.toInstant()
      this.deliveryTimezone = value.zone
    }
    
}
```

```kotlin
// Mapping

private fun ResultSet.toDelivery() : Delivery = Delivery(
  id = getLong("id"),
  deliveryAddress = getString("delivery_address"),
  deliveryTimestamp = getInstant("delivery_timestamp"),
  deliveryTimezone = java.time.ZoneId.of(getString("delivery_timezone"))
)

```

## propertiesToMap

A convenience method has been added to turn an object into a map of its properties. This can be useful to turn a
domain into a map to then be used as named parameters.

```kotlin
val propMap = model.propertiesToMap()
```

Three optional parameters exist to fine tune the process. exclude will skip certain fields when creating the map.

```kotlin
val propMap = model.propertiesToMap(exclude = listOf("fieldOne"))
propMap.containsKey("fieldOne") shoudlBe false
```

`nameTransformer` allows you to transform the keys in the map if you want to use named parameters that don't strictly
match the field names on the domain class. For example, if you want the named parameters in your query to use snake case,
you could use the method as shown below.

```kotlin
val propMap = model.propertiesToMap(nameTransformer = ::camelToSnakeCase)
propMap.containsKey("field_one") shoudlBe true
propMap.containsKey("fieldOne") shoudlBe false
```

`override` will override the values for specific key provided. If this is paired with the
`nameTransformer`, you should match the transformed name (not the field name).

```kotlin
val propMap = model.propertiesToMap(override = mapOf("fieldOne" to "Override"))
propMap["fieldOne"] shoudlBe "Override"
```

# Transactions & Autocommit

Using withAutoCommit and withTransaction on Db will give you the opportunity to use a single ConnectionSession for
multiple calls. Calling the find & execute methods on Db will create a new AutoCommit ConnectionSession for each call.

## withAutoCommit

AutoCommit will commit any DML (INSERT, UPDATE, DELETE, ...) statements immediately upon execution. If a transaction
isn't required, this is more performant and simpler. See the withTransaction section to determine if your use case
may require transactions

Since Db has convenience methods for executing a single command in its own ConnectionSession, withAutoCommit is not
required. But for efficiency reasons, it should be used if multiple sql commands will be executed so that a single
ConnectionSession is used.

At the end of the withAutoCommit block, the AutoCommit ConnectionSession will be closed.

## withTransaction

By using a Transaction ConnectionSession, changes will NOT be immediately committed to the database. Which allows for
multiple features listed below. If any of these features are required, use withTransaction. Also use withTransaction if you need to specify isolation level.

* Commit - Commits any existing changes to the database and clears any Savepoints and Locks
* Rollback - Reverts the changes since the most recent commit, or the beginning of the ConnectionSession if no commits
  have been done. A partial rollback can also be done to a specific Savepoint
* Savepoint - Saves the current point of the transaction which can be used to perform a partial Rollback
* Locks - While not available as an explicit method on the transaction, executing a query to lock database resources,
  which will prevent the use by other connections. See the documentation of your database for specifics on what locks
  are available and what behavior they provide.

At the end of the withTransaction block, if the block is exited normally the Transaction will be committed. If an
exception is thrown, the Transaction will be rolled back. After the final commit/rollback, the Transaction ConnectionSession
will be closed.

### withTransactionAndIsolation - How to Specify Isolation levels

By default, all transactions run with `TRANSACTION_READ_COMMITTED`isolation level. The following shows how to specify a higher one:

```kotlin
  db.withTransactionAndIsolation(isolationLevel = Db.IsolationLevel.TRANSACTION_REPEATABLE_READ) 

  db.withTransactionAndIsolation(isolationLevel = Db.IsolationLevel.TRANSACTION_SERIALIZABLE) 
```

When the transaction is over, isolation level is restored to the default, TRANSACTION_READ_COMMITTED.

## DataSource configuration & AutoCommit

A dataSource has a default setting for the autocommit flag which can be configured. But the individual connections can
be modified to change their autocommit flag. This will be done if the autocommit flag is set to be incompatible with the
ConnectionSession being used. withTransaction requires a connection with autocommit set to false, and withAutoCommit
requires a connection with autocommit set to true.

Because lite-for-jdbc will modify the setting to function with the ConnectionSession, you will not see functionality issues
regardless of your setting. But you should set the DataSource to default to the most common use case in your application,
as there is a potential performance impact to changing that setting.

## DataSource settings

If the dataSource is set to a different autocommit mode than is being used by a call in lite-for-jdbc, the value will be
changed for the duration of that ConnectionSession

## Testing with mockkTransaction

The mockkTransaction method works with mockk. A mock DB (using mockk) is provided to the function. The mockDb will be
setup to invoke any lambda provided while calling withTransaction, providing it with a mock transaction. The mock
transaction will be returned from the method, so additional setup can be performed using it.

Several of the mockk parameters are passed through to the mock Transaction: relaxed, relaxedUnitFun, and name.

Here is an example use of mockTransaction

```kotlin
val mockTransaction: Transaction = mockkTransaction(mockDb, relaxed = true)

mockDb.withTransaction { t: Transaction ->
  // This code is actually executed because mockkTransaction has done the necessary setup to make that happen
  t shouldBeSameInstanceAs mockTransaction
  t.rollback()
  t.commit()
}

// Verify the two calls were made. Because the mock transaction is relaxed, these didn't need to be setup
// before the call
verify {
  mockTransaction.rollback()
  mockTransaction.commit()
}

// Those are the only two calls that were made
confirmVerified(mockTransaction)

```

# IntelliJ SQL language integration

All the query-related methods provided by this library use `sql` as the method parameter name for SQL.
Using this pattern, you can add SQL language support to IntelliJ, which will then give you features
like auto-completion, validation, and syntax highlighting. To enable this, add or edit your
project's `.idea/IntelliLang.xml` file with this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="LanguageInjectionConfiguration">
    <injection language="SQL" injector-id="kotlin">
      <display-name>lite-for-jdbc SQL parameter name</display-name>
      <single-file value="false" />
      <place><![CDATA[kotlinParameter().withName("sql")]]></place>
    </injection>
  </component>
</project>
```

An example showing syntax highlighting and available operations:

![](/docs/resources/sql-language-integration.png)

The authors will typically add this file and `sqlDialects.xml` (which associates the project's SQL language
with the database dialect being used) to source control, ignoring other files in the `.idea` directory.
Example `.gitignore` file in the `.idea/` directory:

```gitignore
# Ignore everything in this directory
*

# Except this file
!.gitignore

# store IntelliLang.xml customizations
!IntelliLang.xml

# keep our code style in source control
!codeStyles/
!codeStyles/*

# store the SQL dialect for this project
!sqldialects.xml
```

In combination with the SQL dialect configured, it provides powerful SQL language support.
[JetBrains documentation](https://www.jetbrains.com/help/idea/using-language-injections.html)

# Development

## Building

Lite for JDBC uses standard gradle tasks

```shell
./gradlew build
```

## Issues

Report issues in the issues section of our repository

## Contributing

Fork the repository and submit a pull request containing the changes, targeting the `main` branch.
and detail the issue it's meant to address.

### Code review standards

Code reviews will look for consistency with existing code standards and naming conventions.

### Testing standards

All changes should include sufficient testing to prove it is working as intended.

# Breaking version changes

### `1.9.2` -> `2.0.0`

**Breaking Change**: Changed the DataSourceFactory to a singleton object and the Type on DbConfig to a String.
**Reason**: Originally only the statically configured DataSource types were supported due to the use of an
enum and a statically coded factory. This change was made so that users can modify the factory list to meet their
individual needs. 
