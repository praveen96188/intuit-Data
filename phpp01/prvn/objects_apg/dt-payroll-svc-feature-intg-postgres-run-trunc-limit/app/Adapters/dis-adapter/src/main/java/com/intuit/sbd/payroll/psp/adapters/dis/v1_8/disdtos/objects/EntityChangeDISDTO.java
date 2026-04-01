package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.domain.EntityChange;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType

public class EntityChangeDISDTO {

    public EntityChangeDISDTO() {
    }

    public EntityChangeDISDTO(EntityChange pEntityChange) {
        if (pEntityChange.getEffectiveDate() != null) {
            Calendar cal = CalendarUtils.convertToCalendar(pEntityChange.getEffectiveDate());
            this.setEffectiveDate(cal);
        }
        this.setNewEin(pEntityChange.getNewEIN());
        this.setOldEin(pEntityChange.getOldEIN());
    }


    @XmlElement
    private String oldEin;

    @XmlElement
    private String newEin;

    @XmlElement
    private Calendar effectiveDate;

    public String getOldEin() {
        return oldEin;
    }

    public void setOldEin(String pOldEin) {
        oldEin = pOldEin;
    }

    public String getNewEin() {
        return newEin;
    }

    public void setNewEin(String pNewEin) {
        newEin = pNewEin;
    }

    public Calendar getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Calendar pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }

}
