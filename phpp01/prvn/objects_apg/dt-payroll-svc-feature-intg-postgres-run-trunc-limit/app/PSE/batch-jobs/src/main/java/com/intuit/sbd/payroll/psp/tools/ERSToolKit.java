package com.intuit.sbd.payroll.psp.tools;

import com.intuit.ems.payroll.psp.gateways.ers.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.developer.JAXWSProperties;
import org.hibernate.*;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 8/2/12
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class ERSToolKit {

    private static FileWriter mFileWriter;
    private static int mInterval;
    private static int mMinPoolSize;
    private static int mMaxPoolSize;
    private static int mMaxWait;
    private static int mBatchSize;
    private static int mRequestTimeout;
    private static int mMaxRetries;
    private static String mERSURL;
    private static String mLocalURL;
    private static String mOutputDirectory;
    private static ExecutorService threadPool;
    private static CompletionService<List<ERSToolKitError>> completionService;

    private static final String SPACE = " ";
    private static final String QUOTE = "\"";
    private static final String FILE_EOL = "\n";
    private static final String DELIMITER = ",";
    private static final String FILE_EXT = ".csv";
    private static final String FILE_PREFIX = "ERSToolkit_Errors_";

    private static final String WSDL_LOCATION = "resources/IntuitEntitlementReqABCSImpl.wsdl";
    private static final String WSDL_NS = "http://www.intuit.com/iep/entitlement/EntitlementService/wsdl";
    private static final String SERVICE_NAME = "EntitlementService";

    private static final String EDITION = "Edition";
    private static final String NUMBER_OF_EMPLOYEES = "Number_of_Employees,Number of Employees";

    private static SpcfLogger logger = Application.getLogger(ERSToolKit.class);
    private static ThreadLocal<EntitlementService> entitlementService = new ThreadLocal<EntitlementService>();
    private static final String ERROR_MESSAGE = "Failure response received.";

    private static int entitlementCount = 0;

    static {
        readConfigurationParameters();
    }

    public static void main(String[] args) {
        StopWatch stopWatch = null;
        try {
            stopWatch = StopWatch.startTimer();

            ToolkitCommand command;
            String licenseNumber = null;
            String entitlementOfferingCode = null;

            if (args.length == 1) {
                //Process command across all companies
                command = ToolkitCommand.valueOf(args[0]);
            } else if (args.length == 3) {
                //Process command for specified entitlement
                command = ToolkitCommand.valueOf(args[0]);
                licenseNumber = args[1];
                entitlementOfferingCode = args[2];
            } else {
                throw new Exception("usage");
            }

            createFileAndWriteHeaders();

            Application.initialize();
            ApplicationSecondary.initialize();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ERSToolkit));
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            List<SpcfUniqueId> entitlements = new ArrayList<SpcfUniqueId>();
            if (ToolkitCommand.Validate.equals(command)) {
                if (licenseNumber != null && entitlementOfferingCode != null) {
                    Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
                    entitlements.add(entitlement.getId());
                    if (entitlement != null) {
                        multithreadProcessing(entitlements);
                    } else {
                        logger.warn("0 entitlements found to compare.");
                    }
                } else {
                    ScrollableResults scrollableResults = getBatchedEntitlements();

                    while (scrollableResults.next()) {
                        entitlements.add((SpcfUniqueId) scrollableResults.get(0));
                        if (entitlements.size() >= mBatchSize) {
                            multithreadProcessing(entitlements);
                            entitlements.clear();
                        }
                    }

                    //Finish any items left in the queue
                    if (entitlements.size() > 0) {
                        multithreadProcessing(entitlements);
                        entitlements.clear();
                    }
                }
            }
        } catch (Throwable t) {
            if (t.getMessage() != null && t.getMessage().equals("usage")) {
                usage();
            } else {
                t.printStackTrace();
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();

            stopWatch.stop();
            logger.info("Elapsed Time: " + stopWatch.getElapsedTimeString());

            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
            }

            try {
                if (mFileWriter != null) {
                    mFileWriter.flush();
                    mFileWriter.close();
                }
            } catch (Exception e) {
                logger.warn(e);
            }
        }
    }

    private static void multithreadProcessing(List<SpcfUniqueId> pEntitlementIds) throws Exception {
        try {
            if (threadPool == null) {
                threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                completionService = new ExecutorCompletionService<List<ERSToolKitError>>(threadPool);
            }

            for (final SpcfUniqueId entitlementId : pEntitlementIds) {
                completionService.submit(new Callable<List<ERSToolKitError>>() {
                    public List<ERSToolKitError> call() throws Exception {
                        return compareData(entitlementId);
                    }
                });
            }

            try {
                for (int t = 0; t < pEntitlementIds.size() ; t++) {
                    entitlementCount++;
                    Future<List<ERSToolKitError>> errorList = completionService.take();
                    logErrors(errorList.get());
                    if (entitlementCount % 100 == 0) {
                        logger.info("working -- completed processing " + entitlementCount + " entitlements");
                    }
                }
            } catch (InterruptedException e) {
                logger.warn(e);
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    private static EntitlementService createEntitlementService() throws Exception {
        EntitlementService service;
        try {
            service = new EntitlementService(
                    new URL(mERSURL + "?wsdl"),
                    new QName(WSDL_NS, SERVICE_NAME));
        } catch (Throwable t) {
            String path;
            if(mLocalURL != null && mLocalURL.trim().length() > 0 ) {
                path = "file:///" + mLocalURL + "/" + WSDL_LOCATION;
            } else {
                path = "file:///" + Application.findFileOnClassPath(WSDL_LOCATION);
            }

            File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                logger.error(String.format("Cannot find or read WSDL file / Exists: %s, CanRead: %s, File: %s.", file.exists(), file.canRead(), path));
            }

            // try to construct the service from the local copy of the wsdl
            try {
                service = new EntitlementService(new URL(path), new QName(WSDL_NS, SERVICE_NAME));
            } catch (Exception e) {
                logger.error("Unable to connect to the ERS web service. ", e);
                throw e;
            }
        }
        return service;
    }

    private static void logErrors(List<ERSToolKitError> pErrorList) throws Exception {
        for (ERSToolKitError ersToolKitError : pErrorList) {
            if (ersToolKitError.getMessage() != null) {
                ersToolKitError.setMessage(ersToolKitError.getMessage().replace(FILE_EOL, SPACE));
            }

            writeData(mFileWriter, QUOTE + (ersToolKitError.getLicenseNumber() == null ? "" : ersToolKitError.getLicenseNumber()) + QUOTE);
            writeData(mFileWriter, DELIMITER);
            writeData(mFileWriter, QUOTE + (ersToolKitError.getEntitlementOfferingCode() == null ? "" : ersToolKitError.getEntitlementOfferingCode()) + QUOTE);
            writeData(mFileWriter, DELIMITER);
            writeData(mFileWriter, QUOTE + (ersToolKitError.getFedTaxId() == null ? "" : ersToolKitError.getFedTaxId()) + QUOTE);
            writeData(mFileWriter, DELIMITER);
            writeData(mFileWriter, QUOTE + (ersToolKitError.getEntitlementState() == null ? "" : ersToolKitError.getEntitlementState()) + QUOTE);
            writeData(mFileWriter, DELIMITER);
            writeData(mFileWriter, QUOTE + (ersToolKitError.getEntitlementUnitStatus() == null ? "" : ersToolKitError.getEntitlementUnitStatus()) + QUOTE);
            writeData(mFileWriter, DELIMITER);
            writeData(mFileWriter, QUOTE + (ersToolKitError.getErsEntitlementState() == null ? "" : ersToolKitError.getErsEntitlementState()) + QUOTE);
            writeData(mFileWriter, DELIMITER);
            writeData(mFileWriter, QUOTE + (ersToolKitError.getErsEntitlementUnitStatus() == null ? "" : ersToolKitError.getErsEntitlementUnitStatus()) + QUOTE);
            writeData(mFileWriter, DELIMITER);
            writeData(mFileWriter, QUOTE + (ersToolKitError.getMessage() == null ? "" : ersToolKitError.getMessage()) + QUOTE);
            writeData(mFileWriter, FILE_EOL);
        }
    }

    private static void createFileAndWriteHeaders() throws Exception {
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        SpcfCalendar systemTime = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        String fileName = mOutputDirectory + File.separator + FILE_PREFIX +
                StringFormatter.formatDate(pspDate, "yyyyMMdd") + "_" +
                StringFormatter.formatDate(systemTime, "HHmmss") + FILE_EXT;
        mFileWriter = new FileWriter(fileName);

        writeData(mFileWriter, "\"License Number\"");
        writeData(mFileWriter, DELIMITER);
        writeData(mFileWriter, "\"Entitlement Offering Code\"");
        writeData(mFileWriter, DELIMITER);
        writeData(mFileWriter, "\"FedTaxId\"");
        writeData(mFileWriter, DELIMITER);
        writeData(mFileWriter, "\"PSP Entitlement State\"");
        writeData(mFileWriter, DELIMITER);
        writeData(mFileWriter, "\"PSP Entitlement Unit Status\"");
        writeData(mFileWriter, DELIMITER);
        writeData(mFileWriter, "\"ERS Entitlement State\"");
        writeData(mFileWriter, DELIMITER);
        writeData(mFileWriter, "\"ERS Entitlement Unit Status\"");
        writeData(mFileWriter, DELIMITER);
        writeData(mFileWriter, "\"Message\"");
        writeData(mFileWriter, FILE_EOL);
    }

    private static void writeData(FileWriter pFileWriter, String pData) throws IOException {
        if (pData == null) {
            pData = "";
        }
        pFileWriter.write(pData);
    }

    public static List<ERSToolKitError> compareData(SpcfUniqueId pEntitlementId) throws Exception {
        return compareData(pEntitlementId, null);
    }

    public static List<ERSToolKitError> compareData(SpcfUniqueId pEntitlementId, EntitlementInfoDTO pEntitlementInfoDTO) throws Exception {
        String message;
        ERSToolKitError error;
        List<ERSToolKitError> errorList = new ArrayList<ERSToolKitError>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Entitlement entitlement = getEntitlementById(pEntitlementId);

            EntitlementInfoDTO entitlementInfoDTO;
            if (pEntitlementInfoDTO == null) {
                entitlementInfoDTO = getEntitlementInfoDTO(entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
            } else {
                entitlementInfoDTO = pEntitlementInfoDTO;
            }

            if (entitlementInfoDTO == null) {
                message = "The License Number and Entitlement Offering Code in PSP was not found in ERS";
                error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                            entitlement.getEntitlementOfferingCode(),
                                            null,
                                            message,
                                            entitlement.getEntitlementState(),
                                            null,
                                            null,
                                            null);
                errorList.add(error);
                return errorList;
            }

            // Compare Entitlement Data
            if ((entitlement.getCustomerId() == null && entitlementInfoDTO.getCustomerId() != null) ||
                    (!entitlement.getCustomerId().equals(entitlementInfoDTO.getCustomerId()))) {
                message = String.format("The Customer Id in PSP (%s) does not match the value in ERS (%s)", entitlement.getCustomerId(), entitlementInfoDTO.getCustomerId()) ;
                error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                            entitlement.getEntitlementOfferingCode(),
                                            null,
                                            message,
                                            entitlement.getEntitlementState(),
                                            null,
                                            entitlementInfoDTO.getEntitlementState(),
                                            null);
                errorList.add(error);
            }
            if (!entitlement.getEntitlementState().equals(entitlementInfoDTO.getEntitlementState())) {
                message = String.format("The Entitlement State in PSP (%s) does not match the value in ERS (%s)", entitlement.getEntitlementState(), entitlementInfoDTO.getEntitlementState()) ;
                error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                            entitlement.getEntitlementOfferingCode(),
                                            null,
                                            message,
                                            entitlement.getEntitlementState(),
                                            null,
                                            entitlementInfoDTO.getEntitlementState(),
                                            null);
                errorList.add(error);
            }

            // Compare EntitlementCode Data
            EntitlementCode entitlementCode = entitlement.getEntitlementCode();
            if (!entitlementCode.getAssetItemNumber().equals(entitlementInfoDTO.getAssetItemNumber())) {
                message = String.format("The Asset Item Number in PSP (%s) does not match the value in ERS (%s)", entitlementCode.getAssetItemNumber(), entitlementInfoDTO.getAssetItemNumber()) ;
                error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                            entitlement.getEntitlementOfferingCode(),
                                            null,
                                            message,
                                            entitlement.getEntitlementState(),
                                            null,
                                            entitlementInfoDTO.getEntitlementState(),
                                            null);
                errorList.add(error);
            }

            if (entitlementCode.getEditionType() != null && !entitlementCode.getEditionType().equals(entitlementInfoDTO.getEditionType())) {
                message = String.format("The Edition in PSP (%s) does not match the value in ERS (%s)", entitlementCode.getEditionType(), entitlementInfoDTO.getEditionType()) ;
                error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                            entitlement.getEntitlementOfferingCode(),
                                            null,
                                            message,
                                            entitlement.getEntitlementState(),
                                            null,
                                            entitlementInfoDTO.getEntitlementState(),
                                            null);
                errorList.add(error);
            }
            if (entitlementCode.getNumberOfEmployeesType() != null && !entitlementCode.getNumberOfEmployeesType().equals(entitlementInfoDTO.getNumberOfEmployeesType())) {
                message = String.format("The Number of Employees in PSP (%s) does not match the value in ERS (%s)", entitlementCode.getNumberOfEmployeesType(), entitlementInfoDTO.getNumberOfEmployeesType()) ;
                error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                            entitlement.getEntitlementOfferingCode(),
                                            null,
                                            message,
                                            entitlement.getEntitlementState(),
                                            null,
                                            entitlementInfoDTO.getEntitlementState(),
                                            null);
                errorList.add(error);
            }

            // Compare EntitlementUnit Data
            Map<String, List<EntitlementUnit>> entitlementUnitMap = new HashMap<String, List<EntitlementUnit>>();
            for (EntitlementUnit entitlementUnit : entitlement.getEntitlementUnitCollection()) {
                if (!entitlementUnitMap.containsKey(entitlementUnit.getFedTaxId())) {
                    entitlementUnitMap.put(entitlementUnit.getFedTaxId(), new ArrayList<EntitlementUnit>());
                }
                entitlementUnitMap.get(entitlementUnit.getFedTaxId()).add(entitlementUnit);
            }

            Map<String, EntitlementUnitInfoDTO> entitlementUnitInfoDTOMap = entitlementInfoDTO.getEntitlementUnits();
            for (String fein : entitlementUnitMap.keySet()) {
                EntitlementUnitInfoDTO entitlementUnitInfoDTO = entitlementUnitInfoDTOMap.get(fein);

                EntitlementUnit activatedEntitlementUnit = null;
                List<EntitlementUnit> deactivatedEntitlementUnits = new ArrayList<EntitlementUnit>();
                for (EntitlementUnit entitlementUnit : entitlementUnitMap.get(fein)) {
                    if (entitlementUnit.isActivated()) {
                        activatedEntitlementUnit = entitlementUnit;
                    } else {
                        deactivatedEntitlementUnits.add(entitlementUnit);
                    }
                }

                if (activatedEntitlementUnit != null) {
                    if (entitlementUnitInfoDTO == null) {
                        if (activatedEntitlementUnit.getEntitlementUnitStatus().notIn(EntitlementUnitStatusCode.ActivationHold,
                                                                                      EntitlementUnitStatusCode.PendingActivation)) {
                            message = "This Entitlement Unit does not exits in ERS";
                            error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                                        entitlement.getEntitlementOfferingCode(),
                                                        fein,
                                                        message,
                                                        entitlement.getEntitlementState(),
                                                        activatedEntitlementUnit.getEntitlementUnitStatus(),
                                                        entitlementInfoDTO.getEntitlementState(),
                                                        null);
                            errorList.add(error);
                        }
                        continue;
                    }

                    if (activatedEntitlementUnit.getEntitlementUnitStatus().notIn(EntitlementUnitStatusCode.PendingReactivation) &&
                            !entitlementUnitInfoDTO.getEntitlementUnitStatusCode().equals(activatedEntitlementUnit.getEntitlementUnitStatus())) {
                        message = String.format("The Entitlement Unit Status in PSP (%s) does not match the value in ERS (%s)", activatedEntitlementUnit.getEntitlementUnitStatus(), entitlementUnitInfoDTO.getEntitlementUnitStatusCode()) ;
                        error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                                    entitlement.getEntitlementOfferingCode(),
                                                    fein,
                                                    message,
                                                    entitlement.getEntitlementState(),
                                                    activatedEntitlementUnit.getEntitlementUnitStatus(),
                                                    entitlementInfoDTO.getEntitlementState(),
                                                    entitlementUnitInfoDTO.getEntitlementUnitStatusCode());
                        errorList.add(error);
                    }
                } else {
                    for (EntitlementUnit entitlementUnit : deactivatedEntitlementUnits) {
                        if (entitlementUnitInfoDTO == null) {
                            continue;
                        }

                        if (entitlementUnit.getEntitlementUnitStatus().notIn(EntitlementUnitStatusCode.PendingDeactivation) &&
                                !entitlementUnitInfoDTO.getEntitlementUnitStatusCode().equals(entitlementUnit.getEntitlementUnitStatus())) {
                            message = String.format("The Entitlement Unit Status in PSP (%s) does not match the value in ERS (%s)", entitlementUnit.getEntitlementUnitStatus(), entitlementUnitInfoDTO.getEntitlementUnitStatusCode()) ;
                            error = new ERSToolKitError(entitlement.getLicenseNumber(),
                                                        entitlement.getEntitlementOfferingCode(),
                                                        fein,
                                                        message,
                                                        entitlement.getEntitlementState(),
                                                        entitlementUnit.getEntitlementUnitStatus(),
                                                        entitlementInfoDTO.getEntitlementState(),
                                                        entitlementUnitInfoDTO.getEntitlementUnitStatusCode());
                            errorList.add(error);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.warn(t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return errorList;
    }

    private static EntitlementInfoDTO getEntitlementInfoDTO(String pLicense, String pEOC) throws Throwable {
        int timeoutCount = 0;
        boolean connected = false;
        if (entitlementService.get() == null) {
            entitlementService.set(createEntitlementService());
        }
        EntitlementServicePortType port = entitlementService.get().getEntitlementServicePort();

        ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mERSURL);
        ((BindingProvider) port).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, mRequestTimeout);
        ((BindingProvider) port).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, mRequestTimeout);
        ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, mRequestTimeout);

        GetEntitlementInformationAndPropertyDetailsRequest getEntitlementInformationAndPropertyDetailsRequest =
                WrapperFactory.generateGetEntitlementInformationAndPropertyDetailsRequest(pLicense, pEOC, true);
        GetEntitlementInformationAndPropertyDetailsResponse getEntitlementInformationAndPropertyDetailsResponse = null;
        while (!connected) {
            try {
                getEntitlementInformationAndPropertyDetailsResponse = port.getEntitlementInformationAndPropertyDetails(getEntitlementInformationAndPropertyDetailsRequest);
            } catch (WebServiceException e) {
                if(e.getCause() instanceof SocketTimeoutException) {
                    logger.info("ERS Gateway timed out. License #: " + pLicense);
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
                    throw new RuntimeException(errorMessage);
            }

            if(getEntitlementInformationAndPropertyDetailsResponse != null) {
                connected = true;
            }
        }

        if(getEntitlementInformationAndPropertyDetailsResponse == null) {
            throw new ERSConnectionException("Entitlement detail response was null.");
        }

        try {
            handleResponseStatus(getEntitlementInformationAndPropertyDetailsResponse.getStatus(), getEntitlementInformationAndPropertyDetailsResponse.getError());
        } catch (Throwable t) {
            return null;
        }

        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();

        Map<String, EditionType> stringToEditionType = EntitlementDTO.getEditionValues();
        Map<String, NumberOfEmployeesType> stringToNumberOfEmployeesType = EntitlementDTO.getNumberOfEmployeesValues();

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
                        entitlementInfoDTO.setAssetItemNumber(entitlement.getOfferingConfiguration().getItemNumber());
                        entitlementInfoDTO.setEntitlementState(EntitlementStateType.ENABLED.equals(entitlement.getEntitlementState()) ? EntitlementStateCode.Enabled : EntitlementStateCode.Disabled);
                    }
                }
                for (GetEntitlementInformationAndPropertyDetailsResponse.LicenseInfo.CoreEntitlement.EntitlementUnit entitlementUnit : coreEntitlement.getEntitlementUnit()) {
                    if (pEOC.equals(entitlementUnit.getEntitlementOfferingCode())) {
                        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                        String fedTaxId = entitlementUnit.getIdentifiedResourceValue();
                        entitlementUnitInfoDTO.setFedTaxId(fedTaxId);
                        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStateType.ACTIVATED.equals(entitlementUnit.getUnitCurrentState()) ? EntitlementUnitStatusCode.Activated : EntitlementUnitStatusCode.Deactivated);
                        entitlementInfoDTO.getEntitlementUnits().put(fedTaxId, entitlementUnitInfoDTO);
                    }
                }
            }
        }

        return createEntitlementInfoDTO(getEntitlementInformationAndPropertyDetailsResponse, pEOC);
    }

    public static EntitlementInfoDTO createEntitlementInfoDTO(GetEntitlementInformationAndPropertyDetailsResponse pGetEntitlementInformationAndPropertyDetailsResponse, String pEOC) {
        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();

        Map<String, EditionType> stringToEditionType = EntitlementDTO.getEditionValues();
        Map<String, NumberOfEmployeesType> stringToNumberOfEmployeesType = EntitlementDTO.getNumberOfEmployeesValues();

        if(pGetEntitlementInformationAndPropertyDetailsResponse.getLicenseInfo() != null) {
            entitlementInfoDTO.setCustomerId(pGetEntitlementInformationAndPropertyDetailsResponse.getLicenseInfo().getAccountId().getValue());
            for (GetEntitlementInformationAndPropertyDetailsResponse.LicenseInfo.CoreEntitlement coreEntitlement : pGetEntitlementInformationAndPropertyDetailsResponse.getLicenseInfo().getCoreEntitlement()) {
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
                        entitlementInfoDTO.setAssetItemNumber(entitlement.getOfferingConfiguration().getItemNumber());
                        entitlementInfoDTO.setEntitlementState(EntitlementStateType.ENABLED.equals(entitlement.getEntitlementState()) ? EntitlementStateCode.Enabled : EntitlementStateCode.Disabled);
                    }
                }
                for (GetEntitlementInformationAndPropertyDetailsResponse.LicenseInfo.CoreEntitlement.EntitlementUnit entitlementUnit : coreEntitlement.getEntitlementUnit()) {
                    if (pEOC.equals(entitlementUnit.getEntitlementOfferingCode())) {
                        EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                        String fedTaxId = entitlementUnit.getIdentifiedResourceValue();
                        entitlementUnitInfoDTO.setFedTaxId(fedTaxId);
                        entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStateType.ACTIVATED.equals(entitlementUnit.getUnitCurrentState()) ? EntitlementUnitStatusCode.Activated : EntitlementUnitStatusCode.Deactivated);
                        entitlementInfoDTO.getEntitlementUnits().put(fedTaxId, entitlementUnitInfoDTO);
                    }
                }
            }
        }

        return entitlementInfoDTO;
    }

    private static void handleResponseStatus(ResponseStatusType pResponseStatusType, ErrorType pErrorType) throws Throwable {
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

    private static void usage() {
        System.out.println("Usage: ERSToolKit <Command> [<License Number> <Entitlement Offering Code>]");
        System.out.println("Valid commands are " + Arrays.toString(ToolkitCommand.values()));
    }

    public static ScrollableResults getBatchedEntitlements() {
        String[] paramNames = new String[0];
        Object[] paramValues = new Object[0];

        return Application.scrollableResultsByNamedQuery("findAllEntitlementsSortedById", paramNames, paramValues, -1, -1, true);
    }

    public static Entitlement getEntitlementById(SpcfUniqueId pId) {
        String[] paramNames = new String[1];
        paramNames[0] = "id";
        Object[] paramValues = new Object[1];
        paramValues[0] = pId.toString();

        DomainEntitySet<Entitlement> entitlements = Application.findByNamedQuery("findEntitlementById", paramNames, paramValues, -1, -1, true);

        if (entitlements.isNotEmpty()) {
            return entitlements.getFirst();
        }
        return null;
    }

    private static enum ToolkitCommand {
        Validate
    }

    private static String transformSOAPFault(SOAPFaultException e) throws Throwable {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(e.getFault());
        transformer.transform(source, result);

        return result.getWriter().toString();
    }

    private static void readConfigurationParameters() {
        mInterval = SystemParameter.findIntValue(SystemParameter.Code.ERS_SYNC_THREAD_POOL_INTERVAL, 60);
        mMaxWait = SystemParameter.findIntValue(SystemParameter.Code.ERS_SYNC_THREAD_POOL_MAX_WAIT, 5 * 60);
        mMinPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ERS_SYNC_MIN_THREAD_POOL_SIZE, 8);
        mMaxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ERS_SYNC_MAX_THREAD_POOL_SIZE, 8);
        //mOutputDirectory = SystemParameter.findStringValue(SystemParameter.Code.ERS_SYNC_OUTPUT_DIRECTORY, "C:/") ;
        mOutputDirectory = SystemParameter.findStringValue(SystemParameter.Code.ERS_SYNC_OUTPUT_DIRECTORY, "/apps/batch/flux/logs") ;
        mBatchSize = SystemParameter.findIntValue(SystemParameter.Code.ERS_SYNC_BATCH_SIZE, 500);
        mRequestTimeout = SystemParameter.findIntValue(SystemParameter.Code.ERS_REQUEST_TIMEOUT, 10000);
        mMaxRetries = SystemParameter.findIntValue(SystemParameter.Code.ERS_MAX_RETRIES, 5);

        mERSURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_ers_server_url");
        mLocalURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_ers_local_url");
    }

    public static class ERSToolKitError {
        String mLicenseNumber;
        String mEntitlementOfferingCode;
        String mFedTaxId;
        String mMessage;
        EntitlementStateCode mPspEntitlementState;
        EntitlementUnitStatusCode mPspEntitlementUnitStatus;
        EntitlementStateCode mErsEntitlementState;
        EntitlementUnitStatusCode mErsEntitlementUnitStatus;

        public ERSToolKitError(String mLicenseNumber, String mEntitlementOfferingCode, String mFedTaxId, String mMessage, EntitlementStateCode mPspEntitlementState, EntitlementUnitStatusCode mPspEntitlementUnitStatus, EntitlementStateCode pErsEntitlementState, EntitlementUnitStatusCode pErsEntitlementUnitStatus) {
            this.mLicenseNumber = mLicenseNumber;
            this.mEntitlementOfferingCode = mEntitlementOfferingCode;
            this.mFedTaxId = mFedTaxId;
            this.mMessage = mMessage;
            this.mPspEntitlementState = mPspEntitlementState;
            this.mPspEntitlementUnitStatus = mPspEntitlementUnitStatus;
            this.mErsEntitlementState = pErsEntitlementState;
            this.mErsEntitlementUnitStatus = pErsEntitlementUnitStatus;
        }

        public String getLicenseNumber() {
            return mLicenseNumber;
        }

        public void setLicenseNumber(String pLicenseNumber) {
            this.mLicenseNumber = pLicenseNumber;
        }

        public String getEntitlementOfferingCode() {
            return mEntitlementOfferingCode;
        }

        public void setEntitlementOfferingCode(String pEntitlementOfferingCode) {
            this.mEntitlementOfferingCode = pEntitlementOfferingCode;
        }

        public String getFedTaxId() {
            return mFedTaxId;
        }

        public void setFedTaxId(String pFedTaxId) {
            this.mFedTaxId = pFedTaxId;
        }

        public EntitlementStateCode getEntitlementState() {
            return mPspEntitlementState;
        }

        public void setEntitlementState(EntitlementStateCode pEntitlementState) {
            this.mPspEntitlementState = pEntitlementState;
        }

        public EntitlementUnitStatusCode getEntitlementUnitStatus() {
            return mPspEntitlementUnitStatus;
        }

        public void setEntitlementUnitStatus(EntitlementUnitStatusCode pEntitlementUnitStatus) {
            this.mPspEntitlementUnitStatus = pEntitlementUnitStatus;
        }

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String pMessage) {
            this.mMessage = pMessage;
        }

        public EntitlementStateCode getErsEntitlementState() {
            return mErsEntitlementState;
        }

        public void setErsEntitlementState(EntitlementStateCode pErsEntitlementState) {
            this.mErsEntitlementState = pErsEntitlementState;
        }

        public EntitlementUnitStatusCode getErsEntitlementUnitStatus() {
            return mErsEntitlementUnitStatus;
        }

        public void setErsEntitlementUnitStatus(EntitlementUnitStatusCode pErsEntitlementUnitStatus) {
            this.mErsEntitlementUnitStatus = pErsEntitlementUnitStatus;
        }
    }

}
