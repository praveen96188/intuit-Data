package com.intuit.sbd.payroll.psp.batchjobs.ers;

import com.intuit.ems.payroll.psp.gateways.ers.IERSGatewayListener;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 29, 2011
 * Time: 3:49:56 PM
 */
public class ERSListener implements IERSGatewayListener {
    private Company mCompany;
    private TransmissionType mTransmissionType;

    public ERSListener(Company pCompany, TransmissionType pTransmissionType) {
        mCompany = pCompany;
        mTransmissionType = pTransmissionType;
    }

    public void onRequest(String pTransmissionId, String pRequest) {
        SourceSystemTransmissionDTO transmissionDTO = new SourceSystemTransmissionDTO(mTransmissionType, pRequest);
        transmissionDTO.setTransmissionType(mTransmissionType);
        transmissionDTO.setFromSourceSystem(SourceSystemCode.PSP);
        transmissionDTO.setToSourceSystem(SourceSystemCode.ERS);
        transmissionDTO.setDescription("ERS " + mTransmissionType);
        transmissionDTO.setRequestToken(0L);
        PayrollServices.transmissionManagerSecondary
                .beginTransmission(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), pTransmissionId, transmissionDTO);
    }

    public void onResponse(String pTransmissionId, String pResponse) {
        SourceSystemTransmissionDTO transmissionDTO = new SourceSystemTransmissionDTO();
        transmissionDTO.setResponseDocument(pResponse);
        transmissionDTO.setResponseToken(0L);
        PayrollServices.transmissionManagerSecondary.endTransmission(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), pTransmissionId, transmissionDTO);
    }
}
