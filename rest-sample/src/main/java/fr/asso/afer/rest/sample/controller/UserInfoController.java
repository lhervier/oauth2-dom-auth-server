package fr.asso.afer.rest.sample.controller;

import fr.asso.afer.rest.sample.model.UserInfo;
import io.swagger.annotations.ApiOperation;

/**
 * Controlleur pour retourner les infos sur l'utilisateur courant
 * @author Lionel HERVIER
 */
public interface UserInfoController {

	/**
	 * Retourne les infos sur l'utilisateur courant
	 * @return le détail de l'utilisateur courant
	 */
	@ApiOperation(value = "Récupère les infos sur l'utilisateur courant", tags = { "user" })
	public UserInfo getUserInfo();
}
