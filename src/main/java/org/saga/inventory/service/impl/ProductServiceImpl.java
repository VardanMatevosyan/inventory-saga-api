package org.saga.inventory.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.saga.common.dto.order.ProductItemDto;
import org.saga.inventory.document.Product;
import org.saga.inventory.dto.ProductDto;
import org.saga.inventory.mapper.ProductDtoMapper;
import org.saga.inventory.repository.ProductRepository;
import org.saga.inventory.service.ProductService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

  ProductRepository productRepository;
  ProductDtoMapper productDtoMapper;

  @Override
  public void create(ProductDto productDto) {
    Product document = productDtoMapper.toDocument(productDto);
    productRepository.save(document);
    // todo need to retrieve and create on top product log record
  }

  @Override
  public List<ProductDto> getAll() {
    Iterable<Product> products = productRepository.findAll();
    return productDtoMapper.toDtoList(products);
  }

  @Override
  public List<Product> findAllByIds(List<String> productIds) {
    return productRepository.findAllById(productIds);
  }

  @Override
  public void updateProduct(Map<String, Product> productMap,
                            List<ProductItemDto> productItems,
                            BiFunction<Integer, Integer, Integer> operation) {
    List<Product> products = getUpdatedProducts(productMap, productItems, operation);
    productRepository.saveAll(products);
    // todo need to retrieve and create on top product log record
  }

  private List<Product> getUpdatedProducts(Map<String, Product> productMap,
                                          List<ProductItemDto> items,
                                          BiFunction<Integer, Integer, Integer> operation) {
    return items
        .stream()
        .map(item -> updateQuantityAvailable(productMap, item, operation))
        .toList();
  }

  private Product updateQuantityAvailable(Map<String, Product> productMap,
                                            ProductItemDto item,
                                            BiFunction<Integer, Integer, Integer> operation) {
    Product product = productMap.get(item.getProductId());
    Integer newQuantity = operation.apply(product.getQuantityAvailable(), item.getAmount());
    product.setQuantityAvailable(newQuantity);
    return product;
  }
}
