package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 *
 * User: rsakhamuri
 * Date: Sep 16, 2008
 * Time: 4:39:34 PM

 */
public class AddCompanyEventNoteCore extends Process implements IProcess {

    /**
     * Core process for adding a new note to the specified company event.
     *
     */

    // input parameters
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mCompanyEventId;
    private String mInsertUserId;
    private String mNotes;
    private boolean mAlert;

    // looked up during validate(), then used by process()
    private Company company;
    private CompanyEvent companyEvent;


    public AddCompanyEventNoteCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pCompanyEventId, String pInsertUserId, String pNotes, boolean pAlert)
    {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mInsertUserId = pInsertUserId;
        mAlert = pAlert;

        //Since database can accommodate only 4000 characters, getting first 4000 chars from the string.
        if (pNotes != null && pNotes.length() > 4000) {
            mNotes = pNotes.substring(0, 4000);
        } else {
            mNotes = pNotes;
        }
        mCompanyEventId = pCompanyEventId;
    }


    public ProcessResult validate()
    {
        // validate company parameters
        ProcessResult result = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId);
        if (! result.isSuccess()) {
            return result;
        }

        // make sure Company exists
        company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (company == null) {
            result.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                                                     mSourceSystemCd.toString(), mSourceCompanyId);
            return result;
        }

        if (mCompanyEventId != null) {
            companyEvent = Application.findById(CompanyEvent.class, SpcfUniqueId.createInstance(mCompanyEventId));
            if (companyEvent == null) {
                result.getMessages().NoEntityWithGivenId("CompanyEvent", mCompanyEventId);
                return result;
            }

            if (companyEvent.getCompany() != company) {
                result.getMessages().GenericError(EntityName.Company, company.getSourceSystemCompanyId(), "Company event does not match company");
                return result;
            }
        }

        return result;
    }


    public ProcessResult process()
    {
        ProcessResult<CompanyNote> result = new ProcessResult<CompanyNote>();

        if (companyEvent == null) {
            companyEvent = CompanyEvent.createCompanyEvent(company, EventTypeCode.ManualNoteEvent);
        }

        companyEvent.setNoteLastUpdatedDate(PSPDate.getPSPTime());

        CompanyNote note = new CompanyNote();
        note.setCompany(company);
        note.setInsertUserId(mInsertUserId);
        note.setNotes(mNotes);
        note.setCompanyEvent(companyEvent);
        note.setAlert(mAlert);

        Application.save(companyEvent);
        Application.save(note);

        result.setResult(note);

        return result;
    }

}
