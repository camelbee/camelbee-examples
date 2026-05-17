package io.fintech.loan.application.service.model.infra.cache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Infra Cache Purchase.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Purchase {

  private String id;

  private String salesChannel;

  private String status;

  private LocalDate purchaseDate;

  private LocalDateTime lastUpdateTimestamp;

  private List<PurchaseItem> items;
}