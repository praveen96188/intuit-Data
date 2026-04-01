package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: Dawn Martens
 * Date: Apr 17, 2008
 * Time: 1:58:32 PM
 *
 * This process should only be used for an agent making a change.
 * Call the MMT method directly for system changes.
 */
public class ChangePaymentMethodTax extends Process implements IProcess {

    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCode;
    private SpcfUniqueId moneyMovementTransactionId;
    private PaymentMethod newPaymentMethod;

    private MoneyMovementTransaction payment;


    public ChangePaymentMethodTax(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                  SpcfUniqueId pMoneyMovementTransactionId, PaymentMethod pPaymentMethod) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        moneyMovementTransactionId = pMoneyMovementTransactionId;
        newPaymentMethod = pPaymentMethod;

    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }

        //Ensure moneyMovementTransactionId is passed
        if (moneyMovementTransactionId == null) {
            validationResult.getMessages().InvalidArgument(EntityName.MoneyMovementTransaction, null, "Money Movement Transaction ID");
            return validationResult;
        }

        //Ensure payment exists
        payment = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, moneyMovementTransactionId);
        if (payment == null) {
            validationResult.getMessages().EntityDoesNotExist(EntityName.MoneyMovementTransaction, moneyMovementTransactionId.toString(), EntityName.MoneyMovementTransaction.toString(), moneyMovementTransactionId.toString());
            return validationResult;
        }


        if (!payment.isPendingMMT()) {
            validationResult.getMessages().CannotUpdatePaymentMethodToManualForPaymentsSentToEFE(EntityName.MoneyMovementTransaction, moneyMovementTransactionId.toString());
        }

        if (newPaymentMethod == null) {
            validationResult.getMessages().InvalidArgument(EntityName.MoneyMovementTransaction, null, "New Payment Method");
            return validationResult;
        }

        switch (newPaymentMethod) {
            case ACHDebit:
            case ACHCredit:
            case CheckPayment:
            case EDI:
            case SuperCheck:
                break;
            default:
                validationResult.getMessages().CannotChangePaymentMethod(EntityName.MoneyMovementTransaction, moneyMovementTransactionId.toString(), newPaymentMethod.toString());

        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (payment.getMoneyMovementPaymentMethod() == newPaymentMethod) {
            return processResult;
        }

        payment.updateTaxPaymentMethod(newPaymentMethod, true);

        payment.addOrRemoveEnrollmentHold();
        return processResult;
    }


}
