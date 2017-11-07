package com.github.lhervier.domino.oauth.server.entity;

import java.util.List;


public class PersonEntity {

	/**
	 * Full name
	 */
	private List<String> fullNames;
	
	/**
	 * Family name
	 */
	private String lastName;
	
	/**
	 * First name
	 */
	private String firstName;
	
	/**
	 * Middle name
	 */
	private String middleInitial;
	
	/**
	 * Preferred user name
	 */
	private String shortName;
	
	/**
	 * Picture
	 */
	private String photoUrl;
	
	/**
	 * Web site
	 */
	private String website;
	
	/**
	 * Gender
	 */
	private String title;
	
	/**
	 * Email
	 */
	private String internetAddress;
	
	/**
	 * Phone
	 */
	private String officePhoneNumber;

	/**
	 * Http Password
	 */
	private String httpPassword;
	
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String familyName) {
		this.lastName = familyName;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String givenName) {
		this.firstName = givenName;
	}

	/**
	 * @return the middleInitial
	 */
	public String getMiddleInitial() {
		return middleInitial;
	}

	/**
	 * @param middleInitial the middleInitial to set
	 */
	public void setMiddleInitial(String middleName) {
		this.middleInitial = middleName;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String preferedUsername) {
		this.shortName = preferedUsername;
	}

	/**
	 * @return the photoUrl
	 */
	public String getPhotoUrl() {
		return photoUrl;
	}

	/**
	 * @param photoUrl the photoUrl to set
	 */
	public void setPhotoUrl(String picture) {
		this.photoUrl = picture;
	}

	/**
	 * @return the website
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * @param website the website to set
	 */
	public void setWebsite(String website) {
		this.website = website;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String gender) {
		this.title = gender;
	}

	/**
	 * @return the internetAddress
	 */
	public String getInternetAddress() {
		return internetAddress;
	}

	/**
	 * @param internetAddress the internetAddress to set
	 */
	public void setInternetAddress(String email) {
		this.internetAddress = email;
	}

	/**
	 * @return the officePhoneNumber
	 */
	public String getOfficePhoneNumber() {
		return officePhoneNumber;
	}

	/**
	 * @param officePhoneNumber the officePhoneNumber to set
	 */
	public void setOfficePhoneNumber(String phoneNumber) {
		this.officePhoneNumber = phoneNumber;
	}

	/**
	 * @return the httpPassword
	 */
	public String getHttpPassword() {
		return httpPassword;
	}

	/**
	 * @param httpPassword the httpPassword to set
	 */
	public void setHttpPassword(String httpPassword) {
		this.httpPassword = httpPassword;
	}

	/**
	 * @return the fullNames
	 */
	public List<String> getFullNames() {
		return fullNames;
	}

	/**
	 * @param fullNames the fullNames to set
	 */
	public void setFullNames(List<String> fullNames) {
		this.fullNames = fullNames;
	}

	
}
