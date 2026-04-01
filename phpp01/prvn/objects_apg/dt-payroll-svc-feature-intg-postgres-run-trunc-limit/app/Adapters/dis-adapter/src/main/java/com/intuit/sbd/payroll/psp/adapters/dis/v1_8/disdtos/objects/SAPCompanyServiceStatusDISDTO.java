package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_8.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPCompanyServiceStatus", propOrder = {"serviceCd","displayStatus","status","canUpdateStatus","hasSignatureFile"
,"custodialId","isSafeHarbor","allowedTransitions","ddLimits","fundingModelCd","serviceStartDate","serviceEndDate"
,"offering","canEditOffering","offer","canEditOffer","directDepositAdditionalInfo","billPaymentAdditionalInfo"})
public class SAPCompanyServiceStatusDISDTO {

    @XmlElement(name = "ServiceCd")
    private String serviceCd;

    public String getServiceCd() {
        return serviceCd;
    }

    public void setServiceCd(String serviceCd) {
        this.serviceCd = serviceCd;
    }

    @XmlElement(name = "DisplayStatus")
    private SAPDisplayStatusDISDTO displayStatus;

    public SAPDisplayStatusDISDTO getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(SAPDisplayStatusDISDTO displayStatus) {
        this.displayStatus = displayStatus;
    }

    @XmlElement(name = "Status")
    private SAPServiceStatusDISDTO status;

    public SAPServiceStatusDISDTO getStatus() {
        return status;
    }

    public void setStatus(SAPServiceStatusDISDTO status) {
        this.status = status;
    }

    @XmlElement(name = "CanUpdateStatus")
    private boolean canUpdateStatus;

    public boolean isCanUpdateStatus() {
        return canUpdateStatus;
    }

    public void setCanUpdateStatus(boolean canUpdateStatus) {
        this.canUpdateStatus = canUpdateStatus;
    }

    @XmlElement(name = "HasSignatureFile")
    private Boolean hasSignatureFile;

    public Boolean getHasSignatureFile() {
        return hasSignatureFile;
    }

    public void setHasSignatureFile(Boolean hasSignatureFile) {
        this.hasSignatureFile = hasSignatureFile;
    }

    @XmlElement(name = "CustodialId")
    private String custodialId;

    public String getCustodialId() {
        return custodialId;
    }

    public void setCustodialId(String custodialId) {
        this.custodialId = custodialId;
    }

    @XmlElement(name = "IsSafeHarbor")
    private boolean isSafeHarbor;

    public boolean isSafeHarbor() {
        return isSafeHarbor;
    }

    public void setSafeHarbor(boolean safeHarbor) {
        isSafeHarbor = safeHarbor;
    }

    @XmlElement(name = "SAPServiceStatus")
    private ArrayList<SAPServiceStatusDISDTO> allowedTransitions;

    public ArrayList<SAPServiceStatusDISDTO> getAllowedTransitions() {
        return allowedTransitions;
    }

    public void setAllowedTransitions(ArrayList<SAPServiceStatusDISDTO> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    @XmlElement(name = "SAPCompanyDdLimits")
    private SAPCompanyDdLimitsDISDTO ddLimits;

    public SAPCompanyDdLimitsDISDTO getDdLimits() {
        return ddLimits;
    }

    public void setDdLimits(SAPCompanyDdLimitsDISDTO ddLimits) {
        this.ddLimits = ddLimits;
    }

    @XmlElement(name = "FundingModelCd")
    private String fundingModelCd;

    public String getFundingModelCd() {
        return fundingModelCd;
    }

    public void setFundingModelCd(String fundingModelCd) {
        this.fundingModelCd = fundingModelCd;
    }

    @XmlElement(name = "ServiceStartDate")
    public Date serviceStartDate;

    public Date getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(Date serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    @XmlElement(name = "ServiceEndDate")
    public Date serviceEndDate;

    public Date getServiceEndDate() {
        return serviceEndDate;
    }

    public void setServiceEndDate(Date serviceEndDate) {
        this.serviceEndDate = serviceEndDate;
    }

    @XmlElement(name = "Offering")
    public String offering;

    public String getOffering() {
        return offering;
    }

    public void setOffering(String offering) {
        this.offering = offering;
    }

    @XmlElement(name = "CanEditOffering")
    public boolean canEditOffering;

    public boolean isCanEditOffering() {
        return canEditOffering;
    }

    public void setCanEditOffering(boolean canEditOffering) {
        this.canEditOffering = canEditOffering;
    }

    @XmlElement(name = "Offer")
    public String offer;

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    @XmlElement(name = "CanEditOffer")
    public boolean canEditOffer;

    public boolean isCanEditOffer() {
        return canEditOffer;
    }

    public void setCanEditOffer(boolean canEditOffer) {
        this.canEditOffer = canEditOffer;
    }

    @XmlElement(name = "DirectDepositAdditionalInfo")
    private SAPDirectDepositServiceInformationDISDTO directDepositAdditionalInfo;

    public SAPDirectDepositServiceInformationDISDTO getDirectDepositAdditionalInfo() {
        return directDepositAdditionalInfo;
    }

    public void setDirectDepositAdditionalInfo(SAPDirectDepositServiceInformationDISDTO directDepositAdditionalInfo) {
        this.directDepositAdditionalInfo = directDepositAdditionalInfo;
    }

    @XmlElement(name = "BillPaymentAdditionalInfo")
    private SAPBillPaymentServiceInformationDISDTO billPaymentAdditionalInfo;

    public SAPBillPaymentServiceInformationDISDTO getBillPaymentAdditionalInfo() {
        return billPaymentAdditionalInfo;
    }

    public void setBillPaymentAdditionalInfo(SAPBillPaymentServiceInformationDISDTO billPaymentAdditionalInfo) {
        this.billPaymentAdditionalInfo = billPaymentAdditionalInfo;
    }

    public void populateServiceEndDate(String pPSID,SourceSystemEnum pSourceSystemEnum) throws Exception {

        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany(pPSID,translateSourceSystemCode(pSourceSystemEnum));
            if (company == null) {
                throw new Exception(DISMessages.companyDoesNotExist(pPSID).toString());
            }
            ServiceCode serviceCode = ServiceCode.valueOf(getServiceCd());
            CompanyService taxService = CompanyService.findCompanyService(company, serviceCode);
            if (taxService.getStatusCd() != null && taxService.getStatusCd().in(ServiceSubStatusCode.Cancelled,ServiceSubStatusCode.Terminated)) {
                SpcfCalendar endServiceSC = taxService.getStatusEffectiveDate();
                Date endServiceDate = CalendarUtils.convertToDate(endServiceSC);
                setServiceEndDate(endServiceDate);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

}
