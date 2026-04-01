package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: May 14, 2008
 * Time: 2:05:04 PM
 */
public class SourceSystemTransmissionDTO {
    private String requestDocument;
    private String responseDocument;
    private Long requestToken;
    private Long responseToken;
    private String description;
    private SourceSystemCode fromSourceSystem;
    private SourceSystemCode toSourceSystem;
    private TransmissionType transmissionType;
    private String IPAddress;
    private QBDTRequestInfoDTO mQBDTRequestInfoDTO;
    private String mTRNUID;
    private String applicationVersion;
    private String applicationId;
    private String taxTableId;

    public SourceSystemTransmissionDTO() {
    }

    public SourceSystemTransmissionDTO(TransmissionType pTransmissionType, String pRequestDocument) {
        this.transmissionType = pTransmissionType;
        this.setRequestDocument(pRequestDocument);
    }

    public String getRequestDocument() {
        return requestDocument;
    }

    public void setRequestDocument(String pRequestDocument) {

        this.requestDocument = pRequestDocument;

    }

    public String getResponseDocument() {
        return responseDocument;
    }

    public void setResponseDocument(String pResponseDocument) {

        this.responseDocument = pResponseDocument;

    }

    public Long getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(Long requestToken) {
        this.requestToken = requestToken;
    }

    public Long getResponseToken() {
        return responseToken;
    }

    public void setResponseToken(Long responseToken) {
        this.responseToken = responseToken;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SourceSystemCode getFromSourceSystem() {
        return fromSourceSystem;
    }

    public void setFromSourceSystem(SourceSystemCode fromSourceSystem) {
        this.fromSourceSystem = fromSourceSystem;
    }

    public SourceSystemCode getToSourceSystem() {
        return toSourceSystem;
    }

    public void setToSourceSystem(SourceSystemCode toSourceSystem) {
        this.toSourceSystem = toSourceSystem;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public QBDTRequestInfoDTO getQBDTRequestInfoDTO() {
        return mQBDTRequestInfoDTO;
    }

    public void setQBDTRequestInfoDTO(QBDTRequestInfoDTO pQBDTRequestInfoDTO) {
        mQBDTRequestInfoDTO = pQBDTRequestInfoDTO;
    }

    public String getTRNUID() {
        return mTRNUID;
    }

    public void setTRNUID(String pTRNUID) {
        mTRNUID = pTRNUID;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String pApplicationVersion) {
        applicationVersion = pApplicationVersion;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String pApplicationId) {
        applicationId = pApplicationId;
    }

    public String getTaxTableId() {
        return taxTableId;
    }

    public void setTaxTableId(String pTaxTableId) {
        taxTableId = pTaxTableId;
    }

    public ProcessResult validateSourceSystemTransmissionDTO() {
        ProcessResult validationResult = new ProcessResult();
        if (requestDocument == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.SourceSystemTransmission, "Request Document", "Request Document");

            return validationResult;
        }

        if (requestToken < 0) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.SourceSystemTransmission, "Request Token", "Request Token");

            return validationResult;
        }
        if (transmissionType == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.SourceSystemTransmission, "Transmission Type", "Transmission Type");

            return validationResult;
        }
        return validationResult;
    }

}
