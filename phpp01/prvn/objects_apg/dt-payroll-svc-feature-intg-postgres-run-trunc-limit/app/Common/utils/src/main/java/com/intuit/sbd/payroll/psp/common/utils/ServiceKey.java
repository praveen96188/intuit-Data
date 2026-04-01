package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.domain.AssetItemCode;

import java.io.PrintStream;
import java.text.StringCharacterIterator;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Brian Ursillo
 *
 * ServiceKey - Ported and refactored based on psappnum.c/.h
 */
public class ServiceKey {

    private static final int APPNUMLENGTH = 19;		// Number of characters in Application Number
    private static final long MAXKEYVAL_CRI = 100000L;	// Maximum value used for security key
    private static final long MAXEINVAL_CRI = 100000L;	// Maximum value used from EIN
    private static final long MAXKEYVAL_SUB = 10000L;	// Maximum value used for security key
    private static final long MAXEINVAL_SUB = 10000L;	// Maximum value used from EIN
    private static final long MAXEXTSECURITY = 10000L;	// Maximum value used for extension security
    private static final long CREDITUNIT = 10000L;	// Credit limit units in $
    private double m_limit;
    private long m_promo;
    private Calendar m_expirationDate;
    private String m_ein;
    private String m_appNum;
    private String m_extensionKey;
    private String m_subId;
    private ServiceType m_service;
    private int m_subType;
    private FormatType m_keyFormat;
    private static final Map<Character, ServiceType> serviceTypeMap = new HashMap<Character, ServiceType>() {

        {
            put('0', ServiceType.SVC_INVALID);
            put('1', ServiceType.SVC_TAX);
            put('2', ServiceType.SVC_DIRDEP);
            put('3', ServiceType.SVC_TAX_DIRDEP);
            put('4', ServiceType.SVC_BASIC);
            put('5', ServiceType.SVC_BASIC_DD);
            put('6', ServiceType.SVC_PREMIUM);
            put('7', ServiceType.SVC_PREMIUM_DD);
            put('8', ServiceType.SVC_BASICDISK);
            put('9', ServiceType.SVC_EXTENDED);
            put('A', ServiceType.SVC_BPLUS_FED);
            put('B', ServiceType.SVC_BPLUS_FEDST);
            put('C', ServiceType.SVC_TYPE_C);
            put('D', ServiceType.SVC_HRORG);
            put('E', ServiceType.SVC_HRCOMPL);
        }
    };

    private static final Map<AssetItemCode, ServiceType> assetItemServiceTypeMap = new HashMap<AssetItemCode, ServiceType>() {
        {
            put(AssetItemCode.Assisted, ServiceType.SVC_BASIC);
            put(AssetItemCode.AssistedAdvantage, ServiceType.SVC_BASIC);
            put(AssetItemCode.DIY, ServiceType.SVC_BASIC);
            put(AssetItemCode.DIYDiskDelivery, ServiceType.SVC_BASICDISK);
            put(AssetItemCode.EmployeeOrganizer, ServiceType.SVC_HRORG);
            put(AssetItemCode.EmploymentRegulation, ServiceType.SVC_HRCOMPL);
        }
    };

    private static enum FormatType {

        FMT_INVALID,
        FMT_CRI,
        FMT_SUB,
        FMT_EXT //Extended key with letters ST
    }

