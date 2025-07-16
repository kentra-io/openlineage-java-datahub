package io.kentra.openlineage.lineage;

import io.kentra.openlineage.lineage.model.Lineage;
import io.kentra.openlineage.lineage.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LineageRegistry {
    final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    final Map<Node, Lineage> lineageMap = new HashMap<>();

    final OpenLineageEmitter openLineageEmitter;

    public LineageRegistry(OpenLineageEmitter openLineageEmitter) {
        this.openLineageEmitter = openLineageEmitter;
    }

    public synchronized void registerLineage(Lineage lineage) {
        var node = lineage.node();
        if (lineageMap.containsKey(node)) {
            Lineage registeredNodeLineage = lineageMap.get(node);
            Lineage newLineage = registeredNodeLineage.copy();
            boolean modifiedInputs = newLineage.inputs().addAll(lineage.inputs());
            boolean modifiedOutputs = newLineage.outputs().addAll(lineage.outputs());
            if (modifiedInputs || modifiedOutputs) {
                lineageMap.put(node, newLineage);
                openLineageEmitter.emitRunEvent(newLineage);
                log.info("Updated lineage for node: {}. Current lineage: {}", node, lineageMap.get(node));
            }
        } else {
            lineageMap.put(lineage.node(), lineage);
            openLineageEmitter.emitRunEvent(lineage);
            log.info("Registered new lineage for node: {}. Current lineage: {}", node, lineageMap.get(node));
        }
    }

    public void clearRegistry() {
        lineageMap.clear();
        NodeMdcUtil.clearNodeMdc();
    }
}
