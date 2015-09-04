package com.teodonnell0.uno;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.teodonnell0.uno.enums.CardColor;
import com.teodonnell0.uno.enums.CardRank;
import com.teodonnell0.uno.enums.Protocal;

import exceptions.GameAlreadyFullException;
import exceptions.GameAlreadyStartedException;

public class UnoGame {

	private static int matchIds = 0;

	private final int INITIAL_NUMBER_OF_CARDS = 7;

	private int MAXIMUM_NUMBER_OF_PLAYERS;

	private final int matchId;

	private final GameInfo gameInfo = new GameInfo();

	private final String SERVER_NAME = "SERVER";

	public enum Direction {
		FORWARD, REVERSE;
	}

	enum GameStatus {
		WAITING_FOR_PLAYERS,
		INITIALIZING_GAME,
		RUNNING,
		ENDED;
	}

	private Card upCard;

	private CardColor currentColor;

	private Deck deck;

	private Direction currentDirection;

	private GameStatus gameStatus;

	private CircularLinkedList<ConnectedClient> clientList;
	private ConnectedClient currentClient;

	private Set<ConnectedClient> unoers;
	private final Logger logger = Logger.getLogger(UnoGame.class);

	public UnoGame() {
		matchId = matchIds++;

		clientList = new CircularLinkedList<ConnectedClient>();
		gameStatus = GameStatus.WAITING_FOR_PLAYERS;
		unoers = Collections.synchronizedSet(new HashSet<ConnectedClient>());
	}

	public int getClientSize() {
		return clientList.size();
	}

	/**
	 * Initializes deck
	 * Distributes INITIAL_NUMBER_OF_CARDS to each player while updating playerList
	 * Send outgoing packets to each client containing client's current hand as well as the updated playerList
	 * Draws card off the top of the deck and points upCard to it
	 * If upCard is not WILD or WILD_DRAW_FOUR, then set currentColor to upCard's color. If upCard happens to be WILD or WILD_DRAW_FOUR, then the currentColor is selected based off of System.curentTimeInMilli%4
	 */
	public void initializeGame(int players) {
		MAXIMUM_NUMBER_OF_PLAYERS = players;
		gameStatus = GameStatus.INITIALIZING_GAME;

		logger.info(matchId + ": Initializing new game of Uno");
		deck = new Deck();
		logger.info(matchId + ": Deck initialized");
		logger.info(matchId + ": Distributing " + INITIAL_NUMBER_OF_CARDS + " cards to each players in game");
		for(int i = 0; i < INITIAL_NUMBER_OF_CARDS; i++) {
			for(int j = 0; j < MAXIMUM_NUMBER_OF_PLAYERS; j++) {
				Card card = deck.draw();
				clientList.get(j).getPlayer().getHand().addCard(card);
				gameInfo.updatePlayer(j, i+1);
			}
		}

		logger.info(matchId + ": Finished distributing cards to each player");
		logger.info(matchId + ": Sending players their initial hands");
		for(ConnectedClient client : clientList) {
			GameProtocal protocal = new GameProtocal(Protocal.HAND, client.getPlayer().getHand().clone());
			client.sendPacket(protocal);
		}



		currentDirection = Direction.FORWARD;

		upCard = deck.draw();
		gameInfo.setUpCard(upCard);
		currentColor = (upCard.getColor() != CardColor.NONE) ? upCard.getColor() : CardColor.values()[(int)(System.currentTimeMillis()%4L)];
		gameInfo.setCurrentColor(currentColor);
		currentClient = clientList.getFirst();
		gameInfo.setCurrentPlayerIndex(currentClient.getPlayer().getPlayerId());

		gameStatus = GameStatus.RUNNING;
		logger.info(matchId + ": Game status set to running");

		logger.info(matchId + ": Sending Game Info to each player");
		for(ConnectedClient client : clientList) {
			GameProtocal protocal = new GameProtocal(Protocal.GAME_INFO, gameInfo.clone());
			client.sendPacket(protocal);
		}
	}

