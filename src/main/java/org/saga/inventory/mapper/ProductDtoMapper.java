package org.saga.inventory.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.saga.inventory.document.Product;
import org.saga.inventory.document.Supplier;
import org.saga.inventory.dto.ProductDto;
import org.saga.inventory.dto.SupplierDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductDtoMapper {

  @Mapping(target = "supplierDto", source = "supplier")
  List<ProductDto> toDtoList(Iterable<Product> products);

  @Mapping(target = "supplierDto", source = "supplier")
  ProductDto toDto(Product product);

  @Mapping(target = "supplier", source = "supplierDto")
  Product toDocument(ProductDto productDto);

//  Supplier toDocument(SupplierDto supplierDto);
////
////  SupplierDto toDto(Supplier supplier);
}
