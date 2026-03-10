package com.vivek.jobportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"spring.config.import=",
		"spring.datasource.url=jdbc:h2:mem:jobportal;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.flyway.enabled=false",
		"jwt.secret=test-secret-key-for-jwt-signing-123456789"
})
@ActiveProfiles("test")
class JobPortalApplicationTests {

	@Test
	void contextLoads() {
	}

}
