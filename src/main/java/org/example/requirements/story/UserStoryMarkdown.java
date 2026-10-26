package org.example.requirements.story;

import java.util.List;

import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.example.requirements.story.UserStoryResult.UserStory;

final class UserStoryMarkdown {

	private UserStoryMarkdown() {
	}

	static String render(UserStoryResult result) {
		StringBuilder md = new StringBuilder("# User stories\n\n").append(result.summary()).append("\n");
		for (UserStory story : result.stories()) {
			md.append("\n## ").append(story.id()).append(": ").append(story.title()).append("\n\n");
			md.append(story.story()).append("\n\n");
			md.append("- **Priority:** ").append(story.priority() == null ? "Not set" : story.priority()).append("\n");
			md.append("- **Source:** \"").append(story.sourceQuote()).append("\"\n\n");
			md.append("### Acceptance criteria\n\n");
			for (AcceptanceCriterion criterion : story.acceptanceCriteria()) {
				md.append("- **").append(criterion.id()).append("** Given ").append(criterion.given())
						.append(", when ").append(criterion.when())
						.append(", then ").append(criterion.then()).append(".\n");
			}
		}
		section(md, "Requirement text not covered by a story", result.uncoveredRequirementText(), "- ");
		section(md, "Definition of done", result.definitionOfDone(), "- [ ] ");
		section(md, "Assumptions", result.assumptions(), "- ");
		section(md, "Open questions", result.openQuestions(), "- ");
		section(md, "Warnings", result.warnings(), "- ");
		return md.toString();
	}

	private static void section(StringBuilder md, String heading, List<String> items, String bullet) {
		if (items.isEmpty()) {
			return;
		}
		md.append("\n## ").append(heading).append("\n\n");
		for (String item : items) {
			md.append(bullet).append(item).append("\n");
		}
	}

}
