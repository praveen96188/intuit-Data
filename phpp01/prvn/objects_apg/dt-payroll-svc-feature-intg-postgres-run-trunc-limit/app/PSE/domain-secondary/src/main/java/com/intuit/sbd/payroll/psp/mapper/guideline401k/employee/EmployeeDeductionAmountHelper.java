package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kAmountType;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kEmployeeDeduction;
import com.intuit.v4.common.Amount;
import com.intuit.v4.common.Decimal;
import com.intuit.v4.common.Rate;
import org.springframework.stereotype.Component;

@Component
public class EmployeeDeductionAmountHelper {

    public Rate createEmployeeDeductionAmount(Hcm401kEmployeeDeduction hcm401kEmployeeDeduction) {
        Rate amount = new Rate();
        if(isDeductionAmountTypeDollar(hcm401kEmployeeDeduction)) {
            amount.setPercent(false);
            amount.setMoneyValue(Amount.valueOf(hcm401kEmployeeDeduction.getAmount()));
        } else {
            amount.setPercent(true);
            amount.setPercentValue(Decimal.valueOf(hcm401kEmployeeDeduction.getAmount()));
        }
        return amount;
    }

    private boolean isDeductionAmountTypeDollar(Hcm401kEmployeeDeduction hcm401kEmployeeDeduction) {
        return hcm401kEmployeeDeduction.getHcm401kAmountType().in(Hcm401kAmountType.Dollar);
    }
}
