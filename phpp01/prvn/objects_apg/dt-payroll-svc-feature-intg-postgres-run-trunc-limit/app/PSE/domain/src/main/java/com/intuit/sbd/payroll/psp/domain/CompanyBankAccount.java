package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.Hibernate;

import java.util.Iterator;
import java.util.List;

/**
 * Hand-written business logic
 */
public class CompanyBankAccount extends BaseCompanyBankAccount implements IUpdatable , EntityChangeListener {

    public static SpcfLogger logger = SpcfLogManager.getLogger(CompanyBankAccount.class);
    private boolean isDuplicate=false;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static CompanyBankAccount findCompanyBankAccount(Company pCompany, String pSourceBankAccountId) {
        CompanyBankAccount companyBankAccount = null;

        NaturalKey naturalKey = new NaturalKey(CompanyBankAccount.class, pCompany.getId(), pSourceBankAccountId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            companyBankAccount = Application.findById(CompanyBankAccount.class, primaryKey);
        }
        if (companyBankAccount == null || companyBankAccount.getExpirationDate() != null) {
            DomainEntitySet<CompanyBankAccount> companyBankAccounts =
                    Application.find(CompanyBankAccount.class,
                                     CompanyBankAccount.Company().equalTo(pCompany)
                                     .And(CompanyBankAccount.SourceBankAccountId().equalTo(pSourceBankAccountId)
                                     .And(CompanyBankAccount.ExpirationDate().isNull())));

            if (companyBankAccounts.size() > 0) {
                companyBankAccount = companyBankAccounts.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, companyBankAccount.getId());
            }
        }

