package com.intuit.ems.payroll.psp.gateways.ers;

import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 25, 2011
 * Time: 1:28:18 PM
 */
public class EntitlementInfoDTO {
    private String mCustomerId;
    private NumberOfEmployeesType mNumberOfEmployeesType;
    private EditionType mEditionType;
    private String mAssetItemNumber;
    private EntitlementStateCode mEntitlementState;
    private Map<String, EntitlementUnitInfoDTO> mEntitlementUnits;

    public NumberOfEmployeesType getNumberOfEmployeesType() {
        return mNumberOfEmployeesType;
    }

    public void setNumberOfEmployeesType(NumberOfEmployeesType pNumberOfEmployeesType) {
        mNumberOfEmployeesType = pNumberOfEmployeesType;
    }

    public EditionType getEditionType() {
        return mEditionType;
    }

    public void setEditionType(EditionType pEditionType) {
        mEditionType = pEditionType;
    }

    public String getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(String pCustomerId) {
        this.mCustomerId = pCustomerId;
    }

    public EntitlementStateCode getEntitlementState() {
        return mEntitlementState;
    }

    public void setEntitlementState(EntitlementStateCode pEntitlementStateCode) {
        this.mEntitlementState = pEntitlementStateCode;
    }

    public String getAssetItemNumber() {
        return mAssetItemNumber;
    }

    public void setAssetItemNumber(String pAssetItemNumber) {
        this.mAssetItemNumber = pAssetItemNumber;
    }

    public Map<String, EntitlementUnitInfoDTO> getEntitlementUnits() {
        if (mEntitlementUnits == null) {
            mEntitlementUnits = new HashMap<String, EntitlementUnitInfoDTO>();
        }

        return mEntitlementUnits;
    }

    public void setEntitlementUnits(Map<String, EntitlementUnitInfoDTO> pEntitlementUnits) {
        this.mEntitlementUnits = pEntitlementUnits;
    }

    public EntitlementDTO copyErsDtoToPspDto(List<String> pSyncOptions) {
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();

        copyErsDtoToPspDto(pSyncOptions, entitlementUnitDTO);

        return entitlementUnitDTO;
    }

    public void copyErsDtoToPspDto(List<String> pSyncOptions, EntitlementUnitDTO pEntitlementUnitDTO) {

        if (pSyncOptions.contains("EntitlementState") && getEntitlementState() != null) {
            pEntitlementUnitDTO.setEntitlementState(getEntitlementState());
        }
        if (pSyncOptions.contains("CustomerId") && getCustomerId() != null) {
            pEntitlementUnitDTO.setCustomerId(getCustomerId());
        }
        if (pSyncOptions.contains("NumberOfEmployeesType") && getNumberOfEmployeesType() != null) {
            pEntitlementUnitDTO.setNumberOfEmployeesType(getNumberOfEmployeesType());
        }
        if (pSyncOptions.contains("EditionType") && getEditionType() != null) {
            pEntitlementUnitDTO.setEditionType(getEditionType());
        }
        if (pSyncOptions.contains("AssetItemNumber") && getAssetItemNumber() != null) {
            pEntitlementUnitDTO.setAssetItemNumber(getAssetItemNumber());
        }

        if (pSyncOptions.contains("EntitlementUnitStatus")) {
            EntitlementUnitInfoDTO entitlementUnitInfoDTO = getEntitlementUnits().get(pEntitlementUnitDTO.getFedTaxId());
            switch (pEntitlementUnitDTO.getEntitlementState()) {
                case Enabled:
                    switch (pEntitlementUnitDTO.getEntitlementUnitStatus()) {
                        case PendingActivation:
                        case PendingDeactivation:
                        case PendingReactivation:
                        case ActivationHold:
                            // Do Nothing
                            break;
                        case Activated:
                            if (entitlementUnitInfoDTO == null) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
                            } else if (EntitlementUnitStatusCode.Deactivated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingReactivation);
                            }
                            break;
                        case ErrorActivating:
                            if (entitlementUnitInfoDTO == null) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
                            } else if (EntitlementUnitStatusCode.Deactivated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingReactivation);
                            } else if (EntitlementUnitStatusCode.Activated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                            }
                            break;
                        case Deactivated:
                            if (EntitlementUnitStatusCode.Activated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
                            }
                            break;
                        case DeactivationHold:
                        case ErrorDeactivating:
                            if (entitlementUnitInfoDTO == null) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                            } else if (EntitlementUnitStatusCode.Deactivated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                            } else if (EntitlementUnitStatusCode.Activated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
                            }
                            break;
                    }
                    break;
                case Disabled:
                    switch (pEntitlementUnitDTO.getEntitlementUnitStatus()) {
                        case PendingActivation:
                        case PendingDeactivation:
                        case PendingReactivation:
                        case ActivationHold:
                            // Do Nothing
                            break;
                        case Activated:
                            if (entitlementUnitInfoDTO == null) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
                            } else if (EntitlementUnitStatusCode.Deactivated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
                            }
                            break;
                        case Deactivated:
                            if (entitlementUnitInfoDTO == null) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                            } else if (EntitlementUnitStatusCode.Activated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
                            }
                            break;
                        case ErrorActivating:
                            if (entitlementUnitInfoDTO == null) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
                            } else if (EntitlementUnitStatusCode.Activated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                            }
                            break;
                        case DeactivationHold:
                        case ErrorDeactivating:
                            if (entitlementUnitInfoDTO == null) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                            } else if (EntitlementUnitStatusCode.Deactivated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                            } else if (EntitlementUnitStatusCode.Activated.equals(entitlementUnitInfoDTO.getEntitlementUnitStatusCode())) {
                                pEntitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
                            }
                            break;
                    }
                    break;
            }
        }
    }
}
