package io.fintech.loan.application.service.model.infra.cache;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Infra Cache PurchaseItem.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PurchaseItem {

  private String id;

  private String purchaseId;

  private String productId;

  private String productName;

  private Integer quantity;

  private BigDecimal price;

}