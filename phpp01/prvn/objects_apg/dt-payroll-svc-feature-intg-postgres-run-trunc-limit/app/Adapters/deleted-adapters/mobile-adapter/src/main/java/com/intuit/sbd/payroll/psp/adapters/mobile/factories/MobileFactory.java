package com.intuit.sbd.payroll.psp.adapters.mobile.factories;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.BankAccountFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyEventFinder;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jan 11, 2011
 * Time: 11:07:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class MobileFactory {

    private static final String NOC = "Notice of Change (NOC)";
    private static final String DD_DEBIT_RETURN = "Direct Deposit Returned";
    private static final String DD_REJECT = "Direct Deposit Rejected";
    private static final String NSF = "Not Sufficient Funds (NSF)";

    private static Map<String, String> creditCardTypeMap;

    private static Map<String, String> mFilingStatusMap;

    static {
        creditCardTypeMap = new HashMap<String, String>();
        creditCardTypeMap.put("AMERICAN_EXPRESS", "American Express");
        creditCardTypeMap.put("American Express", "American Express");
        creditCardTypeMap.put("DISCOVER", "Discover");
        creditCardTypeMap.put("Discover", "Discover");
        creditCardTypeMap.put("MASTER_CARD", "Mastercard");
        creditCardTypeMap.put("Mastercard", "Mastercard");
        creditCardTypeMap.put("VISA", "Visa");
        creditCardTypeMap.put("Visa", "Visa");

        mFilingStatusMap = new HashMap<String, String>();
        mFilingStatusMap.put("Single","Single");
        mFilingStatusMap.put("MarriedFilingJointly","Married Filing Jointly");
        mFilingStatusMap.put("MarriedFilingSeparately","Married Filing Separately");
        mFilingStatusMap.put("UNKNOWN", "Unknown");
        mFilingStatusMap.put("0","Single");
        mFilingStatusMap.put("1","Married");
        mFilingStatusMap.put("2","Jointly");
        mFilingStatusMap.put("3","Head of Household");
        mFilingStatusMap.put("4","Widowed");
        mFilingStatusMap.put("5","A");
        mFilingStatusMap.put("6","B");
        mFilingStatusMap.put("7","C");
        mFilingStatusMap.put("8","D");
        mFilingStatusMap.put("9","E");
        mFilingStatusMap.put("10","Default");
        mFilingStatusMap.put("11","X");
        mFilingStatusMap.put("12","Count");
        //Testing Items
        mFilingStatusMap.put("SINGLE","Single");
        mFilingStatusMap.put("MARRIED","Married");

    }

    public static RSCompany createRSCompany(Company pCompany) {
        RSCompany rsCompany = new RSCompany();

        rsCompany.setPsid(pCompany.getSourceCompanyId());
        rsCompany.setLegalName(pCompany.getLegalName());
        rsCompany.setDba(pCompany.getDbaName());
        rsCompany.setCompanyEmail(pCompany.getNotificationEmail());
        rsCompany.setEin(pCompany.getFedTaxId());

        EntitlementUnit entitlementUnit = pCompany.getActivePrimaryEntitlementUnit();
        if (entitlementUnit != null) {

            rsCompany.setServiceKey(entitlementUnit.getServiceKey());
            rsCompany.setExtensionKey(entitlementUnit.getExtensionKey());

            Entitlement entitlement = entitlementUnit.getEntitlement();
            rsCompany.setSubscriptionNumber(entitlement.getSubscriptionNumber());
            rsCompany.setBillingEmail(entitlement.getContactEmail());
            rsCompany.setCreditCard(entitlement.getCreditCardNumber());
            String ccTypeValue = creditCardTypeMap.get(entitlement.getCreditCardType());
            rsCompany.setCreditCardType(ccTypeValue);
            rsCompany.setBillingContact(entitlement.getContactName());
            rsCompany.setBillingZip(entitlement.getBillingZipCode());

            if (entitlement.getSubscriptionEndDate() != null) {
                rsCompany.setSubscriptionEndDate(entitlement.getSubscriptionEndDate().format("MM/dd/yyyy"));
            }

            if (entitlement.getNextChargeDate() != null) {
                rsCompany.setNextChargeDate(entitlement.getNextChargeDate().format("MM/dd/yyyy"));
            }
        }

        RSAddress mailingAddress = createRSAddress(pCompany.getMailingAddress());
        mailingAddress.setAddressType(RSAddressTypeCode.Mailing);
        rsCompany.getAddresses().add(mailingAddress);

        RSAddress legalAddress = createRSAddress(pCompany.getMailingAddress());
        legalAddress.setAddressType(RSAddressTypeCode.Legal);
        rsCompany.getAddresses().add(legalAddress);

        rsCompany.getServices().addAll(createRSServices(pCompany));

        for (Contact contact : pCompany.getContactCollection().sort(Contact.ContactRoleCd())) {
            rsCompany.getContacts().add(createRSContact(contact));
        }

        CompanyBankAccount cba = BankAccountFinder.findCompanyBankAccount(pCompany);
        if (cba != null) {
            rsCompany.setBankAccount(createRSBankAccount(cba));
        }

        return rsCompany;
    }

    public static List<RSService> createRSServices(Company pCompany) {
        List<RSService> serviceList = new ArrayList<RSService>();

        RSService rsService;
        CompanyService companyService = CompanyService.findActiveCompanyServiceByCompanyAndServiceCode(pCompany, ServiceCode.Tax);
        if (companyService != null) {
            rsService = new RSService();
            rsService.setServiceCd("Assisted");
            rsService.setStatusCd(createRSServiceStatusCode(companyService.getStatusCd()));
            serviceList.add(rsService);

            companyService = CompanyService.findActiveCompanyServiceByCompanyAndServiceCode(pCompany, ServiceCode.CheckDistribution);
            if (companyService != null) {
                rsService = new RSService();
                rsService.setServiceCd("Check Distribution");
                rsService.setStatusCd(createRSServiceStatusCode(companyService.getStatusCd()));
                serviceList.add(rsService);
            }
        } else {
            companyService = CompanyService.findActiveCompanyServiceByCompanyAndServiceCode(pCompany, ServiceCode.DirectDeposit);
            if (companyService != null) {
                rsService = new RSService();
                rsService.setServiceCd("DIY/DD");
                rsService.setStatusCd(createRSServiceStatusCode(companyService.getStatusCd()));
                serviceList.add(rsService);

                companyService = CompanyService.findActiveCompanyServiceByCompanyAndServiceCode(pCompany, ServiceCode.BillPayment);
                if (companyService != null) {
                    rsService = new RSService();
                    rsService.setServiceCd("Bill Payment");
                    rsService.setStatusCd(createRSServiceStatusCode(companyService.getStatusCd()));
                    serviceList.add(rsService);
                }
            } else  {
                companyService = CompanyService.findActiveCompanyServiceByCompanyAndServiceCode(pCompany, ServiceCode.Cloud);
                if (companyService != null) {
                    rsService = new RSService();
                    rsService.setServiceCd("DIY");
                    rsService.setStatusCd(createRSServiceStatusCode(companyService.getStatusCd()));
                    serviceList.add(rsService);
                }
            }
        }

        companyService = CompanyService.findActiveCompanyServiceByCompanyAndServiceCode(pCompany, ServiceCode.ThirdParty401k);
        if (companyService != null) {
            rsService = new RSService();
            rsService.setServiceCd("401k");
            rsService.setStatusCd(createRSServiceStatusCode(companyService.getStatusCd()));
            serviceList.add(rsService);
        }

        return serviceList;
    }

    public static String createRSServiceStatusCode(ServiceSubStatusCode pServiceSubStatusCode) {
        switch (pServiceSubStatusCode) {
            case Cancelled:
                return "Cancelled";
            case Terminated:
                return "Terminated";
            case PendingBankVerification:
            case PendingPinCreation:
            case PendingSetup:
            case PendingBalanceFile:
                return "Pending Activation";
            default:
                return "Active";
        }
    }

    public static RSAddress createRSAddress(Address pAddress) {
        if (pAddress == null)
            return null;

        RSAddress rsAddress = new RSAddress();

        rsAddress.setAddressLine1(pAddress.getAddressLine1());
        rsAddress.setAddressLine2(pAddress.getAddressLine2());
        rsAddress.setCity(pAddress.getCity());
        rsAddress.setState(pAddress.getState());
        rsAddress.setZip(pAddress.getZipCode());

        return rsAddress;
    }

    public static RSContact createRSContact(Contact pContact) {
        RSContact rsContact = new RSContact();

        rsContact.setFirstName(pContact.getFirstName());
        rsContact.setMiddleName(pContact.getMiddleName());
        rsContact.setLastName(pContact.getLastName());
        rsContact.setEmail(pContact.getEmail());
        rsContact.setPrimaryPhone(pContact.getPhone());

        switch (pContact.getContactRoleCd()) {
            case PayrollAdmin:
                rsContact.setContactType("Payroll Admin");
                break;
            case PrimaryPrincipal:
                rsContact.setContactType("Primary Principal");
                break;
            case SecondaryPrincipal:
                rsContact.setContactType("Secondary Principal");
                break;
            case Other:
                rsContact.setContactType("Other");
                break;
        }

        return rsContact;
    }

    public static RSEvent createRSEvent(CompanyEvent pCompanyEvent) {
        if (pCompanyEvent == null)
            return null;

        RSEvent rsEvent = new RSEvent();
        rsEvent.setId(pCompanyEvent.getId().toString());
        rsEvent.setCreatedDate(pCompanyEvent.getCreatedDate().format("MM/dd/yyyy"));
        rsEvent.setStatus(pCompanyEvent.getStatusCd().toString());
        switch (pCompanyEvent.getEventTypeCd()) {
            case DDDebitReturn:
                rsEvent.setEventType(DD_DEBIT_RETURN);
                break;
            case NOC:
                rsEvent.setEventType(NOC);
                break;
            case DDReject:
                rsEvent.setEventType(DD_REJECT);
                break;
            case NSF:
                rsEvent.setEventType(NSF);
                break;
        }

        return rsEvent;
    }

    public static RSEvent createRSEventWithDetail(CompanyEvent pCompanyEvent) {
        RSEvent rsEvent = createRSEvent(pCompanyEvent);

        Collection<String> linkIds = null;

        switch (pCompanyEvent.getEventTypeCd()) {
            case NOC:
                linkIds = pCompanyEvent.getCompanyEventDetailValues(EventDetailTypeCode.CompanyBankAccountId, EventDetailTypeCode.EmployeeBankAccountId);

                if (!linkIds.isEmpty()) {
                    rsEvent.getLinkIdList().add(linkIds.iterator().next());
                }

                rsEvent.setDescription("A Notice of Change (NOC) is a payroll return that Intuit receives from employees' financial institutions when the banking information for their direct deposit is incorrect. This means that the employee receives the money for their paycheck, but Intuit is given a warning stating that the employee information needs to be updated.");
                rsEvent.setKbURL("http://payroll.intuit.com/support/kb/1000490.html");

                for (CompanyEventDetail companyEventDetails : pCompanyEvent.getCompanyEventDetailCollection()) {
                    switch (companyEventDetails.getEventDetailTypeCd()) {
                        case CompanyBankAccountId:
                            rsEvent.setLinkType(RSLinkType.EmployerBankAccount);
                            break;
                        case EmployeeBankAccountId:
                            rsEvent.setLinkType(RSLinkType.EmployeeBankAccount);
                            break;
                        case NewAccountNumber:
                        case NewAccountType:
                        case NewRoutingNumber:
                        case OldAccountNumber:
                        case OldAccountType:
                        case OldRoutingNumber:
                        case EmployeeName:
                            RSEventDetail rsEventDetail = new RSEventDetail();
                            rsEventDetail.setName(companyEventDetails.getEventDetailTypeCd().toString());
                            rsEventDetail.setValue(companyEventDetails.getValue());
                            rsEvent.getEventDetails().add(rsEventDetail);
                            break;
                    }
                }
                break;
            case DDDebitReturn:
                linkIds = pCompanyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);

                if (!linkIds.isEmpty()) {
                    if (linkIds.size() == 1) {
                        rsEvent.getLinkIdList().add(linkIds.iterator().next());
                        rsEvent.setLinkType(RSLinkType.EmployerTransaction);
                    } else {
                        SpcfUniqueId id = SpcfUniqueId.createInstance(linkIds.iterator().next());
                        FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, id);
                        rsEvent.getLinkIdList().add(financialTransaction.getPayrollRun().getId().toString());
                        rsEvent.setLinkType(RSLinkType.EmployerTransmission);
                    }
                }

                rsEvent.setDescription("When you send direct deposit payroll transactions, Intuit attempts to debit the bank account we have on file. If your bank rejects this attempted debit, this is called a bank return, and your direct deposit service with Intuit is suspended until we are able to collect the money associated with that payroll transaction. You will be unable to transmit further payrolls to Intuit until this issue is resolved. For information to resolve your specific bank return, refer to the email sent to you on the date of the return.");
                rsEvent.setKbURL("http://payroll.intuit.com/support/kb/2000321.html");
                break;
            case DDReject:
                linkIds = pCompanyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);

                if (!linkIds.isEmpty()) {
                    SpcfUniqueId id = SpcfUniqueId.createInstance(linkIds.iterator().next());
                    FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, id);

                    if (linkIds.size() == 1) {
                        rsEvent.getLinkIdList().add(linkIds.iterator().next());
                        rsEvent.setLinkType(RSLinkType.EmployeeTransaction);
                    } else {
                        rsEvent.getLinkIdList().add(financialTransaction.getPayrollRun().getId().toString());
                        rsEvent.setLinkType(RSLinkType.EmployeeTransmission);
                    }

                    String employeeName = null;
                    if (financialTransaction.getPaycheckSplit() != null) {
                        Paycheck paycheck = financialTransaction.getPaycheckSplit().getPaycheck();
                        if (paycheck.getDDEmployee() != null) {
                            employeeName = paycheck.getDDEmployee().getFullName();
                        } else
                        if (paycheck.getSourceEmployee() != null) {
                            employeeName = paycheck.getSourceEmployee().getFullName();
                        }
                    } else
                    if (financialTransaction.getBillPaymentSplit() != null) {
                        BillPayment billPayment = financialTransaction.getBillPaymentSplit().getBillPayment();
                        if (billPayment.getPayee() != null) {
                            employeeName = billPayment.getPayee().getName();
                        }
                    }

                    if (employeeName != null && employeeName.length() > 0) {
                        RSEventDetail rsEventDetail = new RSEventDetail();
                        rsEventDetail.setName("EmployeeName");
                        rsEventDetail.setValue(employeeName);
                        rsEvent.getEventDetails().add(rsEventDetail);
                    }
                }

                rsEvent.setDescription("When a Direct Deposit is rejected by an employee's bank, you must verify in QuickBooks that the employee's bank account and routing numbers match the numbers supplied to you by the employee and update if necessary, then issue a regular check (not a paycheck) to the employee, and enter a deposit to offset the rejected direct deposit.");
                rsEvent.setKbURL("http://payroll.intuit.com/support/kb/1000700.html");
                break;
            case NSF:
                linkIds = pCompanyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);

                if (!linkIds.isEmpty()) {
                    if (linkIds.size() == 1) {
                        rsEvent.getLinkIdList().add(linkIds.iterator().next());
                        rsEvent.setLinkType(RSLinkType.EmployerTransaction);
                    } else {
                        SpcfUniqueId id = SpcfUniqueId.createInstance(linkIds.iterator().next());
                        FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, id);
                        rsEvent.getLinkIdList().add(financialTransaction.getPayrollRun().getId().toString());
                        rsEvent.setLinkType(RSLinkType.EmployerTransmission);
                    }
                }

                rsEvent.setDescription("When you send payroll transactions, Intuit attempts to debit the bank account we have on file. If your bank rejects this attempted debit, this is called a bank return and your payroll service with Intuit is suspended until we are able to collect the money associated with that payroll transaction. This article explains how to address this situation. For information on how to resolve your specific bank return, refer to the email sent on the date of the return.");
                rsEvent.setKbURL("http://payroll.intuit.com/support/kb/1000054.html");
                break;
        }

        return rsEvent;
    }

    public static RSPayee createRSPayee(Payee pPayee) {
        RSPayee rsPayee = new RSPayee();

        rsPayee.setId(pPayee.getId().toString());
        rsPayee.setType(RSPayeeType.Vendor);
        rsPayee.setName(pPayee.getName());

        return rsPayee;
    }

    public static RSPayee createRSPayeeWithDetail(Payee pPayee) {
        RSPayee rsPayee = new RSPayee();

        rsPayee.setId(pPayee.getId().toString());
        rsPayee.setType(RSPayeeType.Vendor);
        rsPayee.setName(pPayee.getName());
        rsPayee.setEmail(pPayee.getEmail());
        rsPayee.setPhone(pPayee.getPhone());
        rsPayee.setIs1099(pPayee.getIs1099());
        rsPayee.setTaxId(pPayee.getTaxId());
        rsPayee.setMailingAddress(createRSAddress(pPayee.getMailingAddress()));

        for (PayeeBankAccount payeeBankAccount : pPayee.getPayeeBankAccountCollection()) {
            rsPayee.getBankAccounts().add(createRSBankAccount(payeeBankAccount)) ;
        }

        return rsPayee;
    }

    public static RSPayee createRSPayee(Employee pEmployee) {
        RSPayee rsPayee = new RSPayee();

        rsPayee.setType(RSPayeeType.Employee);
        rsPayee.setId(pEmployee.getId().toString());
        rsPayee.setFirstName(pEmployee.getFirstName());
        rsPayee.setMiddleName(pEmployee.getMiddleName());
        rsPayee.setLastName(pEmployee.getLastName());
        rsPayee.setSuffix(pEmployee.getSuffix());

        return rsPayee;
    }

    public static RSPayee createRSPayeeWithDetail(Employee pEmployee) {
        RSPayee rsPayee = new RSPayee();

        rsPayee.setId(pEmployee.getId().toString());
        rsPayee.setType(RSPayeeType.Employee);
        rsPayee.setFirstName(pEmployee.getFirstName());
        rsPayee.setMiddleName(pEmployee.getMiddleName());
        rsPayee.setLastName(pEmployee.getLastName());
        rsPayee.setSuffix(pEmployee.getSuffix());

        rsPayee.setEmail(pEmployee.getEmail());
        rsPayee.setPhone(pEmployee.getPhone());

        if (pEmployee.getGenderCd() != null) {
            rsPayee.setGender(Gender.Male.equals(pEmployee.getGenderCd()) ? RSGenderCode.Male : RSGenderCode.Female);
        }

        if (pEmployee.getBirthDate() != null) {
            rsPayee.setBirthDate(pEmployee.getBirthDate().format("MM/dd/yyyy"));
        }

        if (pEmployee.getHireDate() != null) {
            rsPayee.setHireDate(pEmployee.getHireDate().format("MM/dd/yyyy"));
        }

        for (EmployeeBankAccount employeeBankAccount : pEmployee.getEmployeeBankAccountCollection().sort(EmployeeBankAccount.StatusCd())
                                                                                                   .sort(EmployeeBankAccount.BankAccount().AccountTypeCd())) {
            rsPayee.getBankAccounts().add(createRSBankAccount(employeeBankAccount)) ;
        }

        for (EmployeeAccrual employeeAccrual : pEmployee.getEmployeeAccrualCollection().sort(EmployeeAccrual.AccrualType())) {
            switch (employeeAccrual.getAccrualType()) {
                case Vacation:
                    rsPayee.setVacation(String.valueOf(employeeAccrual.getHours()));
                    break;
                case Sick:
                    rsPayee.setSick(String.valueOf(employeeAccrual.getHours()));
                    break;
            }
        }

        rsPayee.setFederalFilingStatus(getFilingStatusDescription(pEmployee.getFedFilingStatus()));
        rsPayee.setFederalAllowances(String.valueOf(pEmployee.getFedAllowances()));
        rsPayee.setFederalAdditionalWithholding(String.valueOf(pEmployee.getFedExtraWithholding()));

        RSStateWithholding rsw4Item;
        for (EmployeeTax employeeTax : pEmployee.getEmployeeTaxCollection().sort(EmployeeTax.State())) {
            switch (employeeTax.getTaxType()) {
                case SIT:
                    rsw4Item = new RSStateWithholding();
                    rsw4Item.setType(employeeTax.getTaxType().toString());
                    rsw4Item.setState(employeeTax.getState());
                    rsw4Item.setFilingStatus(getFilingStatusDescription(employeeTax.getFilingStatus()));
                    rsw4Item.setAllowances(String.valueOf(employeeTax.getAllowances()));
                    rsw4Item.setAdditionalWithHolding(String.valueOf(employeeTax.getExtraWithholding()));
                    rsPayee.getStateWithholdings().add(rsw4Item);
                    break;
            }
        }

        return rsPayee;
    }

    public static RSBankAccount createRSBankAccount(CompanyBankAccount pCompanyBankAccount) {
        if (pCompanyBankAccount == null)
            return null;
        BankAccount bankAccount = pCompanyBankAccount.getBankAccount();

        RSBankAccount rsBankAccount = new RSBankAccount();
        rsBankAccount.setId(pCompanyBankAccount.getId().toString());
        rsBankAccount.setAccountNumber(bankAccount.getAccountNumber());
        rsBankAccount.setRoutingNumber(bankAccount.getRoutingNumber());
        rsBankAccount.setBankName(bankAccount.getBankName());
        rsBankAccount.setType(bankAccount.getAccountTypeCd().equals(BankAccountType.Checking)
                ? RSBankAccountTypeCode.Checking : RSBankAccountTypeCode.Savings);

        rsBankAccount.setStatus(RSBankAccountStatusCode.valueOf(pCompanyBankAccount.getStatusCd().toString()));

        return rsBankAccount;
    }

    public static RSBankAccount createRSBankAccount(PayeeBankAccount pPayeeBankAccount) {
        if (pPayeeBankAccount == null)
            return null;
        BankAccount bankAccount = pPayeeBankAccount.getBankAccount();

        RSBankAccount rsBankAccount = new RSBankAccount();
        rsBankAccount.setId(pPayeeBankAccount.getId().toString());
        rsBankAccount.setAccountNumber(bankAccount.getAccountNumber());
        rsBankAccount.setRoutingNumber(bankAccount.getRoutingNumber());
        rsBankAccount.setBankName(bankAccount.getBankName());
        rsBankAccount.setType(bankAccount.getAccountTypeCd().equals(BankAccountType.Checking)
                ? RSBankAccountTypeCode.Checking : RSBankAccountTypeCode.Savings);

        rsBankAccount.setStatus(RSBankAccountStatusCode.valueOf(pPayeeBankAccount.getStatusCd().toString()));

        return rsBankAccount;
    }

    public static RSBankAccount createRSBankAccount(EmployeeBankAccount pEmployeeBankAccount) {
        if (pEmployeeBankAccount == null)
            return null;
        BankAccount bankAccount = pEmployeeBankAccount.getBankAccount();

        RSBankAccount rsBankAccount = new RSBankAccount();
        rsBankAccount.setId(pEmployeeBankAccount.getId().toString());
        rsBankAccount.setAccountNumber(bankAccount.getAccountNumber());
        rsBankAccount.setRoutingNumber(bankAccount.getRoutingNumber());
        rsBankAccount.setBankName(bankAccount.getBankName());
        rsBankAccount.setType(bankAccount.getAccountTypeCd().equals(BankAccountType.Checking)
                ? RSBankAccountTypeCode.Checking : RSBankAccountTypeCode.Savings);

        rsBankAccount.setStatus(RSBankAccountStatusCode.valueOf(pEmployeeBankAccount.getStatusCd().toString()));

        return rsBankAccount;
    }

    public static RSTransmission createRSTransmission(PayrollRun pPayrollRun) {
        RSTransmission rsTransmission = new RSTransmission();

        switch (pPayrollRun.getPayrollRunType()) {
            case Regular:
                rsTransmission.setTransmissionType(RSTransmissionTypeCode.DD);
                break;
            case BillPayment:
                rsTransmission.setTransmissionType(RSTransmissionTypeCode.BillPayment);
                break;
            default:
                return null;
        }

        rsTransmission.setId(pPayrollRun.getId().toString());
        if (pPayrollRun.getPaycheckSettlementDate() != null)
            rsTransmission.setSettlementDate(pPayrollRun.getPaycheckSettlementDate().format("MM/dd/yyyy"));
        if (pPayrollRun.getPayrollRunDate() != null)
            rsTransmission.setRunDate(pPayrollRun.getPayrollRunDate().format("MM/dd/yyyy"));
        rsTransmission.setAmount(SpcfUtils.convertToBigDecimal(pPayrollRun.getPayrollDirectDepositAmount()));

        for (Paycheck paycheck : pPayrollRun.getPaycheckCollection()) {
            if (paycheck.getPayPeriodBeginDate() != null)
                rsTransmission.setPeriodStart(paycheck.getPayPeriodBeginDate().format("MM/dd/yyyy"));
            if (paycheck.getPayPeriodEndDate() != null)
                rsTransmission.setPeriodEnd(paycheck.getPayPeriodEndDate().format("MM/dd/yyyy"));
            break;
        }

        switch (pPayrollRun.getPayrollRunStatus()) {
            case Pending:
                rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Pending);
                break;
            case Canceled:    
                rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Cancelled);
                break;
            case Complete:
                rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Complete);
                break;
            case DebitReturned:
            case ReturnedTwice:
                rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Returned);
                break;
            default:
                rsTransmission.setTransmissionStatus(RSTransmissionStatusCode.Offloaded);
                break;
        }

        return rsTransmission;
    }

    public static RSFee createRSFee(FinancialTransaction pFinancialTransaction) {

        if (pFinancialTransaction == null) {
            return null;
        }

        RSFee rsFee = new RSFee();

        rsFee.setAmount(SpcfUtils.convertToBigDecimal(pFinancialTransaction.getFinancialTransactionAmount()));
        rsFee.setStatus(createRSTransactionSplitStatusCode(pFinancialTransaction.getCurrentTransactionState().getTransactionStateCd()));

        switch (pFinancialTransaction.getTransactionType().getTransactionTypeCd()) {
            case ServiceSalesAndUseTax:
                rsFee.setFeeType("SalesTax");
                break;
            case EmployerFeeDebit:
                rsFee.setFeeType("FeeDebit");
                break;
            case EmployerFeeRedebit:
                rsFee.setFeeType("FeeReDebit");
                break;
            case EmployerFeeRefundCredit:
                rsFee.setFeeType("FeeCredit");
                break;
        }

        return rsFee;
    }

    public static RSTransmission createRSTransmissionWithDetails(PayrollRun pPayrollRun) {
        RSTransmission rsTransmission = createRSTransmission(pPayrollRun);

        TransactionTypeCode txTypeCds[] = new TransactionTypeCode[6];
        txTypeCds[0] = TransactionTypeCode.EmployerFeeDebit;
        txTypeCds[1] = TransactionTypeCode.EmployerFeeRefundCredit;
        txTypeCds[2] = TransactionTypeCode.ServiceSalesAndUseTax;
        txTypeCds[3] = TransactionTypeCode.EmployerDdDebit;
        txTypeCds[4] = TransactionTypeCode.EmployerDdRejectRefundCredit;
        txTypeCds[5] = TransactionTypeCode.EmployerFeeRedebit;

        TransactionStateCode txStateCds[] = new TransactionStateCode[6];
        txStateCds[0] = TransactionStateCode.Cancelled;
        txStateCds[1] = TransactionStateCode.Completed;
        txStateCds[2] = TransactionStateCode.Voided;
        txStateCds[3] = TransactionStateCode.Created;
        txStateCds[4] = TransactionStateCode.Executed;
        txStateCds[5] = TransactionStateCode.Returned;

        CompanyBankAccount companyBankAccount = null;
        DomainEntitySet<FinancialTransaction> financialTransactions = pPayrollRun.getFinancialTransactions(txTypeCds, txStateCds);
        for (FinancialTransaction financialTransaction : financialTransactions) {
            RSEmployerTransaction rsEmployerTransaction = new RSEmployerTransaction();
            rsEmployerTransaction.setId(financialTransaction.getId().toString());
            rsEmployerTransaction.setNetAmount(SpcfUtils.convertToBigDecimal(financialTransaction.getFinancialTransactionAmount()));
            rsEmployerTransaction.setTransactionStatus(createRSTransactionSplitStatusCode(financialTransaction.getCurrentTransactionState().getTransactionStateCd()));

            switch (financialTransaction.getTransactionType().getTransactionTypeCd()) {
                case EmployerFeeDebit:
                    rsEmployerTransaction.setTransactionTypeCode(RSEmployerTransactionTypeCode.FeeDebit);
                    break;
                case EmployerFeeRefundCredit:
                    rsEmployerTransaction.setTransactionTypeCode(RSEmployerTransactionTypeCode.FeeCredit);
                    break;
                case ServiceSalesAndUseTax:
                    rsEmployerTransaction.setTransactionTypeCode(RSEmployerTransactionTypeCode.SalesTax);
                    break;
                case EmployerDdDebit:
                    rsEmployerTransaction.setTransactionTypeCode(RSEmployerTransactionTypeCode.DDDebit);
                    break;
                case EmployerDdRejectRefundCredit:
                    rsEmployerTransaction.setTransactionTypeCode(RSEmployerTransactionTypeCode.DdRejectRefundCredit);
                    break;
            }

            String txIds[] = new String[1];
            txIds[0] = financialTransaction.getId().toString();
            Collection<CompanyEvent> companyEvents = CompanyEventFinder.findCompanyEvents(pPayrollRun.getCompany(),
                    CompanyEventStatus.Active, EventDetailTypeCode.FinancialTransactionId, txIds);
            for (CompanyEvent companyEvent : companyEvents) {
                rsEmployerTransaction.getAlertIdList().add(companyEvent.getId().toString());
            }

            rsTransmission.getEmployerTransactions().add(rsEmployerTransaction);

            if (companyBankAccount == null) {
                BankAccount bankaccount = financialTransaction.getDebitBankAccount();
                companyBankAccount = CompanyBankAccount.findCompanyBankAccount
                        (pPayrollRun.getCompany(), financialTransaction.getDebitBankAccount());
            }
        }

        rsTransmission.setBankAccount(createRSBankAccount(companyBankAccount));

        for (Paycheck paycheck : pPayrollRun.getPaycheckCollection()) {
            RSEmployeeTransaction rsTransaction = new RSEmployeeTransaction();

            rsTransaction.setNetAmount(SpcfUtils.convertToBigDecimal(paycheck.getNetAmount()));
            rsTransaction.setId(paycheck.getId().toString());

            switch (paycheck.getStatus()) {
                case Active:
                    rsTransaction.setTransactionStatus(RSTransactionStatusCode.Active);
                    break;
                case Deleted:
                    rsTransaction.setTransactionStatus(RSTransactionStatusCode.Deleted);
                    break;
                case Inactive:
                    rsTransaction.setTransactionStatus(RSTransactionStatusCode.Inactive);
                    break;
            }

            if (paycheck.getDDEmployee() != null) {
                rsTransaction.setPayee(createRSPayee(paycheck.getDDEmployee()));
            } else
            if (paycheck.getSourceEmployee() != null){
                rsTransaction.setPayee(createRSPayee(paycheck.getSourceEmployee()));
            }

            rsTransmission.getEmployeeTransactions().add(rsTransaction);

            for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
                RSTransactionSplit rsTransactionSplit = new RSTransactionSplit();
                rsTransactionSplit.setAmount(SpcfUtils.convertToBigDecimal(paycheckSplit.getPaycheckSplitAmount()));
                rsTransactionSplit.setBankAccount(createRSBankAccount(paycheckSplit.getEmployeeBankAccount()));
                rsTransactionSplit.setStatus(createRSTransactionSplitStatusCode(
                        paycheckSplit.getFinancialTransaction().getCurrentTransactionState().getTransactionStateCd()));

                String txIds[] = new String[1];
                txIds[0] = paycheckSplit.getFinancialTransaction().getId().toString();
                Collection<CompanyEvent> companyEvents = CompanyEventFinder.findCompanyEvents(pPayrollRun.getCompany(), 
                        CompanyEventStatus.Active, EventDetailTypeCode.FinancialTransactionId, txIds);
                for (CompanyEvent companyEvent : companyEvents) {
                    rsTransactionSplit.getAlertIdList().add(companyEvent.getId().toString());
                }

                rsTransaction.getTransactionSplits().add(rsTransactionSplit);
            }
        }

        Collections.sort(rsTransmission.getEmployeeTransactions());

        return rsTransmission;
    }

    private static RSTransactionSplitStatusCode createRSTransactionSplitStatusCode(TransactionStateCode pTransactionStateCode) {
        if (pTransactionStateCode == null) {
            return null;
        }

        switch (pTransactionStateCode) {
            case Created:
                return RSTransactionSplitStatusCode.Created;
            case Cancelled:
                return RSTransactionSplitStatusCode.Cancelled;
            case Completed:
                return RSTransactionSplitStatusCode.Completed;
            case Returned:
                return RSTransactionSplitStatusCode.Returned;
            case Voided:
                return RSTransactionSplitStatusCode.Voided;
            case Executed:
                return RSTransactionSplitStatusCode.Executed;
            default:
                return null;
        }
    }

    public static RSPaycheck createRSPaycheck(Paycheck pPaycheck) {
        RSPaycheck rsPaycheck = new RSPaycheck();

        Employee employee;
        if (pPaycheck.getSourceEmployee() != null) {
            employee = pPaycheck.getSourceEmployee();
        } else {
            employee = pPaycheck.getDDEmployee();
        }

        if (employee.getQbdtEmployeeInfo() != null) {
            rsPaycheck.setName(employee.getQbdtEmployeeInfo().getPrintAsName());
        } else {
            rsPaycheck.setName(employee.getFullName());
        }

        for (EmployeeAccrual employeeAccrual : employee.getEmployeeAccrualCollection()) {
            RSPaycheckLineItem rsPaycheckLineItem = new RSPaycheckLineItem();
            switch (employeeAccrual.getAccrualType()) {
                case Sick:
                    rsPaycheckLineItem.setCurrentAmount(BigDecimal.valueOf(pPaycheck.getQbdtPaycheckInfo().getSickHoursAccrued()));
                    break;
                case Vacation:
                    rsPaycheckLineItem.setCurrentAmount(BigDecimal.valueOf(pPaycheck.getQbdtPaycheckInfo().getSickHoursAccrued()));
                    break;
            }
            rsPaycheckLineItem.setDescription(employeeAccrual.getAccrualType().toString());
            rsPaycheckLineItem.setYtdAmount(BigDecimal.valueOf(employeeAccrual.getHours()));
            rsPaycheck.getAccruals().add(rsPaycheckLineItem);
        }

        for (Compensation compensation : pPaycheck.getCompensationCollection().sort(Compensation.PayStubOrder())) {
            RSPaycheckLineItem rsPaycheckLineItem = new RSPaycheckLineItem();
            if (compensation.getCompanyPayrollItem() != null) {
                rsPaycheckLineItem.setDescription(compensation.getCompanyPayrollItem().getSourceDescription());
            }
            rsPaycheckLineItem.setCurrentAmount(SpcfUtils.convertToBigDecimal(compensation.getCompensationAmount()));
            rsPaycheckLineItem.setCurrentAmount(SpcfUtils.convertToBigDecimal(compensation.getCompensationAmount()));
            rsPaycheck.getCompensations().add(rsPaycheckLineItem);
        }

        for (Deduction deduction : pPaycheck.getDeductionCollection().sort(Deduction.PayStubOrder())) {
            RSPaycheckLineItem rsPaycheckLineItem = new RSPaycheckLineItem();

            if (deduction.getCompanyPayrollItem() != null) {
                rsPaycheckLineItem.setDescription(deduction.getCompanyPayrollItem().getSourceDescription());
            }
            rsPaycheckLineItem.setCurrentAmount(SpcfUtils.convertToBigDecimal(deduction.getDeductionAmount()));
            rsPaycheckLineItem.setCurrentAmount(SpcfUtils.convertToBigDecimal(deduction.getDeductionYTDAmount()));

            switch (deduction.getCompanyPayrollItem().getPayrollItem().getPayrollItemCode()) {
                case OtherPreTaxDeduction:
                case Tp401kEmployeeDeferral:
                    rsPaycheck.getPreTaxDeductions().add(rsPaycheckLineItem);
                    break;
                case OtherAdditionPostTax:
                case OtherPostTaxDeduction:
                case Tp401kRoth:
                case Tp401kLoanPayment:
                    rsPaycheck.getAfterTaxDeductions().add(rsPaycheckLineItem);
                    break;
            }
        }

        for (Tax tax : pPaycheck.getTaxCollection().sort(Tax.PayStubOrder())) {
            RSPaycheckLineItem rsPaycheckLineItem = new RSPaycheckLineItem();
            if (tax.getCompanyLaw() != null) {
                rsPaycheckLineItem.setDescription(tax.getCompanyLaw().getSourceDescription());
            }
            rsPaycheckLineItem.setCurrentAmount(SpcfUtils.convertToBigDecimal(tax.getTaxLiabilityAmount()));
            rsPaycheckLineItem.setYtdAmount(SpcfUtils.convertToBigDecimal(tax.getTaxLiabilityYTDAmount()));
            rsPaycheck.getTaxes().add(rsPaycheckLineItem);
        }

        for (EmployerContribution employerContribution : pPaycheck.getEmployerContributionCollection().sort(EmployerContribution.PayStubOrder())) {
            RSPaycheckLineItem rsPaycheckLineItem = new RSPaycheckLineItem();
            if (employerContribution.getCompanyPayrollItem() != null) {
                rsPaycheckLineItem.setDescription(employerContribution.getCompanyPayrollItem().getSourceDescription());
            }
            rsPaycheckLineItem.setCurrentAmount(SpcfUtils.convertToBigDecimal(employerContribution.getContributionAmount()));
            rsPaycheckLineItem.setCurrentAmount(SpcfUtils.convertToBigDecimal(employerContribution.getContributionYTDAmount()));
            rsPaycheck.getEmployerContributions().add(rsPaycheckLineItem);
        }

        for (PaycheckSplit paycheckSplit : pPaycheck.getPaycheckSplits().sort(PaycheckSplit.PaycheckSplitAmount())) {
            RSPaycheckSplit rsPaycheckSplit = new RSPaycheckSplit();
            rsPaycheckSplit.setAmount(SpcfUtils.convertToBigDecimal(paycheckSplit.getPaycheckSplitAmount()));
            rsPaycheckSplit.setBankAccount(createRSBankAccount(paycheckSplit.getEmployeeBankAccount()));
            rsPaycheckSplit.setStatus(createRSTransactionSplitStatusCode(paycheckSplit.getFinancialTransaction().getCurrentTransactionState().getTransactionStateCd()).toString());
        }

        rsPaycheck.setNetAmount(SpcfUtils.convertToBigDecimal(pPaycheck.getNetAmount()));
        rsPaycheck.setGrossAmount(SpcfUtils.convertToBigDecimal(pPaycheck.getGrossAmount()));

        return rsPaycheck;
    }

    public static String getFilingStatusDescription(String pFilingStatusCode) {
        String filingStatus = null;
        if (mFilingStatusMap.containsKey(pFilingStatusCode)) {
            filingStatus = mFilingStatusMap.get(pFilingStatusCode);
        }
        return filingStatus;
    }


}
