package io.kentra.openlineage.lineage.interceptors.kafka;

import io.kentra.openlineage.lineage.LineageRegistry;
import io.kentra.openlineage.lineage.NodeMdcUtil;
import io.kentra.openlineage.lineage.model.KafkaDataset;
import io.kentra.openlineage.lineage.model.Lineage;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.Set;

public class KafkaProducerLineageInterceptor<K, V> implements ProducerInterceptor<K, V> {
  private static LineageRegistry lineageRegistry;
  private static String hostname;
  private static String port;

  @Override
  public ProducerRecord<K, V> onSend(ProducerRecord<K, V> producerRecord) {
    var lineageNode = NodeMdcUtil.getFromMdc();
    lineageRegistry.registerLineage(new Lineage(
        lineageNode,
        Set.of(new KafkaDataset(hostname, port, lineageNode.name())),
        Set.of(new KafkaDataset(hostname, port, producerRecord.topic()))
    ));
    return producerRecord;
  }

  @Override
  public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

  }

  @Override
  public void close() {

  }

  @Override
  public void configure(Map<String, ?> map) {

  }

  public static void setLineageRegistrar(LineageRegistry lineageRegistry) {
    KafkaProducerLineageInterceptor.lineageRegistry = lineageRegistry;
  }

  public static void setHostname(String hostname) {
    KafkaProducerLineageInterceptor.hostname = hostname;
  }

  public static void setPort(String port) {
    KafkaProducerLineageInterceptor.port = port;
  }
}
