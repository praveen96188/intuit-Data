package com.intuit.ems.payroll.psp.jss.monitoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intuit.ems.payroll.psp.jss.monitoring.model.BatchJob;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

/**
 * Utility class for monitoring
 * 
 * @author kmuthurangam
 *
 */
public class BatchUtil {

	public static final String TOMCAT_STATUS_FILE = "tomcat-status-file";

	public static void writeJson(File file, Object object) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try {
			mapper.writeValue(file, object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Set<BatchJob> readJson(File file) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		Set<BatchJob> jobList = mapper.readValue(file, new TypeReference<Set<BatchJob>>() {
		});
		return jobList;
	}

	public static boolean canRun(String tomcatStatusFile) {
		String fileContents = null;
		try {
			fileContents = FileUtils.readFileToString(new File(tomcatStatusFile));
		} catch (IOException e) {
			return false;
		}
		if (fileContents == null) {
			return false;
		}
		if (StringUtils.containsIgnoreCase(fileContents, "IN_SERVICE")) {
			return true;
		}
		return false;
	}

	public static void print(String format, Object... args) {
		String message = String.format(format, args);
		StringBuffer logFormattedMessageBuffer = new StringBuffer();
		logFormattedMessageBuffer.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS"));
		String threadedMessage = message;
		if (StringUtils.contains(message, "INFO:")) {
			threadedMessage = StringUtils.replace(message, "INFO:",
					"INFO  [" + Thread.currentThread().getName() + "]  ");
		} else if (StringUtils.contains(message, "ERROR:")) {
			threadedMessage = StringUtils.replace(message, "ERROR:",
					"ERROR  [" + Thread.currentThread().getName() + "]  ");
		}
		logFormattedMessageBuffer.append(" ").append(threadedMessage);
		System.out.println(logFormattedMessageBuffer);
	}
}
