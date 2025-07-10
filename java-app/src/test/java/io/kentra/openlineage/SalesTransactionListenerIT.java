package io.kentra.openlineage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.kentra.openlineage.product.Product;
import io.kentra.openlineage.product.ProductRepository;
import io.kentra.openlineage.transaction.EnrichedSalesTransaction;
import io.kentra.openlineage.transaction.SalesTransaction;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Testcontainers
public class SalesTransactionListenerIT extends IntegrationTest {
  public static final String TRANSACTION_ID = "8fe95ed6-f57f-44a5-bfaf-d533837b0bae";
  @Autowired
  KafkaTemplate<String, SalesTransaction> salesTransactionKafkaTemplate;
  @Autowired
  ProductRepository productRepository;
  @Autowired
  ObjectMapper objectMapper;

  @Value("${kafka.topics.sales-transaction}")
  private String salesTransactionTopic;
  @MockitoSpyBean
  private MockEnrichedSalesTransactionListener spyListener;

  @Value("classpath:expected-events/sales-transaction/1-postgres-lineage-input.json")
  private Resource firstExpectedLineageEventFile;
  @Value("classpath:expected-events/sales-transaction/2-kafka-lineage-output.json")
  private Resource secondExpectedLineageEventFile;

  @Container
  static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.9.2"))
      .withStartupTimeout(Duration.ofSeconds(10))
      .waitingFor(Wait.forLogMessage(".*Kafka startTimeMs:.*", 1));

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Test
  void shouldEnrichTransaction() throws Exception {
    // given
    mockLineageServer.stubFor(post(urlEqualTo("/api/v1/lineage"))
        .willReturn(aResponse().withStatus(200)));
    SalesTransaction salesTransaction = generateSalesTransaction();
    Product product = generateProduct();
    var productId = productRepository.save(product);
    assertEquals(1, productId.id());

    // when
    log.info("Sending SalesTransaction to {}", salesTransactionTopic);
    salesTransactionKafkaTemplate.send(salesTransactionTopic, salesTransaction).get();
    log.info("Sent SalesTransaction to {}", salesTransactionTopic);


    // then
    Mockito.verify(spyListener, Mockito.timeout(30000))
        .listen(Mockito.eq(expectedTransaction()));

    // wiremock returns events in the reverse order - at index 0 is the most recent event
    verifyReceivedEvent(mockLineageServer.getAllServeEvents().get(1), firstExpectedLineageEventFile);
    verifyReceivedEvent(mockLineageServer.getAllServeEvents().get(0), secondExpectedLineageEventFile);
  }

  private void verifyReceivedEvent(ServeEvent receivedEvent, Resource expectedEventFile) throws IOException {
    var firstReceivedLineageEvent = receivedEvent.getRequest().getBodyAsString();
    String firstExpectedLineageEvent = expectedEventFile.getContentAsString(Charset.defaultCharset());
    assertThatJson(firstReceivedLineageEvent)
        .withOptions(Option.IGNORING_ARRAY_ORDER)
        .isEqualTo(firstExpectedLineageEvent);
  }

  private Product generateProduct() {
    return new Product(
        null,
        "name",
        2,
        LocalDateTime.now(clockProvider.getClock())
    );
  }

  private SalesTransaction generateSalesTransaction() {
    return new SalesTransaction(
        UUID.fromString(TRANSACTION_ID),
        LocalDateTime.now(clockProvider.getClock()),
        3,
        5,
        1
    );
  }

  private EnrichedSalesTransaction expectedTransaction() {
    return new EnrichedSalesTransaction(
        UUID.fromString(TRANSACTION_ID),
        LocalDateTime.now(clockProvider.getClock()),
        3,
        5,
        1,
        "name",
        2,
        6
    );
  }

  @Test
  @Disabled
    // enable for manual testing
  void manualSendToDockerCompose() throws Exception {
    // given
    RestTemplate restTemplate = new RestTemplate();

    SalesTransaction salesTransaction = generateSalesTransaction();
    Product product = generateProduct();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Product> request = new HttpEntity<>(product, headers);
    ResponseEntity<Integer> postResponse = restTemplate.postForEntity(
        "http://localhost:8085/products", request, Integer.class);
    log.info("submitted product, received response: {}", postResponse);
    log.info("Send this transaction manually: {}", objectMapper.writeValueAsString(salesTransaction));

  }
}

@Component
class MockEnrichedSalesTransactionListener {
  @KafkaListener(topics = "${kafka.topics.enriched-sales-transaction}", groupId = "test-listener")
  public void listen(EnrichedSalesTransaction transaction) {
    // irrelevant, we just need the MockitoSpyBean
  }
}