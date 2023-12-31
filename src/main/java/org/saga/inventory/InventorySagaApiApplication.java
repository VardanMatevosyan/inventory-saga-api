package org.saga.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableMongoRepositories
@EntityScan(basePackages = {"org.saga.inventory.document"})
@SpringBootApplication
public class InventorySagaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventorySagaApiApplication.class, args);
	}

}
