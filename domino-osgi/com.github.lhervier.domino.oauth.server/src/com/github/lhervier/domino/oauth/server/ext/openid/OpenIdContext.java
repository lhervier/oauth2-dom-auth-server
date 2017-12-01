package com.github.lhervier.domino.oauth.server.ext.openid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public class OpenIdContext {

	/**
	 * The issuer
	 */
	private String iss;
	
	/**
	 * The subject
	 */
	private String sub;
	
	/**
	 * the audiance
	 */
	private String aud;
	
	/**
	 * ACR
	 */
	private String acr;
	
	/**
	 * AMR
	 */
	private String amr;
	
	/**
	 * AZP
	 */
	private String azp;
	
	/**
	 * Authorization time
	 */
	@JsonProperty("auth_time")
	private long authTime;
	
	/**
	 * Nonce
	 */
	private String nonce;
	
	// ==================== "profile" scope ======================
	
	/**
	 * Name
	 */
	private String name;
	
	/**
	 * Family name
	 */
	@JsonProperty("family_name")
	private String familyName;
	
	/**
	 * Given name
	 */
	@JsonProperty("given_name")
	private String givenName;
	
	/**
	 * Middle name
	 */
	@JsonProperty("middle_name")
	private String middleName;
	
	/**
	 * Nickname
	 */
	private String nickname;
	
	/**
	 * Preferred user name
	 */
	@JsonProperty("prefered_username")
	private String preferedUsername;
	
	/**
	 * Profile
	 */
	private String profile;
	
	/**
	 * Picture
	 */
	private String picture;
	
	/**
	 * Web site
	 */
	private String website;
	
	/**
	 * Gender
	 */
	private String gender;
	
	/**
	 * Birth date
	 */
	@JsonProperty("birthdate")
	private String birthdate;
	
	/**
	 * Zone info
	 */
	private String zoneinfo;
	
	/**
	 * Locale
	 */
	private String locale;
	
	/**
	 * Updated at
	 */
	@JsonProperty("updated_at")
	private Long updatedAt;
	
	// ================ "email" scope ========================
	
	/**
	 * Email
	 */
	private String email;
	
	/**
	 * Email verified
	 */
	@JsonProperty("email_verified")
	private String emailVerified;
	
	// =============== "address" scope =====================
	
	/**
	 * Address
	 */
	private String address;
	
	// ===================== "phone" scope ===================
	
	/**
	 * Phone
	 */
	@JsonProperty("phone_number")
	private String phoneNumber;
	
	/**
	 * Phone verified
	 */
	@JsonProperty("phone_number_verified")
	private String phoneNumberVerified;

	/**
	 * @return the iss
	 */
	public String getIss() {
		return iss;
	}

	/**
	 * @param iss the iss to set
	 */
	public void setIss(String iss) {
		this.iss = iss;
	}

	/**
	 * @return the sub
	 */
	public String getSub() {
		return sub;
	}

	/**
	 * @param sub the sub to set
	 */
	public void setSub(String sub) {
		this.sub = sub;
	}

	/**
	 * @return the aud
	 */
	public String getAud() {
		return aud;
	}

	/**
	 * @param aud the aud to set
	 */
	public void setAud(String aud) {
		this.aud = aud;
	}

	/**
	 * @return the authTime
	 */
	public long getAuthTime() {
		return authTime;
	}

	/**
	 * @param authTime the authTime to set
	 */
	public void setAuthTime(long authTime) {
		this.authTime = authTime;
	}

	/**
	 * @return the acr
	 */
	public String getAcr() {
		return acr;
	}

	/**
	 * @param acr the acr to set
	 */
	public void setAcr(String acr) {
		this.acr = acr;
	}

	/**
	 * @return the amr
	 */
	public String getAmr() {
		return amr;
	}

	/**
	 * @param amr the amr to set
	 */
	public void setAmr(String amr) {
		this.amr = amr;
	}

	/**
	 * @return the azp
	 */
	public String getAzp() {
		return azp;
	}

	/**
	 * @param azp the azp to set
	 */
	public void setAzp(String azp) {
		this.azp = azp;
	}

	/**
	 * @return the nonce
	 */
	public String getNonce() {
		return nonce;
	}

	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the familyName
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	/**
	 * @return the givenName
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * @param givenName the givenName to set
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * @return the preferedUsername
	 */
	public String getPreferedUsername() {
		return preferedUsername;
	}

	/**
	 * @param preferedUsername the preferedUsername to set
	 */
	public void setPreferedUsername(String preferedUsername) {
		this.preferedUsername = preferedUsername;
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the picture
	 */
	public String getPicture() {
		return picture;
	}

	/**
	 * @param picture the picture to set
	 */
	public void setPicture(String picture) {
		this.picture = picture;
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
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the birthdate
	 */
	public String getBirthdate() {
		return birthdate;
	}

	/**
	 * @param birthdate the birthdate to set
	 */
	public void setBirthdate(String birthDate) {
		this.birthdate = birthDate;
	}

	/**
	 * @return the zoneinfo
	 */
	public String getZoneinfo() {
		return zoneinfo;
	}

	/**
	 * @param zoneinfo the zoneinfo to set
	 */
	public void setZoneinfo(String zoneinfo) {
		this.zoneinfo = zoneinfo;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the emailVerified
	 */
	public String getEmailVerified() {
		return emailVerified;
	}

	/**
	 * @param emailVerified the emailVerified to set
	 */
	public void setEmailVerified(String emailVerified) {
		this.emailVerified = emailVerified;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the phoneNumberVerified
	 */
	public String getPhoneNumberVerified() {
		return phoneNumberVerified;
	}

	/**
	 * @param phoneNumberVerified the phoneNumberVerified to set
	 */
	public void setPhoneNumberVerified(String phoneNumberVerified) {
		this.phoneNumberVerified = phoneNumberVerified;
	}

	/**
	 * @return the updatedAt
	 */
	public Long getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * @param updatedAt the updatedAt to set
	 */
	public void setUpdatedAt(Long updatedAt) {
		this.updatedAt = updatedAt;
	}
	
}
