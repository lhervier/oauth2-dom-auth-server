package com.github.lhervier.domino.oauth.server.model;

import java.io.Serializable;

public class StateResponse implements Serializable {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7649047910583898257L;
	
	/**
	 * Le state
	 */
	private String state;

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
}
