package com.intuit.sbd.payroll.psp.domain;

import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Hand-written business logic
 */
public class EmployeeBankAccount extends BaseEmployeeBankAccount implements IUpdatable, EntityChangeListener  {
    public static SpcfLogger logger = SpcfLogManager.getLogger(EmployeeBankAccount.class);
    private boolean isDuplicate=false;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static EmployeeBankAccount findEmployeeBankAccount(Employee pEmployee,
                                                       String pSourceBankAccountId) {
        for (EmployeeBankAccount eba : pEmployee.getEmployeeBankAccountCollection().sort(EmployeeBankAccount.StatusCd(), EmployeeBankAccount.StatusEffectiveDate().Descending())) {
            if (eba.getSourceBankAccountId().equals(pSourceBankAccountId)) {
                return eba;
            }
        }
        return null;
    }


    public static EmployeeBankAccount findLatestActiveEBA(Company pCompany, Employee employee) {
        Expression<EmployeeBankAccount> query =
                new Query<EmployeeBankAccount>()
                        .Where(EmployeeBankAccount.Employee().Company().equalTo(pCompany)
                                               .And(EmployeeBankAccount.Employee().equalTo(employee))
                                               .And(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active)))
                        .OrderBy(PayeeBankAccount.CreatedDate().Descending());

