package com.github.lhervier.domino.oauth.server.repo;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.github.lhervier.domino.oauth.server.AuthContext;
import com.github.lhervier.domino.oauth.server.entity.PersonEntity;
import com.github.lhervier.domino.oauth.server.utils.DominoUtils;

@Repository
public class PersonRepository {
	
	/**
	 * Domino Person document field names
	 */
	private static final String FULL_NAME = "FullName";
	private static final String SHORT_NAME = "ShortName";
	private static final String LAST_NAME = "LastName";
	private static final String FIRST_NAME = "FirstName";
	private static final String MIDDLE_INITIAL = "MiddleInitial";
	private static final String TITLE = "Title";
	private static final String WEBSITE = "WebSite";
	private static final String PHOTO_URL = "PhotoUrl";
	private static final String INTERNET_ADDRESS = "InternetAddress";
	private static final String OFFICE_PHONE_NUMBER = "OfficePhoneNumber";
	
	/**
	 * Random number generator
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * Path to the nab database
	 */
	@Value("${oauth2.server.nab}")
	private String nabPath;
	
	/**
	 * The Auth Context
	 */
	@Autowired
	private AuthContext authContext;
	
	/**
	 * Return the nab database where new person will be registered.
	 * @param session the session to be used
	 * @return the nabPath as configured in the parameters
	 * @throws NotesException
	 */
	private Database getNabDatabase(Session session) throws NotesException {
		return DominoUtils.openDatabase(session, this.nabPath);
	}
	
	/**
	 * Find a person Notes document from one if its full name.
	 * This will search in every NAB of the server (but NOT on LDAP directory assistance).
	 * @param session the session to be used to find the user.
	 * @param fullName One of the user full name
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	private Document findPersonDoc(Session session, String fullName) throws NotesException {
		Name nn = null;
		try {
			nn = session.createName(fullName);
			Vector<Database> nabs = session.getAddressBooks();
			for( Database nab : nabs ) {
				if( !nab.isOpen() )
					if( !nab.open() )
						throw new RuntimeException("Server not allowed to open nabPath ???");
				View v = null;
				try {
					v = nab.getView("($VIMPeople)");
					Document doc = v.getDocumentByKey(nn.getAbbreviated(), true);
					if( doc != null )
						return doc;
				} finally {
					DominoUtils.recycleQuietly(v);		// Recycling the view will NOT recycle the doc
				}
			}
			return null;
		} finally {
			DominoUtils.recycleQuietly(nn);
		}
	}
	
	// ====================================================================================
	
	/**
	 * Save a person (user the current user rights)
	 */
	public PersonEntity save(PersonEntity entity) {
		Session userSession = this.authContext.getUserSession();
		
		Document person = null;
		try {
			person = this.getNabDatabase(userSession).createDocument();
			person.replaceItemValue("Form", "Person");
			person.replaceItemValue("Type", "Person");
			
			if( entity.getFullNames() != null ) {
				Vector<String> fullNames = new Vector<String>();
				fullNames.addAll(entity.getFullNames());
				person.replaceItemValue(FULL_NAME, fullNames);
			}
			if( entity.getInternetAddress() != null ) 
				person.replaceItemValue(INTERNET_ADDRESS, entity.getInternetAddress());
			else
				person.replaceItemValue("MailSystem", "100");		// None
			
			if( entity.getShortName() != null ) 
				person.replaceItemValue(SHORT_NAME, entity.getShortName());
			if( entity.getLastName() != null ) 
				person.replaceItemValue(LAST_NAME, entity.getLastName());
			if( entity.getFirstName() != null ) 
				person.replaceItemValue(FIRST_NAME, entity.getFirstName());
			if( entity.getMiddleInitial() != null ) 
				person.replaceItemValue(MIDDLE_INITIAL, entity.getMiddleInitial());
			if( entity.getTitle() != null ) 
				person.replaceItemValue(TITLE, entity.getTitle());
			if( entity.getWebsite() != null ) 
				person.replaceItemValue(WEBSITE, entity.getWebsite());
			if( entity.getPhotoUrl() != null ) 
				person.replaceItemValue(PHOTO_URL, entity.getPhotoUrl());
			if( entity.getOfficePhoneNumber() != null ) 
				person.replaceItemValue(OFFICE_PHONE_NUMBER, entity.getOfficePhoneNumber());
			
			person.replaceItemValue("Owner", userSession.getEffectiveUserName());
			person.replaceItemValue("LocalAdmin", userSession.getEffectiveUserName());
			
			// HTTP password
			// see https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
			String password = new BigInteger(130, RANDOM).toString(32);
			entity.setHttpPassword(password);
			person.replaceItemValue("HTTPPassword", userSession.evaluate("@Password(\"" + password + "\")"));
			person.replaceItemValue("HTTPPasswordChangeDate", userSession.createDateTime(new Date()));
			person.replaceItemValue("$SecurePassword", "1");
			
			DominoUtils.computeAndSave(person);
			
			// Rafraîchit le NAB pour prise en compte immédiate
			DominoUtils.refreshNab(this.getNabDatabase(userSession));
			
			// Return the secret
			return entity;
		} catch (NotesException e) {
			throw new RuntimeException(e);
		} finally {
			DominoUtils.recycleQuietly(person);
		}
	}
	
	/**
	 * Return the person with the given name.
	 * The search is made using server rights.
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	public PersonEntity findOne(String fullName) throws NotesException {
		Session serverSession = this.authContext.getServerSession();
		
		Document doc = this.findPersonDoc(serverSession, fullName);
		if( doc == null )
			return null;
		
		PersonEntity person = new PersonEntity();
		person.setFullNames(doc.getItemValue(FULL_NAME));
		person.setFirstName(doc.getItemValueString(FIRST_NAME));
		person.setLastName(doc.getItemValueString(LAST_NAME));
		person.setMiddleInitial(doc.getItemValueString(MIDDLE_INITIAL));
		person.setTitle(doc.getItemValueString(TITLE)); 
		person.setShortName(doc.getItemValueString(SHORT_NAME));
		person.setWebsite(doc.getItemValueString(WEBSITE));
		person.setPhotoUrl(doc.getItemValueString(PHOTO_URL));
		person.setInternetAddress(doc.getItemValueString(INTERNET_ADDRESS));
		person.setOfficePhoneNumber(doc.getItemValueString(OFFICE_PHONE_NUMBER));
		
		return person;
	}
	
	/**
	 * Remove a person
	 */
	public void delete(String fullName) throws NotesException {
		Session userSession = this.authContext.getUserSession();
		
		Document doc = null;
		try {
			doc = this.findPersonDoc(userSession, fullName);
			if( doc != null ) {
				doc.remove(true);
				DominoUtils.refreshNab(this.getNabDatabase(userSession));
			}
		} finally {
			DominoUtils.recycleQuietly(doc);
		}
	}
}
