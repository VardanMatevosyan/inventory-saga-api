package org.saga.inventory.document;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "inventory_product_logs")
public class InventoryProductLog {

  @Id
  private String id;
  private String productId;
  private String changeType;
  private Integer quantityChange;
  private Integer remainingQuantity;
  private LocalDateTime logDatetime;

}


