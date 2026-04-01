package com.intuit.sbd.payroll.psp.common.pgp;

import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpEncryptedReader;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpUnencryptedReader;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/11/13
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class PgpReaderFactory {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(PgpReaderFactory.class);
    private static Class<? extends PgpReader> instanceClass;

    public static void setInstanceClass(Class<? extends PgpReader> instanceClass) {
        PgpReaderFactory.instanceClass = instanceClass;
    }

    public static PgpReader createInstance(){
        try {
            if (instanceClass != null) {
                return instanceClass.newInstance();
            }

            boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
            if (enableEncryption) {
                return new PgpEncryptedReader();
            } else {
                return new PgpUnencryptedReader();
            }
        } catch (Throwable t) {
            logger.error("Pgp reader could not be constructed.", t);
            return null;
        }
    }

}
