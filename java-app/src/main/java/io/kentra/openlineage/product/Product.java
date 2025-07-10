package io.kentra.openlineage.product;


import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public record Product(
        @Id
        Integer id,
        String name,
        Integer price,
        @CreatedDate
        LocalDateTime creationDate
) {
}
