import org.junit.Test;
import tool.EmailTrigger;
import tool.TaxCreditsEmail;
import tool.TaxCreditsEmailSender;

import java.util.Date;

/**
 * User: dweinberg
 * Date: Apr 20, 2010
 * Time: 4:05:07 PM
 * These actually send emails through IAS/XT
 */
public class SendEmailTests {



    @Test
    public void testSendDavidIntroEmail() throws Exception {        
        System.setProperty("javax.net.ssl.trustStore", "C:/dev/PSP/main/Gateways/Email/resources/pspclient.test.intuit.com.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "intuit");
        System.setProperty("javax.net.ssl.keyStore", "C:/dev/PSP/main/Gateways/Email/resources/pspclient.test.intuit.com.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "intuit");

        TaxCreditsEmail email = new TaxCreditsEmail();
        email.setEmailAddress("DAVID_WEINBERG@INTUIT.COM");
        email.setName("DAVID WEINBERG");
        email.setCanEmail(true);
        email.setFirstName("DAVID");
        email.setHireDate(new Date());
        email.calculate(new Date());
        TaxCreditsEmailSender sender = TaxCreditsEmailSender.createInstance();
        sender.sendEmail(email, EmailTrigger.REMINDER_TRIGGER);

    }
}
