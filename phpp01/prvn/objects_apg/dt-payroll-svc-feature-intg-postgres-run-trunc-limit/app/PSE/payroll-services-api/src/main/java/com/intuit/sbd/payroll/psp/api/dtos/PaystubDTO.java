package com.intuit.sbd.payroll.psp.api.dtos;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/18/13
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaystubDTO {
    private String mChkNum;
    private BigInteger mModTS;
    private PstubEmployeeInfoDTO mEmployeeInfoDTO;
    private PstubEmployerInfoDTO mEmployerInfoDTO;
    private XMLGregorianCalendar mPayBeginDate;
    private XMLGregorianCalendar mPayEndDate;
    private XMLGregorianCalendar mPayDate;
    private String mGrossPay;
    private String mYTDGrossPay;
    private String mNetPay;
    private String mYTDNetPay;
    private String mAdjNetPay;
    private String mYTDAdjNetPay;
    private String mTax;
    private String mYTDTax;
    private String mPreTaxDeducts;
    private String mYTDPreTaxDeducts;
    private Collection<PstubDDItemDTO> mDDItemDTOs;
    private Collection<PstubPaidTimeoffItemDTO> mPaidTimeoffItemDTOs;
    private Collection<PstubPayItemDTO> mPayItemDTOs;
    private Collection<PstubMsgDTO> mMsgDTOs;

    public String getChkNum() {
        return mChkNum;
    }

    public void setChkNum(String pChkNum) {
        mChkNum = pChkNum;
    }

    public BigInteger getModTS() {
        return mModTS;
    }

    public void setModTS(BigInteger pModTS) {
        mModTS = pModTS;
    }

    public PstubEmployeeInfoDTO getEmployeeInfoDTO() {
        return mEmployeeInfoDTO;
    }

    public void setEmployeeInfoDTO(PstubEmployeeInfoDTO pEmployeeInfoDTO) {
        mEmployeeInfoDTO = pEmployeeInfoDTO;
    }

    public PstubEmployerInfoDTO getEmployerInfoDTO() {
        return mEmployerInfoDTO;
    }

    public void setEmployerInfoDTO(PstubEmployerInfoDTO pEmployerInfoDTO) {
        mEmployerInfoDTO = pEmployerInfoDTO;
    }

    public XMLGregorianCalendar getPayBeginDate() {
        return mPayBeginDate;
    }

    public void setPayBeginDate(XMLGregorianCalendar pPayBeginDate) {
        mPayBeginDate = pPayBeginDate;
    }

    public XMLGregorianCalendar getPayEndDate() {
        return mPayEndDate;
    }

    public void setPayEndDate(XMLGregorianCalendar pPayEndDate) {
        mPayEndDate = pPayEndDate;
    }

    public XMLGregorianCalendar getPayDate() {
        return mPayDate;
    }

    public void setPayDate(XMLGregorianCalendar pPayDate) {
        mPayDate = pPayDate;
    }

    public String getGrossPay() {
        return mGrossPay;
    }

    public void setGrossPay(String pGrossPay) {
        mGrossPay = pGrossPay;
    }

    public String getYTDGrossPay() {
        return mYTDGrossPay;
    }

    public void setYTDGrossPay(String pYTDGrossPay) {
        mYTDGrossPay = pYTDGrossPay;
    }

    public String getNetPay() {
        return mNetPay;
    }

    public void setNetPay(String pNetPay) {
        mNetPay = pNetPay;
    }

    public String getYTDNetPay() {
        return mYTDNetPay;
    }

    public void setYTDNetPay(String pYTDNetPay) {
        mYTDNetPay = pYTDNetPay;
    }

    public String getAdjNetPay() {
        return mAdjNetPay;
    }

    public void setAdjNetPay(String pAdjNetPay) {
        mAdjNetPay = pAdjNetPay;
    }

    public String getYTDAdjNetPay() {
        return mYTDAdjNetPay;
    }

    public void setYTDAdjNetPay(String pYTDAdjNetPay) {
        mYTDAdjNetPay = pYTDAdjNetPay;
    }

    public String getTax() {
        return mTax;
    }

    public void setTax(String pTax) {
        mTax = pTax;
    }

    public String getYTDTax() {
        return mYTDTax;
    }

    public void setYTDTax(String pYTDTax) {
        mYTDTax = pYTDTax;
    }

    public String getPreTaxDeducts() {
        return mPreTaxDeducts;
    }

    public void setPreTaxDeducts(String pPreTaxDeducts) {
        mPreTaxDeducts = pPreTaxDeducts;
    }

    public String getYTDPreTaxDeducts() {
        return mYTDPreTaxDeducts;
    }

    public void setYTDPreTaxDeducts(String pYTDPreTaxDeducts) {
        mYTDPreTaxDeducts = pYTDPreTaxDeducts;
    }

    public Collection<PstubDDItemDTO> getDDItemDTOs() {
        if(mDDItemDTOs == null) {
            mDDItemDTOs = new ArrayList<PstubDDItemDTO>();
        }
        return mDDItemDTOs;
    }

    public void setDDItemDTOs(Collection<PstubDDItemDTO> pDDItemDTOs) {
        mDDItemDTOs = pDDItemDTOs;
    }

    public Collection<PstubPaidTimeoffItemDTO> getPaidTimeoffItemDTOs() {
        if(mPaidTimeoffItemDTOs == null) {
            mPaidTimeoffItemDTOs = new ArrayList<PstubPaidTimeoffItemDTO>();
        }
        return mPaidTimeoffItemDTOs;
    }

    public void setPaidTimeoffItemDTOs(Collection<PstubPaidTimeoffItemDTO> pPaidTimeoffItemDTOs) {
        mPaidTimeoffItemDTOs = pPaidTimeoffItemDTOs;
    }

    public Collection<PstubPayItemDTO> getPayItemDTOs() {
        if(mPayItemDTOs == null) {
            mPayItemDTOs = new ArrayList<PstubPayItemDTO>();
        }
        return mPayItemDTOs;
    }

    public void setPayItemDTOs(Collection<PstubPayItemDTO> pPayItemDTOs) {
        mPayItemDTOs = pPayItemDTOs;
    }

    public Collection<PstubMsgDTO> getMsgDTOs() {
        if(mMsgDTOs == null) {
            mMsgDTOs = new ArrayList<PstubMsgDTO>();
        }
        return mMsgDTOs;
    }

    public void setMsgDTOs(Collection<PstubMsgDTO> pMsgDTOs) {
        mMsgDTOs = pMsgDTOs;
    }
}
