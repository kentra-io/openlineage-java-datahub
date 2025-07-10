package io.kentra.openlineage;

import io.kentra.openlineage.lineage.NodeMdcUtil;
import io.kentra.openlineage.lineage.model.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeMdcUtilTest {

  @Test
  public void testSerializeAndDeserializeNode() {
    // given
    var httpNode = new Node("nodeName", Node.Type.HTTP_ENDPOINT);
    NodeMdcUtil.putInMdc(httpNode);

    // when
    var retrievedNode = NodeMdcUtil.getFromMdc();

    // then
    assertEquals(httpNode, retrievedNode);
  }
}
