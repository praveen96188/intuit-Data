package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPITEM;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.PayrollItemCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: rnorian
 * Date: Feb 21, 2011
 * Time: 4:03:57 PM
 */
public class PayrollItemRepository {
    public enum Tax {
        AEIC("Advance Earned Income Credit"), COBRA("COBRA"),
        FIT("Federal Withholding"), FUTA("Federal Unemployment"), FICA_ER("Social Security Employer"), FICA_EE("Social Security Employee"),
        MED_ER("Medicare Employer"), MED_EE("Medicare Employee"),
        CA_SIT("CA - Withholding"), CA_SDI_EE("CA - Disability"), CA_SUI_ER("CA - Unemployment"), CA_ETT("CA - Employment Training Tax"),
        PA_SWT("PA - Withholding");

        Tax(String pPDescription) {
            description = pPDescription;
        }

        public String getDescription() {
            return description;
        }

        private Tax fromDescription(String pDescription) {
            for (Tax tax : Tax.values()) {
                if (tax.description.equalsIgnoreCase(pDescription)) {
                    return tax;
                }
            }
            throw new IllegalArgumentException("no tax matches description: " + pDescription);
        }

        private String description;
    }

    HashMap<QBOFX.OFXPayrollItemType, ArrayList<IPITEM>> payrollItems = new HashMap<QBOFX.OFXPayrollItemType, ArrayList<IPITEM>>();
    public PayrollItemRepository() {
        List<IPITEM> ipitems = OFXRequestGenerator.generateAllPayrollItemTypes(OFXRequestGenerator.generateFedTaxes(), true);
        ipitems.addAll(OFXRequestGenerator.generateAllPayrollItemTypes(OFXRequestGenerator.generateCATaxes(), true));
        ipitems.addAll(OFXRequestGenerator.generateAllPayrollItemTypes(OFXRequestGenerator.generatePATaxes(), true));
        for (IPITEM ipitem : ipitems) {
            PayrollItem payrollItem = new PayrollItem(ipitem);
            if (!payrollItems.containsKey(payrollItem.getItemType())) {
                
                payrollItems.put(payrollItem.getItemType(), new ArrayList<IPITEM>());
            }
            payrollItems.get(payrollItem.getItemType()).add(ipitem);
        }
    }

    private IPITEM getItem(QBOFX.OFXPayrollItemType pItemType) {
        List<IPITEM> ipitems = payrollItems.get(pItemType);

        IPITEM ipitem = null;
        if (ipitems.size() > 0) {
            ipitem = ipitems.get(0);
        }
        return ipitem;
    }

    public IPITEM getItem(QBOFX.OFXPayrollItemType pItemType, String pItemName) {
        return getByName(payrollItems.get(pItemType), pItemName);
    }

    private IPITEM getByName(List<IPITEM> pIPITEMs, String pItemName) {
        IPITEM ipitem = null;
        if (pIPITEMs.size() > 0) {
            for (IPITEM pitem : pIPITEMs) {
                if (pitem.getIPITEMNAME().equalsIgnoreCase(pItemName)) {
                    ipitem = pitem;
                    break;
                }
            }
        }
        return ipitem;
    }


    public IPITEM getSalaryItem() {
        return getItem(QBOFX.OFXPayrollItemType.Salary);
    }

    public IPITEM getSalaryItem(String pItemName) {
        return getItem(QBOFX.OFXPayrollItemType.Salary, pItemName);
    }

    public IPITEM getCommissionItem() {
        return getItem(QBOFX.OFXPayrollItemType.Commission);
    }

    public IPITEM getCommissionItem(String pItemName) {
        return getItem(QBOFX.OFXPayrollItemType.Commission, pItemName);
    }

    public IPITEM getBonusItem() {
        return getItem(QBOFX.OFXPayrollItemType.Bonus);
    }

    public IPITEM getBonusItem(String pName) {
        return getItem(QBOFX.OFXPayrollItemType.Bonus, pName);
    }

    public IPITEM getTaxItem(Tax pTax) {
        return getByName(payrollItems.get(QBOFX.OFXPayrollItemType.Tax), pTax.getDescription());
    }

    public IPITEM getDeductionItem() {
        return getItem(QBOFX.OFXPayrollItemType.Deduction);
    }

    public IPITEM getDeductionItem(String pName) {
        return getByName(payrollItems.get(QBOFX.OFXPayrollItemType.Deduction), pName);
    }

    public IPITEM getDirectDepositItem() {
        return getItem(QBOFX.OFXPayrollItemType.DirectDeposit);
    }

    public IPITEM getDirectDepositItem(String pName) {
        return getItem(QBOFX.OFXPayrollItemType.DirectDeposit, pName);
    }

    public IPITEM getHourlyItem() {
        return getItem(QBOFX.OFXPayrollItemType.Hourly);
    }

    public IPITEM getHourlyItem(String pName) {
        return getItem(QBOFX.OFXPayrollItemType.Hourly, pName);
    }

    public IPITEM getAdditionItem() {
        return getItem(QBOFX.OFXPayrollItemType.Addition);
    }

    public IPITEM getAdditionItem(String pName) {
        return getItem(QBOFX.OFXPayrollItemType.Addition, pName);
    }

    public IPITEM getEmployerContributionItem() {
        return getItem(QBOFX.OFXPayrollItemType.EmployerContribution);
    }

    public IPITEM getEmployerContributionItem(String pName) {
        return getItem(QBOFX.OFXPayrollItemType.EmployerContribution, pName);
    }

    public List<IPITEM> getAllPayrollItems(){
        List<IPITEM> iPItems = new ArrayList<IPITEM>();
        for (QBOFX.OFXPayrollItemType ofxPayrollItemType : payrollItems.keySet()) {
            for (IPITEM ipitem : payrollItems.get(ofxPayrollItemType)) {
                iPItems.add(ipitem);
            }
        }
        return iPItems;
    }
}
