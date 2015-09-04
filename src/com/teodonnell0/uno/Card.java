package com.teodonnell0.uno;

import java.io.Serializable;

import com.teodonnell0.uno.enums.CardColor;
import com.teodonnell0.uno.enums.CardRank;

import exceptions.IllegalCardException;

public class Card implements Serializable, Cloneable {
	private static final long serialVersionUID = 7615448073963590407L;
	private final CardColor color;
	private final CardRank rank;
	private final int value;
	
	public Card(CardColor color, CardRank rank, int value) {
		this.color = color;
		this.rank = rank;
		this.value = value;
	}
	
	public Card clone() {
		return new Card(color, rank, value);
	}

	public Card(CardColor color, CardRank rank) {
		this(color, rank, -1);
	}
	
	public final CardColor getColor() {
		return color;
	}

	public final CardRank getRank() {
		return rank;
	}

	public final int getValue() {
		return value;
	}
	
	public int getForfeitCost() throws IllegalCardException {
		switch(rank) {
		case NUMBER:
			return value;
		case SKIP:
		case REVERSE:
		case DRAW_TWO:
			return 20;
		case WILD:
		case WILD_DRAW_FOUR:
			return 50;
		default:
			throw new IllegalCardException(this);
		}
	}
	
	public boolean followedByCall() {
		return rank == CardRank.WILD || rank == CardRank.WILD_DRAW_FOUR;
	}
	
	public boolean canPlayOn(Card card, CardColor currentColor) {
		if((rank == CardRank.NUMBER && card.getRank() == CardRank.NUMBER && value == card.getValue()) 
			|| rank != CardRank.NUMBER && rank == card.getRank()
			|| rank == CardRank.WILD
			|| rank == CardRank.WILD_DRAW_FOUR
			|| color == currentColor) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		String retval = "";
		switch (color) {
		case RED:
			retval += "Red_";
			break;
		case YELLOW:
			retval += "Yellow_";
			break;
		case GREEN:
			retval += "Green_";
			break;
		case BLUE:
			retval += "Blue_";
			break;
		case NONE:
			retval += "";
			break;
		}
		switch (rank) {
		case NUMBER:
			
			switch(value) {
			case 0:
				retval += "Zero";
				break;
			case 1:
				retval += "One";
				break;
			case 2:
				retval += "Two";
				break;
			case 3:
				retval += "Three";
				break;
			case 4:
				retval += "Four";
				break;
			case 5:
				retval += "Five";
				break;
			case 6:
				retval += "Six";
				break;
			case 7:
				retval += "Seven";
				break;
			case 8:
				retval += "Eight";
				break;
			case 9:
				retval += "Nine";
				break;
			}
			break;
		case SKIP:
			retval += "Skip";
			break;
		case REVERSE:
			retval += "Reverse";
			break;
		case WILD:
			retval += "Wild";
			break;
		case DRAW_TWO:
			retval += "Draw_Two";
			break;
		case WILD_DRAW_FOUR:
			retval += "Wild_Draw_Four";
			break;
		}
		return retval;
	}
}	

