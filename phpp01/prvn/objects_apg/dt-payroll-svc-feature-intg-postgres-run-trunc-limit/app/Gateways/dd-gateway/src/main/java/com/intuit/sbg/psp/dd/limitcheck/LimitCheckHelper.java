package com.intuit.sbg.psp.dd.limitcheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.intuit.pmo.client.model.*;
import org.apache.commons.lang.StringUtils;

import com.intuit.psp.dd.pojo.LimitResponse;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentSplitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PaycheckStatusCode;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbg.psp.dd.exception.DateMismatchException;
import com.intuit.sbg.psp.dd.exception.LimitCheckException;
import com.intuit.sbg.psp.dd.gateway.DDGateway;
import com.intuit.sbg.psp.dd.util.LimitCheckResponse;
import com.intuit.sbg.psp.dd.util.LimitConverter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;



public class LimitCheckHelper {
	
	private final static Logger LOGGER = Logger.getLogger(LimitCheckHelper.class.getName());	
	private static DDGateway ddgateway;
	private static PSPDirectDepositDateHelper pspDirectDepositDateHelper;
	private boolean hasPendingAck=false;

	public LimitResponse isValidDDLimit(DateDTO checkDate, Company company){//TODO change order
		LimitResponse response =new LimitResponse();
		response.setLimitCheckResponse(LimitCheckResponse.DDERROR);
		try{
			RiskCheckRequest riskCheckRequest=createLimitCheckRequest(checkDate,company);
			if(ddgateway==null){
				ddgateway=new DDGateway();
			}
			PayrollCheckResponse payrollCheckResponse=ddgateway.checkLimit(riskCheckRequest);
			
			if(payrollCheckResponse != null ){
				if(payrollCheckResponse.getLimitCheck()!=null && payrollCheckResponse.getLimitCheck().equalsIgnoreCase("Fail")){
					
					for(LimitCheckError error: payrollCheckResponse.getLimitCheckErrorMessages()){
						LOGGER.severe("LimitCheckHelper:LimitCheckError failed with information:"+error.getMessage());
					}
					response.setLimitCheckResponse(LimitCheckResponse.FAIL); 
					if(hasPendingAck){
						response.setLimitCheckResponse(LimitCheckResponse.PARTIAL); 
					}
					response.setErrorMessages(payrollCheckResponse.getLimitCheckErrorMessages());
					return response;
				}
				else if(payrollCheckResponse.getLimitCheck()!=null && payrollCheckResponse.getLimitCheck().equalsIgnoreCase("Pass")){
					//validate the dates
					try{
						if(pspDirectDepositDateHelper==null){
							pspDirectDepositDateHelper=new PSPDirectDepositDateHelper();
						}
						response.setDDLimitCheckDate(payrollCheckResponse.getDirectDepositDates());
						pspDirectDepositDateHelper.validateDDPSPDates(company, checkDate, payrollCheckResponse.getDirectDepositDates());
					}catch(DateMismatchException dateMismatchException){
						//Rite now we are warning , but still pass through
						LOGGER.warning("LimitCheckHelper:isValidDDLimit the PSP and DD dates dont match for company:"+ company.getSourceCompanyId()
										+" with exception:"+dateMismatchException.getMessage());
					}
					response.setLimitCheckResponse(LimitCheckResponse.PASS);
					return response;
				}
			}
		}catch(LimitCheckException limitcheckException){
			response.setLimitCheckResponse(LimitCheckResponse.LIMITCHKERROR);
			LOGGER.severe("LimitCheckHelper:isValidDDLimit failed in creating request for company:"+ company.getSourceCompanyId()+" with exception:"+limitcheckException.getMessage());
		} catch (Exception limitEnpointException) {
			response.setLimitCheckResponse(LimitCheckResponse.DDERROR);
			LOGGER.severe("LimitCheckHelper:isValidDDLimit  failed in getting LimitCheck response for company:"+ company.getSourceCompanyId()+" with exception:"+limitEnpointException.getMessage());
		}
		return response;
	}
	
