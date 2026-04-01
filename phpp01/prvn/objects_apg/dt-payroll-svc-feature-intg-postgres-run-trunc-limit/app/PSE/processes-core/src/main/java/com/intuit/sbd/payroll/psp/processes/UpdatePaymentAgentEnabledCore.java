package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import org.apache.commons.lang.StringUtils;

/**
 * User: dweinberg
 * Date: 7/8/11
 * Time: 3:47 PM
 */
public class UpdatePaymentAgentEnabledCore extends Process {

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private String paymentTemplateCd;
    private PaymentMethod paymentMethod;
    private boolean agentEnabled;
    private Company company;

    private CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate;
    private CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod;

    public UpdatePaymentAgentEnabledCore(SourceSystemCode sourceSystemCd, String sourceCompanyId, String paymentTemplateCd, PaymentMethod paymentMethod, boolean agentEnabled) {
        this.sourceSystemCd = sourceSystemCd;
        this.sourceCompanyId = sourceCompanyId;
        this.paymentTemplateCd = paymentTemplateCd;
        this.paymentMethod = paymentMethod;
        this.agentEnabled = agentEnabled;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (StringUtils.isEmpty(paymentTemplateCd)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.PaymentTemplate, paymentTemplateCd, "paymentTemplateCd");
            return validationResult;
        }

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);

        if (paymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateDoesNotExist(EntityName.PaymentTemplate, sourceCompanyId, paymentTemplateCd);
            return  validationResult;
        }

        companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);

        if (companyAgencyPaymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateNotAssignedToCompany(EntityName.PaymentTemplate, sourceCompanyId, sourceCompanyId, sourceCompanyId, paymentTemplateCd, null);
            return validationResult;
        }

        if (paymentMethod == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Company, sourceCompanyId, "paymentMethod");
            return validationResult;
        }

        companyPaymentTemplatePaymentMethod = companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethod(paymentMethod);

        if (companyPaymentTemplatePaymentMethod == null) {
            validationResult.getMessages().PaymentMethodDoesNotMatch(EntityName.Company, sourceCompanyId, paymentMethod.toString());
        }

        return  validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if(companyPaymentTemplatePaymentMethod.getAgentEnabled() != agentEnabled) {
            companyPaymentTemplatePaymentMethod.setAgentEnabled(agentEnabled);
            Application.save(companyPaymentTemplatePaymentMethod);
            companyAgencyPaymentTemplate.recalculatePaymentMethods();

            // NY Metro ACH Credit payments should follow NY WH
            if(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd().equals(PaymentTemplate.NY_METRO)) {
                processResult.merge(PayrollTaxHelper.processPendingPaymentsThreshold(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO)));
            }
        }

        return processResult;
    }
}
