package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class DepositFrequencyFile extends BaseDepositFrequencyFile {

	/**
	 * Default constructor.
	 */
	public DepositFrequencyFile()
	{
		super();
	}

    public static DepositFrequencyFile findDepositFrequencyFile(DepositFrequencyFileStatus pStatus, boolean pIsArchived) {

        Expression<DepositFrequencyFile> query =
                new Query<DepositFrequencyFile>()
                        .Where(DepositFrequencyFile.Status().equalTo(pStatus)
                                                   .And(DepositFrequencyFile.IsArchived().equalTo(pIsArchived)));

        DomainEntitySet<DepositFrequencyFile> depositFrequencyFiles = Application.find(DepositFrequencyFile.class, query);

        if (depositFrequencyFiles.size() > 1) {
            throw new RuntimeException("Query for deposit frequency file by status '" + pStatus.toString() + "' did not return 0 or 1 results as expected");
        }

        return depositFrequencyFiles.getFirst();
    }

    public static DomainEntitySet<DepositFrequencyFile> findDepositFrequencyFiles(boolean pIsArchived, DepositFrequencyFileStatus... pStatus) {

        Expression<DepositFrequencyFile> query =
                new Query<DepositFrequencyFile>()
                        .Where(DepositFrequencyFile.Status().in(pStatus)
                                                   .And(DepositFrequencyFile.IsArchived().equalTo(pIsArchived)));

        return Application.find(DepositFrequencyFile.class, query);
    }

}