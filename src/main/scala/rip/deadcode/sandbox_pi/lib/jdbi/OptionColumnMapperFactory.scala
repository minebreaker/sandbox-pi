package rip.deadcode.sandbox_pi.lib.jdbi

import com.google.common.reflect.TypeToken
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.generic.GenericTypes
import org.jdbi.v3.core.mapper.{ColumnMapper, ColumnMapperFactory, ColumnMappers, NoSuchMapperException}
import org.jdbi.v3.core.statement.StatementContext
import rip.deadcode.sandbox_pi.lib.jdbi.OptionColumnMapperFactory.{createOptionMapper, createOptionMapperForObject}

import java.lang.reflect.{ParameterizedType, Type}
import java.sql.ResultSet
import java.util.Optional
import scala.jdk.OptionConverters.*

// Basically same as https://jdbi.org/#_columnmapperfactory
class OptionColumnMapperFactory extends ColumnMapperFactory:

  override def build(t: Type, config: ConfigRegistry): Optional[ColumnMapper[?]] = {
    if (classOf[Option[?]] != GenericTypes.getErasedType(t)) {
      return Optional.empty()
    }

    val typeParameterType = GenericTypes.resolveType(classOf[Option[?]].getTypeParameters.apply(0), t)

    // AnyVals are passed as java.lang.Object, so there's no way to determine the parameter types.
    // We assume it as Int for a workaround (since we don't use any other types), but should definitely be fixed.
    // FIXME: A better implementation to handle AnyVal
    val mapper = if (classOf[Object] == typeParameterType) {
      createOptionMapperForObject(config)
    } else {
      createOptionMapper(t, typeParameterType, config)
    }

    Optional.of(mapper)
  }

object OptionColumnMapperFactory {

  private def createOptionMapper(t: Type, typeParameterType: Type, config: ConfigRegistry): ColumnMapper[Option[?]] = {
    (r: ResultSet, columnNumber: Int, ctx: StatementContext) =>
      val innerMapper = config
        .get(classOf[ColumnMappers])
        .findFor(typeParameterType)
        .toScala
        .getOrElse {
          val columnName = r.getMetaData.getColumnName(columnNumber)
          throw NoSuchMapperException(
            s"No column mapper registered for parameter $typeParameterType of type $t for column $columnName"
          )
        }
      Option(innerMapper.map(r, columnNumber, ctx))
  }

  private def createOptionMapperForObject(config: ConfigRegistry): ColumnMapper[Option[?]] = {
    (r: ResultSet, columnNumber: Int, ctx: StatementContext) =>
      val innerMapper = config
        .get(classOf[ColumnMappers])
        .findFor(classOf[java.lang.Integer])
        .toScala
        .getOrElse {
          val columnName = r.getMetaData.getColumnName(columnNumber)
          throw NoSuchMapperException("No registered mapper for java.lang.Integer")
        }
      Option(innerMapper.map(r, columnNumber, ctx))
  }
}
