package com.intuit.sbd.payroll.psp.batchjobs.employeeTotals;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertTrue;

/**
 * User: mvillani
 * Date: 8/29/2012
 * Time: 10:36 AM
 */
public class CalculateEmployeeTotalsTestsHelper {


    public static final List<String> TAX_FORM_LINES = Arrays.asList("ALLOCTIPS", "DPDNTCARE", "DPDNTCARECO", "NONQUALPLAN", "SEC457", "ADOPTION",
                                                                     "GROUPTERMLIFE", "MEDSAVING", "NONTAXSICK", "QUALMVEX", "ROTH401K", "ROTH403B",
                                                                     "SIMPLE", "Q125POP", "Q401K", "Q403B", "Q408K", "Q457B",
                                                                     "TTT14", "Q501C", "TTT3", "TTT7", "TTT8", "FRNGBNFTS",
                                                                     "OTHER", "OTHMVEXP", "TTT1", "TTT2", "TTT4", "TTT5",
                                                                     "LTAX1", "LTAX2", "SECLOCAL", "TTT11", "TTT6", "TTT9", "TIPS", "TTT10", "TTT17","TTT19");

    public static final List<String> DEDUCTION_TAX_FORM_LINES = Arrays.asList("Q125POP", "Q401K", "ROTH401K", "Q403B", "Q457B", "NONTAXSICK",
                                                                               "LTAX1", "TTT10", "GROUPTERMLIFE",
                                                                               "NONQUALPLAN", "DPDNTCARE",
                                                                               "Q408K", "TTT9", "SIMPLE",
                                                                               "DPDNTCARECO", "ROTH403B");

    public static final List<String> COMPENSATION_TAX_FORM_LINES = Arrays.asList("ALLOCTIPS", "DPDNTCARE", "DPDNTCARECO", "NONQUALPLAN", "SEC457", "ADOPTION",
                                                                                  "GROUPTERMLIFE", "MEDSAVING", "NONTAXSICK", "QUALMVEX", "ROTH401K", "ROTH403B",
                                                                                  "SIMPLE", "Q125POP", "Q401K", "Q403B", "Q408K", "Q457B",
                                                                                  "TTT14", "Q501C", "TTT3", "TTT7", "TTT8", "FRNGBNFTS",
                                                                                  "OTHER", "OTHMVEXP", "TTT1", "TTT2", "TTT4", "TTT5",
                                                                                  "LTAX1", "LTAX2", "SECLOCAL", "TTT11", "TTT6", "TTT9", "TIPS", "TTT10", "TTT17", "TTT19");

    public static final List<String> ER_CONTRIB_TAX_FORM_LINES = Arrays.asList("TTT7", "FRNGBNFTS", "TTT17","TTT19",
                                                                                "ALLOCTIPS", "TTT1", "Q501C",
                                                                                "TTT8", "TTT3", "QUALMVEX",
                                                                                "OTHER", "MEDSAVING");

    public static final Map<String, BigDecimal> W2_CODES;

