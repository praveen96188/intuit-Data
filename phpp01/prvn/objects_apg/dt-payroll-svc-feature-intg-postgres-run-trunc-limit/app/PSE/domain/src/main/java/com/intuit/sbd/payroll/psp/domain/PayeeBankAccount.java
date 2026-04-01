package com.intuit.sbd.payroll.psp.domain;

import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.SortableProperty;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * Hand-written business logic
 */
public class PayeeBankAccount extends BasePayeeBankAccount implements EntityChangeListener {
	public static SpcfLogger logger = SpcfLogManager.getLogger(PayeeBankAccount.class);
	private boolean isDuplicate=false;

    /**
     * Default constructor.
     */
    public PayeeBankAccount() {
        super();
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(Payee.class, getPayee().getId(), getSourceBankAccountId());
    }

    public static PayeeBankAccount findPayeeBankAccount(Payee pPayee,
                                                        String pSourceBankAccountId) {
        for (PayeeBankAccount pba : pPayee.getPayeeBankAccountCollection().sort(PayeeBankAccount.StatusEffectiveDate().Descending())) {
            if (pba.getSourceBankAccountId().equals(pSourceBankAccountId) &&
                    pba.getExpirationDate() == null) {
                return pba;
            }
        }
        return null;
    }


    public static DomainEntitySet<PayeeBankAccount> findPayeeBankAccounts(Company pCompany, String pSourcePayeeId) {
        Expression query =
                new Query<PayeeBankAccount>()
                        .Where(PayeeBankAccount.Payee().Company().equalTo(pCompany)
                                .And(PayeeBankAccount.Payee().SourcePayeeId().equalTo(pSourcePayeeId)))
                        .OrderBy(PayeeBankAccount.CreatedDate());

        return Application.find(PayeeBankAccount.class, query);
    }

    public static DomainEntitySet<PayeeBankAccount> findPayeeBankAccounts(Company pCompany, String pSourcePayeeId, String pPayeeBankAccountId) {
        Expression query =
                new Query<PayeeBankAccount>()
                        .Where(PayeeBankAccount.Payee().Company().equalTo(pCompany)
                                .And(PayeeBankAccount.Payee().SourcePayeeId().equalTo(pSourcePayeeId))
                                .And(PayeeBankAccount.SourceBankAccountId().equalTo(pPayeeBankAccountId)))
                        .OrderBy(PayeeBankAccount.CreatedDate());

        return Application.find(PayeeBankAccount.class, query);
    }

    public static PayeeBankAccount findActivePayeeBankAccount(Company pCompany, String pSourcePayeeId, String pAccountNumber, String pRoutingNumber, BankAccountType pBankAccountType) {
        Expression<PayeeBankAccount> query=null;
        Criterion<PayeeBankAccount> criterion =PayeeBankAccount.Payee().Company().equalTo(pCompany).And(PayeeBankAccount.Payee().SourcePayeeId().equalTo(pSourcePayeeId));
        if(pAccountNumber == null){
            criterion = criterion.And(PayeeBankAccount.BankAccount().AccountNumberEnc().isNull());
        } else {
            List<String> accountNumberEncList= EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName,pAccountNumber);
            criterion = criterion.And(PayeeBankAccount.BankAccount().AccountNumberEnc().in(accountNumberEncList));
        }
        criterion = criterion.And(PayeeBankAccount.BankAccount().RoutingNumber().equalTo(pRoutingNumber))
                .And(PayeeBankAccount.BankAccount().AccountTypeCd().equalTo(pBankAccountType))
                .And(PayeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
        query = new Query<PayeeBankAccount>().Where(criterion).OrderBy(PayeeBankAccount.CreatedDate().Descending());
        return Application.find(PayeeBankAccount.class, query).getFirst();
    }

    public static PayeeBankAccount findActivePayeeBankAccount(Company pCompany, String pSourcePayeeId, String pPayeeBankAccountId) {
        Expression<PayeeBankAccount> query =
                new Query<PayeeBankAccount>()
                        .Where(PayeeBankAccount.Payee().Company().equalTo(pCompany)
                                .And(PayeeBankAccount.Payee().SourcePayeeId().equalTo(pSourcePayeeId))
                                .And(PayeeBankAccount.SourceBankAccountId().equalTo(pPayeeBankAccountId)))
                        .OrderBy(PayeeBankAccount.CreatedDate().Descending());

        return Application.find(PayeeBankAccount.class, query).getFirst();
    }

    public static PayeeBankAccount findActivePayeeBankAccount(Company pCompany, Payee payee) {
        Expression<PayeeBankAccount> query =
                new Query<PayeeBankAccount>()
                        .Where(PayeeBankAccount.Payee().Company().equalTo(pCompany)
                        .And(PayeeBankAccount.Payee().equalTo(payee))
                        .And(PayeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active)))
                        .OrderBy(PayeeBankAccount.CreatedDate().Descending());

