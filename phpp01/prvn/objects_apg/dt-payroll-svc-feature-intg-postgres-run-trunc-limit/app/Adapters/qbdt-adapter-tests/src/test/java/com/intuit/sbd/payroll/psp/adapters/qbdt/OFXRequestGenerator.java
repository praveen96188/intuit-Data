package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 18, 2010
 * Time: 1:41:32 PM
 */

public class OFXRequestGenerator {
    private static int mCompanyId = 0;
    private static int mEmployeeId = 0;
    private static int mPayrollItemId = 0;
    private static int mPaycheckId = 0;
    private static int mPayrollTransactionId = 0;

    public static final String EXPENSE_ACCOUNT = "Payroll Expenses";
    public static final String LIABILITY_ACCOUNT = "Payroll Liabilities";

    public static void reset() {
        mEmployeeId = 0;
        mPayrollItemId = 0;
        mPaycheckId = 0;
        mPayrollTransactionId = 0;
    }

    public static void setPayrollItemId(int pPayrollItemId) {
        mPayrollItemId = pPayrollItemId;
    }

    public static int getNextPaycheckId() {
        return ++mPaycheckId;
    }

    public static int getNextPayrollTransactionId() {
        return ++mPayrollTransactionId;
    }

    public static OFX generateSyncRequest(String pPsid, Long pToken) {
        String token;
        if(pToken != null) {
            token = Long.toString(pToken);
        } else {
            token = "";
        }
        
        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(generateSignOnMessage(pPsid, DataLoadServices.PIN));
        ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage(token, false, null));
        return ofx;
    }

    public static OFX generateBalanceFile(String pPsid, boolean pIncludePayrolls) {
        return generateBalanceFile(pPsid, pIncludePayrolls, false, false);
    }

    public static OFX generateBalanceFile(String pPsid, boolean pIncludePayrolls, boolean pIncludePriorPayments, boolean pCreate401K) {
        return generateBalanceFile(pPsid, pIncludePayrolls, pIncludePriorPayments, false, pCreate401K);
    }

    public static OFX generateBalanceFile(String pPsid, boolean pIncludePayrolls, boolean pIncludePriorPayments, boolean pTaxesOnly, boolean pCreate401K) {
        return generateBalanceFile(pPsid, pIncludePayrolls, pIncludePriorPayments, false, pTaxesOnly, pCreate401K);
    }

    public static OFX generateBalanceFile(String pPsid, boolean pIncludePayrolls, boolean pIncludePriorPayments, boolean pIncludePriorRefunds, boolean pTaxesOnly, boolean pCreate401K) {
        return generateBalanceFile(pPsid, pIncludePayrolls, pIncludePriorPayments, pIncludePriorRefunds, pTaxesOnly, pCreate401K,null);
    }
    public static OFX generateBalanceFile(String pPsid, boolean pIncludePayrolls, boolean pIncludePriorPayments, boolean pIncludePriorRefunds, boolean pTaxesOnly, boolean pCreate401K,String state) {
        return generateBalanceFile(pPsid, pIncludePayrolls, pIncludePriorPayments, pIncludePriorRefunds, pTaxesOnly, pCreate401K, state, 5);
    }
    public static OFX generateBalanceFile(String pPsid, boolean pIncludePayrolls, boolean pIncludePriorPayments, boolean pIncludePriorRefunds, boolean pTaxesOnly, boolean pCreate401K,String state, int noOfEmployees) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(generateSignOnMessage(pPsid, DataLoadServices.PIN));

        List<IPITEM> payrollItems = new ArrayList<IPITEM>();
        List<IPITEM> taxes = generateFedTaxes();
        if(state !=null && "GA".equalsIgnoreCase(state.trim())){
            taxes.addAll(generateGATaxes());
        }else{
            taxes.addAll(generateCATaxes());
        }

        payrollItems.addAll(taxes);

        List<IPITEM> wageItems = new ArrayList<IPITEM>();
        List<IPITEM> adjustmentItems = new ArrayList<IPITEM>();
        if(!pTaxesOnly) {
            wageItems = generatePayrollItems(taxes, QBOFX.OFXPayrollItemType.Hourly, QBOFX.OFXPayrollItemType.Salary);
            payrollItems.addAll(wageItems);
            adjustmentItems = generatePayrollItems(taxes, QBOFX.OFXPayrollItemType.EmployerContribution, QBOFX.OFXPayrollItemType.Deduction);
            payrollItems.addAll(adjustmentItems);
            payrollItems.addAll(generateAllPayrollItemTypes(taxes, false));

            if (pCreate401K) {
                payrollItems.add(generatePayrollItem(false,
                                    true,
                                    "401K " + (mPayrollItemId + 1),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    generateDeduction(false, false, "401K", null, null, null, LIABILITY_ACCOUNT, "", false, "401K", taxes),
                                    null,
                                    null,
                                    null));
            }
        }


        List<IEMP> newEmployees = generateNewEmployees(noOfEmployees,
                                                       wageItems,
                                                       adjustmentItems,
                                                       taxes);

        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        if(pIncludePayrolls) {
            for(int i = 0; i < 5; i++) {
                payrollRuns.add(generatePayrollRun(newEmployees,
                                                   payrollItems,
                                                   new Date("01/01/11"),
                                                   new Date("01/01/11"),
                                                   new Date("01/01/11"),
                                                   true));
            }
        }

        List<IPAYROLLTX> payrollTransactions = new ArrayList<IPAYROLLTX>();
        if(pIncludePriorPayments) {
            List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
            int lineCount = 0;
            int transactionTotal = 0;
            for (IPITEM payrollItem : taxes) {
                transactionTotal += lineCount + 1;
                itxlines.add(generateTransactionLine(null,
                                                     new SpcfMoney(SpcfDecimal.createInstance((lineCount + 1))),
                                                     "Class" + lineCount,
                                                     false,
                                                     "Memo" + lineCount,
                                                     payrollItem.getIPITEMID(),
                                                     new SpcfMoney(SpcfDecimal.createInstance(lineCount + 1)),
                                                     new SpcfMoney(SpcfDecimal.createInstance(lineCount + 2))));
                lineCount++;
            }
            for(int i = 0; i < 5; i++) {
                payrollTransactions.add(generatePayrollTransaction("Account" + i,
                                                             new SpcfMoney(SpcfDecimal.createInstance(-transactionTotal)),
                                                             "" + i,
                                                             new Date("01/31/2011"),
                                                             new Date("01/31/2011"),
                                                             null,
                                                             "Memo " + i,
                                                             "Agency" + i,
                                                             false,
                                                             QBOFX.OFXPayrollTransactionTransactionType.PRIORPMT,
                                                             "Ref" + i,
                                                             false,
                                                             itxlines));
            }
        }

        if(pIncludePriorRefunds) {
            List<ITXLINE> itxlines = new ArrayList<ITXLINE>();
            int lineCount = 0;
            int transactionTotal = 0;
            for (IPITEM payrollItem : taxes) {
                int amount = -lineCount - 1;
                transactionTotal -= amount;
                itxlines.add(generateTransactionLine(null,
                                                     new SpcfMoney(SpcfDecimal.createInstance(amount)),
                                                     "Class" + lineCount,
                                                     false,
                                                     "Memo" + lineCount,
                                                     payrollItem.getIPITEMID(),
                                                     new SpcfMoney(SpcfDecimal.createInstance(lineCount + 1)),
                                                     new SpcfMoney(SpcfDecimal.createInstance(lineCount + 2))));
                lineCount++;
            }
            for(int i = 0; i < 5; i++) {
                payrollTransactions.add(generatePayrollTransaction("Account" + i,
                                                             new SpcfMoney(SpcfDecimal.createInstance(-transactionTotal)),
                                                             "" + i,
                                                             new Date("01/31/2011"),
                                                             new Date("01/31/2011"),
                                                             null,
                                                             "Memo " + i,
                                                             "Agency" + i,
                                                             false,
                                                             QBOFX.OFXPayrollTransactionTransactionType.REFUND,
                                                             "Ref" + i,
                                                             false,
                                                             itxlines));
            }
        }

        IPAYROLLTRNRQ ipayrolltrnrq = generatePayrollRequest(true,
                                                             new Date("01/01/11"),
                                                             newEmployees,
                                                             null,
                                                             null,
                                                             payrollItems,
                                                             null,
                                                             null,
                                                             payrollTransactions,
                                                             null,
                                                             null,
                                                             payrollRuns);
        if(company != null) {
            ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage(company, true, ipayrolltrnrq));
        } else {
            ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage("1", true, ipayrolltrnrq));
        }

        return ofx;
    }

    public static OFX generateAllPayrollItemTypes(String pPsid) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(generateSignOnMessage(pPsid, DataLoadServices.PIN));

        IPAYROLLTRNRQ ipayrolltrnrq = generatePayrollRequest(true,
                                                             new Date("10/01/10"),
                                                             null,
                                                             null,
                                                             null,
                                                             generateAllPayrollItemTypes(generateFedTaxes(), true),
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null);
        ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage(company, true, ipayrolltrnrq));

        return ofx;
    }

    public static OFX generateEmployeeAccrualUpdates(OFX balanceFile) {
        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(balanceFile.getSIGNONMSGSRQV1());

        List<IEMP> employeeMods = new ArrayList<IEMP>();
        for (IEMP iemp : balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            double sickHours = Double.parseDouble(iemp.getISICK().getIHRS()) + 5;
            double vacationHours = Double.parseDouble(iemp.getISICK().getIHRS()) + 6;
            IEMP iempmod = generateAccrualEmployeeMod(iemp.getIEMPID(),
                                                      sickHours,
                                                      vacationHours);
            employeeMods.add(iempmod);
        }

        IPAYROLLTRNRQ ipayrolltrnrq = generatePayrollRequest(false,
                                                             null,
                                                             null,
                                                             employeeMods,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null);

        ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage((Integer.parseInt(balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN()) + 1) + "", true, ipayrolltrnrq));

        return ofx;
    }

    public static OFX generateEmployeeFullUpdates(OFX balanceFile, Company pCompany) {
        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(balanceFile.getSIGNONMSGSRQV1());

        List<IEMP> employeeMods = new ArrayList<IEMP>();
        for (IEMP iemp : balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTRELEASE("20101025");
            iemp.setIBILLPAYACCT("blah");
            iemp.getIADDRINFO().setISTATELIVED("OH");
            iemp.getIADDRINFO().setISTATEWORKED("OH");
            iemp.getIADDRINFO().setITITLE("Master");
            iemp.getIADDRINFO().setIADDR1("street 1");
            iemp.getIADDRINFO().setIADDR2("street 2");
            iemp.getIADDRINFO().setICITY("Not Reno");
            iemp.getIADDRINFO().setISTATE("OH");
            iemp.getIPAYROLL().setIPAYPD("MONTHLY");
            iemp.setIINACTIVE("Y");
            iemp.setISICK(generateAccrual("PAYROLL",
                                          85.0,
                                          7.25,
                                          250,
                                          false));
            iemp.setIVAC(generateAccrual("YEARLY",
                                         50.0,
                                         11.11,
                                         300,
                                         false));
            employeeMods.add(iemp);
        }

        IPAYROLLTRNRQ ipayrolltrnrq = generatePayrollRequest(false,
                                                             null,
                                                             null,
                                                             employeeMods,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null);

        ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage(pCompany.getCurrentToken() + "", true, ipayrolltrnrq));

        return ofx;
    }

    public static OFX generateEmployeeDeletes(OFX balanceFile) {
        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(balanceFile.getSIGNONMSGSRQV1());

        List<String> employeeDeletes = new ArrayList<String>();
        for (IEMP iemp : balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            employeeDeletes.add(iemp.getIEMPID());
        }

        IPAYROLLTRNRQ ipayrolltrnrq = generatePayrollRequest(false,
                                                             null,
                                                             null,
                                                             null,
                                                             employeeDeletes,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null);

        ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage((Integer.parseInt(balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN()) + 1) + "", true, ipayrolltrnrq));

        return ofx;
    }

    public static OFX generatePayrollTransactionDeletes(OFX balanceFile) {
        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(balanceFile.getSIGNONMSGSRQV1());

        List<String> payrollTransactionDeleteIds = new ArrayList<String>();
        for (IPAYROLLTX ipayrolltx : balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX()) {
            payrollTransactionDeleteIds.add(ipayrolltx.getIPAYROLLTXID());
        }

        IPAYROLLTRNRQ ipayrolltrnrq = generatePayrollRequest(false,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             payrollTransactionDeleteIds,
                                                             null);

        ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage((Integer.parseInt(balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN()) + 1) + "", true, ipayrolltrnrq));

        return ofx;
    }

    public static List<IPITEM> generateAllPayrollItemTypes(List<IPITEM> pTaxes, boolean pAddAllTaxes) {
        // pre tax
        List<IPITEM> payrollItems = generatePayrollItems(pTaxes,
                                                         QBOFX.OFXPayrollItemType.Addition,
                                                         QBOFX.OFXPayrollItemType.Bonus,
                                                         QBOFX.OFXPayrollItemType.Commission,
                                                         QBOFX.OFXPayrollItemType.Deduction,
                                                         QBOFX.OFXPayrollItemType.DirectDeposit,
                                                         QBOFX.OFXPayrollItemType.EmployerContribution,
                                                         QBOFX.OFXPayrollItemType.Hourly,
                                                         QBOFX.OFXPayrollItemType.Salary);
        // post tax
        payrollItems.addAll(generatePayrollItems(new ArrayList<IPITEM>(),
                                                 QBOFX.OFXPayrollItemType.Addition,
                                                 QBOFX.OFXPayrollItemType.Deduction,
                                                 QBOFX.OFXPayrollItemType.EmployerContribution));

        if(pAddAllTaxes) {
            payrollItems.addAll(pTaxes);
        }

        return payrollItems;
    }

    public static IPAYROLLTX generatePayrollTransaction(String pAccountName,
                                                        SpcfMoney pAmount,
                                                        String pCleared,
                                                        Date pPeriodEndDate,
                                                        Date pTransactionDate,
                                                        String pEmployeeId,
                                                        String pMemo,
                                                        String pAgencyName,
                                                        boolean pOnService,
                                                        QBOFX.OFXPayrollTransactionTransactionType pPayrollTransactionType,
                                                        String pReferenceNumber,
                                                        boolean pVoid,
                                                        List<ITXLINE> pTransactionLines) {
        IPAYROLLTX ipayrolltx = new IPAYROLLTX();
        ipayrolltx.setIACCTNAME(pAccountName != null ? pAccountName : QBOFX.NULL);
        ipayrolltx.setIAMT((pAmount != null) ? "$" + pAmount.toString() : QBOFX.NULL);
        ipayrolltx.setICLEARED(pCleared != null ? pCleared : QBOFX.NULL);
        ipayrolltx.setIDTPAYPDEND(QBOFX.getDTTXResponse(pPeriodEndDate));
        ipayrolltx.setIDTTX(QBOFX.getDTTXResponse(pTransactionDate));
        ipayrolltx.setIEMPID(pEmployeeId);
        ipayrolltx.setIEMPNAME("");
        ipayrolltx.setIMEMO(pMemo != null ? pMemo : QBOFX.NULL);
        ipayrolltx.setINAME(pAgencyName != null ? pAgencyName : QBOFX.NULL);
        ipayrolltx.setIONSERVICE(QBOFX.Y_N(pOnService));
        ipayrolltx.setIPAYROLLTXID(++mPayrollTransactionId + "");
        ipayrolltx.setIPAYROLLTXTYPE(pPayrollTransactionType.toString());
        ipayrolltx.setIREFNUM(pReferenceNumber != null ? pReferenceNumber : QBOFX.NULL);
        ipayrolltx.setIVOID(QBOFX.Y_N(pVoid));
        ipayrolltx.getITXLINE().addAll(pTransactionLines);

        return ipayrolltx;
    }

    public static ITXLINE generateTransactionLine(String pAccountName,
                                                  SpcfMoney pAmount,
                                                  String pTrackingClass,
                                                  boolean pIsDirectDeposit,
                                                  String pMemo,
                                                  String pPayrollItemId,
                                                  SpcfMoney pTaxableWages,
                                                  SpcfMoney pTotalWages) {
        ITXLINE itxline = new ITXLINE();
        itxline.setIACCTNAME(pAccountName);
        itxline.setIAMT((pAmount != null) ? "$"+pAmount.toString() : QBOFX.NULL);
        itxline.setICLASS(pTrackingClass);
        itxline.setIISDD(QBOFX.Y_N(pIsDirectDeposit));
        itxline.setIMEMO(pMemo != null ? pMemo : QBOFX.NULL);
        itxline.setIPITEMID(pPayrollItemId);
        itxline.setITAXABLEWAGE(((pTotalWages != null) ? "$" + pTotalWages.toString() : QBOFX.NULL));
        itxline.setIWB(((pTaxableWages != null) ? "$" + pTaxableWages.toString() : QBOFX.NULL));
        return itxline;
    }

    public static IPAYROLLRUN generatePayrollRun(List<IEMP> pEmployees,
                                                 List<IPITEM> pPayrollItems,
                                                 Date pPaycheckDate,
                                                 Date pPeriodBeginDate,
                                                 Date pPeriodEndDate,
                                                 boolean pIsYTDAdj) {
        return generatePayrollRun(pEmployees,
                                  pPayrollItems,
                                  pPaycheckDate,
                                  pPeriodBeginDate,
                                  pPeriodEndDate,
                                  pIsYTDAdj,
                                  false);
    }

    public static IPAYROLLRUN generatePayrollRun(List<IEMP> pEmployees,
                                                 List<IPITEM> pPayrollItems,
                                                 Date pPaycheckDate,
                                                 Date pPeriodBeginDate,
                                                 Date pPeriodEndDate,
                                                 boolean pIsYTDAdj,
                                                 boolean pIncludeDuplicateItems) {
        IPAYROLLRUN ipayrollrun = new IPAYROLLRUN();

        ipayrollrun.setIDTPAYCHKS(QBOFX.getDTTXResponse(pPaycheckDate));
        for (IEMP employee : pEmployees) {
            ipayrollrun.getIPAYCHK().add(generatePaycheck(employee,
                                                          pPayrollItems,
                                                          pIsYTDAdj,
                                                          pPaycheckDate,
                                                          pPeriodBeginDate,
                                                          pPeriodEndDate,
                                                          LIABILITY_ACCOUNT,
                                                          "Paycheck Class",
                                                          "2",
                                                          "Da Bears",
                                                          "1527",
                                                          5,
                                                          10));
        }

        List<String[]> taxLiab = new ArrayList<String[]>(20);
        taxLiab.add(new String[]{"$0.00","$0.00","$0.00","$0.00","$0.00","$0.00",
                                 "LIAB","10","AEIC",null,null,null});
        taxLiab.add(new String[]{"$0.00","$-42.00","$-42.00","$0.00","$7000.00","$7000.00",
                                 "LIAB","11","FUTA",null,null,null});
        taxLiab.add(new String[]{"$0.00","$-42.00","$-42.00","$0.00","$7000.00","$7000.00",
                                 "LIAB","11","FUTA",null,null,null});           // Intentional Duplicate
        taxLiab.add(new String[]{"$-4.00","$-2707.00","$-2707.00","$125.00","$11737.69","$11737.69",
                                 "LIAB","12","FIT",null,null,null});
        taxLiab.add(new String[]{"$-1.45","$-179.85","$-179.85","$100.00","$12403.73","$12403.73",
                                 "LIAB","13","MED_ER",null,null,null});
        taxLiab.add(new String[]{"$-0.36","$-5.80","$-5.80","$25.00","$400.00","$400.00",
                                 "LIAB","13","MED_ER_TIPS",null,null,null});
        taxLiab.add(new String[]{"$-1.45","$-179.85","$-179.85","$100.00","$12403.73","$12403.73",
                                 "LIAB","14","MED_EE",null,null,null});
        taxLiab.add(new String[]{"$-0.36","$-5.80","$-5.80","$25.00","$400.00","$400.00",
                                 "LIAB","14","MED_EE_TIPS",null,null,null});
        taxLiab.add(new String[]{"$-6.20","$-769.03","$-769.03","$100.00","$12403.73","$12403.73",
                                 "LIAB","15","SS_ER",null,null,null});
        taxLiab.add(new String[]{"$-1.55","$-24.80","$-24.80","$25.00","$400.00","$400.00",
                                 "LIAB","15","SS_ER_TIPS",null,null,null});
        taxLiab.add(new String[]{"$-4.20","$-520.96","$-520.96","$100.00","$12403.73","$12403.73",
                                 "LIAB","16","SS_EE",null,null,null});
        taxLiab.add(new String[]{"$-1.05","$-16.80","$-16.80","$25.00","$400.00","$400.00",
                                 "LIAB","16","SS_EE_TIPS",null,null,null});
        taxLiab.add(new String[]{"$0.00","$0.00","$0.00","$125.00","$12807.69","$12807.69",
                                 "LIAB","17",null,"NV","SUI_ER", null});
        taxLiab.add(new String[]{"$0.00","$0.00","$0.00","$125.00","$12807.69","$12807.69",
                                 "LIAB","17",null,"NV","SUI_ER", null});    // Intentional Duplicate
        taxLiab.add(new String[]{"$-0.06","$-6.40","$-6.40","$125.00","$12807.69","$12807.69",
                                 "LIAB","18",null,null,null,"159"});
        taxLiab.add(new String[]{"$-0.06","$-6.40","$-6.40","$125.00","$12807.69","$12807.69",
                                 "LIAB","18",null,null,null,"159"});        // Intentional Duplicate

        IDISBURSEADVICE idisburseadvice = new IDISBURSEADVICE();
        idisburseadvice.setITAXLIABAMT("$-20.68");
        idisburseadvice.setITAXQTR("4");
        List<ITAXLIAB> itaxliablist = idisburseadvice.getITAXLIAB();

        for(String[] liab : taxLiab) {
            ITAXLIAB itaxliab = new ITAXLIAB();
            itaxliab.setICURAMT(liab[0]);
            itaxliab.setIQTRAMT(liab[1]);
            itaxliab.setIYTDAMT(liab[2]);
            itaxliab.setICURWB(liab[3]);
            itaxliab.setIQTRWB(liab[4]);
            itaxliab.setIYTDWB(liab[5]);
            itaxliab.setITAXPMTTYPE(liab[6]);
            itaxliab.setIPITEMID(liab[7]);
            itaxliab.setIFEDTAX(liab[8]);
            if(liab[9] != null) {
                ISTATETAXDESC stateTaxDesc = new ISTATETAXDESC();
                stateTaxDesc.setISTATE(liab[9]);
                stateTaxDesc.setISTATETAX(liab[10]);
                itaxliab.setISTATETAXDESC(stateTaxDesc);
            }
            itaxliab.setIOTHERTAX(liab[11]);
            itaxliablist.add(itaxliab);
        }
        ipayrollrun.setIDISBURSEADVICE(idisburseadvice);

        return ipayrollrun;
    }

    public static IPAYCHK copyIPAYCHK(IPAYCHK pPaycheck) {
        IPAYCHK ipaychk = new IPAYCHK();
        ipaychk.setIPAYCHKID(pPaycheck.getIPAYCHKID());
        ipaychk.setIEMPID(pPaycheck.getIEMPID());
        // this is not used, but must be included
        ipaychk.setIEMPNAME(pPaycheck.getIEMPNAME());
        ipaychk.setIPAYCHKTYPE(pPaycheck.getIPAYCHKTYPE());
        ipaychk.setIAMT(pPaycheck.getIAMT());

        ipaychk.setIACCTNAME(pPaycheck.getIACCTNAME());
        ipaychk.setICLASS(pPaycheck.getICLASS());
        ipaychk.setICLEARED(pPaycheck.getICLEARED());
        ipaychk.setIDTPAYPDBEGIN(pPaycheck.getIDTPAYPDBEGIN());
        ipaychk.setIDTPAYPDEND(pPaycheck.getIDTPAYPDEND());
        ipaychk.setIDTTX(pPaycheck.getIDTTX());
        ipaychk.setIMEMO(pPaycheck.getIMEMO());
        ipaychk.setIONSERVICE(pPaycheck.getIONSERVICE());
        ipaychk.setIVOID(QBOFX.Y_N(true));

        IPAYCHKINFO ipaychkinfo = new IPAYCHKINFO();
        ipaychkinfo.setICHKNUM(pPaycheck.getIPAYCHKINFO().getICHKNUM());
        ipaychkinfo.setIPRORATE(pPaycheck.getIPAYCHKINFO().getIPRORATE());
        ipaychkinfo.setISICKACCRUED(pPaycheck.getIPAYCHKINFO().getISICKACCRUED());
        ipaychkinfo.setIVACACCRUED(pPaycheck.getIPAYCHKINFO().getIVACACCRUED());
        ipaychk.setIPAYCHKINFO(ipaychkinfo);

        for (IADJLINE adjLine : pPaycheck.getIADJLINE()) {
            IADJLINE iline = new IADJLINE();
            iline.setIAMT(adjLine.getIAMT());
            iline.setIEXPBYJOB(adjLine.getIEXPBYJOB());
            iline.setIPITEMID(adjLine.getIPITEMID());
            iline.setIQTY(adjLine.getIQTY());
            iline.setIRATE(adjLine.getIRATE());
            iline.setIYTDAMT(adjLine.getIYTDAMT());
            ipaychk.getIADJLINE().add(iline);
        }

        for (IDDLINE ddline : pPaycheck.getIDDLINE()) {
            IDDLINE iddline = new IDDLINE();
            iddline.setIAMT(ddline.getIAMT());
            iddline.setIPITEMID(ddline.getIPITEMID());
            iddline.setIPITEMNAME(ddline.getIPITEMNAME());

            IDDACCT iddacct = new IDDACCT();
            iddacct.setIACCTNAME(ddline.getIDDACCT().getIACCTNAME());
            iddacct.setIAMT(ddline.getIDDACCT().getIAMT());

            BANKACCT bankacct = new BANKACCT();
            bankacct.setACCTID(ddline.getIDDACCT().getBANKACCTTO().getACCTID());
            bankacct.setACCTTYPE(ddline.getIDDACCT().getBANKACCTTO().getACCTTYPE());
            bankacct.setBANKID(ddline.getIDDACCT().getBANKACCTTO().getBANKID());
            iddacct.setBANKACCTTO(bankacct);

            iddline.setIDDACCT(ddline.getIDDACCT());

            ipaychk.getIDDLINE().add(ddline);
        }

        for (IHRLYWAGELINE hrlywageline : pPaycheck.getIHRLYWAGELINE()) {
            IHRLYWAGELINE ihrlywageline = new IHRLYWAGELINE();
            ihrlywageline.setIAMT(hrlywageline.getIAMT());
            ihrlywageline.setICLASS(hrlywageline.getICLASS());
            ihrlywageline.setIHRS(hrlywageline.getIHRS());
            ihrlywageline.setIITEM(hrlywageline.getIITEM());
            ihrlywageline.setIJOB(hrlywageline.getIJOB());
            ihrlywageline.setIPITEMID(hrlywageline.getIPITEMID());
            ihrlywageline.setIRATE(hrlywageline.getIRATE());
            ihrlywageline.setIWCCODE(hrlywageline.getIWCCODE());
            ihrlywageline.setIYTDAMT(hrlywageline.getIYTDAMT());
            ipaychk.getIHRLYWAGELINE().add(ihrlywageline);
        }

        for (ISALARYLINE salaryline : pPaycheck.getISALARYLINE()) {
            ISALARYLINE isalaryline = new ISALARYLINE();
            isalaryline.setIAMT(salaryline.getIAMT());
            isalaryline.setICLASS(salaryline.getICLASS());
            isalaryline.setIHRS(salaryline.getIHRS());
            isalaryline.setIITEM(salaryline.getIITEM());
            isalaryline.setIJOB(salaryline.getIJOB());
            isalaryline.setIPITEMID(salaryline.getIPITEMID());
            isalaryline.setIRATE(salaryline.getIRATE());
            isalaryline.setIWCCODE(salaryline.getIWCCODE());
            isalaryline.setIYTDAMT(salaryline.getIYTDAMT());
            ipaychk.getISALARYLINE().add(isalaryline);
        }

        for (ITAXLINE taxline : pPaycheck.getITAXLINE()) {
            ITAXLINE itaxline = new ITAXLINE();
            itaxline.setIAMT(taxline.getIAMT());
            itaxline.setIPITEMID(taxline.getIPITEMID());
            itaxline.setITAXABLEWAGE(taxline.getITAXABLEWAGE());
            itaxline.setITIPSWB(taxline.getITIPSWB());
            itaxline.setIWB(taxline.getIWB());
            itaxline.setIYTDAMT(taxline.getIYTDAMT());
            ipaychk.getITAXLINE().add(itaxline);
        }

        return ipaychk;
    }

    public static IPAYCHK generatePaycheck(IEMP pEmployee,
                                           List<IPITEM> pPayrollItems,
                                           boolean isYTDAdj,
                                           Date pTransactionDate,
                                           Date pPeriodBeginDate,
                                           Date pPeriodEndDate,
                                           String pAccountName,
                                           String pClass,
                                           String pCleared,
                                           String pMemo,
                                           String pCheckNumber,
                                           double pSickAccrued,
                                           double pVacationAccrued) {
        return generatePaycheck(pEmployee,
                                pPayrollItems,
                                isYTDAdj,
                                pTransactionDate,
                                pPeriodBeginDate,
                                pPeriodEndDate,
                                pAccountName,
                                pClass,
                                pCleared,
                                pMemo,
                                pCheckNumber,
                                pSickAccrued,
                                pVacationAccrued,
                                false);
    }

    public static IPAYCHK generatePaycheck(IEMP pEmployee,
                                           List<IPITEM> pPayrollItems,
                                           boolean isYTDAdj,
                                           Date pTransactionDate,
                                           Date pPeriodBeginDate,
                                           Date pPeriodEndDate,
                                           String pAccountName,
                                           String pClass,
                                           String pCleared,
                                           String pMemo,
                                           String pCheckNumber,
                                           double pSickAccrued,
                                           double pVacationAccrued,
                                           boolean pIncludeDuplicateItems) {
        IPAYCHK ipaychk = new IPAYCHK();
        ipaychk.setIPAYCHKID(++mPaycheckId + "");
        ipaychk.setIEMPID(pEmployee.getIEMPID());
        ipaychk.setIPAYCHKTYPE(isYTDAdj ? "YTDADJ" : "PAYCHK");

        ipaychk.setIEMPNAME(pEmployee.getIEMPNAME());
        ipaychk.setIACCTNAME(pAccountName);
        ipaychk.setICLASS(pClass);
        ipaychk.setICLEARED(pCleared);
        ipaychk.setIDTPAYPDBEGIN(QBOFX.getDTTXResponse(pPeriodBeginDate));
        ipaychk.setIDTPAYPDEND(QBOFX.getDTTXResponse(pPeriodEndDate));
        ipaychk.setIDTTX(QBOFX.getDTTXResponse(pTransactionDate));
        ipaychk.setIMEMO(pMemo);
        ipaychk.setIONSERVICE(QBOFX.Y_N(!isYTDAdj));
        ipaychk.setIVOID(QBOFX.Y_N(false));

        IPAYCHKINFO ipaychkinfo = new IPAYCHKINFO();
        ipaychkinfo.setICHKNUM(pCheckNumber);
        ipaychkinfo.setIPRORATE(QBOFX.Y_N(false));
        ipaychkinfo.setISICKACCRUED(pSickAccrued + "");
        ipaychkinfo.setIVACACCRUED(pVacationAccrued + "");
        ipaychk.setIPAYCHKINFO(ipaychkinfo);

        for (IPITEM ipitem : pPayrollItems) {
            PayrollItem payrollItem = new PayrollItem(ipitem);
            switch (payrollItem.getItemType()) {
                case Deduction:
                    IADJLINE iadjline = new IADJLINE();
                    iadjline.setIAMT("$-1.00");
                    iadjline.setIEXPBYJOB(QBOFX.Y_N(true));
                    iadjline.setIPITEMID(payrollItem.getSourceId());
                    iadjline.setIQTY("50");
                    iadjline.setIRATE("0.52%");
                    iadjline.setIYTDAMT("$-2.00");
                    if(pIncludeDuplicateItems) {
                        ipaychk.getIADJLINE().add(iadjline);
                        ipaychk.getIADJLINE().add(iadjline);
                    } else {
                        ipaychk.getIADJLINE().add(iadjline);
                    }
                    break;
                case Addition:
                case EmployerContribution:
                    IADJLINE iPosLineItem = new IADJLINE();
                    iPosLineItem.setIAMT("$1.00");
                    iPosLineItem.setIEXPBYJOB(QBOFX.Y_N(true));
                    iPosLineItem.setIPITEMID(payrollItem.getSourceId());
                    if(payrollItem.getItemType() == QBOFX.OFXPayrollItemType.Addition ||
                            payrollItem.getItemType() == QBOFX.OFXPayrollItemType.Deduction) {
                        iPosLineItem.setIQTY("50");
                        iPosLineItem.setIRATE("0.52%");
                    }
                    iPosLineItem.setIYTDAMT("$2.00");
                    if(pIncludeDuplicateItems) {
                        ipaychk.getIADJLINE().add(iPosLineItem);
                        ipaychk.getIADJLINE().add(iPosLineItem);
                    } else {
                        ipaychk.getIADJLINE().add(iPosLineItem);
                    }
                    break;
                case DirectDeposit:
                    int amount = -3;
                    for (IDDACCT iddacct : pEmployee.getIEMPDD().getIDDACCT()) {
                        IDDLINE iddline = new IDDLINE();
                        iddline.setIAMT("$" + (amount--) +".00");
                        iddline.setIDDACCT(iddacct);
                        iddline.setIPITEMID(payrollItem.getSourceId());
                        // ignored
                        iddline.setIPITEMNAME("");
                        ipaychk.getIDDLINE().add(iddline);
                    }
                    break;
                case Hourly:
                case Bonus:
                case Commission:
                    IHRLYWAGELINE ihrlywageline = new IHRLYWAGELINE();
                    ihrlywageline.setIAMT("$4.00");
                    ihrlywageline.setICLASS("HRLY CLASS");
                    ihrlywageline.setIHRS("15");
                    ihrlywageline.setIITEM("HRLY ITEM");
                    ihrlywageline.setIJOB("HRLY JOB");
                    ihrlywageline.setIPITEMID(payrollItem.getSourceId());
                    ihrlywageline.setIRATE("0.123456%");
                    ihrlywageline.setIWCCODE("HRLY WCCODE");
                    ihrlywageline.setIYTDAMT("$5.00");
                    if(pIncludeDuplicateItems) {
                        ipaychk.getIHRLYWAGELINE().add(ihrlywageline);
                        ipaychk.getIHRLYWAGELINE().add(ihrlywageline);
                    } else {
                        ipaychk.getIHRLYWAGELINE().add(ihrlywageline);
                    }
                    break;
                case Salary:
                    ISALARYLINE isalaryline = new ISALARYLINE();
                    isalaryline.setIAMT("$6.00");
                    isalaryline.setICLASS("SALRY CLASS");
                    isalaryline.setIHRS("16");
                    isalaryline.setIITEM("SALRY ITEM");
                    isalaryline.setIJOB("SALRY JOB");
                    isalaryline.setIPITEMID(payrollItem.getSourceId());
                    isalaryline.setIRATE("0.78%");
                    isalaryline.setIWCCODE("SALRY WCCODE");
                    isalaryline.setIYTDAMT("$7.00");
                    if(pIncludeDuplicateItems) {
                        ipaychk.getISALARYLINE().add(isalaryline);
                        ipaychk.getISALARYLINE().add(isalaryline);
                    } else {
                        ipaychk.getISALARYLINE().add(isalaryline);
                    }
                    break;
                case Tax:
                    ITAXLINE itaxline = new ITAXLINE();
                    itaxline.setIAMT(payrollItem.getIsEmployeePaid() ? "$-8.00" : "$8.00");
                    itaxline.setIPITEMID(payrollItem.getSourceId());
                    itaxline.setITAXABLEWAGE("$9.00");
                    itaxline.setITIPSWB("$10.00");
                    itaxline.setIWB("$11.00");
                    itaxline.setIYTDAMT(payrollItem.getIsEmployeePaid() ? "$-12.00" : "$12.00");
                    ipaychk.getITAXLINE().add(itaxline);
                    break;
            }
        }

        ipaychk.setIAMT("$100.00");

        return ipaychk;
    }

    public static IPAYROLLRUN generatePayrollRun(Company pCompany, PayrollRunDTO pPayrollRunDTO, boolean pIsYearToDate, boolean pIsModification) {
        IPAYROLLRUN ipayrollrun = new IPAYROLLRUN();

        ipayrollrun.setIDTPAYCHKS(QBOFX.getDTTXResponse(CalendarUtils.convertToDate(pPayrollRunDTO.getTargetPayrollTXDate().toSpcfCalendar())));
        for (PaycheckDTO paycheckDTO : pPayrollRunDTO.getPaychecks()) {
            if(paycheckDTO.getCompensationTransactions().size() == 0 &&
                    paycheckDTO.getDdTransactions().size() == 0 &&
                    paycheckDTO.getDeductionTransactions().size() == 0 &&
                    paycheckDTO.getEmployerContributionTransactions().size() == 0 &&
                    paycheckDTO.getLiabilityTransactions().size() == 0) {
                continue;
            }

            if(pIsModification) {
                ipayrollrun.getIPAYCHKMOD().add(generatePaycheck(pCompany, pPayrollRunDTO, paycheckDTO, pIsYearToDate));
            } else {
                ipayrollrun.getIPAYCHK().add(generatePaycheck(pCompany, pPayrollRunDTO, paycheckDTO, pIsYearToDate));
            }
        }

        return ipayrollrun;
    }

    public static IPAYCHK generatePaycheck(Company pCompany, PayrollRunDTO pPayrollRunDTO, PaycheckDTO pPaycheckDTO, boolean isYTDAdj) {
        IPAYCHK ipaychk = new IPAYCHK();
        ipaychk.setIPAYCHKID(pPaycheckDTO.getPaycheckId());
        if(!Paycheck.NOT_ALL_DIGITS_PATTERN.matcher(pPaycheckDTO.getEmployeeId()).matches()) {
            ipaychk.setIEMPID(pPaycheckDTO.getEmployeeId());
            ipaychk.setIEMPNAME("");
        } else {
            ipaychk.setIEMPID(QBOFX.DEFAULT_PITEM_ID);
            PayrollServices.beginUnitOfWork();
            ipaychk.setIEMPNAME(Employee.findEmployee(pCompany, pPaycheckDTO.getEmployeeId()).getFirstMiddleLastName());
            PayrollServices.rollbackUnitOfWork();
        }
        ipaychk.setIPAYCHKTYPE(isYTDAdj ? "YTDADJ" : "PAYCHK");

        ipaychk.setICLASS(QBOFX.NULL);
        ipaychk.setICLEARED(QBOFX.DEFAULT_CLEARED_RESPONSE_STR);
        ipaychk.setIMEMO(QBOFX.NULL);
        if(pPaycheckDTO.getQBDTPaycheckInfoDTO() != null) {
            QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = pPaycheckDTO.getQBDTPaycheckInfoDTO();
            ipaychk.setIQBUNIQUEID(qbdtPaycheckInfoDTO.getListId());
            ipaychk.setIACCTNAME(qbdtPaycheckInfoDTO.getAccountName());
            ipaychk.setICLASS(qbdtPaycheckInfoDTO.getTrackingClass());
            ipaychk.setICLEARED(qbdtPaycheckInfoDTO.getCleared());
            ipaychk.setIMEMO(qbdtPaycheckInfoDTO.getMemo());
            ipaychk.setIONSERVICE(QBOFX.Y_N(qbdtPaycheckInfoDTO.isOnService()));

            IPAYCHKINFO ipaychkinfo = new IPAYCHKINFO();
            ipaychk.setIPAYCHKINFO(ipaychkinfo);
            ipaychkinfo.setIPRORATE(QBOFX.Y_N(qbdtPaycheckInfoDTO.isProrate()));
            ipaychkinfo.setICHKNUM(qbdtPaycheckInfoDTO.getCheckNumber());
            ipaychkinfo.setISICKACCRUED(qbdtPaycheckInfoDTO.getSickHoursAccrued() + "");
            ipaychkinfo.setIVACACCRUED(qbdtPaycheckInfoDTO.getVacationHoursAccrued() + "");
        }

        String paycheckDate = QBOFX.getDTTXResponse(CalendarUtils.convertToDate(pPayrollRunDTO.getTargetPayrollTXDate().toSpcfCalendar()));
        ipaychk.setIDTPAYPDBEGIN(paycheckDate);
        ipaychk.setIDTPAYPDEND(paycheckDate);
        ipaychk.setIDTTX(paycheckDate);

        if(pPaycheckDTO.getPayPeriodBeginDate() != null) {
            ipaychk.setIDTPAYPDBEGIN(QBOFX.getDTTXResponse(CalendarUtils.convertToDate(pPaycheckDTO.getPayPeriodBeginDate().toSpcfCalendar())));
        }
        if(pPaycheckDTO.getPayPeriodEndDate() != null) {
            ipaychk.setIDTPAYPDEND(QBOFX.getDTTXResponse(CalendarUtils.convertToDate(pPaycheckDTO.getPayPeriodEndDate().toSpcfCalendar())));
        }

        ipaychk.setIVOID(pPaycheckDTO.isVoid() ? QBOFX.Y_N(true) : QBOFX.Y_N(false));

        for (CompensationTransactionDTO compensationTransactionDTO : pPaycheckDTO.getCompensationTransactions()) {
            PayrollServices.beginUnitOfWork();
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, compensationTransactionDTO.getSourcePayrollItemId());
            QBOFX.OFXPayrollItemType ofxPayrollItemType = QBOFX.mapOFXPayrollItemType(companyPayrollItem.getPayrollItem().getPayrollItemCode());
            PayrollServices.rollbackUnitOfWork();
            addPaycheckDetail(compensationTransactionDTO, ipaychk, ofxPayrollItemType);
        }

        for (DeductionTransactionDTO deductionTransactionDTO : pPaycheckDTO.getDeductionTransactions()) {
            PayrollServices.beginUnitOfWork();
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, deductionTransactionDTO.getSourcePayrollItemId());
            QBOFX.OFXPayrollItemType ofxPayrollItemType = QBOFX.mapOFXPayrollItemType(companyPayrollItem.getPayrollItem().getPayrollItemCode());
            PayrollServices.rollbackUnitOfWork();
            addPaycheckDetail(deductionTransactionDTO, ipaychk, ofxPayrollItemType);
        }

        for (EmployerContributionTransactionDTO employerContributionTransactionDTO : pPaycheckDTO.getEmployerContributionTransactions()) {
            PayrollServices.beginUnitOfWork();
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, employerContributionTransactionDTO.getSourcePayrollItemId());
            QBOFX.OFXPayrollItemType ofxPayrollItemType = QBOFX.mapOFXPayrollItemType(companyPayrollItem.getPayrollItem().getPayrollItemCode());
            PayrollServices.rollbackUnitOfWork();
            addPaycheckDetail(employerContributionTransactionDTO, ipaychk, ofxPayrollItemType);
        }

        for (LiabilityTransactionDTO liabilityTransactionDTO : pPaycheckDTO.getLiabilityTransactions()) {
            PayrollServices.beginUnitOfWork();
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(pCompany, liabilityTransactionDTO.getPayrollItemId());
            boolean isEmployeePaid = companyLaw.getQbdtPayrollItemInfo().getIsEmployeePaid();
            PayrollServices.rollbackUnitOfWork();
            addPaycheckDetail(liabilityTransactionDTO, ipaychk, isEmployeePaid);
        }

        for (DDTransactionDTO ddTransactionDTO : pPaycheckDTO.getDdTransactions()) {
            addPaycheckDetail(ddTransactionDTO, ipaychk, pCompany);
        }

        ipaychk.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pPaycheckDTO.getPaycheckNetAmount()));

        return ipaychk;
    }

    private static void addPaycheckDetail(CompensationTransactionDTO pCompensationTransactionDTO,
                                          IPAYCHK pIpaychk,
                                          QBOFX.OFXPayrollItemType pPayrollItemType) {
        switch (pPayrollItemType) {
            case Hourly:
            case Bonus:
            case Commission:
                IHRLYWAGELINE ihrlywageline = new IHRLYWAGELINE();
                ihrlywageline.setIPITEMID(pCompensationTransactionDTO.getSourcePayrollItemId());
                ihrlywageline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensationTransactionDTO.getCompensationAmount()));
                ihrlywageline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensationTransactionDTO.getCompensationYTDAmount()));
                ihrlywageline.setIHRS(QBOFX.convertSpcfDecimalToOFXString(pCompensationTransactionDTO.getHoursWorked()));

                if(pCompensationTransactionDTO.getQBDTPaylineInfoDTO() != null) {
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = pCompensationTransactionDTO.getQBDTPaylineInfoDTO();
                    ihrlywageline.setICLASS(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getTrackingClass()));
                    ihrlywageline.setIITEM(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getItem()));
                    ihrlywageline.setIJOB(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getJob()));
                    ihrlywageline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfoDTO.getRateType(), qbdtPaylineInfoDTO.getRate()));
                    ihrlywageline.setIWCCODE(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getWcCode()));
                }

                pIpaychk.getIHRLYWAGELINE().add(ihrlywageline);
                break;
            case Salary:
                ISALARYLINE isalaryline = new ISALARYLINE();
                isalaryline.setIPITEMID(pCompensationTransactionDTO.getSourcePayrollItemId());
                isalaryline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensationTransactionDTO.getCompensationAmount()));
                isalaryline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensationTransactionDTO.getCompensationYTDAmount()));
                isalaryline.setIHRS(QBOFX.convertSpcfDecimalToOFXString(pCompensationTransactionDTO.getHoursWorked()));

                if(pCompensationTransactionDTO.getQBDTPaylineInfoDTO() != null) {
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = pCompensationTransactionDTO.getQBDTPaylineInfoDTO();
                    isalaryline.setICLASS(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getTrackingClass()));
                    isalaryline.setIITEM(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getItem()));
                    isalaryline.setIJOB(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getJob()));
                    isalaryline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfoDTO.getRateType(), qbdtPaylineInfoDTO.getRate()));
                    isalaryline.setIWCCODE(QBOFX.convertNullToOFXString(qbdtPaylineInfoDTO.getWcCode()));
                }
                pIpaychk.getISALARYLINE().add(isalaryline);
                break;
            default:
                throw new RuntimeException("Item type " + pPayrollItemType + " not expected");
        }
    }

    private static void addPaycheckDetail(DeductionTransactionDTO pDeductionTransactionDTO,
                                          IPAYCHK pIpaychk,
                                          QBOFX.OFXPayrollItemType pPayrollItemType) {
        switch (pPayrollItemType) {
            case Deduction:
            case Addition:
                IADJLINE iadjline = new IADJLINE();
                iadjline.setIPITEMID(pDeductionTransactionDTO.getSourcePayrollItemId());
                iadjline.setIAMT(QBOFX.convertSpcfMoneyToOFXString((SpcfMoney)SpcfUtils.convertToSpcfDecimal(pDeductionTransactionDTO.getDeductionAmount())));
                iadjline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString((SpcfMoney) SpcfUtils.convertToSpcfDecimal(pDeductionTransactionDTO.getDeductionYTDAmount())));

                if(pDeductionTransactionDTO.getQBDTPaylineInfoDTO() != null) {
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = pDeductionTransactionDTO.getQBDTPaylineInfoDTO();
                    iadjline.setIEXPBYJOB(QBOFX.Y_N(qbdtPaylineInfoDTO.isExpenseByJob()));
                    iadjline.setIQTY(QBOFX.mapNumericTypeToString(qbdtPaylineInfoDTO.getQuantityType(), qbdtPaylineInfoDTO.getQuantity()));
                    iadjline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfoDTO.getRateType(), qbdtPaylineInfoDTO.getRate()));
                }
                pIpaychk.getIADJLINE().add(iadjline);
                break;
            default:
                throw new RuntimeException("Item type " + pPayrollItemType + " not expected");
        }
    }

    private static void addPaycheckDetail(EmployerContributionTransactionDTO pEmployerContributionTransactionDTO,
                                          IPAYCHK pIpaychk,
                                          QBOFX.OFXPayrollItemType pPayrollItemType) {
        switch (pPayrollItemType) {
            case EmployerContribution:
                IADJLINE iadjline = new IADJLINE();
                iadjline.setIPITEMID(pEmployerContributionTransactionDTO.getSourcePayrollItemId());
                iadjline.setIAMT(QBOFX.convertSpcfMoneyToOFXString((SpcfMoney) SpcfUtils.convertToSpcfDecimal(pEmployerContributionTransactionDTO.getContributionAmount())));
                iadjline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString((SpcfMoney) SpcfUtils.convertToSpcfDecimal(pEmployerContributionTransactionDTO.getContributionYTDAmount())));

                if(pEmployerContributionTransactionDTO.getQBDTPaylineInfoDTO() != null) {
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = pEmployerContributionTransactionDTO.getQBDTPaylineInfoDTO();
                    iadjline.setIEXPBYJOB(QBOFX.Y_N(qbdtPaylineInfoDTO.isExpenseByJob()));
                    iadjline.setIQTY(QBOFX.mapNumericTypeToString(qbdtPaylineInfoDTO.getQuantityType(), qbdtPaylineInfoDTO.getQuantity()));
                    iadjline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfoDTO.getRateType(), qbdtPaylineInfoDTO.getRate()));
                }
                pIpaychk.getIADJLINE().add(iadjline);
                break;
            default:
                throw new RuntimeException("Item type " + pPayrollItemType + " not expected");
        }
    }

    private static void addPaycheckDetail(LiabilityTransactionDTO pLiabilityTransactionDTO,
                                          IPAYCHK pIpaychk,
                                          boolean pIsEmployeePaid) {
        ITAXLINE itaxline = new ITAXLINE();
        itaxline.setIPITEMID(pLiabilityTransactionDTO.getPayrollItemId());
        itaxline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(SpcfUtils.convertToSpcfMoney(pIsEmployeePaid ? pLiabilityTransactionDTO.getLiabilityAmount().negate() : pLiabilityTransactionDTO.getLiabilityAmount())));

        if(pLiabilityTransactionDTO.getLiabilityAmountYTD() != null) {
            itaxline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(SpcfUtils.convertToSpcfMoney(pIsEmployeePaid ? pLiabilityTransactionDTO.getLiabilityAmountYTD().negate() : pLiabilityTransactionDTO.getLiabilityAmountYTD())));
        } else {
            itaxline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(SpcfMoney.ZERO));
        }

        itaxline.setIWB(QBOFX.convertSpcfMoneyToOFXString(SpcfUtils.convertToSpcfMoney(pLiabilityTransactionDTO.getLiabilityTaxableWages())));
        itaxline.setITIPSWB(QBOFX.convertSpcfMoneyToOFXString(SpcfUtils.convertToSpcfMoney(pLiabilityTransactionDTO.getLiabilityTipsTaxableWages())));
        itaxline.setITAXABLEWAGE(QBOFX.convertSpcfMoneyToOFXString(SpcfUtils.convertToSpcfMoney(pLiabilityTransactionDTO.getLiabilityTotalWages())));
        pIpaychk.getITAXLINE().add(itaxline);
    }

    private static void addPaycheckDetail(DDTransactionDTO pDDTransactionDTO,
                                          IPAYCHK pIpaychk,
                                          Company pCompany) {
        IDDLINE iddline = new IDDLINE();
        PayrollServices.beginUnitOfWork();
        CompanyPayrollItem ddItem = CompanyPayrollItem.findDirectDepositPayrollItem(pCompany);
        PayrollServices.rollbackUnitOfWork();

        if(ddItem != null && ddItem.getSourcePayrollItemId().matches("\\d")) {
            iddline.setIPITEMID(ddItem.getSourcePayrollItemId());
        } else {
            iddline.setIPITEMID(QBOFX.DEFAULT_PITEM_ID);
        }
        iddline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(SpcfUtils.convertToSpcfMoney(pDDTransactionDTO.getDDTransactionAmount().negate())));

        IDDACCT iddacct = new IDDACCT();
        iddline.setIDDACCT(iddacct);

        BANKACCT bankacct = new BANKACCT();
        iddacct.setBANKACCTTO(bankacct);

        if(pDDTransactionDTO.getEmployeeBankAccount() != null && pDDTransactionDTO.getEmployeeBankAccount().getBankAccount() != null) {
            BankAccountDTO bankAccountDTO = pDDTransactionDTO.getEmployeeBankAccount().getBankAccount();
            iddacct.setIACCTNAME(bankAccountDTO.getBankName());
            bankacct.setBANKID(bankAccountDTO.getRoutingNumber());
            bankacct.setACCTID(bankAccountDTO.getAccountNumber());
            bankacct.setACCTTYPE(QBOFX.mapOFXBankAccountType(bankAccountDTO.getAccountType()));
        }

        // ignored
        iddline.setIPITEMNAME("");

        pIpaychk.getIDDLINE().add(iddline);
    }

    static class OFXPayrollItemGenerator {
        public static IPITEM generatePI(String pItemName) {
            IPITEM ipitem = new IPITEM();
            ipitem.setIINACTIVE(QBOFX.Y_N(false));
            ipitem.setIPITEMID(Integer.toString(++mPayrollItemId));
            ipitem.setIISEMP(QBOFX.Y_N(true));
            ipitem.setIPITEMNAME(pItemName);
            ipitem.setISPECIALTYPE(null);

            ipitem.setIADDITEM(null);
            ipitem.setIBONUSITEM(null);
            ipitem.setICOMMITEM(null);
            ipitem.setICONTRIBITEM(null);
            ipitem.setIDDITEM(null);
            ipitem.setIDEDUCTITEM(null);
            ipitem.setIHRLYITEM(null);
            ipitem.setISALARYITEM(null);
            ipitem.setITAXITEM(null);

            return ipitem;
        }

        public static IPITEM generateSalaryPI() {
            IPITEM salaryItem = generatePI("Salary");

            ISALARYITEM isalaryitem = new ISALARYITEM();
            isalaryitem.setIEXPACCT(EXPENSE_ACCOUNT);
            isalaryitem.setIPAYTYPE("REG");
            salaryItem.setISALARYITEM(isalaryitem);
            
            return salaryItem;
        }
    }

    public static List<IPITEM> generateFedTaxes() {
        List<IPITEM> taxes = new ArrayList<IPITEM>();

        IPITEM aeic = generatePayrollItemTax(false,
                                             true,
                                             "Advance Earned Income Credit",
                                             "AEIC",
                                             generateTaxItem(false,
                                                             false,
                                                             null,
                                                             null,
                                                             EXPENSE_ACCOUNT,
                                                             "AEIC",
                                                             LIABILITY_ACCOUNT,
                                                             "",
                                                             true,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             null));
        taxes.add(aeic);

        IPITEM futa = generatePayrollItemTax(false,
                                             true,
                                             "Federal Unemployment",
                                             "FUTA",
                                             generateTaxItem(false,
                                                             false,
                                                             null,
                                                             new SpcfMoney("7000.00"),
                                                             EXPENSE_ACCOUNT,
                                                             "FUTA",
                                                             LIABILITY_ACCOUNT,
                                                             "",
                                                             true,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             0.8,
                                                             null));
        taxes.add(futa);

        IPITEM fit = generatePayrollItemTax(false,
                                            true,
                                            "Federal Withholding",
                                            "FEDTAX",
                                            generateTaxItem(false,
                                                            false,
                                                            null,
                                                            null,
                                                            EXPENSE_ACCOUNT,
                                                            "FIT",
                                                            LIABILITY_ACCOUNT,
                                                            "",
                                                            true,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null));
        taxes.add(fit);

        IPITEM meder = generatePayrollItemTax(false,
                                              false,
                                              "Medicare Company",
                                              "COMCARE",
                                              generateTaxItem(false,
                                                              false,
                                                              null,
                                                              null,
                                                              EXPENSE_ACCOUNT,
                                                              "MED_ER",
                                                              LIABILITY_ACCOUNT,
                                                              "",
                                                              true,
                                                              null,
                                                              null,
                                                              null,
                                                              null,
                                                              1.45,
                                                              null));
        taxes.add(meder);

        IPITEM medee = generatePayrollItemTax(false,
                                              true,
                                              "Medicare Employee",
                                              "EEMCARE",
                                              generateTaxItem(false,
                                                              false,
                                                              null,
                                                              null,
                                                              EXPENSE_ACCOUNT,
                                                              "MED_EE",
                                                              LIABILITY_ACCOUNT,
                                                              "",
                                                              true,
                                                              null,
                                                              null,
                                                              null,
                                                              null,
                                                              -1.45,
                                                              null));
        taxes.add(medee);

        IPITEM sser = generatePayrollItemTax(false,
                                             false,
                                             "Social & Security Company",
                                             "COSSEC",
                                             generateTaxItem(false,
                                                             false,
                                                             null,
                                                             new SpcfMoney("106800.00"),
                                                             EXPENSE_ACCOUNT,
                                                             "SS_ER",
                                                             LIABILITY_ACCOUNT,
                                                             "",
                                                             true,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             6.2,
                                                             null));
        taxes.add(sser);

        IPITEM ssee = generatePayrollItemTax(false,
                                             true,
                                             "Social Security Employee",
                                             "EESSEC",
                                             generateTaxItem(false,
                                                             false,
                                                             null,
                                                             new SpcfMoney("-106800.00"),
                                                             EXPENSE_ACCOUNT,
                                                             "SS_EE",
                                                             LIABILITY_ACCOUNT,
                                                             "",
                                                             true,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             -6.2,
                                                             null));
        taxes.add(ssee);

        return taxes;
    }

    public static IPITEM generateCobraPITEM() {
        return  OFXRequestGenerator.generatePayrollItemTax(false,
                                                   false,
                                                   "Cobra",
                                                   null,
                                                   OFXRequestGenerator.generateTaxItem(false,
                                                                                       false,
                                                                                       null,
                                                                                       null,
                                                                                       OFXRequestGenerator.EXPENSE_ACCOUNT,
                                                                                       null,
                                                                                       OFXRequestGenerator.LIABILITY_ACCOUNT,
                                                                                       "",
                                                                                       false,
                                                                                       "196",
                                                                                       null,
                                                                                       null,
                                                                                       null,
                                                                                       null,
                                                                                       null));
    }

    public static List<IPITEM> generateCATaxes() {
        List<IPITEM> taxes = new ArrayList<IPITEM>();

        IPITEM sit = generatePayrollItemTax(false,
                                            true,
                                            "CA - Withholding",
                                            null,
                                            generateTaxItem(false,
                                                            false,
                                                            "999-9999-5",
                                                            null,
                                                            EXPENSE_ACCOUNT,
                                                            null,
                                                            LIABILITY_ACCOUNT,
                                                            "Franchise Tax Board",
                                                            true,
                                                            null,
                                                            "CA",
                                                            "SIT",
                                                            null,
                                                            null,
                                                            null));
        taxes.add(sit);

        IPITEM sdi = generatePayrollItemTax(false,
                                            true,
                                            "CA - Disability",
                                            null,
                                            generateTaxItem(false,
                                                            false,
                                                            "999-9999-5",
                                                            new SpcfMoney("-93316.00"),
                                                            EXPENSE_ACCOUNT,
                                                            null,
                                                            LIABILITY_ACCOUNT,
                                                            "Franchise Tax Board",
                                                            true,
                                                            null,
                                                            "CA",
                                                            "SDI_EE",
                                                            null,
                                                            -1.1,
                                                            null));
        taxes.add(sdi);

        Map<String, Double> rateChanges = new HashMap<String, Double>();
        rateChanges.put("20101001", 3.4);
        rateChanges.put("20100101", 0.0);
        IPITEM sui = generatePayrollItemTax(false,
                                            false,
                                            "CA - Unemployment",
                                            null,
                                            generateTaxItem(false,
                                                            false,
                                                            "999-9999-5",
                                                            new SpcfMoney("7000.00"),
                                                            EXPENSE_ACCOUNT,
                                                            null,
                                                            LIABILITY_ACCOUNT,
                                                            "EDD",
                                                            true,
                                                            null,
                                                            "CA",
                                                            "SUI_ER",
                                                            null,
                                                            3.4,
                                                            rateChanges));
        taxes.add(sui);

        IPITEM ett = generatePayrollItemTax(false,
                                            false,
                                            "CA - Employment Training Tax",
                                            null,
                                            generateTaxItem(false,
                                                            false,
                                                            "999-9999-5",
                                                            new SpcfMoney("7000.00"),
                                                            EXPENSE_ACCOUNT,
                                                            null,
                                                            LIABILITY_ACCOUNT,
                                                            "Franchise Tax Board",
                                                            true,
                                                            "142",
                                                            null,
                                                            null,
                                                            null,
                                                            0.1,
                                                            null));
        taxes.add(ett);

        return taxes;
    }

    public static List<IPITEM> generatePATaxes() {
        List<IPITEM> taxes = new ArrayList<IPITEM>();

        IPITEM sit = generatePayrollItemTax(false,
                                            true,
                                            "PA - Withholding",
                                            null,
                                            generateTaxItem(false,
                                                            false,
                                                            "999-9999-6",
                                                            null,
                                                            EXPENSE_ACCOUNT,
                                                            null,
                                                            LIABILITY_ACCOUNT,
                                                            "Franchise Tax Board",
                                                            true,
                                                            null,
                                                            "PA",
                                                            "SIT",
                                                            null,
                                                            null,
                                                            null));
        taxes.add(sit);

        return taxes;
    }

    public static List<IPITEM> generateGATaxes() {
        List<IPITEM> taxes = new ArrayList<IPITEM>();

        IPITEM sit = generatePayrollItemTax(false,
                                            true,
                                            "GA - Withholding",
                                            null,
                                            generateTaxItem(false,
                                                            false,
                                                            "999-9999-6",
                                                            null,
                                                            EXPENSE_ACCOUNT,
                                                            null,
                                                            LIABILITY_ACCOUNT,
                                                            "Department Of Revenue",
                                                            true,
                                                            null,
                                                            "GA",
                                                            "SIT",
                                                            null,
                                                            null,
                                                            null));
        taxes.add(sit);

        return taxes;
    }
    // generate methods

    public static List<IPITEM> generatePayrollItems(List<IPITEM> taxes,
                                                    QBOFX.OFXPayrollItemType... pItemTypes) {
        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (QBOFX.OFXPayrollItemType itemType : pItemTypes) {
            switch (itemType) {
                case Addition:
                    ipitems.add(generatePayrollItem(false,
                                                    false,
                                                    "Addition " + (mPayrollItemId + 1),
                                                    null,
                                                    generateAddition(true,
                                                                     false,
                                                                     null,
                                                                     null,
                                                                     null,
                                                                     null,
                                                                     EXPENSE_ACCOUNT,
                                                                     false,
                                                                     LIABILITY_ACCOUNT,
                                                                     null,
                                                                     false,
                                                                     null,
                                                                     taxes),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
                    break;
                case Bonus:
                    IBONUSITEM ibonusitem = new IBONUSITEM();
                    ibonusitem.setIEXPACCT(EXPENSE_ACCOUNT);
                    ipitems.add(generatePayrollItem(false,
                                                    false,
                                                    "Bonus " + (mPayrollItemId + 1),
                                                    null,
                                                    null,
                                                    ibonusitem,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
                    break;
                case Commission:
                    ipitems.add(generatePayrollItem(false,
                                                    false,
                                                    "Commission " + (mPayrollItemId + 1),
                                                    null,
                                                    null,
                                                    null,
                                                    generateCommission(true, true, null, null, null, EXPENSE_ACCOUNT, false, true),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
                    break;
                case Deduction:
                    ipitems.add(generatePayrollItem(false,
                                                    true,
                                                    "Deduction " + (mPayrollItemId + 1),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    generateDeduction(false, false, "", null, null, null, LIABILITY_ACCOUNT, "", false, "NONE", taxes),
                                                    null,
                                                    null,
                                                    null));
                    break;
                case DirectDeposit:
                    IDDITEM idditem = new IDDITEM();
                    idditem.setICOMPID("");
                    idditem.setILIABAGENCY("");
                    idditem.setILIABACCT(LIABILITY_ACCOUNT);
                    ipitems.add(generatePayrollItem(false,
                                                    true,
                                                    "DD " + (mPayrollItemId + 1),
                                                    "DIRDEP",
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    idditem,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
                    break;
                case EmployerContribution:
                    ipitems.add(generatePayrollItem(false,
                                                    false,
                                                    "Contribution " + (mPayrollItemId + 1),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    generateContribution(true,
                                                                         false,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         EXPENSE_ACCOUNT,
                                                                         false,
                                                                         LIABILITY_ACCOUNT,
                                                                         null,
                                                                         false,
                                                                         null,
                                                                         taxes),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
                    break;
                case Hourly:
                    IHRLYITEM ihrlyitem = new IHRLYITEM();
                    ihrlyitem.setIEXPACCT(EXPENSE_ACCOUNT);
                    ihrlyitem.setIPAYTYPE("REG");
                    ipitems.add(generatePayrollItem(false,
                                                    false,
                                                    "Hourly " + (mPayrollItemId + 1),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    ihrlyitem,
                                                    null,
                                                    null));
                    break;
                case Salary:
                    ISALARYITEM isalaryitem = new ISALARYITEM();
                    isalaryitem.setIEXPACCT(EXPENSE_ACCOUNT);
                    isalaryitem.setIPAYTYPE("REG");
                    ipitems.add(generatePayrollItem(false,
                                                    false,
                                                    "Salary " + (mPayrollItemId + 1),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    isalaryitem,
                                                    null));
                    break;
                case Tax:
                    throw new UnsupportedOperationException("Use generateTaxItem");
            }
        }
        return ipitems;
    }

    public static IPITEM generatePayrollItemTax(boolean pIsInactive,
                                                boolean pIsEmployeePaid,
                                                String pItemName,
                                                String pSpecialType,
                                                ITAXITEM pTaxItem) {
        return generatePayrollItem(pIsInactive,
                                   pIsEmployeePaid,
                                   pItemName,
                                   pSpecialType,
                                   null,
                                   null,
                                   null,
                                   null,
                                   null,
                                   null,
                                   null,
                                   null,
                                   pTaxItem);
    }

    public static IPITEM generatePayrollItem(boolean pIsInactive,
                                             boolean pIsEmployeePaid,
                                             String pItemName,
                                             String pSpecialType,
                                             IADDITEM pAdditionItem,
                                             IBONUSITEM pBonusItem,
                                             ICOMMITEM pCommisionItem,
                                             ICONTRIBITEM pContributionItem,
                                             IDDITEM pDirectDepositItem,
                                             IDEDUCTITEM pDeductionItem,
                                             IHRLYITEM pHourlyItem,
                                             ISALARYITEM pSalaryItem,
                                             ITAXITEM pTaxItem) {
        IPITEM ipitem = new IPITEM();
        ipitem.setIINACTIVE(QBOFX.Y_N(pIsInactive));
        ipitem.setIPITEMID(Integer.toString(++mPayrollItemId));
        ipitem.setIISEMP(QBOFX.Y_N(pIsEmployeePaid));
        ipitem.setIPITEMNAME(pItemName);
        ipitem.setISPECIALTYPE(pSpecialType);

        ipitem.setIADDITEM(pAdditionItem);
        ipitem.setIBONUSITEM(pBonusItem);
        ipitem.setICOMMITEM(pCommisionItem);
        ipitem.setICONTRIBITEM(pContributionItem);
        ipitem.setIDDITEM(pDirectDepositItem);
        ipitem.setIDEDUCTITEM(pDeductionItem);
        ipitem.setIHRLYITEM(pHourlyItem);
        ipitem.setISALARYITEM(pSalaryItem);
        ipitem.setITAXITEM(pTaxItem);

        return ipitem;
    }

    public static IDEDUCTITEM generateDeduction(boolean pAdjustsGross,
                                                boolean pBasedOnQuantity,
                                                String pAgencyId,
                                                SpcfMoney pDefaultLimit,
                                                Double pDefaultRate,
                                                QbdtNumericType pDefaultRateType,
                                                String pLiabilityAccount,
                                                String pLiabilityAgency,
                                                boolean pOnService,
                                                String pTaxFormLine,
                                                List<IPITEM> pSubjectToTaxes) {
        IDEDUCTITEM ideductitem = new IDEDUCTITEM();
        ideductitem.setIADJGROSS(QBOFX.Y_N(pAdjustsGross));
        ideductitem.setIBASEDONQTY(QBOFX.Y_N(pBasedOnQuantity));

        if(pAgencyId == null) {
            ideductitem.setICOMPID("");
        } else {
            ideductitem.setICOMPID(pAgencyId);
        }

        if(pDefaultLimit != null) {
            ideductitem.setIDEFLIMIT("$" + pDefaultLimit.toString());
        } else {
            ideductitem.setIDEFLIMIT("");
        }

        if(pDefaultRate != null && pDefaultRateType != null) {
            if(pDefaultRateType == QbdtNumericType.MoneyType) {
                ideductitem.setIDEFRATE("$" + pDefaultRate);
            } else {
                ideductitem.setIDEFRATE(pDefaultRate + "%");
            }
        } else {
            ideductitem.setIDEFRATE("$0.00");
        }

        ideductitem.setILIABACCT(pLiabilityAccount != null ? pLiabilityAccount : "");
        ideductitem.setILIABAGENCY(pLiabilityAgency != null ? pLiabilityAgency : "");        

        ideductitem.setIONSERVICE(QBOFX.Y_N(pOnService));
        ideductitem.setITAXFORMLINE(pTaxFormLine);

        for (IPITEM subjectToTax : pSubjectToTaxes) {
            ideductitem.getITAXAFFECTED().add(subjectToTax.getIPITEMID());
        }

        return ideductitem;
    }

    public static ICOMMITEM generateCommission(boolean pAdjustsGross,
                                               boolean pBasedOnQuantity,
                                               SpcfMoney pDefaultLimit,
                                               Double pDefaultRate,
                                               QbdtNumericType pDefaultRateType,
                                               String pExpenseAccount,
                                               boolean pExpenseByJob,
                                               boolean pEarningsTable) {
        ICOMMITEM icommitem = new ICOMMITEM();
        icommitem.setIADJGROSS(QBOFX.Y_N(pAdjustsGross));
        icommitem.setIBASEDONQTY(QBOFX.Y_N(pBasedOnQuantity));
        if(pDefaultLimit != null) {
            icommitem.setIDEFLIMIT("$" + pDefaultLimit.toString());
        }
        if(pDefaultRate != null && pDefaultRateType != null) {
            if(pDefaultRateType == QbdtNumericType.MoneyType) {
                icommitem.setIDEFRATE("$" + pDefaultRate);
            } else {
                icommitem.setIDEFRATE(pDefaultRate + "%");
            }
        } else {
            icommitem.setIDEFRATE("$0.00");
        }
        icommitem.setIEXPACCT(pExpenseAccount);
        icommitem.setIEXPBYJOB(QBOFX.Y_N(pExpenseByJob));
        icommitem.setIEARNINGSTABLE(QBOFX.Y_N(pEarningsTable));

        return icommitem;
    }

    public static ICONTRIBITEM generateContribution(boolean pAdjustsGross,
                                                    boolean pBasedOnQuantity,
                                                    String pAgencyId,
                                                    SpcfMoney pDefaultLimit,
                                                    Double pDefaultRate,
                                                    QbdtNumericType pDefaultRateType,
                                                    String pExpenseAccount,
                                                    boolean pExpenseByJob,
                                                    String pLiabilityAccount,
                                                    String pLiabilityAgency,
                                                    boolean pOnService,
                                                    String pTaxFormLine,
                                                    List<IPITEM> pSubjectToTaxes) {
        ICONTRIBITEM icontribitem = new ICONTRIBITEM();
        icontribitem.setIADJGROSS(QBOFX.Y_N(pAdjustsGross));
        icontribitem.setIBASEDONQTY(QBOFX.Y_N(pBasedOnQuantity));
        icontribitem.setICOMPID(pAgencyId != null ? pAgencyId : "");
        if(pDefaultLimit != null) {
            icontribitem.setIDEFLIMIT("$" + pDefaultLimit.toString());
        } else {
            icontribitem.setIDEFLIMIT("");
        }
        if(pDefaultRate != null && pDefaultRateType != null) {
            if(pDefaultRateType == QbdtNumericType.MoneyType) {
                icontribitem.setIDEFRATE("$" + pDefaultRate);
            } else {
                icontribitem.setIDEFRATE(pDefaultRate + "%");
            }
        } else {
            icontribitem.setIDEFRATE("$0.00");
        }
        icontribitem.setIEXPACCT(pExpenseAccount);
        icontribitem.setIEXPBYJOB(QBOFX.Y_N(pExpenseByJob));
        icontribitem.setILIABACCT(pLiabilityAccount != null ? pLiabilityAccount : "");
        icontribitem.setILIABAGENCY(pLiabilityAgency != null ? pLiabilityAgency : "");
        icontribitem.setIONSERVICE(QBOFX.Y_N(pOnService));
        icontribitem.setITAXFORMLINE(pTaxFormLine);

        for (IPITEM subjectToTax : pSubjectToTaxes) {
            icontribitem.getITAXAFFECTED().add(subjectToTax.getIPITEMID());
        }

        return icontribitem;
    }

    public static IADDITEM generateAddition(boolean pAdjustsGross,
                                            boolean pBasedOnQuantity,
                                            String pAgencyId,
                                            SpcfMoney pDefaultLimit,
                                            Double pDefaultRate,
                                            QbdtNumericType pDefaultRateType,
                                            String pExpenseAccount,
                                            boolean pExpenseByJob,
                                            String pLiabilityAccount,
                                            String pLiabilityAgency,
                                            boolean pOnService,
                                            String pTaxFormLine,
                                            List<IPITEM> pSubjectToTaxes) {
        IADDITEM iadditem = new IADDITEM();
        iadditem.setIADJGROSS(QBOFX.Y_N(pAdjustsGross));
        iadditem.setIBASEDONQTY(QBOFX.Y_N(pBasedOnQuantity));
        iadditem.setICOMPID(pAgencyId != null ? pAgencyId : "");
        if(pDefaultLimit != null) {
            iadditem.setIDEFLIMIT("$" + pDefaultLimit.toString());
        } else {
            iadditem.setIDEFLIMIT("");
        }
        if(pDefaultRate != null && pDefaultRateType != null) {
            if(pDefaultRateType == QbdtNumericType.MoneyType) {
                iadditem.setIDEFRATE("$" + pDefaultRate);
            } else {
                iadditem.setIDEFRATE(pDefaultRate + "%");
            }
        } else {
            iadditem.setIDEFRATE("$0.00");
        }
        iadditem.setIEXPACCT(pExpenseAccount);
        iadditem.setIEXPBYJOB(QBOFX.Y_N(pExpenseByJob));
        iadditem.setILIABACCT(pLiabilityAccount != null ? pLiabilityAccount : "");
        iadditem.setILIABAGENCY(pLiabilityAgency != null ? pLiabilityAgency : "");
        iadditem.setIONSERVICE(QBOFX.Y_N(pOnService));
        iadditem.setITAXFORMLINE(pTaxFormLine != null ? pTaxFormLine : "");

        for (IPITEM subjectToTax : pSubjectToTaxes) {
            iadditem.getITAXAFFECTED().add(subjectToTax.getIPITEMID());
        }

        return iadditem;
    }

    public static ITAXITEM generateTaxItem(boolean pAdjustsGross,
                                           boolean pBasedOnQuantity,
                                           String pAgencyId,
                                           SpcfMoney pDefaultLimit,
                                           String pExpenseAccount,
                                           String pFedTaxLawId,
                                           String pLiabilityAccount,
                                           String pLiabilityAgency,
                                           boolean pOnService,
                                           String pOtherTaxLawId,
                                           String pState,
                                           String pStateTax,
                                           String pTaxFormLine,
                                           Double pRate,
                                           Map<String, Double> pRateChanges) {
        ITAXITEM itaxitem = new ITAXITEM();
        itaxitem.setIADJGROSS(QBOFX.Y_N(pAdjustsGross));
        itaxitem.setIBASEDONQTY(QBOFX.Y_N(pBasedOnQuantity));
        itaxitem.setICOMPID(pAgencyId);
        if(pDefaultLimit != null) {
            itaxitem.setIDEFLIMIT("$" + pDefaultLimit.toString());
        } else {
            itaxitem.setIDEFLIMIT("");
        }
        itaxitem.setIEXPACCT(pExpenseAccount);
        itaxitem.setIFEDTAX(pFedTaxLawId);
        itaxitem.setILIABACCT(pLiabilityAccount != null ? pLiabilityAccount : "");
        itaxitem.setILIABAGENCY(pLiabilityAgency != null ? pLiabilityAgency : "");
        itaxitem.setIONSERVICE(QBOFX.Y_N(pOnService));
        itaxitem.setIOTHERTAX(pOtherTaxLawId);

        if(pState != null || pStateTax != null) {
            ISTATETAXDESC istatetaxdesc = new ISTATETAXDESC();
            istatetaxdesc.setISTATE(pState);
            istatetaxdesc.setISTATETAX(pStateTax);
            itaxitem.setISTATETAXDESC(istatetaxdesc);
        }
        itaxitem.setITAXFORMLINE(pTaxFormLine);

        if(pRate != null) {
            itaxitem.setIRATE(pRate + "%");
        } else {
            itaxitem.setIRATE("");
        }

        if(pRateChanges != null) {
            for (String endDate : pRateChanges.keySet()) {
                IRATECHANGE iratechange = new IRATECHANGE();
                iratechange.setIDTSUNSET(endDate);
                iratechange.setIRATE(pRateChanges.get(endDate) + "%");
                itaxitem.getIRATECHANGE().add(iratechange);
            }
        }

        return itaxitem;
    }

    public static List<IEMP> generateNewEmployees(int pNumberOfEmployees,
                                                  List<IPITEM> pWageItems,
                                                  List<IPITEM> pAdjustments,
                                                  List<IPITEM> pTaxes) {
        List<IEMPOTHERTAX> iempothertaxes = new ArrayList<IEMPOTHERTAX>();
        for (IPITEM tax : pTaxes) {
            if(tax.getITAXITEM().getIOTHERTAX() != null) {
                IEMPOTHERTAX iempothertax = new IEMPOTHERTAX();
                iempothertax.setITAXLAWVER("9701");
                iempothertax.setIPITEMID(tax.getIPITEMID());
            }
        }

        Map<String, String> customFields = new HashMap<String, String>();
        customFields.put("Birthday", "10/24/1901");

        List<IEMP> iemps = new ArrayList<IEMP>();
        for(int i = 0; i < pNumberOfEmployees; i++) {
            IEMP iemp = generateEmployee(Integer.toString(++mEmployeeId),
                                         false,
                                         generateAddressInfo("Mr.",
                                                             "First" + mEmployeeId,
                                                             "M",
                                                             "Last" + mEmployeeId,
                                                             "Address 1" + mEmployeeId,
                                                             "Address 2" + mEmployeeId,
                                                             "Reno",
                                                             "NV",
                                                             "89511",
                                                             "CA",
                                                             "NV",
                                                             "201-123-1234",
                                                             "212-123-6543",
                                                             "QWS",
                                                             "this@that.com"),
                                         "000-00-" + String.format("%04d", mEmployeeId),
                                         Calendar.getInstance().getTime(),
                                         null,
                                         generateEmployeePayroll(PayrollFrequencyCode.Annually,
                                                                 null,
                                                                 false,
                                                                 false,
                                                                 generateWages(pWageItems),
                                                                 generateAdjustments(pAdjustments)),
                                         generateEmployeeTax(false,
                                                             2,
                                                             "SINGLE",
                                                             new SpcfMoney("10.10"),
                                                             Arrays.<String>asList("0", "1"),
                                                             "CA",
                                                             1,
                                                             "MARRIED",
                                                             new SpcfMoney("5.05"),
                                                             "12345",
                                                             Arrays.<String>asList("3", "4"),
                                                             "CA",
                                                             "CA",
                                                             true,
                                                             false,
                                                             true,
                                                             true,
                                                             true,
                                                             true,
                                                             iempothertaxes),
                                         generateAccrual("YEARLY",
                                                         0.0,
                                                         5.25,
                                                         150,
                                                         true),
                                         generateAccrual("YEARLY",
                                                         0.0,
                                                         7.11,
                                                         100,
                                                         true),
                                         generateEmployeeDD(2),
                                         generateWagePlan(1),
                                         false,
                                         "Bill Pay",
                                         "REG",
                                         "MALE",
                                         customFields);
            iemps.add(iemp);
        }
        return iemps;
    }

    private static IEMPCOMPLIANCE generateWagePlan(int numberOfPlans) {
        IEMPCOMPLIANCE iempcompliance = new IEMPCOMPLIANCE();
        for(int i = 0; i < numberOfPlans; i++) {
            ISETTING isetting = new ISETTING();
            isetting.setIDESCRIPTION("blah blah " + i);
            isetting.setIDOMAIN("WorkOrLiveState");
            isetting.setINAME("WPC");
            isetting.setIRULESVERSION("R" + i);
            isetting.setISTATE("AZ");
            isetting.setIVALUE("" + i);
            iempcompliance.getISETTING().add(isetting);
        }
        return iempcompliance;
    }

    private static IEMPDD generateEmployeeDD(int numberOfAccounts) {
        IEMPDD iempdd = new IEMPDD();
        if(numberOfAccounts > 0) {
            iempdd.setIUSEDD(QBOFX.Y_N(true));
            for(int i = 0; i < numberOfAccounts; i++) {
                iempdd.getIDDACCT().add(generateDDAccount(i));
            }
        } else {
            iempdd.setIUSEDD(QBOFX.Y_N(false));
        }
        return iempdd;
    }

    public static IDDACCT generateDDAccount(int i) {
        IDDACCT iddacct = new IDDACCT();
        iddacct.setIACCTNAME("bank " + i);
        iddacct.setIAMT((i%2 == 0) ? "$" + i + ".00" : i + ".0" + "%");
        BANKACCT bankacct = new BANKACCT();
        bankacct.setACCTID("12345678" + i);
        bankacct.setBANKID("111000025");
        bankacct.setACCTTYPE(i%2 == 0 ? "CHECKING" : "SAVINGS");
        iddacct.setBANKACCTTO(bankacct);
        return iddacct;
    }

    private static ACCRUAL generateAccrual(String pAccrualPeriod,
                                           double pHours,
                                           double pHoursPerPeriod,
                                           double pMaxHours,
                                           boolean pNewYearReset) {
        ACCRUAL accrual = new ACCRUAL();
        accrual.setIACCRUALPD(pAccrualPeriod);
        accrual.setIHRS(Double.toString(pHours));
        accrual.setIHRSPERPD(Double.toString(pHoursPerPeriod));
        accrual.setIMAXHRS(Double.toString(pMaxHours));
        accrual.setINEWYRRESET(QBOFX.Y_N(pNewYearReset));
        return accrual;
    }

    public static List<IWAGE> generateWages(List<IPITEM> pPayrollItems) {
        List<IWAGE> iwages = new ArrayList<IWAGE>();
        for (IPITEM payrollItem : pPayrollItems) {
            iwages.add(generateWage(payrollItem.getIPITEMID(),
                                    payrollItem.getIPITEMNAME(),
                                    "$10.00"));
        }
        return iwages;
    }

    public static List<IADJ> generateAdjustments(List<IPITEM> pPayrollItems) {
        List<IADJ> iadjs = new ArrayList<IADJ>();
        for (IPITEM payrollItem : pPayrollItems) {
            iadjs.add(generateAdjustment(payrollItem.getIPITEMID(),
                                         "$10.00",
                                         "$1000.00"));
        }
        return iadjs;
    }

    public static SIGNONMSGSRQV1 generateSignOnMessage(String pUserId, String pPassword) {
        SIGNONMSGSRQV1 signonmsgsrqv1 = new SIGNONMSGSRQV1();
        SONRQ sonrq = new SONRQ();
        sonrq.setAPPID("QBWPRO");
        sonrq.setAPPVER("50.00.R.10/21014#retail");
        sonrq.setDTCLIENT("20100901191619");
        sonrq.setIIPADDRESS("IP address");
        sonrq.setIQBFILEID("blah");
        sonrq.setIQBFILENAME("some file path");
        sonrq.setIQBUSERNAME("Admin");
        sonrq.setLANGUAGE("ENG");
        sonrq.setUSERID(pUserId);
        sonrq.setUSERPASS(pPassword);
        //sonrq.setIRQEIN("000000001");
        signonmsgsrqv1.setSONRQ(sonrq);
        return signonmsgsrqv1;
    }

    public static IPAYROLLMSGSRQV1 generatePayrollMessage(Company pCompany, boolean pRejectIfMissing, IPAYROLLTRNRQ pIPAYROLLTRNRQ) {
        pCompany = DataLoadServices.refreshCompany(pCompany);
        return generatePayrollMessage(Long.toString(pCompany.getCurrentToken()), pRejectIfMissing, pIPAYROLLTRNRQ);
    }

    public static IPAYROLLMSGSRQV1 generatePayrollMessage(String pToken, boolean pRejectIfMissing, IPAYROLLTRNRQ pIPAYROLLTRNRQ) {
        IPAYROLLMSGSRQV1 ipayrollmsgsrqv1 = new IPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ ipayrollupdaterq = new IPAYROLLUPDATERQ();
        ipayrollupdaterq.setTOKEN(pToken);
        ipayrollupdaterq.setREJECTIFMISSING(QBOFX.Y_N(pRejectIfMissing));
        ipayrollupdaterq.setIPAYROLLTRNRQ(pIPAYROLLTRNRQ);
        ipayrollmsgsrqv1.setIPAYROLLUPDATERQ(ipayrollupdaterq);
        return ipayrollmsgsrqv1;
    }

    public static IPAYROLLTRNRQ generatePayrollRequest(boolean pTaxReady,
                                                       Date pQuarterToStart,
                                                       List<IEMP> pNewEmployees,
                                                       List<IEMP> pEmployeeUpdates,
                                                       List<String> pEmployeeDeletes,
                                                       List<IPITEM> pNewPayrollItems,
                                                       List<IPITEM> pPayrollItemUpdates,
                                                       List<String> pPayrollItemDeletes,
                                                       List<IPAYROLLTX> pNewPayrollTransactions,
                                                       List<IPAYROLLTX> pPayrollTransactionUpdates,
                                                       List<String> pPayrollTransactionDeletes,
                                                       List<IPAYROLLRUN> pPayrolls) {
        IPAYROLLTRNRQ ipayrolltrnrq = new IPAYROLLTRNRQ();
        ipayrolltrnrq.setTRNUID(UUID.randomUUID().toString());

        IPAYROLLRQ ipayrollrq = new IPAYROLLRQ();

        if(pTaxReady && pQuarterToStart != null) {
            ICOINFOMOD icoinfomod = new ICOINFOMOD();
            icoinfomod.setITAXREADY(QBOFX.Y_N(pTaxReady));
            icoinfomod.setIDTFILEQTRSTART(QBOFX.getDTTXResponse(pQuarterToStart));
            ipayrollrq.setICOINFOMOD(icoinfomod);
        }

        // employees
        if(pNewEmployees != null) {
            ipayrollrq.getIEMP().addAll(pNewEmployees);
        }
        if(pEmployeeUpdates != null) {
            ipayrollrq.getIEMPMOD().addAll(pEmployeeUpdates);
        }
        if(pEmployeeDeletes != null) {
            ipayrollrq.getIEMPDELID().addAll(pEmployeeDeletes);
        }

        // payroll items
        if(pNewPayrollItems != null) {
            ipayrollrq.getIPITEM().addAll(pNewPayrollItems);
        }
        if(pPayrollItemUpdates != null) {
            ipayrollrq.getIPITEMMOD().addAll(pPayrollItemUpdates);
        }
        if(pPayrollItemDeletes != null) {
            ipayrollrq.getIPITEMDELID().addAll(pPayrollItemDeletes);
        }

        // payroll transactions
        if(pNewPayrollTransactions != null) {
            ipayrollrq.getIPAYROLLTX().addAll(pNewPayrollTransactions);
        }
        if(pPayrollTransactionUpdates != null) {
            ipayrollrq.getIPAYROLLTXMOD().addAll(pPayrollTransactionUpdates);
        }
        if(pPayrollTransactionDeletes != null) {
            ipayrollrq.getIPAYROLLTXDELID().addAll(pPayrollTransactionDeletes);
        }

        // payrolls
        if(pPayrolls != null) {
            ipayrollrq.getIPAYROLLRUN().addAll(pPayrolls);
        }

        ipayrolltrnrq.setIPAYROLLRQ(ipayrollrq);
        return ipayrolltrnrq;
    }

    public static IEMP generateAccrualEmployeeMod(String pEmployeeId,
                                                  double pSickHours,
                                                  double pVacationHours) {
        ACCRUAL sick = new ACCRUAL();
        sick.setIHRS(Double.toString(pSickHours));
        ACCRUAL vacation = new ACCRUAL();
        vacation.setIHRS(Double.toString(pVacationHours));
        return generateEmployee(pEmployeeId,
                                true,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                sick,
                                vacation,
                                null,
                                null,
                                false,
                                null,
                                null,
                                null,
                                null);
    }

    public static IEMP generateEmployee(String pEmployeeId,
                                        boolean pIsAccrualOnly,
                                        IADDRINFO pIADDRINFO,
                                        String pSSN,
                                        Date pHireDate,
                                        Date pReleaseDate,
                                        IPAYROLL pIPAYROLL,
                                        IEMPTAX pEMPTAX,
                                        ACCRUAL pSick,
                                        ACCRUAL pVacation,
                                        IEMPDD pIEMPDD,
                                        IEMPCOMPLIANCE pEmployeeCompliance,
                                        boolean pEmployeeInactive,
                                        String pBillPayAccount,
                                        String pEmployeeType,
                                        String pGender,
                                        Map<String, String> pCustomFields) {
        IEMP iemp = new IEMP();
        iemp.setIEMPID(pEmployeeId);
        if(pIsAccrualOnly) {
            iemp.setISICK(pSick);
            iemp.setIVAC(pVacation);
        } else {
            iemp.setIADDRINFO(pIADDRINFO);
            iemp.setIBILLPAYACCT(pBillPayAccount);
            iemp.setIDTHIRE(QBOFX.getDTTXResponse(pHireDate));
            iemp.setIDTRELEASE(pReleaseDate != null ? QBOFX.getDTTXResponse(pReleaseDate) : "");
            iemp.setIEMPCOMPLIANCE(pEmployeeCompliance);
            iemp.setIEMPDD(pIEMPDD);
            iemp.setIEMPGENDER(pGender);
           if(pIADDRINFO != null) {
                iemp.setIEMPNAME(String.format("%s, %s %s", pIADDRINFO.getILAST(), pIADDRINFO.getIFIRST(), pIADDRINFO.getIMI()));
            }
            iemp.setIEMPTAX(pEMPTAX);
            iemp.setIEMPTYPE(pEmployeeType);
            iemp.setIHASCUSTOMFLD(QBOFX.Y_N(pCustomFields.size() > 0));
            iemp.setIINACTIVE(QBOFX.Y_N(pEmployeeInactive));
            iemp.setIPAYROLL(pIPAYROLL);
            iemp.setISICK(pSick);
            iemp.setISSN(pSSN);
            iemp.setIVAC(pVacation);
            for (String name : pCustomFields.keySet()) {
                ICUSTOMFLD icustomfld = new ICUSTOMFLD();
                icustomfld.setIFLDNAME(name);
                icustomfld.setIFLDVALUE(pCustomFields.get(name));
                iemp.getICUSTOMFLD().add(icustomfld);
            }
        }

        return iemp;
    }

    public static IADDRINFO generateAddressInfo(String pTitle,
                                                String pFirstName,
                                                String pMiddleInitial,
                                                String pLastName,
                                                String pAddressLine1,
                                                String pAddressLine2,
                                                String pCity,
                                                String pState,
                                                String pZipCode,
                                                String pStateWorked,
                                                String pStateLived,
                                                String pPhone,
                                                String pAltPhone,
                                                String pInitials,
                                                String pEmail) {
        IADDRINFO iaddrinfo = new IADDRINFO();
        iaddrinfo.setIADDR1(pAddressLine1);
        iaddrinfo.setIADDR2(pAddressLine2);
        iaddrinfo.setIALTPHONE(pAltPhone);
        iaddrinfo.setICITY(pCity);
        iaddrinfo.setIEMAIL(pEmail);
        iaddrinfo.setIFIRST(pFirstName);
        iaddrinfo.setIINITIALS(pInitials);
        iaddrinfo.setILAST(pLastName);
        iaddrinfo.setIMI(pMiddleInitial);
        iaddrinfo.setIPHONE(pPhone);
        iaddrinfo.setIPOSTALCODE(pZipCode);
        iaddrinfo.setIPRINTASNAME(pFirstName + pMiddleInitial + pLastName);
        iaddrinfo.setISTATE(pState);
        iaddrinfo.setISTATELIVED(pStateLived);
        iaddrinfo.setISTATEWORKED(pStateWorked);
        iaddrinfo.setITITLE(pTitle);
        return iaddrinfo;
    }

    public static IPAYROLL generateEmployeePayroll(PayrollFrequencyCode pPayPeriod,
                                                   String pClass,
                                                   boolean pUseTime,
                                                   boolean pHasRetirementPlan,
                                                   List<IWAGE> pWages,
                                                   List<IADJ> pAdjustments) {
        IPAYROLL ipayroll = new IPAYROLL();
        ipayroll.setICLASS(pClass);
        if(pPayPeriod != null) {
            ipayroll.setIPAYPD(QBOFX.mapPayrollFrequencyToOFXValue(pPayPeriod));
        }
        ipayroll.setIPPLANOVERRIDE(QBOFX.Y_N(pHasRetirementPlan));
        ipayroll.setIUSETIME(QBOFX.Y_N(pUseTime));
        ipayroll.getIADJ().addAll(pAdjustments);
        ipayroll.getIWAGE().addAll(pWages);
        return ipayroll;
    }

    public static IWAGE generateWage(String pPayrollItemId,
                                     String pPayrollItemName,
                                     String pRate) {
        IWAGE iwage = new IWAGE();
        iwage.setIPITEMID(pPayrollItemId);
        iwage.setIPITEMNAME(pPayrollItemName);
        iwage.setIRATE(pRate);
        return iwage;
    }

    public static IADJ generateAdjustment(String pPayrollItemId,
                                          String pAmount,
                                          String pLimit) {
        IADJ iadj = new IADJ();
        iadj.setIPITEMID(pPayrollItemId);
        iadj.setIAMT(pAmount);
        iadj.setILIMIT(pLimit);
        return iadj;
    }

    public static IEMPTAX generateEmployeeTax(boolean pIsDeceased,
                                              int pFederalAllowances,
                                              String pFederalFilingStatus,
                                              SpcfMoney pFederalExtraWithholding,
                                              List<String> pFITTableMiscData,
                                              String pSITState,
                                              int pStateAllowances,
                                              String pStateFilingStatus,
                                              SpcfMoney pStateExtraWithholding,
                                              String pSITTaxLawVersion,
                                              List<String> pSITTableMiscData,
                                              String pSDIState,
                                              String pSUIState,
                                              boolean pEnforceSubjectTo,
                                              boolean pQualifiesForAEIC,
                                              boolean pSubjectToFIT,
                                              boolean pSubjectToFUTA,
                                              boolean pSubjectToMED,
                                              boolean pSubjectToSS,
                                              List<IEMPOTHERTAX> pOtherTaxes) {
        IEMPTAX iemptax = new IEMPTAX();
        iemptax.setIDECEASED(QBOFX.Y_N(pIsDeceased));

        IEMPFIT iempfit = new IEMPFIT();
        iempfit.setIALLOWANCES(Integer.toString(pFederalAllowances));
        iempfit.setIEXTRAWITHHOLD("$" + pFederalExtraWithholding.toString());
        iempfit.setIFEDFILESTATUS(pFederalFilingStatus);
        for (String data : pFITTableMiscData) {
            iempfit.getITAXTBLMISCDATA().add(data);
        }
        iemptax.setIEMPFIT(iempfit);

        IEMPSIT iempsit = new IEMPSIT();
        iempsit.setIALLOWANCES(Integer.toString(pStateAllowances));
        iempsit.setIEXTRAWITHHOLD("$" + pStateExtraWithholding.toString());
        iempsit.setISTATE(pSITState);
        iempsit.setISTATEFILESTATUS(pStateFilingStatus);
        iempsit.setITAXLAWVER(pSITTaxLawVersion);
        for (String data : pSITTableMiscData) {
            iempsit.getITAXTBLMISCDATA().add(data);
        }
        iemptax.setIEMPSIT(iempsit);

        IEMPSDI iempsdi = new IEMPSDI();
        iempsdi.setISTATE(pSDIState);
        iemptax.setIEMPSDI(iempsdi);

        IEMPSUI iempsui = new IEMPSUI();
        iempsui.setISTATE(pSUIState);
        iemptax.setIEMPSUI(iempsui);

        iemptax.setIENFORCESUBJECTTO(QBOFX.Y_N(pEnforceSubjectTo));
        iemptax.setIQUALFORAEIC(QBOFX.Y_N(pQualifiesForAEIC));
        iemptax.setISUBJTOFIT(QBOFX.Y_N(pSubjectToFIT));
        iemptax.setISUBJTOFUTA(QBOFX.Y_N(pSubjectToFUTA));
        iemptax.setISUBJTOMCARE(QBOFX.Y_N(pSubjectToMED));
        iemptax.setISUBJTOSS(QBOFX.Y_N(pSubjectToSS));
        iemptax.getIEMPOTHERTAX().addAll(pOtherTaxes);

        return iemptax;
    }

    public static IEMPOTHERTAX generateOtherTax(String pPayrollItemId,
                                                String pTaxLawVersion,
                                                String pW2Name,
                                                List<String> pTaxTableMiscData) {
        IEMPOTHERTAX iempothertax = new IEMPOTHERTAX();
        iempothertax.setIPITEMID(pPayrollItemId);
        iempothertax.setITAXLAWVER(pTaxLawVersion);
        iempothertax.setIW2NAME(pW2Name);
        iempothertax.getITAXTBLMISCDATA().addAll(pTaxTableMiscData);
        return iempothertax;
    }

    public static IEMPDD generateEmployeeDirectDeposit(boolean pUseDD,
                                                       List<IDDACCT> pDDAccounts) {
        IEMPDD iempdd = new IEMPDD();
        iempdd.setIUSEDD(QBOFX.Y_N(pUseDD));
        iempdd.getIDDACCT().addAll(pDDAccounts);
        return iempdd;
    }

    public static IDDACCT generateDDAccount(String pRoutingNumber,
                                            String pAccountNumber,
                                            String pAccountType,
                                            String pBankName,
                                            String pAmount) {
        IDDACCT iddacct = new IDDACCT();
        BANKACCT bankacct = new BANKACCT();
        bankacct.setACCTID(pAccountNumber);
        bankacct.setACCTTYPE(pAccountType);
        bankacct.setBANKID(pRoutingNumber);
        iddacct.setBANKACCTTO(bankacct);
        iddacct.setIACCTNAME(pBankName);
        iddacct.setIAMT(pAmount);
        return iddacct;
    }

    public static OFX generateEmptyOFX(Company pCompany) {
        OFX ofx = new OFX();
        ofx.setSIGNONMSGSRQV1(new SIGNONMSGSRQV1());

        IPAYROLLTRNRQ ipayrolltrnrq = new IPAYROLLTRNRQ();
        ipayrolltrnrq.setTRNUID(UUID.randomUUID().toString());

        IPAYROLLRQ ipayrollrq = new IPAYROLLRQ();

        ipayrolltrnrq.setIPAYROLLRQ(ipayrollrq);

        ofx.setIPAYROLLMSGSRQV1(generatePayrollMessage(pCompany.getCurrentToken() + "", true, ipayrolltrnrq));
        ofx.setSIGNONMSGSRQV1(generateSignOnMessage(pCompany.getSourceCompanyId(), DataLoadServices.PIN));
        return ofx;
    }

    
    public static OFX generateEmptyBalanceFile(Company pCompany, String quarterToStartDate) {
        OFX ofx = generateEmptyOFX(pCompany);
        ICOINFOMOD icoinfomod = new ICOINFOMOD();
        icoinfomod.setITAXREADY(QBOFX.Y_N(false));
        icoinfomod.setIDTFILEQTRSTART(QBOFX.getDTTXResponse(new Date(quarterToStartDate)));
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().setICOINFOMOD(icoinfomod);
        return ofx;
    }

    public static IPAYROLLRUN generatePayrollRun(String pPayrollDate){
        IPAYROLLRUN payrollRun = new IPAYROLLRUN();
        payrollRun.setIDTPAYCHKS(pPayrollDate);
        return payrollRun;
    }

    public static IPAYROLLRUN generatePayrollRun(){
        return generatePayrollRun(null);
    }

    public static void addPayrollRunToOfx(OFX pOfx, IPAYROLLRUN pPayrollRun){
        pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().add(pPayrollRun);
    }

    public static void addPayrollItemsToOfx(OFX pOfx, List<IPITEM> pIpitems){
        pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().addAll(pIpitems);
    }

    public static IPAYROLLRUN createNewPayroll(IPAYROLLRUN payrollRun, boolean voidNewPayroll) {
        IPAYROLLRUN payrollRunVoid = OFXRequestGenerator.generatePayrollRun();
        payrollRunVoid.setIDTPAYCHKS(payrollRun.getIDTPAYCHKS());
        for (IPAYCHK ipaycheck : payrollRun.getIPAYCHK()) {
            IPAYCHK ipaychkmod = new IPAYCHK();
            ipaychkmod.setIDTTX(ipaycheck.getIDTTX());
            ipaychkmod.setIPAYCHKID(ipaycheck.getIPAYCHKID());
            ipaychkmod.setIEMPID(ipaycheck.getIEMPID());
            ipaychkmod.setIPAYCHKTYPE(ipaycheck.getIPAYCHKTYPE());
            ipaychkmod.setIEMPNAME("");
            ipaychkmod.setICLASS("Changed Class");
            ipaychkmod.setIACCTNAME("Changed Account Name");
            ipaychkmod.setIPAYCHKINFO(ipaycheck.getIPAYCHKINFO());
            ipaychkmod.setIVOID(QBOFX.Y_N(voidNewPayroll));
            ipaychkmod.setIDTPAYPDBEGIN(ipaycheck.getIDTPAYPDBEGIN());
            ipaychkmod.setIDTPAYPDEND(ipaycheck.getIDTPAYPDEND());
            ipaychkmod.setIMEMO("Changed Memo");
            ipaychkmod.setICLEARED("9");
            ipaychkmod.setIONSERVICE("Y");
            payrollRunVoid.getIPAYCHKMOD().add(ipaychkmod);
        }
        return payrollRunVoid;
    }

}