    /**
     * Gets the extension security code from the subscription number
     *
     * @param subIdString - Subscription number
     * @return security code
     */
    private static long getExtensionSecurityCode(String subIdString) {
        long extSecurityCode;
        long subID = Integer.valueOf(subIdString);

        extSecurityCode = subID % MAXEXTSECURITY;
        extSecurityCode = extSecurityCode ^ 0x1555;	// Flip every other bit
        extSecurityCode = extSecurityCode % MAXEXTSECURITY;

        return extSecurityCode;
    }

/*---------------------------------------------------------------------------
 *	ExtValueToDate
 *
 *		Convert a 5-digit numeric value to a date string (YYYY_MM_DD).
 *		This uses a proprietary algorithm.  It is similar to Julian date,
 *		but it does not account for leap year or days in month.
 *
 *
 * Date		By						Description
 * --------	-----------------	--------------------------------------------
 * 11/20/01	EAbbe					Initial revision.
 */
    private static Calendar extValueToDate(int value)
    {
        int year;
        int month;
        int day;

        int temp = value;
        day = temp % 32;
        temp /= 32;
        month = temp % 13;
        year = temp / 13;

        //  Add the year offset
        year += 2000;

        return new GregorianCalendar(year, month-1, day);
    }

    /**
     *
     * Converts a date string to a numeric value
     *
     * @param extDate - Date string in format YYYY_MM_DD
     *
     * @return a five-digit numeric value to represent the date
     *
     */
    private static long extDateToValue(String extDate) {
        if (extDate.length() < 9) {
            return 0;
        }

        String pBuff = extDate.substring(0, 4);
        int year = Integer.valueOf(pBuff);
        year -= 2000;
        if (year < 0 || year > 239) {
            return 0;
        }

        pBuff = extDate.substring(5, 7);
        int month = Integer.valueOf(pBuff);
        if (month > 12) {
            return 0;
        }

        pBuff = extDate.substring(8, 10);
        if (pBuff == null) {
            return 0;
        }

        int day = Integer.valueOf(pBuff);
        if (day > 31) {
            return 0;
        }

        int numericDate = year * 13;
        numericDate = (numericDate + month) * 32;
        numericDate += day;

        return numericDate;
    }

    private static int getExtCheckSumPos() {
        return 1;
    }

    private static String encodeDiskDeliveryKey(String subId, String extDate, PSExtensionType extensionType, int subType) {
        long extTrueValue;
        long extHashValue;
        long extHashHigh1;
        long extHashLow4;
        long extSecurityCode;
        int checkSumPos;
        int chkSum = 0;
        String pBuff;
        String extensionKey;

        // Convert the date to a (5-digit) numeric value, hash it and break it up
        extSecurityCode = getExtensionSecurityCode(subId);

        extTrueValue = extDateToValue(extDate);
        extHashValue = (extTrueValue + 5 * extSecurityCode) % 100000;
        extHashHigh1 = extHashValue / 10000;
        extHashLow4 = extHashValue % 10000;

        //  encode the extension key
        pBuff = String.format("%01d%01d%01d%01d-%04d-%04d",
                extensionType.ordinal(),
                chkSum,
                subType,
                extHashHigh1,
                extHashLow4,
                extSecurityCode % 10000L);

        // Calculate and store the checksum
        checkSumPos = getExtCheckSumPos();
        chkSum = getCheckSum(pBuff, checkSumPos);
        char[] appNumArray = pBuff.toCharArray();
        appNumArray[checkSumPos] = Integer.toString(chkSum % 10).charAt(0);

        extensionKey = new String(appNumArray);
        return extensionKey;

    }

    private static FormatType getKeyFormatFromServiceKey(String serviceKey) {

        if (Character.isDigit(serviceKey.charAt(0))) {
            return getKeyFormatFromServiceType(serviceTypeMap.get(serviceKey.charAt(0)));

        } else if (Character.isLetter(serviceKey.charAt(0))) {
            return getKeyFormatFromServiceType(serviceTypeMap.get(Character.toUpperCase(serviceKey.charAt(0))));
        }

        throw new IllegalArgumentException(String.format("Cannot determine service key format from service key: %s", serviceKey));
    }

    private static double getLimitFromCRIKey(String serviceKey) {
        double limit;
        char[] appnumArray = serviceKey.toCharArray();
        limit = (appnumArray[2] - '0') * 100 + (appnumArray[3] - '0') * 10 + (appnumArray[4] - '0') * 1;
        limit *= CREDITUNIT;
        return limit;
    }

