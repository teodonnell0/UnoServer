package exceptions;

import com.teodonnell0.uno.ConnectedClient;

public class GameAlreadyFullException extends Exception {

	private static final long serialVersionUID = 7174088011838758542L;
	private ConnectedClient client;
	private Integer matchId;
	public GameAlreadyFullException(Integer matchId, ConnectedClient client) {
		super("Attempted to add " + client.getPlayer().getName() + " to matchId:" + matchId + ", but game is already full");
		this.client = client;
		this.matchId = matchId;
	}
	public final ConnectedClient getClient() {
		return client;
	}
	public final Integer getMatchId() {
		return matchId;
	}

	
	
	
	
	
}
