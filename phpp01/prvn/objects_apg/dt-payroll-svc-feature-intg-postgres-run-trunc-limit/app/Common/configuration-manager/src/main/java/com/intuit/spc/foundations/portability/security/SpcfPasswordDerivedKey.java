/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfSecurityException;
import com.intuit.spc.foundations.portability.SpcfStringUtil;

/**
 * This class provides a derived key based of a given password.
 * @author Vishnu Shankar
 */
public abstract class SpcfPasswordDerivedKey {

	private SpcfKeySpec keySpec;
	
    /**
     * Creates a default SpcfPasswordDerivedKey object. 
     * @return Password Derived Key object which can be used to derive the key based of a given password.
     */
    public static SpcfPasswordDerivedKey createInstance() 
    {
        return SpcfFactory.getInstance().createPasswordDerivedKey();        
    }


	/**
	 * Set the key spec object
	 * @param keySpec Key Spec object.
	 */
	public void setKeySpec(SpcfKeySpec keySpec)
	{
		this.keySpec = keySpec;
	}
	
	/**
	 * Get the derived key based on the set key spec.
	 * @return Key result object containing the key and the salt.
	 * @throws SpcfSecurityException
	 */
	public SpcfKeyResult getDerivedKey()
	{
		return getDerivedKey(keySpec);
	}
	
	/**
	 * Get the derived key based on the key spec passed in the parameter list.
	 * @param keySpec Key spec object.
	 * @return Key Result object containing the key and the salt byte array.
	 * @throws SpcfSecurityException
	 */
	public SpcfKeyResult getDerivedKey(SpcfKeySpec keySpec)
	{
		String password = keySpec.getPassword();

		
		if ( password == null )
			throw new SpcfSecurityException("Password is not set.", SpcfPortabilityErrorEnum.PASSWORD_NULL);
		
		SpcfPasswordQualitySpec pwdSpec = keySpec.getPasswordQualitySpec();
		
		if ( pwdSpec == null )
			pwdSpec = SpcfIndustryBestPracticePasswordQualitySpec.createInstance();

		
		byte[] salt = keySpec.getSalt();
		
		if ( salt == null )
			salt = generateSalt();
		
		int iterationCount = keySpec.getIterationCount();
		
		if ( iterationCount == 0 )
			iterationCount = 2000;		// Default iteration count.
		
		int keySize = keySpec.getKeySize();
		String algorithm = keySpec.getAlgorithm();
		
		if ( keySize == 0 && algorithm != null )
		{
			if ( SpcfStringUtil.compareStringsIgnoreCase(algorithm, "3DES") == 0)
				keySize = 168;
			else if ( SpcfStringUtil.compareStringsIgnoreCase(algorithm, "AES") == 0)
				keySize = 128;
			else if ( SpcfStringUtil.compareStringsIgnoreCase(algorithm, "AES-192") == 0)
				keySize = 192;
			else if ( SpcfStringUtil.compareStringsIgnoreCase(algorithm, "AES-256") == 0)
				keySize = 256;
			else if ( SpcfStringUtil.compareStringsIgnoreCase(algorithm, "RC5") == 0)
				keySize = 128;
			else keySize = 128;			// If any other algorithm, default to 128 bits.
		} 
			
		if ( keySize == 0 )		// If both key size and algorithm are not set...
			keySize = 128;			// Default is 128 bits.

		validateParameters(password, salt, iterationCount, keySize, pwdSpec);
		
		byte[] key = generateKey(password, salt, iterationCount, keySize, keySpec.getPseudoRandomFunction());
		
		SpcfKeyResult result = new SpcfKeyResult();
		
		result.setKey(key);
		result.setSalt(salt);
		
		return result;
	}
	
	/**
	 * Validate the password, salt, iteration count and key size parameters.
	 * @param password Password String
	 * @param salt Salt byte array
	 * @param iterationCount Iteration count value.
	 * @param keySize Key Size
	 * @param pwdSpec Password Quality Spec objet.
	 * @return Returns true if validation of all parameters succeeds.
	 * @throws SpcfSecurityException
	 */
	private boolean validateParameters(String password, byte[] salt, int iterationCount, 
			int keySize, SpcfPasswordQualitySpec pwdSpec)
	{
		validateSalt(salt);
		validateIterationCount(iterationCount);
		validatePassword(password, pwdSpec);
		validateKeySize(keySize);
		
		// If everything checks ok, return true.
		return true;
	}
	
