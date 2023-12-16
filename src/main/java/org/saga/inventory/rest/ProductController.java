package org.saga.inventory.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.saga.inventory.dto.ProductDto;
import org.saga.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class ProductController {

  ProductService productService;

  @PostMapping(path = "/inventory/products/", consumes = APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> createProduct(@RequestBody ProductDto productDto) {
    productService.create(productDto);
    log.info("Product created %s".formatted(productDto));
    return ResponseEntity.status(CREATED).build();
  }

}
