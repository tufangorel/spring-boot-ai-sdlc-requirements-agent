package org.example.requirements.story;

final class Samples {

	static final String REQUIREMENT = """
			Customers must be able to cancel an order before it ships.
			The refund goes back to the original payment method within 5 business days.
			Shipped orders cannot be cancelled.
			""";

	static final String REPLY = """
			{
			  "summary": "Order cancellation and refunds for customers.",
			  "stories": [
			    {
			      "title": "Cancel an unshipped order",
			      "role": "customer",
			      "goal": "to cancel my order before it ships",
			      "benefit": "I am not charged for something I no longer want",
			      "priority": "Must have",
			      "sourceQuote": "Customers must be able to cancel an order before it ships.",
			      "acceptanceCriteria": [
			        {"given": "an order that has not shipped", "when": "the customer cancels it", "then": "the order status is Cancelled"},
			        {"given": "an order that has shipped", "when": "the customer tries to cancel it", "then": "the request is refused with a message that shipped orders cannot be cancelled"}
			      ]
			    },
			    {
			      "title": "Refund to the original payment method",
			      "role": "customer",
			      "goal": "to get my refund on the card I paid with",
			      "benefit": "I do not have to claim the money another way",
			      "priority": "SHOULD",
			      "sourceQuote": "The refund goes back to the original payment method within 5 business days.",
			      "acceptanceCriteria": [
			        {"given": "a cancelled order paid by card", "when": "the refund is issued", "then": "the amount is credited to that card within 5 business days"}
			      ]
			    }
			  ],
			  "nonFunctionalRequirements": [
			    {"category": "Performance", "statement": "Refunds reach the original payment method within 5 business days.", "sourceQuote": "within 5 business days"}
			  ],
			  "definitionOfDone": ["Cancellations are written to the audit log", "Code is peer reviewed and merged to the main branch"],
			  "assumptions": ["Partial cancellation of an order is out of scope"],
			  "openQuestions": ["What happens when the original card has expired?"]
			}
			""";

	private Samples() {
	}

}
