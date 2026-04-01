package com.intuit.ems.payroll.psp.gateways.tfs;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/27/12
 * Time: 1:45 PM
 */
public interface ITFSGateway {

    public Map<String, Integer> getW2PageCountsByCompany(int pW2Year);

}
