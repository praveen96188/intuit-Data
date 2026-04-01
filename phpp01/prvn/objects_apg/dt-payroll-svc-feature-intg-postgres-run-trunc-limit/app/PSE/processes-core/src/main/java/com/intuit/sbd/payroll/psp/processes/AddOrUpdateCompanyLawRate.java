package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyLawRateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * User: mwaqarbaig
 * Date: Oct 26, 2010
 * Time: 1:37:28 PM
 */
public class AddOrUpdateCompanyLawRate extends Process implements IProcess {
    private Company company;
    private List<CompanyLawRateDTO> companyLawRateDTOs;
    private String sourceCompanyId;
    private String sourceLawId;
    private SourceSystemCode sourceSystemCd;
    private boolean mIsSystemUpdate;
    public static final SpcfCalendar EARLIEST_DATE = SpcfCalendar.createInstance(1990, 1, 1, SpcfTimeZone.getLocalTimeZone());

    /*
     * Constructor for AddOrUpdateCompanyLawRate
     *
     */
    public AddOrUpdateCompanyLawRate(SourceSystemCode sourceSystemCd, String sourceCompanyId, String sourceLawId, List<CompanyLawRateDTO> companyLawRateDTOs, boolean pIsSystemUpdate) {
        this.sourceCompanyId = sourceCompanyId;
        this.sourceSystemCd = sourceSystemCd;
        this.sourceLawId = sourceLawId;
        this.companyLawRateDTOs = companyLawRateDTOs;
        mIsSystemUpdate = pIsSystemUpdate;
    }

