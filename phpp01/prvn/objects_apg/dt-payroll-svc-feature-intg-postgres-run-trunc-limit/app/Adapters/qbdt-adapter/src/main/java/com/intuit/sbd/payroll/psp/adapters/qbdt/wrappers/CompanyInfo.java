package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.ICOINFOMOD;
import com.intuit.sbd.payroll.psp.common.ofx.request.SONRQ;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 5/13/12
 * Time: 7:07 PM 
 */
public class CompanyInfo {
    private ICOINFOMOD mICOINFOMOD;
    private OFXAPPVERObject mOFXAPPVERObject;
    private String mApplicationId;
    private String mQuickbooksSku;
    
    private boolean mHasCompanyMod = false;
    private boolean mHasAppInfo = false;    

    public CompanyInfo(ICOINFOMOD pICOINFOMOD, SONRQ pSONRQ) {
        mICOINFOMOD = pICOINFOMOD;
        if(mICOINFOMOD != null) {
            mHasCompanyMod = true;
        }

        if(pSONRQ != null) {
            mHasAppInfo = true;
            mOFXAPPVERObject = new OFXAPPVERObject(pSONRQ.getAPPVER());
            mApplicationId = pSONRQ.getAPPID();
            mQuickbooksSku = mOFXAPPVERObject.getFlavorId();
        }
    }

    public boolean hasCompanyMod() {
        return mHasCompanyMod;
    }

    public boolean hasAppInfo() {
        return mHasAppInfo;
    }

    public String getSourceBankAccountName(){
        return mICOINFOMOD.getIACCTNAME();
    }
    
    public AddressDTO getLegalAddressDTO() {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1(mICOINFOMOD.getIADDR1());
        addressDTO.setAddressLine2(mICOINFOMOD.getIADDR2());
        addressDTO.setCity(mICOINFOMOD.getICITY());
        addressDTO.setState(mICOINFOMOD.getISTATE());        
        addressDTO.setZipCode(mICOINFOMOD.getIPOSTALCODE());
        return addressDTO;
    }
    
    public String getLegalName() {
        return mICOINFOMOD.getILEGALNAME();
    }

    public String getTaxTable() {
        return mOFXAPPVERObject.getTaxTableId();
    }
    
    public String getApplicationVersion() {
        return mOFXAPPVERObject.getQBVersionStr();
    }
    
    public String getApplicationId() {
        return mApplicationId;
    }

    public String getQuickbooksSku() {
        return mQuickbooksSku;
    }
    
    public SpcfCalendar getQuarterToStartDate() {
        return mICOINFOMOD.getIDTFILEQTRSTART() == null ? null : CalendarUtils.convertToSpcfCalendar(QBOFX.mapOFXStringToDate(mICOINFOMOD.getIDTFILEQTRSTART()));
    }

    public String getIAMRealmId() {
        return mICOINFOMOD == null ? null : mICOINFOMOD.getIREALMID();
    }
}
