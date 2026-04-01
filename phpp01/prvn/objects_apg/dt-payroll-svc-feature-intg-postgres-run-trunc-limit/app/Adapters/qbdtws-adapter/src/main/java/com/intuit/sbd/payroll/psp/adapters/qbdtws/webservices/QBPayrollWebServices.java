package com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.CommonValidations;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.ErrorMessageList;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.TransmissionLogging;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.QBDTWSSubmitPayrollRequestProcess;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.QBPayrollTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401k;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * WebServices providing payroll service offerings
 * to the QuickBooks Desktop product
 */
@WebService()
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public class QBPayrollWebServices {
    private static final SpcfLogger logger = PayrollServices.getLogger(QBPayrollWebServices.class);

    @WebMethod
    public ProcessingResponse SubmitPayroll(@WebParam(name = "SubmitPayrollRequest") SubmitPayrollRequest submitPayrollRequest) {
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.QBDTWSAdapter));
        StopWatch sw = new StopWatch().start();

        ProcessingResponse response = new ProcessingResponse();
        response.setClearPaycheckQueue(false);

        ProcessResult processResult = new ProcessResult();

        String transmissionId = UUID.randomUUID().toString();
        response.setTransmissionId(transmissionId);

        Long nextToken = null;
        SourceSystemCode companySourceSystem = null;
        String companyId = null;
        try {
            if (submitPayrollRequest == null) {
                response.getProcessingMessagesList().add(ErrorMessageList.invalidArgument("SubmitPayrollRequest"));
                return response;
            }

            // validate the company, PIN and token (basic required message properties)
            PayrollServices.beginUnitOfWork();
            Company pspCompany = CommonValidations.validateCompanyPin(submitPayrollRequest, response);
            if (pspCompany != null && pspCompany.isCompanyTerminated()) {
                response.getProcessingMessagesList().add(ErrorMessageList.ServiceUnavailable());
            }
            PayrollServices.commitUnitOfWork();
            if (response.getProcessingMessagesList().size() != 0) {
                return response;
            }
            companyId = pspCompany.getSourceCompanyId();
            companySourceSystem = pspCompany.getSourceSystemCd();

            ProcessResult<Integer> tokenResult = getClientToken(companyId, submitPayrollRequest.getCurrentToken());
            if (!tokenResult.isSuccess()) {
                ErrorMessageList.mergeResults(tokenResult, response.getProcessingMessagesList());
                return response;
            }

            // update transmission token if this is a company w/cloud services
            PayrollServices.beginUnitOfWork();
            pspCompany = Company.findCompany(companyId, companySourceSystem);
            long currentCloudToken = pspCompany.getCloudCurrentToken();
            boolean clientIsInSync = tokenResult.getResult() == currentCloudToken;
            response.setNextToken(Long.toString(currentCloudToken));

            // this is a kill switch -- it will return false for any company that is not active in a list of
            // services defined in the PSP_SYSTEM_PARAMETER table under the QBDTWS_PROCESS_DATA_FOR_SERVICES code
            ProcessResult<Boolean> processRequestResult = shouldProcessRequest(pspCompany, submitPayrollRequest.getUpdateCompanyRequest());
            if (!processRequestResult.getResult()) {
                ErrorMessageList.mergeResults(processRequestResult, response.getProcessingMessagesList());
                if (pspCompany.isCompanyOnService(ServiceCode.Tax) && !pspCompany.isCompanyOnService(ServiceCode.ThirdParty401k)) {
                    response.setClearPaycheckQueue(true);
                } else {
                    response.setClearPaycheckQueue(false);
                }
                return response;
            }

            nextToken = currentCloudToken + 1;
            pspCompany.setCloudCurrentToken(nextToken);
            PayrollServices.commitUnitOfWork();
            response.setNextToken(Long.toString(nextToken));

            QBPayrollTransmissionLogger.recordTransmissionRequest(TransmissionType.WS401KSubmitPayroll, tokenResult.getResult().longValue(), submitPayrollRequest, transmissionId, companyId);

            ProcessResult<Boolean> canProcessRequestResult = canProcessRequest(pspCompany, submitPayrollRequest);
            if (!canProcessRequestResult.getResult()) {
                PayrollServices.beginUnitOfWork();
                CompanyEvent.createInvalidSourceSystemTransmissionEvents(pspCompany, transmissionId, canProcessRequestResult);
                PayrollServices.commitUnitOfWork();
                ErrorMessageList.mergeResults(canProcessRequestResult, response.getProcessingMessagesList());
                response.setClearPaycheckQueue(false);
                return response;
            }

            // process the company, employee and paycheck information
            // -- if company or employee do not succeed (but do not throw an exception), continue to submit
            //    paycheck by requirement (attempt to allow customers to get their time critical info into the system)

            // update company information
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            pspCompany = Company.findCompany(companyId, companySourceSystem);
            UpdateCompanyRequest updateCompanyRequest = submitPayrollRequest.getUpdateCompanyRequest();
            ProcessResult submitCompanyResult = submitCompany(pspCompany, updateCompanyRequest, nextToken);
            processResult.merge(submitCompanyResult);
            if (submitCompanyResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.rollbackUnitOfWork();
            }
            CompanyEvent.postSourceSystemTransmissionInvalidEvents(pspCompany, transmissionId, submitCompanyResult);

            // submit employees
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            pspCompany = Company.findCompany(companyId, companySourceSystem);
            SubmitEmployeesRequest submitEmployeesRequest = submitPayrollRequest.getSubmitEmployeesRequest();
            ProcessResult submitEmployeesResult = submitEmployees(pspCompany, submitEmployeesRequest, transmissionId, clientIsInSync);
            processResult.merge(submitEmployeesResult);
            if (submitEmployeesResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.rollbackUnitOfWork();
            }

            // submit paycheck information
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            pspCompany = Company.findCompany(companyId, companySourceSystem);
            QBDTWSSubmitPayrollRequestProcess submitPayrollProcess = new QBDTWSSubmitPayrollRequestProcess(transmissionId, pspCompany, submitPayrollRequest);
            ProcessResult submitPayrollResult = submitPayrollProcess.process();

            //TODO_RN: remove use of getAdapterValidationResult for something, anything, better
            CompanyEvent.postSourceSystemTransmissionInvalidEvents(pspCompany, transmissionId, submitPayrollProcess.getAdapterValidationResult());

            processResult.merge(submitPayrollResult);
            if (submitPayrollResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
                response.setClearPaycheckQueue(true);
            } else {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            logger.error("SubmitPayroll exception for PSID: " + companyId, t);
            response.getProcessingMessagesList().add(ErrorMessageList.unexpectedError());
        } finally {
            PayrollServices.rollbackUnitOfWork();

            // return the set of services a company is active on
            // QBDT uses this information to determine whether to show a customer the transmission/validation
            //    response page
            if (companyId != null && companySourceSystem != null) {
                try {
                    PayrollServices.beginUnitOfWork();
                    DomainEntitySet<CompanyService> sortedServices = getActiveCompanyServices(companySourceSystem, companyId);
                    response.setCompanyServices(QBPayrollTranslator.createWSDTO(sortedServices));
                    PayrollServices.rollbackUnitOfWork();
                } catch (Exception e) {
                }
            }

            // error message manipulation -- replace core message w/401K specific
            if (companySourceSystem != null && companyId != null) {
                processResult = MessageList.replaceCompanyOperationNotAllowedMessages(processResult, SystemCapabilityCode.ChangeCompanyInfo, companySourceSystem.name(), companyId);
            }

            ErrorMessageList.mergeResults(processResult, response.getProcessingMessagesList());
            QBPayrollTransmissionLogger.recordTransmissionResponse(TransmissionType.WS401KSubmitPayroll, transmissionId, companyId, nextToken, response);
            logger.info("processed SubmitPayroll for PSID " + companyId + " in " + sw.getElapsedTimeString());
        }

        return response;
    }

    private DomainEntitySet<CompanyService> getActiveCompanyServices(SourceSystemCode pSourceSystem, String pCompanyId) {
        Company pspCompany = Company.findCompany(pCompanyId, pSourceSystem);
        DomainEntitySet<CompanyService> activeServices = CompanyService.findActiveCompanyServices(pspCompany);
        DomainEntitySet<CompanyService> sortedServices = activeServices.sort(CompanyService.Service().ServiceCd());
        return sortedServices;
    }

    private ProcessResult<Boolean> shouldProcessRequest(Company pCompany, UpdateCompanyRequest updateCompanyRequest) {
        ProcessResult<Boolean> processResult = new ProcessResult<Boolean>();

        //PSRV001881 Limit Cloud Data Collection to Venti R8 and above
        //Purposefully falling through and processing request here if we get unexpected or bad data in the QBVersion field
        if (updateCompanyRequest != null) {
            OFXAPPVERObject ofxappverObject = new OFXAPPVERObject(updateCompanyRequest.getCompany().getQBVersion(), false);
            if (ofxappverObject != null && ofxappverObject.isProperlyFormatted()) {
                boolean isV20R8OrGreater = ofxappverObject.getIntQBVersion() > 20 || (ofxappverObject.getIntQBVersion() == 20 && ofxappverObject.getIntRNumber() >= 8);
                if (!isV20R8OrGreater) {
                    processResult.setResult(false);
                    return processResult;
                }
            }
        }

        // Assisted companies can only use cloud submit when they are on 401K service; otherwise data is just an
        // overlap with what is collected through Assisted OFX
        if (pCompany.isCompanyOnService(ServiceCode.Tax) && !pCompany.isCompanyOnService(ServiceCode.ThirdParty401k)) {
            processResult.setResult(false);
            return processResult;
        }

        String serviceNames = SystemParameter.findStringValue(SystemParameter.Code.QBDTWS_PROCESS_DATA_FOR_SERVICES);
        if (serviceNames == null || serviceNames.trim().length() == 0) {
            processResult.setResult(true);
            return processResult;
        }

        if (serviceNames != null) {
            String[] services = serviceNames.split(":");
            for (String serviceName : services) {
                try {
                    ServiceCode serviceCode = ServiceCode.valueOf(serviceName);
                    if (pCompany.isCompanyOnService(serviceCode)) {
                        processResult.setResult(true);
                        return processResult;
                    }
                } catch (IllegalArgumentException e) {
                    logger.error("invalid service name specified in PSP_SYSTEM_PARAMETER for code QBDTWS_PROCESS_DATA_FOR_SERVICES: " + serviceName);
                }
            }
        }

        processResult.setResult(false);
        return processResult;
    }

    private ProcessResult<Boolean> canProcessRequest(Company pCompany, SubmitPayrollRequest pSubmitPayrollRequest) {
        ProcessResult<Boolean> processResult = new ProcessResult<Boolean>();

        //Limit Assisted + 401k integration in QuickBooks to versions w/patch that supplies ofxEmployeeId, ofxPayrollItemId
        if (pCompany.isCompanyOnService(ServiceCode.Tax) && pCompany.isCompanyOnService(ServiceCode.ThirdParty401k)) {

            UpdateCompanyRequest updateCompanyRequest = pSubmitPayrollRequest.getUpdateCompanyRequest();
            // this should only happen in QA environments
            if (updateCompanyRequest == null) {
                if (!Application.isProdEnvironment()) {
                    processResult.setResult(true);
                    return processResult;
                }
            }

            OFXAPPVERObject ofxappverObject = new OFXAPPVERObject(updateCompanyRequest.getCompany().getQBVersion(), false);
            if (!ofxappverObject.isProperlyFormatted() || !assisted401kIntegrationIsAvailable(ofxappverObject.getIntQBVersion())) {
                processResult.getMessages().QBDT401kIntegrationNotAvailable(EntityName.Company, pCompany.getSourceCompanyId());
                processResult.setResult(false);
                return processResult;
            }

            // the slipstream passes the ID values from QBDT to the .NET component
            boolean hasCloudSlipstreamUpdate =
                    // 2010 Venti R10 and above
                    (ofxappverObject.getIntQBVersion() == 20 && ofxappverObject.getIntRNumber() >= 10)
                    // 2011 Emerald R4 and above
                    || (ofxappverObject.getIntQBVersion() == 21 && ofxappverObject.getIntRNumber() >= 4)
                    // 2012 and above
                    || (ofxappverObject.getIntQBVersion() > 21);
            if (!hasCloudSlipstreamUpdate) {
                processResult.getMessages().QBDT401kIntegrationRequiresMoreRecentQBVersion(EntityName.Company, pCompany.getSourceCompanyId());
                processResult.setResult(false);
                return processResult;
            }

            // the updated .NET component passes the ID values to PSP
            boolean hasCloudDotNetComponentUpdate =
                    pSubmitPayrollRequest.getPayrollItemList() != null
                    && pSubmitPayrollRequest.getPayrollItemList().getPayrollItem().size() > 0
                    && pSubmitPayrollRequest.getPayrollItemList().getPayrollItem().get(0).getOfxPayrollId() != null;
            if (!hasCloudDotNetComponentUpdate) {
                processResult.getMessages().QBDT401kIntegrationRequiresMoreRecentQBDotNetVersion(EntityName.Company, pCompany.getSourceCompanyId());
                processResult.setResult(false);
                return processResult;
            }

            // this company is having having its OFX queued so we cannot process its 401k
            DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests =
                    QbdtUnprocessedRequest.findUnprocessedRequests(pCompany, false,
                                                                   QbdtRequestStatus.Error,
                                                                   QbdtRequestStatus.Processing,
                                                                   QbdtRequestStatus.Queued);
            if (qbdtUnprocessedRequests.size() > 0) {
                processResult.getMessages().QBDT401kIntegrationAssistedQueued(EntityName.Company, pCompany.getSourceCompanyId());
                processResult.setResult(false);
                return processResult;
            }
        }

        processResult.setResult(true);
        return processResult;
    }

    private boolean assisted401kIntegrationIsAvailable(int pQBMajorVersion) {
        boolean assistedIntegrationAvailable = true;

        String integrationAvailabilityDate = null;
        try {
            if (pQBMajorVersion < 20) {
                return false;
            } else if (pQBMajorVersion == 20) {
                integrationAvailabilityDate = SystemParameter.findValue(SystemParameter.Code.QBDTWS_ASSISTED_2010R10_401K_AVAILABILITY_DATE);
            } else if (pQBMajorVersion == 21) {
                integrationAvailabilityDate = SystemParameter.findValue(SystemParameter.Code.QBDTWS_ASSISTED_2011R4_401K_AVAILABILITY_DATE);
            } else if (pQBMajorVersion > 21) {
                return true;
            }
        } catch (RuntimeException rte) {
            // if SystemParameter not in DB, assume it is available (app version checks will catch it otherwise)
            return true;
        }

        try {
            SpcfCalendar availableDate = SpcfCalendar.parse("yyyy-MM-dd HH:mm", integrationAvailabilityDate);
            assistedIntegrationAvailable = availableDate.before(PSPDate.getPSPTime());
        } catch (Throwable t) {
            // crud - there was a date there and we can't parse it. assume unavailable.
            logger.error("could not parse value for SystemParameter.Code.QBDTWS_ASSISTED_401K_AVAILABILITY_DATE (" + integrationAvailabilityDate + ") - must be in yyyy-MM-dd HH:mm format");
            assistedIntegrationAvailable = false;
        }
        return assistedIntegrationAvailable;
    }

    private ProcessResult<Integer> getClientToken(String companyId, String clientTokenStr) {
        ProcessResult<Integer> processResult = new ProcessResult<Integer>();
        if (clientTokenStr == null) {
            processResult.getMessages().RequiredAttribute(EntityName.Company, companyId, "CurrentToken");
            return processResult;
        }

        try {
            int clientToken = Integer.parseInt(clientTokenStr);
            processResult.setResult(clientToken);
        } catch (NumberFormatException nfe) {
            processResult.getMessages().InvalidValue(EntityName.Company, companyId, "CurrentToken - " + clientTokenStr);
            return processResult;
        }

        return processResult;
    }

    private ProcessResult submitCompany(Company pspCompany, UpdateCompanyRequest updateCompanyRequest, long nextToken) {
        StopWatch sw = new StopWatch();

        DTOFactory dtoFactory = new DTOFactory();
        CompanyDTO companyDTO = dtoFactory.create(pspCompany);
        companyDTO.setCloudCurrentToken(nextToken);

        CompanyBankAccountDTO companyBankAccountDTO = null;
        if (updateCompanyRequest != null) {
            QBCompany requestCompany = updateCompanyRequest.getCompany();
            companyDTO = QBPayrollTranslator.updateCoreDTO(companyDTO, requestCompany);
            // intentionally ignore company bank account information since it is important to DD handling
        }

        ProcessResult processResult = PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, pspCompany.getSourceCompanyId(), companyDTO);

        String result = processResult.isSuccess() ? "successfully" : "with errors";
        logger.info("submitCompany for PSID: " + pspCompany.getSourceCompanyId() + " completed (" + result + ") in " + sw.getElapsedTimeString());

        return processResult;
    }

    private ProcessResult submitEmployees(Company pspCompany, SubmitEmployeesRequest submitEmployeesRequest, String transmissionId, boolean clientIsInSync) {
        StopWatch sw = new StopWatch().start();

        ProcessResult processResult = new ProcessResult();

        if (submitEmployeesRequest == null) {
            return processResult;
        }

        ProcessingContext processingCtx = new ProcessingContext("submitEmployees");

        EmployeeResultMap employeeValidationResults = new EmployeeResultMap();

        // duplicate SSN check
        List<QBEmployee> incomingEmployees = submitEmployeesRequest.getEmployees().getEmployee();
        int incomingEmployeeCount = incomingEmployees.size();

        try {
            HashMap<String, QBEmployee> ssnQBEmployeeMap = new HashMap<String, QBEmployee>(incomingEmployeeCount);
            String ssn;

            // Check for duplicate SSNs among the incoming employees
            // (Note: empty (i.e. null/whitespace) SSNs are allowed, so ignore them)
            for (QBEmployee incomingEmployee : incomingEmployees) {
                processingCtx = new ProcessingContext("DuplicateSSNCheck", incomingEmployee);

                ssn = incomingEmployee.getSocialSecurityNumber();
                
                if ((ssn != null) && (ssn.trim().length() > 0)) {
                    if (ssnQBEmployeeMap.containsKey(ssn)) {
                        QBEmployee matchedEmployee = ssnQBEmployeeMap.get(ssn);
                        ProcessResult dupeSSNResult = new ProcessResult();

                        dupeSSNResult.getMessages().DuplicateEmployeeSSN(EntityName.Employee,
                                                                         incomingEmployee.getPspEmployeeId(),
                                                                         incomingEmployee.getFullName(),
                                                                         ssn,
                                                                         matchedEmployee.getFullName());

                        // PSRV001844 - Check to ensure we need to do 401k validation for this ee.
                        if (shouldPerform401KValidation(pspCompany, incomingEmployee, false)) {
                            employeeValidationResults.put(incomingEmployee, dupeSSNResult);
                        }

                        processResult.merge(dupeSSNResult);
                    } else {
                        ssnQBEmployeeMap.put(ssn, incomingEmployee);
                    }
                }
            }

            if (!processResult.isSuccess()) {
                return processResult;
            }

        //TODO: put constraint on DB where source-employee-id is unique for cloud employees
            DomainEntitySet<Employee> existingEmployees = pspCompany.getCloudEmployees(Employee.MailingAddress());

            // only update 401k specific EE data if a company is on Assisted to avoid introducing errors into Assisted Disaster Recovery
            // note that for Assisted+401, the processing terminates at the end of this block - no add/delete EE management is performed
            if (pspCompany.isCompanyOnService(ServiceCode.Tax)) {
                DTOFactory dtoFactory = new DTOFactory();
                for (QBEmployee incomingEmployee : submitEmployeesRequest.getEmployees().getEmployee()) {
                    Employee existingEmployee = existingEmployees.findEntity(Employee.SourceEmployeeId().equalTo(incomingEmployee.getPspEmployeeId()));
                    if (existingEmployee == null) {
                        // handle hopefully very unlikely scenario of Assisted+401k, Cancel, new employees added, reactivate Assisted (Cancel->Active, no new signup)
                        existingEmployee = existingEmployees.findEntity(Employee.QbdtEmployeeInfo().ListId().equalTo(incomingEmployee.getSourceEmployeeId()));
                    }
                    if (existingEmployee != null && incomingEmployee.isActive()) {
                        EmployeeDTO employeeDTO = dtoFactory.create(existingEmployee);
                        // special handling for Assisted re-activation where employee was created while Assisted cancelled
                        if (!employeeDTO.getEmployeeId().equals(incomingEmployee.getOfxEmployeeId())) {
                            employeeDTO.setEmployeeId(incomingEmployee.getOfxEmployeeId());
                        }
                        employeeDTO = QBPayrollTranslator.updateCoreThirdParty401kDTO(employeeDTO, incomingEmployee);                        
                        employeeDTO.setBirthDate(QBPayrollTranslator.createCoreDTO(incomingEmployee.getBirthDate()));
                        employeeDTO.setExistingEmployeeGuid(existingEmployee.getId().toString());
                        employeeDTO.getQBDTEmployeeInfoDTO().setListId(incomingEmployee.getSourceEmployeeId());

                        boolean perform401kValidation = shouldPerform401KValidation(pspCompany, incomingEmployee, false);
                        boolean wasValid401kEE = false;
                        if (perform401kValidation) {
                            employeeDTO.setValidator(new EmployeeDTO401kValidator());
                            wasValid401kEE = existingEmployee.isValidForCensusFile().size() > 0;
                        }

                        ProcessResult<Employee> employeeUpdateResult =
                                PayrollServices.employeeManager.updateEmployee(pspCompany.getSourceSystemCd(),
                                                                               pspCompany.getSourceCompanyId(),
                                                                               employeeDTO);                        
                        processResult.merge(employeeUpdateResult);

                        if (shouldPerform401KValidation(pspCompany, incomingEmployee, false)) {
                            //todo_rhn_401k optimize to call only when an EE changes state (valid <> invalid)
                            boolean isValid401kEE = employeeUpdateResult.getResult().isValidForCensusFile().size() > 0;
                            if (wasValid401kEE != isValid401kEE) {
                                ThirdParty401k.updateEmployee401K(employeeUpdateResult.getResult());
                            }
                        }

                        if (perform401kValidation) {
                            employeeValidationResults.put(incomingEmployee, employeeUpdateResult);
                        }
                    }
                }
                return processResult;
            }

            // update all matches
            ArrayList<QBEmployee> unmatchedIncomingEmployees = new ArrayList<QBEmployee>();

            for (QBEmployee incomingEmployee : submitEmployeesRequest.getEmployees().getEmployee()) {
                processingCtx = new ProcessingContext("UpdateMatchingEmployees", incomingEmployee);

                Employee existingEmployee = existingEmployees.findEntity(Employee.SourceEmployeeId().equalTo(incomingEmployee.getPspEmployeeId()));
                if (existingEmployee == null) {
                    // handle case where customer goes from Assisted+401k to (DIY+401k or DD+401k)
                    // instead of creating new employee, match by list-id
                    existingEmployee = existingEmployees.findEntity(Employee.QbdtEmployeeInfo().ListId().equalTo(incomingEmployee.getSourceEmployeeId()));
                }

                if (existingEmployee != null) {
                    processingCtx = new ProcessingContext("UpdateMatchingEmployees", incomingEmployee, existingEmployee);

                    ProcessResult employeeUpdateResult = updateExistingEmployee(pspCompany, existingEmployee, incomingEmployee);

                    // PSRV001844 - Check to ensure we need to do 401k validation for this ee.
                    if (shouldPerform401KValidation(pspCompany, incomingEmployee, false)) {
                        employeeValidationResults.put(incomingEmployee, employeeUpdateResult);
                    }

                    processResult.merge(employeeUpdateResult);

                    existingEmployees.remove(existingEmployee);
                } else {
                    unmatchedIncomingEmployees.add(incomingEmployee);
                }
            }

            // determine whether an unmatched employee is an update to an existing employee (SSN match) in what is likely
            // an 'out of sync' scenario or a new employee to be added
            for (int i = unmatchedIncomingEmployees.size() - 1; i >= 0; i--) {
                QBEmployee unmatchedIncomingEmployee = unmatchedIncomingEmployees.get(i);

                processingCtx = new ProcessingContext("UnmatchedIncomingEmployees", unmatchedIncomingEmployee);

                // QBDT only sends EE source id once an EE is deactivated; not possible to add and pass PSP
                // validations (i.e. no SSN, no first/last name, etc.)
                if (!unmatchedIncomingEmployee.isActive()) {
                    unmatchedIncomingEmployees.remove(i);
                    continue;
                }

                ProcessResult employeeResult = null;

                ssn = unmatchedIncomingEmployee.getSocialSecurityNumber();

                if ((ssn != null) && (ssn.trim().length() > 0)) {
                    List<String> encSsnList = EncryptionUtils.deterministicEncryptWithAllKeys(Employee.TaxIdKeyName, ssn);
                    DomainEntitySet<Employee> ssnMatchedEmployees = existingEmployees.find(Employee.TaxIdEnc().in(encSsnList));

                    if (ssnMatchedEmployees.size() == 1) {
                        Employee ssnMatchedEmployee = ssnMatchedEmployees.get(0);

                        processingCtx = new ProcessingContext("UnmatchedIncomingEmployees", unmatchedIncomingEmployee, ssnMatchedEmployee);
                        employeeResult = updateExistingEmployee(pspCompany, ssnMatchedEmployee, unmatchedIncomingEmployee, true);
                        unmatchedIncomingEmployees.remove(i);
                        existingEmployees.remove(ssnMatchedEmployee);
                    } else if (ssnMatchedEmployees.size() == 0) {
                        employeeResult = addEmployee(pspCompany, unmatchedIncomingEmployee, transmissionId);
                        unmatchedIncomingEmployees.remove(i);
                    } else if (ssnMatchedEmployees.size() > 1) {
                        //TODO: error message: more than 1 ee matched on SSN
                        throw new RuntimeException("companyid: " + pspCompany.getSourceCompanyId() +
                                                   " -- multiple employees in DB (" + ssnMatchedEmployees.size() +
                                                   ") with same TaxID/SSN (" + ssn + ")");
                    }
                } else if (clientIsInSync) {
                    employeeResult = addEmployee(pspCompany, unmatchedIncomingEmployee, transmissionId);
                    unmatchedIncomingEmployees.remove(i);
                } else {
                    employeeResult = new ProcessResult();
                    employeeResult.getMessages().OutOfSyncEmployeeSSNRequired(EntityName.Employee,
                                                                              unmatchedIncomingEmployee.getPspEmployeeId(),
                                                                              unmatchedIncomingEmployee.getFullName());
                }

                // PSRV001844 - Check to ensure we need to do 401k validation for this ee.
                if (shouldPerform401KValidation(pspCompany, unmatchedIncomingEmployee, false)) {
                    employeeValidationResults.put(unmatchedIncomingEmployee, employeeResult);
                }

                processResult.merge(employeeResult);
            }

            // process EEs that have been deleted out of QBDT
            for (Employee unmatchedExistingEmployee : existingEmployees) {
                processingCtx = new ProcessingContext("UnmatchedExistingEmployees", unmatchedExistingEmployee);

                ProcessResult employeeResult = null;

                if (unmatchedExistingEmployee.getStatusCd() == EmployeeStatus.Active) {
                    if (Paycheck.findPaychecksBySourceEmployee(pspCompany, unmatchedExistingEmployee).size() == 0 &&
                            Paycheck.findPaychecksByEmployee(pspCompany, unmatchedExistingEmployee).size() == 0 &&
                            ThirdParty401kBatchEmployee.findTP401kEmployeeBatchByEmployee(unmatchedExistingEmployee).size() == 0) {
                        employeeResult = PayrollServices.employeeManager.deleteEmployee(pspCompany.getSourceSystemCd(), pspCompany.getSourceCompanyId(), unmatchedExistingEmployee.getSourceEmployeeId());
                    } else {
                        employeeResult = PayrollServices.employeeManager.deactivateEmployee(pspCompany.getSourceSystemCd(), pspCompany.getSourceCompanyId(), unmatchedExistingEmployee.getSourceEmployeeId(), null);
                    }

                    processResult.merge(employeeResult);
                }
            }
        } catch (Throwable t) {
            String errorMessage = processingCtx.toString() + " - " + t.getMessage();
            logger.error("SubmitEmployees for PSID: " + pspCompany.getSourceCompanyId() + " - " + errorMessage, t);
            processResult.getMessages().ExceptionOccurred(errorMessage);
        } finally {
            // log all validation events that were not successful from a separate thread
            CompanyEvent.writeInvalidEmployeeInformationEvents(pspCompany, employeeValidationResults, transmissionId);
        }

        String result = processResult.isSuccess() ? "successfully" : "with errors";

        logger.info("SubmitEmployees for PSID: " + pspCompany.getSourceCompanyId() + " completed (" + result + ") in " + sw.getElapsedTimeString() + " for " + incomingEmployeeCount + " submitted employee(s)");

        return processResult;
    }

    private ProcessResult<Employee> addEmployee(Company domainCompany, QBEmployee unmatchedIncomingEmployee, String transmissionId) {
        ProcessResult<Employee> employeeProcessResult;
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO = QBPayrollTranslator.updateCoreDTO(employeeDTO, unmatchedIncomingEmployee);

        if (shouldPerform401KValidation(domainCompany, unmatchedIncomingEmployee, false)) {
            employeeDTO.setValidator(new EmployeeDTO401kValidator());
        }

        employeeProcessResult = PayrollServices.employeeManager.addEmployee(domainCompany.getSourceSystemCd(), domainCompany.getSourceCompanyId(), employeeDTO, transmissionId);
        return employeeProcessResult;
    }

    /**
     * Perform 401K validation for all employees that:
     * - are 'active' in QBDT (i.e. not hidden)
     * - are associated w/a company that has an active 401K service
     * - have not been terminated prior to the 401K service start date (EEs terminated after the service start date
     * are still important to the 'Census File')
     *
     * @param pspCompany    : The PSP company
     * @param employee      : The QB Employee
     * @param validateAll   : Should all employees, including inactive ones be validated
     * @return              : True of the given company and employee are valid 401k participants
     */
    private boolean shouldPerform401KValidation(Company pspCompany, QBEmployee employee, boolean validateAll) {
        if (!validateAll && !employee.isActive()) {
            return false;
        }

        if (!pspCompany.isCompanyOnService(ServiceCode.ThirdParty401k)) {
            return false;
        }

        ThirdParty401kCompanyServiceInfo k401ServiceInfo = (ThirdParty401kCompanyServiceInfo) pspCompany.getService(ServiceCode.ThirdParty401k);
        SpcfCalendar serviceStartDate = k401ServiceInfo.getServiceStartDate();

        if (serviceStartDate != null && employee.getTerminationDate() != null) {
            QBDate terminationDate = employee.getTerminationDate();
            String termDateStr = String.format("%4d%02d%02d", terminationDate.getYear(), terminationDate.getMonth(), terminationDate.getDay());
            SpcfCalendar terminationDateCal = CalendarUtils.createInstanceFromDate(termDateStr);

            if (terminationDateCal.before(serviceStartDate)) {
                return false;
            }
        }

        return true;
    }

    private ProcessResult<Employee> updateExistingEmployee(Company domainCompany, Employee existingEmployee, QBEmployee requestEmployee) {
        return updateExistingEmployee(domainCompany, existingEmployee, requestEmployee, false);
    }

    private ProcessResult<Employee> updateExistingEmployee(Company domainCompany, Employee existingEmployee, QBEmployee requestEmployee, boolean pFuzzyMatch) {
        DTOFactory dtoFactory = new DTOFactory();
        ProcessResult<Employee> employeeProcessResult = new ProcessResult<Employee>();

        // update EEs who are active -- do not 'deactivate' non-active EEs (
        // Deactive is primarily used to shutoff bank account access in DD, the PSP 'employee status' might be better
        // as a DD bank account status.  The QBDT 'employee isActive status' concept is different.  In QBDT it is
        // purely visual whether the EE displays in their drop-lists
        if (requestEmployee.isActive()) {
            if (existingEmployee.getStatusCd() == EmployeeStatus.Inactive) {
                DateDTO rehireDate = QBPayrollTranslator.createCoreDTO(requestEmployee.getHireDate());
                PayrollServices.employeeManager.reactivateEmployee(domainCompany.getSourceSystemCd(), domainCompany.getSourceCompanyId(), requestEmployee.getPspEmployeeId(), rehireDate);
            }

            EmployeeDTO employeeDTO = dtoFactory.create(existingEmployee);

            // non-ID employee match handling
            if (pFuzzyMatch) {
                // update the source employee - this ee is assuming the other's record (DR scenario.)
                // (the list-id is updated in QBPayrollTranslator.updateCoreDTO(..) below)
                employeeDTO.setEmployeeId(requestEmployee.getPspEmployeeId());
            }

            //todo_rhn_401k put optimization to only run updateEmployee401k when employee validation status changed
            if (shouldPerform401KValidation(domainCompany, requestEmployee, false)) {
                employeeDTO.setValidator(new EmployeeDTO401kValidator());
            }

            employeeDTO = QBPayrollTranslator.updateCoreDTO(employeeDTO, requestEmployee);
            employeeDTO.setExistingEmployeeGuid(existingEmployee.getId().toString());
            employeeProcessResult = PayrollServices.employeeManager.updateEmployee(domainCompany.getSourceSystemCd(), domainCompany.getSourceCompanyId(), employeeDTO);

            if (shouldPerform401KValidation(domainCompany, requestEmployee, false)) {
                if (employeeProcessResult.isSuccess()) {
                    existingEmployee = employeeProcessResult.getResult();
                }
                
                ThirdParty401k.updateEmployee401K(existingEmployee);
            }
        }

        return employeeProcessResult;
    }

    class ProcessingContext {
        private String operation;
        private QBEmployee qbEmployee;
        private Employee pspEmployee;

        ProcessingContext(String operation) {
            this.operation = operation;
        }

        ProcessingContext(String operation, QBEmployee qbEmployee) {
            this.operation = operation;
            this.qbEmployee = qbEmployee;
        }

        ProcessingContext(String operation, Employee pspEmployee) {
            this.operation = operation;
            this.pspEmployee = pspEmployee;
        }

        ProcessingContext(String operation, QBEmployee qbEmployee, Employee pspEmployee) {
            this.operation = operation;
            this.qbEmployee = qbEmployee;
            this.pspEmployee = pspEmployee;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("operation='").append(operation).append("'");

            if (qbEmployee != null) {
                sb.append(", qbEmployee = ").append(qbEmployee);
            }

            if (pspEmployee != null) {
                sb.append(", pspEmployee = ").append(pspEmployee);
            }

            return sb.toString();
        }
    }
}

