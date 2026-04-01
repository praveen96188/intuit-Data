
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.ACHEnrollment;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: ihannur
 * Date: 2/4/13
 * Time: 2:45 PM
 */
public class AddACHEnrollmentCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private boolean mReAddIfAdded;
    private Company mCompany;
    private String mPaymentTemplateCd;

    public AddACHEnrollmentCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, boolean pReAddIfAdded) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mPaymentTemplateCd = pPaymentTemplateCd;
        mReAddIfAdded = pReAddIfAdded;
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

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<ACHEnrollment> processResult = new ProcessResult();

        processResult.setResult(ACHEnrollment.createACHEnrollment(mCompany, mReAddIfAdded));

        return processResult;
    }
}
