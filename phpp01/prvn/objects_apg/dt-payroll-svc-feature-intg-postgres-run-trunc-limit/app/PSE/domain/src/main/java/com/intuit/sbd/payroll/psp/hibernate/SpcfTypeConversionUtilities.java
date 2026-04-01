package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: allenc289
 * Date: Oct 14, 2010
 * Time: 9:22:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpcfTypeConversionUtilities {
        /**
     * Converts non-portable SQL timestamp to portable calendar.
     * @param timestamp SQL timestamp.
     * @return Portable calendar, or null if date is null.
     */
    public static final SpcfCalendar convertSqlTimestampToSpcfCalendar(Timestamp timestamp)
    {
        return timestamp == null ? null : SpcfCalendar.createInstance(timestamp.getTime());
    }

    /**
     * Converts portable calendar to non-portable SQL timestamp.
     * @param calendar Calendar to convert.
     * @return SQL timestamp, or null if calendar is null.
     */
    public static final Timestamp convertSpcfCalendarToSqlTimestamp(SpcfCalendar calendar)
    {
        return calendar == null ? null : new Timestamp(calendar.getTimeInMilliseconds());
    }    

    /**
     * Converts string ID to portable unique ID.
     * @param stringId String ID.
     * @return Portable unique ID.
     */
    public static final SpcfUniqueId convertStringToUniqueId(String stringId)
    {
        return stringId == null ? null : SpcfUniqueId.createInstance(stringId);
    }


    /**
     * Converts portable unique ID to string ID.
     * @param id Unique ID.
     * @return String ID.
     */
    public static final String convertUniqueIdToString(SpcfUniqueId id)
    {
        return id == null ? null : id.toString();
    }
    
}