class EmployeeResultMap extends HashMap<String, ProcessResult> {
    public void put(QBEmployee pQBEmployee, ProcessResult result) {
        put(getResultKey(pQBEmployee.getPspEmployeeId(), pQBEmployee.getFullName()), result);
    }

    public void put(Employee pEmployee, ProcessResult result) {
        put(getResultKey(pEmployee.getSourceEmployeeId(), pEmployee.getFullName()), result);
    }

    public ProcessResult put(String pKey, ProcessResult pResult) {
        if (pKey == null) {
            return null;
        }

        if (containsKey(pKey)) {
            ProcessResult existing = get(pKey);
            existing.merge(pResult);
            return existing;
        } else {
            return super.put(pKey, pResult);
        }
    }

    // PSRV001844 - Check to ensure 'name' portion of key can never be null or zero length
    //              (i.e. resulting key = id:name, where 'id:' can never occur)
    private String getResultKey(String pSourceEmployeeId, String pFullName) {
        String id = ((pSourceEmployeeId == null) ? "" : pSourceEmployeeId.trim().replace(":", ""));
        String name = ((pFullName == null) ? "" : pFullName.trim().replace(":", ""));
        return (id.length() == 0) ? null : String.format("%s:%s", id, (name.length() == 0) ? "<missing-name>" : name);
    }
}

