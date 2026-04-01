package com.intuit.sbd.payroll.psp.adapters.ade.processes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.json.AgencyResponse;
import com.intuit.sbd.payroll.psp.adapters.ade.json.ExchangeResponse;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AdditionalFilingIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.IRateConverter;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.RateConverterFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ADERateUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyFilingAmountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.AdditionalFilingAmount;
import com.intuit.sbd.payroll.psp.domain.AgencyRateRequest;
import com.intuit.sbd.payroll.psp.domain.AgencyRateRequestStatus;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.CompanyFilingAmount;
import com.intuit.sbd.payroll.psp.domain.CompanyLaw;
import com.intuit.sbd.payroll.psp.domain.CompanyLawRate;
import com.intuit.sbd.payroll.psp.domain.CompanyRateRequest;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.domain.LawCategoryCode;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateCategory;
import com.intuit.sbd.payroll.psp.domain.RateRequestStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * User: TimothyD698
 * Date: 4/1/13
 */
public class RateApplyProcess implements Runnable {

    protected static final SpcfLogger logger = Application.getLogger(RateApplyProcess.class);
    protected static String DIR_ROOT = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_ade_directory_root");
    public static String RESPONSE_FOLDER = "/response/";
    public static String SOURCE_SYSTEM = "ASSISTED";
    public static final String RESPONSE_FILE_EXT = ".json";

    private String state;
    private int year = 0;
    private int quarter = 0;
    private String requestSeq;

    public RateApplyProcess(String pState, int pYear, int pQuarter, String pRequestSeq) {
        state = pState;
        year = pYear;
        quarter = pQuarter;
        requestSeq = pRequestSeq;
    }

    public void run() {

        int updateCount = 0;
        AgencyRateRequestStatus status;

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);

