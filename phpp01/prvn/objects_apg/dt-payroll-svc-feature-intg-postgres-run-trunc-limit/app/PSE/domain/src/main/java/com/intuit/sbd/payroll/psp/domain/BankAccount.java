package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.Hibernate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Hand-written business logic
 */
public class BankAccount extends BaseBankAccount implements IUpdatable  , EntityChangeListener {
	public static SpcfLogger logger = SpcfLogManager.getLogger(BankAccount.class);
    public static final int ROUTING_NUMBER_LENGTH = 9;
    public static final int ACCOUNT_NUMBER_LENGTH = 17;
    public static String AccountNumberKeyName="Bank_Acount_AccNo";
    private boolean isDuplicate=false;


    public static boolean isValidBankAccountTypeCode(ACHBankAccountType pACHBankAccountType) {
        return pACHBankAccountType != null &&
                (ACHBankAccountType.Checking.equals(pACHBankAccountType) ||
                        ACHBankAccountType.Savings.equals(pACHBankAccountType) ||
                        ACHBankAccountType.Ledger.equals(pACHBankAccountType) ||
                        ACHBankAccountType.Loan.equals(pACHBankAccountType));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public BankAccount()
	{
		super();
	}

    /**
     * @return
     */

    public ProcessResult validateBankAccount()
    {
        ProcessResult processResult = new ProcessResult();
        String accountId = null;

        if (this == null)
        {
            return processResult;
        }

        if (getId() != null) {
            accountId = getId().toString();
        }

        String accountNumber = getAccountNumber();
        if (accountNumber == null) {
            processResult.getMessages().InvalidValue(EntityName.BankAccount, accountId, "AcctNum");

        }

        String routingNumber = getRoutingNumber();
        if (routingNumber == null) {
            processResult.getMessages().InvalidValue(EntityName.BankAccount, accountId, "RoutingNum");
        }

        return processResult;
    }

    /**
     *
     * @param pBankAccount1
     * @param pBankAccount2
     * @return
     */
    public boolean equalsIgnoreBankNameSourceBankName(BankAccount pBankAccount2) {
        if (this == null || pBankAccount2 == null) {
            return false;
        }

        if (equals(pBankAccount2)) {
            return true;
        }

        String pBankAccount1AccountNumber = getAccountNumber();
        String pBankAccount2AccountNumber = pBankAccount2.getAccountNumber();

        BankAccountType pBankAccount1AccountTypeCode = getAccountTypeCd();
        BankAccountType pBankAccount2AccountTypeCode = pBankAccount2.getAccountTypeCd();

        String pBankAccount1RoutingNumber = getRoutingNumber();
        String pBankAccount2RoutingNumber = pBankAccount2.getRoutingNumber();

        return ((pBankAccount1AccountNumber == pBankAccount2AccountNumber || (pBankAccount1AccountNumber != null && pBankAccount1AccountNumber.equals(pBankAccount2AccountNumber))) &&
                (pBankAccount1AccountTypeCode == pBankAccount2AccountTypeCode || (pBankAccount1AccountTypeCode != null && pBankAccount1AccountTypeCode.equals(pBankAccount2AccountTypeCode))) &&
                (pBankAccount1RoutingNumber == pBankAccount2RoutingNumber || (pBankAccount1RoutingNumber != null && pBankAccount1RoutingNumber.equals(pBankAccount2RoutingNumber))));
    }

    public BankAccount updateBankAccountNumber(String pBankAccountNumber){
        setAccountNumber(pBankAccountNumber);
        return Application.save(this);
    }

    public BankAccount updateBankRoutingNumber(String pBankRoutingNumber){
        setRoutingNumber(pBankRoutingNumber);
        return Application.save(this);
    }

    public BankAccount updateBankAccountTypeCd(BankAccountType pBankAccountTypeCd) {
        setAccountTypeCd(pBankAccountTypeCd);
        return Application.save(this);
    }

    public BankAccount updateACHBankAccountTypeCd(ACHBankAccountType pACHBankAccountTypeCd) {
        setACHAccountTypeCd(pACHBankAccountTypeCd);
        return Application.save(this);
    }

    public boolean isPayCardAccount() {
        List<String> payCardRoutingList = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER, "").split(","));
        List<String> payCardAccountList = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX, "").split(","));

        if (payCardAccountList.size() != payCardRoutingList.size()) {
            throw new RuntimeException("Paycard system parameters are unbalanced.");
        }

        for (int i = 0 ; i < payCardRoutingList.size() ; i++) {
            if (this.getRoutingNumber().equals(payCardRoutingList.get(i)) && this.getAccountNumber().startsWith(payCardAccountList.get(i))) {
                return true;
            }
        }
        
        return false;
    }

    // ----- QBDT Token overrides -----


    public void setAccountNumber(String pAccountNumber) {
        if(!ObjectUtils.equals(getAccountNumber(), pAccountNumber)) {
            onUpdate();
        }
        super.setAccountNumberEnc(EncryptionUtils.deterministicEncrypt(AccountNumberKeyName,pAccountNumber));
    }

    @Override
    public void setAccountTypeCd(BankAccountType pAccountTypeCd) {
        if(!ObjectUtils.equals(getAccountTypeCd(), pAccountTypeCd)) {
            onUpdate();
        }
        super.setAccountTypeCd(pAccountTypeCd);
    }

    @Override
    public void setBankName(String pBankName) {
        if(!ObjectUtils.equals(getBankName(), pBankName)) {
            onUpdate();
        }
        super.setBankName(pBankName);
    }

    @Override
    public void setRoutingNumber(String pRoutingNumber) {
        if(!ObjectUtils.equals(getRoutingNumber(), pRoutingNumber)) {
            onUpdate();
        }
        super.setRoutingNumber(pRoutingNumber);
    }

    @Override
    public void setEmployeeBankAccount(EmployeeBankAccount pEmployeeBankAccount) {
        if(!ObjectUtils.equals(getEmployeeBankAccount(), pEmployeeBankAccount)) {
            onUpdate();
        }
        super.setEmployeeBankAccount(pEmployeeBankAccount);
    }

    public void onUpdate() {
        if(getEmployeeBankAccount() != null) {
            getEmployeeBankAccount().onUpdate();
        }
    }

    public String getAccountNumber() {
        return EncryptionUtils.deterministicDecrypt(AccountNumberKeyName,getAccountNumberEnc());
    }

	@Override
	public JsonObject getChangedAttribute() {
        JsonObject json = new JsonObject();
        try{
	        JsonObject jsonProperties = new JsonObject();
	        if(this.getEffectiveDate()!=null){
	        	jsonProperties.addProperty("BankAccount.EffectiveDate", this.getEffectiveDate().toString());
	        }
	        if(this.getAccountTypeCd()!=null){
	        	jsonProperties.addProperty("BankAccount.AccountType", this.getAccountTypeCd().toString());
	        }
	        
		    long version=this.getVersion() +1;
		    jsonProperties.addProperty("Version", String.valueOf(version));
	        	       
	    	jsonProperties.addProperty("ModifiedDate", PSPDate.getPSPTime().toString());     
	        jsonProperties.addProperty("BankAccount.RoutingNumber", this.getRoutingNumber());
	        jsonProperties.addProperty("BankAccount.AccountNumber",this.getAccountNumberEnc());
	        json.addProperty("SessionId", this.getSessionId());
	
	        json.add("BankAccount",jsonProperties);
		}catch(Exception ex){
	    	logger.error("couldnt set ChangedAttributes BankAccount with exception {} "+ ex.getMessage());
		}
	    return json;
    }

	@Override
	public String getuniqueId() {
		return this.getId().toString();
	}

	@Override
	public String getEntitiesName() {
		return "BankAccount";	
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

    public boolean isEqual(BankAccount bankaccountToUpdate) {
    	logger.info("Paycheck isEqual compare");
    	try{
			if( (this.getBankName()!=null) &&!((this.getBankName().toString()).equals(bankaccountToUpdate.getBankName().toString()))){
		    	logger.info("the bankname is different");
				return false;
			}

    	}catch(Exception ex){
	    	logger.error("error occurred while comparing bankaccountToUpdate"+ex.getMessage());
    	}
		return true;
	}

    public Company getAssociatedCompany(){
        DomainEntitySet<CompanyBankAccount> companyBankAccount = Application.find(CompanyBankAccount.class, CompanyBankAccount.BankAccount().equalTo(this));
        if(CollectionUtils.isNotEmpty(companyBankAccount) && companyBankAccount.size() == 1){
            return companyBankAccount.getFirst().getCompany();
        }

        DomainEntitySet<EmployeeBankAccount> employeeBankAccount = Application.find(EmployeeBankAccount.class, EmployeeBankAccount.BankAccount().equalTo(this));
        if(CollectionUtils.isNotEmpty(employeeBankAccount) && employeeBankAccount.size() == 1){
            Employee employee = employeeBankAccount.getFirst().getEmployee();
            if(Objects.isNull(employee)){
                return null;
            }
            return employee.getCompany();
        }

        DomainEntitySet<PayeeBankAccount> payeeBankAccount = Application.find(PayeeBankAccount.class, PayeeBankAccount.BankAccount().equalTo(this));
        if(CollectionUtils.isNotEmpty(payeeBankAccount) && payeeBankAccount.size() == 1){
            Payee payee = payeeBankAccount.getFirst().getPayee();
            if(Objects.isNull(payee)){
                return null;
            }
            return payee.getCompany();
        }

        return null;
    }

}