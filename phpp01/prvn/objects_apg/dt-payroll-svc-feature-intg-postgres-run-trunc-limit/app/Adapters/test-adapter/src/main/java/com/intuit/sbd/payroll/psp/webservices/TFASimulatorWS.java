package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.eftps.*;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.ops.eftpsBp.EdiEftpsRecordList;
import com.paycycle.ops.eftpsBp.EnrollmentFile;
import com.paycycle.ops.eftpsBp.GenericEdiFile;
import com.paycycle.ops.eftpsBp.PaymentFile;
import org.apache.commons.lang.StringUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 15, 2010
 * Time: 2:03:42 PM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class TFASimulatorWS {
    @WebMethod
    public List<EFTPSFileWSDTO> processEnrollments(@WebParam(name = "wantFileContents") boolean pWantFileContents,
                                                   @WebParam(name = "formatContents") boolean pFormatContents) throws Exception {
        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        EdiManager.processEnrollments();

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<EftpsFile> eftpsFiles =
                    EftpsFile.getEftpsFilesByTypeAndStatus(EdiFileType.EftpsEnrollment,
                                                           EdiFileStatus.PendingTransmission);

            for (EftpsFile eftpsFile : eftpsFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();

                eftpsFileWSDTO.fileName = eftpsFile.getFileName();

                if (pWantFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(eftpsFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(pFormatContents);
                }

                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processPayments(@WebParam(name = "displayFileContents") boolean displayFileContents,
                                                @WebParam(name = "formattedContents") boolean formattedContents,
                                                @WebParam(name = "settlementDate") String pSettlementDate) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        try{
            PayrollServices.beginUnitOfWork();
            //ProcessNextDayPayments will process payment record whose initiation date is PSPDate. So converting settlement date to initiation date by subtracting one business day.
            SpcfCalendar presentTime = PSPDate.getPSPTime();
            SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
            if(!StringUtils.isEmpty(pSettlementDate)){
                settlementDate = CalendarUtils.convertToSpcfCalendar(new Date(pSettlementDate));
            }
            CalendarUtils.addBusinessDays(settlementDate, -1);
            PSPDate.setPSPTime(settlementDate);
            PayrollServices.commitUnitOfWork();

            BatchJobManager.runJob(BatchJobType.EftpsPayment);

            PayrollServices.beginUnitOfWork();
            //After processing payments update the PSPDate to previous date.
            PSPDate.setPSPTime(presentTime);
            PayrollServices.commitUnitOfWork();
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }

        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<EftpsFile> eftpsFiles =
                    EftpsFile.getEftpsFilesByTypeAndStatus(EdiFileType.EftpsPayment,
                                                           EdiFileStatus.PendingTransmission);

            for (EftpsFile eftpsFile : eftpsFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();

                eftpsFileWSDTO.fileName = eftpsFile.getFileName();

                if (displayFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(eftpsFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(formattedContents);
                }

                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processAs400Files(@WebParam(name = "wantFileContents") boolean pWantFileContents,
                                                  @WebParam(name = "formatContents") boolean pFormatContents) throws Exception {
        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        EdiManager.processAS400Files();

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<EftpsFile> eftpsFiles =
                    EftpsFile.getEftpsFilesByTypeAndStatus(EdiFileType.EftpsPayment,
                                                           EdiFileStatus.PendingTransmission);

            for (EftpsFile eftpsFile : eftpsFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();

                eftpsFileWSDTO.fileName = eftpsFile.getFileName();

                if (pWantFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(eftpsFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(pFormatContents);
                }

                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processPendingTransmissions(@WebParam(name = "wantFileContents") boolean pWantFileContents,
                                                            @WebParam(name = "formatContents") boolean pFormatContents) throws Exception {
        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        // simulate files have been transmitted to TFA
        try {
            PayrollServices.beginUnitOfWork();

            for (EftpsFile eftpsFile : EftpsFile.getPendingTransmissionEftpsFiles()) {
                eftpsFile.setStatusCd(EdiFileStatus.Completed);
                eftpsFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                eftpsFile.setSubmitDate(PSPDate.getPSPTime());
                Application.save(eftpsFile);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<EftpsFile> eftpsFiles = EftpsFile.getCompletedEftpsFiles();

            for (EftpsFile eftpsFile : eftpsFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();

                eftpsFileWSDTO.fileName = eftpsFile.getFileName();

                if (pWantFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(eftpsFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(pFormatContents);
                }

                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processWaitingResponseFiles(@WebParam(name = "wantFileContents") boolean pWantFileContents,
                                                            @WebParam(name = "formatContents") boolean pFormatContents) throws Exception {
        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        EdiManager.processWaitingResponseFiles();

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<EftpsFile> eftpsFiles = EftpsFile.getCompletedEftpsFiles();

            for (EftpsFile eftpsFile : eftpsFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();

                eftpsFileWSDTO.fileName = eftpsFile.getFileName();

                if (pWantFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(eftpsFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(pFormatContents);
                }

                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> transmitAS400Files(@WebParam(name = "wantFileContents") boolean pWantFileContents,
                                                   @WebParam(name = "formatContents") boolean pFormatContents) throws Exception {
        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        // simulate files have been transmitted to the AS400
        try {
            PayrollServices.beginUnitOfWork();

            Criterion<EdiTaxFile> where = EdiTaxFile.StatusCd().equalTo(EdiFileStatus.SendToAS400);
            DomainEntitySet<EftpsFile> eftpsFileSet = Application.find(EftpsFile.class, where);

            for (EftpsFile eftpsFile : eftpsFileSet) {
                eftpsFile.setStatusCd(EdiFileStatus.Completed);
                eftpsFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                Application.save(eftpsFile);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<EftpsFile> eftpsFiles = EftpsFile.getCompletedEftpsFiles();

            for (EftpsFile eftpsFile : eftpsFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();

                eftpsFileWSDTO.fileName = eftpsFile.getFileName();

                if (pWantFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(eftpsFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(pFormatContents);
                }

                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> archiveFiles(@WebParam(name = "wantFileContents") boolean pWantFileContents,
                                             @WebParam(name = "formatContents") boolean pFormatContents) throws Exception {
        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        DomainEntitySet<EftpsFile> completedEftpsFiles;
        try {
            Application.beginUnitOfWork();
            completedEftpsFiles = EftpsFile.getCompletedEftpsFiles();
            Application.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        EdiManager.archiveFiles();

        try {
            PayrollServices.beginUnitOfWork();
            for (EftpsFile eftpsFile : completedEftpsFiles) {
                Application.refresh(eftpsFile);
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();

                eftpsFileWSDTO.fileName = eftpsFile.getFileName();

                if (pWantFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(eftpsFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(pFormatContents);
                }

                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public ProcessedFileInfoWSDTO createResponse(@WebParam(name = "sourceFileName") String pSourceFileName,
                                                 @WebParam(name = "rejectionInfo") List<RejectionInfoWSDTO> pRejectionInfo) throws Exception {
        ProcessedFileInfoWSDTO processedFileInfoWSDTO = new ProcessedFileInfoWSDTO();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        GenericEdiFile genericEdiFile = new GenericEdiFile();
        genericEdiFile.setFileName(pSourceFileName);
        genericEdiFile.read();

        switch (genericEdiFile.getEdiFileType()) {
            case EDI813:
                createPaymentResponseFiles(pSourceFileName, EftpsUtil.getTfaDir(), pRejectionInfo, processedFileInfoWSDTO, null);
                break;
            case EDI151:
                // TODO: Add support for creating 151 response
                break;
            case EDI838:
                creteEnrollmentResponseFiles(pSourceFileName, EftpsUtil.getTfaDir(), pRejectionInfo, processedFileInfoWSDTO);
                break;
            case EDI824:
                processEnrollmentResponseFile(pSourceFileName, EftpsUtil.getWorkDir(), processedFileInfoWSDTO);
                break;
            case EDI826:
                // TODO: Add support for creating 826 response
                break;
            case EDI827:
                // TODO: Add support for creating 827 response
                break;
        }

        processedFileInfoWSDTO.inputFileType = genericEdiFile.getEftpsFileType().toString();

        return processedFileInfoWSDTO;
    }

    @WebMethod
    public void processEnrollmentAgeOut() throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        EdiManager.ageOutEnrollments();
    }

    @WebMethod
    public void resetFileSequence() throws Exception {
        try {
            Application.beginUnitOfWork();

            Statement statement = Application.getConnection().createStatement();

            try {
                int fileSequence = EftpsUtil.getNextEftpsFileSequence()*-1 + 1;

                String environmentId = ConfigurationManager.getEnvironmentIdentifier();
                String ownerName = "pspadm";
                if("local".equals(environmentId)) {
                    ownerName = "psp_local";
                }

                statement.execute("alter sequence "+ownerName+".seq_eftps_file_sequence increment by "+ fileSequence +" minvalue 1");
                Application.nextSequenceValue(SequenceId.SEQ_EFTPS_FILE_SEQUENCE, Long.class);
                statement.execute("alter sequence "+ownerName+".seq_eftps_file_sequence increment by 1 minvalue 1");

            } finally {
                statement.close();
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void resetPaymentSequence() throws Exception {
        try {
            Application.beginUnitOfWork();

            Statement statement = Application.getConnection().createStatement();

            try {
                int paymentSequence = EftpsUtil.getNextEftpsPaymentSequence()*-1 + 1;

                String environmentId = ConfigurationManager.getEnvironmentIdentifier();
                String ownerName = "pspadm";
                if("local".equals(environmentId)) {
                    ownerName = "psp_local";
                }

                statement.execute("alter sequence "+ownerName+".seq_eftps_payment_sequence increment by "+ paymentSequence +" minvalue 1");
                Application.nextSequenceValue(SequenceId.SEQ_EFTPS_PAYMENT_SEQUENCE, Long.class);
                statement.execute("alter sequence "+ownerName+".seq_eftps_payment_sequence increment by 1 minvalue 1");
            } finally {
                statement.close();
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void resetSegmentSequence() throws Exception {
        try {
            Application.beginUnitOfWork();

            Statement statement = Application.getConnection().createStatement();

            try {
                int segmentSequence = EftpsUtil.getNextEftpsSegmentSequence()*-1 + 1001;

                String environmentId = ConfigurationManager.getEnvironmentIdentifier();
                String ownerName = "pspadm";
                if("local".equals(environmentId)) {
                    ownerName = "psp_local";
                }

                statement.execute("alter sequence "+ownerName+".seq_eftps_segment_sequence increment by "+ segmentSequence +" minvalue 1000");
                Application.nextSequenceValue(SequenceId.SEQ_EFTPS_SEGMENT_SEQUENCE, Long.class);
                statement.execute("alter sequence "+ownerName+".seq_eftps_segment_sequence increment by 1 minvalue 1000");

            } finally {
                statement.close();
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<EftpsFileRecordWSDTO> queryEFTPSFile(@WebParam(name = "fileStatus") EdiFileStatus fileStatus) {
        DomainEntitySet<EftpsFile> eftpsFiles;

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        if (fileStatus == null) {
            eftpsFiles = Application.find(EftpsFile.class);
        } else {
            eftpsFiles = Application.find(EftpsFile.class, EftpsFile.StatusCd().equalTo(fileStatus));
        }

        List<EftpsFileRecordWSDTO> eftpsFileRecordWSDTOs = new ArrayList<EftpsFileRecordWSDTO>();

        for (EftpsFile eftpsFile : eftpsFiles) {
            EftpsFileRecordWSDTO eftpsFileRecordWSDTO = new EftpsFileRecordWSDTO();

            eftpsFileRecordWSDTO.fileType = eftpsFile.getFileType().toString();
            eftpsFileRecordWSDTO.fileCode = Integer.toString(eftpsFile.getFileCode());
            eftpsFileRecordWSDTO.fileId = Integer.toString(eftpsFile.getFileId());
            eftpsFileRecordWSDTO.systemOwner = eftpsFile.getSystemOwner().toString();
            eftpsFileRecordWSDTO.statusCode = eftpsFile.getStatusCd().toString();
            eftpsFileRecordWSDTO.fileName = eftpsFile.getFileName();
            eftpsFileRecordWSDTO.statusEffectiveDate =
                    eftpsFile.getStatusEffectiveDate() != null ? eftpsFile.getStatusEffectiveDate().toString() : "";

            eftpsFileRecordWSDTOs.add(eftpsFileRecordWSDTO);
        }

        return eftpsFileRecordWSDTOs;
    }

    @WebMethod
    public List<EftpsEnrollmentWSDTO> queryEFTPSEnrollment(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                           @WebParam(name = "sourceCompanyID") String sourceCompanyID) {
        if (StringUtils.isEmpty(sourceSystemCD)) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (StringUtils.isEmpty(sourceCompanyID)) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        List<EftpsEnrollmentWSDTO> eftpsEnrollmentWSDTOs = new ArrayList<EftpsEnrollmentWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (company == null) {
                throw new RuntimeException(String.format("Company %s:%s does not exist.", sourceSystemCD, sourceCompanyID));
            }

            for (EftpsEnrollment enrollment : company.getAllEnrollments()) {
                EftpsEnrollmentWSDTO enrollmentWSDTO = new EftpsEnrollmentWSDTO();
                enrollmentWSDTO.eftpsEnrollmentId = enrollment.getEftpsEnrollmentId();
                enrollmentWSDTO.statusCd = enrollment.getStatusCd().toString();
                enrollmentWSDTO.statusEffectiveDate = enrollment.getStatusEffectiveDate().format("MM/dd/yyyy");
                enrollmentWSDTO.secondary = enrollment.getSecondary();
                eftpsEnrollmentWSDTOs.add(enrollmentWSDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsEnrollmentWSDTOs;
    }

    @WebMethod
    public List<EFTPSEnrollmentDetailsWSDTO> queryEFTPSEnrollmentDetails(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                                         @WebParam(name = "sourceCompanyID") String sourceCompanyID) {
        if (StringUtils.isEmpty(sourceSystemCD)) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (StringUtils.isEmpty(sourceCompanyID)) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        List<EFTPSEnrollmentDetailsWSDTO> eftpsEnrollmentDetailsWSDTOs = new ArrayList<EFTPSEnrollmentDetailsWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (company == null) {
                throw new RuntimeException(String.format("Company %s:%s does not exist.", sourceSystemCD, sourceCompanyID));
            }

            Criterion<EftpsEnrollmentDetail> eftpsEnrollmentCriteria = null;
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EftpsEnrollmentDetail.FedTaxIdKeyName,company.getFedTaxId());
            eftpsEnrollmentCriteria = EftpsEnrollmentDetail.FedTaxIdEnc().in(fedTaxIdEncList);
            DomainEntitySet<EftpsEnrollmentDetail> eftpsEnrollmentDetails = Application.find(EftpsEnrollmentDetail.class, eftpsEnrollmentCriteria);

            for (EftpsEnrollmentDetail eftpsEnrollmentDetail : eftpsEnrollmentDetails) {
                EFTPSEnrollmentDetailsWSDTO eftpsEnrollmentDetailsWSDTO = new EFTPSEnrollmentDetailsWSDTO();

                eftpsEnrollmentDetailsWSDTO.fedTaxId = eftpsEnrollmentDetail.getFedTaxId();
                eftpsEnrollmentDetailsWSDTO.legalName = eftpsEnrollmentDetail.getLegalName();
                eftpsEnrollmentDetailsWSDTO.legalZip = eftpsEnrollmentDetail.getLegalZip();
                eftpsEnrollmentDetailsWSDTO.rejectCd = eftpsEnrollmentDetail.getRejectCd();
                eftpsEnrollmentDetailsWSDTO.rejectReason = eftpsEnrollmentDetail.getRejectReason();
                eftpsEnrollmentDetailsWSDTO.statusCd =
                        eftpsEnrollmentDetail.getStatusCd() != null ? eftpsEnrollmentDetail.getStatusCd().toString() : "";

                eftpsEnrollmentDetailsWSDTO.responseDate =
                        eftpsEnrollmentDetail.getResponseDate() != null ? eftpsEnrollmentDetail.getResponseDate().toString() : "";

                eftpsEnrollmentDetailsWSDTO.statusEffectiveDate =
                        eftpsEnrollmentDetail.getStatusEffectiveDate() != null ? eftpsEnrollmentDetail.getStatusEffectiveDate().toString() : "";

                eftpsEnrollmentDetailsWSDTOs.add(eftpsEnrollmentDetailsWSDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsEnrollmentDetailsWSDTOs;
    }

    @WebMethod
    public List<EFTPSPaymentDetailsWSDTO> queryEFTPSPaymentDetails(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                                   @WebParam(name = "sourceCompanyID") String sourceCompanyID) {
        if (StringUtils.isEmpty(sourceSystemCD)) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (StringUtils.isEmpty(sourceCompanyID)) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        List<EFTPSPaymentDetailsWSDTO> detailWSDTOs = new ArrayList<EFTPSPaymentDetailsWSDTO>();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (company == null) {
                throw new RuntimeException(String.format("Company %s:%s does not exist.", sourceSystemCD, sourceCompanyID));
            }

            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EftpsPaymentDetail.FedTaxIdKeyName,company.getFedTaxId());
            Expression<EftpsPaymentDetail> where =  EftpsPaymentDetail.FedTaxIdEnc().in(fedTaxIdEncList);
            DomainEntitySet<EftpsPaymentDetail> detailSet = Application.find(EftpsPaymentDetail.class, where);

            for (EftpsPaymentDetail detail : detailSet) {
                EFTPSPaymentDetailsWSDTO detailWSDTO = new EFTPSPaymentDetailsWSDTO();

                detailWSDTO.agencyPaymentId = asString(detail.getAgencyPaymentId());
                detailWSDTO.eftTransactionId = asString(detail.getEftTransactionId());
                detailWSDTO.fedTaxId = asString(detail.getFedTaxId());
                detailWSDTO.groupId = asString(detail.getGroupId());
                detailWSDTO.paymentAmount = asString(detail.getPaymentAmount());
                detailWSDTO.paymentDetails = asString(detail.getPaymentDetails());
                detailWSDTO.paymentDueDate = asString(detail.getPaymentDueDate());
                detailWSDTO.paymentInitiationDate = asString(detail.getPaymentInitiationDate());
                detailWSDTO.periodEndDate = asString(detail.getPeriodEndDate());
                detailWSDTO.reason = asString(detail.getReason());
                detailWSDTO.rejectCd = asString(detail.getRejectCd());
                detailWSDTO.responseDate = asString(detail.getResponseDate());
                detailWSDTO.returnCd = asString(detail.getReturnCd());
                detailWSDTO.sameDayAckNumber = asString(detail.getSameDayAckNumber());
                detailWSDTO.statusCd = asString(detail.getStatusCd());
                detailWSDTO.statusEffectiveDate = asString(detail.getStatusEffectiveDate());
                detailWSDTO.taxTypeCode = asString(detail.getTaxTypeCode());
                detailWSDTO.transactionId = asString(detail.getTransactionId());
                detailWSDTO.transactionSetId = asString(detail.getTransactionSetId());

                detailWSDTOs.add(detailWSDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return detailWSDTOs;
    }

    @WebMethod
    public String clearFilesInFolder(@WebParam(name = "FolderName") String pPath) {
        if (StringUtils.isEmpty(pPath)) {
            pPath = EftpsUtil.getWorkDir();
        }

        File[] fileList = new File(pPath).listFiles(new FileFilter() {
            public boolean accept(File pFile) {
                return !pFile.isDirectory() && !pFile.isHidden();
            }
        });

        for (File file : fileList) {
            file.delete();
        }

        return "Deleted all files from folder: " + pPath;
    }

    private String asString(Integer pValue) {
        return (pValue == null) ? "" : pValue.toString();
    }

    private String asString(String pValue) {
        return (pValue == null) ? "" : pValue;
    }

    private String asString(SpcfCalendar pValue) {
        return (pValue == null) ? "" : pValue.format("MM/dd/yyyy");
    }

    private String asString(TaxPaymentStatus pValue) {
        return (pValue == null) ? "" : pValue.toString();
    }

    private String asString(ACHReturnReason pValue) {
        return (pValue == null) ? "" : pValue.toString();
    }

    private String asString(SpcfMoney pValue) {
        return (pValue == null) ? "" : pValue.toString();
    }

    private void createPaymentResponseFiles(String pFileName,
                                            String pOutputFileFolderName,
                                            List<RejectionInfoWSDTO> pRejectionInfos,
                                            ProcessedFileInfoWSDTO pProcessedFileInfoWSDTO,
                                            List<ReturnSegmentInfoWSDTO> pReturnSegmentInfoWSDTOs) throws IOException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        try {
            PayrollServices.beginUnitOfWork();

            PaymentFileReadWrite paymentFileReadWrite = new PaymentFileReadWrite(pOutputFileFolderName);

            paymentFileReadWrite.setFileName(pFileName);
            paymentFileReadWrite.setOutboundDir(pOutputFileFolderName);

            List<RejectionInfo> rejectionInfos = new ArrayList<RejectionInfo>();

            for (RejectionInfoWSDTO pRejectionInfo : pRejectionInfos) {
                rejectionInfos.add(new RejectionInfo(pRejectionInfo.id, pRejectionInfo.code));
            }

            paymentFileReadWrite.setRejectionInfos(rejectionInfos);

            // Return segment Infos if any
            List<ReturnSegInfo> returnInfos = new ArrayList<ReturnSegInfo>();

            if (pReturnSegmentInfoWSDTOs != null) {
                for (ReturnSegmentInfoWSDTO returnSegmentInfoWSDTO : pReturnSegmentInfoWSDTOs) {
                    returnInfos.add(new ReturnSegInfo(returnSegmentInfoWSDTO.errorCode,
                                                      returnSegmentInfoWSDTO.returnSegId,
                                                      returnSegmentInfoWSDTO.returnSegErrorCode));
                }
            }

            paymentFileReadWrite.setReturnInfos(returnInfos);
            paymentFileReadWrite.read();

            PayrollServices.commitUnitOfWork();

            pProcessedFileInfoWSDTO.ackFileName = paymentFileReadWrite.getAckFileName();
            pProcessedFileInfoWSDTO.responseFileName = paymentFileReadWrite.getPaymentResponseFileName();
            pProcessedFileInfoWSDTO.returnFileName = paymentFileReadWrite.getPaymentReturnFileName();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void creteEnrollmentResponseFiles(String pFileName,
                                              String pOutputFileFolderName,
                                              List<RejectionInfoWSDTO> pRejectionInfos,
                                              ProcessedFileInfoWSDTO pProcessedFileInfoWSDTO) throws IOException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        try {
            PayrollServices.beginUnitOfWork();

            EnrollmentFileReadWrite enrollmentFileReadWrite = new EnrollmentFileReadWrite(pOutputFileFolderName);

            enrollmentFileReadWrite.setFileName(pFileName);
            enrollmentFileReadWrite.setOutboundDir(pOutputFileFolderName);

            List<RejectionInfo> rejectionInfos = new ArrayList<RejectionInfo>();

            if (pRejectionInfos != null && pRejectionInfos.size() > 0)
            {
                for (RejectionInfoWSDTO pRejectionInfo : pRejectionInfos) {
                    rejectionInfos.add(new RejectionInfo(pRejectionInfo.id, pRejectionInfo.code));
                }

                enrollmentFileReadWrite.setRejectionInfos(rejectionInfos);
            }
            enrollmentFileReadWrite.read();

            PayrollServices.commitUnitOfWork();

            pProcessedFileInfoWSDTO.ackFileName = enrollmentFileReadWrite.getAckFileName();
            pProcessedFileInfoWSDTO.responseFileName = enrollmentFileReadWrite.getEnrollmentResponseFileName();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void processEnrollmentResponseFile(String pFileName, String pOutputFileFolderName,
                                               ProcessedFileInfoWSDTO pProcessedFileInfoWSDTO) throws IOException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        try {
            PayrollServices.beginUnitOfWork();

            EnrollmentResponseFileReadWrite enrollmentResponseFileReadWrite = new EnrollmentResponseFileReadWrite(null);

            enrollmentResponseFileReadWrite.setFileName(pFileName);
            enrollmentResponseFileReadWrite.setOutboundDir(pOutputFileFolderName);
            enrollmentResponseFileReadWrite.read();

            pProcessedFileInfoWSDTO.ackFileName = enrollmentResponseFileReadWrite.getAckFileName();

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /* ************************************************************************************************************ */
    /* ************************************************************************************************************ */
    /* ************************************************************************************************************ */
    /* ************************************************************************************************************ */
    /* ************************************************************************************************************ */

    @WebMethod
    public ProcessedFileInfoWSDTO processFile(@WebParam(name = "inputFileFolderName") String inputFileFolderName,
                                              @WebParam(name = "fileName") String inputFileName,
                                              @WebParam(name = "outputFilesFolderName") String outputFileFolderName,
                                              @WebParam(name = "RejectionInfoWSDTO") List<RejectionInfoWSDTO> pRejectionInfos) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        if (StringUtils.isEmpty(inputFileName) || inputFileName.contains("/") || inputFileName.contains("\\")) {
            throw new RuntimeException("Invalid Input File Name");
        }

        if (StringUtils.isEmpty(inputFileFolderName)) {
            inputFileFolderName = EftpsUtil.getWorkDir();
        }

        if (StringUtils.isEmpty(outputFileFolderName)) {
            outputFileFolderName = EftpsUtil.getWorkDir();
        }

        File inputFile = new File(inputFileFolderName, inputFileName);

        if (inputFile == null) {
            throw new RuntimeException("Input File does not exist in:" + inputFileFolderName);
        }

        ProcessedFileInfoWSDTO processedFileInfoWSDTO = new ProcessedFileInfoWSDTO();
        GenericEdiFile genericEdiFile = new GenericEdiFile();

        genericEdiFile.setFileName(inputFile.getPath());
        genericEdiFile.read();

        switch (genericEdiFile.getEdiFileType()) {
            case EDI151:
                break;
            case EDI813:
                createPaymentResponseFiles(inputFile.getPath(), outputFileFolderName, pRejectionInfos, processedFileInfoWSDTO, null);
                break;
            case EDI821:
                break;
            case EDI824:
                processEnrollmentResponseFile(inputFile.getPath(), outputFileFolderName, processedFileInfoWSDTO);
                break;
            case EDI826:
                break;
            case EDI827:
                break;
            case EDI838:
                creteEnrollmentResponseFiles(inputFile.getPath(), outputFileFolderName, pRejectionInfos, processedFileInfoWSDTO);
                break;
            case EDI997:
                //Not required to process further
        }

        processedFileInfoWSDTO.inputFileType = genericEdiFile.getEftpsFileType().toString();

        return processedFileInfoWSDTO;
    }

    @WebMethod
    public ProcessedFileInfoWSDTO ProcessPaymentFile(@WebParam(name = "inputFileFolderName") String inputFileFolderName,
                                                     @WebParam(name = "fileName") String inputFileName,
                                                     @WebParam(name = "outputFilesFolderName") String outputFilesFolderName,
                                                     @WebParam(name = "RejectionInfo") List<RejectionInfoWSDTO> pRejectionInfos,
                                                     @WebParam(name = "ReturnSegmentInfo") List<ReturnSegmentInfoWSDTO> pReturnInfos) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        if (StringUtils.isEmpty(inputFileName) || inputFileName.contains("/") || inputFileName.contains("\\")) {
            throw new RuntimeException("Invalid Input File Name");
        }

        if (StringUtils.isEmpty(inputFileFolderName)) {
            inputFileFolderName = EftpsUtil.getWorkDir();
        }

        if (StringUtils.isEmpty(outputFilesFolderName)) {
            outputFilesFolderName = EftpsUtil.getWorkDir();
        }

        ProcessedFileInfoWSDTO processedFileInfoWSDTO = new ProcessedFileInfoWSDTO();
        GenericEdiFile genericEdiFile = new GenericEdiFile();
        File inputFile = new File(inputFileFolderName, inputFileName);

        genericEdiFile.setFileName(inputFile.getPath());
        genericEdiFile.read();

        processedFileInfoWSDTO.inputFileType = genericEdiFile.getEftpsFileType().toString();

        createPaymentResponseFiles(inputFile.getPath(), outputFilesFolderName, pRejectionInfos, processedFileInfoWSDTO, pReturnInfos);

        return processedFileInfoWSDTO;
    }

    @WebMethod
    public ProcessedFileInfoWSDTO processFileAndFTP(@WebParam(name = "inputFileFolderName") String inputFileFolderName,
                                                    @WebParam(name = "fileName") String inputFileName,
                                                    @WebParam(name = "outputFilesFolderName") String outputFileFolderName,
                                                    @WebParam(name = "sftpHostName") String ftpHostName,
                                                    @WebParam(name = "sftpServerUserName") String ftpServerUserName,
                                                    @WebParam(name = "sftpServerPass") String ftpServerPass,
                                                    @WebParam(name = "RejectionInfoWSDTO") List<RejectionInfoWSDTO> pRejectionInfos) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        ProcessedFileInfoWSDTO processedFileInfoWSDTO = processFile(inputFileFolderName, inputFileName, outputFileFolderName, pRejectionInfos);

        if (StringUtils.isEmpty(outputFileFolderName)) {
            outputFileFolderName = EftpsUtil.getWorkDir();
        }

        if (StringUtils.isEmpty(ftpHostName)) {
            ftpHostName = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_eftps_ftp_server");
        }

        if (StringUtils.isEmpty(ftpServerUserName)) {
            ftpServerUserName = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_eftps_ftp_username");
        }

        if (StringUtils.isEmpty(ftpServerPass)) {
            ftpServerPass = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_eftps_ftp_password");
        }

        File inputFile = new File(inputFileFolderName, inputFileName);
        Transporter sftp = BatchUtils.getJschConnection(ftpHostName, ftpServerUserName, ftpServerPass, null, false);

        try {
            sftp.connect();
            sftp.changeRemoteDir(EftpsUtil.getWorkDir());
            sftp.uploadFile(processedFileInfoWSDTO.responseFileName);
            sftp.uploadFile(processedFileInfoWSDTO.ackFileName);
            sftp.changeRemoteDir(EftpsUtil.getArchiveDir());
            sftp.uploadFile(inputFile.getPath());
            inputFile.delete();
        } finally {
            sftp.disconnect();
        }

        return processedFileInfoWSDTO;
    }

    @WebMethod
    public String updateEftpsFileStatus(@WebParam(name = "eftpsFileName") String eftpsFileName,
                                        @WebParam(name = "fileStatus") EdiFileStatus fileStatus) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        if (StringUtils.isEmpty(eftpsFileName)) {
            throw new RuntimeException("Invalid EFTPS File Name");
        }

        if (fileStatus == null) {
            EdiFileStatus[] fileStatuses = EdiFileStatus.values();
            List<String> statusNames = new ArrayList<String>();

            for (EdiFileStatus status : fileStatuses) {
                statusNames.add(status.name());
            }

            throw new RuntimeException("Pass valid EFTPS file status one of these:" + statusNames);
        }

        try {
            PayrollServices.beginUnitOfWork();

            Criterion<EdiTaxFile> eftpsFileCriteria = EdiTaxFile.FileName().equalTo(eftpsFileName);
            DomainEntitySet<EftpsFile> eftpsFiles = Application.find(EftpsFile.class, eftpsFileCriteria);

            if (eftpsFiles == null || eftpsFiles.size() == 0) {
                throw new RuntimeException("Eftps file does not exist for the file name:" + eftpsFileName);
            }

            EftpsFile eftpsFile = eftpsFiles.get(0);

            eftpsFile.setStatusCd(fileStatus);

            Application.save(eftpsFile);

            PayrollServices.commitUnitOfWork();

            return "Successfully updated Eftps file:" + eftpsFileName + " Status to:" + fileStatus;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * EFTPS enrolls a company
     *
     * @param psourceSystemCD
     * Source System Code of the company
     * @param pCompanyId
     * Source Company Id (PSID) of the company
     * @return
     * A string describing success or failure of the operation
     * @throws Exception
     */
    @WebMethod
    public String enrollACompany(@WebParam(name = "sourceSystemCD") String psourceSystemCD,
                                 @WebParam(name = "sourceCompanyID") String pCompanyId)  throws Exception{
        return processPendingEnrollments(pCompanyId, psourceSystemCD, null);
    }

    /**
     * Rejects a company's EFTPS Enrollment
     *
     * @param pSourceSystemCD
     * Source System Code of the company
     * @param pCompanyId
     * Source Company Id (PSID) of the company
     * @param pRejectionCode
     * Rejection code used to reject the EFTPS enrollment. Could be 5,6 or 13 (check EftpsBpConstants for valid values).
     * @return
     * A string describing success or failure of the operation
     * @throws Exception
     */
    @WebMethod
    public String rejectACompany(@WebParam(name = "sourceSystemCD") String pSourceSystemCD,
                                 @WebParam(name = "sourceCompanyID") String pCompanyId,
                                 @WebParam(name = "rejectionCode") String pRejectionCode) throws Exception {
        return processPendingEnrollments(pCompanyId, pSourceSystemCD, pRejectionCode);
    }

    public String processPendingEnrollments(String pCompanyId, String psourceSystemCD, String pRejectionCode) throws Exception{
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        StopWatch timer = StopWatch.startTimer();

        DomainEntitySet<EftpsEnrollment> pendingEnrollments;
        String returnFileName = "";

        if (StringUtils.isEmpty(psourceSystemCD)) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (StringUtils.isEmpty(pCompanyId)) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            EnrollmentFile file_838;
            Application.beginUnitOfWork();
            /*  Step 1 - Process Enrollments   */
            pendingEnrollments = getPendingEftpsEnrollmentsForACompany(pCompanyId);

            if (pendingEnrollments.isEmpty()) {
                throw new RuntimeException("There are no EFTPS Enrollments to be processed.");
            }
            else {
                try {
                    file_838 = new EnrollmentFile();

                    try {
                        if (file_838.write(pendingEnrollments) > 0) {
                            StringBuffer sb = new StringBuffer();

                            sb.append("EFTPS Enrollment File processing stats (enrollment file created):").append(EftpsUtil.NEWLINE);
                            sb.append("> File Name                        : ").append(file_838.getDetailedFileName()).append(EftpsUtil.NEWLINE);
                            sb.append("> Selected Enrollment Count        : ").append(pendingEnrollments.size()).append(EftpsUtil.NEWLINE);
                            sb.append("> Processed Enrollment Count       : ").append(file_838.getRecordCount()).append(EftpsUtil.NEWLINE);
                            sb.append("> Successful Enrollments (in file) : ").append(file_838.getSuccessfulEnrollmentCount()).append(EftpsUtil.NEWLINE);
                            sb.append("> Invalid Enrollments (not in file): ").append(file_838.getInvalidEnrollmentCount()).append(EftpsUtil.NEWLINE);

                            System.out.println(sb.toString());

                            returnFileName = file_838.getFileName();
                        }
                        else {
                            StringBuffer sb = new StringBuffer();

                            sb.append("EFTPS Enrollment File processing stats (enrollment file not created):").append(EftpsUtil.NEWLINE);
                            sb.append("> Selected Enrollment Count        : ").append(pendingEnrollments.size()).append(EftpsUtil.NEWLINE);
                            sb.append("> Processed Enrollment Count       : ").append(file_838.getRecordCount()).append(EftpsUtil.NEWLINE);
                            sb.append("> Successful Enrollments (in file) : ").append(file_838.getSuccessfulEnrollmentCount()).append(EftpsUtil.NEWLINE);
                            sb.append("> Invalid Enrollments (not in file): ").append(file_838.getInvalidEnrollmentCount()).append(EftpsUtil.NEWLINE);

                            System.out.println(sb.toString());
                        }
                    }
                    catch (Throwable t) {
                        file_838.cleanup();
                        throw t;
                    }
                }
                catch (Throwable t) {
                    throw new RuntimeException("Error creating EFTPS Enrollment file. ", t);
                }
            }
            Application.commitUnitOfWork();
            /*  Now that the enrollment file has been created for the company, rest of the process remains the same */
            /*  Step 2 - Induce Errors into enrollment */
            int lastPos = returnFileName.lastIndexOf("\\");
            if (lastPos == -1) {
                throw new RuntimeException("Invalid 838 file name. Possibly, path not included.");
            }

            List<RejectionInfoWSDTO> rejectionInfoDTOs = null;
            if (pRejectionCode != null && pRejectionCode.trim().length() > 0) {
                List<RejectionInfo> rejectionInfo = EftpsDataLoader.induceEnrollmentRejectInfo(returnFileName, pRejectionCode);
                rejectionInfoDTOs = new ArrayList<RejectionInfoWSDTO>();
                for (RejectionInfo info : rejectionInfo) {
                    RejectionInfoWSDTO infoDTO = new RejectionInfoWSDTO();
                    infoDTO.id = info.getId();
                    infoDTO.code = pRejectionCode;
                    rejectionInfoDTOs.add(infoDTO);
                }
            }
            /*  Step 3 - Process File   */
            ProcessedFileInfoWSDTO processedFileInfo = processFile(returnFileName.substring(0, lastPos), returnFileName.substring(lastPos + 1), null, rejectionInfoDTOs);
            /*  Step 4 - Process Pending Xmissions  */
            processPendingTransmissions(false, false);
            /*  Step 5 - Create Response    */
            createResponse(processedFileInfo.responseFileName, null);
        } finally {
            Application.rollbackUnitOfWork();
        }
        return String.format("Successfully created and processed %s in %s ms", returnFileName,timer.getElapsedTimeString());
    }


    public static DomainEntitySet<EftpsEnrollment> getPendingEftpsEnrollmentsForACompany(String pCompanyId) {
        Company company = Company.findCompany(pCompanyId, SourceSystemCode.QBDT);

        Expression<EftpsEnrollment> query = new Query<EftpsEnrollment>()
                .Where(EftpsEnrollment.StatusCd().equalTo(EftpsEnrollmentStatus.PendingEnrollment)
                        .And(EftpsEnrollment.CompanyAgency().Company().equalTo(company)))
                .OrderBy(EftpsEnrollment.CreatedDate());

        return Application.find(EftpsEnrollment.class, query);
    }

    public static DomainEntitySet<EftpsFile> getEftpsFilesByName(String pFileName) {
        Expression<EftpsFile> query = new Query<EftpsFile>()
                .Where(EftpsFile.FileName().equalTo(pFileName));
        return Application.find(EftpsFile.class, query);
    }

    /**
     * Returns a company's payment with the return code
     *
     * @param pCompanyId
     * PSID of the company whose EFTPS payments need to be returned
     * @param pDueDate
     * Due date of the payment as outlined in the MMT
     * @param pReturnCode
     * Return code used to return the payment(s). Should be C01 or R01
     * @return
     * A string describing failure or success of the operation
     * @throws Exception
     */
    @WebMethod
    public String returnCompanyPayments(@WebParam(name = "sourceCompanyID") String pCompanyId,
                                        @WebParam(name = "dueDate") String pDueDate,
                                        @WebParam(name = "returnCode") String pReturnCode) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        if (StringUtils.isEmpty(pCompanyId)) {
            throw new RuntimeException("sourceCompanyID is required.");
        }

        if (StringUtils.isEmpty(pDueDate)) {
                   throw new RuntimeException("Payment due date is required.");
 }

        if (StringUtils.isEmpty(pReturnCode) || !(pReturnCode.trim().equals("R01") || pReturnCode.trim().equals("C01"))) {
            throw new RuntimeException("Payment return code(R01/C01) is required.");
        }

        return returnAllPayments(pCompanyId, CalendarUtils.convertToSpcfCalendar(new Date(pDueDate)), pReturnCode.equals("C01"));
    }

    public String returnAllPayments(String pPsid, SpcfCalendar pDueDate, Boolean pIsNOCReturn) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        System.out.println("Begin processing EFTPS Payments.");

        StopWatch timer = StopWatch.startTimer();
        int maxRecords = EftpsUtil.getMaxAllowedPaymentsPerFile();
        List<Integer> fileIdList = new Vector<Integer>();
        DomainEntitySet<MoneyMovementTransaction> pendingPayments;
        boolean processAbandonedTransactions;

        do {
        try {
            Application.beginUnitOfWork();
                PSPDate.setPSPTime(pDueDate);
                pendingPayments = getPendingCompanyTaxPaymentsForDate(PaymentMethod.EFTPSDirectDebit, pDueDate, maxRecords,pPsid);
                processAbandonedTransactions = false;

                if (pendingPayments.isEmpty()) {
                throw new RuntimeException("There are no EFTPS Payments to be processed.");
                } else {
                    System.out.println(String.format("Generating EFTPS Payments file for %d pending payments.",
                                              pendingPayments.size()));
                    try {
                        PaymentFile file = new PaymentFile(PaymentFile.PaymentFileMode.PFM_100K);

                        try {
                            if (file.write(pendingPayments) > 0) {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EFTPS Payment File processing stats (payment file created):").append(EftpsUtil.NEWLINE);
                                sb.append("> File Name                     : ").append(file.getDetailedFileName()).append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Payment Count        : ").append(pendingPayments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Payment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Payments (in file) : ").append(file.getSuccessfulPaymentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Skipped Payments (not in file): ").append(file.getSkippedPaymentCount()).append(EftpsUtil.NEWLINE);

                                System.out.println(sb.toString());

                                //
                                // Save the file id for later notification email
                                //
                                fileIdList.add(file.getFileControlNumber());
                            } else {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EFTPS Payment File processing stats (payment file not created):").append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Payment Count        : ").append(pendingPayments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Payment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Payments (in file) : ").append(file.getSuccessfulPaymentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Skipped Payments (not in file): ").append(file.getSkippedPaymentCount()).append(EftpsUtil.NEWLINE);

                                System.out.println(sb.toString());
            }

                            //
                            // We need to check to make sure all the payments in this result set were processed
                            // (some could have been abandoned due to EFTPS file spec constraints - i.e. max segments reached)
                            // ('skipped' records count as processed records - they were just disqualified from the file.)
                            //
                            // If any payments were abandoned, we want to go around again to pick them up after this batch commits
                            //
                            processAbandonedTransactions = !file.allPaymentsProcessed();
            } catch (Throwable t) {
                            file.cleanup();
                            throw t;
                        }
                    } catch (Throwable t) {
                throw new RuntimeException("Error creating EFTPS Payment file. ", t);
            }
                }
                Application.commitUnitOfWork();

            processPendingTransmissions(false, false);
            EftpsDataLoader.callSimulator(false,true,pIsNOCReturn);
            processWaitingResponseFiles(false, false);
        } finally {
            Application.rollbackUnitOfWork();
        }
        } while (!(pendingPayments.size() < maxRecords) || processAbandonedTransactions);


        return String.format("End processing EFTPS Payments in %s.", timer.getElapsedTimeString());
    }

    public static DomainEntitySet<MoneyMovementTransaction> getPendingCompanyTaxPaymentsForDate(PaymentMethod pPaymentMethod,
                                                                                         SpcfCalendar pDate,
                                                                                         int pMaxRows,String pPsid) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TFASimulatorBatchJob);

        //
        // Date range should be (for example date 01/20/2011): '01/20/2011 00:00:00.000' and '01/20/2011 23:59:59.999'
        //

        SpcfCalendar day = pDate.copy();
        CalendarUtils.clearTime(day); // clear time from date.

        SpcfCalendar nextDay = day.copy();
        nextDay.addDays(1);
        nextDay.addMilliseconds(-1);

        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)
                        .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created))
                        .And(MoneyMovementTransaction.InitiationDate().between(day, nextDay))
                        .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(pPaymentMethod))
                        .And(MoneyMovementTransaction.Company().SourceCompanyId().equalTo(pPsid))
                )
                .OrderBy(MoneyMovementTransaction.CreatedDate())
                .LimitResults(0, pMaxRows);

        return Application.find(MoneyMovementTransaction.class, query);
}
}
