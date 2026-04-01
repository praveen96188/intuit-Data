package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.webservices.wsdto.ACHEnrollmentFileWSDTO;
import org.apache.commons.lang.StringUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ihannur
 * Date: 2/8/13
 * Time: 3:34 PM
 */
@WebService()
public class ACHEnrollmentWS {
    @WebMethod
    public void runACHEnrollmentBatch(@WebParam(name = "quarterStartDate") String pQuarterStartDate) throws Exception {

        if (StringUtils.isEmpty(pQuarterStartDate) || !pQuarterStartDate.matches(BatchUtils.VALIDYYYYMMDD)) {
            throw new RuntimeException("Quarter Start Date for Add file can not be null/empty and has to be in YYYYMMDD format");
        }
        BatchJobManager.runJob(BatchJobType.ACHEnrollmentBatchJob, pQuarterStartDate);
    }

    @WebMethod
    public void runACHDeEnrollmentBatch(@WebParam(name = "quarterEndDate") String pQuarterEndDate) throws Exception {

        if (StringUtils.isEmpty(pQuarterEndDate) || !pQuarterEndDate.matches(BatchUtils.VALIDYYYYMMDD)) {
            throw new RuntimeException("Quarter End Date for Delete file can not be null/empty and has to be in YYYYMMDD format");
        }
        BatchJobManager.runJob(BatchJobType.ACHDeEnrollmentBatchJob, pQuarterEndDate);
    }

    @WebMethod
    public ACHEnrollmentStatus getACHEnrollmentStatus(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                      @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                                      @WebParam(name = "paymentTemplate") String pPaymentTemplateCd) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (StringUtils.isEmpty(pSourceSystemCd)) {
            throw new RuntimeException("pSourceSystemCd can not be null/empty");
        }

        if (StringUtils.isEmpty(pSourceCompanyId)) {
            throw new RuntimeException("pSourceCompanyId can not be null/empty");
        }

        if (StringUtils.isEmpty(pPaymentTemplateCd) || !pPaymentTemplateCd.equals(PaymentTemplate.FL_SUI)) {
            throw new RuntimeException("pPaymentTemplateCd can not be null/empty and presently ACH Enrollment is only for - FL-UCT6-PAYMENT ");
        }

        try {
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId + " Source system code:" + pSourceSystemCd);
            }
            return company.getCurrentACHEnrollmentStatus();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateACHEnrollmentStatus(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                          @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                          @WebParam(name = "paymentTemplate") String pPaymentTemplateCd,
                                          @WebParam(name = "newACHEnrollmentStatus") String pACHEnrollmentStatus) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (StringUtils.isEmpty(pSourceSystemCd)) {
            throw new RuntimeException("pSourceSystemCd can not be null/empty");
        }

        if (StringUtils.isEmpty(pSourceCompanyId)) {
            throw new RuntimeException("pSourceCompanyId can not be null/empty");
        }

        if (StringUtils.isEmpty(pPaymentTemplateCd) || !pPaymentTemplateCd.equals(PaymentTemplate.FL_SUI)) {
            throw new RuntimeException("pPaymentTemplateCd can not be null/empty and presently ACH Enrollment is only for - FL-UCT6-PAYMENT ");
        }

        ACHEnrollmentStatus achEnrollmentStatus;
        if (StringUtils.isEmpty(pACHEnrollmentStatus)) {
            throw new RuntimeException("pACHEnrollmentStatus can not be null/empty");
        } else {
            achEnrollmentStatus = ACHEnrollmentStatus.valueOf(pACHEnrollmentStatus);
        }

        try {
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId + " Source system code:" + pSourceSystemCd);
            }
            ACHEnrollment achEnrollment = company.getCurrentACHEnrollment();
            achEnrollment.updateStatus(achEnrollmentStatus);

            PayrollServices.commitUnitOfWork();

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<ACHEnrollmentFileWSDTO> getACHEnrollmentFiles(@WebParam(name = "paymentTemplate") String pPaymentTemplateCd) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        List<ACHEnrollmentFileWSDTO> files = new ArrayList<ACHEnrollmentFileWSDTO>();

        if (StringUtils.isEmpty(pPaymentTemplateCd) || !pPaymentTemplateCd.equals(PaymentTemplate.FL_SUI)) {
            throw new RuntimeException("pPaymentTemplateCd can not be null/empty and presently ACH Enrollment is only for - FL-UCT6-PAYMENT ");
        }

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<ACHEnrollmentFile> achEnrollmentFiles = Application.find(ACHEnrollmentFile.class).sort(ACHEnrollmentFile.StatusEffectiveDate());
            for (ACHEnrollmentFile achEnrollmentFile : achEnrollmentFiles) {
                StringBuilder fileContents = new StringBuilder();
                ACHEnrollmentFileWSDTO fileWSDTO = new ACHEnrollmentFileWSDTO();
                fileWSDTO.fileName = achEnrollmentFile.getFileName();
                fileWSDTO.status = achEnrollmentFile.getStatus().toString();
                fileWSDTO.type = achEnrollmentFile.getType().toString();

                File file = new File(achEnrollmentFile.getFileName());
                FileInputStream iStreamFile = new FileInputStream(file);
                InputStreamReader iStreamReader = new InputStreamReader(iStreamFile);
                BufferedReader reader = new BufferedReader(iStreamReader);

                try {
                    while (reader.ready()) {
                        fileContents.append(reader.readLine());
                        fileContents.append(System.getProperty("line.separator"));
                    }
                } finally {
                    reader.close();
                }

                fileWSDTO.content = fileContents.toString();
                files.add(fileWSDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return files;
    }

    @WebMethod
    public String updateFilingStatus(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                     @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                     @WebParam(name = "lawId") String pLawId,
                                     @WebParam(name = "FilingStatus") String pFilingStatus) throws Exception {

        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
            PayrollItemStatus filingStatus;

            if (StringUtils.isEmpty(pSourceSystemCd)) {
                throw new RuntimeException("pSourceSystemCd can not be null/empty");
            }

            if (StringUtils.isEmpty(pSourceCompanyId)) {
                throw new RuntimeException("pSourceCompanyId can not be null/empty");
            }

            if (StringUtils.isEmpty(pLawId)) {
                throw new RuntimeException("pLawId can not be null/empty");
            }

            if (StringUtils.isEmpty(pFilingStatus)) {
                throw new RuntimeException("pFilingStatus can not be null/empty");
            } else {
                filingStatus = PayrollItemStatus.valueOf(pFilingStatus);
            }

            com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId + " Source system code:" + pSourceSystemCd);
            }

            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, pLawId);
            if (companyLaw == null) {
                throw new RuntimeException("Company Law is not found for LawId:" + pLawId + " Company:" + company.getSourceSystemCompanyId());
            }

            CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(companyLaw);
            companyLawDTO.setFilingStatus(filingStatus);

            ProcessResult result = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);

            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
                return "Filing status is updated";
            } else {
                return "Failed to update filing status, Details:" + result.toString();
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

}
