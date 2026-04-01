/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/SAPTranslator.java#5 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * SAPTranslator - SAP translator class for translating basic data types between SPCF types and SAP usable types
 */
@SuppressWarnings({"deprecation"})
public class SAPTranslator {

    public static final int DEFAULT_NUMBER_VALUE = -1;
    private static final SpcfLogger logger = PayrollServices.getLogger(SAPTranslator.class); ;

    public static Date getDateFromSpcfCalendar(SpcfCalendar s) {
        if (s == null) return null;
        return new Date(s.toLocal().getTimeInMilliseconds());
    }

    public static Date getDateFromSpcfCalendarNoTime(SpcfCalendar s) {
        if (s == null) return null;
        Date sDate = new Date(s.toLocal().getTimeInMilliseconds());
        return new Date(sDate.getYear(), sDate.getMonth(), sDate.getDate());
    }

    public static Date createDate(int year, int month, int day) {
        return getDateFromSpcfCalendar(SpcfCalendar.createInstance(year, month, day, SpcfTimeZone.getLocalTimeZone()));
    }

	public static Date getDateFromSpcfCalendarNoToLocalNoTime(SpcfCalendar s) {
        if (s == null) return null;
        return new Date(s.getYear() -1900, s.getMonth() -1, s.getDay());
	}

    public static String getUserNameFromUserID(String userID) {
        return getUserNameFromUserID(userID, null);
    }

    public static String getUserNameFromUserID(String userID, Map<String, String> cache) {

        if (StringUtils.isEmpty(userID)) {
            return "";
        }

        //only try to get actual users, not system principals
        if (!userID.matches("\\d{11}")) {
            return userID;
        }

        if (cache != null) {
            if (cache.containsKey(userID)) {
                return cache.get(userID);
            }
        }

        AuthUser user = AuthUser.findUser(userID);
        String userName = user != null ? user.getFirstName() + " " + user.getLastName() : userID;

        if (cache != null) {
            cache.put(userID, userName);
        }

        return userName;
    }

    @Deprecated
    public static double getDoubleFromSpcfMoney(SpcfDecimal pDecimal) {
        if (pDecimal == null)
            return DEFAULT_NUMBER_VALUE;

        return SpcfUtils.convertToBigDecimal(pDecimal).doubleValue();
    }

    public static double getDoubleFromSpcfMoneyNullZero(SpcfDecimal pDecimal) {
        if (pDecimal == null) {
            return 0.;
        } else {
            return SpcfUtils.convertToBigDecimal(pDecimal).doubleValue();
        }
    }

    //todo this is so wrong... Should use NaN and BigDecimal if need this semantics
    @Deprecated
    public static SpcfMoney getSpcfMoneyFromDouble(double pAmount) {
        if(Double.compare(pAmount, DEFAULT_NUMBER_VALUE) == 0) return null;
        BigDecimal amount = new BigDecimal(pAmount);
        return SpcfUtils.convertToSpcfMoney(amount);
    }

    public static SpcfMoney getSpcfMoneyFromDoubleNoSentinel(double pAmount) {
        if (Double.isNaN(pAmount) || Double.isNaN(pAmount)) {
            return null;
        }
        BigDecimal amount = new BigDecimal(pAmount);
        return SpcfUtils.convertToSpcfMoney(amount);
    }

    public static SpcfMoney getSpcfMoneyFromString(String pAmount) {
        return getSpcfMoneyFromString(pAmount, SpcfMoney.ZERO);
    }

    public static SpcfMoney getSpcfMoneyFromString(String pAmount, SpcfMoney defaultValue) {
        if(StringUtils.isEmpty(pAmount)){
            return defaultValue;
        }
        return new SpcfMoney(pAmount);
    }

    public static SpcfCalendar getSpcfCalendarFromDate(Date pDate) {
        if (pDate == null)
            return null;

        return SpcfCalendar.createInstance(pDate.getTime(), SpcfTimeZone.getLocalTimeZone());
    }

    /**
     * Returns SpcfCalender from a date with the time set to 00:00:00.0
     * @param pDate - input date
     * @return SpcfCalendar
     */
    public static SpcfCalendar getSpcfCalendarFromDate_BeginDay(Date pDate) {
        if (pDate == null)
            return null;
        SpcfCalendar spcfCalendar = SpcfCalendar.createInstance(pDate.getTime(), SpcfTimeZone.getLocalTimeZone());
        spcfCalendar.setValues(spcfCalendar.getYear(), spcfCalendar.getMonth(), spcfCalendar.getDay(), 0, 0, 0, 0);
        return spcfCalendar;
    }

