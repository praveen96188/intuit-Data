package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.reports.RecordEnricher;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.FlushMode;

import java.util.*;

/**
 * @author kmuthurangam
 * <p>
 * MtlTransactionRecordEnricher enriches each MtlTransactionRecord in the MTL Transaction Report with sensitive PII and missing critical information.
 * <p>
 * MtlTransactionRecord Headers
 * <p>
 * transaction number,transaction date,transaction time,transaction amount,amount &amp; type of currency received and given,rate of exchange,transaction fee or commission,customer company id,customer name,customer address,customer address 2,customer city,customer zip code,customer state,customer country,customer telephone,customer ein,customer passport num,customer passport country,customer photo id num,customer photo id type,primary principal officer name,primary principal officer address,primary principal officer ssn,primary principal officer dob,beneficiary type,beneficiary company id,beneficiary country,beneficiary name,beneficiary address,beneficiary phone,beneficiary bank,beneficiary bank account number,sender name,sender address,sender phone,office location,payment method,employee initials,comments,product,rails
 * <p>
 * Raw MtlTransactionRecord
 * <p>
 * e1f7de35-e2ef-4280-b0dd-f7a60dacf876,[2020/12/17],[2020/12/15:08:00:00 AM],-2302.81,N/A,N/A,N/A,448010974,"TAURUS TECHNOLOGIES, INC.",1420 LAKESIDE PKWY,STE 100,FLOWER MOUND,75028,TX,US,8174104790,,N/A,N/A,N/A,N/A,JACKSON FABIA,,,,Employee,a373387c-907d-4e95-98ad-9480249f327c,US,JACKSON FABIA,"107 LOVING COURT , RHOME, TX, 76078, US",,,,N/A,N/A,N/A,N/A,ACH,N/A,N/A,Payroll,PSP
 * <p>
 * Enriched MtlTransactionRecord
 * <p>
 * e1f7de35-e2ef-4280-b0dd-f7a60dacf876,[2020/12/17],[2020/12/15:08:00:00 AM],-2302.81,N/A,N/A,N/A,448010974,"TAURUS TECHNOLOGIES, INC.",1420 LAKESIDE PKWY,STE 100,FLOWER MOUND,75028,TX,US,8174104790,1234*****,N/A,N/A,N/A,N/A,JACKSON FABIA,,1234*****,**&#47;13&#47;****,Employee,a373387c-907d-4e95-98ad-9480249f327c,US,JACKSON FABIA,"107 LOVING COURT , RHOME, TX, 76078, US",210-415-6213,"JPMC Chase Inc..,",1234****,N/A,N/A,N/A,N/A,ACH,N/A,N/A,Payroll,PSP
 */
public class MtlTransactionRecordEnricher implements RecordEnricher<MtlTransactionRecord> {

    private static final SpcfLogger logger = Application.getLogger(MtlTransactionReportEnricher.class);

    private Map<String, String> allLawAgencyMap;

