package io.kentra.openlineage.config;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openlineage.client.Clients;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.OpenLineageClientUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.net.URI;

@Configuration
public class OpenLineageConfig {
  public static final String OPEN_LINEAGE_PRODUCER_NAMESPACE = "https://kentra-openlineage-java-lib";

  @Value("${openlineage.config.location}")
  public Resource openLineageConfigLocation;

  @Bean
  public OpenLineageClient openLineageClient() throws Exception {
    System.out.println("LOADING CLIENT");
    var openLineageConfig = OpenLineageClientUtils.loadOpenLineageConfigYaml(
        openLineageConfigLocation.getInputStream(),
        new TypeReference<>() {}
    );
    return Clients.newClient(openLineageConfig);
  }

  @Bean
  public OpenLineage openLineage() {
    return new OpenLineage(URI.create(OPEN_LINEAGE_PRODUCER_NAMESPACE));
  }

}
