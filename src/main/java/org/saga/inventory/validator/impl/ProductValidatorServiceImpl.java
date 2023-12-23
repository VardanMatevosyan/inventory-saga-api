package org.saga.inventory.validator.impl;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.saga.inventory.document.Product;
import org.saga.inventory.dto.saga.order.ProductItemDto;
import org.saga.inventory.validator.ProductValidatorService;
import org.saga.inventory.validator.model.Violation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductValidatorServiceImpl implements ProductValidatorService {

  @Override
  public List<Violation> validate(Map<String, Product> productMap, List<ProductItemDto> items) {
    return items
        .stream()
        .filter(productNotAvailablePredicate(productMap))
        .map(pi -> toViolation(pi, productMap))
        .toList();
  }


  private Predicate<ProductItemDto> productNotAvailablePredicate(Map<String, Product> productMap) {
    return pi -> {
      Product product = productMap.get(pi.getProductId());
      return isNull(product) || product.getQuantityAvailable() < pi.getAmount();
    };
  }



  private Violation toViolation(ProductItemDto pi, Map<String, Product> productMap) {
    Product product = productMap.get(pi.getProductId());
    return Violation.builder()
        .productName(product.getName())
        .availableQuantity(product.getQuantityAvailable())
        .build();
  }
}
