package org.example.requirements.story;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The model's raw reply. {@link StoryGrounding} checks it before anything reaches the caller.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StoryDraft(
		String summary,
		List<Story> stories,
		List<Nfr> nonFunctionalRequirements,
		List<String> definitionOfDone,
		List<String> assumptions,
		List<String> openQuestions) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Story(
			String title,
			String role,
			String goal,
			String benefit,
			Priority priority,
			String sourceQuote,
			List<Criterion> acceptanceCriteria) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Criterion(String given, String when, String then) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Nfr(NfrCategory category, String statement, String sourceQuote) {
	}

}
