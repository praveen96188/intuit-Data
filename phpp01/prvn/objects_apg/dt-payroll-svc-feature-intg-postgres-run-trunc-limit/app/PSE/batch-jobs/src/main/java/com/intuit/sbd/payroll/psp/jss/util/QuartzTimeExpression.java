package com.intuit.sbd.payroll.psp.jss.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * Following is the format of the Quartz CRON expression
 * 
 * Seconds Minutes Hours Day-of-Month Month Day-of-Week Year
 * 
 * <p>
 * Support for specifying both a day-of-week and a day-of-month value is not complete (you must currently use the ‘?’
 * character in one of these fields).
 * </p>
 * 
 * @author kmuthurangam
 *
 */
public class QuartzTimeExpression {

	private static final String DEFAULT_TIME_UNIT_VALUE = "*";
	private static final String TIME_EXPRESSION_DELIMITER = StringUtils.SPACE;

	private String second = DEFAULT_TIME_UNIT_VALUE;
	private String minute = DEFAULT_TIME_UNIT_VALUE;
	private String hour = DEFAULT_TIME_UNIT_VALUE;
	private String dayOfMonth = DEFAULT_TIME_UNIT_VALUE;
	private String month = DEFAULT_TIME_UNIT_VALUE;
	private String dayOfWeek = DEFAULT_TIME_UNIT_VALUE;
	private String year = DEFAULT_TIME_UNIT_VALUE;

	public QuartzTimeExpression(FluxTimeExpression fluxTimeExpression) {
		super();
		convertFluxExpressionToQuartz(fluxTimeExpression);
	}

	public QuartzTimeExpression(String second, String minute, String hour, String dayOfMonth, String month,
			String dayOfWeek, String year) {
		super();
		this.second = second;
		this.minute = minute;
		this.hour = hour;
		this.dayOfMonth = dayOfMonth;
		this.month = month;
		this.dayOfWeek = dayOfWeek;
		this.year = year;
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

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	/**
	 * Currently it supports only the flux schedules configured in the database. Any new schedule needs to implemented
	 * 
	 * @param fluxTimeExpression
	 */
	private void convertFluxExpressionToQuartz(FluxTimeExpression fluxTimeExpression) {
		String fluxTimeUnit = null;
		for (TimeUnit timeUnit : TimeUnit.values()) {
			switch (timeUnit) {
			case SECOND:
				this.second = fluxTimeExpression.getSecond();
				break;

			case MINUTE:
				fluxTimeUnit = fluxTimeExpression.getMinute();
				fluxTimeUnit = fluxTimeUnit.replace("0-59", "0");
				this.minute = fluxTimeUnit;
				break;

			case HOUR:
				this.hour = fluxTimeExpression.getHour();
				break;

			case DAY_OF_MONTH:
				fluxTimeUnit = fluxTimeExpression.getDayOfMonth();
				if (!fluxTimeUnit.equals(DEFAULT_TIME_UNIT_VALUE)) {
					dayOfMonth = SpecialCharacter.NO_SPECIAL_VALUE.value();
				}
				fluxTimeUnit = fluxTimeUnit.replace("$", SpecialCharacter.LAST.value());
				this.dayOfMonth = fluxTimeUnit;
				break;

			case MONTH:
				this.month = fluxTimeExpression.getMonth();
				break;

			case DAY_OF_WEEK:
				fluxTimeUnit = fluxTimeExpression.getDayOfWeek();
				// Both day-of-month and day-of-week are having ALL(*), assign NO_SPECIAL_VALUE(?) to day-of-week
				if (dayOfMonth.equals(DEFAULT_TIME_UNIT_VALUE) && fluxTimeUnit.equals(DEFAULT_TIME_UNIT_VALUE)) {
					fluxTimeUnit = SpecialCharacter.NO_SPECIAL_VALUE.value();
				} else if (!dayOfMonth.equals(DEFAULT_TIME_UNIT_VALUE)) {
					// day-of-month have some value, assign NO_SPECIAL_VALUE(?) to day-of-week
					fluxTimeUnit = SpecialCharacter.NO_SPECIAL_VALUE.value();
				} else if (!fluxTimeUnit.equals(DEFAULT_TIME_UNIT_VALUE)) {
					// day-of-week have some value, assign NO_SPECIAL_VALUE(?) to day-of-month
					this.dayOfMonth = SpecialCharacter.NO_SPECIAL_VALUE.value();
				}
				this.dayOfWeek = fluxTimeUnit;
				break;

			case YEAR:
				this.year = fluxTimeExpression.getYear();
				break;
			}
		}
	}

	public String getTimerExpression() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getSecond());
		buffer.append(TIME_EXPRESSION_DELIMITER);
		buffer.append(getMinute());
		buffer.append(TIME_EXPRESSION_DELIMITER);
		buffer.append(getHour());
		buffer.append(TIME_EXPRESSION_DELIMITER);
		buffer.append(getDayOfMonth());
		buffer.append(TIME_EXPRESSION_DELIMITER);
		buffer.append(getMonth());
		buffer.append(TIME_EXPRESSION_DELIMITER);
		buffer.append(getDayOfWeek());
		buffer.append(TIME_EXPRESSION_DELIMITER);
		buffer.append(getYear());
		return buffer.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("QuartzTimeExpression [getSecond()=");
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
		builder.append(", getYear()=");
		builder.append(getYear());
		builder.append(", getExpression()=");
		builder.append(getTimerExpression());
		builder.append("]");
		return builder.toString();
	}

	public enum TimeUnit {
		SECOND, MINUTE, HOUR, DAY_OF_MONTH, MONTH, DAY_OF_WEEK, YEAR
	}

	public enum SpecialCharacter {

		ALL("*"), NO_SPECIAL_VALUE("?"), RANGE("-"), ADDITONAL_VALUE(","), INCREMENT("/"), LAST("L"), WEEK("W"), HASH(
				"#");

		private final String character;

		private SpecialCharacter(final String character) {
			this.character = character;
		}

		public String value() {
			return this.character;
		}

		@Override
		public String toString() {
			return this.character;
		}
	}

}
