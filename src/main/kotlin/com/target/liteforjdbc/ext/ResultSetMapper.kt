package com.target.liteforjdbc.ext

import java.sql.ResultSet
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor
import mu.KotlinLogging

typealias Transform = (Any?) -> Any?

private val log = KotlinLogging.logger {}

/**
 * Applying this function to a [ResultSet] will attempt to instantiate [R] by matching column names within [rs],
 * converting column names (snake case assumed) to their equivalent kotlin constructor property names
 * (camel case assumed).
 *
 * __Example usage__
 *
 * Consider simple table structure such as:
 *
 * ```
 * create table if not exists users
 * (
 *     id         serial primary key,
 *     created_at timestamp with time zone default CURRENT_TIMESTAMP,
 *     username   varchar(30) not null,
 *     info       varchar(400),
 *     email      varchar(40),
 *     phone      varchar(40),
 *     status     varchar(15)
 * )
 * ```
 * Our simple User class:
 * ```
 * data class User(
 *   val id: Int,
 *   val createdAt: Instant,
 *   val username: String,
 *   val info: String?,
 *   val phone: String?,
 *   val email: String?,
 *   val status: String?
 * )
 * *  ```
 *
 * In this example, the simplest use case could be reduced to simply supplying a reference to this [mapRow]
 * function to the `rowMapper` property (within the `lite-for-jdbc` library).
 *
 * ```
 * fun findAllUsers(): List<User> = db.findAll(sql = "select * from users", rowMapper = ::mapRow)
 * ```
 *
 * Creating a val to use as a mapper (all options being utilized for informational purposes, not required):
 * ```
 * private val rowMapper: RowMapper<User> = { mapRow(
 *            rs = it,
 *            valueFor = mapOf("phone" to "xxx-xxx-xxxx"),
 *            transform = mapOf("username" to { x -> (x as? String)?.lowercase() }),
 *            skip = setOf("status"),
 *            remap = mapOf("someProperty" to "some_column_name")
 *        )
 *  }
 *
 * fun findAllUsers(): List<User> = db.findAll(sql = "select * from users", rowMapper = mapper)
 *
 *```
 *
 * Declaring the function inline:
 * ```
 * fun findAllUsers(): List<User> = db.findAll(
 *      sql = "select * from users",
 *      rowMapper = { mapRow(
 *          rs = it,
 *          valueFor = mapOf("phone" to "xxx-xxx-xxxx"),
 *          transform = mapOf("username" to { x -> (x as? String)?.lowercase() }),
 *          skip = setOf("status"),
 *          remap = mapOf("someProperty" to "some_column_name")
 *       )
 *  )
 * ```
 *
 * The simplest case in this form is (again, could be reduced to ::mapRow):
 *
 * ```
 * fun findAllUsers(): List<User> = db.findAll(
 *      sql = "select * from users",
 *      rowMapper = { mapRow(it) }
 *  )
 * ```
 *
 * Simply applying the [mapRow] function to a result set (as this function is not tied to
 * lite-for-jdbc):
 *
 * ```
 * val user: User = mapRow(resultSet)
 * ```
 *
 * Or manually mapping over a result set:
 * ```
 * fun users(rs: ResultSet): List<User> = mutableListOf<Example>().apply {
 *       while(rs.next()) {
 *         add(mapRow(rs))
 *       }
 *     }
 * ```
 *
 * @param rs a result set
 *
 * @param remap a mapping of a kotlin class' constructor argument and pairs that with a column which doesn't by
 *         default match the property by a simple "snake to camel case" conversion.
 *
 * @param valueFor a mapping of a class' constructor arg to a default value. This is useful for when a result set
 *         does not contain a value for the argument and/or if the constructor argument does not have a default value,
 *         or if the default value is not appropriate for the use case
 *
 * @param skip a set of property names which informs the mapper to simply skip the given property and not attempt to bind
 *        that value from the result set This is close to the [valueFor] argument, but requires no value (there
 *        must be a default value set in the constructor)
 *
 * @param transform a mapping of an incoming property name (using the kotlin property, not column name), which will apply
 *        the transformation function and bind this value to the constructor argument
 *
 * @return [R] the expected output type
 */
inline fun <reified R : Any> mapRow(
  rs: ResultSet,
  remap: Map<String, String> = emptyMap(),
  valueFor: Map<String, Any> = emptyMap(),
  skip: Set<String> = emptySet(),
  transform: Map<String, (Any?) -> Any?> = emptyMap()
): R {
  val constructor = R::class.primaryConstructor
    ?: error("No primary constructor found on <${R::class}>, is this a class?")

  return rs.mapRow(
    constructor = constructor,
    valueFor = valueFor,
    remap = remap,
    skip = skip,
    transform = transform
  )
}

val identity = { value: Any? -> value }

// This was extracted from `mapRow` as we only need the inline function [mapRow] for the reified ability,
// and this will reduce amount of inlined code
fun <R : Any?> ResultSet.mapRow(
  constructor: KFunction<R>,
  valueFor: Map<String, Any>,
  remap: Map<String, String>,
  skip: Set<String>,
  transform: Map<String, (Any?) -> Any?>
): R {
  val columnMapping = metaData.let { metadata ->
    (1..metadata.columnCount).associate {
      metadata.getColumnName(it) to metadata.getColumnType(it)
    }
  }

  return constructor.parameters
    .associateWith { parameter ->
      val name = parameter.name ?: error("Parameter `$parameter` does not have a name.")
      when {
        skip.contains(name) -> Unit // unit is not a valid value and will be filtered out
        valueFor.contains(name) -> valueFor[name]
        else -> {
          remap.getOrDefault(name, name).let {
            val columnName = it.camelToSnakeCase()
            val sqlType = columnMapping[columnName]
            if (sqlType == null) {
              log.debug {
                """No column matches entry for: `$columnName`.
                | The column's value will simply be ignored and removed from the parameter list when invoking the 
                | the constructor. If the constructor does not have a default argument, then instantiation will fail.
                |Consider:
                |- Using an alias for the column to match the property (snake case form)
                |- Provide a default value within the `valueFor` using the class' constructor parameter name `$it`
                |- Use `remap` to provide a different property to match the column name
                |- Use `skip` to ignore the binding attempt altogether for this property
                |- Do not query for the value to simply ignore it
                |""".trimMargin()
              }
              // Unit is returned which will be filtered out.
            } else {
              transform.getOrDefault(name, identity)(readValue(sqlType, columnName))
            }
          }
        }
      }
    }
    .filter { (_, v) -> v != Unit } // Unit is not a valid value meaning one wasn't supplied.
    .also {
      log.debug {
        "Parameter bindings to be applied to $constructor are: $it"
      }
    }
    .runCatching(constructor::callBy)
    .onFailure { ex ->
      log.error(ex) {
        """Unable to invoke constructor for $constructor. Message: ${ex.message}. 
          |Hint: Check the expected types in the constructor match the inputs given to invoke
          |Constructor parameters are: ${constructor.parameters.map { it.name }}""".trimMargin()
      }
    }
    .getOrThrow()
}