        return Application.find(EmployeeBankAccount.class, query).getFirst();
    }

    public static EmployeeBankAccount findOldEmployeeBankAccount(Employee employee, Integer accountOrder) {
        Criterion<EmployeeBankAccount> criteria = EmployeeBankAccount.Employee().equalTo(employee)
                .And(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active))
                .And(EmployeeBankAccount.AccountOrder().equalTo(accountOrder));
        DomainEntitySet<EmployeeBankAccount> foundEBA = Application.find(EmployeeBankAccount.class, criteria);
        if(foundEBA.isNotEmpty()) {
            return foundEBA.getFirst();
        }
        return null;
    }

    public static EmployeeBankAccount findEmployeeBankAccount(Employee pEmployee,
                                                              String pAccountNumber,
                                                              String pRoutingNumber) {
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts=null;
        Criterion<EmployeeBankAccount> criterion = null;
        if(pAccountNumber == null){
            criterion = EmployeeBankAccount.BankAccount().AccountNumberEnc().isNull();
        } else {
            List<String> accountNumberEncList= EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName,pAccountNumber);
            criterion = EmployeeBankAccount.BankAccount().AccountNumberEnc().in(accountNumberEncList);
        }
        criterion = criterion.And(EmployeeBankAccount.BankAccount().RoutingNumber().equalTo(pRoutingNumber))
                .And(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active))
                .And(EmployeeBankAccount.ExpirationDate().isNull());
        employeeBankAccounts = pEmployee.getEmployeeBankAccountCollection().find(criterion);
        /** an assisted employee could have two accounts setup with the same account information. Here we just use the first one. **/
        if(employeeBankAccounts.size() > 0) {
            return employeeBankAccounts.get(0);
        }
        return null;
    }

    public static DomainEntitySet<EmployeeBankAccount> findEmployeeBankAccounts(Company pCompany,
                                                                  String pSourceEmployeeId,
                                                                  String pSourceEmployeeBankAccountId) {
        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "srcEmployeeId";
        paramNames[2] = "sourceBankAccountId";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = pSourceEmployeeId;
        paramValues[2] = pSourceEmployeeBankAccountId;

        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts =
                Application.findByNamedQueryUsingCache(EmployeeBankAccount.class, "findEEBAByCompanyIdEEId", paramNames, paramValues);

        return employeeBankAccounts;
    }

    public static List<EmployeeBankAccount> getActiveBankAccountsForWalletIdCriteria(Employee employee, Boolean isWalletNull) {
        Criterion<EmployeeBankAccount> employeeBankAccountCriterion = EmployeeBankAccount.Employee().equalTo(employee)
                .And(EmployeeBankAccount.StatusCd().in(BankAccountStatus.Active));
        if(isWalletNull) {
            employeeBankAccountCriterion = employeeBankAccountCriterion.And(EmployeeBankAccount.BankAccount().WalletId().isNull());
        } else {
            employeeBankAccountCriterion = employeeBankAccountCriterion.And(EmployeeBankAccount.BankAccount().WalletId().isNotNull());
        }
        return Application.executeQuery(EmployeeBankAccount.class, employeeBankAccountCriterion);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public EmployeeBankAccount()
	{
		super();
	}

    /**
     * Deactivates an EmployeeBankAccount with status date of today
     *
     * @return
     */
    public void deactivate() {
        deactivate(PSPDate.getPSPTime());
    }

    /**
     * Deactivates an EmployeeBankAccount with a specific status date
     *
     * @param pStatusEffectiveDate
     * @return
     */

    public void deactivate(SpcfCalendar pStatusEffectiveDate) {

        setStatusCd(BankAccountStatus.Inactive);
        setStatusEffectiveDate(pStatusEffectiveDate);
    }

  
    

    /**
     * Expires an Employee Bank Account as of Today's date
     *
     */
    public void expireEmployeeBankAccount() {
        expireEmployeeBankAccount(PSPDate.getPSPTime());
    }

    /**
     * Expires an Employee Bank Account as of a specific date
     *
     * @param pExpirationDate
     */
    public void expireEmployeeBankAccount(
            SpcfCalendar pExpirationDate) {

        setStatusCd(BankAccountStatus.Inactive);
        setStatusEffectiveDate(PSPDate.getPSPTime());
        setExpirationDate(pExpirationDate);

    }

    public static void deactivateActiveEmployeeBankAccounts(Employee pEmployee, DomainEntitySet<EmployeeBankAccount> pActiveEmployeeBankAccounts, boolean pCreateEvents) {
        // deactivate accounts that are not included in the dto
        for (EmployeeBankAccount activeEmployeeBankAccount : pActiveEmployeeBankAccounts) {
            activeEmployeeBankAccount.expireEmployeeBankAccount();
            Application.save(activeEmployeeBankAccount);
            // find matching bank account
            DomainEntitySet<EmployeeBankAccount> activeAccounts = pEmployee.getEmployeeBankAccountCollection().find(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
            EmployeeBankAccount replacingAccount = null;
            DomainEntitySet<EmployeeBankAccount> matchingAccounts = activeAccounts.find(EmployeeBankAccount.AccountOrder().equalTo(activeEmployeeBankAccount.getAccountOrder()));
            if(matchingAccounts.size() > 0) {
                replacingAccount = matchingAccounts.get(0);
            } else if(activeAccounts.size() > 0) {
                replacingAccount = activeAccounts.get(0);
            }

            if(pCreateEvents && replacingAccount != null) {
                CompanyEvent.createEBAChangeEvent(activeEmployeeBankAccount, replacingAccount);
            }
        }
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(EmployeeBankAccount.class, getEmployee().getId(), getSourceBankAccountId());
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setStatusCd(BankAccountStatus pStatusCd) {
        if(!ObjectUtils.equals(getStatusCd(), pStatusCd)) {
            onUpdate();
        }
        super.setStatusCd(pStatusCd);
    }

    @Override
    public void setAmount(double pAmount) {
        if(!ObjectUtils.equals(getAmount(), pAmount)) {
            onUpdate();
        }
        super.setAmount(pAmount);
    }

    @Override
    public void setAmountType(QbdtNumericType pAmountType) {
        if(!ObjectUtils.equals(getAmountType(), pAmountType)) {
            onUpdate();
        }
        super.setAmountType(pAmountType);
    }

    @Override
    public void setAccountOrder(int pAccountOrder) {
        if(!ObjectUtils.equals(getAccountOrder(), pAccountOrder)) {
            onUpdate();
        }
        super.setAccountOrder(pAccountOrder);
    }

    @Override
    public void setBankAccount(BankAccount pBankAccount) {
        if(!ObjectUtils.equals(getBankAccount(), pBankAccount)) {
            onUpdate();
        }
        super.setBankAccount(pBankAccount);
    }

    @Override
    public void setEmployee(Employee pEmployee) {
        if(!ObjectUtils.equals(getEmployee(), pEmployee)) {
            onUpdate();
        }
        super.setEmployee(pEmployee);
    }

    /**
     * set the mandatory properties
     * @param EmployeeBankAccount
     * @return
     */
    @Override
    public  JsonObject getChangedAttribute(){
        JsonObject json = new JsonObject();
        try{
	        JsonObject jsonProperties = new JsonObject();
	        jsonProperties.addProperty("EmployeeBankAccount.SourceBankAccountId", this.getSourceBankAccountId());
	        jsonProperties.addProperty("BankStatus", this.getStatusCd().toString());		      
	        jsonProperties.addProperty("BankAccount.EffectiveDate", this.getStatusEffectiveDate().toUtc().toString());
	        
		    long version=this.getVersion() +1;
		    jsonProperties.addProperty("Version", String.valueOf(version));		    
	    	jsonProperties.addProperty("ModifiedDate", PSPDate.getPSPTime().toString());
	        
	        if( this.getBankAccount()!=null){
			        jsonProperties.addProperty("BankAccount.AccountType", this.getBankAccount().getAccountTypeCd().toString());
			        jsonProperties.addProperty("BankAccount.RoutingNumber", this.getBankAccount().getRoutingNumber());
			        jsonProperties.addProperty("BankAccount.AccountNumber",this.getBankAccount().getAccountNumberEnc());
	        }
	
	        if( this.getEmployee()!=null){
		        jsonProperties.addProperty("Employee.id", this.getEmployee().getId().toString());;
		        jsonProperties.addProperty("Employee.FirstName", this.getEmployee().getFirstName());
		        jsonProperties.addProperty("Employee.LastName", this.getEmployee().getLastName());  
		        jsonProperties.addProperty("Employee.BirthDate",  this.getEmployee().getBirthDateEnc());
		        jsonProperties.addProperty("Employee.TaxId", this.getEmployee().getTaxIdEnc());
		        	
		        if(this.getEmployee().getMailingAddress()!=null){
			        jsonProperties.addProperty("Employee.AddressLine1", this.getEmployee().getMailingAddress().getAddressLine1());
			        jsonProperties.addProperty("Employee.AddressLine2", this.getEmployee().getMailingAddress().getAddressLine2());
			        jsonProperties.addProperty("Employee.AddressLine3", this.getEmployee().getMailingAddress().getAddressLine3());
			        jsonProperties.addProperty("Employee.City", this.getEmployee().getMailingAddress().getCity());
			        jsonProperties.addProperty("Employee.Country", this.getEmployee().getMailingAddress().getCountry());
			        jsonProperties.addProperty("Employee.State", this.getEmployee().getMailingAddress().getState());
			        jsonProperties.addProperty("Employee.ZipCode", this.getEmployee().getMailingAddress().getZipCode());
		        }
	        }
	        
	        json.addProperty("SessionId", this.getSessionId());
	        
	        if(this.getSessionId()!=null){
	        	logger.info("session id is no null for EmployeeBankAccount with EmployeeBankAccountid "+this.getId().toString());
	        }
	        json.add("EmployeeBankAccount",jsonProperties);
		}catch(Exception ex){
	    	logger.error("couldnt set ChangedAttributes EmployeeBankAccount with exception {} "+ ex.getMessage());
		}
	    return json;
    }
    
	@Override
    public String getEntitiesName(){
		return "EmployeeBankAccount";	
    }

	@Override
	public Long getEntityVersion() {
		return this.getVersion();
	}
	
	@Override
	public String getuniqueId() {
		return this.getId().toString();
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
        if(getEmployee() != null) {
            getEmployee().onUpdate();
        }
    }


	public boolean isEqual(EmployeeBankAccount employeeBankAccountToUpdate) {
    	logger.info("isEqual compare");
    	try{
			if(!(this.getStatusCd().toString()).equals(employeeBankAccountToUpdate.getStatusCd().toString())){
		    	logger.info("the status is different");
				return false;
			}
			if(this.getSourceBankAccountId()!=null && !(this.getSourceBankAccountId().toString()).equals(employeeBankAccountToUpdate.getSourceBankAccountId().toString())){
		    	logger.info("the getSourceBankAccountId is different");
				return false;
			}

    	}catch(Exception ex){
	    	logger.error("error occurred while comparing EmployeeBankAccount"+ex.getMessage());
    	}
		return true;
	}

}