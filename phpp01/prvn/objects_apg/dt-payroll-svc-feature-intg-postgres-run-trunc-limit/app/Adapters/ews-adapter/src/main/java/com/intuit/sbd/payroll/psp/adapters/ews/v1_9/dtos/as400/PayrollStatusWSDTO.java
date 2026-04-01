/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import javax.xml.bind.annotation.*;
import java.util.*;

/**
    @author Jeff Jones
 */

@XmlType(name = "PayrollStatus", propOrder = {"userID", "errorWSDTO", "account", "transmissionsWSDTO",
        "modTransmissionsWSDTO", "failedTransmissionsWSDTO", "DDRejectionsWSDTO"})
public class PayrollStatusWSDTO implements Cloneable {

    private String userID;
    private ErrorWSDTO errorWSDTO;
    private AccountWSDTO account;
    private List<TransmissionWSDTO> transmissionsWSDTO;
    private List<TransmissionWSDTO> modTransmissionsWSDTO;
    private List<FailedTransmissionWSDTO> failedTransmissionsWSDTO;
    private List<DDRejectionWSDTO> ddRejectionsWSDTO;

    public PayrollStatusWSDTO clone() throws CloneNotSupportedException {
        PayrollStatusWSDTO clone = (PayrollStatusWSDTO) super.clone();

        if (errorWSDTO != null) {
            clone.setErrorWSDTO(errorWSDTO.clone());
        }

        if (account != null) {
            clone.setAccount(account.clone());
        }                

        if (transmissionsWSDTO != null) {
            clone.transmissionsWSDTO = new ArrayList<TransmissionWSDTO>();
            for (TransmissionWSDTO transmissionWSDTO: transmissionsWSDTO) {
                clone.transmissionsWSDTO.add(transmissionWSDTO.clone());
            }
        }

        if (modTransmissionsWSDTO != null) {
            clone.modTransmissionsWSDTO = new ArrayList<TransmissionWSDTO>();
            for (TransmissionWSDTO modTransmissionWSDTO: modTransmissionsWSDTO) {
                clone.modTransmissionsWSDTO.add(modTransmissionWSDTO.clone());
            }
        }

        if (failedTransmissionsWSDTO != null) {
            clone.failedTransmissionsWSDTO = new ArrayList<FailedTransmissionWSDTO>();
            for (FailedTransmissionWSDTO failedTransmissionWSDTO: failedTransmissionsWSDTO) {
                clone.failedTransmissionsWSDTO.add(failedTransmissionWSDTO.clone());
            }
        }

        if (ddRejectionsWSDTO != null) {
            clone.ddRejectionsWSDTO = new ArrayList<DDRejectionWSDTO>();
            for (DDRejectionWSDTO ddRejectionWSDTO: ddRejectionsWSDTO) {
                clone.ddRejectionsWSDTO.add(ddRejectionWSDTO.clone());
            }
        }          

        return clone;
    }

    public PayrollStatusWSDTO() {
        this.userID = null;
        this.errorWSDTO = null;
        this.transmissionsWSDTO = null;
        this.modTransmissionsWSDTO = null;
        this.failedTransmissionsWSDTO = null;
        this.ddRejectionsWSDTO = null;
    }

    @XmlAttribute(name = "UserID", required = true)
    public String getUserID() {
        return userID;
    }

    public void setUserID(String pUserID) {
        this.userID = pUserID;
    }

    @XmlElement(name = "Error")
    public ErrorWSDTO getErrorWSDTO() {
        return errorWSDTO;
    }

    public void setErrorWSDTO(ErrorWSDTO pErrorWSDTO) {
        this.errorWSDTO = pErrorWSDTO;
    }

    @XmlElement(name = "Account", required = true)
    public AccountWSDTO getAccount() {
        return account;
    }

    public void setAccount(AccountWSDTO pAccount) {
        this.account = pAccount;
    }

    @XmlElement(name = "Transmission")
    public List<TransmissionWSDTO> getTransmissionsWSDTO() {
        return transmissionsWSDTO;
    }

    public void setTransmissionsWSDTO(List<TransmissionWSDTO> pTransmissionsWSDTO) {
        this.transmissionsWSDTO = pTransmissionsWSDTO;
    }

    @XmlElement(name = "ModTransmission")
    public List<TransmissionWSDTO> getModTransmissionsWSDTO() {
        return modTransmissionsWSDTO;
    }

    public void setModTransmissionsWSDTO(List<TransmissionWSDTO> pModTransmissionsWSDTO) {
        this.modTransmissionsWSDTO = pModTransmissionsWSDTO;
    }

    @XmlElement(name = "FailedTransmission")
    public List<FailedTransmissionWSDTO> getFailedTransmissionsWSDTO() {
        return failedTransmissionsWSDTO;
    }

    public void setFailedTransmissionsWSDTO(List<FailedTransmissionWSDTO> pFailedTransmissionsWSDTO) {
        this.failedTransmissionsWSDTO = pFailedTransmissionsWSDTO;
    }

    @XmlElement(name = "DDRejection")
    public List<DDRejectionWSDTO> getDDRejectionsWSDTO() {
        return ddRejectionsWSDTO;
    }

    public void setDDRejectionsWSDTO(List<DDRejectionWSDTO> pDDRejectionsWSDTO) {
        this.ddRejectionsWSDTO = pDDRejectionsWSDTO;
    }
}
