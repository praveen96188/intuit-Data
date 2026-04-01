package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplate;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: mvillani
 * Date: Feb 1, 2012
 * Time: 4:37:17 PM
 */
@WebService()
public class SUIPaymentsWS {

    @WebMethod
    public void FinalizeSUIPayments(@WebParam(name = "moneyMovementTransactions") List<String> pMoneyMovementTransactions,
                                    @WebParam(name = "paymentTemplate") String pPaymentTemplateCd,
                                    @WebParam(name = "year") int pYear,
                                    @WebParam(name = "quarter") int pQuarter) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(SUIPaymentsWS.class);

        if ((pMoneyMovementTransactions == null || pMoneyMovementTransactions.size() == 0) && pPaymentTemplateCd == null) {
            throw new RuntimeException(
                    "Please enter either MMTs or Payment Template");
        }

        try {
            PayrollServices.beginUnitOfWork();
            ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>();
            if (pMoneyMovementTransactions != null) {
                for (String mmtId : pMoneyMovementTransactions) {
                    MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
                    if (mmt != null) {
                        mmts.add(mmt);
                    }
                }
            }
            PaymentTemplate paymentTemplate = null;
            if (pPaymentTemplateCd != null) {
                paymentTemplate = PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd);
            }

            ProcessResult processResult = PayrollServices.paymentManager.finalizeSUIPayments(mmts, paymentTemplate, pYear, pQuarter);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @WebMethod
    public void UnfinalizeSUIPayments(@WebParam(name = "moneyMovementTransactions") List<String> pMoneyMovementTransactions,
                                      @WebParam(name = "paymentTemplate") String pPaymentTemplateCd,
                                      @WebParam(name = "year") int pYear,
                                      @WebParam(name = "quarter") int pQuarter) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(SUIPaymentsWS.class);

        if ((pMoneyMovementTransactions == null || pMoneyMovementTransactions.size() == 0) && pPaymentTemplateCd == null) {
            throw new RuntimeException(
                    "Both MMT list and Payment Template are null");
        }

        try {
            PayrollServices.beginUnitOfWork();
            ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>();
            if (pMoneyMovementTransactions != null) {
                for (String mmtId : pMoneyMovementTransactions) {
                    MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
                    if (mmt != null) {
                        mmts.add(mmt);
                    }
                }
            }
            PaymentTemplate paymentTemplate = null;
            if (pPaymentTemplateCd != null) {
                paymentTemplate = PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd);
            }

            ProcessResult processResult = PayrollServices.paymentManager.unfinalizeSUIPayments(mmts, paymentTemplate, pYear, pQuarter);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void SplitSUIPayments(@WebParam(name = "financialTransactions") List<String> pFinancialTransactions) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(SUIPaymentsWS.class);

        if ((pFinancialTransactions == null || pFinancialTransactions.size() == 0)) {
            throw new RuntimeException(
                    "No financial transactions");
        }
        try {
            PayrollServices.beginUnitOfWork();
            ArrayList<FinancialTransaction> fts = new ArrayList<FinancialTransaction>();

            for (String ftId : pFinancialTransactions) {
                FinancialTransaction ft = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(ftId));
                if (ft != null) {
                    fts.add(ft);
                }
            }

            ProcessResult processResult = PayrollServices.paymentManager.splitSUIPayments(fts, null);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void CombineSUIPayments(@WebParam(name = "financialTransactions") List<String> pFinancialTransactions) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(SUIPaymentsWS.class);

        if ((pFinancialTransactions == null || pFinancialTransactions.size() == 0)) {
            throw new RuntimeException(
                    "No financial transactions");
        }
        try {
            PayrollServices.beginUnitOfWork();
            ArrayList<FinancialTransaction> fts = new ArrayList<FinancialTransaction>();

            for (String ftId : pFinancialTransactions) {
                FinancialTransaction ft = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(ftId));
                if (ft != null) {
                    fts.add(ft);
                }
            }

            ProcessResult processResult = PayrollServices.paymentManager.combineSUIPayments(fts, null);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void AdjustSUITaxPayments(@WebParam(name = "moneyMovementTransaction")String pMoneyMovementTransactionId,
                                     @WebParam(name = "lawId") String pLawId,
                                     @WebParam(name = "lawAmount") String pLawAmount,
                                     @WebParam(name = "immmediateDebitOrRefund") boolean pImmediateDebitOrRefund) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        Map<String, String> lawAmountsMap = new HashMap<String, String>();
        lawAmountsMap.put(pLawId, pLawAmount);

        if (lawAmountsMap.size() == 0) {
            throw new RuntimeException(
                    "No adjustments");
        }
        try {
            PayrollServices.beginUnitOfWork();
            MoneyMovementTransaction mmt = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(pMoneyMovementTransactionId));
            if (mmt == null) {
                throw new RuntimeException("Money Movement Transaction does not exist.");
            }
            Map<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();

            for (String lawId : lawAmountsMap.keySet()) {
                DomainEntitySet<Law> laws = Application.find(Law.class, Law.LawId().equalTo(lawId));
                if(laws.size() == 0){
                    throw new RuntimeException(
                            "Law not found :" + lawId);
                }

                Law law = laws.get(0);
                if (law != null) {
                    lawAmounts.put(law, new SpcfMoney(lawAmountsMap.get(lawId)));
                }
            }

            ProcessResult processResult = PayrollServices.paymentManager.adjustSUITaxPayment(mmt, lawAmounts, pImmediateDebitOrRefund, null);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
