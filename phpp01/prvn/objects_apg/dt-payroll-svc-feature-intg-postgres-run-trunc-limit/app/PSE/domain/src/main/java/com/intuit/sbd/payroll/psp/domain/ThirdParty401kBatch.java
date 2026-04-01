package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class ThirdParty401kBatch extends BaseThirdParty401kBatch {

	/**
	 * Default constructor.
	 */
	public ThirdParty401kBatch()
	{
		super();
	}

    public static ThirdParty401kBatch getMostRecentCensusTP401kBatch(ThirdParty401kBatchStatusCode pStatus) {
        return getMostRecentCensusTP401kBatch(new ThirdParty401kBatchStatusCode[] { pStatus });
    }

    public static ThirdParty401kBatch getMostRecentCensusTP401kBatch() {
        return getMostRecentCensusTP401kBatch(
                new ThirdParty401kBatchStatusCode[] { ThirdParty401kBatchStatusCode.Finalized,
                                                      ThirdParty401kBatchStatusCode.Archived});
    }

    public static ThirdParty401kBatch getMostRecentCensusTP401kBatch(ThirdParty401kBatchStatusCode[] pStatuses) {
        return  getMostRecentTP401kBatch(pStatuses, "Census_Batch_Intuit_");
    }

    public static ThirdParty401kBatch getMostRecentPayrollTP401kBatch() {
        return  getMostRecentPayrollTP401kBatch(new ThirdParty401kBatchStatusCode[] { ThirdParty401kBatchStatusCode.Finalized,
                                                      ThirdParty401kBatchStatusCode.Archived});
    }

    public static ThirdParty401kBatch getMostRecentPayrollTP401kBatch(ThirdParty401kBatchStatusCode pStatus) {
        return getMostRecentPayrollTP401kBatch(new ThirdParty401kBatchStatusCode[] { pStatus });
    }

    public static ThirdParty401kBatch getMostRecentPayrollTP401kBatch(ThirdParty401kBatchStatusCode[] pStatuses) {
        return getMostRecentTP401kBatch(pStatuses, "Payroll_Batch_Intuit_");
    }

    public static ThirdParty401kBatch getMostRecentTP401kBatch(ThirdParty401kBatchStatusCode[] pStatuses, String pFileNameFragment) {
        //TODO: this needs to be changed to have specific column for identifying a file type
        Expression<ThirdParty401kBatch> query =
                new Query<ThirdParty401kBatch>()
                        .Where(ThirdParty401kBatch.UploadStatusCd().in(pStatuses)
                                .And(ThirdParty401kBatch.FileName().like("%" + pFileNameFragment + "%")))
                        .OrderBy(ThirdParty401kBatch.BatchId().Descending())
                        .LimitResults(0, 1);

        DomainEntitySet<ThirdParty401kBatch> thirdParty401kBatchs = Application.find(ThirdParty401kBatch.class, query);

        return thirdParty401kBatchs.isEmpty() ? null : thirdParty401kBatchs.get(0);
    }

}