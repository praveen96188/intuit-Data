package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Hand-written business logic
 */
public class OffloadGroup extends BaseOffloadGroup {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static OffloadGroup findOffloadGroup(String pGroupCode) {
        DomainEntitySet<OffloadGroup> offloadGroups = Application.find(OffloadGroup.class, OffloadGroupCd().equalTo(pGroupCode));
        if (offloadGroups.size() > 0) {
            return offloadGroups.iterator().next();
        } else {
            return null;
        }
    }

    public static OffloadGroup findStandardOffloadGroup() {
        return findOffloadGroup(Codes.STANDARD);
    }

    public static OffloadGroup findTaxPaymentOffloadGroup() {
        return findOffloadGroup(Codes.TAXPAYMENT);
    }

    public static OffloadGroup findPSPOffloadsOffloadGroup() {
        return findOffloadGroup(Codes.PSPOFFLOADS);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public OffloadGroup() {
        super();
    }

    /**
     * Determines whether the given date/time (calendar argument) is before the cutoff time for the given offload group.
     *
     * @param calendar The date/time in question
     * @return TRUE if and only if the given calendar is prior to the offload group's cutoff time
     */
    public boolean isBeforeCutoffTime(final SpcfCalendar calendar) {
        SpcfCalendar cutoffCalendar = getCalendarForCutoffTime(PSPDate.getPSPTime());
        return calendar.before(cutoffCalendar);
    }

    /**
     * This method returns an SpcfCalendar representing the payroll subission cutoff time for the given date.
     * It is initialized as follows:
     * Date portion = pCalendar.getYear(), pCalendar.getMonth(), pCalendar.getDay()
     * Time portion = pOffloadGroup.getCutoffTime()
     *
     * @param pCalendar The date to use for the cutoff time
     * @return An SpcfCalendar containing: date:cutoff-time
     */
    public SpcfCalendar getCalendarForCutoffTime(final SpcfCalendar pCalendar) {
        SpcfCalendar cutoffCalendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        String offloadGroupCutoff = getCutoffTime();

        String[] offloadTime = offloadGroupCutoff.split(":");
        Integer offloadHours = Integer.parseInt(offloadTime[0]);
        Integer offloadMinutes = Integer.parseInt(offloadTime[1]);
        Integer offloadSeconds = Integer.parseInt(offloadTime[2]);
        cutoffCalendar.setValues(pCalendar.getYear(), pCalendar.getMonth(), pCalendar.getDay(), offloadHours,
                offloadMinutes, offloadSeconds, 0);

        return cutoffCalendar;
    }

    /**
     * Determines whether the current date/time is before the cutoff time for the given offload group.
     *
     * @return TRUE if and only if the current system time is prior to the offload group's cutoff time
     */
    public boolean isBeforeCutoffTime() {
        return isBeforeCutoffTime(PSPDate.getPSPTime());
    }

    /**
     * Determines whether the given date/time (calendar argument) is before the cutoff time for the given offload group,
     * taking into account a scheduled second offload (if present).
     *
     * @param calendar The date/time in question
     * @return TRUE if and only if the given calendar is prior to the offload group's cutoff time
     */
    public boolean isBeforeActualCutoffTime(final SpcfCalendar calendar) {
        SpcfCalendar cutoffCalendar = getCalendarForActualCutoffTime(PSPDate.getPSPTime());
        return calendar.before(cutoffCalendar);
    }

    public static boolean isBeforeTOKCutoffTime(final SpcfCalendar testDate, final SpcfCalendar currentDate) {
        SourcePayrollParameter thirdParty401kCutoffTime = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT, SourcePayrollParameterCode.ThirdParty401kCutoffTime);
        String cutOff = thirdParty401kCutoffTime.getParameterValue();
        SpcfCalendar tokCutoffCalendar = CalendarUtils.getCalendarForDateAndTime(testDate, cutOff);
        return currentDate.before(tokCutoffCalendar);
    }

