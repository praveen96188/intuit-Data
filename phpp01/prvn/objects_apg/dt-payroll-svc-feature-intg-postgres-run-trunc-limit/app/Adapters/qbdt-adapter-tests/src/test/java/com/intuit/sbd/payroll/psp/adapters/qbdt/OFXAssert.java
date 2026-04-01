package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollTransaction;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

/**
 * User: rnorian
 * Date: Oct 30, 2010
 * Time: 3:38:27 PM
 */
public class OFXAssert {
    public static void assertEmployees(DomainEntitySet<Employee> pEmployees, Collection<IEMP> pOFXEmployees) {
        for (IEMP iemp : pOFXEmployees) {
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee ofxEmployee = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee(iemp);
            Employee employee = pEmployees.findEntity(Employee.SourceEmployeeId().equalTo(ofxEmployee.getSourceId()));
            assertNotNull("Employee not found with id" + ofxEmployee.getSourceId(), employee);

            assertEquals(ofxEmployee.getAddressLine1(), employee.getMailingAddress().getAddressLine1());
            assertEquals(ofxEmployee.getAddressLine2(), employee.getMailingAddress().getAddressLine2());
            assertEquals(ofxEmployee.getCity(), employee.getMailingAddress().getCity());
            assertEquals(ofxEmployee.getState(), employee.getMailingAddress().getState());
            assertEquals(ofxEmployee.getZipCode(), employee.getMailingAddress().getZipCode());

            assertEquals(ofxEmployee.getAltPhone(), employee.getQbdtEmployeeInfo().getAltPhone());
            assertEquals(ofxEmployee.getBillPayAccount(), employee.getQbdtEmployeeInfo().getBillPayAccount());
            assertEquals(ofxEmployee.getClassTracking(), employee.getQbdtEmployeeInfo().getTrackingClass());
            assertEquals(ofxEmployee.getEmployeeType(), employee.getQbdtEmployeeInfo().getEmployeeType());
            assertEquals(ofxEmployee.enforceSubjectTo(), employee.getQbdtEmployeeInfo().getEnforceSubjectTo());
            assertEquals(ofxEmployee.getInitials(), employee.getQbdtEmployeeInfo().getInitials());
            assertEquals(ofxEmployee.getPrintAsName(), employee.getQbdtEmployeeInfo().getPrintAsName());
            assertEquals(ofxEmployee.getTitle(), employee.getQbdtEmployeeInfo().getTitle());
            assertEquals(ofxEmployee.isDeceased(), employee.getQbdtEmployeeInfo().getIsDeleted());
            assertEquals(ofxEmployee.useTime(), employee.getQbdtEmployeeInfo().getUseTime());

            assertEquals(ofxEmployee.getEmail(), employee.getEmail());
            assertEquals(ofxEmployee.getFedFilingAllowances(), employee.getFedAllowances());
            assertEquals(ofxEmployee.getFedExtraWithholding(), employee.getFedExtraWithholding());
            assertEquals(ofxEmployee.getFedFilingStatus(), employee.getFedFilingStatus());
            assertEquals(ofxEmployee.getFirstName(), employee.getFirstName());
            assertEquals(ofxEmployee.getGender(), employee.getGenderCd());
            assertEquals(ofxEmployee.getHasRetirementPlan(), employee.getHasRetirementPlan());
            assertEquals(ofxEmployee.isDeceased(), employee.getIsDeceased());
            assertEquals(ofxEmployee.getLastName(), employee.getLastName());
            assertEquals(ofxEmployee.getLiveState(), employee.getLiveState());
            assertEquals(ofxEmployee.getMiddleInitial(), employee.getMiddleName());
            assertEquals(ofxEmployee.getPayPeriod(), employee.getPayPeriod());
            assertEquals(ofxEmployee.getPhone(), employee.getPhone());
            assertEquals(ofxEmployee.qualifiesForAEIC(), employee.getQualifiesForAeic());
            assertEquals(ofxEmployee.getEmployeeStatus(), employee.getStatusCd());
            assertEquals(ofxEmployee.getSSN(), employee.getTaxId());
            assertEquals(ofxEmployee.getWorkState(), employee.getWorkState());
            if(ofxEmployee.getHireDate() != null && employee.getHireDate() != null) {
                assertEquals(ofxEmployee.getHireDate().getTime(), employee.getHireDate().getTimeInMilliseconds());
            } else {
                assertNull(ofxEmployee.getHireDate());
                assertNull(employee.getHireDate());
            }
            if(ofxEmployee.getReleaseDate() != null && employee.getTerminationDate() != null) {
                assertEquals(ofxEmployee.getReleaseDate().getTime(), employee.getTerminationDate().getTimeInMilliseconds());
            } else {
                assertNull(ofxEmployee.getReleaseDate());
                assertNull(employee.getTerminationDate());
            }

            assertEquals(ofxEmployee.getEmployeeWagePlans().size(), employee.getEmployeeWagePlanCollection().size());
            // there is no significant order that can be relied on unless there is only one
            if(employee.getEmployeeWagePlanCollection().size() == 1) {
                com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee.EmployeeWagePlan ofxWagePlan = ofxEmployee.getEmployeeWagePlans().get(0);
                EmployeeWagePlan wagePlan = employee.getEmployeeWagePlanCollection().get(0);
                assertEquals(ofxWagePlan.getDescription(), wagePlan.getDescription());
                assertEquals(ofxWagePlan.getState(), wagePlan.getState());
                assertEquals(QBOFX.mapOFXWagePlanDomainCode(ofxWagePlan.getDomain()), wagePlan.getWagePlanDomain());
                assertEquals(QBOFX.mapOFXWagePlanNameCode(ofxWagePlan.getName()), wagePlan.getName());
                assertEquals(ofxWagePlan.getRulesVersion(), wagePlan.getRulesVersion());
                assertEquals(ofxWagePlan.getValue(), wagePlan.getWagePlanValue());
            }

            assertEquals(ofxEmployee.getWages().size() + ofxEmployee.getAdjustments().size(), employee.getEmployeePayrollItemCollection().size());
            for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee.EmployeePayrollItem ofxEmployeePayrollItem : ofxEmployee.getWages()) {
                EmployeePayrollItem employeePayrollItem =
                        employee.getEmployeePayrollItemCollection().findEntity(EmployeePayrollItem.CompanyPayrollItem().SourcePayrollItemId().equalTo(ofxEmployeePayrollItem.getPayrollItemId()));
                assertNotNull("Employee payroll item with id: " + ofxEmployeePayrollItem.getPayrollItemId() + " does not exist" + employeePayrollItem);

                assertEquals(ofxEmployeePayrollItem.getAmount(), employeePayrollItem.getAmount());
                assertEquals(ofxEmployeePayrollItem.getAmountType(), employeePayrollItem.getAmountType());
                assertEquals(ofxEmployeePayrollItem.getItemLimit(), employeePayrollItem.getItemLimit());
                assertEquals(ofxEmployeePayrollItem.getLimitType(), employeePayrollItem.getLimitType());
                assertEquals(PaylineType.Wage, employeePayrollItem.getType());
            }
            for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee.EmployeePayrollItem ofxEmployeePayrollItem : ofxEmployee.getAdjustments()) {
                EmployeePayrollItem employeePayrollItem =
                        employee.getEmployeePayrollItemCollection().findEntity(EmployeePayrollItem.CompanyPayrollItem().SourcePayrollItemId().equalTo(ofxEmployeePayrollItem.getPayrollItemId()));
                assertNotNull("Employee payroll item with id: " + ofxEmployeePayrollItem.getPayrollItemId() + " does not exist", employeePayrollItem);

                assertEquals(ofxEmployeePayrollItem.getAmount(), employeePayrollItem.getAmount());
                assertEquals(ofxEmployeePayrollItem.getAmountType(), employeePayrollItem.getAmountType());
                assertEquals(ofxEmployeePayrollItem.getItemLimit(), employeePayrollItem.getItemLimit());
                assertEquals(ofxEmployeePayrollItem.getLimitType(), employeePayrollItem.getLimitType());
                assertEquals(PaylineType.Adjustment, employeePayrollItem.getType());
            }

            assertEquals(ofxEmployee.getCustomFields().size(), employee.getEmployeeCustomFieldCollection().size());
            // there is no significant order that can be relied on unless there is only one
            if(employee.getEmployeeCustomFieldCollection().size() == 1) {
                for (String name : ofxEmployee.getCustomFields().keySet()) {
                    EmployeeCustomField employeeCustomField = employee.getEmployeeCustomFieldCollection().get(0);
                    assertEquals(name, employeeCustomField.getName());
                    assertEquals(ofxEmployee.getCustomFields().get(name), employeeCustomField.getValue());
                }
            }

            int accrualsCount = (ofxEmployee.getSickAccrual() != null ? 1 : 0) + (ofxEmployee.getVacationAccrual() != null ? 1 : 0);
             assertEquals(accrualsCount, employee.getEmployeeAccrualCollection().size());
            for (EmployeeAccrual employeeAccrual : employee.getEmployeeAccrualCollection()) {
                assertNotNull(employeeAccrual.getAccrualType());
                com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee.EmployeeAccrual ofxEmployeeAccrual = null;
                if(employeeAccrual.getAccrualType().equals(AccrualType.Sick)) {
                    ofxEmployeeAccrual = ofxEmployee.getSickAccrual();
                } else if(employeeAccrual.getAccrualType().equals(AccrualType.Vacation)) {
                    ofxEmployeeAccrual = ofxEmployee.getVacationAccrual();
                }
                if(ofxEmployeeAccrual != null) {
                    assertEquals(ofxEmployeeAccrual.getAccrualPeriod(), employeeAccrual.getAccrualPeriod());
                    assertEquals(ofxEmployeeAccrual.getAccrualType(), employeeAccrual.getAccrualType());
                    assertEquals(ofxEmployeeAccrual.getHoursPerPeriod(), employeeAccrual.getHoursPerPeriod());
                    assertEquals(ofxEmployeeAccrual.getMaxHours(), employeeAccrual.getMaxHours());
                    assertEquals(ofxEmployeeAccrual.getHours(), employeeAccrual.getHours());
                    assertEquals(ofxEmployeeAccrual.isNewYearReset(), employeeAccrual.getNewYearReset());
                }
            }

