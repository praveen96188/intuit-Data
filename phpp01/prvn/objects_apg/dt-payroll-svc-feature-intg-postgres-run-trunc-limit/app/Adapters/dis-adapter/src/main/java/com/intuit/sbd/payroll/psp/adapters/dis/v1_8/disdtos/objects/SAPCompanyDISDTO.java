package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp.PSPHelper;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp.PSPToDISTransformer;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompany;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementInfo;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementSearchResult;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollFrequencyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_8.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Company DIS DTO that will be returned by the WS
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPCompany")
public class SAPCompanyDISDTO {

    public SAPCompanyDISDTO() {
    }

    public static SAPCompanyDISDTO createFromPSPCompany(Company pCompany) throws Throwable {
        CompanyAdapter companyAdapter = new CompanyAdapter();
        SAPCompany sapCompany = CompanyTranslator.getSAPCompanyFromDomainEntity(pCompany);
        SAPCompanyDISDTO sapCompanyDISDTO = PSPToDISTransformer.createCompanySAPDISDTO(sapCompany);
        return sapCompanyDISDTO;
    }

    public static List<String> assistedPayrollSubtypes;
    static {
        assistedPayrollSubtypes = new ArrayList<String>();
        assistedPayrollSubtypes.add(EnumUtils.getReadableName(PayrollSubtypeCode.Assisted));
        assistedPayrollSubtypes.add(EnumUtils.getReadableName(PayrollSubtypeCode.AssistedAdv));
    }

    @XmlElement(name = "SourceSystemEnum",nillable = false,required = true)
    private SourceSystemEnum sourceSystemEnum;

    public SourceSystemEnum getSourceSystemEnum() {
        return sourceSystemEnum;
    }

    public void setSourceSystemCd(SourceSystemEnum sourceSystemEnum) {
        this.sourceSystemEnum = sourceSystemEnum;
    }

    @XmlElement(name = "CompanyId",nillable = false,required = true)
    private String companyId;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    @XmlElement(name = "PSID")
    private String PSID;

    public String getPSID() {
        return PSID;
    }

    public void setPSID(String PSID) {
        this.PSID = PSID;
    }

    @XmlElement(name = "RealmId")
    private String realmId;

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    @XmlElement(name = "EIN",nillable = false,required = true)
    private String fein;

    public String getFein() {
        return fein;
    }

    public void setFein(String fein) {
        this.fein = fein;
    }

    @XmlElement(name = "LegalName")
    private String legalName;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    @XmlElement(name = "DBA")
    private String DBA;

    public String getDBA() {
        return DBA;
    }

    public void setDBA(String DBA) {
        this.DBA = DBA;
    }

    @XmlElement(name = "LegalAddress")
    private SAPAddressDISDTO legalAddress;

    public SAPAddressDISDTO getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(SAPAddressDISDTO legalAddress) {
        this.legalAddress = legalAddress;
    }

    @XmlElement(name = "MailingAddress")
    private SAPAddressDISDTO mailingAddress;

    public SAPAddressDISDTO getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(SAPAddressDISDTO mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    @XmlElement(name = "NotificationEmail")
    private String notificationEmail;

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    @XmlElement(name = "SAPContact")
    private ArrayList<SAPContactDISDTO> contacts = new ArrayList<SAPContactDISDTO>();

    public ArrayList<SAPContactDISDTO> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<SAPContactDISDTO> contacts) {
        this.contacts = contacts;
    }

    @XmlElement(name = "PayrollFrequency")
    private PayrollFrequencyDTO payrollFrequencyCd;

    public PayrollFrequencyDTO getPayrollFrequencyCd() {
        return payrollFrequencyCd;
    }

    public void setPayrollFrequencyCd(PayrollFrequencyDTO payrollFrequencyCd) {
        this.payrollFrequencyCd = payrollFrequencyCd;
    }

    @XmlElement(name = "TaxCompanyServiceInfo")
    private SAPTaxCompanyServiceInfoDISDTO taxService;

    public SAPTaxCompanyServiceInfoDISDTO getTaxService() {
        return taxService;
    }

    public void setTaxService(SAPTaxCompanyServiceInfoDISDTO taxService) {
        this.taxService = taxService;
    }

    @XmlElement(name = "PayrollRunCount")
    private int payrollRunCount = -1;

    public int getPayrollRunCount() {
        return payrollRunCount;
    }

    public void setPayrollRunCount(int payrollRunCount) {
        this.payrollRunCount = payrollRunCount;
    }

    @XmlElement(name = "BankReturnTransactionCount")
    private int bankReturnTransactionCount = -1;

    public int getBankReturnTransactionCount() {
        return bankReturnTransactionCount;
    }

    public void setBankReturnTransactionCount(int bankReturnTransactionCount) {
        this.bankReturnTransactionCount = bankReturnTransactionCount;
    }

    @XmlElement(name = "LastTransactionResponseToken")
    private long lastTransactionResponseToken = -1;

    public long getLastTransactionResponseToken() {
        return lastTransactionResponseToken;
    }

