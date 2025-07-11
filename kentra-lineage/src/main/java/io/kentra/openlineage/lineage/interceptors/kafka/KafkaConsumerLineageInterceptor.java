package io.kentra.openlineage.lineage.interceptors.kafka;

import io.kentra.openlineage.lineage.NodeMdcUtil;
import io.kentra.openlineage.lineage.model.Node;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;

public class KafkaConsumerLineageInterceptor<K, V> implements ConsumerInterceptor<K, V> {
  public static String KAFKA_TOPIC_THREAD_CONTEXT_KEY = "kafkaTopic";

  @Override
  public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
    // The assumption here is that all consumed records are from the same topic
    records.partitions().stream().findFirst().ifPresent(
        partition -> NodeMdcUtil.putInMdc(new Node(partition.topic(), Node.Type.KAFKA_LISTENER))
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
}