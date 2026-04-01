package com.intuit.sbd.payroll.psp.adapters.qbdt.translators;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.DisburseAdvice.TaxLiability;
import com.intuit.sbd.payroll.psp.common.ofx.request.IDISBURSEADVICE;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.ITAXLIAB;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.DisburseAdvice;
import com.intuit.sbd.payroll.psp.domain.DisburseAdviceTaxLiab;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * User: TimothyD698
 * Date: 11/8/12
 */
public class DisburseAdviceTranslator {

    public static DisburseAdvice createDisburseAdvice(com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.DisburseAdvice disburseAdviceWrapper) {

        DisburseAdvice disburseAdvice = new DisburseAdvice();

        disburseAdvice.setTaxLiabilityAmount(disburseAdviceWrapper.getTaxLiabilityAmount());
        disburseAdvice.setTaxQuarter(disburseAdviceWrapper.getTaxQuarter());
        disburseAdvice.setPaycheckDate(disburseAdviceWrapper.getCheckDate());

        return disburseAdvice;
    }

    public static DisburseAdviceTaxLiab createDisburseAdviceTaxLiab(TaxLiability pTaxLiability) {

        DisburseAdviceTaxLiab taxLiability = new DisburseAdviceTaxLiab();

        taxLiability.setPayrollItemId(pTaxLiability.getPayrollItemId());
        taxLiability.setCurrentAmount(pTaxLiability.getCurrentAmount());
        taxLiability.setQuarterAmount(pTaxLiability.getQuarterAmount());
        taxLiability.setYTDAmount(pTaxLiability.getYTDAmount());
        taxLiability.setCurrentTaxableAmount(pTaxLiability.getCurrentTaxableAmount());
        taxLiability.setQuarterTaxableAmount(pTaxLiability.getQuarterTaxableAmount());
        taxLiability.setYTDTaxableAmount(pTaxLiability.getYTDTaxableAmount());
        taxLiability.setFedTaxDesc(pTaxLiability.getFedTaxDesc());
        taxLiability.setState(pTaxLiability.getState());
        taxLiability.setStateTaxDesc(pTaxLiability.getStateTaxDesc());
        taxLiability.setOtherTaxDesc(pTaxLiability.getOtherTaxDesc());

        return taxLiability;
    }
}
