package io.fintech.loan.application.service.model.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Domain Order.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Order {

  private String id;

  private String salesChannel;

  private StatusEnum status;

  private LocalDate orderDate;

  private OffsetDateTime lastUpdateTimestamp;

  private List<OrderItem> items;

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

}
