package com.teodonnell0.uno;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.teodonnell0.uno.enums.Protocal;

import exceptions.GameAlreadyFullException;
import exceptions.GameAlreadyStartedException;


public class UnoServer implements Cloneable, Runnable {

	private static Queue<ConnectedClient> lobby = new LinkedList<>();
	private static List<ConnectedClient> clients = new ArrayList<>();
	private static Map<Integer, UnoGame> games = new HashMap<>();

	private static boolean acceptingConnections;
	private final static Logger logger = Logger.getLogger(UnoServer.class);

	private static long startTime = System.nanoTime();
	
	public static UnoGame getUnoGame(ConnectedClient client) {

		Integer matchId = client.getPlayer().getMatchNumber();
		
		if(games.containsKey(matchId)) {
			return games.get(matchId);
		}
		return null;
	}
	
	public static boolean addToLobby(ConnectedClient client) {
		if(lobby.size()==0)
			startTime = System.nanoTime();
		return lobby.add(client);
	}
	
	public static boolean removeFromServer(ConnectedClient client) {
		return lobby.remove(client) || clients.remove(client);
	}

	public static void lobbyCheck() {
		if((System.nanoTime()-startTime)/1_000_000_000 > 60) {

			if(lobby.size() > 1) {
				UnoGame game = new UnoGame();
				int matchId = game.getMatchId();
				while(lobby.size() != 0 && game.getClientSize() <= 4) {
					ConnectedClient client = lobby.poll();
					if(client.getPlayer().getName() == null) {
						logger.fatal("Client doesnt have a name");
						lobby.add(client);
						continue;
					}
					try {
						game.addPlayer(client);
						logger.info(matchId + ": added client#"+client.getClientId()+" ("+client.getPlayer().getName()+")");
					} catch (GameAlreadyFullException | GameAlreadyStartedException e) {
						e.printStackTrace();
						logger.warn("Match:"+matchId + " was already full when server tried to add a client:" + client.getClientId());
						lobby.add(client);
						logger.warn("Client" + client.getClientId() + " has been added back into the lobby's queue");
					}
					client.setState(ClientState.CONNECTED);
					client.getPlayer().setMatchNumber(matchId);
					client.getPlayer().setPlaying(true);
					client.sendPacket(new GameProtocal(Protocal.FOUND_MATCH, matchId));
					
					if(!games.containsKey(matchId)) {
						games.put(matchId, game);
					}
				}
				game.initializeGame(game.getClientSize());
				startTime = System.nanoTime();
				return;
			} else {
				return;
			}
		}
		if(lobby.size() >= 4) {
			logger.info("Server has at least 4 players in queue. Setting up a new game");
			UnoGame game = new UnoGame();

			int matchId = game.getMatchId();
			logger.info("MatchId will be " + matchId);
			while(game.getClientSize() != 4) {
				ConnectedClient client = lobby.poll();
				if(client.getPlayer().getName() == null) {
					logger.fatal("Client doesnt have a name");
					lobby.add(client);
					continue;
				}
				try {
					game.addPlayer(client);
					logger.info(matchId + ": added client#"+client.getClientId()+" ("+client.getPlayer().getName()+")");
				} catch (GameAlreadyFullException | GameAlreadyStartedException e) {
					e.printStackTrace();
					logger.warn("Match:"+matchId + " was already full when server tried to add a client:" + client.getClientId());
					lobby.add(client);
					logger.warn("Client" + client.getClientId() + " has been added back into the lobby's queue");
				}
				client.setState(ClientState.CONNECTED);
				client.getPlayer().setMatchNumber(matchId);
				client.getPlayer().setPlaying(true);
				client.sendPacket(new GameProtocal(Protocal.FOUND_MATCH, matchId));
				
				if(!games.containsKey(matchId)) {
					games.put(matchId, game);
				}
			}
			game.initializeGame(4);
			startTime = System.nanoTime();
		}
	}
	
	private static int DEFAULT_PORT = 49817;
	public static void main(String...strings) {
		UnoServer unoServer = new UnoServer();
		Thread thread = new Thread(unoServer);
		thread.start();
		

		Logger logger = Logger.getLogger("Main");
		acceptingConnections = true;
		
		ServerSocket server = null;
		logger.info("Attempting to open server socket on port:" + DEFAULT_PORT);
		try {
			server = new ServerSocket(DEFAULT_PORT);
		} catch (IOException e) {
			acceptingConnections = false;
			e.printStackTrace();
			logger.fatal("Port:" + DEFAULT_PORT + " is already in use. Exiting.");
		}
		
		logger.info("Server socket opened successfully");
		
		while(acceptingConnections) {
			try {
				Socket connection = server.accept();
				logger.info("Obtained connection from " + connection.getInetAddress().toString() + ":" + connection.getPort());
				ConnectedClient client = new ConnectedClient(connection, unoServer);
				lobby.add(client);
				clients.add(client);
			} catch (IOException e) {
				e.printStackTrace();
				logger.warn("Something went wrong while setting up new ConnectedClient.class");
			}
		}
	}

	@Override
	public void run() {
		while(true) {

		lobbyCheck();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}

}
