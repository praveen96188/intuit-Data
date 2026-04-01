package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.adapters.qbdt.CredentialType;
import com.intuit.sbd.payroll.psp.adapters.qbdt.translators.EmployeeTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.translators.PaycheckTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.utils.GuideLineUtils;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollRun;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SystemParameter.Code;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 11, 2010
 * Time: 1:49:07 PM
 */
public class PayrollProcessor {
    private static final String FICA_EE_LAW_ID = "61";
    private static final String FICA_ER_LAW_ID = "62";

    private Company mCompany;
    private AssistedConnectionInformation mConnectionInformation;
    private String mTransmissionId;
    private CredentialType mCredentialType;

    private boolean mCalculateHireAct;
    private SpcfDecimal mTotalHIREActReduction = new SpcfMoney("0.0");
    private Collection<PayItemDTO> mPayItemDTOs = new ArrayList<PayItemDTO>();
    private TransmissionType mTransmissionType;
    private SpcfMoney mPennyCutoff;
    private boolean mOnMinSupportedVersion;
    private boolean mIsAssistedRequest;
    private Set<String> mMigratedEmployees;
    private boolean mCanMigratePaycheck;

    private static SpcfLogger logger = PayrollServices.getLogger(PayrollProcessor.class);

    public PayrollProcessor(Company pCompany, AssistedConnectionInformation pAssistedConnectionInformation, String pTransmissionId, TransmissionType pTransmissionType, OFXAPPVERObject pOFXAPPVERObject, CredentialType pCredentialType, Set<String> pMigratedEmployees) {
        mCompany = pCompany;
        mConnectionInformation = pAssistedConnectionInformation;
        mTransmissionId = pTransmissionId;
        mCalculateHireAct = SystemParameter.findBooleanValue(Code.HIRE_ACT_ENABLED, false);
        mTransmissionType = pTransmissionType;
        mPennyCutoff = new SpcfMoney(SystemParameter.findStringValue(SystemParameter.Code.QBDT_PENNY_CUTOFF, "0.03"));
        mOnMinSupportedVersion = pOFXAPPVERObject.isMinQBVersionSupported();
        mCredentialType = pCredentialType;
        mIsAssistedRequest = mConnectionInformation.isAssistedRequest();
        mMigratedEmployees = pMigratedEmployees;

        //Only allow on balance files.
        //Only allow if not on 401k since we don't want to double count liabilities.  Will need manual workaround if on 401k
        mCanMigratePaycheck = mTransmissionType.equals(TransmissionType.BalanceFile) && !mCompany.isCompanyOnService(ServiceCode.ThirdParty401k);
    }

    public ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> processPayrolls(List<IPAYROLLRUN> pPayrolls, Map<SpcfCalendar, LiabilityAdjustmentSubmission> pNewAdjustmentMap) {
        ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> processResult = new ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>>();
        processResult.setResult(new ArrayList<com.intuit.sbd.payroll.psp.domain.PayrollRun>());

        // PSP-2963
        int maxNumPaychecks = SystemParameter.findIntValue(SystemParameter.Code.MAX_NUM_PAYCHKS_PER_OFX, 5000);
        int numPaychecks = 0;
        for (IPAYROLLRUN payrollrun : pPayrolls) {
            numPaychecks += payrollrun.getIPAYCHK().size();
        }

        // Note that we have the paychecks that will be ignored by PSP-3047
        if (numPaychecks > maxNumPaychecks) {
            processResult.getMessages().RequestTooBig(EntityName.Company, mCompany.getSourceCompanyId(), "paychecks", numPaychecks);
            return processResult;
        }

        List<PayrollRun> newPayrolls = new ArrayList<PayrollRun>();
        List<PayrollRun> updatePayrolls = new ArrayList<PayrollRun>();
        List<PayrollRun> migratePayrolls = new ArrayList<PayrollRun>();

        if (mCredentialType != CredentialType.Pin && (mCompany.isCompanyOnService(ServiceCode.Tax) || (mCompany.isCompanyOnService(ServiceCode.DirectDeposit) && (!mCompany.onUsageBilling())))) {
            processResult.getMessages().OperationDeniedForAuthentication(EntityName.Company, mCompany.getSourceCompanyId());
            return processResult;
        }



        for (IPAYROLLRUN iPayrollRun : pPayrolls) {

            updatePaycheckWithDeviceSID(iPayrollRun);

            PayrollRun payrollRunWrapper = new PayrollRun(iPayrollRun);

            // find paycheck updates that have not been saved (do this first so we can migrate the paychecks in the next step)
            processResult.merge(moveModToAddWhenPaychecksDoNotAlreadyExistOrMigrate(payrollRunWrapper));
            if (!processResult.isSuccess()) {
                return processResult;
            }

            // find "new" paychecks that have already been saved
            processResult.merge(moveAddToModOrMigrateWhenPaycheckAlreadyExists(payrollRunWrapper));
            if (!processResult.isSuccess()) {
                return processResult;
            }

            // if an employee on a paycheck does not exist add an empty employee with the source id
            processResult.merge(addEmployeesThatDoNotExist(payrollRunWrapper));
            if (!processResult.isSuccess()) {
                return processResult;
            }

            if(payrollRunWrapper.hasPaycheckMigrates()) {
                migratePayrolls.add(payrollRunWrapper);
            }

            if(payrollRunWrapper.hasNewPaychecks()) {
                newPayrolls.add(payrollRunWrapper);
            }

            if (payrollRunWrapper.hasPaycheckUpdates()) {
                updatePayrolls.add(payrollRunWrapper);
            }


        }

        //migrate
        ProcessResult migratePayrollsProcessResult =
                migratePayrolls(migratePayrolls);
        processResult.merge(migratePayrollsProcessResult);
        if (!processResult.isSuccess()) {
            return processResult;
        }

        // update QBDT info
        for (PayrollRun updatePayroll : updatePayrolls) {
            processResult.merge(updateQBDTInfo(updatePayroll));
            if(!processResult.isSuccess()) {
                return processResult;
            }
        }

        // update payrollRun for enhanced guideline company
        if (updatePayrolls.size() > 0 && GuideLineUtils.isUpdatePayrollAllowed(mCompany)) {
            for (PayrollRun updatePayroll : updatePayrolls) {
                processResult.merge(updatePayrollRun(updatePayroll));
                if (!processResult.isSuccess()) {
                    return processResult;
                }
            }
        }

        // void paychecks that have not already been voided
        // process paycheck mods that are voids
        for (PayrollRun updatePayroll : updatePayrolls) {
            ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> voidPayrollsProcessResult;
            // if there are no new payrolls apply adjustments to the void dates
            if (newPayrolls.size() > 0) {
                voidPayrollsProcessResult = voidPaychecks(updatePayroll, null);
            } else {
                voidPayrollsProcessResult = voidPaychecks(updatePayroll, pNewAdjustmentMap);
            }
            if (!voidPayrollsProcessResult.isSuccess()) {
                processResult.merge(voidPayrollsProcessResult);
                return processResult;
            }
            processResult.getResult().addAll(voidPayrollsProcessResult.getResult());
        }

        ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> newPayrollsProcessResult =
                processNewPayrolls(newPayrolls, pNewAdjustmentMap);
        processResult.merge(newPayrollsProcessResult);
        if (!newPayrollsProcessResult.isSuccess()) {
            return processResult;
        }
        processResult.getResult().addAll(newPayrollsProcessResult.getResult());

        return processResult;
    }

