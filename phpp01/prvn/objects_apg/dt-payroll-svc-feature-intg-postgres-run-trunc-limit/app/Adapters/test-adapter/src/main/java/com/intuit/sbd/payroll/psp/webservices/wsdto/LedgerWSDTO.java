package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 2, 2009
 * Time: 3:22:40 PM
 */
public class LedgerWSDTO {
    public String name;
    public String description;
    public String ledgerAccountCode;
    public BigDecimal balance;
    public boolean isCredit;
    public Collection<String> allowableActions;
    public Collection<LedgerEntryWSDTO> ledgerEntries;
}
