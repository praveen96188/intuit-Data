package com.intuit.sbd.payroll.psp.domain;


import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Hand-written business logic
 */
public class AgencyIdRequirement extends BaseAgencyIdRequirement {

    /**
     * Default constructor.
     */
    public AgencyIdRequirement()
    {
        super();
    }

    @Override
    public boolean isRequirementMet(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        //sometimes a requirement is not really a requirement, but need to borrow functionality to validate the ID
        return !getRequired() || isAgencyIdValid(companyPaymentTemplatePaymentMethod);

    }

    public String getIdToTest(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        String idToTest;
        if (getPaymentTemplateAgencyId() == null) {
            idToTest = companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getAgencyTaxpayerId();
        } else {
            DomainEntitySet<CompanyPaymentTemplateAgencyId> companyIds =
                    companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getCompanyPaymentTemplateAgencyIdCollection()
                            .find(CompanyPaymentTemplateAgencyId.Name().equalTo(getPaymentTemplateAgencyId().getName()));
            if (companyIds.isEmpty()) {
                idToTest = null;
            } else {
                idToTest = companyIds.get(0).getAgencyTaxpayerId();
            }
        }
        return idToTest;
    }

    public boolean isAgencyIdValid(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod){
        String idToTest = getIdToTest(companyPaymentTemplatePaymentMethod);
        String fedTaxId = companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getCompanyAgency().getCompany().getFedTaxId();
        boolean meetsRequirements =  matchesPattern(idToTest) &&
                meetsCustomRequirements(idToTest, fedTaxId);
        boolean meetsDefaultIdChecks = !getProhibitDefaultIds() || isNotADefaultId(idToTest, fedTaxId);
        return meetsRequirements && meetsDefaultIdChecks;
    }

    public boolean matchesPattern(String idToTest) {
        //First apply pattern before validating with EIN
        if (StringUtils.isEmpty(getPattern())) {
            return StringUtils.isNotEmpty(idToTest);
        } else {
            return !StringUtils.isEmpty(idToTest) && idToTest.matches(getPattern());
        }
    }

