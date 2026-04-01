package com.intuit.ems.payroll.psp.jss.monitoring;

import java.util.Date;

/**
 * 
 * @author kmuthurangam
 *
 */
public interface LogSearchService {

	public int execute(String query, Date startTime, Date endTime);
}
