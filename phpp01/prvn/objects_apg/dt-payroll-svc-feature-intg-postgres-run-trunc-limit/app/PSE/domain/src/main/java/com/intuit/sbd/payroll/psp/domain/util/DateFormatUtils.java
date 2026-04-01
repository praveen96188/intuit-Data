package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@Slf4j
public class DateFormatUtils {

    public static SpcfCalendar dateStringToSpcfCalendar(String date) {
        SpcfCalendar instance = null;
        try {

            instance=  SpcfCalendar.parse("yyyy/MM/dd HH:mm:ss.SSS",date);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }

    public static String manipulateDateToString(SpcfCalendar date, int numberOfDays){
        SpcfCalendar tempDate = date.copy();
        tempDate.addDays(numberOfDays);
        String manipulatedDate = SpcfCalendar.toDateLiteral(tempDate);
        if(Objects.isNull(manipulatedDate)){
            log.warn("Null Date returned from manipulateDateToString");
        }
        return manipulatedDate;
    }
}