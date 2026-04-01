package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import java.lang.reflect.Constructor;

/**
 * Created by anandp233 on 2/23/14.
 */
public class ClassLocator {

    public static <T> T getInstance(String implClassName) {
        Object instance = null;
        try {
            Class service = null;
            service = implClassName != null ? Class.forName(implClassName) : null;
            instance = service != null ? service.newInstance() : null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) instance;
    }

    public static <T> T getInstance(Class implClassName) {
        return getInstance(implClassName.getName());
    }

    public static <T> T getInstance(Class implClassName, Object... args) throws Throwable {
        return getInstance(implClassName.getName(), args);
    }

    public static <T> T getInstance(String implClassName, Object... args) throws Throwable {
        Object instance = null;
        try {
            Class service = null;
            service = implClassName != null ? Class.forName(implClassName) : null;
            Class[] parameters = new Class[args.length];
            int i = 0;
            for (Object a : args) {
                parameters[i++] = a.getClass();
            }
            Constructor constructor = service.getConstructor(parameters);
            instance = constructor != null ? constructor.newInstance(args) : null;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
        return (T) instance;
    }

}
