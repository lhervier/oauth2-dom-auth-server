package fr.asso.afer.rest.sample.service;

import java.text.ParseException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;

import fr.asso.afer.rest.sample.model.AccessToken;
import net.minidev.json.JSONObject;

/**
 * Ce service retourne des infos sur l'utilisateur courant
 * @author Lionel HERVIER
 */
@Component
public class CurrentUserService {

	/**
	 * La requête http
	 */
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * L'environnement Spring
	 */
	@Autowired
	private Environment env;
	
	/**
	 * Retourne les infos sur l'utilisateur courant
	 */
	public AccessToken getUserInfo() {
		try {
			// On doit avoir l'en tête http
			String auth = this.request.getHeader("Authorization");
			if( auth == null )
				return null;
			
			// Elle doit commencer par "Bearer "
			if( !auth.startsWith("Bearer ") )
				return null;
			
			// Extrait le token
			String accessToken = auth.substring("Bearer ".length());
			
			// Extrait le nom de la clé du header
			JWSObject jwsObj = JWSObject.parse(accessToken);
			String kid = jwsObj.getHeader().getKeyID();
			String alg = jwsObj.getHeader().getAlgorithm().getName();
			
			// Récupère le secret
			int i = 0;
			byte[] secret = null;
			while( this.env.containsProperty("jwt.keys." + i + ".kid") ) {
				String currKid = this.env.getProperty("jwt.keys." + i + ".kid");
				String currAlg = this.env.getProperty("jwt.keys." + i + ".alg");
				if( Objects.equal(kid, currKid) && Objects.equal(alg, currAlg) ) {
					String sSecret = this.env.getProperty("jwt.keys." + i + ".secret");
					secret = Base64.getDecoder().decode(sSecret);
					break;
				}
				i++;
			}
			if( secret == null )
				return null;
			 
			JWSVerifier verifier = new MACVerifier(secret);
			if( !jwsObj.verify(verifier) )
				return null;
			
			// Extrait le contenu du token
			JSONObject json = jwsObj.getPayload().toJSONObject();
			AccessToken ret = new AccessToken();
			ret.setAud(json.getAsString("aud"));
			ret.setExp(json.getAsNumber("exp").longValue());
			ret.setIss(json.getAsString("iss"));
			ret.setSub(json.getAsString("sub"));
			
			// Vérifie qu'il n'est pas périmé
			if( ret.getExp() < (System.currentTimeMillis() / 1000L) )
				return null;
			
			return ret;
		} catch (ParseException | JOSEException e) {
			return null;
		}
	}
	
}
