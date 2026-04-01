package com.intuit.ems.payroll.psp.gateway.brm;

import com.intuit.iep.serviceusage.intuitserviceusageabo.v1.ErrorType;
import com.intuit.iep.serviceusage.intuitserviceusageabo.v1.QueryUsageBalanceResponseType;
import com.intuit.iep.serviceusage.intuitserviceusageabo.v1.ResponseTypeType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/15/12
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryUsageBalanceResponse {
    private String mSuccess = null;
    private String mErrorCategory = null;
    List<QueryUsageBalanceResponseType.Balance> mBalanceList = null;

    public QueryUsageBalanceResponse(ResponseTypeType pResponseTypeType,List<QueryUsageBalanceResponseType.Balance> pBalanceList){
        if(pResponseTypeType!=null) {
            this.mSuccess =  pResponseTypeType.getSuccess();
            ErrorType et = pResponseTypeType.getError();
            if (et!=null){
                this.mErrorCategory = et.getCategory();
            }
        }
        this.mBalanceList =  pBalanceList;
    }


    public String getSuccess() {
        return mSuccess;
    }

    public String getErrorCategory() {
        return mErrorCategory;
    }

    public List<QueryUsageBalanceResponseType.Balance> getBalanceList() {
        return mBalanceList;
    }

    public double getBalanceCurrencyAmt() throws Exception {
        for (QueryUsageBalanceResponseType.Balance balance : getBalanceList()) {
            if ("Currency".equals(balance.getActivityName())) {
                return Double.parseDouble(balance.getBalance());
            }
        }

        throw new Exception("No currency balance is found");
    }
}
