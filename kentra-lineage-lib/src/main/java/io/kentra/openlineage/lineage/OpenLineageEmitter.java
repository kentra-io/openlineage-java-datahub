package io.kentra.openlineage.lineage;

import io.kentra.openlineage.lineage.model.Lineage;
import io.openlineage.client.OpenLineageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenLineageEmitter {

    private final OpenLineageClient openLineageClient;
    private final OpenLineageEventFactory eventFactory;

    public OpenLineageEmitter(OpenLineageClient openLineageClient, OpenLineageEventFactory eventFactory) {
        this.openLineageClient = openLineageClient;
        this.eventFactory = eventFactory;
    }

    public void emitRunEvent(Lineage lineage) {
        var runEvent = eventFactory.createRunEvent(lineage);
        openLineageClient.emit(runEvent);
    }
}
