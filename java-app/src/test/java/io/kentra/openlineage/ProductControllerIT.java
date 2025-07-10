package io.kentra.openlineage;

import io.kentra.openlineage.product.Product;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductControllerIT extends IntegrationTest {

  public static final int ONE = 1;

  @Autowired
  private TestRestTemplate restTemplate;

  @Value("classpath:expected-events/product/1-postgres-output.json")
  private Resource expectedLineageEventJson;

  @Test
  void postAndGetProduct() throws Exception {
    // Create a product
    HttpEntity<Product> request = createProductHttpEntity(null, 100);

    // POST request
    ResponseEntity<Integer> postResponse = restTemplate.postForEntity(
        "http://localhost:" + port + "/products", request, Integer.class);
    assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(postResponse.getBody()).isEqualTo(ONE);

    // Retrieve the product by ID (assuming auto-increment starts at 1)
    ResponseEntity<Product> getResponse = restTemplate.getForEntity(
        "http://localhost:" + port + "/products/" + ONE, Product.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody()).isNotNull();
    assertThat(getResponse.getBody().name()).isEqualTo("Test Product");

    // validate emitted OpenLineage event
    var receivedLineageEventString = mockLineageServer.getAllServeEvents().get(0).getRequest().getBodyAsString();
    String expectedLineageEvent = expectedLineageEventJson.getContentAsString(Charset.defaultCharset());
    assertThatJson(receivedLineageEventString)
        .isEqualTo(expectedLineageEvent);
  }

  @Test
  void postUpdateAndGetProduct() {
    // Create a product
    HttpEntity<Product> request = createProductHttpEntity(null, 100);

    // POST request
    ResponseEntity<Integer> postResponse = restTemplate.postForEntity(
        "http://localhost:" + port + "/products", request, Integer.class);
    assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Integer productId = postResponse.getBody();

    // Update the product
    var updateProductEntity = createProductHttpEntity(productId, 200);
    restTemplate.postForEntity(
        "http://localhost:" + port + "/products", updateProductEntity, Integer.class);

    // GET request to validate update
    ResponseEntity<Product> getResponse = restTemplate.getForEntity(
        "http://localhost:" + port + "/products/" + productId, Product.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody()).isNotNull();
    assertThat(getResponse.getBody().price()).isEqualTo(200);
  }


  private static @NotNull HttpEntity<Product> createProductHttpEntity(Integer id, Integer price) {
    Product product = new Product(id, "Test Product", price, null);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Product> request = new HttpEntity<>(product, headers);
    return request;
  }
}
