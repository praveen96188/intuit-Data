package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.dtos;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * @author Jeff Jones
 */
public class ThirdParty401kCensusDTO implements Comparable<ThirdParty401kCensusDTO> {

    Employee mEmployee;

    //Common File Data
    private String mBureauName;
    private String mBureauCompanyId;
    private String mFEIN;
    private String mCustodialAccountId;
    private String mLastName;
    private String mFirstName;
    private String mTaxId;

    //Census File Data
    private String mAsOfDate;
    private String mMiddleName;
    private String mBirthDate;
    private String mStreetAddress;
    private String mCity;
    private String mState;
    private String mZip;
    private String mPhone;
    private String mEMail;
    private String mDateOfHire;
    private String mTerminationDate;
    private String mTerminationStatus;
    private String mIsHCE;
    private String mIsFamilyMember;
    private Long mOwnershipPercentage;

    private ArrayList<String> validationErrors;

    public ThirdParty401kCensusDTO() {

    }

    public ThirdParty401kCensusDTO(ThirdParty401kCompanyServiceInfo pTP401kCompanyServiceInfo, Employee pEmployee) {
        mEmployee = pEmployee;

        validationErrors = new ArrayList<String>();
        Company company = pTP401kCompanyServiceInfo.getCompany();

        this.mBureauName = "Intuit";
        this.mBureauCompanyId = "Intuit";

        SpcfCalendar lastTOKModifiedDate = pEmployee.getLastTOKModifiedDate();
        if (lastTOKModifiedDate != null) {
            this.setAsOfDate(lastTOKModifiedDate);
        } else {
            this.setAsOfDate(pEmployee.getCreatedDate());
        }

        this.setFEIN(company.getFedTaxId());
        this.setCustodialAccountId(pTP401kCompanyServiceInfo.getCustodialId());
        this.setFirstName(pEmployee.getFirstName());
        this.setMiddleName(pEmployee.getMiddleName());
        this.setTaxId(pEmployee.getTaxId());

        Address address = pEmployee.getMailingAddress();
        if (address != null) {
            this.setStreetAddress(address.getAddressLine1());
            this.setCity(address.getCity());
            this.setState(address.getState());
            this.setZip(address.getZipCode());
        }

        this.setPhone(pEmployee.getPhone());
        this.setEMail(pEmployee.getEmail());
        this.setDateOfHire(pEmployee.getHireDate());
        this.setTerminationDate(pEmployee.getTerminationDate());
        this.setLastName(pEmployee.getLastName());
        this.setBirthDate(pEmployee.getBirthDate());

        if (pEmployee.getThirdParty401kInfo() != null) {
            this.setIsHCE(pEmployee.getThirdParty401kInfo().getIsHighlyCompensated());
            this.setIsFamilyMember(pEmployee.getThirdParty401kInfo().getIsFamilyMember());
            this.setOwnershipPercentage(pEmployee.getThirdParty401kInfo().getOwnershipPercentage());
        }

        validateCensusData(pEmployee);
    }

    public Employee getEmployee() {
        return mEmployee;
    }

    public String getBureauName() {
        return mBureauName;
    }

    public String getBureauCompanyId() {
        return mBureauCompanyId;
    }

    public String getAsOfDate() {
        return mAsOfDate;
    }

    public void setAsOfDate(SpcfCalendar pAsOfDate) {
        this.mAsOfDate = pAsOfDate.toLocal().format("yyyyMMdd");
    }

    public String getFEIN() {
        return mFEIN;
    }

    public void setFEIN(String pFEIN) {
        Pattern pattern = Pattern.compile(".*?([0-9]{2}).*?([0-9]{7}).*?");
        Matcher matcher = pattern.matcher(pFEIN.trim());
        if (matcher.matches()) {
            this.mFEIN = matcher.replaceAll("$1$2");
        } else {
            this.mFEIN = null;
        }
    }

    public String getCustodialAccountId() {
        return mCustodialAccountId;
    }

    public void setCustodialAccountId(String pCustodialAccountId) {
        this.mCustodialAccountId = pCustodialAccountId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String pFirstName) {
        if (pFirstName != null) {
            pFirstName = pFirstName.replaceAll(",", " ");
            if (pFirstName.trim().length() > 20) {
                this.mFirstName = pFirstName.trim().substring(0, 20);
            } else {
                this.mFirstName = pFirstName.trim();
            }
        } else {
            this.mFirstName = null;
        }
    }

    public String getMiddleName() {
        return mMiddleName;
    }

    public void setMiddleName(String pMiddleName) {
        if (pMiddleName != null) {
            pMiddleName = pMiddleName.replaceAll(",", " ");
            if (pMiddleName.trim().length() > 20) {
                this.mMiddleName = pMiddleName.trim().substring(0, 20);
            } else {
                this.mMiddleName = pMiddleName.trim();
            }
        } else {
            this.mMiddleName = null;
        }
    }

    public String getTaxId() {
        return mTaxId;
    }

    public void setTaxId(String pTaxId) {
        Pattern pattern = Pattern.compile(".*?([0-9]{3}).*?([0-9]{2}).*?([0-9]{4}).*?");
        if (pTaxId != null) {
            Matcher matcher = pattern.matcher(pTaxId.trim());
            if (matcher.matches()) {
                this.mTaxId = matcher.replaceAll("$1$2$3");
            } else {
                this.mTaxId = null;
            }
        } else {
            this.mTaxId = null;
        }
    }

