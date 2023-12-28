package org.saga.inventory.listener.saga;


import static org.saga.common.enums.PaymentStatus.PAYMENT_REJECTED;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.saga.common.dto.inventory.InventoryDto;
import org.saga.common.dto.inventory.event.InventoryOrderEvent;
import org.saga.common.dto.order.ProductItemDto;
import org.saga.common.dto.order.event.OrderCreateEvent;
import org.saga.common.dto.payment.PaymentDto;
import org.saga.common.dto.payment.event.PaymentEvent;
import org.saga.inventory.document.Product;
import org.saga.inventory.service.ProductService;
import org.saga.inventory.service.impl.MessageBroker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSagaListener {

  final MessageBroker messageBroker;
  final ProductService productService;

  @Value("${inventory-order-saga-topic}")
  String inventoryOrderSagaTopic;

  // todo need to refactor. factory for creation, biFunction and updateProduct one time execution
  @RetryableTopic
  @KafkaListener(groupId = "inventory-consumer", topics = {"payment-saga-topic"})
  public void onOrderCreate(PaymentEvent paymentEvent) {
    log.info("Received payment event %s".formatted(paymentEvent));
    List<ProductItemDto> productItems = paymentEvent.getPaymentDto().getProductItems();
    List<String> productIds = getProductIds(productItems);
    List<Product> products = productService.findAllByIds(productIds); // experiment returning Map<String, Product>
    Map<String, Product> productMap = convertToProductMap(products);

    if (isPaymentFailed(paymentEvent)) {
      compensateInventory(productMap, productItems, Integer::sum);
    }

    sendInventoryOrderEvent(paymentEvent);
  }

  @DltHandler
  public void processOrderCreateEventAfterRetries(OrderCreateEvent orderCreateEvent) {
    log.info("End of retires on payment event. Can't finish inventory completion process. "
        + "Need to mark this event for future processing");
  }

  private boolean isPaymentFailed(PaymentEvent paymentEvent) {
    return paymentEvent.getPaymentStatus().equals(PAYMENT_REJECTED);
  }

  // this should be process only ones if successfully executed
  // if retries for other exceptions do not need to execute this code
  private void compensateInventory(Map<String, Product> productMap,
                            List<ProductItemDto> productItems,
                            BiFunction<Integer, Integer, Integer> operation) {
      productService.updateProduct(productMap, productItems, operation);
  }

  private Map<String, Product> convertToProductMap(List<Product> products) {
    return products
        .stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));
  }

  private void sendInventoryOrderEvent(PaymentEvent paymentEvent) {
    InventoryOrderEvent inventoryOrderEvent = buildInventoryOrderEvent(paymentEvent);
    messageBroker.send(inventoryOrderSagaTopic, inventoryOrderEvent);
  }

  private InventoryOrderEvent buildInventoryOrderEvent(PaymentEvent paymentEvent) {
    PaymentDto paymentDto = paymentEvent.getPaymentDto();
    InventoryDto inventoryDto = buildInventoryDto(paymentDto);
    return InventoryOrderEvent.builder()
        .inventoryDto(inventoryDto)
        .paymentStatus(paymentEvent.getPaymentStatus())
        .build();
  }

  private InventoryDto buildInventoryDto(PaymentDto paymentDto) {
    return InventoryDto.builder()
        .orderId(paymentDto.getOrderId())
        .orderPrice(paymentDto.getOrderPrice())
        .customerEmail(paymentDto.getCustomerEmail())
        .productItems(paymentDto.getProductItems())
        .build();
  }

  private static List<String> getProductIds(List<ProductItemDto> productItems) {
    return productItems
        .stream()
        .map(ProductItemDto::getProductId)
        .toList();
  }

}
