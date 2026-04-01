package com.intuit.sbg.psp.dd.gateway;

import com.intuit.pmo.client.model.PayrollCheckResponse;
import com.intuit.pmo.client.model.RiskCheckRequest;
import com.intuit.sbd.payroll.psp.iam.AuthorizationBuilder;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.dd.limitcheck.DDRestClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyMap;

@PowerMockIgnore({"javax.net.ssl.*", "javax.security.auth.x500.X500Principal", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.intuit.sbg.psp.dd.limitcheck.DDRestClient")
@PrepareForTest({DDRestClient.class, DDGateway.class, AuthorizationBuilder.class, PayrollApplicationBeanFactory.class})
public class DDGatewayTest {

	private HttpServiceClient mockHttpServiceClient;
    @Before
    public void setup() throws Exception {
        AuthorizationBuilder mockAuthorizationBuilder=PowerMockito.mock(AuthorizationBuilder.class);
        PowerMockito.whenNew(AuthorizationBuilder.class).withAnyArguments().thenReturn(mockAuthorizationBuilder);

		mockHttpServiceClient = PowerMockito.mock(HttpServiceClient.class);
		PowerMockito.mockStatic(PayrollApplicationBeanFactory.class);
		PowerMockito.when(PayrollApplicationBeanFactory.getBean(HttpServiceClient.class)).thenReturn(mockHttpServiceClient);
    }

	@Test(expected=Exception.class)
    public void checkLimitParseExceptionTest() throws Exception{
    	DDGateway ddGateway=new DDGateway();
    	RiskCheckRequest riskCheckRequest=new RiskCheckRequest();
    	PowerMockito.mockStatic(DDRestClient.class);
    	String mservicePath="v1/payrollcheck";
    	String mUrl="https://payrollmoneyorchestrator-aws-e2e.api.intuit.com";
    	PowerMockito.when(DDRestClient.getServicePath()).thenReturn(mservicePath);
    	PowerMockito.when(DDRestClient.getPostRetryCount()).thenReturn(5);
		
    	PowerMockito.when(DDRestClient.getuRL()).thenReturn(mUrl);
    	LinkedHashMap<String, Object> headers=new LinkedHashMap<>();
    	headers.put("headerkey1","headervalue1");
		PowerMockito.when(DDRestClient.getHeaders()).thenReturn(headers);
		
		PowerMockito.when(DDRestClient.getProxyUrl()).thenReturn("qy1prdproxy01.pprod.ie.intuit.net");
		PowerMockito.when(DDRestClient.getProxyPort()).thenReturn(80);
		
    	PowerMockito.doNothing().when(DDRestClient.class);
    	DDRestClient.getConfigurations();

		HttpServiceResponse mockHttpServiceResponse=PowerMockito.mock(HttpServiceResponse.class);
		PowerMockito.when(mockHttpServiceClient.post(anyString(), anyString(), anyMap())).thenReturn(mockHttpServiceResponse);
		PowerMockito.when(mockHttpServiceResponse.getBody()).thenReturn("mockResponse");

		ddGateway.checkLimit(riskCheckRequest);
    }
		
	@Test(expected=Exception.class)
    public void checkLimitFailExceptionTest() throws Exception{
    	DDGateway ddGateway=new DDGateway();
    	RiskCheckRequest riskCheckRequest=new RiskCheckRequest();
    	PowerMockito.mockStatic(DDRestClient.class);
    	String mservicePath="v1/payrollcheck";
    	String mUrl="https://payrollmoneyorchestrator-e2e.api.intuit.net/";
    	PowerMockito.when(DDRestClient.getServicePath()).thenReturn(mservicePath);
    	PowerMockito.when(DDRestClient.getuRL()).thenReturn(mUrl);
    	PowerMockito.when(DDRestClient.getPostRetryCount()).thenReturn(5);

		PowerMockito.when(DDRestClient.getProxyUrl()).thenReturn("qy1prdproxy01.pprod.ie.intuit.net");
		PowerMockito.when(DDRestClient.getProxyPort()).thenReturn(80);
    	LinkedHashMap<String, Object> headers=new LinkedHashMap<>();
    	headers.put("headerkey1","headervalue1");
		PowerMockito.when(DDRestClient.getHeaders()).thenReturn(headers);
    	PowerMockito.doNothing().when(DDRestClient.class);
    	DDRestClient.getConfigurations();

		HttpServiceResponse mockHttpServiceResponse=PowerMockito.mock(HttpServiceResponse.class);
		PowerMockito.when(mockHttpServiceClient.post(anyString(), anyString(), anyMap())).thenReturn(mockHttpServiceResponse);
		PowerMockito.when(mockHttpServiceResponse.getBody()).thenReturn("mockResponse");

		ddGateway.checkLimit(riskCheckRequest);
    }
	
	
	@Test
    public void checkLimitTest() throws Exception{
    	DDGateway ddGateway=new DDGateway();
    	RiskCheckRequest riskCheckRequest=new RiskCheckRequest();
    	PowerMockito.mockStatic(DDRestClient.class);
    	String mservicePath="v1/payrollcheck";
    	String mUrl="https://payrollmoneyorchestrator-e2e.api.intuit.net/";
    	PowerMockito.when(DDRestClient.getServicePath()).thenReturn(mservicePath);
    	PowerMockito.when(DDRestClient.getuRL()).thenReturn(mUrl);
    	PowerMockito.when(DDRestClient.getPostRetryCount()).thenReturn(5);

    	LinkedHashMap<String, Object> headers=new LinkedHashMap<>();
    	headers.put("headerkey1","headervalue1");
		PowerMockito.when(DDRestClient.getHeaders()).thenReturn(headers);
    	PowerMockito.doNothing().when(DDRestClient.class);
    	DDRestClient.getConfigurations();
    	
		PowerMockito.when(DDRestClient.getProxyUrl()).thenReturn("qy1prdproxy01.pprod.ie.intuit.net");
		PowerMockito.when(DDRestClient.getProxyPort()).thenReturn(80);

		HttpServiceResponse mockHttpServiceResponse=PowerMockito.mock(HttpServiceResponse.class);
		PowerMockito.when(mockHttpServiceClient.post(anyString(), anyString(), anyMap())).thenReturn(mockHttpServiceResponse);
		PowerMockito.when(mockHttpServiceResponse.getBody()).thenReturn("{\"EmployerId\":\"999079876\"}");

		PayrollCheckResponse compareResponse=new PayrollCheckResponse();
		compareResponse.setEmployerId("999079876");
		compareResponse.setLimitCheck("PASS");
		PayrollCheckResponse payrollCheckResponse=ddGateway.checkLimit(riskCheckRequest);
		Assert.assertEquals(payrollCheckResponse.getEmployerId(),compareResponse.getEmployerId());
    }


	@Test
	public void checkGetSubmissionId() throws Exception{

		DDGateway ddGateway=new DDGateway();
		PowerMockito.mockStatic(DDRestClient.class);
		String mservicePath="v1/submissions";
		String mUrl="https://dps-e2e-app-active.sbg-dps-preprod.a.intuit.com/DirectDepositService";
		PowerMockito.when(DDRestClient.getGetSubmissionsPath()).thenReturn(mservicePath);
		PowerMockito.when(DDRestClient.getDesktopApiUrl()).thenReturn(mUrl);
		PowerMockito.when(DDRestClient.getPostRetryCount()).thenReturn(5);

		LinkedHashMap<String, Object> headers=new LinkedHashMap<>();
		headers.put("headerkey1","headervalue1");
		PowerMockito.when(DDRestClient.getHeaders()).thenReturn(headers);
		PowerMockito.doNothing().when(DDRestClient.class);
		DDRestClient.getConfigurations();

		HttpServiceResponse mockHttpServiceResponse=PowerMockito.mock(HttpServiceResponse.class);
		PowerMockito.when(mockHttpServiceClient.get(anyString(), anyMap())).thenReturn(mockHttpServiceResponse);
		PowerMockito.when(mockHttpServiceResponse.getBody()).thenReturn("{\"Submissions\":[{\"SourceSubmissionId\":\"88888888-4444-4444-4444-cccccccccccc\", \"SubmissionId\":\"11111111-2222-3333-4444-555555555555\"}]}");

		String submissionId=ddGateway.getSubmissionId("999079876", "DirectDepositLimits", "88888888-4444-4444-4444-cccccccccccc");
		Assert.assertEquals("11111111-2222-3333-4444-555555555555", submissionId);
	}


	@Test
    public void checkGetSubmissionIdNoSourceSubmissionId() throws Exception{
		DDGateway ddGateway=new DDGateway();
		PowerMockito.mockStatic(DDRestClient.class);
		String mservicePath="v1/submissions";
		String mUrl="https://dps-e2e-app-active.sbg-dps-preprod.a.intuit.com/DirectDepositService";
		PowerMockito.when(DDRestClient.getGetSubmissionsPath()).thenReturn(mservicePath);
		PowerMockito.when(DDRestClient.getDesktopApiUrl()).thenReturn(mUrl);
		PowerMockito.when(DDRestClient.getPostRetryCount()).thenReturn(5);

		LinkedHashMap<String, Object> headers=new LinkedHashMap<>();
		headers.put("headerkey1","headervalue1");
		PowerMockito.when(DDRestClient.getHeaders()).thenReturn(headers);
		PowerMockito.doNothing().when(DDRestClient.class);
		DDRestClient.getConfigurations();

		HttpServiceResponse mockHttpServiceResponse=PowerMockito.mock(HttpServiceResponse.class);
		PowerMockito.when(mockHttpServiceClient.get(anyString(), anyMap())).thenReturn(mockHttpServiceResponse);
		PowerMockito.when(mockHttpServiceResponse.getBody()).thenReturn("{\"Submissions\":[{\"SubmissionId\":\"11111111-2222-3333-4444-555555555555\"}]}");

		String submissionId=ddGateway.getSubmissionId("999079876", "DirectDepositLimits", "88888888-4444-4444-4444-cccccccccccc");
		Assert.assertEquals(null, submissionId);
	}


	@Test
	public void checkGetSubmissionIdNoExpectedSourceSubmissionId() throws Exception{
		DDGateway ddGateway=new DDGateway();
		PowerMockito.mockStatic(DDRestClient.class);
		String mservicePath="v1/submissions";
		String mUrl="https://dps-e2e-app-active.sbg-dps-preprod.a.intuit.com/DirectDepositService";
		PowerMockito.when(DDRestClient.getGetSubmissionsPath()).thenReturn(mservicePath);
		PowerMockito.when(DDRestClient.getDesktopApiUrl()).thenReturn(mUrl);
		PowerMockito.when(DDRestClient.getPostRetryCount()).thenReturn(5);

		LinkedHashMap<String, Object> headers=new LinkedHashMap<>();
		headers.put("headerkey1","headervalue1");
		PowerMockito.when(DDRestClient.getHeaders()).thenReturn(headers);
		PowerMockito.doNothing().when(DDRestClient.class);
		DDRestClient.getConfigurations();

		HttpServiceResponse mockHttpServiceResponse=PowerMockito.mock(HttpServiceResponse.class);
		PowerMockito.when(mockHttpServiceClient.get(anyString(), anyMap())).thenReturn(mockHttpServiceResponse);
		PowerMockito.when(mockHttpServiceResponse.getBody()).thenReturn("{\"Submissions\":[{\"SourceSubmissionId\":\"88888888-4444-4444-4444-cccccccccccd\", \"SubmissionId\":\"11111111-2222-3333-4444-555555555555\"}]}");

		String submissionId=ddGateway.getSubmissionId("999079876", "DirectDepositLimits", "88888888-4444-4444-4444-cccccccccccc");
		Assert.assertEquals(null, submissionId);
	}

	@Test(expected=Exception.class)
	public void checkGetSubmissionIdException() throws Exception{
		DDGateway ddGateway=new DDGateway();
		PowerMockito.mockStatic(DDRestClient.class);
		String mservicePath="v1/submissions";
		String mUrl="https://dps-e2e-app-active.sbg-dps-preprod.a.intuit.com/DirectDepositService";
		PowerMockito.when(DDRestClient.getGetSubmissionsPath()).thenReturn(mservicePath);
		PowerMockito.when(DDRestClient.getDesktopApiUrl()).thenReturn(mUrl);
		PowerMockito.when(DDRestClient.getPostRetryCount()).thenReturn(5);

		LinkedHashMap<String, Object> headers=new LinkedHashMap<>();
		headers.put("headerkey1","headervalue1");
		PowerMockito.when(DDRestClient.getHeaders()).thenReturn(headers);
		PowerMockito.doNothing().when(DDRestClient.class);
		DDRestClient.getConfigurations();

		HttpServiceResponse mockHttpServiceResponse=PowerMockito.mock(HttpServiceResponse.class);
		PowerMockito.when(mockHttpServiceClient.get(anyString(), anyMap())).thenReturn(mockHttpServiceResponse);
		PowerMockito.when(mockHttpServiceResponse.getBody()).thenReturn("mockResponse");

		String submissionId=ddGateway.getSubmissionId("999079876", "DirectDepositLimits", "88888888-4444-4444-4444-cccccccccccc");
	}
}
