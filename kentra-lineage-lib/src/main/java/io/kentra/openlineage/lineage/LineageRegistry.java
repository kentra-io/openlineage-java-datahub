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

    public synchronized void registerLineage(Lineage rawLineage) {
        var node = rawLineage.node();
        if (lineageMap.containsKey(node)) {
            Lineage registeredNodeLineage = lineageMap.get(node);
            Lineage newLineage = registeredNodeLineage.copy();
            boolean modifiedInputs = newLineage.inputs().addAll(rawLineage.inputs());
            boolean modifiedOutputs = newLineage.outputs().addAll(rawLineage.outputs());
            if (modifiedInputs || modifiedOutputs) {
                updateLineage(newLineage);
                log.info("Updated lineage for node: {}. Current lineage: {}", node, lineageMap.get(node));
            }
        } else {
            var lineage = rawLineage.copy();
            updateLineage(lineage);
            log.info("Registered new lineage for node: {}. Current lineage: {}", node, lineageMap.get(node));
        }
    }

    private void updateLineage(Lineage newLineage) {
        newLineage.inputs().removeIf(newLineage.outputs()::contains);
        lineageMap.put(newLineage.node(), newLineage);
        if (!newLineage.outputs().isEmpty()) {
            openLineageEmitter.emitRunEvent(newLineage);
        }
    }

    public void clearRegistry() {
        lineageMap.clear();
        NodeMdcUtil.clearNodeMdc();
    }
}
