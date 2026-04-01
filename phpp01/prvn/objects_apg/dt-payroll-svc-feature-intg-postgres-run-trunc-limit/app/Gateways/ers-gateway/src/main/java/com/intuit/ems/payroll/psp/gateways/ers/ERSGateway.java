package com.intuit.ems.payroll.psp.gateways.ers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.developer.JAXWSProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 13, 2010
 * Time: 1:37:58 PM
 */
public class ERSGateway implements IERSGateway {
    private static SpcfLogger logger = PayrollServices.getLogger(ERSGateway.class);


    private static final String WSDL_LOCATION = "resources/IntuitEntitlementReqABCSImpl.wsdl";
    private static final String WSDL_NS = "http://www.intuit.com/iep/entitlement/EntitlementService/wsdl";
    private static final String SERVICE_NAME = "EntitlementService";

    private static final String EDITION = "Edition";
    private static final String NUMBER_OF_EMPLOYEES = "Number_of_Employees,Number of Employees";

    private int mRequestTimeout;
    private int mMaxRetries;
    private String mLocalURL;
    private String mERSURLAWS;
    private String mERSURL;
    private EntitlementServicePortType mPort;
    private Marshaller mMarshaller;
    private static final String ERROR_MESSAGE = "Failure response received.";

    /**
     * ** The marshalling of the requests and responses make the methods in this class not thread safe **
     *
     * @throws Throwable - exception
     */

