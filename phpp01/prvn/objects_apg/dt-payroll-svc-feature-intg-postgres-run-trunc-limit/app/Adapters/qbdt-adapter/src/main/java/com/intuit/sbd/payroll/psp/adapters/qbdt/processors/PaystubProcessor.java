package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/13/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaystubProcessor {
    private Company mCompany;
    private AssistedConnectionInformation mConnectionInformation;


    public PaystubProcessor(Company pCompany, AssistedConnectionInformation pConnectionInformation) {
        mCompany = pCompany;
        mConnectionInformation = pConnectionInformation;
    }

    public ProcessResult<List<Paystub>> processPaystubs(List<IPAYROLLRUN> pPayrolls, List<PayrollRun> payrollRunList) {
        ProcessResult<List<Paystub>> processResult = new ProcessResult<List<Paystub>>();
        processResult.setResult(new ArrayList<Paystub>());
        // to-do
        //mConnectionInformation...

        // collect all paycheck tags
        ArrayList<IPAYCHK> sourcePaychecks = new ArrayList<IPAYCHK>();

        for (IPAYROLLRUN iPayrollRun : pPayrolls) {
            sourcePaychecks.addAll(iPayrollRun.getIPAYCHK());
            sourcePaychecks.addAll(iPayrollRun.getIPAYCHKMOD());
        }

        for (IPAYCHK sourcePaycheck : sourcePaychecks) {

            IPAYSTUBINFO sourcePaystub = sourcePaycheck.getIPAYSTUBINFO();
            ProcessResult<Paystub> paystubProcessResult = processPaystub(sourcePaystub);

            if (!paystubProcessResult.isSuccess()) {
                processResult.merge(paystubProcessResult);
                return processResult;
            } else if (paystubProcessResult.getResult() != null) {
                processResult.getResult().add(paystubProcessResult.getResult());
            }
        }

        return processResult;
    }

    public ProcessResult<Paystub> processPaystub(IPAYSTUBINFO pPaystub) {
        ProcessResult<Paystub> processResult = new ProcessResult<Paystub>();

        if (pPaystub != null) {
            Paycheck paycheck = Paycheck.findPaycheckByQbdtListId(mCompany, pPaystub.getIQBUNIQUEID());

            if (paycheck == null) {
                processResult.setSuccess(false);
                processResult.getMessages().NoEntityWithGivenId("Paycheck", pPaystub.getIQBUNIQUEID());
                return processResult;
            }

            Employee employee = paycheck.getSourceEmployee();
            if (employee == null) {
                employee = paycheck.getDDEmployee();
            }

            if (employee == null){
                processResult.setSuccess(false);
                processResult.getMessages().PaycheckEmployeeNull(EntityName.Employee, pPaystub.getIQBUNIQUEID(), "paycheck");
                return processResult;
            }

            if(employee.getQbdtEmployeeInfo() == null){
                processResult.setSuccess(false);
                processResult.getMessages().NoEntityWithGivenId("Employee", pPaystub.getIEMPLOYEEINFO().getIQBUNIQUEID());
                return processResult;
            }

            if(!pPaystub.getIEMPLOYEEINFO().getIQBUNIQUEID().equals(employee.getQbdtEmployeeInfo().getListId())){
                processResult.setSuccess(false);
                processResult.getMessages().EntitiesDontMatch(EntityName.Employee, pPaystub.getIEMPLOYEEINFO().getIQBUNIQUEID(),
                                                              employee.getSourceEmployeeId());
                return processResult;

            }

            PaystubDTO paystubDTO = buildPaystubDTOFromOFX(pPaystub);
            Paystub paystub = Paystub.findPaystub(paycheck);
            if (paystub == null) {
                return PayrollServices.paystubManager.addPaystub(paycheck, employee, paystubDTO);
            } else if (paystub.getSourceModTime() != paystubDTO.getModTS().intValue()) {
                return PayrollServices.paystubManager.updatePaystub(paycheck, employee, paystub, paystubDTO);
            }
        }

        return processResult;
    }

    private PstubAddressDTO buildPstubAddressDTO(IPSTUBADDRESS pAddress) {
        PstubAddressDTO pstubAddressDTO = new PstubAddressDTO();

        if (pAddress != null) {
            pstubAddressDTO.setLine1(pAddress.getILINE1());
            pstubAddressDTO.setLine2(pAddress.getILINE2());
            pstubAddressDTO.setLine3(pAddress.getILINE3());
            pstubAddressDTO.setLine4(pAddress.getILINE4());
            pstubAddressDTO.setLine5(pAddress.getILINE5());
        }
        return pstubAddressDTO;
    }

    private Collection<PstubStateTaxInfoDTO> buildPstubStateTaxInfoDTO(List<String> pAgencyIds) {
        ArrayList<PstubStateTaxInfoDTO> pstubStateTaxInfoDTOs = new ArrayList<PstubStateTaxInfoDTO>();
        if (pAgencyIds != null) {
            for(String agencyId : pAgencyIds) {
                PstubStateTaxInfoDTO pstubStateTaxInfoDTO = new PstubStateTaxInfoDTO();
                pstubStateTaxInfoDTO.setAgencyId(agencyId);
                pstubStateTaxInfoDTO.setAgencyName("");
                pstubStateTaxInfoDTOs.add(pstubStateTaxInfoDTO);
            }

        }
        return pstubStateTaxInfoDTOs;
    }

    private PstubEmployeeInfoDTO buildPstubEmployeeInfoDTOFromOFX(IEMPLOYEEINFO pEmployeeInfo) {
        PstubEmployeeInfoDTO pstubEmployeeInfoDTO = new PstubEmployeeInfoDTO();

        pstubEmployeeInfoDTO.setFirstName(pEmployeeInfo.getIFIRSTNAME());
        pstubEmployeeInfoDTO.setMiddleName(pEmployeeInfo.getIMIDDLENAME());
        pstubEmployeeInfoDTO.setLastName(pEmployeeInfo.getILASTNAME());
        pstubEmployeeInfoDTO.setAddressDTO(buildPstubAddressDTO(pEmployeeInfo.getIADDRESS()));
        pstubEmployeeInfoDTO.setSSN(pEmployeeInfo.getISSN());
        pstubEmployeeInfoDTO.setFedTaxFilingStatus(pEmployeeInfo.getIFEDTAXFILINGSTATUS());
        pstubEmployeeInfoDTO.setFedTaxFilingStatusCode(pEmployeeInfo.getIFEDTAXFILINGSTATUSCODE());
        pstubEmployeeInfoDTO.setStateTaxFilingStatus(pEmployeeInfo.getISTATETAXFILINGSTATUS());
        pstubEmployeeInfoDTO.setStateTaxFilingStatusCode(pEmployeeInfo.getISTATETAXFILINGSTATUSCODE());
        pstubEmployeeInfoDTO.setFedAllowances(pEmployeeInfo.getIFEDERALALLOWANCES());
        pstubEmployeeInfoDTO.setStateAllowances(pEmployeeInfo.getISTATEALLOWANCES());
        pstubEmployeeInfoDTO.setFedExtra(pEmployeeInfo.getIFEDERALEXTRA());
        pstubEmployeeInfoDTO.setFedClaimDependents(pEmployeeInfo.getIFEDERALCLAIMDEPENDENTS());
        pstubEmployeeInfoDTO.setFedOtherIncome(pEmployeeInfo.getIFEDERALOTHERINCOME());
        pstubEmployeeInfoDTO.setFedDeduction(pEmployeeInfo.getIFEDERALDEDUCTIONS());
        pstubEmployeeInfoDTO.setFedMultipleJobs(pEmployeeInfo.getIFEDERALMULTIPLEJOBS());
        pstubEmployeeInfoDTO.setFedW4EmpPref(pEmployeeInfo.getIFEDERALW4EMPPREF());
        pstubEmployeeInfoDTO.setStateExtra(pEmployeeInfo.getISTATEEXTRA());
        pstubEmployeeInfoDTO.setTaxFilingState(pEmployeeInfo.getITAXFILINGSTATE());
        pstubEmployeeInfoDTO.setCreateTS(pEmployeeInfo.getICREATETIMESTAMP());
        pstubEmployeeInfoDTO.setModTS(pEmployeeInfo.getIMODTIMESTAMP());

        return pstubEmployeeInfoDTO;
    }

    private PstubEmployerInfoDTO buildPstubEmployerInfoDTOFromOFX(IEMPLOYERINFO pEmployerInfo) {
        PstubEmployerInfoDTO pstubEmployerInfoDTO = new PstubEmployerInfoDTO();

        pstubEmployerInfoDTO.setName(pEmployerInfo.getINAME() == null ? "" : pEmployerInfo.getINAME());
        pstubEmployerInfoDTO.setAddressDTO(buildPstubAddressDTO(pEmployerInfo.getIADDRESS()));
        pstubEmployerInfoDTO.setNameAddrRefId(pEmployerInfo.getINAMEADDRFEDID());
        pstubEmployerInfoDTO.setObjHash(pEmployerInfo.getIEMPLOYERHASH());
        pstubEmployerInfoDTO.setStateTaxDTO(buildPstubStateTaxInfoDTO(pEmployerInfo.getIAGENCYID()));

        return pstubEmployerInfoDTO;
    }

    private ArrayList<PstubDDItemDTO> buildPstubDDItemDTOFromOFX(IPAYSTUBINFO.IDIRECTDEPOSITLINEITEMS pDDLineItems) {
        ArrayList<PstubDDItemDTO> pstubDDItemDTOs = new ArrayList<PstubDDItemDTO>();

        if (pDDLineItems != null) {
            for (IDIRECTDEPOSITLINEITEM ddLineItem : pDDLineItems.getIDIRECTDEPOSITLINEITEM()) {
                PstubDDItemDTO pstubDDItemDTO = new PstubDDItemDTO();

                pstubDDItemDTO.setAcctName(ddLineItem.getIACCTNAME());
                pstubDDItemDTO.setAcctNumber(ddLineItem.getIACCTNUMBER());
                pstubDDItemDTO.setAcctType(ddLineItem.getIACCTTYPE());
                pstubDDItemDTO.setBankName(ddLineItem.getIBANKNAME());
                pstubDDItemDTO.setCurAmt(ddLineItem.getICURAMT());
                pstubDDItemDTO.setName(ddLineItem.getINAME());
                pstubDDItemDTO.setPItemListId(ddLineItem.getIQBUNIQUEID());
                pstubDDItemDTO.setRoutingNumber(ddLineItem.getIROUTINGNUMBER());

                pstubDDItemDTOs.add(pstubDDItemDTO);
            }
        }

        return pstubDDItemDTOs;
    }

    private ArrayList<PstubPaidTimeoffItemDTO> buildPstubPaidTimeoffItemDTOFromOFX(IPAYSTUBINFO.IPAIDTIMEOFFLINEITEMS pTimeoffLineItems) {
        ArrayList<PstubPaidTimeoffItemDTO> pstubPaidTimeoffItemDTOs = new ArrayList<PstubPaidTimeoffItemDTO>();

        if (pTimeoffLineItems != null) {
            for (IPAIDTIMEOFFLINEITEM timeoffLineItem : pTimeoffLineItems.getIPAIDTIMEOFFLINEITEM()) {
                PstubPaidTimeoffItemDTO pstubPaidTimeoffItemDTO = new PstubPaidTimeoffItemDTO();

                pstubPaidTimeoffItemDTO.setAcctName(timeoffLineItem.getIACCTNAME());
                pstubPaidTimeoffItemDTO.setAvailable(timeoffLineItem.getIAVAILABLE());
                pstubPaidTimeoffItemDTO.setName(timeoffLineItem.getINAME());
                pstubPaidTimeoffItemDTO.setPItemListId(timeoffLineItem.getIQBUNIQUEID());
                pstubPaidTimeoffItemDTO.setYTDUsed(timeoffLineItem.getIYTDUSED());

                pstubPaidTimeoffItemDTOs.add(pstubPaidTimeoffItemDTO);
            }
        }

        return pstubPaidTimeoffItemDTOs;
    }

    private ArrayList<PstubMsgDTO> buildPstubMsgDTOFromOFX(IPAYSTUBINFO pPaystub) {
        ArrayList<PstubMsgDTO> pstubMsgDTOs = new ArrayList<PstubMsgDTO>();

        if (pPaystub.getICOMPANYMESSAGES() != null) {
            for (String msg : pPaystub.getICOMPANYMESSAGES().getICOMPANYMESSAGE()) {
                PstubMsgDTO pstubMsgDTO = new PstubMsgDTO();

                pstubMsgDTO.setText(msg);
                pstubMsgDTO.setType(PstubMsgType.Company);

                pstubMsgDTOs.add(pstubMsgDTO);
            }
        }

        if (pPaystub.getIUSERMESSAGES() != null) {
            for (String msg : pPaystub.getIUSERMESSAGES().getIUSERMESSAGE()) {
                PstubMsgDTO pstubMsgDTO = new PstubMsgDTO();

                pstubMsgDTO.setText(msg);
                pstubMsgDTO.setType(PstubMsgType.User);

                pstubMsgDTOs.add(pstubMsgDTO);
            }
        }

        return pstubMsgDTOs;
    }

    private PstubPayItemDTO buildPstubPayItemDTO(IPSTUBPAYLINEITEM pIPSTUBPAYLINEITEM, PstubItemType pType) {
        PstubPayItemDTO pstubPayItemDTO = new PstubPayItemDTO(pType);

        pstubPayItemDTO.setAcctName(pIPSTUBPAYLINEITEM.getIACCTNAME());
        pstubPayItemDTO.setCurAmt(pIPSTUBPAYLINEITEM.getICURAMT());
        pstubPayItemDTO.setEmployeePaid(pIPSTUBPAYLINEITEM.getIEMPPAID());
        pstubPayItemDTO.setIncomeSubjectToTax(pIPSTUBPAYLINEITEM.getIINCOMESUBJECTTOTAX());
        pstubPayItemDTO.setName(pIPSTUBPAYLINEITEM.getINAME());
        pstubPayItemDTO.setPItemListId(pIPSTUBPAYLINEITEM.getIQBUNIQUEID());
        pstubPayItemDTO.setQtyAmt(pIPSTUBPAYLINEITEM.getIQTYAMOUNT());
        pstubPayItemDTO.setQtyTime(pIPSTUBPAYLINEITEM.getIQTYTIME());
        pstubPayItemDTO.setRate(pIPSTUBPAYLINEITEM.getIRATE());
        pstubPayItemDTO.setWageBase(pIPSTUBPAYLINEITEM.getIWAGEBASE());
        pstubPayItemDTO.setYTD(pIPSTUBPAYLINEITEM.getIYTDAMT());

        return pstubPayItemDTO;
    }

    private ArrayList<PstubPayItemDTO> buildPstubPayItemDTOFromOFX(IPAYSTUBINFO pPaystub) {
        ArrayList<PstubPayItemDTO> pstubPayItemDTOs = new ArrayList<PstubPayItemDTO>();

        if (pPaystub.getIEARNINGSLINEITEMS() != null) {
            for (IPSTUBPAYLINEITEM payLineItem : pPaystub.getIEARNINGSLINEITEMS().getIEARNINGSLINEITEM()) {
                PstubPayItemDTO pstubPayItemDTO = buildPstubPayItemDTO(payLineItem, PstubItemType.Earnings);
                pstubPayItemDTOs.add(pstubPayItemDTO);
            }
        }

        if (pPaystub.getIPRETAXDEDUCTIONLINEITEMS() != null) {
            for (IPSTUBPAYLINEITEM payLineItem : pPaystub.getIPRETAXDEDUCTIONLINEITEMS().getIPRETAXDEDUCTIONLINEITEM()) {
                PstubPayItemDTO pstubPayItemDTO = buildPstubPayItemDTO(payLineItem, PstubItemType.PreTaxDeduct);
                pstubPayItemDTOs.add(pstubPayItemDTO);
            }
        }

        if (pPaystub.getIADJNETPAYLINEITEMS() != null) {
            for (IPSTUBPAYLINEITEM payLineItem : pPaystub.getIADJNETPAYLINEITEMS().getIADJNETPAYLINEITEM()) {
                PstubPayItemDTO pstubPayItemDTO = buildPstubPayItemDTO(payLineItem, PstubItemType.AdjNetPay);
                pstubPayItemDTOs.add(pstubPayItemDTO);
            }
        }

        if (pPaystub.getITAXLINEITEMS() != null) {
            for (IPSTUBPAYLINEITEM payLineItem : pPaystub.getITAXLINEITEMS().getITAXLINEITEM()) {
                PstubPayItemDTO pstubPayItemDTO = buildPstubPayItemDTO(payLineItem, PstubItemType.Tax);
                pstubPayItemDTOs.add(pstubPayItemDTO);
            }
        }

        if (pPaystub.getINONTAXCOMPANYCONTRIBUTIONLINEITEMS() != null) {
            for (IPSTUBPAYLINEITEM payLineItem : pPaystub.getINONTAXCOMPANYCONTRIBUTIONLINEITEMS().getINONTAXCOMPANYCONTRIBUTIONLINEITEM()) {
                PstubPayItemDTO pstubPayItemDTO = buildPstubPayItemDTO(payLineItem, PstubItemType.NonTaxCompContri);
                pstubPayItemDTOs.add(pstubPayItemDTO);
            }
        }

        if (pPaystub.getITAXCOMPANYCONTRIBUTIONLINEITEMS() != null) {
            for (IPSTUBPAYLINEITEM payLineItem : pPaystub.getITAXCOMPANYCONTRIBUTIONLINEITEMS().getITAXCOMPANYCONTRIBUTIONLINEITEM()) {
                PstubPayItemDTO pstubPayItemDTO = buildPstubPayItemDTO(payLineItem, PstubItemType.TaxCompContri);
                pstubPayItemDTOs.add(pstubPayItemDTO);
            }
        }

        return pstubPayItemDTOs;
    }

    private PaystubDTO buildPaystubDTOFromOFX(IPAYSTUBINFO pPaystub) {
        PaystubDTO paystubDTO = new PaystubDTO();

        paystubDTO.setAdjNetPay(pPaystub.getIADJNETPAY());
        paystubDTO.setChkNum(pPaystub.getICHKNUM());
        paystubDTO.setGrossPay(pPaystub.getIGROSSPAY());
        paystubDTO.setModTS(pPaystub.getIMODTIMESTAMP());
        paystubDTO.setNetPay(pPaystub.getINETPAY());
        paystubDTO.setPayBeginDate(pPaystub.getIPAYPERIOD().getIBEGINDATE());
        paystubDTO.setPayDate(pPaystub.getIPAYPERIOD().getICHECKDATE());
        paystubDTO.setPayEndDate(pPaystub.getIPAYPERIOD().getIENDDATE());
        paystubDTO.setPreTaxDeducts(pPaystub.getIPRETAXDEDUCTIONS());
        paystubDTO.setTax(pPaystub.getITAXES());
        paystubDTO.setYTDAdjNetPay(pPaystub.getIYTDADJNETPAY());
        paystubDTO.setYTDGrossPay(pPaystub.getIYTDGROSSPAY());
        paystubDTO.setYTDNetPay(pPaystub.getIYTDNETPAY());
        paystubDTO.setYTDPreTaxDeducts(pPaystub.getIYTDPRETAXDEDUCTIONS());
        paystubDTO.setYTDTax(pPaystub.getIYTDTAXES());

        PstubEmployeeInfoDTO pstubEmployeeInfoDTO = buildPstubEmployeeInfoDTOFromOFX(pPaystub.getIEMPLOYEEINFO());
        paystubDTO.setEmployeeInfoDTO(pstubEmployeeInfoDTO);

        PstubEmployerInfoDTO pstubEmployerInfoDTO = buildPstubEmployerInfoDTOFromOFX(pPaystub.getIEMPLOYERINFO());
        paystubDTO.setEmployerInfoDTO(pstubEmployerInfoDTO);

        ArrayList<PstubDDItemDTO> pstubDDItemDTOs = buildPstubDDItemDTOFromOFX(pPaystub.getIDIRECTDEPOSITLINEITEMS());
        paystubDTO.setDDItemDTOs(pstubDDItemDTOs);

        ArrayList<PstubPaidTimeoffItemDTO> pstubPaidTimeoffItemDTOs = buildPstubPaidTimeoffItemDTOFromOFX(pPaystub.getIPAIDTIMEOFFLINEITEMS());
        paystubDTO.setPaidTimeoffItemDTOs(pstubPaidTimeoffItemDTOs);

        ArrayList<PstubMsgDTO> pstubMsgDTOs = buildPstubMsgDTOFromOFX(pPaystub);
        paystubDTO.setMsgDTOs(pstubMsgDTOs);

        ArrayList<PstubPayItemDTO> pstubPayItemDTOs = buildPstubPayItemDTOFromOFX(pPaystub);
        paystubDTO.setPayItemDTOs(pstubPayItemDTOs);

        return paystubDTO;
    }
}
