package com.intuit.sbd.payroll.psp.batchjobs.iop;

import com.intuit.onlinepayroll.webservices.v1.*;
import com.intuit.onlinepayroll.webservices.v1.TransactionType;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Gender;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jeff Jones
 */
public class PSPFactory {

    private static final String DEFAULT_EIN = "999999999";

    private static final String REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://webservices.onlinepayroll.intuit.com/v1\">\n"+
        "   <soapenv:Header/>\n"+
        "   <soapenv:Body>\n"+
        "      <v1:getPayrollCompanyModel>\n"+
        "         <companyID>%s</companyID>\n"+
        "         <startDateTime>%s</startDateTime>\n"+
        "         <endDateTime>%s</endDateTime>\n"+
        "      </v1:getPayrollCompanyModel>\n"+
        "   </soapenv:Body>\n"+
        "</soapenv:Envelope>";

    public static CompanyDTO createCompanyDTO(CompanyModel pCompanyModel){
        CompanyDTO companyDTO = new CompanyDTO();

        companyDTO.setSourceSystemCd(SourceSystemCode.IOP);
        companyDTO.setCompanyId(String.valueOf(pCompanyModel.getId()));

        String legalName = null;
        AddressModel legalAddress = null;
        CompanyTaxSetupModel companyTaxSetupModel = pCompanyModel.getCompanyTaxSetup();
        if (companyTaxSetupModel != null) {
            if (Validator.isValidEIN(companyTaxSetupModel.getFederalEIN())) {
                companyDTO.setFein(companyTaxSetupModel.getFederalEIN());
            } else {
                companyDTO.setFein(DEFAULT_EIN);
            }

            legalName = companyTaxSetupModel.getFilingName();
            legalAddress = companyTaxSetupModel.getFilingAddress();
        }

        AddressModel mailingAddress = pCompanyModel.getBusinessAddress();
        if (mailingAddress != null) {
            companyDTO.setMailingAddress(createAddressDTO(mailingAddress));
        } else if (legalAddress != null)  {
            companyDTO.setMailingAddress(createAddressDTO(legalAddress));
        }
        if (legalAddress != null) {
            companyDTO.setLegalAddress(createAddressDTO(legalAddress));
        } else if (mailingAddress != null) {
            companyDTO.setLegalAddress(createAddressDTO(mailingAddress));
        }

        if (pCompanyModel.getBusinessName() == null) {
            companyDTO.setDBA(legalName);
        } else {
            companyDTO.setDBA(pCompanyModel.getBusinessName());
        }
        if (legalName == null) {
            companyDTO.setLegalName(pCompanyModel.getBusinessName());
        } else {
            companyDTO.setLegalName(legalName);
        }

        PrimaryContactModel primaryContactModel = pCompanyModel.getPrimaryContact();
        if (primaryContactModel != null) {
            ContactDTO contactDTO = createContactDTO(pCompanyModel);
            companyDTO.getContacts().add(contactDTO);
            if (contactDTO.getEmail() != null) {
                companyDTO.setNotificationEmail(contactDTO.getEmail());
            } else {
                for (Phone phone : primaryContactModel.getPhones()) {
                    if (phone.getPerson() != null && phone.getPerson().getEmailAddress() != null) {
                        companyDTO.setNotificationEmail(phone.getPerson().getEmailAddress());
                        break;
                    }
                }
            }
        }

        if (pCompanyModel.getSignupDate() != null) {
            DateDTO signupDate = createDateDTO(pCompanyModel.getSignupDate());
            companyDTO.setSignUpDate(signupDate);

            int diff = CalendarUtils.getDifferenceInDays(PSPDate.getPSPTime(), signupDate.toSpcfCalendar());
            if (diff >= 60) {
                QuickbooksInfoDTO qbInfoDTO = new QuickbooksInfoDTO();
                qbInfoDTO.setPayrollCount(6);
                companyDTO.setQuickBooksInfo(qbInfoDTO);
            }
        }

        return companyDTO;
    }

