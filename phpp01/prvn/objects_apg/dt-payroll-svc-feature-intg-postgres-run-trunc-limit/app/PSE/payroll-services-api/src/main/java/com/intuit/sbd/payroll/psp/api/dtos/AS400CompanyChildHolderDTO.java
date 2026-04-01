package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: May 12, 2010
 * Time: 2:30:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AS400CompanyChildHolderDTO {
    private String sourceCompanyId;
    private CompanyBankAccountDTO companyBankAccountDTO;
    private Collection<EmployeeDTO> employees = new ArrayList<EmployeeDTO>();
    private Collection<CompanyPayrollItemDTO> payrollItems = new ArrayList<CompanyPayrollItemDTO>();
    private ArrayList<CompanyLawDTO> lawItems = new ArrayList<CompanyLawDTO>();
    private HashMap<String, List<CompanyLawRateDTO>> ratesPerSourceLawId = new HashMap<String, List<CompanyLawRateDTO>>();
    private ArrayList<FormTemplateDTO> formTemplates =new ArrayList<FormTemplateDTO>();
    private ArrayList<ServiceSubStatusCode> onHoldReasons =new ArrayList<ServiceSubStatusCode>();
    private EntityChangeDTO entityChangeDTO;
    private AS400EFTPSEnrollmentDTO eftpsDTO;
    private AS400RAFEnrollmentDTO rafDTO;

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    public Collection<EmployeeDTO> getEmployees() {
        return employees;
    }

    public void setEmployees(Collection<EmployeeDTO> employees) {
        this.employees = employees;
    }

    public Collection<CompanyPayrollItemDTO> getPayrollItems() {
        return payrollItems;
    }

    public void setPayrollItems(Collection<CompanyPayrollItemDTO> payrollItems) {
        this.payrollItems = payrollItems;
    }

    public ArrayList<CompanyLawDTO> getLawItems() {
        return lawItems;
    }

    public void setLawItems(ArrayList<CompanyLawDTO> lawItems) {
        this.lawItems = lawItems;
    }

    public HashMap<String, List<CompanyLawRateDTO>> getRatesPerSourceLawId() {
        return ratesPerSourceLawId;
    }

    public void setRatesPerSourceLawId(HashMap<String, List<CompanyLawRateDTO>> ratesPerSourceLawId) {
        this.ratesPerSourceLawId = ratesPerSourceLawId;
    }

    public CompanyBankAccountDTO getCompanyBankAccountDTO() {
        return companyBankAccountDTO;
    }

    public void setCompanyBankAccountDTO(CompanyBankAccountDTO companyBankAccountDTO) {
        this.companyBankAccountDTO = companyBankAccountDTO;
    }

    public ArrayList<FormTemplateDTO> getFormTemplates() {
        return formTemplates;
    }

    public void setFormTemplates(ArrayList<FormTemplateDTO> formTemplates) {
        this.formTemplates = formTemplates;
    }

    public ArrayList<ServiceSubStatusCode> getOnHoldReasons() {
        return onHoldReasons;
    }

    public void setOnHoldReasons(ArrayList<ServiceSubStatusCode> onHoldReasons) {
        this.onHoldReasons = onHoldReasons;
    }

    public EntityChangeDTO getEntityChangeDTO() {
        return entityChangeDTO;
    }

    public void setEntityChangeDTO(EntityChangeDTO entityChangeDTO) {
        this.entityChangeDTO = entityChangeDTO;
    }

    public AS400EFTPSEnrollmentDTO getEftpsDTO() {
        return eftpsDTO;
    }

    public void setEftpsDTO(AS400EFTPSEnrollmentDTO eftpsDTO) {
        this.eftpsDTO = eftpsDTO;
    }

    public AS400RAFEnrollmentDTO getRafDTO() {
        return rafDTO;
    }

    public void setRafDTO(AS400RAFEnrollmentDTO rafDTO) {
        this.rafDTO = rafDTO;
    }
}
