package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class CompanyPaycheckBatch extends BaseCompanyPaycheckBatch {

	/**
	 * Default constructor.
	 */
	public CompanyPaycheckBatch()
	{
		super();
	}

    public String findMinMaxPaycheckId(boolean findMin) {
        long returnPaycheckId = findMin ? Long.MAX_VALUE : Long.MIN_VALUE;
        long paycheckId;
        for (CheckPrintPaycheck checkPrintPaycheck : getCheckPrintPaycheckCollection()) {
            if(checkPrintPaycheck.getSourcePaycheckId().equalsIgnoreCase("Test")) {
                return "Test";
            }

            paycheckId = findMin ? Long.MAX_VALUE : Long.MIN_VALUE;
            try {
                paycheckId = Long.parseLong(checkPrintPaycheck.getSourcePaycheckId());
            } catch (NumberFormatException e) {
                // skip paycheck ids that are not numbers
            }
            returnPaycheckId = findMin ? Math.min(returnPaycheckId, paycheckId) : Math.max(returnPaycheckId, paycheckId);

        }
        if(returnPaycheckId == Long.MAX_VALUE  || returnPaycheckId == Long.MIN_VALUE) {
            return null;
        } else {
            return Long.toString(returnPaycheckId);
        }
    }

    public static ScalarProperty<CompanyPaycheckBatch, SpcfCalendar> BaseSentToPrinter() {return new ScalarProperty<CompanyPaycheckBatch, SpcfCalendar>(null, "SentToPrinter");};
    public static ScalarProperty<CompanyPaycheckBatch, CheckPrintBatchStatus> BaseCheckPrintBatchStatusCode() {return new ScalarProperty<CompanyPaycheckBatch, CheckPrintBatchStatus>(null, "CheckPrintBatchStatusCode");};

}