package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.PaystubHelper;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/18/13
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdatePaystubCore extends Process implements IProcess {
    private PaystubDTO mPaystubDTO;
    private Employee mEmployee;
    private Paycheck mPaycheck;
    private Paystub mPaystub;

    // temps to store translated data
    private SpcfMoney mEEFedExtra;
    private SpcfMoney mEEFedClaimDependents;
    private SpcfMoney mEEFedOtherIncome;
    private SpcfMoney mEEFedDeductions;
    private SpcfMoney mEEStateExtra;
    private SpcfMoney mGrossPay;
    private SpcfMoney mAdjNetPay;
    private SpcfMoney mNetPay;
    private SpcfMoney mPreTaxDeduct;
    private SpcfMoney mTax;
    private SpcfMoney mYTDTax;
    private SpcfMoney mYTDAdjNetPay;
    private SpcfMoney mYTDGrossPay;
    private SpcfMoney mYTDNetPay;
    private SpcfMoney mYTDPreTaxDeduct;

    private SpcfCalendar mPayBeginDate;
    private SpcfCalendar mPaycheckDate;
    private SpcfCalendar mPayEndDate;

    public UpdatePaystubCore(Paycheck pPaycheck, Employee pEmployee, Paystub pPaystub, PaystubDTO pPaystubDTO) {
        mPaycheck = pPaycheck;
        mEmployee = pEmployee;
        mPaystubDTO = pPaystubDTO;
        mPaystub = pPaystub;
    }

    public ProcessResult process() {
        ProcessResult<Paystub> processResult = new ProcessResult<Paystub>();

        mPaystub.setSourceModTime(mPaystubDTO.getModTS().intValue());
        mPaystub.setGrossPay(mGrossPay);
        mPaystub.setCheckNumber(mPaystubDTO.getChkNum());
        mPaystub.setAdjNetPay(mAdjNetPay);
        mPaystub.setNetPay(mNetPay);
        mPaystub.setPayBeginDate(mPayBeginDate);
        mPaystub.setPaycheckDate(mPaycheckDate);
        mPaystub.setPayEndDate(mPayEndDate);
        mPaystub.setPreTaxDeductions(mPreTaxDeduct);
        mPaystub.setTaxes(mTax);
        mPaystub.setYTDTaxes(mYTDTax);
        mPaystub.setYTDAdjNetPay(mYTDAdjNetPay);
        mPaystub.setYTDGrossPay(mYTDGrossPay);
        mPaystub.setYTDNetPay(mYTDNetPay);
        mPaystub.setYTDPreTaxDeductions(mYTDPreTaxDeduct);

        // update employerinfo
        PstubEmployerInfoDTO employerInfoDTO = mPaystubDTO.getEmployerInfoDTO();
        PstubEmployerInfo oldPstubEmployer = mPaystub.getPstubEmployerInfo();
        PstubEmployerInfo newPstubEmployer = PaystubHelper.findOrCreatePstubEmployerInfo(employerInfoDTO);
        oldPstubEmployer.getPaystubCollection().remove(mPaystub);
        mPaystub.setPstubEmployerInfo(newPstubEmployer);
        newPstubEmployer.getPaystubCollection().add(mPaystub);

        if (oldPstubEmployer != null && oldPstubEmployer.getPaystubCollection().isEmpty()) {
            oldPstubEmployer.delete();
        }



        // update employeeinfo
        PstubEmployeeInfoDTO employeeInfoDTO = mPaystubDTO.getEmployeeInfoDTO();
        PstubEmployeeInfo oldPstubEmployee = mPaystub.getPstubEmployeeInfo();
        PstubEmployeeInfo newPstubEmployee = PaystubHelper.findOrCreatePstubEmployeeInfo(mEmployee, mPaystubDTO.getEmployeeInfoDTO(), mEEFedExtra,
                mEEFedClaimDependents, mEEFedOtherIncome, mEEFedDeductions,  mEEStateExtra);
        oldPstubEmployee.getPaystubCollection().remove(mPaystub);
        mPaystub.setPstubEmployeeInfo(newPstubEmployee);
        newPstubEmployee.getPaystubCollection().add(mPaystub);
        if (oldPstubEmployee != null && oldPstubEmployee.getPaystubCollection().isEmpty()) {
            oldPstubEmployee.delete();
        }



        WorkersCompPaycheck.updateWorkersCompPaycheck(mPaystub, mPaycheck) ;


        Application.save(mPaystub);

        // create line items
        mPaystub.getPstubDDItemCollection().clear();
        for (PstubDDItemDTO pstubDDItemDTO : mPaystubDTO.getDDItemDTOs()) {
            PaystubHelper.createPstubDDItem(mPaystub, pstubDDItemDTO);
        }

        mPaystub.getPstubPaidTimeoffItemCollection().clear();
        for (PstubPaidTimeoffItemDTO pstubPaidTimeoffItemDTO : mPaystubDTO.getPaidTimeoffItemDTOs()) {
            PaystubHelper.createPstubPaidTimeoffItem(mPaystub, pstubPaidTimeoffItemDTO);
        }

        mPaystub.getPstubPayItemCollection().clear();
        for (PstubPayItemDTO pstubPayItemDTO : mPaystubDTO.getPayItemDTOs()) {
            PaystubHelper.createPstubPayItem(mPaystub, pstubPayItemDTO);
        }

        // create msgs
        mPaystub.getPstubMsgCollection().clear();

        int userMsgSequence = 1;
        int companyMsgSequence = 1;
        for (PstubMsgDTO pstubMsgDTO : mPaystubDTO.getMsgDTOs()) {
            if (pstubMsgDTO.getType() == PstubMsgType.Company) {
                PaystubHelper.createPstubMsg(mPaystub, pstubMsgDTO, companyMsgSequence++);
            } else if (pstubMsgDTO.getType() == PstubMsgType.User) {
                PaystubHelper.createPstubMsg(mPaystub, pstubMsgDTO, userMsgSequence++);
            }
        }

        Application.save(mPaystub);
        processResult.setResult(mPaystub);
        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // pstub fields
        // String to Money conversion
        try {
            mGrossPay = QBOFX.mapOFXStringToMoney(mPaystubDTO.getGrossPay());
            mAdjNetPay = QBOFX.mapOFXStringToMoney(mPaystubDTO.getAdjNetPay());
            mNetPay = QBOFX.mapOFXStringToMoney(mPaystubDTO.getNetPay());
            mPreTaxDeduct = QBOFX.mapOFXStringToMoney(mPaystubDTO.getPreTaxDeducts());
            mTax = QBOFX.mapOFXStringToMoney(mPaystubDTO.getTax());
            mYTDTax = QBOFX.mapOFXStringToMoney(mPaystubDTO.getYTDTax());
            mYTDAdjNetPay = QBOFX.mapOFXStringToMoney(mPaystubDTO.getYTDAdjNetPay());
            mYTDGrossPay = QBOFX.mapOFXStringToMoney(mPaystubDTO.getYTDGrossPay());
            mYTDNetPay = QBOFX.mapOFXStringToMoney(mPaystubDTO.getYTDNetPay());
            mYTDPreTaxDeduct = QBOFX.mapOFXStringToMoney(mPaystubDTO.getYTDPreTaxDeducts());
        } catch (Exception e) {
            validationResult.getMessages().InvalidArgumentType(EntityName.Paystub, mPaycheck.getId().toString(), "", "Money");
            return validationResult;
        }

        // String to Calendar conversion
        try {
            mPayBeginDate = CalendarUtils.createInstanceFromXMLGregorianCalendar(mPaystubDTO.getPayBeginDate());
            mPaycheckDate = CalendarUtils.createInstanceFromXMLGregorianCalendar(mPaystubDTO.getPayDate());
            mPayEndDate = CalendarUtils.createInstanceFromXMLGregorianCalendar(mPaystubDTO.getPayEndDate());
        } catch (Exception e) {
            validationResult.getMessages().InvalidArgumentType(EntityName.Paystub, mPaycheck.getId().toString(), "", "Date");
            return validationResult;
        }

        // check employeeInfo
        try {
            mEEFedExtra = QBOFX.mapOFXStringToMoney(mPaystubDTO.getEmployeeInfoDTO().getFedExtra());
            mEEFedClaimDependents = QBOFX.mapOFXStringToMoney(mPaystubDTO.getEmployeeInfoDTO().getFedClaimDependents());
            mEEFedOtherIncome = QBOFX.mapOFXStringToMoney(mPaystubDTO.getEmployeeInfoDTO().getFedOtherIncome());
            mEEFedDeductions = QBOFX.mapOFXStringToMoney(mPaystubDTO.getEmployeeInfoDTO().getFedDeduction());
            mEEStateExtra = QBOFX.mapOFXStringToMoney(mPaystubDTO.getEmployeeInfoDTO().getStateExtra());
        } catch (Exception e) {
            validationResult.getMessages().InvalidArgumentType(EntityName.PstubEmployeeInfo, mEmployee.getSourceEmployeeId(), "", "Money");
            return validationResult;
        }

        if ((mPaystubDTO.getEmployeeInfoDTO().getFirstName() == null || !Validator.isValidLength(mPaystubDTO.getEmployeeInfoDTO().getFirstName().trim(), 1, 80))
                && (mPaystubDTO.getEmployeeInfoDTO().getLastName() == null || !(Validator.isValidLength(mPaystubDTO.getEmployeeInfoDTO().getLastName().trim(), 1, 80)))) {
            validationResult.getMessages().InvalidArgumentType(EntityName.PstubEmployeeInfo, mEmployee.getSourceEmployeeId(), "", "Name");
            return validationResult;
        }

        // check DDListItem
        try {
            for (PstubDDItemDTO pstubDDItemDTO : mPaystubDTO.getDDItemDTOs()) {
                QBOFX.mapOFXStringToMoney(pstubDDItemDTO.getCurAmt());
            }
        } catch (Exception e) {
            validationResult.getMessages().InvalidArgumentType(EntityName.PstubDDItems, mPaycheck.getId().toString(), "", "Money");
            return validationResult;
        }

        // check PayListItem
        try {
            for (PstubPayItemDTO pstubPayItemDTO : mPaystubDTO.getPayItemDTOs()) {
                QBOFX.mapOFXStringToMoney(pstubPayItemDTO.getCurAmt());
                QBOFX.mapOFXStringToMoney(pstubPayItemDTO.getIncomeSubjectToTax());
                QBOFX.mapOFXStringToMoney(pstubPayItemDTO.getWageBase());
                QBOFX.mapOFXStringToMoney(pstubPayItemDTO.getYTD());
            }
        } catch (Exception e) {
            validationResult.getMessages().InvalidArgumentType(EntityName.PstubPayItems, mPaycheck.getId().toString(), "", "Money");
            return validationResult;
        }

        return validationResult;
    }
}
