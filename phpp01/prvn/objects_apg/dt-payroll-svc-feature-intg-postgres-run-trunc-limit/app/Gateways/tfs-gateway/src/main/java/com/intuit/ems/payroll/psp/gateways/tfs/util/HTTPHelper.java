package com.intuit.ems.payroll.psp.gateways.tfs.util;

import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import static com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader.getOfflineHeaderViaJersey;

/**
 * Created by IntelliJ IDEA.
 * User: vishalb849
 * Date: Dec 12, 2018
 * Time: 12:16:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class HTTPHelper {

    //method to make HTTP GET call for TFS
    public static ClientResponse executeGet(WebResource pWebResource, String pMediaType) {
        return getOfflineHeaderViaJersey(pWebResource, ConfigType.PSP, pMediaType).get(ClientResponse.class);
    }

    //method to make HTTP POST call for TFS
    public static ClientResponse executePost(WebResource pWebResource, String pMediaType, Object pObject) {
        return getOfflineHeaderViaJersey(pWebResource, ConfigType.PSP, pMediaType).post(ClientResponse.class, pObject);
    }

}
