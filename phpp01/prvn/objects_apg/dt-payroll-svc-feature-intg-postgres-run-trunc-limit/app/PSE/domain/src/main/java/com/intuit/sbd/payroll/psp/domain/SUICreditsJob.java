package com.intuit.sbd.payroll.psp.domain;


import org.hibernate.Hibernate;

import javax.persistence.Entity;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class SUICreditsJob extends BaseSUICreditsJob {

	/**
	 * Default constructor.
	 */
	public SUICreditsJob()
	{
		super();
	}

        public void setProcessedFile(String pProcessedFile) {
            super.setProcessedFile(pProcessedFile);
        }

        public String getProcessedFileString() {
            return getProcessedFile();
        }

}