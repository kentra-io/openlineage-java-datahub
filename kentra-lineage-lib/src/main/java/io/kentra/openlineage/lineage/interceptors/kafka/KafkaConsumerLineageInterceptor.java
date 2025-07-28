package io.kentra.openlineage.lineage.interceptors.kafka;

import io.kentra.openlineage.lineage.LineageRegistry;
import io.kentra.openlineage.lineage.NodeMdcUtil;
import io.kentra.openlineage.lineage.model.KafkaDataset;
import io.kentra.openlineage.lineage.model.Lineage;
import io.kentra.openlineage.lineage.model.Node;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;
import java.util.Set;

public class KafkaConsumerLineageInterceptor<K, V> implements ConsumerInterceptor<K, V> {
  private static LineageRegistry lineageRegistry;
  private static String hostname;
  private static String port;

  @Override
  public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
    // The assumption here is that all consumed records are from the same topic
    records.partitions().stream().findFirst().ifPresent(
        partition -> {
          Node node = new Node(partition.topic(), Node.Type.KAFKA_LISTENER);
          NodeMdcUtil.putInMdc(node);
          lineageRegistry.registerLineage(new Lineage(
              node,
              Set.of(new KafkaDataset(hostname, port, node.name())),
              Set.of()
          ));
        }
    );
    return records;
  }

  @Override
  public void onCommit(Map offsets) {
  }

  @Override
  public void close() {
  }

  @Override
  public void configure(Map<String, ?> configs) {
  }

  public static void setLineageRegistry(LineageRegistry lineageRegistry) {
    KafkaConsumerLineageInterceptor.lineageRegistry = lineageRegistry;
  }

  public static void setHostname(String hostname) {
    KafkaConsumerLineageInterceptor.hostname = hostname;
  }

  public static void setPort(String port) {
    KafkaConsumerLineageInterceptor.port = port;
  }
}