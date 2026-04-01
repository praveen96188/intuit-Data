package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class PaymentTemplateBankAccount extends BasePaymentTemplateBankAccount {

	/**
	 * Default constructor.
	 */
	public PaymentTemplateBankAccount()
	{
		super();
	}

    public static PaymentTemplateBankAccount findActiveBankAccount(PaymentTemplate pPaymentTemplate) {
        DomainEntitySet<PaymentTemplateBankAccount> paymentTemplateBankAccounts =
                Application.find(PaymentTemplateBankAccount.class,
                        PaymentTemplateBankAccount.PaymentTemplate().equalTo(pPaymentTemplate)
                                .And(PaymentTemplateBankAccount.StatusCd().equalTo(BankAccountStatus.Active)));

        if (paymentTemplateBankAccounts.size() > 1) {
            throw new RuntimeException("Payment Template " + pPaymentTemplate.getPaymentTemplateCd()
                    + " has more than one active account");
        }
        if (paymentTemplateBankAccounts.size() != 1) {
            return null;
        }

        return paymentTemplateBankAccounts.get(0);
    }

}