package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: ihannur
 * Date: 2/4/13
 * Time: 2:45 PM
 */
public class DeleteACHEnrollmentCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private Company mCompany;
    private String mPaymentTemplateCd;

    public DeleteACHEnrollmentCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mPaymentTemplateCd = pPaymentTemplateCd;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                                                               mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(mPaymentTemplateCd);

        if (paymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateDoesNotExist(EntityName.PaymentTemplate, mSourceCompanyId, mPaymentTemplateCd);
            return  validationResult;
        }

        if(!PaymentTemplate.FL_SUI.equals(paymentTemplate.getPaymentTemplateCd())) {
            validationResult.getMessages().ACHEnrollmentNotSupported(EntityName.PaymentTemplate, mSourceCompanyId, mPaymentTemplateCd);
            return  validationResult;
        }

        ACHEnrollment achEnrollment = mCompany.getAllACHEnrollments().find(ACHEnrollment.Status().in(ACHEnrollmentStatus.Enrolled, ACHEnrollmentStatus.Deleted))
                                                                         .sort(ACHEnrollment.StatusEffectiveDate().Descending()).getFirst();
        if(achEnrollment == null || achEnrollment.getStatus() != ACHEnrollmentStatus.Enrolled) {
            validationResult.getMessages().ACHEnrollmentStatusNotInEnrolledToDelete(EntityName.ACHEnrollment, mSourceCompanyId, mCompany.getSourceSystemCompanyId(), mPaymentTemplateCd);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        processResult.setResult(ACHEnrollment.deleteACHEnrollment(mCompany));

        return processResult;
    }
}
