package org.example.requirements.story;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "requirements")
public class UserStoryProperties {

	private int maxRequirementCharacters = 20_000;

	private int maxContextCharacters = 4_000;

	private int maxStories = 10;

	private List<String> definitionOfDone = new ArrayList<>(List.of(
			"Code is peer reviewed and merged to the main branch",
			"Every acceptance criterion is covered by an automated test that passes in CI",
			"No open critical or major defects remain for the story",
			"User-facing and API documentation is updated",
			"The product owner has accepted the story"));

	public int getMaxRequirementCharacters() {
		return maxRequirementCharacters;
	}

	public void setMaxRequirementCharacters(int maxRequirementCharacters) {
		this.maxRequirementCharacters = maxRequirementCharacters;
	}

	public int getMaxContextCharacters() {
		return maxContextCharacters;
	}

	public void setMaxContextCharacters(int maxContextCharacters) {
		this.maxContextCharacters = maxContextCharacters;
	}

	public int getMaxStories() {
		return maxStories;
	}

	public void setMaxStories(int maxStories) {
		this.maxStories = maxStories;
	}

	public List<String> getDefinitionOfDone() {
		return definitionOfDone;
	}

	public void setDefinitionOfDone(List<String> definitionOfDone) {
		this.definitionOfDone = definitionOfDone;
	}

}
