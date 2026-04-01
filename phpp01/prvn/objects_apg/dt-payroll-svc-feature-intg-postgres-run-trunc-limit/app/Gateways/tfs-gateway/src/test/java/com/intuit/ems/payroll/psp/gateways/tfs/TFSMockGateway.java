package com.intuit.ems.payroll.psp.gateways.tfs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/27/12
 * Time: 1:09 PM
 */
public class TFSMockGateway implements ITFSGateway{

    private static Map<String, Integer> mW2PageCountsByCompany;

    public static void reset() {
        mW2PageCountsByCompany = null;
    }

    public static void setW2PageCountsByCompany(Map<String, Integer> pW2PageCountsByCompany) {
        mW2PageCountsByCompany = pW2PageCountsByCompany;
    }

    public Map<String, Integer> getW2PageCountsByCompany(int pW2Year) {
        if (mW2PageCountsByCompany == null) {
            mW2PageCountsByCompany = new HashMap<String, Integer>();
        }

        return mW2PageCountsByCompany;
    }

}
