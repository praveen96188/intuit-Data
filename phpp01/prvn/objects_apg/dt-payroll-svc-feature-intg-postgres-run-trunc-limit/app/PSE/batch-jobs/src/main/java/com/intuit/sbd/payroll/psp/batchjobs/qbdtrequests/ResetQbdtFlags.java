package com.intuit.sbd.payroll.psp.batchjobs.qbdtrequests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

public class ResetQbdtFlags {
    private static SpcfLogger logger = Application.getLogger(ResetQbdtFlags.class);

    private PSPRequestContextManager pspRequestContextManager;

    public ResetQbdtFlags() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * PSP-13615
     * Method to reset the flags
     */
    public void resetFlags() {

        SpcfCalendar processStartTime = PSPDate.getPSPTime();
        logger.info("Reset Flags Process Started at " + processStartTime);
        StopWatch stopWatch = new StopWatch().start();

        // Find companies with processing flag set to zero and if found enable the flag
        DomainEntitySet<Company> companies = Company.findProcessingDisabledCompanies();

        int processedCount = enableProcessTransmission(companies);

        // Log Processed Count
        logger.info("Completed flags reset for " + processedCount + "/" + companies.size() + " companies in " + stopWatch.getElapsedTimeString());
        stopWatch.stop();
    }

    /**
     * PSP-13615
     * Enable Process Transmission Flag for the Set of Companies
     *
     * @param pCompanies
     * @return processedCount
     */
    private int enableProcessTransmission(DomainEntitySet<Company> pCompanies) {
        int processedCount = 0;

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RetryUnprocessedQbdtRqBatchJob));
        for (Company company : pCompanies) {
            pspRequestContextManager.setRequestContextCompany(company);
            Company eagerlyLoadedCompany = null;
            try {
                PayrollServices.beginUnitOfWork();
                eagerlyLoadedCompany = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
                if (!SourceSystemCode.QBDT.equals(eagerlyLoadedCompany.getSourceSystemCd()) || !(eagerlyLoadedCompany.isCompanyOnService(ServiceCode.Cloud) || company.isCompanyOnService(ServiceCode.Tax))) {
                    throw new RuntimeException("Transmission processing cannot be changed unless the company is on DIY/DD/Assisted service.");
                }

                CompanyDTO companyDTO = PayrollServices.dtoFactory.create(eagerlyLoadedCompany);

                companyDTO.getQuickBooksInfo().setProcessTransmissions(true);

                ProcessResult<Company> pr = PayrollServices.companyManager.updateQBCompanyInfo(companyDTO.getSourceSystemCd(),
                        companyDTO.getCompanyId(),
                        companyDTO);

                if (pr.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                } else {
                    throw new RuntimeException("Error updating process transmissions flag for " + eagerlyLoadedCompany.getSourceSystemCd() + ":" + eagerlyLoadedCompany.getSourceCompanyId());
                }
                processedCount++;
            } catch (Throwable t) {
                if (eagerlyLoadedCompany != null) {
                    logger.error(t.getMessage() + " for " + eagerlyLoadedCompany.getSourceSystemCd() + ":" + eagerlyLoadedCompany.getSourceCompanyId(), t);
                } else {
                    logger.error(t.getMessage(), t);
                }

            } finally {
                PayrollServices.rollbackUnitOfWork();
                pspRequestContextManager.clearRequestContextCompany();
            }
        }
        return processedCount;
    }


}