    public void setLastTransactionResponseToken(long lastTransactionResponseToken) {
        this.lastTransactionResponseToken = lastTransactionResponseToken;
    }

    @XmlElement(name = "TaxExemptExpirationDate")
    private Date taxExemptExpirationDate;

    public Date getTaxExemptExpirationDate() {
        return taxExemptExpirationDate;
    }

    public void setTaxExemptExpirationDate(Date taxExemptExpirationDate) {
        this.taxExemptExpirationDate = taxExemptExpirationDate;
    }

    @XmlElement(name = "TaxExempt")
    private boolean taxExempt;

    public boolean isTaxExempt() {
        return taxExempt;
    }

    public void setTaxExempt(boolean taxExempt) {
        this.taxExempt = taxExempt;
    }

    @XmlElement(name = "IsEditable")
    private boolean isEditable;

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    @XmlElement(name = "DoesPSPProvideCustomerService")
    private boolean doesPSPProvideCustomerService;

    public boolean isDoesPSPProvideCustomerService() {
        return doesPSPProvideCustomerService;
    }

    public void setDoesPSPProvideCustomerService(boolean doesPSPProvideCustomerService) {
        this.doesPSPProvideCustomerService = doesPSPProvideCustomerService;
    }

    @XmlElement(name = "DoesPSPMoveMoneyFor")
    private boolean doesPSPMoveMoneyFor;

    public boolean isDoesPSPMoveMoneyFor() {
        return doesPSPMoveMoneyFor;
    }

    public void setDoesPSPMoveMoneyFor(boolean doesPSPMoveMoneyFor) {
        this.doesPSPMoveMoneyFor = doesPSPMoveMoneyFor;
    }

    @XmlElement(name = "ActiveBankAccount")
    private SAPCompanyBankAccountDISDTO activeBankAccount;

    public SAPCompanyBankAccountDISDTO getActiveBankAccount() {
        return activeBankAccount;
    }

    public void setActiveBankAccount(SAPCompanyBankAccountDISDTO activeBankAccount) {
        this.activeBankAccount = activeBankAccount;
    }

    @XmlElement(name = "SAPAgreementInfo")
    private SAPAgreementInfoDISDTO agreementInfo;

    public SAPAgreementInfoDISDTO getAgreementInfo() {
        return agreementInfo;
    }

    public void setAgreementInfo(SAPAgreementInfoDISDTO agreementInfo) {
        this.agreementInfo = agreementInfo;
    }

    @XmlElement(name = "SAPQuickbooksInfo")
    private SAPQuickbooksInfoDISDTO quickbooksInfo;

    public SAPQuickbooksInfoDISDTO getQuickbooksInfo() {
        return quickbooksInfo;
    }

    public void setQuickbooksInfo(SAPQuickbooksInfoDISDTO quickbooksInfo) {
        this.quickbooksInfo = quickbooksInfo;
    }

    @XmlElement(name = "FraudFlag")
    private boolean fraudFlag;

    public boolean isFraudFlag() {
        return fraudFlag;
    }

    public void setFraudFlag(boolean fraudFlag) {
        this.fraudFlag = fraudFlag;
    }

    @XmlElement(name = "PinCreated")
    private boolean pinCreated;

    public boolean isPinCreated() {
        return pinCreated;
    }

    public void setPinCreated(boolean pinCreated) {
        this.pinCreated = pinCreated;
    }

    @XmlElement(name = "PinLocked")
    private boolean pinLocked;

    public boolean isPinLocked() {
        return pinLocked;
    }

    public void setPinLocked(boolean pinLocked) {
        this.pinLocked = pinLocked;
    }

    @XmlElement(name = "NextPayrollTransactionId")
    private String nextPayrollTransactionId;

    public String getNextPayrollTransactionId() {
        return nextPayrollTransactionId;
    }

    public void setNextPayrollTransactionId(String nextPayrollTransactionId) {
        this.nextPayrollTransactionId = nextPayrollTransactionId;
    }

    @XmlElement(name = "NextPaycheckId")
    private String nextPaycheckId;

    public String getNextPaycheckId() {
        return nextPaycheckId;
    }

    public void setNextPaycheckId(String nextPaycheckId) {
        this.nextPaycheckId = nextPaycheckId;
    }

    @XmlElement(name = "MigrationStatus")
    private String migrationStatus;

    public String getMigrationStatus() {
        return migrationStatus;
    }

    public void setMigrationStatus(String migrationStatus) {
        this.migrationStatus = migrationStatus;
    }

    @XmlElement(name = "HasChecklist")
    private boolean hasChecklist;

    public boolean isHasChecklist() {
        return hasChecklist;
    }

    public void setHasChecklist(boolean hasChecklist) {
        this.hasChecklist = hasChecklist;
    }

    @XmlElement(name = "Gseq")
    private String gseq;

    public String getGseq() {
        return gseq;
    }

    public void setGseq(String gseq) {
        this.gseq = gseq;
    }

    @XmlElement(name = "OffloadGrp")
    private String offloadGrp;

    public String getOffloadGrp() {
        return offloadGrp;
    }

