package org.example.requirements.story;

import java.util.List;

public record UserStoryResult(
		String summary,
		List<UserStory> stories,
		List<String> definitionOfDone,
		List<String> assumptions,
		List<String> openQuestions,
		List<String> warnings) {

	public record UserStory(
			String id,
			String title,
			String story,
			String role,
			String goal,
			String benefit,
			Priority priority,
			String sourceQuote,
			List<AcceptanceCriterion> acceptanceCriteria) {
	}

	public record AcceptanceCriterion(String id, String given, String when, String then) {
	}

}
