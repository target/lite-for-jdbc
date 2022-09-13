# lite-for-jdbc
Lightweight library to help simplify JDBC database access. Main features:
- Lets you use real SQL statements in your code along with named parameters
- Automates resource cleanup
- Provides a handful of functions for common database interaction patterns like executing
  individual SQL statements, selecting individual and list results, and batch statements.

# Gradle Setup
```kotlin
repositories {
    maven("https://binrepo.target.com/artifactory/toolshed")
}

dependencies {
  api("toolshed:lite-for-jdbc:1.6.0")
}
```

# Db Setup

The core of lite-for-jdbc is the Db class. A Db object is intended to be used as a singleton and
injected as a dependency in your Repository classes. The
runtime setup is very open. Provide a DataSource to construct a new
Db object. If the project has multiple DataSources, create multiple Db objects.

The most common use case requires a Connection Pool. The recommendation in this case is to Hikari, and a
DataSourceFactory has been provided for such a use case. Reasonable defaults are set in the Config, but they should be
reviewed for your project. It can be used as follows:

```kotlin
val dataSource = DatasourceFactory(DbConfig(
    username = "user",
    password = "password",
    databaseName = "dbName"
)).dataSource()

val db = Db(dataSource)
```

For an in-memory DB option, H2 is supports

```kotlin
val dataSource = DatasourceFactory(DbConfig(
  type = "H2_INMEM",
  username = "user",
  password = "password",
  databaseName = "dbName"
)).dataSource()

val db = Db(dataSource)
```

See DbConfig for a full list of configuration options available.

If another implementation of DataSource is required, you can construct your own as shown below:

```kotlin
val dataSource = JdbcDataSource()
dataSource.setURL("jdbc:oracle:thin@localhost:5221:dbName")
dataSource.user = "sa"
dataSource.password = ""

val db = Db(dataSource)
```

# Methods
## executeQuery

Signature: executeQuery(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: (rs: ResultSet) -> T): T?

executeQuery is used to run select queries and map the response. This query will only process the FIRST result. If
multiple results are returned, the rest will be ignored.

```kotlin
val user: User  = db.executeQuery(sql = "SELECT id, username FROM USERS WHERE id = :id",
  args = mapOf("id" to 86753),
  rowMapper = { resultSet -> User(resultSet.toMap()) },
)
```

Or if you want to be more concise (this applies to all the following examples):
```kotlin
val user: User  = db.executeQuery(sql = "SELECT id, username FROM USERS WHERE id = :id",
    args = mapOf("id" to 86753)) { resultSet ->
    User(resultSet.toMap())
}
```

## findAll

Signature: findAll(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: (rs: ResultSet) -> T): List<T>

findAll is used to run select queries and map the responses. This query will process all of the results

```kotlin
val adminUsers: List<User> = db.findAll(
  sql = "SELECT id, username FROM USERS WHERE is_admin = :isAdmin",
  args = mapOf("isAdmin" to true),
  rowMapper = { resultSet -> resultSetToUser(resultSet) },
)
```

## executeUpdate

Signature: executeUpdate(sql: String, args: Map<String, Any?> = mapOf()): Int

executeUpdate is used to run queries with no response data, such as an UPDATE or DDL. No rowMapper is provided because
there will be no results to map.

```kotlin
val model = Model(id = 100, field1 = "testName", field2 = 1000)
val count = db.executeUpdate(sql = "INSERT INTO T (id, field1, field2) VALUES (:id, :field1, :field2)",
  args = model.toMap()
)
println("$count row(s) inserted")
```

## executeWithGeneratedKeys

Signature: executeWithGeneratedKeys(sql: String, args: Map<String, Any?> = mapOf(), rowMapper: (rs: ResultSet) -> T): List<T>

executeWithGeneratedKeys is used to run queries that will generate default value, using something like a sequence or a
random UUID. These results will need to be mapped since multiple columns can be populated by defaults in a single
insert.

