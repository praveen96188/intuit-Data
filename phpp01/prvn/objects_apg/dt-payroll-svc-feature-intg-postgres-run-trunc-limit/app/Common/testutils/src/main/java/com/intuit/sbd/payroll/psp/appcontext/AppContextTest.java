package com.intuit.sbd.payroll.psp.appcontext;

import org.junit.Assert;
import org.springframework.context.ApplicationContext;

import com.intuit.sbd.payroll.psp.entity.publisher.EntityEventPublisherExecutor;
import com.intuit.sbg.payroll.application.context.PayrollApplicationContext;

public class AppContextTest {
	public static ApplicationContext createApplicationContext() {
		ApplicationContext applicationContext = PayrollApplicationContext.getApplicationContext();
		EntityEventPublisherExecutor eventPublisherExecutor = applicationContext
				.getBean(EntityEventPublisherExecutor.class);
		Assert.assertNotNull(eventPublisherExecutor);
		return applicationContext;
	}
}
