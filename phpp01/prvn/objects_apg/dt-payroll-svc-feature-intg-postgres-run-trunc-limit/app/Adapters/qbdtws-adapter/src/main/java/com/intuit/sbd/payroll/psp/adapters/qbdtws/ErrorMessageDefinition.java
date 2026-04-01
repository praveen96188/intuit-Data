package com.intuit.sbd.payroll.psp.adapters.qbdtws;


import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * Object representation of properties file defined message.
 * Messages have the following properties:
 * Number
 * Source
 * TemplateText
 * ...
 */
class ErrorMessageDefinition
{
    private static SpcfLogger logger = SpcfLogManager.getLogger(ErrorMessageDefinition.class);
                                                     
    public static final String DEFAULT_BUNDLE_NAME = "resources/QBDTWSMessageDefinitions";

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
            String message = "Unable to load MessageDefinition bundle: " + bundleName;
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
    public static ErrorMessageDefinition getMessageDefinition(int messageNumber)
    {
        ErrorMessageDefinition definition = new ErrorMessageDefinition();
        definition.setNumber(messageNumber);

        String keyPrefix = "message." + messageNumber;

        // read in message level and convert to enum
        String templateText = readMessageFormat(keyPrefix);

        definition.setMessageFormat(templateText);
        return definition;
    }

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
     * @return
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

    protected void setNumber(int number)
    {
        this.number = number;
    }

    protected void setMessageFormat(String messageFormat)
    {
        this.messageFormat = messageFormat;
    }
}
