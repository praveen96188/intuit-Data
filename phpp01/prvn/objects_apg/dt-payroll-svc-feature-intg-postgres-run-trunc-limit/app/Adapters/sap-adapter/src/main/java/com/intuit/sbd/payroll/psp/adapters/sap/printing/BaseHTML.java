package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 2, 2008
 * Time: 11:37:50 AM
 */
public abstract class BaseHTML {
    protected HttpServletRequest mRequest;
    protected HttpServletResponse mResponse;
    protected NumberFormat mNumberFormatter;
    protected NumberFormat mCurrencyFormatter;
    protected SimpleDateFormat mDateFormat;
    protected SimpleDateFormat mTimeDateFormat;
    protected boolean mCanViewFullAccountNumber;

    public BaseHTML(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber){
        mRequest = request;
        mResponse = response;

        mCurrencyFormatter = NumberFormat.getInstance();
        mCurrencyFormatter.setCurrency(Currency.getInstance(Locale.US));

        mNumberFormatter = NumberFormat.getInstance();
        mNumberFormatter.setMinimumFractionDigits(2);
        mNumberFormatter.setMaximumFractionDigits(2);

        mDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        mTimeDateFormat = new SimpleDateFormat("hh:mm aaa MM/dd/yyyy");
        mCanViewFullAccountNumber = pCanViewFullAccountNumber;
    }

    public abstract String getTitle();
    public abstract String getBody() throws Throwable;

    protected String maskBankAccountNumber(String bankAccountNumber){


        // return full number
        if(mCanViewFullAccountNumber){
            return bankAccountNumber;
        }

        // return ***Last 4
        String retString = "";
        if(bankAccountNumber == null)
            return retString;
        int strLen = bankAccountNumber.length();
        if(strLen < 5){
            return bankAccountNumber;
        }
        for(int i = 0; i < (strLen - 4); i++){
            retString += "*";
        }
        retString += bankAccountNumber.substring(strLen - 4, strLen);
        return retString;
    }
}
