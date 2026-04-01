package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.eftps.EdiResponseFileTxnDetails;
import com.intuit.sbd.payroll.psp.agency.eftps.VANSimulator;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EDIPaymentDetailWSDTO;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EFTPSFileWSDTO;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EdiPaymentResponseWSDTO;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EftpsFileRecordWSDTO;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.ops.eftpsBp.EdiEftpsRecordList;
import com.paycycle.ops.eftpsBp.StateEDIFileReader;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 21, 2011
 * Time: 4:30:44 PM
 * To change this template use File | Settings | File Templates.
 */
@WebService
public class EDIPaymentsWS {

    @WebMethod()
    public Collection<EftpsFileRecordWSDTO> QueryStateEDIFiles() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        ArrayList<EftpsFileRecordWSDTO> ediFileRecordWSDTOs = new ArrayList<EftpsFileRecordWSDTO>();

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<StateEdiTaxFile> stateEdiTaxFiles = Application.find(StateEdiTaxFile.class);
            for (StateEdiTaxFile stateEdiTaxFile : stateEdiTaxFiles) {
                EftpsFileRecordWSDTO eftpsFileRecordWSDTO = new EftpsFileRecordWSDTO();
                eftpsFileRecordWSDTO.statusCode = stateEdiTaxFile.getStatusCd().toString();
                eftpsFileRecordWSDTO.fileName = stateEdiTaxFile.getFileName();
                eftpsFileRecordWSDTO.fileCode = String.valueOf(stateEdiTaxFile.getFileCode());
                eftpsFileRecordWSDTO.fileId = String.valueOf(stateEdiTaxFile.getFileId());
                eftpsFileRecordWSDTO.fileType = stateEdiTaxFile.getFileType().toString(); 
                eftpsFileRecordWSDTO.systemOwner = stateEdiTaxFile.getSystemOwner().toString();
                eftpsFileRecordWSDTO.statusEffectiveDate = (stateEdiTaxFile.getStatusEffectiveDate() == null) ? "" : stateEdiTaxFile.getStatusEffectiveDate().toString();
                ediFileRecordWSDTOs.add(eftpsFileRecordWSDTO);
            }
            PayrollServices.rollbackUnitOfWork();
            return ediFileRecordWSDTOs;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @WebMethod()
    public Collection<EDIPaymentDetailWSDTO> QueryEDIPaymentDetails(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                                        @WebParam(name = "sourceCompanyID") String pSourceCompanyId) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(PaymentsWS.class);
        String envId = ConfigurationManager.getEnvironmentIdentifier();

        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        logger.info("hello" + pSourceCompanyId);

        try {
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }
            Criterion<EdiPaymentDetail> where = EdiPaymentDetail.MoneyMovementTransaction().Company().equalTo(company);
            DomainEntitySet<EdiPaymentDetail> paymentDetails = Application.find(EdiPaymentDetail.class, where);

