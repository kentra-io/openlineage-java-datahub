# DataHub - Spring Boot Data Lineage integration POC
## What's in this repository?
- DataHub running locally in docker-compose
- kafka-client-app - Spring Boot app that listens to Kafka Topics, publishes to other topics and reports lineage to DataHub
- Apache Kafka 4.0, Schema Registry and Kafka UI in docker-compose

## What's the purpose of this POC?
I wanted to explore and document how to report data lineage in Spring Boot event-driven microservices. 
DataHub is AFAIK the most popular open-source data catalog, and if you want to visualize data lineage and document your 
streaming data products - this is one of the top choices. Sadly some capabilities are not yet present or not that 
documented, but precisely because of that I hope that this POC can be valuable.

If you know about an open source data catalog that's supporting the Data Streaming space better, and also supports data
contracts and data product documentation and discoverability - please let me know 
(https://www.linkedin.com/in/jan-siekierski/).

## How to run it?
### Prerequisites
   - Docker and Docker Compose
   - Java
   - Gradle
### Step by step guide to see the lineage graph in DataHub
1. Build the java app:

run the build script from the project root:
```bash
  ./build-kafka-client-app.sh
```
It builds the jar package and the Docker image that you'll be using

2. Run docker-compose with env variables from .env file
```bash
  docker-compose --env-file .env up
```
3. Run the script that will publish messages to two Kafka topics:
```bash
  ./publish-kafka-input-messages.sh 
```
4. Validate in Kafka UI (`localhost:8090`) that the following topics were created:
- input-topic-1
- input-topic-2
- output-topic-1
- output-topic-2
- joined-topic

At this stage you should have all topics created and schemas registered. Now you can ingest Kafka metadata into DataHub.

5. Ingest Kafka data in DataHub:
- Open http://localhost:9002/ingestion (credentials: `datahub/datahub`)
- Click "Create new source"
- Find "Kafka" and fill the form with the following values:
- Bootstrap Servers: `kafka:9094`
- Schema Registry URL: `http://schema-registry:8081`
- Click next, on the "Finish up" screen (4th step) give your cluster a name, for example "Kafka Cluster" and click Save & Run

6. Restart the client apps
```bash
  docker-compose restart kafka-client-app-1 kafka-client-app-2 kafka-joining-app
```
This is necessary to refresh the DynamicLineageService bean, which is responsible for reporting lineage data to DataHub.

7. Publish messages to the input topics again:
```bash
  ./publish-kafka-input-messages.sh 
```
This time the whole setup is waiting, and once your messages are processed, the lineage data will be reported to DataHub.

8. Validate in DataHub UI (`localhost:9002`) that the lineage data is ingested:
- click the search box on the top
- open "joined-topic"
- click the "Lineage" tab
- unwrap the nodes (left of both output topics) to see the full lineage graph.

Expected result:

<img src="img/lineage-graph-screenshot.png" width="600">

## How is Lineage reported to DataHub? 
In this setup each service instance registers as a node and reports all topics it's consuming from and publishing to.
This way you can achieve a graph of your event-driven microservices without any overhead. You can just build a library with 
this logic, add it to your dependencies and have it report lineage, and you'll see the full lineage graph in DataHub.

This Spring Boot service is using DataHub's library to emit DataHub-specific metadata events. There are two parts of this process.

### Registering as DataHub node
First, the service needs to register in DataHub as a node (DataJob). This is done in DataHubNodeReporter during startup. 
The approach taken registers each service as a node, but if you want something more granular - this should be easy to tailor to your needs.
This is a POC, so I was going for a simple, general solution.

### Registering lineage graph edges
Then during processing the application will register the lineage graph edges - what topics it's consuming from and publishing to.
The way this is achieved is by using AOP to intercept topic names from @KafkaListener beans and KafkaTemplate's send methods.
LineageReportingAspect is intercepting, extracting topic names and registering them in DynamicLineageService. 
DynamicLineageService is reporting lineage

## Alternative approaches
### Static context scanning
You could scan your application during startup - this will be more performant, as AOP adds some overhead, but will be much less flexible - 
you'd need some conventions, for example listing all topics in properties, or creating one KafkaTemplate per topic you're publishing to.

### Publishing OpenLineage events
DataHub's integration with OpenLineage is rather fresh and I've seen some issues in it. Might be a better general-purpose solution, 
I'd love to see an example that doesn't introduce overhead with new topics.

### Scanning Kafka metadata
AFAIK while you could get group ids from Kafka, producer identifiers are not stored in KRaft. On top of that, to actually draw the graph you'd need to 
link consumers and producers somehow, so you'd still need some conventions if you chose that approach.

### Using Kafka Headers to draw detailed lineage graph
You could accumulate lineage data in Kafka headers, but that's a bit more complicated setup, and out of scope of this POC. I might explore that in the future.

# TODO:
rework to add asynchronous reporting