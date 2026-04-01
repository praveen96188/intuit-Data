/**
 * User.java
 *
 * Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */


package com.paycycle.user;

//import com.paycycle.data.Transaction;

public class UserException extends RuntimeException {
    private long m_helpId = 0;
    private Object m_clientData = null;
    private UserExceptionErrorCode errorCode = UserExceptionErrorCode.NoCode;

    public UserException() {
        //KP Transaction.suppressUncommittedWarning();
        errorCode = UserExceptionErrorCode.NoCode;
    }

    public UserException(String msg) {
        super(msg);
        //KP Transaction.suppressUncommittedWarning();
        errorCode = UserExceptionErrorCode.NoCode;
        m_helpId = 0;
    }

    public UserException(UserExceptionErrorCode code, String msg) {
        super(msg);
        //KP Transaction.suppressUncommittedWarning();
        errorCode = code;
        m_helpId = 0;
    }

    public UserException(String msg, Throwable cause) {
        super(msg, cause);
        //KP Transaction.suppressUncommittedWarning();
        m_helpId = 0;
    }

    public UserExceptionErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(UserExceptionErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public void setHelpId(long helpId) {
        m_helpId = helpId;
    }

    public long getHelpId() {
        return m_helpId;
    }

    public void setClientData(Object clientData) {
        m_clientData = clientData;
    }

    public Object getClientData() {
        return m_clientData;
    }

    public String getHelpIdStr() {
        return String.valueOf(m_helpId);
    }
}
