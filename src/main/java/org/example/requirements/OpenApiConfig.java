package org.example.requirements;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI requirementsOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Spring Boot AI Requirements Agent")
				.description("Submit a user requirement and get back user stories, acceptance criteria, and a definition of done.")
				.version("0.0.1"));
	}

}
