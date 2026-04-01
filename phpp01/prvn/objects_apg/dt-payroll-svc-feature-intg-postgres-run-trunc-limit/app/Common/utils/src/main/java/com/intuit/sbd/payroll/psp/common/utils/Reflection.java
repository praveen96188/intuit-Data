package com.intuit.sbd.payroll.psp.common.utils;

import java.lang.reflect.Constructor;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Sep 22, 2008
 * Time: 8:14:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class Reflection {
    public static Object createInstance(Class pClass, Class[] pParameterTypes, Object[] pInitArgs) {
        try {
            Constructor ctor = pClass.getDeclaredConstructor(pParameterTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(pInitArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object createInstance(String pClassName, Class[] pParameterTypes, Object[] pInitArgs) {
        try {
            return createInstance(Class.forName(pClassName), pParameterTypes, pInitArgs);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