    private static long getPromoFromCRIKey(String serviceKey) {
        char[] appnumArray = serviceKey.toCharArray();
        long promo = appnumArray[12] - '0';
        promo = promo * 10 + appnumArray[13] - '0';
        promo = promo * 10 + appnumArray[14] - '0';
        promo = promo * 10 + appnumArray[16] - '0';
        promo = promo * 10 + appnumArray[17] - '0';
        promo = promo * 10 + appnumArray[18] - '0';
        return promo;

    }

    public static String getSubscriptionIdFromServiceKey(String serviceKey) {
        long subHashHigh;
        long subHashLow;
        long subHashVeryHigh;
        long subHash;
        long subID;
        long keyVal;
        char[] appnumArray = serviceKey.toCharArray();
        subHashHigh = (appnumArray[1] - '0') * 100 + (appnumArray[2] - '0') * 10 + (appnumArray[3] - '0') * 1;

        keyVal = appnumArray[5] - '0';
        keyVal = keyVal * 10 + appnumArray[6] - '0';
        keyVal = keyVal * 10 + appnumArray[7] - '0';
        keyVal = keyVal * 10 + appnumArray[8] - '0';

        subHashVeryHigh = appnumArray[10] - '0';

        subHashLow = appnumArray[11] - '0';
        subHashLow = subHashLow * 10 + appnumArray[12] - '0';
        subHashLow = subHashLow * 10 + appnumArray[13] - '0';
        subHashLow = subHashLow * 10 + appnumArray[16] - '0';
        subHashLow = subHashLow * 10 + appnumArray[17] - '0';
        subHashLow = subHashLow * 10 + appnumArray[18] - '0';

        subHash = subHashVeryHigh * 1000000000L + subHashHigh * 1000000 + subHashLow;
        subID = subHash ^ keyVal;
        return String.format("%010d", subID);
    }

    private static int getCheckSum(String buffer, int checkSumPos) {

        int checkSum = -(buffer.charAt(checkSumPos) - '0');

        StringCharacterIterator iter = new StringCharacterIterator(buffer);
        for (char c = iter.first(); c != StringCharacterIterator.DONE; c = iter.next()) {
            if (Character.isDigit(c)) {
                checkSum += c - '0';
            } else if (Character.isLetter(c)) {
                checkSum += Character.toUpperCase(c) - 'A' + ServiceType.SVC_EXTENDED.ordinal();
            }
        }

        return checkSum % 10;
    }

    private static int getCheckSumPos(FormatType keyFormat) {
        int position;

        switch (keyFormat) {
            case FMT_CRI:
                position = 1;
                break;

            case FMT_SUB:
            case FMT_EXT:
            default:
                position = 15;
                break;
        }

        return position;
    }

    private static long getCRIFormatKeyVal(String ein, double limit, long promo, ServiceType service) {
        long creditVal = (long) (limit / CREDITUNIT);
        long keyVal = 0;
        long place = 1;
        for (int i = ein.length() - 1; i >= 0; i--) {
            char character = ein.charAt(i);
            if ('0' <= character && character <= '9') {
                keyVal += (character - '0') * place;
                place *= 10;
                if (keyVal > MAXEINVAL_CRI) {
                    break;
                }
            }
        }
        keyVal = keyVal % MAXEINVAL_CRI;

        keyVal = keyVal ^ promo;
        keyVal = keyVal ^ (creditVal * service.ordinal());
        keyVal = keyVal % MAXKEYVAL_CRI;

        return keyVal;

    }

    private static long getSubscriptionFormatKeyVal(String ein, String subIDStr) {
        long keyVal = 0;
        long place = 1;


        for (int i = ein.length() - 1; i >= 0; i--) {
            char character = ein.charAt(i);
            if ('0' <= character && character <= '9') {
                keyVal += (character - '0') * place;
                place *= 10;
                if (keyVal > MAXEINVAL_SUB) {
                    break;
                }
            }
        }
        keyVal = keyVal % MAXEINVAL_SUB;
        long subID = Integer.parseInt(subIDStr);
        keyVal = keyVal % MAXEINVAL_SUB;
        keyVal = keyVal ^ subID;
        keyVal = keyVal % MAXKEYVAL_SUB;
        return keyVal;
    }

