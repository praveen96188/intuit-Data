/**
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portabilitySpecific.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfSecurityException;
import com.intuit.spc.foundations.portability.security.SpcfPasswordDerivedKey;

/**
 * This class implements the abstract methods defined in SpcfPasswordDerivedKey.
 * @author Vishnu Shankar
 */
public class SpcfPasswordDerivedKeyImpl extends SpcfPasswordDerivedKey 
{
	
	private static final String PRNG_ALGORITHM = "SHA1PRNG";

	/**
	 * Generate a random salt byte array.
	 * @return Salt byte array.
	 * @throws SpcfSecurityException
	 */
	protected byte[] generateSalt()
	{
        SecureRandom rand;
        try 
        {
            rand = SecureRandom.getInstance(PRNG_ALGORITHM);
        } 
        catch (NoSuchAlgorithmException ex)
        {
           throw new SpcfSecurityException("System error: " + ex.getMessage(), SpcfPortabilityErrorEnum.DERIVED_KEY_ERROR);
        }
        
        // Get 64 random bits.
        byte[] output = new byte[8];
        rand.nextBytes(output);
        return output;

	}
	
	/**
	 * Generate the key based of the given password and other parameters.
	 * @param password Password String
	 * @param salt Salt byte array
	 * @param iterationCount Iteration count
	 * @param keySize Key Size.
	 * @param prf An optional parameter to specify the Pseudo random function to use. Currently only "HMAC-SHA1" algorithm is supported and is the default algorithm.
	 * @return Derived Key.
	 */
	protected byte[] generateKey(String password, byte[] salt, int iterationCount, int keySize, String prf)
	{
		// prf is ignored currently and in the future may be supported.
		PKCS5S2ParametersGenerator paramsGen = new PKCS5S2ParametersGenerator();
		
		paramsGen.init(password.getBytes(), salt, iterationCount);

		KeyParameter keyParam = (KeyParameter) paramsGen.generateDerivedParameters(keySize);
		
		byte [] key = keyParam.getKey();
		
		return key;
	}
}