```kotlin
// Table T has an auto-generated value for the ID column in this example
val model = Model(field1 = "testName1", field2 = 1001)
val results = db.executeWithGeneratedKeys(sql = "INSERT INTO T (field1, field2) VALUES (:field1, :field2)",
  args = listOf(model.toMap(), model2.toMap()),
  rowMapper = { resultSet -> resultSet.get("id") }
)

val newModel = model.copy(id = results.first())
```

## executeBatch

Signature: executeBatch(sql: String, args: List<Map<String, Any?>>): List<Int>

executeBatch is used to run the same SQL statement with different parameters in batch mode. This can be used to improve
performance.

```kotlin
val model1 = Model(field1 = "testName1", field2 = 1001)
val model2 = Model(field1 = "testName2", field2 = 1002)
val results = db.executeBatch(sql = "INSERT INTO T (field1, field2) VALUES (:field1, :field2)",
  args = listOf(model1.toMap(), model2.toMap())
)

results.forEach { println("$it row(s) inserted")}
```

## useNamedParamPreparedStatement

Signature: useNamedParamPreparedStatement(sql: String, block: (NamedParamPreparedStatement) -> T): T

usePreparedStatement is used to run blocks of code against a prepared statement that is created for you, and clean up
is done automatically. This should only be used if none of the above methods meet your needs, and you need access to the
raw NamedParamPreparedStatement.

This method will NOT return generated keys.

Unlike the other methods listed here, the PositionalParam option is simply usePreparedStatement (since the vanilla
PreparedStatement is what will be provided to you)

## useNamedParamPreparedStatementWithAutoGenKeys

Signature: useNamedParamPreparedStatementWithAutoGenKeys(sql: String, block: (NamedParamPreparedStatement) -> T): T

useNamedParamPreparedStatementWithAutoGenKeys is used to run blocks of code against a prepared statement that is created
for you, and clean up is done automatically. This should only be used if none of the above methods meet your needs, and
you need access to the raw NamedParamPreparedStatement.

This method will set the PreparedStatement to return generated keys.

Unlike the other methods listed here, the PositionalParam option is simply usePreparedStatement (since the vanilla
PreparedStatement is what will be provided to you)

## useConnection

Signature: useConnection(block: (Connection) -> T): T

useConnection is the lowest level method, and as such should only be used if you require access to the JDBC Connection
directly. The connection will be created for you, and after the execution it will be cleaned up.

# Query Parameters

lite-for-jdbc supports named parameters in your query. The Named Parameter syntax is recommended pattern
for ease of maintenance and readability. Positional Parameters are also supported for backword
compatability.

Every method described below is using the Named Parameter pattern. There is also a Positional
Parameter version of each method by adding PositionalParams at the end of the method name.

## Named Parameters

In your query, use a colon to indicate a named parameter.
```sql
SELECT * FROM T WHERE field = :value1 OR field2 = :value2
```

In the above example, invoking it would require a map defind like this
```kotlin
mapOf("value1" to "string value", "value2" to 123)
```

Named Parameters can NOT be mixed with positional parameters. Doing so will result in an exception.
```sql
-- ILLEGAL
SELECT * FROM T WHERE field = :value1 OR field2 = ?
```

Colons inside of quotes or double quotes will be ignored.
```sql
SELECT * FROM T WHERE field = 'This will ignore the : in the string'
```

If you have some syntax that requires a colon in the SQL, you can escape a colon by using a double colon
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

# ConnectionSession (Transactions & Autocommit)

A ConnectionSession is a single use of a connection from the Datasource before it's closed (returning it to the pool
if a Connection Pool is being used).

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
multiple features listed below. If any of these features are required, use withTransaction.

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

## DataSource configuration & AutoCommit

A datasource has a default setting for the autocommit flag which can be configured. But the individual connections can
be modified to change their autocommit flag. This will be done if the autocommit flag is set to be incompatible with the
ConnectionSession being used. withTransaction requires a connection with autocommit set to false, and withAutoCommit
requires a connection with autocommit set to true.

