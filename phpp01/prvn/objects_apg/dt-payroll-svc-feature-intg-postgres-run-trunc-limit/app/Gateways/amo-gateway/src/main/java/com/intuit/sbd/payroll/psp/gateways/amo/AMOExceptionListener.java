package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.jms.connection.CachingConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 5, 2010
 * Time: 9:47:44 AM
 */
public class AMOExceptionListener implements ExceptionListener {
    private static SpcfLogger logger = SpcfLogManager.getLogger(AMOGateway.class);
    private ConnectionFactory mConnectionFactory;

    public AMOExceptionListener(CachingConnectionFactory pCachingConnectionFactory) {
        mConnectionFactory = pCachingConnectionFactory;
    }

    public void onException(JMSException e) {                
        logger.error("Error thrown by the AMO connection factory.", e);
        if(mConnectionFactory != null && e != null && e.getMessage().contains("closed")) {
            try {
                mConnectionFactory.createConnection();
            } catch (JMSException e1) {
                logger.error("Could not reconnect to AMO", e);
            }
        }
    }
}