    public static void updateCompanyDTO(CompanyDTO pCompanyDTO, CompanyModel pCompanyModel){        
        pCompanyDTO.setDBA(pCompanyModel.getBusinessName());

        String legalName = null;
        AddressModel legalAddress = null;
        CompanyTaxSetupModel companyTaxSetupModel = pCompanyModel.getCompanyTaxSetup();
        if (companyTaxSetupModel != null) {
            if (Validator.isValidEIN(companyTaxSetupModel.getFederalEIN())) {
                pCompanyDTO.setFein(companyTaxSetupModel.getFederalEIN());
            }

            legalName = companyTaxSetupModel.getFilingName();
            legalAddress = companyTaxSetupModel.getFilingAddress();
        }

        AddressModel mailingAddress = pCompanyModel.getBusinessAddress();
        if (mailingAddress != null) {
            pCompanyDTO.setMailingAddress(createAddressDTO(mailingAddress));
        } else if (legalAddress != null)  {
            pCompanyDTO.setMailingAddress(createAddressDTO(legalAddress));
        }
        if (legalAddress != null) {
            pCompanyDTO.setLegalAddress(createAddressDTO(legalAddress));
        } else if (mailingAddress != null) {
            pCompanyDTO.setLegalAddress(createAddressDTO(mailingAddress));
        }

        if (pCompanyModel.getBusinessName() == null) {
            pCompanyDTO.setDBA(legalName);
        } else {
            pCompanyDTO.setDBA(pCompanyModel.getBusinessName());
        }
        if (legalName == null) {
            pCompanyDTO.setLegalName(pCompanyModel.getBusinessName());
        } else {
            pCompanyDTO.setLegalName(legalName);
        }

        PrimaryContactModel primaryContactModel = pCompanyModel.getPrimaryContact();
        if (primaryContactModel != null) {
            ContactDTO contactDTO = pCompanyDTO.getContacts().iterator().next();
            updateContactDTO(contactDTO, pCompanyModel);
            if (contactDTO.getEmail() != null) {
                pCompanyDTO.setNotificationEmail(contactDTO.getEmail());
            } else {
                for (Phone phone : primaryContactModel.getPhones()) {
                    if (phone.getPerson() != null && phone.getPerson().getEmailAddress() != null) {
                        pCompanyDTO.setNotificationEmail(phone.getPerson().getEmailAddress());
                        break;
                    }
                }
            }
        }

        if (pCompanyModel.getSignupDate() != null) {
            DateDTO signupDate = createDateDTO(pCompanyModel.getSignupDate());
            pCompanyDTO.setSignUpDate(signupDate);

            int diff = CalendarUtils.getDifferenceInDays(PSPDate.getPSPTime(), signupDate.toSpcfCalendar());
            if (diff >= 60) {
                QuickbooksInfoDTO qbInfoDTO =  pCompanyDTO.getQuickBooksInfo();
                if (qbInfoDTO == null) {
                    qbInfoDTO = new QuickbooksInfoDTO();
                    pCompanyDTO.setQuickBooksInfo(qbInfoDTO);
                }
                qbInfoDTO.setPayrollCount(6);                
            }
        }
    }

    public static AddressDTO createAddressDTO(AddressModel pAddressModel) {
        AddressDTO addressDTO = new AddressDTO();

        addressDTO.setAddressLine1(pAddressModel.getAddress1());
        addressDTO.setAddressLine2(pAddressModel.getAddress2());
        addressDTO.setCity(pAddressModel.getCity());
        addressDTO.setState(pAddressModel.getState());
        addressDTO.setZipCode(pAddressModel.getZip());

        return addressDTO;
    }