    public ProcessResult validate() {
        /*  Ensure that passed parameters are valid     */
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters
                (sourceSystemCd, sourceCompanyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        /*  Ensure Company Exists   */
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (sourceLawId == null) {
            validationResult.getMessages().InvalidArgument(EntityName.CompanyLaw, "null", "sourceLawId");
            return validationResult;
        }

        if (companyLawRateDTOs != null) {
            for (CompanyLawRateDTO companyLawRateDTO : companyLawRateDTOs) {

                if (companyLawRateDTO.getEffectiveDate() != null) {
                    /*  Effective date must be a valid date */
                    validationResult.merge(companyLawRateDTO.getEffectiveDate().validate());
                    if (!validationResult.isSuccess()) {
                        return validationResult;
                    }
                    /*  Effective date must be on quarter boundary   */
                    SpcfCalendar effectiveDate = companyLawRateDTO.getEffectiveDate().toSpcfCalendar();
                    if (CalendarUtils.getFirstDayOfQuarter(effectiveDate).subtract(effectiveDate) != 0) {
                        validationResult.getMessages().EffectiveDateNotOnQuarterStart(EntityName.CompanyLaw,
                                this.sourceSystemCd.toString(), this.sourceCompanyId, this.sourceLawId,
                                companyLawRateDTO.getEffectiveDate().toString());
                    }
                }

            }
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(this.company, this.sourceLawId);
        DomainEntitySet<CompanyLawRate> currentRates = companyLaw.getCompanyLawRateCollection().find(CompanyLawRate.InvalidDate().isNull());

        // find rates that already exist and remove them from both the DTO list and the current rates list
        for (Iterator<CompanyLawRateDTO> iterator = companyLawRateDTOs.iterator(); iterator.hasNext() && currentRates.size() > 0; ) {
            CompanyLawRateDTO lawRateDTO = iterator.next();

            Criterion<CompanyLawRate> companyLawRateCriterion = CompanyLawRate.Rate().equalTo(lawRateDTO.getRate());
            //Rate and rate type should match - 9% vs $9 are different values
            companyLawRateCriterion.And(CompanyLawRate.RateType().equalTo(lawRateDTO.getRateType()));
            if(lawRateDTO.getEffectiveDate() != null) {
                companyLawRateCriterion = companyLawRateCriterion.And(CompanyLawRate.EffectiveDate().equalTo(lawRateDTO.getEffectiveDate().toSpcfCalendar()));
            } else {
                companyLawRateCriterion = companyLawRateCriterion.And(CompanyLawRate.EffectiveDate().equalTo(EARLIEST_DATE));
            }

            DomainEntitySet<CompanyLawRate> matchingRates = currentRates.find(companyLawRateCriterion);
            if(matchingRates.size() > 0) {
                currentRates.removeAll(matchingRates);
                iterator.remove();
            }
        }

        // invalidate any current rates that do not match
        for (CompanyLawRate currentRate : currentRates) {
            currentRate.setInvalidDate(PSPDate.getPSPTime());
            Application.save(currentRate);
        }

        Collections.sort(companyLawRateDTOs, new LawRatesDTOSorter());
        if(!companyLawRateDTOs.isEmpty()) {

            StringBuilder newValues = new StringBuilder();
            newValues.append(String.format("\n\nTax Law Modified: <b>%s</b>\nNew Rates:\n", companyLaw.getSourceDescription()));
            for (CompanyLawRateDTO companyLawRateDTO : companyLawRateDTOs) {
                CompanyLawRate companyLawRate = new CompanyLawRate();
                companyLawRate.setCompanyLaw(companyLaw);
                companyLaw.addCompanyLawRate(companyLawRate);

                if (companyLawRateDTO.getEffectiveDate() == null) {
                    companyLawRateDTO.setEffectiveDate(new DateDTO(EARLIEST_DATE));
                }

                DateDTO dtoEffectiveDate = companyLawRateDTO.getEffectiveDate();
                //noinspection ConstantConditions
                companyLawRate.setEffectiveDate(dtoEffectiveDate.toSpcfCalendar());
                newValues.append("\t\t\tEff: ").append(dtoEffectiveDate.getYear()).append(" Q").append(CalendarUtils.getQuarterAsInt(dtoEffectiveDate.toSpcfCalendar()));


                companyLawRate.setRate(companyLawRateDTO.getRate());
                companyLawRate.setRateType(companyLawRateDTO.getRateType());
                DecimalFormat rateFormatter = new DecimalFormat("#0.0000 %");
                LawRateRange lawRateRange = companyLaw.getLaw().getLawRateRange();
                if (lawRateRange != null) {
                    rateFormatter.setMaximumFractionDigits(lawRateRange.getPrecision() - 2);
                }

                newValues.append("\t\t\tRate: ").append(rateFormatter.format(companyLawRateDTO.getRate())).append("\n\n");

                /**
                 * PSRV004127: Duplicate PITEM  from QB for the same Law Causes Duplicate Company Law Rate entries
                 * Company Law Rates coming through OFX transactions are not committed in DB yet, so there are not de-duped by the erstwhile logic
                 * Adding new call  findValidCompanyLawRateByRateAndEffectiveDates to check the hibernate cache
                 */
                CompanyLawRate cachedCompanyLawRate = CompanyLawRate.findValidCompanyLawRateByRateAndEffectiveDates(companyLaw, companyLawRate.getRate(), companyLawRate.getEffectiveDate(), companyLawRate.getInvalidDate());
                if(cachedCompanyLawRate==null) {
                    Application.save(companyLawRate);
                    if (processResult.isSuccess()) {
                        companyLawRate.cache();
                    }
                }
            }
            String eventDetailValue = newValues.toString();

            // truncate to 4000 characters
            if (eventDetailValue.length() > 4000) {
                eventDetailValue = eventDetailValue.substring(0, 4000);
            }

            /*  Create an event to reflect the changes made */
            CompanyEvent.createCompanyEventAndDetail(this.company, EventTypeCode.CompanyLawUpdated, EventDetailTypeCode.Details, eventDetailValue);

            // if any rates were updated updated by the system update the rate push token
            if(mIsSystemUpdate) {
                companyLaw.getQbdtPayrollItemInfo().setRatePushToken(company.getNextToken());
                Application.save(companyLaw.getQbdtPayrollItemInfo());
            }
        }

        return processResult;
    }

    private class LawRatesDTOSorter implements Comparator<CompanyLawRateDTO> {
        public int compare(CompanyLawRateDTO arg0, CompanyLawRateDTO arg1) {
            DateDTO firstEffectiveDate = arg0.getEffectiveDate();
            DateDTO secondEffectiveDate = arg1.getEffectiveDate();
            if (firstEffectiveDate == null && secondEffectiveDate == null) {
                return 0;
            } else if (firstEffectiveDate == null) {
                return -1;
            } else if (secondEffectiveDate == null) {
                return 1;
            } else {
                return firstEffectiveDate.compareTo(secondEffectiveDate);
            }
        }
    }
}
