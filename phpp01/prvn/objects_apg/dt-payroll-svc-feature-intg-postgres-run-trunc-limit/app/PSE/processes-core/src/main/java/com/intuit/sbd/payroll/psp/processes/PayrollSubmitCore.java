/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/PayrollSubmitCore.java#18 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Marcela Villani
 */
public class PayrollSubmitCore extends Process implements IProcess {

    private static String HISTORIC_PAYROLL_SUBMIT_THRESHOLD_DEFAULT_DATE = "2011-01-01";
    /*The date below is used just for all the test environments so that tests don't break, we will remove this
        once we fix all the unit and SOA tests.
    */
    private static String HISTORIC_PAYROLL_SUBMIT_THRESHOLD_DATE_FOR_TEST_ONLY = "2007-01-01";
    private PayrollRunDTO payrollRunDTO;
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private PayrollRun payrollRun;
    private Company company;
    private PayrollSubmitDD payrollSubmitDD = null;
    private CheckDDLimits checkDDLimits = null;
    private PayrollSubmitTax payrollSubmitTax = null;
    private List<AddLiabilityAdjustmentsCore> mAddLiabilityAdjustmentsCores = new ArrayList<AddLiabilityAdjustmentsCore>();
    private PayrollSubmit401k payrollSubmit401k = null;
    private PayrollSubmitWorkersComp payrollSubmitWorkersComp = null;
    private SpcfCalendar paycheckSettlementDate;
    private Boolean isBackdatedPayroll = false;
    private boolean bypassTaxService = false;
    private SpcfCalendar payrollRunDate = null;
    private String transmissionId = null;
    boolean mustProcessDD = false;
    boolean mustProcessTax = false;
    boolean mustProcess401k = false;
    boolean mustProcessWorkersComp = false;
    boolean mustProcessViewMyPaycheck = false;
    boolean mustProcessRiskAssessment = false;
    private List<AddOrUpdatePayrollTransactionCore> mAddOrUpdatePayrollTransactionCores = new ArrayList<AddOrUpdatePayrollTransactionCore>();
    private CompanyBankAccount mCompanyBankAccount;

