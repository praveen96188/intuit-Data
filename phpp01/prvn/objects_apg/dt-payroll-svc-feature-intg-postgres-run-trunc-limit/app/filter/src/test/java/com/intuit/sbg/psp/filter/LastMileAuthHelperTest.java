package com.intuit.sbg.psp.filter;

import java.util.Set;

import com.intuit.sbg.psp.filter.service.constants.Constants;
import org.apache.commons.collections4.SetValuedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.intuit.sbg.psp.filter.helper.LastMileAuthHelper;

/**
 * @author rn5
 *
 */
public class LastMileAuthHelperTest {
	private LastMileAuthHelper lastMileAuthHelper;
	
	@Before
	public void setup() {
		lastMileAuthHelper = new LastMileAuthHelper();
	}
	
	@Test
	public void getUrlPattternSet() {
	  String audienceList = "https://psp.sys.intuit.net, https://psp.vmp.intuit.net,https://psp.batchjobs.intuit.net";
	  Set<String> audienceSet = lastMileAuthHelper.getUrlPattternSet(audienceList);
	  Assert.assertEquals(3, audienceSet.size());
	  audienceSet.forEach(audience -> System.out.println(audience));
	}
	
	@Test
	public void isFilterSkipRequired() {
		String input = "/appversion.html,GET:/services/KeynoteWS,GET:/services/EWSAdapter/v1_10";
		LastMileAuthHelper lmah = new LastMileAuthHelper();
		SetValuedMap<String, String> map = lmah.getIgnoreUrlPattternMap(input);
		boolean isFilterSkipRequired = lastMileAuthHelper.isFilterSkipRequired("http://localhost:8080/vmp/appversion.html", "/appversion.html", map.get(Constants.COMMON_HTTP_METHOD));
		Assert.assertTrue(isFilterSkipRequired);
		isFilterSkipRequired = lastMileAuthHelper.isFilterSkipRequired("http://localhost:8080/EWSAdapter/services/EWSAdapter/v1_10", "/services/EWSAdapter/v1_10", map.get("GET"));
		Assert.assertTrue(isFilterSkipRequired);
	}
	
	@Test
	public void isFilterSkipRequiredNoPatternMatched() {
		String input = "/health,GET:/services/KeynoteWS,GET:/services/EWSAdapter/v1_10";
		LastMileAuthHelper lmah = new LastMileAuthHelper();
		SetValuedMap<String, String> map = lmah.getIgnoreUrlPattternMap(input);
		boolean isFilterSkipRequired = lastMileAuthHelper.isFilterSkipRequired("http://pspuat801as:8080/vmp/appversion.html", "/appversion.html", map.get(Constants.COMMON_HTTP_METHOD));
		Assert.assertFalse(isFilterSkipRequired);
	}

	@Test
	public void getIgnoreUrlPattternMapNegativeTest() {
		String input = "/health,:/services/EWSAdapter/v1_10,GET:";
		LastMileAuthHelper lmah = new LastMileAuthHelper();
		SetValuedMap<String, String> map = lmah.getIgnoreUrlPattternMap(input);
		Assert.assertEquals(0, map.get("GET").size());
		boolean isFilterSkipRequired = lastMileAuthHelper.isFilterSkipRequired("http://pspuat801as:8080/EWSAdapter/services/EWSAdapter/v1_10", "/services/EWSAdapter/v1_10", map.get(Constants.COMMON_HTTP_METHOD));
		Assert.assertFalse(isFilterSkipRequired);
		isFilterSkipRequired = lastMileAuthHelper.isFilterSkipRequired("http://pspuat801as:8080/EWSAdapter/", "/", map.get(Constants.COMMON_HTTP_METHOD));
		Assert.assertFalse(isFilterSkipRequired);
	}
}