        return Application.find(PayeeBankAccount.class, query).getFirst();
    }

    public static PayeeBankAccount findOldPayeeBankAccount(Payee payee) {
        DomainEntitySet<PayeeBankAccount> payeeBankAccounts = payee.getPayeeBankAccountCollection().find(PayeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active))
                .sort(PayeeBankAccount.BankAccount().EffectiveDate());
        return payeeBankAccounts.isNotEmpty() ? payeeBankAccounts.get(payeeBankAccounts.size() - 1) : null;
    }

    public static DomainEntitySet<PayeeBankAccount> findPayeeBankAccount(BankAccount bankAccount) {
        Expression<PayeeBankAccount> query =
                new Query<PayeeBankAccount>()
                        .Where(PayeeBankAccount.BankAccount().equalTo(bankAccount))
                        .OrderBy(PayeeBankAccount.CreatedDate().Descending());
        return Application.find(PayeeBankAccount.class, query);
    }
    
    public static PayeeBankAccount findPayeeBankAccount(Company pCompany, String pPayeeBankAccountNum) {
        Expression<PayeeBankAccount> query = null;
        Criterion<PayeeBankAccount> criterion = PayeeBankAccount.Payee().Company().equalTo(pCompany);
        if(pPayeeBankAccountNum == null){
            criterion = criterion.And(PayeeBankAccount.BankAccount().AccountNumberEnc().isNull());
        } else {
            List<String> accountNumberEncList = EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName,pPayeeBankAccountNum);
            criterion = criterion.And(PayeeBankAccount.BankAccount().AccountNumberEnc().in(accountNumberEncList));
        }
        query =
                new Query<PayeeBankAccount>()
                        .Where(criterion)
                        .OrderBy(PayeeBankAccount.CreatedDate().Descending());
        return Application.find(PayeeBankAccount.class, query).getFirst();
    }

    public static List<PayeeBankAccount> getActiveBankAccountsForWalletIdCriteria(Payee payee, Boolean isWalletNull) {
        Criterion<PayeeBankAccount> payeeBankAccountCriterion = PayeeBankAccount.Payee().equalTo(payee)
                .And(PayeeBankAccount.StatusCd().in(BankAccountStatus.Active));
        if(isWalletNull) {
            payeeBankAccountCriterion = payeeBankAccountCriterion.And(PayeeBankAccount.BankAccount().WalletId().isNull());
        } else {
            payeeBankAccountCriterion = payeeBankAccountCriterion.And(PayeeBankAccount.BankAccount().WalletId().isNotNull());
        }
        return Application.executeQuery(PayeeBankAccount.class, payeeBankAccountCriterion);
    }
    
    /**
     * set the mandatory properties
     * @param PayeeBankAccount
     * @return
     */
    @Override
    public  JsonObject getChangedAttribute(){
    	  JsonObject json = new JsonObject();
    	  try{
          JsonObject jsonProperties = new JsonObject();
          jsonProperties.addProperty("PayeeBankAccount.SourceBankAccountId", this.getSourceBankAccountId());
	      jsonProperties.addProperty("BankStatus", this.getStatusCd().toString());

          jsonProperties.addProperty("BankAccount.EffectiveDate", this.getStatusEffectiveDate().toString());

		    long version=this.getVersion() +1;
		    jsonProperties.addProperty("Version", String.valueOf(version));
		    
	    	jsonProperties.addProperty("ModifiedDate", PSPDate.getPSPTime().toString());
	        
	        
          if(this.getBankAccount()!=null){

        	  if(this.getBankAccount()!=null && this.getBankAccount().getAccountTypeCd()!=null){
        		  jsonProperties.addProperty("BankAccount.AccountType", this.getBankAccount().getAccountTypeCd().toString());
        	  }
        	  if(this.getBankAccount()!=null){
        		  jsonProperties.addProperty("BankAccount.RoutingNumber", this.getBankAccount().getRoutingNumber());
        	  }
        	  if(this.getBankAccount()!=null){
        		  jsonProperties.addProperty("BankAccount.AccountNumber",this.getBankAccount().getAccountNumberEnc());
        	  }
          }
          
          if(this.getPayee()!=null){
        	  if(this.getPayee().getId()!=null){
            		  jsonProperties.addProperty("Payee.id", this.getPayee().getId().toString());
            	  }
	          jsonProperties.addProperty("Payee.Name", this.getPayee().getName());
	          jsonProperties.addProperty("Payee.TaxId", this.getPayee().getTaxIdEnc());

	          if(this.getPayee().getMailingAddress()!=null){
		          jsonProperties.addProperty("Payee.AddressLine1", this.getPayee().getMailingAddress().getAddressLine1());
		          jsonProperties.addProperty("Payee.AddressLine2", this.getPayee().getMailingAddress().getAddressLine2());
		          jsonProperties.addProperty("Payee.AddressLine3", this.getPayee().getMailingAddress().getAddressLine3());
		          jsonProperties.addProperty("Payee.City", this.getPayee().getMailingAddress().getCity());
		          jsonProperties.addProperty("Payee.Country", this.getPayee().getMailingAddress().getCountry());
		          jsonProperties.addProperty("Payee.State", this.getPayee().getMailingAddress().getState());
		          jsonProperties.addProperty("Payee.ZipCode", this.getPayee().getMailingAddress().getZipCode());
	          }
          }
          
          json.addProperty("SessionId", this.getSessionId());
          
	      if(this.getSessionId()!=null){
	        logger.info("session id is no null for PayeeBankAccount with PayeeBankAccountid "+this.getId().toString());
	      }
          json.add("PayeeBankAccount",jsonProperties);
          
		}catch(Exception ex){
	    	logger.error("couldnt set ChangedAttributes PayeeBankAccount with exception {} "+ ex.getMessage());
		}
	    return json;
    }
    
    
	@Override
	public String getuniqueId() {
		return this.getId().toString();
	}
	
	@Override
    public String getEntitiesName(){
		return "PayeeBankAccount";	
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

}