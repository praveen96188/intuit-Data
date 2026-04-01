package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.FormTemplateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: May 4, 2009
 * Time: 1:33:50 PM
 */
public class TaxTranslator {


    public static void getSAPAgencyFromDomainEntity(Agency agency,  ArrayList<SAPAgency> pAgencyList, Boolean pShowUnsupported) {
        SAPAgency sapAgency = new SAPAgency();
        sapAgency.setAgencyId(agency.getAgencyId());
        sapAgency.setAgencyAbbrev(agency.getAgencyAbbrev());

        ArrayList<SAPPaymentTemplate> paymentTemplates = new ArrayList<SAPPaymentTemplate>();

        for(PaymentTemplate paymentTemplate : agency.getPaymentTemplateCollection()) {
            if (pShowUnsupported || (paymentTemplate.getSupportStartDate() != null)) {
                paymentTemplates.add(getPaymentTemplateFromDomainEntity(paymentTemplate));
            }
        }
        if(paymentTemplates.size() > 0){
            sapAgency.setPaymentTemplates(paymentTemplates);
            pAgencyList.add(sapAgency);
        }
    }

    public static SAPAgency getSAPAgencyFromDomainEntity(Agency agency, DomainEntitySet<PaymentTemplate> paymentTemplates) {
        SAPAgency sapAgency = new SAPAgency();
        sapAgency.setAgencyId(agency.getAgencyId());
        sapAgency.setAgencyAbbrev(agency.getAgencyAbbrev());
        sapAgency.setAgencyName(agency.getName());

        ArrayList<SAPPaymentTemplate> sapPaymentTemplates = new ArrayList<SAPPaymentTemplate>();
        if (paymentTemplates != null) {
            for (PaymentTemplate paymentTemplate : paymentTemplates) {
                if (paymentTemplate.getSupportStartDate() != null) {
                    sapPaymentTemplates.add(getPaymentTemplateFromDomainEntity(paymentTemplate));
                }
            }
        }

        sapAgency.setPaymentTemplates(sapPaymentTemplates);

        return sapAgency;
    }

    public static SAPPaymentTemplate getPaymentTemplateFromDomainEntity(PaymentTemplate paymentTemplate){
        SAPPaymentTemplate sapPaymentTemplate = new SAPPaymentTemplate();
        sapPaymentTemplate.setPaymentTemplateCd(paymentTemplate.getPaymentTemplateCd());
        sapPaymentTemplate.setPaymentTemplateName(paymentTemplate.getPaymentTemplateAbbrev());
        sapPaymentTemplate.setAgencyName(paymentTemplate.getAgency().getAgencyId());
        sapPaymentTemplate.setSupportStartDate(SAPTranslator.getDateFromSpcfCalendar(paymentTemplate.getSupportStartDate()));
        sapPaymentTemplate.setProcessingStartDate(SAPTranslator.getDateFromSpcfCalendar(paymentTemplate.getProcessingStartDate()));
        sapPaymentTemplate.setCanBeFinalized(paymentTemplate.getCategory() == PaymentTemplateCategory.SUI);
        sapPaymentTemplate.setFollowsFedDepositFrequency(paymentTemplate.isFollowsFederal());
        for (PaymentTemplateAgencyId agencyId : paymentTemplate.getAgencyIds()) {
            sapPaymentTemplate.getAgencyIDs().add(agencyId.getId().toString());
        }
        ArrayList<SAPLawItem> sapLawItems = new ArrayList<SAPLawItem>();
        for(Law law : paymentTemplate.getLawCollection()){
            if (! law.shouldExcludeFromUI()) {
                sapLawItems.add(getLawItemsFromDomainEntity(law));
            }

        }

        sapPaymentTemplate.setLawItems(sapLawItems);

        ArrayList<String> possibleDf = new ArrayList<String>();
        if (! paymentTemplate.getNonModifiableFrequency()) {
            //if the agent can't modify, then the list will be empty
            for (PaymentTemplateFrequency ptf : paymentTemplate.getPaymentTemplateFrequencyCollection()) {
                possibleDf.add(ptf.getPaymentFrequencyId().toString());
            }
        }
        sapPaymentTemplate.setPossibleDepositFrequencies(possibleDf);

        return sapPaymentTemplate;
    }


    public static SAPPaymentTemplate getPaymentTemplateFromDomainEntity(CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate){
        SAPPaymentTemplate sapPaymentTemplate = new SAPPaymentTemplate();
        sapPaymentTemplate.setPaymentTemplateCd(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd());
        sapPaymentTemplate.setPaymentTemplateName(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateAbbrev());
        sapPaymentTemplate.setSupportStartDate(SAPTranslator.getDateFromSpcfCalendar(companyAgencyPaymentTemplate.getPaymentTemplate().getSupportStartDate()));
        sapPaymentTemplate.setProcessingStartDate(SAPTranslator.getDateFromSpcfCalendar(companyAgencyPaymentTemplate.getPaymentTemplate().getProcessingStartDate()));
        sapPaymentTemplate.setFollowsFedDepositFrequency(companyAgencyPaymentTemplate.getPaymentTemplate().isFollowsFederal());
        return sapPaymentTemplate;
    }

    public static SAPLawItem getLawItemsFromDomainEntity(Law law){
        SAPLawItem sapLawItem = new SAPLawItem();
        sapLawItem.setLawId(law.getLawId());
        sapLawItem.setDescription(law.getDescription());
        sapLawItem.setName(law.getLawAbbrev());
        sapLawItem.setNegativeLiability(law.isIRSCreditLaw());
        sapLawItem.setPaymentTemplateCd(law.getPaymentTemplate().getPaymentTemplateCd());
        return sapLawItem;
    }

    public static SAPPaymentTemplateQuarterPayment createQuarterPayment(String quarter,
                                                                        String year,
                                                                        String paymentTemplateName,
                                                                        String paymentTemplateCd){
        SAPPaymentTemplateQuarterPayment quarterPayment = new SAPPaymentTemplateQuarterPayment();
        quarterPayment.setQuarter(quarter);
        quarterPayment.setYear(year);
        quarterPayment.setPaymentTemplateName(paymentTemplateName);
        quarterPayment.setPaymentTemplateCd(paymentTemplateCd);
        return quarterPayment;
    }

    public static SAPTaxPaymentCheckDateSet getTaxPaymentCheckDateSetFromDomainEntities(ArrayList<FinancialTransaction> financialTransactions) {
        SAPTaxPaymentCheckDateSet sapTaxPaymentCheckDateSet = new SAPTaxPaymentCheckDateSet();
        SpcfDecimal total = new SpcfMoney("0.00");
        ArrayList<SAPPaymentDetails> sapPaymentDetailsCollection = new ArrayList<SAPPaymentDetails>();
        for(FinancialTransaction financialTransaction : financialTransactions){
            SAPPaymentDetails details = getPaymentDetails(financialTransaction);
            if (details != null) {
                total = SpcfUtils.add(total, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(details.getAmount()));
                sapPaymentDetailsCollection.add(details);
            }
        }
        sapTaxPaymentCheckDateSet.setAgencyTransactions(sapPaymentDetailsCollection);
        sapTaxPaymentCheckDateSet.setCheckDateTotal(SAPTranslator.getDoubleFromSpcfMoneyNullZero(total));
        return sapTaxPaymentCheckDateSet;
    }