        try {
            // Make sure the directory path exists.
            ADERateUtils.verifyDirectory(DIR_ROOT + RESPONSE_FOLDER);

            // Find the Agency Request.
            Application.beginUnitOfWork();
            AgencyRateRequest request = Application.findById(AgencyRateRequest.class, SpcfUniqueId.createInstance(requestSeq));
            Application.commitUnitOfWork();

            AgencyResponse jsonInput;
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            FileInputStream inputStream = new FileInputStream(DIR_ROOT + RESPONSE_FOLDER + state + "_" + year + "_" + quarter + "_" + requestSeq + RESPONSE_FILE_EXT);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            jsonInput = gson.fromJson(inputReader, AgencyResponse.class);

            // Verify that the request and response match.
            if (!request.getAgency().getAgencyId().startsWith(jsonInput.getAgencyRates().getJurisdiction())) {
                logger.warn("Invalid response jurisdiction for request.");
                status = AgencyRateRequestStatus.Cancelled;
                // Requested year/quarter should match the year/quarter in the file.
            } else if (!request.getYearQuarter().equals(jsonInput.getAgencyRates().getYear() + jsonInput.getAgencyRates().getQuarter())) {
                logger.warn("Invalid year/quarter in response for request.");
                status = AgencyRateRequestStatus.Cancelled;
                // We supply a GUID in the request that should come back in the response.
                // For now, if we get a null response, we'll accept that as well.
            } else if (jsonInput.getAgencyRates().getClientRequestId() != null &&
                    !request.getId().toString().equals(jsonInput.getAgencyRates().getClientRequestId())) {
                logger.warn("Invalid response client request ID for request.");
                status = AgencyRateRequestStatus.Cancelled;
            } else if (!SOURCE_SYSTEM.equals(jsonInput.getAgencyRates().getSourceSystem())) {
                logger.warn("Invalid response source system ID for request.");
                status = AgencyRateRequestStatus.Cancelled;
            } else {
                // Attempt to apply the contents to the database.
                updateCount = applyRates(jsonInput.getAgencyRates().getExchangeResponse(), request);
                request.setRecordCount(updateCount);
                status = AgencyRateRequestStatus.ResponseApplied;
            }

            Application.beginUnitOfWork();
            request = Application.refresh(request);
            request.setRecordCount(updateCount);
            request.setStatus(status);
            Application.commitUnitOfWork();

        } catch (FileNotFoundException fnf) {
            logger.error("File Not Found Exception", fnf);
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    private int applyRates(List<ExchangeResponse> responses, AgencyRateRequest request) {

        int recordCount = 0;
        boolean pushToQuickbooks = true;

        // Should be able to use a single effective date for all rates based on the year/quarter of the request.
        SpcfCalendar effectiveDate = CalendarUtils.getFirstDayOfQuarter(year, quarter);

        // We should be able to use a single instance of this since each response file is specific to a single state/jurisdiction.
        IRateConverter converter = RateConverterFactory.createInstance(state);

        for (ExchangeResponse response : responses) {
            // Begin a new UOW for each company so can commit/rollback all rate changes together.
            Application.beginUnitOfWork();

            Company company = findCompany(response);

            // Verify we can find this company.
            if (company == null) {
                logger.warn("Unable to find Company for Source Company Id - " + response.getCompanyId() );

            } else {

                // Find the Company Rate Request.
                DomainEntitySet<CompanyRateRequest> rateRequests = Application.find(CompanyRateRequest.class,
                                                                                    CompanyRateRequest.AgencyRateRequest().equalTo(request)
                                                                                                      .And(CompanyRateRequest.CompanyAgency().Company().equalTo(company)));

                if (rateRequests.size() != 1) {
                    logger.warn("Unable to find single CompanyRateRequest for Company - " + company.getSourceCompanyId());
                } else {
                    CompanyRateRequest rateRequest = rateRequests.getFirst();
                    Map<Law, BigDecimal> rates = converter.getRates(state, response.getUiRate(), response.getTaxRate());
                    StringBuilder statusMessage = new StringBuilder();
                    RateRequestStatus status = rateRequest.getStatus();
                    RateRequestStatus baseRateStatus = null;

                    for (Law suiLaw : rates.keySet()) {
                        // ADE Rates are percentages, we compare and store decimal values.
                        BigDecimal rate = ADERateUtils.convertPercentageToDecimal(suiLaw, rates.get(suiLaw));
                        boolean isBaseRate = suiLaw.getLawCategoryCode().equals(LawCategoryCode.UnemploymentEmployer);

                        // Determine the current rate for this Company/Law
                        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, suiLaw.getLawId());
                        CompanyLawRate currentRate = CompanyLawRate.findEffectiveLawRate(companyLaw, effectiveDate);

                        // See if the rate has changed.
                        if (currentRate != null && rate.doubleValue() == currentRate.getRate()) {
                            // Rates are the same.
                            status = determineNewStatus(baseRateStatus, RateRequestStatus.NoChange);
                            if (isBaseRate) {
                                //If base rate ,then only determine new status if no change in rate.
                                baseRateStatus = status;
                                rateRequest.setNewRate(rate.doubleValue());
                            }
                            statusMessage.append(suiLaw.getLawTypeCd()).append(" is unchanged|");
                            // Verify the new rate is a valid value for the law.
                        } else if (!rateIsValidValueAndWithinRange(suiLaw, rate)) {
                            logger.warn("Rate of " + rate.toString() + " is not valid for Law Id " + suiLaw.getLawId());
                            status = determineNewStatus(status, RateRequestStatus.Error);
                            if (isBaseRate) {
                                baseRateStatus = status;
                            }
                            statusMessage.append(suiLaw.getLawTypeCd());
                            statusMessage.append(" rate of ");
                            statusMessage.append(rate);
                            statusMessage.append(" is invalid|");
                            // Attempt to update the rate.
                        } else {
                            // Update the rate.
                            ProcessResult<CompanyLawRate> results = PayrollServices.companyManager.updateCompanyLawRate(company.getSourceSystemCd(),
                                                                                                                        company.getSourceCompanyId(),
                                                                                                                        suiLaw,
                                                                                                                        effectiveDate,
                                                                                                                        rate.doubleValue(),
                                                                                                                        true);
                            if (results.isSuccess()) {
                                status = determineNewStatus(status, RateRequestStatus.Applied);
                                if (isBaseRate) {
                                    baseRateStatus = status;
                                    // Save the old/new rate values.
                                    rateRequest.setOldRate(currentRate == null ? 0 : currentRate.getRate());
                                    rateRequest.setNewRate(rate.doubleValue());
                                }
                                statusMessage.append(suiLaw.getLawTypeCd());
                                statusMessage.append(" updated to ");
                                statusMessage.append(rate);
                                statusMessage.append("|");
                            } else {
                                status = determineNewStatus(status, RateRequestStatus.Error);
                                if (isBaseRate) {
                                    baseRateStatus = status;
                                }
                                String errorMessage = results.getErrorMessages().size() > 0 ? results.getErrorMessages().get(0).getMessage() : "Unknown Error";
                                logger.warn("Error applying rate of " + rate.toString() + " for " + suiLaw.getLawId() + " - " + errorMessage);
                                statusMessage.append(suiLaw.getLawTypeCd());
                                statusMessage.append(" error applying - ");
                                statusMessage.append(errorMessage);
                                statusMessage.append("|");
                            }
                        }

                    }

                    if (response.getTaxRate() != null && (baseRateStatus == RateRequestStatus.Applied || baseRateStatus == RateRequestStatus.NoChange) && status != RateRequestStatus.Error) {
                        RateRequestStatus addFilingrateStatus = updateAdditionalFilingRate(company, response.getTaxRate(), effectiveDate, statusMessage);
                        if (addFilingrateStatus != null) {
                            status = addFilingrateStatus;
                        }
                    }
                    if (baseRateStatus == RateRequestStatus.NoChange && status != RateRequestStatus.Error) {
                        status = baseRateStatus;
                    } else if (baseRateStatus == RateRequestStatus.Applied && status == RateRequestStatus.NoChange) {
                        status = baseRateStatus;
                    }

                    // If we encountered any error during the rate generation/apply process, roll back the changes
                    // so that none of the rates are applied for this company.
                    if (status == RateRequestStatus.Error) {
                        Application.rollbackUnitOfWork();

                        // Start a new UOW so we can persist the status and status message.
                        Application.beginUnitOfWork();
                        Application.refresh(rateRequest);
                    }

                    // Persist the status and status message.
                    rateRequest.setStatus(status);
                    // Some states have enough tax rates that the combined status message can become too large.
                    String statusMessageString = statusMessage.toString();
                    if (statusMessageString.length() > 256) {
                        statusMessageString = statusMessageString.substring(0, 256);
                    }
                    rateRequest.setStatusMessage(statusMessageString);
                    Application.commitUnitOfWork();

                }

                recordCount++;
            }

            // If we were unable to find the Company or the request, we can just rollback.
            if (Application.hasActiveTransaction()) {
                Application.rollbackUnitOfWork();
            }
        }

        return recordCount;
    }


