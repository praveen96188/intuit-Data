package com.intuit.sbd.payroll.psp.batchjobs.employeeTotals;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: ihannur
 * Date: 10/29/12
 * Time: 8:51 PM
 */
public class EETotalsUtil {

    public static SpcfMoney getTaxableWages(SpcfUniqueId pEmployeeId, int pQuarter, int pYear, String pLawId, SpcfMoney pTaxableWages, SpcfMoney pTotalWages) {
        //If Taxable wage is zero for current period, return zero as checking the limits is not required.
        if(pTaxableWages.equals(SpcfMoney.ZERO)) {
            return pTaxableWages;
        }

        //Wage limit for the Law and quarter
        WageLimit wageLimit = WageLimit.findWageLimitAmount(pYear, pQuarter, pLawId);
        if (wageLimit == null) {
            return pTaxableWages;
        }

        //If Taxable wages less than wage limit and total wages is greater than or equal to wage limit, overriding taxable wages with total wages and applying below limit logic.
        if (pTaxableWages.isLessThan(wageLimit.getAmount()) && pTotalWages.isGreaterThanEqualTo(wageLimit.getAmount())) {
            pTaxableWages = pTotalWages;
        }

        if (pTaxableWages.isGreaterThan(SpcfMoney.ZERO)) {

            //Previous quarters taxable wages is added, to check against wage limit
            //Previous quarters taxable wages is greater than or equal to wage limit, return zero
            //(Taxable wages + Previous quarters taxable wages) is greater than wage limit - return (wage limit - PreviousQuarterTaxableWages)
            //Other wise return taxable wages
            SpcfMoney previousQuartersTaxableWages = EmployeeLawQtrTotals.getPreviousQuartersTaxableWages(pEmployeeId, pYear, pQuarter, pLawId);

            if (previousQuartersTaxableWages.isGreaterThanEqualTo(wageLimit.getAmount())) {
                pTaxableWages = SpcfMoney.ZERO;
            } else {
                if (pTaxableWages.add(previousQuartersTaxableWages).isGreaterThan(wageLimit.getAmount())) {
                    pTaxableWages = (SpcfMoney) wageLimit.getAmount().subtract(previousQuartersTaxableWages);
                }
            }
        }

        return pTaxableWages;
    }

}
