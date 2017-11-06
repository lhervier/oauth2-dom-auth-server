package com.github.lhervier.domino.oauth.server.services;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.View;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.BaseServerComponent;
import com.github.lhervier.domino.oauth.server.model.Application;
import com.github.lhervier.domino.oauth.server.model.Person;
import com.github.lhervier.domino.oauth.server.utils.DominoUtils;

@Service
public class NabService extends BaseServerComponent {

	/**
	 * The nab
	 */
	@Value("${oauth2.server.nab}")
	private String nab;
	
	/**
	 * Notre générateur de nombres aléatoires
	 */
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * @return the nab as configured in the parameters
	 * @throws NotesException
	 */
	private Database getNab() throws NotesException {
		return DominoUtils.openDatabase(this.notesContext.getUserSession(), this.nab);
	}

	/**
	 * Génère un secret
	 * @return un mot de passe aléatoire
	 */
	private String generatePassword() {
		// see https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
		return new BigInteger(130, RANDOM).toString(32);
	}
	
	/**
	 * Create a new person in the NAB
	 * @return the http password
	 */
	public String createPersonForApp(Application app) throws NotesException {
		Document person = null;
		try {
			person = this.getNab().createDocument();
			person.replaceItemValue("Form", "Person");
			person.replaceItemValue("Type", "Person");
			person.replaceItemValue("ShortName", app.getName());
			person.replaceItemValue("LastName", app.getName());
			person.replaceItemValue("MailSystem", "100");		// None
			Vector<String> fullNames = new Vector<String>();
			fullNames.add(app.getFullName());
			fullNames.add(app.getClientId());
			person.replaceItemValue("FullName", fullNames);
			String password = this.generatePassword();
			person.replaceItemValue("HTTPPassword", this.notesContext.getUserSession().evaluate("@Password(\"" + password + "\")"));
			person.replaceItemValue("HTTPPasswordChangeDate", this.notesContext.getUserSession().createDateTime(new Date()));
			person.replaceItemValue("$SecurePassword", "1");
			person.replaceItemValue("Owner", this.notesContext.getUserSession().getEffectiveUserName());
			person.replaceItemValue("LocalAdmin", this.notesContext.getUserSession().getEffectiveUserName());
			DominoUtils.computeAndSave(person);
			
			// Rafraîchit le NAB pour prise en compte immédiate
			DominoUtils.refreshNab(this.getNab());
			
			// Return the secret
			return password;
		} finally {
			DominoUtils.recycleQuietly(person);
		}
	}
	
	/**
	 * Return the document that correspond to a person
	 * @param userName the user name
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	private Document getPersonDoc(String userName) throws NotesException {
		Name nn = null;
		try {
			nn = this.notesContext.getServerSession().createName(userName);
			Vector<Database> nabs = this.notesContext.getServerSession().getAddressBooks();
			for( Database nab : nabs ) {
				if( !nab.isOpen() )
					if( !nab.open() )
						throw new RuntimeException("Server not allowed to open nab ???");
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
	
	/**
	 * Return the person with the given name
	 * @throws NotesException
	 */
	public Person getPerson(String userName) throws NotesException {
		Document doc = this.getPersonDoc(userName);
		if( doc == null )
			return null;
		
		Person person = new Person();
		person.setName(userName);
		person.setFirstName(doc.getItemValueString("FirstName"));
		person.setLastName(doc.getItemValueString("LastName"));
		person.setMiddleInitial(doc.getItemValueString("MiddleInitial"));
		person.setTitle(doc.getItemValueString("Title")); 
		person.setShortName(doc.getItemValueString("ShortName"));
		person.setWebsite(doc.getItemValueString("WebSite"));
		person.setPhotoUrl(doc.getItemValueString("PhotoUrl"));
		person.setInternetAddress(doc.getItemValueString("InternetAddress"));
		person.setOfficePhoneNumber(doc.getItemValueString("OfficePhoneNumber"));
		
		return person;
	}
	
	/**
	 * Remove a person
	 */
	public void removePerson(String fullName) throws NotesException {
		Document doc = null;
		try {
			doc = this.getPersonDoc(fullName);
			if( doc != null ) {
				doc.remove(true);
				DominoUtils.refreshNab(this.getNab());
			}
		} finally {
			DominoUtils.recycleQuietly(doc);
		}
	}
}
