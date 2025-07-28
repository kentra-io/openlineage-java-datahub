package io.kentra.openlineage.lineage;

import io.kentra.openlineage.ClockProvider;
import io.kentra.openlineage.lineage.model.Lineage;
import io.openlineage.client.OpenLineage;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class OpenLineageEventFactory {
  // one run id per instance
  public static final UUID RUN_ID = UUID.randomUUID();

  private final String namespace;
  private final ClockProvider clockProvider;
  private final OpenLineage ol;

  public OpenLineageEventFactory(String namespace, ClockProvider clockProvider, OpenLineage ol) {
    this.namespace = namespace;
    this.clockProvider = clockProvider;
    this.ol = ol;
  }

  public OpenLineage.RunEvent createRunEvent(Lineage lineage) {
    ZonedDateTime now = ZonedDateTime.now(clockProvider.getClock());

    // run facets
    OpenLineage.RunFacets runFacets = ol.newRunFacetsBuilder()
        .processing_engine(ol.newProcessingEngineRunFacet("1.0", "java", "1.34.0")) // todo set properly
        .build();

    // a run is composed of run id, and run facets
    OpenLineage.Run run = ol.newRunBuilder().runId(RUN_ID).facets(runFacets).build();

    // job facets
    OpenLineage.JobFacets jobFacets = ol.newJobFacetsBuilder().build();

    // job
    String name = lineage.node().getReadableName();
    OpenLineage.Job job = ol.newJobBuilder().namespace(namespace).name(name).facets(jobFacets).build();

    // input dataset
    List<OpenLineage.InputDataset> inputs = lineage.inputs().stream().map(input ->
        ol.newInputDatasetBuilder()
            .namespace(input.namespace())
            .name(input.name())
            .facets(ol.newDatasetFacetsBuilder()
                .version(ol.newDatasetVersionDatasetFacet("input-version"))
                .build()) // todo what is this?
            .inputFacets(ol.newInputDatasetInputFacetsBuilder().build())
            .build()
    ).toList();
    // output dataset
    List<OpenLineage.OutputDataset> outputs = lineage.outputs().stream().map(output ->
        ol.newOutputDatasetBuilder()
            .namespace(output.namespace())
            .name(output.name())
            .facets(ol.newDatasetFacetsBuilder()
                .version(ol.newDatasetVersionDatasetFacet("output-version"))
                .build()) // todo what is this?
            .build()
    ).toList();

    // run state update which encapsulates all - with START event in this case
    OpenLineage.RunEvent runStateUpdate = ol.newRunEventBuilder()
        .eventType(OpenLineage.RunEvent.EventType.START)
        .eventTime(now)
        .run(run)
        .job(job)
        .inputs(inputs)
        .outputs(outputs)
        .build();

    return runStateUpdate;
  }

}
