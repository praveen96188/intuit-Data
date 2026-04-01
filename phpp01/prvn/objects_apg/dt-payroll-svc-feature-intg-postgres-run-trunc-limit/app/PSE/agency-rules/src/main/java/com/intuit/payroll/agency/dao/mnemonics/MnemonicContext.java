//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.dao.mnemonics;

/// <summary>
/// Simple container object for the input and output of the Mnemonic interpreter.
/// </summary>
class MnemonicContext
{
    private MnemonicInterpreterRequest input;
    private MnemonicInterpreterResponse output;

    public MnemonicContext(MnemonicInterpreterRequest input)
    {
        this.input = input;
        this.output = new MnemonicInterpreterResponse();
    }

    public MnemonicInterpreterRequest getInput()
    {
        return input;
    }

    public void setInput(MnemonicInterpreterRequest that)
    {
        input = that;
    }

    public MnemonicInterpreterResponse getOutput()
    {
        return output;
    }

    public void setOutput(MnemonicInterpreterResponse that)
    {
        output = that;
    }
}