        return companyBankAccount;
    }

    public static CompanyBankAccount findExistingCBAIncludingExpired(Company pCompany, String pSourceBankAccountId, BankAccount pBankAccount) {
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = findCompanyBankAccountsIncludingExpired(pCompany, pSourceBankAccountId);

        for (CompanyBankAccount foundCBA : companyBankAccounts) {
            if (foundCBA.getBankAccount().equalsIgnoreBankNameSourceBankName(pBankAccount)) {
                return foundCBA;
            }
        }
        return null;
    }

    public static DomainEntitySet<CompanyBankAccount> findCompanyBankAccountsIncludingExpired(Company pCompany, String pSourceBankAccountId) {
        return Application.find(CompanyBankAccount.class,
                                CompanyBankAccount.Company().equalTo(pCompany)
                                .And(CompanyBankAccount.SourceBankAccountId().equalTo(pSourceBankAccountId)));

    }

    public static DomainEntitySet<CompanyBankAccount> findDeactivatedCompanyBankAccounts(Company pCompany, String pSourceBankAccountId) {
        return Application.find(CompanyBankAccount.class,
                                CompanyBankAccount.Company().equalTo(pCompany)
                                .And(CompanyBankAccount.SourceBankAccountId().equalTo(pSourceBankAccountId)
                                .And(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.Inactive))));

    }

    public static CompanyBankAccount findActiveCompanyBankAccount(Company pCompany) {
        DomainEntitySet<CompanyBankAccount> companyBankAccounts =
                Application.find(CompanyBankAccount.class,
                                                CompanyBankAccount.Company().equalTo(pCompany)
                                                .And(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.Active)));

        if (companyBankAccounts.size() > 1) {
            throw new RuntimeException("Company " + pCompany.getSourceSystemCd()
                    + ":" + pCompany.getSourceCompanyId() + " has more than one active account");
        }
        if (companyBankAccounts.size() != 1) {
            return null;
        }

        return companyBankAccounts.get(0);
    }

    public static DomainEntitySet<CompanyBankAccount> findCompanyBankAccounts(Company pCompany) {
        Expression<CompanyBankAccount> query =
                new Query<CompanyBankAccount>()
                        .Where(CompanyBankAccount.Company().equalTo(pCompany))
                        .OrderBy(CompanyBankAccount.StatusEffectiveDate().Descending());


        DomainEntitySet<CompanyBankAccount> companyBankAccounts = Application.find(CompanyBankAccount.class, query);
        return companyBankAccounts;
    }

    /**
     * Returns the most recent company bank account for a particular
     * given status
     *
     * @param pCompany
     * @param pStatus
     * @return
     */
    public static CompanyBankAccount findCompanyBankAccount(Company pCompany, BankAccountStatus pStatus) {
        CompanyBankAccount companyBankAccount = null;

        Expression<CompanyBankAccount> query =
                new Query<CompanyBankAccount>()
                        .Where(CompanyBankAccount.Company().equalTo(pCompany)
                               .And(CompanyBankAccount.StatusCd().equalTo(pStatus)
                               .And(CompanyBankAccount.ExpirationDate().isNull())))
                        .OrderBy(CompanyBankAccount.StatusEffectiveDate().Descending());

        DomainEntitySet<CompanyBankAccount> companyBankAccounts = Application.find(CompanyBankAccount.class, query);


        if (!companyBankAccounts.isEmpty()) {
            companyBankAccount = companyBankAccounts.get(0);
        }

        return companyBankAccount;
    }


    public static CompanyBankAccount findCompanyBankAccountIncludingExpired(Company pCompany, BankAccount pBankAccount) {
        CompanyBankAccount companyBankAccount = null;

        DomainEntitySet<CompanyBankAccount> companyBankAccounts =
                Application.find(CompanyBankAccount.class,
                                 CompanyBankAccount.Company().equalTo(pCompany)
                                 .And(CompanyBankAccount.BankAccount().equalTo(pBankAccount)));

        if (!companyBankAccounts.isEmpty()) {
            companyBankAccount = companyBankAccounts.get(0);
        }

        return companyBankAccount;
    }

    /**
     * To get all the bank accounts for a given source system code and the status code.
     * @param pSrouceSystemCode
     * @param pStatus
     * @return
     */
    public static DomainEntitySet<CompanyBankAccount> findCompanyBankAccounts(SourceSystemCode pSrouceSystemCode, BankAccountStatus pStatus) {
        Expression<CompanyBankAccount> query =
                new Query<CompanyBankAccount>()
                        .Where(CompanyBankAccount.Company().SourceSystemCd().equalTo(pSrouceSystemCode)
                                .And(CompanyBankAccount.StatusCd().equalTo(pStatus)));
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = Application.find(CompanyBankAccount.class, query);
        return(companyBankAccounts);
    }

    public static CompanyBankAccount findCompanyBankAccount(Company pCompany, BankAccount pBankAccount) {
        CompanyBankAccount companyBankAccount = null;

        DomainEntitySet<CompanyBankAccount> companyBankAccounts =
                Application.find(CompanyBankAccount.class,
                                 CompanyBankAccount.Company().equalTo(pCompany)
                                 .And(CompanyBankAccount.BankAccount().equalTo(pBankAccount)
                                 .And(CompanyBankAccount.ExpirationDate().isNull())));

        if (!companyBankAccounts.isEmpty()) {
            companyBankAccount = companyBankAccounts.get(0);
        }

        return companyBankAccount;
    }

    public static CompanyBankAccount findCompanyBankAccount(Company pCompany, String pAccountNumber, String pRoutingNumber, BankAccountType pBankAccountType) {
        CompanyBankAccount companyBankAccount = null;
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = null;
        Criterion<CompanyBankAccount> criterion = CompanyBankAccount.Company().equalTo(pCompany);
        if(pAccountNumber == null){
            criterion = criterion.And(CompanyBankAccount.BankAccount().AccountNumberEnc().isNull());
        }else{
            List<String> bankAccountEncList = EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName,pAccountNumber);
            criterion = criterion.And(CompanyBankAccount.BankAccount().AccountNumberEnc().in(bankAccountEncList));
        }
        criterion = criterion.And(CompanyBankAccount.BankAccount().RoutingNumber().equalTo(pRoutingNumber)
                .And(CompanyBankAccount.BankAccount().AccountTypeCd().equalTo(pBankAccountType)
                        .And(CompanyBankAccount.ExpirationDate().isNull()
                                .And(CompanyBankAccount.StatusCd().in(BankAccountStatus.Active,BankAccountStatus.PendingVerification)))));
        Expression<CompanyBankAccount> companyBankAccountExpression = new Query<CompanyBankAccount>().Where(criterion);
        companyBankAccounts = Application.find(CompanyBankAccount.class, companyBankAccountExpression).sort(Company().CreatedDate().Descending());
        if (!companyBankAccounts.isEmpty()) {
            companyBankAccount = companyBankAccounts.get(0);
        }

        return companyBankAccount;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public CompanyBankAccount()
	{
		super();
	}

    /**
     * Cancels EmployerVerificationDebits in Create and sets the CBA status to Inactive
     * @return the modified CBA or, if the CBA was already Inactive, the unmodified CBA
     */
    public CompanyBankAccount deactivate() {
        if (getStatusCd() == BankAccountStatus.Inactive) {
            return this;
        }

        // Cancel any pending verification transactions for this bank account
        DomainEntitySet<FinancialTransaction> verificationTransactions =
                FinancialTransaction.findFinancialTransactions(
                        getCompany().getSourceSystemCd(), getCompany().getSourceCompanyId(), getBankAccount(),
                        TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        for (FinancialTransaction transaction : verificationTransactions) {
            if (transaction.isCancellationAllowed()) {
                transaction.cancelFinancialTransaction();
            }
        }

        // update the CompanyBankAccount status
        updateBankAccountStatus(BankAccountStatus.Inactive);
        setStatusEffectiveDate(PSPDate.getPSPTime());
        setExpirationDate(PSPDate.getPSPTime());

        return Application.save(this);
    }

    public CompanyBankAccount activate() {
        if (getStatusCd() != BankAccountStatus.PendingVerification) {
            return this;
        }

        // Cancel any pending verification transactions for this bank account
        DomainEntitySet<FinancialTransaction> verificationTransactions =
                FinancialTransaction.findFinancialTransactions(
                        getCompany().getSourceSystemCd(), getCompany().getSourceCompanyId(), getBankAccount(),
                        TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
        for (FinancialTransaction transaction : verificationTransactions) {
            transaction.cancelFinancialTransaction();
        }

        // update the CompanyBankAccount status
        updateBankAccountStatus(BankAccountStatus.Active);
        setStatusEffectiveDate(PSPDate.getPSPTime());

        return Application.save(this);
    }

    /**
     * Adds a financial transaction to the database for the company bank account
     * that is used for the customer to verify the bank account's validity.
     * The credit bank account will be an INTUIT bank account
     * The debit bank acocunt is the company bank account
     * The transaction type is Employer Verification Debit
     * The settlement type is ACH
     * The settlement date is one day in advance
     * *
     *
     * @return
     */
    public FinancialTransaction addVerificationTransaction() {

        FinancialTransaction financialTransaction = new FinancialTransaction();

        financialTransaction.setCompany(getCompany());

        // assign the SKU based on the company's offering
        OfferingServiceChargeGroup group = OfferingServiceChargeGroup.findFirstOfferingServiceChargeGroup(getCompany(), OfferingServiceChargeType.BankVerificationDebit);
        OfferingServiceCharge charge = group.selectTier(1);
        financialTransaction.setSku(charge.getSKU());
        financialTransaction.setSkuQuantity(1);

        // The Company Bank Account will be the Debit Bank Account of the financial transaction
        financialTransaction.setDebitBankAccountType(BankAccountOwnerType.Company);
        financialTransaction.setDebitBankAccount(getBankAccount());

        TransactionType transactionType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerVerificationDebit);
        financialTransaction.setTransactionType(transactionType);

        //  Get Intuit Bank Account, which will be the credit bank account of the financial transaction
        //  Transaction type EMPLOYER_VERIFICATION_DEBIT, Credit Indicator = "C"
        financialTransaction.setCreditBankAccountType(BankAccountOwnerType.Intuit);
        financialTransaction.setCreditBankAccount(IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Credit).getBankAccount());

        // Settlement type is ACH
        financialTransaction.setSettlementTypeCd(SettlementType.ACH);

        // Settlement Date
        OffloadGroup offloadGroup = getCompany().getOffloadGroup();
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerVerificationDebit, offloadGroup);
        financialTransaction.setSettlementDate(settlementDate);
        financialTransaction.setOriginalSettlementDate(settlementDate);

        // Amount is a random value between $0.01 and $0.99
        financialTransaction.setFinancialTransactionAmount(FinancialTransaction.generateRandomAmount());

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        financialTransaction = Application.save(financialTransaction);
        FinancialTransactionState financialTransactionState = financialTransaction.addTransactionState(currentTransactionState);

        financialTransaction = Application.save(financialTransaction);

        financialTransaction.validateCanCreateFinancialTransaction();
        return financialTransaction;
    }

    public void updateBankAccountStatus(BankAccountStatus pNewBankAccountStatus) {
        BankAccountStatus oldBankAccountStatus = getStatusCd();
        if (oldBankAccountStatus == pNewBankAccountStatus) {
            // nothing to do
            return;
        }
        setStatusCd(pNewBankAccountStatus);

        CompanyEvent.createCBAStatusChangeEvent(Application.save(this),
                oldBankAccountStatus,
                pNewBankAccountStatus,
                PSPDate.getPSPTime());
    }

    public boolean bankAccountMeetsFraudCriteria(StringBuilder pCompanyNotes) {
        boolean exceptionState = false;

        DomainEntitySet<FraudBankAccount> fraudBankAccounts = findSimilarFraudBankAccounts(getCompany(), getBankAccount().getAccountNumber(), getBankAccount().getRoutingNumber());

        if (fraudBankAccounts.size() > 0) {
            exceptionState = true;
        }

        // If any companies matched, assemble the company notes entry
        for (FraudBankAccount fraudBankAccount : fraudBankAccounts) {
            Company company = fraudBankAccount.getCompany();

            String compStatus;
            if (company.isCompanyTerminated()) {
                compStatus = "Terminated";
            } else {
                compStatus = "On Hold " + company.getOnHoldNotesString();
            }

            switch (fraudBankAccount.getFraudBankAccountReason()) {
                case EmployerBankAccountOfTerminatedCompany:
                    pCompanyNotes.append("This company was not activated because the company bank account matches the company bank account of company ");
                    break;

                case EmployeeBankAccountOfTerminatedCompany:
                    pCompanyNotes.append("This company was not activated because the company bank account matches the employee bank account ");
                    pCompanyNotes.append(fraudBankAccount.getBankAccountOwnerName());
                    pCompanyNotes.append(" of company ");
                    break;
            }

            pCompanyNotes.append(company.getLegalName());
            pCompanyNotes.append(" (Source System=");
            pCompanyNotes.append(company.getSourceSystemCd());
            pCompanyNotes.append(" Source ID=");
            pCompanyNotes.append(company.getSourceCompanyId());
            pCompanyNotes.append(") with status of ");
            pCompanyNotes.append(compStatus);
            pCompanyNotes.append(".");
        }

        if (pCompanyNotes.length() > Company.MAX_LENGTH_NOTES) {
            pCompanyNotes.setLength(Company.MAX_LENGTH_NOTES);
        }

        return exceptionState;
    }

    private DomainEntitySet<Employee> findSimilarEmployeeBankAccounts(Company pCompany, String pBankAccountNumber, String pRoutingNumber) {
        String[] paramNames = new String[3];
        paramNames[0] = "routingNumber";
        paramNames[1] = "accountNumberEncList";
        paramNames[2] = "pCompany";

        Object[] paramValues = new Object[3];
        paramValues[0] = pRoutingNumber;
        paramValues[1] = EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName, pBankAccountNumber);
        paramValues[2] = pCompany;

        String namedQuery = "findEmployeesByBAFraudCriteriaENC";

        DomainEntitySet<Employee> employees = Application.findByNamedQueryUsingCache(Employee.class, namedQuery, paramNames, paramValues);
        return employees;
    }

    private DomainEntitySet<FraudBankAccount> findSimilarFraudBankAccounts(Company pCompany, String pBankAccountNumber, String pRoutingNumber) {

        String[] paramNames = new String[3];
        paramNames[0] = "routingNumber";
        paramNames[1] = "accountNumberEncList";
        paramNames[2] = "pCompany";

        Object[] paramValues = new Object[3];
        paramValues[0] = pRoutingNumber;
        paramValues[1] = EncryptionUtils.deterministicEncryptWithAllKeys(FraudBankAccount.AccountNumberKeyName, pBankAccountNumber);
        paramValues[2] = pCompany;

        String namedQuery = "findCompaniesByBAFraudCriteriaENC";
        DomainEntitySet<FraudBankAccount> results = Application.findByNamedQueryUsingCache(FraudBankAccount.class, namedQuery, paramNames, paramValues);
        return results;
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(CompanyBankAccount.class, getCompany().getId(), getSourceBankAccountId());
    }

    /**
     * Finds verification transactions for a company bank account starting from two weeks before
     * today's date
     *
     * @return
     */
    public DomainEntitySet<FinancialTransaction> getVerificationTransactions() {

        DomainEntitySet<FinancialTransaction> txnsToReturn = new DomainEntitySet<FinancialTransaction>();

        for (int i = 0; i < 2; i++) {
            NaturalKey naturalKey = new NaturalKey(FinancialTransaction.class, getId(), i);
            SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

            if (primaryKey == null) {
                break;
            }

            txnsToReturn.add(Application.findById(FinancialTransaction.class, primaryKey));
        }

        if (txnsToReturn.size() == 2) {
            return txnsToReturn;
        }

        SpcfCalendar dateToCompare = PSPDate.getPSPTime();

        CalendarUtils.clearTime(dateToCompare);
        Company company = getCompany();

        //dateToCompare.addWeeks(-2);
        int verificationLimitDays = 36500;
        LimitRule limitRule = LimitRule.findLimitRule(company, ServiceCode.DirectDeposit);
        if(limitRule != null) {
            verificationLimitDays =
                    Integer.parseInt(limitRule.findLimitValueByName(LimitValueType.CompanyBankAccountDurationLimitForVerification).getValue());
        }
        dateToCompare.addDays(-verificationLimitDays);

        DomainEntitySet<FinancialTransaction> financialTransactions = findDebitTransactions(dateToCompare, company);

        if (financialTransactions.size() > 2) {
            Iterator<FinancialTransaction> it = financialTransactions.iterator();
            txnsToReturn.add(it.next());
            txnsToReturn.add(it.next());
        }
        else {
            txnsToReturn.addAll(financialTransactions);
        }

        return txnsToReturn;
    }

    private DomainEntitySet<FinancialTransaction> findDebitTransactions(SpcfCalendar dateToCompare, Company company) {
        String[] paramNames = new String[5];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "sourceCompanyId";
        paramNames[2] = "bankAccount";
        paramNames[3] = "txnType";
        paramNames[4] = "fromDate";

        Object[] paramValues = new Object[5];
        paramValues[0] = company.getSourceSystemCd();
        paramValues[1] = company.getSourceCompanyId();
        paramValues[2] = getBankAccount();
        paramValues[3] = TransactionTypeCode.EmployerVerificationDebit;
        paramValues[4] = dateToCompare;

        DomainEntitySet<FinancialTransaction> financialTransactions = Application.findByNamedQuery("findFinTxnByCompanyBankAcctDateTxnType", paramNames, paramValues);
        return financialTransactions;
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setSourceBankAccountName(String pSourceBankAccountName) {
        // don't update the token when the name is set initially
        if(getSourceBankAccountName() != null && !ObjectUtils.equals(getSourceBankAccountName(), pSourceBankAccountName)) {
            onUpdate();
        }
        super.setSourceBankAccountName(pSourceBankAccountName);
    }

    @Override
    public void setStatusCd(BankAccountStatus pStatusCd) {
        boolean changingToActive =
                (getStatusCd() == null || getStatusCd() == BankAccountStatus.Inactive) &&
                        pStatusCd != null &&
                        pStatusCd.in(BankAccountStatus.Active, BankAccountStatus.PendingVerification);
        super.setStatusCd(pStatusCd);

        if(changingToActive) {
            onUpdate();
        }
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);
    }

    /**
     * set the mandatory properties
     * @param CompanyBankAccount
     * @return
     */
    @Override
    public  JsonObject getChangedAttribute(){
        JsonObject json = new JsonObject();
        try{
	        JsonObject jsonProperties = new JsonObject();
	        jsonProperties.addProperty("SourceBankAccountId", this.getSourceBankAccountId());
	        jsonProperties.addProperty("BankStatus", this.getStatusCd().toString());		      
	        jsonProperties.addProperty("BankAccount.EffectiveDate", this.getStatusEffectiveDate().toString());

	       long version=this.getVersion() +1;
		   jsonProperties.addProperty("Version", String.valueOf(version));

	    	jsonProperties.addProperty("ModifiedDate", PSPDate.getPSPTime().toString());

		    
	        
	        
	        jsonProperties.addProperty("BankAccount.AccountType", this.getBankAccount().getAccountTypeCd().toString());
	        jsonProperties.addProperty("BankAccount.RoutingNumber", this.getBankAccount().getRoutingNumber());
	        jsonProperties.addProperty("BankAccount.AccountNumber",this.getBankAccount().getAccountNumberEnc());
	        
	        jsonProperties.addProperty("Company.id", this.getCompany().getId().toString());
	        jsonProperties.addProperty("Company.Name", this.getCompany().getLegalName());
	        jsonProperties.addProperty("Company.FedTaxId", this.getCompany().getFedTaxIdEnc());
	        jsonProperties.addProperty("Company.AddressLine1", this.getCompany().getLegalAddress().getAddressLine1());
	        jsonProperties.addProperty("Company.AddressLine2", this.getCompany().getLegalAddress().getAddressLine2());
	        jsonProperties.addProperty("Company.AddressLine3", this.getCompany().getLegalAddress().getAddressLine3());
	        jsonProperties.addProperty("Company.City", this.getCompany().getLegalAddress().getCity());
	        jsonProperties.addProperty("Company.Country", this.getCompany().getLegalAddress().getCountry());
	        jsonProperties.addProperty("Company.State", this.getCompany().getLegalAddress().getState());
	        jsonProperties.addProperty("Company.ZipCode", this.getCompany().getLegalAddress().getZipCode());	        
	        json.addProperty("SessionId", this.getSessionId());
	        json.add("CompanyBankAccount",jsonProperties);
		}catch(Exception ex){
	    	logger.error("couldnt set ChangedAttributes CompanyBankAccount with exception {} "+ ex.getMessage());
		}
        return json;
    }
    
	@Override
	public String getuniqueId() {
		return this.getId().toString();
	}
	
	@Override
    public String getEntitiesName(){
		return "CompanyBankAccount";
    	
    }
	
	@Override
	public Long getEntityVersion() {
		return this.getVersion();
	}
	
	@Override
	public void isDuplicate(boolean duplicate) {
		 this.isDuplicate=duplicate;
	}

	@Override
	public boolean getDuplicate() {
		return isDuplicate;
	}

    public void onUpdate() {
        if(getCompany() != null && getStatusCd().in(BankAccountStatus.Active, BankAccountStatus.PendingVerification)) {
            getCompany().onUpdate();
        }
    }

    /**
     * Returns the most recent company bank account for a particular
     * given status
     *
     * @param pCompany
     * @param pStatus
     * @return
     */
    public static CompanyBankAccount findCompanyBankAccountFromCompany(Company pCompany, com.intuit.sbd.payroll.psp.domain.BankAccountStatus pStatus) {
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = pCompany.getCompanyBankAccountCollection()
                .find(CompanyBankAccount.StatusCd().equalTo(pStatus)
                        .And(CompanyBankAccount.StatusCd().equalTo(pStatus)))
                .sort(CompanyBankAccount.StatusEffectiveDate().Descending());

        return companyBankAccounts.isEmpty() ? null : companyBankAccounts.getFirst();
    }

    /**
     * @param pCompany
     * @return
     */
    public static CompanyBankAccount findCompanyBankAccount(Company pCompany)  {
        CompanyBankAccount companyBankAccount = findCompanyBankAccountFromCompany(pCompany, com.intuit.sbd.payroll.psp.domain.BankAccountStatus.PendingVerification);
        if (companyBankAccount == null) {
            companyBankAccount = findCompanyBankAccountFromCompany(pCompany, com.intuit.sbd.payroll.psp.domain.BankAccountStatus.Active);
            if (companyBankAccount == null) {
                companyBankAccount = findCompanyBankAccountFromCompany(pCompany, com.intuit.sbd.payroll.psp.domain.BankAccountStatus.Inactive);
            }
        }

        return companyBankAccount;
    }


    /**
     * Returns the most recent company bank account for a particular
     * given status
     *
     * @param pCompany
     * @param pStatus
     * @return
     */
    public static CompanyBankAccount findCompanyBankAccountByAccountNumber(Company pCompany, String pBankAccountNumber, String pRoutingNumber,BankAccountType pBankAccountType ) {

        List<String> bankAccountEncList = EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName,pBankAccountNumber);

        DomainEntitySet<CompanyBankAccount> companyBankAccounts = pCompany.getCompanyBankAccountCollection()
                .find(CompanyBankAccount.BankAccount().RoutingNumber().equalTo(pRoutingNumber)
                .And(CompanyBankAccount.BankAccount().AccountTypeCd().equalTo(pBankAccountType))
                .And(CompanyBankAccount.BankAccount().AccountNumberEnc().in(bankAccountEncList)))
                        .sort(CompanyBankAccount.StatusEffectiveDate().Descending());

        return companyBankAccounts.isEmpty() ? null : companyBankAccounts.getFirst();
    }
}