package com.intuit.sbd.payroll.psp.processes.wallet.clone;

import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.model.ParentType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WalletCloneModel {
    private List<BankAccount> bankAccounts;
    private String parentId;
    private ParentType parentType;
    private EventDetailTypeCode parentTypeEvent;
    private EventTypeCode successEventTypeCode;
    private EventTypeCode failedEventTypeCode;

    public WalletCloneModel(List<BankAccount> bankAccounts, String parentId, ParentType parentType) {
        this.bankAccounts = bankAccounts;
        this.parentId = parentId;
        this.parentType = parentType;
        switch(parentType) {
            case employees:
                this.parentTypeEvent = EventDetailTypeCode.EmployeeId;
                this.successEventTypeCode = EventTypeCode.CloneEmployeeWalletOnRealmChangeSuccess;
                this.failedEventTypeCode = EventTypeCode.CloneEmployeeWalletOnRealmChangeFailure;
                break;
            case vendors:
                this.parentTypeEvent = EventDetailTypeCode.VendorId;
                this.successEventTypeCode = EventTypeCode.CloneVendorWalletOnRealmChangeSuccess;
                this.failedEventTypeCode = EventTypeCode.CloneVendorWalletOnRealmChangeFailure;
                break;
        }
    }
}
