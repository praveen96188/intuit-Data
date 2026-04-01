package com.intuit.sbd.payroll.psp.jss.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for JSS Batch Jobs
 * 
 * @author kmuthurangam
 *
 */
public class BatchUtils {

	public static final String JOB_ID = "jobId";
	public static final String ARGS = "args";

	protected static SpcfLogger logger = Application.getLogger(BatchUtils.class);

	public static String getValue(JsonObject jsonObject, String propertyName) {
		if (jsonObject == null) {
			return null;
		}
		JsonElement propertyValue = jsonObject.get(propertyName);
		return (propertyValue != null) ? propertyValue.getAsString() : null;
	}

	public static String parseJobId(String argument) {
		String[] guidPatterns = { "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}", "[a-z0-9]{32}" };
		String jobId = StringUtils.EMPTY;
		if (argument == null) {
			return jobId;
		}
		String[] arguments = argument.split(StringUtils.SPACE);
		for (int i = 0; i < arguments.length; i++) {
			for (int j = 0; j < guidPatterns.length; j++) {
				if (arguments[i].matches(guidPatterns[j])) {
					jobId = arguments[i];
					logger.info("JobId pattern match found for " + guidPatterns[j] + " and Job Id is " + jobId);
					break;
				}
			}
		}
		return jobId;
	}

	public static String getValidJobId(String jobId) {
		if (jobId == null) {
			return SpcfUniqueId.generateRandomUniqueId().toString();
		}
		String validJobId = BatchUtils.parseJobId(jobId);
		return (validJobId == StringUtils.EMPTY) ? SpcfUniqueId.generateRandomUniqueId().toString() : validJobId;
	}

	public static boolean isJSONValid(String json) {
		try {
			new JsonParser().parse(json);
			return true;
		} catch (JsonSyntaxException jse) {
			return false;
		}
	}
}
