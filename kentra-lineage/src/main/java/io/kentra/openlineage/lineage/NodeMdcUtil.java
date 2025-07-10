package io.kentra.openlineage.lineage;

import io.kentra.openlineage.lineage.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class NodeMdcUtil {
  private static Logger log = LoggerFactory.getLogger(NodeMdcUtil.class);

  public static final String LINEAGE_NODE_NAME = "lineageNodeName";
  public static final String LINEAGE_NODE_TYPE = "lineageNodeType";

  public static void putInMdc(Node node) {
    MDC.put(LINEAGE_NODE_NAME, node.name());
    MDC.put(LINEAGE_NODE_TYPE, node.type().toString());
  }

  public static Node getFromMdc() {
    var nodeName = MDC.get(LINEAGE_NODE_NAME);
    var nodeType = MDC.get(LINEAGE_NODE_TYPE);
    if (nodeName == null || nodeType == null) {
      log.warn("Failed to retrieve node from MDC. Found nodeName: {}, nodeType: {}", nodeName, nodeType);
      return null;
    }
    return new Node(nodeName, Node.Type.valueOf(nodeType));
  }

  public static void clearNodeMdc() {
    MDC.remove(LINEAGE_NODE_TYPE);
    MDC.remove(LINEAGE_NODE_NAME);
  }
}
