package com.intuit.sbd.payroll.psp.adapters.lt.utilities.services;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Jun 8, 2010
 * Time: 2:55:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class Marshalling {

    public String outputAsString(Object request){
        StringWriter requestXML = new StringWriter(2048);
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(
                    Request.class,
                    QBProcessingMessages.class,
                    ProcessingResponse.class,
                    SubmitEmployeesProcessing.class,
                    SubmitEmployeesRequest.class,
                    UpdateCompanyProcessing.class,
                    UpdateCompanyRequest.class,
                    SubmitPayrollRequest.class);


            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(request, requestXML);
        } catch (Exception e) {
            System.out.println("failed to marshall request" +  e);
        }

        return requestXML.toString();
    }

}
