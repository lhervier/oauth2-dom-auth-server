package fr.asso.afer.rest.sample.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.asso.afer.rest.sample.ex.NotAuthorizedException;
import fr.asso.afer.rest.sample.model.AccessToken;
import fr.asso.afer.rest.sample.service.CurrentUserService;

/**
 * Cette classe sert à protéger les appels aux
 * méthodes des controlleurs
 * @author Lionel HERVIER
 */
@Component
@Aspect
public class SecurityAspect {

	/**
	 * Le service utilisateur
	 */
	@Autowired
	private CurrentUserService userSvc;
	
	/**
	 * Pointcut pour définir toutes les classes qu'on va protégér
	 */
	@Pointcut("within(fr.asso.afer.rest.sample.controller.secure.*)")
	private void controller() {
	}
	
	/**
	 * Traite le point de jointure
	 * @param joinPoint le point de jointure
	 * @throws NotAuthorizedException Si l'utilisateur n'est pas autorisé
	 */
	@Before(value = "controller()")
	public void checkEcriturenBefore(JoinPoint joinPoint) throws NotAuthorizedException {
		AccessToken token = this.userSvc.getUserInfo();
		if( token == null )
			throw new NotAuthorizedException();
	}
}
