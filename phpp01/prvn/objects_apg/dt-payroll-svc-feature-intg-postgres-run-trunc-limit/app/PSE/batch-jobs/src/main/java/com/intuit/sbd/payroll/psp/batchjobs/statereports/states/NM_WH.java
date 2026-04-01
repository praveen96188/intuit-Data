package com.intuit.sbd.payroll.psp.batchjobs.statereports.states;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.*;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.FSETReturnHeaderStateType;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.ReturnState;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.Transmission;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.TransmissionHeaderType;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Outputs New Mexico's coupon file<br>
 * <p/>
 * Example output:<br>
 * <pre>
 * <File xmlns="https://tap.state.nm.us/WebFiles/CombinedReportingSystem_2.0">
 * <FilerInformation>
 * <ReturnType>Combined Reporting System</ReturnType>
 * <PreparerId>
 * <PreparerFEIN>880146711</PreparerFEIN>
 * </PreparerId>
 * <PreparerName>Coreen Solano</PreparerName>
 * <PreparerPhone>7754248000</PreparerPhone>
 * <PreparerEmail>_TaxQEFiles@intuit.com</PreparerEmail>
 * </FilerInformation>
 * <Reports>
 * <TaxpayerInformation>
 * <TaxpayerID>
 * <TaxpayerFEIN>987654321</TaxpayerFEIN>
 * </TaxpayerID>
 * <TaxpayerCRS>12123456121</TaxpayerCRS>
 * <TaxpayerName>TEST_COMPANY_1</TaxpayerName>
 * <TaxpayerAddress>COLEGAL_AddressLine1,COLEGAL_AddressLine2,COLEGAL_AddressLine3</TaxpayerAddress>
 * <TaxpayerCity>Ridgewood</TaxpayerCity>
 * <TaxpayerState>NJ</TaxpayerState>
 * <TaxpayerZipCode>07450</TaxpayerZipCode>
 * <TaxpayerCountry>USA</TaxpayerCountry>
 * <TaxpayerPhone>7751111111</TaxpayerPhone>
 * <TaxpayerEmail>PrimaryPrincipal@aol.com</TaxpayerEmail>
 * </TaxpayerInformation>
 * <ReturnInformation>
 * <ReturnPeriodEndDate>2011-01-31</ReturnPeriodEndDate>
 * <AmendedFlag>N</AmendedFlag>
 * <GrossTax>0.00</GrossTax>
 * <CompensatingTax>0.00</CompensatingTax>
 * <WithholdingTax>000000068.00</WithholdingTax>
 * <TotalTax>000000068.00</TotalTax>
 * <Penalty>0.00</Penalty>
 * <Interest>0.00</Interest>
 * <TotalDue>000000068.00</TotalDue>
 * </ReturnInformation>
 * </Reports>
 * </File>
 *  </pre>
 */
public class NM_WH extends StateReportBase {
    private static String FILE_INFO_RETURN_TYPE = "Combined Reporting System";
    private static String FILE_INFO_PREPARER_FEIN = "880146711";
    private static String FILE_INFO_PREPARER_NAME = "Lisa Hyde";
    private static String FILE_INFO_PREPARER_PHONE = "7754248000";
    private static String FILE_INFO_PREPARER__EMAIL = "_TaxQEFiles@intuit.com";
    private static String TAXPAYER_INFO_EMAIL = "taxdeposits@intuit.com";
    private static String TAXPAYER_INFO_PHONE = "7754248000";

    private static String JURISDICTION = "NM";
    private static String STATEEIN_SUFFIX = "WWT";
    private static String TRANSMITTER_ETIN = "13699";
    private static String STATE_SCHEMA_VERSION = "FSETV5.6";
    private static String PAID_PREPARER_FEIN = "880146711";
    private static String PREPARER_PERSON_NAME = "Jason Shipp";
    private static String PREPARER_PHONE = "7754248000";
    private static String PREPARER_EMAIL = "taxdeposits@intuit.com";
    private static String FILING_ACTION = "Original";
    private static String TRANSMISSION_PREFIX = "NM-41414-";
    private static String TYPE_TIN="FEIN";
    private static String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static JAXBContext jaxbContext;

    private Marshaller mMarshaller;


    public NM_WH() {
        reportNamesList = new String[]{"NM-CRS1-PAYMENT"};
    }

