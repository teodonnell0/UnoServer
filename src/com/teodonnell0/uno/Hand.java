package com.teodonnell0.uno;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.teodonnell0.uno.enums.CardColor;

public class Hand implements Serializable, Cloneable {

	private static final long serialVersionUID = -3599560180216695546L;
	private List<Card> cards = new ArrayList<>();

	public Hand clone() {
		Hand hand = new Hand();
		for(int i = 0; i < this.cards.size(); i++) {
			hand.addCard(this.cards.get(i).clone());
		}
		return hand;
		
	}
	
	public void addCard(Card card) {
		cards.add(card);
	}

	public void removeCard(Card card) {
		cards.remove(card);
	}

	public Card get(int index) {
		if(index >= 0 && index < cards.size()) {
			return cards.get(index);
		}
		return null;
	}

	public boolean contains(Card card) {
		return cards.contains(card);
	}

	public final List<Card> getCards() {
		return cards;
	}

	public int size() {
		return cards.size();
	}

	public CardColor getBestColor() {
		int r = 0, y = 0, b = 0, g = 0;

		Iterator<Card> cardIterator = cards.iterator();
		while(cardIterator.hasNext()) {
			Card card = cardIterator.next();

			switch(card.getColor()) {
			case RED:
				r++;
				break;
			case YELLOW:
				y++;
				break;
			case BLUE:
				b++;
				break;
			case GREEN:
				g++;
				break;
			default:
				break;
			}
		}

		List<CardColor> maximums = new LinkedList<>();
		int max = 0;
		for(int i  = 0; i < 4; i++) {
			if(r >= max) {
				if(r > max) {
					maximums.clear();
					maximums.add(CardColor.RED);
					max = r;
				}
			}
			
			if(b >= max) {
				if(b > max) {
					maximums.clear();
					maximums.add(CardColor.BLUE);
					max = b;
				}
			}
			
			if(g >= max) {
				if(g > max) {
					maximums.clear();
					maximums.add(CardColor.GREEN);
					max = g;
				}
			}
			
			if(y >= max) {
				if(y > max) {
					maximums.clear();
					maximums.add(CardColor.YELLOW);
					max = y;
				}
			}
		}
		
		if(maximums.size() > 1) {
			Random random = new Random();
			return maximums.get(random.nextInt(maximums.size()));
		}
		
		if(maximums.size() > 0)
			return maximums.get(0);
		return CardColor.RED;
	}
}
