package org.saga.inventory.dto.saga.payment.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.saga.inventory.dto.saga.payment.PaymentRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEvent {

  PaymentRequest paymentRequest;
  PaymentStatus paymentStatus;

}
