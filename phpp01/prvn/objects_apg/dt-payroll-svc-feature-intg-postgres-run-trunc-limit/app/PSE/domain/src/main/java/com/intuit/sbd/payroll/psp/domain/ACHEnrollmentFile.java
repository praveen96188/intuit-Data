package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

import javax.persistence.Entity;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class ACHEnrollmentFile extends BaseACHEnrollmentFile {

	/**
	 * Default constructor.
	 */
	public ACHEnrollmentFile()
	{
		super();
	}

    public void updateStatus(ACHEnrollmentFileStatus pACHEnrollmentFileStatus) {
        setStatus(pACHEnrollmentFileStatus);
        setStatusEffectiveDate(PSPDate.getPSPTime());
    }

    public static DomainEntitySet<ACHEnrollmentFile> getACHFilesByActionCode(ACHEnrollmentFileType pType) {
        Expression<ACHEnrollmentFile> query =
                new Query<ACHEnrollmentFile>()
                        .Where(ACHEnrollmentFile.Status().in(ACHEnrollmentFileStatus.SentToAgency, ACHEnrollmentFileStatus.Archived)
                                                .And(ACHEnrollmentFile.Type().equalTo(pType)))
                        .OrderBy(ACHEnrollmentFile.CreatedDate());
        return Application.find(ACHEnrollmentFile.class, query);
    }
}