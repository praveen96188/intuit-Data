package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils;



import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.PSIMessageWSDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
    @author Jeff Jones
 */
public class JaxBUtils {
    private static SpcfLogger logger;
    private static JAXBContext jaxbContext;

    static {
        logger = PayrollServices.getLogger(JaxBUtils.class);
        try {
            jaxbContext = JAXBContext.newInstance(PSIMessageWSDTO.class);
        } catch (Exception e) {
            logger.fatal(e.getMessage(), e);
        }
    }

    public static String marshall(PSIMessageWSDTO pPSIMessageWSDTO) throws Exception {
        Marshaller marshaller = jaxbContext.createMarshaller();
        OutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(pPSIMessageWSDTO, outputStream);
        return outputStream.toString();

    }

    public static PSIMessageWSDTO Unmarshall(String pXML) throws Exception {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream inputStream = new ByteArrayInputStream(pXML.getBytes());
        return (PSIMessageWSDTO) unmarshaller.unmarshal(inputStream);
    }
}