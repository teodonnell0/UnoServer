package exceptions;

import com.teodonnell0.uno.Card;

public class IllegalCardException extends Exception {

	private static final long serialVersionUID = 1L;
	private final Card card;
	
	public IllegalCardException(Card card) {
		super(card.getColor() + " " + card.getRank() + " (" + card.getValue() + ")");
		this.card = card;
	}
	
	public Card getCard() {
		return card;
	}
}
