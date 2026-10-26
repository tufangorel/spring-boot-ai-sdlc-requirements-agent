package org.example.requirements;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
@AutoConfigureMockMvc
class ApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void exposesHealthAndInfoOnly() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/actuator"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$._links.health").exists())
				.andExpect(jsonPath("$._links.info").exists())
				.andExpect(jsonPath("$._links.env").doesNotExist())
				.andExpect(jsonPath("$._links.beans").doesNotExist());
	}

	@Test
	void documentsTheUserStoryEndpoints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Spring Boot AI Requirements Agent"))
				.andExpect(jsonPath("$.paths['/api/user-stories'].post.summary").value("Generate user stories"))
				.andExpect(jsonPath("$.paths['/api/user-stories'].post.tags[0]").value("User stories"))
				.andExpect(jsonPath("$.paths['/api/user-stories/markdown'].post.summary").value("Generate user stories as Markdown"));
	}

	@Test
	void mainStartsWithEitherProvider() {
		assertThatNoException().isThrownBy(() -> SpringBootAiSdlcRequirementsAgentApplication.main(new String[] {
				"--spring.main.web-application-type=none", "--spring.ai.openai.api-key=test-key" }));
		assertThatNoException().isThrownBy(() -> SpringBootAiSdlcRequirementsAgentApplication.main(new String[] {
				"--spring.main.web-application-type=none", "--AI_PROVIDER=anthropic",
				"--spring.ai.anthropic.api-key=test-key" }));
	}

}
