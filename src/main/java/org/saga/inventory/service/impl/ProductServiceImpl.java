package org.saga.inventory.service.impl;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.saga.inventory.document.Product;
import org.saga.inventory.dto.ProductDto;
import org.saga.inventory.dto.saga.order.ProductItemDto;
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
  public void updateProduct(Map<String, Product> productMap, List<ProductItemDto> productItems) {
    List<Product> products = getUpdatedProducts(productMap, productItems);
    productRepository.saveAll(products);
    // todo need to retrieve and create on top product log record
  }

  private List<Product> getUpdatedProducts(Map<String, Product> productMap,
                                          List<ProductItemDto> items) {
    return items
        .stream()
        .map(item -> decreaseQuantityAvailable(productMap, item))
        .toList();
  }

  private Product decreaseQuantityAvailable(Map<String, Product> productMap, ProductItemDto item) {
    Product product = productMap.get(item.getProductId());
    int newQuantityAvailable = product.getQuantityAvailable() - item.getAmount();
    product.setQuantityAvailable(newQuantityAvailable);
    return product;
  }
}
