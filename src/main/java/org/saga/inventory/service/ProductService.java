package org.saga.inventory.service;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.saga.common.dto.order.ProductItemDto;
import org.saga.inventory.document.Product;
import org.saga.inventory.dto.ProductDto;
import org.springframework.retry.annotation.Retryable;

public interface ProductService {

  void create(ProductDto productDto);

  List<ProductDto> getAll();

  List<Product> findAllByIds(List<String> productIds);

  @Retryable(maxAttempts = 3, retryFor = RuntimeException.class)
  void updateProduct(Map<String, Product> productMap,
                    List<ProductItemDto> productItems,
                    BiFunction<Integer, Integer, Integer> operation);
}