            PayrollServices.rollbackUnitOfWork();
            return buildEdiPaymentDetailDTOs(paymentDetails);
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processEDIPayments(@WebParam(name = "displayFileContents") boolean displayFileContents,
                                                @WebParam(name = "formattedContents") boolean formattedContents) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);

        BatchJobManager.runJob(BatchJobType.EdiPayment);

        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();
        try {
            PayrollServices.beginUnitOfWork();

            Expression<StateEdiTaxFile> query = new Query<StateEdiTaxFile>()
                .Where(StateEdiTaxFile.FileType().equalTo(EdiFileType.StateEdiPayment)
                        .And(StateEdiTaxFile.StatusCd().in(EdiFileStatus.PendingTransmission))).OrderBy(StateEdiTaxFile.CreatedDate());
            DomainEntitySet<StateEdiTaxFile> stateEdiTaxFiles = Application.find(StateEdiTaxFile.class, query);

            for (StateEdiTaxFile ediTaxFile : stateEdiTaxFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();
                eftpsFileWSDTO.fileName = ediTaxFile.getFileName();
                if (displayFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(ediTaxFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(formattedContents);
                }
                ediTaxFile.setStatusCd(EdiFileStatus.Archived);
                ediTaxFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                Application.save(ediTaxFile);
                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> submitEDIPaymentsOnDueDate(@WebParam(name = "displayFileContents") boolean displayFileContents,
                                                @WebParam(name = "formattedContents") boolean formattedContents,
                                                @WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                                @WebParam(name = "dueDate") String pDueDate) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);

        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }


        if (pDueDate == null || pDueDate.length() != 10) {
            throw new RuntimeException(
                    "Invalid from date format" + pDueDate + ".  Correct format: MM/dd/yyyy");
        }

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

        if(company == null) {
            throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId +" Source system code:"+ pSourceSystemCd);
        }
        SpcfCalendar dueDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
        dateFormat.setPattern("MM/dd/yyyy");
        SpcfCalendar tempDueDate = dateFormat.parse(pDueDate);
        dueDate.setValues(tempDueDate.getYear(), tempDueDate.getMonth(), tempDueDate.getDay());

        //EDI Payments based on Init Date, company
        PaymentMethod[] paymentMethods = {PaymentMethod.EDI};
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setDueDate(dueDate).setPaymentMethods(paymentMethods).setReadyToSend().find();

        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setInitiationDate(today); // Update Init date to today's date so that these will be picked by EDI Payment job and processed
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EdiPayment);

        List<EFTPSFileWSDTO> eftpsFileWSDTOs = new ArrayList<EFTPSFileWSDTO>();
        try {
            PayrollServices.beginUnitOfWork();

            Expression<StateEdiTaxFile> query = new Query<StateEdiTaxFile>()
                .Where(StateEdiTaxFile.FileType().equalTo(EdiFileType.StateEdiPayment)
                        .And(StateEdiTaxFile.StatusCd().in(EdiFileStatus.PendingTransmission))).OrderBy(StateEdiTaxFile.CreatedDate());
            DomainEntitySet<StateEdiTaxFile> stateEdiTaxFiles = Application.find(StateEdiTaxFile.class, query);

            for (StateEdiTaxFile ediTaxFile : stateEdiTaxFiles) {
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();
                eftpsFileWSDTO.fileName = ediTaxFile.getFileName();
                if (displayFileContents) {
                    EdiEftpsRecordList ediRecordList = new EdiEftpsRecordList(ediTaxFile.getFileName());
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(formattedContents);
                }
                ediTaxFile.setStatusCd(EdiFileStatus.Archived);
                ediTaxFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                Application.save(ediTaxFile);
                eftpsFileWSDTOs.add(eftpsFileWSDTO);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return eftpsFileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processEDIPaymentsGenerate997(@WebParam(name = "displayFileContents") boolean displayFileContents,
                                                @WebParam(name = "formattedContents") boolean formattedContents,
                                                @WebParam(name = "process997Files") boolean processFiles) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);
        List<EFTPSFileWSDTO> fileWSDTOs = new ArrayList<EFTPSFileWSDTO>();
        PayrollServices.beginUnitOfWork();
        
        //Generate 997 Files
        VANSimulator vanSimulator = new VANSimulator();
        try {
            DomainEntitySet<StateEdiTaxFile> stateEdiTaxFiles = Application.find(StateEdiTaxFile.class, StateEdiTaxFile.AckFile().isNull().And(StateEdiTaxFile.FileType().equalTo(EdiFileType.StateEdiPayment)));
            for (EdiTaxFile ediTaxFile : stateEdiTaxFiles) {
                //Get hibernate session
                //Execute Query
                org.hibernate.Query hibernateQuery = Application.createHibernateQuery("select distinct (ediPaymentDetail.TransactionSetId) from com.intuit.sbd.payroll.psp.domain.EdiPaymentDetail ediPaymentDetail where ediPaymentDetail.GroupId= :fileId");
                hibernateQuery.setParameter("fileId", ediTaxFile.getFileId());

                List<Integer> txnSetIds = hibernateQuery.list();

                String fileName = vanSimulator.generateAckEdiFile(EftpsUtil.getEdiVanDir(), txnSetIds, String.valueOf(ediTaxFile.getFileId()));
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();
                eftpsFileWSDTO.fileName = fileName;
                if (displayFileContents) {
                    StateEDIFileReader ediRecordList = new StateEDIFileReader(fileName);
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(formattedContents);
                }
                fileWSDTOs.add(eftpsFileWSDTO);
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        if(processFiles) {
            //Process simulator generated 151 Files.
            BatchJobManager.runJob(BatchJobType.EdiResponse);
        }

        return fileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processEDIPaymentsGenerate151(@WebParam(name = "displayFileContents") boolean displayFileContents,
                                                @WebParam(name = "formattedContents") boolean formattedContents,
                                                @WebParam(name = "paymentDetailsToReject") EdiPaymentResponseWSDTO paymentDetailsToReject,
                                                @WebParam(name = "paymentDetailsToReturn") EdiPaymentResponseWSDTO paymentDetailsToReturn,
                                                @WebParam(name = "process151Files") boolean processFiles) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);
        List<EFTPSFileWSDTO> fileWSDTOs = new ArrayList<EFTPSFileWSDTO>();
        PayrollServices.beginUnitOfWork();
        try {
            DomainEntitySet<EdiPaymentDetail> ediPaymentDetailsToReject = new DomainEntitySet<EdiPaymentDetail>();
            DomainEntitySet<EdiPaymentDetail> ediPaymentDetailsToReturn = new DomainEntitySet<EdiPaymentDetail>();

            if(paymentDetailsToReject != null) {
                ediPaymentDetailsToReject = getEdiPayments(paymentDetailsToReject);
            }

            if(paymentDetailsToReturn != null) {
                ediPaymentDetailsToReturn = getEdiPayments(paymentDetailsToReturn);
            }

            if(ediPaymentDetailsToReject.size() == 0 && ediPaymentDetailsToReturn.size() == 0) {
                return fileWSDTOs;
            }

            Set<Integer> groupIdSet = new HashSet<Integer>(); // 813 File Ids to create corresponding 151 file
            for (EdiPaymentDetail ediPaymentDetail : ediPaymentDetailsToReturn) {
                groupIdSet.add(ediPaymentDetail.getGroupId());
            }
            for (EdiPaymentDetail ediPaymentDetail : ediPaymentDetailsToReject) {
                groupIdSet.add(ediPaymentDetail.getGroupId());
            }

            //Generate 151 Files for 813 Files
            VANSimulator vanSimulator = new VANSimulator();
            for (Integer groupId : groupIdSet) {
                DomainEntitySet<EdiPaymentDetail> ediPaymentDetails = Application.find(EdiPaymentDetail.class, EdiPaymentDetail.GroupId().equalTo(groupId)
                        .And(EdiPaymentDetail.StatusCd().equalTo(TaxPaymentStatus.SentToAgency)));
                List<EdiResponseFileTxnDetails> txnDetails = new ArrayList<EdiResponseFileTxnDetails>();
                for (EdiPaymentDetail ediPaymentDetail : ediPaymentDetails) {
                    EdiResponseFileTxnDetails ediResponseFileTxnDetails = new EdiResponseFileTxnDetails();
                    ediResponseFileTxnDetails.setTxnId(ediPaymentDetail.getTransactionId());
                    ediResponseFileTxnDetails.setTxnSetId(ediPaymentDetail.getTransactionSetId());
                    if(ediPaymentDetailsToReject.contains(ediPaymentDetail)) {
                        ediResponseFileTxnDetails.setErrorCd(paymentDetailsToReject.errorCd);
                        ediResponseFileTxnDetails.setMessage(paymentDetailsToReject.errorMessage);
                        ediResponseFileTxnDetails.setActionCode("U");
                    } else if(ediPaymentDetailsToReturn.contains(ediPaymentDetail)) {
                        ediResponseFileTxnDetails.setErrorCd(paymentDetailsToReturn.errorCd);
                        ediResponseFileTxnDetails.setMessage(paymentDetailsToReturn.errorMessage);
                        ediResponseFileTxnDetails.setActionCode("WQ");
                    } else {
                        ediResponseFileTxnDetails.setErrorCd("000000");
                        ediResponseFileTxnDetails.setMessage("Confirmation");
                        ediResponseFileTxnDetails.setActionCode("CF");
                    }
                    txnDetails.add(ediResponseFileTxnDetails);
                }
                String fileName = vanSimulator.generatePaymentResponseEdiFile(EftpsUtil.getEdiVanDir(), ediPaymentDetails.get(0).getGroupId(), ediPaymentDetails.get(0).getGroupTransactionTime(), txnDetails);
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();
                eftpsFileWSDTO.fileName = fileName;
                if (displayFileContents) {
                    StateEDIFileReader ediRecordList = new StateEDIFileReader(fileName);
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(formattedContents);
                }
                fileWSDTOs.add(eftpsFileWSDTO);
            }
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }

        if(processFiles) {
            //Process simulator generated 151 Files.
            BatchJobManager.runJob(BatchJobType.EdiResponse);            
        }

        return fileWSDTOs;
    }

    @WebMethod
    public List<EFTPSFileWSDTO> processEDIPaymentsToConfirm(@WebParam(name = "displayFileContents") boolean displayFileContents,
                                                @WebParam(name = "formattedContents") boolean formattedContents,
                                                @WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                                @WebParam(name = "dueDate") String pDueDate,
                                                @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                                @WebParam(name = "process151Files") boolean processFiles) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);
        
        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }


        if (pDueDate == null || pDueDate.length() != 10) {
            throw new RuntimeException(
                    "Invalid from date format" + pDueDate + ".  Correct format: MM/dd/yyyy");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.trim().length() == 0) {
            throw new RuntimeException("No paymentTemplateCd is specified");
        }

        List<EFTPSFileWSDTO> fileWSDTOs = new ArrayList<EFTPSFileWSDTO>();
        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            if(company == null) {
                throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId +" Source system code:"+ pSourceSystemCd);
            }

            SpcfCalendar dueDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar tempDueDate = dateFormat.parse(pDueDate);
            dueDate.setValues(tempDueDate.getYear(), tempDueDate.getMonth(), tempDueDate.getDay());

            DomainEntitySet<EdiPaymentDetail> ediPaymentDetails = Application.find(EdiPaymentDetail.class,  EdiPaymentDetail.MoneyMovementTransaction().Company().equalTo(company)
                        .And(EdiPaymentDetail.MoneyMovementTransaction().PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd)))
                        .And(EdiPaymentDetail.PaymentDueDate().equalTo(dueDate)).And(EdiPaymentDetail.StatusCd().equalTo(TaxPaymentStatus.SentToAgency)));

            if(ediPaymentDetails.size() == 0) {
                return fileWSDTOs;
            }

            Set<Integer> groupIdSet = new HashSet<Integer>(); // 813 File Ids to create corresponding 151 file
            for (EdiPaymentDetail ediPaymentDetail : ediPaymentDetails) {
                groupIdSet.add(ediPaymentDetail.getGroupId());
            }

            //Generate 151 Files for 813 Files
            VANSimulator vanSimulator = new VANSimulator();
            for (Integer groupId : groupIdSet) {
                ediPaymentDetails = Application.find(EdiPaymentDetail.class, EdiPaymentDetail.GroupId().equalTo(groupId)
                        .And(EdiPaymentDetail.StatusCd().equalTo(TaxPaymentStatus.SentToAgency)));
                List<EdiResponseFileTxnDetails> txnDetails = new ArrayList<EdiResponseFileTxnDetails>();
                for (EdiPaymentDetail ediPaymentDetail : ediPaymentDetails) {
                    EdiResponseFileTxnDetails ediResponseFileTxnDetails = new EdiResponseFileTxnDetails();
                    ediResponseFileTxnDetails.setTxnId(ediPaymentDetail.getTransactionId());
                    ediResponseFileTxnDetails.setTxnSetId(ediPaymentDetail.getTransactionSetId());
                    ediResponseFileTxnDetails.setErrorCd("000000");
                    ediResponseFileTxnDetails.setMessage("Confirmation");
                    ediResponseFileTxnDetails.setActionCode("CF");
                    txnDetails.add(ediResponseFileTxnDetails);
                }
                String fileName = vanSimulator.generatePaymentResponseEdiFile(EftpsUtil.getEdiVanDir(), ediPaymentDetails.get(0).getGroupId(), ediPaymentDetails.get(0).getGroupTransactionTime(), txnDetails);
                EFTPSFileWSDTO eftpsFileWSDTO = new EFTPSFileWSDTO();
                eftpsFileWSDTO.fileName = fileName;
                if (displayFileContents) {
                    StateEDIFileReader ediRecordList = new StateEDIFileReader(fileName);
                    eftpsFileWSDTO.fileContent = ediRecordList.toString(formattedContents);
                }
                fileWSDTOs.add(eftpsFileWSDTO);
            }
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }

        if(processFiles) {
            //Process simulator generated 151 Files.
            BatchJobManager.runJob(BatchJobType.EdiResponse);
        }

        return fileWSDTOs;
    }

    private DomainEntitySet<EdiPaymentDetail> getEdiPayments(EdiPaymentResponseWSDTO paymentResponseWSDTO) {
        DomainEntitySet<EdiPaymentDetail> ediPaymentDetails = new DomainEntitySet<EdiPaymentDetail>();
        if(paymentResponseWSDTO != null) {
            if (paymentResponseWSDTO.sourceSystemCD == null || paymentResponseWSDTO.sourceSystemCD.trim().length() == 0) {
                throw new RuntimeException("No sourceSystemCD is specified");
            }

            if (paymentResponseWSDTO.pspCompanyID == null || paymentResponseWSDTO.pspCompanyID.trim().length() == 0) {
                throw new RuntimeException("No sourceCompanyID is specified");
            }

            if (paymentResponseWSDTO.paymentTemplateCd == null || paymentResponseWSDTO.paymentTemplateCd.trim().length() == 0) {
                throw new RuntimeException("No paymentTemplateCd is specified");
            }

            if (paymentResponseWSDTO.paymentDueDate == null || paymentResponseWSDTO.paymentDueDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid from date format" + paymentResponseWSDTO.paymentDueDate + ".  Correct format: MM/dd/yyyy");
            }

            Company company = Company.findCompany(paymentResponseWSDTO.pspCompanyID, SourceSystemCode.valueOf(paymentResponseWSDTO.sourceSystemCD));

            if(company == null) {
                throw new RuntimeException("No company is found with PS Id:" + paymentResponseWSDTO.pspCompanyID +" Source system code:"+ paymentResponseWSDTO.sourceSystemCD);
            }
            SpcfCalendar rejectDueDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(paymentResponseWSDTO.paymentDueDate);
            rejectDueDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());

            ediPaymentDetails = Application.find(EdiPaymentDetail.class,  EdiPaymentDetail.MoneyMovementTransaction().Company().equalTo(company)
                    .And(EdiPaymentDetail.MoneyMovementTransaction().PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate(paymentResponseWSDTO.paymentTemplateCd)))
                    .And(EdiPaymentDetail.PaymentDueDate().equalTo(rejectDueDate)).And(EdiPaymentDetail.StatusCd().equalTo(TaxPaymentStatus.SentToAgency)));

        }
        return ediPaymentDetails;

    }

    private Collection<EDIPaymentDetailWSDTO> buildEdiPaymentDetailDTOs(DomainEntitySet<EdiPaymentDetail> pPaymentDetails) {
        Collection<EDIPaymentDetailWSDTO> paymentDetailWSDTOs = new ArrayList<EDIPaymentDetailWSDTO>();
        for (EdiPaymentDetail paymentDetail : pPaymentDetails) {
            EDIPaymentDetailWSDTO detailWSDTO = new EDIPaymentDetailWSDTO();
            detailWSDTO.mPaymentAmount = paymentDetail.getPaymentAmount().getIntegerPart();
            detailWSDTO.mPaymentInitiationDate = (paymentDetail.getPaymentInitiationDate() == null) ? null : new Date(paymentDetail.getPaymentInitiationDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mPaymentSettlementDate = (paymentDetail.getPaymentSettlementDate() == null) ? null : new Date(paymentDetail.getPaymentSettlementDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mPaymentDueDate = (paymentDetail.getPaymentDueDate() == null) ? null : new Date(paymentDetail.getPaymentDueDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mErrorCd = paymentDetail.getErrorCd();
            detailWSDTO.mErrorMessage = paymentDetail.getErrorMessage();
            detailWSDTO.mFedTaxId = paymentDetail.getFedTaxId();
            detailWSDTO.mGroupId = paymentDetail.getGroupId();
            detailWSDTO.mGroupTxnTime = paymentDetail.getGroupTransactionTime();
            detailWSDTO.mPeriodEndDate = (paymentDetail.getPeriodEndDate() == null) ? null : new Date(paymentDetail.getPeriodEndDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mPeriodBeginDate = (paymentDetail.getPeriodBeginDate() == null) ? null : new Date(paymentDetail.getPeriodBeginDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mResponseDate = (paymentDetail.getResponseDate() == null) ? null : new Date(paymentDetail.getResponseDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mStatusCd = (paymentDetail.getStatusCd() == null) ? null : paymentDetail.getStatusCd().toString();
            detailWSDTO.mStatusEffectiveDate = (paymentDetail.getStatusEffectiveDate() == null) ? null : new Date(paymentDetail.getStatusEffectiveDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mTaxTypeCode = paymentDetail.getTaxTypeCode();
            detailWSDTO.mTransactionId = paymentDetail.getTransactionId();
            detailWSDTO.mTransactionSetId = paymentDetail.getTransactionSetId();
            detailWSDTO.mTransactionConfirmationNumber = paymentDetail.getConfirmationNumber();
            paymentDetailWSDTOs.add(detailWSDTO);
        }
        return paymentDetailWSDTOs;
    }

}
