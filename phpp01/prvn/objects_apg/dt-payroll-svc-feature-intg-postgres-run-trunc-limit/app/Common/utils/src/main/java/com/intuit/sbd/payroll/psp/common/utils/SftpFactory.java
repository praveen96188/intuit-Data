package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.lang.reflect.Constructor;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 15, 2011
 * Time: 2:43:17 PM
 */
public class SftpFactory {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(Transporter.class);
    private static Class<? extends Transporter> instanceClass = Transporter.class;

    public static void setInstanceClass(Class<? extends Transporter> instanceClass) {
        SftpFactory.instanceClass = instanceClass;
    }

    public static Transporter createInstance(String pHost, String pUsername, String passKey, boolean useAuthKey){
        try {
            Class[] argsClass = new Class[]{String.class, String.class, String.class, boolean.class};
            Object[] args = new Object[]{pHost, pUsername, passKey, useAuthKey};
            Constructor constructor = instanceClass.getConstructor(argsClass);
            return (Transporter)constructor.newInstance(args);
        } catch (Throwable t) {
            logger.info("SimpleSftpFile could not be constructed.", t);
            return null;
        }
    }
}