            assertEquals(ofxEmployee.getEmployeeTaxes().size(), employee.getEmployeeTaxCollection().size());
            for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee.EmployeeTax ofxEmployeeTax : ofxEmployee.getEmployeeTaxes()) {
                EmployeeTax employeeTax = employee.getEmployeeTaxCollection().findEntity(EmployeeTax.TaxType().equalTo(ofxEmployeeTax.getTaxType()));
                assertNotNull("Employee tax with type: " + ofxEmployeeTax.getTaxType() + " does not exist", employeeTax);
                assertEquals(ofxEmployeeTax.getState(), employeeTax.getState());
                assertEquals(ofxEmployeeTax.getTaxLawVersion(), employeeTax.getTaxLawVersion());
                if(ofxEmployeeTax.getCompanyLawId() != null || employeeTax.getCompanyLaw() != null) {
                    assertEquals(ofxEmployeeTax.getCompanyLawId(), employeeTax.getCompanyLaw().getSourceId());
                }
                assertEquals(ofxEmployeeTax.getW2Name(), employeeTax.getW2Name());
                assertEquals(ofxEmployeeTax.getMiscData().size(), employeeTax.getTaxTableMiscDataCollection().size());
                if(ofxEmployeeTax.getTaxType() == EmployeeTaxType.SIT) {
                    assertEquals(ofxEmployee.getStateExtraWithholding(), employeeTax.getExtraWithholding());
                    assertEquals(ofxEmployee.getStateExtraWithholdingType(), employeeTax.getExtraWithholdingType());
                    assertEquals(ofxEmployee.getStateFilingAllowances(), employeeTax.getAllowances());
                    assertEquals(ofxEmployee.getStateFilingStatus(), employeeTax.getFilingStatus());
                }
            }

            assertEquals(ofxEmployee.useDD(), employee.getQbdtEmployeeInfo().getUseDD());
            assertEquals(ofxEmployee.getEmployeeBankAccounts().size(), employee.getEmployeeBankAccountCollection().size());
            for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee.EmployeeBankAccount employeeBankAccountWrapper : ofxEmployee.getEmployeeBankAccounts()) {
                boolean found = false;
                for (EmployeeBankAccount employeeBankAccount : employee.getEmployeeBankAccountCollection()) {
                    if(ObjectUtils.equals(employeeBankAccountWrapper.getAccountNumber(), employeeBankAccount.getBankAccount().getAccountNumber()) &&
                            ObjectUtils.equals(employeeBankAccountWrapper.getAccountType(), employeeBankAccount.getBankAccount().getAccountTypeCd()) &&
                            ObjectUtils.equals(employeeBankAccountWrapper.getAmountType(), employeeBankAccount.getAmountType()) &&
                            ObjectUtils.equals(employeeBankAccountWrapper.getAmount(), employeeBankAccount.getAmount()) &&
                            ObjectUtils.equals(employeeBankAccountWrapper.getBankName(), employeeBankAccount.getBankAccount().getBankName()) &&
                            ObjectUtils.equals(employeeBankAccountWrapper.getRoutingNumber(), employeeBankAccount.getBankAccount().getRoutingNumber())) {
                        found = true;
                    }
                }

                if(!found) {
                    fail("employee bank account not found" + employeeBankAccountWrapper.toString());
                }
            }
        }
    }

    public static void assertPayrollTransactions(List<IPAYROLLTX> pPayrollTransaction, Company pCompany) {
        assertPayrollTransactions(pPayrollTransaction, pCompany, false);
    }

    public static void assertPayrollTransactions(List<IPAYROLLTX> pPayrollTransaction, Company pCompany, boolean pIsBalanceFile) {
        assertPayrollTransactions(pPayrollTransaction, pCompany, pIsBalanceFile, false);
    }

    public static void assertPayrollTransactions(List<IPAYROLLTX> pPayrollTransaction, Company pCompany, boolean pIsBalanceFile, boolean pCheckTokens) {
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        for (IPAYROLLTX ipayrolltx : pPayrollTransaction) {
            PayrollTransaction payrollTransaction = new PayrollTransaction(ipayrolltx);
            switch (payrollTransaction.getTransactionType()) {
                case PriorPayment:
                case Refund:
                    assertPriorPaymentOrRefund(payrollTransaction, pCompany, pCheckTokens);
                    break;
                case EmployeeLiabilityAdjustment:
                case CompanyLiabilityAdjustment:
                    assertLiabilityAdjustment(payrollTransaction, pCompany, pCheckTokens);
                    break;
                case LiabilityCheck:
                    if(pIsBalanceFile) {
                        assertPriorPaymentOrRefund(payrollTransaction, pCompany, pCheckTokens);
                    } else {
                        assertLiabilityCheck(payrollTransaction, pCompany, pCheckTokens);
                    }
                    break;
                case DirectDepositReturn:
                case FundsTransfer:
                    assertQBDTPayrollTransactionInfo(payrollTransaction, pCompany, pCheckTokens, payrollTransaction.getTransactionType());
                default:
                    fail("Unsupported type " + payrollTransaction.getTransactionType());

            }
        }
    }

    private static void assertPriorPaymentOrRefund(PayrollTransaction pPayrollTransaction, Company pCompany, boolean pCheckTokens) {
        DomainEntitySet<PriorPaymentSubmission> priorPaymentSubmissions = Application.find(PriorPaymentSubmission.class, PriorPaymentSubmission.Company().equalTo(pCompany)
                .And(PriorPaymentSubmission.SourceId().equalTo(pPayrollTransaction.getSourceId())));
        assertEquals("Prior payment with source id:" + pPayrollTransaction.getSourceId() + " does not exist", 1, priorPaymentSubmissions.size());
        PriorPaymentSubmission priorPaymentOrRefund = priorPaymentSubmissions.get(0);
        boolean isPriorPayment = pPayrollTransaction.getTransactionType() == PayrollTransaction.TransactionType.PriorPayment || pPayrollTransaction.getTransactionType() == PayrollTransaction.TransactionType.LiabilityCheck;

        SpcfDecimal total = SpcfMoney.ZERO;
        int totalLines = 0;
        for (QbdtTransactionInfo qbdtTransactionInfo : priorPaymentOrRefund.getQbdtTransactionInfoCollection()) {
            assertNotNull(qbdtTransactionInfo);
            assertQBDTTransactionInfo(pPayrollTransaction, pCompany, pCheckTokens, qbdtTransactionInfo);

            MoneyMovementTransaction moneyMovementTransaction = qbdtTransactionInfo.getMoneyMovementTransaction();
            total = total.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());

            if(pPayrollTransaction.getIsVoided()) {
                assertEquals(ManualPaymentStatus.Voided, moneyMovementTransaction.getManualPaymentStatus());
            }

            if(isPriorPayment) {
                assertEquals(PaymentMethod.HPDE, moneyMovementTransaction.getMoneyMovementPaymentMethod());
            } else {
                assertEquals(PaymentMethod.HPDERefund, moneyMovementTransaction.getMoneyMovementPaymentMethod());
            }

            DomainEntitySet<FinancialTransaction> transactionLines = moneyMovementTransaction.getFinancialTransactionCollection();
            totalLines += transactionLines.size();

            for (PayrollTransaction.TransactionLine transactionLine : pPayrollTransaction.getTransactionLines()) {
                CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(pCompany, transactionLine.getPayrollItemId());

                if (companyLaw == null || !companyLaw.getLaw().getPaymentTemplate().equals(moneyMovementTransaction.getPaymentTemplate())) {
                    continue;
                }

                FinancialTransaction domainTransactionLine = transactionLines.findEntity(FinancialTransaction.CompanyLaw().SourceId().equalTo(transactionLine.getPayrollItemId()));
                assertNotNull("Transaction line with payroll item id:" + transactionLine.getPayrollItemId() + " does not exist", domainTransactionLine);
                assertEquals(transactionLine.getAmount(), isPriorPayment ? domainTransactionLine.getFinancialTransactionAmount() : domainTransactionLine.getFinancialTransactionAmount().negate());

                assertNotNull(domainTransactionLine.getQbdtTransactionInfo());
                QbdtTransactionInfo txnQbdtTransactionInfo = domainTransactionLine.getQbdtTransactionInfo();
                assertQBDTTransactionLineInfo(transactionLine, txnQbdtTransactionInfo);
            }
        }

        if(priorPaymentOrRefund.getQbdtPayrollTransaction() != null) {
            QbdtPayrollTransaction qbdtPayrollTransaction = priorPaymentOrRefund.getQbdtPayrollTransaction();
            QbdtPayrollTransactionType qbdtPayrollTransactionType;
            if(pPayrollTransaction.getTransactionType() == PayrollTransaction.TransactionType.LiabilityCheck) {
                qbdtPayrollTransactionType = QbdtPayrollTransactionType.LiabilityCheck;
            } else {
                qbdtPayrollTransactionType = isPriorPayment ? QbdtPayrollTransactionType.PriorPayment : QbdtPayrollTransactionType.Refund;
            }

            totalLines += qbdtPayrollTransaction.getQbdtPayrollTransactionLineCollection().size();
            assertQBDTPayrollTransactionInfo(pPayrollTransaction, pCompany, pCheckTokens, qbdtPayrollTransaction, qbdtPayrollTransactionType);
        } else {
            if(isPriorPayment) {
                assertEquals(pPayrollTransaction.getTotalAmount(), total);
            } else {
                assertEquals(pPayrollTransaction.getTotalAmount(), total.negate());
            }
        }

        assertEquals("Transaction line count", pPayrollTransaction.getTransactionLines().size(), totalLines);
    }

    private static void assertQBDTPayrollTransactionInfo(PayrollTransaction pPayrollTransaction, Company pCompany, boolean pCheckTokens, PayrollTransaction.TransactionType pTransactionType) {
         DomainEntitySet<QbdtPayrollTransaction> qbdtPayrollTransactions = Application.find(QbdtPayrollTransaction.class, QbdtPayrollTransaction.Company().equalTo(pCompany)
                .And(QbdtPayrollTransaction.SourceId().equalTo(pPayrollTransaction.getSourceId())));
        assertEquals("Payroll transaction with source id:" + pPayrollTransaction.getSourceId() + " does not exist", 1, qbdtPayrollTransactions.size());

        QbdtPayrollTransactionType qbdtPayrollTransactionType = null;
        if(pTransactionType == PayrollTransaction.TransactionType.DirectDepositReturn) {
            qbdtPayrollTransactionType = QbdtPayrollTransactionType.DDReturn;
        } else if(pTransactionType == PayrollTransaction.TransactionType.FundsTransfer) {
            qbdtPayrollTransactionType = QbdtPayrollTransactionType.FundsTransfer;
        }

        assertQBDTPayrollTransactionInfo(pPayrollTransaction, pCompany, pCheckTokens, qbdtPayrollTransactions.get(0), qbdtPayrollTransactionType);
    }

    private static void assertQBDTPayrollTransactionInfo(PayrollTransaction pPayrollTransaction, Company pCompany, boolean pCheckTokens, QbdtPayrollTransaction pQbdtPayrollTransaction, QbdtPayrollTransactionType pQbdtPayrollTransactionType) {
        assertNotNull("company", pQbdtPayrollTransaction.getCompany());
        assertEquals(pPayrollTransaction.getTotalAmount(), pQbdtPayrollTransaction.getAmount());
        assertEquals(pPayrollTransaction.getIsVoided(), pQbdtPayrollTransaction.getIsVoided());
        assertEquals(pPayrollTransaction.getPeriodEndDate(), pQbdtPayrollTransaction.getPeriodEndDate());
        assertEquals(pPayrollTransaction.getSourceId(), pQbdtPayrollTransaction.getSourceId());
        assertEquals(SpcfCalendar.createInstance(pPayrollTransaction.getTransactionDate().getTime()), pQbdtPayrollTransaction.getTransactionDate());
        assertEquals(pQbdtPayrollTransactionType, pQbdtPayrollTransaction.getTransactionType());

        QbdtTransactionInfo qbdtTransactionInfo = pQbdtPayrollTransaction.getQbdtTransactionInfo();
        assertNotNull(qbdtTransactionInfo);
        assertQBDTTransactionInfo(pPayrollTransaction, pCompany, pCheckTokens, qbdtTransactionInfo);

        for (PayrollTransaction.TransactionLine transactionLine : pPayrollTransaction.getTransactionLines()) {
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, transactionLine.getPayrollItemId());

            if (companyPayrollItem == null) {
                continue;
            }

            QbdtPayrollTransactionLine domainTransactionLine =
                    pQbdtPayrollTransaction.getQbdtPayrollTransactionLineCollection().findEntity(QbdtPayrollTransactionLine.CompanyPayrollItem().SourcePayrollItemId().equalTo(transactionLine.getPayrollItemId()));
            assertNotNull("Transaction line with payroll item id:" + transactionLine.getPayrollItemId() + " does not exist", domainTransactionLine);
            assertEquals(transactionLine.getAmount(), domainTransactionLine.getAmount());

            assertNotNull(domainTransactionLine.getQbdtTransactionInfo());
            QbdtTransactionInfo txnQbdtTransactionInfo = domainTransactionLine.getQbdtTransactionInfo();
            assertQBDTTransactionLineInfo(transactionLine, txnQbdtTransactionInfo);
        }
    }

    private static void assertQBDTTransactionLineInfo(PayrollTransaction.TransactionLine transactionLine, QbdtTransactionInfo pTxnQbdtTransactionInfo) {
        assertEquals(transactionLine.getAccountName(), pTxnQbdtTransactionInfo.getAccountName());
        assertEquals(transactionLine.getMemo(), pTxnQbdtTransactionInfo.getMemo());
        assertEquals(transactionLine.getTrackingClass(), pTxnQbdtTransactionInfo.getTrackingClass());
    }

    private static void assertLiabilityCheck(PayrollTransaction pPayrollTransaction, Company pCompany, boolean pCheckTokens) {
        DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class,
                                                                           LiabilityCheck.Company().equalTo(pCompany)
                                                                                   .And(LiabilityCheck.SourceId().equalTo(pPayrollTransaction.getSourceId())));
        assertEquals("Liability check with source id:" + pPayrollTransaction.getSourceId() + " does not exist", 1, liabilityChecks.size());
        LiabilityCheck liabilityCheck = liabilityChecks.get(0);
        assertEquals("source id", pPayrollTransaction.getSourceId(), liabilityCheck.getSourceId());
        assertEquals("amount", pPayrollTransaction.getTotalAmount(), liabilityCheck.getAmount());
        assertEquals("void", pPayrollTransaction.getIsVoided(), liabilityCheck.getIsVoid());
        assertEquals("period end date", pPayrollTransaction.getPeriodEndDate(), liabilityCheck.getPeriodEndDate());
        assertEquals("transaction date", pPayrollTransaction.getTransactionDate().getTime(), liabilityCheck.getTransactionDate().getTimeInMilliseconds());
        assertNull("liability check type", liabilityCheck.getType());
        assertNotNull("qbdt transaction info", liabilityCheck.getQbdtTransactionInfo());
        QbdtTransactionInfo qbdtTransactionInfo = liabilityCheck.getQbdtTransactionInfo();
        assertQBDTTransactionInfo(pPayrollTransaction, pCompany, pCheckTokens, qbdtTransactionInfo);

        for (PayrollTransaction.TransactionLine transactionLine : pPayrollTransaction.getTransactionLines()) {
            String sourceId = transactionLine.getPayrollItemId();
            if(sourceId != null) {
                DomainEntitySet<LiabilityCheckLine> liabilityCheckLines =
                        liabilityCheck.getLiabilityCheckLineCollection().find(LiabilityCheckLine.CompanyLaw().SourceId().equalTo(sourceId)
                        .Or(LiabilityCheckLine.CompanyPayrollItem().SourcePayrollItemId().equalTo(sourceId)));
                assertEquals("check line", 1, liabilityCheckLines.size());
                LiabilityCheckLine liabilityCheckLine = liabilityCheckLines.get(0);
                assertEquals("amount", transactionLine.getAmount(), liabilityCheckLine.getAmount());
                QbdtTransactionInfo lineQbdtTransactionInfo = liabilityCheckLine.getQbdtTransactionInfo();
                assertNotNull(lineQbdtTransactionInfo);
                assertEquals("account name", transactionLine.getAccountName(), lineQbdtTransactionInfo.getAccountName());
                assertEquals("tracking class", transactionLine.getTrackingClass(), lineQbdtTransactionInfo.getTrackingClass());
                assertEquals("account name", transactionLine.getMemo(), lineQbdtTransactionInfo.getMemo());
                assertFalse("system generated", lineQbdtTransactionInfo.getSystemGenerated());
            } else {
                DomainEntitySet<LiabilityCheckLine> liabilityCheckLines =
                        liabilityCheck.getLiabilityCheckLineCollection().find(LiabilityCheckLine.QbdtTransactionInfo().Memo().equalTo(transactionLine.getMemo()));
                assertEquals("check line", 1, liabilityCheckLines.size());
                LiabilityCheckLine liabilityCheckLine = liabilityCheckLines.get(0);
                assertEquals("amount", transactionLine.getAmount(), liabilityCheckLine.getAmount());
                QbdtTransactionInfo lineQbdtTransactionInfo = liabilityCheckLine.getQbdtTransactionInfo();
                assertNotNull(lineQbdtTransactionInfo);
                assertEquals("account name", transactionLine.getAccountName(), lineQbdtTransactionInfo.getAccountName());
                assertEquals("tracking class", transactionLine.getTrackingClass(), lineQbdtTransactionInfo.getTrackingClass());
                assertEquals("account name", transactionLine.getMemo(), lineQbdtTransactionInfo.getMemo());
                assertFalse("system generated", lineQbdtTransactionInfo.getSystemGenerated());
            }
        }
    }

    public static void assertLiabilityAdjustment(PayrollTransaction pPayrollTransaction, Company pCompany, boolean pCheckTokens) {
        DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class,
                                                                                                     CompanyAdjustmentSubmission.Company().equalTo(pCompany)
                                                                                                             .And(CompanyAdjustmentSubmission.SourceId().equalTo(pPayrollTransaction.getSourceId())));
        assertEquals("Liability adjustment with source id:" + pPayrollTransaction.getSourceId() + " does not exist", 1, companyAdjustmentSubmissions.size());
        CompanyAdjustmentSubmission liabilityAdjustmentSubmission = companyAdjustmentSubmissions.get(0);

        assertEquals(pPayrollTransaction.getTotalAmount(), liabilityAdjustmentSubmission.getAmount());

        assertNotNull(liabilityAdjustmentSubmission.getQbdtTransactionInfo());
        QbdtTransactionInfo qbdtTransactionInfo = liabilityAdjustmentSubmission.getQbdtTransactionInfo();
        assertQBDTTransactionInfo(pPayrollTransaction, pCompany, pCheckTokens, qbdtTransactionInfo);

        int transactionLinesCount = 0;
        if(liabilityAdjustmentSubmission.getQbdtPayrollTransaction() != null) {
            assertQBDTPayrollTransactionInfo(pPayrollTransaction, pCompany, pCheckTokens, liabilityAdjustmentSubmission.getQbdtPayrollTransaction(), QbdtPayrollTransactionType.LiabilityAdjustment);
            transactionLinesCount += liabilityAdjustmentSubmission.getQbdtPayrollTransaction().getQbdtPayrollTransactionLineCollection().size();
        }

        DomainEntitySet<LiabilityAdjustment> transactionLines = liabilityAdjustmentSubmission.getLiabilityAdjustmentCollection();
        for (CompanyAdjustmentSubmission companyAdjustmentSubmission : liabilityAdjustmentSubmission.getAssociatedSubmissionCollection()) {
            transactionLines.addAll(companyAdjustmentSubmission.getLiabilityAdjustmentCollection());
        }

        for (PayrollTransaction.TransactionLine transactionLine : pPayrollTransaction.getTransactionLines()) {
            if(transactionLine.getPayrollItemId() != null) {
                CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(pCompany, transactionLine.getPayrollItemId());
                if(companyLaw != null) {
                    DomainEntitySet<LiabilityAdjustment> domainTransactionLines = transactionLines.find(LiabilityAdjustment.CompanyLaw().SourceId().equalTo(transactionLine.getPayrollItemId()));
                    assertTrue("Transaction line with payroll item id: " + transactionLine.getPayrollItemId() + " does not exist", domainTransactionLines.size() > 0);
                    transactionLinesCount++;

                    SpcfDecimal amount;
                    SpcfDecimal totalWages;
                    SpcfDecimal taxableWages;
                    if(liabilityAdjustmentSubmission.isVoid()) {
                        amount = SpcfDecimal.createInstance(0);
                        totalWages = SpcfDecimal.createInstance(0);
                        taxableWages = SpcfDecimal.createInstance(0);
                    } else {
                        amount = transactionLine.getAmount();
                        totalWages = transactionLine.getTotalWages();
                        taxableWages = transactionLine.getTaxableWages();
                    }
                    for (LiabilityAdjustment domainTransactionLine : domainTransactionLines) {
                        // ee taxes are negated when saved
                        if(domainTransactionLine.getCompanyLaw().getQbdtPayrollItemInfo().getIsEmployeePaid()) {
                            if(amount!= null && transactionLine.getAmount() != null && domainTransactionLine.getAmount() != null) {
                                amount = amount.subtract(domainTransactionLine.getAmount().negate());
                            } else {
                                assertNull(amount);
                            }
                        } else {
                            if(amount != null) {
                                amount = amount.subtract(domainTransactionLine.getAmount());
                            } else {
                                assertNull(amount);
                            }
                        }
                        if(domainTransactionLine.getTaxableWages() != null) {
                            if(taxableWages != null) {
                            taxableWages = taxableWages.subtract(domainTransactionLine.getTaxableWages());
                            } else {
                                assertNull(taxableWages);
                            }
                        }
                        if(domainTransactionLine.getTotalWages() != null) {
                            if(totalWages != null) {
                                totalWages = totalWages.subtract(domainTransactionLine.getTotalWages());
                            } else {
                                assertNull(totalWages);
                            }
                        }

                        // each adjustment should have the same information on it
                        if(pPayrollTransaction.getEmployeeId() != null) {
                            assertNotNull(domainTransactionLine.getEmployee());
                        }

                        assertNotNull(domainTransactionLine.getQbdtTransactionInfo());
                        qbdtTransactionInfo = domainTransactionLine.getQbdtTransactionInfo();
                        assertQBDTTransactionLineInfo(transactionLine, qbdtTransactionInfo);
                    }

                    // the amounts and wages should end up 0 if they all match
                    if(amount != null) {
                        assertEquals("Transaction line with payroll item id: " + transactionLine.getPayrollItemId() + " amount", SpcfMoney.ZERO, new SpcfMoney(amount));
                    }
                    if(taxableWages != null) {
                        assertEquals("Transaction line with payroll item id: " + transactionLine.getPayrollItemId() + " taxable wages", SpcfMoney.ZERO, new SpcfMoney(taxableWages));
                    }
                    if(totalWages != null) {
                        assertEquals("Transaction line with payroll item id: " + transactionLine.getPayrollItemId() + " total wages", SpcfMoney.ZERO, new SpcfMoney(totalWages));
                    }
                }
            }
        }


        assertEquals("transaction line count", pPayrollTransaction.getTransactionLines().size(), transactionLinesCount);
    }

    private static void assertQBDTTransactionInfo(PayrollTransaction pPayrollTransaction, Company pCompany, boolean pCheckTokens, QbdtTransactionInfo pQbdtTransactionInfo) {
        assertEquals(pPayrollTransaction.getAccountName(), pQbdtTransactionInfo.getAccountName());
        assertEquals(pPayrollTransaction.getAgencyName(), pQbdtTransactionInfo.getAgencyName());
        assertEquals(pPayrollTransaction.getCleared(), pQbdtTransactionInfo.getCleared());
        assertFalse(pQbdtTransactionInfo.getIsDeleted());
        assertEquals(pPayrollTransaction.getIsOnService(), pQbdtTransactionInfo.getOnService());
        assertEquals(pPayrollTransaction.getReferenceNumber(), pQbdtTransactionInfo.getReferenceNumber());
        if(pCheckTokens) {
            assertEquals(pCompany.getCurrentToken(), pQbdtTransactionInfo.getToken());
        }
    }

    public static void assertPayrolls(List<IPAYROLLRUN> pOFXPayrollRuns, Company pCompany) {
        assertPayrolls(pOFXPayrollRuns, pCompany, null);
    }

    public static void assertPayrolls(List<IPAYROLLRUN> pOFXPayrollRuns, Company pCompany, Long pPayrollToken) {
        for (IPAYROLLRUN ipayrollrun : pOFXPayrollRuns) {
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollRun ofxPayrollRun = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollRun(ipayrollrun);

            Collection<Paycheck> ofxPaychecks = ofxPayrollRun.getNewPaychecks();
            if(ofxPayrollRun.hasNewPaychecks()) {
                if (ofxPaychecks.size() > 0) {

                    DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Paycheck> paychecks = null;
                    for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck ofxPaycheck : ofxPaychecks) {
                        com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(pCompany, ofxPaycheck.getSourceId());
                        assertNotNull("Paycheck with source id " + ofxPaycheck.getSourceId() + "does not exist", paycheck);
                        if(pPayrollToken != null) {
                            assertEquals(pPayrollToken, new Long(paycheck.getQbdtPaycheckInfo().getToken()));
                        }

                        if(paychecks == null) {
                            PayrollRun payrollRun = paycheck.getPayrollRun();
                            Assert.assertEquals(ofxPayrollRun.getPaycheckDate().getTime(), payrollRun.getPaycheckDate().getTimeInMilliseconds());
                            paychecks = payrollRun.getPaycheckCollection();
                            Assert.assertEquals("Paycheck count", ofxPaychecks.size(), paychecks.size());
                        }

                        Assert.assertEquals(ofxPaycheck.isYTDAdjustment(), paycheck.getIsYTDAdjustment());
                        Assert.assertEquals(ofxPaycheck.getNetAmount(), paycheck.getNetAmount().negate());
                        Assert.assertEquals(ofxPaycheck.getPeriodBeginDate().getTime(), paycheck.getPayPeriodBeginDate().getTimeInMilliseconds());
                        Assert.assertEquals(ofxPaycheck.getPeriodEndDate().getTime(), paycheck.getPayPeriodEndDate().getTimeInMilliseconds());
                        Assert.assertEquals(ofxPaycheck.getSourceEmployeeId(), paycheck.getSourceEmployee().getSourceEmployeeId());
                        Assert.assertEquals(ofxPaycheck.isVoid(), paycheck.isVoidedOrRecalled());

                        Assert.assertEquals(ofxPaycheck.getAccountName(), paycheck.getQbdtPaycheckInfo().getAccountName());
                        Assert.assertEquals(ofxPaycheck.getCheckNumber(), paycheck.getQbdtPaycheckInfo().getCheckNumber());
                        Assert.assertEquals(ofxPaycheck.getCleared(), paycheck.getQbdtPaycheckInfo().getCleared());
                        Assert.assertEquals(ofxPaycheck.getMemo(), paycheck.getQbdtPaycheckInfo().getMemo());
                        Assert.assertEquals(ofxPaycheck.isOnService() == null ? false : ofxPaycheck.isOnService(), paycheck.getQbdtPaycheckInfo().getOnService());
                        Assert.assertEquals(ofxPaycheck.getProrate(), paycheck.getQbdtPaycheckInfo().getProrate());
                        Assert.assertEquals(ofxPaycheck.getTrackingClass(), paycheck.getQbdtPaycheckInfo().getTrackingClass());
                        Assert.assertEquals(new Double(ofxPaycheck.getSickHoursAccrued()), new Double(paycheck.getQbdtPaycheckInfo().getSickHoursAccrued()));
                        Assert.assertEquals(new Double(ofxPaycheck.getVacationHoursAccrued()), new Double(paycheck.getQbdtPaycheckInfo().getVacationHoursAccrued()));

                        Collection<com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment> compensationAdjustments = new ArrayList<Paycheck.Adjustment>();
                        Collection<com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment> deductionAdjustments = new ArrayList<com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment>();
                        Collection<com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment> companyContributionAdjustments = new ArrayList<com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment>();
                        for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment adjustment : ofxPaycheck.getAdjustments()) {
                            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, adjustment.getPayrollItemId());
                            switch (companyPayrollItem.getPayrollItem().getPayrollItemType()) {
                                case Compensation:
                                    compensationAdjustments.add(adjustment);
                                    break;
                                case Deduction:
                                    deductionAdjustments.add(adjustment);
                                    break;
                                case EmployerContribution:
                                    companyContributionAdjustments.add(adjustment);
                                    break;
                            }
                        }

                        DomainEntitySet<Compensation> compensations = paycheck.getCompensationCollection();
                        Assert.assertEquals(ofxPaycheck.getHourlyWages().size() + ofxPaycheck.getSalaryWages().size() + compensationAdjustments.size(), paycheck.getCompensationCollection().size());
                        for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment compensationAdjustment : compensationAdjustments) {
                            DomainEntitySet<Compensation> filteredCompensations = compensations.find(Compensation.CompanyPayrollItem().SourcePayrollItemId().equalTo(compensationAdjustment.getPayrollItemId()));
                            assertTrue("Compensation does not exist", filteredCompensations.size() > 0);
                            boolean matchFound = false;
                            for (Compensation compensation : filteredCompensations) {
                                if(matchFound) {
                                    break;
                                }

                                if(compensationAdjustment.getAmount().compareTo(compensation.getCompensationAmount()) == 0 &&
                                        compensationAdjustment.getYTDAmount().compareTo(compensation.getCompensationYTDAmount()) == 0 &&
                                        compensationAdjustment.getQuantity() == compensation.getQbdtPaylineInfo().getQuantity() &&
                                        compensationAdjustment.getQuantityType() == compensation.getQbdtPaylineInfo().getQuantityType() &&
                                        compensationAdjustment.getRate() == compensation.getQbdtPaylineInfo().getRate() &&
                                        compensationAdjustment.getRateType() == compensation.getQbdtPaylineInfo().getRateType() &&
                                        compensationAdjustment.getPayStubOrder() == compensation.getPayStubOrder()) {
                                    matchFound = true;
                                }
                            }
                            assertTrue("Compensation does not match", matchFound);
                        }
                        for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Compensation ofxCompensation : ofxPaycheck.getHourlyWages()) {
                            DomainEntitySet<Compensation> filteredCompensations = compensations.find(Compensation.CompanyPayrollItem().SourcePayrollItemId().equalTo(ofxCompensation.getPayrollItemId()));
                            assertTrue("Compensation does not exist", filteredCompensations.size() > 0);
                            boolean matchFound = false;
                            for (Compensation compensation : filteredCompensations) {
                                if(matchFound) {
                                    break;
                                }

                                if(ofxCompensation.getAmount().compareTo(compensation.getCompensationAmount()) == 0 &&
                                        ofxCompensation.getYTDAmount().compareTo(compensation.getCompensationYTDAmount()) == 0 &&
                                        ofxCompensation.getHours() == compensation.getHoursWorked() &&
                                        ofxCompensation.getRate() == compensation.getQbdtPaylineInfo().getRate() &&
                                        ofxCompensation.getRateType() == compensation.getQbdtPaylineInfo().getRateType() &&
                                        ofxCompensation.getItem().equals(compensation.getQbdtPaylineInfo().getItem()) &&
                                        ofxCompensation.getJob().equals(compensation.getQbdtPaylineInfo().getJob()) &&
                                        ofxCompensation.getTrackingClass().equals(compensation.getQbdtPaylineInfo().getTrackingClass()) &&
                                        ofxCompensation.getWCCode().equals(compensation.getQbdtPaylineInfo().getWcCode()) &&
                                        ofxCompensation.getPayStubOrder() == compensation.getPayStubOrder()
                                ) {
                                    matchFound = true;
                                }
                            }
                            assertTrue("Compensation does not match", matchFound);
                        }
                        for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Compensation ofxCompensation : ofxPaycheck.getSalaryWages()) {
                            DomainEntitySet<Compensation> filteredCompensations = compensations.find(Compensation.CompanyPayrollItem().SourcePayrollItemId().equalTo(ofxCompensation.getPayrollItemId()));
                            assertTrue("Compensation does not exist", filteredCompensations.size() > 0);
                            boolean matchFound = false;
                            for (Compensation compensation : filteredCompensations) {
                                if(matchFound) {
                                    break;
                                }

                                if(ofxCompensation.getAmount().compareTo(compensation.getCompensationAmount()) == 0 &&
                                        ofxCompensation.getYTDAmount().compareTo(compensation.getCompensationYTDAmount()) == 0 &&
                                        ofxCompensation.getHours() == compensation.getHoursWorked() &&
                                        ofxCompensation.getRate() == compensation.getQbdtPaylineInfo().getRate() &&
                                        ofxCompensation.getRateType() == compensation.getQbdtPaylineInfo().getRateType() &&
                                        ofxCompensation.getItem().equals(compensation.getQbdtPaylineInfo().getItem()) &&
                                        ofxCompensation.getJob().equals(compensation.getQbdtPaylineInfo().getJob()) &&
                                        ofxCompensation.getTrackingClass().equals(compensation.getQbdtPaylineInfo().getTrackingClass()) &&
                                        ofxCompensation.getWCCode().equals(compensation.getQbdtPaylineInfo().getWcCode()) &&
                                        ofxCompensation.getPayStubOrder() == compensation.getPayStubOrder()
                                ) {
                                    matchFound = true;
                                }
                            }
                            assertTrue("Compensation does not match", matchFound);
                        }

                        DomainEntitySet<Deduction> deductions = paycheck.getDeductionCollection();
                        Assert.assertEquals(deductionAdjustments.size(), deductions.size());
                        for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment adjustment : deductionAdjustments) {
                            DomainEntitySet<Deduction> filteredDeductions = deductions.find(Deduction.CompanyPayrollItem().SourcePayrollItemId().equalTo(adjustment.getPayrollItemId()));
                            assertTrue("Deduction does not exist", filteredDeductions.size() > 0);
                            boolean matchFound = false;
                            for (Deduction deduction : filteredDeductions) {
                                if(matchFound) {
                                    break;
                                }

                                if(adjustment.getAmount().compareTo(deduction.getDeductionAmount().negate()) == 0 &&
                                        adjustment.getYTDAmount().compareTo(deduction.getDeductionYTDAmount().negate()) == 0 &&
                                        adjustment.getQuantity() == deduction.getQbdtPaylineInfo().getQuantity() &&
                                        adjustment.getQuantityType() == deduction.getQbdtPaylineInfo().getQuantityType() &&
                                        adjustment.getRate() == deduction.getQbdtPaylineInfo().getRate() &&
                                        adjustment.getRateType() == deduction.getQbdtPaylineInfo().getRateType() &&
                                        adjustment.getPayStubOrder() == deduction.getPayStubOrder()) {
                                    matchFound = true;
                                }
                            }
                            assertTrue("Deduction does not match", matchFound);
                        }

                        DomainEntitySet<PaycheckSplit> paycheckSplits = paycheck.getPaycheckSplitCollection().sort(PaycheckSplit.PayStubOrder());
                        Assert.assertEquals(ofxPaycheck.getDirectDeposits().size(), paycheckSplits.size());
                        for (int i = 0; i < paycheckSplits.size(); i++) {
                            PaycheckSplit paycheckSplit = paycheckSplits.get(i);
                            Paycheck.DirectDeposit directDeposit = ofxPaycheck.getDirectDeposits().get(i);
                            Assert.assertEquals(directDeposit.getAmount(), paycheckSplit.getPaycheckSplitAmount().negate());
                            BankAccount bankAccount = paycheckSplit.getEmployeeBankAccount().getBankAccount();
                            Assert.assertEquals(directDeposit.getAccountNumber(), bankAccount.getAccountNumber());
                            Assert.assertEquals(directDeposit.getAccountType(), bankAccount.getAccountTypeCd());
                            Assert.assertEquals(directDeposit.getRoutingNumber(), bankAccount.getRoutingNumber());                            
                        }

                        DomainEntitySet<EmployerContribution> employerContributions = paycheck.getEmployerContributionCollection();
                        Assert.assertEquals(companyContributionAdjustments.size(), employerContributions.size());
                        for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Adjustment adjustment : companyContributionAdjustments) {
                            DomainEntitySet<EmployerContribution> filteredEmployerContribution = employerContributions.find(EmployerContribution.CompanyPayrollItem().SourcePayrollItemId().equalTo(adjustment.getPayrollItemId()));
                            assertTrue("Employer contribution does not exist", filteredEmployerContribution.size() > 0);
                            boolean matchFound = false;
                            for (EmployerContribution employerContribution : filteredEmployerContribution) {
                                if(matchFound) {
                                    break;
                                }

                                if(adjustment.getAmount().compareTo(employerContribution.getContributionAmount()) == 0 &&
                                        adjustment.getYTDAmount().compareTo(employerContribution.getContributionYTDAmount()) == 0 &&
                                        adjustment.getQuantity() == employerContribution.getQbdtPaylineInfo().getQuantity() &&
                                        adjustment.getQuantityType() == employerContribution.getQbdtPaylineInfo().getQuantityType() &&
                                        adjustment.getRate() == employerContribution.getQbdtPaylineInfo().getRate() &&
                                        adjustment.getRateType() == employerContribution.getQbdtPaylineInfo().getRateType() &&
                                        adjustment.getPayStubOrder() == employerContribution.getPayStubOrder()) {
                                    matchFound = true;
                                }
                            }
                            assertTrue("Employer contribution does not match", matchFound);
                        }

                        DomainEntitySet<Tax> taxCollection = paycheck.getTaxCollection();
                        Assert.assertEquals(ofxPaycheck.getTaxes().size(), taxCollection.size());
                        for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.Tax ofxTax : ofxPaycheck.getTaxes()) {
                            Tax tax = taxCollection.findEntity(Tax.CompanyLaw().SourceId().equalTo(ofxTax.getPayrollItemId()));
                            assertNotNull("Tax does not exist", tax);
                            boolean isEmployeeTax = tax.getCompanyLaw().getQbdtPayrollItemInfo().getIsEmployeePaid();
                            Assert.assertEquals((isEmployeeTax ? ofxTax.getAmount().negate() : ofxTax.getAmount()), tax.getTaxLiabilityAmount());
                            Assert.assertEquals(ofxTax.getTaxableWageAmount(), tax.getTaxableWagesAmount());
                            Assert.assertEquals(ofxTax.getTipTaxableWageAmount(), tax.getTipsTaxableWageAmount());
                            Assert.assertEquals(ofxTax.getTotalWageAmount(), tax.getTotalWagesAmount());
                            Assert.assertEquals((isEmployeeTax ? ofxTax.getYTDAmount().negate() : ofxTax.getYTDAmount()), tax.getTaxLiabilityYTDAmount());
                            Assert.assertEquals(ofxTax.getPayStubOrder(), tax.getPayStubOrder());
                        }
                    }                    
                }
            } else if(ofxPayrollRun.hasPaycheckUpdates()) {
                if (ofxPaychecks.size() > 0) {

                    DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Paycheck> paychecks = null;
                    for (com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck ofxPaycheck : ofxPaychecks) {
                        com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(pCompany, ofxPaycheck.getSourceId());
                        assertNotNull("Paycheck with source id " + ofxPaycheck.getSourceId() + "does not exist", paycheck);
                        if(pPayrollToken != null) {
                            assertEquals(pPayrollToken, new Long(paycheck.getQbdtPaycheckInfo().getToken()));
                        }

                        if(paychecks == null) {
                            PayrollRun payrollRun = paycheck.getPayrollRun();
                            paychecks = payrollRun.getPaycheckCollection();
                            Assert.assertEquals("Paycheck count", ofxPaychecks.size(), paychecks.size());
                        }

                        Assert.assertEquals(ofxPaycheck.isVoid(), paycheck.isVoidedOrRecalled());

                        Assert.assertEquals(ofxPaycheck.getAccountName(), paycheck.getQbdtPaycheckInfo().getAccountName());
                        Assert.assertEquals(ofxPaycheck.getCheckNumber(), paycheck.getQbdtPaycheckInfo().getCheckNumber());
                        Assert.assertEquals(ofxPaycheck.getCleared(), paycheck.getQbdtPaycheckInfo().getCleared());
                        Assert.assertEquals(ofxPaycheck.getMemo(), paycheck.getQbdtPaycheckInfo().getMemo());
                        Assert.assertEquals(ofxPaycheck.isOnService() == null ? true : ofxPaycheck.isOnService(), paycheck.getQbdtPaycheckInfo().getOnService());
                        Assert.assertEquals(ofxPaycheck.getProrate(), paycheck.getQbdtPaycheckInfo().getProrate());
                        Assert.assertEquals(ofxPaycheck.getTrackingClass(), paycheck.getQbdtPaycheckInfo().getTrackingClass());
                    }
                }
            }
            
            // Verify Disburse Advice
            if(ipayrollrun.getIDISBURSEADVICE() != null) {
                IDISBURSEADVICE ofxDisburseAdvice = ipayrollrun.getIDISBURSEADVICE();
                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.DisburseAdvice> disburseAdviceSet =
                        Application.find(com.intuit.sbd.payroll.psp.domain.DisburseAdvice.class, com.intuit.sbd.payroll.psp.domain.DisburseAdvice.Company().equalTo(pCompany));
                Assert.assertTrue(disburseAdviceSet.size() > 0);    // Some tests submit multiple payrolls, but the Disburse Advice should be the same for all of them.

                // Verify the parent Disburse Advice record.
                com.intuit.sbd.payroll.psp.domain.DisburseAdvice disburseAdvice = disburseAdviceSet.get(0);
                Assert.assertEquals("Disburse Advice Liability Amount", QBOFX.mapOFXStringToMoney(ofxDisburseAdvice.getITAXLIABAMT()), disburseAdvice.getTaxLiabilityAmount());
                Assert.assertEquals("Disburse Advice Tax Quarter", QBOFX.mapOFXStringToInt(ofxDisburseAdvice.getITAXQTR()), disburseAdvice.getTaxQuarter());

                DomainEntitySet<DisburseAdviceTaxLiab> taxLiabilities = Application.find(DisburseAdviceTaxLiab.class, DisburseAdviceTaxLiab.DisburseAdvice().equalTo(disburseAdvice));
                Assert.assertTrue("Relative size of collections", ofxDisburseAdvice.getITAXLIAB().size() >= taxLiabilities.size());     // OFX may have duplicates.

                // Verify the child tax liability records.
                for(DisburseAdviceTaxLiab taxLiability : taxLiabilities) {
                    boolean matchFound = false;
                    
                    // If this tax liability has a tip liability child, verify the naming is correct.
                    if(taxLiability.getTipsLiability() != null) {
                        Assert.assertEquals("Correct TIP Child", taxLiability.getFedTaxDesc() + "_TIPS", taxLiability.getTipsLiability().getFedTaxDesc());
                    }

                    // Find the matching OFX liability.
                    for(ITAXLIAB ofxTaxLiab : ofxDisburseAdvice.getITAXLIAB()) {
                        if((taxLiability.getFedTaxDesc() != null && taxLiability.getFedTaxDesc().equals(ofxTaxLiab.getIFEDTAX())) ||
                           (taxLiability.getState() != null && ofxTaxLiab.getISTATETAXDESC() != null && taxLiability.getState().equals(ofxTaxLiab.getISTATETAXDESC().getISTATE())
                                   && taxLiability.getStateTaxDesc().equals(ofxTaxLiab.getISTATETAXDESC().getISTATETAX())) ||
                           (taxLiability.getOtherTaxDesc() != null && taxLiability.getOtherTaxDesc().equals(ofxTaxLiab.getIOTHERTAX()))) {

                            Assert.assertEquals("PITEM", ofxTaxLiab.getIPITEMID(), taxLiability.getPayrollItemId());
                            Assert.assertEquals("Current Amount", QBOFX.mapOFXStringToMoney(ofxTaxLiab.getICURAMT()), taxLiability.getCurrentAmount());
                            Assert.assertEquals("Quarter Amount", QBOFX.mapOFXStringToMoney(ofxTaxLiab.getIQTRAMT()), taxLiability.getQuarterAmount());
                            Assert.assertEquals("YTD Amount", QBOFX.mapOFXStringToMoney(ofxTaxLiab.getIYTDAMT()), taxLiability.getYTDAmount());
                            Assert.assertEquals("Current Taxable Amount", QBOFX.mapOFXStringToMoney(ofxTaxLiab.getICURWB()), taxLiability.getCurrentTaxableAmount());
                            Assert.assertEquals("Quarter Taxable Amount", QBOFX.mapOFXStringToMoney(ofxTaxLiab.getIQTRWB()), taxLiability.getQuarterTaxableAmount());
                            Assert.assertEquals("YTD Taxable Amount", QBOFX.mapOFXStringToMoney(ofxTaxLiab.getIYTDWB()), taxLiability.getYTDTaxableAmount());

                            matchFound = true;
                            break;
                        }
                    }
                    Assert.assertTrue("Found OFX Liability Match", matchFound);
                }
            }
        }
    }

    public static void assertPayrolls(Company pCompany, OFX pPayrollOfx) {
        assertPayrolls(pCompany, pPayrollOfx, null);
    }

    public static void assertPayrolls(Company pCompany, OFX pPayrollOfx, Long pPayrollToken) {
        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());        
        assertPayrolls(pPayrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), pCompany, pPayrollToken);
        OFXAssert.assertPayrollTransactions(pPayrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX(), pCompany);
        PayrollServices.rollbackUnitOfWork();
    }

    public static void assertNonVoidLiabilityCheckExceptTransactionLines(Company pCompany,
                                                                         com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX pIPAYROLLTX,
                                                                         String pPeriodEndDate) {
        assertNonVoidLiabilityCheckExceptTransactionLines(pCompany,
                                                          pIPAYROLLTX,
                                                          null,
                                                          null,
                                                          pPeriodEndDate,
                                                          null);
    }

    public static void assertNonVoidLiabilityCheckExceptTransactionLines(Company pCompany,
                                                                         com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX pIPAYROLLTX,
                                                                         PayrollRun pPayrollRun) {
        assertNonVoidLiabilityCheckExceptTransactionLines(pCompany,
                                                          pIPAYROLLTX,
                                                          null,
                                                          null,
                                                          null,
                                                          pPayrollRun);
    }

    public static void assertNonVoidLiabilityCheckExceptTransactionLines(Company pCompany,
                                                                         com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX pIPAYROLLTX,
                                                                         String pSourcePaycheckId,
                                                                         String pSourceLiabilityAdjustmentId,
                                                                         String pPeriodEndDate,
                                                                         PayrollRun pPayrollRun) {
        try {
            PayrollServices.beginUnitOfWork();
            pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
            CompanyBankAccount companyBankAccount = pCompany.getCompanyBankAccountCollection().get(0);

            PayrollRun payrollRun = null;
            if(pPayrollRun != null) {
                payrollRun = Application.findById(PayrollRun.class, pPayrollRun.getId());
            } else if(pSourcePaycheckId != null) {
                com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = com.intuit.sbd.payroll.psp.domain.Paycheck.findPaycheck(pCompany, pSourcePaycheckId);
                if(paycheck != null) {
                    payrollRun = paycheck.getPayrollRun();
                }
            } else if(pSourceLiabilityAdjustmentId != null) {
                CompanyAdjustmentSubmission companyAdjustmentSubmission = CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(pCompany, pSourceLiabilityAdjustmentId);
                if(companyAdjustmentSubmission != null) {
                    payrollRun = companyAdjustmentSubmission.getLiabilityAdjustmentCollection().get(0).getPayrollRun();
                }
            } else if(pPeriodEndDate != null) {
                DomainEntitySet<PayrollRun> domainPayrollRuns = PayrollRun.findPayrollRuns(pCompany);
                assertTrue(domainPayrollRuns.size() > 0);
                payrollRun = domainPayrollRuns.findEntity(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending)
                        .And(PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(QBOFX.mapOFXStringToDate(pPeriodEndDate).getTime()))));
            }
            assertNotNull("Payroll Run", payrollRun);

            com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX)pIPAYROLLTX;
            assertEquals("account name", companyBankAccount.getSourceBankAccountName(), ipayrolltx.getIACCTNAME());
            assertEquals("cleared", "0", ipayrolltx.getICLEARED());
            assertEquals("on service", "Y", ipayrolltx.getIONSERVICE());
            assertEquals("void", "N", ipayrolltx.getIVOID());
            assertEquals("name", QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, ipayrolltx.getINAME());
            assertEquals("memo", QBOFX.MEMOS.CREATED_BY_PAYROLL_SERVICE + QBOFX.DATE_FORMATTER.format(SpcfUtils.convertSpcfCalendarToDate(PSPDate.getPSPTime())), ipayrolltx.getIMEMO());

            assertNotNull("period end date", ipayrolltx.getIDTPAYPDEND());
            assertEquals("period end date", QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(payrollRun.getPaycheckDate())), ipayrolltx.getIDTPAYPDEND());
            DomainEntitySet<FinancialTransaction> debits = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerDdDebit));
            if(debits.size() > 0) {
                FinancialTransaction debit = debits.get(0);
                assertNotNull("er debit", debit);
                assertEquals("transaction date", QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(debit.getSettlementDate())), ipayrolltx.getIDTTX());
            } else {
                assertEquals("transaction date", QBOFX.getDTTXResponse(SpcfUtils.convertSpcfCalendarToDate(payrollRun.getPaycheckDate())), ipayrolltx.getIDTTX());
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static void assertPaychecks(List<IPAYCHK> pRequestPaychecks, List<IPAYCHKMOD> pResponsePaychecks) {
        for (IPAYCHK requestPaycheck : pRequestPaychecks) {
            boolean found = false;
            for (IPAYCHKMOD responsePaycheck : pResponsePaychecks) {
                if(requestPaycheck.getIPAYCHKID().equals(responsePaycheck.getIPAYCHKID())) {
                    found = true;

                    assertEquals(requestPaycheck.getIACCTNAME(), responsePaycheck.getIACCTNAME());
                    assertEquals(requestPaycheck.getIAMT(), responsePaycheck.getIAMT());
                    assertEquals(requestPaycheck.getICLASS(), responsePaycheck.getICLASS());
                    assertEquals(requestPaycheck.getICLEARED(), responsePaycheck.getICLEARED());
                    assertEquals(requestPaycheck.getIDTPAYPDBEGIN(), responsePaycheck.getIDTPAYPDBEGIN());
                    assertEquals(requestPaycheck.getIDTPAYPDEND(), responsePaycheck.getIDTPAYPDEND());
                    assertEquals(requestPaycheck.getIDTTX(), responsePaycheck.getIDTTX());
                    assertEquals(requestPaycheck.getIEMPID(), responsePaycheck.getIEMPID());
                    if(requestPaycheck.getIEMPNAME() != null && requestPaycheck.getIEMPNAME().length() > 0) {
                        assertEquals(requestPaycheck.getIEMPNAME(), responsePaycheck.getIEMPNAME());
                    }
                    assertEquals(requestPaycheck.getIMEMO(), responsePaycheck.getIMEMO());
                    assertEquals(requestPaycheck.getIONSERVICE(), responsePaycheck.getIONSERVICE());
                    assertEquals(requestPaycheck.getIPAYCHKTYPE(), responsePaycheck.getIPAYCHKTYPE());
                    assertEquals(requestPaycheck.getIVOID(), responsePaycheck.getIVOID());

                    Assert.assertEquals(requestPaycheck.getIDDLINE().size(), responsePaycheck.getIDDLINE().size());
                    for (int i = 0; i < requestPaycheck.getIDDLINE().size(); i++) {
                        IDDLINE reqDdline = requestPaycheck.getIDDLINE().get(i);
                        com.intuit.sbd.payroll.psp.common.ofx.response.IDDLINE respDdline = responsePaycheck.getIDDLINE().get(i);
                        assertEquals(reqDdline.getIAMT(), respDdline.getIAMT());
                        assertEquals(reqDdline.getIPITEMID(), respDdline.getIPITEMID());
                        assertEquals(reqDdline.getIDDACCT().getIACCTNAME(), respDdline.getIDDACCT().getIACCTNAME());
                        assertEquals("", respDdline.getIDDACCT().getIAMT());
                        assertEquals(reqDdline.getIDDACCT().getBANKACCTTO().getACCTID(), respDdline.getIDDACCT().getBANKACCTTO().getACCTID());
                        assertEquals(reqDdline.getIDDACCT().getBANKACCTTO().getBANKID(), respDdline.getIDDACCT().getBANKACCTTO().getBANKID());
                        assertEquals(reqDdline.getIDDACCT().getBANKACCTTO().getACCTTYPE(), respDdline.getIDDACCT().getBANKACCTTO().getACCTTYPE());
                    }
                    
                    Assert.assertEquals(requestPaycheck.getIADJLINE().size(), responsePaycheck.getIADJLINE().size());
                    for (int i = 0; i < requestPaycheck.getIADJLINE().size(); i++) {
                        IADJLINE reqAdjline = requestPaycheck.getIADJLINE().get(i);
                        com.intuit.sbd.payroll.psp.common.ofx.response.IADJLINE respAdjline = responsePaycheck.getIADJLINE().get(i);
                        assertEquals(reqAdjline.getIAMT(), respAdjline.getIAMT());
                        assertEquals(reqAdjline.getIEXPBYJOB(), respAdjline.getIEXPBYJOB());
                        assertEquals(reqAdjline.getIPITEMID(), respAdjline.getIPITEMID());
                        assertEquals(QBOFX.convertNULLToEmptyString(reqAdjline.getIQTY()), respAdjline.getIQTY());
                        assertEquals(QBOFX.convertNULLToEmptyString(reqAdjline.getIRATE()), respAdjline.getIRATE());
                        assertEquals(reqAdjline.getIYTDAMT(), respAdjline.getIYTDAMT());
                    }

                    Assert.assertEquals(requestPaycheck.getIHRLYWAGELINE().size(), responsePaycheck.getIHRLYWAGELINE().size());
                    for (int i = 0; i < requestPaycheck.getIHRLYWAGELINE().size(); i++) {
                        IHRLYWAGELINE reqHrlywageline = requestPaycheck.getIHRLYWAGELINE().get(i);
                        com.intuit.sbd.payroll.psp.common.ofx.response.IHRLYWAGELINE respHrlywageline = responsePaycheck.getIHRLYWAGELINE().get(i);
                        assertEquals(reqHrlywageline.getIAMT(), respHrlywageline.getIAMT());
                        assertEquals(reqHrlywageline.getICLASS(), respHrlywageline.getICLASS());
                        assertEquals(reqHrlywageline.getIHRS(), respHrlywageline.getIHRS());
                        assertEquals(reqHrlywageline.getIITEM(), respHrlywageline.getIITEM());
                        assertEquals(reqHrlywageline.getIJOB(), respHrlywageline.getIJOB());
                        assertEquals(reqHrlywageline.getIPITEMID(), respHrlywageline.getIPITEMID());
                        assertEquals(reqHrlywageline.getIRATE(), respHrlywageline.getIRATE());
                        assertEquals(reqHrlywageline.getIWCCODE(), respHrlywageline.getIWCCODE());
                        assertEquals(reqHrlywageline.getIYTDAMT(), respHrlywageline.getIYTDAMT());
                    }

                    assertEquals(requestPaycheck.getIPAYCHKINFO().getICHKNUM(), responsePaycheck.getIPAYCHKINFO().getICHKNUM());
                    assertEquals(requestPaycheck.getIPAYCHKINFO().getIPRORATE(), responsePaycheck.getIPAYCHKINFO().getIPRORATE());
                    assertEquals(new Double(QBOFX.mapOFXStringToDouble(requestPaycheck.getIPAYCHKINFO().getISICKACCRUED())), new Double(QBOFX.mapOFXStringToDouble(responsePaycheck.getIPAYCHKINFO().getISICKACCRUED())));
                    assertEquals(new Double(QBOFX.mapOFXStringToDouble(requestPaycheck.getIPAYCHKINFO().getIVACACCRUED())), new Double(QBOFX.mapOFXStringToDouble(responsePaycheck.getIPAYCHKINFO().getIVACACCRUED())));

                    Assert.assertEquals(requestPaycheck.getISALARYLINE().size(), responsePaycheck.getISALARYLINE().size());
                    for (int i = 0; i < requestPaycheck.getISALARYLINE().size(); i++) {
                        ISALARYLINE reqSalaryline = requestPaycheck.getISALARYLINE().get(i);
                        com.intuit.sbd.payroll.psp.common.ofx.response.ISALARYLINE respSalaryline = responsePaycheck.getISALARYLINE().get(i);
                        assertEquals(reqSalaryline.getIAMT(), respSalaryline.getIAMT());
                        assertEquals(reqSalaryline.getICLASS(), respSalaryline.getICLASS());
                        assertEquals(reqSalaryline.getIHRS(), respSalaryline.getIHRS());
                        assertEquals(reqSalaryline.getIITEM(), respSalaryline.getIITEM());
                        assertEquals(reqSalaryline.getIJOB(), respSalaryline.getIJOB());
                        assertEquals(reqSalaryline.getIPITEMID(), respSalaryline.getIPITEMID());
                        assertEquals(reqSalaryline.getIRATE(), respSalaryline.getIRATE());
                        assertEquals(reqSalaryline.getIWCCODE(), respSalaryline.getIWCCODE());
                        assertEquals(reqSalaryline.getIYTDAMT(), respSalaryline.getIYTDAMT());
                    }
                    
                    Assert.assertEquals(requestPaycheck.getITAXLINE().size(), responsePaycheck.getITAXLINE().size());
                    for (int i = 0; i < requestPaycheck.getITAXLINE().size(); i++) {
                        ITAXLINE reqTaxline = requestPaycheck.getITAXLINE().get(i);
                        com.intuit.sbd.payroll.psp.common.ofx.response.ITAXLINE respTaxline = responsePaycheck.getITAXLINE().get(i);
                        assertEquals(reqTaxline.getIAMT(), respTaxline.getIAMT());
                        assertEquals(reqTaxline.getIPITEMID(), respTaxline.getIPITEMID());
                        assertEquals(reqTaxline.getITAXABLEWAGE(), respTaxline.getITAXABLEWAGE());
                        assertEquals(reqTaxline.getITIPSWB(), respTaxline.getITIPSWB());
                        assertEquals(reqTaxline.getIWB(), respTaxline.getIWB());
                        assertEquals(reqTaxline.getIYTDAMT(), respTaxline.getIYTDAMT());
                    }

                    break;
                }
            }
            if(!found) {
                fail("Paycheck " + requestPaycheck.getIPAYCHKID() + " not found");
            }
        }
    }

    public static void assertPayrollTransactions(List<IPAYROLLTX> pRequestTransactions, List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX> pResponseTransactions) {
        for (IPAYROLLTX requestTransaction : pRequestTransactions) {
            boolean found = false;
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX responseTransaction : pResponseTransactions) {
                if(requestTransaction.getIPAYROLLTXID().equals(responseTransaction.getIPAYROLLTXID())) {
                    assertEquals(requestTransaction.getIACCTNAME(), responseTransaction.getIACCTNAME());
                    assertEquals(requestTransaction.getIAMT(), responseTransaction.getIAMT());
                    assertEquals(requestTransaction.getICLEARED(), responseTransaction.getICLEARED());
                    assertEquals(requestTransaction.getIDTPAYPDEND(), responseTransaction.getIDTPAYPDEND());
                    assertEquals(requestTransaction.getIDTTX(), responseTransaction.getIDTTX());
                    if(QBOFX.nullStringCheck(requestTransaction.getIEMPNAME()) != null) {
                        assertEquals(QBOFX.nullStringCheck(requestTransaction.getIEMPNAME()), responseTransaction.getIEMPNAME());
                    }
                    assertEquals(requestTransaction.getIMEMO(), responseTransaction.getIMEMO());
                    assertEquals(requestTransaction.getINAME(), responseTransaction.getINAME());
                    assertEquals(requestTransaction.getIONSERVICE(), responseTransaction.getIONSERVICE());
                    assertEquals(requestTransaction.getIPAYROLLTXID(), responseTransaction.getIPAYROLLTXID());
                    assertEquals(requestTransaction.getIPAYROLLTXTYPE(), responseTransaction.getIPAYROLLTXTYPE());
                    assertEquals(requestTransaction.getIREFNUM(), responseTransaction.getIREFNUM());
                    assertEquals(requestTransaction.getIVOID(), responseTransaction.getIVOID());

                    assertEquals(requestTransaction.getITXLINE().size(), responseTransaction.getITXLINE().size());
                    int matchedLines = 0;
                    for (ITXLINE requestLine : requestTransaction.getITXLINE()) {
                        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE responseLine : responseTransaction.getITXLINE()) {
                            if(ObjectUtils.equals(requestLine.getIPITEMID(), responseLine.getIPITEMID()) &&
                                    ObjectUtils.equals(requestLine.getIAMT(), responseLine.getIAMT()) &&
                                    ObjectUtils.equals(requestLine.getIACCTNAME(), responseLine.getIACCTNAME()) &&
                                    ObjectUtils.equals(requestLine.getICLASS(), responseLine.getICLASS()) &&
                                    ObjectUtils.equals(QBOFX.mapOFXStringToBoolean(requestLine.getIISDD()), QBOFX.mapOFXStringToBoolean(responseLine.getIISDD())) &&
                                    ObjectUtils.equals(requestLine.getIMEMO(), responseLine.getIMEMO()) &&
                                    ObjectUtils.equals(QBOFX.nullStringCheck(requestLine.getITAXABLEWAGE()), responseLine.getITAXABLEWAGE()) &&
                                    ObjectUtils.equals(QBOFX.nullStringCheck(requestLine.getIWB()), responseLine.getIWB())) {
                                matchedLines++;
                            }
                        }
                    }
                    assertEquals(requestTransaction.getITXLINE().size(), matchedLines);
                    found = true;
                }
            }
            if(!found) {
                fail("Payroll transaction with id '" + requestTransaction.getIPAYROLLTXID() + "' does not exist in the response");
            }
        }
    }

    public static void assertResponsePayrollTransactions(List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX> pATransactions, List<com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX> pBTransactions) {
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ATransaction : pATransactions) {
            boolean found = false;
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX BTransaction : pBTransactions) {
                if(ATransaction.getIPAYROLLTXID().equals(BTransaction.getIPAYROLLTXID())) {
                    assertEquals(ATransaction.getIACCTNAME(), BTransaction.getIACCTNAME());
                    assertEquals(ATransaction.getIAMT(), BTransaction.getIAMT());
                    assertEquals(ATransaction.getICLEARED(), BTransaction.getICLEARED());
                    assertEquals(ATransaction.getIDTPAYPDEND(), BTransaction.getIDTPAYPDEND());
                    assertEquals(ATransaction.getIDTTX(), BTransaction.getIDTTX());
                    if(QBOFX.nullStringCheck(ATransaction.getIEMPNAME()) != null) {
                        assertEquals(QBOFX.nullStringCheck(ATransaction.getIEMPNAME()), BTransaction.getIEMPNAME());
                    }
                    assertEquals(ATransaction.getIMEMO(), BTransaction.getIMEMO());
                    assertEquals(ATransaction.getINAME(), BTransaction.getINAME());
                    assertEquals(ATransaction.getIONSERVICE(), BTransaction.getIONSERVICE());
                    assertEquals(ATransaction.getIPAYROLLTXID(), BTransaction.getIPAYROLLTXID());
                    assertEquals(ATransaction.getIPAYROLLTXTYPE(), BTransaction.getIPAYROLLTXTYPE());
                    assertEquals(ATransaction.getIREFNUM(), BTransaction.getIREFNUM());
                    assertEquals(ATransaction.getIVOID(), BTransaction.getIVOID());

                    assertEquals(ATransaction.getITXLINE().size(), BTransaction.getITXLINE().size());
                    boolean matchedFound = false;
                    for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE ALine : ATransaction.getITXLINE()) {
                        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE BLine : BTransaction.getITXLINE()) {
                            if(ObjectUtils.equals(ALine.getIPITEMID(), BLine.getIPITEMID()) &&
                                    ObjectUtils.equals(QBOFX.mapOFXStringToMoney(ALine.getIAMT()), QBOFX.mapOFXStringToMoney(BLine.getIAMT())) &&
                                    ObjectUtils.equals(ALine.getIACCTNAME(), BLine.getIACCTNAME()) &&
                                    ObjectUtils.equals(ALine.getICLASS(), BLine.getICLASS()) &&
                                    ObjectUtils.equals(QBOFX.mapOFXStringToBoolean(ALine.getIISDD()), QBOFX.mapOFXStringToBoolean(BLine.getIISDD())) &&
                                    ObjectUtils.equals(ALine.getIMEMO(), BLine.getIMEMO()) &&
                                    ObjectUtils.equals(ALine.getITAXABLEWAGE(), BLine.getITAXABLEWAGE()) &&
                                    ObjectUtils.equals(ALine.getIWB(), BLine.getIWB())) {
                                matchedFound = true;
                                break;
                            }
                        }
                        if(!matchedFound) {
                            fail("No match found for: \n" + ALine.getIPITEMID() + " " + ALine.getIMEMO());
                        }
                        matchedFound = false;
                    }
                    found = true;
                }
            }
            if(!found) {
                fail("Payroll transaction with id '" + ATransaction.getIPAYROLLTXID() + "' does not exist in the response");
            }
        }
    }

    public static void assertTXLINEFound(List<com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE> pITXLINEs,
                             String pAccountName,
                             String pAmount,
                             String pTrackingClass,
                             boolean pIsDD,
                             String pMemo,
                             String pPitemId) {
        boolean foundLine = false;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : pITXLINEs) {
            if(ObjectUtils.equals(QBOFX.nullStringCheck(itxline.getIPITEMID()), pPitemId) &&
                    ObjectUtils.equals(QBOFX.nullStringCheck(itxline.getIAMT()), pAmount) &&
                    ObjectUtils.equals(QBOFX.nullStringCheck(itxline.getIACCTNAME()), pAccountName) &&
                    ObjectUtils.equals(QBOFX.nullStringCheck(itxline.getICLASS()), pTrackingClass) &&
                    ObjectUtils.equals(QBOFX.mapOFXStringToBoolean(itxline.getIISDD()), pIsDD) &&
                    ObjectUtils.equals(QBOFX.nullStringCheck(itxline.getIMEMO()), pMemo)) {
                foundLine = true;
                break;
            }
        }
        assertTrue("Transaction line not found", foundLine);
    }
}
