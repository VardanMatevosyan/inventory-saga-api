package org.saga.inventory.service;

import java.util.List;
import java.util.Map;
import org.saga.inventory.document.Product;
import org.saga.inventory.dto.ProductDto;
import org.saga.inventory.dto.saga.order.ProductItemDto;

public interface ProductService {

  void create(ProductDto productDto);

  List<ProductDto> getAll();

  List<Product> findAllByIds(List<String> productIds);

  void updateProduct(Map<String, Product> productMap , List<ProductItemDto> productItems);
}
