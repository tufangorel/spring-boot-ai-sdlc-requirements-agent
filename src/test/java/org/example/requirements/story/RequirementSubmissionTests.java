package org.example.requirements.story;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequirementSubmissionTests {

	private final RequirementSubmission submission = new RequirementSubmission(new UserStoryProperties());

	@Test
	void trimsTheRequirementAndContext() {
		UserStoryRequest prepared = submission.prepare(new UserStoryRequest("  Users can reset a password.  ", "  Portal  "));

		assertThat(prepared).isEqualTo(new UserStoryRequest("Users can reset a password.", "Portal"));
	}

	@Test
	void treatsABlankContextAsMissing() {
		assertThat(submission.prepare(new UserStoryRequest("Users can reset a password.", "  ")).context()).isNull();
		assertThat(submission.prepare(new UserStoryRequest("Users can reset a password.", null)).context()).isNull();
	}

	@Test
	void rejectsAMissingRequirement() {
		assertThatThrownBy(() -> submission.prepare(new UserStoryRequest(null, null)))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Requirement is required")
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.BAD_REQUEST);
		assertThatThrownBy(() -> submission.prepare(new UserStoryRequest("   ", null)))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Requirement is required");
	}

	@Test
	void rejectsARequirementOrContextOverTheLimits() {
		UserStoryProperties properties = new UserStoryProperties();
		properties.setMaxRequirementCharacters(10);
		properties.setMaxContextCharacters(5);
		RequirementSubmission limited = new RequirementSubmission(properties);

		assertThatThrownBy(() -> limited.prepare(new UserStoryRequest("Users can reset a password.", null)))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Requirement exceeds 10 characters");
		assertThatThrownBy(() -> limited.prepare(new UserStoryRequest("Reset it.", "Customer portal")))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Context exceeds 5 characters");
	}

}
