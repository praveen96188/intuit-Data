package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.pse.dd.wsapi.xsd.responsemessage.ResponseMessage;
import intuit.osp.pse.dd.wsapi.xsd.responsestatus.ResponseStatus;
import intuit.osp.pse.dd.wsapi.xsd.systemeventdata.SystemEventData;
import intuit.osp.pse.dd.wsapi.xsd.companysystemeventret.CompanySystemEventRet;
import intuit.osp.pse.dd.wsapi.xsd.companyeventret.CompanyEventRet;

import javax.xml.bind.JAXBException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jul 21, 2006
 * Time: 11:52:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DDCommon {

    public static final String pse_Error = "Internal PSE system error";
    public static final String npe_Error = "A null {0} object was passed into method {1}";

    private static final String INVALID = "INVALID";

    private static SpcfLogger logger = Application.getLogger(DDCommon.class);
    private static final intuit.osp.pse.dd.wsapi.xsd.responsestatus.ObjectFactory responseStatusFactory =
            new intuit.osp.pse.dd.wsapi.xsd.responsestatus.ObjectFactory();
    private static final intuit.osp.pse.dd.wsapi.xsd.responsemessage.ObjectFactory responseMessageFactory =
            new intuit.osp.pse.dd.wsapi.xsd.responsemessage.ObjectFactory();
    private static final intuit.osp.pse.dd.wsapi.xsd.responsemessagedata.ObjectFactory responseMessageDataFactory =
            new intuit.osp.pse.dd.wsapi.xsd.responsemessagedata.ObjectFactory();
    public static ResponseStatus SUCCESS;

    static {
        try {
            SUCCESS = responseStatusFactory.createResponseStatus();
            SUCCESS.setStatus(0);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    //V0 mapping between Enum and API and vice versa.  In V1, API and DO values will match

    protected static HashMap BankAccountTypeEnumToAPI;
    protected static HashMap BankAccountTypeAPIToEnum;

    static {
        BankAccountTypeEnumToAPI = new HashMap();
        BankAccountTypeEnumToAPI.put("Checking", "C");
        BankAccountTypeEnumToAPI.put("Savings", "S");

        BankAccountTypeAPIToEnum = new HashMap();
        BankAccountTypeAPIToEnum.put("C", "Checking");
        BankAccountTypeAPIToEnum.put("S", "Savings");
    }


    //V0 mapping between Message Level ENUM and API
    public static int getMessageLevelEnumToAPI(Message message) {
        int severity = 0;
        String messageCode = message.getMessageCode();

        MessageInfo.MessageLevel level = message.GetMessageInfo().Level;

        //Changing code 1040 to error for backwards-compatibility with DD
        if (messageCode.equalsIgnoreCase("1040")) {
            level = MessageInfo.MessageLevel.ERROR;
        }

        if (level.equals(MessageInfo.MessageLevel.ERROR)) {
            severity = 3;
        } else if (level.equals(MessageInfo.MessageLevel.WARNING)) {
            severity = 2;
        } else {
            severity = 1;
        }

        return severity;
    }


    public static HashMap getBankAccountTypeEnumToAPI() {
        return BankAccountTypeEnumToAPI;
    }

    public static HashMap getBankAccountTypeAPIToEnum() {
        return BankAccountTypeAPIToEnum;
    }

    private static ResponseStatus build_ResponseStatus() throws Exception {
        ResponseStatus responseStatus;
        responseStatus = responseStatusFactory.createResponseStatus();
        responseStatus.setStatus(0);
        return responseStatus;
    }


    //message.1100.messageFormat=The operation {3} for service {2} is not allowed for company {0}:{1} in it's current state.
    public static final String ERROR_1100 = "1100";

    //message.1101.messageFormat=The operation {2} is not allowed for company {0}:{1} in it's current state.
    public static final String ERROR_1101 = "1101";

    /**
     * Translate PSP error messages to expected errors by QBOE WS, and replace PSP errors with the expected QBOE errors.
     *
     * @param pProcessResult
     * @param pFromPspErrorCode
     * @param pToQboeErrorCode
     * @param pEntity
     */
    public static void replacePSPError(
            ProcessResult pProcessResult,
            String pFromPspErrorCode,
            String pToQboeErrorCode,
            final DomainEntity pEntity) {

        // Translate PSP error messages to expected errors by QBOE WS
        Message pspMessage = findMessage(pProcessResult, pFromPspErrorCode);
        Message qboeMessage = findMessage(pProcessResult, pToQboeErrorCode);

        if (qboeMessage == null && pspMessage != null) {
            int index = pProcessResult.getMessages().indexOf(pspMessage);
            pProcessResult.getMessages().remove(index);
            ProcessResult tempResult = new ProcessResult();

            com.intuit.sbd.payroll.psp.domain.Company company = null;
            if (pEntity instanceof com.intuit.sbd.payroll.psp.domain.Company) {
                company = (com.intuit.sbd.payroll.psp.domain.Company) pEntity;
            }
            SourceSystemCode sourceSystemCd = company == null ? null : company.getSourceSystemCd();
            String sourceCompanyId = company == null ? null : company.getSourceCompanyId();

            if ("177".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages()
                            .CompanyNotActive(EntityName.Company, sourceCompanyId,
                                    sourceSystemCd.toString(), sourceCompanyId);
                }
            } else if ("1011".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanyNotActiveOnService(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(),
                                    sourceCompanyId, ServiceCode.DirectDeposit.toString());
                }
            } else if ("1012".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanyAlreadyTerminatedForService(EntityName.Company, sourceCompanyId,
                                    sourceSystemCd.toString(), sourceCompanyId);
                }
            } else if ("1022".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanyAlreadyCancelledOnService(EntityName.Company, sourceCompanyId,
                                    sourceSystemCd.toString(),
                                    sourceCompanyId, ServiceCode.DirectDeposit.toString());
                }
            } else if ("1023".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanyPreviouslyTerminatedOnService(EntityName.Company, sourceCompanyId,
                                    sourceSystemCd.toString(),
                                    sourceCompanyId, ServiceCode.DirectDeposit.toString());
                }
            } else if ("1024".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanyOnHoldForService(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(),
                                    sourceCompanyId, ServiceCode.DirectDeposit.toString());
                }
            } else if ("1025".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanySuspendedForService(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(),
                                    sourceCompanyId, ServiceCode.DirectDeposit.toString());
                }
            } else if ("10012".equals(pToQboeErrorCode)) {
                if (company != null) {
                    CompanyService domainCompanyService = CompanyService.findCompanyService(
                            company, ServiceCode.DirectDeposit);
                    String qboeStatus = DDCodeToPSP.getQBOEServiceStatus(domainCompanyService);

                    tempResult.getMessages().
                            CompanyOperationNotAllowedForCurrentStatus(sourceSystemCd.toString(), sourceCompanyId, qboeStatus);
                }
            } else if ("1026".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanyPendingTerminationForService(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(),
                                    sourceCompanyId, ServiceCode.DirectDeposit.toString());
                }
            } else if ("1027".equals(pToQboeErrorCode)) {
                if (company != null) {
                    tempResult.getMessages().
                            CompanyPendingActivationForService(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(),
                                    sourceCompanyId, ServiceCode.DirectDeposit.toString());
                }
            } else {
                throw new RuntimeException("Unknown error code: " + pToQboeErrorCode);
            }

            qboeMessage = tempResult.getMessages().get(0);
            pProcessResult.getMessages().add(index, qboeMessage);
        }
    }

    public static Message findMessage(final ProcessResult pProcessResult, final String pMessageCode) {
        // Since the number of messages will generally be very small, we'll simply iterate over all messages
        for (Message message : pProcessResult.getMessages()) {
            if (message.getMessageCode().equals(pMessageCode)) {
                return message;
            }
        }
        return null;
    }


    public static ResponseStatus build_ResponseStatus(
            final ProcessResult pProcessResult,
            final String[] pExpectedErrorCodes) throws Exception {
        ResponseStatus responseStatus;
        ResponseMessage responseMessage = null;
        responseStatus = responseStatusFactory.createResponseStatus();

        //todo:v2 find somewhere to put the Severity codes instead of hard-coding here
        //Init the status to success and override later if necessary
        responseStatus.setStatus(0);

        Message errorMessage = null;

        // Search for error messages in the response in expected order
        for (String errorCode : pExpectedErrorCodes) {
            errorMessage = findMessage(pProcessResult, errorCode);
            if (errorMessage != null) {
                break;
            }
        }

        if (errorMessage != null) {
            responseMessage = responseMessageFactory.createResponseMessage();
            responseMessage.setCode(Integer.parseInt(errorMessage.getMessageCode()));
            responseMessage.setMessage(errorMessage.getMessage());
            responseStatus.setStatus(getMessageLevelEnumToAPI(errorMessage));
        } else if (pProcessResult.isSuccess() == false) {
            // Unexpected error message
            throw new RuntimeException("Unexpected error message: " + pProcessResult);
        }

        if (responseMessage != null) {
            responseStatus.getResponseMessage().add(responseMessage);
        }
        return responseStatus;
    }

    public static String generateContactKey(ContactDTO pContact) {
        String key = null;

        String contactRoleCode = pContact.getContactRoleCd().toString();
        contactRoleCode = (contactRoleCode == null) ? "" : contactRoleCode;

        key = contactRoleCode;

        String firstName = pContact.getFirstName();
        firstName = (firstName == null) ? "" : firstName;
        key += firstName;

        String lastName = pContact.getLastName();
        lastName = (lastName == null) ? "" : lastName;
        key += lastName;

        String middleName = pContact.getMiddleName();
        middleName = (middleName == null) ? "" : middleName;
        key += middleName;

        return key;
    }

    /**
     * 7/2/07 DM commented out
     * public static ResponseStatus buildErrorResponseStatus(BaseException e) throws WSException {
     * ResponseStatus responseStatus;
     * try {
     * responseStatus = responseStatusFactory.createResponseStatus();
     * responseStatus.setStatus(e.getSeverity());
     * ResponseMessage responseMessage = responseMessageFactory.createResponseMessage();
     * responseMessage.setCode(e.getErrorCode());
     * responseMessage.setMessage(e.getMessage());
     * responseMessage.setExceptionType(e.getClass().getName());
     * responseStatus.getResponseMessage().add(responseMessage);
     * Map argMap = e.getArgMap();
     * if (argMap != null && argMap.size() > 0) {
     * for (Iterator iterator = argMap.entrySet().iterator(); iterator.hasNext();) {
     * Map.Entry entry = (Map.Entry) iterator.next();
     * String key = (String) entry.getKey();
     * String value = (String) entry.getValue();
     * ResponseMessageData responseMessageData =
     * responseMessageDataFactory.createResponseMessageData();
     * responseMessageData.setName(key);
     * responseMessageData.setValue(value);
     * responseMessage.getResponseMessageData().add(responseMessageData);
     * }
     * }
     * }
     * catch (Throwable error) {
     * eventLogger.info(error);
     * throw new WSException(DDCommon.pse_Error, error);
     * }
     * return responseStatus;
     * }
     * <p/>
     * <p/>
     * /**
     * Builds the EmployeeInfo portion of the EmployeeRet xml
     *
     * @param pEmployee Domain Employee to encode into the XML
     * @return EmployeeInfo to insert into the returned XML
     * @throws Exception JaxbException whenever a error occurs creating the factory.  Shouldn't ever occur
     */
    protected static intuit.osp.pse.dd.wsapi.xsd.employeeinfo.EmployeeInfo build_EmployeeInfo(
            Employee pEmployee) throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.employeeinfo.ObjectFactory employeeInfoObjectFactory = new intuit.osp.pse.dd.wsapi.xsd.employeeinfo.ObjectFactory();
        intuit.osp.pse.dd.wsapi.xsd.employeeinfo.EmployeeInfo employeeinfo = employeeInfoObjectFactory
                .createEmployeeInfo();
        if (pEmployee != null) {
            employeeinfo.setEmployeeID(pEmployee.getSourceEmployeeId());
            employeeinfo.setLastName(pEmployee.getLastName());
            employeeinfo.setFirstName(pEmployee.getFirstName());
            if (pEmployee.getMiddleName() != null) {
                employeeinfo.setMiddleName(pEmployee.getMiddleName());
            }
            if (pEmployee.getTaxId() != null) {
                employeeinfo.setSocialSecurityNumber(pEmployee.getTaxId());
            }
            String employeeStatus = DDCodeToPSP.getQBOEEmployeeStatus(pEmployee.getStatusCd());
            employeeinfo.setEmployeeStatusCd(employeeStatus);
        }
        return employeeinfo;
    }

    protected static BankAccount build_BankAccountDO(
            intuit.osp.pse.dd.wsapi.xsd.bankaccount.BankAccount bankAccountDTO) throws Exception {
        BankAccount domainBankAccount = new BankAccount();
        if (bankAccountDTO != null) {
            domainBankAccount.setAccountNumber(bankAccountDTO.getAccountNumber());
            domainBankAccount.setRoutingNumber(bankAccountDTO.getRoutingNumber());
            String enumBankAccountTypeValue = (String) getBankAccountTypeAPIToEnum().get(
                    bankAccountDTO.getAccountType());
            domainBankAccount.setAccountTypeCd(BankAccountType.valueOf(enumBankAccountTypeValue));
            if (bankAccountDTO.getBankName() != null) {
                domainBankAccount.setBankName(bankAccountDTO.getBankName());
            }
        }
        return domainBankAccount;
    }


    protected static BankAccountDTO build_BankAccountDTO(
            intuit.osp.pse.dd.wsapi.xsd.bankaccount.BankAccount pbankAccountDDDTO) throws Exception {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        if (pbankAccountDDDTO != null) {
            bankAccountDTO.setAccountNumber(pbankAccountDDDTO.getAccountNumber());
            bankAccountDTO.setRoutingNumber(pbankAccountDDDTO.getRoutingNumber());
            String enumBankAccountTypeValue = (String) getBankAccountTypeAPIToEnum().get(
                    pbankAccountDDDTO.getAccountType());
            bankAccountDTO.setAccountType(BankAccountType.valueOf(enumBankAccountTypeValue));
            if (pbankAccountDDDTO.getBankName() != null) {
                bankAccountDTO.setBankName(pbankAccountDDDTO.getBankName());
            }
        }
        return bankAccountDTO;
    }

    protected static intuit.osp.pse.dd.wsapi.xsd.bankaccount.BankAccount build_BankAccount(
            BankAccount pBankAccountDO) throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.bankaccount.ObjectFactory bankAccountObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.bankaccount.ObjectFactory();
        intuit.osp.pse.dd.wsapi.xsd.bankaccount.BankAccount bankAccountDTO = bankAccountObjectFactory.createBankAccount();


        if (pBankAccountDO != null) {
            bankAccountDTO.setAccountNumber(pBankAccountDO.getAccountNumber());
            bankAccountDTO.setRoutingNumber(pBankAccountDO.getRoutingNumber());
            String bankAccountTypeAPICd = (String) getBankAccountTypeEnumToAPI().get(
                    pBankAccountDO.getAccountTypeCd().name());
            bankAccountDTO.setAccountType(bankAccountTypeAPICd);
            if (pBankAccountDO.getBankName() != null) {
                bankAccountDTO.setBankName(pBankAccountDO.getBankName());
            }
        }
        return bankAccountDTO;
    }

    protected static String getErrorMessage(String errorMsg, String[] args) {
        if (args != null) {
            // replace parameters
            String arg;
            for (int i = 0; i < args.length; i++) {
                if ((arg = args[i]) == null) {
                    arg = "";
                }
                errorMsg = errorMsg.replaceAll("\\{" + i + "\\}", arg);
            }
        }
        return errorMsg;
    }

    public static intuit.osp.pse.dd.wsapi.xsd.address.Address addressToXML(Address pDomainAddress) throws JAXBException {
        if (pDomainAddress != null) {
            intuit.osp.pse.dd.wsapi.xsd.address.ObjectFactory objectFactory =
                    new intuit.osp.pse.dd.wsapi.xsd.address.ObjectFactory();

            intuit.osp.pse.dd.wsapi.xsd.address.Address xmlAddress = objectFactory.createAddress();

            addressToXML(pDomainAddress, xmlAddress);

            return xmlAddress;
        } else {
            return null;
        }
    }

    public static void addressToXML(Address pDomainAddress, intuit.osp.pse.dd.wsapi.xsd.address.Address pXMLAddress)
            throws JAXBException {
        if (pDomainAddress != null) {
            pXMLAddress.setAddressLine1(pDomainAddress.getAddressLine1());
            pXMLAddress.setAddressLine2(pDomainAddress.getAddressLine2());
            pXMLAddress.setAddressLine3(pDomainAddress.getAddressLine3());
            pXMLAddress.setCity(pDomainAddress.getCity());
            pXMLAddress.setState(pDomainAddress.getState());
            pXMLAddress.setZipCode(pDomainAddress.getZipCode());
            pXMLAddress.setZipCodeExtension(pDomainAddress.getZipCodeExtension());
            pXMLAddress.setCountry(pDomainAddress.getCountry());
        }
    }

    public static intuit.osp.pse.dd.wsapi.xsd.contact.Contact contactToXML(Contact pDomainContact) throws JAXBException {
        if (pDomainContact != null) {
            intuit.osp.pse.dd.wsapi.xsd.contact.ObjectFactory objectFactory =
                    new intuit.osp.pse.dd.wsapi.xsd.contact.ObjectFactory();

            intuit.osp.pse.dd.wsapi.xsd.contact.Contact xmlContact = objectFactory.createContact();

            contactToXML(pDomainContact, xmlContact);

            if (xmlContact.getPhoneNumber() == null) {
                String args[] = {"phone number", "DDCommon.contactToXML"};
                throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
            } else if (xmlContact.getEmail() == null) {
                String args[] = {"email address", "DDCommon.contactToXML"};
                throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
            }

            return xmlContact;
        } else {
            return null;
        }
    }

    public static void contactToXML(Contact pDomainContact, intuit.osp.pse.dd.wsapi.xsd.contact.Contact pXMLContact)
            throws JAXBException {
        if (pDomainContact != null) {
            pXMLContact.setAddress(DDCommon.addressToXML(pDomainContact.getMailingAddress()));
            pXMLContact.setAccountSignatory(pDomainContact.getAuthSignerYnInd());
            pXMLContact.setContactRoleCd(DDCodeToPSP.getQBOEContactRole(pDomainContact.getContactRoleCd()));
            pXMLContact.setEmail(pDomainContact.getEmail());
            pXMLContact.setPhoneNumber(pDomainContact.getPhone());
            pXMLContact.setFirstName(pDomainContact.getFirstName());
            pXMLContact.setLastName(pDomainContact.getLastName());
            if (pDomainContact.getMiddleName() != null) {
                pXMLContact.setMiddleName(pDomainContact.getMiddleName());
            }
            CommunicationType ddCode = pDomainContact.getCommunicationTypePreference();
            if (ddCode != null) {
                pXMLContact.setCommunicationPref(DDCodeToPSP.getQBOECommunicationTypePreference(ddCode));
            }
        }
    }

    private static String formatEventDate(SpcfCalendar pDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(CalendarUtils.convertToCalendar(pDate).getTime());
    }

    /**
     * This is a convenience method that allows the retrieval of event details when the specific type of the
     * event is unknown.
     *
     * @param pEvent The CompanyEvent from which to retrieve the event details.
     * @return A sorted set of the event details for the given event, or null if the event could not be matched to an
     *         existing event type. Each event detail is represented as a name-value pair (String[2])
     */
    public static SortedSet<String[]> getEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {
        HashMap<String, String> eventDetailsHashMap = null;
        switch (pEvent.getEventTypeCd()) {
            case CompanyMatchesFraudulentCompany:
                eventDetailsHashMap = getFraudCompanyDetails(pEvent);
                break;
            case CompanyBankAccountStatusChange:
                eventDetailsHashMap = getCBAStatusChangeEventDetails(pEvent);
                break;
            case DDIncreasePayrollLimit:
                eventDetailsHashMap = getLimitIncreaseEventDetails(pEvent);
                break;
            case LimitViolation:
                eventDetailsHashMap = getLimitViolationEventDetails(pEvent);
                break;
            case ServiceStatusChange:
                eventDetailsHashMap = getServiceStatusChangeEventDetails(pEvent);
                break;
            case PayrollCancelled:
                eventDetailsHashMap = getPayrollCancelledEventDetails(pEvent);
                break;
            case Strike:
                eventDetailsHashMap = getStrikeEventDetails(pEvent);
                break;
            case ReversalRequested:
                eventDetailsHashMap = getReversalRequestedEventDetails(pEvent);
                break;
            case ReversalOK:
                eventDetailsHashMap = getReversalOkEventDetails(pEvent);
                break;
            case NOC:
                eventDetailsHashMap = getNOCEventDetails(pEvent);
                break;
            case ReversalReturn:
                eventDetailsHashMap = getReversalReturnEventDetails(pEvent);
                break;
            case FeeReturn:
                eventDetailsHashMap = getReturnEventDetails(pEvent);
                break;
            case ERRefundReturn:
                eventDetailsHashMap = getReturnEventDetails(pEvent);
                break;
            case CBAVerifyReturn:
                eventDetailsHashMap = getReturnEventDetails(pEvent);
                break;
            case DDDebitReturn:
                eventDetailsHashMap = getReturnEventDetails(pEvent);
                break;
            case NSF:
                eventDetailsHashMap = getReturnEventDetails(pEvent);
                break;
            case DDReject:
                eventDetailsHashMap = getReturnEventDetails(pEvent);
                break;
        }

        SortedSet<String[]> sortedEventDetailsSet = new TreeSet<String[]>(new Comparator<String[]>() {
            public int compare(String[] nv1, String[] nv2) {
                // compare keys
                int keysCompareResult = nv1[0].compareTo(nv2[0]);
                if (0 == keysCompareResult) {
                    // compare values
                    return nv1[1].compareTo(nv2[1]);
                }
                else {
                    return keysCompareResult;
                }
            }
        });

        if (eventDetailsHashMap != null) {
            for (String key : eventDetailsHashMap.keySet()) {
                String[] nameValuePair = new String[2];
                nameValuePair[0] = key;
                nameValuePair[1] = eventDetailsHashMap.get(key);
                sortedEventDetailsSet.add(nameValuePair);
            }
        }
        return sortedEventDetailsSet;
    }

    public static HashMap<String, String> getCBAStatusChangeEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {

        HashMap<String, String> eventDetails = new HashMap<String, String>();
        com.intuit.sbd.payroll.psp.domain.CompanyBankAccount companyBankAccount = PayrollServices.entityFinder.findById(com.intuit.sbd.payroll.psp.domain.CompanyBankAccount.class, SpcfUniqueId.createInstance(pEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId)));
        eventDetails.put("Company Bank Account ID", companyBankAccount.getSourceBankAccountId());

        switch (EnumUtils.getEnumForReadableName(BankAccountStatus.class, pEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldBAStatus))) {
            case Active:
                eventDetails.put("Old Status", "ACTV");
                break;
            case Inactive:
                eventDetails.put("Old Status", "INACTV");
                break;
            case PendingVerification:
                eventDetails.put("Old Status", "PNDVER");
                break;
        }

        switch (EnumUtils.getEnumForReadableName(BankAccountStatus.class, pEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewBAStatus))) {
            case Active:
                eventDetails.put("New Status", "ACTV");
                break;
            case Inactive:
                eventDetails.put("New Status", "INACTV");
                break;
            case PendingVerification:
                eventDetails.put("New Status", "PNDVER");
                break;
        }

        return eventDetails;

    }

    public static HashMap<String, String> getLimitIncreaseEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {

        HashMap<String, String> eventDetails = new HashMap<String, String>();
        DecimalFormat df = (DecimalFormat) DecimalFormat.getCurrencyInstance();

        df.applyPattern("000000.00");

        eventDetails.put("Source Payroll ID", pEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
        eventDetails.put("Old Limit Amount", df.format(new BigDecimal(pEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount))));
        eventDetails.put("New Limit Amount", df.format(new BigDecimal(pEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount))));

        switch ((EnumUtils.getEnumForReadableName(EventLimitCode.class, pEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType)))) {
            case Company:
                eventDetails.put("Limit Type", "Company");
                break;
            case Employee:
                eventDetails.put("Limit Type", "Employee");
                eventDetails.put("PSE Employee ID", pEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                break;
        }

        return eventDetails;
    }

    public static HashMap<String, String> getLimitViolationEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {
        HashMap<String, String> eventDetails = new HashMap<String, String>();

        for (CompanyEventDetail eventDetail : pEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case EmployeeId:
                    Employee employee = PayrollServices.entityFinder.findById(Employee.class, SpcfUniqueId.createInstance(eventDetail.getValue()));
                    eventDetails.put("PSE Employee ID", employee.getId().toString());
                    eventDetails.put("Employee Name", employee.getFirstName() + " " + employee.getLastName());
                    break;

                case PayrollRunId:
                    PayrollRun payrollRun = PayrollServices.entityFinder.findById(
                            PayrollRun.class, SpcfUniqueId.createInstance(eventDetail.getValue()));
                    eventDetails.put("Source Payroll ID", payrollRun.getSourcePayRunId());
                    break;

                default:
                    EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());
                    eventDetails.put(eventDetailType.getName(), eventDetail.getValue());
            }
        }

        return eventDetails;
    }

    public static HashMap<String, String> getServiceStatusChangeEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {

        ServiceSubStatusCode oldDDStatusCode = EnumUtils.getEnumForReadableName(ServiceSubStatusCode.class, pEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldServiceStatus));
        Collection<ServiceSubStatusCode> oldOnHoldReasonCodes = new ArrayList<ServiceSubStatusCode>();

        for (String oldOnHoldReasonValue : pEvent.getCompanyEventDetailValues(EventDetailTypeCode.OldOnHoldReason)) {
            oldOnHoldReasonCodes.add(EnumUtils.getEnumForReadableName(ServiceSubStatusCode.class, oldOnHoldReasonValue));
        }

        String oldDDStatus = DDCodeToPSP.getQBOEServiceStatus(oldDDStatusCode, oldOnHoldReasonCodes);

        ServiceSubStatusCode newDDStatusCode = EnumUtils.getEnumForReadableName(ServiceSubStatusCode.class, pEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewServiceStatus));
        Collection<ServiceSubStatusCode> newOnHoldReasonCodes = new ArrayList<ServiceSubStatusCode>();

        for (String newOnHoldReasonValue : pEvent.getCompanyEventDetailValues(EventDetailTypeCode.NewOnHoldReason)) {
            newOnHoldReasonCodes.add(EnumUtils.getEnumForReadableName(ServiceSubStatusCode.class, newOnHoldReasonValue));
        }

        String newDDStatus = DDCodeToPSP.getQBOEServiceStatus(newDDStatusCode, newOnHoldReasonCodes);

        HashMap<String, String> eventDetails = new HashMap<String, String>();

        if (null == oldDDStatus) {
            oldDDStatus = "";
        }

        if (null == newDDStatus) {
            newDDStatus = "";
        }
        eventDetails.put("Old Status", oldDDStatus);
        eventDetails.put("New Status", newDDStatus);

        return eventDetails;
    }

    public static HashMap<String, String> getPayrollCancelledEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {
        HashMap<String, String> eventDetails = new HashMap<String, String>();

        for (CompanyEventDetail eventDetail : pEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {

                case PayrollRunId:
                    PayrollRun payrollRun = PayrollServices.entityFinder.findById(
                            PayrollRun.class, SpcfUniqueId.createInstance(eventDetail.getValue()));
                    eventDetails.put("Source Batch ID", payrollRun.getSourcePayRunId());
                    break;

                default:
                    EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());
                    eventDetails.put(eventDetailType.getName(), eventDetail.getValue());
            }
        }

        return eventDetails;
    }

    public static HashMap<String, String> getStrikeEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {

        HashMap<String, String> eventDetails = new HashMap<String, String>();

        for (CompanyEventDetail eventDetail : pEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {

                case FinancialTransactionId:
                    FinancialTransaction financialTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId)));
                    if (financialTransaction != null) {
                        eventDetails.put("PSE Transaction ID", financialTransaction.getId().toString());
                    }
                    break;

                default:
                    EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());
                    eventDetails.put(eventDetailType.getName(), eventDetail.getValue());
            }
        }

        return eventDetails;
    }

    public static HashMap<String, String> getReversalRequestedEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {

        HashMap<String, String> eventDetails = new HashMap<String, String>();
        FinancialTransaction financialTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId)));

        String sourceDDTxnId = financialTransaction.getOriginalTransaction().getPaycheckSplit().getSourceDdTxnId();
        eventDetails.put("Source Transaction ID", sourceDDTxnId);
        eventDetails.put("Source Batch ID", financialTransaction.getPayrollRun().getSourcePayRunId());
        eventDetails.put("Transaction Date", formatEventDate(financialTransaction.getSettlementDate().toLocal()));

        return eventDetails;
    }

    public static HashMap<String, String> getReversalOkEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {

        HashMap<String, String> eventDetails = new HashMap<String, String>();
        for (CompanyEventDetail eventDetail : pEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {

                case FinancialTransactionId:
                    FinancialTransaction eeReversalDebit = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId)));
                    eventDetails.put("PSE Transaction ID", eeReversalDebit.getId().toString());
                    String sourceDDTxnId = eeReversalDebit.getOriginalTransaction().getPaycheckSplit().getSourceDdTxnId();
                    eventDetails.put("Source Transaction ID", sourceDDTxnId);
                    eventDetails.put("Source Batch ID", eeReversalDebit.getPayrollRun().getSourcePayRunId());
                    eventDetails.put("Transaction Date", formatEventDate(eeReversalDebit.getSettlementDate().toLocal()));
                    break;

                default:
                    EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());
                    eventDetails.put(eventDetailType.getName(), eventDetail.getValue());
            }
        }


        return eventDetails;
    }

    public static HashMap<String, String> getNOCEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {
        HashMap<String, String> eventDetails = new HashMap<String, String>();

        eventDetails.put("Return Type", "NOC");
        eventDetails.put("ACH Change CD", pEvent.getCompanyEventDetailValue(EventDetailTypeCode.ACHEventCd));

        String companyBankAccountId = pEvent.getCompanyEventDetailValue(EventDetailTypeCode.CompanyBankAccountId);
        String employeeBankAccountId = pEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
        if (companyBankAccountId != null) {
            com.intuit.sbd.payroll.psp.domain.CompanyBankAccount companyBankAccount = PayrollServices.entityFinder.findById(com.intuit.sbd.payroll.psp.domain.CompanyBankAccount.class, SpcfUniqueId.createInstance(companyBankAccountId));
            eventDetails.put("Bank Account Type", "Company");
            eventDetails.put("Bank Account ID", companyBankAccount.getSourceBankAccountId());
        } else if (employeeBankAccountId != null) {
            eventDetails.put("Bank Account Type", "Employee");
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));
            eventDetails.put("Bank Account ID", employeeBankAccount.getSourceBankAccountId());
            eventDetails.put("Source Employee ID", employeeBankAccount.getEmployee().getSourceEmployeeId());
        }

        if (!pEvent.getCompanyEventDetailValue(EventDetailTypeCode.ACHEventCd).equals("C04")) {
            eventDetails.put("Old Account Num", pEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));
            eventDetails.put("Old Routing Num", pEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldRoutingNumber));
            String newAccountNumber = pEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber);
            if (null != newAccountNumber) {
                eventDetails.put("New Account Num", newAccountNumber);
            } else {
                eventDetails.put("New Account Num", INVALID);
            }
            String newRoutingNumber = pEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewRoutingNumber);
            if (null != newRoutingNumber) {
                eventDetails.put("New Routing Num", newRoutingNumber);
            } else {
                eventDetails.put("New Routing Num", INVALID);
            }



            switch (EnumUtils.getEnumForReadableName(BankAccountType.class, pEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountType))) {
                case Checking:
                    eventDetails.put("Old AccountTypeCd", "C");
                    break;
                case Savings:
                    eventDetails.put("Old AccountTypeCd", "S");
                    break;
            }

            BankAccountType newAccountType = (EnumUtils.getEnumForReadableName(BankAccountType.class, pEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountType)));
            if (null == newAccountType) {
                eventDetails.put("New AccountTypeCd", INVALID);
            } else {
                switch (newAccountType) {
                    case Checking:
                        eventDetails.put("New AccountTypeCd", "C");
                        break;
                    case Savings:
                        eventDetails.put("New AccountTypeCd", "S");
                        break;
                }
            }
        }

        return eventDetails;
    }

    public static HashMap<String, String> getFraudCompanyDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {
        HashMap<String, String> eventDetails = new HashMap<String, String>();

        eventDetails.put("Details", pEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));
        eventDetails.put("Fraud Event Category", pEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));

        return eventDetails;
    }

    public static HashMap<String, String> getReturnEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {
        HashMap<String, String> eventDetails = new HashMap<String, String>();
        for (CompanyEventDetail eventDetail : pEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {

                case FinancialTransactionId:
                    //FinancialTransaction financialTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(CompanyEventBE.getCompanyEventDetailValue(pEvent, EventDetailTypeCode.FinancialTransactionId)));
                    FinancialTransaction financialTransaction =
                            PayrollServices.entityFinder.findById(FinancialTransaction.class,
                                                                  SpcfUniqueId.createInstance(eventDetail.getValue()));
                    eventDetails.put("PSE Transaction ID", financialTransaction.getId().toString());
                    if (null != financialTransaction.getPaycheckSplit()) {
                        eventDetails.put("Source Transaction ID", financialTransaction.getPaycheckSplit().getSourceDdTxnId());
                    }
                    if (null != financialTransaction.getPayrollRun()) {
                        eventDetails.put("Source Batch ID", financialTransaction.getPayrollRun().getSourcePayRunId());
                    }
                    eventDetails.put("Transaction Date", formatEventDate(financialTransaction.getSettlementDate().toLocal()));
                    break;

                case ACHReturnReasonCode:
                    // ignore it
                    break;

                case PayrollStatus:
                    // For DDDebitReturn events, PayrollStatus event details, change ReturnedTwice to DebitReturned
                    // to retain backward compatibility. otherwise, handle normally...
                    if (pEvent.getEventTypeCd() == EventTypeCode.DDDebitReturn) {
                        PayrollStatus status =  EnumUtils.getEnumForReadableName(PayrollStatus.class, eventDetail.getValue());
                        if (status == PayrollStatus.ReturnedTwice) {
                            EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());
                            eventDetails.put(eventDetailType.getName(), EnumUtils.getReadableName(PayrollStatus.DebitReturned));
                            break;
                        }
                    }

                    // fall-thru intentional...

                default:
                    EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());
                    eventDetails.put(eventDetailType.getName(), eventDetail.getValue());
            }
        }

        return eventDetails;
    }

    public static HashMap<String, String> getReversalReturnEventDetails(com.intuit.sbd.payroll.psp.domain.CompanyEvent pEvent) {
        HashMap<String, String> eventDetails = new HashMap<String, String>();
        for (CompanyEventDetail eventDetail : pEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {

                case FinancialTransactionId:
                    FinancialTransaction financialTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId)));

                    eventDetails.put("Source Transaction ID", financialTransaction.getOriginalTransaction().getPaycheckSplit().getSourceDdTxnId());
                    eventDetails.put("Source Batch ID", financialTransaction.getPayrollRun().getSourcePayRunId());
                    eventDetails.put("Transaction Date", formatEventDate(financialTransaction.getSettlementDate().toLocal()));
                    break;

                case ACHReturnReasonCode:
                    // ignore it
                    break;

                default:
                    EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());
                    eventDetails.put(eventDetailType.getName(), eventDetail.getValue());
            }
        }

        return eventDetails;
    }
    
    public static boolean isStatusUnchanged(final CompanySystemEventRet pEventRet) {
        if (pEventRet.getSystemEventCd().equals("DDSTATCHG")) {
            String oldStatus = null;
            String newStatus = null;
            for (SystemEventData systemEventData : (List<SystemEventData>) pEventRet.getSystemEventData()) {
                if (systemEventData.getName().equals("Old Status")) {
                    oldStatus = systemEventData.getValue();
                }
                if (systemEventData.getName().equals("New Status")) {
                    newStatus = systemEventData.getValue();
                }
            }
            return oldStatus.equals(newStatus);
        } else {
            throw new RuntimeException("The parameter is not a DDSTATCHG system event.");
        }
    }

    public static boolean isStatusUnchanged(final CompanyEventRet pEventRet) {
        if (pEventRet.getCompanyEventCd().equals("DDSTATCHG")) {
            if (pEventRet.getCompanyEventData().size() == 0) {
                // throw new RuntimeException("DDSTATCHG company event must contain event data");
                return false;
            }
            String oldStatus = null;
            String newStatus = null;
            for (SystemEventData systemEventData : (List<SystemEventData>) pEventRet.getCompanyEventData()) {
                if (systemEventData.getName().equals("Old Status")) {
                    oldStatus = systemEventData.getValue();
                }
                if (systemEventData.getName().equals("New Status")) {
                    newStatus = systemEventData.getValue();
                }
            }
            if (oldStatus != null) {
                return oldStatus.equals(newStatus);
            } else if (newStatus != null) {
                return newStatus.equals(oldStatus);
            } else {
                // both nulls
                return true;
            }
        } else {
            throw new RuntimeException("The parameter is not a DDSTATCHG system event.");
        }
    }
}

class FinancialTransactionComparator implements Comparator {
    public int compare(Object obj1, Object obj2) {
        FinancialTransaction finTxn1 = (FinancialTransaction) obj1;
        FinancialTransaction finTxn2 = (FinancialTransaction) obj2;
        return getFinTxnKey(finTxn1).compareTo(getFinTxnKey(finTxn2));
    }

    private String getFinTxnKey(FinancialTransaction pFinTxn) {
        String key = "";
        if (pFinTxn.getPayrollRun() != null) {
            key = pFinTxn.getPayrollRun().getSourcePayRunId();
        }
        if (pFinTxn.getPaycheckSplit() != null) {
            if (key.length() > 0) {
                key += ":";
            }
            key += pFinTxn.getPaycheckSplit().getSourceDdTxnId();
        }
        if (key.length() > 0) {
            key += ":";
        }
        key += pFinTxn.getCreatedDate().toString();
        return key;
    }
}