	/**
	 * Validate salt byte array.
	 * @param salt Salt byte array.
	 * @return True if salt byte array has atleast 
	 * @throws SpcfSecurityException
	 */
	private boolean validateSalt(byte[] salt)
	{
		if ( salt.length < 8 )
			throw new SpcfSecurityException("Salt array has less than the minimumu accepted length (64bits).", SpcfPortabilityErrorEnum.SALT_INVALID);
		
		if ( salt.length > 128 )
			throw new SpcfSecurityException("Salt array has more than the maximum accepted length (1024 bits).", SpcfPortabilityErrorEnum.SALT_INVALID);
		
		// If everything is ok, return true.
		return true;
	}
	
	/**
	 * Validate the Iteration Count.
	 * @param iterationCount Iteration count.
	 * @return Returns true if iteration count is valid.
	 * @throws SpcfSecurityException
	 */
	private boolean validateIterationCount(int iterationCount)
	{
		if ( iterationCount < 1000 )
			throw new SpcfSecurityException("Iteration count specified is less than minimum accepted count. (1000). ", SpcfPortabilityErrorEnum.ITERATION_COUNT_INVALID);
	
		if ( iterationCount > 10000000 )
			throw new SpcfSecurityException("Iteration count specified is more than the maximum accepted count. (10000000). ", SpcfPortabilityErrorEnum.ITERATION_COUNT_INVALID);
	
		// If everything is ok, return true.
		return true;
	}
	
	/**
	 * Validate the key size.
	 * @param keySize Key size in bits.
	 * @return Returns true if key size is valid.
	 * @throws SpcfSecurityException
	 */
	private boolean validateKeySize(int keySize)
	{
		if ( keySize < 128 )
			throw new SpcfSecurityException("Key size specified is less than the minimum accepted length. (128 bits)", SpcfPortabilityErrorEnum.KEY_SIZE_INVALID);
	
		if ( keySize > 5120 )
			throw new SpcfSecurityException("Key size specified is more than the maximum accepted length. (5120 bits)", SpcfPortabilityErrorEnum.KEY_SIZE_INVALID);
	
		// If everything is ok, return true.
		return true;
	}
	
	/**
	 * Validate the password.
	 * @param password Password String
	 * @param pwdSpec Password Quality Spec.
	 * @return Returns true if the password is valid.
	 * @throws SpcfSecurityException
	 */
	private boolean validatePassword(String password, SpcfPasswordQualitySpec pwdSpec)
	{
		if ( password.length() < 1 )
			throw new SpcfSecurityException("Password length is less than the minimum accepted length. (1)", SpcfPortabilityErrorEnum.PASSWORD_INVALID);
		
		if ( password.length() > 256 )
			throw new SpcfSecurityException("Password length is more than the maximum accepted length. (256)", SpcfPortabilityErrorEnum.PASSWORD_INVALID);
		
		
		SpcfPasswordQualityValidator validator = SpcfPasswordQualityValidator.createInstance();
		
		try {
			validator.validatePassword(password, pwdSpec);
		} catch (SpcfSecurityException se)
		{
			// We are throwing 5000 here for all error messages from PQV component
			throw new SpcfSecurityException(se.getErrorId() + ": " + se.getMessage(), SpcfPortabilityErrorEnum.PASSWORD_INVALID);
		}

		// If everything is ok, return true.
		return true;
	}
	
	/**
	 * Generate a new salt array.
	 * @return Salt byte array.
	 * @throws SpcfSecurityException
	 */
	protected abstract byte[] generateSalt();
	
	/**
	 * Generate the derived key based of the given password and other parameters.
	 * @param password Password String
	 * @param salt Salt byte array
	 * @param iterationCount Iteration Count
	 * @param keySize Key Size
	 * @param prf Optional parameter to specify the Pseudo Random function to use.
	 * @return Generated key
	 */
	protected abstract byte[] generateKey(String password, byte[] salt, int iterationCount, int keySize, String prf);
}
