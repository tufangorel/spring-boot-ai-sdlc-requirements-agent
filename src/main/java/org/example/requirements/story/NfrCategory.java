package org.example.requirements.story;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NfrCategory {

	PERFORMANCE, SECURITY, AVAILABILITY, SCALABILITY, COMPLIANCE, USABILITY, MAINTAINABILITY, OTHER;

	@JsonCreator
	static NfrCategory fromJson(String value) {
		if (value == null) {
			return OTHER;
		}
		String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
		for (NfrCategory category : values()) {
			if (letters.startsWith(category.name())) {
				return category;
			}
		}
		return OTHER;
	}

}