class QBPayrollTransmissionLogger {
    private static final SpcfLogger logger = PayrollServices.getLogger(QBPayrollTransmissionLogger.class);
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(Request.class, QBProcessingMessages.class, ProcessingResponse.class, SubmitEmployeesProcessing.class, SubmitEmployeesRequest.class, UpdateCompanyProcessing.class, UpdateCompanyRequest.class, SubmitPayrollRequest.class);
        } catch (Exception e) {
            logger.error("Unable to create QB Payroll JAXBContext for marshalling operation responses to transmission XML for recording in PSP", e);
        }
    }

    public static void recordTransmissionRequest(TransmissionType pTransmissionType, Long requestToken, Request pRequest, String pTransmissionId, String pSourceCompanyId) throws IOException {
        TransmissionLogging.recordTransmissionRequest(pTransmissionType, requestToken, pRequest, pTransmissionId, pSourceCompanyId, jaxbContext);
    }

    public static void recordTransmissionResponse(TransmissionType pTransmissionType, String pTransmissionId, String pSourceCompanyId, Long responseToken, QBProcessingMessages pResponse) {
        TransmissionLogging.recordTransmissionResponse(pTransmissionType, pTransmissionId, pSourceCompanyId, responseToken, pResponse, null, jaxbContext);
    }
}