    private static void initializeContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd");
        }
    }

    protected void initializeMarshaller() throws JAXBException {
        if (mMarshaller == null) {
            initializeContext();
            mMarshaller = jaxbContext.createMarshaller();
            mMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        }
    }

    @Override
    public void process(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        SpcfCalendar now = PSPDate.getPSPTime();

        /** The end date of the first half of the year */
        SpcfCalendar firstHalfEnd = SpcfCalendar.createInstance(now.getYear(), 6, 30, SpcfTimeZone.getLocalTimeZone());
        /** The end date of the second half of the year, report is generated on the following year (subtracting 1 from year) */
        SpcfCalendar secondHalfEnd = SpcfCalendar.createInstance(now.getYear() - 1, 12, 31, SpcfTimeZone.getLocalTimeZone());

        if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
            // Do nothing
        } else if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.QUARTERLY)) {
            // Skip quarterly as it is done along with monthly
            return;
        } else if (paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.SEMIANNUAL)) {
            // Skip semi annual as it is done along with monthly
            return;
        } else {
            // Unimplemented frequency, skip
            logger.info("Skipping processing of " + paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev()
                                + " for frequency " + paymentTemplateFrequency.getPaymentFrequencyId().toString());
            return;
        }

        SpcfCalendar[] dates = getPreviousPeriodStartAndEnd(passedInDate, paymentTemplateFrequency);

        SpcfCalendar startDate = dates[0];
        SpcfCalendar endDate = dates[1];

        SpcfCalendar monthEndDate = endDate.copy();
        CalendarUtils.clearTime(monthEndDate);

        boolean outputQuarterly = false;
        boolean outputSemiAnnual = false;

        ArrayList<PaymentTemplateFrequency> frequencyList = new ArrayList<PaymentTemplateFrequency>();
        frequencyList.add(paymentTemplateFrequency);

        if (CalendarUtils.getLastDayOfQuarter(monthEndDate).equals(monthEndDate)) {
            // Its the end of a quarter.  Add the quarterly reports now
            outputQuarterly = true;
        }

        if (monthEndDate.equals(firstHalfEnd) || monthEndDate.equals(secondHalfEnd)) {
            // Its semi annual end.  Add the semi annual reports now
            outputSemiAnnual = true;
        }

        StringBuilder builder = new StringBuilder();

        boolean enabledNEWCRS1WHStateReport = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_NEW_NHCRS1_REPORT,
                false);

        PaymentTemplateFrequency[] frequenciesArray = frequencyList.toArray(
                new PaymentTemplateFrequency[frequencyList.size()]);
        int totalProcessed = 0;
        if(enabledNEWCRS1WHStateReport){
            logger.info("ENABLE_NEW_NHCRS1_REPORT is on, NM_WH will generate new CRS1 report");
            try{
                initializeMarshaller();
                Transmission transmission = createHeaderV2(passedInDate);

                totalProcessed += createReportsReturnState(transmission, startDate, endDate,
                        DepositFrequencyCode.MONTHLY, passedInDate);

                if (outputQuarterly) {
                    // Output quarterly reports

                    PaymentTemplateFrequency quarterlyPaymentTemplateFrequency =
                            PaymentTemplateFrequency.getPaymentTemplateFrequency(
                            reportNamesList[0], DepositFrequencyCode.QUARTERLY);
                    frequencyList.add(quarterlyPaymentTemplateFrequency);

                    dates = getPreviousPeriodStartAndEnd(passedInDate, quarterlyPaymentTemplateFrequency);

                    totalProcessed += createReportsReturnState(transmission, dates[0], dates[1],
                            DepositFrequencyCode.QUARTERLY, passedInDate);
                }

                if (outputSemiAnnual) {
                    // Output semi annual reports
                    PaymentTemplateFrequency semiannualPaymentTemplateFrequency =
                            PaymentTemplateFrequency.getPaymentTemplateFrequency(
                            reportNamesList[0], DepositFrequencyCode.SEMIANNUAL);
                    frequencyList.add(semiannualPaymentTemplateFrequency);

                    dates = getPreviousPeriodStartAndEnd(passedInDate, semiannualPaymentTemplateFrequency);

                    totalProcessed += createReportsReturnState(transmission, dates[0], dates[1],
                            DepositFrequencyCode.SEMIANNUAL, passedInDate);
                }

                if(totalProcessed == -9999) {
                    logger.error("Not able to find the law in NM_WH report");
                    return;
                }

                StringWriter stringWriter = new StringWriter();
                mMarshaller.marshal(transmission, stringWriter);

                builder.append(stringWriter.toString());

            }catch (JAXBException e){
                logger.error("Problem with JAXB Marshaller");
                e.printStackTrace();
            }catch (DatatypeConfigurationException e){
                logger.error("Problem with XML Gregorian calender");
                e.printStackTrace();
            }
        }else{
            //OLD NH-CRS1 Report
            logger.info("ENABLE_NEW_NHCRS1_REPORT is off, NM_WH will generate old report");
            createHeader(builder);

            // Output monthly reports
            totalProcessed = createReport(DepositFrequencyCode.MONTHLY, startDate, endDate, builder);

            if (outputQuarterly) {
                // Output quarterly reports
                PaymentTemplateFrequency quarterlyPaymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                        reportNamesList[0], DepositFrequencyCode.QUARTERLY);
                frequencyList.add(quarterlyPaymentTemplateFrequency);

                dates = getPreviousPeriodStartAndEnd(passedInDate, quarterlyPaymentTemplateFrequency);

                totalProcessed += createReport(DepositFrequencyCode.QUARTERLY, dates[0], dates[1], builder);
            }

            if (outputSemiAnnual) {
                // Output semi annual reports
                PaymentTemplateFrequency semiannualPaymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                        reportNamesList[0], DepositFrequencyCode.SEMIANNUAL);
                frequencyList.add(semiannualPaymentTemplateFrequency);

                dates = getPreviousPeriodStartAndEnd(passedInDate, semiannualPaymentTemplateFrequency);

                totalProcessed += createReport(DepositFrequencyCode.SEMIANNUAL, dates[0], dates[1], builder);
            }

            // Create footer
            builder.append("</File>");
        }

        saveStateCoupon(builder, startDate, endDate, StateReportType.Recon, frequenciesArray);
        BatchUtils.createStateReportEmail(builder,".xml", totalProcessed, startDate, endDate, StateReportType.Recon, frequenciesArray);

    }

    /**
     * Creates a report for the given frequency
     *
     * @param frequencyCode The DepositFrequencyCode to create the report for
     * @param startDate     The start date to base the report on
     * @param endDate       The end date to base the report on
     * @param builder       The builder object to append to
     * @return The number of reports processed
     */
    private int createReport(DepositFrequencyCode frequencyCode, SpcfCalendar startDate, SpcfCalendar endDate,
                             StringBuilder builder) {
        PaymentTemplateFrequency quarterlyPaymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(
                reportNamesList[0], frequencyCode);

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactionsQuarterly =
                getMoneyMovementTransactions(quarterlyPaymentTemplateFrequency, startDate, endDate);

        logger.info("Running report " + frequencyCode.toString() + " for class " +
                            getClass().getSimpleName() + " for start " + startDate.format("yyyy/MM/dd") + " and end " + endDate.format("yyyy/MM/dd") +
                            " with " + companyToMoneyMovementTransactionsQuarterly.size() + " MMTs");

        return createReports(builder, startDate, endDate, companyToMoneyMovementTransactionsQuarterly);
    }


    private com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.Transmission createHeaderV2(SpcfCalendar passedInDate){
        try {
            com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.Transmission transmission = new Transmission();
            com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.TransmissionHeaderType transHeader =
                    new TransmissionHeaderType();
            transHeader.setRecordCount(BigInteger.valueOf(0));
            transHeader.setJurisdiction(JURISDICTION);
            transHeader.setTransmissionId(generateTransmissionId());
            transHeader.setTimestamp(formatTimeStamp(passedInDate,"TIME_ZOME"));

            TransmissionHeaderType.Transmitter transmitter = new TransmissionHeaderType.Transmitter();
            transmitter.setETIN(TRANSMITTER_ETIN);

            transHeader.setTransmitter(transmitter);

            transmission.setTransmissionHeader(transHeader);
            return transmission;
        }catch(DatatypeConfigurationException e){
            logger.error("error in generating transmission id");
            e.printStackTrace();
            return null;
        }
    }

    private int createReportsReturnState(
            com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.Transmission transmission,
            SpcfCalendar startDate, SpcfCalendar endDate, DepositFrequencyCode frequencyCode, SpcfCalendar passedInDate)
            throws DatatypeConfigurationException{
        DecimalFormat regularFormat = new DecimalFormat("0.00");
        String zeroFormatted = regularFormat.format(0.00D);

        int totalProcessed = 0;

        PaymentTemplateFrequency quarterlyPaymentTemplateFrequency =
                PaymentTemplateFrequency.getPaymentTemplateFrequency(reportNamesList[0], frequencyCode);

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>>
                companyToMoneyMovementTransactionsQuarterly =
                getMoneyMovementTransactions(quarterlyPaymentTemplateFrequency, startDate, endDate);

        logger.info("Running report " + frequencyCode.toString() + " for class " +
                getClass().getSimpleName() + " for start " + startDate.format("yyyy/MM/dd") + " and end "
                + endDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactionsQuarterly.size() + " MMTs");

        DomainEntitySet<Law> laws = Law.findWithholdingLawForTemplate(reportNamesList[0]);
        if (laws.size() != 1) {
            logger.fatal("Could not find law for NM Withholding.  Aborting!");
            return -9999;
        }
        Law law = laws.get(0);

        List<com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.ReturnState> returnStateList
                = transmission.getReturnState();
        for(Company company : companyToMoneyMovementTransactionsQuarterly.keySet()){
            try {
                pspRequestContextManager.setRequestContextCompany(company);
                ArrayList<MoneyMovementTransaction> mmts =
                        companyToMoneyMovementTransactionsQuarterly.get(company).getValueItem();
                MoneyMovementTransaction mmt = mmts.get(0);

                SpcfMoney totalPayments = getTotalPayments(mmts);
                SpcfMoney totalLiabilities = getTotalLiabilities(mmts);

                if (totalPayments.isLessThan(SpcfMoney.ZERO) || totalLiabilities.isLessThan(SpcfMoney.ZERO)) {
                    logger.error("Company " + company.getSourceCompanyId() +
                            " has a total payment or total liability that is negative, skipping.  Total Payments:" +
                            totalPayments.toString() + " Total Liabilities:" + totalLiabilities.toString());
                    continue;
                }

                totalProcessed++;

                //CREATING <RETURNSTATE>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.ReturnState returnState = new ReturnState();
                returnState.setStateSchemaVersion(STATE_SCHEMA_VERSION);
                returnState.setSubmissionId(generateSubmissionId());

                //CREATING <RETURNHEADESTATE>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.FSETReturnHeaderStateType
                        fsetReturnHeaderStateType = new FSETReturnHeaderStateType();
                fsetReturnHeaderStateType.setJurisdiction(JURISDICTION);
                fsetReturnHeaderStateType.setTimestamp(formatTimeStamp(passedInDate, "TIME_ZOME"));
                fsetReturnHeaderStateType.setTaxPeriodBeginDate(formatTimeStamp(startDate, "ONLY_DATE"));
                fsetReturnHeaderStateType.setTaxPeriodEndDate(formatTimeStamp(endDate, "ONLY_DATE"));

                // CREATING <PaidPreparerInformation>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.ReturnHeaderType.PaidPreparerInformation
                        paidPreparerInformation
                        = new com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.ReturnHeaderType.
                        PaidPreparerInformation();

                paidPreparerInformation.setFEIN(PAID_PREPARER_FEIN);
                paidPreparerInformation.setPreparerPersonName(PREPARER_PERSON_NAME);
                paidPreparerInformation.setPhone(PREPARER_PHONE);
                paidPreparerInformation.setEmailAddress(PREPARER_EMAIL);
                fsetReturnHeaderStateType.setPaidPreparerInformation(paidPreparerInformation);

                // CREATING <Filer>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.FSETReturnHeaderStateType.Filer filer
                        = new com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.FSETReturnHeaderStateType.Filer();

                // CREATING <TIN>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.TINType tinType
                        = new com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.TINType();
                tinType.setTypeTIN(TYPE_TIN);
                tinType.setTINTypeValue(company.getFedTaxId());
                filer.setTIN(tinType);

                String stateEin = prepareStateAgencyId(mmt, -1);
                stateEin = stateEin + STATEEIN_SUFFIX;
                filer.setStateEIN(stateEin);

                String legalName = company.getLegalName();

                legalName = legalName.replaceAll("[^a-zA-Z0-9\\s]", "");
                legalName = org.apache.commons.lang3.StringUtils.normalizeSpace(legalName);


                String addressLine1 = company.getLegalAddress().getAddressLine1();
                if (addressLine1 != null && !addressLine1.trim().equals("")) {
                    addressLine1 = addressLine1.replaceAll("[^a-zA-Z0-9\\s]", "");
                    addressLine1 = org.apache.commons.lang3.StringUtils.normalizeSpace(addressLine1);
                    if (addressLine1.length() > 35) {
                        addressLine1 = addressLine1.substring(0, 34);
                    }
                } else
                    addressLine1 = "";

                String city = company.getLegalAddress().getCity();
                String state = company.getLegalAddress().getState();
                String zipCode = company.getLegalAddress().getZipCode();

                // ATTACHING <Name>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.BusinessNameType businessNameType
                        = new com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.BusinessNameType();
                businessNameType.setBusinessNameLine1Txt(legalName);

                // CREATING </Name>
                filer.setName(businessNameType);

                // CREATING <USAddress>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.USAddressType usAddressType
                        = new com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.USAddressType();
                usAddressType.setAddressLine1Txt(addressLine1);
                usAddressType.setCityNm(city);
                usAddressType.setStateAbbreviationCd(
                        com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.StateType.NM);
                usAddressType.setZIPCd(zipCode);

                // ATTACHING </USAddress>
                filer.setUSAddress(usAddressType);

                // ATTACHING </Filer>
                fsetReturnHeaderStateType.setFiler(filer);

                // CREATING <FilingAction>
                FSETReturnHeaderStateType.FilingAction filingAction = new FSETReturnHeaderStateType.FilingAction();
                filingAction.setAction(FILING_ACTION);
                // ATTACHING </FilingAction>
                fsetReturnHeaderStateType.setFilingAction(filingAction);

                // ATTACHING </ReturnHeaderState>
                returnState.setReturnHeaderState(fsetReturnHeaderStateType);

                //CREATING <ReturnDataState>
                ReturnState.ReturnDataState returnDataState = new ReturnState.ReturnDataState();

                //CREATING <StateGeneralInformation>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.StateGeneralInformationType
                        stateGeneralInformationType
                        = new com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.StateGeneralInformationType();
                stateGeneralInformationType.setAddressChangeElect(
                        com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.CheckboxType.X);
                //ATTACHING </StateGeneralInformation>
                returnDataState.setStateGeneralInformation(stateGeneralInformationType);

                //CREATING <StateWH>
                com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.StateWHType stateWHType
                        = new com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.StateWHType();

                int employeeCount = Paycheck.findEmployeeCount(company, startDate, endDate, law);
                stateWHType.setNumberOfEmployees(BigInteger.valueOf(employeeCount));

                DomainEntitySet<CompanyDailyLiability> companyDailyLiabilities =
                        CompanyDailyLiability.findCompanyDailyLiabilitiesBetweenDates(company, startDate,
                                endDate, law);
                SpcfMoney whTaxableWages = getTaxableWages(companyDailyLiabilities);
                stateWHType.setWHTaxableWages(SpcfUtils.convertToBigDecimal(whTaxableWages));

                stateWHType.setWHNetTax(SpcfUtils.convertToBigDecimal(totalLiabilities));
                stateWHType.setWHInterest(BigDecimal.ZERO);
                stateWHType.setWHPenalty(BigDecimal.ZERO);
                stateWHType.setWHTotalTax(SpcfUtils.convertToBigDecimal(totalLiabilities));
                stateWHType.setWHAmountDue(SpcfUtils.convertToBigDecimal(totalLiabilities));

                //ATTACHING </StateWH>
                returnDataState.setStateWH(stateWHType);

                //ATTACHING </ReturnDataState>
                returnState.setReturnDataState(returnDataState);

                //ATTACHING </ReturnState>
                returnStateList.add(returnState);
            }finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }
        //Setting the total record count in <TransmissionHeader recordCount="0">;
        com.intuit.sbd.payroll.psp.batchjobs.statereports.states.xsd.TransmissionHeaderType transmissionHeaderType
                =  transmission.getTransmissionHeader();
        int existingCount = transmissionHeaderType.getRecordCount().intValue();
        transmissionHeaderType.setRecordCount(BigInteger.valueOf(totalProcessed+existingCount));

        return totalProcessed;
    }

    /**
     * Creates the reports for a all MoneyMovementTransactions passed in
     *
     * @param builder The builder to append to
     * @param companyToMoneyMovementTransactions
     *                The list of MoneyMovementTransactions to output
     * @return The number of reports processed
     */
    private int createReports(StringBuilder builder, SpcfCalendar startDate, SpcfCalendar endDate,
                              HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions) {
        DecimalFormat regularFormat = new DecimalFormat("0.00");
        String zeroFormatted = regularFormat.format(0.00D);

        int totalProcessed = 0;

        for (Company company : companyToMoneyMovementTransactions.keySet()) {
            ArrayList<MoneyMovementTransaction> mmts = companyToMoneyMovementTransactions.get(company).getValueItem();
            MoneyMovementTransaction mmt = mmts.get(0);
            SpcfMoney totalPayments = getTotalPayments(mmts);
            SpcfMoney totalLiabilities = getTotalLiabilities(mmts);

            if (totalPayments.isLessThan(SpcfMoney.ZERO) || totalLiabilities.isLessThan(SpcfMoney.ZERO)) {
                logger.error("Company " + company.getSourceCompanyId() + " has a total payment or total liability that is negative, skipping.  Total Payments:" +
                                     totalPayments.toString() + " Total Liabilities:" + totalLiabilities.toString());
                continue;
            }

            totalProcessed++;

            String totalLiabilitiesFormatted = getPaddedMoney(totalLiabilities, 9, 2, true);
            // Start creating individual reports
            builder.append("<Reports>");
            //begin:add taxpayer information
            builder.append("<TaxpayerInformation>");
            builder.append("<TaxpayerID>");
            builder.append("<TaxpayerFEIN>");
            builder.append(company.getFedTaxId());
            builder.append("</TaxpayerFEIN>");
            builder.append("</TaxpayerID>");

            builder.append("<TaxpayerCRS>");
            builder.append(prepareStateAgencyId(mmt, -1));
            builder.append("</TaxpayerCRS>");

            builder.append("<TaxpayerName>");
            builder.append(crop(company.getLegalName().replaceAll("&", ""),50));
            builder.append("</TaxpayerName>");
            Address legalAddress = company.getLegalAddress();
            builder.append("<TaxpayerAddress>");
            builder.append(getFullAddress(legalAddress));
            builder.append("</TaxpayerAddress>");

            builder.append("<TaxpayerCity>");
            builder.append(legalAddress.getCity());
            builder.append("</TaxpayerCity>");

            builder.append("<TaxpayerState>");
            builder.append(legalAddress.getState());
            builder.append("</TaxpayerState>");

            builder.append("<TaxpayerZipCode>");
            builder.append(legalAddress.getZipCode());
            builder.append("</TaxpayerZipCode>");

            builder.append("<TaxpayerCountry>");
            builder.append("USA");
            builder.append("</TaxpayerCountry>");
            //Contact contact = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
            builder.append("<TaxpayerPhone>");
            builder.append(TAXPAYER_INFO_PHONE);
            builder.append("</TaxpayerPhone>");

            builder.append("<TaxpayerEmail>");
            builder.append(TAXPAYER_INFO_EMAIL);
            builder.append("</TaxpayerEmail>");

            builder.append("</TaxpayerInformation>");
            //end:add tax payer information

            //begin:add return information
            builder.append("<ReturnInformation>");
            builder.append("<ReturnPeriodEndDate>");
            builder.append(endDate.format("yyyy-MM-dd"));
            builder.append("</ReturnPeriodEndDate>");

            builder.append("<AmendedFlag>");
            builder.append("N");
            builder.append("</AmendedFlag>");

            builder.append("<GrossTax>");
            builder.append(zeroFormatted);
            builder.append("</GrossTax>");

            builder.append("<CompensatingTax>");
            builder.append(zeroFormatted);
            builder.append("</CompensatingTax>");

            builder.append("<WithholdingTax>");
            builder.append(totalLiabilitiesFormatted);
            builder.append("</WithholdingTax>");

            builder.append("<TotalTax>");
            builder.append(totalLiabilitiesFormatted);
            builder.append("</TotalTax>");

            builder.append("<Penalty>");
            builder.append(zeroFormatted);
            builder.append("</Penalty>");

            builder.append("<Interest>");
            builder.append(zeroFormatted);
            builder.append("</Interest>");

            builder.append("<TotalDue>");
            builder.append(totalLiabilitiesFormatted);
            builder.append("</TotalDue>");
            builder.append("</ReturnInformation>");
            //end:add return information

            builder.append("</Reports>");
        }

        return totalProcessed;
    }

    private StringBuilder getFullAddress(Address pLegalAddress) {
        StringBuilder address = new StringBuilder();
        if (pLegalAddress == null) {
            return address;
        }
        if (StringUtils.isNotEmpty(pLegalAddress.getAddressLine3())) {
            address.append(pLegalAddress.getAddressLine1());
            if (StringUtils.isNotEmpty(pLegalAddress.getAddressLine2())) {
                address.append(",").append(pLegalAddress.getAddressLine2());
            }
            address.append(",").append(pLegalAddress.getAddressLine3());

        } else if (StringUtils.isNotEmpty(pLegalAddress.getAddressLine2())) {
            address.append(pLegalAddress.getAddressLine1()).append(",").append(pLegalAddress.getAddressLine2());
        } else {
            address.append(pLegalAddress.getAddressLine1());
        }
        int index =-1;
        String toReplace = "&";

        while ((index = address.lastIndexOf(toReplace)) != -1) {
            address.replace(index, index+toReplace.length() , "");
        }

        return address;
    }

    /**
     * Creates the header nodes for the file
     *
     * @param builder The builder to append to
     */
    private void createHeader(StringBuilder builder) {
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        builder.append("<File xmlns=\"https://tap.state.nm.us/WebFiles/CombinedReportingSystem_2.0\">");
        appendFilerInformation(builder);
    }

    @Override
    public boolean isScheduled(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        if (!paymentTemplateFrequency.getPaymentFrequencyId().equals(DepositFrequencyCode.MONTHLY)) {
            // Skip quarterly as it is done along with monthly
            // Skip semi annual as it is done along with monthly
            return false;
        }

        return checkDay(paymentTemplateFrequency, 25, 3, false);
    }

    private void appendFilerInformation(StringBuilder builder) {
        builder.append("<FilerInformation>");
        builder.append("<ReturnType>");
        builder.append(FILE_INFO_RETURN_TYPE);
        builder.append("</ReturnType>");

        builder.append("<PreparerId>");
        builder.append("<PreparerFEIN>");
        builder.append(FILE_INFO_PREPARER_FEIN);
        builder.append("</PreparerFEIN>");
        builder.append("</PreparerId>");

        builder.append("<PreparerName>");
        builder.append(FILE_INFO_PREPARER_NAME);
        builder.append("</PreparerName>");

        builder.append("<PreparerPhone>");
        builder.append(FILE_INFO_PREPARER_PHONE);
        builder.append("</PreparerPhone>");

        builder.append("<PreparerEmail>");
        builder.append(FILE_INFO_PREPARER__EMAIL);
        builder.append("</PreparerEmail>");
        builder.append("</FilerInformation>");
    }

    private SpcfMoney getTaxableWages(DomainEntitySet<CompanyDailyLiability> companyDailyLiabilities) {
        SpcfMoney totalTaxableWages = SpcfMoney.ZERO;
        for(CompanyDailyLiability companyDailyLiability : companyDailyLiabilities){
            totalTaxableWages = (SpcfMoney) totalTaxableWages.add(companyDailyLiability.getTaxableWages());
        }
        return totalTaxableWages;
    }

    private String generateTransmissionId(){
        String timeFormat = "yyyyMMdd'T'HH:mm:ss.S";
        SpcfCalendar passedInDate = PSPDate.getPSPTime();
        return TRANSMISSION_PREFIX+passedInDate.format(timeFormat);
    }
    private int counter = 1;
    private String generateSubmissionId(){
        String timeFormat = "yyyyMMdd";
        SpcfCalendar passedInDate = PSPDate.getPSPTime();
        return passedInDate.format(timeFormat)+String.format("%05d", counter++)+"nmcrs12";
    }

    private XMLGregorianCalendar formatTimeStamp(SpcfCalendar passedInDate, String removalPart)
            throws DatatypeConfigurationException{
        XMLGregorianCalendar gcNow = null;
        if("TIME_ZOME".equals(removalPart)) {
            DateFormat formatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
            Date dt = CalendarUtils.convertToDate(passedInDate);
            gcNow = DatatypeFactory.newInstance().newXMLGregorianCalendar(formatter.format(dt));
        }

        if("ONLY_DATE".equals(removalPart)){
            GregorianCalendar now = new GregorianCalendar();
            now.setTimeInMillis(passedInDate.getTimeInMilliseconds());
            gcNow = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            gcNow.setTime(DatatypeConstants.FIELD_UNDEFINED,DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,DatatypeConstants.FIELD_UNDEFINED);
            gcNow.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        }
        return  gcNow;
    }
}
