package org.example.requirements.story;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RequirementSubmission {

	private final UserStoryProperties properties;

	public RequirementSubmission(UserStoryProperties properties) {
		this.properties = properties;
	}

	public UserStoryRequest prepare(UserStoryRequest request) {
		if (request.requirement() == null || request.requirement().isBlank()) {
			throw badRequest("Requirement is required");
		}
		String requirement = request.requirement().trim();
		if (requirement.length() > properties.getMaxRequirementCharacters()) {
			throw badRequest("Requirement exceeds " + properties.getMaxRequirementCharacters() + " characters");
		}
		String context = request.context() == null || request.context().isBlank() ? null : request.context().trim();
		if (context != null && context.length() > properties.getMaxContextCharacters()) {
			throw badRequest("Context exceeds " + properties.getMaxContextCharacters() + " characters");
		}
		return new UserStoryRequest(requirement, context);
	}

	private static ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

}
