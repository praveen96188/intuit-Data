package com.intuit.sbd.payroll.psp.adapters.qbdt;

/**
 * Created with IntelliJ IDEA.
 * User: YifengS302
 * Date: 8/20/12
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public enum CredentialType {
    Pin,

    Secondary;

    public boolean in(CredentialType... pCredentialType) {
        for (CredentialType curCredentialType : pCredentialType) {
            if (this == curCredentialType) {
                return true;
            }
        }
        return false;
    }

    public boolean notIn(CredentialType... pCredentialType) {
        return !in(pCredentialType);
    }
}
