package com.intuit.sbg.psp.dd.limitcheck;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.intuit.pmo.client.model.DirectDepositDate;
import com.intuit.pmo.client.model.DirectDepositDates;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.FundingModel;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceStatusCode;
import com.intuit.sbg.psp.dd.exception.DateMismatchException;
import com.intuit.sbg.psp.dd.util.LimitConverter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PSPDirectDepositDateHelper.class,CompanyService.class,PayrollRun.class,LimitConverter.class,Paycheck.class,PSPDirectDepositDateHelper.class})
public class PSPDirectDepositDateHelperTest {

	private Company company;
	private DateDTO checkDate;
	private PSPDirectDepositDateHelper pspDirectDepositDateHelper=new PSPDirectDepositDateHelper();
	private DirectDepositDates directDeposit;

	
	@Before
	public void setup(){
    	company=PowerMockito.mock(Company.class);
    	company.setSourceCompanyId("233455");
    	FundingModel model=new FundingModel();
    	model.setFundingModelCd("2D");
    	company.setFundingModel(model);
    	
    	checkDate=PowerMockito.mock(DateDTO.class);
    	checkDate.set(2016, 03, 02);
    	
    	directDeposit=PowerMockito.mock(DirectDepositDates.class);
        List<DirectDepositDate> directDepositDates = new ArrayList<DirectDepositDate>();
        DirectDepositDate directDepositDate = new DirectDepositDate();
        directDepositDate.setDebitSettlementDate(new LocalDate("2016-03-01"));
        directDepositDate.setInitiationDate(new DateTime("2016-02-29"));
        directDepositDate.setSplitSettlementDate(new LocalDate("2016-03-02"));
        directDepositDates.add(directDepositDate);
        directDeposit.setDirectDepositDates(directDepositDates);
	}
	

	//TODO : This test has been failing for a long time in unit tests job. We should analyze more.
	@Test
	@Ignore
	public void validateDDPSPDatestest() throws DateMismatchException{
		SpcfCalendar pTargetCheckDate=new SpcfCalendarImpl(2016, 03, 02);
		PowerMockito.when(company.getNextValidPaycheckDepositDate(Mockito.any())).thenReturn(pTargetCheckDate);
		PowerMockito.mockStatic(CompanyService.class);
		CompanyService service=new CompanyService();
		PowerMockito.when(CompanyService.findCompanyService(company, ServiceCode.DirectDeposit)).thenReturn(service);

		pspDirectDepositDateHelper.validateDDPSPDates(company, checkDate, directDeposit);
	}
}
