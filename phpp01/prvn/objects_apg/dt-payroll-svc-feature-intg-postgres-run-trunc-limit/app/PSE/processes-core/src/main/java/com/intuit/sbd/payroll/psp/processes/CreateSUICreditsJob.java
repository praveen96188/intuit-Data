package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.SUICreditsJobDTO;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.SUICreditsJob;
import com.intuit.sbd.payroll.psp.domain.SUICreditsJobStatus;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import org.apache.commons.lang.StringUtils;

/**
 * User: dweinberg
 * Date: 09/26/13
 * Time: 10:18 AM
 */
public class CreateSUICreditsJob extends Process {

    private SUICreditsJobDTO jobDTO;

    private PaymentTemplate mPaymentTemplate;

    public CreateSUICreditsJob(SUICreditsJobDTO pJobDTO) {
        jobDTO = pJobDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(jobDTO.validate());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (StringUtils.isNotEmpty(jobDTO.getPaymentTemplateCd())) {
            mPaymentTemplate = PaymentTemplate.findPaymentTemplate(jobDTO.getPaymentTemplateCd());
            if (mPaymentTemplate == null) {
                validationResult.getMessages().PaymentTemplateDoesNotExist(EntityName.PaymentTemplate, jobDTO.getPaymentTemplateCd(), jobDTO.getPaymentTemplateCd());
                return validationResult;
            }
        }

        Criterion<SUICreditsJob> ptClause = SUICreditsJob.PaymentTemplate().isNull();
        if (mPaymentTemplate != null) {
            ptClause = ptClause.Or(SUICreditsJob.PaymentTemplate().equalTo(mPaymentTemplate));
        }

        //validate no existing job
        DomainEntitySet<SUICreditsJob> existingJobs =
                Application.find(SUICreditsJob.class,
                                 SUICreditsJob.Year().equalTo(jobDTO.getYear())
                                              .And(SUICreditsJob.Quarter().equalTo(jobDTO.getQuarter()))
                                              .And(ptClause));

        if (existingJobs.isNotEmpty()) {
            validationResult.getMessages().GenericError(null, null, "There is already a job for this quarter[/template]");
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        SUICreditsJob job = new SUICreditsJob();
        job.setStatus(SUICreditsJobStatus.Created);
        job.setYear(jobDTO.getYear());
        job.setQuarter(jobDTO.getQuarter());
        job.setPaymentTemplate(mPaymentTemplate);
        Application.save(job);

        return processResult;
    }

}