    private static FormatType getKeyFormatFromServiceType(ServiceType service) {
        FormatType keyFormat;
        switch (service) {

            case SVC_TAX:
            case SVC_DIRDEP:
            case SVC_TAX_DIRDEP:
                keyFormat = FormatType.FMT_CRI;
                break;

            case SVC_BASIC:
            case SVC_BASIC_DD:
            case SVC_PREMIUM:
            case SVC_PREMIUM_DD:
            case SVC_BASICDISK:
                keyFormat = FormatType.FMT_SUB;
                break;


            case SVC_BPLUS_FED:
            case SVC_BPLUS_FEDST:
            case SVC_HRORG:
            case SVC_HRCOMPL:
                keyFormat = FormatType.FMT_EXT;
                break;

            default:
                keyFormat = FormatType.FMT_INVALID;
                break;
        }

        return keyFormat;
    }

    private boolean isClassicService(ServiceType servType) {
        return servType.ordinal() < ServiceType.SVC_EXTENDED.ordinal();
    }

    private void setApplicationNumber(String appNum) {
        StringBuffer buffer = new StringBuffer();
        int digitCount = 0;

        StringCharacterIterator iter = new StringCharacterIterator(appNum);
        for (char c = iter.first(); c != StringCharacterIterator.DONE; c = iter.next()) {

            if (Character.isLetterOrDigit(c)) {
                buffer.append(c);
                digitCount++;

                switch (m_keyFormat) {
                    case FMT_CRI:
                        switch (digitCount) {
                            case 5:
                            case 10:
                            case 13:
                                buffer.append('-');
                                break;
                            default:
                                break;
                        }
                        break;

                    case FMT_SUB:
                    case FMT_EXT:
                        switch (digitCount) {
                            case 4:
                            case 8:
                            case 12:
                                buffer.append('-');
                                break;
                            default:
                                break;

                        }
                        break;

                    default:
                        break;
                }
            }
        }

        m_appNum = buffer.toString();
    }

    private void setKeyFormatFromServiceType() {


        switch (m_service) {

            case SVC_TAX:
            case SVC_DIRDEP:
            case SVC_TAX_DIRDEP:
                m_keyFormat = FormatType.FMT_CRI;
                break;

            case SVC_BASIC:
            case SVC_BASIC_DD:
            case SVC_PREMIUM:
            case SVC_PREMIUM_DD:
            case SVC_BASICDISK:
                m_keyFormat = FormatType.FMT_SUB;
                break;


            case SVC_BPLUS_FED:
            case SVC_BPLUS_FEDST:
            case SVC_HRORG:
            case SVC_HRCOMPL:
                m_keyFormat = FormatType.FMT_EXT;
                break;

            default:
                m_keyFormat = FormatType.FMT_INVALID;
                break;

        }
    }

    private ServiceType peekSubscriptionType() {
        int checkSumPos = getCheckSumPos(m_keyFormat);
        ServiceType retType = ServiceType.SVC_INVALID;

        // Do the Check Sum check first - only want to return the type of a
        // somewhat valid key. (Don't care about EIN agreement)
        char[] appNumArray = m_appNum.toCharArray();
        if (getCheckSum(m_appNum, checkSumPos) == m_appNum.charAt(checkSumPos) - '0') {
            if (Character.isDigit(appNumArray[0])) {
                retType = ServiceType.values()[appNumArray[0] - '0'];
            } else if (Character.isLetter(appNumArray[0])) // extended key format with letters for ST
            {
                retType = ServiceType.values()[Character.toUpperCase(appNumArray[0]) - 'A' + ServiceType.SVC_EXTENDED.ordinal()];
            }
        }

        return retType;
    }