    /**
     * Determines whether the current date/time is before the cutoff time for the given offload group, taking into
     * account a scheduled second offload (if present).
     *
     * @return TRUE if and only if the current system time is prior to the offload group's cutoff time
     */
    public boolean isBeforeActualCutoffTime() {
        return isBeforeActualCutoffTime(PSPDate.getPSPTime());
    }

    /**
     * This method returns an SpcfCalendar representing the payroll submission cutoff time for the given date. It will
     * take into account any cutoff time extension due to a scheduled second offload, if one is present.
     * It is initialized as follows:
     * Date portion = pCalendar.getYear(), pCalendar.getMonth(), pCalendar.getDay()
     * Time portion = SecondOffload.OverideCutoffTime or pOffloadGroup.getCutoffTime() if no second offload is
     * scheduled.
     *
     * @param pCalendar The date to use for the cutoff time and second offload
     * @return An SpcfCalendar containing: date:cutoff-time
     */
    public SpcfCalendar getCalendarForActualCutoffTime(final SpcfCalendar pCalendar) {
        SecondOffload secondOffload = getSecondOffload(pCalendar);
        SpcfCalendar cutoffCalendar;

        if (secondOffload == null) {
            cutoffCalendar = getCalendarForCutoffTime(pCalendar);
        } else {
            String overrideCutoffTime = secondOffload.getOverrideCutoffTime();
            String[] offloadTime = overrideCutoffTime.split(":");
            Integer offloadHours = Integer.parseInt(offloadTime[0]);
            Integer offloadMinutes = Integer.parseInt(offloadTime[1]);
            Integer offloadSeconds = Integer.parseInt(offloadTime[2]);

            cutoffCalendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            cutoffCalendar.setValues(pCalendar.getYear(), pCalendar.getMonth(), pCalendar.getDay(), offloadHours,
                    offloadMinutes, offloadSeconds, 0);
        }

        return cutoffCalendar;
    }

    public OffloadBatch createOffloadBatchAndNachaFileRecords(SpcfCalendar offloadDate) {
        // See if we have created an offload batch already
        DomainEntitySet<OffloadBatch> customOffloadBatches = Application.getSessionCache().getEntityCollection(OffloadBatch.class, "customOffloadBatches");
        if (customOffloadBatches != null) {
            for (OffloadBatch customOffloadBatch : customOffloadBatches) {
                if (customOffloadBatch.getOffloadDate().compareTo(offloadDate) == 0 &&
                        customOffloadBatch.getStatusCd() == OffloadBatchStatus.InProcess &&
                        customOffloadBatch.getOffloadGroup().getOffloadGroupCd().equals(this.getOffloadGroupCd())) {
                    return customOffloadBatch;
                }
            }
        }
        else {
            Application.getSessionCache().addEntityCollection(OffloadBatch.class, "customOffloadBatches", new DomainEntitySet<OffloadBatch>());
        }

        // create new offload batch and nacha files
        OffloadBatch offloadBatch = new OffloadBatch();
        offloadBatch.setStatusCd(OffloadBatchStatus.InProcess);
        offloadBatch.setIsOffloadedTransactionsEventCreationComplete(false);
        offloadBatch.setOffloadDate(offloadDate);
        offloadBatch.setOffloadGroup(this);
        offloadBatch = Application.save(offloadBatch);

        if (!getOffloadGroupCd().equals("TXP")) {
            NACHAFile nachaFile = new NACHAFile();
            nachaFile.setStatus(NACHAFileStatus.InProcess);
            nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            nachaFile.setOffloadBatch(offloadBatch);
            nachaFile.setFinalizationDate(PSPDate.getPSPTime());
            nachaFile.setFileType(NACHAFileType.CCD);
            nachaFile.setFileIDModifier(NACHAFile.getNextFileIDCounter());
            nachaFile = Application.save(nachaFile);
            offloadBatch.addNACHAFile(nachaFile);

            nachaFile = new NACHAFile();
            nachaFile.setStatus(NACHAFileStatus.InProcess);
            nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            nachaFile.setOffloadBatch(offloadBatch);
            nachaFile.setFinalizationDate(PSPDate.getPSPTime());
            nachaFile.setFileType(NACHAFileType.PPD);
            nachaFile.setFileIDModifier(NACHAFile.getNextFileIDCounter());
            nachaFile = Application.save(nachaFile);
            offloadBatch.addNACHAFile(nachaFile);
        }
        else {
            NACHAFile nachaFile = new NACHAFile();
            nachaFile.setStatus(NACHAFileStatus.InProcess);
            nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            nachaFile.setOffloadBatch(offloadBatch);
            nachaFile.setFinalizationDate(PSPDate.getPSPTime());
            nachaFile.setFileType(NACHAFileType.CCDPlus);
            nachaFile.setFileIDModifier(NACHAFile.getNextFileIDCounter());
            nachaFile = Application.save(nachaFile);
            offloadBatch.addNACHAFile(nachaFile);
        }

        Application.getSessionCache().addEntity(OffloadBatch.class, "customOffloadBatches", offloadBatch);

        return offloadBatch;
    }

