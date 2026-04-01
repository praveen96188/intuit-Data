/*
 * Copyright Statement:
 * CONFIDENTIAL -- Copyright 2000-2003 Intuit Inc. This material contains
 * certain trade secrets and confidential and proprietary information
 * of Intuit Inc. Use, reproduction, disclosure and distribution by
 * any means are prohibited, except pursuant to a written license from
 * Intuit Inc. Use of copyright notice is precautionary and does not
 * imply publication or disclosure.
 */
package com.intuit.payroll.agency.dao;



public class LawData {
	private Integer lawID;
	private String description;
    private String lawAbbrev;

	public Integer getLawID()
	{
		return lawID;
	}
	public void setLawID(Integer lawID) 
	{
		this.lawID = lawID;
	}

	public String getDescription()
	{
		return description;
	}
	public void setDescription(String desc)
	{
		this.description = desc;
	}

    public String getLawAbbrev() {
        return lawAbbrev;
    }

    public void setLawAbbrev(String lawAbbrev) {
        this.lawAbbrev = lawAbbrev;
    }
}

