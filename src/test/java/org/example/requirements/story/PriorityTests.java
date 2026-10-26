package org.example.requirements.story;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityTests {

	@ParameterizedTest
	@CsvSource({
			"MUST, MUST",
			"must have, MUST",
			"Must-have, MUST",
			"should, SHOULD",
			"Could have, COULD",
			"WONT, WONT",
			"Won't have, WONT" })
	void readsMoscowValuesLeniently(String value, Priority expected) {
		assertThat(Priority.fromJson(value)).isEqualTo(expected);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "high", "P1", "  " })
	void treatsUnknownValuesAsMissing(String value) {
		assertThat(Priority.fromJson(value)).isNull();
	}

}
