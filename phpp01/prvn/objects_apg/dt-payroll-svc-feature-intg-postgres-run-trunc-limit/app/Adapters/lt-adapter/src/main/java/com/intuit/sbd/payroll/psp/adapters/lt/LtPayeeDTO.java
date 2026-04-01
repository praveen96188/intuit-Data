package com.intuit.sbd.payroll.psp.adapters.lt;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Mar 8, 2010
 * Time: 10:18:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class LtPayeeDTO {
    private String payeeId;
    private String payeeBankAccountId;
    private String payeeName;
    private String payeeBankAccountNumber;

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public String getPayeeBankAccountId() {
        return payeeBankAccountId;
    }

    public void setPayeeBankAccountId(String payeeBankAccountId) {
        this.payeeBankAccountId = payeeBankAccountId;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getPayeeBankAccountNumber() {
        return payeeBankAccountNumber;
    }

    public void setPayeeBankAccountNumber(String payeeBankAccountNumber) {
        this.payeeBankAccountNumber = payeeBankAccountNumber;
    }
}
