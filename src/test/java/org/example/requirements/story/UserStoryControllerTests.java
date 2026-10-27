package org.example.requirements.story;

import java.util.List;

import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.example.requirements.story.UserStoryResult.UserStory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserStoryController.class)
class UserStoryControllerTests {

	private static final UserStoryRequest REQUEST = new UserStoryRequest("Users can reset a password.", "Portal");

	private static final UserStoryResult RESULT = new UserStoryResult("Password reset.",
			List.of(new UserStory("US-1", "Reset a password", "As a user, I want to reset my password, so that I can sign in.",
					"user", "to reset my password", "I can sign in", Priority.MUST, "Users can reset a password.",
					List.of(new AcceptanceCriterion("AC-1.1", "a registered user", "they ask for a reset",
							"a reset link is emailed")))),
			List.of("Links expire after one hour."), List.of("Code is peer reviewed"), List.of(),
			List.of("How long is the link valid?"), List.of());

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserStoryAgent agent;

	@MockitoBean
	private RequirementSubmission submission;

	@Test
	void returnsStoriesAsJson() throws Exception {
		given(submission.prepare(REQUEST)).willReturn(REQUEST);
		given(agent.generate(REQUEST)).willReturn(RESULT);

		mockMvc.perform(post("/api/user-stories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"requirement":"Users can reset a password.","context":"Portal"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stories[0].id").value("US-1"))
				.andExpect(jsonPath("$.stories[0].story").value("As a user, I want to reset my password, so that I can sign in."))
				.andExpect(jsonPath("$.stories[0].priority").value("MUST"))
				.andExpect(jsonPath("$.stories[0].acceptanceCriteria[0].then").value("a reset link is emailed"))
				.andExpect(jsonPath("$.uncoveredRequirementText[0]").value("Links expire after one hour."))
				.andExpect(jsonPath("$.definitionOfDone[0]").value("Code is peer reviewed"))
				.andExpect(jsonPath("$.openQuestions[0]").value("How long is the link valid?"));
	}

	@Test
	void returnsStoriesAsMarkdown() throws Exception {
		given(submission.prepare(REQUEST)).willReturn(REQUEST);
		given(agent.generate(REQUEST)).willReturn(RESULT);

		mockMvc.perform(post("/api/user-stories/markdown")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"requirement":"Users can reset a password.","context":"Portal"}
								"""))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/markdown"))
				.andExpect(content().string(containsString("## US-1: Reset a password")))
				.andExpect(content().string(containsString("- [ ] Code is peer reviewed")));
	}

	@Test
	void exportsAReviewedResultAsGherkinWithoutCallingTheAgent() throws Exception {
		mockMvc.perform(post("/api/user-stories/gherkin")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"summary":"Password reset.","stories":[{"id":"US-1","title":"Reset a password",
								"priority":"MUST","acceptanceCriteria":[{"id":"AC-1.1","given":"a user’s account",
								"when":"they ask for a reset","then":"a reset link is emailed"}]}]}
								"""))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/x-gherkin"))
				.andExpect(content().string(containsString("Rule: US-1: Reset a password")))
				.andExpect(content().string(containsString("@US-1 @AC-1.1 @MUST")))
				.andExpect(content().string(containsString("Given a user’s account")));

		verifyNoInteractions(agent, submission);
	}

	@Test
	void exportsAMinimalBody() throws Exception {
		mockMvc.perform(post("/api/user-stories/gherkin")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"stories":[{"id":"US-1","title":"Cancel an unshipped order","acceptanceCriteria":[
								{"id":"AC-1.1","given":"an order that has not shipped","when":"the customer cancels it",
								"then":"the order status is Cancelled"}]}]}
								"""))
				.andExpect(status().isOk())
				.andExpect(content().string("""
						Feature: User stories

						  Rule: US-1: Cancel an unshipped order

						    @US-1 @AC-1.1
						    Scenario: AC-1.1: the customer cancels it
						      Given an order that has not shipped
						      When the customer cancels it
						      Then the order status is Cancelled
						"""));
	}

	@Test
	void rejectsAGherkinExportWithoutStories() throws Exception {
		for (String body : new String[] { "{\"summary\":\"Nothing\",\"stories\":[]}", "{\"summary\":\"Nothing\"}",
				"{\"stories\":" }) {
			mockMvc.perform(post("/api/user-stories/gherkin")
							.contentType(MediaType.APPLICATION_JSON)
							.content(body))
					.andExpect(status().isBadRequest());
		}

		verifyNoInteractions(agent, submission);
	}

	@Test
	void rejectsABlankRequirement() throws Exception {
		mockMvc.perform(post("/api/user-stories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"requirement":"  "}
								"""))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(agent, submission);
	}

}
