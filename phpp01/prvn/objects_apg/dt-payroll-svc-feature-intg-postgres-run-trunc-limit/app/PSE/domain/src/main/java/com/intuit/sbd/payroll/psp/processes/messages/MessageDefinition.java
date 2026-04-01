package com.intuit.sbd.payroll.psp.processes.messages;

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
public class MessageDefinition
{
    private static SpcfLogger logger = SpcfLogManager.getLogger(MessageDefinition.class);

    public static final String DEFAULT_BUNDLE_NAME = "resources/messageDefinitions";
    public static final MessageInfo.MessageLevel DEFAULT_LEVEL = MessageInfo.MessageLevel.ERROR;

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
     * information level
     */
    MessageInfo.MessageLevel level;

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
    public static MessageDefinition getMessageDefinition(int messageNumber)
    {
        MessageDefinition definition = new MessageDefinition();
        definition.setNumber(messageNumber);

        String keyPrefix = "message." + messageNumber;

        // read in message level and convert to enum
        MessageInfo.MessageLevel level = readLevel(keyPrefix);
        String templateText = readMessageFormat(keyPrefix);


        definition.setLevel(level);
        definition.setMessageFormat(templateText);
        return definition;
    }


    private static MessageInfo.MessageLevel readLevel(String keyPrefix)
    {
        MessageInfo.MessageLevel level = DEFAULT_LEVEL;
        String levelKey = keyPrefix + ".level";
        try
        {
            level = MessageInfo.MessageLevel.valueOf(messageDefinitions.getString(levelKey));
        }
        catch (IllegalArgumentException iae)
        {
            logger.warn(
                    "Invalid level specified for message number: " + keyPrefix + " - using default level: " + DEFAULT_LEVEL, iae);
        }
        catch (MissingResourceException mre)
        {
            //todo determine if we should throw this
            logger.warn("Invalid key: "+levelKey, mre);
        }
        return level;
    }

    private static String readMessageFormat(String keyPrefix)
    {
        try
        {
            return messageDefinitions.getString(keyPrefix + ".messageFormat");
        }
        catch (MissingResourceException mre)
        {
            //todo determine if we should throw this or a missingResourceException
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
     * Message Level - one of ERROR, WARNING, INFO
     *
     * @return
     */
    public MessageInfo.MessageLevel getLevel()
    {
        return level;
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

    void setLevel(MessageInfo.MessageLevel level)
    {
        this.level = level;
    }

    void setMessageFormat(String messageFormat)
    {
		this.messageFormat = messageFormat;
	}
}
