package com.intuit.sbd.payroll.psp.domain;


import org.hibernate.Hibernate;

import javax.persistence.Entity;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class LedgerOperationJob extends BaseLedgerOperationJob {

	/**
	 * Default constructor.
	 */
	public LedgerOperationJob()
	{
		super();
	}

    public void setOriginalFile(String pOriginalFile) {
        super.setOriginalFile(pOriginalFile);
    }

    public String getOriginalFileString() {
        return getOriginalFile();
    }

    public void setProcessedFile(String pProcessedFile) {
        super.setProcessedFile(pProcessedFile);
    }

    public String getProcessedFileString() {
        return getProcessedFile();
    }

}