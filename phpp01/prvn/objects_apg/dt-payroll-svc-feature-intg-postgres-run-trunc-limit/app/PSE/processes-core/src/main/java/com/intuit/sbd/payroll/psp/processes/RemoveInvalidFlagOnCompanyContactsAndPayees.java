package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Query;
import org.apache.commons.lang.StringUtils;

/**
 * User: ihannur
 * Date: 7/23/13
 * Time: 11:05 PM
 */
public class RemoveInvalidFlagOnCompanyContactsAndPayees extends Process implements IProcess {

    private Company mCompany;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mEmailAddress;

    public RemoveInvalidFlagOnCompanyContactsAndPayees(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pEmailId) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mEmailAddress = pEmailId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists
        mCompany = Application.find(Company.class, new Query<Company>().Where(Company.SourceCompanyId().equalTo(mSourceCompanyId)
                                                                                     .And(Company.SourceSystemCd().equalTo(mSourceSystemCd)))
                                                                       .EagerLoad(Company.ContactSet(), Company.PayeeSet())).getFirst();
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (StringUtils.isBlank(mEmailAddress)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Address, null, "Email Address");
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult result = new ProcessResult();
        for (Contact contact : mCompany.getContactCollection().find(Contact.Email().equalTo(mEmailAddress).And(Contact.HasInvalidEmail().equalTo(true)))) {
            contact.setHasInvalidEmail(false);
        }

        for (Payee payee : mCompany.getPayeeCollection().find(Payee.Email().equalTo(mEmailAddress).And(Payee.HasInvalidEmail().equalTo(true)))) {
            payee.setHasInvalidEmail(false);
        }

        for (Employee employee : Application.find(Employee.class, Employee.Company().equalTo(mCompany).And(Employee.Email().equalTo(mEmailAddress)).And(Employee.HasInvalidEmail().equalTo(true)))) {
            employee.setHasInvalidEmail(false);
        }

        return result;
    }
}
