package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.iep.abcsimpl.commoncomponents.v1.EntitlementIdType;
import com.intuit.iep.abcsimpl.commoncomponents.v1.LicenseIdType;
import com.intuit.iep.customeraccount.customeraccountbase.v1.AccountNumberType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.*;
import com.intuit.iep.intuitcustomerasset.v1.CustomerAssetPort;
import com.intuit.iep.intuitcustomerasset.v1.ESBIntuitCustomerAssetService;
import com.intuit.iep.intuitcustomerasset.v1.GetCustomerAssetFaultMsg;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.utils.GUUID;
import com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.developer.JAXWSProperties;

import javax.ws.rs.core.MediaType;
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
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/11/13
 * Time: 3:00 PM
 */
public class AMOWSGateway implements IAMOWSGateway {

    private String mAMOURLAWS;
    private String mLocalURL;
    private int mRequestTimeout;

    private static SpcfLogger logger = SpcfLogManager.getLogger(AMOWSGateway.class);
    private static ThreadLocal<Marshaller> mMarshaller = new ThreadLocal<Marshaller>();
    private static ThreadLocal<ESBIntuitCustomerAssetService> mService = new ThreadLocal<ESBIntuitCustomerAssetService>();


    private static final String SCHEME_NAME = "ERS";
    private static final String SALES_ORGANIZATION = "PSP";
    private static final String ERROR_MESSAGE = "Failure response received.";
    private static final String SERVICE_NAME = "__soap_IntuitCustomerAsset_CustomerAssetPort";
    private static final String WSDL_NS = "http://www.intuit.com/iep/CustomerAsset/IntuitCustomerAssetABO/V1";
    private static final String WSDL_LOCATION = "resources/IntuitCustomerAsset_V1.wsdl";