    private RateRequestStatus updateAdditionalFilingRate(Company pCompany, Map<String, BigDecimal> pTaxRate, SpcfCalendar pEffectiveDate, StringBuilder pStatusMessage) {
        if (pCompany == null || pTaxRate == null || pStatusMessage == null) {
            return null;
        }
        RateRequestStatus status = null;
        for (String taxItem : pTaxRate.keySet()) {
            try {
                String atfLawId = AdditionalFilingIdMapper.getPspAtfLawIdByComplianceAdditionalId(taxItem);
                Criterion<AdditionalFilingAmount> afaWhere = AdditionalFilingAmount.ATFLawId().equalTo(atfLawId);
                DomainEntitySet<AdditionalFilingAmount> additionalFilingAmountSet = Application.find(AdditionalFilingAmount.class, afaWhere);
                if (additionalFilingAmountSet == null || additionalFilingAmountSet.size() == 0) {
                    return null;
                }
                if (pEffectiveDate == null) {
                    pEffectiveDate = CalendarUtils.getFirstDayOfQuarter(year, quarter);
                }

                AdditionalFilingAmount additionalFilingAmount = additionalFilingAmountSet.getFirst();
                CompanyFilingAmountDTO companyFilingAmountDTO = new CompanyFilingAmountDTO();
                companyFilingAmountDTO.setName(additionalFilingAmount.getName());
                companyFilingAmountDTO.setEffectiveDate(new DateDTO(pEffectiveDate));
                BigDecimal rate = additionalFilingAmount.getRate() ? ADERateUtils.convertPercentageToDecimal(pTaxRate.get(taxItem)) : ADERateUtils.applyPrecision(pTaxRate.get(taxItem));
                BigDecimal rateInDecimal = additionalFilingAmount.getRate() ? ADERateUtils.convertPercentageToDecimal(pTaxRate.get(taxItem)) : pTaxRate.get(taxItem);
                companyFilingAmountDTO.setAmount(rateInDecimal.doubleValue());
                ProcessResult<CompanyFilingAmount> processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(pCompany.getSourceSystemCd(),
                                                                                                                                 pCompany.getSourceCompanyId(),
                                                                                                                                 companyFilingAmountDTO);
                if (processResult.isSuccess()) {
                    pStatusMessage.append(companyFilingAmountDTO.getName());
                    pStatusMessage.append(" updated to ");
                    pStatusMessage.append((rate.toString()));
                    pStatusMessage.append("|");
                    status = determineNewStatus(status, RateRequestStatus.Applied);
                } else if (!processResult.isSuccess()) {
                    String errorMessage = processResult.getErrorMessages().size() > 0 ? processResult.getErrorMessages().get(0).getMessage() : "Unknown Error";
                    logger.warn("Error applying rate of " + pTaxRate.get(taxItem).toString() + " for " + taxItem + " - " + errorMessage);
                    pStatusMessage.append(companyFilingAmountDTO.getName());
                    pStatusMessage.append(" error applying - ");
                    pStatusMessage.append(errorMessage);
                    pStatusMessage.append("|");
                    status = determineNewStatus(status, RateRequestStatus.Error);
                }

            } catch (Exception ex) {
                status = determineNewStatus(status, RateRequestStatus.Error);
                logger.info("Exception during applying additional amount for taxitem " + taxItem + ", psid:" + pCompany.getSourceCompanyId() + ". reason is", ex);
            } finally {
                //Nothing at present
            }

        }
        return status;
    }

