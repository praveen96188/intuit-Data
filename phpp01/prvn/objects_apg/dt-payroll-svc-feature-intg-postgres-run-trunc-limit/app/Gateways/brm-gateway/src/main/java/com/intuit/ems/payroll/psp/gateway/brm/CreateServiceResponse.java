package com.intuit.ems.payroll.psp.gateway.brm;

import com.intuit.iep.serviceusage.intuitserviceusageabo.v1.ErrorType;
import com.intuit.iep.serviceusage.intuitserviceusageabo.v1.ResponseTypeType;

/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/15/12
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateServiceResponse {
    private String mSuccess = null;
    private String mErrorCategory = null;
    private String mErrorCode = null;
    private String mErrorDescription = null;
    private String mErrorSource = null;

    public CreateServiceResponse(ResponseTypeType pResponseTypeType){
        if(pResponseTypeType!=null) {
            this.mSuccess =  pResponseTypeType.getSuccess();
            ErrorType et = pResponseTypeType.getError();
            if (et!=null){
                this.mErrorCategory = et.getCategory();
                this.mErrorCode = et.getCode();
                this.mErrorDescription = et.getDescription();
                this.mErrorSource = et.getSource();
            }
        }
    }

    public String getSuccess() {
        return mSuccess;
    }

    public String getErrorCategory() {
        return mErrorCategory;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public String getErrorSource() {
        return mErrorSource;
    }

    public String getErrorDescription() {
        return mErrorDescription;
    }
}
