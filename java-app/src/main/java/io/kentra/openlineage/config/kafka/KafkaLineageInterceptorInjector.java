package io.kentra.openlineage.config.kafka;

import io.kentra.openlineage.lineage.LineageRegistry;
import io.kentra.openlineage.lineage.interceptors.kafka.KafkaProducerLineageInterceptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaLineageInterceptorInjector {

  private final LineageRegistry lineageRegistry;
  private final String kafkaBootstrapServers;

  public KafkaLineageInterceptorInjector(
      @Autowired LineageRegistry lineageRegistry,
      @Value("${spring.kafka.bootstrap-servers}") String kafkaBootstrapServers) {
    this.lineageRegistry = lineageRegistry;
    this.kafkaBootstrapServers = kafkaBootstrapServers;
  }

  @PostConstruct
  public void inject() {
    KafkaProducerLineageInterceptor.setLineageRegistrar(lineageRegistry);
    String[] hostAndPort = parseBootstrapServers(kafkaBootstrapServers);
    KafkaProducerLineageInterceptor.setHostname(hostAndPort[0]);
    KafkaProducerLineageInterceptor.setPort(hostAndPort[1]);
  }

  private String[] parseBootstrapServers(String kafkaBootstrapServers) {
    var hostAndPort = kafkaBootstrapServers.split(":");
    if (hostAndPort.length > 2) {
      throw new IllegalStateException("This setup assumes there's only one bootstrap server");
    }
    return hostAndPort;
  }
}
