package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class PayrollItem extends BasePayrollItem {
    public static final PayrollItemCode[] COMPENSATION_CODES = new PayrollItemCode[] {
            PayrollItemCode.Bonus,
            PayrollItemCode.Commission,
            PayrollItemCode.Hourly,
            PayrollItemCode.Salary,
            PayrollItemCode.Compensation
    };

    public static final PayrollItemCode[] DEDUCTION_CODES = new PayrollItemCode[] {
            PayrollItemCode.OtherAdditionPreTax,
            PayrollItemCode.OtherAdditionPostTax,
            PayrollItemCode.OtherPreTaxDeduction,
            PayrollItemCode.OtherPostTaxDeduction,
            PayrollItemCode.DirectDeposit,
            PayrollItemCode.Tp401kEmployeeDeferral,
            PayrollItemCode.Tp401kRoth,
            PayrollItemCode.Tp401kLoanPayment
    };

    public static final PayrollItemCode[] CONTRIBUTION_CODES = new PayrollItemCode[] {
            PayrollItemCode.OtherTaxableEmployerContribution,
            PayrollItemCode.OtherNonTaxableEmployerContribution,
            PayrollItemCode.Tp401kSafeHarbor,
            PayrollItemCode.Tp401kProfitSharing
    };

	/**
	 * Default constructor.
	 */
	public PayrollItem()
	{
		super();
	}

    public static PayrollItem findItemByPayrollItemCode(PayrollItemCode pPayrollItemCode) {
        return Application.findById(PayrollItem.class, pPayrollItemCode);
    }

    public boolean isTaxableAddition() {
        return getPayrollItemCode() == PayrollItemCode.OtherAdditionPreTax;
    }

    public boolean isAdditionNoTaxAffect() {
        return getPayrollItemCode() == PayrollItemCode.OtherAdditionPostTax;
    }

    public boolean isDirectDeposit() {
        return getPayrollItemCode() == PayrollItemCode.DirectDeposit;
    }
}