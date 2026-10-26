package org.example.requirements.story;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserStoryRequest(
		@Schema(description = "The user requirement in plain language", example = "Customers must be able to cancel an order before it ships and get a refund to the original payment method.")
		@NotBlank String requirement,
		@Schema(description = "Optional product or domain context, such as the system, users, or constraints", example = "Web shop for a mid-size retailer")
		String context) {
}
