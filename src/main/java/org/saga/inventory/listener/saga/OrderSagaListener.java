package org.saga.inventory.listener.saga;


import static java.util.Objects.isNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.saga.inventory.document.Product;
import org.saga.inventory.dto.saga.inventory.InventoryDto;
import org.saga.inventory.dto.saga.inventory.event.InventoryOrderEvent;
import org.saga.inventory.dto.saga.inventory.event.InventoryPaymentEvent;
import org.saga.inventory.dto.saga.inventory.event.InventoryStatus;
import org.saga.inventory.dto.saga.order.OrderCreateRequest;
import org.saga.inventory.dto.saga.order.ProductItemDto;
import org.saga.inventory.dto.saga.order.event.OrderCreateEvent;
import org.saga.inventory.dto.saga.payment.event.PaymentStatus;
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
  final ThreadLocal<Boolean> isProductUpdated = ThreadLocal.withInitial(() -> false);

  @Value("${inventory-payment-saga-topic}")
  String inventoryPaymentSagaTopic;

  @Value("${inventory-order-saga-topic}")
  String inventoryOrderSagaTopic;

  @RetryableTopic
  @KafkaListener(groupId = "inventory-consumer", topics = {"order-saga-topic"},
      properties = {"spring.json.value.default.type=org.saga.inventory.dto.saga.order.event.OrderCreateEvent"})
  public void onOrderCreate(OrderCreateEvent orderCreateEvent) {
    log.info("Received order create event %s".formatted(orderCreateEvent));

    List<ProductItemDto> productItems = orderCreateEvent.getOrderCreateRequest().getProductItems();
    List<String> productIds = getProductIds(productItems);
    List<Product> products = productService.findAllByIds(productIds); // experiment returning Map<String, Product>
    Map<String, Product> productMap = convertToProductMap(products);
    List<Violation> violations = productValidatorService.validate(productMap, productItems);

    if (areProductsValid(violations)) {
      updateProduct(productMap, productItems);
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

  private void updateProduct(Map<String, Product> productMap, List<ProductItemDto> productItems) {
    if (!isProductUpdated.get()) {
      productService.updateProduct(productMap, productItems);
      isProductUpdated.set(true);
    }
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
        .paymentStatus(PaymentStatus.PAYMENT_REJECTED)
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
        .inventoryStatus(InventoryStatus.INVENTORY_APPROVED)
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
