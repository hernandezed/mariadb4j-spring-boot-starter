/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass({MariaDB4jSpringService.class, DataSource.class})
@EnableConfigurationProperties(value = {MariaDB4jProperties.class})
public class MariaDB4jAutoConfiguration {

    private final MariaDB4jProperties mariaDB4jProperties;
    private static Log log = LogFactory.getLog(MariaDB4jAutoConfiguration.class);

    public MariaDB4jAutoConfiguration(MariaDB4jProperties mariaDB4jProperties) {
        this.mariaDB4jProperties = mariaDB4jProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public MariaDB4jSpringService mariaDB4jSpringService() {
        MariaDB4jSpringService mariaDB4jSpringService = new MariaDB4jSpringService();
        DBConfigurationBuilder dBConfigurationBuilder = mariaDB4jSpringService.getConfiguration();

        String dataDir = (StringUtils.isEmpty(mariaDB4jProperties.getDataDir())) ? dBConfigurationBuilder.getDataDir() : mariaDB4jProperties.getDataDir();
        String libDir = (StringUtils.isEmpty(mariaDB4jProperties.getLibDir())) ? dBConfigurationBuilder.getLibDir() : mariaDB4jProperties.getLibDir();
        String socket = (StringUtils.isEmpty(mariaDB4jProperties.getSocket())) ? dBConfigurationBuilder.getSocket() : mariaDB4jProperties.getSocket();
        Integer port = (mariaDB4jProperties.getPort() == null) ? dBConfigurationBuilder.getPort() : mariaDB4jProperties.getPort();
        String baseDir = (StringUtils.isEmpty(mariaDB4jProperties.getBaseDir())) ? dBConfigurationBuilder.getBaseDir() : mariaDB4jProperties.getBaseDir();

        mariaDB4jSpringService.setDefaultBaseDir(baseDir);
        mariaDB4jSpringService.setDefaultDataDir(dataDir);
        mariaDB4jSpringService.setDefaultLibDir(libDir);
        mariaDB4jSpringService.setDefaultPort(port);
        mariaDB4jSpringService.setDefaultSocket(socket);
        return mariaDB4jSpringService;
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("mariaDB4jSpringService")
    public DataSource dataSource(DataSourceProperties dataSourceProperties, MariaDB4jSpringService mariaDB4jSpringService) throws ManagedProcessException {
        String databaseName = mariaDB4jProperties.getDatabaseName();
        mariaDB4jSpringService.getDB().createDB(databaseName);
        DBConfigurationBuilder dBConfiguration = mariaDB4jSpringService().getConfiguration();

        return DataSourceBuilder
                .create()
                .url(dBConfiguration.getURL(databaseName))
                .driverClassName(dataSourceProperties.determineDriverClassName())
                .username(dataSourceProperties.determineUsername())
                .password(dataSourceProperties.determinePassword())
                .build();
    }

    @Bean
    @DependsOn("dataSource")
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(dataSource);
        return manager;
    }
}
