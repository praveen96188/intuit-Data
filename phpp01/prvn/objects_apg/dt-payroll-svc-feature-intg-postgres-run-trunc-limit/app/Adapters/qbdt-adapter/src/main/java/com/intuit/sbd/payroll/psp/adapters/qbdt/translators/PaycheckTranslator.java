package com.intuit.sbd.payroll.psp.adapters.qbdt.translators;

import com.intuit.sbd.payroll.psp.adapters.qbdt.utils.GuideLineUtils;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyLaw;
import com.intuit.sbd.payroll.psp.domain.CompanyPayrollItem;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 16, 2010
 * Time: 10:14:25 AM
 */
public class PaycheckTranslator {
    private static final SpcfLogger logger = PayrollServices.getLogger(PaycheckTranslator.class);

    public static ProcessResult populatePaycheckDTO(Company pCompany, Paycheck pPaycheck, PaycheckDTO pPaycheckDTO, boolean pCanBeRecovered) {
        ProcessResult processResult = new ProcessResult();

        pPaycheckDTO.setPaycheckId(pPaycheck.getSourceId());
        pPaycheckDTO.setEmployeeId(pPaycheck.getSourceEmployeeId());
        pPaycheckDTO.setPayPeriodBeginDate(new DateDTO(pPaycheck.getPeriodBeginDate()));
        pPaycheckDTO.setPayPeriodEndDate(new DateDTO(pPaycheck.getPeriodEndDate()));
        if(pPaycheck.getNetAmount() != null) {
            pPaycheckDTO.setPaycheckNetAmount(new SpcfMoney(pPaycheck.getNetAmount().negate()));
        }

        QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = new QBDTPaycheckInfoDTO();
        PaycheckTranslator.populateQBDTPaycheckInfo(pPaycheck, qbdtPaycheckInfoDTO);
        if (!pCanBeRecovered) {
            qbdtPaycheckInfoDTO.setToken(Company.EXCLUDE_TOKEN);
        }
        pPaycheckDTO.setQBDTPaycheckInfoDTO(qbdtPaycheckInfoDTO);

        pPaycheckDTO.setIsYTDAdjustment(pPaycheck.isYTDAdjustment());
        pPaycheckDTO.setIsVoid(pPaycheck.isVoid());
        //if(pPaycheck.getIPAYCHK())
        if(pPaycheck.getIPAYCHK()!=null){
        	pPaycheckDTO.setSessionID(pPaycheck.getIPAYCHK().getISESSIONID());
        }
        Collection<CompensationTransactionDTO> compensationTransactionDTOs = new ArrayList<CompensationTransactionDTO>();
        Collection<DeductionTransactionDTO> deductionTransactionDTOs = new ArrayList<DeductionTransactionDTO>();
        Collection<EmployerContributionTransactionDTO> employerContributionTransactionDTOs = new ArrayList<EmployerContributionTransactionDTO>();

        for (Paycheck.Compensation compensation : pPaycheck.getHourlyWages()) {
            compensationTransactionDTOs.add(PaycheckTranslator.buildCompensationTransactionDTO(compensation));
        }

        for (Paycheck.Compensation compensation : pPaycheck.getSalaryWages()) {
            compensationTransactionDTOs.add(PaycheckTranslator.buildCompensationTransactionDTO(compensation));
        }

        for (Paycheck.Adjustment adjustment : pPaycheck.getAdjustments()) {
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, adjustment.getPayrollItemId());
            if(companyPayrollItem == null) {
                processResult.getMessages().PayrollItemDoesNotExist(EntityName.PayrollItem,
                                                                    adjustment.getPayrollItemId(),
                                                                    pCompany.getSourceSystemCd().toString(),
                                                                    pCompany.getSourceCompanyId(),
                                                                    adjustment.getPayrollItemId());
                return processResult;
            }

            switch(companyPayrollItem.getPayrollItem().getPayrollItemType()) {
                case Compensation:
                    compensationTransactionDTOs.add(PaycheckTranslator.buildCompensationTransactionDTO(adjustment));
                    break;
                case Deduction:
                    deductionTransactionDTOs.add(PaycheckTranslator.buildDeductionTransactionDTO(adjustment));
                    break;
                case EmployerContribution:
                    employerContributionTransactionDTOs.add(PaycheckTranslator.buildEmployerContributionTransactionDTO(adjustment));
                    break;
            }
        }

        pPaycheckDTO.setCompensationTransactions(compensationTransactionDTOs);
        pPaycheckDTO.setDeductionTransactions(deductionTransactionDTOs);
        pPaycheckDTO.setEmployerContributionTransactions(employerContributionTransactionDTOs);

