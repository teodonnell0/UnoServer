package com.teodonnell0.uno;
import java.io.Serializable;

import com.teodonnell0.uno.enums.Protocal;


public class GameProtocal implements Serializable {
	
	private Protocal protocal;
	private Object message;
	
	public GameProtocal(Protocal protocal) {
		this.protocal = protocal;
	}
	
	public GameProtocal(Protocal protocal, Object message) {
		this.protocal = protocal;
		this.message = message;
	}

	public final Protocal getProtocal() {
		return protocal;
	}

	public final Object getMessage() {
		return message;
	}
	
	
}
