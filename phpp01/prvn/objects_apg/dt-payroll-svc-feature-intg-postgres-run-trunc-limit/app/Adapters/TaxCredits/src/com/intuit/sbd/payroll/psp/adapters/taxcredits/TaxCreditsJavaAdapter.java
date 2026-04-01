package com.intuit.sbd.payroll.psp.adapters.taxcredits;

import com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter.AddressDoesNotExistException;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.MessageException;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;
import flex.messaging.services.remoting.adapters.JavaAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: rnorian
 * Date: Jun 16, 2008
 * Time: 9:27:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaxCreditsJavaAdapter extends JavaAdapter {

    private static SpcfLogger logger = PayrollServices.getLogger(TaxCreditsJavaAdapter.class);

    private static final String RETHROW_EXCEPTION_MESSAGE = "Error in method %1$s.%2$s():%3$s";

    public TaxCreditsJavaAdapter() {
        super();
    }

    public TaxCreditsJavaAdapter(boolean value) {
        super(value);
    }

    @Override
    public Object invoke(Message message) {
        //NOTE: the principal must be set before any modifications to the DB
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TaxCredits));

        try {
            return super.invoke(message);
        } catch (Throwable t) {
            String source = ((RemotingMessage)message).getSource();
            String className = source.substring(source.lastIndexOf('.') + 1, source.length());
            String operation = ((RemotingMessage)message).getOperation();
            rethrowException(className, operation, t);
            return null;
        }
    }

    public void rethrowException(String className, String methodName, Throwable e) {
        //this is public facing so we want to expose a minimum of information
        //in this case we will use className, methodName, and the message from the exception and that should
        //be enough for troubleshooting without exposing too many details.
        MessageException dse = new MessageException();

        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }

        String exceptionMessage; //the one we will include in the response
        if (t.getMessage() != null) {
            exceptionMessage = t.getMessage();
        } else {
            exceptionMessage = t.getClass().getName();
        }
        
        String message = String.format(RETHROW_EXCEPTION_MESSAGE, className, methodName, exceptionMessage);
        dse.setMessage(message);

        //log as error if needed
        //do not log address does not exist exceptions, but do log if HUD goes down
        if (! ( e instanceof MessageException)) {
            logger.error("Tax Credits Exception", e);
        } else {
            MessageException mse = (MessageException) e;
            if (! (mse.getRootCause() instanceof AddressDoesNotExistException)) {
                logger.error("Tax Credits Exception", mse.getRootCause() == null ? mse : mse.getRootCause());
            }
        }



        throw dse;
    }
}