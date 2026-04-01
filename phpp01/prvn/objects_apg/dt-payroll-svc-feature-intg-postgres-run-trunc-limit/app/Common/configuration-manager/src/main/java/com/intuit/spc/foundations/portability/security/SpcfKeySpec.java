/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;


/**
 * A class to store the key specifications.
 * @author Vishnu Shankar
 */
public class SpcfKeySpec {

	private String password = null;
	private byte[] salt = null;
	private int iterationCount = 0;
	private int keySize = 0;
	private SpcfPasswordQualitySpec spec = null;
	private String algorithm = null;
	private String prf = "HMAC-SHA1";		// Default pseuedo random function.

	
	/**
	 * Create a new instance of SpcfKeySpec.
	 * @return
	 */
	public static SpcfKeySpec createInstance()
	{
		return new SpcfKeySpec();
	}
	
	/**
	 * Get the algorithm specified for key spec.
	 * @return Algorithm name.
	 */
	public String getAlgorithm()
	{
		return algorithm;
	}
	
	/**
	 * Get the password specified.
	 * @return Password
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * Get the salt byte array specified for the key spec.
	 * @return Salt as byte array
	 */
	public byte[] getSalt()
	{
		return salt;
	}
	
	/**
	 * Get the iteration count.
	 * @return Iteration count.
	 */
	public int getIterationCount()
	{
		return iterationCount;
	}
	
	/**
	 * Get the key size.
	 * @return Key Size.
	 */
	public int getKeySize()
	{
		return keySize;
	}
	
	/**
	 * Get the password quality spec specified.
	 * @return Password Quality Spec.
	 */
	public SpcfPasswordQualitySpec getPasswordQualitySpec()
	{
		return  spec;
	}
	
	/**
	 * Get the pseudo random function specified.
	 * @return Pseudo random function name.
	 */
	public String getPseudoRandomFunction()
	{
		return prf;
	}
	
	/**
	 * Set the algorithm name for generating a key of a specific size.
	 * This method is optional and can be used to specify the algorithm name and hence the key size. 
	 * Accepted Strings: "3DES", "RC5", "AES", "AES-192", "AES-256".
	 * @param algorithm
	 */
	public void setAlgorithm(String algorithm)
	{
		this.algorithm = algorithm;
	}
	
	/**
	 * Set the password which is used to generate the key.
	 * @param password Password
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	/**
	 * Set the salt for generating the key.
	 * @param salt Salt as byte array.
	 */
	public void setSalt(byte[] salt)
	{
		this.salt = salt;
	}
	
	/**
	 * Set the iteration count.
	 * @param iterationCount Iteration count.
	 */
	public void setIterationCount(int iterationCount)
	{
		this.iterationCount = iterationCount;
	}
	
	/**
	 * Set the key size.
	 * @param keySize Key Size.
	 */
	public void setKeySize(int keySize)
	{
		this.keySize = keySize;
	}
	
	/**
	 * Set the password quality spec.
	 * Ex.: SpcfPCIPasswordQualitySpec, SpcfIndustryBestPracticePasswordQualitySpec, SpcfIntuitMinimumPasswordQualitySpec
	 * @param spec Password Quality Spec.
	 */
	public void setPasswordQualitySpec(SpcfPasswordQualitySpec spec)
	{
		this.spec = spec;
	}
	
	/**
	 * Set the pseudo random function.
	 * Currently HMAC-SHA1 is the only algorithm supported. In the future, we might support more algorithms.
	 * The default value for psuedo random function if not set is "HMAC-SHA1".
	 * @param prf Pseudo random function used in the key generation process.
	 */
	public void setPseudoRandomFunction(String prf)
	{
		this.prf = prf;
	}
	
}
