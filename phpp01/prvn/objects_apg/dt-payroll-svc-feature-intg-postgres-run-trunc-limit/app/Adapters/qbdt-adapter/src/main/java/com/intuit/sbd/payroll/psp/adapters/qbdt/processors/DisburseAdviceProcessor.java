package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.adapters.qbdt.translators.DisburseAdviceTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.DisburseAdvice;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollRun;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: TimothyD698
 * Date: Nov 2, 2012
 */
public class DisburseAdviceProcessor {
    private Company mCompany;

    public DisburseAdviceProcessor(Company pCompany) {
        mCompany = pCompany;
    }

    public ProcessResult processDisburseAdvice(List<IPAYROLLRUN> payrollRuns) {

        for(IPAYROLLRUN payrollRun : payrollRuns) {

            if(payrollRun.getIDISBURSEADVICE() != null) {
                DisburseAdvice disburseAdviceWrapper = new DisburseAdvice(payrollRun.getIDISBURSEADVICE(), payrollRun.getIDTPAYCHKS());

                // Create the domain entity.
                com.intuit.sbd.payroll.psp.domain.DisburseAdvice disburseAdvice = DisburseAdviceTranslator.createDisburseAdvice(disburseAdviceWrapper);
                disburseAdvice.setCompany(mCompany);
                Application.save(disburseAdvice);

                // For each liability
                Collection<DisburseAdvice.TaxLiability> taxLiabs = disburseAdviceWrapper.getTaxLiabilities();
                for(DisburseAdvice.TaxLiability taxLiab : taxLiabs) {
                    // Create the domain entity.
                    com.intuit.sbd.payroll.psp.domain.DisburseAdviceTaxLiab taxLiability = DisburseAdviceTranslator.createDisburseAdviceTaxLiab(taxLiab);
                    taxLiability.setDisburseAdvice(disburseAdvice);
                    taxLiability.setCompany(mCompany);

                    // See if there is an associated tip liability.
                    DisburseAdvice.TaxLiability tipLiab = taxLiab.getTipsLiability();
                    if(tipLiab != null) {
                        com.intuit.sbd.payroll.psp.domain.DisburseAdviceTaxLiab tipLiability = DisburseAdviceTranslator.createDisburseAdviceTaxLiab(tipLiab);
                        tipLiability.setDisburseAdvice(disburseAdvice);
                        tipLiability.setCompany(mCompany);
                        // Set my tax parent to reference this tip liability.
                        taxLiability.setTipsLiability(tipLiability);
                        Application.save(tipLiability);
                    }
                    Application.save(taxLiability);
                }
            }
        }

        return new ProcessResult();
    }

}