	public synchronized boolean addPlayer(ConnectedClient client) throws GameAlreadyFullException, GameAlreadyStartedException {
		if(gameStatus != GameStatus.WAITING_FOR_PLAYERS) {
			throw new GameAlreadyStartedException(matchId, client);
		}

		if(clientList.size() > 4) {
			throw new GameAlreadyFullException(matchId, client);
		}

		Hand newHand = new Hand();
		client.getPlayer().setHand(newHand);
		client.getPlayer().setPlayerId(clientList.size());
		clientList.add(client);

		Integer i = clientList.size()-1;
		client.sendPacket(new GameProtocal(Protocal.PLAYER_ID, i));
		gameInfo.addPlayer(client.getPlayer().getPlayerId(), client.getPlayer().getName(), 0);

		return true;
	}

	public synchronized Protocal attemptMove(ConnectedClient client, Card card) {
		if(gameStatus != GameStatus.ENDED) {
			if(!currentClient.equals(client)) {
				return Protocal.INVALID_PLAYER;
			}

			if(unoers.size() > 0) {
				for(Object o : unoers.toArray()) {
					ConnectedClient c = (ConnectedClient) o;
					sendProtocalToAll(Protocal.CHAT, SERVER_NAME + " > " + c.getPlayer().getName() + " forgot to call UNO!");
					drawToClient(c);
					drawToClient(c);
					drawToClient(c);
					drawToClient(c);
					c.sendPacket(new GameProtocal(Protocal.HAND, c.getPlayer().getHand()));
					unoers.remove(c);
				}
			}

			//Player is drawing card
			if(card == null) {
				drawToCurrentClient();
				currentClient.sendPacket(new GameProtocal(Protocal.HAND, currentClient.getPlayer().getHand().clone()));
				getNextClient();
				sendProtocalToAll(Protocal.GAME_INFO, gameInfo.clone());
				return Protocal.VALID_MOVE;
			}

			if(client.getPlayer().getHand().contains(card)) {
				if(isPlayable(card)) {

					removeCardFromCurrentClient(card);

					if(client.getPlayer().getHand().size() == 1) {
						unoers.add(client);
					}
					setUpCard(card);
					if(upCard.getColor() != CardColor.NONE) {
						setColor(card.getColor());
					}

					switch(upCard.getRank()) {
					case NUMBER:
						getNextClient();
						break;
					case DRAW_TWO:
						getNextClient();
						drawToCurrentClient();
						drawToCurrentClient();
						break;
					case SKIP:
						getNextClient();
						getNextClient();
						break;
					case REVERSE:
						switchDirection();
						getNextClient();
						break;
					case WILD:
						setColor(currentClient.getPlayer().getHand().getBestColor());
						getNextClient();
						break;
					case WILD_DRAW_FOUR:
						setColor(currentClient.getPlayer().getHand().getBestColor());
						getNextClient();
						drawToCurrentClient();
						drawToCurrentClient();
						drawToCurrentClient();
						drawToCurrentClient();
						getNextClient();
						break;
					}

					checkWinner();
					for(ConnectedClient c : clientList) {
						c.sendPacket(new GameProtocal(Protocal.HAND,c.getPlayer().getHand().clone()));
						c.sendPacket(new GameProtocal(Protocal.GAME_INFO,gameInfo.clone()));
					}

					return Protocal.VALID_MOVE;
				} else {
					return Protocal.INVALID_MOVE;
				}
			}
			return Protocal.INVALID_MOVE;
		}
		return Protocal.INVALID_MOVE;
	}

	private void checkWinner() {
		for(int i = 0; i < MAXIMUM_NUMBER_OF_PLAYERS; i++) {
			if(gameInfo.getPlayerInfo(i).getHandSize() == 0) {
				sendProtocalToAll(Protocal.GAME_OVER, gameInfo.getPlayerInfo(i).getName());
				logger.info(matchId + ": " + gameInfo.getPlayerInfo(i).getName() + " has won the game");
				gameStatus = GameStatus.ENDED;
				return;
			}
		}
	}

