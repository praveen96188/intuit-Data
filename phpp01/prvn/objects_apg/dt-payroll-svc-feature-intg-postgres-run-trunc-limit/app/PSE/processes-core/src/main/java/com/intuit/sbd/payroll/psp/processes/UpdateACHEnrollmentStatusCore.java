package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import org.apache.commons.lang.StringUtils;

/**
 * User: ihannur
 * Date: 2/12/13
 * Time: 2:50 PM
 */
public class UpdateACHEnrollmentStatusCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private ACHEnrollmentStatus mNewACHEnrollmentStatus;
    private String mPaymentTemplateCd;
    private Company mCompany;
    private ACHEnrollment mACHEnrollment;

    public UpdateACHEnrollmentStatusCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, ACHEnrollmentStatus pNewACHEnrollmentStatus) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mNewACHEnrollmentStatus = pNewACHEnrollmentStatus;
        mPaymentTemplateCd = pPaymentTemplateCd;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                                                               mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (StringUtils.isEmpty(mPaymentTemplateCd)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.PaymentTemplate, mPaymentTemplateCd, "paymentTemplateCd");
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

        mACHEnrollment = mCompany.getCurrentACHEnrollment();
        if(mACHEnrollment == null || mACHEnrollment.getStatus() == null) {
            validationResult.getMessages().ACHEnrollmentDoesNotExist(EntityName.ACHEnrollment, mSourceCompanyId, mCompany.getSourceSystemCompanyId(), mPaymentTemplateCd);
            return  validationResult;
        }

        if(!mACHEnrollment.isTransitionAllowed(mNewACHEnrollmentStatus)) {
            validationResult.getMessages().NewACHEnrollmentStatusNotAllowed(EntityName.ACHEnrollment, mSourceCompanyId, mCompany.getSourceSystemCompanyId(), mPaymentTemplateCd, mACHEnrollment.getStatus().toString(), mNewACHEnrollmentStatus.toString());
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {

        ProcessResult<ACHEnrollment> processResult = new ProcessResult<ACHEnrollment>();

        //Cancel the existing and new record if moving from EnrollmentRejected to PendingEnrollment
        if(mACHEnrollment.getStatus() == ACHEnrollmentStatus.EnrollmentRejected && mNewACHEnrollmentStatus == ACHEnrollmentStatus.PendingEnrollment) {
            mACHEnrollment.updateStatus(ACHEnrollmentStatus.Cancelled);

            CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(mCompany, Agency.FL_AGENT_ID);

            ACHEnrollment achEnrollment = new ACHEnrollment();
            achEnrollment.setCompanyAgency(companyAgency);
            achEnrollment.updateStatus(ACHEnrollmentStatus.PendingEnrollment);
            achEnrollment.setEffectiveDate(mACHEnrollment.getEffectiveDate());
            Application.save(achEnrollment);

            Application.save(achEnrollment);
            processResult.setResult(achEnrollment);

        } else {

            if(mNewACHEnrollmentStatus == ACHEnrollmentStatus.Enrolled) {
                //Updating Agent register flag to true, when manually updating to Enrolled
                processResult.merge(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(mCompany.getSourceSystemCd(),
                                                                             mCompany.getSourceCompanyId(),
                                                                             mPaymentTemplateCd, PaymentMethod.ACHCredit, true));
            }
            mACHEnrollment.updateStatus(mNewACHEnrollmentStatus);
            Application.save(mACHEnrollment);
            processResult.setResult(mACHEnrollment);
        }

        return processResult;
    }
}
