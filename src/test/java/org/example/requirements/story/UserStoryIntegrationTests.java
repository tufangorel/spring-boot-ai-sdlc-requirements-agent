package org.example.requirements.story;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "spring.ai.openai.api-key=test-key", "requirements.max-requirement-characters=500" })
@AutoConfigureMockMvc
class UserStoryIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StubChatModel chatModel;

	@BeforeEach
	void clearPrompts() {
		chatModel.prompts().clear();
	}

	@Test
	void generatesStoriesEndToEnd() throws Exception {
		mockMvc.perform(post("/api/user-stories")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body(Samples.REQUIREMENT)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stories", hasSize(2)))
				.andExpect(jsonPath("$.stories[0].id").value("US-1"))
				.andExpect(jsonPath("$.stories[0].acceptanceCriteria[1].id").value("AC-1.2"))
				.andExpect(jsonPath("$.uncoveredRequirementText", hasSize(1)))
				.andExpect(jsonPath("$.uncoveredRequirementText[0]").value("Shipped orders cannot be cancelled."))
				.andExpect(jsonPath("$.definitionOfDone[0]").value("Code is peer reviewed and merged to the main branch"))
				.andExpect(jsonPath("$.definitionOfDone[5]").value("Cancellations are written to the audit log"))
				.andExpect(jsonPath("$.warnings", hasSize(0)));

		assertThat(chatModel.lastPrompt().getUserMessage().getText())
				.contains("Context: Web shop")
				.contains("Shipped orders cannot be cancelled.");
	}

	@Test
	void generatesMarkdownEndToEnd() throws Exception {
		mockMvc.perform(post("/api/user-stories/markdown")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body(Samples.REQUIREMENT)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("## US-2: Refund to the original payment method")))
				.andExpect(content().string(containsString("## Open questions")));
	}

	@Test
	void rejectsAnOversizedRequirementWithoutCallingTheModel() throws Exception {
		mockMvc.perform(post("/api/user-stories")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body("x".repeat(501))))
				.andExpect(status().isBadRequest());

		assertThat(chatModel.prompts()).isEmpty();
	}

	private static String body(String requirement) {
		return "{\"requirement\":\"" + requirement.replace("\n", "\\n") + "\",\"context\":\"Web shop\"}";
	}

	@TestConfiguration
	static class StubChatModelConfig {

		@Bean
		@Primary
		StubChatModel stubChatModel() {
			return new StubChatModel(Samples.REPLY);
		}

	}

}
