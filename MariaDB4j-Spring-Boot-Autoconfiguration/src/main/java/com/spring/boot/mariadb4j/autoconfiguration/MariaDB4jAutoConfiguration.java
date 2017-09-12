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
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass({MariaDB4jSpringService.class, DataSource.class})
@EnableConfigurationProperties(value = {DatasourceProperties.class, MariaDB4jProperties.class})
public class MariaDB4jAutoConfiguration {

    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";
    private static final String DEFAULT_DATABASE_NAME = "test";
    private static final String DEFAULT_USERNAME = "su";
    private static final String DEFAULT_PASSWORD = "";

    private static final String DEFAULT_BASE_DIR = "";
    private static final String DEFAULT_DATA_DIR = "";
    private static final String DEFAULT_LIB_DIR = "";
    private static final Integer DEFAULT_PORT = 0;
    private static final String DEFAULT_SOCKET = "";

    private final DatasourceProperties datasourceProperties;
    private final MariaDB4jProperties mariaDB4jProperties;
    private static Log log = LogFactory.getLog(MariaDB4jAutoConfiguration.class);

    public MariaDB4jAutoConfiguration(DatasourceProperties datasourceProperties, MariaDB4jProperties mariaDB4jProperties) {
        this.datasourceProperties = datasourceProperties;
        this.mariaDB4jProperties = mariaDB4jProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public MariaDB4jSpringService mariaDB4jSpringService() {
        String baseDir = (StringUtils.isEmpty(mariaDB4jProperties.getBaseDir())) ? DEFAULT_BASE_DIR : mariaDB4jProperties.getBaseDir();
        String dataDir = (StringUtils.isEmpty(mariaDB4jProperties.getDataDir())) ? DEFAULT_DATA_DIR : mariaDB4jProperties.getDataDir();
        String libDir = (StringUtils.isEmpty(mariaDB4jProperties.getLibDir())) ? DEFAULT_LIB_DIR : mariaDB4jProperties.getLibDir();
        String socket = (StringUtils.isEmpty(mariaDB4jProperties.getSocket())) ? DEFAULT_SOCKET : mariaDB4jProperties.getSocket();
        Integer port = (mariaDB4jProperties.getPort() == null) ? DEFAULT_PORT : mariaDB4jProperties.getPort();
        MariaDB4jSpringService mariaDB4jSpringService = new MariaDB4jSpringService();
        mariaDB4jSpringService.setDefaultBaseDir(baseDir);
        mariaDB4jSpringService.setDefaultDataDir(dataDir);
        mariaDB4jSpringService.setDefaultLibDir(libDir);
        mariaDB4jSpringService.setDefaultPort(port);
        mariaDB4jSpringService.setDefaultSocket(socket);
        return mariaDB4jSpringService;
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() throws ManagedProcessException {
        mariaDB4jSpringService().getDB().createDB(mariaDB4jProperties.getDatabaseName());
        DBConfigurationBuilder dBConfiguration = mariaDB4jSpringService().getConfiguration();

        String databaseName = mariaDB4jProperties.getDatabaseName();
        String username = datasourceProperties.getUsername();
        String password = datasourceProperties.getPassword();
        String driverClassName = datasourceProperties.getDriverClassName();

        driverClassName = (StringUtils.isEmpty(driverClassName)) ? DEFAULT_DRIVER_CLASS_NAME : driverClassName;
        databaseName = (StringUtils.isEmpty(databaseName)) ? DEFAULT_DATABASE_NAME : databaseName;
        username = (StringUtils.isEmpty(username)) ? DEFAULT_USERNAME : username;
        password = (StringUtils.isEmpty(password)) ? DEFAULT_PASSWORD : password;

        return DataSourceBuilder
                .create()
                .url(dBConfiguration.getURL(databaseName))
                .driverClassName(driverClassName)
                .username(username)
                .password(password)
                .build();
    }
}
