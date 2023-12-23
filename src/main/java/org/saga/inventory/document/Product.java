package org.saga.inventory.document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "products")
public class Product {

  @Id
  private String id;
  @Version
  private Long version;
  private String name;
  private String description;
  private Integer price;
  private Integer quantityAvailable;
  private String category;
  private Supplier supplier;


}
