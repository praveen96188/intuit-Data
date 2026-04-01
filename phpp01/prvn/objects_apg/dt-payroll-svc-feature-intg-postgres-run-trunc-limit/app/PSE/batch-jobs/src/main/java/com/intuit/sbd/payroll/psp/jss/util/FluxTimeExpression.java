package com.intuit.sbd.payroll.psp.jss.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * Following is the format of the Flux CRON expression
 * 
 * milliseconds seconds minutes hours days-of-month months days-of-week day-of-year week-of-month week-of-year year
 * 
 * <p>
 * PSP supports Flux scheduling in 2 modes (Relative & Absolute). Below implementation supports both modes
 * <p>
 * 
 * @author kmuthurangam
 *
 */
public class FluxTimeExpression {

	public enum TimeMode {
		RELATIVE, ABSOLUTE
	};

	private static final String DEFAULT_TIME_UNIT_VALUE = "*";
	private static final String RELATIVE_TIME_MODE_IDENTIFIER = "+";
	private static final String TIME_EXPRESSION_DELIMITER = StringUtils.SPACE;
	private static final String TIME_INDEPENDENT_INCREMENT_IDENTIFIER = "*/";

	private static final String RELATIVE_SECOND_EXPR = "s";
	private static final String RELATIVE_MINUTE_EXPR = "m";
	private static final String RELATIVE_HOUR_EXPR = "h";

	private String milliSecond = DEFAULT_TIME_UNIT_VALUE;
	private String second = DEFAULT_TIME_UNIT_VALUE;
	private String minute = DEFAULT_TIME_UNIT_VALUE;
	private String hour = DEFAULT_TIME_UNIT_VALUE;
	private String dayOfMonth = DEFAULT_TIME_UNIT_VALUE;
	private String month = DEFAULT_TIME_UNIT_VALUE;
	private String dayOfWeek = DEFAULT_TIME_UNIT_VALUE;
	private String dayOfYear = DEFAULT_TIME_UNIT_VALUE;
	private String weekOfMonth = DEFAULT_TIME_UNIT_VALUE;
	private String weekOfYear = DEFAULT_TIME_UNIT_VALUE;
	private String year = DEFAULT_TIME_UNIT_VALUE;

	private TimeMode timeMode = TimeMode.ABSOLUTE;

	public FluxTimeExpression(String fluxTimeExpression) {
		parse(fluxTimeExpression);
	}

	public TimeMode getTimeMode() {
		return timeMode;
	}

	public void setTimeMode(TimeMode timeMode) {
		this.timeMode = timeMode;
	}

	public String getMilliSecond() {
		return milliSecond;
	}

	public void setMilliSecond(String milliSecond) {
		this.milliSecond = milliSecond;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getMinute() {
		return minute;
	}

	public void setMinute(String minute) {
		this.minute = minute;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getDayOfYear() {
		return dayOfYear;
	}

	public void setDayOfYear(String dayOfYear) {
		this.dayOfYear = dayOfYear;
	}

	public String getWeekOfMonth() {
		return weekOfMonth;
	}

	public void setWeekOfMonth(String weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
	}

	public String getWeekOfYear() {
		return weekOfYear;
	}

	public void setWeekOfYear(String weekOfYear) {
		this.weekOfYear = weekOfYear;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String parse(String fluxTimeExpression) {
		if (fluxTimeExpression == null) {
			throw new RuntimeException("Flux Time Expression cannot be null");
		}
		if (fluxTimeExpression.startsWith(RELATIVE_TIME_MODE_IDENTIFIER)) {
			setTimeMode(TimeMode.RELATIVE);
		} else {
			setTimeMode(TimeMode.ABSOLUTE);
		}

		switch (getTimeMode()) {
		case ABSOLUTE:
			parseAbsolute(fluxTimeExpression);
			break;
		case RELATIVE:
			parseRelative(fluxTimeExpression);
			break;
		}

		return null;
	}

	public void parseAbsolute(String fluxTimeExpression) {
		String[] fluxTimeExpressions = fluxTimeExpression.split(TIME_EXPRESSION_DELIMITER);
		for (int i = 0; i < fluxTimeExpressions.length; i++) {
			switch (i) {
			case 0:
				milliSecond = fluxTimeExpressions[i];
				break;
			case 1:
				second = fluxTimeExpressions[i];
				break;
			case 2:
				minute = fluxTimeExpressions[i];
				break;
			case 3:
				hour = fluxTimeExpressions[i];
				break;
			case 4:
				dayOfMonth = fluxTimeExpressions[i];
				break;
			case 5:
				month = fluxTimeExpressions[i];
				break;
			case 6:
				dayOfWeek = fluxTimeExpressions[i];
				break;
			case 7:
				dayOfYear = fluxTimeExpressions[i];
				break;
			case 8:
				weekOfMonth = fluxTimeExpressions[i];
				break;
			case 9:
				weekOfYear = fluxTimeExpressions[i];
				break;
			case 10:
				year = fluxTimeExpressions[i];
				break;
			default:
				throw new RuntimeException("Invalid Flux Time Expression");
			}
		}
	}

	public void parseRelative(String fluxTimeExpression) {
		String relativeTime = fluxTimeExpression.replace(RELATIVE_TIME_MODE_IDENTIFIER, StringUtils.EMPTY);
		if (StringUtils.containsIgnoreCase(relativeTime, RELATIVE_SECOND_EXPR)) {
			setSecond(TIME_INDEPENDENT_INCREMENT_IDENTIFIER
					+ removeTimeUnitExpression(relativeTime, RELATIVE_SECOND_EXPR));
		} else if (StringUtils.containsIgnoreCase(relativeTime, RELATIVE_MINUTE_EXPR)) {
			setSecond("0");
			setMinute(TIME_INDEPENDENT_INCREMENT_IDENTIFIER
					+ removeTimeUnitExpression(relativeTime, RELATIVE_MINUTE_EXPR));
		} else if (StringUtils.containsIgnoreCase(relativeTime, RELATIVE_HOUR_EXPR)) {
			setSecond("0");
			setMinute("0");
			setHour(TIME_INDEPENDENT_INCREMENT_IDENTIFIER + removeTimeUnitExpression(relativeTime, RELATIVE_HOUR_EXPR));
		}
	}

	/**
	 * 
	 * Remove Time Units case insensitive
	 * 
	 * @param relativeTime
	 * @param timeUnitExpression
	 * @return
	 */
	private String removeTimeUnitExpression(String relativeTime, String timeUnitExpression) {
		return relativeTime.replaceAll("(?i)" + timeUnitExpression, StringUtils.EMPTY);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FluxTimeExpression [getMilliSecond()=");
		builder.append(getMilliSecond());
		builder.append(", getSecond()=");
		builder.append(getSecond());
		builder.append(", getMinute()=");
		builder.append(getMinute());
		builder.append(", getHour()=");
		builder.append(getHour());
		builder.append(", getDayOfMonth()=");
		builder.append(getDayOfMonth());
		builder.append(", getMonth()=");
		builder.append(getMonth());
		builder.append(", getDayOfWeek()=");
		builder.append(getDayOfWeek());
		builder.append(", getDayOfYear()=");
		builder.append(getDayOfYear());
		builder.append(", getWeekOfMonth()=");
		builder.append(getWeekOfMonth());
		builder.append(", getWeekOfYear()=");
		builder.append(getWeekOfYear());
		builder.append(", getYear()=");
		builder.append(getYear());
		builder.append("]");
		return builder.toString();
	}

}
