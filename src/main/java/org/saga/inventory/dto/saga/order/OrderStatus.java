package org.saga.inventory.dto.saga.order;

import static org.saga.inventory.dto.saga.payment.event.PaymentStatus.PAYMENT_COMPLETED;
import static org.saga.inventory.dto.saga.payment.event.PaymentStatus.PAYMENT_REJECTED;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.saga.inventory.dto.saga.payment.event.PaymentStatus;

@Getter
public enum OrderStatus {
  ORDER_CREATED,
  ORDER_COMPLETED,
  ORDER_CANCELED;

  private final static Map<String, String> ORDER_PAYMENT_STATUS_MAP = new HashMap<>();

  static  {
    ORDER_PAYMENT_STATUS_MAP.put(PAYMENT_COMPLETED.name(), ORDER_COMPLETED.name());
    ORDER_PAYMENT_STATUS_MAP.put(PAYMENT_REJECTED.name(), ORDER_CANCELED.name());
  }

  public static String getOrderStatus(PaymentStatus paymentStatus) {
    return ORDER_PAYMENT_STATUS_MAP.get(paymentStatus.name());
  }
}
