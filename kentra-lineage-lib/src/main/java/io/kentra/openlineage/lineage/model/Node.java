package io.kentra.openlineage.lineage.model;

public record Node(
    String name,
    Type type
) {
  public String getReadableName() {
    return type().readableName + " " + name();
  }

  public enum Type {
    HTTP_ENDPOINT("Http endpoint"),
    KAFKA_LISTENER("Kafka listener");
    private final String readableName;

    Type(String readableName) {
      this.readableName = readableName;
    }
  }

}
