package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 20, 2009
 * Time: 2:30:17 PM
 */
public class VoidPayrollCore extends Process implements IProcess {

    private static final SpcfLogger logger = Application.getLogger(VoidPayrollCore.class);

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private VoidPayrollDTO voidPayrollDTO;
    private Company company;
    private CompanyAdjustmentSubmission companyVoid;
    private List<Paycheck> payChecksToVoid = new Vector<Paycheck>(10, 10);
    private List<String> paycheckIdsToVoidTax = new ArrayList<String>();
    private List<Paycheck> paychecksToVoid401k = new ArrayList<Paycheck>();
    private VoidPayrollTax voidPayrollTax;
    private VoidPayroll401k voidPayroll401k;
    private CancelOrDeletePayrollWorkersComp cancelOrDeletePayrollWorkersComp;
    private String transmissionId;

    public VoidPayrollCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, VoidPayrollDTO pVoidPayrollDTO, String pTransmissionId) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        voidPayrollDTO = pVoidPayrollDTO;
        transmissionId = pTransmissionId;
    }

    public CompanyAdjustmentSubmission getCompanyVoid() {
        return companyVoid;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        // Check if Company Exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (voidPayrollDTO == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.PayrollRun,
                                                                       "VoidPayrollTax",
                                                                       "voidPayrollDTO");
            return validationResult;
        }

        // validate DTO
        validationResult.merge(voidPayrollDTO.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        PayrollRun voidedPayrollRun = PayrollRun.findPayrollRun(company, voidPayrollDTO.getSourcePayrollRunId());
        PayrollRun.getPayrollsInMemory(company).add(voidedPayrollRun);
        if (voidedPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(
                    EntityName.PayrollRun,
                    voidPayrollDTO.getSourcePayrollRunId(),
                    voidPayrollDTO.getSourcePayrollRunId(),
                    sourceSystemCd.toString(),
                    sourceCompanyId);
            return validationResult;
        }

        if ((voidPayrollDTO.getPaycheckIdList() == null) || voidPayrollDTO.getPaycheckIdList().isEmpty()) {
            ArrayList<String> paycheckList = new ArrayList<String>();

            for (Paycheck paycheck : voidedPayrollRun.getPaycheckCollection()) {
                if (!paycheck.isVoided() && paycheck.getStatus() == PaycheckStatusCode.Active) {
                    paycheckList.add(paycheck.getSourcePaycheckId());
                }
            }
            voidPayrollDTO.setPaycheckIdList(paycheckList);
        }

        boolean hasDDService = company.isCompanyOnService(ServiceCode.DirectDeposit);
        boolean hasTaxService = company.isCompanyOnService(ServiceCode.Tax);
        boolean has401kService = company.isCompanyOnService(ServiceCode.ThirdParty401k);
        boolean hasWorkersCompService = company.isCompanyOnService(ServiceCode.WorkersComp);

        //Validate paychecks to be voided
        if ((voidPayrollDTO.getPaycheckIdList() != null) && !voidPayrollDTO.getPaycheckIdList().isEmpty()) {
            voidedPayrollRun.eagerLoadPaychecks(voidPayrollDTO.getPaycheckIdList(), hasDDService, hasTaxService);

            Paycheck paycheck;
            Set<String> negativePaychecksIds = new HashSet<String>();
            for (String paycheckId : voidPayrollDTO.getPaycheckIdList()) {
                paycheck = Paycheck.findPaycheck(company, paycheckId);

                if (paycheck == null) {
                    validationResult.getMessages().PaycheckDoesNotExist(
                            EntityName.PayrollRun,
                            paycheckId,
                            sourceSystemCd.toString(),
                            sourceCompanyId,
                            paycheckId);
                    return validationResult;
                } else {
                    boolean mustProcessTax = hasTaxService && PayrollSubmitHelper.getInstance().anyTaxTransactionsInPaycheck(paycheck)
                            && !ServiceSubStatusCode.PendingSetup.equals(company.getService(ServiceCode.Tax).getStatusCd());
                    boolean mustProcess401k = has401kService && Application.getCurrentPrincipal().getSystemPrincipal() == SystemPrincipal.QBDTWSAdapter;
                    if (mustProcessTax) {
                        paycheckIdsToVoidTax.add(paycheckId);
                    }
                    if (mustProcess401k) {
                        paychecksToVoid401k.add(paycheck);
                    }

                    payChecksToVoid.add(paycheck);
                }

                //noinspection EmptyCatchBlock
                try {
                    negativePaychecksIds.add(Integer.toString(Integer.parseInt(paycheckId) * -1));
                } catch (NumberFormatException e) {
                }

            }

            for (Paycheck supersededPaycheck : Paycheck.findPaychecks(company, negativePaychecksIds)) {
                logger.error(String.format("Voiding a paycheck (%s) that has a related, superseded paycheck on company %s:%s.  Manual intervention required.",
                                           supersededPaycheck.getSourcePaycheckId(),
                                           company.getSourceSystemCd().toString(),
                                           company.getSourceCompanyId()));
            }
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        boolean mustProcessTax = paycheckIdsToVoidTax != null && paycheckIdsToVoidTax.size() > 0;
        boolean mustProcess401k = paychecksToVoid401k != null && paychecksToVoid401k.size() > 0;
        boolean mustProcessWorkersComp = hasWorkersCompService && payChecksToVoid != null && payChecksToVoid.size() > 0;

        //Service-specific validation
        if (mustProcessTax) {
            if (!company.isAllowedCapability(SystemCapabilityCode.VoidPayroll, ServiceCode.Tax)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SystemCapabilityCode.VoidPayroll.toString());
                return validationResult;
            }

            voidPayrollTax = new VoidPayrollTax(company, voidPayrollDTO);
            validationResult.merge(voidPayrollTax.validate());
        }

        if (mustProcess401k) {
            if (!company.isAllowedCapability(SystemCapabilityCode.VoidPayroll, ServiceCode.ThirdParty401k)) {
                validationResult.getMessages().CompanyOperationNotAllowed401k(EntityName.Company,
                                                                              company.getSourceSystemCd().toString(),
                                                                              company.getSourceCompanyId(), SystemCapabilityCode.VoidPayroll.toString());
                return validationResult;
            }

            voidPayroll401k = new VoidPayroll401k(company, paychecksToVoid401k, transmissionId);
            validationResult.merge(voidPayroll401k.validate());
        }

        if (mustProcessWorkersComp) {
            if (!company.isAllowedCapability(SystemCapabilityCode.VoidPayroll, ServiceCode.WorkersComp)) {
                validationResult.getMessages().CompanyOperationNotAllowed(company.getSourceSystemCd().toString(),
                                                                          company.getSourceCompanyId(),
                                                                          SystemCapabilityCode.VoidPayroll.toString());
                return validationResult;
            }
            cancelOrDeletePayrollWorkersComp = new CancelOrDeletePayrollWorkersComp(payChecksToVoid);
            validationResult.merge(cancelOrDeletePayrollWorkersComp.validate());
        }

        if (!mustProcess401k && !mustProcessTax) {
            if (!company.isAllowedCapability(SystemCapabilityCode.VoidPayroll)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SystemCapabilityCode.VoidPayroll.toString());
                return validationResult;
            }
        }

        return validationResult;
    }

    public ProcessResult<CompanyAdjustmentSubmission> process() {
        SpcfCalendar pspDate = PSPDate.getPSPTime();

        ProcessResult<CompanyAdjustmentSubmission> processResult = new ProcessResult<CompanyAdjustmentSubmission>();

        if (payChecksToVoid != null && payChecksToVoid.size() > 0) {
            companyVoid = new CompanyAdjustmentSubmission();
            companyVoid.setSubmissionDate(pspDate);
            companyVoid.setCompany(company);
            companyVoid = Application.save(companyVoid);

            for (Paycheck paycheck : payChecksToVoid) {
                paycheck.setStatus(PaycheckStatusCode.Inactive);
                paycheck.setCompanyAdjustmentSubmission(companyVoid);
                //resetting source approval time in case of paycheck getting voided
                paycheck.setApprovalDateTimeEnd(PSPDate.getPSPTime()); //added for checksum dd service source approval date
                Application.save(paycheck);
            }

            companyVoid = Application.save(companyVoid);
        }

        if (voidPayroll401k != null) {
            processResult.merge(voidPayroll401k.process());
        }

        if (cancelOrDeletePayrollWorkersComp != null) {
            processResult.merge(cancelOrDeletePayrollWorkersComp.process());
        }

        if (voidPayrollTax != null) {
            voidPayrollTax.setCompanyVoid(companyVoid);
            processResult.merge(voidPayrollTax.process());
        }

        processResult.setResult(companyVoid);

        //Update Payroll Run EECalculationToken
        DomainEntitySet<PayrollRun> payrollRuns = new DomainEntitySet<PayrollRun>();
        for (Paycheck paycheck : payChecksToVoid) {
            payrollRuns.add(paycheck.getPayrollRun());
        }
        for (PayrollRun payrollRun : payrollRuns) {
            if (payrollRun.updateEETotalsCalculationRequired()) {
                EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
            }
        }

        return processResult;
    }
}