    public static SAPDepositFrequency getDepositFrequencyFromDomainEntity(EffectiveDepositFrequency edf) {
        SAPDepositFrequency df = new SAPDepositFrequency();
        if (edf != null) {
            if (edf.getPaymentTemplateFrequency().getPaymentFrequencyId()!=null) {
                df.setDepositFrequency(edf.getPaymentTemplateFrequency().getPaymentFrequencyId().toString());
                if (edf.getPaymentTemplateFrequency().getObsolete() || edf.getPaymentTemplateFrequency().getAgentDisallowed()) {
                    df.setObsoleteFrequency(df.getDepositFrequency());
                }
            } else {
                df.setDepositFrequency(null);
            }
            df.setEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(edf.getEffectiveDate()));
        }
        return df;
    }

    public static SAPPayment getPayment(MoneyMovementTransaction moneyMovementTransaction, String status, List<SAPPaymentMethod> paymentMethods){

        SAPPayment payment = new SAPPayment();

        payment.setPaymentId(moneyMovementTransaction.getId().toString());

        payment.setCompanyKey(new SAPCompanyKey(moneyMovementTransaction.getCompany()));
        payment.setPsId(moneyMovementTransaction.getCompany().getSourceCompanyId());
        payment.setEin(moneyMovementTransaction.getCompany().getFedTaxId());
        payment.setCompanyName(moneyMovementTransaction.getCompany().getLegalName());

        payment.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(moneyMovementTransaction.getMoneyMovementTransactionAmount()));

        payment.setSettlementDate(SAPTranslator.getDateFromSpcfCalendar(moneyMovementTransaction.getSettlementDate()));
        payment.setInitiationDate(SAPTranslator.getDateFromSpcfCalendar(moneyMovementTransaction.getInitiationDate()));
        payment.setDueDate(SAPTranslator.getDateFromSpcfCalendar(moneyMovementTransaction.getDueDate()));

        payment.setStatus(status);



        payment.setPaymentMethod(moneyMovementTransaction.getMoneyMovementPaymentMethodString());
        if(moneyMovementTransaction.getPaymentTemplate() != null){
            payment.setPaymentType(moneyMovementTransaction.getPaymentTemplate().getPaymentTemplateAbbrev());
        }
        if(moneyMovementTransaction.getAgencyForMoneyMovementTransaction() != null){
            payment.setAgencyName(moneyMovementTransaction.getAgencyForMoneyMovementTransaction().getAgencyAbbrev());
            payment.setAgencyId(moneyMovementTransaction.getAgencyTaxpayerId());                    
        }

        DomainEntitySet<TaxPaymentOnHoldReason> taxPaymentOnHoldReasons = Application.find(TaxPaymentOnHoldReason.class, TaxPaymentOnHoldReason.MoneyMovementTransaction().equalTo(moneyMovementTransaction).And(TaxPaymentOnHoldReason.MoneyMovementTransaction().Company().equalTo(moneyMovementTransaction.getCompany())).And(TaxPaymentOnHoldReason.ExpirationDate().isNull()));

        for (TaxPaymentOnHoldReason taxPaymentOnHoldReason : taxPaymentOnHoldReasons) {
            payment.addHold(taxPaymentOnHoldReason.getOnHoldReasonCd().toString());
            if (taxPaymentOnHoldReason.getOnHoldReasonCd() == PaymentOnHoldReason.Agent) {
                payment.setManualHoldReason(taxPaymentOnHoldReason.getNote());
                payment.setManualHoldCreator(SAPTranslator.getUserNameFromUserID(taxPaymentOnHoldReason.getCreatorId()));
            }
        }

        if (moneyMovementTransaction.getPaymentFrequency() != null && moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId() != null) {
            payment.setPaymentFrequency(moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId().toString());
        } else {
            payment.setPaymentFrequency(null);
        }

        payment.setIsPending(moneyMovementTransaction.isPendingTaxPayment());

        payment.setQuarter(getSAPQuarter(moneyMovementTransaction.getPaymentPeriodEnd()));

        payment.setMmtStatus(moneyMovementTransaction.getStatus().toString());
        payment.setTaxPaymentStatus(moneyMovementTransaction.getTaxPaymentStatus().toString());
        payment.setPeriodBegin(SAPTranslator.getDateFromSpcfCalendarNoTime(moneyMovementTransaction.getPaymentPeriodBegin()));
        payment.setPeriodEnd(SAPTranslator.getDateFromSpcfCalendarNoTime(moneyMovementTransaction.getPaymentPeriodEnd()));
        payment.setCrossesQuarters(!new SAPQuarter(moneyMovementTransaction.getPaymentPeriodBegin()).equals(new SAPQuarter(moneyMovementTransaction.getPaymentPeriodEnd())));

        if (paymentMethods != null) {
            populatePaymentMethodProperties(payment, paymentMethods, moneyMovementTransaction);
        }

        return payment;
    }

    public static void populatePaymentMethodProperties(SAPPayment payment, List<SAPPaymentMethod> paymentMethods, MoneyMovementTransaction moneyMovementTransaction){
        List<String> requirements = new ArrayList<String>();
        String currentPaymentMethod = moneyMovementTransaction.getMoneyMovementPaymentMethodString();
        String highestPriorityPaymentMethod = "";

        for(SAPPaymentMethod paymentMethodCounter : paymentMethods){
            if(paymentMethodCounter.getPaymentMethodOrder() == 1){
                requirements = paymentMethodCounter.getRequirements();
                highestPriorityPaymentMethod = paymentMethodCounter.getPaymentMethodName();
                break;
            }
        }
        if(!currentPaymentMethod.equalsIgnoreCase(highestPriorityPaymentMethod))   {

            payment.setNotPriorityPaymentMethodReasons(requirements);
            payment.setHighestPriorityPaymentMethod(highestPriorityPaymentMethod);
            if(!(payment.getPaymentType().equalsIgnoreCase("IRS-940") || payment.getPaymentType().equalsIgnoreCase("IRS-941/944"))){
                payment.setIsNotPriorityPaymentMethod(true);
            } else {
                payment.setIsNotPriorityPaymentMethod(false);
            }

            Company company = Company.findCompany(moneyMovementTransaction.getCompany().getSourceCompanyId(), moneyMovementTransaction.getCompany().getSourceSystemCd());
            DomainEntitySet<CompanyEvent> companyEventList = CompanyEvent.findCompanyEventsEagerLoad(company, EventTypeCode.PaymentMethodChanged).sort(CompanyEvent.<CompanyEvent>CreatedDate().Descending());
            AuthUser user = null;
            for (CompanyEvent companyEvent : companyEventList) {
                String mmtId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.UniqueIdentifier);
                if (mmtId.equals(moneyMovementTransaction.getId().toString())) {
                    user = AuthUser.findUser(companyEvent.getModifierId());
                    break;
                }
            }
            if(user != null)
                payment.getNotPriorityPaymentMethodReasons().add("Modified by: " + user.getFirstName() + " " + user.getLastName());
        }
    }

    //returns null if does not impact balance
    public static SAPPaymentDetails getPaymentDetails(FinancialTransaction financialTransaction){

        SAPPaymentDetails details = new SAPPaymentDetails();

        details.setFtId(financialTransaction.getId().toString());

        SpcfMoney amount = financialTransaction.getFinancialTransactionAmount();

        if (amount.equals(SpcfMoney.ZERO)) {
            return null;
        }

        TransactionTypeCode transactionTypeCd = financialTransaction.getTransactionType().getTransactionTypeCd();
        if (TransactionType.subtractsFromPayment(transactionTypeCd)) {
            amount = (SpcfMoney) amount.negate();
        } else if (!TransactionType.addsToPayment(transactionTypeCd)) {
            return null;
        }

        details.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount));
        details.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(financialTransaction.getCreatedDate()));
        details.setCheckDate(SAPTranslator.getDateFromSpcfCalendar(financialTransaction.getPayrollRun() == null ? financialTransaction.getCreatedDate() : financialTransaction.getPayrollRun().getPaycheckDate()));
        details.setLaw(financialTransaction.getLaw() != null ? financialTransaction.getLaw().getLawAbbrev() : "");
        details.setLawType(financialTransaction.getLaw() != null ? financialTransaction.getLaw().getLawTypeCd() : "");
        if(financialTransaction.getPayrollRun() == null || financialTransaction.getPayrollRun().getPaycheckCollection().size() == 0){
            details.setTxnType("Adjustment");
        } else {
            details.setTxnType("Payroll");
        }

        return details;
    }

    public static SAPPaymentForVerification getPaymentsForVerification(MoneyMovementTransaction pMoneyMovementTransaction){
        SAPPaymentForVerification sapPaymentForVerification = new SAPPaymentForVerification();
        sapPaymentForVerification.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pMoneyMovementTransaction.getMoneyMovementTransactionAmount()));
        sapPaymentForVerification.setDueDate(SAPTranslator.getDateFromSpcfCalendar(pMoneyMovementTransaction.getDueDate()));
        sapPaymentForVerification.setInitiationDate(SAPTranslator.getDateFromSpcfCalendar(pMoneyMovementTransaction.getInitiationDate()));
        sapPaymentForVerification.setPaymentId(pMoneyMovementTransaction.getId().toString());
        sapPaymentForVerification.setPaymentMethod(pMoneyMovementTransaction.getMoneyMovementPaymentMethod().toString());
        sapPaymentForVerification.setPaymentTemplate(getPaymentTemplateFromDomainEntity(pMoneyMovementTransaction.getPaymentTemplate()));
        sapPaymentForVerification.setPeriodBeginDate(SAPTranslator.getDateFromSpcfCalendar(pMoneyMovementTransaction.getPaymentPeriodBegin()));
        sapPaymentForVerification.setPeriodEndDate(SAPTranslator.getDateFromSpcfCalendar(pMoneyMovementTransaction.getPaymentPeriodEnd()));
        sapPaymentForVerification.setTaxpayerAgencyId(StringUtils.defaultIfEmpty(pMoneyMovementTransaction.getAgencyTaxpayerId(), ""));
        sapPaymentForVerification.setTaxPaymentStatus(pMoneyMovementTransaction.getTaxPaymentStatus().toString());
        TransactionState transactionState = TransactionState.findTransactionState(TransactionStateCode.Cancelled);
        DomainEntitySet<FinancialTransaction> financialTransactions = pMoneyMovementTransaction.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType()
                                .in(TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxCredit), TransactionType.findTransactionType(TransactionTypeCode.AgencyDirectCredit))
                                .And(FinancialTransaction.CurrentTransactionState().notEqualTo(transactionState)));
        if(financialTransactions.size() > 0) {
            FinancialTransaction financialTransaction = financialTransactions.get(0);
            if(financialTransaction.getDebitBankAccount() != null) {
                sapPaymentForVerification.setDebitAccountNumber(financialTransaction.getDebitBankAccount().getAccountNumber());
            }
            if(financialTransaction.getCreditBankAccount() != null) {
                sapPaymentForVerification.setCreditAccountRouting(financialTransaction.getCreditBankAccount().getRoutingNumber());
                sapPaymentForVerification.setCreditAccountNumber(financialTransaction.getCreditBankAccount().getAccountNumber());
            }
            sapPaymentForVerification.setSettlementDate(SpcfUtils.convertSpcfCalendarToDate(financialTransaction.getSettlementDate()));
        }

        ArrayList<SAPKeyValuePair> details = new ArrayList<SAPKeyValuePair>();
        if(pMoneyMovementTransaction.getMoneyMovementPaymentMethod().equals(PaymentMethod.EFTPS) || pMoneyMovementTransaction.getMoneyMovementPaymentMethod().equals(PaymentMethod.EFTPSDirectDebit)) {
            EftpsPaymentDetail eftpsPaymentDetail = null;

            // Only include zero payments where the agency requires a zero payment
            if (!pMoneyMovementTransaction.getMoneyMovementTransactionAmount().equals(SpcfMoney.ZERO)) {
                eftpsPaymentDetail = EftpsPaymentDetail.findPaymentDetailByMoneyMovementTransaction(pMoneyMovementTransaction);
            }

            if(eftpsPaymentDetail != null){
                if(eftpsPaymentDetail.getEftTransactionId() != null) {
                    details.add(new SAPKeyValuePair("Eft Transaction Id", eftpsPaymentDetail.getEftTransactionId()));
                }

                if(eftpsPaymentDetail.getAgencyPaymentId() != null) {
                    details.add(new SAPKeyValuePair("Agency Payment Id", eftpsPaymentDetail.getAgencyPaymentId()));
                }

                if(eftpsPaymentDetail.getPaymentDetails() != null) {
                    details.add(new SAPKeyValuePair("Payment details", eftpsPaymentDetail.getPaymentDetails()));
                }

                details.add(new SAPKeyValuePair("Group Id", String.valueOf(eftpsPaymentDetail.getGroupId())));

                if(eftpsPaymentDetail.getReason() != null){
                    details.add(new SAPKeyValuePair("Reason", eftpsPaymentDetail.getReason()));
                }
                if(eftpsPaymentDetail.getRejectCd() != null){
                    details.add(new SAPKeyValuePair("Reject Code", eftpsPaymentDetail.getRejectCd()));
                }
                if(eftpsPaymentDetail.getReturnCd() != null) {
                    details.add(new SAPKeyValuePair("Return Code", eftpsPaymentDetail.getReturnCd().toString()));
                }
                if (eftpsPaymentDetail.getTaxTypeCode() != null) {
                    details.add(new SAPKeyValuePair("Tax Type Code", eftpsPaymentDetail.getTaxTypeCode()));
                }
            }
        } else if(pMoneyMovementTransaction.getMoneyMovementPaymentMethod().equals(PaymentMethod.ACHCredit)) {
            for (EntryDetailRecord entryDetailRecord : pMoneyMovementTransaction.getEntryDetailRecordCollection()
                                                                                .find(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)
                                                                                                       .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)))) {
                details.add(new SAPKeyValuePair("Record Data", entryDetailRecord.getRecordData()));
                details.add(new SAPKeyValuePair("Txp Record Data", entryDetailRecord.getTxpRecordData()));
            }
        } else if(pMoneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.CheckPayment || pMoneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.SuperCheck) {
            if(pMoneyMovementTransaction.getReferenceNumber() != null){
                details.add(new SAPKeyValuePair("Check Number", pMoneyMovementTransaction.getReferenceNumber()));   
            }
        } else if (pMoneyMovementTransaction.getMoneyMovementPaymentMethod().equals(PaymentMethod.EDI)) {
            EdiPaymentDetail ediPaymentDetail;

            ediPaymentDetail = EdiPaymentDetail.findPaymentDetailByMoneyMovementTransaction(pMoneyMovementTransaction);

            if(ediPaymentDetail != null){
                if(ediPaymentDetail.getConfirmationNumber() != null) {
                    details.add(new SAPKeyValuePair("Confirmation Number", ediPaymentDetail.getConfirmationNumber()));
                }

                if(ediPaymentDetail.getFedTaxId() != null) {
                    details.add(new SAPKeyValuePair("Federal Tax Id", ediPaymentDetail.getFedTaxId()));
                }

                if(ediPaymentDetail.getErrorCd() != null) {
                    details.add(new SAPKeyValuePair("Error Code", ediPaymentDetail.getErrorCd()));
                }

                details.add(new SAPKeyValuePair("Group Id", String.valueOf(ediPaymentDetail.getGroupId())));

                if(ediPaymentDetail.getErrorMessage() != null){
                    details.add(new SAPKeyValuePair("Message", ediPaymentDetail.getErrorMessage()));
                }
                if(ediPaymentDetail.getTaxTypeCode() != null){
                    details.add(new SAPKeyValuePair("Tax Code", ediPaymentDetail.getTaxTypeCode()));
                }
            }
        }
        sapPaymentForVerification.setDetails(details);
        return sapPaymentForVerification;
    }

    public static SAPPaymentMethod getCompanyPaymentTemplatePaymentMethodFromDomainEntity(CompanyPaymentTemplatePaymentMethod pCompanyPaymentTemplatePaymentMethod, MoneyMovementTransaction pPayment) {
        SAPPaymentMethod sapPaymentMethod = new SAPPaymentMethod();
        sapPaymentMethod.setPaymentMethodName(pCompanyPaymentTemplatePaymentMethod.getPaymentMethod().toString());
        sapPaymentMethod.setIsAgentEnabled(pCompanyPaymentTemplatePaymentMethod.getAgentEnabled());
        sapPaymentMethod.setIsEnabled(pCompanyPaymentTemplatePaymentMethod.getEnabled());
        sapPaymentMethod.setChangedBy(SAPTranslator.getUserNameFromUserID(pCompanyPaymentTemplatePaymentMethod.getModifierId()));
        sapPaymentMethod.setModifiedDate(SpcfUtils.convertSpcfCalendarToDate(pCompanyPaymentTemplatePaymentMethod.getModifiedDate()));
        Set<String> requirementsSet = new HashSet<String>();
        Set<String> additionalRequirementSet = new HashSet<String>();
        for (PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : pCompanyPaymentTemplatePaymentMethod.getCompanyAgencyPaymentTemplate().getPaymentTemplate().getPaymentTemplatePaymentMethods()) {
            if (paymentTemplatePaymentMethod.getPaymentMethod().equals(pCompanyPaymentTemplatePaymentMethod.getPaymentMethod())) {
                sapPaymentMethod.setPaymentMethodOrder(paymentTemplatePaymentMethod.getPaymentMethodOrder());
                for (PaymentMethodRequirement paymentMethodRequirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
                    if (paymentMethodRequirement instanceof ManualRequirement) {
                        sapPaymentMethod.setHasManualRequirement(true);
                    }
                    if (!paymentMethodRequirement.isRequirementMet(pCompanyPaymentTemplatePaymentMethod)) {
                        /*  This paymentMethod has a requirement that's not been met    */
                        requirementsSet.add(paymentMethodRequirement.getRequirementString(pCompanyPaymentTemplatePaymentMethod));
                    }
                    if (paymentMethodRequirement instanceof PaymentRequirement) {
                        if (pPayment == null) {
                            additionalRequirementSet.add(paymentMethodRequirement.getRequirementString(pCompanyPaymentTemplatePaymentMethod));
                        } else {
                            if (!((PaymentRequirement) paymentMethodRequirement).isRequirementMet(pCompanyPaymentTemplatePaymentMethod, pPayment)) {
                                requirementsSet.add(paymentMethodRequirement.getRequirementString(pCompanyPaymentTemplatePaymentMethod));
                                sapPaymentMethod.setIsEnabled(false);
                            }
                        }

                    }
                }
            }
        }
        for (String s : requirementsSet) {
            sapPaymentMethod.getRequirements().add(s);
        }
        for (String str : additionalRequirementSet) {
            sapPaymentMethod.getAdditionalRequirements().add(str);
        }

        return sapPaymentMethod;
    }

    public static SAPCompanyAgencyPaymentTemplateAgencyId getAdditionalAgencyIdFromDomainEntity(CompanyPaymentTemplateAgencyId pCompanyPaymentTemplateAgencyId) {
        SAPCompanyAgencyPaymentTemplateAgencyId sapCompanyAgencyPaymentTemplateAgencyId = new SAPCompanyAgencyPaymentTemplateAgencyId();
        sapCompanyAgencyPaymentTemplateAgencyId.setId(StringUtils.defaultString(pCompanyPaymentTemplateAgencyId.getAgencyTaxpayerId()));
        sapCompanyAgencyPaymentTemplateAgencyId.setName(pCompanyPaymentTemplateAgencyId.getName());
        sapCompanyAgencyPaymentTemplateAgencyId.setModifiedDate(SpcfUtils.convertSpcfCalendarToDate(pCompanyPaymentTemplateAgencyId.getModifiedDate()));       //todo refactor
        sapCompanyAgencyPaymentTemplateAgencyId.setModifiedBy(SAPTranslator.getUserNameFromUserID(pCompanyPaymentTemplateAgencyId.getModifierId()));
        return sapCompanyAgencyPaymentTemplateAgencyId;
    }

    public static SAPCompanyAgencyPaymentTemplateAgencyId getMissingAdditionalAgencyId(String pAdditionalAgencyId) {
        SAPCompanyAgencyPaymentTemplateAgencyId sapCompanyAgencyPaymentTemplateAgencyId = new SAPCompanyAgencyPaymentTemplateAgencyId();
        sapCompanyAgencyPaymentTemplateAgencyId.setId(null);
        sapCompanyAgencyPaymentTemplateAgencyId.setName(pAdditionalAgencyId);
        return sapCompanyAgencyPaymentTemplateAgencyId;
    }

    public static SAPCompanyAgencyPaymentTemplateAgencyId getMainAgencyIdFromDomainEntity(CompanyAgencyPaymentTemplate pCompanyAgencyPaymentTemplate) {
        SAPCompanyAgencyPaymentTemplateAgencyId sapCompanyAgencyPaymentTemplateAgencyId = new SAPCompanyAgencyPaymentTemplateAgencyId();
        sapCompanyAgencyPaymentTemplateAgencyId.setId(StringUtils.defaultString(pCompanyAgencyPaymentTemplate.getAgencyTaxpayerId()));
        sapCompanyAgencyPaymentTemplateAgencyId.setName(null);
        return sapCompanyAgencyPaymentTemplateAgencyId;
    }

    public static SAPCompanyFilingAmountHistory getCompanyFilingAmountHistory(CompanyFilingAmount pCompanyFilingAmount, CompanyFilingAmount pPreviousQtrCompanyFilingAmount) {
        SAPCompanyFilingAmountHistory sapCompanyFilingAmount = new SAPCompanyFilingAmountHistory();
        sapCompanyFilingAmount.setName(pCompanyFilingAmount.getName());
        sapCompanyFilingAmount.setEffectiveQuarter(TaxTranslator.getSAPQuarter(pCompanyFilingAmount.getEffectiveDate()));
        sapCompanyFilingAmount.setInvalidDate(SAPTranslator.getDateFromSpcfCalendar(pCompanyFilingAmount.getInvalidDate()));
        sapCompanyFilingAmount.setIsRate(pCompanyFilingAmount.getAdditionalFilingAmount().getRate());
        sapCompanyFilingAmount.setModifiedBy(SAPTranslator.getUserNameFromUserID(pCompanyFilingAmount.getModifierId()));
        sapCompanyFilingAmount.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(pCompanyFilingAmount.getModifiedDate()));
        if (Double.isNaN(pCompanyFilingAmount.getAmount())) {
            sapCompanyFilingAmount.setValue("Missing");
        } else {
            sapCompanyFilingAmount.setValue(new DecimalFormat("########.#########").format(pCompanyFilingAmount.getAmount()));
        }
        if(pPreviousQtrCompanyFilingAmount == null || Double.isNaN(pPreviousQtrCompanyFilingAmount.getAmount())) {
            sapCompanyFilingAmount.setPreviousQuarterValue("Missing");
        } else {
            sapCompanyFilingAmount.setPreviousQuarterValue(new DecimalFormat("########.#########").format(pPreviousQtrCompanyFilingAmount.getAmount()));
        }
        return sapCompanyFilingAmount;
    }

    public static SAPCompanyFilingAmount getCompanyFilingAmount(CompanyFilingAmount pCompanyFilingAmount) {
        SAPCompanyFilingAmount sapCompanyFilingAmount = new SAPCompanyFilingAmount();
        sapCompanyFilingAmount.setHasCurrentValue(true);
        sapCompanyFilingAmount.setName(pCompanyFilingAmount.getName());
        sapCompanyFilingAmount.setIsRate(pCompanyFilingAmount.getAdditionalFilingAmount().getRate());
        if (Double.isNaN(pCompanyFilingAmount.getAmount())) {
            sapCompanyFilingAmount.setValue("");
        } else {
            sapCompanyFilingAmount.setValue(new DecimalFormat("########.#########").format(pCompanyFilingAmount.getAmount() * (pCompanyFilingAmount.getAdditionalFilingAmount().getRate() ? 100 : 1)));
        }
        return sapCompanyFilingAmount;
    }

    public static SAPCompanyFilingAmount getCompanyFilingAmount(AdditionalFilingAmount pAdditionalFilingAmount) {
        SAPCompanyFilingAmount sapCompanyFilingAmount = new SAPCompanyFilingAmount();
        sapCompanyFilingAmount.setHasCurrentValue(false);
        sapCompanyFilingAmount.setName(pAdditionalFilingAmount.getName());
        sapCompanyFilingAmount.setIsRate(pAdditionalFilingAmount.getRate());
        sapCompanyFilingAmount.setValue("");
        return sapCompanyFilingAmount;
    }

    public static SAPDataSyncDetailEmployee getSapDataSyncDetailEmployee(Employee employee) {
        SAPDataSyncDetailEmployee detailEmployee = new SAPDataSyncDetailEmployee();

        detailEmployee.setEmployeeId(employee.getSourceEmployeeId());
        detailEmployee.setEmployeeName(employee.getFullName());
        detailEmployee.setDetailId(employee.getId().toString());
        detailEmployee.setToken((int)employee.getQbdtEmployeeInfo().getToken());
        detailEmployee.setClassString(employee.getQbdtEmployeeInfo().getTrackingClass());
        detailEmployee.setIsDeleted(employee.getQbdtEmployeeInfo().getIsDeleted());

        return detailEmployee;
    }

    public static SAPDataSyncDetailPaycheck getSapDataSyncDetailPaycheck(Paycheck paycheck) {
        SAPDataSyncDetailPaycheck detailPaycheck = new SAPDataSyncDetailPaycheck();

        detailPaycheck.setDetailId(paycheck.getId().toString());
        detailPaycheck.setToken((int)paycheck.getQbdtPaycheckInfo().getToken());

        detailPaycheck.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(paycheck.getNetAmount()));
        detailPaycheck.setCheckDate(SAPTranslator.getDateFromSpcfCalendar(paycheck.getPayrollRun().getPaycheckDate()));
        detailPaycheck.setCheckNumber(paycheck.getQbdtPaycheckInfo().getCheckNumber());
        detailPaycheck.setEmployeeId(paycheck.getSourceEmployee().getSourceEmployeeId());
        detailPaycheck.setEmployeeName(paycheck.getSourceEmployee().getFullName());
        detailPaycheck.setPaycheckId(Integer.parseInt(paycheck.getSourcePaycheckId()));
        if(paycheck.getIsYTDAdjustment()) {
            detailPaycheck.setPaycheckType("YTD");
        } else {
            detailPaycheck.setPaycheckType("Paycheck");
        }
        detailPaycheck.setMemo(paycheck.getQbdtPaycheckInfo().getMemo());

        return detailPaycheck;
    }

    public static SAPDataSyncDetailPayrollTransaction getSapDataSyncDetailPayrollTransaction(PriorPaymentSubmission pps) {
        SAPDataSyncDetailPayrollTransaction detailTxn = new SAPDataSyncDetailPayrollTransaction();

        /*
        Prior payment submissions are the object we are trying to represent here.
        However, the data that we need is stored all over the place.

        A prior payment has 0 or more QbdtTransactionInfos.  If it has more than 1, the only thing different will be the MMT.
        Logically, a PriorPaymentSubmission has 0 to many MMTs.

        A PriorPaymentSubmission has 0 or 1 QbdtPayrollTransaction (it says 1 in the model, but the model is a lie).
        The MMTs represent real prior payments/refunds to real agencies that we really care about; the QbdtPayrollTransaction represents the entirety of all
        prior payments/refunds, both fake and real, but only exists if there are any fake prior payments/refunds.

        The amount of a submission is the QbdtPayrollTransaction Amount if not null or else the sum of the MMTs.

        A prior payment submission logically has 1 QbdtTransactionInfo.  This is either one of the many on PriorPaymentSubmission or it is the one on QbdtPayrollTransaction.
        All the data will be the same between all of these instances.

        Prior payment submissions can be either Prior Payments or Refunds.
        Payments: All MMTs have method of HPDE; QbdtPayrollTransaction is PriorPayment.
        Refunds: All MMTs have method of HPDERefund; QbdtPayrollTransaction is Refund .
        */

        //So first I'm going to convert the actual model to a logical one.
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = new DomainEntitySet<MoneyMovementTransaction>();
        QbdtTransactionInfo info;
        if (pps.getQbdtTransactionInfoCollection().isNotEmpty()) {
            info = pps.getQbdtTransactionInfoCollection().getFirst();
        } else {
            info = pps.getQbdtPayrollTransaction().getQbdtTransactionInfo();
        }

        for (QbdtTransactionInfo qbdtTransactionInfo : pps.getQbdtTransactionInfoCollection()) {
            MoneyMovementTransaction mmt = qbdtTransactionInfo.getMoneyMovementTransaction();
            if (mmt != null) {
                moneyMovementTransactions.add(mmt);
            }
        }



        SpcfDecimal amountTotal = SpcfMoney.ZERO;
        boolean isRefund = false;

        if (pps.getQbdtPayrollTransaction() != null) {
            amountTotal = amountTotal.add(pps.getQbdtPayrollTransaction().getAmount());
            if (pps.getQbdtPayrollTransaction().getTransactionType() == QbdtPayrollTransactionType.Refund) {
                isRefund = true;
            }
            detailTxn.setTransactionDate(SAPTranslator.getDateFromSpcfCalendar(pps.getQbdtPayrollTransaction().getTransactionDate()));
        } else {
            for (MoneyMovementTransaction mmt : moneyMovementTransactions) {
                amountTotal = amountTotal.add(mmt.getMoneyMovementTransactionAmount());
                if (mmt.getMoneyMovementPaymentMethod() == PaymentMethod.HPDERefund) {
                    isRefund = true;
                }
            }
            SpcfCalendar transactionDate;
            if (pps.getQbdtTransactionInfoCollection().isNotEmpty()) {
                transactionDate = pps.getQbdtTransactionInfoCollection().getFirst().getMoneyMovementTransaction().getInitiationDate();
            } else {
                transactionDate = pps.getCreatedDate();
            }
            detailTxn.setTransactionDate(SAPTranslator.getDateFromSpcfCalendar(transactionDate));
        }

        //now translate the values

        detailTxn.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(new SpcfMoney(amountTotal)));
        detailTxn.setPayrollTransactionType(isRefund ? "Refund" : "Prior Payment");

        detailTxn.setDetailId(pps.getId().toString());
        detailTxn.setPayrollTransactionId(Integer.parseInt(pps.getSourceId()));

        detailTxn.setToken((int)info.getToken());
        detailTxn.setMemo(info.getMemo());
        detailTxn.setIsDeleted(info.getIsDeleted());

        return detailTxn;
    }

    public static SAPDataSyncDetailPayrollTransaction getSapDataSyncDetailPayrollTransaction(CompanyAdjustmentSubmission cas) {
        SAPDataSyncDetailPayrollTransaction detailTxn = new SAPDataSyncDetailPayrollTransaction();

        detailTxn.setPayrollTransactionType("Liability Adjustment");
        detailTxn.setDetailId(cas.getId().toString());

        detailTxn.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(cas.getAmount()));
        detailTxn.setPayrollTransactionId(Integer.parseInt(cas.getSourceId()));

        detailTxn.setToken((int) cas.getQbdtTransactionInfo().getToken());
        detailTxn.setMemo(cas.getQbdtTransactionInfo().getMemo());
        detailTxn.setIsDeleted(cas.getQbdtTransactionInfo().getIsDeleted());
        if(cas.getQbdtPayrollTransaction() != null){
            detailTxn.setTransactionDate(SAPTranslator.getDateFromSpcfCalendar(cas.getQbdtPayrollTransaction().getTransactionDate()));
        } else {
            detailTxn.setTransactionDate(SAPTranslator.getDateFromSpcfCalendar(cas.getSubmissionDate()));
        }

        return detailTxn;
    }

    public static SAPDataSyncDetailPayrollTransaction getSapDataSyncDetailPayrollTransaction(LiabilityCheck lc) {
        SAPDataSyncDetailPayrollTransaction detailTxn = new SAPDataSyncDetailPayrollTransaction();

        detailTxn.setPayrollTransactionType("Liability Check");
        detailTxn.setDetailId(lc.getId().toString());

        detailTxn.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(lc.getAmount()));
        detailTxn.setPayrollTransactionId(Integer.parseInt(lc.getSourceId()));

        detailTxn.setToken((int)lc.getQbdtTransactionInfo().getToken());
        detailTxn.setMemo(lc.getQbdtTransactionInfo().getMemo());
        detailTxn.setIsDeleted(lc.getQbdtTransactionInfo().getIsDeleted());
        detailTxn.setTransactionDate(SAPTranslator.getDateFromSpcfCalendar(lc.getTransactionDate()));

        return detailTxn;
    }


    public static SAPDataSyncDetailPayrollTransaction getSapDataSyncDetailPayrollTransaction(QbdtPayrollTransaction pt) {
        SAPDataSyncDetailPayrollTransaction detailTxn = new SAPDataSyncDetailPayrollTransaction();

        detailTxn.setPayrollTransactionType(humanize(pt.getTransactionType()));
        detailTxn.setDetailId(pt.getId().toString());

        detailTxn.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pt.getAmount()));
        detailTxn.setPayrollTransactionId(Integer.parseInt(pt.getSourceId()));

        detailTxn.setToken((int)pt.getQbdtTransactionInfo().getToken());
        detailTxn.setMemo(pt.getQbdtTransactionInfo().getMemo());
        detailTxn.setIsDeleted(pt.getQbdtTransactionInfo().getIsDeleted());
        detailTxn.setTransactionDate(SAPTranslator.getDateFromSpcfCalendar(pt.getTransactionDate()));

        if (pt.getEmployee() != null) {
            detailTxn.setEmployeeId(pt.getEmployee().getSourceEmployeeId());
            detailTxn.setEmployeeName(pt.getEmployee().getFullName());
        }

        detailTxn.setIsQBOnly(true);

        return detailTxn;
    }

    private static String humanize(QbdtPayrollTransactionType payrollTransactionType) {
        switch (payrollTransactionType) {
            case FundsTransfer:
                return "Funds Transfer";
            case DDReturn:
                return "DD Return";
            case LiabilityCheck:
                return "Liability Check";
            default:
                return null;
        }
    }


    public static SAPDataSyncDetailPayrollItem getSapDataSyncDetailPayrollItem(QbdtPayrollItemInfo info) {
        SAPDataSyncDetailPayrollItem detailPayrollItem = new SAPDataSyncDetailPayrollItem();
        detailPayrollItem.setEE(info.getIsEmployeePaid());
        detailPayrollItem.setDetailId(info.getId().toString());
        detailPayrollItem.setToken((int)info.getToken());
        detailPayrollItem.setIsDeleted(info.getIsDeleted());

        if (info.getCompanyLaw() != null) {
            CompanyLaw companyLaw = info.getCompanyLaw();
            detailPayrollItem.setInactive(companyLaw.getStatus() == PayrollItemStatus.Inactive);
            detailPayrollItem.setPayrollItemId(Integer.parseInt(companyLaw.getSourceId()));
            detailPayrollItem.setPayrollItemName(companyLaw.getSourceDescription());
            detailPayrollItem.setPayrollItemType("Tax");
        } else if (info.getCompanyPayrollItem() != null) {
            CompanyPayrollItem companyPayrollItem = info.getCompanyPayrollItem();
            detailPayrollItem.setInactive(companyPayrollItem.getStatus() == PayrollItemStatus.Inactive);
            detailPayrollItem.setPayrollItemId(Integer.parseInt(companyPayrollItem.getSourcePayrollItemId()));
            detailPayrollItem.setPayrollItemName(companyPayrollItem.getSourceDescription());
            detailPayrollItem.setPayrollItemType(companyPayrollItem.getPayrollItem().getPayrollItemType().toString());
        }

        return detailPayrollItem;
    }

    public static SAPTransaction getSAPTransaction(FinancialTransaction pFinancialTransaction) {
        SAPTransaction sapTransaction = new SAPTransaction();
        sapTransaction.setAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(pFinancialTransaction.getFinancialTransactionAmount()));
        sapTransaction.setSettlementDate(SAPTranslator.getDateFromSpcfCalendar(pFinancialTransaction.getSettlementDate()));
        sapTransaction.setTransactionId(pFinancialTransaction.getId().toString());
        sapTransaction.setStatus(pFinancialTransaction.getCurrentTransactionState().getName());
        sapTransaction.setCreatedBy(SAPTranslator.getUserNameFromUserID(pFinancialTransaction.getCreatorId()));
        sapTransaction.setTransactionType(pFinancialTransaction.getTransactionType().getTransactionTypeCd().toString());
        sapTransaction.setSettlementType(pFinancialTransaction.getSettlementTypeCd().toString());
        sapTransaction.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(pFinancialTransaction.getCreatedDate()));
        sapTransaction.setActionCollection(PayrollRunTranslator.getSAPActionEventsFromDomainEntities(pFinancialTransaction.getActionCollection()));
        String transactionReturns = "";
        if(pFinancialTransaction.getMoneyMovementTransaction() != null){
            DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(pFinancialTransaction);
            for(TransactionReturn txnReturn : returnList){
                transactionReturns += txnReturn.getBankReturnCd() + "\n";
            }
        }
        sapTransaction.setReturnCd(transactionReturns);

        return sapTransaction;
    }

    public static SAPRAFEnrollmentDetail getSAPRAFEnrollmentDetailFromDomainEntity(RAFEnrollment enrollment, RAFEnrollmentDetail rafEnrollmentDetail, boolean companyHasPayrolls) {
        SAPRAFEnrollmentDetail saprafEnrollmentDetail = new SAPRAFEnrollmentDetail();
        if (rafEnrollmentDetail != null) {
            saprafEnrollmentDetail.setCompanyName(rafEnrollmentDetail.getLegalName());
            saprafEnrollmentDetail.setEin(rafEnrollmentDetail.getFedTaxid());
            if (rafEnrollmentDetail.getCreatedDate() != null) {
                saprafEnrollmentDetail.setCreationDate(SAPTranslator.getDateFromSpcfCalendar(rafEnrollmentDetail.getCreatedDate()));
            }
            if (rafEnrollmentDetail.getModifiedDate() != null) {
                saprafEnrollmentDetail.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(rafEnrollmentDetail.getModifiedDate()));
            }
        } else {
            saprafEnrollmentDetail.setEin(enrollment.getCompanyAgency().getCompany().getFedTaxId());
            if (enrollment.getCreatedDate() != null) {
                saprafEnrollmentDetail.setCreationDate(SAPTranslator.getDateFromSpcfCalendar(enrollment.getCreatedDate()));
            }
            if (enrollment.getModifiedDate() != null) {
                saprafEnrollmentDetail.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(enrollment.getModifiedDate()));
            }
            saprafEnrollmentDetail.setCompanyName(enrollment.getCompanyAgency().getCompany().getLegalName());
        }
        saprafEnrollmentDetail.setCompanyKey(new SAPCompanyKey(enrollment.getCompanyAgency().getCompany()));
        saprafEnrollmentDetail.setRejectionReason(enrollment.getStatusReason());
        saprafEnrollmentDetail.setEnrollmentId(enrollment.getId().toString());
        saprafEnrollmentDetail.setCompanyHasPayrolls(companyHasPayrolls);
        return saprafEnrollmentDetail;
    }

    public static SAPACHEnrollmentDetail getSAPACHEnrollmentDetailFromDomainEntity(ACHEnrollment enrollment, ACHEnrollmentDetail achEnrollmentDetail) {
        SAPACHEnrollmentDetail sapAchEnrollmentDetail = new SAPACHEnrollmentDetail();
        if (achEnrollmentDetail != null) {
            sapAchEnrollmentDetail.setCompanyName(achEnrollmentDetail.getLegalName());
            sapAchEnrollmentDetail.setEin(achEnrollmentDetail.getFEIN());
            if (achEnrollmentDetail.getCreatedDate() != null) {
                sapAchEnrollmentDetail.setCreationDate(SAPTranslator.getDateFromSpcfCalendar(achEnrollmentDetail.getCreatedDate()));
            }
            if (achEnrollmentDetail.getModifiedDate() != null) {
                sapAchEnrollmentDetail.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(achEnrollmentDetail.getModifiedDate()));
            }
        } else {
            sapAchEnrollmentDetail.setEin(enrollment.getCompanyAgency().getCompany().getFedTaxId());
            if (enrollment.getCreatedDate() != null) {
                sapAchEnrollmentDetail.setCreationDate(SAPTranslator.getDateFromSpcfCalendar(enrollment.getCreatedDate()));
            }
            if (enrollment.getModifiedDate() != null) {
                sapAchEnrollmentDetail.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(enrollment.getModifiedDate()));
            }
            sapAchEnrollmentDetail.setCompanyName(enrollment.getCompanyAgency().getCompany().getLegalName());
        }
        sapAchEnrollmentDetail.setCompanyKey(new SAPCompanyKey(enrollment.getCompanyAgency().getCompany()));
        sapAchEnrollmentDetail.setRejectionReason(enrollment.getStatusReason());
        sapAchEnrollmentDetail.setEnrollmentId(enrollment.getId().toString());
        sapAchEnrollmentDetail.setAid(enrollment.getCompanyAgency().getCompanyAgencyPaymentTemplateCollection().getFirst().getAgencyTaxpayerId());
        if(enrollment.getEffectiveDate() != null) {
            sapAchEnrollmentDetail.setEffectiveDate(SAPTranslator.getDateFromSpcfCalendar(enrollment.getEffectiveDate()));
        }
        return sapAchEnrollmentDetail;
    }

    public static SAPEnrollmentFile getSAPEnrollmentFile(SpcfCalendar createdDate, SpcfUniqueId fileId, String type) {
        SAPEnrollmentFile SAPEnrollmentFile = new SAPEnrollmentFile();
        SAPEnrollmentFile.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(createdDate));
        SAPEnrollmentFile.setFileId(fileId.toString());
        SAPEnrollmentFile.setType(type);
        return SAPEnrollmentFile;
    }

    public static SAPQuarter getSAPQuarter(SpcfCalendar calendar){
        return new SAPQuarter(TaxPeriod.getYearNumber(calendar),
                              TaxPeriod.getQuarterNumber(calendar));
    }

    public static SAPLawRate getSAPLawRate(Law law, CompanyLawRate pCompanyLawRate) {
        SAPLawRate sapLawRate = new SAPLawRate();
        sapLawRate.setLaw(getLawItemsFromDomainEntity(law));
        if (pCompanyLawRate != null) {
            sapLawRate.setHasCurrentRate(true);
            sapLawRate.setCurrentPercentage(pCompanyLawRate.getRate() * 100);
        } else {
            sapLawRate.setHasCurrentRate(false);
        }


        LawRateRange lawRateRange = law.getLawRateRange();
        DecimalFormat decimalFormat = new DecimalFormat("#.#####");

        if (lawRateRange == null) {
            sapLawRate.setMinPercentage(-1);
            sapLawRate.setMaxPercentage(-1);
            sapLawRate.setMaxPrecision(-1);
        } else {
            sapLawRate.setMaxPrecision(lawRateRange.getPrecision());
            if(lawRateRange.getMaxRate()==null && lawRateRange.getMinRate()==null)
            {
                DomainEntitySet<LawRateValue> lawRateValues=law.getLawRateValues();
                if(lawRateValues==null || lawRateValues.size()==0)
                {
                    sapLawRate.setMinPercentage(-1);
                    sapLawRate.setMaxPercentage(-1);
                }
                else
                {
                    sapLawRate.setMinPercentage(Double.valueOf(decimalFormat.format(SAPTranslator.getDoubleFromSpcfMoneyNullZero(lawRateValues.get(0).getRate()) * 100)));
                    if(lawRateValues.size()==2) {
                        sapLawRate.setMaxPercentage(Double.valueOf(decimalFormat.format(SAPTranslator.getDoubleFromSpcfMoneyNullZero(lawRateValues.get(1).getRate()) * 100)));
                    }
                    else {
                        sapLawRate.setMaxPercentage(-1);
                    }
                    sapLawRate.setHasValuesInsteadOfRanges(true);
                }
            }
            else {
                sapLawRate.setMinPercentage(Double.valueOf(decimalFormat.format(SAPTranslator.getDoubleFromSpcfMoneyNullZero(lawRateRange.getMinRate()) * 100)));
                sapLawRate.setMaxPercentage(Double.valueOf(decimalFormat.format(SAPTranslator.getDoubleFromSpcfMoneyNullZero(lawRateRange.getMaxRate()) * 100)));
            }
        }

        return sapLawRate;
    }

    public static SAPQuarterRate getSAPQuarterRate(CompanyLawRate pCompanyLawRate) {
        SAPQuarterRate sapQuarterRate = new SAPQuarterRate();
        sapQuarterRate.setQuarter(getSAPQuarter(pCompanyLawRate.getEffectiveDate()));
        sapQuarterRate.setCurrentPercentage(pCompanyLawRate.getRate() * 100);
        return sapQuarterRate;
    }

    public static SAPFilerType get941FilerTypeFromFormTemplate(CompanyAgencyFormTemplate pCompanyAgencyFormTemplate){
        SAPFilerType sapFilerType = new SAPFilerType();
        if (pCompanyAgencyFormTemplate != null) {
            if (pCompanyAgencyFormTemplate.getFormTemplate().getFormTemplateCd().equals(FormTemplate.IRS_944)) {
                sapFilerType.setFilerType("944");
            } else if (pCompanyAgencyFormTemplate.getFormTemplate().getFormTemplateCd().equals(FormTemplate.IRS_941)) {
                sapFilerType.setFilerType("941");
            }
            sapFilerType.setEffectiveQuarter(getSAPQuarter(pCompanyAgencyFormTemplate.getEffectiveDate()));
        }
        return sapFilerType;
    }

    public static FormTemplateDTO get941FormTemplateDTOFromSAPFilerType(SAPFilerType pFilerType) {
        FormTemplateDTO dto = new FormTemplateDTO();
        dto.setEffectiveDate(pFilerType.getEffectiveQuarter().getFirstDayOfQuarter());
        dto.setFilerType(pFilerType.getFilerType().equals("944") ? FormTemplate.IRS_944 : FormTemplate.IRS_941);
        return dto;
    }

}