    private void updatePaycheckWithDeviceSID(IPAYROLLRUN iPayrollRun){
       // ProcessResult processResult = new ProcessResult();

    	String sessionId=iPayrollRun.getISESSIONID();

        List<IPAYCHK> paychks=iPayrollRun.getIPAYCHK();
        List<IPAYCHK> paychksmod=iPayrollRun.getIPAYCHKMOD();


        for (Iterator<IPAYCHK> iterator = paychks.iterator(); iterator.hasNext();) {
            IPAYCHK ipaychk = iterator.next();
            ipaychk.setISESSIONID(sessionId);
        }

        for (Iterator<IPAYCHK> iterator = paychksmod.iterator(); iterator.hasNext();) {
            IPAYCHK ipaychkmod = iterator.next();
            ipaychkmod.setISESSIONID(sessionId);
        }

        for (Iterator<IPAYCHK> iterator = paychks.iterator(); iterator.hasNext();) {
            IPAYCHK ipaychk = iterator.next();
            }
        }


    private ProcessResult moveAddToModOrMigrateWhenPaycheckAlreadyExists(PayrollRun pPayrollRunWrapper) {
        ProcessResult processResult = new ProcessResult();

        Set<String> paycheckIds = pPayrollRunWrapper.getNewPaycheckIds();
        if(paycheckIds.size() > 0) {
            DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Paycheck> paychecks =
                    com.intuit.sbd.payroll.psp.domain.Paycheck.findPaychecks(mCompany, paycheckIds);

            if(paycheckIds.size() != paychecks.size()) {
                // check for duplicates with different source ids
                // list id -> new source id
                Map<String, String> paycheckListIdMap = pPayrollRunWrapper.getNewPaycheckListIdMap();
                if(paycheckListIdMap.size() > 0) {
                    paychecks.addAll(com.intuit.sbd.payroll.psp.domain.Paycheck.findPaychecksByQBListIds(mCompany, paycheckListIdMap.keySet()));

                    for (com.intuit.sbd.payroll.psp.domain.Paycheck paycheck : paychecks) {
                        if(paycheck.getQbdtPaycheckInfo().getListId() != null && paycheckListIdMap.get(paycheck.getQbdtPaycheckInfo().getListId()) != null) {
                            paycheck.setOriginalSourceId(paycheck.getSourcePaycheckId());
                            paycheck.setSourcePaycheckId(paycheckListIdMap.get(paycheck.getQbdtPaycheckInfo().getListId()));
                            paycheck.cache();
                        }
                    }
                }
            }

            List<String> paycheckIdsToMove = new ArrayList<String>();
            List<String> paycheckIdsToMigrate = new ArrayList<String>();
            for (com.intuit.sbd.payroll.psp.domain.Paycheck paycheck : paychecks) {
                String sourcePaycheckId = paycheck.getSourcePaycheckId();
                Paycheck paycheckWrapper = pPayrollRunWrapper.findNewPaycheckById(sourcePaycheckId);
                if (mCanMigratePaycheck && (mMigratedEmployees.contains(paycheckWrapper.getSourceEmployeeId()) || paycheck.isFromDDService())) {
                    paycheckIdsToMigrate.add(sourcePaycheckId);
                } else if(paycheckWrapper.equals(paycheck, pPayrollRunWrapper.getPaycheckDate(), mIsAssistedRequest)) {
                    paycheckIdsToMove.add(sourcePaycheckId);
                } else {
                    processResult.getMessages().GenericError(EntityName.Paycheck, sourcePaycheckId, "New paycheck with id '" + sourcePaycheckId + "' does not match with the same id that has already been saved");
                    return processResult;
                }
            }
            pPayrollRunWrapper.markPaychecksAsMigrate(paycheckIdsToMigrate);
            pPayrollRunWrapper.markPaychecksAsUpdates(paycheckIdsToMove);
        }
        return processResult;
    }

    private ProcessResult moveModToAddWhenPaychecksDoNotAlreadyExistOrMigrate(PayrollRun pPayrollRunWrapper) {
        ProcessResult processResult = new ProcessResult();

        Set<String> paycheckIds = new HashSet<String>(pPayrollRunWrapper.getPaycheckUpdateIds());
        if(paycheckIds.size() > 0) {

            // treat all paychecks as new in a balf
            if(!mConnectionInformation.isBalanceFile()) {
                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Paycheck> paychecks =
                        com.intuit.sbd.payroll.psp.domain.Paycheck.findPaychecks(mCompany, paycheckIds);

                for (com.intuit.sbd.payroll.psp.domain.Paycheck paycheck : paychecks) {
                    paycheckIds.remove(paycheck.getSourcePaycheckId());
                }
            }

            pPayrollRunWrapper.markPaychecksAsNew(paycheckIds);
        }
        return processResult;
    }

    private ProcessResult addEmployeesThatDoNotExist(PayrollRun pPayrollRunWrapper) {
        Map<String, Paycheck> employeePaycheckMap = new HashMap<String, Paycheck>();
        for (Paycheck paycheck : pPayrollRunWrapper.getPaycheckUpdates()) {
            employeePaycheckMap.put(paycheck.getSourceEmployeeId(), paycheck);
        }

        for (Paycheck paycheck : pPayrollRunWrapper.getNewPaychecks()) {
            employeePaycheckMap.put(paycheck.getSourceEmployeeId(), paycheck);
        }

        if(employeePaycheckMap.keySet().size() > 0) {
            Employee.eagerlyLoadEmployeesAndAssociatedEntities(mCompany, new ArrayList<String>(employeePaycheckMap.keySet()));
        }
        ProcessResult processResult = new ProcessResult();
        for (Paycheck paycheck : employeePaycheckMap.values()) {
            processResult.merge(addEmployee(paycheck));
            if(!processResult.isSuccess()) {
                return processResult;
            }
        }
        return processResult;
    }

    private ProcessResult addEmployee(Paycheck pPaycheck) {
        if (pPaycheck != null && pPaycheck.getSourceEmployeeId() != null) {
            Employee employee = Employee.findEmployee(mCompany, pPaycheck.getSourceEmployeeId());

            if(employee != null && !employee.canBeRecoveredByQB() && mIsAssistedRequest) {
                throw new RuntimeException("DIY Employee found in payroll for Assisted Company. See Paycheck: " + pPaycheck.getSourceId());
            }
            // add employees that do not exist
            if(employee == null) {
                return PayrollServices.employeeManager.addEmployee(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), EmployeeTranslator.buildEmployeeDTOFromPaycheck(null, pPaycheck, mCompany, mIsAssistedRequest));
            }
            /*// update diy employees: we only get employee info from paychecks
            //fixing the code so the name is set by the EMPMOD instead of the PAYCHK using FeatureFlags   (JIRA: PD-218035)
            else if(!employee.canBeRecoveredByQB() && !pPaycheck.isModification()) {
                logger.info("Feature flag for ENABLE_DIY_EMPMOD is: " + enableDiyEmpmod + " for PSID: " + mCompany.getSourceCompanyId() + " and employee-id: " + pPaycheck.getSourceEmployeeId());
                EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
                return PayrollServices.employeeManager.updateEmployee(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), EmployeeTranslator.buildEmployeeDTOFromPaycheck(employeeDTO, pPaycheck, mCompany, mIsAssistedRequest));
            }*/
        }