    public OffloadBatch createSecondOffload(SpcfCalendar pOffloadDateTime) {

        // the time portion of the effective date should be set to midnight (for consistency in future queries)
        SpcfCalendar effectiveDate = SpcfCalendar.createInstance(pOffloadDateTime.getYear(),
                                                                 pOffloadDateTime.getMonth(),
                                                                 pOffloadDateTime.getDay(),
                                                                 0, 0, 0, 0,
                                                                 SpcfTimeZone.getLocalTimeZone());

        // create the new second offload record
        SecondOffload secondOffload = new SecondOffload();
        secondOffload.setEffectiveDate(effectiveDate);
        secondOffload.setOffloadGroup(this);
        secondOffload.setOverrideCutoffTime(pOffloadDateTime.format("HH:mm:ss"));
        Application.save(secondOffload);

        return createOffloadBatchAndNachaFileRecords(effectiveDate);
    }
    
    
    public boolean isCustom() {
        return !getOffloadGroupCd().equals("STD") && !getOffloadGroupCd().equals("TXP");
    }

    public interface Codes {
        public static String STANDARD = "STD";
        public static String TAXPAYMENT = "TXP";
        public static String DirectDepositService = "STD_DDS";
        public static String PSPOFFLOADS = "PSPO";
    }

    public SecondOffload getSecondOffload(SpcfCalendar pEffectiveDate) {
        NaturalKey naturalKey = new NaturalKey(SecondOffload.class, this.getId(), pEffectiveDate);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null && primaryKey.toString().equals(SpcfUniqueId.EmptyGuid)) {
            return null;
        } else if(primaryKey != null) {
            return Application.findById(SecondOffload.class, primaryKey);
        } else {
            SpcfCalendar effDate = SpcfCalendar.createInstance(pEffectiveDate.getYear(),
                                                               pEffectiveDate.getMonth(),
                                                               pEffectiveDate.getDay(),
                                                               0, 0, 0, 0,
                                                               SpcfTimeZone.getLocalTimeZone());
            Expression<SecondOffload> query =
                    new Query<SecondOffload>()
                            .Where(SecondOffload.OffloadGroup().equalTo(this)
                                                .And(SecondOffload.EffectiveDate().equalTo(effDate)))
                            .OrderBy(SecondOffload.CreatedDate().Descending());

            DomainEntitySet<SecondOffload> list = Application.find(SecondOffload.class, query);
            SecondOffload secondOffload = ((list != null) && (list.size() > 0)) ? list.get(0) : null;

            if(secondOffload != null) {
                Application.getSessionCache().addPrimaryKey(naturalKey, secondOffload.getId());
            } else {
                // put a fake unique id in the cache so that we don't check again
                Application.getSessionCache().addPrimaryKey(naturalKey, SpcfUniqueId.createInstance(SpcfUniqueId.EmptyGuid));
            }

            return secondOffload;
        }
    }

}