    private void encode() {
        m_appNum = encode(m_subId, m_service, m_ein);
    }

    private void encodeCRIFormat() {
        m_appNum = encodeCRIFormat(m_ein, m_limit, m_promo, m_service);
    }

    private void decode() {
        if (Character.isDigit(m_appNum.charAt(0))) {
            m_service = serviceTypeMap.get(m_appNum.charAt(0));


        } else if (Character.isLetter(m_appNum.charAt(0))) {
            m_service = serviceTypeMap.get(Character.toUpperCase(m_appNum.charAt(0)));

        } else {
            throw new IllegalArgumentException("Invalid service key");
        }

        m_keyFormat = getKeyFormatFromServiceType(m_service);
        int checkSumPos = getCheckSumPos(m_keyFormat);

        if (getCheckSum(m_appNum, checkSumPos) != (int) m_appNum.charAt(checkSumPos) - (int) '0') {
            throw new IllegalArgumentException("Checksum failed");
        }

        switch (m_keyFormat) {
            case FMT_CRI:
                m_limit = getLimitFromCRIKey(m_appNum);
                m_promo = getPromoFromCRIKey(m_appNum);
                break;

            case FMT_SUB:
            case FMT_EXT:
            default:
                m_subId = getSubscriptionIdFromServiceKey(m_appNum);
                break;
        }

        if (m_appNum.length() != APPNUMLENGTH) {

            throw new IllegalArgumentException("Invalid service key");
        }

    }

    private void decodeExtensionKey(String extensionKey, String subId)
    {        
        int extensionDigit = extensionKey.charAt(0) - '0';
        if(extensionDigit != 0 && extensionDigit != 1) {
            throw new IllegalArgumentException("unknown extension type in extension key");
        }

        m_subType = extensionKey.charAt(2) - '0';

        // Verify the checksum
        int checkSumPos = getExtCheckSumPos();
        int checkSum = getCheckSum(extensionKey, checkSumPos);
        if (checkSum != extensionKey.charAt(checkSumPos) - '0') {
            throw new IllegalArgumentException("bad checksum in extension key");
        }

        // Extract the Security Code
        int extSecurityCode = extensionKey.charAt(10) - '0';
        extSecurityCode = extSecurityCode * 10 + extensionKey.charAt(11) - '0';
        extSecurityCode = extSecurityCode * 10 + extensionKey.charAt(12) - '0';
        extSecurityCode = extSecurityCode * 10 + extensionKey.charAt(13) - '0';

        // Extract the hashed value
        int extHashValue = extensionKey.charAt(3) - '0';
        extHashValue = extHashValue * 10 + extensionKey.charAt(5) - '0';
        extHashValue = extHashValue * 10 + extensionKey.charAt(6) - '0';
        extHashValue = extHashValue * 10 + extensionKey.charAt(7) - '0';
        extHashValue = extHashValue * 10 + extensionKey.charAt(8) - '0';

        // Get the true value and convert it to a date string
        extHashValue += 100000;  // I don't trust mod of negative numbers
        extHashValue -= 5*extSecurityCode;
        int extTrueValue = extHashValue % 100000;
        m_expirationDate = extValueToDate(extTrueValue);

        // Verify the security code
        if (extSecurityCode != getExtensionSecurityCode(subId)) {
            throw new IllegalArgumentException("invalid security code in extension key");
        }
    }

    public static enum ServiceType {

        SVC_INVALID,
        SVC_TAX, // Tax payment and filing
        SVC_DIRDEP, // Employee direct deposit
        SVC_TAX_DIRDEP, // Tax payment and filing with employee direct deposit

        SVC_BASIC, // Basic Service (also used by Homer)
        SVC_BASIC_DD, // Basic Service + Direct Deposit
        SVC_PREMIUM, // Premium Service
        SVC_PREMIUM_DD, // Premium Service + Direct Deposit

