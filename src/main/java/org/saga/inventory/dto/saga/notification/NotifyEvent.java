package org.saga.inventory.dto.saga.notification;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.saga.inventory.dto.saga.EventDto;
import org.saga.inventory.dto.saga.order.OrderStatus;
import org.saga.inventory.dto.saga.order.ProductItemDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotifyEvent extends EventDto {

  String customerEmail;
  Long orderId;
  List<ProductItemDto> productItems;
  OrderStatus orderStatus;
  Integer price;

}