    static {
        W2_CODES = new HashMap<String, BigDecimal>();
        W2_CODES.put("ALLOCTIPS", new BigDecimal("51"));
        W2_CODES.put("DPDNTCARE", new BigDecimal("6"));
        W2_CODES.put("DPDNTCARECO", new BigDecimal("48"));
        W2_CODES.put("NONQUALPLAN", new BigDecimal("8"));
        W2_CODES.put("SEC457", new BigDecimal("7"));
        W2_CODES.put("ADOPTION", new BigDecimal("47"));
        W2_CODES.put("GROUPTERMLIFE", new BigDecimal("50"));
        W2_CODES.put("MEDSAVING", new BigDecimal("45"));
        W2_CODES.put("NONTAXSICK", new BigDecimal("2"));
        W2_CODES.put("QUALMVEX", new BigDecimal("29"));
        W2_CODES.put("ROTH401K", new BigDecimal("57"));
        W2_CODES.put("ROTH403B", new BigDecimal("58"));
        W2_CODES.put("SIMPLE", new BigDecimal("46"));
        W2_CODES.put("Q125POP", new BigDecimal("53"));
        W2_CODES.put("Q401K", new BigDecimal("11"));
        W2_CODES.put("Q403B", new BigDecimal("12"));
        W2_CODES.put("Q408K", new BigDecimal("13"));
        W2_CODES.put("Q457B", new BigDecimal("14"));
        W2_CODES.put("TTT14", new BigDecimal("67"));
        W2_CODES.put("Q501C", new BigDecimal("15"));
        W2_CODES.put("TTT3", new BigDecimal("56"));
        W2_CODES.put("TTT7", new BigDecimal("60"));
        W2_CODES.put("TTT8", new BigDecimal("61"));
        W2_CODES.put("FRNGBNFTS", new BigDecimal("9"));
        W2_CODES.put("OTHER", new BigDecimal("3"));
        W2_CODES.put("OTHMVEXP", new BigDecimal("10"));
        W2_CODES.put("TTT1", new BigDecimal("54"));
        W2_CODES.put("TTT2", new BigDecimal("55"));
        W2_CODES.put("TTT4", new BigDecimal("57"));
        W2_CODES.put("TTT5", new BigDecimal("58"));
        W2_CODES.put("LTAX1", new BigDecimal("17"));
        W2_CODES.put("LTAX2", new BigDecimal("19"));
        W2_CODES.put("SECLOCAL", new BigDecimal("55"));
        W2_CODES.put("TTT11", new BigDecimal("64"));
        W2_CODES.put("TTT6", new BigDecimal("59"));
        W2_CODES.put("TTT9", new BigDecimal("62"));
        W2_CODES.put("TIPS", new BigDecimal("4"));
        W2_CODES.put("TTT10", new BigDecimal("63"));
        W2_CODES.put("TTT17", new BigDecimal("70"));
        W2_CODES.put("TTT19", new BigDecimal("72"));
    }

    public static final String TEST_COMPANY_ID = "158906";
    public static final String TEST_EMPLOYEE_ID = "First_4 M_4 Last_4";





