package fr.asso.afer.rest.sample.service;

import java.text.ParseException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
	 * Le secret pour décoder le JWT
	 */
	@Value("${jwt.secret}")
	private String secret;

	/**
	 * Retourne le secret pour vérifier le JWT
	 * @return le secret
	 */
	private byte[] getSecret() {
		return Base64.getDecoder().decode(this.secret);
	}
	
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
			
			// Vérifie que le token est bon
			JWSObject jwsObj = JWSObject.parse(accessToken);
			JWSVerifier verifier = new MACVerifier(this.getSecret());
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
