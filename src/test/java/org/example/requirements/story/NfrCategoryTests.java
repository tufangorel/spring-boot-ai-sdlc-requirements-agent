package org.example.requirements.story;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NfrCategoryTests {

	@ParameterizedTest
	@CsvSource({
			"PERFORMANCE, PERFORMANCE",
			"performance, PERFORMANCE",
			"Security & privacy, SECURITY",
			"availability, AVAILABILITY",
			"Scalability, SCALABILITY",
			"compliance (GDPR), COMPLIANCE",
			"usability, USABILITY",
			"Maintainability, MAINTAINABILITY",
			"other, OTHER" })
	void readsCategoriesLeniently(String value, NfrCategory expected) {
		assertThat(NfrCategory.fromJson(value)).isEqualTo(expected);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "latency", "  " })
	void treatsUnknownValuesAsOther(String value) {
		assertThat(NfrCategory.fromJson(value)).isEqualTo(NfrCategory.OTHER);
	}

}
