package com.teodonnell0.uno;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.teodonnell0.uno.enums.Protocal;

public class ConnectedClient implements Runnable {

	private static int clients = 0;
	
	private final int clientId;
	
	private volatile boolean connected;
	
	private Socket connection = null;
	
	private final Logger logger = Logger.getLogger(ConnectedClient.class);
	
	private ObjectInputStream objectInputStream = null;
	
	private ObjectOutputStream objectOutputStream = null;
	
	private Player player;

	private Thread thread;
	
	public ConnectedClient(Socket incomingConnection, UnoServer server) {
		clientId = clients++;
		player = new Player();
		player.setClientNumber(clientId);
		this.connection = incomingConnection;
		try {
			OutputStream outputStream = connection.getOutputStream();
			objectOutputStream = new ObjectOutputStream(outputStream);
			
			InputStream inputStream = connection.getInputStream();
			objectInputStream = new ObjectInputStream(inputStream);
			
			connected = connection.isConnected();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		thread = new Thread(this);
		thread.start();
	}
	
	public Integer getClientId() {
		return clientId;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isConnected() {
		connected = connection.isConnected();
		return connected;
	}

	@Override
	public void run() {
		UnoGame game = null;
		while(isConnected()) {
			try {
				GameProtocal incoming = (GameProtocal) objectInputStream.readObject();
				GameProtocal outgoing;
				switch(incoming.getProtocal()) {
				case ACCOUNT:
					String playerName = (String) incoming.getMessage();
					player = new Player();
					player.setName(playerName);
					player.setClientNumber(clientId);
					outgoing = new GameProtocal(Protocal.ACCOUNT, player.clone());
					sendPacket(outgoing);
					logger.info("Client#:"+clientId+" has created a player named " + player.getName());
					UnoServer.lobbyCheck();
					break;
				case MOVE:
					game = UnoServer.getUnoGame(this);
					if(game != null) {
						Integer cardIndex = (Integer) incoming.getMessage();
						Card card = null;
						
						if(cardIndex == -2) { //Player is drawing from deck 
							card = null;
						}
						
						if(cardIndex >= 0 && cardIndex < getPlayer().getHand().size()) {
							card = getPlayer().getHand().get(cardIndex);
						}
						
						if(game.isClientsTurn(this)) {
							Protocal protocal = game.attemptMove(this, card);
							sendPacket(new GameProtocal(protocal));
						}
					}
					break;
				case UNO:
					game.callUno(this);
					break;
				default:
					break;
					
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.warn(player.getName() + " disconnected");
				setState(ClientState.QUIT);
				UnoServer.removeFromServer(this);
				return;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void sendPacket(GameProtocal packet) {
		try {
			objectOutputStream.writeObject(packet);
			objectOutputStream.reset();
		} catch (IOException e) {
			System.err.println("Message failed due to client disconnect");
			connected = false;
		}
	}

	public void setState(ClientState state) {
	}
	
}
