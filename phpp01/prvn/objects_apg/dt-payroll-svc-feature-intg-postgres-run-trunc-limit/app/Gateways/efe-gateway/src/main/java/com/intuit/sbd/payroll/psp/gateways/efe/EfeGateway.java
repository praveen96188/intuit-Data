package com.intuit.sbd.payroll.psp.gateways.efe;

import com.intuit.idps.domain.item.Key;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileInputStream;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.developer.JAXWSProperties;
import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * User: rnorian
 * Date: 11/11/11
 * Time: 2:34 PM
 */
public class EfeGateway {
    private SpcfLogger logger = SpcfLogManager.getLogger(EfeGateway.class);
    private String mEfeURL = null;
    private int mRequestTimeout = 0;
    private String swimlane=null;
    private String mLocalURL;
    private static final String WSDL_LOCATION = "resources/psp.wsdl";
    private static final String EFE_AUTHORIZATION_HEADER="Authorization";
    private static final String EFE_SWIMLANE_HEADER="intuit_swimlane";
    private static final String EFE_HTTP_REQUEST_HEADER="javax.xml.ws.http.request.headers";


    public static void main(String[] args) {
        File incomingDir = new File("C:\\Users\\rnorian\\Documents\\PSP\\");
        File[] rafFiles = incomingDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return (name.startsWith("rafa") || name.startsWith("rafd")) && name.endsWith(".csv");
            }
        });

        EfeGateway efeGateway = getInstance();
        efeGateway.sendRAFEnrollments(Arrays.asList(rafFiles), "OSP-TEST", "PSP-RAF-ENROLL", "raffi_norian@intuit.com");
    }

    private static EfeGateway instance = null;
    public static EfeGateway getInstance() {
        if (instance == null) {
            // intentionally do not set instance; instance is only used for testing
            return new EfeGateway();
        }
        return instance;
    }

    public static void setInstance(EfeGateway pEfeGateway) {
        instance = pEfeGateway;
    }

    public void sendRAFEnrollmentFile(File pEnrollmentFile, String pSenderId, String pSenderAuthCode, String pSenderEmail) {
        List<File> files = new ArrayList<File>();
        files.add(pEnrollmentFile);
        sendRAFEnrollments(files, pSenderId, pSenderAuthCode, pSenderEmail);
    }

    public void test() {

    }

    /**
     * EFE has requested that we send 1 file at a time.
     */
    private void sendRAFEnrollments(Collection<File> pEnrollmentFiles, String pSenderId, String pSenderAuthCode, String pSenderEmail) {
        EfeFilerService efeFilerService=null;
        List<EfeMessageType> responses;
        List<FilingType> filings = new ArrayList<FilingType>();
        validateFiles(pEnrollmentFiles);
        readConfigurationParameters();

        if (pEnrollmentFiles.size() == 0) {
            logger.warn("EFEGateway.sendRAFEnrollments called with no files to send.  Exiting without send.");
            return;
        }

        for (File rafEnrollmentFile : pEnrollmentFiles) {
            FilingType enrollmentFiling = createFiling(rafEnrollmentFile);
            filings.add(enrollmentFiling);
        }
        String path =null;
        if(mLocalURL != null && mLocalURL.trim().length() > 0 ) {
            path = mLocalURL + "/" + WSDL_LOCATION;
        } else {
            path = Application.findFileOnClassPath(WSDL_LOCATION);
        }
        File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            logger.error(String.format("Cannot find or read WSDL file / Exists: %s, CanRead: %s, File: %s.", file.exists(), file.canRead(), path));
            throw new RuntimeException(file + " not found");
        }

        // try to construct the service from the local copy of the wsdl
        try {
            efeFilerService = new EfeFilerService(new URL("file:///" + path), new QName("http://www.intuit.com/engine/efp/efe/filer/wsdl/v2", "EfeFilerService"));
        } catch (Exception e) {
            logger.error("Unable to connect to the EFE web service. ", e);

        }
        EfeFilerPort efeFilerPort = setRequestContextProperties(efeFilerService.getEfeFilerPort());
        SenderInfoType sender = createSenderInfo(pSenderId, pSenderAuthCode, pSenderEmail, ((File)pEnrollmentFiles.toArray()[0]).getName());
        try {
            responses = efeFilerPort.submit(sender, filings);
        }
        catch (Throwable t) {
            throw new RuntimeException("failure sending RAF Enrollments", t);
        }

        for (EfeMessageType response : responses) {
            logger.info("EFE Response (" + sender.getFilerId() + "): " + response.getStatusMsg().getFilingState().name());
            if (!response.getStatusMsg().getFilingState().equals(FilingStateType.PENDING_EFE)) {
                throw new RuntimeException("RAF file send error - EFE returned filing state: " + response.getStatusMsg().getFilingState().name() + buildErrorString(response.getStatusMsg()));
            }
        }
    }
    private EfeFilerPort setRequestContextProperties(EfeFilerPort efeFilerPort) {
        ((BindingProvider) efeFilerPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mEfeURL);
        ((BindingProvider) efeFilerPort).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) efeFilerPort).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) efeFilerPort).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) efeFilerPort).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) efeFilerPort).getRequestContext().put(EFE_HTTP_REQUEST_HEADER, getSOAPHeader());
        return efeFilerPort;
    }

    private Map<String, List<String>> getSOAPHeader(){
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        String offlineTicket = null;
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_ID2_ENABLED_FOR_EFE, true)) {
            logger.info("Setting authorization ID2 context for EFE gateway");
            offlineTicket = PayrollApplicationBeanFactory.getBean(OfflineTicketClient.class).getOfflineTicket();
        } else {
            logger.info("Setting authorization ID1 context for EFE gateway");
            offlineTicket = OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.EFE);
        }
        logger.info("EFE offlineTicket generated successfully");
        if(!StringUtils.isEmpty(offlineTicket)) {
            headers.put(EFE_AUTHORIZATION_HEADER, Arrays.asList(offlineTicket));
        }
        headers.put(EFE_SWIMLANE_HEADER,Arrays.asList(swimlane));
        return headers;
    }

    private void readConfigurationParameters() {
        boolean manageTransaction = !Application.hasActiveTransaction();
        mEfeURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_efe_url");
        swimlane = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_efe_swimlane");
        mLocalURL =  ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_efe_local_url");
        try {
            if (manageTransaction) {
                Application.beginUnitOfWork();
            }
            mRequestTimeout = SystemParameter.findIntValue(SystemParameter.Code.EFE_REQUEST_TIMEOUT, 10000);
        } finally {
            if (manageTransaction) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    private void validateFiles(Collection<File> pEnrollmentFiles) {
        for (File enrollmentFile : pEnrollmentFiles) {
            if (!enrollmentFile.exists()) {
                throw new RuntimeException("file " + enrollmentFile.getAbsolutePath() + " does not exist");
            }
            if (!enrollmentFile.getName().startsWith("RAFA") && !enrollmentFile.getName().startsWith("RAFD")) {
                throw new RuntimeException("invalid file name -- file name must start with 'RAFA' or 'RAFD'");
            }
        }
    }

    private SenderInfoType createSenderInfo(String pSenderId, String pSenderAuthCode, String pSenderEmail, String pFilerId) {
        ApplicationType application = new ApplicationType();
        application.setName("PSP-RAF");
        application.setVersion("1.0");

        SenderInfoType sender = new SenderInfoType();
        sender.setApp(application);
        sender.setSenderId(pSenderId);

        // use unique FilerID to enable fetching status values later
        sender.setFilerId(pFilerId);

        sender.setLegalName("PSP-RAF-ENROLLMENT");
        sender.setIpAddress("127.0.0.1");
        sender.setTimezone("PST");
        sender.setAuthCode(pSenderAuthCode);
        sender.setEfeSchemaVersion("5.0");
        sender.setSenderEmail(pSenderEmail);
        return sender;
    }

    private String buildErrorString(StatusMessageType pStatusMessageType) {
        StringBuilder responseInfo = new StringBuilder();
        if (pStatusMessageType == null) {
            return "No StatusMessage";
        }

        responseInfo.append("\n\n")
                    .append("EFE Filing State: ").append(pStatusMessageType.getFilingState().name()).append("\n")
                    .append("EFE Message: ").append(pStatusMessageType.getMessage()).append("\n");
        for (MsgDetailsType msgDetailsType : pStatusMessageType.getDetails()) {
            if (msgDetailsType.getErrorDetails() != null) {
                responseInfo.append("Error Code: ").append(msgDetailsType.getErrorDetails().getErrorCode()).append("\n")
                            .append("Problem Msg: ").append(msgDetailsType.getErrorDetails().getProblemMsg()).append("\n")
                            .append("Solution Msg: ").append(msgDetailsType.getErrorDetails().getSolutionMsg()).append("\n");
            }
            for (FormsAndFieldsType formsAndFieldsType : msgDetailsType.getErrorDetails().getFormsAndFields()) {
                responseInfo.append("\tIntuitFieldId: ").append(formsAndFieldsType.getIntuitFieldId()).append("\n");
                responseInfo.append("\tTaxDevType: ").append(formsAndFieldsType.getTaxDevType()).append("\n");
            }
        }
        responseInfo.append("\n\n");

        return responseInfo.toString();
    }

    private FilingType createFiling(File pRafEnrollmentFile) {
        FilingType filingType = new FilingType();
        filingType.setEnrollment(createEnrollment(pRafEnrollmentFile));
        return filingType;
    }

    private EnrollmentType createEnrollment(File pRafEnrollmentFile) {
        EnrollmentType enrollmentType = new EnrollmentType();
        enrollmentType.setAgency(createAgency());
        enrollmentType.setClientFilingId(pRafEnrollmentFile.getName());
        enrollmentType.setFormType(pRafEnrollmentFile.getName().startsWith("RAFA") ? "IRS-RAFADD-FILING" : "IRS-RAFDELETE-FILING");
        enrollmentType.setFormset(createFormset());
        enrollmentType.setPayload(readFile(pRafEnrollmentFile));
        enrollmentType.setCompressed(false);
        enrollmentType.setLinked(false);
        enrollmentType.setRequestsEmailNotification(true);

        return enrollmentType;
    }

    private byte[] readFile(File pFile) {
        if(StreamUtil.isFileIDPSEncrypted(pFile))
        {
            return readSecuredFile(pFile);
        }
        BufferedInputStream inputStream = null;
        byte[] buffer = new byte[0];
        try {
            buffer = new byte[(int) pFile.length()];
            inputStream = new BufferedInputStream(new FileInputStream(pFile));
            inputStream.read(buffer);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to read RAFEnrollmentFile: " + pFile.getName(), ioe);
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); }
                catch (Throwable t) {}
            }
        }

        return buffer;
    }
    private static byte[] readSecuredFile(File pFile) {
        BufferedInputStream inputStream = null;
        byte[] buffer = new byte[0];

        try {
            Key key = IDPSFileStreamManager.newKeyHandleLatest();
            buffer = new byte[(int) pFile.length()];
            inputStream = new BufferedInputStream(new IDPSFileInputStream(pFile,key));
            inputStream.read(buffer);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to read RAFEnrollmentFile: " + pFile.getName(), ioe);
        }catch (Exception ex){

        }
        finally {
            if (inputStream != null) {
                try { inputStream.close(); }
                catch (Throwable t) {}
            }
        }

        return buffer;
    }

    private AgencyType createAgency() {
        AgencyType agency = new AgencyType();
        agency.setName("IRS");
        return agency;
    }

    private FormsetType createFormset() {
        FormsetType formsetType = new FormsetType();
        formsetType.setFormsetId("FSDYS");
        formsetType.setVersion("");
        formsetType.setEngineVersion("");
        return formsetType;
    }
}
