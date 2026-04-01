package com.intuit.sbd.payroll.psp.jss.util;

/**
 * 
 * @author kmuthurangam
 *
 */
public class TimeExpressionConverter {

	/**
	 * Converts Flux time expression to Quartz time expression. Currently it supports only the flux schedules configured in
	 * the database. Any new schedule needs to implemented
	 * 
	 * @param fluxTimeExpression
	 * @return
	 */
	public static String convertFluxToQuartz(String fluxTimeExpression) {
		FluxTimeExpression timeExpression = new FluxTimeExpression(fluxTimeExpression);
		QuartzTimeExpression quartzTimeExpression = new QuartzTimeExpression(timeExpression);
		return quartzTimeExpression.getTimerExpression();
	}
}
