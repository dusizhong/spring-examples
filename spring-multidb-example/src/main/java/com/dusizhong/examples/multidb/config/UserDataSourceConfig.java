package com.dusizhong.examples.multidb.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.dusizhong.examples.multidb.repository.user"}, entityManagerFactoryRef = "userEntityManagerFactory", transactionManagerRef = "userTransactionManager")
public class UserDataSourceConfig {

    @Primary
    @Bean(name = "userDataSourceProperties")
    @ConfigurationProperties("spring.datasource.user")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "userDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari.user")
    public DataSource dataSource(@Qualifier("userDataSourceProperties") DataSourceProperties userDataSourceProperties) {
        HikariDataSource dataSource = userDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(userDataSourceProperties.getName())) {
            dataSource.setPoolName(userDataSourceProperties.getName());
        }
        return dataSource;
    }

    //无hikari配置
//    @Primary
//    @Bean(name = "userDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.user.configuration")
//    public DataSource dataSource(@Qualifier("userDataSourceProperties") DataSourceProperties userDataSourceProperties) {
//        return userDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//    }

    @Primary
    @Bean(name = "userEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("userDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("com.dusizhong.examples.multidb.entity.user").persistenceUnit("user").build();
    }

    @Primary
    @Bean(name = "userTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("userEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
