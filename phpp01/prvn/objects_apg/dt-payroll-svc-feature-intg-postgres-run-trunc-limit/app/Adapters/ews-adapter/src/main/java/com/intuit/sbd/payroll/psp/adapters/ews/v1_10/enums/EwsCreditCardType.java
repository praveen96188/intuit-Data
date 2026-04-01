package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums;

import java.util.Objects;

/**
 * @author Jeff Jones
 */
public enum EwsCreditCardType {
    AMEX,

    VISA,

    MC,

    DISC;
    
    public static EwsCreditCardType mapPaymentProfileCardType(String cardType){
        if (Objects.isNull(cardType)){
            return null;
        } else if (cardType.equalsIgnoreCase("Visa") || cardType.equals("VISA")) {
            return EwsCreditCardType.VISA;
        } else if (cardType.equalsIgnoreCase("MasterCard") || cardType.equals("MC")) {
            return EwsCreditCardType.MC;
        } else if (cardType.equalsIgnoreCase("American Express") || cardType.equals("AMEX")) {
            return EwsCreditCardType.AMEX;
        } else if (cardType.equalsIgnoreCase("Discover") || cardType.equals("DISC")) {
            return EwsCreditCardType.DISC;
        }
        return null;
    }
}
