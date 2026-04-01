package com.intuit.sbd.payroll.psp.api.dtos;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/20/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class PstubEmployerInfoDTO {
    private String mName;
    private PstubAddressDTO mAddressDTO;
    private String mNameAddrRefId;
    private String mObjHash;
    private Collection<PstubStateTaxInfoDTO> mStateTaxDTO;

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public PstubAddressDTO getAddressDTO() {
        return mAddressDTO;
    }

    public void setAddressDTO(PstubAddressDTO pAddressDTO) {
        mAddressDTO = pAddressDTO;
    }

    public String getNameAddrRefId() {
        return mNameAddrRefId;
    }

    public void setNameAddrRefId(String pNameAddrRefId) {
        mNameAddrRefId = pNameAddrRefId;
    }

    public String getObjHash() {
        return mObjHash;
    }

    public void setObjHash(String pObjHash) {
        mObjHash = pObjHash;
    }

    public Collection<PstubStateTaxInfoDTO> getStateTaxDTO() {

        if (mStateTaxDTO == null) {
            mStateTaxDTO = new ArrayList<PstubStateTaxInfoDTO>();
        }
        return mStateTaxDTO;
    }

    public void setStateTaxDTO(Collection<PstubStateTaxInfoDTO> pStateTaxDTO) {
        mStateTaxDTO = pStateTaxDTO;
    }
}