	protected Integer getMatchId() {
		return matchId;
	}

	protected int getClientCount() {
		return clientList.size();
	}

	protected boolean isClientsTurn(ConnectedClient client) {
		return currentClient.equals(client);
	}

	private void sendProtocalToAll(Protocal protocal, Object object) {
		for(ConnectedClient client : clientList) {
			GameProtocal packet = new GameProtocal(protocal, object);
			client.sendPacket(packet);
		}
	}
	
	private void drawToCurrentClient() {
		Card card = deck.draw();
		currentClient.getPlayer().getHand().addCard(card);
		gameInfo.updatePlayer(clientList.indexOf(currentClient), currentClient.getPlayer().getHand().size());
		sendProtocalToAll(Protocal.CHAT, SERVER_NAME + " > " + currentClient.getPlayer().getName() + " drew a card from the deck");
	}

	private void drawToClient(ConnectedClient client) {
		Card card = deck.draw();
		client.getPlayer().getHand().addCard(card);
		gameInfo.updatePlayer(clientList.indexOf(client), client.getPlayer().getHand().size());
		sendProtocalToAll(Protocal.CHAT, SERVER_NAME + " > " + client.getPlayer().getName() + " drew a card from the deck");
	}

	private void removeCardFromCurrentClient(Card card) {
		if(currentClient.getPlayer().getHand().contains(card)) {
			currentClient.getPlayer().getHand().removeCard(card);
			deck.discard(card);
			gameInfo.updatePlayer(clientList.indexOf(currentClient), currentClient.getPlayer().getHand().size());
			sendProtocalToAll(Protocal.CHAT, SERVER_NAME + " > " + currentClient.getPlayer().getName() + " has played a " + (card.getColor() != CardColor.NONE ? card.getColor().toString() : "") + " " + (card.getRank() != CardRank.NUMBER ? card.getRank() : card.getValue()));
		}
	}

	private void switchDirection () {
		if(currentDirection == Direction.FORWARD)
			currentDirection = Direction.REVERSE;
		else
			currentDirection = Direction.FORWARD;
		sendProtocalToAll(Protocal.CHAT, SERVER_NAME + " > " + currentClient.getPlayer().getName() + " switched directions!");
	}

	private boolean isPlayable(Card card) {
		if(card.getColor() == currentColor) {
			return true;
		}

		if(card.getRank() == CardRank.NUMBER && card.getValue() == upCard.getValue()) {
			return true;
		}

		if(card.getRank() != CardRank.NUMBER && card.getRank() != CardRank.WILD && card.getRank() != CardRank.WILD_DRAW_FOUR && card.getRank() == upCard.getRank()) {
			return true;
		}

		if(card.getRank() == CardRank.WILD || card.getRank() == CardRank.WILD_DRAW_FOUR) {
			return true;
		}

		return false;
	}

	private void getNextClient() {
		if(currentDirection == Direction.FORWARD)
			currentClient = clientList.getNext();
		else
			currentClient = clientList.getPrevious(); 
		gameInfo.setCurrentPlayerIndex(clientList.indexOf(currentClient));
		sendProtocalToAll(Protocal.CHAT, SERVER_NAME + " > " + currentClient.getPlayer().getName() + "'s turn");

	}

	private void setColor(CardColor color) {
		currentColor = color;
		sendProtocalToAll(Protocal.CHAT, SERVER_NAME + " > The color has been changed to " + currentColor);
		gameInfo.setCurrentColor(color);
	}

	private void setUpCard(Card card) {
		upCard = card;
		gameInfo.setUpCard(card);
	}

	protected void callUno(ConnectedClient client) {
		if(unoers.contains(client)) {
			sendProtocalToAll(Protocal.CHAT, client.getPlayer().getName() + " > UNO!");
			sendProtocalToAll(Protocal.UNO, client.getPlayer().getClientNumber());
			unoers.remove(client);
		}
	}
}
