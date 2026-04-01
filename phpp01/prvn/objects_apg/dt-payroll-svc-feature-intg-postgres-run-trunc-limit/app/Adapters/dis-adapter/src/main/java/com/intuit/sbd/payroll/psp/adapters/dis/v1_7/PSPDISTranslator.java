package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.*;
import com.intuit.sbd.payroll.psp.domain.*;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPDISTranslator.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Class to centralize translations between PSP and the Web Services.
 *
 */
public class PSPDISTranslator {

    /***
     * Translate PSP source system cd into DIS SourceSystemEnum
     * @param pSourceSystemCd
     * @return
     */
    public static SourceSystemEnum translateSourceSystemCd(SourceSystemCode pSourceSystemCd) {
        switch(pSourceSystemCd) {
            case QBDT:
                return SourceSystemEnum.QBDT;
        }
        throw new UnsupportedOperationException("SourceSystemCd " + pSourceSystemCd + " is unsupported.");
    }

    /***
     * Translate DIS SourceSystemEnum to PSP source system cd
     * @param pSourceSystemEnum
     * @return
     */
    public static SourceSystemCode translateSourceSystemCode(SourceSystemEnum pSourceSystemEnum ) {
        switch(pSourceSystemEnum) {
            case QBDT:
                return SourceSystemCode.QBDT;
        }
        throw new UnsupportedOperationException("SourceSystemEnum " + pSourceSystemEnum + " is unsupported.");
    }

}
