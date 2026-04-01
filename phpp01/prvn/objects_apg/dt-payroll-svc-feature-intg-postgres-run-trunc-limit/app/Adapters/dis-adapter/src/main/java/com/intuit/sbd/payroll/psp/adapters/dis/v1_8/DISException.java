package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import java.util.ArrayList;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
