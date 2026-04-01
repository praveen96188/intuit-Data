package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.domain.HashType;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;


/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Apr 4, 2008
 * Time: 3:54:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class PINUtils {

    /**
     * Verifies that PIN has a valid format
     *
     * @param pPIN
     * @return
     */

    private static String UTF_ENCODING = "UTF-8";
    private static String EBCDIC_ENCODING = "Cp037";

    private static final String SIGNATURE_ALGORITHM = "RSA";
    private static int SIGNATURE_KEY_SIZE = 1024;

    private static int TABLE_LENGTH = 256;
    private static int S[] = new int[TABLE_LENGTH];              //RC4 state table
    private static int NUMBER_OF_MIXES = TABLE_LENGTH * 10;     //number of characters discarded per generated character
    private static int ii = 0;
    private static int jj = 0;

    public static final HashType CURRENT_HASH_TYPE = HashType.SHA512;

    /**
     * Validates a PIN format according to the following rule:
     * . PIN Length has to be between 8 and 12 characters
     * . At least one letter
     * . At least one number
     *
     * @param pPIN
     * @return true if PIN is in a valid format
     */
    public static boolean validatePINFormat(String pPIN) {

        return (pPIN.matches("^.*\\p{Alpha}.*$")
                && pPIN.matches("^.*\\p{Digit}.*$")
                && pPIN.matches("^[\\p{Alnum}!@#$%^&]{8,12}$"));

    }


    /**
     * Generates a random PIN with a valid format
     *
     * @return generated PIN with a 8 character length and at least one number and one letter
     */
    public static String generateRandomPIN() {

        String generatedPIN[] = new String[8];

        for (int i = 0; i < 256; i++) {
            S[i] = i;
        }

        // Create a 6 character random PIN that contains either
        // numbers, lowercase letters or uppercase letters

        for (int i = 0; i < 6; i++) {
            mix(new Date().getTime());  //use current time (long milliseconds) as source randomness
            int ch = generateCharacter(62);
            if (ch < 10) {
                ch = ch + (int) '0';
            } else if (ch < 36) {
                ch = ch + (int) 'A' - 10;
            } else {
                ch = ch + (int) 'a' - 36;
            }
            Character generatedChar = (char) ch;
            generatedPIN[i] = generatedChar.toString();
        }

        // Now append the last two characters to the PIN
        // Make sure we have a valid PIN by forcing these two characters to be one letter and one number

        //Get a letter
        int ch = generateCharacter(26) + (int) 'A';
        Character generatedChar = (char) ch;
        generatedPIN[6] = generatedChar.toString();

        //Get a number
        ch = generateCharacter(10) + (int) '0';
        generatedChar = (char) ch;
        generatedPIN[7] = generatedChar.toString();

        // Then shuffle the generated string
        return shuffle(generatedPIN).toUpperCase();

    }


    /**
     * Generates a pair of Public/Private Keys
     *
     * @return String[]
     */
    public static String[] generateKeyPair() {

        /* Generate a Key Pair using RSA */

        try {

            /* Generate a key pair */

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
            keyGen.initialize(SIGNATURE_KEY_SIZE);

            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            String[] keyPair = new String[2];
            keyPair[0] = getPrivateKeyString(privateKey);
            keyPair[1] = getPublicKeyString(publicKey);

            return keyPair;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Swaps string array elements i and j
     *
     * @param pString
     * @param i
     * @param j
     */
    private static void swapElements(String[] pString, int i, int j) {
        String swap = pString[i];
        pString[i] = pString[j];
        pString[j] = swap;
    }

    /**
     * Rearranges a array of strings in random order
     *
     * @param pStringToShuffle
     * @return String
     */
    private static String shuffle(String[] pStringToShuffle) {
        String shuffled = "";

        int N = pStringToShuffle.length;
        for (int i = 0; i < N; i++) {
            int j = i + (int) (Math.random() * (N - i));   // between i and N-1
            swapElements(pStringToShuffle, i, j);
        }
        for (int i = 0; i < N; i++) {
            shuffled = shuffled + pStringToShuffle[i];
        }
        return shuffled;
    }

    /**
     * RC4 Key Scheduling Algorithm (KSA)- Generates a random 256-value state
     * array S
     *
     * @param pNumber
     * @return int
     */
    private static int mix(long pNumber) {

        if (pNumber < 0) {
            pNumber = -pNumber;
        }

        while (true) {
            ii = (ii + 1) % 256;
            jj = (int) ((jj + S[ii] + pNumber) % 256);
            int temp = S[ii];
            S[ii] = S[jj];
            S[jj] = temp;
            pNumber = pNumber / 256;
            if (pNumber == 0) break;
        }
        return (S[(S[ii] + S[jj]) % 256]);
    }

    /**
     * Generates a random character
     *
     * @param r
     * @return int
     */
    private static int generateCharacter(int r) {
        int q = 0;
        ii = jj = 0;
        // perform extra mixing
        for (int k = 0; k < NUMBER_OF_MIXES; k++) {
            mix(0);
        }

        while (true) {
            q = mix(0);
            if (q < r * (256 / r)) break; // avoid biased choice
        }
        return (q % r);
    }

    public static String encrypt(String pPIN) {
        return encrypt(pPIN, CURRENT_HASH_TYPE);
    }

        /**
         * Encrypts a PIN using MessageDigest with SHA
         *
         * @param pPIN
         * @return
         */
    public static String encrypt(String pPIN, HashType pHashType) {
        pPIN = pPIN.toUpperCase();
        String encryptedPIN = null;

        MessageDigest md = null;
        try {
            switch (pHashType) {
                case SHA :
                    md = MessageDigest.getInstance("SHA");
                    break;
                case SHA256 :
                    md = MessageDigest.getInstance("SHA-256");
                    break;
                case SHA512 :
                    md = MessageDigest.getInstance("SHA-512");
                    break;
                case AS400 :
                    encryptedPIN = encryptAS400(pPIN);
                    break;
                default :
                    throw new NoSuchAlgorithmException("Invalid Hash Type - " + pHashType);
            }
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (md != null) {
            try {
                    md.update(pPIN.getBytes(UTF_ENCODING));
                    byte raw[] = md.digest();
                    encryptedPIN = (new BASE64Encoder()).encode(raw);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return encryptedPIN;
    }

    /**
     * Encrypts a PIN using AS400 algorithm
     *
     * @param pPIN
     * @return String
     */
    private static String encryptAS400(String pPIN) {
        pPIN = pPIN.toUpperCase();
        String msHashString = "4x90-@2KQ#a=";
        String encryptedPIN = "";
        byte[] reqByte = null;
        byte mHashBytes[] = null;
        byte[] space = null;

        try {
            reqByte = pPIN.getBytes(EBCDIC_ENCODING);
            space = new String(" ").getBytes(EBCDIC_ENCODING);
            mHashBytes = msHashString.getBytes(EBCDIC_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }

        byte[] mEncBytes = new byte[12];

        for (int i = 0; i < 12; i++) {
            if (i < reqByte.length) {
                mEncBytes[i] = (byte) (reqByte[i] + mHashBytes[i]);
            } else {
                mEncBytes[i] = (byte) (space[0] + mHashBytes[i]);
            }
        }
        try {
            encryptedPIN = new String(mEncBytes, EBCDIC_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
        return encryptedPIN;
    }

    /**
     * This method is used to obtain the String
     * representation of the PublicKey.
     *
     * @param publicKey of type {@link PublicKey}
     * @return PublicKey as a String
     */

    public static String getPublicKeyString(PublicKey publicKey) {

        return new String(encode(publicKey.getEncoded()));

    }

    /**
     * This method is used to obtain the String
     * representation of the PrivateKey.
     *
     * @param privateKey of type
     * @return PrivateKey as a String
     */

    public static String getPrivateKeyString(PrivateKey privateKey) {

        return new String(encode(privateKey.getEncoded()));

    }

    /**
     * This method is used to obtain the
     * PrivateKey object from the
     * String representation.
     *
     * @param key of type String
     * @return PrivateKey
     * @throws Exception
     */

    public static PrivateKey getPrivateKeyFromString(String key) throws Exception {

        PrivateKey privateKey = null;

        if (key != null) {
            try {

                KeyFactory keyFactory = KeyFactory.getInstance(SIGNATURE_ALGORITHM);
                EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                        decode(key.getBytes("UTF8")));
                privateKey = keyFactory.generatePrivate(privateKeySpec);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return privateKey;

    }

    /**
     * This method is used to obtain the PublicKey object
     * from the String representation of the Public Key.
     *
     * @param key of type String
     * @return PublicKey
     * @throws Exception
     */

    public static PublicKey getPublicKeyFromString(String key) throws Exception {

        PublicKey publicKey = null;
        if (key != null) {
            try {

                KeyFactory keyFactory = KeyFactory.getInstance(SIGNATURE_ALGORITHM);
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                        decode(key.getBytes("UTF8")));
                publicKey = keyFactory.generatePublic(publicKeySpec);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return publicKey;
    }

    /**
     * This method is used to obtain the
     * encrypted contents from the original
     * contents by passing the PublicKey.
     * This method is useful when the byte is more
     * than 117.
     *
     * @param text of type String
     * @param key  of type {@link PublicKey}
     * @return encrypted value as a String
     * @throws Exception
     */

    public static String getEncryptedValue(String text, PrivateKey key) throws Exception {
        String encryptedText;

        try {

            byte[] textBytes = text.getBytes("UTF8");
            Cipher cipher = Cipher.getInstance("RSA"); // RSA/ECB/PKCS1Padding
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] cipherBytes = cipher.doFinal(textBytes);
            byte[] base64EncodedCypherBytes = encode(cipherBytes);
            encryptedText = new String(base64EncodedCypherBytes, 0, base64EncodedCypherBytes.length, "UTF8");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return encryptedText;
    }


    /**
     * This method is used to decrypt the contents.
     * <p/>
     * This method is useful when the size of the
     * <p/>
     * bytes is more than 117.
     *
     * @param text of type String indicating the
     *             <p/>
     *             encrypted contents.
     * @param key  of type {@link PrivateKey}
     * @return decrypted value as a String
     */

    public static String getDecryptedValue(String text, PublicKey key) {

        String decryptedText = null;

        try {
            byte[] encryptedBytes = decode(text.getBytes());
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            decryptedText = new String(decryptedBytes, 0, decryptedBytes.length, "UTF8");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return decryptedText;
    }

    /** */
    private static final int BASELENGTH = 255;

    /** */
    private static final int LOOKUPLENGTH = 64;

    /** */
    private static final int TWENTYFOURBITGROUP = 24;

    /** */
    private static final int EIGHTBIT = 8;

    /** */
    private static final int SIXTEENBIT = 16;

    /** */
    private static final int SIXBIT = 6;

    /** */
    private static final int FOURBYTE = 4;

    /**
     * The sign bit as an int
     */
    private static final int SIGN = -128;

    /**
     * The padding character
     */
    private static final byte PAD = (byte) '=';

    /**
     * The alphabet
     */
    private static final byte[] BASE64_ALPHABET = new byte[BASELENGTH];

    /**
     * The lookup alphabet
     */
    private static final byte[] LOOKUP_BASE64_ALPHABET = new byte[LOOKUPLENGTH];

    static {

        for (int i = 0; i < BASELENGTH; i++) {
            BASE64_ALPHABET[i] = -1;
        }
        for (int i = 'Z'; i >= 'A'; i--) {
            BASE64_ALPHABET[i] = (byte) (i - 'A');
        }
        for (int i = 'z'; i >= 'a'; i--) {
            BASE64_ALPHABET[i] = (byte) (i - 'a' + 26);
        }

        for (int i = '9'; i >= '0'; i--) {
            BASE64_ALPHABET[i] = (byte) (i - '0' + 52);
        }

        BASE64_ALPHABET['+'] = 62;
        BASE64_ALPHABET['/'] = 63;

        for (int i = 0; i <= 25; i++) {
            LOOKUP_BASE64_ALPHABET[i] = (byte) ('A' + i);
        }

        for (int i = 26, j = 0; i <= 51; i++, j++) {
            LOOKUP_BASE64_ALPHABET[i] = (byte) ('a' + j);
        }

        for (int i = 52, j = 0; i <= 61; i++, j++) {
            LOOKUP_BASE64_ALPHABET[i] = (byte) ('0' + j);
        }
        LOOKUP_BASE64_ALPHABET[62] = (byte) '+';
        LOOKUP_BASE64_ALPHABET[63] = (byte) '/';

    }


    /**
     * @param binaryData
     * @return byte[]
     */
    public static byte[] encode(byte[] binaryData) {


        int lengthDataBits = binaryData.length * EIGHTBIT;
        int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
        int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
        byte encodedData[] = null;


        if (fewerThan24bits != 0) { //data not divisible by 24 bit
            encodedData = new byte[(numberTriplets + 1) * 4];
        } else { // 16 or 8 bit
            encodedData = new byte[numberTriplets * 4];
        }

        byte k = 0;
        byte l = 0;
        byte b1 = 0;
        byte b2 = 0;
        byte b3 = 0;
        int encodedIndex = 0;
        int dataIndex = 0;
        int i = 0;
        for (i = 0; i < numberTriplets; i++) {

            dataIndex = i * 3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            b3 = binaryData[dataIndex + 2];

            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            encodedIndex = i * 4;
            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                    : (byte) ((b1) >> 2 ^ 0xc0);

            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
                    : (byte) ((b2) >> 4 ^ 0xf0);
            byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6)
                    : (byte) ((b3) >> 6 ^ 0xfc);

            encodedData[encodedIndex] = LOOKUP_BASE64_ALPHABET[val1];
            encodedData[encodedIndex + 1] = LOOKUP_BASE64_ALPHABET[val2
                    | (k << 4)];
            encodedData[encodedIndex + 2] = LOOKUP_BASE64_ALPHABET[(l << 2)
                    | val3];
            encodedData[encodedIndex + 3] = LOOKUP_BASE64_ALPHABET[b3 & 0x3f];
        }

        // form integral number of 6-bit groups
        dataIndex = i * 3;
        encodedIndex = i * 4;
        if (fewerThan24bits == EIGHTBIT) {
            b1 = binaryData[dataIndex];
            k = (byte) (b1 & 0x03);
            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                    : (byte) ((b1) >> 2 ^ 0xc0);
            encodedData[encodedIndex] = LOOKUP_BASE64_ALPHABET[val1];
            encodedData[encodedIndex + 1] = LOOKUP_BASE64_ALPHABET[k << 4];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        } else if (fewerThan24bits == SIXTEENBIT) {
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                    : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
                    : (byte) ((b2) >> 4 ^ 0xf0);

            encodedData[encodedIndex] = LOOKUP_BASE64_ALPHABET[val1];
            encodedData[encodedIndex + 1] = LOOKUP_BASE64_ALPHABET[val2
                    | (k << 4)];
            encodedData[encodedIndex + 2] = LOOKUP_BASE64_ALPHABET[l << 2];
            encodedData[encodedIndex + 3] = PAD;
        }
        return encodedData;
    }


    /**
     * Decodes Base64 data into octects
     *
     * @param base64Data byte array containing Base64 data
     * @return Array containing decoded data.
     */
    public static byte[] decode(byte[] base64Data) {
        // Should we throw away anything not in base64Data ?

        // handle the edge case, so we don't have to worry about it later
        if (base64Data.length == 0) {
            return new byte[0];
        }

        int numberQuadruple = base64Data.length / FOURBYTE;
        byte decodedData[] = null;
        byte b1 = 0, b2 = 0, b3 = 0, b4 = 0, marker0 = 0, marker1 = 0;

        int encodedIndex = 0;
        int dataIndex = 0;
        {
            // this block sizes the output array properly - rlw
            int lastData = base64Data.length;
            // ignore the '=' padding
            while (base64Data[lastData - 1] == PAD) {
                if (--lastData == 0) {
                    return new byte[0];
                }
            }
            decodedData = new byte[lastData - numberQuadruple];
        }

        for (int i = 0; i < numberQuadruple; i++) {
            dataIndex = i * 4;
            marker0 = base64Data[dataIndex + 2];
            marker1 = base64Data[dataIndex + 3];

            b1 = BASE64_ALPHABET[base64Data[dataIndex]];
            b2 = BASE64_ALPHABET[base64Data[dataIndex + 1]];

            if (marker0 != PAD && marker1 != PAD) {     //No PAD e.g 3cQl
                b3 = BASE64_ALPHABET[marker0];
                b4 = BASE64_ALPHABET[marker1];

                decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4)
                        | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex + 2] = (byte) (b3 << 6 | b4);
            } else if (marker0 == PAD) {    //Two PAD e.g. 3c[Pad][Pad]
                decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
            } else if (marker1 == PAD) {    //One PAD e.g. 3cQ[Pad]
                b3 = BASE64_ALPHABET[marker0];
                decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4)
                        | ((b3 >> 2) & 0xf));
            }
            encodedIndex += 3;
        }
        return decodedData;
    }
}


