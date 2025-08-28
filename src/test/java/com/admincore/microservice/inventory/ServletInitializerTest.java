package com.admincore.microservice.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServletInitializerTest {

    @Test
    void testConfigure() {
        ServletInitializer initializer = new ServletInitializer();
        SpringApplicationBuilder builder = mock(SpringApplicationBuilder.class);
        SpringApplicationBuilder expectedBuilder = mock(SpringApplicationBuilder.class);

        when(builder.sources(InventoryServiceApplication.class)).thenReturn(expectedBuilder);

        SpringApplicationBuilder result = initializer.configure(builder);

        verify(builder).sources(InventoryServiceApplication.class);
        assertSame(expectedBuilder, result);
    }
}