    private Company findCompany(ExchangeResponse exchangeResponse) {

        // First look by Source System/PSID.
        Company company = Company.findCompany(exchangeResponse.getCompanyId(), SourceSystemCode.QBDT);

        // If company has changed PSID since request was generated, see if we can find them by FEIN/AID.
        if (company == null) {
            // Find the SUI Payment Template for this state.
            DomainEntitySet<PaymentTemplate> pts = Application.find(PaymentTemplate.class,
                                                                    PaymentTemplate.PaymentTemplateCd().like(state + "-%")
                                                                                   .And(PaymentTemplate.Category().equalTo(PaymentTemplateCategory.SUI)));
            if (pts.size() == 1) {
                Criterion<CompanyAgencyPaymentTemplate> criterion = null;
                if(exchangeResponse.getFein() == null){
                    criterion = CompanyAgencyPaymentTemplate.CompanyAgency().Company().FedTaxIdEnc().isNull();
                }else{
                    List<String> feinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,exchangeResponse.getFein());
                    criterion = CompanyAgencyPaymentTemplate.CompanyAgency().Company().FedTaxIdEnc().in(feinEncList);
                }
                criterion = criterion.And(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(pts.getFirst()));
                if(exchangeResponse.getStateEin() == null){
                    criterion = criterion.And(CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().isNull());
                }else{
                    List<String> agencyTaxerPayerIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName,exchangeResponse.getStateEin());
                    criterion = criterion.And(CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().in(agencyTaxerPayerIdEncList));
                }
                Expression<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplateExpression = new Query<CompanyAgencyPaymentTemplate>().Where(criterion);
                DomainEntitySet<CompanyAgencyPaymentTemplate> capts = Application.find(CompanyAgencyPaymentTemplate.class,companyAgencyPaymentTemplateExpression);
                if (capts.size() == 1) {
                    company = capts.getFirst().getCompanyAgency().getCompany();
                }
            }

        }

        return company;
    }

    private static boolean rateIsValidValueAndWithinRange(Law law, BigDecimal rate) {
        return law.rateIsValidValue(SpcfDecimal.createInstance(rate.toString())) && law.rateFallsWithinRange(SpcfDecimal.createInstance(rate.toString()));
    }

    private RateRequestStatus determineNewStatus(RateRequestStatus currentStatus, RateRequestStatus newStatus) {

        // An error will always be an error.
        if (currentStatus == RateRequestStatus.Error || newStatus == RateRequestStatus.Error) {
            return RateRequestStatus.Error;
            // Outside of an error, any status can be updated to Applied.
        } else if (newStatus == RateRequestStatus.Applied) {
            return RateRequestStatus.Applied;
            // Only update to NoChange if the current status was not Applied.
        } else if (newStatus == RateRequestStatus.NoChange && currentStatus != RateRequestStatus.Applied) {
            return RateRequestStatus.NoChange;
        } else {
            return newStatus;
        }

    }

}