	/**
	 * Create the LimitCheck Request RiskCheckRequest
	 * @param pPayrollRunDTO
	 * @param company
	 * @param retryCount
	 * @throws LimitCheckException
	 */
	public RiskCheckRequest createLimitCheckRequest( DateDTO checkDate, Company company ) throws LimitCheckException{
		
        RiskCheckRequest limitCheckRequest=new RiskCheckRequest();
        
        //will throw LimitCheckException in case of Unack message
        List<SplitDetail> splitDetails=getSplitDetails(company);
        if(splitDetails==null || !(splitDetails.size()>0)){
        	throw new LimitCheckException("No paycheck found");
        }
        limitCheckRequest.setCheckDate(LimitConverter.convertDateDTOToLocalDate(checkDate));
        limitCheckRequest.setFundingModel(LimitConverter.convertFundingModel( company.getFundingModel()));
        limitCheckRequest.setSplitDetails(splitDetails);
        limitCheckRequest.setEmployerId(company.getSourceCompanyId());
        
        return limitCheckRequest;

	}
	
	
	/**
	 *  Get the split paychecks to send request to LimitCheck
	 *  
	 * @param pPayrollRun
	 * @param company
	 * @return
	 * @throws LimitCheckException
	 */
	public List<SplitDetail> getSplitDetails(Company company) throws LimitCheckException{
		List<SplitDetail> splitDetails = new ArrayList<>();
		//check if there is any payroll with status PendingToDD, SentToDD for company
        DomainEntitySet<PayrollRun> pendingToDDPayrolls = PayrollRun.findPayrollRunsByState(company, PayrollStatus.PendingToDD);

        DomainEntitySet<PayrollRun> sentToDDPayrolls =PayrollRun.findPayrollRunsByState(company, PayrollStatus.SentToDD);
        
        //STEP1: pendingToDDPayrolls Exist
        if ( pendingToDDPayrolls!=null  && pendingToDDPayrolls.size() > 0 ){
        	splitDetails = addpayrollsToDDPayrollsSplit(pendingToDDPayrolls, company);
        } 
        
        //STEP2.1: sentToDDPayrolls Exist- For payroll flow
        ArrayList<PayrollRunDTO> inMemoryPayroll=LimitConverter.getPayrollsInMemory(company);
        if  ( inMemoryPayroll!=null && (inMemoryPayroll.size()>0) ){
            for (PayrollRunDTO payrollInMemory : inMemoryPayroll) {
            	
        		Collection<PaycheckDTO> paychecks = payrollInMemory.getPaychecks();
                if (paychecks != null) {
                {
                    for (PaycheckDTO currCheck : paychecks) {
     
                        if(currCheck.getDdTransactions() != null) {
           					SplitDetail splitDetail=new SplitDetail();
                            for (DDTransactionDTO ddTransactionDTO : currCheck.getDdTransactions()) {
                                SpcfMoney currAmount = SpcfUtils.convertToSpcfMoney(ddTransactionDTO.getDDTransactionAmount());
               				 	splitDetail.setAmount(SpcfUtils.convertToBigDecimal(currAmount));
                            }
                            //get employee id
                            Employee currEmployee = Employee.findEmployee(company, currCheck.getEmployeeId());

           				 	splitDetail.setPayeeId(currEmployee.getId().toString());
           				 	splitDetails.add(splitDetail);
                        }

                    }
                }
        		
            }
            }
        }
        
        //STEP:2.2:For bill Flow
        ArrayList<BillPaymentDTO> inMemoryBillPayroll=LimitConverter.getBillPayrollsInMemory(company);
        if  ( inMemoryBillPayroll!=null && (inMemoryBillPayroll.size()>0) ){
            for (BillPaymentDTO billInMemory : inMemoryBillPayroll) {

                Payee currPayee = Payee.findPayee(company, billInMemory.getPayeeDTO().getSourcePayeeId());

                SplitDetail splitDetail=new SplitDetail();
				splitDetail.setAmount(SpcfUtils.convertToBigDecimal(billInMemory.getAmount()));
				splitDetail.setPayeeId(currPayee.getId().toString());
		   		splitDetails.add(splitDetail);
            			
            }
        }
        
        //STEP3: after exponential rety for sentToDDpayrolls
        if  ( sentToDDPayrolls!=null && (sentToDDPayrolls.size()>0) ){

        	List<String> sentPayrollId=LimitConverter.convertPayrollEntityToList(sentToDDPayrolls);
	         //find paychecks total amount
	       	try{
				boolean pendingStatusExists=exponentialBackOffRetry(company, sentPayrollId, PaycheckStatusCode.Created, splitDetails);
				if(pendingStatusExists==false){
		       		//Get the time we checked for unack message

					LOGGER.warning("LimitCheckHelper:getSplitDetails Unack Paycheck exists "
							+ "for the source company id:" +company.getSourceCompanyId()+"at Time:" +SpcfCalendar.getNow());
					this.hasPendingAck=true;
					//throw new LimitCheckException("LimitCheckHelper:getSplitDetails Unack Paycheck exists for the source"
						//	+ " company id:" +company.getSourceCompanyId()+"at Time:" +dateFormat.format(date) );
				}
	       	}catch (Exception e) {
				LOGGER.severe("LimitCheckHelper:getSplitDetails couldnt get splitDetails for Limit Check, with exception:"+e.getMessage());
				throw new LimitCheckException("LimitCheckHelper:getSplitDetails couldnt get splitDetails for Limit Check, with exception:"+e.getMessage());
			}
        } 
        
		return splitDetails;
	}

	
	/**
	 * 
	 * @param company
	 * @param sentToDDPayrolls
	 * @param status
	 * @param retryCount
	 */
	private boolean exponentialBackOffRetry(Company company, List<String> sentToDDPayrolls,PaycheckStatusCode status, List<SplitDetail> splitdetails) throws Exception{

        DomainEntitySet<Paycheck> paychecks=null;
		for (int n = 1; n <=DDRestClient.getAckRetryCount()+1; n++) {
		    try {
		    	// check of any paycheck that exist with status CREATED for the payroll
		    	paychecks=Paycheck.findPaychecksbyStatus(company, sentToDDPayrolls, status);
		    	
		    	if((paychecks!=null && paychecks.size()>0) && (n!=DDRestClient.getAckRetryCount()+1)){
		    		// Wait an indeterminate amount of time (range determined by n)
			        try {
			            Thread.sleep(((int) Math.round(DDRestClient.getAckRetryIntervalExponential()*n) * 1000)) ;
			        } catch (InterruptedException ignored) {
			            // Ignoring interruptions in the Thread sleep so that
			            // retries continue
			        }
		    		continue;
		    	}else if (paychecks==null || !(paychecks.size()>0)){
		    		return true;
		    	}
		   		 if( paychecks!=null && paychecks.size()>0){
		   			 addSplits(splitdetails,paychecks);
		   		 }
		    	return false;
		    	
		    }catch (Exception e) {
		    	// If we've exhausted our retries, throw the exception
		        if (n == DDRestClient.getAckRetryCount()+1) {
		            throw e;
		        }
		    }
		}
		return false;
	}


