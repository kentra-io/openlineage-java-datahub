package io.kentra.openlineage.transaction;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrichedSalesTransaction (
    UUID transactionId,
    LocalDateTime timestamp,
    Integer amount,
    Integer sellerId,
    Integer productId,
    String productName,
    Integer productPrice,
    Integer transactionValue
) {
}