    public static void updateAddressDTO(AddressDTO pAddressDTO, AddressModel pAddressModel) {
        if (pAddressModel.getAddress1() != null) {
            pAddressDTO.setAddressLine1(pAddressModel.getAddress1());
        }
        if (pAddressModel.getAddress2() != null) {
            pAddressDTO.setAddressLine2(pAddressModel.getAddress2());
        }
        if (pAddressModel.getCity() != null) {
            pAddressDTO.setCity(pAddressModel.getCity());
        }
        if (pAddressModel.getState() != null) {
            pAddressDTO.setState(pAddressModel.getState());
        }
        if (pAddressModel.getZip() != null) {
            pAddressDTO.setZipCode(pAddressModel.getZip());
        }
    }

    public static ContactDTO createContactDTO(CompanyModel pCompanyModel) {
        ContactDTO contactDTO = new ContactDTO();

        contactDTO.setContactRoleCd(ContactRole.PrimaryPrincipal);
        contactDTO.setAccountSignatory(true);

        PrimaryContactModel primaryContactModel = pCompanyModel.getPrimaryContact();
        contactDTO.setContactId(String.valueOf(primaryContactModel.getId()));
        contactDTO.setFirstName(primaryContactModel.getFirstName());
        contactDTO.setMiddleName(primaryContactModel.getMiddleInitial());
        contactDTO.setLastName(primaryContactModel.getLastName());

        if (primaryContactModel.getEMailAddress() != null) {
            contactDTO.setEmail(primaryContactModel.getEMailAddress());
        } else if (primaryContactModel.getEMailAddress2() != null) {
            contactDTO.setEmail(primaryContactModel.getEMailAddress2());
        }

        String workPhone = null;
        String altPhone = null;
        for (Phone phone : primaryContactModel.getPhones()) {
            switch (phone.getPhoneType()) {
                case WORK:
                case WORK_2:
                    if(workPhone == null) {
                        workPhone = phone.getPhoneNumber();
                        break;
                    }
                default:
                    if(altPhone == null) {
                        altPhone = phone.getPhoneNumber();
                    }
            }
        }

        if (workPhone != null) {
            contactDTO.setPhoneNumber(workPhone);
        } else {
            contactDTO.setPhoneNumber(altPhone);
        }

        return contactDTO;
    }

    public static void updateContactDTO(ContactDTO pContactDTO, CompanyModel pCompanyModel) {
        pContactDTO.setContactRoleCd(ContactRole.PrimaryPrincipal);
        pContactDTO.setAccountSignatory(true);

        PrimaryContactModel primaryContactModel = pCompanyModel.getPrimaryContact();
        pContactDTO.setContactId(String.valueOf(primaryContactModel.getId()));
        pContactDTO.setFirstName(primaryContactModel.getFirstName());
        pContactDTO.setMiddleName(primaryContactModel.getMiddleInitial());
        pContactDTO.setLastName(primaryContactModel.getLastName());

        if (primaryContactModel.getEMailAddress() != null) {
            pContactDTO.setEmail(primaryContactModel.getEMailAddress());
        } else if (primaryContactModel.getEMailAddress2() != null) {
            pContactDTO.setEmail(primaryContactModel.getEMailAddress2());
        }

        String workPhone = null;
        String altPhone = null;
        for (Phone phone : primaryContactModel.getPhones()) {
            switch (phone.getPhoneType()) {
                case WORK:
                case WORK_2:
                    if(workPhone == null) {
                        workPhone = phone.getPhoneNumber();
                        break;
                    }
                default:
                    if(altPhone == null) {
                        altPhone = phone.getPhoneNumber();
                    }
            }
        }

        if (workPhone != null) {
            pContactDTO.setPhoneNumber(workPhone);
        } else {
            pContactDTO.setPhoneNumber(altPhone);
        }
    }

