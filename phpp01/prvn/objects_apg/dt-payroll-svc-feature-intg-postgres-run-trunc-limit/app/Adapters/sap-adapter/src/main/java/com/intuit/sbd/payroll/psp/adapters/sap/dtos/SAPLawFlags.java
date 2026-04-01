package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 3/8/13
 * Time: 2:53 PM
 */
public class SAPLawFlags {
    private SAPLawItem law;
    private boolean inactive;
    private boolean exempt;
    private boolean reimbursable;

    public SAPLawItem getLaw() {
        return law;
    }

    public void setLaw(SAPLawItem pLaw) {
        law = pLaw;
    }

    public boolean getInactive() {
        return inactive;
    }

    public void setInactive(boolean pInactive) {
        inactive = pInactive;
    }

    public boolean getExempt() {
        return exempt;
    }

    public void setExempt(boolean pExempt) {
        exempt = pExempt;
    }

    public boolean getReimbursable() {
        return reimbursable;
    }

    public void setReimbursable(boolean pReimbursable) {
        reimbursable = pReimbursable;
    }
}
