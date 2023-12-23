package org.saga.inventory.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.saga.inventory.dto.ProductDto;
import org.saga.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

  ProductService productService;

  @PostMapping(path = "/inventory/products/", consumes = APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> createProduct(@RequestBody ProductDto productDto) {
    productService.create(productDto);
    log.info("Product created %s".formatted(productDto));
    return ResponseEntity.status(CREATED).build();
  }

  @GetMapping(path = "/inventory/products/", produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ProductDto>> getAll() {
    List<ProductDto> products =  productService.getAll();
    return ResponseEntity.ok(products);
  }

}
