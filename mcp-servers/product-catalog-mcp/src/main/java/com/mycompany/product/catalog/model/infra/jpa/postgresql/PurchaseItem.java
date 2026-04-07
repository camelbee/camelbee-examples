package com.mycompany.product.catalog.model.infra.jpa.postgresql;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain Order JPA Entity Object to be used to poll event table.
 */

@Entity
@Table(name = "CAMELBEE_PURCHASEITEMS_TABLE_JPA")
@NoArgsConstructor
@Getter
@Setter
public class PurchaseItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String productId;

  private String productName;

  private Integer quantity;

  private BigDecimal price;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "PURCHASE_ID")
  private Purchase purchase;

  /**
   * The constructor.
   */
  public PurchaseItem(String productId, String productName, Integer quantity, BigDecimal price, Purchase purchase) {
    super();
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.price = price;
    this.purchase = purchase;
  }

}
