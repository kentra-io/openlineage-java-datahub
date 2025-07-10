package io.kentra.openlineage;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.kentra.openlineage.config.DatasourceTestConfig;
import io.kentra.openlineage.config.KafkaTestProducerConfig;
import io.kentra.openlineage.config.TableTruncator;
import io.kentra.openlineage.lineage.LineageRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({@ConfigureWireMock(port = 12345)})
@Import({DatasourceTestConfig.class, KafkaTestProducerConfig.class})
@ActiveProfiles("test")
@DirtiesContext
public class IntegrationTest {
  @LocalServerPort
  protected int port;
  @InjectWireMock
  protected WireMockServer mockLineageServer;
  @Autowired
  private TableTruncator tableTruncator;
  @Autowired
  private LineageRegistry lineageRegistry;

  @Mock
  ClockProvider clockProvider = mock(ClockProvider.class);

  @BeforeEach
  public void setUpMocks() {
    mockLineageServer.resetAll();
    tableTruncator.truncateAllTables();
    when(clockProvider.getClock()).thenReturn(Clock.fixed(
        LocalDateTime.of(2025, 1, 1, 10, 30).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    );
    mockLineageServer.stubFor(post(urlEqualTo("/api/v1/lineage"))
        .willReturn(aResponse().withStatus(200)));
    lineageRegistry.clearRegistry();
  }
}