	/**
	 * Utility method to add pendingToDDPayroll to splitDetails
	 * 
	 * @param splitDetails
	 * @param pendingToDDPayrolls
	 * @param pPayrollRun
	 * @param company
	 * @throws LimitCheckException 
	 */
	private List<SplitDetail> addpayrollsToDDPayrollsSplit(DomainEntitySet<PayrollRun> pendingToDDPayrolls, Company company) throws LimitCheckException{
    	//find paychecks total amount
		List<SplitDetail> splitDetails=new ArrayList<>();
		for(PayrollRun payrollRun:pendingToDDPayrolls){
   		 
			
   		 //find all the paychecks
   		// DomainEntitySet<Paycheck> activePaychecks=Paycheck.findActivePaychecks(company, payrollRun.getSourcePayRunId());
			DomainEntitySet<Paycheck> activePaychecks=payrollRun.getPaycheckCollection();
			
   		 if( activePaychecks!=null && activePaychecks.size()>0){
   			addSplits(splitDetails,activePaychecks);
   		 }
   	 }
   	 return splitDetails;
	}
	
	
	/**
	 * 
	 * @param splitDetails
	 * @param activePaychecks
	 * @throws LimitCheckException 
	 */
	private void addSplits(List<SplitDetail> splitDetails, DomainEntitySet<Paycheck> activePaychecks) throws LimitCheckException{
		for(Paycheck paycheck:activePaychecks){
   			if(paycheck.getSourcePaycheckId()!=null && !StringUtils.isEmpty(paycheck.getSourcePaycheckId()) && paycheck.getNetAmount()!=null){

   					SplitDetail splitDetail=new SplitDetail();
   					if(paycheck.getDDEmployee()!=null){
   						splitDetail.setPayeeId(paycheck.getDDEmployee().getId().toString());
   					}else{
   						throw new LimitCheckException("No employee is present for paycheck"+paycheck.getSourcePaycheckId());

   					}
					SpcfMoney totalPaycheckAmount = paycheck.findTotalAmountPerPaycheck();   					
   				 	splitDetail.setAmount(SpcfUtils.convertToBigDecimal(totalPaycheckAmount));
   				 	splitDetails.add(splitDetail);
   			 }
   		}
	}
	
}
