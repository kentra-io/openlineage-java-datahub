package io.kentra.openlineage.transaction;

import io.kentra.openlineage.lineage.NodeMdcUtil;
import io.kentra.openlineage.product.Product;
import io.kentra.openlineage.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Slf4j
@Component
public class SalesTransactionListener {
  @Value("${kafka.topics.enriched-sales-transaction}")
  private String enrichedSalesTransaction;

  private final KafkaTemplate<String, EnrichedSalesTransaction> kafkaTemplate;
  private final ProductRepository productRepository;

  @KafkaListener(topics = "${kafka.topics.sales-transaction}", groupId = "${spring.application.name}")
  public void processSalesTransaction(SalesTransaction transaction) throws ExecutionException, InterruptedException {
    log.debug("Entering listener, Lineage Node retrieved from context: {}", NodeMdcUtil.getFromMdc());
    var product = productRepository.findById(transaction.productId()).get();
    var enrichedTransaction = enrich(transaction, product);
    kafkaTemplate.send(enrichedSalesTransaction, enrichedTransaction).get();
  }

  private EnrichedSalesTransaction enrich(SalesTransaction transaction, Product product) {
    return new EnrichedSalesTransaction(
        transaction.transactionId(),
        transaction.timestamp(),
        transaction.amount(),
        transaction.sellerId(),
        transaction.productId(),
        product.name(),
        product.price(),
        transaction.amount() * product.price()
    );
  }
}
