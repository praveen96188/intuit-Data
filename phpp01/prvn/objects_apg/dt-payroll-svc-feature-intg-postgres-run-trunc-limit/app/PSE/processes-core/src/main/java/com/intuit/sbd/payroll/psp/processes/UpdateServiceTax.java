package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 28, 2011
 * Time: 10:25:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateServiceTax implements IProcess {

    private Company company;
    private SpcfCalendar serviceStartDate;
    private TaxServiceInfoDTO taxServiceInfoDTO;

    public UpdateServiceTax(Company pCompany, SpcfCalendar pServiceStartDate, TaxServiceInfoDTO pTaxServiceInfoDTO) {
        company = pCompany;
        serviceStartDate = pServiceStartDate;
        taxServiceInfoDTO = pTaxServiceInfoDTO;
    }

    public ProcessResult execute() {
        ProcessResult processResult = new ProcessResult();
        if (serviceStartDate != null) {
            for (CompanyAgency companyAgency : company.getCompanyAgencyCollection()) {
                 if (companyAgency.getIntuitResponsibilityStartDate() != null && companyAgency.getIntuitResponsibilityStartDate().compareTo(taxServiceInfoDTO.getServiceStartDate()) != 0) {
                     companyAgency.setIntuitResponsibilityStartDate(taxServiceInfoDTO.getServiceStartDate());
                       Application.save(companyAgency);
                   }
            }
        }

        if (taxServiceInfoDTO != null) {
            CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) taxService;
            if (taxCompanyServiceInfo == null) {
               taxCompanyServiceInfo = new TaxCompanyServiceInfo();
            }

            taxCompanyServiceInfo.setLastQuarterToFile(taxServiceInfoDTO.getLastQuarterToFile());
            taxCompanyServiceInfo.setFileAnnualReturns(taxServiceInfoDTO.getFileAnnualReturns());
            taxCompanyServiceInfo.setFinalAnnualReturns(taxServiceInfoDTO.isFinalAnnualReturns());
            taxCompanyServiceInfo.setLastPayrollDate(taxServiceInfoDTO.getLastPayrollDate());
            taxCompanyServiceInfo.setClientPacketDeliveryPreferenceCd(taxServiceInfoDTO.getClientPacketDeliveryPreferenceCd());
            taxCompanyServiceInfo.setW2DeliveryPreferenceCd(taxServiceInfoDTO.getW2DeliveryPreferenceCd());
            taxCompanyServiceInfo.setInHouseW2(taxServiceInfoDTO.isInHouseW2());
            taxCompanyServiceInfo.setIncludeOnSSAFile(taxServiceInfoDTO.isIncludeOnSsaFile());

            Application.save(taxCompanyServiceInfo);
        }
        return processResult;
    }
}
