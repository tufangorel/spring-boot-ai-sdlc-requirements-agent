package org.example.requirements.story;

import java.util.List;

import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.example.requirements.story.UserStoryResult.UserStory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserStoryMarkdownTests {

	@Test
	void rendersStoriesCriteriaAndChecklists() {
		UserStoryResult result = new UserStoryResult("Account locking.",
				List.of(new UserStory("US-1", "Lock an account", "As an admin, I want to lock an account, so that misuse stops.",
						"admin", "to lock an account", "misuse stops", Priority.MUST, "Admins can lock a user account.",
						List.of(new AcceptanceCriterion("AC-1.1", "an active user", "the admin locks the account",
								"the user is signed out")))),
				List.of("Locks expire after 24 hours."), List.of("Code is peer reviewed"), List.of("Only admins lock accounts"),
				List.of("How long does a lock last?"), List.of("Dropped story \"Delete\": its source quote does not appear in the requirement."));

		assertThat(UserStoryMarkdown.render(result)).isEqualTo("""
				# User stories

				Account locking.

				## US-1: Lock an account

				As an admin, I want to lock an account, so that misuse stops.

				- **Priority:** MUST
				- **Source:** "Admins can lock a user account."

				### Acceptance criteria

				- **AC-1.1** Given an active user, when the admin locks the account, then the user is signed out.

				## Requirement text not covered by a story

				- Locks expire after 24 hours.

				## Definition of done

				- [ ] Code is peer reviewed

				## Assumptions

				- Only admins lock accounts

				## Open questions

				- How long does a lock last?

				## Warnings

				- Dropped story "Delete": its source quote does not appear in the requirement.
				""");
	}

	@Test
	void skipsEmptySectionsAndShowsAMissingPriority() {
		UserStoryResult result = new UserStoryResult("One story.",
				List.of(new UserStory("US-1", "Reset", "As a user, I want to reset my password, so that I can sign in.",
						"user", "to reset my password", "I can sign in", null, "Users can reset a password.",
						List.of(new AcceptanceCriterion("AC-1.1", "a user", "they reset", "a link is sent")))),
				List.of(), List.of(), List.of(), List.of(), List.of());

		assertThat(UserStoryMarkdown.render(result))
				.contains("- **Priority:** Not set")
				.doesNotContain("## Requirement text not covered by a story")
				.doesNotContain("## Definition of done")
				.doesNotContain("## Assumptions")
				.doesNotContain("## Open questions")
				.doesNotContain("## Warnings");
	}

}
