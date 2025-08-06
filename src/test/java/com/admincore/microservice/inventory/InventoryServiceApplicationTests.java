package com.admincore.microservice.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceApplicationTest {

	@Test
	void testMainMethod() {
		assertDoesNotThrow(() -> {
			String[] args = {};
			InventoryServiceApplication.main(args);
		});
	}
}
