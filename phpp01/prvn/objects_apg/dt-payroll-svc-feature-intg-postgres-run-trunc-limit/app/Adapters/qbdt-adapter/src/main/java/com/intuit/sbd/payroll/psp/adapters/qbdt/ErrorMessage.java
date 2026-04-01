package com.intuit.sbd.payroll.psp.adapters.qbdt;

/**
 * Error Message DTO to hold OFX error code and Error description.
 *
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Feb 12, 2008
 * Time: 5:58:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class ErrorMessage {

    private String ofxErrorCode;
    private String messageCode;
    private String errorDescription;
    private String transmissionErrorDescription;
    private ErrorMessages.ErrorEnum uniqueErrorIdentifier;

    /**
     * ErrorMessage
     *
     * @param pOfxErrorCode - OFX Error code
     * @param pErrorDescription
     */
    public ErrorMessage(ErrorMessages.ErrorEnum pUniqueErrorIdentifier,String pOfxErrorCode,String pErrorDescription,String pTransmissionErrorDescription,String pMessageCode) {
        this.ofxErrorCode = pOfxErrorCode;
        this.messageCode = pMessageCode;
        this.errorDescription = pErrorDescription;
        this.transmissionErrorDescription = pTransmissionErrorDescription;
        this.uniqueErrorIdentifier = pUniqueErrorIdentifier;
    }

    /**
     * Return OFX error code.
     *
     * @return - OFX error code string.
     */
    public String getOfxErrorCode() {
        return ofxErrorCode;
    }

    /**
     * Return error description.
     *
     * @return - Error description string.
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Return error description.
     *
     * @return - Error description string.
     */
    public String getTransmissionErrorDescription() {
        return transmissionErrorDescription;
    }

    /**
     * Return message code.
     *
     * @return - Message code associated with this error.
     */
    public String getMessageCode() {
        return messageCode;
    }

    /**
     * Return a unique string representing this error.
     *
     * @return - Error description string.
     */
    public ErrorMessages.ErrorEnum getUniqueErrorIdentifier() {
        return uniqueErrorIdentifier;
    }


    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        result.append("ErrorMessage:");
        result.append(newLine);

        result.append("=============");
        result.append(newLine);

        result.append("Error Description: " + this.getErrorDescription());
        result.append(newLine);

        result.append("Ofx Error Code: " + this.getOfxErrorCode());
        result.append(newLine);

        result.append("Transmission Error Description: " + this.getTransmissionErrorDescription());
        result.append(newLine);

        result.append("Unique Error Identifier: " + this.getUniqueErrorIdentifier());
        result.append(newLine);

        result.append("Message Code: " + this.getMessageCode());
        result.append(newLine);
        return result.toString();
    }

}
