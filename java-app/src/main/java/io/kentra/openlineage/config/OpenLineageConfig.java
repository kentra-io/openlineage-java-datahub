package io.kentra.openlineage.config;

import io.openlineage.client.Clients;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class OpenLineageConfig {
    public static final String OPEN_LINEAGE_PRODUCER_NAMESPACE = "https://kentra-openlineage-java-lib";

    @Value("${openlineage.config.location}")
    public String openLineageConfigLocation;

    @Bean
    public OpenLineageClient openLineageClient() throws Exception {
        URI openLineageConfigResource = new ClassPathResource(openLineageConfigLocation).getURI();
        return Clients.newClient(() -> List.of(Paths.get(openLineageConfigResource)));
    }

    @Bean
    public OpenLineage openLineage() {
        return new OpenLineage(URI.create(OPEN_LINEAGE_PRODUCER_NAMESPACE));
    }

}
