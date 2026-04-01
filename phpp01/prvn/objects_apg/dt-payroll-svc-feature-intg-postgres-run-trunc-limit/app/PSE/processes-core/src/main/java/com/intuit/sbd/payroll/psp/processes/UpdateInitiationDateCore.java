package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 19, 2011
 * Time: 2:58:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateInitiationDateCore extends Process implements IProcess {
    private MoneyMovementTransaction moneyMovementTransaction;
    private String mmtId;
    private SpcfCalendar newInitiationDate;

    public UpdateInitiationDateCore(String id, SpcfCalendar pNewInitiationDate) {
        mmtId = id;
        newInitiationDate = pNewInitiationDate;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        if (mmtId == null || mmtId.length() == 0) {
            result.getMessages().RequiredInputMissingOrBlank(EntityName.MoneyMovementTransaction, "Money Movement Transaction Id", "Money Movement Transaction ID");
        } else {
            moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
            if (moneyMovementTransaction == null) {
                result.getMessages().NoEntityWithGivenId("MoneyMovementTransaction", mmtId);
            }
        }
        if (newInitiationDate == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "New Initiation Date");
        }
        if (!result.isSuccess()) {
            return result;
        }
        if (!moneyMovementTransaction.hasTaxCredits()) {
            result.getMessages().PaymentMethodDoesNotMatch(EntityName.MoneyMovementTransaction, "Payment Method", "EFTPS or EFTPSDirectDebit");
        }
        CalendarUtils.clearTime(newInitiationDate);
        SpcfCalendar nextOffloadDate = MoneyMovementTransaction.getNextInitiationDate(moneyMovementTransaction.getMoneyMovementPaymentMethod()).copy();
        CalendarUtils.clearTime(nextOffloadDate);
        if (newInitiationDate.before(nextOffloadDate)) {
            result.getMessages().NewInitiationDateBeforeOffloadDate(EntityName.MoneyMovementTransaction, "Initiation Date", newInitiationDate, MoneyMovementTransaction.getNextInitiationDate(moneyMovementTransaction.getMoneyMovementPaymentMethod()));
        }
        SpcfCalendar newDate = newInitiationDate.copy();
        CalendarUtils.clearTime(newDate);
        SpcfCalendar presentInitDate = moneyMovementTransaction.getInitiationDate().copy().toLocal();
        CalendarUtils.clearTime(presentInitDate);
        if(newDate.equals(presentInitDate)){
            result.getMessages().NewInitiationDateIsSame(EntityName.MoneyMovementTransaction, "Initiation Date", newDate, presentInitDate);
        }
        return result;
    }

    @Override
    public ProcessResult<MoneyMovementTransaction> process() {
        ProcessResult<MoneyMovementTransaction> processResult = new ProcessResult<MoneyMovementTransaction>();
        moneyMovementTransaction.updateTaxInitiationDate(newInitiationDate);
        processResult.setResult(moneyMovementTransaction);
        return processResult;
    }
}
