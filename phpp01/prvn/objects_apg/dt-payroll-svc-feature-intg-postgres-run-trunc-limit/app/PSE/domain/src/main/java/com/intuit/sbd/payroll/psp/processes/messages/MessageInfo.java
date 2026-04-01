package com.intuit.sbd.payroll.psp.processes.messages;

/**
 * The Message class holds an information accompanying the result of a process.
 */
public final class MessageInfo
{
    /**
     * Indicates if the message is a warning, an error, or an info
     */
    public MessageLevel Level;

    /**
     * Message code uniquely identifying the message.
     */
    public String MessageCode;

    /**
     * Text message describing the condition that happened
     */
    public String Message;

    /**
     * The source system id of a data element that is a root cause of a message.
     */
    public String SourceId;

    /**
     * The entity name of a data element that is a root cause of a message
     */
    public EntityName EntityName;

    /**
     * Message Level enumeration.
     */
    public enum MessageLevel
    {
        // The ordinal position matters as it is used to compare message level severity in the QBDTWS
        // TransmissionResponseServlet.
        INFO, WARNING, ERROR
    }
}