    public PayrollSubmitCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, PayrollRunDTO pPayrollRunDTO, String pTransmissionId) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        payrollRunDTO = pPayrollRunDTO;
        transmissionId = pTransmissionId;
    }

    public PayrollRun getPayrollRun() {
        return payrollRun;
    }

    protected void setBypassTaxService(boolean pBypassTaxService) {
        bypassTaxService = pBypassTaxService;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        payrollRunDate = PSPDate.getPSPTime();

        if (payrollRunDTO == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.PayrollRun,
                    "PayrollSubmitCore",
                    "payrollRunDTO");
            return validationResult;
        }
        // Validate PayrollRun DTO
        ProcessResult validatePayrollRunDTOtResult = payrollRunDTO.validatePayrollRunDTO();
        validationResult.merge(validatePayrollRunDTOtResult);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
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

        // Check if PayrollRun already exists for the company
        String payrollRunSourceId = payrollRunDTO.getPayrollTXBatchId();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunSourceId);
        if (payrollRun != null) {
            validationResult.getMessages().PayrollRunAlreadyExists(EntityName.PayrollRun, payrollRunSourceId,
                    payrollRunSourceId, sourceSystemCd.toString(), sourceCompanyId);
        }

        // Validate Paycheck Date  - verify if it's not too far in the future according to SourcePayrollParameter MaxWarehouseTransactionDays
        SourcePayrollParameter maxWarehouseTransactionDays = SourcePayrollParameter.findSourcePayrollParameter(
                sourceSystemCd, SourcePayrollParameterCode.MaxWarehouseTransactionDays);
        int numberOfDays = Integer.parseInt(maxWarehouseTransactionDays.getParameterValue());
        SpcfCalendar paycheckDate = DateDTO.convertToSpcfCalendar(payrollRunDTO.getTargetPayrollTXDate());

        mustProcessDD = company.isCompanyOnService(ServiceCode.DirectDeposit) && PayrollSubmitHelper.anyDDTransactionsInPayroll(payrollRunDTO);
        mustProcessTax = company.isCompanyOnService(ServiceCode.Tax) && PayrollSubmitHelper.getInstance().anyTaxTransactionsInPayroll(payrollRunDTO)
                && !ServiceSubStatusCode.PendingSetup.equals(company.getService(ServiceCode.Tax).getStatusCd());
        mustProcess401k = PayrollSubmitHelper.shouldProcess401kForPayroll(company, payrollRunDTO);
        mustProcessWorkersComp = PayrollSubmitHelper.shouldProcessWorkersCompForPayroll(company, payrollRunDTO);
        mustProcessRiskAssessment = company.isCompanyOnService(ServiceCode.RiskAssessment);
        boolean payrollRunDTOContainsBankAccounts = payrollRunDTO.getCompanyBankAccounts() != null && payrollRunDTO.getCompanyBankAccounts().size() > 0;

        if (mustProcessDD || mustProcessTax || mustProcess401k || mustProcessWorkersComp || !company.onUsageBilling()) {
            if (CalendarUtils.getDifferenceInDays(paycheckDate, PSPDate.getPSPTime()) > numberOfDays) {
                validationResult.getMessages().PayCheckDateTooFarInTheFuture(EntityName.PayrollRun, payrollRunSourceId, Integer.toString(numberOfDays));
            }
        }

        if ((mustProcessDD || mustProcessTax) && !payrollRunDTOContainsBankAccounts) {
            validationResult.getMessages().CompanyBankAccountNotSpecified(EntityName.PayrollRun, "CompanyBankAccounts");
            return validationResult;
        }

        // Validate Company Service Bank Accounts
        if (payrollRunDTO.getCompanyBankAccounts() != null) {
            for (ServiceBankAccountDTO serviceBankAccountDTO : payrollRunDTO.getCompanyBankAccounts()) {
                String serviceName = serviceBankAccountDTO.getServiceCode().toString().toLowerCase();
                if (!company.isCompanyOnService(serviceBankAccountDTO.getServiceCode())) {
                    validationResult.getMessages().CompanyDoesNotExistOnService(EntityName.Company, sourceCompanyId,
                            sourceSystemCd.toString(), sourceCompanyId, serviceName);
                }
                // Check if company bank account exists and is Active
                mCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(
                        company, serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID());
                if (mCompanyBankAccount == null) {
                    if (company.deactivatedCBAExistsForSourceBankAccountId(serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID())) {
                        validationResult.getMessages().CompanyBankAccountNotActive(EntityName.CompanyBankAccount,
                                serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID(),
                                serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID(),
                                sourceSystemCd.toString(), sourceCompanyId);

                    } else {
                        validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                                sourceSystemCd.toString(), serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID(),
                                sourceSystemCd.toString(), sourceCompanyId);
                    }
                } else if (mCompanyBankAccount.getStatusCd() != BankAccountStatus.Active) {
                    validationResult.getMessages().CompanyBankAccountNotActive(EntityName.CompanyBankAccount,
                            serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID(),
                            serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID(),
                            sourceSystemCd.toString(), sourceCompanyId);
                }
            }
        }

        // Preload employees and related data to avoid multiple round trips to db
        // ** Do not preload employees that do not have paychecks companies can have
        // hundreds of ees and only be paying a few **
        List<String> sourceEmployeeIds = new ArrayList<String>();
        for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
            if (!sourceEmployeeIds.contains(paycheck.getEmployeeId())) {
                sourceEmployeeIds.add(paycheck.getEmployeeId());
            }
        }

        for (CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO : payrollRunDTO.getCompanyAdjustmentSubmissionDTOs()) {
            for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
                if (liabilityAdjustmentDTO.getSourceEmployeeId() != null && !sourceEmployeeIds.contains(liabilityAdjustmentDTO.getSourceEmployeeId())) {
                    sourceEmployeeIds.add(liabilityAdjustmentDTO.getSourceEmployeeId());
                }
            }
        }
        Employee.eagerlyLoadEmployeesAndAssociatedEntities(company, sourceEmployeeIds);

        // Create a Paycheck list to check for duplicate paychecks in the request.  We only create events if we have to process 401k
        PayrollSubmitHelper.getInstance().createPaycheckList(payrollRunDTO, validationResult, company, transmissionId, mustProcess401k);


        //Even if we are not processing for any specific service, we must still return the operation not allowed error if
        // the company overall status disallows it
        if (!mustProcessDD && !mustProcessTax && !mustProcess401k && !mustProcessWorkersComp && !mustProcessViewMyPaycheck) {
            if (!company.isAllowedCapability(SystemCapabilityCode.SubmitPayroll, ServiceCode.Cloud)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SystemCapabilityCode.SubmitPayroll.toString());
            }
        }

        // Get the next valid PaycheckSettlementDate based on the paycheck date
        paycheckSettlementDate = DateDTO.convertToSpcfCalendar(payrollRunDTO.getTargetPayrollTXDate());

        if (company.isPayrollSubmissionBackdated(paycheckSettlementDate)) {
            isBackdatedPayroll = true;
        }

        paycheckSettlementDate = company.getNextValidPaycheckDepositDate(paycheckSettlementDate);

        //validate line items
        if (validationResult.isSuccess()) {
            for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
                validationResult.merge(PayrollSubmitHelper.validatePayrollItemsExistForLineItems(company, paycheck));
                CompanyEvent.updateInvalidPaycheckInformationEvents(company, paycheck.getEmployeeId(), paycheck.getPaycheckId(), transmissionId, validationResult);
                // Check if it is a Historical Payroll

            }
        }

        if (mustProcessDD) {
            // Check if Submitting a payroll is allowed based on the current Service Status
            if (!company.isAllowedCapability(SystemCapabilityCode.SubmitPayroll, ServiceCode.DirectDeposit)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SystemCapabilityCode.SubmitPayroll.toString());
            }

            // Validate DD
            if (validationResult.isSuccess())

            {
                payrollSubmitDD = new PayrollSubmitDD(payrollRunDTO, sourceSystemCd, sourceCompanyId);
                validationResult.merge(payrollSubmitDD.validate());
            }

            if (company.isDDMigrated()) {
                CheckDDLimits.getPayrollsInMemoryforDDMigrated(company).add(payrollRunDTO);
            }

            // Call DD Limits Validation
            if (validationResult.isSuccess() && !payrollRunDTO.getBalanceFilePayroll()) {
                checkDDLimits = new CheckDDLimits(payrollRunDTO, sourceSystemCd, sourceCompanyId);
                validationResult.merge(checkDDLimits.validate());
            }
        }

        // Validate Payments

        // PayrollSubmitTax Validation if Company has non cancelled/terminated tax service
        if (mustProcessTax) {
            if (!company.isAllowedCapability(SystemCapabilityCode.SubmitPayroll, ServiceCode.Tax)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SystemCapabilityCode.SubmitPayroll.toString());
            }

            payrollSubmitTax = new PayrollSubmitTax(sourceSystemCd, sourceCompanyId, payrollRunDTO);
            validationResult.merge(payrollSubmitTax.validate());
        }

        if (mustProcess401k) {
            if (!company.isAllowedCapability(SystemCapabilityCode.SubmitPayroll, ServiceCode.ThirdParty401k)) {
                validationResult.getMessages().CompanyOperationNotAllowed401k(EntityName.Company,
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SystemCapabilityCode.SubmitPayroll.toString());
            }

            if (validationResult.isSuccess()) {
                payrollSubmit401k = new PayrollSubmit401k(sourceSystemCd, sourceCompanyId, payrollRunDTO, payrollRunDate, transmissionId);
                validationResult.merge(payrollSubmit401k.validate());
            }
        }

        if (mustProcessWorkersComp) {
            if (!company.isAllowedCapability(SystemCapabilityCode.SubmitPayroll, ServiceCode.WorkersComp)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SystemCapabilityCode.SubmitPayroll.toString());
            }

            if (validationResult.isSuccess()) {
                payrollSubmitWorkersComp = new PayrollSubmitWorkersComp();
                validationResult.merge(payrollSubmitWorkersComp.validate());
            }
        }

        for (CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO : payrollRunDTO.getCompanyAdjustmentSubmissionDTOs()) {
            AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore =
                    new AddLiabilityAdjustmentsCore(company.getSourceSystemCd(), company.getSourceCompanyId(),
                            null, companyAdjustmentSubmissionDTO, new DateDTO(paycheckDate),
                            new LiabilityAdjustmentOptionsDTO(true, true, false, new DateDTO(paycheckSettlementDate), false, payrollRunDTO.getBalanceFilePayroll()),
                            null, false);
            validationResult.merge(addLiabilityAdjustmentsCore.validate());
            mAddLiabilityAdjustmentsCores.add(addLiabilityAdjustmentsCore);
        }

        // Create Payroll Reject Events for the all the validation errors
        if (!validationResult.isSuccess()) {
            PayrollSubmitHelper.getInstance().createPayrollRejectEvents(company, payrollRunDTO, validationResult);
        }

        return validationResult;
    }

    public ProcessResult<PayrollRun> process() {
        // Create Payroll Run
        ProcessResult<PayrollRun> processResult = PayrollSubmitHelper.getInstance().createPayrollRun(company, payrollRunDTO, paycheckSettlementDate, payrollRunDate);
        if (!processResult.isSuccess()) {
            return processResult;
        }

        payrollRun = processResult.getResult();

        // calculate all for the sales tax at the end of this method
        payrollRun.setCalculateSalesTax(false);

        // Associate with transmission
        if (payrollRunDTO.getTransmissionId() != null) {
            payrollRun = Application.save(payrollRun);
            SourceSystemTransmission transmission = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(payrollRunDTO.getTransmissionId());
            TransmissionPayrollRun transmissionPayrollRun = new TransmissionPayrollRun();
            transmissionPayrollRun.setPayrollRun(payrollRun);
            transmissionPayrollRun.setSourceSystemTransmissionId(transmission.getId().toString());
            transmissionPayrollRun.setPayrollProcess(PayrollProcessCode.SubmitPayroll);
            transmissionPayrollRun = Application.save(transmissionPayrollRun);
            transmission.addTransmissionPayrollRun(transmissionPayrollRun);
            payrollRun.addTransmissionPayrollRun(transmissionPayrollRun);
        }

        // Save the PayrollRun
        payrollRun = Application.save(payrollRun);

        for (AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore : mAddLiabilityAdjustmentsCores) {
            addLiabilityAdjustmentsCore.setPayrollRun(payrollRun);
            processResult.merge(addLiabilityAdjustmentsCore.process());
        }

        for (CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO : payrollRunDTO.getCompanyAdjustmentSubmissionDTOs()) {
            if (companyAdjustmentSubmissionDTO.getQBDTPayrollTransactionDTO() != null) {
                AddOrUpdatePayrollTransactionCore addOrUpdatePayrollTransactionCore =
                        new AddOrUpdatePayrollTransactionCore(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAdjustmentSubmissionDTO.getQBDTPayrollTransactionDTO());
                processResult.merge(addOrUpdatePayrollTransactionCore.validate());
                mAddOrUpdatePayrollTransactionCores.add(addOrUpdatePayrollTransactionCore);
            }
        }

        // Create Payroll Reject Events for the all the validation errors from AddOrUpdatePayrollTransactionCore
        if (!processResult.isSuccess()) {
            PayrollSubmitHelper.getInstance().createPayrollRejectEvents(company, payrollRunDTO, processResult);
            return processResult;
        }

        // Create First Payroll Received Event
        if (!payrollRunDTO.isYTDAdjustment()) {
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FirstPayrollReceived, CompanyEventStatus.Active, null, null, true);
            if (CompanyEvent.hasNoCompanyEventsWithServiceCodeDetails(companyEvents)) {
                //If the transmission has multiple payrolls, we only want to create the event for the first payroll in the transmission
                boolean firstPayrollInTransmission = isFirstPayrollInTransmission();
                if (mustProcessDD && firstPayrollInTransmission && !CompanyEvent.isServiceCodeEventDetailPresent(companyEvents, ServiceCode.DirectDeposit.toString())) {
                    CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.FirstPayrollReceived, ServiceCode.DirectDeposit);
                }
                if (mustProcessTax && firstPayrollInTransmission && !CompanyEvent.isServiceCodeEventDetailPresent(companyEvents, ServiceCode.Tax.toString())) {
                    CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.FirstPayrollReceived, ServiceCode.Tax);
                }
                if (mustProcess401k && firstPayrollInTransmission && !CompanyEvent.isServiceCodeEventDetailPresent(companyEvents, ServiceCode.ThirdParty401k.toString())) {
                    CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.FirstPayrollReceived, ServiceCode.ThirdParty401k);
                }
                if (mustProcessWorkersComp && firstPayrollInTransmission && !CompanyEvent.isServiceCodeEventDetailPresent(companyEvents, ServiceCode.WorkersComp.toString())) {
                    CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.FirstPayrollReceived, ServiceCode.WorkersComp);
                }
                if (mustProcessRiskAssessment && firstPayrollInTransmission && !CompanyEvent.isServiceCodeEventDetailPresent(companyEvents, ServiceCode.RiskAssessment.toString())) {
                    CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.FirstPayrollReceived, ServiceCode.RiskAssessment);
                }
            }
        }

        if (mustProcessDD || mustProcessTax) {
            for (CompanyOffer companyOffer : company.getCompanyOffers()) {
                if (companyOffer.getBeginDate() == null &&
                        companyOffer.getOffer().getBeginEvent().equals(OfferBeginEvent.FirstUseEvent) &&
                        companyOffer.getOffer().getEndEvent().equals(OfferEndEvent.DurationEvent)) {
                    SpcfCalendar today = PSPDate.getPSPTime().toLocal().copy();
                    CalendarUtils.clearTime(today);
                    companyOffer.setBeginDate(today);
                    SpcfCalendar endDate = today.copy();
                    endDate.addDays(companyOffer.getOffer().getDurationDays());
                    companyOffer.setEndDate(endDate);
                    companyOffer.setUsagesRemaining(0);
                    Application.save(companyOffer);
                }
            }
        }

        for (AddOrUpdatePayrollTransactionCore addOrUpdatePayrollTransactionCore : mAddOrUpdatePayrollTransactionCores) {
            processResult.merge(addOrUpdatePayrollTransactionCore.process());
        }

        if (payrollSubmitDD != null && !payrollRun.isHistoricalPayroll() && !isBeyondHistoricPaycheckThreshold(payrollRun)
                && isPaycheckDateNotBeyondMaxPastDays(payrollRun)) {
            // Process DD
            payrollSubmitDD.setPayrollRun(payrollRun);
            processResult.merge(payrollSubmitDD.process());
        }


        if (checkDDLimits != null && !payrollRun.isHistoricalPayroll() && !isBeyondHistoricPaycheckThreshold(payrollRun)
                && isPaycheckDateNotBeyondMaxPastDays(payrollRun)) {
            checkDDLimits.setPayrollRun(payrollRun);
            processResult.merge(checkDDLimits.process());
        }

        payrollRun = Application.save(payrollRun);

        // Create Payroll Received Event
        // Create different events to keep email processing separate
        mustProcessDD = (company.isCompanyOnService(ServiceCode.DirectDeposit) && PayrollSubmitHelper.anyDDTransactionsInPayroll(payrollRunDTO)) ||
                company.hasService(ServiceCode.RiskAssessment);
        // We are not changing the company payroll events depending on the paycheck date in the past
        if (mustProcessDD && !mustProcessTax) {
            CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.PayrollReceived);
        } else if (mustProcessTax) {
            // don't create payroll confirmation events for balance file payrolls
            if (!payrollRunDTO.getBalanceFilePayroll()) {
                CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.AssistedPayrollConfirmation);
            }
            payrollRun.setPayrollRunType((mustProcessDD && !payrollRunDTO.getBalanceFilePayroll()) ? PayrollType.Regular : PayrollType.CloudOnly);
        } else {
            CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.PayrollReceivedCloud);
            payrollRun.setPayrollRunType(PayrollType.CloudOnly);
        }

        // PSP shall check on the paycheck date being beyond n days in the past
        // if and only if the OFX contains DD transactions.
        // Else it could be a valid tax submission and fee transfer
        boolean processNonDDLines = PayrollSubmitHelper.anyDDTransactionsInPayroll(payrollRunDTO) ?
                isPaycheckDateNotBeyondMaxPastDays(payrollRun) : true;
        //if the payroll is balance file payroll override processNonDDLines to allow tax transactions to be created
        if (payrollRunDTO.getBalanceFilePayroll()) {
            processNonDDLines = Boolean.TRUE;
        }
        if (payrollRunDTO.getPayrollType() != null) {
            payrollRun.setPayrollRunType(payrollRunDTO.getPayrollType());
        }

        // Create Backdated Payroll Event
        if (isBackdatedPayroll) {
            CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.BackdatedPayrollReceived);
        }

        // add DTO to cache so subsequent runs of CheckDDLimits on the same unit of work finds them
        CheckDDLimits.getPayrollsInMemory(company).add(payrollRunDTO);

        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(payrollRun.getNaturalKey(), payrollRun.getId());
            DomainEntitySet<Paycheck> paychecks = payrollRun.getPaycheckCollection();
            if (paychecks.size() > 0) {
                for (Paycheck currentPaycheck : paychecks) {
                    Application.getSessionCache().addPrimaryKey(currentPaycheck.getNaturalKey(), currentPaycheck.getId());
                }
            }
        }

        // Add payrollrun to cache
        PayrollRun.getPayrollsInMemory(company).add(payrollRun);

        // Process Payments
        if (!bypassTaxService && payrollSubmitTax != null && !isBeyondHistoricPaycheckThreshold(payrollRun) && processNonDDLines) {
            payrollSubmitTax.setPayrollRun(payrollRun);
            processResult.merge(payrollSubmitTax.process());
        }

        if (payrollSubmit401k != null && !isBeyondHistoricPaycheckThreshold(payrollRun)) {
            processResult.merge(payrollSubmit401k.process());
        }

        if (payrollSubmitWorkersComp != null && !isBeyondHistoricPaycheckThreshold(payrollRun)) {
            payrollSubmitWorkersComp.setPayrollRun(payrollRun);
            processResult.merge(payrollSubmitWorkersComp.process());
        }

        // billing
        CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);
        OfferingCode offeringCode = null;
        if (companyOffering != null) {
            offeringCode = companyOffering.getOffering().getOfferingCode();
        }

        if (offeringCode != null && mCompanyBankAccount != null && payrollRunDTO.chargeFees() && !isBeyondHistoricPaycheckThreshold(payrollRun) && processNonDDLines) {
            SpcfCalendar feeSettlementDate = FinancialTransaction.findNextAvailableSettlementDate(company.getCompanyService(ServiceCode.DirectDeposit), payrollRun.getPaycheckSettlementDate());

            if (!payrollRun.isHistoricalPayroll()) {
                //
                // Create PerTransmission fee transaction (if appropriate)
                //
                if (payrollRunDTO.isChargeTransmissionFee() && (mustProcessDD || mustProcessTax)) {
                    BillingDetail.createBillingDetail(payrollRun, mCompanyBankAccount, OfferingServiceChargeType.PerTransmission, 1, feeSettlementDate, offeringCode);
                }

                //
                // Create EmployeesPaid fee transaction
                //
                if (payrollRunDTO.getEmployeesPaidInTransmission() > 0) {
                    BillingDetail.createBillingDetail(payrollRun, mCompanyBankAccount, OfferingServiceChargeType.EmployeesPaid, payrollRunDTO.getEmployeesPaidInTransmission(), feeSettlementDate, offeringCode);
                }

                //
                // Create MonthlyFee fee transaction (if appropriate)
                //
                if (payrollRunDTO.getPayrollType() != PayrollType.Adjustment) {
                    BillingDetail.createMonthlyFeeForPayrollRunIfMeetsCriteria(payrollRun, mCompanyBankAccount, feeSettlementDate, offeringCode, false);
                }

                // Backdated payroll rules:
                //
                // NextOffloadDate = Today, or next biz day if already offloaded today
                // BackDateThreshold = NextOffloadDate + funding model biz days
                //
                // Is backdated payroll IF:
                //   - PaycheckDate < BackDateThreshold
                //   *AND*
                //   - Total Liability amount > $4.99
                //
                //   Exception:
                //     - If paycheck is for terminated EE then don't include in assessment logic
                //     *UNLESS*
                //     - PaycheckDate < Today (current calendar day), then include in assessment logic

                //
                // Create BackdatedPayroll fee transaction
                //
                if (payrollRunDTO.transmissionHasBackdatedPayrolls()) {
                    DomainEntitySet<BillingDetail> billingDetails = BillingDetail.createBillingDetail(payrollRun, mCompanyBankAccount, OfferingServiceChargeType.BackdatedPayroll, 1, feeSettlementDate, offeringCode);
                    if (billingDetails.isNotEmpty()) {
                        processResult.getMessages().BackdateFeeChargedWarning(EntityName.Company, company.getSourceCompanyId(), company.getSourceSystemCd().toString(), company.getSourceCompanyId());
                    }
                }
            }

            // call the sales tax gateway once for the entire payroll run
            BillingDetail.computeSalesTax(payrollRun);
            if (mCompanyBankAccount != null) {
                for (BillingDetail billingDetail : payrollRun.getBillingDetailCollection()) {
                    FinancialTransaction.createTaxFinancialTransactionForBillingDetail(billingDetail,
                            mCompanyBankAccount,
                            SettlementType.ACH,
                            feeSettlementDate,
                            payrollRun);
                }
            }
        }

        // If there is no impound to offload, change the payroll status to Complete
        if (!payrollRun.hasImpoundDebit() && !payrollRun.hasFeeDebit()) {
            boolean createAtfPayrollRecord = !bypassTaxService && payrollSubmitTax != null;
            payrollRun.setPayrollRunStatus(PayrollStatus.Complete, createAtfPayrollRecord);
        }

        if (processResult.isSuccess()) {
            // todo Cloud: not sure if this is correct
            CompanyService cloudService = CompanyService.findCompanyService(company, ServiceCode.Cloud);
            if (cloudService != null && cloudService.getStatusCd() == ServiceSubStatusCode.PendingFirstPayroll) {
                ServiceSubStatusCode nextServiceSubStatusCd = cloudService.getNextValidServiceStatus(ServiceSubStatusCode.PendingFirstPayroll);
                cloudService.updateCompanyServiceStatus(nextServiceSubStatusCd);
            }
        }

        if (payrollRun != null && payrollRun.updateEETotalsCalculationRequired() && !isBeyondHistoricPaycheckThreshold(payrollRun)) {
            EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
        }
        
        /* If Liability Adjustment is run after payroll run for any quarter, then force Employee Totals re-calculation
        If Liability Adjustment is run for any quarter which doesn't have any payroll run, then no action is taken */
        for (CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO : payrollRunDTO.getCompanyAdjustmentSubmissionDTOs()) {

            QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = companyAdjustmentSubmissionDTO.getQBDTPayrollTransactionDTO();
            if (qbdtPayrollTransactionDTO != null) {

                SpcfCalendar quarterStartDate = CalendarUtils.getFirstDayOfQuarter(qbdtPayrollTransactionDTO.getPeriodEndDate());
                EmpTotalsPayrollRun empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(payrollRun.getCompany(), quarterStartDate, EmpTotalsPayrollStatus.Pending);
                if (empTotalsPayrollRun != null) {
                    continue;
                }

                empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(payrollRun.getCompany(), quarterStartDate, EmpTotalsPayrollStatus.Processed);

                if (empTotalsPayrollRun != null) {
                    empTotalsPayrollRun.updateEmpTotalsPayrollRunStatus(EmpTotalsPayrollStatus.Pending);
                }
            }
        }

        processResult.setResult(payrollRun);
        return processResult;
    }

    private boolean isFirstPayrollInTransmission() {
        if (payrollRun.getTransmissionPayrollRunCollection().size() > 0) {
            TransmissionPayrollRun transmissionPayrollRun = payrollRun.getTransmissionPayrollRunCollection().get(0);
            SourceSystemTransmission sourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(transmissionPayrollRun.getSourceSystemTransmissionId());
            return sourceSystemTransmission.getTransmissionPayrollRunCollection().size() == 1;
        } else {
            return true;
        }

    }

    private boolean isPaycheckDateNotBeyondMaxPastDays(PayrollRun pPayrollRun) {
        String numberOfDaysAllowedInPast = SystemParameter.findStringValue(SystemParameter.Code.MAX_PAST_DAYS_MMT);

        SpcfCalendar paycheckDate = pPayrollRun.getPaycheckDate();
        if (StringUtils.isEmpty(numberOfDaysAllowedInPast) || paycheckDate == null) {
            return false;
        }

        SpcfCalendar maxAllowedDateInPast = PSPDate.getPSPTime().toLocal();
        CalendarUtils.clearTime(maxAllowedDateInPast);
        maxAllowedDateInPast.addDays(-1 * Integer.valueOf(numberOfDaysAllowedInPast));
        return !(paycheckDate.before(maxAllowedDateInPast));

    }

    private boolean isBeyondHistoricPaycheckThreshold(PayrollRun pPayrollRun) {
        //Todo Replace this conditional threshold with one single threshold once unit/SOA tests have been fixed
        String thresholdDateString = Application.isProdEnvironment() ?
                SystemParameter.findStringValue(SystemParameter.Code.HISTORIC_PAYCHECK_PROCESS_THRESHOLD,
                        HISTORIC_PAYROLL_SUBMIT_THRESHOLD_DEFAULT_DATE)
                : HISTORIC_PAYROLL_SUBMIT_THRESHOLD_DATE_FOR_TEST_ONLY;
        SpcfCalendar paycheckDate = pPayrollRun.getPaycheckDate();
        if (StringUtils.isEmpty(thresholdDateString) || paycheckDate == null) {
            return false;
        }
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date thresholdDate;
        try {
            thresholdDate = formatter.parse(thresholdDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return (paycheckDate.before(new SpcfCalendarImpl(thresholdDate.getTime())));
    }
}
