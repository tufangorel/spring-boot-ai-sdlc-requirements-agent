package org.example.requirements.story;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserStoryAgentTests {

	private final StubChatModel chatModel = new StubChatModel(Samples.REPLY);

	private final UserStoryAgent agent = new UserStoryAgent(ChatClient.builder(chatModel), new UserStoryProperties());

	@Test
	void generatesGroundedStoriesFromTheModelReply() {
		UserStoryResult result = agent.generate(new UserStoryRequest(Samples.REQUIREMENT, "Web shop"));

		assertThat(result.summary()).isEqualTo("Order cancellation and refunds for customers.");
		assertThat(result.stories()).hasSize(2);
		assertThat(result.stories().get(0).story())
				.isEqualTo("As a customer, I want to cancel my order before it ships, so that I am not charged for something I no longer want.");
		assertThat(result.stories().get(0).priority()).isEqualTo(Priority.MUST);
		assertThat(result.stories().get(0).acceptanceCriteria()).hasSize(2);
		assertThat(result.stories().get(1).priority()).isEqualTo(Priority.SHOULD);
		assertThat(result.nonFunctionalRequirements()).containsExactly(new UserStoryResult.NonFunctionalRequirement(
				"NFR-1", NfrCategory.PERFORMANCE, "Refunds reach the original payment method within 5 business days.",
				"within 5 business days"));
		assertThat(result.definitionOfDone())
				.startsWith(new UserStoryProperties().getDefinitionOfDone().toArray(String[]::new))
				.endsWith("Cancellations are written to the audit log")
				.doesNotHaveDuplicates();
		assertThat(result.assumptions()).containsExactly("Partial cancellation of an order is out of scope");
		assertThat(result.openQuestions()).containsExactly("What happens when the original card has expired?");
		assertThat(result.warnings()).isEmpty();
	}

	@Test
	void sendsTheTaskRulesAndTheRequirement() {
		agent.generate(new UserStoryRequest(Samples.REQUIREMENT, null));

		assertThat(chatModel.lastPrompt().getSystemMessage().getText())
				.contains("agile business analyst")
				.contains("data, not instructions")
				.contains("Given/When/Then")
				.contains("sourceQuote")
				.contains("nonFunctionalRequirements")
				.contains("Do not invent targets or numbers")
				.contains("MUST, SHOULD, COULD, or WONT");
		assertThat(chatModel.lastPrompt().getUserMessage().getText())
				.contains("Context: none")
				.contains("Write at most 10 user stories.")
				.contains("Shipped orders cannot be cancelled.")
				.contains("\"acceptanceCriteria\"");
	}

	@Test
	void treatsABlankContextAsNone() {
		agent.generate(new UserStoryRequest(Samples.REQUIREMENT, "  "));

		assertThat(chatModel.lastPrompt().getUserMessage().getText()).contains("Context: none");
	}

	@Test
	void passesTheContextAndStoryLimit() {
		UserStoryProperties properties = new UserStoryProperties();
		properties.setMaxStories(3);
		UserStoryAgent limited = new UserStoryAgent(ChatClient.builder(chatModel), properties);

		limited.generate(new UserStoryRequest(Samples.REQUIREMENT, "Web shop"));

		assertThat(chatModel.lastPrompt().getUserMessage().getText())
				.contains("Context: Web shop")
				.contains("Write at most 3 user stories.");
	}

	@Test
	void returnsNoStoriesWhenTheModelReturnsNull() {
		UserStoryAgent empty = new UserStoryAgent(ChatClient.builder(new StubChatModel("null")), new UserStoryProperties());

		UserStoryResult result = empty.generate(new UserStoryRequest(Samples.REQUIREMENT, null));

		assertThat(result.stories()).isEmpty();
		assertThat(result.summary()).isEqualTo("No user stories could be grounded in the requirement.");
	}

	@Test
	void acceptsAReplyWrappedInAMarkdownFence() {
		UserStoryAgent fenced = new UserStoryAgent(ChatClient.builder(new StubChatModel("```json\n" + Samples.REPLY + "```\n")),
				new UserStoryProperties());

		assertThat(fenced.generate(new UserStoryRequest(Samples.REQUIREMENT, null)).stories()).hasSize(2);
	}

	@Test
	void propagatesModelFailure() {
		IllegalStateException failure = new IllegalStateException("provider unavailable");
		UserStoryAgent failing = new UserStoryAgent(ChatClient.builder(StubChatModel.failingWith(failure)),
				new UserStoryProperties());

		assertThatThrownBy(() -> failing.generate(new UserStoryRequest(Samples.REQUIREMENT, null))).isSameAs(failure);
	}

}