        pPaycheckDTO.setDdTransactions(new ArrayList<DDTransactionDTO>());
        int ddOrder = 0;
        for (Paycheck.DirectDeposit directDeposit : pPaycheck.getDirectDeposits()) {
            pPaycheckDTO.getDdTransactions().add(PaycheckTranslator.buildDDTransactionDTO(directDeposit, ddOrder++));
        }

        Collection<LiabilityTransactionDTO> liabilityTransactionDTOs = new ArrayList<LiabilityTransactionDTO>();
        for (Paycheck.Tax tax : pPaycheck.getTaxes()) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(pCompany, tax.getPayrollItemId());
            if(companyLaw == null) {
                processResult.getMessages().CompanyLawDoesNotExist(EntityName.PayrollItem,
                                                                   pCompany.getSourceSystemCd().toString(),
                                                                   pCompany.getSourceCompanyId(),
                                                                   tax.getPayrollItemId());
                return processResult;
            }
            liabilityTransactionDTOs.add(PaycheckTranslator.buildLiabilityTransactionDTO(tax, companyLaw.getLaw().getLawId(), companyLaw.getQbdtPayrollItemInfo().getIsEmployeePaid()));
        }
        pPaycheckDTO.setLiabilityTransactions(liabilityTransactionDTOs);

        return processResult;
    }

    public static void populatePaycheckUpdateDTO(Company pCompany, Paycheck pPaycheckWrapper, com.intuit.sbd.payroll.psp.domain.Paycheck pPaycheck, PaycheckDTO pPaycheckDTO) {

        pPaycheckDTO.setPaycheckId(pPaycheck.getSourcePaycheckId());
        pPaycheckDTO.setEmployeeId(pPaycheckWrapper.getSourceEmployeeId());

        QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = new QBDTPaycheckInfoDTO();
        PaycheckTranslator.populateQBDTPaycheckInfo(pPaycheckWrapper, qbdtPaycheckInfoDTO);
        pPaycheckDTO.setQBDTPaycheckInfoDTO(qbdtPaycheckInfoDTO);

        if (GuideLineUtils.isUpdatePayrollAllowed(pCompany)) {
            pPaycheckDTO.setPayPeriodBeginDate(new DateDTO(pPaycheckWrapper.getPeriodBeginDate()));
            pPaycheckDTO.setPayPeriodEndDate(new DateDTO(pPaycheckWrapper.getPeriodEndDate()));

            if (pPaycheck.getNetAmount() != null) {
                pPaycheckDTO.setPaycheckNetAmount(new SpcfMoney(pPaycheckWrapper.getNetAmount().negate()));
            }
        }

        Collection<CompensationTransactionDTO> compensationTransactionDTOs = new ArrayList<CompensationTransactionDTO>();
        Collection<DeductionTransactionDTO> deductionTransactionDTOs = new ArrayList<DeductionTransactionDTO>();
        Collection<EmployerContributionTransactionDTO> employerContributionTransactionDTOs = new ArrayList<EmployerContributionTransactionDTO>();

        for (Paycheck.Compensation compensation : pPaycheckWrapper.getHourlyWages()) {
            compensationTransactionDTOs.add(PaycheckTranslator.buildCompensationTransactionDTO(compensation));
        }

        for (Paycheck.Compensation compensation : pPaycheckWrapper.getSalaryWages()) {
            compensationTransactionDTOs.add(PaycheckTranslator.buildCompensationTransactionDTO(compensation));
        }

        for (Paycheck.Adjustment adjustment : pPaycheckWrapper.getAdjustments()) {
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, adjustment.getPayrollItemId());

            switch(companyPayrollItem.getPayrollItem().getPayrollItemType()) {
                case Compensation:
                    compensationTransactionDTOs.add(PaycheckTranslator.buildCompensationTransactionDTO(adjustment));
                    break;
                case Deduction:
                    deductionTransactionDTOs.add(PaycheckTranslator.buildDeductionTransactionDTO(adjustment));
                    break;
                case EmployerContribution:
                    employerContributionTransactionDTOs.add(PaycheckTranslator.buildEmployerContributionTransactionDTO(adjustment));
                    break;
            }
        }

        pPaycheckDTO.setSessionID(pPaycheck.getSessionId());
        pPaycheckDTO.setCompensationTransactions(compensationTransactionDTOs);
        pPaycheckDTO.setDeductionTransactions(deductionTransactionDTOs);
        pPaycheckDTO.setEmployerContributionTransactions(employerContributionTransactionDTOs);

        pPaycheckDTO.setDdTransactions(new ArrayList<DDTransactionDTO>());
        int ddOrder = 0;
        for (Paycheck.DirectDeposit directDeposit : pPaycheckWrapper.getDirectDeposits()) {
            pPaycheckDTO.getDdTransactions().add(PaycheckTranslator.buildDDTransactionDTO(directDeposit, ddOrder++));
        }

        Collection<LiabilityTransactionDTO> liabilityTransactionDTOs = new ArrayList<LiabilityTransactionDTO>();
        for (Paycheck.Tax tax : pPaycheckWrapper.getTaxes()) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(pCompany, tax.getPayrollItemId());
            liabilityTransactionDTOs.add(PaycheckTranslator.buildLiabilityTransactionDTO(tax, companyLaw.getLaw().getLawId(), companyLaw.getQbdtPayrollItemInfo().getIsEmployeePaid()));
        }
        pPaycheckDTO.setLiabilityTransactions(liabilityTransactionDTOs);
    }

    public static CompensationTransactionDTO buildCompensationTransactionDTO(Paycheck.Compensation pCompensation) {
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setCompensationAmount(pCompensation.getAmount());
        compensationTransactionDTO.setCompensationYTDAmount(pCompensation.getYTDAmount());
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance(pCompensation.getHours()));
        compensationTransactionDTO.setSourcePayrollItemId(pCompensation.getPayrollItemId());
        compensationTransactionDTO.setPayStubOrder(pCompensation.getPayStubOrder());

        QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
        populateQBDTPaylineInfoDTO(pCompensation, qbdtPaylineInfoDTO);
        compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
        return compensationTransactionDTO;
    }
    public static CompensationTransactionDTO buildCompensationTransactionDTO(Paycheck.Adjustment pAdjustment) {
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setCompensationAmount(pAdjustment.getAmount());
        compensationTransactionDTO.setCompensationYTDAmount(pAdjustment.getYTDAmount());
        compensationTransactionDTO.setSourcePayrollItemId(pAdjustment.getPayrollItemId());
        compensationTransactionDTO.setPayStubOrder(pAdjustment.getPayStubOrder());

        QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
        populateQBDTPaylineInfoDTO(pAdjustment, qbdtPaylineInfoDTO);
        compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
        return compensationTransactionDTO;
    }

    public static DeductionTransactionDTO buildDeductionTransactionDTO(Paycheck.Adjustment pAdjustment) {
        DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
        if(pAdjustment.getAmount() != null) {
            deductionTransactionDTO.setDeductionAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(pAdjustment.getAmount().negate())));
        }
        if(pAdjustment.getYTDAmount() != null) {
            deductionTransactionDTO.setDeductionYTDAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(pAdjustment.getYTDAmount().negate())));
        }
        deductionTransactionDTO.setSourcePayrollItemId(pAdjustment.getPayrollItemId());
        deductionTransactionDTO.setPayStubOrder(pAdjustment.getPayStubOrder());

        QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
        populateQBDTPaylineInfoDTO(pAdjustment, qbdtPaylineInfoDTO);
        deductionTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
        return deductionTransactionDTO;
    }

    public static DDTransactionDTO buildDDTransactionDTO(Paycheck.DirectDeposit pDirectDeposit, int pDdOrder) {
        DDTransactionDTO ddTransactionDTO = new DDTransactionDTO();
        ddTransactionDTO.setDDTransactionAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(pDirectDeposit.getAmount().negate())));
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(pDirectDeposit.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pDirectDeposit.getRoutingNumber());
        bankAccountDTO.setAccountType(pDirectDeposit.getAccountType());
        bankAccountDTO.setBankName(QBOFX.nullStringCheck(pDirectDeposit.getBankName()));
        employeeBankAccountDTO.setBankAccount(bankAccountDTO);
        employeeBankAccountDTO.setOrder(pDdOrder);
        employeeBankAccountDTO.setPaycheckUpdate(true);
        employeeBankAccountDTO.setGenerateNewSourceId(true);
        ddTransactionDTO.setEmployeeBankAccount(employeeBankAccountDTO);
        ddTransactionDTO.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ddTransactionDTO.setPayStubOrder(pDirectDeposit.getPayStubOrder());
        return ddTransactionDTO;
    }

    public static EmployerContributionTransactionDTO buildEmployerContributionTransactionDTO(Paycheck.Adjustment pAdjustment) {
        EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
        employerContributionTransactionDTO.setContributionAmount(SpcfUtils.convertToBigDecimal(pAdjustment.getAmount()));
        employerContributionTransactionDTO.setContributionYTDAmount(SpcfUtils.convertToBigDecimal(pAdjustment.getYTDAmount()));
        employerContributionTransactionDTO.setSourcePayrollItemId(pAdjustment.getPayrollItemId());
        employerContributionTransactionDTO.setPayStubOrder(pAdjustment.getPayStubOrder());

        // todo do we need these? employerContributionTransactionDTO.setTaxableWagesAmount();
        // todo do we need these? employerContributionTransactionDTO.setTotalWagesAmount();

        QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
        populateQBDTPaylineInfoDTO(pAdjustment, qbdtPaylineInfoDTO);
        employerContributionTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
        return employerContributionTransactionDTO;
    }

    private static void populateQBDTPaylineInfoDTO(Paycheck.Adjustment pAdjustment, QBDTPaylineInfoDTO pQbdtPaylineInfoDTO) {
        pQbdtPaylineInfoDTO.setRate(pAdjustment.getRate());
        pQbdtPaylineInfoDTO.setRateType(pAdjustment.getRateType());
        pQbdtPaylineInfoDTO.setQuantity(pAdjustment.getQuantity());
        pQbdtPaylineInfoDTO.setQuantityType(pAdjustment.getQuantityType());
        pQbdtPaylineInfoDTO.setExpenseByJob(pAdjustment.isExpenseByJob());
    }

    private static void populateQBDTPaylineInfoDTO(Paycheck.Compensation pCompensation, QBDTPaylineInfoDTO pQbdtPaylineInfoDTO) {
        pQbdtPaylineInfoDTO.setItem(pCompensation.getItem());
        pQbdtPaylineInfoDTO.setJob(pCompensation.getJob());
        pQbdtPaylineInfoDTO.setRate(pCompensation.getRate());
        pQbdtPaylineInfoDTO.setRateType(pCompensation.getRateType());
        pQbdtPaylineInfoDTO.setTrackingClass(pCompensation.getTrackingClass());
        pQbdtPaylineInfoDTO.setWcCode(pCompensation.getWCCode());
    }

    public static void populateQBDTPaycheckInfo(Paycheck pPaycheck, QBDTPaycheckInfoDTO pQBDTPaycheckInfoDTO) {
        pQBDTPaycheckInfoDTO.setAccountName(pPaycheck.getAccountName());
        pQBDTPaycheckInfoDTO.setCheckNumber(pPaycheck.getCheckNumber());
        pQBDTPaycheckInfoDTO.setCleared(pPaycheck.getCleared());
        pQBDTPaycheckInfoDTO.setMemo(pPaycheck.getMemo());
        pQBDTPaycheckInfoDTO.setOnService(pPaycheck.isOnService());
        pQBDTPaycheckInfoDTO.setProrate(pPaycheck.getProrate());
        pQBDTPaycheckInfoDTO.setTrackingClass(pPaycheck.getTrackingClass());
        pQBDTPaycheckInfoDTO.setSickHoursAccrued(pPaycheck.getSickHoursAccrued());
        pQBDTPaycheckInfoDTO.setVacationHoursAccrued(pPaycheck.getVacationHoursAccrued());
        pQBDTPaycheckInfoDTO.setListId(pPaycheck.getListId());
    }

    public static LiabilityTransactionDTO buildLiabilityTransactionDTO(Paycheck.Tax pTax, String pLawId, boolean pIsEmployeeTax) {
        LiabilityTransactionDTO liabilityTransactionDTO = new LiabilityTransactionDTO();
        liabilityTransactionDTO.setLawId(pLawId);
        liabilityTransactionDTO.setPayrollItemId(pTax.getPayrollItemId());
        liabilityTransactionDTO.setPayStubOrder(pTax.getPayStubOrder());
        liabilityTransactionDTO.setLiabilityTaxableWages(SpcfUtils.convertToBigDecimal(pTax.getTaxableWageAmount()));
        liabilityTransactionDTO.setLiabilityTotalWages(SpcfUtils.convertToBigDecimal(pTax.getTotalWageAmount()));
        liabilityTransactionDTO.setLiabilityAmount(SpcfUtils.convertToBigDecimal(pIsEmployeeTax ? new SpcfMoney(pTax.getAmount().negate()) : pTax.getAmount()));
        liabilityTransactionDTO.setLiabilityAmountYTD(SpcfUtils.convertToBigDecimal(pIsEmployeeTax ? new SpcfMoney(pTax.getYTDAmount().negate()) : pTax.getYTDAmount()));
        liabilityTransactionDTO.setLiabilityTipsTaxableWages(SpcfUtils.convertToBigDecimal(pTax.getTipTaxableWageAmount()));
        
        return liabilityTransactionDTO;
    }
}
