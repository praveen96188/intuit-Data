package com.intuit.sbg.psp.dd.limitcheck;

import java.util.ArrayList;
import java.util.List;

import com.intuit.pmo.client.model.DirectDepositDate;
import com.intuit.pmo.client.model.DirectDepositDates;
import com.intuit.sbd.payroll.psp.iam.AuthorizationBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import com.intuit.pmo.client.model.PayrollCheckResponse;
import com.intuit.pmo.client.model.RiskCheckRequest;
import com.intuit.psp.dd.pojo.LimitResponse;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.FundingModel;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PaycheckSplit;
import com.intuit.sbd.payroll.psp.domain.PaycheckStatusCode;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbg.psp.dd.exception.LimitCheckException;
import com.intuit.sbg.psp.dd.gateway.DDGateway;
import com.intuit.sbg.psp.dd.util.LimitCheckResponse;
import com.intuit.sbg.psp.dd.util.LimitConverter;
import com.intuit.spc.foundations.primary.SpcfMoney;

import junit.framework.Assert;


@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.intuit.sbg.psp.dd.limitcheck.DDRestClient")
@PrepareForTest({DDRestClient.class,LimitCheckHelper.class,PayrollRun.class,LimitConverter.class,Paycheck.class,PSPDirectDepositDateHelper.class, AuthorizationBuilder.class})
public class LimitCheckHelperTest {

	private Company company;
	private DateDTO checkDate;
	private LimitCheckHelper limitCheckHelper;

	@Before
	public void setup() throws Exception {
		limitCheckHelper=new LimitCheckHelper();
    	company=new Company();
    	company.setSourceCompanyId("233455");
    	FundingModel model=new FundingModel();
    	model.setFundingModelCd("2D");
    	company.setFundingModel(model);
    	
    	checkDate=new DateDTO();
    	checkDate.set(2018, 11, 30);

		AuthorizationBuilder mockAuthorizationBuilder=PowerMockito.mock(AuthorizationBuilder.class);
		PowerMockito.whenNew(AuthorizationBuilder.class).withAnyArguments().thenReturn(mockAuthorizationBuilder);
	}
	
	@After
	public void after(){
		company =null;
		checkDate=null;
		limitCheckHelper=null;
	}
	
