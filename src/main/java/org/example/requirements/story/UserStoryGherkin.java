package org.example.requirements.story;

import java.util.List;

import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.example.requirements.story.UserStoryResult.UserStory;

/**
 * Renders a result as one Gherkin feature file: a {@code Rule} per story and a {@code Scenario} per acceptance
 * criterion. Gherkin is line based, so every value is flattened to a single line.
 */
final class UserStoryGherkin {

	private UserStoryGherkin() {
	}

	static String render(UserStoryResult result) {
		String summary = line(result.summary());
		StringBuilder feature = new StringBuilder("Feature: ").append(summary.isEmpty() ? "User stories" : summary)
				.append("\n");
		for (UserStory story : nullSafe(result.stories())) {
			if (story == null) {
				continue;
			}
			String id = tag(story.id());
			feature.append("\n  Rule: ").append(join(line(story.id()), line(story.title()))).append("\n");
			if (!line(story.story()).isEmpty()) {
				feature.append("    ").append(line(story.story())).append("\n");
			}
			for (AcceptanceCriterion criterion : nullSafe(story.acceptanceCriteria())) {
				if (criterion == null || line(criterion.given()).isEmpty() || line(criterion.when()).isEmpty()
						|| line(criterion.then()).isEmpty()) {
					continue;
				}
				String tags = tags(id, tag(criterion.id()), story.priority() == null ? "" : story.priority().name());
				feature.append("\n");
				if (!tags.isEmpty()) {
					feature.append("    ").append(tags).append("\n");
				}
				feature.append("    Scenario: ").append(join(line(criterion.id()), line(criterion.when()))).append("\n");
				feature.append("      Given ").append(line(criterion.given())).append("\n");
				feature.append("      When ").append(line(criterion.when())).append("\n");
				feature.append("      Then ").append(line(criterion.then())).append("\n");
			}
		}
		return feature.toString();
	}

	private static String tags(String... names) {
		StringBuilder tags = new StringBuilder();
		for (String name : names) {
			if (!name.isEmpty()) {
				tags.append(tags.isEmpty() ? "@" : " @").append(name);
			}
		}
		return tags.toString();
	}

	private static String join(String id, String text) {
		if (id.isEmpty()) {
			return text;
		}
		return text.isEmpty() ? id : id + ": " + text;
	}

	private static String tag(String text) {
		return text == null ? "" : text.replaceAll("[\\s@]+", "");
	}

	private static String line(String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").trim();
	}

	private static <T> List<T> nullSafe(List<T> items) {
		return items == null ? List.of() : items;
	}

}
