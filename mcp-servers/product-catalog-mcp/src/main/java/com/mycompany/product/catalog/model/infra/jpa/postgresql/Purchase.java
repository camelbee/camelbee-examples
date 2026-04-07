package com.mycompany.product.catalog.model.infra.jpa.postgresql;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain Order JPA Entity Object to be used to poll event table.
 */

@Entity
@Table(name = "CAMELBEE_PURCHASES_TABLE_JPA")
@NamedQueries({
    @NamedQuery(
        name = "Purchase.findPurchasesBySalesChannel",
        query = "SELECT p FROM Purchase p WHERE p.salesChannel = :salesChannel ORDER BY p.id"
    ),
    @NamedQuery(
        name = "Purchase.findPurchaseBySalesChannelAndId",
        query = "SELECT p FROM Purchase p WHERE p.id = :id AND p.salesChannel = :salesChannel"
    ),
    @NamedQuery(
        name = "Purchase.countPurchasesBySalesChannel",
        query = "SELECT COUNT(p) FROM Purchase p WHERE p.salesChannel = :salesChannel"
    )
})
@NoArgsConstructor
@Getter
@Setter
public class Purchase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String salesChannel;

  @Convert(converter = StatusEnumConverter.class)
  private StatusEnum status;

  private LocalDate purchaseDate;

  private LocalDateTime lastUpdateTimestamp;

  @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<PurchaseItem> items = new ArrayList<>();

  // Helper method to add items
  public void addItem(PurchaseItem item) {
    items.add(item);
    item.setPurchase(this);
  }

  // Helper method to remove items
  public void removeItem(PurchaseItem item) {
    items.remove(item);
    item.setPurchase(null);
  }

  /**
   * The constructor.
   */
  public Purchase(String salesChannel, StatusEnum status, LocalDate purchaseDate, LocalDateTime lastUpdateTimestamp, List<PurchaseItem> items) {
    super();
    this.salesChannel = salesChannel;
    this.status = status;
    this.purchaseDate = purchaseDate;
    this.lastUpdateTimestamp = lastUpdateTimestamp;
    this.items = items;
  }

  // CPD-OFF
  /**
   * The status of an order.
   */
  public enum StatusEnum {

    PENDING("Pending"),

    CONFIRMED("Confirmed"),

    PROCESSING("Processing"),

    SHIPPED("Shipped"),

    DELIVERED("Delivered"),

    COMPLETED("Completed"),

    CANCELED("Canceled"),

    RETURNED("Returned"),

    FAILED("Failed"),

    ON_HOLD("On Hold");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    /**
     * StatusEnum.
     *
     * @param value The Status value.
     * @return StatusEnum.
     */
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : values()) {
        if (b.value.equalsIgnoreCase(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

  }

  /**
   * StatusEnumConverter.
   */
  @Converter(autoApply = true)
  public static class StatusEnumConverter implements AttributeConverter<StatusEnum, String> {

    @Override
    public String convertToDatabaseColumn(StatusEnum status) {
      if (status == null) {
        return null;
      }
      return status.getValue();
    }

    @Override
    public StatusEnum convertToEntityAttribute(String value) {
      if (value == null) {
        return null;
      }
      return StatusEnum.fromValue(value);
    }
  }

}
