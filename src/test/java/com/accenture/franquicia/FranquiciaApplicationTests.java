package com.accenture.franquicia;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Test de integración: requiere Docker con MySQL y LocalStack corriendo")
class FranquiciaApplicationTests {

	@Test
	void contextLoads() {
	}
}
