package org.example.requirements.story;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Priority {

	MUST, SHOULD, COULD, WONT;

	@JsonCreator
	static Priority fromJson(String value) {
		if (value == null) {
			return null;
		}
		String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
		for (Priority priority : values()) {
			if (letters.startsWith(priority.name())) {
				return priority;
			}
		}
		return null;
	}

}