        return new ProcessResult();
    }

    private ProcessResult migratePayrolls(List<PayrollRun> pPayrolls) {
        ProcessResult processResult = new ProcessResult();

        if (mCredentialType != CredentialType.Pin) {
            return processResult;
        }

        Set<String> paycheckIdsToMigrate = new HashSet<String>();
        for (PayrollRun payrollRun : pPayrolls) {
            for (Paycheck migratePaycheck : payrollRun.getMigratePaychecks()) {
                paycheckIdsToMigrate.add(migratePaycheck.getSourceId());
            }
        }

        Set<com.intuit.sbd.payroll.psp.domain.PayrollRun> priorPayrollRuns = new HashSet<com.intuit.sbd.payroll.psp.domain.PayrollRun>();
        for (com.intuit.sbd.payroll.psp.domain.Paycheck priorPaycheck : com.intuit.sbd.payroll.psp.domain.Paycheck.findPaychecks(mCompany, paycheckIdsToMigrate)) {
            if (! priorPayrollRuns.contains(priorPaycheck.getPayrollRun())) {
                priorPayrollRuns.add(priorPaycheck.getPayrollRun());
            }
        }

        for (com.intuit.sbd.payroll.psp.domain.PayrollRun priorPayrollRun : priorPayrollRuns) {
            ProcessResult changeIdResult = PayrollServices.payrollManager.changePaycheckSourceIds(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), priorPayrollRun.getSourcePayRunId());
            if (!changeIdResult.isSuccess()) {
                processResult.merge(changeIdResult);
                return processResult;
            }
        }

        return processResult;
    }

    private ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> processNewPayrolls(List<PayrollRun> pPayrolls, Map<SpcfCalendar, LiabilityAdjustmentSubmission> pAdjustmentMap) {
        ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> processResult = new ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>>();
        processResult.setResult(new ArrayList<com.intuit.sbd.payroll.psp.domain.PayrollRun>());

        CompanyBankAccount coBankAcct = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
        List<ServiceBankAccountDTO> serviceBankAccountList = new LinkedList<ServiceBankAccountDTO>();

        if (coBankAcct == null) {
            //if (mCompany.isCompanyOnService(ServiceCode.Tax) || (mCompany.isCompanyOnService(ServiceCode.DirectDeposit) && !mCompany.onUsageBilling())) {
            if (mCompany.isCompanyOnService(ServiceCode.Tax)) {
                processResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount, "null", mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId());
                return processResult;
            } else if (mCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
                boolean hasDD = false;
                A: for (PayrollRun payrollWrapper : pPayrolls) {
                    B: for (Paycheck paycheckWrapper : payrollWrapper.getNewPaychecks()) {
                        if (!paycheckWrapper.getDirectDeposits().isEmpty()) {
                            hasDD = true;
                            break A;
                        }
                    }
                }
                if (hasDD) {
                    processResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount, "null", mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId());
                    return processResult;
                }
            }
        } else {
            CompanyBankAccountDTO coBankAcctDTO = PayrollServices.dtoFactory.create(coBankAcct);

            if (mCompany.isCompanyOnService(ServiceCode.Tax)) {
                ServiceBankAccountDTO taxServiceBankAccountDTO = new ServiceBankAccountDTO();
                taxServiceBankAccountDTO.setCompanyBankAccount(coBankAcctDTO);
                taxServiceBankAccountDTO.setServiceCode(ServiceCode.Tax);
                serviceBankAccountList.add(taxServiceBankAccountDTO);
            }

            if (mCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
                ServiceBankAccountDTO ddServiceBankAccountDTO = new ServiceBankAccountDTO();
                ddServiceBankAccountDTO.setCompanyBankAccount(coBankAcctDTO);
                ddServiceBankAccountDTO.setServiceCode(ServiceCode.DirectDeposit);
                serviceBankAccountList.add(ddServiceBankAccountDTO);
            }
        }

        ProcessResult<List<PayrollRunDTO>> payrollRunDTOsResult = buildPayrollRunDtosForNewPayrolls(pPayrolls, pAdjustmentMap, serviceBankAccountList);
        if (!payrollRunDTOsResult.isSuccess()) {
            processResult.merge(payrollRunDTOsResult);
            return processResult;
        }

        // create a map of the payrolls and liability adjustments, sort them by date (with tree map), and process them in date order (adjustments then payrolls)
        TreeMap<DateDTO, PayrollSubmissionHolder> payrollsMap = new TreeMap<DateDTO, PayrollSubmissionHolder>();
        for (PayrollRunDTO payrollRunDTO : payrollRunDTOsResult.getResult()) {
            PayrollSubmissionHolder payrollSubmissionHolder = payrollsMap.get(payrollRunDTO.getTargetPayrollTXDate());
            if (payrollSubmissionHolder == null) {
                payrollSubmissionHolder = new PayrollSubmissionHolder();
                payrollsMap.put(payrollRunDTO.getTargetPayrollTXDate(), payrollSubmissionHolder);
            }
            payrollSubmissionHolder.payrollRunDTOs.add(payrollRunDTO);
        }

        for (SpcfCalendar adjustmentDate : pAdjustmentMap.keySet()) {
            LiabilityAdjustmentSubmission liabilityAdjustmentSubmission = pAdjustmentMap.get(adjustmentDate);

            DateDTO dateDTO = new DateDTO(adjustmentDate);
            PayrollSubmissionHolder payrollSubmissionHolder = payrollsMap.get(dateDTO);
            if (payrollSubmissionHolder == null) {
                payrollSubmissionHolder = new PayrollSubmissionHolder();
                payrollsMap.put(dateDTO, payrollSubmissionHolder);
            }
            payrollSubmissionHolder.liabilityAdjustmentSubmission = liabilityAdjustmentSubmission;
        }

        for (DateDTO dateDTO : payrollsMap.keySet()) {
            PayrollSubmissionHolder payrollSubmissionHolder = payrollsMap.get(dateDTO);

            // void adjustments first
            LiabilityAdjustmentSubmission liabilityAdjustmentSubmission = payrollSubmissionHolder.liabilityAdjustmentSubmission;
            if (liabilityAdjustmentSubmission != null) {
                findAndUpdateHireActAdjustments(liabilityAdjustmentSubmission);
                String payrollRunId = liabilityAdjustmentSubmission.getPayrollRunId();
                com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = null;
                for (PayrollTransactionProcessor.AdjustmentHolder adjustmentHolder : liabilityAdjustmentSubmission.getAdjustmentHolders()) {
                    ProcessResult<CompanyAdjustmentSubmission> adjustmentSubmitProcessResult =
                            PayrollServices.payrollManager.addLiabilityAdjustments(mCompany.getSourceSystemCd(),
                                                                                   mCompany.getSourceCompanyId(),
                                                                                   payrollRunId,
                                                                                   adjustmentHolder.getCompanyAdjustmentSubmissionDTO(),
                                                                                   dateDTO,
                                                                                   new LiabilityAdjustmentOptionsDTO(true,
                                                                                                                     true,
                                                                                                                     true,
                                                                                                                     null, false, mTransmissionType.equals(TransmissionType.BalanceFile)),
                                                                                   mTransmissionId);

                    if (adjustmentHolder.getQBDTPayrollTransactionDTO() != null) {
                        adjustmentSubmitProcessResult.merge(PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(mCompany.getSourceSystemCd(),
                                                                                                                             mCompany.getSourceCompanyId(),
                                                                                                                             adjustmentHolder.getQBDTPayrollTransactionDTO()));
                    }

                    if (!adjustmentSubmitProcessResult.isSuccess()) {
                        processResult.merge(adjustmentSubmitProcessResult);
                        return processResult;
                    } else {
                        CompanyAdjustmentSubmission companyAdjustmentSubmission = adjustmentSubmitProcessResult.getResult();
                        if (payrollRun == null && companyAdjustmentSubmission != null &&
                                companyAdjustmentSubmission.getLiabilityAdjustmentCollection().size() > 0) {
                            payrollRun = companyAdjustmentSubmission.getLiabilityAdjustmentCollection().get(0).getPayrollRun();
                            payrollRunId = payrollRun.getSourcePayRunId();
                        }
                    }
                }

                if (payrollRun != null) {
                    processResult.getResult().add(payrollRun);
                }
            }

            // then payrolls
            for (PayrollRunDTO payrollRunDTO : payrollSubmissionHolder.payrollRunDTOs) {
                mConnectionInformation.addNumberOfNewPaychecks(payrollRunDTO.getPaychecks().size());

                // map existing employee bank account ids for legacy diy dd
                for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                    if (paycheckDTO.getDdTransactions() != null &&
                            paycheckDTO.getDdTransactions().size() > 0 &&
                            Paycheck.NOT_ALL_DIGITS_PATTERN.matcher(paycheckDTO.getEmployeeId()).matches()) {
                        Employee employee = Employee.findEmployee(mCompany, paycheckDTO.getEmployeeId());
                        if (employee != null) {
                            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts =
                                    employee.getEmployeeBankAccountCollection().find(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
                            for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                                BankAccountDTO bankAccountDTO = ddTransactionDTO.getEmployeeBankAccount().getBankAccount();
                                DomainEntitySet<EmployeeBankAccount> foundEmployeeBankAccount = null;
                                Criterion<EmployeeBankAccount> criterion = null;
                                if(bankAccountDTO.getAccountNumber() == null){
                                    criterion = EmployeeBankAccount.BankAccount().AccountNumberEnc().isNull();
                                }else{
                                    List<String> bankAccountEncList = EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName,bankAccountDTO.getAccountNumber());
                                    criterion = EmployeeBankAccount.BankAccount().AccountNumberEnc().in(bankAccountEncList);
                                }
                                criterion = criterion.And(EmployeeBankAccount.BankAccount().RoutingNumber().equalTo(bankAccountDTO.getRoutingNumber()))
                                        .And(EmployeeBankAccount.BankAccount().AccountTypeCd().equalTo(bankAccountDTO.getAccountType()));
                                foundEmployeeBankAccount = employeeBankAccounts.find(criterion);
                                if (foundEmployeeBankAccount.size() > 0) {
                                    ddTransactionDTO.getEmployeeBankAccount().setEmployeeBankAccountId(foundEmployeeBankAccount.get(0).getSourceBankAccountId());
                                }
                            }
                        }
                    }
                }


                ProcessResult<com.intuit.sbd.payroll.psp.domain.PayrollRun> payrollSubmitProcessResult =
                        PayrollServices.payrollManager.submitPayroll(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), payrollRunDTO);
                processResult.merge(payrollSubmitProcessResult);
                if (!payrollSubmitProcessResult.isSuccess()) {
                    return processResult;
                }
                processResult.getResult().add(payrollSubmitProcessResult.getResult());
            }
        }

        return processResult;
    }

    private ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> voidPaychecks(PayrollRun pPayrollRunWrapper, Map<SpcfCalendar, LiabilityAdjustmentSubmission> pAdjustmentMap) {
        ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>> processResult = new ProcessResult<List<com.intuit.sbd.payroll.psp.domain.PayrollRun>>();
        processResult.setResult(new ArrayList<com.intuit.sbd.payroll.psp.domain.PayrollRun>());

        VoidDateHolder[] dateArray = new VoidDateHolder[4];

        List<String> voidPaycheckIds = new ArrayList<String>();
        for (Paycheck paycheckWrapper : pPayrollRunWrapper.getPaycheckUpdates()) {
            if (paycheckWrapper.isVoid()) {
            	//save session id
                com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.
                		Paycheck.findPaycheck(mCompany, paycheckWrapper.getSourceId());
                if (paycheck!=null) {
                	paycheck.setSessionId(paycheckWrapper.getIPAYCHK().getISESSIONID());

                }
                voidPaycheckIds.add(paycheckWrapper.getSourceId());
            }
        }

        // payroll run id -> paycheck ids
        List<com.intuit.sbd.payroll.psp.domain.Paycheck> paychecksAlreadyVoided = new ArrayList<com.intuit.sbd.payroll.psp.domain.Paycheck>();
        Map<String, List<String>> paychecksToVoid = new HashMap<String, List<String>>();
        if (voidPaycheckIds.size() > 0) {
            // find paychecks that have not been voided yet
            for (String paycheckId : voidPaycheckIds) {
                com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(mCompany, paycheckId);
                if (paycheck.getStatus() != PaycheckStatusCode.Active) {
                    paychecksAlreadyVoided.add(paycheck);
                    continue;
                }
                // disallow any DD paycheck void in the case of weak authentication
                if (mCredentialType != CredentialType.Pin && paycheck.isDDPaycheck()) {
                    processResult.getMessages().OperationDeniedForAuthentication(EntityName.Paycheck, paycheckId);
                    return processResult;
                }
                String payrollRunId = paycheck.getPayrollRun().getSourcePayRunId();
                List<String> paycheckIds = paychecksToVoid.get(payrollRunId);
                if (paycheckIds == null) {
                    paycheckIds = new ArrayList<String>();
                    paychecksToVoid.put(payrollRunId, paycheckIds);
                }
                paycheckIds.add(paycheck.getSourcePaycheckId());
            }
        }

        // send back paycheck mod so QB stops sending void
        if(!paychecksAlreadyVoided.isEmpty()) {
            mConnectionInformation.addNumberOfVoids(voidPaycheckIds.size());
            for (com.intuit.sbd.payroll.psp.domain.Paycheck paycheck : paychecksAlreadyVoided) {
                if(paycheck.getQbdtPaycheckInfo() != null) {
                    paycheck.getQbdtPaycheckInfo().setVoidToken(paycheck.getCompany().getNextToken());
                    Application.save(paycheck);
                }
            }
        }

        for (String payrollRunId : paychecksToVoid.keySet()) {
            com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRun(mCompany, payrollRunId);
            SpcfCalendar usageBillingEffectiveDate = mCompany.getUsageBillingEffectiveDate();
            if (usageBillingEffectiveDate != null && !usageBillingEffectiveDate.after(payrollRun.getPaycheckDate())) {
                payrollRun.setUsageBillingToken(com.intuit.sbd.payroll.psp.domain.PayrollRun.fetchNextUsageBillingToken());
            }
            boolean hasDebitOffloaded = payrollRun.havePayrollDebitTransactionsOffloaded();

            // if there are adjustments to be applied to a void find the latest non offloaded date or latest date in each quarter
            if (pAdjustmentMap != null && pAdjustmentMap.keySet().size() > 0) {
                SpcfCalendar paycheckDate = payrollRun.getPaycheckDate();
                int quarter = CalendarUtils.getQuarterAsInt(paycheckDate) - 1;
                VoidDateHolder voidDateHolder = dateArray[quarter];
                if (voidDateHolder == null) {
                    voidDateHolder = new VoidDateHolder();
                }

                if (voidDateHolder.date == null || paycheckDate.after(voidDateHolder.date)) {
                    if (voidDateHolder.payrollRunId == null) {
                        voidDateHolder.date = paycheckDate;
                        if (!hasDebitOffloaded) {
                            voidDateHolder.payrollRunId = payrollRunId;
                        }
                    } else if (!hasDebitOffloaded) {
                        voidDateHolder.date = paycheckDate;
                        voidDateHolder.payrollRunId = payrollRunId;
                    }
                }
                dateArray[quarter] = voidDateHolder;
            }

            List<String> paycheckIds = paychecksToVoid.get(payrollRunId);

            mConnectionInformation.addNumberOfVoids(paycheckIds.size());
            if (!hasDebitOffloaded) {
                TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
                transactionCancelEEDTO.setSourcePaycheckIdList(paycheckIds);
                transactionCancelEEDTO.setSourcePayrollRunId(payrollRunId);
                ProcessResult<TransactionResponse> recallPR =
                        PayrollServices.payrollManager.cancelEmployeeTransaction(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), transactionCancelEEDTO);
                if (!recallPR.isSuccess()) {
                    processResult.merge(recallPR);
                    return processResult;
                }
                payrollRun = com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRun(mCompany, payrollRunId);
                processResult.getResult().add(payrollRun);
            } else {
                if(mCompany.isCompanyOnService(ServiceCode.Tax)) {
                    VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
                    voidPayrollDTO.setPaycheckIdList(paycheckIds);
                    voidPayrollDTO.setSourcePayrollRunId(payrollRunId);
                    ProcessResult<CompanyAdjustmentSubmission> voidPayrollPR =
                            PayrollServices.payrollManager.voidPayroll(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), voidPayrollDTO);
                    if (!voidPayrollPR.isSuccess()) {
                        processResult.merge(voidPayrollPR);
                        return processResult;
                    }
                    payrollRun = com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRun(mCompany, payrollRunId);
                    processResult.getResult().add(payrollRun);
                } else {
                    for (String paycheckId : paycheckIds) {
                        com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(mCompany, paycheckId);
                        processResult.merge(PayrollServices.payrollManager.updateVoidedAfterOffload(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), paycheckId, true, null));
                        CompanyEvent.createPaycheckEvent(mCompany, paycheckId, paycheck.getId(), EventTypeCode.PaycheckRecalledAfterOffload);
                    }
                }
            }
        }

        // update adjustment dates to latest void date per quarter
        // this map will only be non null for void only submissions
        if (pAdjustmentMap != null && pAdjustmentMap.keySet().size() > 0) {
            Map<SpcfCalendar, LiabilityAdjustmentSubmission> tempMap = new HashMap<SpcfCalendar, LiabilityAdjustmentSubmission>();
            for (Iterator<SpcfCalendar> iterator = pAdjustmentMap.keySet().iterator(); iterator.hasNext();) {
                SpcfCalendar effectiveDate = iterator.next();
                LiabilityAdjustmentSubmission liabilityAdjustmentSubmission = pAdjustmentMap.get(effectiveDate);
                // if there are any penny adjustments update the date to roll the adjustment into the latest void date per quarter
                VoidDateHolder closestVoidDate = dateArray[CalendarUtils.getQuarterAsInt(effectiveDate) - 1];
                if (closestVoidDate != null) {
                    LiabilityAdjustmentSubmission newLiabilityAdjustmentSubmission = new LiabilityAdjustmentSubmission(mPennyCutoff);
                    for (Iterator<PayrollTransactionProcessor.AdjustmentHolder> liabilityAdjustmentSubmissionIterator = liabilityAdjustmentSubmission.getAdjustmentHolders().iterator(); liabilityAdjustmentSubmissionIterator.hasNext();) {
                        PayrollTransactionProcessor.AdjustmentHolder adjustmentHolder = liabilityAdjustmentSubmissionIterator.next();

                        if(liabilityAdjustmentSubmission.rollInToPayroll(adjustmentHolder.getCompanyAdjustmentSubmissionDTO())) {
                            newLiabilityAdjustmentSubmission.setPayrollRunId(closestVoidDate.payrollRunId);
                            DateDTO dateDTO = new DateDTO(closestVoidDate.date);
                            adjustmentHolder.getCompanyAdjustmentSubmissionDTO().setSubmissionDate(dateDTO);
                            for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : adjustmentHolder.getCompanyAdjustmentSubmissionDTO().getLiabilityAdjustmentDTOs()) {
                                liabilityAdjustmentDTO.setEffectiveDate(dateDTO);
                            }
                            newLiabilityAdjustmentSubmission.addAdjustmentHolder(adjustmentHolder);
                            liabilityAdjustmentSubmissionIterator.remove();
                        }
                    }

                    if(newLiabilityAdjustmentSubmission.getAdjustmentHolders().size() > 0) {
                        tempMap.put(closestVoidDate.date, newLiabilityAdjustmentSubmission);
                    }

                    if(liabilityAdjustmentSubmission.getAdjustmentHolders().size() == 0) {
                        iterator.remove();
                    }
                }
            }

            // put the adjustment submissions back into the map with the updated date
            for (SpcfCalendar spcfCalendar : tempMap.keySet()) {
                pAdjustmentMap.put(spcfCalendar, tempMap.get(spcfCalendar));
            }
        }

        return processResult;
    }

    // The current implementation only handles DIY usage paycheck deletion
    // the whole request will be rejected if it contains any DD paycheck here
    // PSRV004235: workaround to loosen the rules for DELID tag. QBDT sets that tag for DD/Assisted paychecks, too!
    public ProcessResult deletePaychecks(List<String> pPaycheckIds, String sessionid) {
        ProcessResult processResult = new ProcessResult();

        // ignore the tag for assisted companies
        if (mCompany.isCompanyOnService(ServiceCode.Tax)) {
            return processResult;
        }
        SpcfCalendar usageBillingEffectiveDate = mCompany.getUsageBillingEffectiveDate();

        for (String paycheckId : pPaycheckIds) {
            com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(mCompany, paycheckId);

            if (paycheck == null || paycheck.isDDPaycheck()) {
                continue;
            }


            //afroza786: should we be calling PayrollServices.payrollManager.deletePaycheck instead of directly updatinf paycheck from this method.....

            paycheck.setStatus(PaycheckStatusCode.Deleted);
            paycheck.setSessionId(sessionid);

            //afroza786: calling workerscomp method to capture delete info. Again this would not have been needed had PayrollServices.payrollManager.deletePaycheck been called
            WorkersCompPaycheck.cancelOrDeleteWorkersCompPaycheck(paycheck);


            if (mCompany.onUsageBilling() && usageBillingEffectiveDate != null && !usageBillingEffectiveDate.after(paycheck.getPayrollRun().getPaycheckDate())) {
                paycheck.getPayrollRun().setUsageBillingToken(com.intuit.sbd.payroll.psp.domain.PayrollRun.fetchNextUsageBillingToken());
            }
        }

        return processResult;
    }

    private ProcessResult updateQBDTInfo(PayrollRun pPayrollRunWrapper) {
        if (mCredentialType != CredentialType.Pin) {
            return new ProcessResult();
        }

        PayrollRunDTO payrollRunDTO = buildPayrollRunQBDTInfoUpdateDTO(pPayrollRunWrapper);
        mConnectionInformation.addNumberOfPaycheckMods(payrollRunDTO.getPaychecks().size());
        return PayrollServices.payrollManager.updateQBPayrollInfo(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), payrollRunDTO);
    }

    private ProcessResult updatePayrollRun(PayrollRun pPayrollRunWrapper) {
        if (mCredentialType != CredentialType.Pin) {
            return new ProcessResult();
        }
        PayrollRunDTO payrollRunDTO = buildPayrollRunQBDTInfoUpdateDTO(pPayrollRunWrapper);
        if (payrollRunDTO.getPaychecks().isEmpty()) {
            logger.warn("updated paycheck not present in request with PSID: " + mCompany.getSourceCompanyId());
            return new ProcessResult();
        }

        String sourceId = payrollRunDTO.getPaychecks().stream().findFirst().get().getPaycheckId();
        com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(mCompany, sourceId);
        if (Objects.isNull(paycheck)) {
            logger.warn("paycheck not found with paycheck sourceId: " + sourceId + " and company PSID: " + mCompany.getSourceCompanyId());
            return new ProcessResult();
        }
        logger.info("updated guideline paychecks count=" + payrollRunDTO.getPaychecks().size() + " PSID=" + mCompany.getSourceCompanyId()+" paycheck_seq="+
                paycheck.getId()+" payroll_run_seq="+paycheck.getPayrollRun().getId());
        return PayrollServices.payrollManager.updatePayroll(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), paycheck.getPayrollRun(), payrollRunDTO.getPaychecks());
    }

    private PayrollRunDTO buildPayrollRunQBDTInfoUpdateDTO(PayrollRun pPayrollRunWrapper) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        for (Paycheck paycheckWrapper : pPayrollRunWrapper.getPaycheckUpdates()) {
            com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(mCompany, paycheckWrapper.getSourceId());
            if (payrollRunDTO.getPayrollTXBatchId() == null && paycheck != null) {
                payrollRunDTO.setPayrollTXBatchId(paycheck.getPayrollRun().getSourcePayRunId());
            }
            PaycheckDTO paycheckDTO = new PaycheckDTO();
            PaycheckTranslator.populatePaycheckUpdateDTO(mCompany, paycheckWrapper, paycheck, paycheckDTO);
            payrollRunDTO.getPaychecks().add(paycheckDTO);

            payrollRunDTO.setSessionId(paycheckWrapper.getIPAYCHK().getISESSIONID());
        }

        return payrollRunDTO;
    }

    private void findAndUpdateHireActAdjustments(LiabilityAdjustmentSubmission pLiabilityAdjustmentSubmission) {
        if (!mCalculateHireAct || pLiabilityAdjustmentSubmission == null) {
            return;
        }

        /*  Go through all adjustments for this date and create & fill PayItemDTOs with the info    */
        for (PayrollTransactionProcessor.AdjustmentHolder adjustmentHolder : pLiabilityAdjustmentSubmission.getAdjustmentHolders()) {
            CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = adjustmentHolder.getCompanyAdjustmentSubmissionDTO();
            for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
                if(liabilityAdjustmentDTO.getSourceEmployeeId() != null) {
                    if (FICA_EE_LAW_ID.equals(liabilityAdjustmentDTO.getLawId()) && liabilityAdjustmentDTO.getAmount() != null) {   /* Adjustment for SS EE */
                        PayItemDTO payItemDTO = new PayItemDTO();
                        payItemDTO.setAmount(liabilityAdjustmentDTO.getAmount());
                        payItemDTO.setEmployeeId(liabilityAdjustmentDTO.getSourceEmployeeId());
                        payItemDTO.setEffectiveDate(liabilityAdjustmentDTO.getEffectiveDate());
                        payItemDTO.setPayItemCode(PayItemCode.HIREAct);
                        mTotalHIREActReduction = mTotalHIREActReduction.add(liabilityAdjustmentDTO.getAmount());
                        mPayItemDTOs.add(payItemDTO);
                    }
                    else if (FICA_ER_LAW_ID.equals(liabilityAdjustmentDTO.getLawId()) && liabilityAdjustmentDTO.getAmount() != null) {   /*  Adjustment for SS ER, need to reduce by the HIREActReduction    */
                        liabilityAdjustmentDTO.setAmount((SpcfMoney) liabilityAdjustmentDTO.getAmount().subtract(mTotalHIREActReduction));
                        liabilityAdjustmentDTO.setPayItemDTOs(mPayItemDTOs);
                        mTotalHIREActReduction = new SpcfMoney("0.00");
                        mPayItemDTOs = new ArrayList<PayItemDTO>();
                    }
                }
            }
        }
    }

    private void processHireActPaychecks(Paycheck pPaycheckWrapper, SpcfCalendar pPaycheckDate) {
        if (!mCalculateHireAct) {
            return;
        }

        /*
          �	Zero SS ER liability
          �	Greater than zero SS EE liability
          �	Positive SS EE wage base
        */
        boolean zeroFICAERLiability = false;
        boolean positiveFICAERWageBase = false;
        boolean nonZeroFICAEELiability = false;
        SpcfDecimal taxAmount = new SpcfMoney("0.0");
        for (Paycheck.Tax tax : pPaycheckWrapper.getTaxes()) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, tax.getPayrollItemId());

            if (companyLaw != null) {
                // check for Hire Act
                // fica ee
                if (FICA_EE_LAW_ID.equals(companyLaw.getLaw().getLawId())) {
                    nonZeroFICAEELiability = tax.getAmount() != null && tax.getAmount().compareTo(SpcfMoney.ZERO) != 0;
                    // negate employee taxes
                    taxAmount = taxAmount.add(tax.getAmount().negate());
                }
                // fica er
                else if (FICA_ER_LAW_ID.equals(companyLaw.getLaw().getLawId())) {
                    zeroFICAERLiability = tax.getAmount() == null || tax.getAmount().compareTo(SpcfMoney.ZERO) == 0;
                    positiveFICAERWageBase = tax.getTaxableWageAmount() != null && tax.getTaxableWageAmount().compareTo(SpcfMoney.ZERO) != 0;
                }
            }
        }

        if (zeroFICAERLiability && positiveFICAERWageBase && nonZeroFICAEELiability) {
            mTotalHIREActReduction = mTotalHIREActReduction.add(taxAmount);
            PayItemDTO payItemDTO = new PayItemDTO();
            payItemDTO.setAmount(new SpcfMoney(taxAmount));
            payItemDTO.setEmployeeId(pPaycheckWrapper.getSourceEmployeeId());
            payItemDTO.setEffectiveDate(new DateDTO(pPaycheckDate));
            payItemDTO.setPayItemCode(PayItemCode.HIREAct);
            mPayItemDTOs.add(payItemDTO);
        }
    }

    private ProcessResult<List<PayrollRunDTO>> buildPayrollRunDtosForNewPayrolls(List<PayrollRun> pPayrolls, Map<SpcfCalendar, LiabilityAdjustmentSubmission> pAdjustmentMap, List<ServiceBankAccountDTO> pServiceBankAccountList) {
        // keep track of the paychecks that have been submitted so that we can filter out duplicate payroll runs from QB
        Set<String> paycheckIds = new HashSet<String>();
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        CompanyService ddService = mCompany.getCompanyService(ServiceCode.DirectDeposit);

        SourcePayrollParameter parameter = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT, SourcePayrollParameterCode.QBVersionSunsetString);
        SpcfCalendar expirationDate = parameter.getExpirationDate().toLocal();

        ProcessResult<List<PayrollRunDTO>> processResult = new ProcessResult<List<PayrollRunDTO>>();
        List<PayrollRunDTO> payrollRunDTOs = new ArrayList<PayrollRunDTO>();
        boolean chargeTransmissionFee = true;
        boolean transmissionHasBackdatedPayrolls = false;
        Set<String> employeesPaidSet = new HashSet<String>();
        for (PayrollRun payrollWrapper : pPayrolls) {
            if(payrollWrapper.getNewPaychecks().size() == 0) {
                // all of the paychecks were moved to modifications
                continue;
            }

            SpcfCalendar paycheckDate = SpcfCalendar.createInstance(payrollWrapper.getPaycheckDate().getTime());
            if (mOnMinSupportedVersion && paycheckDate.getTimeInMilliseconds() >= expirationDate.getTimeInMilliseconds()) {
                processResult.getMessages().SoftwareVersionUnsupported();
                return processResult;
            }

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            payrollRunDTO.setCompanyBankAccounts(pServiceBankAccountList);
            payrollRunDTO.setTargetPayrollTXDate(new DateDTO(paycheckDate));
            payrollRunDTO.setTransmissionId(mTransmissionId);
            payrollRunDTO.setPayrollTXBatchId(UUID.randomUUID().toString());
            payrollRunDTO.setBalanceFilePayroll(mTransmissionType.equals(TransmissionType.BalanceFile));
            payrollRunDTO.setIsAssisted(mConnectionInformation.isAssistedRequest());

            Collection<PaycheckDTO> paycheckDTOs = new ArrayList<PaycheckDTO>();
            for (Paycheck paycheckWrapper : payrollWrapper.getNewPaychecks()) {
                // disallow any DD line in the case of weak authentication
                if (mCredentialType != CredentialType.Pin && !paycheckWrapper.getDirectDeposits().isEmpty()) {
                    processResult.getMessages().OperationDeniedForAuthentication(EntityName.Paycheck, paycheckWrapper.getSourceId());
                    return processResult;
                }

                if (!paycheckIds.contains(paycheckWrapper.getSourceId())) {
                    paycheckIds.add(paycheckWrapper.getSourceId());
                    PaycheckDTO paycheckDTO = new PaycheckDTO();

                    // PSRV004202 :: If we receive a paycheck for Assisted that we have not saved before (does not exist in the paycheck table)
                    // and is dated after 2011-01-01 with no detail we should reject the payroll submission.
                    // This indicates that there is a problem in QB. This should be check for Assisted only, a symphony check will not have detail lines.

                    SpcfCalendar qbdtThresholdDate = SpcfCalendar.createInstance(2011,1,1);
                    if (mCompany.hasService(ServiceCode.Tax) && !paycheckWrapper.isVoid() && paycheckDate.after(qbdtThresholdDate) && paycheckWrapper.hasNoDetails())  {
                        processResult.getMessages().PaycheckHasNoDetails(EntityName.PayrollItem,paycheckWrapper.getSourceId(),paycheckWrapper.getSourceId());
                        return processResult;
                    }
                    SpcfCalendar watermarkDate = mCompany.getQuickbooksInfo().getWatermarkDate();

                    // Reject the payroll if the watermark date is after or equal to the
                    // DD paycheck date
                    if (watermarkDate != null &&
                            !mIsAssistedRequest &&
                            !paycheckWrapper.getDirectDeposits().isEmpty() &&
                            !paycheckDate.after(watermarkDate)) {
                        processResult.getMessages().InvalidPaycheckDate(EntityName.PayrollItem,paycheckWrapper.getSourceId(), watermarkDate.format("MM/dd/yyyy"));
                        return processResult;
                    }

                    processResult.merge(PaycheckTranslator.populatePaycheckDTO(mCompany, paycheckWrapper, paycheckDTO, mIsAssistedRequest));
                    if (!processResult.isSuccess()) {
                        return processResult;
                    }

                    processHireActPaychecks(paycheckWrapper, paycheckDate);

                    if(!paycheckDTO.isVoid()) {
                        employeesPaidSet.add(paycheckDTO.getEmployeeId());
                    }
                    paycheckDTOs.add(paycheckDTO);
                }
            }

            // check for all duplicates
            if(paycheckDTOs.size() == 0) {
                continue;
            }

            payrollRunDTO.setPaychecks(paycheckDTOs);

            List<CompanyAdjustmentSubmissionDTO> companyAdjustmentSubmissionDTOs = new ArrayList<CompanyAdjustmentSubmissionDTO>();
            if (pAdjustmentMap.keySet().size() > 0) {
                // All liability adjustments that are less than or equal to the penny cutoff, do not have wage amounts,
                // and have an effective date in the same quarter as the payroll will be rolled into the payroll or if they are COBRA.
                // If there are multiple payrolls then the new liability adjustments will be rolled
                // into the first payroll matching their effective quarter
                SpcfCalendar firstDayOfQuarter = CalendarUtils.getFirstDayOfQuarter(paycheckDate);
                SpcfCalendar lastDayOfQuarter = CalendarUtils.getLastDayOfQuarter(paycheckDate);
                for (Iterator<SpcfCalendar> iterator = pAdjustmentMap.keySet().iterator(); iterator.hasNext();) {
                    SpcfCalendar adjustmentDate = iterator.next();
                    if (adjustmentDate.between(firstDayOfQuarter, lastDayOfQuarter)) {
                        LiabilityAdjustmentSubmission liabilityAdjustmentSubmission = pAdjustmentMap.get(adjustmentDate);
                        for (Iterator<PayrollTransactionProcessor.AdjustmentHolder>  adjustmentHolderIterator = liabilityAdjustmentSubmission.getAdjustmentHolders().iterator(); adjustmentHolderIterator.hasNext();) {
                            PayrollTransactionProcessor.AdjustmentHolder adjustmentHolder = adjustmentHolderIterator.next();
                            CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = adjustmentHolder.getCompanyAdjustmentSubmissionDTO();
                            if (liabilityAdjustmentSubmission.rollInToPayroll(companyAdjustmentSubmissionDTO)) {
                                findAndUpdateHireActAdjustments(liabilityAdjustmentSubmission);
                                companyAdjustmentSubmissionDTOs.add(companyAdjustmentSubmissionDTO);
                                adjustmentHolderIterator.remove();
                            }
                        }

                        if(liabilityAdjustmentSubmission.getAdjustmentHolders().size() == 0) {
                            iterator.remove();
                        }
                    }
                }

                // we need to update the liability adjustment effective date to get it to apply to the payment for the payroll
                // we also need to update the submission date because the effective date cannot be after the submission date in QB
                DateDTO paycheckDateDTO = new DateDTO(paycheckDate);
                for (CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO : companyAdjustmentSubmissionDTOs) {
                    companyAdjustmentSubmissionDTO.setSubmissionDate(paycheckDateDTO);
                    for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
                        liabilityAdjustmentDTO.setEffectiveDate(paycheckDateDTO);
                    }
                }

                payrollRunDTO.setCompanyAdjustmentSubmissionDTOs(companyAdjustmentSubmissionDTOs);
            }

            payrollRunDTOs.add(payrollRunDTO);

            // charge the transmission fee on the first payroll
            if(chargeTransmissionFee && employeesPaidSet.size() > 0) {
                payrollRunDTO.setChargeTransmissionFee(true);
                chargeTransmissionFee = false;
            }

            if(!transmissionHasBackdatedPayrolls && ddService != null) {
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
                // Note: To answer your question: If the payroll contains even one terminated EE (at any time) we do not charge the backdate fee (whatever, don't ask).

                if (paycheckDate.before(today)) {
                    // always a back date
                    transmissionHasBackdatedPayrolls  = true;
                } else {
                    SpcfCalendar initiationDate = FinancialTransaction.getInitiationDate(FinancialTransaction.calculateSettlementDate(ddService, paycheckDate),
                                                                                         TransactionTypeCode.EmployerDdDebit);
                    if(initiationDate.before(today)) {
                        boolean foundTerminatedEmployee = false;

                        for (PaycheckDTO paycheckDTO : paycheckDTOs) {
                            Employee employee = Employee.findEmployee(mCompany, paycheckDTO.getEmployeeId());

                            if(employee != null && employee.getTerminationDate() != null) {
                                foundTerminatedEmployee = true;
                                break;
                            }
                        }

                        if(!foundTerminatedEmployee) {
                            transmissionHasBackdatedPayrolls = true;
                        }
                    }
                }

                if (transmissionHasBackdatedPayrolls) {
                    SpcfDecimal backDatedTaxLiability = SpcfMoney.ZERO;
                    for (PaycheckDTO paycheckDTO : paycheckDTOs) {
                        for (LiabilityTransactionDTO liabilityTransactionDTO : paycheckDTO.getLiabilityTransactions()) {
                            backDatedTaxLiability = backDatedTaxLiability.add(SpcfUtils.convertToSpcfDecimal(liabilityTransactionDTO.getLiabilityAmount()));
                        }
                    }

                    if (backDatedTaxLiability.isLessThanEqualTo(new SpcfMoney("4.99"))) {
                        transmissionHasBackdatedPayrolls = false;
                    }
                }
            }
        }

        if(!payrollRunDTOs.isEmpty()) {
            payrollRunDTOs.get(0).setEmployeesPaidInTransmission(employeesPaidSet.size());

            if (transmissionHasBackdatedPayrolls) {
                payrollRunDTOs.get(0).setTransmissionHasBackdatedPayrolls(transmissionHasBackdatedPayrolls);
            }
        }

        // create a payroll run dto for each of the remaining adjustments that do not have a payroll run id
        // having a payroll run id signifies that the adjustment needs to associated to a voided or recalled payroll
        for (Iterator<SpcfCalendar> iterator = pAdjustmentMap.keySet().iterator(); iterator.hasNext();) {
            SpcfCalendar adjustmentDate = iterator.next();
            LiabilityAdjustmentSubmission liabilityAdjustmentSubmission = pAdjustmentMap.get(adjustmentDate);
            if(liabilityAdjustmentSubmission.getPayrollRunId() == null) {
                PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
                payrollRunDTO.setCompanyBankAccounts(pServiceBankAccountList);
                payrollRunDTO.setTargetPayrollTXDate(new DateDTO(adjustmentDate));
                payrollRunDTO.setTransmissionId(mTransmissionId);
                payrollRunDTO.setPayrollTXBatchId(UUID.randomUUID().toString());
                payrollRunDTO.setBalanceFilePayroll(mTransmissionType.equals(TransmissionType.BalanceFile));
                payrollRunDTO.setPayrollType(PayrollType.Adjustment);

                List<CompanyAdjustmentSubmissionDTO> companyAdjustmentSubmissionDTOs = new ArrayList<CompanyAdjustmentSubmissionDTO>();
                findAndUpdateHireActAdjustments(liabilityAdjustmentSubmission);
                for (PayrollTransactionProcessor.AdjustmentHolder adjustmentHolder : liabilityAdjustmentSubmission.getAdjustmentHolders()) {
                    companyAdjustmentSubmissionDTOs.add(adjustmentHolder.getCompanyAdjustmentSubmissionDTO());
                }
                payrollRunDTO.setCompanyAdjustmentSubmissionDTOs(companyAdjustmentSubmissionDTOs);

                payrollRunDTOs.add(payrollRunDTO);
                iterator.remove();
            }
        }

        processResult.setResult(payrollRunDTOs);
        return processResult;
    }

    private class VoidDateHolder {
        public SpcfCalendar date;
        public String payrollRunId;
    }

    private class PayrollSubmissionHolder {
        public LiabilityAdjustmentSubmission liabilityAdjustmentSubmission;
        public List<PayrollRunDTO> payrollRunDTOs = new ArrayList<PayrollRunDTO>();
    }

}
