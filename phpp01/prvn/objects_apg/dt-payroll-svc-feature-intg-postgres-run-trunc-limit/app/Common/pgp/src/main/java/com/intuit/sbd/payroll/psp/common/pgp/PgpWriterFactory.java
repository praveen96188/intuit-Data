package com.intuit.sbd.payroll.psp.common.pgp;

import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpEncryptedWriter;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpUnencryptedWriter;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 2/8/13
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PgpWriterFactory {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(PgpWriterFactory.class);
    private static Class<? extends PgpWriter> instanceClass;

    public static void setInstanceClass(Class<? extends PgpWriter> instanceClass) {
        PgpWriterFactory.instanceClass = instanceClass;
    }

    public static PgpWriter createInstance(){
        try {
            if (instanceClass != null) {
                return instanceClass.newInstance();
            }

            boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
            if (enableEncryption) {
                return new PgpEncryptedWriter();
            } else {
                return new PgpUnencryptedWriter();
            }
        } catch (Throwable t) {
            logger.error("Pgp writer could not be constructed.", t);
            return null;
        }
    }

}
