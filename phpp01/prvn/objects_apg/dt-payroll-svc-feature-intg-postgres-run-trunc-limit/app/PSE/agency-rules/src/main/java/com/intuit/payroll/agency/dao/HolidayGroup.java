//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction
// is a violation of applicable law. This material contains certain
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

//import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.payroll.agency.util.IAgencyHoliday;

import java.util.ArrayList;


public class HolidayGroup {
    String m_id;
    String m_extendsID;
    ArrayList<IAgencyHoliday> m_holidays= new ArrayList();
//    SpcfArrayList<IAgencyHoliday> m_holidays= SpcfFactory.getInstance().createArrayList();

    String getHolidayGroupID()
    {
        return m_id;
    }
    public void setHolidayGroupID(String id)
    {
        m_id = id;
    }

    String getExtendsHolidayGroupID()
    {
        return m_extendsID;
    }
    public void setExtendsHolidayGroupID(String id)
    {
        m_extendsID = id;
    }

    public void addHoliday(IAgencyHoliday holiday)
    {
        m_holidays.add(holiday);
    }

    ArrayList<IAgencyHoliday> getHolidays()
//    SpcfArrayList<IAgencyHoliday> getHolidays()
    {
        return m_holidays;
    }
}