    public static CompanyBankAccountDTO createCompanyBankAccountDTO(BankAccountModel pBankAccountModel) {
        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();

        companyBankAccountDTO.setCompanyBankAccountID(SpcfUniqueId.generateRandomUniqueIdString());

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        companyBankAccountDTO.setBankAccountDTO(bankAccountDTO);

        bankAccountDTO.setAccountNumber(pBankAccountModel.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccountModel.getBankRoutingNumber());
        bankAccountDTO.setBankName(pBankAccountModel.getBankName());

        if (pBankAccountModel.getBankAccountType() != null) {
            switch (pBankAccountModel.getBankAccountType()) {
                case CHECKING:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Checking);
                    break;
                case SAVINGS:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Savings);
                    break;
            }
        }

        return companyBankAccountDTO;
    }

    public static void updateCompanyBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO, BankAccountModel pBankAccountModel) {

        BankAccountDTO bankAccountDTO = pCompanyBankAccountDTO.getBankAccountDTO();

        bankAccountDTO.setAccountNumber(pBankAccountModel.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccountModel.getBankRoutingNumber());
        bankAccountDTO.setBankName(pBankAccountModel.getBankName());

        if (pBankAccountModel.getBankAccountType() != null) {
            switch (pBankAccountModel.getBankAccountType()) {
                case CHECKING:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Checking);
                    break;
                case SAVINGS:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Savings);
                    break;
            }
        }
    }

    public static EmployeeDTO createEmployeeDTO(EmployeeModel pEmployeeModel) {
        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setEmployeeId(String.valueOf(pEmployeeModel.getId()));
        employeeDTO.setFirstName(pEmployeeModel.getFirstName());
        employeeDTO.setMiddleName(pEmployeeModel.getMiddleInitial());
        employeeDTO.setLastName(pEmployeeModel.getLastName());
        employeeDTO.setSocialSecurityNumber(pEmployeeModel.getSocialSecurityNumber());
        employeeDTO.setHireDate(createDateDTO(pEmployeeModel.getHireDate()));
        employeeDTO.setBirthDate(createDateDTO(pEmployeeModel.getBirthDate()));
        employeeDTO.setTerminationDate(createDateDTO(pEmployeeModel.getTerminationDate()));

        if (pEmployeeModel.getGender() != null) {
            switch (pEmployeeModel.getGender()) {
                case FEMALE:
                    employeeDTO.setGender(Gender.Female);
                    break;
                case MALE:
                    employeeDTO.setGender(Gender.Male);
                    break;
            }
        }

        return employeeDTO;
    }

    public static PayeeDTO createPayeeDTO(ContractorModel pContractorModel) {
        PayeeDTO payeeDTO = new PayeeDTO();

        payeeDTO.setSourcePayeeId(String.valueOf(pContractorModel.getId()));

        String phone = null;
        String email = null;

        if (pContractorModel.getContact() != null) {
            email = pContractorModel.getContact().getEmailAddress();

            if (pContractorModel.getContact().getPhones() != null && pContractorModel.getContact().getPhones().size() > 0) {
                phone = pContractorModel.getContact().getPhones().get(0).getPhoneNumber();
            }
        }

        payeeDTO.setEmail(email);
        payeeDTO.setPhone(phone);

        AddressDTO addressDTO = null;

        if (pContractorModel.getAddressModel() != null) {
            addressDTO = createAddressDTO(pContractorModel.getAddressModel());
        }

        payeeDTO.setMailingAddress(addressDTO);
        payeeDTO.setIs1099(true);


        if (pContractorModel.getType() == 1) {
            // The incoming contractor is a company
            payeeDTO.setName(pContractorModel.getBusinessName());
            payeeDTO.setTaxId(pContractorModel.getTIN());
        } else if (pContractorModel.getType() == 2) {
            // The incoming contractor is an individual
            payeeDTO.setName(pContractorModel.getFirstName() + " " + pContractorModel.getLastName());
            payeeDTO.setTaxId(pContractorModel.getSocialSecurityNumber());
        } else {
            throw new RuntimeException("Invalid Contractor type found.  Contractor Type:" +
                    pContractorModel.getType());
        }

        return payeeDTO;
    }

    public static void updateEmployeeDTO(EmployeeDTO pEmployeeDTO, EmployeeModel pEmployeeModel) {

        pEmployeeDTO.setEmployeeId(String.valueOf(pEmployeeModel.getId()));
        pEmployeeDTO.setFirstName(pEmployeeModel.getFirstName());
        pEmployeeDTO.setMiddleName(pEmployeeModel.getMiddleInitial());
        pEmployeeDTO.setLastName(pEmployeeModel.getLastName());
        pEmployeeDTO.setSocialSecurityNumber(pEmployeeModel.getSocialSecurityNumber());
        pEmployeeDTO.setHireDate(createDateDTO(pEmployeeModel.getHireDate()));
        pEmployeeDTO.setBirthDate(createDateDTO(pEmployeeModel.getBirthDate()));
        pEmployeeDTO.setTerminationDate(createDateDTO(pEmployeeModel.getTerminationDate()));

        if (pEmployeeModel.getGender() != null) {
            switch (pEmployeeModel.getGender()) {
                case FEMALE:
                    pEmployeeDTO.setGender(Gender.Female);
                    break;
                case MALE:
                    pEmployeeDTO.setGender(Gender.Male);
                    break;
            }
        }
    }

    public static EmployeeBankAccountDTO createEmployeeBankAccountDTO(String pSourceBankAccountId, BankAccountModel pBankAccountModel) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();

        employeeBankAccountDTO.setEmployeeBankAccountId(pSourceBankAccountId);

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        employeeBankAccountDTO.setBankAccount(bankAccountDTO);

        bankAccountDTO.setAccountNumber(pBankAccountModel.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccountModel.getBankRoutingNumber());
        bankAccountDTO.setBankName(pBankAccountModel.getBankName());

        if (pBankAccountModel.getBankAccountType() != null) {
            switch (pBankAccountModel.getBankAccountType()) {
                case CHECKING:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Checking);
                    break;
                case SAVINGS:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Savings);
                    break;
            }
        }

        return employeeBankAccountDTO;
    }

    public static PayeeBankAccountDTO createPayeeBankAccountDTO(String pSourceBankAccountId, BankAccountModel pBankAccountModel) {
        PayeeBankAccountDTO payeeBankAccountDTO = new PayeeBankAccountDTO();

        payeeBankAccountDTO.setPayeeBankAccountId(pSourceBankAccountId);

        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        payeeBankAccountDTO.setBankAccount(bankAccountDTO);

        bankAccountDTO.setAccountNumber(pBankAccountModel.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccountModel.getBankRoutingNumber());
        bankAccountDTO.setBankName(pBankAccountModel.getBankName());

        if (pBankAccountModel.getBankAccountType() != null) {
            switch (pBankAccountModel.getBankAccountType()) {
                case CHECKING:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Checking);
                    break;
                case SAVINGS:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Savings);
                    break;
            }
        }

        return payeeBankAccountDTO;
    }

    public static void updateEmployeeBankAccountDTO(EmployeeBankAccountDTO pEmployeeBankAccountDTO, BankAccountModel pBankAccountModel) {
        BankAccountDTO bankAccountDTO = pEmployeeBankAccountDTO.getBankAccount();

        bankAccountDTO.setAccountNumber(pBankAccountModel.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccountModel.getBankRoutingNumber());
        bankAccountDTO.setBankName(pBankAccountModel.getBankName());

        if (pBankAccountModel.getBankAccountType() != null) {
            switch (pBankAccountModel.getBankAccountType()) {
                case CHECKING:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Checking);
                    break;
                case SAVINGS:
                    bankAccountDTO.setAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType.Savings);
                    break;
            }
        }
    }

    public static PayrollRunDTO createPayrollRunDTO(XMLGregorianCalendar pCheckDate, 
                                                    String pTransmissionId,
                                                    CompanyBankAccountDTO pCompanyBankAccountDTO,
                                                    List<PaycheckDTO> pPaycheckDTOList) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        payrollRunDTO.setPayrollTXBatchId(SpcfUniqueId.generateRandomUniqueIdString());
        payrollRunDTO.setTransmissionId(pTransmissionId);
        payrollRunDTO.setTargetPayrollTXDate(createDateDTO(pCheckDate));
        payrollRunDTO.setSettlementDate(createDateDTO(pCheckDate));
        payrollRunDTO.setCompanyBankAccounts(new ArrayList<ServiceBankAccountDTO>());
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(ServiceCode.RiskAssessment);
        payrollRunDTO.getCompanyBankAccounts().add(serviceBankAccountDTO);

        for (PaycheckDTO paycheckDTO : pPaycheckDTOList) {
            payrollRunDTO.getPaychecks().add(paycheckDTO);
        }

        return payrollRunDTO;
    }

    public static PaycheckDTO createPaycheckDTO(PaycheckModel pPaycheckModel,
                                                Map<String, Map<String, EmployeeBankAccountDTO>> pEmployeeBankAccountList) {
        PaycheckDTO paycheckDTO = new PaycheckDTO();

        paycheckDTO.setPaycheckId(String.valueOf(pPaycheckModel.getId()));
        paycheckDTO.setPayPeriodBeginDate(createDateDTO(pPaycheckModel.getPeriodStartDate()));
        paycheckDTO.setPayPeriodEndDate(createDateDTO(pPaycheckModel.getPeriodEndDate()));

        String employeeId = String.valueOf(pPaycheckModel.getEmployee().getId());
        paycheckDTO.setEmployeeId(employeeId);
        paycheckDTO.setPaycheckNetAmount(SpcfUtils.convertToSpcfMoney(pPaycheckModel.getNetCheckAmount()));
        paycheckDTO.setPaycheckGrossAmount(SpcfUtils.convertToSpcfMoney(pPaycheckModel.getGrossAmount()));

        paycheckDTO.setDdTransactions(new ArrayList<DDTransactionDTO>());
        if (pPaycheckModel.getDdAmount() != null && pPaycheckModel.getDdAmount().doubleValue() > 0) {
            EmployeeBankAccountDTO employeeBankAccountDTO = pEmployeeBankAccountList.get(employeeId).get("DirectDepositAccount1");
            paycheckDTO.getDdTransactions().add(createDDTransactionDTO(pPaycheckModel.getDdAmount(), employeeBankAccountDTO));
        }

        if (pPaycheckModel.getDdAmount2() != null && pPaycheckModel.getDdAmount2().doubleValue() > 0) {
            EmployeeBankAccountDTO employeeBankAccountDTO = pEmployeeBankAccountList.get(employeeId).get("DirectDepositAccount2");
            paycheckDTO.getDdTransactions().add(createDDTransactionDTO(pPaycheckModel.getDdAmount2(), employeeBankAccountDTO));
        }

        //TODO if there is time add pay line items

        return paycheckDTO;
    }

    public static BillPaymentDTO createPaymentDTO(ContractorPaymentModel pContractorPaymentModel, ContractorPaymentCompanyModel pContractorPaymentCompanyModel) {
        BillPaymentDTO paymentDTO = new BillPaymentDTO();

        paymentDTO.setBillPaymentId(String.valueOf(pContractorPaymentModel.getId()));
        paymentDTO.setAmount(SpcfUtils.convertToSpcfMoney(pContractorPaymentModel.getGrossAmount()));
        paymentDTO.setMemo(pContractorPaymentModel.getMemo());
        paymentDTO.setDepositDate(createDateDTO(pContractorPaymentModel.getCheckDate()));

        // Find real ContractorModel that corresponds to ContractorPaymentModel
        // This works around malformed ContractorModel that is in ContractorPaymentModel object
        ContractorModel foundContractorModel = null;

        for (ContractorModel contractorModel : pContractorPaymentCompanyModel.getContractors()) {
            if (contractorModel.getId() == pContractorPaymentModel.getContractor().getId()) {
                foundContractorModel = contractorModel;
            }
        }

        if (foundContractorModel == null) {
            // Contractor not found in list, throw exception
            StringBuilder listOfIds = new StringBuilder();

            for (ContractorModel contractorModel : pContractorPaymentCompanyModel.getContractors()) {
                listOfIds.append(contractorModel.getId()).append(", ");
            }

            throw new RuntimeException(String.format("Contractor Id %d Not found in Payment Id %d Possible Contractor Ids:%s",
                    pContractorPaymentModel.getContractor().getId(), pContractorPaymentModel.getId(), listOfIds));
        }
        
        paymentDTO.setPayeeDTO(createPayeeDTO(foundContractorModel));

        // IOP only supports paying bills, not writing checks
        paymentDTO.setTransactionType(BillPaymentTransactionType.PayBills);

        ArrayList<BillPaymentSplitDTO> billPaymentSplitDTOs = new ArrayList<BillPaymentSplitDTO>();
        BillPaymentSplitDTO billPaymentSplitDTO = new BillPaymentSplitDTO();
        // Another workaround for IOP sending in amounts with 4 decimal places "0.0000"
        billPaymentSplitDTO.setAmount(pContractorPaymentModel.getGrossAmount().setScale(2));
        billPaymentSplitDTO.setBillPaymentSplitId(String.valueOf(pContractorPaymentModel.getId()));

        BankAccountModel bankAccountModel = foundContractorModel.getDirectDepositAccount1();

        if (bankAccountModel == null) {
            throw new RuntimeException("PaymentModel sent in without a BankAccountModel.  The PaymentModel id is " + pContractorPaymentModel.getId());
        }

        billPaymentSplitDTO.setPayeeBankAccount(createPayeeBankAccountDTO(IOPProcessor.getDdAccount1Name(), bankAccountModel));
        billPaymentSplitDTO.setReferenceNumber(String.valueOf(pContractorPaymentModel.getId()));
        billPaymentSplitDTOs.add(billPaymentSplitDTO);
        paymentDTO.setPaymentTransactions(billPaymentSplitDTOs);

        //TODO if there is time add pay line items

        return paymentDTO;
    }

    private static DDTransactionDTO createDDTransactionDTO(BigDecimal pAmount, EmployeeBankAccountDTO pEmployeeBankAccountDTO) {
        DDTransactionDTO ddTransactionDTO = new DDTransactionDTO();

        ddTransactionDTO.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ddTransactionDTO.setDDTransactionAmount(pAmount.setScale(2));
        ddTransactionDTO.setEmployeeBankAccount(pEmployeeBankAccountDTO);

        return ddTransactionDTO;
    }


    public static SourceSystemTransmissionDTO createBeginningTransmission(String pSourceCompanyId, SpcfCalendar pStart, SpcfCalendar pEnd) {
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();

        String soapRequest = String.format(REQUEST, pSourceCompanyId, pStart.toString(), pEnd.toString());
        sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.PSP);
        sourceSystemTransmissionDTO.setRequestDocument(soapRequest);
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.PayrollSubmission);
        sourceSystemTransmissionDTO.setToSourceSystem(SourceSystemCode.IOP);
        sourceSystemTransmissionDTO.setRequestToken(0l);
        sourceSystemTransmissionDTO.setResponseToken(0l);

        return sourceSystemTransmissionDTO;
    }


    public static DateDTO createDateDTO(DateModel pDateModel) {
        if (pDateModel == null) {
            return null;
        }

        return new DateDTO(pDateModel.getYear(), pDateModel.getMonth(), pDateModel.getDay());
    }

    public static DateDTO createDateDTO(XMLGregorianCalendar pXMLGregorianCalendar) {
        if (pXMLGregorianCalendar == null) {
            return null;
        }

        return new DateDTO(pXMLGregorianCalendar.getYear(), pXMLGregorianCalendar.getMonth(), pXMLGregorianCalendar.getDay());
    }
}
