package com.teodonnell0.uno;

import java.io.Serializable;


public class Player implements Serializable, Cloneable {


	private static final long serialVersionUID = -1362259592047758373L;
	
	private Integer clientNumber;
	
	private Integer matchNumber;
	
	private String name;
	
	private Integer playerId;
	
	private boolean playing;
	
	private Hand hand;
	
	public Player() {
		this.clientNumber = -1;
		this.name = null;
		this.playerId = -1;
		playing = false;
		matchNumber = new Integer(-1);
		hand = new Hand();
	}
	
	public Player clone() {
		Player player = new Player();

		int cn = this.getClientNumber();
		int pi = this.playerId;
		player.setPlaying(this.isPlaying());
		player.setClientNumber(cn);
		player.setPlayerId(pi);
		player.setName(this.getName());
		player.setHand(this.hand.clone());
		player.setMatchNumber(this.getMatchNumber());
		return player;
	}

	
	public void setClientNumber(Integer clientNumber) {
		this.clientNumber = clientNumber;
	}

	public void setPlayerId(Integer playerId) {
		this.playerId = playerId;
	}
	
	public final Integer getClientNumber() {
		return clientNumber;
	}


	public final Integer getMatchNumber() {
		return matchNumber;
	}

	public final String getName() {
		return name;
	}

	public final Integer getPlayerId() {
		return playerId;
	}

	public final boolean isPlaying() {
		return playing;
	}

	public final void setMatchNumber(Integer matchNumber) {
		this.matchNumber = matchNumber;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final void setPlaying(boolean playing) {
		this.playing = playing;
	}


	public final Hand getHand() {
		return hand;
	}


	public final void setHand(Hand hand) {
		this.hand = hand;
	}
	
	
	
	

}
