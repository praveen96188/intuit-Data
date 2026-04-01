package com.intuit.sbd.payroll.psp.jss.util;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * 
 * Utility methods for Quartz scheduler.
 * 
 * As the scheduling is based on abstract JSS layer, this explicit Quartz scheduler dependency needs to be removed at later
 * point of time.
 * 
 * @author kmuthurangam
 *
 */
public class QuartzUtils {

	public static Date getNextValidTimeAfter(Date date, String cronExpression) {
		Date nextScheduleDate = null;
		try {
			CronExpression expression = new CronExpression(cronExpression);
			nextScheduleDate = expression.getNextValidTimeAfter(date);
		} catch (ParseException e) {
			throw new RuntimeException("Error while parsing the cron expression " + cronExpression);
		}
		return nextScheduleDate;
	}

	public static long getNextValidTimeAfter(long timeInMilliSeconds, String cronExpression) {
		Date nextScheduleDate = getNextValidTimeAfter(new Date(timeInMilliSeconds), cronExpression);
		return (nextScheduleDate != null) ? nextScheduleDate.getTime() : 0L;
	}
}
