/**
 * --------------------------------------------------------------------------
 * Copyright (c) 2008 Intuit, Inc. All rights reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * --------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------
 *
 * Author	PSP CodeGen
 * Model Version	$modelVersion$
 *
 * --------------------------------------------------------------------------
 */
 package com.intuit.sbd.payroll.psp.domain;

 import com.intuit.sbd.payroll.psp.hibernate.SpcfStringEnumUserType;

 /**
 * Hibernate UserType for $enumerationName$
 */

 public class $enumerationName$UserType extends SpcfStringEnumUserType{
	/**
	 * Default constructor.
	 */ 
	public $enumerationName$UserType()
	{
		setEnumClassName("com.intuit.sbd.payroll.psp.domain.$enumerationName$");
	}
 }