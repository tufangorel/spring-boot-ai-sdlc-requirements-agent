package org.example.requirements.story;

import java.util.Arrays;
import java.util.List;

import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.example.requirements.story.UserStoryResult.UserStory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserStoryGherkinTests {

	@Test
	void rendersARulePerStoryAndAScenarioPerCriterion() {
		UserStoryResult result = new UserStoryResult("Account locking.",
				List.of(new UserStory("US-1", "Lock an account", "As an admin, I want to lock an account, so that misuse stops.",
						"admin", "to lock an account", "misuse stops", Priority.MUST, "Admins can lock a user account.",
						List.of(new AcceptanceCriterion("AC-1.1", "an active user", "the admin locks the account",
								"the user is signed out"),
								new AcceptanceCriterion("AC-1.2", "a locked user", "they sign in", "access is denied"))),
						new UserStory("US-2", "Explain the lock", "As a user, I want to see why I am locked, so that I can ask for help.",
								"user", "to see why I am locked", "I can ask for help", null, "Locked users cannot sign in.",
								List.of(new AcceptanceCriterion("AC-2.1", "a locked user", "they sign in",
										"a lock notice is shown")))),
				List.of(), List.of(), List.of(), List.of(), List.of());

		assertThat(UserStoryGherkin.render(result)).isEqualTo("""
				Feature: Account locking.

				  Rule: US-1: Lock an account
				    As an admin, I want to lock an account, so that misuse stops.

				    @US-1 @AC-1.1 @MUST
				    Scenario: AC-1.1: the admin locks the account
				      Given an active user
				      When the admin locks the account
				      Then the user is signed out

				    @US-1 @AC-1.2 @MUST
				    Scenario: AC-1.2: they sign in
				      Given a locked user
				      When they sign in
				      Then access is denied

				  Rule: US-2: Explain the lock
				    As a user, I want to see why I am locked, so that I can ask for help.

				    @US-2 @AC-2.1
				    Scenario: AC-2.1: they sign in
				      Given a locked user
				      When they sign in
				      Then a lock notice is shown
				""");
	}

	@Test
	void flattensEditedTextAndSkipsMissingParts() {
		UserStoryResult result = new UserStoryResult(null,
				Arrays.asList(null, new UserStory(null, "Lock\nan account", null, null, null, null, null, null,
						Arrays.asList(null, new AcceptanceCriterion(null, "an active\n  user", "the admin locks it", "it is locked"),
								new AcceptanceCriterion("AC-1.2", "x", " ", "y")))),
				null, null, null, null, null);

		assertThat(UserStoryGherkin.render(result)).isEqualTo("""
				Feature: User stories

				  Rule: Lock an account

				    Scenario: the admin locks it
				      Given an active user
				      When the admin locks it
				      Then it is locked
				""");
	}

}
