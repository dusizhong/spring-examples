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
@EnableJpaRepositories(basePackages = {"com.dusizhong.examples.multidb.repository.tender"}, entityManagerFactoryRef = "tenderEntityManagerFactory", transactionManagerRef = "tenderTransactionManager")
public class TenderDataSourceConfig {

    @Bean(name = "tenderDataSourceProperties")
    @ConfigurationProperties("spring.datasource.tender")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "tenderDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari.tender")
    public DataSource dataSource(@Qualifier("tenderDataSourceProperties") DataSourceProperties tenderDataSourceProperties) {
        HikariDataSource dataSource = tenderDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(tenderDataSourceProperties.getName())) {
            dataSource.setPoolName(tenderDataSourceProperties.getName());
        }
        return dataSource;
    }

    @Bean(name = "tenderEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("tenderDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("com.dusizhong.examples.multidb.entity.tender").persistenceUnit("tender").build();
    }

    @Primary
    @Bean(name = "tenderTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("tenderEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