    public boolean meetsCustomRequirements(String idToTest, String ein) {
        ein = StringUtils.defaultString(ein).replaceAll("-", "");
        String taxId = idToTest.replaceAll("-", "");
        switch (getCustomRequirement()) {
            case None:
                return true;
            case MustNotContainFedTaxId:
                return !taxId.contains(ein);
            case MustNotFollowFedTaxId:
                return !taxId.equals(ein);
            case MustNotFollowFedTaxIdSubstitueIf8Digits:
                if (taxId.length() == 8) {
                    int checkDigit = SpcfUtils.getCheckDigit(taxId);
                    taxId += String.valueOf(checkDigit);
                }
                return !taxId.equals(ein);
            case MustFollowFedTaxId:
                return taxId.equals(ein);
            case MustStartWithFedTaxId:
                return taxId.startsWith(ein);
            case IfNotMEorTRMustFollowFedTaxId:
                if (!taxId.startsWith("ME") && !taxId.startsWith("TE")) {
                    return taxId.equals(ein);
                } else {
                    return true;
                }
            case Digits4Through12FollowFedTaxId:
            case Digits2Through10FollowFedTaxId:
            case Digits3Through11FollowFedTaxId:
                //Todo ending index is not used to exactly compare 4-15 digits or 2-11 digits, because not sure that is with or without "-"
                int index = 1;
                if (getCustomRequirement() == AgencyIdCustomRequirement.Digits4Through12FollowFedTaxId) {
                    index = 3;
                }
                if(getCustomRequirement() == AgencyIdCustomRequirement.Digits3Through11FollowFedTaxId){
                    index = 2;
                }
                if (taxId.length() > index) {
                    taxId = taxId.substring(index);
                }
                return taxId.startsWith(ein);
            case MustNotInExemptedIdList:
                List<String> exemptedIds = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.EXEMPTED_AGENCY_IDS, "").split(","));
                return !exemptedIds.contains(idToTest);
            case IFNotPatternMustFollowFedTaxId:
                return true;
            default:
                throw new RuntimeException("Custom requirement not handled");
        }

    }

    /**
     * Returns true if given a valid not default id, else returns false.
     * Rules for a non default id:
     * 1. Should not be a sequence of numbers starting from 0/1  e.g. - 123-456-78
     * 2. Should not have a single digit repeated across all the places e.g. 111-111-11
     * 3. Should not be same as federal EIN if not mandated by the agency
     *
     * @param idString       - Id to be validated
     * @param fedTaxIdString - Federal EIN
     * @return - true if the id is valid, false if it is a default id
     */
    public boolean isNotADefaultId(String idString, String fedTaxIdString) {
        if (StringUtils.isEmpty(idString)) {
            return true;
        }
        String idStringWithoutHyphenAndSpaces = idString.replaceAll("[- ]", "");
        if (StringUtils.isEmpty(idStringWithoutHyphenAndSpaces)) {
            return false;
        }
        // if id is alphanumeric return true because this is validated only for numeric ids
        if (!StringUtils.isNumeric(idStringWithoutHyphenAndSpaces)) {
            return true;
        }

        //Now we have a numeric id without hyphen and space
        /* Explaining the regex - ^(\\d)\\1+$ for repetition pattern matching
           In Java to escape \ we have to add a \ so the real pattern is - ^(\d)\1+$
           Pattern - ^(\d)\1+$
           Lets take an example - 5555
           ^ - matches start of the id
           \d - matches any digit - matches first 5 in the example, 555 left to go
           (\d) - group the first digit matched as group 1  - (5) becomes group 1
           \1 - back reference group 1 i.e. the first digit matched
           \1+ - 1 or more occurences of the first digit matched - matches 555
           $ - matches end of the id
         */
        if (idStringWithoutHyphenAndSpaces.matches("^(\\d)\\1+$")) {
            return false;
        }

        //Check if id has a sequence of numbers
        if (AgencyIdRequirement.isInNumericSequence(idStringWithoutHyphenAndSpaces)) {
            return false;
        }

        //Check if the id given is same as federal tax id if applicable i.e. agency does not mandate it to be.
        boolean followsFedTaxId = false;
        switch (getCustomRequirement()) {
            case MustFollowFedTaxId:
            case Digits2Through10FollowFedTaxId:
            case Digits4Through12FollowFedTaxId:
            case MustStartWithFedTaxId:
            case IfNotMEorTRMustFollowFedTaxId:
            case IFNotPatternMustFollowFedTaxId:
            case Digits3Through11FollowFedTaxId:
                //The requirement in this case is that the id should start with ME/TR if it is not to follow fed tax id
                // now if he id is start we alphabets it is alphanumeric which we are not filtering and return would
                // have happened above in the code with a true value so anything coming here has to be just numeric
                // meaning not starting with ME or TR hence it should follow fed tax id

                //If the id is valid so far and it should follow federal tax id then there we should not validate the
                // id != fedTaxId requirement so the id is valid.
                return true;
        }

        //Reaching here means that the id should not follow fed tax id
        if (StringUtils.isNotEmpty(fedTaxIdString)) {
            String fexTaxIdStringWithoutHyphenAndSpaces = fedTaxIdString.replaceAll("[- ]", "");
            if (StringUtils.equals(idStringWithoutHyphenAndSpaces, fexTaxIdStringWithoutHyphenAndSpaces)) {
                followsFedTaxId = true;
            }

        }
        return !(followsFedTaxId);
    }

    /**
     * This method return true when the given number is in sequence else returns false
     *
     * @param numberString - ex "12345678"
     * @return - true if number is in sequence else false
     */
    private static boolean isInNumericSequence(String numberString) {
        boolean isInSequence = true;
        Integer prvDigit = null;
        Integer currDigit;
        for (Character currChar : numberString.toCharArray()) {
            currDigit = Character.getNumericValue(currChar);
            if (prvDigit != null) {
                if (((prvDigit != 9) && (currDigit != prvDigit + 1)) || ((prvDigit == 9) && (currDigit != 0))) {
                    isInSequence = false;
                    break;
                }
            }
            prvDigit = currDigit;
        }
        return isInSequence;
    }

    //copied from TxpRecordManager
    public static String getDigitsOnly(String pString){
        if (pString == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        char c;
        for (int i = 0; i < pString.length() ; i++) {
            c = pString.charAt(i);
            if (Character.isDigit(c)) {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public String getRequirementString(CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod) {
        String idToTest = getIdToTest(companyPaymentTemplatePaymentMethod);
        String idName = getPaymentTemplateAgencyId() != null ? getPaymentTemplateAgencyId().getName() : "Agency ID";
        String fedTaxId = companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getCompanyAgency().getCompany().getFedTaxId();
        String pymtTemplt = companyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplateCd();
        String paymentMethod = companyPaymentTemplatePaymentMethod.getPaymentMethod().name();

        if(StringUtils.isEmpty(idToTest)){
            return idName + " not set";
        } else if(getProhibitDefaultIds() && !isNotADefaultId(idToTest, fedTaxId)){
            if(getCustomRequirement() == com.intuit.sbd.payroll.psp.domain.AgencyIdCustomRequirement.IFNotPatternMustFollowFedTaxId &&
                    pymtTemplt.equals("WA-PFLM-PAYMENT") && paymentMethod.equals("ACHCredit")){
                return idName + "is invalid. It should not be a sequence of numbers, repetition of the same number";
            }else {
                return idName + " is invalid. It should not be a sequence of numbers, repetition of the same number" +
                        " or be same as the Fed EIN if not mandated by agency.";
            }
        } else {
            return idName + " has incorrect format (Example: " + getExample()+")";
        }
    }

    public String getCustomRequirementDescription() {
        switch (getCustomRequirement()) {
            case MustNotContainFedTaxId:
                return "Must not contain the FEIN";
            case MustNotFollowFedTaxId:
                return "Must be different from the FEIN";
            case MustNotFollowFedTaxIdSubstitueIf8Digits:
                return "Must be different from the FEIN including check digit";
            case MustFollowFedTaxId:
                return "Must be the same as the FEIN";
            case MustStartWithFedTaxId:
                return "Must begin with the FEIN";
            case IfNotMEorTRMustFollowFedTaxId:
                return "Must either start with \"ME\" or \"TR\" or be the same as the FEIN";
            case Digits4Through12FollowFedTaxId:
                return "Digits 4 through 12 must follow the FEIN";
            case Digits2Through10FollowFedTaxId:
                return "Digits 2 through 10 must follow the FEIN";
            case MustNotInExemptedIdList:
                return "Agency Id is in Exempted Ids list";
            case IFNotPatternMustFollowFedTaxId:
                return "Agency Id must be according to the pattern";
            case Digits3Through11FollowFedTaxId:
                return "Digits 3 through 11 must follow the FEIN";
            default:
                throw new RuntimeException("Custom requirement not handled");
        }
    }
}