    public String getBirthDate() {
        return mBirthDate;
    }

    public void setBirthDate(SpcfCalendar pBirthDate) {
        if (pBirthDate != null) {
            this.mBirthDate = pBirthDate.toLocal().format("MMddyyyy");
        } else {
            this.mBirthDate = null;
        }
    }

    public String getStreetAddress() {
        return mStreetAddress;
    }

    public void setStreetAddress(String pStreetAddress) {
        if (pStreetAddress != null) {
            pStreetAddress = pStreetAddress.replaceAll(",", " ");
            if (pStreetAddress.trim().length() > 30) {
                this.mStreetAddress = pStreetAddress.trim().substring(0, 30);
            } else {
                this.mStreetAddress = pStreetAddress.trim();
            }
        } else {
            this.mStreetAddress = null;
        }
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String pCity) {
        if (pCity != null) {
            pCity = pCity.replaceAll(",", " ");
            if (pCity.trim().length() > 30) {
                this.mCity = pCity.trim().substring(0, 30);
            } else {
                this.mCity = pCity.trim();
            }
        } else {
            this.mCity = null;
        }
    }

    public String getState() {
        return mState;
    }

    public void setState(String pState) {
        this.mState = null;

        if (pState != null) {
            pState = pState.replaceAll(",", " ");
            if (Address.States.isValid(pState)) {
                this.mState = pState;
            }
        }
    }

    public String getZip() {
        return mZip;
    }

    public void setZip(String pZip) {
        if (pZip != null) {
            Pattern pattern = Pattern.compile("^([0-9]{5})(.*?[0-9]{1,4})?$");
            Matcher matcher = pattern.matcher(pZip.trim());
            if (matcher.matches()) {
                this.mZip = matcher.replaceAll("$1");
            } else {
                this.mZip = null;
            }
        }
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String pPhone) {
        if (pPhone != null) {
            Pattern pattern = Pattern.compile(".*?\\(?([2-9]{1}[0-9]{2})\\)?.*?([0-9]{3}).*?([0-9]{4}).*");
            Matcher matcher = pattern.matcher(pPhone.trim());
            if (matcher.matches()) {
                this.mPhone = matcher.replaceAll("$1$2$3");
            } else {
                this.mPhone = null;
            }
        } else {
            this.mPhone = null;
        }
    }

    public String getEMail() {
        return mEMail;
    }

    public void setEMail(String pEMail) {
        if (Validator.isValidEmail(pEMail)) {
            this.mEMail = pEMail;
        } else {
            this.mEMail = null;
        }
    }

    public String getDateOfHire() {
        return mDateOfHire;
    }

    public void setDateOfHire(SpcfCalendar pDateOfHire) {
        if (pDateOfHire != null) {
            this.mDateOfHire = pDateOfHire.toLocal().format("MMddyyyy");
        } else {
            this.mDateOfHire = null;
        }
    }

    public String getTerminationDate() {
        return mTerminationDate;
    }

    public void setTerminationDate(SpcfCalendar pTerminationDate) {
        if (pTerminationDate != null) {
            this.mTerminationDate = pTerminationDate.toLocal().format("MMddyyyy");
            this.mTerminationStatus = "T";
        } else {
            this.mTerminationDate = null;
            this.mTerminationStatus = null;
        }
    }

    public String getTerminationStatus() {
        return mTerminationStatus;
    }

    public String getIsHCE() {
        return mIsHCE;
    }

    public void setIsHCE(Boolean pIsHCE) {
        if (pIsHCE) {
            this.mIsHCE = "Y";
        } else {
            this.mIsHCE = "N";
        }
    }

    public String getIsFamilyMember() {
        return mIsFamilyMember;
    }

    public void setIsFamilyMember(Boolean pIsFamilyMember) {
        if (pIsFamilyMember) {
            this.mIsFamilyMember = "1";
        } else {
            this.mIsFamilyMember = "0";
        }
    }

    public Long getOwnershipPercentage() {
        return mOwnershipPercentage;
    }

    public void setOwnershipPercentage(Double pOwnershipPercentage) {
        //Must be between 0 to 100
        if (pOwnershipPercentage >= 0.00 && pOwnershipPercentage <= 100) {
            this.mOwnershipPercentage = Math.round(pOwnershipPercentage);
        } else {
            this.mOwnershipPercentage = (long) 0;
        }
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String pLastName) {
        if (pLastName != null) {
            pLastName = pLastName.replaceAll(",", " ");
            if (pLastName.trim().length() > 20) {
                this.mLastName = pLastName.trim().substring(0, 20);
            } else {
                this.mLastName = pLastName.trim();
            }
        } else {
            this.mLastName = null;
        }
    }

    public ArrayList<String> getValidationErrors() {
        return validationErrors;
    }

    private void validateCensusData(Employee pEmployee) {
        ArrayList<String> employeeValidationErrors = pEmployee.isValidForCensusFile();
        validationErrors.addAll(employeeValidationErrors);
    }

    public int compareTo(ThirdParty401kCensusDTO o) {
        return key().compareTo(o.key());
    }

    protected String key() {
        String value = "";
        if (getFEIN() != null) {
            value += getFEIN();
        }
        if (getTaxId() != null) {
            value += getTaxId();
        }
        return value;
    }
}