        SVC_BASICDISK, // Basic Disk Service
        SVC_PLACEHOLDER, //SVC_EXTENDED needs to be 10
        SVC_EXTENDED, // this is not a service per se, but it is used as a marker

        SVC_BPLUS_FED, // 'A'  New extended services for FLEXSUBS
        SVC_BPLUS_FEDST, // 'B'  Those can't be printf'd as '%d'
        SVC_TYPE_C, // Placeholder so HROrg=13
        SVC_HRORG, // 'D'
        SVC_HRCOMPL // 'E'
    }

    public static enum PSExtensionType {
        EXT_INVALID,
        EXT_EXPIREDATE
    }

    public static String encodeCRIFormat(String ein, double limit, long promo, ServiceType serviceType) {
        String m_appNum;
        long creditVal = (long) (limit / CREDITUNIT);
        long keyVal = getCRIFormatKeyVal(ein, limit, promo, serviceType);
        int checkSumPos = getCheckSumPos(FormatType.FMT_CRI);
        int chkSum = 0;

        m_appNum = String.format("%01d%01d%03d-%05d-%03d-%03d",
                serviceType.ordinal(),
                chkSum,
                creditVal,
                keyVal,
                promo / 1000,
                promo % 1000);
        chkSum = getCheckSum(m_appNum, checkSumPos);
        StringBuffer appNum = new StringBuffer(m_appNum.substring(0, checkSumPos));
        appNum.append(chkSum % 10);
        if (m_appNum.length() > checkSumPos) {
            String remainder = m_appNum.substring(checkSumPos + 1, m_appNum.length());
            appNum.append(remainder);
        }

        m_appNum = appNum.toString();
        return m_appNum;
    }

    public static String encode(String subId, ServiceType serviceType, String ein) {
        String m_appNum;
        FormatType keyFormat = getKeyFormatFromServiceType(serviceType);

        long keyVal = getSubscriptionFormatKeyVal(ein, subId);
        long subID = Integer.parseInt(subId);
        long subHash = subID ^ keyVal;
        long subHashHigh = (subHash / 1000000L) % 1000;
        long subHashVeryHigh = subHash / 1000000000L;
        long subHashLow = subHash % 1000000L;
        int checkSumPos = getCheckSumPos(keyFormat);
        int chkSum = 0;

        switch (keyFormat) {
            case FMT_CRI:
                throw new IllegalArgumentException("Wrong encoder for CRI format");


            case FMT_EXT:
                m_appNum = String.format("%c%03d-%04d-%01d%03d-%01d%03d",
                        serviceType.ordinal() + 'A' - ServiceType.SVC_BPLUS_FED.ordinal(), // 1 digit
                        subHashHigh, // 3 digits
                        keyVal, // 4 digits
                        subHashVeryHigh, // 1 digit
                        subHashLow / 1000, // 3 digits
                        chkSum, // 1 digit
                        subHashLow % 1000);								// 3 digits
                break;

            case FMT_SUB:
            default:
                m_appNum = String.format("%01d%03d-%04d-%01d%03d-%01d%03d",
                        serviceType.ordinal(), // 1 digit
                        subHashHigh, // 3 digits
                        keyVal, // 4 digits
                        subHashVeryHigh, // 1 digit
                        subHashLow / 1000, // 3 digits
                        chkSum, // 1 digit
                        subHashLow % 1000);		// 3 digits
                break;

        }

        chkSum = getCheckSum(m_appNum, checkSumPos);
        StringBuffer appNum = new StringBuffer(m_appNum.substring(0, checkSumPos));
        appNum.append(chkSum % 10);
        if (m_appNum.length() > checkSumPos) {
            String remainder = m_appNum.substring(checkSumPos + 1, m_appNum.length());
            appNum.append(remainder);
        }

        m_appNum = appNum.toString();
        return m_appNum;

    }

