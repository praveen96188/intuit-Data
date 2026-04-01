package com.intuit.sbd.payroll.psp.ius;

import com.intuit.client.ius.*;
import com.intuit.iam.utilities.IamConfiguration;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSAppCallback;

import java.util.Objects;

public class IUSInitializer {

    private static IUSAppCallback iusAppCallback;

    public static IUSAppCallback getIusAppCallback() {
        if(Objects.isNull(iusAppCallback)) {
            iusAppCallback = initializeIUSClientAppCallback();
        }

        return iusAppCallback;
    }

    public static IUSAppCallback initializeIUSClientAppCallback() {
        IUSAppCallback appCallback = new IUSAppCallback() {
            @Override
            public Boolean enableHystrix() {
                return false;
            }
        };

        IamConfiguration.setIUSRestUri(appCallback.getIUSRestUri());
        IamConfiguration.setIUSConnectionTimeOut(30000);
        IamConfiguration.setIUSReadTimeOut(30000);
        IamConfiguration.setLogger(appCallback.getLogger());
        IUSRestTransport iusRestTransport = new IUSRestTransportImpl(appCallback);

        IUSUserClient.setTransport(iusRestTransport);
        IUSRealmClient.setTransport(iusRestTransport);
        IUSGrantClient.setTransport(iusRestTransport);

        return appCallback;
    }
}
