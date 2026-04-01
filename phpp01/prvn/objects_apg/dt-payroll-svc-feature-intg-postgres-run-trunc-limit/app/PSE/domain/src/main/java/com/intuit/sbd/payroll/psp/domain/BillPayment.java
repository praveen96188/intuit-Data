package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;

import java.util.Set;

import org.hibernate.Hibernate;

/**
 * Hand-written business logic
 */
@SuppressWarnings("unchecked")
public class BillPayment extends BaseBillPayment  implements EntityChangeListener{
    private static final SpcfLogger logger = SpcfLogManager.getLogger(BillPayment.class);
    private boolean isDuplicate=false;

	/**
     * Finds a bill payment for a given source id and company
     *
     * @param pCompany - company
     * @param pSourceBillPaymentId - source payment id
     * @return billPayment
     */
    public static BillPayment findBillPaymentBySourceId(Company pCompany, String pSourceBillPaymentId) {

        DomainEntitySet<BillPayment> billPayments = Application.find(BillPayment.class,
                BillPayment.PayrollRun().Company().equalTo(pCompany)
                        .And(BillPayment.SourceId().equalTo(pSourceBillPaymentId)));

        // there should only be one bill payment for a given company with a given source id
        if(billPayments.size() > 1) {
            throw new RuntimeException("More than one bill payment found for company " +
                    pCompany.getSourceSystemCd() + ":" + pCompany.getSourceCompanyId() + " and source id: " + pSourceBillPaymentId);
        }

        if(billPayments.size() == 1) {
            return billPayments.get(0);
        }

        return null;
    }

    /**
     *
     * @param pCompany
     * @param pSourceBillPaymentId
     * @return
     */
    public static DomainEntitySet<BillPayment> findBillPaymentBySourceIds(Company pCompany, Set<String> pSourceBillPaymentId) {
        if (pSourceBillPaymentId == null || pSourceBillPaymentId.size() == 0) {
            return new DomainEntitySet<BillPayment>();
        }
        DomainEntitySet<BillPayment> billPayments = Application.find(BillPayment.class,
                                                                     BillPayment.PayrollRun().Company().equalTo(pCompany)
                                                                                .And(BillPayment.SourceId().in(pSourceBillPaymentId)));
        return billPayments;
    }
    

	@Override
	public JsonObject getChangedAttribute() {
  	  JsonObject json = new JsonObject();
  	  try{
	      JsonObject jsonProperties = new JsonObject();
	      
	      if(this.getAmount()!=null){
	    	  jsonProperties.addProperty("NetAmount", this.getAmount().toString());
	      }
	      if(this.getId()!=null){
	    	  jsonProperties.addProperty("BillPaymentId", this.getId().toString());
	      }
	      		      
	      if(this.getPayrollRun()!=null && this.getPayrollRun().getPaycheckSettlementDate()!=null){
		      jsonProperties.addProperty("PayrollRun.PaycheckSettlementDate", this.getPayrollRun().getPaycheckSettlementDate().toString());  
	      }
	      
		    long version=this.getVersion() +1;
		    jsonProperties.addProperty("Version", String.valueOf(version));	   
	    	jsonProperties.addProperty("ModifiedDate", PSPDate.getPSPTime().toString());
	       
	      JsonArray arrayPSPbillSplit=new JsonArray();
	      JsonObject billPaymentsplitjson=null;
	      for(BillPaymentSplit billPaymentsplit:this.getBillPaymentSplitSet()){
	    	  billPaymentsplitjson=new  JsonObject();
	    	  billPaymentsplitjson.addProperty("BillPaymentSplit.Amount",billPaymentsplit.getAmount().toString());
	    	  billPaymentsplitjson.addProperty("BillPaymentSplit.PayeeBankAccount",billPaymentsplit.getPayeeBankAccount().getId().toString());
	    	  arrayPSPbillSplit.add(billPaymentsplitjson);
	      }
	      
	      json.add("moneyDistributionLine",arrayPSPbillSplit);
	      json.addProperty("SessionId", this.getSessionId());
	      
	        if(this.getSessionId()!=null){
	        	logger.info("session id is no null for BillPayment with BillPaymentid "+this.getId().toString());
	        }
	      json.add("BillPayment",jsonProperties);
      
		}catch(Exception ex){
	    	logger.error("couldnt set ChangedAttributes BillPayment with exception {} "+ ex.getMessage());
		}
	    return json;
    }

	@Override
	public String getuniqueId() {
		return this.getId().toString();
	}

	@Override
	public String getEntitiesName() {
		return "BillPayment";	
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