    public static void testHappyPath() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts = initializeLawAmounts(i);
            // Create Law 177 with 4 source ids
            DataLoadServices.addCompanyLaws_177(company, "1771", "1772", "1773", "1774");
            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyReg = String.valueOf(companyPayrollItemSourceId);

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "134");
            lawIds.add(companyLaw.getSourceId());
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
            lawIds.add(companyLaw.getSourceId());
            companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyVac = String.valueOf(companyPayrollItemSourceId);

            createPayrollItems(company, PayrollItemType.Deduction);
            createPayrollItems(company, PayrollItemType.EmployerContribution);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2012-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                // Create amounts for law 177

                HashMap<String,String> law177Amounts =  initializeLaw177Amounts(i);
                for (String sourceLawId : law177Amounts.keySet()) {
                    BigDecimal amount = new BigDecimal(law177Amounts.get(sourceLawId));
                    LiabilityTransactionDTO liabilityTransactionDTO = new LiabilityTransactionDTO();
                    liabilityTransactionDTO.setLiabilityTaxableWages(amount.multiply(new BigDecimal("10")));
                    liabilityTransactionDTO.setLiabilityTotalWages(amount.multiply(new BigDecimal("10")));
                    liabilityTransactionDTO.setLiabilityTipsTaxableWages(amount.multiply(new BigDecimal("10")));
                    liabilityTransactionDTO.setLiabilityAmount(new BigDecimal(law177Amounts.get(sourceLawId)));
                    liabilityTransactionDTO.setLawId("177");
                    liabilityTransactionDTO.setPayrollItemId(sourceLawId);
                    paycheckDTO.getLiabilityTransactions().add(liabilityTransactionDTO);
                }

                for (String stateLawId : stateLawIds) {
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                    EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                    employerContributionTransactionDTO.setContributionAmount(new BigDecimal(i));
                    employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                    employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                    employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                    deductionTransactionDTO.setDeductionAmount(new BigDecimal(i));
                    deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));

                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                    if ((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                        deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                        employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                        deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                        employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder((long) k);

                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                    paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                    paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);

                    employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
                    employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
                    employerContributionTransactionDTO.setContributionAmount(W2_CODES.get("TTT1"));
                    employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                    employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                    employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                    deductionTransactionDTO = new DeductionTransactionDTO();
                    deductionTransactionDTO.setDeductionAmount(W2_CODES.get("TTT10"));
                    deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));
                    deductionTransactionDTO.setSourcePayrollItemId("TTT10");

                    paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                    paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
                }
                k++;
            }

            CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_" + k, new DateDTO("2011-08-14"));
            Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
            companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

            LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
            liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
            liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
            liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

            for (Employee employee : employees) {

                QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                qbdtPayrollTransactionDTO.setAmount(new SpcfMoney("27.27"));
                qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 2));
                QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                qTlDTO.setAmount(new SpcfMoney("27.27"));
                qTlDTO.setPayrollItemId(String.valueOf(companyPayrollItemSourceId));
                qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                for (String stateLawId : stateLawIds) {
                    SpcfMoney amount = new SpcfMoney(stateLawId);
                    LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, (SpcfMoney) amount.multiply(new SpcfMoney("2")), (SpcfMoney) amount.divide(new SpcfMoney("2")), false);
                    QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                    liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
                    liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);

                }
            }

            payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().add(companyAdjustmentSubmissionDTO);
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;
        }


        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[] {"-year:2012"});
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companiesToProcess = Application.find(Company.class);
        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        String value = "";
        for (Company company:companiesToProcess)  {
           value = value + company.getSourceCompanyId()+ ",";
        }
        value = value.substring(0,value.lastIndexOf(","));
        companyListParameter.setSystemParameterValue(value);
        PayrollServices.commitUnitOfWork();

    }

    public static void createPayrollItems(Company pCompany, PayrollItemType pPayrollItemType) {
        List<String> taxFormLines = new ArrayList<String>();
        PayrollItemCode payrollItemCode = null;
        switch (pPayrollItemType) {
            case Deduction:
                taxFormLines = DEDUCTION_TAX_FORM_LINES;
                payrollItemCode = PayrollItemCode.OtherPreTaxDeduction;
                break;
            case Compensation:
                taxFormLines = COMPENSATION_TAX_FORM_LINES;
                payrollItemCode = PayrollItemCode.Compensation;
                break;
            case EmployerContribution:
                taxFormLines = ER_CONTRIB_TAX_FORM_LINES;
                payrollItemCode = PayrollItemCode.OtherNonTaxableEmployerContribution;
                break;
        }
        for (String taxFormLine : taxFormLines) {
            createCompanyPayrollItem(pCompany, taxFormLine, taxFormLine, payrollItemCode);
        }
    }

    public static void createCompanyPayrollItem(Company pCompany, String pTaxFormLine, String pSourcePayrollItemId, PayrollItemCode pPayrollItemCode) {

        List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(pPayrollItemCode);
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxFormLine(pTaxFormLine);
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(pSourcePayrollItemId);
        DataLoadServices.persistPayrollItems(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyPayrollItemDTOs);
    }

    public static HashMap<String, String> initializeLawAmounts(double pMultiplier) {
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", String.valueOf(6.1 * pMultiplier));
        lawAmounts.put("62", String.valueOf(6.2 * pMultiplier));
        lawAmounts.put("63", String.valueOf(6.3 * pMultiplier));
        lawAmounts.put("64", String.valueOf(6.4 * pMultiplier));
        lawAmounts.put("1", String.valueOf(1.5 * pMultiplier));
        lawAmounts.put("134", String.valueOf(13.4 * pMultiplier)); // WY SUI-ER
        lawAmounts.put("131", String.valueOf(13.1 * pMultiplier)); // WA SUI-ER
        lawAmounts.put("130", String.valueOf(13.0 * pMultiplier)); // VT SUI-ER
        lawAmounts.put("120", String.valueOf(12 * pMultiplier));   // OR SUI-ER
        lawAmounts.put("116", String.valueOf(11.6 * pMultiplier)); // NV SUI-ER
        lawAmounts.put("87", String.valueOf(8.7 * pMultiplier));   // CA SUI-ER
        return lawAmounts;
    }

    public static HashMap<String, String> initializeLaw177Amounts(double pMultiplier) {
        HashMap<String, String> law177Amounts = new HashMap<String, String>();
        law177Amounts.put("1771", String.valueOf(17.1 * pMultiplier));
        law177Amounts.put("1772", String.valueOf(17.2 * pMultiplier));
        law177Amounts.put("1773", String.valueOf(17.3 * pMultiplier));
        law177Amounts.put("1774", String.valueOf(17.4 * pMultiplier));

        return law177Amounts;
    }
}
