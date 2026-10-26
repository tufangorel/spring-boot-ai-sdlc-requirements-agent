package org.example.requirements.story;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.example.requirements.story.StoryDraft.Criterion;
import org.example.requirements.story.StoryDraft.Story;
import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoryGroundingTests {

	private static final String REQUIREMENT = "Admins can “lock” a user account.\nLocked users cannot sign in.";

	private static final List<String> BASELINE = List.of("Code is peer reviewed", "Tests pass in CI");

	private static final Criterion CRITERION = new Criterion("a locked user", "they sign in", "access is denied");

	@Test
	void buildsTheStorySentenceAndNumbersStoriesAndCriteria() {
		UserStoryResult result = ground(draft(
				new Story("Lock an account", "As an admin", "I want to lock a user account.", "So that misuse stops.",
						Priority.MUST, "admins can \"lock\"   a user account",
						List.of(new Criterion("Given an active user", "When the admin locks the account",
								"Then the user is signed out."), CRITERION)),
				story("Block locked users", "user", "to be told my account is locked", "I know why I cannot sign in",
						"Locked users cannot sign in.")));

		assertThat(result.stories()).hasSize(2);
		assertThat(result.stories().get(0)).satisfies(story -> {
			assertThat(story.id()).isEqualTo("US-1");
			assertThat(story.role()).isEqualTo("admin");
			assertThat(story.goal()).isEqualTo("to lock a user account");
			assertThat(story.benefit()).isEqualTo("misuse stops");
			assertThat(story.story()).isEqualTo("As an admin, I want to lock a user account, so that misuse stops.");
			assertThat(story.priority()).isEqualTo(Priority.MUST);
			assertThat(story.acceptanceCriteria()).containsExactly(
					new AcceptanceCriterion("AC-1.1", "an active user", "the admin locks the account", "the user is signed out"),
					new AcceptanceCriterion("AC-1.2", "a locked user", "they sign in", "access is denied"));
		});
		assertThat(result.stories().get(1).id()).isEqualTo("US-2");
		assertThat(result.stories().get(1).story())
				.isEqualTo("As a user, I want to be told my account is locked, so that I know why I cannot sign in.");
		assertThat(result.stories().get(1).acceptanceCriteria()).extracting(AcceptanceCriterion::id).containsExactly("AC-2.1");
		assertThat(result.warnings()).isEmpty();
	}

	@Test
	void choosesTheArticleFromHowTheRoleSounds() {
		UserStoryResult result = ground(draft(
				story("A", "unit manager", "to lock accounts", "misuse stops", "Admins can"),
				story("B", "engineer", "to unlock accounts", "users get back in", "Admins can"),
				story("C", "unverified visitor", "to see a lock notice", "I know what happened", "Locked users")));

		assertThat(result.stories()).extracting(UserStoryResult.UserStory::story).containsExactly(
				"As a unit manager, I want to lock accounts, so that misuse stops.",
				"As an engineer, I want to unlock accounts, so that users get back in.",
				"As an unverified visitor, I want to see a lock notice, so that I know what happened.");
	}

	@Test
	void dropsStoriesThatAreIncompleteUngroundedUntestableOrDuplicated() {
		UserStoryResult result = ground(draft(
				null,
				story("No role", " ", "to lock accounts", "misuse stops", "Admins can"),
				story("No goal", "admin", null, "misuse stops", "Admins can"),
				story(null, "admin", "to lock accounts", " ", "Admins can"),
				story(null, " ", null, null, "Admins can"),
				story("Invented", "admin", "to delete accounts", "storage is freed", "Admins can delete a user account."),
				story("No quote", "admin", "to unlock accounts", "users get back in", null),
				new Story("No criteria", "admin", "to lock accounts", "misuse stops", null, "Admins can", null),
				new Story("Broken criteria", "admin", "to lock accounts", "misuse stops", null, "Admins can",
						Arrays.asList(null, new Criterion(" ", "x", "y"), new Criterion("x", null, "y"),
								new Criterion("x", "y", ""))),
				story("Kept", "admin", "to lock accounts", "misuse stops", "Admins can"),
				story("Same goal", "Admin", "To  lock accounts", "a different benefit", "Locked users")));

		assertThat(result.stories()).extracting(UserStoryResult.UserStory::title).containsExactly("Kept");
		assertThat(result.warnings()).containsExactly(
				"Dropped story \"No role\": role, goal, and benefit are all required.",
				"Dropped story \"No goal\": role, goal, and benefit are all required.",
				"Dropped story \"to lock accounts\": role, goal, and benefit are all required.",
				"Dropped story \"untitled\": role, goal, and benefit are all required.",
				"Dropped story \"Invented\": its source quote does not appear in the requirement.",
				"Dropped story \"No quote\": its source quote does not appear in the requirement.",
				"Dropped story \"No criteria\": it has no complete Given/When/Then acceptance criterion.",
				"Dropped story \"Broken criteria\": it has no complete Given/When/Then acceptance criterion.",
				"Dropped story \"Same goal\": it duplicates an earlier story.");
	}

	@Test
	void keepsAtMostTheConfiguredNumberOfStories() {
		List<Story> stories = new ArrayList<>();
		for (int i = 1; i <= 4; i++) {
			stories.add(story("Story " + i, "admin", "goal " + i, "benefit", "Admins can"));
		}

		UserStoryResult one = StoryGrounding.ground(REQUIREMENT, draft(stories.subList(0, 2).toArray(Story[]::new)),
				BASELINE, 1);
		UserStoryResult two = StoryGrounding.ground(REQUIREMENT, draft(stories.toArray(Story[]::new)), BASELINE, 2);

		assertThat(one.stories()).hasSize(1);
		assertThat(one.warnings()).containsExactly("Dropped 1 more story over the limit of 1.");
		assertThat(two.stories()).extracting(UserStoryResult.UserStory::id).containsExactly("US-1", "US-2");
		assertThat(two.warnings()).containsExactly("Dropped 2 more stories over the limit of 2.");
	}

	@Test
	void mergesTheBaselineDefinitionOfDoneAndCleansTheOtherLists() {
		UserStoryResult result = ground(new StoryDraft("  Account locking.  ",
				List.of(story("Lock", "admin", "to lock accounts", "misuse stops", "Admins can")),
				Arrays.asList("code is PEER reviewed", " Locking is audited ", null, " "),
				Arrays.asList("Only admins lock accounts", "only admins  lock accounts", null),
				List.of("  How long does a lock last?  ")));

		assertThat(result.summary()).isEqualTo("Account locking.");
		assertThat(result.definitionOfDone()).containsExactly("Code is peer reviewed", "Tests pass in CI", "Locking is audited");
		assertThat(result.assumptions()).containsExactly("Only admins lock accounts");
		assertThat(result.openQuestions()).containsExactly("How long does a lock last?");
	}

	@Test
	void writesASummaryWhenTheModelGivesNoneOrNothingIsGrounded() {
		assertThat(ground(draft(story("Lock", "admin", "to lock accounts", "misuse stops", "Admins can"))).summary())
				.isEqualTo("Generated 1 user story from the requirement.");
		assertThat(ground(draft(story("Lock", "admin", "to lock accounts", "misuse stops", "Admins can"),
				story("Deny", "user", "to see why", "I can ask for help", "Locked users"))).summary())
				.isEqualTo("Generated 2 user stories from the requirement.");
		assertThat(ground(new StoryDraft("The model claims stories", List.of(), null, null, null)).summary())
				.isEqualTo("No user stories could be grounded in the requirement.");
	}

	@Test
	void listsRequirementSentencesThatNoStoryQuotes() {
		assertThat(ground(draft(story("Lock", "admin", "to lock accounts", "misuse stops", "Admins can")))
				.uncoveredRequirementText()).containsExactly("Locked users cannot sign in.");

		String requirement = """
				Admins can lock accounts. Locks last one day!

				---
				Locked users see a notice? Locked users see a notice?
				Admins get an email""";
		UserStoryResult result = StoryGrounding.ground(requirement,
				draft(story("Lock", "admin", "to lock accounts", "misuse stops", "accounts.  Locks last")), BASELINE, 10);

		assertThat(result.uncoveredRequirementText()).containsExactly("Admins can lock accounts.", "Locks last one day!",
				"Locked users see a notice?", "Admins get an email");

		UserStoryResult spanning = StoryGrounding.ground(requirement,
				draft(story("Lock", "admin", "to lock accounts", "misuse stops", "Admins can lock accounts. Locks last one day!")),
				BASELINE, 10);

		assertThat(spanning.uncoveredRequirementText()).containsExactly("Locked users see a notice?", "Admins get an email");
	}

	@Test
	void handlesANullReply() {
		UserStoryResult result = ground(null);

		assertThat(result.uncoveredRequirementText())
				.containsExactly("Admins can “lock” a user account.", "Locked users cannot sign in.");
		assertThat(result.stories()).isEmpty();
		assertThat(result.definitionOfDone()).isEqualTo(BASELINE);
		assertThat(result.assumptions()).isEmpty();
		assertThat(result.openQuestions()).isEmpty();
		assertThat(result.warnings()).isEmpty();
	}

	private static UserStoryResult ground(StoryDraft draft) {
		return StoryGrounding.ground(REQUIREMENT, draft, BASELINE, 10);
	}

	private static StoryDraft draft(Story... stories) {
		return new StoryDraft(null, Arrays.asList(stories), null, null, null);
	}

	private static Story story(String title, String role, String goal, String benefit, String quote) {
		return new Story(title, role, goal, benefit, Priority.SHOULD, quote, List.of(CRITERION));
	}

}