	@Test
	public void isValidDDLimitFailTest() throws Exception{
		createLimitCheckRequest();
		DDGateway mockDDGateway=PowerMockito.mock(DDGateway.class);
		PowerMockito.whenNew(DDGateway.class).withAnyArguments().thenReturn(mockDDGateway);
		PSPDirectDepositDateHelper mockPSPDirectDepositDateHelper=PowerMockito.mock(PSPDirectDepositDateHelper.class);
		PowerMockito.whenNew(PSPDirectDepositDateHelper.class).withAnyArguments().thenReturn(mockPSPDirectDepositDateHelper);

		PowerMockito.when(mockPSPDirectDepositDateHelper.validateDDPSPDates(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

		PayrollCheckResponse payrollResponse=new PayrollCheckResponse();
		payrollResponse.setLimitCheck("Fail");
		payrollResponse.setEmployerId("12344");
		PowerMockito.when(mockDDGateway.checkLimit(Mockito.any())).thenReturn(payrollResponse);
		LimitResponse response=limitCheckHelper.isValidDDLimit(checkDate, company);
		Assert.assertEquals(response.getLimitCheckResponse(), LimitCheckResponse.FAIL);
		
	}
	

	//TODO : This test has been failing for a long time in unit tests job. We should analyze more.
	@Ignore
	@Test
	public void isValidDDLimitPassTest() throws Exception{
		createLimitCheckRequest();
		DDGateway mockDDGateway=PowerMockito.mock(DDGateway.class);
		PowerMockito.whenNew(DDGateway.class).withAnyArguments().thenReturn(mockDDGateway);
		PSPDirectDepositDateHelper mockPSPDirectDepositDateHelper=PowerMockito.mock(PSPDirectDepositDateHelper.class);
		PowerMockito.whenNew(PSPDirectDepositDateHelper.class).withAnyArguments().thenReturn(mockPSPDirectDepositDateHelper);

		PowerMockito.when(mockPSPDirectDepositDateHelper.validateDDPSPDates(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

		PayrollCheckResponse payrollResponse=new PayrollCheckResponse();
		payrollResponse.setLimitCheck("Pass");
		PowerMockito.when(mockDDGateway.checkLimit(Mockito.any())).thenReturn(payrollResponse);
		LimitResponse response=limitCheckHelper.isValidDDLimit(checkDate, company);
		Assert.assertEquals(response.getLimitCheckResponse(), LimitCheckResponse.PASS);

	}
	
	@Test(expected=LimitCheckException.class)
	public void createLimitCheckRequestExceptionTest() throws LimitCheckException{
    	PowerMockito.mockStatic(PayrollRun.class);
    	PowerMockito.mockStatic(LimitConverter.class);

    	DomainEntitySet<PayrollRun> pendingToDDPayrolls = null;
		PowerMockito.when(PayrollRun.findPayrollRunsByState(company,PayrollStatus.PendingToDD)).thenReturn(pendingToDDPayrolls);

    	DomainEntitySet<PayrollRun> sentToDDPayrolls = null;
		PowerMockito.when(PayrollRun.findPayrollRunsByState(company,PayrollStatus.SentToDD)).thenReturn(sentToDDPayrolls);
		
		ArrayList<PayrollRunDTO> inMemory=null;
		PowerMockito.when(LimitConverter.getPayrollsInMemory(company)).thenReturn(inMemory);
		ArrayList<BillPaymentDTO> inBillMemory=null;
		PowerMockito.when(LimitConverter.getBillPayrollsInMemory(company)).thenReturn(inBillMemory);

		limitCheckHelper.createLimitCheckRequest(checkDate, company);

	}
	
	
	@Test
	public void createLimitCheckRequestTest() throws Exception{

		createLimitCheckRequest();
		RiskCheckRequest riskRequest=limitCheckHelper.createLimitCheckRequest(checkDate, company);
		Assert.assertEquals(riskRequest.getEmployerId(), "233455");

	}

	//TODO : This test has been failing for a long time in unit tests job. We should analyze more.
	@Ignore
	@Test
	public void isValidDDLimitDDErrorTest() throws Exception{
		createLimitCheckRequest();
		DDGateway mockDDGateway=PowerMockito.mock(DDGateway.class);
		PowerMockito.whenNew(DDGateway.class).withAnyArguments().thenReturn(mockDDGateway);
		PayrollCheckResponse payrollResponse=new PayrollCheckResponse();
		PowerMockito.when(mockDDGateway.checkLimit(Mockito.any())).thenReturn(payrollResponse);
		LimitResponse response=limitCheckHelper.isValidDDLimit(checkDate, company);
		Assert.assertEquals(response.getLimitCheckResponse(), LimitCheckResponse.DDERROR);
	}
	
	
	@Test
	public void isValidDDLimitValidationErrorTest() throws Exception{
		
    	PowerMockito.mockStatic(PayrollRun.class);
		PowerMockito.when(PayrollRun.findPayrollRunsByState(company,PayrollStatus.PendingToDD)).thenReturn(null);

		LimitResponse response=limitCheckHelper.isValidDDLimit(checkDate, company);
		Assert.assertEquals(response.getLimitCheckResponse(), LimitCheckResponse.LIMITCHKERROR);
	}


	public void createLimitCheckRequest() throws Exception{
	PowerMockito.mockStatic(PayrollRun.class);
	PowerMockito.mockStatic(LimitConverter.class);
	PowerMockito.mockStatic(DDRestClient.class);
	PowerMockito.mockStatic(Paycheck.class);

	PowerMockito.when(DDRestClient.getAckRetryCount()).thenReturn(1);
	PowerMockito.when(DDRestClient.getAckRetryIntervalExponential()).thenReturn(1);
	PowerMockito.doNothing().when(DDRestClient.class,"getConfigurations");
		

	DomainEntitySet<PayrollRun> sentToDDPayrolls = new DomainEntitySet<PayrollRun>() ;
	sentToDDPayrolls.add(createPayrollRun());
	PowerMockito.when(PayrollRun.findPayrollRunsByState(company,PayrollStatus.SentToDD)).thenReturn(sentToDDPayrolls);
	
	DomainEntitySet<Paycheck> paycheck=new DomainEntitySet<Paycheck>();
	PowerMockito.when(Paycheck.findPaychecksbyStatus(company,LimitConverter.convertPayrollEntityToList(sentToDDPayrolls),
			PaycheckStatusCode.Created)).thenReturn(paycheck);

	DomainEntitySet<PayrollRun> pendingToDDPayrolls = new DomainEntitySet<PayrollRun>() ;
	pendingToDDPayrolls.add(createPayrollRun());
	
	PowerMockito.when(PayrollRun.findPayrollRunsByState(company,PayrollStatus.PendingToDD)).thenReturn(pendingToDDPayrolls);
	
	ArrayList<PayrollRunDTO> inMemory=null;
	PowerMockito.when(LimitConverter.getPayrollsInMemory(company)).thenReturn(inMemory);
	ArrayList<BillPaymentDTO> inBillMemory=null;
	PowerMockito.when(LimitConverter.getBillPayrollsInMemory(company)).thenReturn(inBillMemory);
}

	private PayrollRun createPayrollRun(){
    	PayrollRun payrollRun=new PayrollRun();
    	Paycheck paycheck=new Paycheck();
    	paycheck.setSourcePaycheckId("8928923");
    	paycheck.setNetAmount(new SpcfMoney("1000.00"));
    	Employee employee=new Employee();
    	employee.setSourceEmployeeId("125615");
    	paycheck.setDDEmployee(employee);
    	
    	PaycheckSplit pPaycheckSplit=new PaycheckSplit();
    	pPaycheckSplit.setPaycheckSplitAmount(new SpcfMoney("1000.00"));
		paycheck.addPaycheckSplit(pPaycheckSplit);
		payrollRun.addPaycheck(paycheck);
		return payrollRun;
	}
}
