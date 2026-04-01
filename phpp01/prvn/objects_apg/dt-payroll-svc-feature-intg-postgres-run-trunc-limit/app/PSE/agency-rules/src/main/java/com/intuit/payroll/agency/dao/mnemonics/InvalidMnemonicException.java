//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
	/// <summary>
	/// Summary description for InvalidMnemonicException.
	/// </summary>
package com.intuit.payroll.agency.dao.mnemonics;

//import com.intuit.spc.foundations.portability.SpcfRuntimeException;

//public class InvalidMnemonicException extends SpcfRuntimeException
public class InvalidMnemonicException extends RuntimeException
{
    InvalidMnemonicException (String str)
    {
        super(str);
    }

    InvalidMnemonicException (String str, Exception e)
    {
        super(str, e);
    }
}
