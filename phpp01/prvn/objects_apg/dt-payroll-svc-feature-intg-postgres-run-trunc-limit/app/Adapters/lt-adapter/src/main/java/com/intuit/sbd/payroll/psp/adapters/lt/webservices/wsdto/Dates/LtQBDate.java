package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QBDate", propOrder = {
    "day",
    "month",
    "year"
})
public class LtQBDate {
    @XmlElement(name = "Day", required = true)
    protected int day;

    @XmlElement(name = "Month", required = true)
    protected int month;

    @XmlElement(name = "Year", required = true)
    protected int year;

    public LtQBDate() {
    }

    /**
     * Gets the value of the day property.
     *
     */
    public int getDay() {
        return day;
    }

    /**
     * Sets the value of the day property.
     *
     */
    public void setDay(int value) {
        this.day = value;
    }

    /**
     * Gets the value of the month property.
     *
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     *
     */
    public void setMonth(int value) {
        this.month = value;
    }

    /**
     * Gets the value of the year property.
     *
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the value of the year property.
     *
     */
    public void setYear(int value) {
        this.year = value;
    }

    @Override
    public int hashCode() {
        // bit shifting and masking would be nicer of course but this should work easily enough
        int hashCode = (Integer.toString(getYear()) + Integer.toString(getMonth()) + Integer.toString(getDay())).hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LtQBDate qbDate = (LtQBDate) o;

        if (day != qbDate.day) return false;
        if (month != qbDate.month) return false;
        if (year != qbDate.year) return false;

        return true;
    }

    @Override
    public String toString() {
        return "QBDate{" +
                "day=" + day +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
