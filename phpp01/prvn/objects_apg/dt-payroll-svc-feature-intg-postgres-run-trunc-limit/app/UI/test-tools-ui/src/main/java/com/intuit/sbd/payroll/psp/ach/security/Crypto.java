/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 *
 * @author shivanandad069
 */
public class Crypto {
    
    static final String TRANSFORMATION_DES_NO_PADDING = "DESede/ECB/NoPadding";
    static final String DEFKEY = "80516EA7BC9E0786D967760749B6A25D80516EA7BC9E0786";
    String m_key;
    /**
	 * Constructor
	 */
	public Crypto ()
	{
		// Install SunJCE provider
		Provider sunJce = new com.sun.crypto.provider.SunJCE();
		Security.addProvider(sunJce);
		m_key = DEFKEY;
	}
        
         public String decrypt (File src) throws IOException
	{
		StringBuffer buf = new StringBuffer ();

		FileInputStream fis = null;
		try
		{
			Cipher cipher = createCipher (false);
			fis = new FileInputStream (src);

			// Read file content into one string buffer before sending for decrypt
			byte[] b = new byte[8];		// !!Note!! Important to be 8 bytes only
			int l, i = fis.read (b);
			String s;
			while (i != -1)
			{
				l = i;
				s = new String (cipher.doFinal (b));

				// Detect null padding if any
				i = s.indexOf (0);
				buf.append (i == -1 ? s : s.substring (0, i));
		    	i = fis.read (b);
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException ("Unable to decrypt file : " + ex);  //$NON-NLS-L$ 
		}
		finally
		{
			if (fis != null)
				fis.close ();
		}

		return buf.toString ();
	}
    
    
    
	public Cipher createCipher (boolean encrypt)
	{
		Cipher desCipher = null;

		try
		{
			// Create a triple-DES key from the keyspec
			DESedeKeySpec keyspec = new DESedeKeySpec (hexToByteArray (m_key));
			SecretKey des3Key = SecretKeyFactory.getInstance ("DESede").generateSecret (keyspec);

			// Create the cipher desCipher
			desCipher = Cipher.getInstance(TRANSFORMATION_DES_NO_PADDING);

			// Initialize the same cipher for decryption
			desCipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, des3Key);
		}
		catch (Exception ex)
		{
			System.out.println ("Exception: " + ex);
		}

		return desCipher;
	}

        
	/**
	 * Convert a hex encoded string to a byte array
	 */
	protected byte[] hexToByteArray (String s)
	{
		if (s == null || (s.length () % 2) > 0)
			return null;

		int i, j;
		byte [] b = new byte[s.length () / 2];
		for (i = 0, j = 0; i < s.length () ; i = i+2, j++)
			b[j] = (byte)Integer.parseInt (s.substring (i, i+2), 16);

		return b;
	}
    
}
