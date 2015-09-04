package exceptions;

import com.teodonnell0.uno.ConnectedClient;

public class GameAlreadyStartedException extends Exception {

	private static final long serialVersionUID = 1251191684723447579L;
	private ConnectedClient client;
	private Integer matchId;
	
	public GameAlreadyStartedException(Integer matchId, ConnectedClient client) {
		super(client.getPlayer().getName() + " attempted joining matchId:" + matchId +", but game has already started");
		this.matchId = matchId;
		this.client = client;
	}

	public final ConnectedClient getClient() {
		return client;
	}

	public final Integer getMatchId() {
		return matchId;
	}
	
	
}
