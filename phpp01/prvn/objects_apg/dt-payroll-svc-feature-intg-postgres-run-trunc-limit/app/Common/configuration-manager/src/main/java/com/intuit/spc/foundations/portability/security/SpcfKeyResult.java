/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;

/**
 * This class mostly serves the function of data storage and contains the key and the salt. 
 * @author Vishnu Shankar
 *
 */
public class SpcfKeyResult {

	private byte[] key;
	private byte[] salt;
	
	/**
	 * Get the key.
	 * @return Key byte array.
	 */
	public byte[] getKey()
	{
		return key;
	}
	
	/**
	 * Get the salt.
	 * @return Salt byte array.
	 */
	public byte[] getSalt()
	{
		return salt;
	}
	
	/**
	 * Set the salt
	 * @param salt Salt byte array.
	 */
	public void setSalt(byte[] salt)
	{
		this.salt = salt;
	}
	
	/**
	 * Set the key.
	 * @param key Key byte array.
	 */
	public void setKey(byte[] key)
	{
		this.key = key;
	}
	
}
