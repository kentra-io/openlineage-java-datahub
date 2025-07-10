package io.kentra.openlineage.config;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableTruncator {
    private static final Logger log = LoggerFactory.getLogger(TableTruncator.class);

    @Autowired
    @Qualifier("realJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @SneakyThrows
    public void truncateAllTables() {
        // Get all user table names
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public' " +
                        "AND tablename NOT LIKE 'pg_%' AND tablename NOT LIKE 'sql_%'",
                String.class
        );

        log.info("Truncating tables: {}", tables);
        if (!tables.isEmpty()) {
            // Disable FK constraints
            jdbcTemplate.execute("SET session_replication_role = 'replica'");

            // Truncate all tables
            jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE");

            // Re-enable FK constraints
            jdbcTemplate.execute("SET session_replication_role = 'origin'");
        }
    }
}