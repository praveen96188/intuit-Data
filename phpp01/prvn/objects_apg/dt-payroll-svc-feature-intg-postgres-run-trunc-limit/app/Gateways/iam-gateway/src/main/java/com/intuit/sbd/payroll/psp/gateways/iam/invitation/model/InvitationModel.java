package com.intuit.sbd.payroll.psp.gateways.iam.invitation.model;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;

public class InvitationModel {
    private String emailTemplateName;
    private boolean isResend;
    private EmployeeModel employeeModel;
    private CompanyModel companyModel;

    public InvitationModel(Employee employee, Company company, boolean isResend, String emailTemplateName) {
        this.employeeModel = new EmployeeModel(employee);
        this.companyModel = new CompanyModel(company);
        this.isResend = isResend;
        this.emailTemplateName = emailTemplateName;
    }

    public String getEmailTemplateName() {
        return this.emailTemplateName;
    }

    public void setEmailTemplateName(String emailTemplateName) {
        this.emailTemplateName = emailTemplateName;
    }

    public boolean isResend() {
        return isResend;
    }

    public void setResend(boolean resend) {
        isResend = resend;
    }

    public EmployeeModel getEmployeeModel() {
        return employeeModel;
    }

    public void setEmployeeModel(EmployeeModel employeeModel) {
        this.employeeModel = employeeModel;
    }

    public CompanyModel getCompanyModel() {
        return companyModel;
    }

    public void setCompanyModel(CompanyModel companyModel) {
        this.companyModel = companyModel;
    }

    public class EmployeeModel {
        private String employeeId;
        private String firstName;
        private String lastName;
        private String personaId;
        private String emailID;

        public EmployeeModel(Employee employee) {
            this.employeeId = employee.getId().toString();
            this.firstName = employee.getFirstName();
            this.lastName = employee.getLastName();
            this.personaId = employee.getPersonaId();
            this.emailID = employee.getEmail();
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPersonaId() {
            return personaId;
        }

        public String getEmailID() {
            return emailID;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setPersonaId(String personaId) {
            this.personaId = personaId;
        }

        public void setEmailID(String emailID) {
            this.emailID = emailID;
        }
    }

    public class CompanyModel {
        private String realmId;
        private String sourceCompanyId;
        private String coName;

        public CompanyModel(Company company) {
            this.realmId = company.getIAMRealmId();
            this.sourceCompanyId = company.getSourceCompanyId();
            this.coName = company.getLegalName();
        }

        public String getCoName() {
            return coName;
        }

        public void setCoName(String coName) {
            this.coName = coName;
        }

        public String getRealmId() {
            return realmId;
        }

        public void setRealmId(String realmId) {
            this.realmId = realmId;
        }

        public String getSourceCompanyId() {
            return sourceCompanyId;
        }

        public void setSourceCompanyId(String sourceCompanyId) {
            this.sourceCompanyId = sourceCompanyId;
        }
    }
}
