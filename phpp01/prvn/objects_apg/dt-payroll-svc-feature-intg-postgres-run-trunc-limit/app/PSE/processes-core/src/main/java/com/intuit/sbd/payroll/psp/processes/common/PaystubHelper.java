package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/4/13
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaystubHelper {
    private static final SpcfLogger logger = PayrollServices.getLogger(PaystubHelper.class);
    public static PstubEmployerInfo findOrCreatePstubEmployerInfo(PstubEmployerInfoDTO pEmployerInfoDTO) {
       String objHash =generateObjectHashForEmployer(pEmployerInfoDTO);

        PstubEmployerInfo employerInfo = PstubEmployerInfo.findPstubEmployerInfo(pEmployerInfoDTO.getName(), objHash);
        if (employerInfo == null) {
            employerInfo = PstubEmployerInfo.createPstubEmployerInfo(pEmployerInfoDTO.getName(), objHash);
            employerInfo.setNameAddrFedId(pEmployerInfoDTO.getNameAddrRefId());
            employerInfo.setPstubAddress(PstubAddress.createPstubAddress(pEmployerInfoDTO.getAddressDTO().getLine1(),
                    pEmployerInfoDTO.getAddressDTO().getLine2(),
                    pEmployerInfoDTO.getAddressDTO().getLine3(),
                    pEmployerInfoDTO.getAddressDTO().getLine4(),
                    pEmployerInfoDTO.getAddressDTO().getLine5()));
            for (PstubStateTaxInfoDTO pstubStateTax : pEmployerInfoDTO.getStateTaxDTO()) {
                employerInfo.addPstubStateTaxInfo(createPstubStateTax(pstubStateTax, employerInfo));
            }
            Application.save(employerInfo);
        }
        return employerInfo;
    }

   /* public static void main(String []args){
        PstubEmployerInfoDTO dto=new PstubEmployerInfoDTO();
        dto.setName("TestUSTool");
        dto.setNameAddrRefId("TestUSTool, Test Address, Address2, AK 23345");
                         PstubAddressDTO addr=new PstubAddressDTO();
        addr.setLine1("Test Address");
        addr.setLine2("Address2");
        addr.setLine3("AK 23345");
        Set<PstubStateTaxInfoDTO> set=new HashSet<PstubStateTaxInfoDTO>();
        PstubStateTaxInfoDTO tax=new PstubStateTaxInfoDTO();
        tax.setAgencyId("23456");
        set.add(tax);
       // dto.setStateTaxDTO(set);
        dto.setAddressDTO(addr);
        System.out.println(generateObjectHashForEmployer(dto));

    }*/
   private static String generateObjectHashForEmployer(PstubEmployerInfoDTO pEmployerInfoDTO) {
       String input = createXMLString(pEmployerInfoDTO);
       try {
           return sha1(input);
       } catch (NoSuchAlgorithmException e) {
           logger.info("Exception while encryption");
       }
       return null;
   }

    private static String createXMLString(PstubEmployerInfoDTO pEmployerInfoDTO) {
        //"<I.NAME>VBD_VMP1\n<I.ADDRESS>\n<I.LINE1>23 Street\n<I.LINE2>CA 94040\n</I.ADDRESS>\n<I.NAMEADDRFEDID>VBD_VMP1, 23 Street, CA 94040\n"
        String xmlString = "";
        if (pEmployerInfoDTO.getName() != null)
            xmlString = xmlString + "<I.NAME>" + pEmployerInfoDTO.getName() + "\n";
        if (pEmployerInfoDTO.getAddressDTO() != null) {
            xmlString = xmlString + "<I.ADDRESS>\n";
            if (pEmployerInfoDTO.getAddressDTO().getLine1() != null)
                xmlString = xmlString + "<I.LINE1>" + pEmployerInfoDTO.getAddressDTO().getLine1() + "\n";
            if (pEmployerInfoDTO.getAddressDTO().getLine2() != null)
                xmlString = xmlString + "<I.LINE2>" + pEmployerInfoDTO.getAddressDTO().getLine2() + "\n";
            if (pEmployerInfoDTO.getAddressDTO().getLine3() != null)
                xmlString = xmlString + "<I.LINE3>" + pEmployerInfoDTO.getAddressDTO().getLine3() + "\n";
            if (pEmployerInfoDTO.getAddressDTO().getLine4() != null)
                xmlString = xmlString + "<I.LINE4>" + pEmployerInfoDTO.getAddressDTO().getLine4() + "\n";
            if (pEmployerInfoDTO.getAddressDTO().getLine5() != null)
                xmlString = xmlString + "<I.LINE5>" + pEmployerInfoDTO.getAddressDTO().getLine5() + "\n";
            xmlString = xmlString + "</I.ADDRESS>\n";
        }
        if (pEmployerInfoDTO.getNameAddrRefId() != null)
            xmlString = xmlString + "<I.NAMEADDRFEDID>" + pEmployerInfoDTO.getNameAddrRefId() + "\n";
        if(pEmployerInfoDTO.getStateTaxDTO()!=null){
            for(PstubStateTaxInfoDTO stateTaxInfoDTO : pEmployerInfoDTO.getStateTaxDTO()) {
                 xmlString = xmlString +"<I.AGENCYID>" +  stateTaxInfoDTO.getAgencyId() +"\n";
            }
        }
        return xmlString;
    }

    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes());

        Base64 base64 = new Base64();
        return(new String(base64.encode(result)));

    }


    public static PstubEmployeeInfo findOrCreatePstubEmployeeInfo(Employee pEmployee, PstubEmployeeInfoDTO pEmployeeInfoDTO, SpcfMoney pEEFedExtra,
                                                                  SpcfMoney pEEFedClaimDependents,SpcfMoney pEEFedOtherIncome,SpcfMoney pEEFedDeductions,
                                                                  SpcfMoney pEEStateExtra){
        PstubEmployeeInfo pstubEmployee = PstubEmployeeInfo.findPstubEmployeeInfo(pEmployee, pEmployeeInfoDTO.getModTS().intValue());
        if (pstubEmployee == null) {
            pstubEmployee = PstubEmployeeInfo.createPstubEmployeeInfo(pEmployee, pEmployeeInfoDTO.getModTS().intValue(),Boolean.FALSE);
            pstubEmployee.setFedAllowances(pEmployeeInfoDTO.getFedAllowances());
            pstubEmployee.setFedExtra(pEEFedExtra);
            pstubEmployee.setFedClaimDependents(pEEFedClaimDependents);
            pstubEmployee.setFedOtherIncome(pEEFedOtherIncome);
            pstubEmployee.setFedDeductions(pEEFedDeductions);
            pstubEmployee.setFedMultipleJobs(pEmployeeInfoDTO.getFedMultipleJobs());
            pstubEmployee.setFedW4EmployeePref(pEmployeeInfoDTO.getFedW4EmpPref());
            pstubEmployee.setFedTaxFilingStatus(pEmployeeInfoDTO.getFedTaxFilingStatus());
            pstubEmployee.setFedTaxFilingStatusCode(pEmployeeInfoDTO.getFedTaxFilingStatusCode());
            pstubEmployee.setFirstName(pEmployeeInfoDTO.getFirstName());
            pstubEmployee.setMiddleName(pEmployeeInfoDTO.getMiddleName());
            pstubEmployee.setLastName(pEmployeeInfoDTO.getLastName());
            pstubEmployee.setSSN(pEmployeeInfoDTO.getSSN());
            if (pEmployeeInfoDTO.getStateAllowances() != null) {
                pstubEmployee.setStateAllowances(pEmployeeInfoDTO.getStateAllowances());
            }
            pstubEmployee.setStateExtra(pEEStateExtra);
            pstubEmployee.setStateTaxFilingStatus(pEmployeeInfoDTO.getStateTaxFilingStatus());
            if (pEmployeeInfoDTO.getStateTaxFilingStatusCode() != null) {
                pstubEmployee.setStateTaxFilingStatusCode(pEmployeeInfoDTO.getStateTaxFilingStatusCode());
            }
            pstubEmployee.setTaxFilingState(pEmployeeInfoDTO.getTaxFilingState());
            pstubEmployee.setPstubAddress(PstubAddress.createPstubAddress(pEmployeeInfoDTO.getAddressDTO().getLine1(),
                    pEmployeeInfoDTO.getAddressDTO().getLine2(),
                    pEmployeeInfoDTO.getAddressDTO().getLine3(),
                    pEmployeeInfoDTO.getAddressDTO().getLine4(),
                    pEmployeeInfoDTO.getAddressDTO().getLine5()));
            Application.save(pstubEmployee);
        }
        return pstubEmployee;
    }

    public static PstubPayItem createPstubPayItem(Paystub pPaystub, PstubPayItemDTO pPayItemDTO) {
        PstubPayItem pstubPayItem = new PstubPayItem();
        pstubPayItem.setCompany(pPaystub.getPaycheck().getCompany());
        pstubPayItem.setPaystub(pPaystub);
        pstubPayItem.setType(pPayItemDTO.getType());
        pstubPayItem.setAcctName(pPayItemDTO.getAcctName());
        pstubPayItem.setName(pPayItemDTO.getName());
        pstubPayItem.setPayrollItemListId(pPayItemDTO.getPItemListId());
        pstubPayItem.setYTD(QBOFX.mapOFXStringToMoney(pPayItemDTO.getYTD()));
        // optional fields
        if (pPayItemDTO.getCurAmt() != null) {
            pstubPayItem.setCurAmt(QBOFX.mapOFXStringToMoney(pPayItemDTO.getCurAmt()));
        }
        if (pPayItemDTO.getIncomeSubjectToTax() != null) {
            pstubPayItem.setIncomeSubjectToTax(QBOFX.mapOFXStringToMoney(pPayItemDTO.getIncomeSubjectToTax()));
        }
        pstubPayItem.setRate(pPayItemDTO.getRate());
        if (pPayItemDTO.getWageBase() != null) {
            pstubPayItem.setWageBase(QBOFX.mapOFXStringToMoney(pPayItemDTO.getWageBase()));
        }
        pstubPayItem.setQtyAmt(pPayItemDTO.getQtyAmt());
        pstubPayItem.setQtyTime(pPayItemDTO.getQtyTime());
        pstubPayItem.setEmployeePaid(new Boolean(pPayItemDTO.getEmployeePaid()));
        Application.save(pstubPayItem);
        return pstubPayItem;
    }

    public static PstubMsg createPstubMsg(Paystub pPaystub, PstubMsgDTO pPstubMsgDTO, int pSequence) {
        PstubMsg pstubMsg = new PstubMsg();
        pstubMsg.setPaystub(pPaystub);
        pstubMsg.setCompany(pPaystub.getCompany());
        pstubMsg.setType(pPstubMsgDTO.getType());
        pstubMsg.setSequence(pSequence);
        pstubMsg.setText(pPstubMsgDTO.getText());
        Application.save(pstubMsg);
        return pstubMsg;
    }

    public static PstubPaidTimeoffItem createPstubPaidTimeoffItem(Paystub pPaystub, PstubPaidTimeoffItemDTO pPstubPaidTimeoffItemDTO) {
        PstubPaidTimeoffItem pstubPaidTimeoffItem = null;
        if (pPstubPaidTimeoffItemDTO.getName() != null){
            pstubPaidTimeoffItem = new PstubPaidTimeoffItem();
            pstubPaidTimeoffItem.setPaystub(pPaystub);
            pstubPaidTimeoffItem.setCompany(pPaystub.getCompany());
            pstubPaidTimeoffItem.setAcctName(pPstubPaidTimeoffItemDTO.getAcctName());
            pstubPaidTimeoffItem.setName(pPstubPaidTimeoffItemDTO.getName());
            pstubPaidTimeoffItem.setPayrollItemListId(pPstubPaidTimeoffItemDTO.getPItemListId());
            pstubPaidTimeoffItem.setYTDUsed(pPstubPaidTimeoffItemDTO.getYTDUsed() == null ? "0:00" : pPstubPaidTimeoffItemDTO.getYTDUsed());
            pstubPaidTimeoffItem.setAvailable(pPstubPaidTimeoffItemDTO.getAvailable() == null ? "0:00" : pPstubPaidTimeoffItemDTO.getAvailable());
            Application.save(pstubPaidTimeoffItem);
        }else {
            if (!"0:00".equalsIgnoreCase(pPstubPaidTimeoffItemDTO.getAvailable()) ||
                    !"0:00".equalsIgnoreCase(pPstubPaidTimeoffItemDTO.getYTDUsed())) {
                throw new RuntimeException("Error saving paid time off. OFX has non-zero hours with no name sent.");
            }
            Application.getLogger(PaystubHelper.class).warn("Not saving the paid time off since name was not sent as part of the OFX request for paystub:" + pPaystub.getId());
        }
        return pstubPaidTimeoffItem;
    }

    public static PstubDDItem createPstubDDItem(Paystub pPaystub, PstubDDItemDTO pPstubDDItemDTO) {
        PstubDDItem pstubDDItem = new PstubDDItem();
        pstubDDItem.setPaystub(pPaystub);
        pstubDDItem.setCompany(pPaystub.getCompany());
        pstubDDItem.setAcctName(pPstubDDItemDTO.getAcctName());
        pstubDDItem.setAcctNumber(pPstubDDItemDTO.getAcctNumber());
        pstubDDItem.setAcctType(pPstubDDItemDTO.getAcctType());
        pstubDDItem.setBankName(pPstubDDItemDTO.getBankName());
        pstubDDItem.setName(pPstubDDItemDTO.getName());
        pstubDDItem.setPayrollItemListId(pPstubDDItemDTO.getPItemListId());
        pstubDDItem.setRoutingNumber(pPstubDDItemDTO.getRoutingNumber());
        pstubDDItem.setCurAmt(QBOFX.mapOFXStringToMoney(pPstubDDItemDTO.getCurAmt()));
        Application.save(pstubDDItem);
        return pstubDDItem;
    }

    private static PstubStateTaxInfo createPstubStateTax(PstubStateTaxInfoDTO pPstubStateTaxInfoDTO, PstubEmployerInfo pPstubEmployerInfo) {
        PstubStateTaxInfo pstubStateTaxInfo = new PstubStateTaxInfo();
        pstubStateTaxInfo.setAgencyId(pPstubStateTaxInfoDTO.getAgencyId());
        pstubStateTaxInfo.setAgencyName(pPstubStateTaxInfoDTO.getAgencyName());
        pstubStateTaxInfo.setPstubEmployerInfo(pPstubEmployerInfo);
        Application.save(pstubStateTaxInfo);
        return pstubStateTaxInfo;
    }
}