    public void setOffloadGrp(String offloadGrp) {
        this.offloadGrp = offloadGrp;
    }

    @XmlElement(name = "DebugLogging")
    private boolean debugLogging;

    public boolean isDebugLogging() {
        return debugLogging;
    }

    public void setDebugLogging(boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    @XmlElement(name = "IsAssisted")
    private boolean isAssisted;

    public boolean isAssisted() {
        return isAssisted;
    }

    public void setAssisted(boolean assisted) {
        isAssisted = assisted;
    }

    @XmlElement(name = "HasCompanyAgencies")
    private boolean hasCompanyAgencies;

    public boolean isHasCompanyAgencies() {
        return hasCompanyAgencies;
    }

    public void setHasCompanyAgencies(boolean hasCompanyAgencies) {
        this.hasCompanyAgencies = hasCompanyAgencies;
    }

    @XmlElement(name = "FundingModelCd")
    private String fundingModelCd;

    public String getFundingModelCd() {
        return fundingModelCd;
    }

    public void setFundingModelCd(String fundingModelCd) {
        this.fundingModelCd = fundingModelCd;
    }

//    private ArrayList<SAPCompanyStrike> strikes;
//    private ArrayList<SAPServiceSubStatus> onHoldReasons;

    // This is not part of the SAP Company DTO
    @XmlElement(name = "SAPCompanyStatus")
    private SAPCompanyStatusDISDTO sapCompanyStatus;

    public SAPCompanyStatusDISDTO getSapCompanyStatus() {
        return sapCompanyStatus;
    }

    public void setSapCompanyStatus(SAPCompanyStatusDISDTO sapCompanyStatus) {
        this.sapCompanyStatus = sapCompanyStatus;
    }

    @XmlElement(name = "StrikeCount")
    int strikeCount;

    public int getStrikeCount() {
        return strikeCount;
    }

    public void setStrikeCount(int strikeCount) {
        this.strikeCount = strikeCount;
    }

    @XmlElement(name = "AssistedCustomerAccountNumber")
    String assistedCustomerAccountNumber;

    public String getAssistedCustomerAccountNumber() {
        return assistedCustomerAccountNumber;
    }

    public void setAssistedCustomerAccountNumber(String assistedCustomerAccountNumber) {
        this.assistedCustomerAccountNumber = assistedCustomerAccountNumber;
    }

    @XmlElement
    private List<EntityChangeDISDTO> entityChange = new ArrayList<EntityChangeDISDTO>();

    public List<EntityChangeDISDTO> getEntityChange() {
        return entityChange;
    }

    public void setEntityChange(List<EntityChangeDISDTO> pEntityChange) {
        entityChange = pEntityChange;
    }

    // Set the customers CAN.  If not available, do not fail.
    // Since using SAP, a UOW cannot be active.
    // JPC 11/5/2012 - Fixing DE1042 DAS - CAN Number - agent reporting incorrect CAN pulled for the company
    //      added line to checked PSID since entitlements are returned by EIN.
    public void populateCustomerAccountNumber() {
        try {
            CompanyAdapter companyAdapter = new CompanyAdapter();
            ArrayList<SAPEntitlementSearchResult> sapEntitlementSearchResults = companyAdapter.findEntitlementUnits(getFein());
            for (SAPEntitlementSearchResult sapEntitlementSearchResult : sapEntitlementSearchResults) {
                if (sapEntitlementSearchResult.getPSID().equals(getPSID())) {
                    String subtypeDescription = sapEntitlementSearchResult.getSubtypeDescription();
                    if (assistedPayrollSubtypes.contains(subtypeDescription)) {
                        SAPEntitlementInfo sapEntitlementInfo = companyAdapter.getEntitlementInfo(sapEntitlementSearchResult.getLicenseNumber(),sapEntitlementSearchResult.getEoc());
                        if (sapEntitlementInfo != null) {
                            setAssistedCustomerAccountNumber(sapEntitlementInfo.getCustomerId());
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            // Just leave CAN as blank if there is a problem retrieving that item.
        }
    }

    public void populateEntityChanges() throws Exception {
        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany(getPSID(),translateSourceSystemCode(getSourceSystemEnum()));
            if (company == null) {
                throw new Exception(DISMessages.companyDoesNotExist(getPSID()).toString());
            }
            DomainEntitySet<EntityChange> entityChanges = company.getEntityChangeCollection();

            for (EntityChange entityChange : entityChanges) {
                getEntityChange().add(new EntityChangeDISDTO(entityChange));
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public void updateSAPCompanyDISDTO() throws Throwable {
        PSPHelper.updateSAPCompanyDISDTO(this);
    }


    public void populateServicesEndDate() throws Exception {
        // The service end date is not stored in PSP or calculated in SAP.  This requires the company to go through all of the services
        //    and calculate that companies service end dates.
        for (SAPCompanyServiceStatusDISDTO sapCompanyServiceStatusDISDTO : getSapCompanyStatus().getServiceStatusCollection()) {
            sapCompanyServiceStatusDISDTO.populateServiceEndDate(getPSID(),getSourceSystemEnum());
        }

    }
}
