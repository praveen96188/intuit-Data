package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import java.util.ArrayList;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/DISException.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * DISException class that provide mechanism for processes to halt and return the error
 *    reason as part of the exception.
 *
 */
public class DISException extends Exception {
    private List<DISMessage> disMessages;
    private Throwable sourceException;

    public DISException(DISMessage pDisMessage) {

        this.disMessages=new ArrayList<DISMessage>();
        this.disMessages.add(pDisMessage);
    }

    public DISException(DISMessage pDisMessage, Throwable pSourceException) {
        this(pDisMessage);
        this.sourceException = pSourceException;
    }

    public DISException(List<DISMessage> pDisMessages) {
        this.disMessages=pDisMessages;
    }

    public List<DISMessage> getDisMessages() {
        return this.disMessages;
    }

    public void setDisMessage(List<DISMessage> pDISMessages) {
        this.disMessages = pDISMessages;
    }

    public Throwable getSourceException() {
        return sourceException;
    }

    public void setSourceException(Throwable pSourceException) {
        sourceException = pSourceException;
    }
}
