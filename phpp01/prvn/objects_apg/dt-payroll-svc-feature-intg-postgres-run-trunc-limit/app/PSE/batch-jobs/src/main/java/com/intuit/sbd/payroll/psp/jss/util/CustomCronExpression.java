package com.intuit.sbd.payroll.psp.jss.util;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author kmuthurangam
 *
 */
public class CustomCronExpression extends CronExpression {
	/**
	 * 
	 */
	private static final long serialVersionUID = 72658204212665887L;
	
	private static final String DELIMITER = " ";

	/**
	 * Constructs a new <CODE>CronExpression</CODE> based on the specified parameter.
	 *
	 * @param cronExpression
	 *            String representation of the cron expression the new object should represent
	 * @throws java.text.ParseException
	 *             if the string expression cannot be parsed into a valid <CODE>CronExpression</CODE>
	 */

	public CustomCronExpression(String cronExpression) throws ParseException {
		super(cronExpression);
	}

	// here we can override this method and call it in future for the first-time
	// lauch to get previous time
	@Override
	public Date getTimeBefore(Date targetDate) {
		Date nextFireTime = getTimeAfter(targetDate);

		Calendar calendar = Calendar.getInstance(getTimeZone());
		calendar.setTime(nextFireTime);
		calendar.add(Calendar.SECOND, -1);

		int dateUnit = getDateUnitCronExpression();

		switch (dateUnit) {
		case -1:
			break;
		case MINUTE:
			calendar.add(Calendar.MINUTE, -2);
			break;
		case HOUR:
			calendar.add(Calendar.HOUR_OF_DAY, -2);
			break;
		case DAY_OF_MONTH:
		case DAY_OF_WEEK:
			calendar.add(Calendar.DAY_OF_YEAR, -2);
			break;
		case MONTH:
			calendar.add(Calendar.MONTH, -2);
			break;
		default:
			calendar.add(Calendar.YEAR, -2);
			break;
		}

		Date previousDate = getTimeAfter(calendar.getTime());
		Date afterPreviousDate = getTimeAfter(previousDate);
		return findPreviousJobFireDate(afterPreviousDate, targetDate);
	}

	private int getDateUnitCronExpression() {
		String cronExpression = getCronExpression();
		if (StringUtils.isEmpty(cronExpression)) {
			return -1;
		}

		if (StringUtils.containsIgnoreCase(cronExpression, "mon-fri")) {
			return 4;
		}

		String[] expression = cronExpression.split(DELIMITER);
		return findDateUnitToWorkWith(expression);
	}

	private Date findPreviousJobFireDate(Date afterFuturePreviousDate, Date targetDate) {
		Date futureDate = afterFuturePreviousDate;
		Date resultDate = targetDate;

		while (true) {
			if (futureDate.after(targetDate)) {
				return resultDate;
			} else {
				resultDate = futureDate;
				futureDate = getTimeAfter(resultDate);
				// prevent NPE and return current job time
				if (futureDate == null) {
					return resultDate;
				}
			}
		}
	}

	private int findDateUnitToWorkWith(String[] expression) {
		// * - represent the unit of date in cron expression
		// [0]Sec [1]Min [2]Hour [3]DayOfMonth [4]Month [5]DayOfWeek [6]Year
		for (int i = 0, n = expression.length; i < n; i++) {
			if (expression[i].equals("*")) {
				return i;
			}
		}
		return YEAR;
	}

}