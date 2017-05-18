package fr.asso.afer.rest.sample.controller.secure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.asso.afer.rest.sample.controller.UserInfoController;
import fr.asso.afer.rest.sample.model.UserInfo;
import fr.asso.afer.rest.sample.service.CurrentUserService;

/**
 * Controlleur qui retourne les infos sur l'utilisateur courant
 * @author Lionel HERVIER
 */
@RestController
public class UserInfoControllerImpl implements UserInfoController {

	/**
	 * Le service pour accéder à l'utilisateur courant
	 */
	@Autowired
	private CurrentUserService userSvc;
	
	/**
	 * Retourne les infos sur l'utilisateur courant
	 * @return le détail de l'utilisateur courant
	 */
	@RequestMapping(value = "/userInfo", method = RequestMethod.GET)
	public UserInfo getUserInfo() {
		UserInfo ret = new UserInfo();
		ret.setName(this.userSvc.getUserInfo().getSub());
		return ret;
	}
}
