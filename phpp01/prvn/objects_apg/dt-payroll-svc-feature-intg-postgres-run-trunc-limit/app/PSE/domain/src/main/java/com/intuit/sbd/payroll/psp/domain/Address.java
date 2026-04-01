package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;


/**
 * Hand-written business logic
 */
@Slf4j
public class Address extends BaseAddress implements IUpdatable {

    public static final Pattern ZIP_CODE_PATTERN = Pattern.compile("((\\d){5})(\\-)?((\\d){4})?$");
    public static final String ADDRESS_SPLIT_REGEX = "\\r?\\n|\\r";
    public static final String NUMBER_SPECIAL_CHAR_REGEX = "^[0-9]+$";
    public static final String ONLY_DIGITS = "[^0-9]";
    public static final String US = "US";
    public static final String USA = "USA";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Address() {
        super();
    }

    public ProcessResult validateAddress(EntityName pOwnerEntityName, String pOwnerEntityId) {
        ProcessResult processResult = new ProcessResult();

        if ((getAddressLine1() == null) ||
                (!Validator.isValidLength(getAddressLine1(), 1, 80))) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "AddressLine1");
        }

        if (!Validator.isValidLength(getAddressLine2(), 0, 80)) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "AddressLine2");
        }

        if (!Validator.isValidLength(getAddressLine3(), 0, 80)) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "AddressLine3");
        }

        if ((getCity() == null) ||
                !(Validator.isValidLength(getCity(), 1, 255))) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "City");
        }

        if ((getState() == null) ||
                !(Validator.isValidLength(getState(), 1, 21))) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "State");
        }

        if ((getZipCode() == null) ||
                !(Validator.isValidLength(getZipCode(), 1, 13))) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "ZipCode");
        }

        if (!Validator.isValidLength(getZipCodeExtension(), 0, 10)) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "ZipCodeExtension");
        }

        if (!Validator.isValidLength(getCountry(), 0, 255)) {
            processResult.getMessages().InvalidValue(pOwnerEntityName, pOwnerEntityId, "Country");
        }

        return processResult;
    }

    /**
     * Must be called with an address that has been persisted to the database (e.g. has a unique id associated with it)
     *
     * @return Companies that have this address as either their mailing or legal address
     */
    public DomainEntitySet<Company> findCompanyForAddress() {
        DomainEntitySet<Company> retList = new DomainEntitySet<Company>();

        retList.addAll(Application.<Company>find(Company.class, Company.MailingAddress().equalTo(this)));
        retList.addAll(Application.<Company>find(Company.class, Company.LegalAddress().equalTo(this)));

        return retList;
    }

    public enum States {
        AL, AK, AS, AZ, AR, CA, CO, CT, DE, DC, FM, FL, GA, GU, HI, ID, IL, IN, IA, KS, KY, LA, ME, MH, MD, MA, MI, MN, MS, MO, MT, NE, NV, NH, NJ, NM, NY, NC, ND, MP, OH, OK, OR, PW, PA, PR, RI, SC, SD, TN, TX, UT, VT, VI, VA, WA, WV, WI, WY;

        public static boolean isValid(String pStateCode) {
            boolean isValid = false;
            if (pStateCode != null) {
                try {
                    States.valueOf(pStateCode.toUpperCase()).toString();
                    isValid = true;
                } catch (Exception e) {
                    //Trap exception do nothing
                }
            }
            return isValid;
        }

    }

    public String getFullZipCode() {
        String zipCode = "";
        if (getZipCode() != null) {
            zipCode += getZipCode();
        }

        if (getZipCodeExtension() != null) {
            zipCode += "-" + getZipCodeExtension();
        }

        return zipCode;
    }

    // ----- QBDT Token overrides -----

    @Override
    public void setAddressLine2(String pAddressLine2) {
        if (!ObjectUtils.equals(getAddressLine2(), pAddressLine2)) {
            onUpdate();
        }
        super.setAddressLine2(pAddressLine2);
    }

    @Override
    public void setAddressLine1(String pAddressLine1) {
        if (!ObjectUtils.equals(getAddressLine1(), pAddressLine1)) {
            onUpdate();
        }
        super.setAddressLine1(pAddressLine1);
    }

    @Override
    public void setCity(String pCity) {
        if (!ObjectUtils.equals(getCity(), pCity)) {
            onUpdate();
        }
        super.setCity(pCity);
    }

    @Override
    public void setState(String pState) {
        if (!ObjectUtils.equals(getState(), pState)) {
            onUpdate();
        }
        super.setState(pState);
    }

    @Override
    public void setZipCode(String pZipCode) {
        if (!ObjectUtils.equals(getZipCode(), pZipCode)) {
            onUpdate();
        }
        super.setZipCode(pZipCode);
    }

    @Override
    public void setZipCodeExtension(String pZipCodeExtension) {
        if (!ObjectUtils.equals(getZipCodeExtension(), pZipCodeExtension)) {
            onUpdate();
        }
        super.setZipCodeExtension(pZipCodeExtension);
    }

    @Override
    public void setCompany(Company pCompany) {
        if (!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);
    }

    @Override
    public void setIndividual(Individual pIndividual) {
        if (!ObjectUtils.equals(getIndividual(), pIndividual)) {
            onUpdate();
        }
        super.setIndividual(pIndividual);
    }

    public void onUpdate() {
        if (getCompany() != null) {
            getCompany().onUpdate();
        } else if (getIndividual() != null && getIndividual() instanceof Employee) {
            ((Employee) getIndividual()).onUpdate();
        }
    }

    public String getStreetAddress(String delimiter) {
        StringBuffer streetAddress = new StringBuffer();

        appendIfNotEmpty(streetAddress, getAddressLine1(), true, delimiter);
        appendIfNotEmpty(streetAddress, getAddressLine2(), true, delimiter);
        appendIfNotEmpty(streetAddress, getAddressLine3(), true, delimiter);

        return streetAddress.toString();
    }

    public String getStreetAddress() {
        String delimiter = StringUtils.CR + StringUtils.LF;
        return getStreetAddress(delimiter);
    }

    public String getFullAddress() {
        StringBuffer fullAddressBuffer = new StringBuffer();

        appendIfNotEmpty(fullAddressBuffer, getAddressLine1(), true);
        appendIfNotEmpty(fullAddressBuffer, getAddressLine2(), true);
        appendIfNotEmpty(fullAddressBuffer, getAddressLine3(), true);
        appendIfNotEmpty(fullAddressBuffer, getCity(), true);
        appendIfNotEmpty(fullAddressBuffer, getState(), true);
        appendIfNotEmpty(fullAddressBuffer, getZipCode(), true);
        appendIfNotEmpty(fullAddressBuffer, getZipCodeExtension(), true);
        appendIfNotEmpty(fullAddressBuffer, getCountry(), false);

        return fullAddressBuffer.toString();
    }

    public static boolean isAddressEqual(Address a, Address b) {
        boolean isAddressLine1Equal = equalsIgnoreCase(a.getAddressLine1(), b.getAddressLine1());
        boolean isAddressLine2Equal = equalsIgnoreCase(a.getAddressLine2(), b.getAddressLine2());
        boolean isAddressLine3Equal = equalsIgnoreCase(a.getAddressLine3(), b.getAddressLine3());
        boolean isCityEqual = equalsIgnoreCase(a.getCity(), b.getCity());
        boolean isStateEqual = equalsIgnoreCase(a.getState(), b.getState());
        boolean isCountryEqual = (
                // either both countries are same or one is 'US' & other is 'USA'
                (equalsIgnoreCase(a.getCountry(), b.getCountry())) ||
                        (("US".equals(a.getCountry()) || "USA".equals(a.getCountry()))
                                && ("US".equals(b.getCountry()) || "USA".equals(b.getCountry())))
        );
        boolean isZipEqual = areNumbersEqual(a.getZipCode(), b.getZipCode());
        boolean isZipExtnEqual = areNumbersEqual(a.getZipCodeExtension(), b.getZipCodeExtension());

        return isAddressLine1Equal && isAddressLine2Equal && isAddressLine3Equal && isCityEqual && isStateEqual /*&& isCountryEqual*/
                && isZipEqual && isZipExtnEqual;
    }

    private void appendIfNotEmpty(StringBuffer fullAddressBuffer, String addressPart, boolean includeDelimiter, String delimiter) {
        if (StringUtils.isEmpty(addressPart)) {
            return;
        }

        fullAddressBuffer.append(addressPart);

        if (includeDelimiter) {
            delimiter = StringUtils.isEmpty(delimiter) ? "," : delimiter;
            fullAddressBuffer.append(delimiter);
        }
    }

    private void appendIfNotEmpty(StringBuffer fullAddressBuffer, String addressPart, boolean includeDelimiter) {
        appendIfNotEmpty(fullAddressBuffer, addressPart, includeDelimiter, null);
    }

    private static boolean areNumbersEqual(String a, String b) {
        return StringUtils.equals(extractDigitsOnly(a), extractDigitsOnly(b));
    }

    private static CharSequence extractDigitsOnly(String value) {
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return value.replaceAll("[^0-9]", StringUtils.EMPTY);
    }

    public static String buildPostalCode(String zipCode, String zipCodeExtension){

        StringBuffer sb = new StringBuffer();
        sb.append(extractDigitsOnly(zipCode));
        sb.append(extractDigitsOnly(zipCodeExtension));
        return sb.toString();

    }

}