    public ERSGateway() throws Throwable {
        JAXBContext jaxbContext = JAXBContext.newInstance("com.intuit.ems.payroll.psp.gateways.ers");
        mMarshaller = jaxbContext.createMarshaller();
        mMarshaller.setProperty("jaxb.formatted.output", true);
        readConfigurationParameters();
        EntitlementService service = null;
        service= getService();
        mPort = service.getEntitlementServicePort();

        ((BindingProvider) getSOAPPort()).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mERSURL);
        ((BindingProvider) getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) getSOAPPort()).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) getSOAPPort()).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) getSOAPPort()).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, mRequestTimeout);

        //adding the PA+ header for calling endpoint behind gateway
        logger.info("executing with endpoint behind gateway with PA+");
        logger.info("URL used for ERSGateway "+ mERSURLAWS);
        ((BindingProvider) getSOAPPort()).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mERSURLAWS);
    }

    //method to get the service locally
    private EntitlementService getService() throws MalformedURLException {
        EntitlementService service;
        File file = Application.findFileObjectOnClassPath(WSDL_LOCATION);
        String path = file.getAbsolutePath();
        //File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            logger.error(String.format("Cannot find or read WSDL file / Exists: %s, CanRead: %s, File: %s.", file.exists(), file.canRead(), path));
        }

        // try to construct the service from the local copy of the wsdl
        try {
            service = new EntitlementService(new URL("file:///"+path), new QName(WSDL_NS, SERVICE_NAME));
            logger.info("constructing service from local copy of wsdl");
        } catch (Exception e) {
            logger.error("Unable to connect to the ERS web service. ", e);
            throw e;
        }
        return service;
    }

    private void readConfigurationParameters() {
        boolean manageTransaction = !Application.hasActiveTransaction();

        mERSURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_ers_server_url");
        mERSURLAWS = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_ers_server_awsurl");
        mLocalURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_ers_local_url");
        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            mRequestTimeout = SystemParameter.findIntValue(SystemParameter.Code.ERS_REQUEST_TIMEOUT, 10000);
            mMaxRetries = SystemParameter.findIntValue(SystemParameter.Code.ERS_MAX_RETRIES, 5);
        }
        finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public EntitlementServicePortType getSOAPPort() {
        return mPort;
    }

    public void activateEntitlement(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, boolean pIsReactivation, IERSGatewayListener pListener) throws Throwable {
        int timeoutCount = 0;
        boolean connected = false;
        String response = null;
        String transmissionId = null;
        try {
            setOfflineTicketHeader();
            ActivateEntitlementRequest activateEntitlementRequest = WrapperFactory.generateActivateEntitlementRequest(pLicenseNumber, pEntitlementOfferingCode, pEIN, pIsReactivation);
            transmissionId = activateEntitlementRequest.getTransactionInfo().transactionId;
            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(activateEntitlementRequest, stringWriter);
                pListener.onRequest(transmissionId, stringWriter.toString());
            }

            ActivateEntitlementResponse activateEntitlementResponse = null;
            while (!connected) {
                try {
                    activateEntitlementResponse = getSOAPPort().activateEntitlement(activateEntitlementRequest);
                    logger.info(String.format("AuthN Status=%s", activateEntitlementResponse.getStatus()));
                } catch (WebServiceException e) {
                    if(e.getCause() instanceof SocketTimeoutException) {
                        logger.info("ERS Gateway timed out. License #: " + pLicenseNumber + " EOC: " + pEntitlementOfferingCode + " CustomerId: " +activateEntitlementRequest.customerId);
                        timeoutCount++;
                        if(timeoutCount >= mMaxRetries) {
                            // throw an exception if there is no response from the server
                            throw new ERSConnectionException("ERS connection timed out more than " + mMaxRetries + " times.", e);
                        }
                    } else if(e instanceof SOAPFaultException) {
                        // transform any soap faults that are caught
                        response = transformSOAPFault((SOAPFaultException)e);
                        throw new RuntimeException(response, e);
                    } else {
                        // re-throw the exception
                        throw e;
                    }
                } catch (Fault f) {
                    String errorMessage = "Fault Received.";
                    if(f.getFaultInfo() != null) {
                        errorMessage += " Code: " + f.getFaultInfo().getCode();
                        errorMessage += " Description: " + f.getFaultInfo().getDescription();
                    }
                    response = errorMessage;
                    throw new RuntimeException(errorMessage);
                }

                if(activateEntitlementResponse != null) {
                    connected = true;
                }
            }

            if(activateEntitlementResponse == null) {
                throw new ERSConnectionException("Activate response was null.");
            }

            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(activateEntitlementResponse, stringWriter);
                response = stringWriter.toString();
            }

            handleResponseStatus(activateEntitlementResponse.getStatus(), activateEntitlementResponse.getError());

        } catch (Throwable t) {
            if(response == null && t.getMessage() != null && t.getMessage().indexOf(ERROR_MESSAGE) < 0) {
                response = t.getMessage();
            }
            throw t;
        } finally {
            if(pListener != null && transmissionId != null) {
                pListener.onResponse(transmissionId, response);
            }
        }
    }

    private void setOfflineTicketHeader() {
        String intuitTid = OfflineTicketHeader.generateTid();
        logger.info("executing with endpoint behind gateway with PA+ intuit_tid="+intuitTid);
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDENTITY2_ENABLED_FOR_ERS,true)){
            logger.info("AuthN: Identity2.0");
            ((BindingProvider) getSOAPPort()).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, OfflineTicketHeader.getAuthNHeader("application/soap+xml", intuitTid));
            logger.info("AuthN: Identity2 Header Received");
        }else {
            logger.info("AuthN: Identity1.0");
            ((BindingProvider) getSOAPPort()).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, OfflineTicketHeader.getHeader(ConfigType.PSP, "application/soap+xml", intuitTid));
            logger.info("AuthN: Identity1 Header Received");
        }
    }

    public void deactivateEntitlementUnit(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, IERSGatewayListener pListener) throws Throwable {
        int timeoutCount = 0;
        boolean connected = false;
        String response = null;
        String transmissionId = null;
        try {
            setOfflineTicketHeader();
            DeactivateEntitlementUnitRequest deactivateEntitlementUnitRequest = WrapperFactory.generateDeactivateEntitlementUnitRequest(pLicenseNumber, pEntitlementOfferingCode, pEIN);
            transmissionId = deactivateEntitlementUnitRequest.getTransactionInfo().transactionId;
            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(deactivateEntitlementUnitRequest, stringWriter);
                pListener.onRequest(transmissionId, stringWriter.toString());
            }

            DeactivateEntitlementUnitResponse deactivateEntitlementUnitResponse = null;
            while (!connected) {
                try {
                    deactivateEntitlementUnitResponse = getSOAPPort().deactivateEntitlementUnit(deactivateEntitlementUnitRequest);
                } catch (WebServiceException e) {
                    if(e.getCause() instanceof SocketTimeoutException) {
                        logger.info("ERS Gateway timed out. License #: " + pLicenseNumber + " EOC: " + pEntitlementOfferingCode );
                        timeoutCount++;
                        if(timeoutCount >= mMaxRetries) {
                            // throw an exception if there is no response from the server
                            throw new ERSConnectionException("ERS connection timed out more than " + mMaxRetries + " times.", e);
                        }
                    } else if(e instanceof SOAPFaultException) {
                        // transform any soap faults that are caught
                        response = transformSOAPFault((SOAPFaultException)e);
                        throw new RuntimeException(e);
                    } else{
                        // re-throw the exception
                        throw e;
                    }
                } catch (Fault f) {
                    String errorMessage = "Fault Received.";
                    if(f.getFaultInfo() != null) {
                        errorMessage += " Code: " + f.getFaultInfo().getCode();
                        errorMessage += " Description: " + f.getFaultInfo().getDescription();
                    }
                    response = errorMessage;
                    throw new RuntimeException(errorMessage);
                }

                if(deactivateEntitlementUnitResponse != null) {
                    connected = true;
                }
            }

            if(deactivateEntitlementUnitResponse == null) {
                throw new ERSConnectionException("Deactivate response was null.");
            }

            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(deactivateEntitlementUnitResponse, stringWriter);
                response = stringWriter.toString();
            }

            handleResponseStatus(deactivateEntitlementUnitResponse.getStatus(), deactivateEntitlementUnitResponse.getError());
        } catch (Throwable t) {
            if(response == null && t.getMessage() != null && t.getMessage().indexOf(ERROR_MESSAGE) < 0) {
                response = t.getMessage();
            }
            throw t;
        } finally {
            if(pListener != null && transmissionId != null) {
                pListener.onResponse(transmissionId, response);
            }
        }
    }

    public void disableEntitlement(String pLicenseNumber, String pEOC, IERSGatewayListener pListener) throws Throwable {
        int timeoutCount = 0;
        boolean connected = false;
        String response = null;
        String transmissionId = null;
        try {
            setOfflineTicketHeader();
            CancelEntitlementsRequest cancelEntitlementsRequest = WrapperFactory.generateCancelEntitlementRequest(pLicenseNumber, pEOC);
            transmissionId = cancelEntitlementsRequest.getTransactionInfo().getTransactionId();
            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(cancelEntitlementsRequest, stringWriter);
                pListener.onRequest(transmissionId, stringWriter.toString());
            }

            CancelEntitlementsResponse cancelEntitlementsResponse = null;
            while (!connected) {

                try {
                    cancelEntitlementsResponse = getSOAPPort().cancelEntitlements(cancelEntitlementsRequest);
                } catch (WebServiceException e) {
                    if(e.getCause() instanceof SocketTimeoutException) {
                        logger.info("ERS Gateway timed out. License #: " + pLicenseNumber);
                        timeoutCount++;
                        if(timeoutCount >= mMaxRetries) {
                            // throw an exception if there is no response from the server
                            throw new ERSConnectionException("ERS connection timed out more than " + mMaxRetries + " times.", e);
                        }
                    } else if(e instanceof SOAPFaultException) {
                        // transform any soap faults that are caught
                        throw new RuntimeException(transformSOAPFault((SOAPFaultException)e), e);
                    } else {
                        // re-throw the exception
                        throw e;
                    }
                } catch (Fault f) {
                    String errorMessage = "Fault Received.";
                    if(f.getFaultInfo() != null) {
                        errorMessage += " Code: " + f.getFaultInfo().getCode();
                        errorMessage += " Description: " + f.getFaultInfo().getDescription();
                    }
                    response = errorMessage;
                    throw new RuntimeException(errorMessage);
                }

                if(cancelEntitlementsResponse != null) {
                    connected = true;
                }
            }

            if(cancelEntitlementsResponse == null) {
                throw new ERSConnectionException("Disable response was null.");
            }

            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(cancelEntitlementsResponse, stringWriter);
                response = stringWriter.toString();
            }

            handleResponseStatus(cancelEntitlementsResponse.getStatus(), cancelEntitlementsResponse.getError());
        } catch (Throwable t) {
            if(response == null && t.getMessage() != null && t.getMessage().indexOf(ERROR_MESSAGE) < 0) {
                response = t.getMessage();
            }
            throw t;
        } finally {
            if(pListener != null && transmissionId != null) {
                pListener.onResponse(transmissionId, response);
            }
        }
    }

    public EntitlementInfoDTO getEntitlementInfo(String pLicenseNumber, String pEOC, boolean pIncludeDisabled, IERSGatewayListener pListener) throws Throwable {
        int timeoutCount = 0;
        boolean connected = false;
        String response = null;
        String transmissionId = null;

        Map<String, EditionType> stringToEditionType = EntitlementDTO.getEditionValues();
        Map<String, NumberOfEmployeesType> stringToNumberOfEmployeesType = EntitlementDTO.getNumberOfEmployeesValues();

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        try {
            setOfflineTicketHeader();
            GetEntitlementInformationAndPropertyDetailsRequest getEntitlementInformationAndPropertyDetailsRequest = WrapperFactory.generateGetEntitlementInformationAndPropertyDetailsRequest(pLicenseNumber, pEOC, pIncludeDisabled);
            transmissionId = getEntitlementInformationAndPropertyDetailsRequest.getTransactionInfo().getTransactionId();
            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(getEntitlementInformationAndPropertyDetailsRequest, stringWriter);
                pListener.onRequest(transmissionId, stringWriter.toString());
            }

            GetEntitlementInformationAndPropertyDetailsResponse getEntitlementInformationAndPropertyDetailsResponse = null;
            while (!connected) {

                try {
                    getEntitlementInformationAndPropertyDetailsResponse = getSOAPPort().getEntitlementInformationAndPropertyDetails(getEntitlementInformationAndPropertyDetailsRequest);
                } catch (WebServiceException e) {
                    if(e.getCause() instanceof SocketTimeoutException) {
                        logger.info("ERS Gateway timed out. License #: " + pLicenseNumber);
                        timeoutCount++;
                        if(timeoutCount >= mMaxRetries) {
                            // throw an exception if there is no response from the server
                            throw new ERSConnectionException("ERS connection timed out more than " + mMaxRetries + " times.", e);
                        }
                    } else if(e instanceof SOAPFaultException) {
                        // transform any soap faults that are caught
                        throw new RuntimeException(transformSOAPFault((SOAPFaultException)e), e);
                    } else {
                        // re-throw the exception
                        throw e;
                    }
                } catch (Fault f) {
                    String errorMessage = "Fault Received.";
                    if(f.getFaultInfo() != null) {
                        errorMessage += " Code: " + f.getFaultInfo().getCode();
                        errorMessage += " Description: " + f.getFaultInfo().getDescription();
                    }
                    response = errorMessage;
                    throw new RuntimeException(errorMessage);
                }

                if(getEntitlementInformationAndPropertyDetailsResponse != null) {
                    connected = true;
                }
            }

            if(getEntitlementInformationAndPropertyDetailsResponse == null) {
                throw new ERSConnectionException("Entitlement detail response was null.");
            }

            if(pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(getEntitlementInformationAndPropertyDetailsResponse, stringWriter);
                response = stringWriter.toString();
            }

            handleResponseStatus(getEntitlementInformationAndPropertyDetailsResponse.getStatus(), getEntitlementInformationAndPropertyDetailsResponse.getError());

            if(getEntitlementInformationAndPropertyDetailsResponse.getLicenseInfo() != null) {
                entitlementInfoDTO.setCustomerId(getEntitlementInformationAndPropertyDetailsResponse.getLicenseInfo().getAccountId().getValue());
                for (GetEntitlementInformationAndPropertyDetailsResponse.LicenseInfo.CoreEntitlement coreEntitlement : getEntitlementInformationAndPropertyDetailsResponse.getLicenseInfo().getCoreEntitlement()) {
                    for (GetEntitlementInformationAndPropertyDetailsResponse.LicenseInfo.CoreEntitlement.Entitlement entitlement : coreEntitlement.getEntitlement()) {
                        if(entitlement.getOfferingConfiguration() != null &&
                                entitlement.getEntitlementOffering() != null &&
                                pEOC.equals(entitlement.getEntitlementOffering().getEntitlementOfferingCode())) {
                            for (EntitlementAttributeType entitlementAttributeType : entitlement.getOfferingConfiguration().getTransactionAttribute()) {
                                if(EDITION.equals(entitlementAttributeType.getName())) {
                                    entitlementInfoDTO.setEditionType(stringToEditionType.get(entitlementAttributeType.getValue()));
                                } else if(NUMBER_OF_EMPLOYEES.contains(entitlementAttributeType.getName())) {
                                    entitlementInfoDTO.setNumberOfEmployeesType(stringToNumberOfEmployeesType.get(entitlementAttributeType.getValue()));
                                }
                            }
                            entitlementInfoDTO.setAssetItemNumber(entitlement.getOfferingConfiguration().itemNumber);
                            if (entitlement.getEntitlementState() != null ) {
                                entitlementInfoDTO.setEntitlementState(EntitlementStateType.ENABLED.equals(entitlement.getEntitlementState()) ? EntitlementStateCode.Enabled : EntitlementStateCode.Disabled);
                            }
                        }
                    }
                    for (GetEntitlementInformationAndPropertyDetailsResponse.LicenseInfo.CoreEntitlement.EntitlementUnit entitlementUnit : coreEntitlement.getEntitlementUnit()) {
                        if (pEOC.equals(entitlementUnit.getEntitlementOfferingCode())) {
                            EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                            String fedTaxId = entitlementUnit.identifiedResourceValue;
                            entitlementUnitInfoDTO.setFedTaxId(fedTaxId);
                            entitlementInfoDTO.getEntitlementUnits().put(fedTaxId, entitlementUnitInfoDTO);
                            if (entitlementUnit.getUnitCurrentState() != null) {
                                entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStateType.ACTIVATED.equals(entitlementUnit.getUnitCurrentState()) ? EntitlementUnitStatusCode.Activated : EntitlementUnitStatusCode.Deactivated);
                            }
                        }
                    }
                }
            }

        } catch (Throwable t) {
            if(response == null && t.getMessage() != null && t.getMessage().indexOf(ERROR_MESSAGE) < 0) {
                response = t.getMessage();
            }
            throw t;
        } finally {
            if(pListener != null && transmissionId != null) {
                pListener.onResponse(transmissionId, response);
            }
        }
        return entitlementInfoDTO;
    }

    private void handleResponseStatus(ResponseStatusType pResponseStatusType, ErrorType pErrorType) throws Throwable {
        switch(pResponseStatusType) {
            case SUCCESS:
                break;
            case FAILURE:
            default:
                if(pErrorType != null) {
                    String errorMessage = ERROR_MESSAGE + "\nError Code: " + pErrorType.getCode() +
                            "\nError Category: " + pErrorType.getCategory() +
                            "\nError Description: " + pErrorType.getDescription();
                    if(pErrorType.getCategory().equals("SystemicError")){
                        throw new ERSConnectionException(errorMessage);
                    } else {
                        throw new RuntimeException(errorMessage);
                    }
                } else {
                    throw new RuntimeException("ERS call failed without an error message.");
                }
        }
    }

    private String transformSOAPFault(SOAPFaultException e) throws Throwable {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(e.getFault());
        transformer.transform(source, result);

        return result.getWriter().toString();
    }
}
