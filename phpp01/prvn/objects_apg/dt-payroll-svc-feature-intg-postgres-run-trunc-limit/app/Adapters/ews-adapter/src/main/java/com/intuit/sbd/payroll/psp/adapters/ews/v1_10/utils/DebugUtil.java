package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessage;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.OverrideVerificationTransactionAmounts;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class DebugUtil {

    private static SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(DebugUtil.class);
    }    

    public static void OverrideRandomDebits(String pSourceCompanyId, String pSourceCompanyBankAccountId) throws Exception {
        Application.getHibernateSession().flush();

        SpcfMoney randomAmt1 = new SpcfMoney(".12");
        SpcfMoney randomAmt2 = new SpcfMoney(".34");
        SpcfCalendar originalDate = PSPDate.getPSPTime();
        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(settlementDate, -2);
        CalendarUtils.clearTime(settlementDate);
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(pspDate, -3);
        pspDate.setValues(pspDate.getYear(), pspDate.getMonth(), pspDate.getDay(), 17, 0, 0, 0);
        PSPDate.setPSPTime(pspDate);
        OverrideVerificationTransactionAmounts OverrideDebitAmounts = new OverrideVerificationTransactionAmounts
                (SourceSystemCode.QBDT, pSourceCompanyId, pSourceCompanyBankAccountId, randomAmt1, randomAmt2, settlementDate);
        ProcessResult processResult = OverrideDebitAmounts.validate();
        if (!processResult.isSuccess()) {
            MessageList messages = processResult.getMessages();
            for (Message message : messages) {
                logger.debug("Message Code: " + message.getMessageCode() +" Message: " + message.getMessage());               
            }
            EwsMessage ewsMessage = EwsMessages.forceRandomAmountProcessError();
            ewsMessage.setMessage(messages.toString());
            throw new EwsException(ewsMessage);
        }
        processResult = OverrideDebitAmounts.process();
        if (!processResult.isSuccess()) {
            MessageList messages = processResult.getMessages();
            for (Message message : messages) {
                logger.debug("Message Code: " + message.getMessageCode() +" Message: " + message.getMessage());
            }
            EwsMessage ewsMessage = EwsMessages.forceRandomAmountProcessError();
            ewsMessage.setMessage(messages.toString());
            throw new EwsException(ewsMessage);
        }
        PSPDate.setPSPTime(originalDate);
    }

    public static boolean isCompanyDebugLoggingOn(String pPSID, String pEIN, String pSubscriptionNumber) {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            if (pPSID != null) {
                Company company = PspFactory.findCompany(pPSID);
                return company.getDebugLogging();
            } else if (pEIN != null) {
                Company company = PspFactory.findCompanyByEin(pEIN, pSubscriptionNumber);
                return company.getDebugLogging();
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            return false;
        } finally {
            PayrollServices.commitUnitOfWork();
        }
    }

}
