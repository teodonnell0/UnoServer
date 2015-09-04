package com.teodonnell0.uno;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.teodonnell0.uno.enums.CardColor;

public class GameInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = -7504311551180798872L;
	
	private final List<PlayerInfo> infoList;
	
	private CardColor currentColor;
	
	private Card upCard;
	
	private Integer currentPlayerIndex;
	
	public GameInfo() {
		infoList = new ArrayList<>();
		currentPlayerIndex = 0;
	}
	
	public void addPlayer(Integer playerId, String name, Integer handSize) {
		infoList.add(new PlayerInfo(playerId, name, handSize));
	}
	
	public void updatePlayer(Integer index, Integer handSize) {
		infoList.get(index).setHandSize(handSize);
	}
	
	public void updateCurrentPlayerIndex(int index) {
		this.currentPlayerIndex = index;
	}
	
	public Integer getCurrentPlayerIndex() {
		return currentPlayerIndex;
	}
	
	public List<PlayerInfo> getInfoList() {
		return infoList;
	}	
	
	public CardColor getCurrentColor() {
		return currentColor;
	}

	public void setCurrentColor(CardColor currentColor) {
		this.currentColor = currentColor;
	}

	public Card getUpCard() {
		return upCard;
	}

	public void setUpCard(Card upCard) {
		this.upCard = upCard;
	}

	public void setCurrentPlayerIndex(Integer currentPlayerIndex) {
		this.currentPlayerIndex = currentPlayerIndex;
	}

	public PlayerInfo getPlayerInfo(Integer index) {
		return infoList.get(index);
	}

	public GameInfo clone() {
		GameInfo info = new GameInfo();
		List<PlayerInfo> list = new ArrayList<>();
		for(int i = 0; i < infoList.size(); i++) {
			Integer hs = infoList.get(i).getHandSize().intValue();
			String n = infoList.get(i).getName();
			Integer id = infoList.get(i).getPlayerId().intValue();
			info.addPlayer(id, n, hs);
		}
		info.currentPlayerIndex = this.currentPlayerIndex.intValue();
		info.currentColor = this.currentColor;
		info.upCard = this.upCard.clone();
		return info;
	}

	public class PlayerInfo implements Serializable {
		private static final long serialVersionUID = 6426766421467819256L;
		private Integer playerId;
		private String name;
		private Integer handSize;
		
		PlayerInfo(Integer playerId, String name, Integer handSize) {
			this.playerId = playerId;
			this.name = name;
			this.handSize = handSize;
		}
		public String getName() {
			return name;
		}
		
		public Integer getHandSize() {
			return handSize;
		}
		
		public void setHandSize(Integer handSize) {
			this.handSize = handSize;
		}
		
		public Integer getPlayerId() {
			return playerId;
		}
	}
}
