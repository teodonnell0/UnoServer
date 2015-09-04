package com.teodonnell0.uno;

import java.util.Random;
import java.util.Stack;

import com.teodonnell0.uno.Card;
import com.teodonnell0.uno.enums.CardColor;
import com.teodonnell0.uno.enums.CardRank;

public class Deck {

	private final int NUMBER_OF_DUPLICATE_REGULAR_CARDS = 2;
	private final int NUMBER_OF_DUPLICATE_ZERO_CARDS = 1;
	private final int NUMBER_OF_DUPLICATE_SPECIAL_CARDS = 2;
	private final int NUMBER_OF_WILD_CARDS = 4;
	private final int NUMBER_OF_WILD_DRAW_FOUR_CARDS = 4;
	private final int SHUFFLE_FACTOR = 10;
	
	private final Stack<Card> cards = new Stack<>();
	private final Stack<Card> discardedCards = new Stack<>();
	
	public Deck() {
		reset();
		initializeDeck();
		shuffle();
	}
	
	private void reset() {
		cards.clear();
		discardedCards.clear();
	}
	
	private void initializeDeck() {
		for(int i = 0; i < NUMBER_OF_DUPLICATE_ZERO_CARDS; i++) {
			for(CardColor c : CardColor.values()) {
				if(c != CardColor.NONE) {
					cards.push(new Card(c, CardRank.NUMBER, 0));
				}
			}
		}
		
		for(int i = 0; i < NUMBER_OF_DUPLICATE_REGULAR_CARDS; i++) {
			for(int j = 1; j <= 9; j++) {
				for(CardColor c : CardColor.values()) {
					if(c != CardColor.NONE) {
						cards.push(new Card(c, CardRank.NUMBER, j));
					}
				}
			}
		}
		
		for(int i = 0; i < NUMBER_OF_DUPLICATE_SPECIAL_CARDS; i++) {
			for(CardColor c : CardColor.values()) {
				if(c != CardColor.NONE) {
					cards.add(new Card(c, CardRank.SKIP));
					cards.add(new Card(c, CardRank.DRAW_TWO));
					cards.add(new Card(c, CardRank.REVERSE));
				}
			}
		}
		
		for(int i = 0; i < NUMBER_OF_WILD_CARDS; i++) {
			cards.add(new Card(CardColor.NONE, CardRank.WILD));
		}
		
		for(int i = 0; i < NUMBER_OF_WILD_DRAW_FOUR_CARDS; i++) {
			cards.add(new Card(CardColor.NONE, CardRank.WILD_DRAW_FOUR));
		}
	}
	
	public void shuffle() {
		Random random = new Random();
		for(int i = 0; i < SHUFFLE_FACTOR; i++) {
			for(int k = 0; k < cards.size(); k++) {
			int j = random.nextInt(cards.size());

			Card tempCard = cards.get(k);
			cards.set(k, cards.get(j));
			cards.set(j, tempCard);
			}
		}
	}
	
	private void remix() {
		Card upCard = discardedCards.pop(); // Top card currently is the 'upCard'. If it isn't popped now, it will be duplicated.
		cards.addAll(discardedCards);
		discardedCards.clear();
		discardedCards.push(upCard);
		System.out.println(cards.size());
		System.out.println(discardedCards.size());
		shuffle();
	}
	
	public boolean isDeckEmpty() {
		return cards.size() == 0;
	}
	
	public Card draw() {
		if(cards.size() == 0) {
			remix();
		}
		return cards.pop();
	}
	
	public void discard(Card card) {
		discardedCards.add(card);
	}
}