    public AMOWSGateway() throws Exception {
        readConfigurationParameters();

        if (mService.get() == null) {
            mService.set(createESBIntuitCustomerAssetService());
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(SyncCustomerAssetDataAreaType.class);
        mMarshaller.set(jaxbContext.createMarshaller());
        mMarshaller.get().setProperty("jaxb.formatted.output", true);
    }

    private void readConfigurationParameters() {
        boolean manageTransaction = !Application.hasActiveTransaction();

        mAMOURLAWS = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_ws_server_awsurl");
        mLocalURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_ws_local_url");
        try {
            if (manageTransaction) {
                Application.beginUnitOfWork();
            }

            mRequestTimeout = SystemParameter.findIntValue(SystemParameter.Code.AMO_WS_REQUEST_TIMEOUT, 10000);
        } finally {
            if (manageTransaction) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    private ESBIntuitCustomerAssetService createESBIntuitCustomerAssetService() throws Exception {
        String URL = mAMOURLAWS + "Wsdl";
        logger.info("URL used for AMOWSGateway "+ URL);
        return getService();
    }

    private ESBIntuitCustomerAssetService getService() throws MalformedURLException {
        ESBIntuitCustomerAssetService service;
        String path =null;
        if(mLocalURL != null && mLocalURL.trim().length() > 0 ) {
            path = mLocalURL + "/" + WSDL_LOCATION;
        } else {
            path = Application.findFileOnClassPath(WSDL_LOCATION);
        }
        File file = new File(path);

        //File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            logger.error(String.format("Cannot find or read WSDL file / Exists: %s, CanRead: %s, File: %s.", file.exists(), file.canRead(), path));
        }

        // try to construct the service from the local copy of the wsdl
        try {
            service = new ESBIntuitCustomerAssetService(new URL("file:///" + path), new QName("http://www.intuit.com/iep/IntuitCustomerAsset/V1", "ESB_IntuitCustomerAsset_Service"));
            logger.info("constructing service from local copy of wsdl");
        } catch (Exception e) {
            logger.error("Unable to connect to the AMO web service. ", e);
            throw e;
        }
        return service;
    }

    public GetCustomerAssetResponseTypeDTO getCustomerAsset(String pLicenseNumber, String pEntitlementOfferingCode, IAMOGatewayListener pListener) throws Exception {
        GetCustomerAssetResponseTypeDTO getCustomerAssetResponseTypeDTO = null;

        String strResponse = null;
        String transmissionId = null;
        try {
            CustomerAssetPort port = mService.get().getSoapIntuitCustomerAssetCustomerAssetPort();
            setPortConfiguration(port);

            GetCustomerAssetDataAreaType request = new GetCustomerAssetDataAreaType();
            request.setTransactionInfo(createTransactionInfoType());
            request.setGetCustomerAsset(createGetCustomerAssetType(null, pLicenseNumber, pEntitlementOfferingCode));

            transmissionId = request.getTransactionInfo().getTransactionId();
            if (pListener != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.get().marshal(request, stringWriter);
                pListener.onRequest(transmissionId, stringWriter.toString());
            }

            GetCustomerAssetResponseDataAreaType response = null;
            try {
                response = port.getCustomerAsset(request);
            } catch (WebServiceException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    logger.warn("AMO WS Gateway timed out. License #: " + pLicenseNumber, e);
                } else if (e instanceof SOAPFaultException) {
                    // transform any soap faults that are caught
                    throw new RuntimeException(transformSOAPFault((SOAPFaultException) e), e);
                } else {
                    // re-throw the exception
                    throw e;
                }
            } catch (GetCustomerAssetFaultMsg f) {
                String errorMessage = "Fault Received.";
                if (f.getFaultInfo() != null) {
                    errorMessage += " Code: " + f.getFaultInfo().getCode();
                    errorMessage += " Description: " + f.getFaultInfo().getDescription();
                }
                strResponse = errorMessage;
                throw new RuntimeException(errorMessage);
            }

            if (response != null) {
                StringWriter stringWriter = new StringWriter();
                mMarshaller.get().marshal(response, stringWriter);
                strResponse = stringWriter.toString();
            } else {
                strResponse = "Null Response";
            }

            if (response != null) {
                GetCustomerAssetResponseType getCustomerAssetResponseType = response.getGetCustomerAssetResponse();
                if (getCustomerAssetResponseType != null) {
                    IntuitCustomerAssetABOType intuitCustomerAssetABOType = getCustomerAssetResponseType.getCustomerAsset();
                    if (intuitCustomerAssetABOType != null) {
                        getCustomerAssetResponseTypeDTO = new GetCustomerAssetResponseTypeDTO(intuitCustomerAssetABOType);
                    }
                }
            }
        } catch (Exception e) {
            if (strResponse == null && e.getMessage() != null && !e.getMessage().contains(ERROR_MESSAGE)) {
                strResponse = e.getMessage();
            }
            throw e;
        } finally {
            if (pListener != null && transmissionId != null) {
                pListener.onResponse(transmissionId, strResponse);
            }
        }

        return getCustomerAssetResponseTypeDTO;
    }

    private void setPortConfiguration(CustomerAssetPort pPort) {
        //adding the PA+ header for calling endpoint behind gateway
        logger.info("Hitting with endpoint behind gateway with PA+");
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDENTITY2_ENABLED_FOR_AMOWS,true)){
            logger.info("AuthN: Identity 2 - executing with endpoint behind AMOWS Gateway");
        ((BindingProvider) pPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mAMOURLAWS);((BindingProvider) pPort).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, OfflineTicketHeader.getAuthNHeader("application/soap+xml", GUUID.getUUID()));
        } else{
            logger.info("AuthN: Identity 1 - executing with endpoint behind AMOWS Gateway");
            ((BindingProvider) pPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mAMOURLAWS);((BindingProvider) pPort).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, new OfflineTicketHeader().getHeader(ConfigType.PSP, "application/soap+xml", GUUID.getUUID()));
        }
        ((BindingProvider) pPort).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) pPort).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) pPort).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) pPort).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, mRequestTimeout);

    }

    private TransactionInfoType createTransactionInfoType() throws Exception {
        TransactionInfoType transactionInfoType = new TransactionInfoType();
        transactionInfoType.setSalesOrganization(SALES_ORGANIZATION);
        transactionInfoType.setTransactionDatetime(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(PSPDate.getPSPTime()));
        transactionInfoType.setTransactionId(UUID.randomUUID().toString());
        return transactionInfoType;
    }

    private GetCustomerAssetType createGetCustomerAssetType(String pCustomerAccountNumber, String pLicenseNumber, String pEntitlementOfferingCode) {
        GetCustomerAssetType getCustomerAssetType = new GetCustomerAssetType();

        if (pCustomerAccountNumber != null) {
            AccountNumberType accountNumberType = new AccountNumberType();
            getCustomerAssetType.setCustomerAccountNumber(accountNumberType);
            accountNumberType.setValue(pCustomerAccountNumber);
        }

        if (pLicenseNumber != null || pEntitlementOfferingCode != null) {
            CustomerAssetRequestType.Asset asset = new CustomerAssetRequestType.Asset();
            getCustomerAssetType.setAsset(asset);
            boolean isWalletEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.NGP_WALLET, false);
            if(isWalletEnabled){
                getCustomerAssetType.setIsWalletEnabled(new Boolean("true"));
            } else {
                getCustomerAssetType.setIsWalletEnabled(new Boolean("false"));
            }

            if (pLicenseNumber != null) {
                LicenseIdType licenseIdType = new LicenseIdType();
                asset.setLicenseId(licenseIdType);
                licenseIdType.setSchemeName(SCHEME_NAME);
                licenseIdType.setValue(pLicenseNumber);
            }

            if (pEntitlementOfferingCode != null) {
                EntitlementIdType entitlementIdType = new EntitlementIdType();
                asset.setEntitlementId(entitlementIdType);
                entitlementIdType.setSchemeName(SCHEME_NAME);
                entitlementIdType.setValue(pEntitlementOfferingCode);
            }
        }


        return getCustomerAssetType;
    }

    private String transformSOAPFault(SOAPFaultException e) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(e.getFault());
        transformer.transform(source, result);

        return result.getWriter().toString();
    }
}
