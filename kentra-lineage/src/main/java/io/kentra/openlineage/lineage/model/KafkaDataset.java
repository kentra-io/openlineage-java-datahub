package io.kentra.openlineage.lineage.model;

public record KafkaDataset(
    String hostname,
    String port,
    String topicName
) implements Dataset {

  @Override
  public String namespace() {
    return "kafka://" + hostname + ":" + port;
  }

  @Override
  public String name() {
    return topicName;
  }
}
