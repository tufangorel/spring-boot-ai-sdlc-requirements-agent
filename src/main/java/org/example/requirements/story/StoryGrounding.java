package org.example.requirements.story;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.example.requirements.story.StoryDraft.Criterion;
import org.example.requirements.story.StoryDraft.Story;
import org.example.requirements.story.UserStoryResult.AcceptanceCriterion;
import org.example.requirements.story.UserStoryResult.UserStory;

final class StoryGrounding {

	private static final Pattern ROLE_PREFIX = Pattern.compile("(?i)^as\\s+(an?|the)\\s+");

	private static final Pattern GOAL_PREFIX = Pattern.compile("(?i)^i\\s+want\\s+");

	private static final Pattern BENEFIT_PREFIX = Pattern.compile("(?i)^so\\s+that\\s+");

	private static final Pattern CLAUSE_PREFIX = Pattern.compile("(?i)^(given|when|then)\\s+");

	private StoryGrounding() {
	}

	static UserStoryResult ground(String requirement, StoryDraft draft, List<String> baselineDefinitionOfDone,
			int maxStories) {
		StoryDraft reply = draft == null ? new StoryDraft(null, null, null, null, null) : draft;
		String haystack = normalize(requirement);
		List<String> warnings = new ArrayList<>();
		Map<String, UserStory> kept = new LinkedHashMap<>();
		int overLimit = 0;
		for (Story story : nullSafe(reply.stories())) {
			if (story == null) {
				continue;
			}
			String role = clause(story.role(), ROLE_PREFIX);
			String goal = clause(story.goal(), GOAL_PREFIX);
			String benefit = clause(story.benefit(), BENEFIT_PREFIX);
			String title = hasText(story.title()) ? story.title().trim() : goal;
			String label = hasText(title) ? title : "untitled";
			if (!hasText(role) || !hasText(goal) || !hasText(benefit)) {
				warnings.add("Dropped story \"" + label + "\": role, goal, and benefit are all required.");
				continue;
			}
			String quote = story.sourceQuote() == null ? "" : story.sourceQuote().trim();
			if (quote.isEmpty() || !haystack.contains(normalize(quote))) {
				warnings.add("Dropped story \"" + label + "\": its source quote does not appear in the requirement.");
				continue;
			}
			List<Criterion> criteria = nullSafe(story.acceptanceCriteria()).stream()
					.filter(criterion -> criterion != null && hasText(criterion.given()) && hasText(criterion.when())
							&& hasText(criterion.then()))
					.toList();
			if (criteria.isEmpty()) {
				warnings.add("Dropped story \"" + label + "\": it has no complete Given/When/Then acceptance criterion.");
				continue;
			}
			String key = normalize(role) + "|" + normalize(goal);
			if (kept.containsKey(key)) {
				warnings.add("Dropped story \"" + label + "\": it duplicates an earlier story.");
				continue;
			}
			if (kept.size() >= maxStories) {
				overLimit++;
				continue;
			}
			String id = "US-" + (kept.size() + 1);
			List<AcceptanceCriterion> numbered = new ArrayList<>();
			for (Criterion criterion : criteria) {
				numbered.add(new AcceptanceCriterion(id.replace("US-", "AC-") + "." + (numbered.size() + 1),
						clause(criterion.given(), CLAUSE_PREFIX), clause(criterion.when(), CLAUSE_PREFIX),
						clause(criterion.then(), CLAUSE_PREFIX)));
			}
			kept.put(key, new UserStory(id, title, sentence(role, goal, benefit), role, goal, benefit,
					story.priority(), quote, List.copyOf(numbered)));
		}
		if (overLimit > 0) {
			warnings.add("Dropped " + overLimit + " more " + (overLimit == 1 ? "story" : "stories")
					+ " over the limit of " + maxStories + ".");
		}
		List<UserStory> stories = List.copyOf(kept.values());
		String summary;
		if (stories.isEmpty()) {
			summary = "No user stories could be grounded in the requirement.";
		}
		else if (hasText(reply.summary())) {
			summary = reply.summary().trim();
		}
		else {
			summary = "Generated " + stories.size() + " user " + (stories.size() == 1 ? "story" : "stories")
					+ " from the requirement.";
		}
		List<String> definitionOfDone = new ArrayList<>(baselineDefinitionOfDone);
		definitionOfDone.addAll(nullSafe(reply.definitionOfDone()));
		return new UserStoryResult(summary, stories, distinct(definitionOfDone), distinct(reply.assumptions()),
				distinct(reply.openQuestions()), List.copyOf(warnings));
	}

	private static String sentence(String role, String goal, String benefit) {
		return "As " + article(role) + " " + role + ", I want " + goal + ", so that " + benefit + ".";
	}

	private static String article(String word) {
		String lower = word.toLowerCase(Locale.ROOT);
		if (lower.startsWith("us") || lower.startsWith("uni")) {
			return "a";
		}
		return "aeiou".indexOf(lower.charAt(0)) >= 0 ? "an" : "a";
	}

	private static String clause(String raw, Pattern prefix) {
		if (raw == null) {
			return "";
		}
		String text = prefix.matcher(raw.trim()).replaceFirst("").trim();
		while (!text.isEmpty() && ".,;".indexOf(text.charAt(text.length() - 1)) >= 0) {
			text = text.substring(0, text.length() - 1).trim();
		}
		return text;
	}

	private static List<String> distinct(List<String> items) {
		Set<String> seen = new LinkedHashSet<>();
		List<String> result = new ArrayList<>();
		for (String item : nullSafe(items)) {
			if (hasText(item) && seen.add(normalize(item))) {
				result.add(item.trim());
			}
		}
		return List.copyOf(result);
	}

	static String normalize(String text) {
		return text.replace('\u2018', '\'').replace('\u2019', '\'').replace('\u201C', '"').replace('\u201D', '"')
				.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
	}

	private static boolean hasText(String text) {
		return text != null && !text.isBlank();
	}

	private static <T> List<T> nullSafe(List<T> items) {
		return items == null ? List.of() : items;
	}

}
