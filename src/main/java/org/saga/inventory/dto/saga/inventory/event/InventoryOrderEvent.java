package org.saga.inventory.dto.saga.inventory.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.saga.inventory.dto.saga.EventDto;
import org.saga.inventory.dto.saga.inventory.InventoryDto;
import org.saga.inventory.dto.saga.payment.event.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryOrderEvent extends EventDto {

  InventoryDto inventoryDto;
  PaymentStatus paymentStatus;

}
