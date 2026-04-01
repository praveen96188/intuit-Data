package com.intuit.sbd.payroll.psp.processes.Workforce;

import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.IProcess;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class EmployeeChunkWorkforceCore extends Process implements IProcess {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeChunkWorkforceCore.class);

    private List<SpcfUniqueId> partitionedEmployeeIdSubList;
    private Company company;
    private IAMTicket iamTicket;
    private boolean isResend;
    private String invitationSource;
    private String emailTemplateName;

    public EmployeeChunkWorkforceCore(List<SpcfUniqueId>  partitionedEmployeeIdSubList, Company company, IAMTicket iamTicket, boolean isResend, String emailTemplateName, String invitationSource) {
        this.partitionedEmployeeIdSubList = partitionedEmployeeIdSubList;
        this.company = company;
        this.iamTicket = iamTicket;
        this.isResend = isResend;
        this.invitationSource = invitationSource;
        this.emailTemplateName = emailTemplateName;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (Objects.isNull(partitionedEmployeeIdSubList)) {
            validationResult.getMessages()
                    .BadProcessArgument("PartitionedEmployeeIdSubList");
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult<>();
        int inviteCount = 0;

        for(SpcfUniqueId employeeId : partitionedEmployeeIdSubList) {

            Employee employee = Application.findById(Employee.class, employeeId);

            try{
                ProcessResult inviteResult = new EmployeeWorkforceInviteCore(employee, company, iamTicket, isResend, emailTemplateName, invitationSource).execute();
                processResult.merge(inviteResult);
                if(!inviteResult.isSuccess()) {
                    logger.error("Invitation=Failed EmployeeId={} EmailId={} CompanyId={} Template={} InvitationSource={} Error={}",
                            employeeId, Objects.nonNull(employee) ? EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, employee.getEmail(), employeeId.toString()) : null, company.getSourceCompanyId(), emailTemplateName, invitationSource, inviteResult.getMessages());
                    continue;
                }
                inviteCount++;
            } catch (Exception e) {
                logger.error("Invitation=Failed EmployeeId={} EmailId={} CompanyId={} Template={} InvitationSource={} Error={}",
                        employeeId, Objects.nonNull(employee) ? EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, employee.getEmail(), employeeId.toString()) : null, company.getSourceCompanyId(), emailTemplateName, invitationSource, e.getMessage(), e);
                processResult.getMessages().ExceptionOccurred("Exception=" + e);
            }
        }
        processResult.setResult(inviteCount);
        return processResult;
    }
}
