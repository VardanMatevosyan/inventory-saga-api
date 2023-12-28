package org.saga.inventory.validator;

import java.util.List;
import java.util.Map;
import org.saga.common.dto.order.ProductItemDto;
import org.saga.inventory.document.Product;
import org.saga.inventory.validator.model.Violation;

public interface ProductValidatorService {

  List<Violation> validate(Map<String, Product> productMap, List<ProductItemDto> productItems);
}
