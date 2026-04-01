package com.intuit.sbd.payroll.psp.gateways.walletv4service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class WalletV4CloneModel {
    private List<String> walletIds;
    private String parentId;
    private String parentType;
    private String oldRealmId;
    private String newRealmId;
}
