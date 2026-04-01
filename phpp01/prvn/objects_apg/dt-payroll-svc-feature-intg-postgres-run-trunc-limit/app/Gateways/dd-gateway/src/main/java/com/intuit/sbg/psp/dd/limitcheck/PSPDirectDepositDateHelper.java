package com.intuit.sbg.psp.dd.limitcheck;

import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.intuit.pmo.client.model.DirectDepositDate;
import com.intuit.pmo.client.model.DirectDepositDates;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbg.psp.dd.exception.DateMismatchException;
import com.intuit.sbg.psp.dd.util.LimitConverter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * This class is used to check dates between
 * PSP and DD and validate if they are same
 * @author dchoudhary1
 *
 */
public class PSPDirectDepositDateHelper {

	/**
	 * Used to validate DD dates( debitsettlement, splitsettlement
	 * and initation date ) with PSP
	 * @param company
	 * @param payrollDate
	 * @param directDepositDates
	 * @return
	 * @throws DateMismatchException
	 */
	public boolean validateDDPSPDates(Company company,DateDTO payrollDate,DirectDepositDates directDepositDates) throws DateMismatchException {		
		List<DirectDepositDate> directDepositList=directDepositDates.getDirectDepositDates();	
		DirectDepositDate pspDate=pspSettlementDate(company,payrollDate);
		DirectDepositDate ddDate=directDepositList.get(0);
		//intiation date
		if(pspDate.getDebitSettlementDate()==null || pspDate.getDebitSettlementDate().toDate().compareTo(ddDate.getDebitSettlementDate().toDate())!=0){
			throw new DateMismatchException("Debit settlement date doesn't match between DD and PSP");
		}
		if(pspDate.getSplitSettlementDate()==null || pspDate.getSplitSettlementDate().toDate().compareTo(ddDate.getSplitSettlementDate().toDate())!=0){
			throw new DateMismatchException("Split settlement date doesn't match between DD and PSP");
		}
		if(!((pspDate.getInitiationDate()).toLocalDate().isEqual((ddDate.getInitiationDate()).toLocalDate() ))) {
			throw new DateMismatchException("Initation date doesn't match between DD and PSP");
		}
		return true;
	}
	
	

	/**
	 * Get the settlement dates for PSP
	 * @param company
	 * @param payrollRunDTO
	 * @return
	 * @throws DateMismatchException
	 */
	public DirectDepositDate pspSettlementDate(Company company, DateDTO payrollRunDTO) throws DateMismatchException{
		//reusing DD Pojo
		DirectDepositDate pspdate=new DirectDepositDate();
		
        // Get the next valid PaycheckSettlementDate based on the paycheck date
		SpcfCalendar paycheckSettlementDate = DateDTO.convertToSpcfCalendar(payrollRunDTO);
        paycheckSettlementDate = company.getNextValidPaycheckDepositDate(paycheckSettlementDate);

        //debit settlement date
        CompanyService service = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        SpcfCalendar debitsettlementDate = FinancialTransaction.findNextAvailableSettlementDate( service, paycheckSettlementDate);
        
        SpcfCalendar initDay=FinancialTransaction.getInitiationDate(paycheckSettlementDate,TransactionTypeCode.EmployeeDdCredit);
       
        pspdate.setDebitSettlementDate(LimitConverter.convertSpcfCalendarToLocalDate(debitsettlementDate));
        pspdate.setSplitSettlementDate(LimitConverter.convertSpcfCalendarToLocalDate(paycheckSettlementDate));
        pspdate.setInitiationDate(LimitConverter.convertSpcfCalendarToDateTime(initDay));

        return pspdate;
	}

}
