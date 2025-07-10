package io.kentra.openlineage.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@TestConfiguration
public class DatasourceTestConfig {

    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public JdbcTemplate realJdbcTemplate(@Qualifier("realDataSource") DataSource realDataSource) {
        // Uses the non-proxied DataSource
        return new JdbcTemplate(realDataSource);
    }
}
