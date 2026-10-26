package org.example.requirements.story;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-stories")
@Tag(name = "User stories", description = "Turn a user requirement into user stories, acceptance criteria, and a definition of done")
public class UserStoryController {

	static final String TEXT_MARKDOWN = "text/markdown";

	private final UserStoryAgent agent;

	private final RequirementSubmission submission;

	public UserStoryController(UserStoryAgent agent, RequirementSubmission submission) {
		this.agent = agent;
		this.submission = submission;
	}

	@Operation(summary = "Generate user stories",
			description = "Returns user stories with Given/When/Then acceptance criteria, a definition of done, assumptions, and open questions as JSON.")
	@PostMapping
	public UserStoryResult generate(@Valid @RequestBody UserStoryRequest request) {
		return agent.generate(submission.prepare(request));
	}

	@Operation(summary = "Generate user stories as Markdown",
			description = "Returns the same result as Markdown that can be pasted into a backlog tool or a wiki.")
	@PostMapping(path = "/markdown", produces = TEXT_MARKDOWN)
	public String generateMarkdown(@Valid @RequestBody UserStoryRequest request) {
		return UserStoryMarkdown.render(generate(request));
	}

}
