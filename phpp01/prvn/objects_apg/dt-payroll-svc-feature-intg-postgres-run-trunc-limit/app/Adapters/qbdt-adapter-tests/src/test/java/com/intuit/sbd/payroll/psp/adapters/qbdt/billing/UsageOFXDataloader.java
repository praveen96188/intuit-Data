package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Tiger Shao
 * Date: 6/27/12
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsageOFXDataloader {
    private ObjectFactory mObjFactory = new ObjectFactory();

    public static final String OFX_NULL_STRING = QBOFX.NULL;

    public OFX createOFX(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pVoid) {
        return createOFX(pUsername, pPassword, pEIN, pSubscriptionNum, pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, false, 1, true, true);
    }

    public OFX createOFX(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pVoid, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        return createOFX(pUsername, pPassword, pEIN, pSubscriptionNum, pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, false, 1, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
    }

    public OFX createOFX(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pVoid, boolean pWithPaystub, int pNumOfPaychecks) {
        return createOFX(pUsername, pPassword, pEIN, pSubscriptionNum, pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, pWithPaystub, pNumOfPaychecks);
    }

    public OFX createOFX(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pVoid, boolean pWithPaystub, int pNumOfPaychecks, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        OFX ofxObj = mObjFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = createSignOnMessage(pUsername, pPassword, pEIN, pSubscriptionNum);
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = createPayrollUpdateMessage(pUsername, pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, pWithPaystub, pNumOfPaychecks, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX createOFX(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, SpcfCalendar pPaycheckDate, List<EmployeeDTO> pEmployees, String pPaycheckId, String pVoid, boolean pWithPaystub, int pNumOfPaychecks, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        return createOFX(pUsername, pPassword, pEIN, pSubscriptionNum, Arrays.asList(pPaycheckDate), pEmployees, pPaycheckId, pVoid, pWithPaystub, pNumOfPaychecks, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
    }

    public OFX createOFX(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, List<SpcfCalendar> pPaycheckDate, List<EmployeeDTO> pEmployees, String pPaycheckId, String pVoid, boolean pWithPaystub, int pNumOfPaychecks, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        OFX ofxObj = mObjFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = createSignOnMessage(pUsername, pPassword, pEIN, pSubscriptionNum);
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = createPayrollUpdateMessage(pUsername, pPaycheckDate, pEmployees, pPaycheckId, pVoid, pWithPaystub, pNumOfPaychecks, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public OFX createOFX(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, String pDeletedPaycheckId) {
        OFX ofxObj = mObjFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = createSignOnMessage(pUsername, pPassword, pEIN, pSubscriptionNum);
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = createDeletePaycheckMessage(pUsername, pDeletedPaycheckId);
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public SIGNONMSGSRQV1 createSignOnMessage(String pUsername, String pPassword, String pEIN, String pSubscriptionNum) {

        SIGNONMSGSRQV1 signOnRequestMessage = mObjFactory.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = mObjFactory.createSONRQ();

        signOnRequest.setAPPID(QBOFX.QB_APP_ID_VERSIONS.QBWPRO);
        signOnRequest.setAPPVER("22.00.P.102/21212#bel");
        signOnRequest.setDTCLIENT(QBOFX.getOFXServerDTTM(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())));
        signOnRequest.setIIPADDRESS("FileInfo:QB_MTVL07EF17D9C43_22:172.28.18.31#55348 ServerInfo:QB_MTVL07EF17D9C43_22:172.28.18.31#55348");
        signOnRequest.setIQBFILEID("ed03d160085c4d6eb15055b479375b47");
        signOnRequest.setIQBFILENAME("C:\\Documents and Settings\\All Users\\Documents\\Intuit\\QuickBooks\\Company Files\\Joes Cool Co.QBW");
        signOnRequest.setIQBUSERNAME("Admin");
        signOnRequest.setLANGUAGE(QBOFX.LANGUAGE);
        signOnRequest.setUSERPASS(pPassword);

        signOnRequest.setUSERID(pUsername);
        signOnRequest.setIRQEIN(pEIN);
        signOnRequest.setISUBSCRIPTIONNUM(pSubscriptionNum);

        signOnRequestMessage.setSONRQ(signOnRequest);
        return signOnRequestMessage;
    }

    public IPAYROLLMSGSRQV1 createPayrollUpdateMessage(String pUsername, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId,String pVoid, boolean pWithPaystub, int pNumOfPaychecks, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = mObjFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = mObjFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(pUsername, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = mObjFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0026");
        IPAYROLLRQ payrollRequest = mObjFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        IEMP employee = createEmployee(pEmpId, pFirstName, pLastName, pGenerateStandardEmployeeListId);
        payrollRequest.getIEMP().add(employee);

        IPAYROLLRUN payrollRun = createPayrollRun(pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, pWithPaystub, pNumOfPaychecks, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        payrollRequest.getIPAYROLLRUN().add(payrollRun);

        return rtnPayrollUpdateMessage;
    }

    public IPAYROLLMSGSRQV1 createPayrollUpdateMessage(String pUsername, List<SpcfCalendar> pPaycheckDates, List<EmployeeDTO> pEmployees, String pPaycheckId,String pVoid, boolean pWithPaystub, int pNumOfPaychecks, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = mObjFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = mObjFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(pUsername, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = mObjFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0026");
        IPAYROLLRQ payrollRequest = mObjFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        for (EmployeeDTO employeeDTO: pEmployees) {
            IEMP employee = createEmployee(employeeDTO.getEmployeeId(), employeeDTO.getFirstName(), employeeDTO.getLastName(), pGenerateStandardEmployeeListId);
            payrollRequest.getIEMP().add(employee);
        }

        String paycheck = "1";
        for (SpcfCalendar pPaycheckDate:pPaycheckDates){
            IPAYROLLRUN payrollRun = createPayrollRun(pPaycheckDate, payrollRequest.getIEMP(), pPaycheckId, pVoid, pWithPaystub, pNumOfPaychecks, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
            payrollRequest.getIPAYROLLRUN().add(payrollRun);
        }

        return rtnPayrollUpdateMessage;
    }

    public OFX createOFXWithErrEEs(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pVoid) {
        return createOFXWithErrEEs(pUsername, pPassword, pEIN, pSubscriptionNum, pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, true, true);
    }

    public OFX createOFXWithErrEEs(String pUsername, String pPassword, String pEIN, String pSubscriptionNum, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pVoid, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        OFX ofxObj = mObjFactory.createOFX();
        SIGNONMSGSRQV1 signOnObj = createSignOnMessage(pUsername, pPassword, pEIN, pSubscriptionNum);
        ofxObj.setSIGNONMSGSRQV1(signOnObj);
        IPAYROLLMSGSRQV1 payrollObj = createPayrollUpdateMessageWithErrEEs(pUsername, pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        ofxObj.setIPAYROLLMSGSRQV1(payrollObj);
        return ofxObj;
    }

    public IPAYROLLMSGSRQV1 createPayrollUpdateMessageWithErrEEs(String pUsername, SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId,String pVoid, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = mObjFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = mObjFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(pUsername, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = mObjFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0026");
        IPAYROLLRQ payrollRequest = mObjFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        IEMP employee = createEmployee(pEmpId, pFirstName, pLastName, pGenerateStandardEmployeeListId);
        payrollRequest.getIEMP().add(employee);

        // err ees
        for (int i=0; i<200; i++) {
            employee = createEmployee(pEmpId+"00"+String.valueOf(i), pFirstName, pLastName, pGenerateStandardEmployeeListId);
            payrollRequest.getIEMP().add(employee);
        }

        IPAYROLLRUN payrollRun = createPayrollRun(pPaycheckDate, pEmpId, pFirstName, pLastName, pPaycheckId, pVoid, false, 1, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        payrollRequest.getIPAYROLLRUN().add(payrollRun);

        return rtnPayrollUpdateMessage;
    }

    public IPAYROLLMSGSRQV1 createDeletePaycheckMessage(String pUsername, String pPaycheckId) {
        IPAYROLLMSGSRQV1 rtnPayrollUpdateMessage = mObjFactory.createIPAYROLLMSGSRQV1();
        IPAYROLLUPDATERQ rtnPayrollUpdateRequest = mObjFactory.createIPAYROLLUPDATERQ();
        rtnPayrollUpdateMessage.setIPAYROLLUPDATERQ(rtnPayrollUpdateRequest);

        Company company = Company.findCompany(pUsername, SourceSystemCode.QBDT);
        rtnPayrollUpdateRequest.setTOKEN((company.getCurrentToken())+"");
        rtnPayrollUpdateRequest.setREJECTIFMISSING("Y");
        IPAYROLLTRNRQ payrollTransactionRequest = mObjFactory.createIPAYROLLTRNRQ();
        rtnPayrollUpdateRequest.setIPAYROLLTRNRQ(payrollTransactionRequest);

        payrollTransactionRequest.setTRNUID("87536D20-79F5-1000-BB15-CB9C31AB0026");
        IPAYROLLRQ payrollRequest = mObjFactory.createIPAYROLLRQ();
        payrollTransactionRequest.setIPAYROLLRQ(payrollRequest);

        payrollRequest.getIPAYCHKDELID().add(pPaycheckId);

        return rtnPayrollUpdateMessage;
    }

    private IPAYROLLRUN createPayrollRun(SpcfCalendar pPaycheckDate, String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pVoid, boolean pWithPaystub, int pNumOfPaychecks, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        IPAYROLLRUN payrollRun = mObjFactory.createIPAYROLLRUN();

        payrollRun.setIDTPAYCHKS(pPaycheckDate.format("yyyyMMdd"));

        SpcfCalendar payPDBegin = pPaycheckDate.copy();
        CalendarUtils.addBusinessDays(payPDBegin, -12);
        SpcfCalendar payPDEnd = pPaycheckDate.copy();
        CalendarUtils.addBusinessDays(payPDEnd,-2);
        int iPaycheckId = Integer.parseInt(pPaycheckId);
        for (int i=0; i<pNumOfPaychecks; i++) {
            IPAYCHK paycheck = createPaycheck(pEmpId, pFirstName, pLastName, String.valueOf(iPaycheckId+i), payPDBegin.format("yyyyMMdd"), payPDEnd.format("yyyyMMdd"),pVoid, pWithPaystub, pGenerateStandardPaycheckListId);
            payrollRun.getIPAYCHK().add(paycheck);
        }

        return payrollRun;
    }

    private IPAYROLLRUN createPayrollRun(SpcfCalendar pPaycheckDate,List<IEMP> pEmployees, String pPaycheckId, String pVoid, boolean pWithPaystub, int pNumOfPaychecks, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        IPAYROLLRUN payrollRun = mObjFactory.createIPAYROLLRUN();
        payrollRun.setIDTPAYCHKS(pPaycheckDate.format("yyyyMMdd"));

        SpcfCalendar payPDBegin = pPaycheckDate.copy();
        CalendarUtils.addBusinessDays(payPDBegin, -12);
        SpcfCalendar payPDEnd = pPaycheckDate.copy();
        CalendarUtils.addBusinessDays(payPDEnd,-2);
        for (IEMP employee: pEmployees) {
            for (int j=0; j< pNumOfPaychecks; j++) {
                IPAYCHK paycheck = createPaycheck(employee.getIEMPID(), employee.getIADDRINFO().getIFIRST(), employee.getIADDRINFO().getILAST(), pPaycheckDate, (employee.getIEMPID()+ pPaycheckId + pPaycheckDate.format("yyMMdd")), payPDBegin.format("yyyyMMdd"), payPDEnd.format("yyyyMMdd"),pVoid, pWithPaystub, pGenerateStandardPaycheckListId);
                payrollRun.getIPAYCHK().add(paycheck);
            }
        }

        return payrollRun;
    }

    private IEMP createEmployee(String pEmpId, String pFirstName, String pLastName, boolean pGenerateStandardEmployeeListId) {
        IEMP employee = mObjFactory.createIEMP();

        employee.setIEMPID(pEmpId);

        // By default list ids are created with epoch time (January 1, 2011 12:00:00 AM)
        String listId = pEmpId+"-1293840000";

        // Override the list Id
        if(pGenerateStandardEmployeeListId){
            long epoch = PSPDate.getPSPTime().getTimeInMilliseconds();
            int epochInSeconds = (int)(epoch/1000);
            listId = pEmpId+"-"+epochInSeconds;
        }

        employee.setIQBUNIQUEID(listId);

        employee.setIINACTIVE("N");
        employee.setIEMPTYPE("REG");

        IADDRINFO addrinfo = mObjFactory.createIADDRINFO();
        addrinfo.setIFIRST(pFirstName);
        addrinfo.setILAST(pLastName);

        employee.setIADDRINFO(addrinfo);

        return employee;
    }

    private IPAYCHK createPaycheck(String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pPayPDBegin, String pPayPDEnd, String pVoid, boolean pWithPaystub, boolean pGenerateStandardPaycheckListId) {
        return createPaycheck(pEmpId, pFirstName, pLastName, PSPDate.getPSPTime(), pPaycheckId, pPayPDBegin, pPayPDEnd, pVoid, pWithPaystub, pGenerateStandardPaycheckListId);
    }

    private IPAYCHK createPaycheck(String pEmpId, String pFirstName, String pLastName, SpcfCalendar pPaycheckDate, String pPaycheckId, String pPayPDBegin, String pPayPDEnd, String pVoid, boolean pWithPaystub, boolean pGenerateStandardPaycheckListId) {
        IPAYCHK paycheck = mObjFactory.createIPAYCHK();

        paycheck.setIEMPID(pEmpId);
        paycheck.setIPAYCHKID(pPaycheckId);

        String listId = pPaycheckId+"ppp";

        // Override the list Id
        if(pGenerateStandardPaycheckListId){
            long epoch = PSPDate.getPSPTime().getTimeInMilliseconds();
            int epochInSeconds = (int)(epoch/1000);
            listId = pPaycheckId+"-"+epochInSeconds;
        }

        paycheck.setIQBUNIQUEID(listId);

        paycheck.setIDTTX(pPaycheckDate.format("yyyyMMdd"));
        paycheck.setIPAYCHKTYPE("PAYCHK");
        paycheck.setIEMPNAME(OFX_NULL_STRING);
        paycheck.setICLASS(OFX_NULL_STRING);

        IPAYCHKINFO paycheckInfo = mObjFactory.createIPAYCHKINFO();
        paycheck.setIPAYCHKINFO(paycheckInfo);

        paycheckInfo.setICHKNUM("TOPRINT");
        paycheckInfo.setIPRORATE("N");
        paycheckInfo.setISICKACCRUED(OFX_NULL_STRING);
        paycheckInfo.setIVACACCRUED(OFX_NULL_STRING);

        paycheck.setIVOID(pVoid);
        paycheck.setIDTPAYPDBEGIN(pPayPDBegin);
        paycheck.setIDTPAYPDEND(pPayPDEnd);
        paycheck.setIMEMO(OFX_NULL_STRING);
        paycheck.setICLEARED("2");
        paycheck.setIAMT("-2000");

        if (pWithPaystub) {
            try {
                paycheck.setIPAYSTUBINFO(createPaystub(pEmpId, pPaycheckId, pFirstName, pLastName));
            } catch (Exception e) {
            }
        }

        return paycheck;
    }

    private IPAYSTUBINFO createPaystub(String pEmpId, String pPaycheckId, String pFirstName, String pLastName) throws Exception {
        IPAYSTUBINFO paystub = mObjFactory.createIPAYSTUBINFO();

        paystub.setIADJNETPAY("0.00");
        paystub.setICHKNUM("TOPRINT");
        long now = SpcfCalendar.getNow().getTimeInMilliseconds();
        paystub.setICREATETIMESTAMP(BigInteger.valueOf(now));
        paystub.setIGROSSPAY("100.00");
        paystub.setIMODTIMESTAMP(BigInteger.valueOf(now));
        paystub.setINETPAY("0.00");
        paystub.setIPAYCHKID(pPaycheckId);
        paystub.setIPRETAXDEDUCTIONS("0.00");
        paystub.setIQBUNIQUEID(pPaycheckId+"ppp");
        paystub.setITAXES("0.00");
        paystub.setIYTDADJNETPAY("1000.00");
        paystub.setIYTDGROSSPAY("1000.00");
        paystub.setIYTDNETPAY("0.00");
        paystub.setIYTDPRETAXDEDUCTIONS("0.00");
        paystub.setIYTDTAXES("0.00");

        IPAYPERIOD payperiod = mObjFactory.createIPAYPERIOD();
        XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-03-26");
        payperiod.setIBEGINDATE(calendar);
        payperiod.setICHECKDATE(calendar);
        payperiod.setIENDDATE(calendar);
        paystub.setIPAYPERIOD(payperiod);

        IEMPLOYERINFO employerinfo = mObjFactory.createIEMPLOYERINFO();
        employerinfo.setINAMEADDRFEDID("");
        employerinfo.setIEMPLOYERHASH("12345678");
        paystub.setIEMPLOYERINFO(employerinfo);

        IEMPLOYEEINFO employeeinfo = mObjFactory.createIEMPLOYEEINFO();
        employeeinfo.setIFIRSTNAME(pFirstName);
        employeeinfo.setILASTNAME(pLastName);
        employeeinfo.setIQBUNIQUEID(pEmpId+"eee");
        employeeinfo.setIFEDTAXFILINGSTATUS("Single");
        employeeinfo.setIFEDTAXFILINGSTATUSCODE(0);
        employeeinfo.setIFEDERALALLOWANCES(0);
        employeeinfo.setICREATETIMESTAMP(BigInteger.valueOf(now));
        employeeinfo.setIMODTIMESTAMP(BigInteger.valueOf(now));
        paystub.setIEMPLOYEEINFO(employeeinfo);

        return paystub;
    }

    private IPAYCHK createDDPaycheck(String pEmpId, String pFirstName, String pLastName, String pPaycheckId, String pPayPDBegin, String pPayPDEnd, String pVoid, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) {
        IPAYCHK paycheck = createPaycheck(pEmpId, pFirstName, pLastName, pPaycheckId, pPayPDBegin, pPayPDEnd, pVoid, false, pGenerateStandardPaycheckListId);


        IDDLINE ddLine = mObjFactory.createIDDLINE();

        IDDACCT ddLineDDAcct = mObjFactory.createIDDACCT();
        ddLine.setIDDACCT(ddLineDDAcct);

        ddLineDDAcct.setIACCTNAME("Bank of Money");
        ddLineDDAcct.setIAMT(OFX_NULL_STRING);

        BANKACCT ddLineDDAcctBankAcctTo = mObjFactory.createBANKACCT();
        ddLineDDAcct.setBANKACCTTO(ddLineDDAcctBankAcctTo);

        ddLineDDAcctBankAcctTo.setBANKID("113003842");
        ddLineDDAcctBankAcctTo.setACCTID("0011992288");
        ddLineDDAcctBankAcctTo.setACCTTYPE("SAVINGS");

        ddLine.setIPITEMID("0");
        ddLine.setIAMT("$-1.00");

        paycheck.getIDDLINE().add(ddLine);
        return paycheck;
    }

}
