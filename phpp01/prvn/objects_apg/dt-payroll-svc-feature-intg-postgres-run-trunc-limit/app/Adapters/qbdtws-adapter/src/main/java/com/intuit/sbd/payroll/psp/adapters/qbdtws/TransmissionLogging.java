package com.intuit.sbd.payroll.psp.adapters.qbdtws;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;

/**
 * User: rnorian
 * Date: Jan 12, 2010
 * Time: 4:35:14 PM
 */
public class TransmissionLogging {
    private static final SpcfLogger logger = PayrollServices.getLogger(TransmissionLogging.class);

    // todo Ask Mike if we want to record the IP addresses
    public static void recordTransmissionRequest(TransmissionType pTransmissionType, Request request, String pTransmissionId, String pSourceCompanyId, JAXBContext pJAXBContext) throws IOException {
        recordTransmissionRequest(pTransmissionType, 0L, request, pTransmissionId, pSourceCompanyId, pJAXBContext);
    }

    public static void recordTransmissionRequest(TransmissionType pTransmissionType, Long requestToken, Request request, String pTransmissionId, String pSourceCompanyId, JAXBContext pJAXBContext) throws IOException {
        StringWriter requestXML = new StringWriter(2048);

        try {
            Marshaller marshaller = pJAXBContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(request, requestXML);
        } catch (Exception e) {
            logger.warn("failed to marshall request", e);
            requestXML.append("failed to marshall request");
        }

        try {
            SourceSystemTransmissionDTO transmissionDTO = new SourceSystemTransmissionDTO(pTransmissionType, requestXML.toString());
            transmissionDTO.setFromSourceSystem(SourceSystemCode.QBDT);
            transmissionDTO.setToSourceSystem(SourceSystemCode.PSP);
            transmissionDTO.setDescription(pTransmissionType.toString());
            transmissionDTO.setRequestToken(requestToken);
            PayrollServices.transmissionManagerSecondary.beginTransmission(SourceSystemCode.QBDT, pSourceCompanyId, pTransmissionId, transmissionDTO);
        } catch (Exception e) {
            logger.warn("failed to log transmission request", e);
        }
    }

    public static void recordTransmissionResponse(TransmissionType pTransmissionType, String pTransmissionId, String pSourceCompanyId, QBProcessingMessages pResponse, String pTransmissionDescription, JAXBContext pJAXBContext) {
        recordTransmissionResponse(pTransmissionType, pTransmissionId, pSourceCompanyId, 0L, pResponse, pTransmissionDescription, pJAXBContext);
    }

    public static void recordTransmissionResponse(TransmissionType pTransmissionType, String pTransmissionId, String pSourceCompanyId, Long responseToken, QBProcessingMessages pResponse, String pTransmissionDescription, JAXBContext pJAXBContext) {
        if (pJAXBContext == null)
            return;

        StringWriter responseXML = new StringWriter(512);
        try {
            Marshaller marshaller = pJAXBContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(pResponse, responseXML);
        } catch (Exception e) {
            logger.warn("failed to marshall response", e);
            responseXML.append("failed to marshall response");
        }

        try {
            SourceSystemTransmissionDTO transmissionDTO = new SourceSystemTransmissionDTO(pTransmissionType, responseXML.toString());
            transmissionDTO.setFromSourceSystem(SourceSystemCode.QBDT);
            transmissionDTO.setToSourceSystem(SourceSystemCode.PSP);
            if (pTransmissionDescription != null) {
                transmissionDTO.setDescription(pTransmissionDescription);
            }
            transmissionDTO.setResponseToken(responseToken);
            transmissionDTO.setResponseDocument(responseXML.toString());
            transmissionDTO.setToSourceSystem(SourceSystemCode.PSP);
            PayrollServices.transmissionManagerSecondary.endTransmission(SourceSystemCode.QBDT, pSourceCompanyId, pTransmissionId, transmissionDTO);
        } catch (Exception e) {
            logger.warn("failed to log transmission response", e);
        }
    }

}
