package com.dusizhong.examples.multidb.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

/**
 * 自定义命名转换器
 * 将entity中驼峰命名转换为数据库下划线命名（自定义多数据源后，默认转换无效了）
 * @author dusizhong
 * @since 2023-11-29
 */
public class CustomNamingStrategy extends SpringPhysicalNamingStrategy {
    @Override
    protected Identifier getIdentifier(String name, boolean quoted, JdbcEnvironment jdbcEnvironment) {
        return new Identifier(name.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase(), quoted);
    }
}