    public static String decode(String appNum) {

        String subIDString = null;

        FormatType keyFormat = getKeyFormatFromServiceKey(appNum);

        int checkSumPos = getCheckSumPos(keyFormat);

        if (getCheckSum(appNum, checkSumPos) != appNum.charAt(checkSumPos) - '0') {
            throw new IllegalArgumentException(String.format("Checksum failed on service key: %s", appNum));
        }

        switch (keyFormat) {
            case FMT_CRI:
                // No subscription ID
                break;

            case FMT_SUB:
            case FMT_EXT:
            default:
                subIDString = getSubscriptionIdFromServiceKey(appNum);
                break;
        }

        return subIDString;
    }

    public static String getExtensionKey(String subId, PSExtensionType extensionType, Calendar expireDate, int extensionReserved) {
        String dateString = String.format("%1tY_%<tm_%<td", expireDate);
        return encodeDiskDeliveryKey(subId, dateString, extensionType, extensionReserved);
    }

    private boolean hasExtensionKey() {
        return m_service.equals(ServiceType.SVC_BASICDISK);
    }

    private static boolean isCRIService(ServiceType service) {
        switch (service) {
            case SVC_TAX:
            case SVC_DIRDEP:
            case SVC_TAX_DIRDEP:
                return true;
            default:
                return false;
        }

    }

    /**
     * Construct a service key for the given EIN, subscription ID, and service
     *
     * @param ein   federal employer identification number
     * @param subId subscription (agreement) number
     * @param service the offering that this service key enables
     */
    public ServiceKey(String ein, String subId, ServiceType service) {
        this.m_ein = ein;
        this.m_subId = subId;
        this.m_service = service;
        setKeyFormatFromServiceType();
        encode();
    }
    
    public ServiceKey(String ein, String subId, AssetItemCode pAssetItemCode) {
        this.m_ein = ein;
        this.m_subId = subId;
        this.m_service = assetItemServiceTypeMap.get(pAssetItemCode);
        if(m_service == ServiceType.SVC_BASICDISK) {
            throw new IllegalArgumentException("Disk Delivery service key should use constructor with expiration date");
        }
        setKeyFormatFromServiceType();
        encode();
    }

    /**
     * Construct a Disk Delivery service key
     *
     * @param ein   federal employer identification number
     * @param subId subscription (agreement) number
     * @param expireDate the expiration date of the service key
     * @param subType   the subtype of the service (Basic Unlimited, etc.)
     */
    public ServiceKey(String ein, String subId, Calendar expireDate, int subType) {

        this.m_subId = subId;
        this.m_ein = ein;
        String dateString = String.format("%1tY_%<tm_%<td", expireDate);
        m_service = ServiceType.SVC_BASICDISK;
        setKeyFormatFromServiceType();
        encode();
        m_extensionKey = encodeDiskDeliveryKey(subId, dateString, PSExtensionType.EXT_EXPIREDATE, subType);
    }

    /**
     * Construct a CRI format service key
     *
     * @param limit account credit limit (a multiple of 10000)
     * @param promo promotion code
     * @param ein   federal employer identification number
     * @param service valid service types for CRI format are SVC_TAX, SVC_DIRDEP, and SVC_TAX_DIRDEP
     */
    public ServiceKey(double limit, long promo, String ein, ServiceType service) {
        if (!isCRIService(service)) {
            throw new IllegalArgumentException("CRI service keys are for CRI services only");
        }
        this.m_limit = limit;
        this.m_promo = promo;
        this.m_ein = ein;
        this.m_service = service;
        setKeyFormatFromServiceType();
        encodeCRIFormat();
    }

    /**
     * Construct from a service key string and an EIN
     *
     * @param serviceKey the service key string (aka application number)
     * @param ein   federal employer identification number
     */
    public ServiceKey(String serviceKey, String ein) {
        m_ein = ein;
        m_keyFormat = getKeyFormatFromServiceKey(serviceKey);
        setApplicationNumber(serviceKey);
        decode();
        if (hasExtensionKey())
        {
            throw new IllegalArgumentException("Disk Delivery service key should use constructor with extension key");
        }
    }

