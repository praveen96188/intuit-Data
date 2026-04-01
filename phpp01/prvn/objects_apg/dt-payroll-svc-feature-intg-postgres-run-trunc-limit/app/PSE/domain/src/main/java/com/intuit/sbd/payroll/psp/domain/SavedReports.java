package com.intuit.sbd.payroll.psp.domain;

import javax.persistence.Entity;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class SavedReports extends BaseSavedReports {

	/**
	 * Default constructor.
	 */
	public SavedReports()
	{
		super();
	}

}