package io.kentra.openlineage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@EnableJdbcRepositories(basePackageClasses = JavaOpenlineageApp.class)
@SpringBootApplication
public class JavaOpenlineageApp {

    public static void main(String[] args) {
        SpringApplication.run(JavaOpenlineageApp.class, args);
    }

}
