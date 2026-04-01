package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class RAFEnrollmentFile extends BaseRAFEnrollmentFile {

	/**
	 * Default constructor.
	 */
	public RAFEnrollmentFile()
	{
		super();
	}

    public static DomainEntitySet<RAFEnrollmentFile> getRAFFilesByStatus(RAFFileStatus... pStatus) {
        Expression<RAFEnrollmentFile> query =
                new Query<RAFEnrollmentFile>()
                        .Where(RAFEnrollmentFile.Status().in(pStatus))
                        .OrderBy(RAFEnrollmentFile.CreatedDate());

        return Application.find(RAFEnrollmentFile.class, query);
    }

    public static void initiateRecreation(RAFEnrollmentFile pOriginalEnrollmentFile) {
        pOriginalEnrollmentFile.setStatus(RAFFileStatus.RecreationInitiated);
        Application.save(pOriginalEnrollmentFile);
    }

    public static RAFEnrollmentFile createFile(RAFActionCode pActionCode) {
        RAFEnrollmentFile file = new RAFEnrollmentFile();
        file.setRAFActionCode(pActionCode);
        file.setStatus(RAFFileStatus.Initiated);
        file.setStatusEffectiveDate(PSPDate.getPSPTime());
        Application.save(file);
        return file;
    }

    public static DomainEntitySet<RAFEnrollmentFile> getRAFFilesByActionCode(RAFActionCode pActionCode) {
        Expression<RAFEnrollmentFile> query =
                new Query<RAFEnrollmentFile>()
                        .Where(RAFEnrollmentFile.Status().equalTo(RAFFileStatus.Completed)
                                .And(RAFEnrollmentFile.RAFActionCode().equalTo(pActionCode)))
                        .OrderBy(RAFEnrollmentFile.CreatedDate());
        return Application.find(RAFEnrollmentFile.class, query);
    }
}