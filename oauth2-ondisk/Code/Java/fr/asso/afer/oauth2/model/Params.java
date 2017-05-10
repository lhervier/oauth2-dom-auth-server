package fr.asso.afer.oauth2.model;

import java.io.Serializable;

/**
 * Les paramètres de l'application
 * @author Lionel HERVIER
 */
public class Params implements Serializable {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -8822255456808484726L;

	/**
	 * Le chemin vers le NAB
	 */
	private String nab;

	/**
	 * @return the nab
	 */
	public String getNab() {
		return nab;
	}

	/**
	 * @param nab the nab to set
	 */
	public void setNab(String nab) {
		this.nab = nab;
	}
}
