package io.kentra.openlineage.lineage.model;


import java.util.HashSet;
import java.util.Set;

public record Lineage(
    Node node,
    Set<Dataset> inputs,
    Set<Dataset> outputs
) {
  public Lineage copy() {
    return new Lineage(node, new HashSet<>(inputs), new HashSet<>(outputs));
  }
}
