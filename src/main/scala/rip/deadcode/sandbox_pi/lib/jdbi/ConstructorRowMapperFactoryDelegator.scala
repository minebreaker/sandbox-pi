package rip.deadcode.sandbox_pi.lib.jdbi

import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper
import org.jdbi.v3.core.mapper.{RowMapper, RowMapperFactory}

import java.lang.reflect.Type
import java.util.Optional

// This factory automatically creates ConstructorMapper for internal classes
// so that we don't need to register each type.
class ConstructorRowMapperFactoryDelegator extends RowMapperFactory {
  override def build(t: Type, config: ConfigRegistry): Optional[RowMapper[?]] = {
    t match {
      case _ if !t.getTypeName.startsWith("rip.deadcode.ssch") =>
        Optional.empty()
      case clz: Class[_] =>
        Optional.of(
          ConstructorMapper.of(clz): RowMapper[?]
        )
      case _ =>
        Optional.empty()
    }
  }
}
