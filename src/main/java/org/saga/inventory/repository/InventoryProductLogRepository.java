package org.saga.inventory.repository;

import org.saga.inventory.document.InventoryProductLog;
import org.saga.inventory.document.Supplier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryProductLogRepository
    extends MongoRepository<InventoryProductLog, String> {

}
