package io.fintech.loan.application.service.model.domain;

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
public class PurchaseEventMessage {

  private EventTypeEnum eventType;

  private String data;

  /**
   * The status of an order.
   */
  public enum EventTypeEnum {

    LISTORDERS("ListOrders"),

    CREATEORDER("CreateOrder"),

    CREATEORDERBATCH("CreateOrdersBatch"),

    GETORDER("GetOrder"),

    REPLACEORDER("ReplaceOrder"),

    UPDATEORDER("UpdateOrder"),

    DELETEORDER("DeleteOrder");

    private String value;

    EventTypeEnum(String value) {
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
     * EventTypeEnum.
     *
     * @param value The EventType value.
     * @return EventTypeEnum.
     */
    public static EventTypeEnum fromValue(String value) {
      for (EventTypeEnum b : values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

}
