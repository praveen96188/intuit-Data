package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.AgencyIdDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import org.apache.commons.lang.StringUtils;

/**
 * User: dweinberg
 * Date: 7/2/11
 * Time: 11:03 AM
 */
public class AddOrUpdateAgencyIdCore extends Process {

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private AgencyIdDTO agencyIdDTO;

    private Company company;
    private CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate;

    private CompanyPaymentTemplateAgencyId companyPaymentTemplateAgencyId;

    public AddOrUpdateAgencyIdCore(SourceSystemCode sourceSystemCd, String sourceCompanyId, AgencyIdDTO agencyIdDTO) {
        this.sourceSystemCd = sourceSystemCd;
        this.sourceCompanyId = sourceCompanyId;
        this.agencyIdDTO = agencyIdDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd,
                sourceCompanyId);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate Company Exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (!company.isAllowedCapability(SystemCapabilityCode.ChangeCompanyInfo)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
            return validationResult;
        }

        if (!company.passesAdditionalCancelTermValidation(false, true, true)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.ChangeCompanyInfo.toString());
        }

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(agencyIdDTO.getPaymentTemplateCd());
        if (paymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateDoesNotExist(EntityName.PaymentTemplate, sourceCompanyId, agencyIdDTO.getPaymentTemplateCd());
            return validationResult;
        }

        companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);
        if (companyAgencyPaymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateNotAssignedToCompany(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(), sourceCompanyId, agencyIdDTO.getPaymentTemplateCd(), "");
            return validationResult;
        }

        companyPaymentTemplateAgencyId = companyAgencyPaymentTemplate.getCompanyPaymentTemplateAgencyIdCollection()
                .find(CompanyPaymentTemplateAgencyId.Name().equalTo(agencyIdDTO.getIdName()))
                .getFirst();

        return validationResult;

    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (companyPaymentTemplateAgencyId == null) {
            companyPaymentTemplateAgencyId = new CompanyPaymentTemplateAgencyId();
            companyPaymentTemplateAgencyId.setName(agencyIdDTO.getIdName());
            companyPaymentTemplateAgencyId.setCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);
            companyAgencyPaymentTemplate.getCompanyPaymentTemplateAgencyIdCollection().add(companyPaymentTemplateAgencyId);
        }

        String oldValue = companyPaymentTemplateAgencyId.getAgencyTaxpayerId();
        if (StringUtils.isEmpty(oldValue)) {
            oldValue = "[none]";
        }

        if (agencyIdDTO.getAgencyTaxpayerId() != null && !agencyIdDTO.getAgencyTaxpayerId().equals(oldValue)) {
            companyPaymentTemplateAgencyId.setAgencyTaxpayerId(agencyIdDTO.getAgencyTaxpayerId());
            Application.save(companyPaymentTemplateAgencyId);

            companyAgencyPaymentTemplate.recalculatePaymentMethods();

            // NY Metro ACH Credit payments should follow NY WH
            if(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd().equals(PaymentTemplate.NY_METRO)) {
                processResult.merge(PayrollTaxHelper.processPendingPaymentsThreshold(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO)));
            }

            CompanyEvent.createAIDUpdatedEvent(company, agencyIdDTO.getIdName(), companyAgencyPaymentTemplate.getCompanyAgency().getAgency().getAgencyId(), oldValue, agencyIdDTO.getAgencyTaxpayerId());
        }


        return processResult;
    }
}
