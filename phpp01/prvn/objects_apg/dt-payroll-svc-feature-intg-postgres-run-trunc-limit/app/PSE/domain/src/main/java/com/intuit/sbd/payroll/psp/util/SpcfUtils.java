package com.intuit.sbd.payroll.psp.util;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Oct 3, 2007
 * Time: 11:20:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpcfUtils {
    public static SpcfMoney convertToSpcfMoney(BigDecimal pAmount) {
        return new SpcfMoney(pAmount.toString());
    }

    public static SpcfDecimal convertToSpcfDecimal(BigDecimal pAmount) {
        return SpcfDecimal.createInstance(pAmount.toString());
    }

    public static BigDecimal convertToBigDecimal(SpcfDecimal pAmount) {
        return new BigDecimal(pAmount.toString());
    }

    public static Date convertSpcfCalendarToDate(SpcfCalendar s) {
        if (s == null) return null;
        return new Date(s.toLocal().getTimeInMilliseconds());
    }

    public static long convertSpcfCalendarToAS400DateFormat(SpcfCalendar pSpcfCalendar) {
        return Long.parseLong(pSpcfCalendar.format("yyyyMMdd"));
    }

    public static XMLGregorianCalendar convertSpcfCalendarToXmlGregorianCalendar(SpcfCalendar pSpcfCalendar) throws Exception {
        if (pSpcfCalendar == null) return null;

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar();
        
        calendar.setDay(pSpcfCalendar.getDay());
        calendar.setMonth(pSpcfCalendar.getMonth());
        calendar.setYear(pSpcfCalendar.getYear());
        calendar.setHour(pSpcfCalendar.getHour());
        calendar.setMinute(pSpcfCalendar.getMinute());
        calendar.setSecond(pSpcfCalendar.getSecond());
        calendar.setMillisecond(pSpcfCalendar.getMillisecond());

        return calendar;
    }

    //null-safe SpcfDecimal math
    public static SpcfDecimal add(SpcfDecimal op1, SpcfDecimal op2) {
        if (op1 == null && op2 == null) {
            return SpcfMoney.ZERO;
        } else if (op1 == null) {
            return op2;
        } else if (op2 == null) {
            return  op1;
        }
        return op1.add(op2);
    }

    public static SpcfDecimal subtract(SpcfDecimal op1, SpcfDecimal op2) {
        if (op1 == null && op2 == null) {
            return SpcfMoney.ZERO;
        } else if (op1 == null) {
            return op2;
        } else if (op2 == null) {
            return  op1;
        }
        return op1.subtract(op2);
    }

    // null safe compare
    public static int compareSpcfDecimalTo(SpcfDecimal a, SpcfDecimal b) {
        if(a == null && b == null) {
            return 0;
        } else if(a != null && b == null) {
            return 1;
        } else if(a == null && b != null) {
            return -1;
        } else {
            return a.compareTo(b);
        }
    }

    public static SpcfCalendar convertXmlGregorianCalendarToSpcfCalendar(XMLGregorianCalendar pXMLGregorianCalendar) throws Exception {
        if (pXMLGregorianCalendar == null) return null;

        SpcfCalendar spcfCalendar = SpcfCalendar.createInstance();

        if(pXMLGregorianCalendar.getHour() > 0) {

            int hour = pXMLGregorianCalendar.getHour();
            int minute = pXMLGregorianCalendar.getMinute();
            int seconds = pXMLGregorianCalendar.getSecond();
            int milliseconds = pXMLGregorianCalendar.getMillisecond();
            if ( hour == DatatypeConstants.FIELD_UNDEFINED) {
                hour = 0;
            }
            if ( minute == DatatypeConstants.FIELD_UNDEFINED) {
                minute = 0;
            }
            if ( seconds == DatatypeConstants.FIELD_UNDEFINED) {
                seconds = 0;
            }
            if ( milliseconds == DatatypeConstants.FIELD_UNDEFINED) {
                milliseconds = 0;
            }

            spcfCalendar.setValues(pXMLGregorianCalendar.getYear(),
                    pXMLGregorianCalendar.getMonth(),
                    pXMLGregorianCalendar.getDay(),
                    hour,
                    minute,
                    seconds,
                    milliseconds);
        } else {
            spcfCalendar.setValues(pXMLGregorianCalendar.getYear(),
                    pXMLGregorianCalendar.getMonth(),
                    pXMLGregorianCalendar.getDay());
        }

        return spcfCalendar;    
}
    public static int getCheckDigit(String pNumber) {
        if(pNumber == null || pNumber.trim().equals("")){
            throw new RuntimeException("Error in calculating check digit for Number:"+ pNumber);
        }
        StringBuilder result= new StringBuilder("");
        for(int i=0; i < pNumber.length(); i++){
            int multiplier = (i % 2) + 1;
            result.append(Integer.parseInt(pNumber.subSequence(i, i+1).toString())*multiplier);
        }
        int resultSum = 0;
        for(int i=0; i < result.length(); i++){
            resultSum +=Integer.parseInt(result.subSequence(i, i+1).toString());
        }
        resultSum = resultSum % 10;
        if(resultSum == 0){
            return resultSum;
        } else {
            return 10 - resultSum;
        }
    }

}