    /**
     * Returns SpcfCalender from a date with the time set to 23:59:59.999
     * @param pDate - input date
     * @return SpcfCalendar
     */
    public static SpcfCalendar getSpcfCalendarFromDate_EndDay(Date pDate) {
        if (pDate == null)
            return null;
        SpcfCalendar spcfCalendar = SpcfCalendar.createInstance(pDate.getTime(), SpcfTimeZone.getLocalTimeZone());
        spcfCalendar.setValues(spcfCalendar.getYear(), spcfCalendar.getMonth(), spcfCalendar.getDay(), 23, 59, 59, 999);
        return spcfCalendar;
    }


    // compares two different "complex" objects (beans)
    public static List getDifferences(Object obj1, Object obj2) {
        ArrayList<String> changes = new ArrayList<String>();
        if ((obj1 == null) || (obj2 == null)) {
            if ((obj1 != null) || (obj2 != null))
                changes.add("Different");

            return changes;
        }

        try {
            // different types
            Class class1 = obj1.getClass();
            Class class2 = obj2.getClass();
            if (!class1.toString().equals(class2.toString())) {
                return changes;
            }

            // compare if it's a primitive or String
            if (class1.toString().startsWith("class java.lang.")) {
                if (!obj1.equals(obj2))
                    changes.add("Different");

                return changes;
            }

            // check for iterable to handle bottom two
            if (obj1 instanceof Iterable) {
                Iterator iter2 = ((Iterable)obj2).iterator();
                for (Object iter1Obj: (Iterable)obj1) {
                    Object iter2Obj = null;
                    try {
                        iter2Obj = iter2.next();
                    } catch (NoSuchElementException nsee) {
                        changes.add("Different");
                        return changes;
                    }

                    List subChanges = getDifferences(iter1Obj, iter2Obj);
                    if ((subChanges != null) && (subChanges.size() > 0)) {
                        changes.add("Different");
                        return changes;
                    }
                }
            }

            // handle basic arrays
            if (obj1 instanceof Object[]) {
                Object[] arr1 = (Object[])obj1;
                Object[] arr2 = (Object[])obj2;

                if (arr1.length != arr2.length) {
                    changes.add("Different");
                    return changes;
                }

                for (int i = 0; i < arr1.length; i++) {
                    List subChanges = getDifferences(arr1[i], arr2[i]);
                    if ((subChanges != null) && (subChanges.size() > 0)) {
                        changes.add("Different");
                        return changes;
                    }
                }
            }

            BeanUtilsBean bub = new BeanUtilsBean();
            bub.getPropertyUtils().addBeanIntrospector(
                    SuppressPropertiesBeanIntrospector.SUPPRESS_CLASS);

            Map props1 = bub.describe(obj1);
            Map props2 = bub.describe(obj2);

            // different number of properties
            if (props1.keySet().size() != props2.keySet().size()) {
                return null;
            }

            Set keys = props1.keySet();
            // compare properties
            for (Object key : keys) {
                String propertyName = (String)key;

                Object val1 = bub.getProperty(obj1, propertyName);
                Object val2 = bub.getProperty(obj2, propertyName);

                List subChanges = getDifferences(val1, val2);
                if (subChanges == null || subChanges.size() > 0) {
                    changes.add(propertyName);
                }
            }

            return changes;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getEmployeeFullName(Employee employee) {
        if(employee != null){
            StringBuffer employeeName = new StringBuffer();
            if(employee.getLastName() != null){
                employeeName.append(employee.getLastName());
            }
            employeeName.append(", ");
            if(employee.getFirstName() != null){
                employeeName.append(employee.getFirstName());
            }
            if(employee.getMiddleName() != null){
                employeeName.append(" ").append(employee.getMiddleName());
            }
            if(employee.getSuffix() != null){
                employeeName.append(" ").append(employee.getSuffix());
            }

            return employeeName.toString();
        }
        return null;
    }

    public static Date getGMTFormatDateWithDSTHandled(Date pDate){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a");
        DateFormat gmtFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a");
        TimeZone gmtTime = TimeZone.getTimeZone("GMT");
        gmtFormat.setTimeZone(gmtTime);
        Date formattedDate = pDate;
        try {
          if (pDate != null) {
                formattedDate = formatter.parse(gmtFormat.format(pDate));
            }
        } catch (ParseException e) {
             logger.warn("Parse Exception: "+pDate);
        }
        return formattedDate;
    }

    public static String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.replaceAll("[a-zA-Z]", "");
    }


    public static String getDateFormat(Date scheduleDate) {
        if (scheduleDate == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateFormat = formatter.format(scheduleDate);
        return dateFormat;
    }


}
