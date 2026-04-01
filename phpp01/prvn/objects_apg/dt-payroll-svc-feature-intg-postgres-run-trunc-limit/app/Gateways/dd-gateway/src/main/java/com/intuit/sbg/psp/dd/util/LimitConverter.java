package com.intuit.sbg.psp.dd.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FundingModel;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbg.psp.dd.exception.LimitCheckException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class LimitConverter {

	private final static Logger LOGGER = Logger.getLogger(LimitConverter.class.getName());	
    private static final String PAYROLLS_IN_MEMORY_CACHE_KEY = "PayrollInMemoryFromPayrollSubmitCore";
    private static final String DDPAYROLLS_IN_MEMORY_CACHE_KEY = "DDPayrollInMemoryFromPayrollSubmitCore";
    private static final String BILLPAYMENTS_IN_MEMORY_CACHE_KEY = "BillPaymentInMemoryFromBillPaymentSubmitCore";


	/**
	 * convert the Funding Model
	 * @param fundingModel
	 * @return
	 * @throws LimitCheckException
	 */
	public static BigInteger convertFundingModel(FundingModel fundingModel) throws LimitCheckException{
		if (fundingModel.getFundingModelCd().equals("2D")) {
			return BigInteger.valueOf(2);
		} else if (fundingModel.getFundingModelCd().equals("5D")) {
			return BigInteger.valueOf(5);
		}
			LOGGER.severe("LimitCheckHelper:convertFundingModel Doesn't have correct funding Model value:"+fundingModel.getFundingModelCd().toString());
			throw new LimitCheckException("LimitCheckHelper:convertFundingModel Doesn't have correct funding Model value:"
					+ ""+fundingModel.getFundingModelCd().toString());
	}
	
	
	/**
	 * convert DateDTO To LocalDate
	 * @param calendarTime
	 * @return
	 * @throws LimitCheckException
	 */
	public static LocalDate convertDateDTOToLocalDate(DateDTO calendarTime) throws  LimitCheckException{
		
		SpcfCalendar spcfCalendar=DateDTO.convertToSpcfCalendar(calendarTime);
		return convertSpcfCalendarToLocalDate(spcfCalendar);
	}
	
	

	/**
	 * convert spcfcalendar to LocalDate
	 * @param source
	 * @return
	 * @throws LimitCheckException
	 */
	public static LocalDate convertSpcfCalendarToLocalDate(SpcfCalendar source) {
		if (source == null) {
			return null;
		}
		return new LocalDate(source.getYear(), source.getMonth(), source.getDay());
	}
	
	/**
	 * convert spcfcalendar to DateTime
	 * @param source
	 * @return
	 * @throws LimitCheckException
	 */
	public static DateTime convertSpcfCalendarToDateTime(SpcfCalendar source) {
		if (source == null) {
			return null;
		}
		
		return new DateTime(source.getTimeInMilliseconds()).withZone(DateTimeZone.UTC);
	}
	/**
	 * convert DateTime to SpcfCalendar
	 * @param source
	 * @return
	 *
	 */
	public static  SpcfCalendar convertDateTimeToSpcfCalendar(DateTime source) {
		if (source == null) {
			return null;
		}
		return SpcfCalendar.createInstance(source.getMillis());
	}
	/**
	 * convert LocalTime to SpcfCalendar
	 * @param source
	 * @return
	 *
	 */
	public static SpcfCalendar convertLocalTimeToSpcfCalendar(LocalDate source) {
		if (source == null) {
			return null;
		}
		return  SpcfCalendar.createInstance(source.getYear(), source.getMonthOfYear(), source.getDayOfMonth());
	}
	/**
	 * 
	 * @param entitydata
	 * @return
	 */
	public static List<String> convertPayrollEntityToList(DomainEntitySet<PayrollRun> entitydata){
        if (entitydata != null && entitydata.size() > 0) {
        	List<String> payrollId=new ArrayList<>();
        	for( PayrollRun payrollRun:entitydata)
        	{
        		payrollId.add(payrollRun.getSourcePayRunId());
        	}
    		return payrollId;
        }
        return null;
	}
		
	/**
	 * Find in memory Payrolls
	 * @param company
	 * @return
	 */
    public static ArrayList<PayrollRunDTO> getPayrollsInMemory(Company company) {
        ArrayList<PayrollRunDTO> payrollsInMemory = Application.getSessionCache().getNonHibernateObject(PAYROLLS_IN_MEMORY_CACHE_KEY  + ":" + company.getId());
        ArrayList<PayrollRunDTO> ddPayrollsInMemory = Application.getSessionCache().getNonHibernateObject(DDPAYROLLS_IN_MEMORY_CACHE_KEY  + ":" + company.getId());
        
        ArrayList<PayrollRunDTO> finalPayroll =new ArrayList<PayrollRunDTO>();

        if(payrollsInMemory !=null){
        	finalPayroll.addAll(payrollsInMemory);
        }
        if(ddPayrollsInMemory !=null){
        	finalPayroll.addAll(ddPayrollsInMemory);
        }

      
        return finalPayroll;
    }
    
    /**
     * Find in memory bills
     * @param company
     * @return
     */
    public static ArrayList<BillPaymentDTO> getBillPayrollsInMemory(Company company) {
        ArrayList<BillPaymentDTO> billPayrollsInMemory = Application.getSessionCache().getNonHibernateObject(BILLPAYMENTS_IN_MEMORY_CACHE_KEY  + ":" + company.getId());

    	  return billPayrollsInMemory;
    }
    
}