Because lite-for-jdbc will modify the setting to function with the ConnectionSession, you will not see functionality issues
regardless of your setting. But you should set the DataSource to default to the most common use case in your application,
as there is a potential performance impact to changing that setting.


## DataSource settings

If the datasource is set to a different autocommit mode than is being used by a call in lite-for-jdbc, the value will be
changed for the duration of that ConnectionSession

## 

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


## ResultSet extensions

To facilitate mapping, ResultSet.get[Date/Time] extensions have been added to ResultSet.


| Extension methods                   | Behavior of get                        | Behavior of set                                                |
|-------------------------------------|----------------------------------------|----------------------------------------------------------------|
| getInstant/setInstant               | getLocalDateTime(c).toInstant(UTC)     | setObject(c, LocalDateTime.ofInstant(instant, ZoneOffset.UTC)) |
| getLocalDateTime/setLocalDateTime   | getObject(c, LocalDateTime)            | setObject(c, LocalDateTime)                                    |
| getLocalDate/setLocalDate           | getObject(c, LocalDate)                | setObject(c, LocalDate)                                        |
| getLocalTime/setLocalTime           | getObject(c, LocalTime)                | setObject(c, LocalTime)                                        |
| getOffsetDateTime/setOffsetDateTime | getObject(c, OffsetDateTime)           | setObject(c, OffsetDateTime)                                   |
| getOffsetTime/setOffsetTime         | getObject(c, OffsetTime)               | setObject(c, OffsetTime)                                       |                                      
| getZonedDateTime/setZonedDateTime   | getOffsetDateTime(c).toZonedDateTime() | setObject(c, zonedDateTime.toOffsetDateTime())                 |

## Date Time in Postgresql


| Type            | Postgresql Type | Description                                         | Example fields          |
|-----------------|-----------------|-----------------------------------------------------|-------------------------|
| Instant         | Timestamp       | A moment in time without a time zone                | created_timestamp       |
| LocalDateTime   | Timestamp       | A date/time without considering time zones          | ?                       |
| LocalDate       | Date            | A day with no time or time zone information         | product_launch_date     |
| LocalTime       | Time            | A time of day without date or time zone information | ?                       |
| OffsetDateTime  | TimestampTZ     | A date/time with a set offset (-/+hh:mm)            | flight_depart_timestamp |
| OffsetTime      | TimeTZ          | A time with a set offset (-/+hh:mm)                 | store_open_time         |
| ZonedDateTime   | TimestampTZ     | A date/time with a time zone code                   | meeting_start_timestamp |

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

private fun Delivery.toMap(): Map<String, Any> {
 return mapOf( 
   "id" to id, 
   "deliver_address" to deliveryAddress, 
   "delivery_timestamp" to deliveryTimestamp, 
   "delivery_timezone" to deliveryTimezone
 )
}
```

## propertiesToMap

A convenience method has been added to turn an object into a map of it's properties. This can be useful to turn a
domain into a map to then be used as named parameters.

```kotlin
val propMap = model.propertiesToMap()
```

Two optional parameters exist to fine tune the process. exclude will skip certain fields when creating the map.

```kotlin
val propMap = model.propertiesToMap(exclude = listOf("fieldOne"))
propMap.containsKey("fieldOne") shoudlBe false
```

And nameTransformer allows you to transform the keys in the map if you want to use named parameters that don't strictly
match the field names on the domain class. For example, if you want the named parameters in your query to use snake case,
you could use the method as shown below

```kotlin
val propMap = model.propertiesToMap(nameTransformer = ::camelToSnakeCase)
propMap.containsKey("field_one") shoudlBe true
propMap.containsKey("fieldOne") shoudlBe false
```

# lite-for-jdbc-test-common

Another artifact is available to assist in testing code that uses lite-for-jdbc.

```kotlin
repositories {
    maven("https://binrepo.target.com/artifactory/toolshed")
}

dependencies {
    testImplementation("toolshed:lite-for-jdbc-test-common:1.2.1") 
}
```

## mockkTransaction

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

# Lite for JDBC Development

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
