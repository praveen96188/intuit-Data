package com.intuit.sbd.payroll.psp.adapters.ews.appcontext;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.intuit.sbd.payroll.psp.appcontext.AppContextTest;
import com.intuit.sbd.payroll.psp.emailsender.processor.EmailResponseProcessor;

//todo: Can we call the testApplicationContext test method from testutils
public class ApplicationContextTest {
	@Test
	public void testApplicationContext() {
		try {
			ApplicationContext applicationContext = AppContextTest.createApplicationContext();
			EmailResponseProcessor emailResponseProcessor = applicationContext
					.getBean(EmailResponseProcessor.class);
			Assert.assertNotNull(emailResponseProcessor);
			System.out.println("EWS Adapter is up...");
		} catch (Exception e) {
			System.out.println("EWS Adapter is not up...");
			e.printStackTrace();
		}
	}
}
