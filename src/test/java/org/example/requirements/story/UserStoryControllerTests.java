package org.example.requirements.story;

import java.util.List;

import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.example.requirements.story.UserStoryResult.NonFunctionalRequirement;
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
			List.of(new NonFunctionalRequirement("NFR-1", NfrCategory.SECURITY, "Reset links expire after one hour.",
					"expire after one hour")),
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
				.andExpect(jsonPath("$.nonFunctionalRequirements[0].id").value("NFR-1"))
				.andExpect(jsonPath("$.nonFunctionalRequirements[0].category").value("SECURITY"))
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
	void exportsTheReadmeExampleResponse() throws Exception {
		mockMvc.perform(post("/api/user-stories/gherkin")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "summary": "Order cancellation and refunds for customers.",
								  "stories": [
								    {
								      "id": "US-1",
								      "title": "Cancel an unshipped order",
								      "story": "As a customer, I want to cancel my order before it ships, so that I am not charged for something I no longer want.",
								      "role": "customer",
								      "goal": "to cancel my order before it ships",
								      "benefit": "I am not charged for something I no longer want",
								      "priority": "MUST",
								      "sourceQuote": "Customers must be able to cancel an order before it ships.",
								      "acceptanceCriteria": [
								        { "id": "AC-1.1", "given": "an order that has not shipped", "when": "the customer cancels it", "then": "the order status is Cancelled" },
								        { "id": "AC-1.2", "given": "an order that has shipped", "when": "the customer tries to cancel it", "then": "the request is refused with a message that shipped orders cannot be cancelled" }
								      ]
								    }
								  ],
								  "nonFunctionalRequirements": [
								    {
								      "id": "NFR-1",
								      "category": "PERFORMANCE",
								      "statement": "Refunds reach the original payment method within 5 business days.",
								      "sourceQuote": "within 5 business days"
								    }
								  ],
								  "uncoveredRequirementText": ["Shipped orders cannot be cancelled."],
								  "definitionOfDone": ["Cancellations are written to the audit log"],
								  "assumptions": ["Partial cancellation of an order is out of scope"],
								  "openQuestions": ["What happens when the original card has expired?"],
								  "warnings": []
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(content().string("""
						Feature: Order cancellation and refunds for customers.

						  Rule: US-1: Cancel an unshipped order
						    As a customer, I want to cancel my order before it ships, so that I am not charged for something I no longer want.

						    @US-1 @AC-1.1 @MUST
						    Scenario: AC-1.1: the customer cancels it
						      Given an order that has not shipped
						      When the customer cancels it
						      Then the order status is Cancelled

						    @US-1 @AC-1.2 @MUST
						    Scenario: AC-1.2: the customer tries to cancel it
						      Given an order that has shipped
						      When the customer tries to cancel it
						      Then the request is refused with a message that shipped orders cannot be cancelled
						"""));
	}

	@Test
	void exportsTheReadmeEditedInputSample() throws Exception {
		mockMvc.perform(post("/api/user-stories/gherkin")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "summary": "Order cancellation and refunds",
								  "stories": [
								    {
								      "id": "US-1",
								      "title": "Cancel an unshipped order",
								      "priority": "MUST",
								      "acceptanceCriteria": [
								        { "id": "AC-1.1", "given": "an order that\\nhas not shipped", "when": "the customer cancels it", "then": "the order status is Cancelled" },
								        { "id": "AC-1.2", "given": "an order that has shipped", "when": "the customer tries to cancel it", "then": "" }
								      ]
								    },
								    {
								      "id": "US 2",
								      "title": "Refund to the original payment method",
								      "acceptanceCriteria": [
								        { "id": "AC-2.1", "given": "a cancelled order paid by card", "when": "the refund is issued", "then": "the amount is credited to that card within 5 business days" }
								      ]
								    },
								    {
								      "id": "US-3",
								      "title": "Explain why a shipped order cannot be cancelled"
								    }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(content().string("""
						Feature: Order cancellation and refunds

						  Rule: US-1: Cancel an unshipped order

						    @US-1 @AC-1.1 @MUST
						    Scenario: AC-1.1: the customer cancels it
						      Given an order that has not shipped
						      When the customer cancels it
						      Then the order status is Cancelled

						  Rule: US 2: Refund to the original payment method

						    @US2 @AC-2.1
						    Scenario: AC-2.1: the refund is issued
						      Given a cancelled order paid by card
						      When the refund is issued
						      Then the amount is credited to that card within 5 business days

						  Rule: US-3: Explain why a shipped order cannot be cancelled
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
