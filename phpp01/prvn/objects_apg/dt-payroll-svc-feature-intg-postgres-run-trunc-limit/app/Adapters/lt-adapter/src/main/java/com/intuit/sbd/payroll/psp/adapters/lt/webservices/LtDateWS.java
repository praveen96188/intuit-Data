package com.intuit.sbd.payroll.psp.adapters.lt.webservices;


import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;
import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates.*;
import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.GetTransactionDatesRequest;
import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.LtDateRsWSDTO;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.Date;


/**
 * Used to return dates based on the PSP Date suitable for driving load
 */

@WebService
public class LtDateWS {

    /**
     * Retrieves the required dates to run payroll for the specified service
     */
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtDateRsWSDTO getTransactionDates(@WebParam(name = "GetTransactionDatesRequest") GetTransactionDatesRequest dateSpecIn) throws Exception {

        Date dateIn;
        LtDateRsWSDTO responseDTO = new LtDateRsWSDTO();


        //Determine Date
        DateUtilities.DateSpecification dateSpec = dateSpecIn.getDateSpec();
        switch (dateSpec){
            case PSP_DATE:
                dateIn = DateUtilities.getToday();
                break;
            
            case RANDOM:
                dateIn = DateUtilities.getRandomDay();
                //Stuff here
                break;
            case SPECIFIC:
                if (dateSpecIn.getDate() == null){
                    throw new RuntimeException("Provided Date is NULL");
                }
                dateIn = dateSpecIn.getDate();
                break;
            default:
                throw new RuntimeException("Unsupported Date Specification: " + dateSpec);
        }



        //Build Response Based on System Type
        DateUtilities.LtSourceSystemCode code = dateSpecIn.getSourceSystemId();
        switch (code){
            case QBOE:
                responseDTO.setQboeDTO(new QBOEDateDTO(dateIn));
                break;
            case EWS:
                responseDTO.setEwsDTO(new EWSDateDTO(dateIn));
                break;
            case QBDT:
                responseDTO.setQbdtDTO(new QBDTDateDTO(dateIn));
                break;
            case QBDTWS:
                responseDTO.setQbdtwsDTO(new QBDT_WSDateDTO(dateIn));
                break;
            case TESTWS:
                responseDTO.setTestwsDTO(new TestWSDateDTO(dateIn));
                break;
            case ALL_WS:
                responseDTO.setQbdtDTO(new QBDTDateDTO(dateIn));
                responseDTO.setQbdtwsDTO(new QBDT_WSDateDTO(dateIn));
                responseDTO.setTestwsDTO(new TestWSDateDTO(dateIn));
                responseDTO.setEwsDTO(new EWSDateDTO(dateIn));
                responseDTO.setQboeDTO(new QBOEDateDTO(dateIn));
                break;
            default:
                throw new RuntimeException("Unsupported Source System: " + code);

        }

        return responseDTO;
        
    }
}
