//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.dao.mnemonics;

//import com.intuit.spc.foundations.portability.collections.*;
//import com.intuit.spc.foundations.portability.SpcfFactory;

import java.util.ArrayList;
/// <summary>
/// This class parses Mnemonics as defined in the Agency Rules Schema.
/// This class implements an Interpreter pattern, mostly.  Could have done a Visitor I guess.
/// Each Expression class knows how to interpret the 3 different
///
/// Unit tests: MnemonicTest.cs
///
/// Pattern: Interpreter
/// </summary>
public class MnemonicInterpreter
{
    private static ArrayList<AbstractMnemonicExpression> s_expressions;
//    private static SpcfArrayList <AbstractMnemonicExpression> s_expressions;

    /// <summary>
    /// Static intializer which builds the expression interpreter stack.
    /// </summary>
    static
    {
//        s_expressions = SpcfFactory.getInstance().<AbstractMnemonicExpression>createArrayList();
        s_expressions = new ArrayList<AbstractMnemonicExpression>();
        s_expressions.add(new StartDateMnemonicExpression());
        s_expressions.add(new EndDateMnemonicExpression());
        s_expressions.add(new DueDateMnemonicExpression());
    }

    /// <summary>
    /// Accepts an InterpreterRequest with values such as MAR-1 or MON and the current date (which provides context).
    /// Returns an InterpreterResponse instance with the resolved dates set.
    /// </summary>
    /// <param name="request">The Mnemonic Dates to parse.</param>
    /// <returns>The resolved Dates</returns>
    /// <exception cref="InvalidMnemonicException">Thrown if the Mnemonic is not supported or malformed</exception>
    public static MnemonicInterpreterResponse parseMnemonicDates(MnemonicInterpreterRequest request)
    {
        // The context stores the input and output for this request, and allows the Interpreters to work with
        // values from prior interpreters.
        MnemonicContext context = new MnemonicContext(request);

        // Loops through the expression interpreters and they each set their results accordingly on the context.
        for (AbstractMnemonicExpression exp: s_expressions)
        {
            exp.interpret(context);
        }

        return context.getOutput();
    }
}
