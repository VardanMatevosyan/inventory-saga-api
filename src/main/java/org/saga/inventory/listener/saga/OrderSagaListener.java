package org.saga.inventory.listener.saga;


import static java.util.Objects.isNull;
import static org.saga.common.enums.InventoryStatus.INVENTORY_APPROVED;
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
import org.saga.common.dto.inventory.event.InventoryPaymentEvent;
import org.saga.common.dto.order.OrderCreateRequest;
import org.saga.common.dto.order.ProductItemDto;
import org.saga.common.dto.order.event.OrderCreateEvent;
import org.saga.inventory.document.Product;
import org.saga.inventory.service.ProductService;
import org.saga.inventory.service.impl.MessageBroker;
import org.saga.inventory.validator.ProductValidatorService;
import org.saga.inventory.validator.model.Violation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSagaListener {

  final MessageBroker messageBroker;
  final ProductService productService;
  final ProductValidatorService productValidatorService;

  @Value("${inventory-payment-saga-topic}")
  String inventoryPaymentSagaTopic;

  @Value("${inventory-order-saga-topic}")
  String inventoryOrderSagaTopic;

  // todo need to refactor. factory for creation, biFunction and updateProduct one time execution
  @RetryableTopic
  @KafkaListener(groupId = "inventory-consumer", topics = {"order-saga-topic"})
  public void onOrderCreate(OrderCreateEvent orderCreateEvent) {
    log.info("Received order create event %s".formatted(orderCreateEvent));

    List<ProductItemDto> productItems = orderCreateEvent.getOrderCreateRequest().getProductItems();
    List<String> productIds = getProductIds(productItems);
    List<Product> products = productService.findAllByIds(productIds); // experiment returning Map<String, Product>
    Map<String, Product> productMap = convertToProductMap(products);
    List<Violation> violations = productValidatorService.validate(productMap, productItems);

    if (areProductsValid(violations)) {
      updateProduct(productMap, productItems, (a, b) -> a - b);
      sendInventoryPaymentEvent(orderCreateEvent);
    } else {
      sendInventoryOrderEvent(orderCreateEvent);
    }
  }

  @DltHandler
  public void processOrderCreateEventAfterRetries(OrderCreateEvent orderCreateEvent) {
    log.info("end of retires sending to an inventory order event");
    sendInventoryOrderEvent(orderCreateEvent);
  }

  private void updateProduct(Map<String, Product> productMap,
                            List<ProductItemDto> productItems,
                            BiFunction<Integer, Integer, Integer> operation) {
    // this should be process only ones if successfully executed
    // if retries for other exceptions do not need to execute this code
      productService.updateProduct(productMap, productItems, operation);
  }

  private Map<String, Product> convertToProductMap(List<Product> products) {
    return products
        .stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));
  }

  private void sendInventoryOrderEvent(OrderCreateEvent orderCreateEvent) {
    InventoryOrderEvent inventoryEvent = buildInventoryOrderEvent(orderCreateEvent);
    messageBroker.send(inventoryOrderSagaTopic, inventoryEvent);
  }

  private InventoryOrderEvent buildInventoryOrderEvent(OrderCreateEvent orderCreateEvent) {
    OrderCreateRequest orderCreateRequest = orderCreateEvent.getOrderCreateRequest();
    InventoryDto inventoryDto = buildInventoryDto(orderCreateRequest);
    return InventoryOrderEvent.builder()
        .inventoryDto(inventoryDto)
        .paymentStatus(PAYMENT_REJECTED)
        .build();
  }

  private InventoryDto buildInventoryDto(OrderCreateRequest orderCreateRequest) {
    return InventoryDto.builder()
        .orderId(orderCreateRequest.getOrderId())
        .orderPrice(orderCreateRequest.getPrice())
        .customerEmail(orderCreateRequest.getCustomerEmail())
        .productItems(orderCreateRequest.getProductItems())
        .build();
  }

  private void sendInventoryPaymentEvent(OrderCreateEvent orderCreateEvent) {
    InventoryPaymentEvent paymentEvent = buildPaymentEvent(orderCreateEvent);
    messageBroker.send(inventoryPaymentSagaTopic, paymentEvent);
  }

  private InventoryPaymentEvent buildPaymentEvent(OrderCreateEvent orderCreateEvent) {
    OrderCreateRequest orderCreateRequest = orderCreateEvent.getOrderCreateRequest();
    InventoryDto inventoryDto = buildInventoryDto(orderCreateRequest);
    return InventoryPaymentEvent.builder()
        .inventoryDto(inventoryDto)
        .inventoryStatus(INVENTORY_APPROVED)
        .build();
  }

  private static boolean areProductsValid(List<Violation> violations) {
    return isNull(violations) || violations.isEmpty();
  }

  private static List<String> getProductIds(List<ProductItemDto> productItems) {
    return productItems
        .stream()
        .map(ProductItemDto::getProductId)
        .toList();
  }

}
