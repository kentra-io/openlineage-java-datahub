package io.kentra.openlineage.config;

import io.kentra.openlineage.ClockProvider;
import io.kentra.openlineage.lineage.LineageRegistry;
import io.kentra.openlineage.lineage.OpenLineageEmitter;
import io.kentra.openlineage.lineage.OpenLineageEventFactory;
import io.kentra.openlineage.lineage.interceptors.jdbc.JdbcLineageInterceptor;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KentraLineageConfig {

  @Bean
  public ClockProvider clockProvider() {
    return new ClockProvider();
  }

  @Bean
  public OpenLineageEventFactory openLineageEventFactory(
      @Value("${spring.application.name}") String namespace,
      ClockProvider clockProvider,
      OpenLineage openLineage
  ) {
    return new OpenLineageEventFactory(namespace, clockProvider, openLineage);
  }

  @Bean
  public OpenLineageEmitter openLineageEmitter(OpenLineageClient openLineageClient, OpenLineageEventFactory eventFactory) {
    return new OpenLineageEmitter(openLineageClient, eventFactory);
  }

  @Bean
  public LineageRegistry lineageRegistrar(OpenLineageEmitter openLineageEmitter) {
    return new LineageRegistry(openLineageEmitter);
  }

  @Bean
  public JdbcLineageInterceptor jdbcLineageInterceptor(LineageRegistry registrar) {
    return new JdbcLineageInterceptor(registrar);
  }
}
