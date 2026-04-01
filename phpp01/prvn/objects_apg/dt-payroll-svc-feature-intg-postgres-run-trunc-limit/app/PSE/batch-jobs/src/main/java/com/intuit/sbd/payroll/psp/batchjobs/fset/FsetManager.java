package com.intuit.sbd.payroll.psp.batchjobs.fset;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.fset.xsd.*;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.*;

/**
 * User: ihannur
 * Date: 9/12/12
 * Time: 4:13 PM
 */
public class FsetManager {

    protected static final SpcfLogger logger = SpcfLogManager.getLogger(FsetManager.class);

    private Map<String,List<String>> rejectionEmailMap;
    private List<String> rejectionEmailList;
    private static JAXBContext jaxbContext;

    private Unmarshaller mUnmarshaller;
    private Marshaller mMarshaller;

    private PSPRequestContextManager pspRequestContextManager;

    public FsetManager() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }


    protected void initializeUnmarshaller() throws JAXBException {
        if (mUnmarshaller == null) {
            initializeContext();
            mUnmarshaller = jaxbContext.createUnmarshaller();
        }
    }

    protected void initializeMarshaller() throws JAXBException {
        if (mMarshaller == null) {
            initializeContext();
            mMarshaller = jaxbContext.createMarshaller();
            mMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        }
    }

    private static void initializeContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("com.intuit.sbd.payroll.psp.batchjobs.fset.xsd");
        }
    }

    public void createFsetReturnFile(DomainEntitySet<MoneyMovementTransaction> pPayments) throws JAXBException, DatatypeConfigurationException {
        initializeMarshaller();

        GregorianCalendar now = new GregorianCalendar();
        now.setTimeInMillis(PSPDate.getPSPTime().getTimeInMilliseconds());
        XMLGregorianCalendar gcNow = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
        gcNow.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);

        Transmission transmission = new Transmission();

        TransmissionHeaderType transmissionHeader = new TransmissionHeaderType();
        transmissionHeader.setRecordCount(BigInteger.valueOf(pPayments.size()));
        transmissionHeader.setJurisdiction("MS Department of Revenue");

        String etin = BatchUtils.getTaxAgencyConfigString("psp_fset_sender_etin");
        String efin = BatchUtils.getTaxAgencyConfigString("psp_fset_sender_efin");

        String transmissionId = etin + PSPDate.getPSPTime().format("yyyyMMddHHmmssS");
        transmissionHeader.setTransmissionId(transmissionId);
        transmissionHeader.setTimestamp(gcNow);

        TransmissionHeaderType.Transmitter transmitter = new TransmissionHeaderType.Transmitter();
        transmitter.setETIN(etin);
        transmissionHeader.setTransmitter(transmitter);
        transmissionHeader.setProcessType(BatchUtils.getTaxAgencyConfigString("psp_fset_fset_mode"));
        transmissionHeader.setAgentIdentifier(BatchUtils.getTaxAgencyConfigString("psp_fset_sender_agent_id"));

        transmission.setTransmissionHeader(transmissionHeader);

        FsetFile fsetFile = new FsetFile();
        fsetFile.setFileType(FsetFileType.FsetReturns);
        fsetFile.setStatusCd(FsetFileStatus.PendingTransmission);
        fsetFile.setStatusEffectiveDate(PSPDate.getPSPTime());
        fsetFile.setTransmissionId(transmissionId);
        Application.save(fsetFile);

        for (MoneyMovementTransaction payment : pPayments) {
            FsetFilingDetail fsetFilingDetail = new FsetFilingDetail();
            fsetFilingDetail.setCompany(payment.getCompany());
            fsetFilingDetail.setAgencyId(payment.getAgencyTaxpayerId());
            Company company = payment.getCompany();
            String legalName = company.getLegalName();
            if (legalName != null) {
                legalName = legalName.replaceAll("[^A-Za-z0-9#\\-\\(\\)&\\s']", "").replaceAll("\\s+", " ").trim();
                if (legalName.length() > 75) {
                    legalName = legalName.substring(0, 75);
                }
                fsetFilingDetail.setBusinessName(legalName);
            }

            Address mailingAddress = company.getLegalAddress();
            String cityName =  mailingAddress.getCity().replaceAll("[^A-Za-z0-9\\s]", "").replaceAll("\\s+", " ").trim();
            fsetFilingDetail.setCity(cityName);
            fsetFilingDetail.setZip(mailingAddress.getZipCode());
            fsetFilingDetail.setState(mailingAddress.getState());

            String addressLine1 = mailingAddress.getAddressLine1();
            String addressLine2 = mailingAddress.getAddressLine2();

            if (addressLine1 != null) {
                fsetFilingDetail.setAddressLine1(formatAddressLine(addressLine1));
            }

            if (addressLine2 != null) {
                fsetFilingDetail.setAddressLine2(formatAddressLine(addressLine2));
            }

            fsetFilingDetail.setFedTaxId(company.getFedTaxId());
            fsetFilingDetail.setFilingAmount(payment.getMoneyMovementTransactionAmount());
            fsetFilingDetail.setFilingDueDate(payment.getDueDate());
            fsetFilingDetail.setMoneyMovementTransaction(payment);
            fsetFilingDetail.setPeriodEndDate(payment.getPaymentPeriodEnd());
            fsetFilingDetail.setStatus(FsetPaymentStatus.SentToAgency);

            //Submission ID format => 6 digit EFIN + 4 digit year + 3 digit Julian Date + 7 unique Alphanumerics (Generating 7 digit random number)
            //noinspection MalformedFormatString
            fsetFilingDetail.setSubmissionId(efin + String.format("%1$tY%1$tj", now) + String.valueOf(Math.round(Math.random() * 9000000) + 1000000));

            fsetFilingDetail.setParentFile(fsetFile);

            transmission.getReturnState().add(createReturnState(fsetFilingDetail, gcNow));
            Application.save(fsetFilingDetail);
        }

        StringWriter stringWriter = new StringWriter();
        mMarshaller.marshal(transmission, stringWriter);

        fsetFile.setFileName(writeMessageToFile(stringWriter.toString(), etin));

    }

    public void processResponseFile(DomainEntitySet<FsetFile> pFsetFiles) throws JAXBException {
        initializeUnmarshaller();

        rejectionEmailMap = new HashMap<String, List<String>>();
        rejectionEmailList = new ArrayList<String>();

        DomainEntitySet<FsetFilingDetail> rejectedFilings = new DomainEntitySet<FsetFilingDetail>();
        DomainEntitySet<FsetFilingDetail> parseRejectedFilings = new DomainEntitySet<FsetFilingDetail>();
        for (FsetFile fsetFile : pFsetFiles) {
            boolean isParseError = false;
            AckTransmission ackTransmission = (AckTransmission) mUnmarshaller.unmarshal(new File(fsetFile.getFileName()));
            fsetFile.setTransmissionId(ackTransmission.getTransmissionHeader().getTransmissionId());
            //encypt this file and delete unencypted file
            BatchUtils.encryptFileInStreamsUsingIDPS(new File(fsetFile.getFileName()));
            String efin = BatchUtils.getTaxAgencyConfigString("psp_fset_sender_efin");
            String etin = BatchUtils.getTaxAgencyConfigString("psp_fset_sender_etin");
            String alternateEtin = BatchUtils.getTaxAgencyConfigString("psp_fset_sender_etin_alternate"); // for QDC, this is the LVDC value - and vice versa

            Acknowledgement parseErrorAck = null;
            for (Acknowledgement acknowledgement : ackTransmission.getAcknowledgement()) {
                String ackEfin = acknowledgement.getEFIN();
                if (ackEfin != null && (ackEfin.equals(efin) || ackEfin.equals(etin) || ackEfin.equals(alternateEtin))) {
                    // log the special case, when alternate ETIN matched
                    if (ackEfin.equals(alternateEtin)) {
                        logger.warn("EFIN matched against alternate ETIN value: " + ackEfin);
                    }
                    FsetFilingDetail fsetFilingDetail = FsetFilingDetail.findFsetFilingDetailBySubmissionId(acknowledgement.getSubmissionId());
                    if (fsetFilingDetail != null) {
                        String filingStatus = acknowledgement.getFilingStatus();
                        fsetFilingDetail.setResponseFile(fsetFile);
                        if ("A".equals(filingStatus)) {
                            fsetFilingDetail.setStatus(FsetPaymentStatus.AcceptedByAgency);
                            fsetFilingDetail.setFilingStatusDate(PSPDate.getPSPTime());
                        } else if ("R".equals(filingStatus)) {
                            rejectedFilings.add(fsetFilingDetail);
                            fsetFilingDetail.setStatus(FsetPaymentStatus.RejectedByAgency);
                            fsetFilingDetail.setFilingStatusDate(PSPDate.getPSPTime());

                            rejectionEmailList.add(0,ackTransmission.getTransmissionHeader().getJurisdiction());
                            rejectionEmailList.add(1,acknowledgement.getSubmissionType());
                            rejectionEmailMap.put(acknowledgement.getSubmissionId(),rejectionEmailList);

                            populateErrorMessage(acknowledgement, fsetFilingDetail);

                        } else {
                            logger.error("FilingStatus has to either be A or R. Received: " + filingStatus);
                        }
                        Application.save(fsetFilingDetail);
                    } else {
                        // parse error has only one acknowledgement
                        parseErrorAck = acknowledgement;
                        isParseError = true;
                    }
                } else {
                    logger.error("FSET AckTransmission contains - responses with EFIN:" + ackEfin + " Intuit EFIN is:" + efin + " Intuit ETIN is:" + etin);
                }
            }
            if (!isParseError) {
                fsetFile.setStatusCd(FsetFileStatus.Completed);
            } else {
                DomainEntitySet<FsetFilingDetail> details = FsetFilingDetail.findFsetFilingDetailByTransmissionId(fsetFile.getTransmissionId());
                for (FsetFilingDetail detail : details) {
                    // We ll add the parse errors to a separate List to separate the two errors.
                    // Note that this would be different from the FSET rejection email which consist of response file details
                    populateErrorMessage(parseErrorAck, detail);
                    parseRejectedFilings.add(detail);
                }
                fsetFile.setStatusCd(FsetFileStatus.Error);
            }
            Application.save(fsetFile);
        }

        sendEmailForFsetRejections("FSET Rejections", rejectedFilings);
        sendEmailForFsetRejections("FSET Parse Error", parseRejectedFilings);
    }

    private void populateErrorMessage(Acknowledgement acknowledgement, FsetFilingDetail pFsetFilingDetail) {
        StringBuilder errorMessage = new StringBuilder();
        if (acknowledgement.getErrorList() != null) {
            for (ValidationErrorListType.Error error : acknowledgement.getErrorList().getError()) {
                errorMessage.append(error.getErrorId() + ":" + error.getErrorCategory() + ":" + error.getErrorMessage());
            }
        }
        if (StringUtils.isEmpty(errorMessage.toString())) {
            errorMessage.append("No error message in Ack File");
        }

        if (errorMessage.length() > 4000) {
            pFsetFilingDetail.setErrorMessage(errorMessage.substring(0, 4000));
        } else {
            pFsetFilingDetail.setErrorMessage(errorMessage.toString());
        }
    }

    public void archiveFsetFiles() throws S3UploadException, S3ConnectionException {
        logger.info("Begin archiveFsetFiles.");

        try {
            String archiveDir = BatchUtils.getTaxAgencyConfigString("psp_fset_arcv_dir");

            Application.beginUnitOfWork();

            DomainEntitySet<FsetFile> fsetFiles = Application.find(FsetFile.class, FsetFile.StatusCd().in(FsetFileStatus.SentToAgency, FsetFileStatus.Completed));

            for (FsetFile fsetFile : fsetFiles) {
                String batchJobName = BatchJobType.FsetFilingProcessor.name();

                fsetFile.setFileName(S3UploadUtils.archive(batchJobName,archiveDir,fsetFile.getFileName()));
                fsetFile.setStatusCd(FsetFileStatus.Archived);
                fsetFile.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(fsetFile);
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

        logger.info("End archiveFsetFiles.");
    }

    protected String writeMessageToFile(String pMessage, String pEtin) {
        String outDir = BatchUtils.getTaxAgencyConfigString("psp_fset_send_dir");
        //File Name format => RTN_ETINYYYYMMDDHHMMSS.xml
        String fileName = outDir + File.separator + "RTN_" + pEtin + PSPDate.getPSPTime().format("yyyyMMddHHmmss") + ".xml";
        try {
            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            OutputStreamWriter fileWriter = new IDPSFileWriter(fileName,key);
            fileWriter.write(pMessage);
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Failed to write message.\n" + pMessage, e);
        }
        return fileName;
    }

    protected ReturnState createReturnState(FsetFilingDetail pFsetFilingDetail, XMLGregorianCalendar pNow) throws DatatypeConfigurationException {
        ReturnState returnState = new ReturnState();
        FSETReturnHeaderStateType returnHeaderState = new FSETReturnHeaderStateType();
        returnHeaderState.setJurisdiction("MS");

        returnHeaderState.setTimestamp(pNow);

        //Setting unwanted fields to undefined to get the formatted value - YYYY-MM-DD
        XMLGregorianCalendar gcEndDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(pFsetFilingDetail.getPeriodEndDate().getYear(), pFsetFilingDetail.getPeriodEndDate().getMonth(),
                                                                                               pFsetFilingDetail.getPeriodEndDate().getDay(), DatatypeConstants.FIELD_UNDEFINED,
                                                                                               DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                                                                                               DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
        returnHeaderState.setTaxPeriodEndDate(gcEndDate);

        //Due date format YYYY-MM-DD
        XMLGregorianCalendar gcDueDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(pFsetFilingDetail.getFilingDueDate().getYear(), pFsetFilingDetail.getFilingDueDate().getMonth(),
                                                                                               pFsetFilingDetail.getFilingDueDate().getDay(), DatatypeConstants.FIELD_UNDEFINED,
                                                                                               DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                                                                                               DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
        returnHeaderState.setDueDate(gcDueDate);

        //Setting unwanted fields to undefined to get the formatted value - YYYY
        XMLGregorianCalendar gcYearEnd = DatatypeFactory.newInstance().newXMLGregorianCalendar(pFsetFilingDetail.getPeriodEndDate().getYear(), DatatypeConstants.FIELD_UNDEFINED,
                                                                                               DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                                                                                               DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                                                                                               DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
        returnHeaderState.setTaxYear(gcYearEnd);

        ReturnHeaderType.Originator originator = new ReturnHeaderType.Originator();
        String efin = BatchUtils.getTaxAgencyConfigString("psp_fset_sender_efin");
        originator.setEFIN(efin);
        originator.setOriginatorTypeCd(OriginatorType.REPORTING_AGENT);
        returnHeaderState.setOriginator(originator);

        returnHeaderState.setSoftwareId("Intuit_PSP");
        FSETReturnHeaderStateType.Filer filer = new FSETReturnHeaderStateType.Filer();
        TINType value = new TINType();
        value.setTINTypeValue(pFsetFilingDetail.getFedTaxId());
        value.setTypeTIN("FEIN");
        filer.setTIN(value);
        filer.setStateEIN(pFsetFilingDetail.getAgencyId());
        BusinessNameType businessNameType = new BusinessNameType();
        businessNameType.setBusinessNameLine1(pFsetFilingDetail.getBusinessName());
        filer.setName(businessNameType);
        USAddressType addressType = new USAddressType();
        addressType.setAddressLine1(pFsetFilingDetail.getAddressLine1());
        if (pFsetFilingDetail.getAddressLine2() != null) {
            addressType.setAddressLine2(pFsetFilingDetail.getAddressLine2());
        }
        addressType.setCity(pFsetFilingDetail.getCity());
        addressType.setState(StateType.fromValue(pFsetFilingDetail.getState()));
        addressType.setZIPCode(pFsetFilingDetail.getZip().substring(0,5));

        filer.setUSAddress(addressType);

        returnHeaderState.setFiler(filer);
        returnHeaderState.setForm("WTH");

        FSETReturnHeaderStateType.FilingAction filingAction = new FSETReturnHeaderStateType.FilingAction();
        filingAction.setAction("Original");
        returnHeaderState.setFilingAction(filingAction);
        returnState.setReturnHeaderState(returnHeaderState);
        ReturnDataState returnDataState = new ReturnDataState();
        returnDataState.setSubmissionId(pFsetFilingDetail.getSubmissionId());
        StateWHType stateWHType = new StateWHType();
        stateWHType.setTotalIncomeTaxWithheld(SpcfUtils.convertToBigDecimal(pFsetFilingDetail.getFilingAmount()));
        stateWHType.setWHTotalTax(SpcfUtils.convertToBigDecimal(pFsetFilingDetail.getFilingAmount()));
        returnDataState.setStateWH(stateWHType);

        returnState.setReturnDataState(returnDataState);

        return returnState;
    }

    private void sendEmailForFsetRejections(String pMessageHeader, DomainEntitySet<FsetFilingDetail> pFsetFilingDetails) {

        if (pFsetFilingDetails.isNotEmpty()) {

            StringBuilder messageBody = new StringBuilder();
            String crlf = "\r\n";

            // Build the message body
            messageBody.append(pMessageHeader).append(crlf);

            for (int i = pMessageHeader.length(); i > 0; --i) {
                messageBody.append("-");
            }

            messageBody.append(crlf);

            for (FsetFilingDetail fsetFilingDetail : pFsetFilingDetails) {
                try {
                    pspRequestContextManager.setRequestContextCompany(fsetFilingDetail.getCompany());
                    messageBody.append(crlf); // add empty line for separation
                    if (fsetFilingDetail.getResponseFile() != null) {
                        messageBody.append("File Name       : ").append(fsetFilingDetail.getResponseFile().getFileName()).append(crlf);
                    } else {
                        messageBody.append("File Name       : ").append(fsetFilingDetail.getParentFile().getFileName()).append(crlf);
                    }
                    messageBody.append("Filing Status       : ").append(fsetFilingDetail.getStatus().toString()).append(crlf);
                    messageBody.append("Filing Error        : ").append(fsetFilingDetail.getErrorMessage()).append(crlf);
                    messageBody.append("Filing Amount       : ").append(fsetFilingDetail.getFilingAmount().toString()).append(crlf);
                    messageBody.append("Filing PSID         : ").append(fsetFilingDetail.getMoneyMovementTransaction().getCompany().getSourceCompanyId()).append(crlf);
                    messageBody.append("Filing Due Date     : ").append(fsetFilingDetail.getFilingDueDate().toString()).append(crlf);
                    messageBody.append("Filing Jurisdiction : ").append(rejectionEmailMap.get(fsetFilingDetail.getSubmissionId()).get(0)).append(crlf);
                    messageBody.append("Filing Tax Type     : ").append(rejectionEmailMap.get(fsetFilingDetail.getSubmissionId()).get(1)).append(crlf);
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }

            // Send the email
            BatchUtils.sendFsetRejectionsNotificationEmail("FSET Rejections Notification", messageBody.toString());
        }
    }

    private String formatAddressLine(String pAddressLine) {
        if (pAddressLine != null) {
            pAddressLine = pAddressLine.replaceAll("[^A-Za-z0-9\\-/\\s]", "").replaceAll("\\s+", " ").replaceAll("^[^A-Za-z0-9]+", "").trim();
            if (pAddressLine.length() > 35) {
                pAddressLine = pAddressLine.substring(0, 35);
            }
        }
        return pAddressLine;
    }
}