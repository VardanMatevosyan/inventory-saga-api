package org.saga.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

  private String id;
  private String name;
  private String description;
  private Integer price;
  private Integer quantityAvailable;
  private String category;
  private SupplierDto supplierDto;

}
