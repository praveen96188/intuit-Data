package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/DISMessageDefinition.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Object representation of properties file defined message.
 * Messages have the following properties:
 * Number
 * Source
 * TemplateText
 * This was copied from EWS codeline.
 */
class DISMessageDefinition
{
    private static SpcfLogger logger = SpcfLogManager.getLogger(DISMessageDefinition.class);

    public static final String DEFAULT_BUNDLE_NAME = "resources/DISMessageDefinitions";

    /**
     * The name of the bundle -- see ResourceBundle documentation for rules regarding the
     * name and how it affects the search for the bundle.
     */
    private static String bundleName;

    /**
     * all messages are defined in a single file that must be placed on the classpath
     */
    private static ResourceBundle messageDefinitions;

    /**
     * unique message identifier
     */
    private int number;

    /**
     * template text for forming eventual message; message format
     * must conform with {@link java.text.MessageFormat} rules.
     */
    private String messageFormat;

    /**
     * This static intializer block will throw a RuntimeException
     * if the properties file cannot be found and loaded.
     */
    static
    {
        bundleName = DEFAULT_BUNDLE_NAME;
        loadBundle(DEFAULT_BUNDLE_NAME);
    }

    public static void loadBundle(String bundleName)
    {
        try
        {
            messageDefinitions = ResourceBundle.getBundle(bundleName);
        }
        catch (Exception e)
        {
            String message = "Unable to load DISMessageDefinition bundle: " + bundleName;
            logger.fatal(message, e);
            throw new RuntimeException(message, e);
        }
    }

    /**
     * Factory method to create a MessageInfoDefintion.
     *
     * @param messageNumber unique identifier used to search properties file
     *                      for message definition values
     * @throws RuntimeException if no entry is found in the properties
     *                          file for the requested message number.
     * @return populated defintion instance
     */
    public static DISMessageDefinition getMessageDefinition(int messageNumber)
    {
        DISMessageDefinition definition = new DISMessageDefinition();
        definition.setNumber(messageNumber);

        String keyPrefix = "message." + messageNumber;

        String templateText = readMessageFormat(keyPrefix);

        definition.setMessageFormat(templateText);
        return definition;
    }

    /***
     *
     * @param keyPrefix
     * @return
     */
    private static String readMessageFormat(String keyPrefix)
    {
        try
        {
            return messageDefinitions.getString(keyPrefix + ".messageFormat");
        }
        catch (MissingResourceException mre)
        {
            throw new RuntimeException(
                    "Message number " + keyPrefix + " is not configured correctly -- missing messageFormat");
        }
    }

    /**
     * Unique message number.
     *
     * @return int
     */
    public int getNumber()
    {
        return number;
    }

    /**
     * The message string the user will be presented
     * with.  It may contain substitution markers
     * that are compliant with the {@link java.text.MessageFormat}
     * API.
     *
     * @return templated message text
     */
    public String getMessageFormat()
    {
        return messageFormat;
    }

    void setNumber(int number)
    {
        this.number = number;
    }

    void setMessageFormat(String messageFormat)
    {
		this.messageFormat = messageFormat;
	}
}