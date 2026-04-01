package com.intuit.sbd.payroll.psp.batchjobs.statereports.states;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class KY_WH extends StateReportBase{

    private static String NEW_LINE = "\n";
    private static String TRANSMITTER_ID = "01008";
    private static String ACK_ADDRESS = "tax_eservice@intuit.com";
    private static String STATE_SCHEMA_VERSION = "KYWithholding2017v0.3";
    private static String SOFTWARE_ID = "QBDESKKY21";
    private static String FORM_KY = "KYWHK1v1";
    private static String SIGNATURE = "0000000000";
    private static String RETURN_SIGNER_NAME = "Jason Shipp";
    private static String RETURN_SIGNER_FIRST_NAME = "Jason";
    private static String RETURN_SIGNER_LAST_NAME = "Shipp";
    private static String RETURN_SIGNER_PHONE = "8889277478";
    private static String RETURN_SIGNER_EMAIL = "tax_eservice@intuit.com";
    private static String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static String YYYYMMDD = "yyyy-MM-dd";

    private static int BINARY_ATTACHEMNT_COUNT = 0;
    private static int MAX_COMPNAY_COUNT = 1000;

    private int companyCount = 0;
    private int submissionSeq = 0;
    private String kyProcessType = "";

    public KY_WH(){
        reportNamesList = new String[]{"KY-K1-PAYMENT"};
        companyCount = 0;
    }

    @Override
    public boolean isScheduled(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY) &&
                !paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.TWICEMONTHLY)) {
            return false;
        }
        int mon = passedInDate.getMonth();

        if(paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)){
            if(mon > 1)
                return checkDay(paymentTemplateFrequency, 15, 3, false);
            else if(mon == 1)
                return checkDay(paymentTemplateFrequency, 31, 3, false);
        }else if(paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.TWICEMONTHLY)){
            if(mon > 1) {
                SpcfCalendar endDate = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency)[1];
                if(endDate.getDay() > 15 )
                    return checkDay(paymentTemplateFrequency, 10, 3, false);
                else
                    return checkDay(paymentTemplateFrequency, 25, 3, false);
            }else if(mon == 1)
                return checkDay(paymentTemplateFrequency, 31, 3, false);
        }
        return false;
    }

    @Override
    public void process(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {

        submissionSeq = SystemParameter.findIntValue(SystemParameter.Code.KY_SUBMISSION_SEQ, 999950);
        kyProcessType = SystemParameter.findStringValue(SystemParameter.Code.KY_PROCESS_TYPE, "P");

        logger.info("Systemparameter Value for  KY_SUBMISSION_SEQ = "
                        +submissionSeq+" KY_PROCESS_TYPE = "+kyProcessType);

        SpcfCalendar[] dates = null;

        if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY) &&
                !paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.TWICEMONTHLY)) {
            // ATF creates quarterly reports.  Only do monthly reports.
            logger.info("Skipping processing of " + paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev()
                    + " for frequency " + paymentTemplateFrequency.getPaymentFrequencyId().toString());
            return;
        }
        logger.info("Processing starts for " + paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev()
                + " for frequency " + paymentTemplateFrequency.getPaymentFrequencyId().toString());

        dates = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency);

        SpcfCalendar startDate = dates[0];
        SpcfCalendar endDate = dates[1];

        ArrayList<PaymentTemplateFrequency> frequencyList = new ArrayList<PaymentTemplateFrequency>();
        frequencyList.add(paymentTemplateFrequency);

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions =
                getMoneyMovementTransactions(paymentTemplateFrequency, startDate, endDate);

        logger.info("Running report " + paymentTemplateFrequency.getPaymentFrequencyId().toString() + " for class " +
                getClass().getSimpleName() + " for start " + startDate.format("yyyy/MM/dd") + " and end " + endDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " MMTs");

        StringBuilder body = new StringBuilder();
        int totalProcessed = createReports(companyToMoneyMovementTransactions, startDate, endDate,body,passedInDate);

        if(totalProcessed == -9999)
            return;

        companyCount = totalProcessed;
        StringBuilder builder = new StringBuilder();
        createHeader(builder,passedInDate);
        builder.append(body);
        builder.append("</Transmission>");

        updateSystemParameter(SystemParameter.Code.KY_SUBMISSION_SEQ,submissionSeq);

        PaymentTemplateFrequency[] frequenciesArray = frequencyList.toArray(new PaymentTemplateFrequency[frequencyList.size()]);
        saveStateCoupon(builder, startDate, endDate, StateReportType.Recon, frequenciesArray);
        BatchUtils.createStateReportEmail(builder,".xml", totalProcessed, startDate, endDate, StateReportType.Recon, frequenciesArray);

    }

    private void createHeader(StringBuilder builder,SpcfCalendar passedInDate){
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        builder.append(NEW_LINE);
        builder.append("<Transmission xmlns=\"http://www.irs.gov/efile\">");
        builder.append(NEW_LINE);
        createTransmissionHeader(builder,passedInDate);
    }
    private void createTransmissionHeader(StringBuilder builder, SpcfCalendar passedInDate){
        builder.append("<TransmissionHeader recordCount=\""+companyCount+"\">");
        builder.append(NEW_LINE);
        builder.append("<Jurisdiction>KY</Jurisdiction>");
        builder.append(NEW_LINE);
        builder.append("<TransmissionId>"+getSubmissionId(passedInDate)+"</TransmissionId>");
        builder.append(NEW_LINE);
        builder.append("<Timestamp>"+passedInDate.format(TIMESTAMP_FORMAT)+"</Timestamp>");
        builder.append(NEW_LINE);
        builder.append("<Transmitter>");
        builder.append(NEW_LINE);
        builder.append("<ETIN>"+TRANSMITTER_ID+"</ETIN>");
        builder.append(NEW_LINE);
        builder.append("</Transmitter>");
        builder.append(NEW_LINE);
        builder.append("<ProcessType>"+kyProcessType+"</ProcessType>");
        builder.append(NEW_LINE);
        builder.append("<ReportingAgentSignature>PayrollOps</ReportingAgentSignature>");
        builder.append(NEW_LINE);
        builder.append("<AckAddress>"+ACK_ADDRESS+"</AckAddress>");
        builder.append(NEW_LINE);
        builder.append("</TransmissionHeader>");
        builder.append(NEW_LINE);
    }

    private int createReports (HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions, SpcfCalendar startDate,
                               SpcfCalendar endDate, StringBuilder builder,SpcfCalendar passedInDate){
        int totalProcessed = 0;

        DomainEntitySet<Law> laws = Law.findWithholdingLawForTemplate(reportNamesList[0]);
        if (laws.size() != 1) {
            logger.fatal("Could not find law for KY Withholding.  Aborting!");
            return -9999;
        }
        Law law = laws.get(0);

        for (Company company : companyToMoneyMovementTransactions.keySet()) {
            try {
                pspRequestContextManager.setRequestContextCompany(company);
                // Need to handle scenario if company count get increased by 1000
                if (totalProcessed >= MAX_COMPNAY_COUNT) {
                    logger.fatal("It reached maximum count need to resolve for KY_WH!");
                    break;
                }

                ArrayList<MoneyMovementTransaction> mmts = companyToMoneyMovementTransactions.get(company).getValueItem();
                MoneyMovementTransaction mmt = mmts.get(0);


                String stateEIN = prepareStateAgencyId(mmt, 6);
                if (stateEIN == null) {
                    logger.error("Company " + company.getSourceCompanyId() + " state tax id too long.  The state tax id is \"" +
                            mmt.getAgencyTaxpayerId() + "\".  Skipping output");
                    continue;
                }


                SpcfMoney totalPayments = getTotalPayments(mmts);
                SpcfMoney totalLiabilities = getTotalLiabilities(mmts);

                if (totalPayments.isLessThan(SpcfMoney.ZERO) || totalLiabilities.isLessThan(SpcfMoney.ZERO)) {
                    logger.error("Company " + company.getSourceCompanyId() + " has a total payment or total liability that is negative, skipping.  Total Payments:" +
                            totalPayments.toString() + " Total Liabilities:" + totalLiabilities.toString());
                    continue;
                }

                DomainEntitySet<CompanyDailyLiability> companyDailyLiabilities = CompanyDailyLiability.findCompanyDailyLiabilitiesBetweenDates(company, startDate,
                        endDate, law);

                SpcfMoney totalIncomeTaxWithHeld = getTaxAmount(companyDailyLiabilities);
                SpcfMoney whTaxableWages = getTaxableWages(companyDailyLiabilities);

                SpcfMoney whPeriodPayment = totalPayments;
                SpcfMoney whTaxDue = (SpcfMoney) totalIncomeTaxWithHeld.subtract(totalPayments);

                String legalName = company.getLegalName();


                legalName = legalName.replaceAll("[^a-zA-Z0-9\\s]", "");
                legalName = StringUtils.normalizeSpace(legalName);

                String nameControl = crop(legalName.replaceAll("[^a-zA-Z0-9]", ""), 4).toUpperCase();

                String addressLine1 = company.getLegalAddress().getAddressLine1();

                addressLine1 = addressLine1.replaceAll("[^a-zA-Z0-9\\s]", "");
                addressLine1 = StringUtils.normalizeSpace(addressLine1);

                String city = company.getLegalAddress().getCity();
                city = city.replaceAll("[^a-zA-Z0-9\\s]", "");
                city = StringUtils.normalizeSpace(city);

                String state = company.getLegalAddress().getState();
                String zipCode = company.getLegalAddress().getZipCode();
                zipCode = zipCode.replaceAll("[^0-9]", "");

                totalProcessed++;
                int employeeCount = Paycheck.findEmployeeCount(company, startDate, endDate, law);
                ;

                // Start creating individual reports
                builder.append("<ReturnState stateSchemaVersion=\"" + STATE_SCHEMA_VERSION + "\">");
                builder.append(NEW_LINE);
                builder.append("<SubmissionId>" + getSubmissionId(passedInDate) + "</SubmissionId>");
                builder.append(NEW_LINE);

                // Return header state
                builder.append("<ReturnHeaderState binaryAttachmentCount=\"" + BINARY_ATTACHEMNT_COUNT + "\">");
                builder.append(NEW_LINE);
                builder.append("<Jurisdiction>KY</Jurisdiction>");
                builder.append(NEW_LINE);
                builder.append("<Timestamp>" + passedInDate.format(TIMESTAMP_FORMAT) + "</Timestamp>");
                builder.append(NEW_LINE);
                builder.append("<TaxPeriodBeginDate>" + startDate.format(YYYYMMDD) + "</TaxPeriodBeginDate>");
                builder.append(NEW_LINE);
                builder.append("<TaxPeriodEndDate>" + endDate.format(YYYYMMDD) + "</TaxPeriodEndDate>");
                builder.append(NEW_LINE);
                builder.append("<TaxYear>" + startDate.getYear() + "</TaxYear>");
                builder.append(NEW_LINE);
                builder.append("<SoftwareId>" + SOFTWARE_ID + "</SoftwareId>");
                builder.append(NEW_LINE);
                builder.append("<SignatureOption>");
                builder.append(NEW_LINE);
                builder.append("<SignatureDocument>Retain</SignatureDocument>");
                builder.append(NEW_LINE);
                builder.append("</SignatureOption>");
                builder.append(NEW_LINE);
                builder.append("<Filer>");
                builder.append(NEW_LINE);
                builder.append("<TIN>");
                builder.append(NEW_LINE);
                builder.append("<TypeTIN>FEIN</TypeTIN>");
                builder.append(NEW_LINE);
                builder.append("<TINTypeValue>" + company.getFedTaxId() + "</TINTypeValue>");
                builder.append(NEW_LINE);
                builder.append("</TIN>");
                builder.append(NEW_LINE);
                builder.append("<StateEIN>" + stateEIN + "</StateEIN>");
                builder.append(NEW_LINE);
                builder.append("<Name>");
                builder.append(NEW_LINE);
                builder.append("<BusinessNameLine1>" + legalName + "</BusinessNameLine1>");
                builder.append(NEW_LINE);
                builder.append("</Name>");
                builder.append(NEW_LINE);
                builder.append("<NameControl>" + nameControl + "</NameControl>");
                builder.append(NEW_LINE);

                builder.append("<USAddress>");
                builder.append(NEW_LINE);
                builder.append("<AddressLine1>" + addressLine1 + "</AddressLine1>");
                builder.append(NEW_LINE);
                builder.append("<City>" + city + "</City>");
                builder.append(NEW_LINE);
                builder.append("<State>" + state + "</State>");
                builder.append(NEW_LINE);
                builder.append("<ZIPCode>" + zipCode + "</ZIPCode>");
                builder.append(NEW_LINE);
                builder.append("</USAddress>");

                builder.append(NEW_LINE);
                builder.append("</Filer>");
                builder.append(NEW_LINE);

                builder.append("<Form>" + FORM_KY + "</Form>");
                builder.append(NEW_LINE);
                builder.append("<FilingAction>");
                builder.append(NEW_LINE);
                builder.append("<Action>Original</Action>");
                builder.append(NEW_LINE);
                builder.append("</FilingAction>");
                builder.append(NEW_LINE);
                builder.append("<AckAddress>" + ACK_ADDRESS + "</AckAddress>");
                builder.append(NEW_LINE);
                builder.append("</ReturnHeaderState>");
                builder.append(NEW_LINE);


                builder.append("<ReturnDataState>");
                builder.append(NEW_LINE);
                builder.append("<StateGeneralInformation>");
                builder.append(NEW_LINE);
                builder.append("<ReturnSigner>");
                builder.append(NEW_LINE);
                builder.append("<Name>" + RETURN_SIGNER_NAME + "</Name>");
                builder.append(NEW_LINE);
                builder.append("<Phone>" + RETURN_SIGNER_PHONE + "</Phone>");
                builder.append(NEW_LINE);
                builder.append("<EmailAddress>" + RETURN_SIGNER_EMAIL + "</EmailAddress>");
                builder.append(NEW_LINE);
                builder.append("<Signature>" + SIGNATURE + "</Signature>");
                builder.append(NEW_LINE);
                builder.append("<DateSigned>" + passedInDate.format(YYYYMMDD) + "</DateSigned>");
                builder.append(NEW_LINE);
                builder.append("</ReturnSigner>");
                builder.append(NEW_LINE);
                builder.append("<Contact>");
                builder.append(NEW_LINE);
                builder.append("<ContactName>");
                builder.append(NEW_LINE);
                builder.append("<FirstName>" + RETURN_SIGNER_FIRST_NAME + "</FirstName>");
                builder.append(NEW_LINE);
                builder.append("<LastName>" + RETURN_SIGNER_LAST_NAME + "</LastName>");
                builder.append(NEW_LINE);
                builder.append("</ContactName>");
                builder.append(NEW_LINE);
                builder.append("</Contact>");
                builder.append(NEW_LINE);
                builder.append("</StateGeneralInformation>");
                builder.append(NEW_LINE);
                builder.append("<HasTaxPayersPermssionToFileOnBehalf>true</HasTaxPayersPermssionToFileOnBehalf>");
                builder.append(NEW_LINE);
                builder.append("<HasTaxPayersPermssionToDiscussOnBehalf>false</HasTaxPayersPermssionToDiscussOnBehalf>");
                builder.append(NEW_LINE);
                builder.append("<KYWHState>");
                builder.append(NEW_LINE);
                builder.append("<StateWH>");
                builder.append(NEW_LINE);
                builder.append("<NumberOfEmployees>" + employeeCount + "</NumberOfEmployees>");
                builder.append(NEW_LINE);
                builder.append("<WHTaxableWages>" + whTaxableWages + "</WHTaxableWages>");
                builder.append(NEW_LINE);
                builder.append("<TotalIncomeTaxWithheld>" + totalIncomeTaxWithHeld + "</TotalIncomeTaxWithheld>");
                builder.append(NEW_LINE);
                builder.append("<WHPeriodPayments>" + whPeriodPayment + "</WHPeriodPayments>");
                builder.append(NEW_LINE);

                builder.append("<WHTaxDue>" + whTaxDue + "</WHTaxDue>");
                builder.append(NEW_LINE);
                builder.append("<WHPenalty>" + SpcfMoney.ZERO + "</WHPenalty>");
                builder.append(NEW_LINE);
                builder.append("<WHInterest>" + SpcfMoney.ZERO + "</WHInterest>");
                builder.append(NEW_LINE);
                builder.append("<WHTotalDue>" + whTaxDue + "</WHTotalDue>");
                builder.append(NEW_LINE);


                SpcfMoney whOverpayment = SpcfMoney.ZERO;
                if (whTaxDue.isLessThan(SpcfMoney.ZERO)) {
                    whOverpayment = (SpcfMoney) totalIncomeTaxWithHeld.subtract(totalPayments).abs();

                    builder.append("<WHOverpayment>");
                    builder.append(NEW_LINE);
                    builder.append("<AmountOfOverpayment>" + whOverpayment + "</AmountOfOverpayment>");
                    builder.append(NEW_LINE);
                    builder.append("</WHOverpayment>");
                    builder.append(NEW_LINE);
                }

                builder.append("</StateWH>");
                builder.append(NEW_LINE);
                builder.append("</KYWHState>");
                builder.append(NEW_LINE);
                builder.append("</ReturnDataState>");
                builder.append(NEW_LINE);
                builder.append("</ReturnState>");
                builder.append(NEW_LINE);
            }finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        return totalProcessed;
    }

    private SpcfMoney getTaxAmount(DomainEntitySet<CompanyDailyLiability> companyDailyLiabilities) {
        SpcfMoney totalTax = SpcfMoney.ZERO;
        for(CompanyDailyLiability companyDailyLiability : companyDailyLiabilities){
            totalTax = (SpcfMoney) totalTax.add(companyDailyLiability.getTaxAmount());
        }
        return totalTax;
    }

    private SpcfMoney getTaxableWages(DomainEntitySet<CompanyDailyLiability> companyDailyLiabilities) {
        SpcfMoney totalTaxableWages = SpcfMoney.ZERO;
        for(CompanyDailyLiability companyDailyLiability : companyDailyLiabilities){
            totalTaxableWages = (SpcfMoney) totalTaxableWages.add(companyDailyLiability.getTaxableWages());
        }
        return totalTaxableWages;
    }


    private String getSubmissionId(SpcfCalendar passedInDate){
        int oldSeq = ++submissionSeq;
        Calendar cal = Calendar.getInstance();
        cal.set(passedInDate.getYear(),passedInDate.getMonth(),passedInDate.getDay());
        int doy = cal.get(Calendar.DAY_OF_YEAR);
        String outPut = "0"+TRANSMITTER_ID + passedInDate.getYear() + String.format("%03d", doy) + String.format("%07d", oldSeq);
        return outPut;
    }

    private void updateSystemParameter(SystemParameter.Code pParameterCode, int val){
        try{
            ProcessResult pr = null;
            if(val > 999948)
                pr = PayrollServices.systemParameterManager.updateSystemParameterValue(pParameterCode,0 +"");
            else
                pr = PayrollServices.systemParameterManager.updateSystemParameterValue(pParameterCode,val +"");
            if (!pr.isSuccess()) {
                logger.error("failed to Update submission counter (" + val + ") to PSP_SYSTEM_PARAMETER under key: " + pParameterCode);
            }
        }catch (Throwable t){
            logger.error("Error to Update submission counter (" + val + ") to PSP_SYSTEM_PARAMETER under key: " + pParameterCode);
            throw new RuntimeException(t);
        }
    }
}
