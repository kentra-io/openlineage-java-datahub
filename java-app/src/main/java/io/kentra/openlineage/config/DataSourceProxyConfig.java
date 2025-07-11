package io.kentra.openlineage.config;

import com.zaxxer.hikari.HikariDataSource;
import io.kentra.openlineage.lineage.interceptors.jdbc.PostgresJdbcLineageInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Slf4j
public class DataSourceProxyConfig {

    @Bean
    public DataSource realDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public DataSource dataSourceProxy(DataSource dataSource, PostgresJdbcLineageInterceptor lineageInterceptor) throws Exception {
        String dbUrl = cleanDbUrl(dataSource.getConnection().getMetaData().getURL());
        ProxyDataSourceBuilder.SingleQueryExecution beforeQuery = (execInfo, queryInfoList) -> {
            queryInfoList.forEach(queryInfo ->
                    lineageInterceptor.registerJdbcLineage(dbUrl, queryInfo.getQuery())
            );
        };
        return ProxyDataSourceBuilder
                .create(dataSource)
                .beforeQuery(beforeQuery)
                .name("ProxyDataSource")
                .build();
    }

    private String cleanDbUrl(String dbUrl) {
        String urlWithoutPrefix = dbUrl.startsWith("jdbc:") ? dbUrl.substring(5) : dbUrl;
        int queryIdx = urlWithoutPrefix.indexOf('?');
        String baseUrl = queryIdx >= 0 ? urlWithoutPrefix.substring(0, queryIdx) : urlWithoutPrefix;
        return URI.create(baseUrl).toString();
    }

}