    /**
     * Construct from a service key string, extension key string and an EIN
     *
     * @param serviceKey the service key string (aka application number)
     * @param extensionKey the extension key string
     * @param ein   federal employer identification number
     */
    public ServiceKey(String serviceKey, String extensionKey, String ein) {
        m_ein = ein;
        m_keyFormat = getKeyFormatFromServiceKey(serviceKey);
        m_extensionKey = extensionKey;
        setApplicationNumber(serviceKey);
        decode();
        decodeExtensionKey(extensionKey, m_subId);
    }

    /**
     *
     * @return the application number (aka service key string)
     */
    public String getApplicationNumber() {
        return m_appNum;

    }

    /**
     *
     * @return the service type
     */
    public ServiceType getServiceType() {
        return m_service;
    }

    /**
     *
     * @return the expiration date for a disk delivery key
     */
    public Calendar getExpirationDate() {
        return m_expirationDate;
    }

    /**
     *
     * @return the credit limit for a CRI service key
     */
    public double getCreditLimit() {
        if (m_keyFormat != FormatType.FMT_CRI) {
            throw new IllegalStateException("Credit limit is only encoded in CRI format service keys");
        }
        return m_limit;
    }

    public String getEIN() {
        return m_ein;
    }

    public String getSubscriptionID() {
        return m_subId;
    }

    public String getExtensionKey() {
        return m_extensionKey;
    }

    public int getSubType() {
        return m_subType;
    }

    public long getPromoCode() {
        if (m_keyFormat != FormatType.FMT_CRI) {
            throw new IllegalStateException("Promo code is only encoded in CRI format service keys");
        }
        return m_promo;
    }

    /**
     * Dumps a description of the service key to the given PrintStream
     * @param outputStream  the PrintStream to receive the output
     */
    public void dump(PrintStream outputStream) {
        String output;
        switch (m_keyFormat) {
            case FMT_CRI:

                output = String.format(
                        "AppNum : %s\n"
                                + "EIN    : %s\n"
                                + "credit : %.1lf\n"
                                + "service: %d\n"
                                + "promo  : %06d\n",
                        m_appNum,
                        getEIN(),
                        getCreditLimit(),
                        getServiceType(),
                        getPromoCode());
                break;

            case FMT_SUB:
            case FMT_EXT:
            default:

                output = String.format(
                        "fidKey : %s\n"
                                + "EIN    : %s\n"
                                + "subID  : %s\n"
                                + "ST     : %c\n"
                                + "serv.  : %s\n",
                        m_appNum,
                        getEIN(),
                        getSubscriptionID(),
                        peekSubscriptionType().ordinal() + ((getServiceType().ordinal() >= ServiceType.SVC_EXTENDED.ordinal()) ? 'A' - ServiceType.SVC_EXTENDED.ordinal() : '0'),
                        isClassicService(peekSubscriptionType()) ? "Classic service" : "Extended service");
                break;

        }

        outputStream.println(output);
    }

    /**
     *
     * @return the service key and, if applicable, the extension key.
     */
    @Override
    public String toString() {
        if (hasExtensionKey()) {
            return String.format("%s %s", m_appNum, m_extensionKey);
        } else {
            return m_appNum;
        }
    }

    public static boolean isDiskDeliveryAssetCode(AssetItemCode pAssetItemCode) {
        return assetItemServiceTypeMap.get(pAssetItemCode) == ServiceType.SVC_BASICDISK;
    }

    public static String getExtensionKey(String serviceKey) {
        if (serviceKey == null) {
            return null;
        }

        String[] keys = serviceKey.split(" ");
        if (keys.length > 1) {
            return keys[1];
        }

        return null;        
    }

    public static String getServiceKey(String serviceKey) {
        if (serviceKey == null) {
            return null;
        }
        return serviceKey.split(" ")[0];
    }
}
