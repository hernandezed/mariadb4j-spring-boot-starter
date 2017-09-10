package com.spring.boot.mariadb4j.autoconfiguration;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({MariaDB4jSpringService.class, DataSource.class})
@EnableConfigurationProperties(value = {DatasourceProperties.class, MariaDB4jProperties.class})
public class MariaDB4jAutoConfiguration {

    private final DatasourceProperties datasourceProperties;
    private final MariaDB4jProperties mariaDB4jProperties;
    private static Log log = LogFactory.getLog(MariaDB4jAutoConfiguration.class);

    public MariaDB4jAutoConfiguration(DatasourceProperties datasourceProperties, MariaDB4jProperties mariaDB4jProperties) {
        this.datasourceProperties = datasourceProperties;
        this.mariaDB4jProperties = mariaDB4jProperties;
        validateProperties();
    }

    private void validateProperties() {
        if (mariaDB4jProperties.getDatabaseName() == null || mariaDB4jProperties.getPort() == null) {
            log.error("You must enter the name of the database and port.");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MariaDB4jSpringService mariaDB4jSpringService() {
        MariaDB4jSpringService mariaDB4jSpringService = new MariaDB4jSpringService();
        if (mariaDB4jProperties.getBaseDir() != null) {
            mariaDB4jSpringService.setDefaultBaseDir(mariaDB4jProperties.getBaseDir());
        }
        if (mariaDB4jProperties.getDataDir() != null) {
            mariaDB4jSpringService.setDefaultDataDir(mariaDB4jProperties.getDataDir());
        }
        if (mariaDB4jProperties.getLibDir() != null) {
            mariaDB4jSpringService.setDefaultLibDir(mariaDB4jProperties.getLibDir());
        }
        if (mariaDB4jProperties.getPort() != null) {
            mariaDB4jSpringService.setDefaultPort(mariaDB4jProperties.getPort());
        }
        if (mariaDB4jProperties.getSocket() != null) {
            mariaDB4jSpringService.setDefaultSocket(mariaDB4jProperties.getSocket());
        }
        return mariaDB4jSpringService;
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() throws ManagedProcessException {
        mariaDB4jSpringService().getDB().createDB(mariaDB4jProperties.getDatabaseName());
        DBConfigurationBuilder dBConfiguration = mariaDB4jSpringService().getConfiguration();

        return DataSourceBuilder
                .create()
                .url(dBConfiguration.getURL(mariaDB4jProperties.getDatabaseName()))
                .driverClassName("org.mariadb.jdbc.Driver")
                .build();
    }
}
