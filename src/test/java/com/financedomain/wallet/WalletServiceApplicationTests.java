package com.financedomain.wallet;

import com.financedomain.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
		"server.port=8083",
		"wallet-service.uriport=8083",
		"wallet-service.datasource-url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"wallet-service.username=sa",
		"wallet-service.password=",
		"wallet-service.hibernate-ddl-auto=create-drop",
		"wallet-service.showsql=true",
		"wallet-service.urlregistry=http://localhost:8761/eureka",
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false"
})
class WalletServiceApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private WalletService walletService;

	@Test
	@DisplayName("Vérifie le chargement du contexte Spring Boot et des beans pour wallet-service")
	void contextLoads() {
		assertNotNull(applicationContext, "Le contexte Spring Boot du wallet-service doit s'initialiser correctement.");
		assertThat(walletService).isNotNull();
	}

}