    public void enrichRecords(List<MtlTransactionRecord> mtlTransactionRecords) {
        if (CollectionUtils.isEmpty(mtlTransactionRecords)) {
            logger.info("Skipped enriching the records due to Null or Empty MtlTransactionRecord list");
            return;
        }

        try {
            Application.beginUnitOfWork(FlushMode.MANUAL, true);

            buildLawAgencyMap();

            Map<SpcfUniqueId, String> financialTransactionCompanyMap = getFinancialTransactionCompanyMap(mtlTransactionRecords);

            if (MapUtils.isEmpty(financialTransactionCompanyMap)) {
                logger.info("Skipped enriching the records due to Null or Empty financialTransactionIdSet");
                return;
            }

            // Load all the financial transactions to avoid repeated database requests
            DomainEntitySet<FinancialTransaction> financialTransactions = getFinancialTransactions(financialTransactionCompanyMap);

            if (CollectionUtils.isEmpty(financialTransactions)) {
                logger.info("Skipped enriching the records due to Null or Empty financialTransactions");
                return;
            }

            Map<String, FinancialTransaction> financialTransactionMap = getFinancialTransactionMap(financialTransactions);

            if (MapUtils.isEmpty(financialTransactionMap)) {
                logger.info("Skipped enriching the records due to Null or Empty financialTransactionMap");
                return;
            }

            Set<SpcfUniqueId> addressIdSet = getAddressIdSet(financialTransactions);

            // Load all the addresses for Employees only as Payee addresses are already eager loaded
            Map<String, Address> addressMap = getAddressMap(addressIdSet);

            for (MtlTransactionRecord mtlTransactionRecord : mtlTransactionRecords) {
                enrich(financialTransactionMap, addressMap, mtlTransactionRecord);
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private void enrich(Map<String, FinancialTransaction> financialTransactionMap, Map<String, Address> addressMap, MtlTransactionRecord mtlTransactionRecord) {
        if (!mtlTransactionRecord.isValid()) {
            logger.info(String.format("Skipped enriching the MtlTransactionRecord for transactionNumber=%s due to missing important properties", mtlTransactionRecord.getTransactionNumber()));
            return;
        }

        try {
            BeneficiaryType beneficiaryType = BeneficiaryType.valueOf(mtlTransactionRecord.getBeneficiaryType());

            if (beneficiaryType == BeneficiaryType.Sender) {
                logger.info(String.format("Skipped enriching the MtlTransactionRecord for transactionNumber=%s because BeneficiaryType is Sender", mtlTransactionRecord.getTransactionNumber()));
                return;
            }

            enrichRecordFields(financialTransactionMap, addressMap, mtlTransactionRecord);
        } catch (RuntimeException runtimeException) {
            logger.error(String.format("MtlTransactionRecord with transactionId=%s couldn't be enhanced", mtlTransactionRecord.getTransactionNumber()), runtimeException);
        } catch (Exception exception) {
            logger.error(String.format("MtlTransactionRecord with transactionId=%s couldn't be enhanced due to unexpected exception", mtlTransactionRecord.getTransactionNumber()), exception);
        }
    }

    private void enrichRecordFields(Map<String, FinancialTransaction> financialTransactionMap, Map<String, Address> addressMap, MtlTransactionRecord mtlTransactionRecord) {
        FinancialTransaction financialTransaction = financialTransactionMap.get(mtlTransactionRecord.getTransactionNumber());
        Objects.requireNonNull(financialTransaction, "Financial Transaction cannot be null");

        Company company = financialTransaction.getCompany();
        Objects.requireNonNull(company, "Company cannot be null");

        Contact contact = getPrimaryPrincipalOfficer(company);
        Objects.requireNonNull(contact, "Primary Principal Officer cannot be null");

        BankAccount bankAccount = getBankAccount(financialTransaction);
        Objects.requireNonNull(bankAccount, "Bank Account cannot be null");

        for (EnrichFieldName enrichField : EnrichFieldName.values()) {
            switch (enrichField) {
                case CUSTOMER_EIN:
                    mtlTransactionRecord.setCustomerEin(getCustomerEin(company));
                    break;
                case PRIMARY_PRINCIPAL_OFFICER_SSN:
                    mtlTransactionRecord.setPrimaryPrincipalOfficerSsn(getPrimaryPrincipalOfficerSsn(contact));
                    break;
                case PRIMARY_PRINCIPAL_OFFICER_DOB:
                    mtlTransactionRecord.setPrimaryPrincipalOfficerDob(getPrimaryPrincipalOfficerDob(contact));
                    break;
                case BENEFICIARY_BANK:
                    mtlTransactionRecord.setBeneficiaryBank(getBeneficiaryBank(bankAccount));
                    break;
                case BENEFICIARY_BANK_ACCOUNT_NUMBER:
                    mtlTransactionRecord.setBeneficiaryBankAccountNumber(getBeneficiaryBankAccountNumber(bankAccount));
                    break;
                case BENEFICIARY_NAME:
                    mtlTransactionRecord.setBeneficiaryName(getBeneficiaryName(mtlTransactionRecord, financialTransaction));
                    break;
                case BENEFICIARY_ADDRESS:
                    mtlTransactionRecord.setBeneficiaryAddress(getBeneficiaryAddress(addressMap, mtlTransactionRecord, financialTransaction));
                    break;
                case BENEFICIARY_PHONE:
                    mtlTransactionRecord.setBeneficiaryPhone(getBeneficiaryPhone(mtlTransactionRecord, financialTransaction));
                    break;
            }
        }
    }

    private String getCustomerEin(Company company) {
        return PIIMask.maskText(company.getFedTaxId(), 5);
    }

    private Contact getPrimaryPrincipalOfficer(Company company) {
        DomainEntitySet<Contact> contacts = company.getContactCollection();
        Optional<Contact> contactOptional = contacts
                .stream()
                .filter(contact -> contact.getContactRoleCd() == ContactRole.PrimaryPrincipal)
                .findFirst();
        return contactOptional.get();
    }

    private String getPrimaryPrincipalOfficerSsn(Contact contact) {
        String socialSecurityNumber = contact.getSocialSecurityNumber();

        if (StringUtils.isNotEmpty(socialSecurityNumber)) {
            return PIIMask.maskText(socialSecurityNumber);
        }

        socialSecurityNumber = getPrimaryPrincipalOfficerSsnFromCompanyEvent(contact.getCompany());

        return StringUtils.isEmpty(socialSecurityNumber) ? MtlTransactionRecord.FIELD_NOT_AVAILABLE : PIIMask.maskText(socialSecurityNumber);
    }

    private String getPrimaryPrincipalOfficerSsnFromCompanyEvent(Company company) {
        logger.info(String.format("Trying to get PrimaryPrincipalOfficerSsn from CompanyEvent for SourceCompanyId=%s", company.getSourceCompanyId()));
        String socialSecurityNumber = null;
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEventsEagerLoadCompanyEventDetail(company, EventTypeCode.PrimaryPrincipalSSNChanged, CompanyEventStatus.Active, true);
        for (CompanyEvent companyEvent : companyEvents) {
            String oldSocialSecurityNumber = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue);
            String newSocialSecurityNumber = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue);

            if (StringUtils.isNotEmpty(oldSocialSecurityNumber) && StringUtils.isEmpty(newSocialSecurityNumber)) {
                socialSecurityNumber = oldSocialSecurityNumber;
                break;
            }
        }
        return socialSecurityNumber;
    }

    private String getPrimaryPrincipalOfficerDob(Contact contact) {
        SpcfCalendar dateOfBirth = contact.getDateOfBirth();

        if (Objects.nonNull(dateOfBirth)) {
            return PIIMask.getMaskedDate(dateOfBirth);
        }

        dateOfBirth = getPrimaryPrincipalOfficerDobFromCompanyEvent(contact.getCompany());

        return Objects.isNull(dateOfBirth) ? MtlTransactionRecord.FIELD_NOT_AVAILABLE : PIIMask.getMaskedDate(dateOfBirth);
    }

    private SpcfCalendar getPrimaryPrincipalOfficerDobFromCompanyEvent(Company company) {
        logger.info(String.format("Trying to get PrimaryPrincipalOfficerDob from CompanyEvent for SourceCompanyId=%s", company.getSourceCompanyId()));
        SpcfCalendar dateOfBirth = null;
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEventsEagerLoadCompanyEventDetail(company, EventTypeCode.PrimaryPrincipalDOBChanged, CompanyEventStatus.Active, true);
        for (CompanyEvent companyEvent : companyEvents) {
            String oldDateOfBirth = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue);
            String newDateOfBirth = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue);

            if (StringUtils.isNotEmpty(oldDateOfBirth) && StringUtils.isEmpty(newDateOfBirth)) {
                String[] dateOfBirthStringArray = oldDateOfBirth.split(":");
                String dateOfBirthStringValue = dateOfBirthStringArray[2];
                dateOfBirth = SpcfCalendar.parse("mm/dd/yyyy", dateOfBirthStringValue);
                break;
            }
        }
        return dateOfBirth;
    }

    private BankAccount getBankAccount(FinancialTransaction financialTransaction) {
        return financialTransaction.getCreditBankAccount();
    }

    private String getBeneficiaryBank(BankAccount bankAccount) {
        return Objects.isNull(bankAccount) ? MtlTransactionRecord.FIELD_NOT_AVAILABLE : bankAccount.getBankName();
    }

    private String getBeneficiaryBankAccountNumber(BankAccount bankAccount) {
        return Objects.isNull(bankAccount) ? MtlTransactionRecord.FIELD_NOT_AVAILABLE : PIIMask.maskText(bankAccount.getAccountNumber());
    }

    private String getBeneficiaryName(MtlTransactionRecord mtlTransactionRecord, FinancialTransaction financialTransaction) {
        String beneficiaryName = mtlTransactionRecord.getBeneficiaryName();
        if (!isEnrichmentRequired(beneficiaryName)) {
            return beneficiaryName;
        }

        switch (getBeneficiaryType(mtlTransactionRecord)) {
            case Employee:
                beneficiaryName = getEmployeeName(financialTransaction);
                break;
            case Company:
                beneficiaryName = getPayeeName(financialTransaction);
                break;
            case Agency:
                beneficiaryName = getAgencyName(financialTransaction);
                break;
            case Sender:
                // Skip enrich
                break;
        }

        return beneficiaryName;
    }

    private String getBeneficiaryAddress(Map<String, Address> addressMap, MtlTransactionRecord mtlTransactionRecord, FinancialTransaction financialTransaction) {
        String beneficiaryAddress = mtlTransactionRecord.getBeneficiaryAddress();
        if (!isEnrichmentRequired(beneficiaryAddress)) {
            return beneficiaryAddress;
        }

        switch (getBeneficiaryType(mtlTransactionRecord)) {
            case Employee:
                beneficiaryAddress = getEmployeeAddress(addressMap, financialTransaction);
                break;
            case Company:
                beneficiaryAddress = getPayeeAddress(financialTransaction);
                break;
            case Agency:
                beneficiaryAddress = MtlTransactionRecord.FIELD_NOT_AVAILABLE;
                break;
            case Sender:
                // Skip enrich
                break;
        }

        return beneficiaryAddress;
    }

    private String getBeneficiaryPhone(MtlTransactionRecord mtlTransactionRecord, FinancialTransaction financialTransaction) {
        String beneficiaryPhone = mtlTransactionRecord.getBeneficiaryPhone();
        if (!isEnrichmentRequired(beneficiaryPhone)) {
            return beneficiaryPhone;
        }

        switch (getBeneficiaryType(mtlTransactionRecord)) {
            case Employee:
                beneficiaryPhone = getEmployeePhone(financialTransaction);
                break;
            case Company:
                beneficiaryPhone = getPayeePhone(financialTransaction);
                break;
            case Agency:
                beneficiaryPhone = MtlTransactionRecord.FIELD_NOT_AVAILABLE;
                break;
            case Sender:
                // Skip enrich
                break;
        }

        return beneficiaryPhone;
    }

    private DomainEntitySet<FinancialTransaction> getFinancialTransactions(Map<SpcfUniqueId, String> financialTransactionCompanyMap) {
        if (MapUtils.isEmpty(financialTransactionCompanyMap)) {
            return new DomainEntitySet<FinancialTransaction>();
        }

        Set<SpcfUniqueId> financialTransactionIdSet = financialTransactionCompanyMap.keySet();
        Collection<String> sourceCompanyIdCollection = financialTransactionCompanyMap.values();
        // Eager load all the properties except the Employee addresses (due to technical limitation with the Query Expression framework on Inherited classes)
        Property[] eagarLoadProperties = new Property[]{FinancialTransaction.Company(),
                FinancialTransaction.CreditBankAccount(),
                FinancialTransaction.PayrollRun(),
                FinancialTransaction.Company().ContactSet(),
                FinancialTransaction.PaycheckSplit(),
                FinancialTransaction.PaycheckSplit().EmployeeBankAccount(),
                FinancialTransaction.PaycheckSplit().EmployeeBankAccount().Employee(),
                FinancialTransaction.BillPaymentSplit(),
                FinancialTransaction.BillPaymentSplit().PayeeBankAccount(),
                FinancialTransaction.BillPaymentSplit().PayeeBankAccount().Payee(),
                FinancialTransaction.BillPaymentSplit().PayeeBankAccount().Payee().MailingAddress(),
                FinancialTransaction.Law()};

        DomainEntitySet<Company> companies =  Company.findCompaniesBySourceCompanyIds(SourceSystemCode.QBDT, new ArrayList<>(sourceCompanyIdCollection));
        Criterion<FinancialTransaction> financialTransactionCriterion = FinancialTransaction.Company().in(companies.toArray(new Company[companies.size()]))
                .And(FinancialTransaction.Id().in(financialTransactionIdSet));
        Expression<FinancialTransaction> financialTransactionExpression = new Query<FinancialTransaction>()
                .QueryHint(" leading(psp_financial_transaction psp_company) ")
                .Where(financialTransactionCriterion)
                .EagerLoad(FinancialTransaction.PaycheckSplit().Company().equalTo(FinancialTransaction.Company()))
                .EagerLoad(eagarLoadProperties)
                .ReadOnly(true);

        return Application.find(FinancialTransaction.class, financialTransactionExpression);
    }

    private DomainEntitySet<Law> getAllLaws() {
        Expression<Law> lawExpression = new Query<Law>()
                .EagerLoad(Law.PaymentTemplate(),
                        Law.PaymentTemplate().Agency())
                .ReadOnly(true);

        return Application.find(Law.class, lawExpression);
    }

    private Map<String, String> getAllLawAgencyMap() {
        Map<String, String> paymentTemplateCodeAgencyMap = new HashMap<>();

        for (Law law : getAllLaws()) {
            paymentTemplateCodeAgencyMap.put(law.getLawId(), law.getPaymentTemplate().getAgency().getName());
        }

        return paymentTemplateCodeAgencyMap;
    }

    private void buildLawAgencyMap() {

        if (Objects.nonNull(allLawAgencyMap)) {
            return;
        }

        allLawAgencyMap = getAllLawAgencyMap();
        logger.info("Successfully loaded all laws and its agencies");
    }

    private Map<SpcfUniqueId, String> getFinancialTransactionCompanyMap(List<MtlTransactionRecord> mtlTransactionRecords) {
        Map<SpcfUniqueId, String> financialTransactionCompanyMap = new HashMap<>();

        for (MtlTransactionRecord mtlTransactionRecord : mtlTransactionRecords) {
            SpcfUniqueId spcfUniqueId = getValidSpcfUniqueId(mtlTransactionRecord.getTransactionNumber());
            if(Objects.isNull(spcfUniqueId)) {
                continue;
            }
            financialTransactionCompanyMap.put(spcfUniqueId, mtlTransactionRecord.getCustomerCompanyId());
        }
        return financialTransactionCompanyMap;
    }

    private SpcfUniqueId getValidSpcfUniqueId(String spcfUniqueIdString) {
        try {
            return SpcfUniqueId.createInstance(spcfUniqueIdString);
        } catch (SpcfIllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    private Map<String, FinancialTransaction> getFinancialTransactionMap(DomainEntitySet<FinancialTransaction> financialTransactions) {
        Map<String, FinancialTransaction> financialTransactionMap = new HashMap<>();

        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionMap.put(financialTransaction.getId().toString(), financialTransaction);
        }

        return financialTransactionMap;
    }

    private Set<SpcfUniqueId> getAddressIdSet(DomainEntitySet<FinancialTransaction> financialTransactions) {
        Set<SpcfUniqueId> addressIdSet = new HashSet<>();

        for (FinancialTransaction financialTransaction : financialTransactions) {
            SpcfUniqueId addressId = getAddressId(financialTransaction);
            if (Objects.isNull(addressId)) {
                continue;
            }

            addressIdSet.add(addressId);
        }

        return addressIdSet;
    }

    private Map<String, Address> getAddressMap(Set<SpcfUniqueId> addressIdSet) {
        Map<String, Address> addressMap = new HashMap<>();

        if (CollectionUtils.isEmpty(addressIdSet)) {
            return addressMap;
        }

        Expression<Address> addressExpression = new Query<Address>()
                .Where(Address.Id().in(addressIdSet))
                .ReadOnly(true);

        DomainEntitySet<Address> addresses = Application.find(Address.class, addressExpression);

        if (addresses.isEmpty()) {
            return addressMap;
        }

        for (Address address : addresses) {
            addressMap.put(address.getId().toString(), address);
        }

        return addressMap;
    }

    private Employee getEmployee(FinancialTransaction financialTransaction) {
        EmployeeBankAccount employeeBankAccount = financialTransaction.getEmployeeBankAccount();
        if (Objects.isNull(employeeBankAccount)) {
            return null;
        }
        return employeeBankAccount.getEmployee();
    }

    private Payee getPayee(FinancialTransaction financialTransaction) {
        PayeeBankAccount payeeBankAccount = financialTransaction.getPayeeBankAccount();
        if (Objects.isNull(payeeBankAccount)) {
            return null;
        }
        return payeeBankAccount.getPayee();
    }

    private String getEmployeeName(FinancialTransaction financialTransaction) {
        Employee employee = getEmployee(financialTransaction);

        if (Objects.isNull(employee)) {
            return MtlTransactionRecord.FIELD_NOT_AVAILABLE;
        }

        return employee.getFullName();
    }

    private String getPayeeName(FinancialTransaction financialTransaction) {
        Payee payee = getPayee(financialTransaction);

        if (Objects.isNull(payee)) {
            return MtlTransactionRecord.FIELD_NOT_AVAILABLE;
        }

        return payee.getName();
    }

    private String getAgencyName(FinancialTransaction financialTransaction) {
        String agencyName = MtlTransactionRecord.FIELD_NOT_AVAILABLE;
        Law law = financialTransaction.getLaw();

        if (Objects.isNull(law)) {
            return agencyName;
        }

        agencyName = allLawAgencyMap.get(law.getLawId());

        return StringUtils.isBlank(agencyName) ? MtlTransactionRecord.FIELD_NOT_AVAILABLE : agencyName;
    }

    private SpcfUniqueId getAddressId(FinancialTransaction financialTransaction) {
        SpcfUniqueId addressId = null;
        if (Objects.nonNull(financialTransaction.getPaycheckSplit())) {
            addressId = getEmployeeAddressId(financialTransaction);
        }
        return addressId;
    }

    private SpcfUniqueId getEmployeeAddressId(FinancialTransaction financialTransaction) {
        Employee employee = getEmployee(financialTransaction);

        if (Objects.isNull(employee)) {
            return null;
        }

        Address address = employee.getMailingAddress();

        if (Objects.isNull(address)) {
            return null;
        }

        return address.getId();
    }

    private String getEmployeeAddress(Map<String, Address> addressMap, FinancialTransaction financialTransaction) {
        SpcfUniqueId employeeAddressId = getEmployeeAddressId(financialTransaction);

        if (Objects.isNull(employeeAddressId)) {
            return MtlTransactionRecord.FIELD_NOT_AVAILABLE;
        }

        return getAddress(addressMap.get(employeeAddressId.toString()));
    }

    private String getPayeeAddress(FinancialTransaction financialTransaction) {
        Payee payee = getPayee(financialTransaction);

        if (Objects.isNull(payee)) {
            return null;
        }

        Address address = payee.getMailingAddress();

        if (Objects.isNull(address)) {
            return null;
        }

        return getAddress(address);
    }

    private String getEmployeePhone(FinancialTransaction financialTransaction) {
        Employee employee = getEmployee(financialTransaction);

        if (Objects.isNull(employee)) {
            return MtlTransactionRecord.FIELD_NOT_AVAILABLE;
        }

        return employee.getPhone();
    }

    private String getPayeePhone(FinancialTransaction financialTransaction) {
        Payee payee = getPayee(financialTransaction);

        if (Objects.isNull(payee)) {
            return MtlTransactionRecord.FIELD_NOT_AVAILABLE;
        }

        return payee.getPhone();
    }

    private String getAddress(Address address) {
        if (Objects.isNull(address)) {
            return MtlTransactionRecord.FIELD_NOT_AVAILABLE;
        }

        return address.getFullAddress();
    }

    private boolean isEnrichmentRequired(String recordValue) {
        return StringUtils.isBlank(recordValue) || StringUtils.equals(StringUtils.trim(recordValue), "N/A");
    }

    private BeneficiaryType getBeneficiaryType(MtlTransactionRecord mtlTransactionRecord) {
        return BeneficiaryType.valueOf(mtlTransactionRecord.getBeneficiaryType());
    }

    public enum BeneficiaryType {
        Employee("Employee"),
        Company("Payee"),
        Agency("Agency"),
        Sender("Sender");

        private String beneficiaryType;

        BeneficiaryType(String beneficiaryType) {
            this.beneficiaryType = beneficiaryType;
        }
    }

    public enum EnrichFieldName {
        // Enrich company information
        CUSTOMER_EIN,

        // Enrich Primary Principal information
        PRIMARY_PRINCIPAL_OFFICER_SSN,
        PRIMARY_PRINCIPAL_OFFICER_DOB,

        // Enrich Beneficiary Bank information
        BENEFICIARY_BANK,
        BENEFICIARY_BANK_ACCOUNT_NUMBER,

        // Ennrich Beneficiary information
        BENEFICIARY_NAME,
        BENEFICIARY_ADDRESS,
        BENEFICIARY_PHONE;
    }

}
