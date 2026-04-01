package com.intuit.sbd.payroll.psp.batchjobs.as400sync;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class PaymentComparator {
    private static final SpcfDecimal ZERO = SpcfMoney.createInstance(0.00);
    private List<String> errorMessages = new ArrayList<String>();
    private List<String> infoMessages = new ArrayList<String>();

    public PaymentComparator() {
    }

    /**
     * ***************************************************************************************************************
     * comparePayments
     * ****************************************************************************************************************
     */
    public void comparePayments(DomainEntitySet<MoneyMovementTransaction> pspPayments, List<ATFPaymentDTO> atfPayments, String paymentTemplate) {
        List<ATFPaymentDTO> atfPaymentsLocalCopy = new ArrayList<ATFPaymentDTO>(atfPayments);

        for (MoneyMovementTransaction pspPayment : pspPayments) {
            Boolean matchedDueDate = false;
            Boolean matchedDueDateAndAmount = false;
            for (ATFPaymentDTO atfPayment : atfPaymentsLocalCopy) {
                if (DateDTO.convertToSpcfCalendar(atfPayment.getDueDate()).compareTo(pspPayment.getDueDate()) == 0) {
                    matchedDueDateAndAmount = compareAmount(pspPayment.getMoneyMovementTransactionAmount(), atfPayment.getAmountDue());
                    if (!matchedDueDateAndAmount) {
                        errorMessages.add(paymentTemplate + " payment for due date " + pspPayment.getDueDate().toString() + " does not match - PSP amount:" + pspPayment.getMoneyMovementTransactionAmount().toString() + " ATF amount: " + atfPayment.getAmountDue().toString());
                    }
                    atfPaymentsLocalCopy.remove(atfPayment);
                    matchedDueDate = true;
                    break;
                }
            }
            if (matchedDueDateAndAmount) {
                infoMessages.add(paymentTemplate + " payment matched for due date " + pspPayment.getDueDate().toString() + " and amount " + pspPayment.getMoneyMovementTransactionAmount().toString());
            } else {
                if (!matchedDueDate) {
                    errorMessages.add(paymentTemplate + " payment in PSP not found in ATF for due date " + pspPayment.getDueDate().toString() + " and amount " + pspPayment.getMoneyMovementTransactionAmount().toString());
                }
            }
        }

        for (ATFPaymentDTO atfPayment : atfPaymentsLocalCopy) {
            errorMessages.add(paymentTemplate + " payment in ATF not found in PSP for due date " + atfPayment.getDueDate().toSpcfCalendar().toString() + " and amount " + atfPayment.getAmountDue().toString());
        }
    }

    public void comparePayments(DomainEntitySet<MoneyMovementTransaction> pspLocalPayments, DomainEntitySet<MoneyMovementTransaction> pspProdPayments, String paymentTemplate) {
        DomainEntitySet<MoneyMovementTransaction> pspProdPaymentsLocalCopy = new DomainEntitySet<MoneyMovementTransaction>();

        for (MoneyMovementTransaction moneyMovementTransaction : pspProdPayments) {
            pspProdPaymentsLocalCopy.add(moneyMovementTransaction);
        }

        for (MoneyMovementTransaction pspLocalPayment : pspLocalPayments) {
            Boolean matchedDueDate = false;
            Boolean matchedDueDateAndAmount = false;
            for (MoneyMovementTransaction pspProdPayment : pspProdPaymentsLocalCopy) {
                if (pspProdPayment.getDueDate().compareTo(pspLocalPayment.getDueDate()) == 0) {
                    matchedDueDateAndAmount = pspLocalPayment.getMoneyMovementTransactionAmount().compareTo(pspProdPayment.getMoneyMovementTransactionAmount()) == 0;
                    if (!matchedDueDateAndAmount) {
                        errorMessages.add(paymentTemplate + " payment for due date " + pspLocalPayment.getDueDate().toString() + " does not match - PSP LOCAL amount:" + pspLocalPayment.getMoneyMovementTransactionAmount().toString() + " PSP PROD amount: " + pspProdPayment.getMoneyMovementTransactionAmount().toString());
                    }
                    pspProdPaymentsLocalCopy.remove(pspProdPayment);
                    matchedDueDate = true;
                    break;
                }
            }
            if (matchedDueDateAndAmount) {
                infoMessages.add(paymentTemplate + " payment matched for due date " + pspLocalPayment.getDueDate().toString() + " and amount " + pspLocalPayment.getMoneyMovementTransactionAmount().toString());
            } else {
                if (!matchedDueDate) {
                    errorMessages.add(paymentTemplate + " payment in PSP LOCAL not found in PSP PROD for due date " + pspLocalPayment.getDueDate().toString() + " and amount " + pspLocalPayment.getMoneyMovementTransactionAmount().toString());
                }
            }
        }

        for (MoneyMovementTransaction pspProdPayment : pspProdPaymentsLocalCopy) {
            errorMessages.add(paymentTemplate + " payment in PSP PROD not found in PSP LOCAL for due date " + pspProdPayment.getDueDate().toString() + " and amount " + pspProdPayment.getMoneyMovementTransactionAmount().toString());
        }
    }


    /**
     * ***************************************************************************************************************
     * compareAmount, compareHours, etc.
     * ****************************************************************************************************************
     */
    private Boolean compareAmount(SpcfMoney netAmount, BigDecimal checkNetPay) {
        SpcfMoney as400NetPay = SpcfUtils.convertToSpcfMoney(checkNetPay);
        return netAmount.setScale(as400NetPay.getScale(), SpcfDecimal.SpcfRoundingType.HalfUp).compareTo(as400NetPay) == 0;
    }

    public String getMessage() {
        final String newLine = System.getProperty("line.separator");
        StringBuilder result = new StringBuilder(newLine);

        for (String currMessage : infoMessages) {
            result.append(currMessage);
            result.append(newLine);
        }

        for (String currMessage : errorMessages) {
            result.append(currMessage);
            result.append(newLine);
        }

        return result.toString();
    }

    public boolean getHasErrors() {
        return errorMessages.size() > 0;
    }


}
