package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/15/13
 * Time: 3:22 PM
 */
public class AMOListener implements IAMOGatewayListener {
    private Company mCompany;
    private TransmissionType mTransmissionType;

    public AMOListener(Company pCompany, TransmissionType pTransmissionType) {
        mCompany = pCompany;
        mTransmissionType = pTransmissionType;
    }

    public void onRequest(String pTransmissionId, String pRequest) {
        SourceSystemTransmissionDTO transmissionDTO = new SourceSystemTransmissionDTO(mTransmissionType, pRequest);
        transmissionDTO.setTransmissionType(mTransmissionType);
        transmissionDTO.setFromSourceSystem(SourceSystemCode.PSP);
        transmissionDTO.setToSourceSystem(SourceSystemCode.AMO);
        transmissionDTO.setDescription("AMO " + mTransmissionType);
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
