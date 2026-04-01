package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.SourceSystem;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccountadd.EmployeeBankAccountAdd;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccountaddrs.EmployeeBankAccountAddRs;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccountdeactivate.EmployeeBankAccountDeactivate;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccountdeactivaters.EmployeeBankAccountDeactivateRs;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccounthistoryquery.EmployeeBankAccountHistoryQuery;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccounthistoryqueryret.EmployeeBankAccountHistoryQueryRet;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccounthistoryqueryrs.EmployeeBankAccountHistoryQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccountret.EmployeeBankAccountRet;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccountupdate.EmployeeBankAccountUpdate;
import intuit.osp.pse.dd.wsapi.xsd.employeebankaccountupdaters.EmployeeBankAccountUpdateRs;
import intuit.osp.pse.dd.wsapi.xsd.employeeinfo.EmployeeInfo;
import org.w3c.dom.Element;

import java.util.Calendar;
import java.util.Iterator;

/**
 *
 * User: mvillani
 * Date: Aug 23, 2007
 * Time: 3:30:44 PM

 */
public class EmployeeBankAccount extends WS {
    private static SpcfLogger logger = Application.getLogger(EmployeeBankAccount.class);
    private static final String EMPLOYEEBANKACCOUNT_ALREADY_EXISTS = "164";

    DDCommon ddCommon = new DDCommon();
    WSServerContext context = null;
    EmployeeBankAccountAddRs employeeBankAccountAddRs = null;
    EmployeeBankAccountUpdateRs employeeBankAccountUpdateRs = null;
    EmployeeBankAccountDeactivateRs employeeBankAccountDeactivateRs = null;



    // Employee.BankAccount.Add
    public Element add(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"5001", "142", "255", "137", "138", "125", "169", "177", "1101", "168", "178", "164"};

        try {
            PayrollServices.beginUnitOfWork();
            context = new WSServerContext("EmployeeBankAccount", "add");
            //Get the incoming EmployeeBankAccountDTO
            EmployeeBankAccountAdd employeeBankAccountAddDDDTO = (EmployeeBankAccountAdd) context.translateInputElement(requestDoc);

            //Instantiate PSP DTO
            EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();

            //Populate attributes of the employee bank account DTO we'll persist from the incoming employee bank account DTO
            populateAddEmployeeBankAccountFromDTO(employeeBankAccountAddDDDTO, employeeBankAccountDTO);

            //Execute the process flow for adding a new employee bank account, and gather the result and newly created employee bank account afterwards
            ProcessResult<com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount> result =
                    PayrollServices.employeeManager.addEmployeeBankAccount(
                            SourceSystemCode.valueOf(employeeBankAccountAddDDDTO.getSourceSystemCd()),
                            employeeBankAccountAddDDDTO.getCompanyID(), employeeBankAccountAddDDDTO.getEmployeeID(),
                            employeeBankAccountDTO);

            //Need to get the employee bank account from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
            com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount domainEmployeeBankAccount = result.getResult();
            com.intuit.sbd.payroll.psp.domain.Company domainCompany = com.intuit.sbd.payroll.psp.domain.Company.findCompany(
                    employeeBankAccountAddDDDTO.getCompanyID(),
                    SourceSystemCode.valueOf(employeeBankAccountAddDDDTO.getSourceSystemCd()));

            //Build the response to give back to the client
            employeeBankAccountAddRs = (EmployeeBankAccountAddRs) context.getOutputDTO();
            EmployeeBankAccountRet employeeBankAccountRet = employeeBankAccountAddRs.getEmployeeBankAccountRet();

            if (result.isSuccess()) {
                build_EmployeeBankAccountRet(domainEmployeeBankAccount, employeeBankAccountRet);
                employeeBankAccountAddRs.setEmployeeBankAccountRet(employeeBankAccountRet);
                PayrollServices.commitUnitOfWork();
            } else  {
                DDCommon.replacePSPError(result, "1101", "177", domainCompany);
                // Check for message 164 - Employee Bank Account already exists and change the level from ERROR to WARNING
                if (result.getMessages().size() == 1 && result.getMessages().get(0).getMessageCode().equals(EMPLOYEEBANKACCOUNT_ALREADY_EXISTS)) {
                    build_EmployeeBankAccountRet(domainEmployeeBankAccount, employeeBankAccountRet);
                    result.getMessages().get(0).setLevel(MessageInfo.MessageLevel.WARNING);
                } else {
                    employeeBankAccountAddRs.setEmployeeBankAccountRet(null);
                }
                PayrollServices.rollbackUnitOfWork();
            }
            employeeBankAccountAddRs.setResponseStatus(ddCommon.build_ResponseStatus(result, expectedErrorCodes));

            returnDoc = context.translateOutputDTO();
        } catch (WSException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        
        return returnDoc;
    }

    private void populateAddEmployeeBankAccountFromDTO(EmployeeBankAccountAdd pEmployeeBankAccountAddDDDTO, EmployeeBankAccountDTO pEmployeeBankAccountDTO) throws Exception {
        if (pEmployeeBankAccountAddDDDTO != null) {
            pEmployeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccountAddDDDTO.getEmployeeBankAccountID());
            pEmployeeBankAccountDTO.setBankAccount(DDCommon.build_BankAccountDTO(pEmployeeBankAccountAddDDDTO.getBankAccount()));
        } else {
            String args[] = {"EmployeeBankAccountAdd", "build_EmployeeBankAccountAddRs"};
            throw new NullPointerException(ddCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    // Employee.BankAccount.Update

    public Element update(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"5001", "142", "255", "137", "138", "125", "169", "177", "1101","168", "178", "166", "187"};

        try {
            PayrollServices.beginUnitOfWork();
            //Get the incoming EmployeeBankAccountDTO
            context = new WSServerContext("EmployeeBankAccount", "update");
            EmployeeBankAccountUpdate employeeBankAccountUpdateDDDTO = (EmployeeBankAccountUpdate) context.translateInputElement(requestDoc);

            //Instantiate PSP DTO
            EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();

            //Populate attributes of the employee bank account DTO we'll persist from the incoming employee bank account DTO
            populateUpdateEmployeeBankAccountFromDTO(employeeBankAccountUpdateDDDTO, employeeBankAccountDTO);

            //Execute the process flow for updating an employee bank account
            ProcessResult<com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount> result =
                    PayrollServices.employeeManager.updateEmployeeBankAccount(
                            SourceSystemCode.valueOf(employeeBankAccountUpdateDDDTO.getSourceSystemCd()), 
                            employeeBankAccountUpdateDDDTO.getCompanyID(),
                            employeeBankAccountUpdateDDDTO.getEmployeeID(), employeeBankAccountDTO);

            //Need to get the employee bank account from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
            com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount domainEmployeeBankAccount = result.getResult();

            //Build the response to give back to the client
            employeeBankAccountUpdateRs = (EmployeeBankAccountUpdateRs) context.getOutputDTO();
            EmployeeBankAccountRet employeeBankAccountRet = employeeBankAccountUpdateRs.getEmployeeBankAccountRet();

            if (result.isSuccess()) {
                build_EmployeeBankAccountRet(domainEmployeeBankAccount, employeeBankAccountRet);
                employeeBankAccountUpdateRs.setEmployeeBankAccountRet(employeeBankAccountRet);
                PayrollServices.commitUnitOfWork();
            } else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = com.intuit.sbd.payroll.psp.domain.Company.findCompany(
                            employeeBankAccountUpdateDDDTO.getCompanyID(),
                            SourceSystemCode.valueOf(employeeBankAccountUpdateDDDTO.getSourceSystemCd()));
                DDCommon.replacePSPError(result, "1101", "177", domainCompany);

                employeeBankAccountUpdateRs.setEmployeeBankAccountRet(null);
                PayrollServices.rollbackUnitOfWork();
            }

            employeeBankAccountUpdateRs.setResponseStatus(ddCommon.build_ResponseStatus(result, expectedErrorCodes));

            returnDoc = context.translateOutputDTO();

        } catch (WSException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        
        return returnDoc;
    }

    private void populateUpdateEmployeeBankAccountFromDTO(EmployeeBankAccountUpdate pEmployeeBankAccountUpdateDDDTO, EmployeeBankAccountDTO pEmployeeBankAccountDTO) throws Exception {
        if (pEmployeeBankAccountUpdateDDDTO != null) {
            pEmployeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccountUpdateDDDTO.getEmployeeBankAccountID());
            pEmployeeBankAccountDTO.setBankAccount(DDCommon.build_BankAccountDTO(pEmployeeBankAccountUpdateDDDTO.getBankAccount()));
        } else {
            String args[] = {"EmployeeBankAccountUpdate", "build_EmployeeBankAccountAddRs"};
            throw new NullPointerException(ddCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    // Employee.BankAccount.Deactivate

    public Element deactivate(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"5001", "137", "138", "125", "169", "177", "1101", "168", "166", "187"};

        try {
            PayrollServices.beginUnitOfWork();
            //Get the incoming EmployeeBankAccountDTO
            context = new WSServerContext("EmployeeBankAccount", "deactivate");
            EmployeeBankAccountDeactivate employeeBankAccountDeactivateDDDTO = (EmployeeBankAccountDeactivate) context.translateInputElement(requestDoc);

           //Instantiate PSP DTO
            EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();

            //Set Employee Bank Account Id
             employeeBankAccountDTO.setEmployeeBankAccountId(employeeBankAccountDeactivateDDDTO.getEmployeeBankAccountID());

            //Execute the prcoess flow for deactivating the employee bank account
            ProcessResult<com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount> result =
                    PayrollServices.employeeManager.deactivateEmployeeBankAccount(
                            SourceSystemCode.valueOf(employeeBankAccountDeactivateDDDTO.getSourceSystemCd()),
                            employeeBankAccountDeactivateDDDTO.getCompanyID(),
                            employeeBankAccountDeactivateDDDTO.getEmployeeID(), employeeBankAccountDTO);

            //Need to get the employee bank account from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
             com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount domainEmployeeBankAccount = result.getResult();

            //Build the response to give back to the client
            employeeBankAccountDeactivateRs = (EmployeeBankAccountDeactivateRs) context.getOutputDTO();
            EmployeeBankAccountRet employeeBankAccountRet = employeeBankAccountDeactivateRs.getEmployeeBankAccountRet();

            if (result.isSuccess()) {
                build_EmployeeBankAccountRet(domainEmployeeBankAccount, employeeBankAccountRet);
                employeeBankAccountDeactivateRs.setEmployeeBankAccountRet(employeeBankAccountRet);
                PayrollServices.commitUnitOfWork();
            } else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = com.intuit.sbd.payroll.psp.domain.Company.findCompany(
                        employeeBankAccountDeactivateDDDTO.getCompanyID(),
                        SourceSystemCode.valueOf(employeeBankAccountDeactivateDDDTO.getSourceSystemCd()));

                DDCommon.replacePSPError(result, "1101", "177", domainCompany);
                employeeBankAccountDeactivateRs.setEmployeeBankAccountRet(null);
                PayrollServices.rollbackUnitOfWork();
            }

            employeeBankAccountDeactivateRs.setResponseStatus(ddCommon.build_ResponseStatus(result, expectedErrorCodes));

            returnDoc = context.translateOutputDTO();
            returnDoc = context.translateOutputDTO();
        } catch (WSException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        
        return returnDoc;
    }


    public Element historyQuery(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"169"};

        try {
            PayrollServices.beginUnitOfWork();
            context = new WSServerContext("EmployeeBankAccount","historyQuery");
            EmployeeBankAccountHistoryQuery employeeBankAccountHistoryQuery =
                    (EmployeeBankAccountHistoryQuery) context.translateInputElement(requestDoc);
            EmployeeBankAccountHistoryQueryRs employeeBankAccountHistoryQueryRs = (EmployeeBankAccountHistoryQueryRs) context.getOutputDTO();
            ProcessResult processResult = new ProcessResult();

            String sourceSystemCode = employeeBankAccountHistoryQuery.getSourceSystemCd();
            String sourceCompanyId = employeeBankAccountHistoryQuery.getCompanyID();
            String sourceEmployeeBankAccountId = employeeBankAccountHistoryQuery.getEmployeeBankAccountID();

            // Validate whether the company exists or not
            com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));
            if (company == null) {
                processResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCode, sourceCompanyId);
            } else {
                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount> ebaCollection;
                if (sourceEmployeeBankAccountId == null ||
                    sourceEmployeeBankAccountId.length() == 0) {

                    ebaCollection = com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount.findEmployeeBankAccounts
                        (company, employeeBankAccountHistoryQuery.getEmployeeID(), null);
                } else {
                    ebaCollection = com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount.findEmployeeBankAccounts
                        (company,
                         employeeBankAccountHistoryQuery.getEmployeeID(),
                         sourceEmployeeBankAccountId);
                }
                build_EmployeeBankAccountHistoryQueryRs(employeeBankAccountHistoryQueryRs, ebaCollection);
            }
            employeeBankAccountHistoryQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }  catch (WSException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(),e.getCause());
            throw e;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(),e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;

    }



    /**
     * Build Return DTO
     *
     * @param pEmployeeBankAccountDO
     * @param pEmployeeBankAccountRet
     * @return
     * @throws Exception
     */

    private EmployeeBankAccountRet build_EmployeeBankAccountRet(com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount pEmployeeBankAccountDO, EmployeeBankAccountRet pEmployeeBankAccountRet) throws Exception {

        if (pEmployeeBankAccountDO != null) {
            pEmployeeBankAccountRet.setSourceSystemCd
                    (pEmployeeBankAccountDO.getEmployee().getCompany().getSourceSystemCd().toString());
            pEmployeeBankAccountRet.setCompanyID
                    (pEmployeeBankAccountDO.getEmployee().getCompany().getSourceCompanyId());
            pEmployeeBankAccountRet.setEmployeeID
                    (pEmployeeBankAccountDO.getEmployee().getSourceEmployeeId());
            pEmployeeBankAccountRet.setEmployeeBankAccountID
                    (pEmployeeBankAccountDO.getSourceBankAccountId());
            pEmployeeBankAccountRet.setEmployeeBankAccountStatusCd
                    (DDCodeToPSP.getQBOEBankAccountStatus(pEmployeeBankAccountDO.getStatusCd()));
            pEmployeeBankAccountRet.setBankAccount(ddCommon.build_BankAccount(pEmployeeBankAccountDO.getBankAccount()));
        } else {
            String args[] = {"EmployeeBankAccount, build_EmployeeBankAccountRet"};
            throw new NullPointerException(ddCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
        return pEmployeeBankAccountRet;
    }

    private void build_EmployeeBankAccountHistoryQueryRs
            (EmployeeBankAccountHistoryQueryRs employeeBankAccountHistoryQueryRs, DomainEntitySet<com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount> ebaHistoryQueryCollection)
            throws Exception {

        intuit.osp.pse.dd.wsapi.xsd.employeebankaccounthistoryqueryret.ObjectFactory ebaHistoryQueryRetObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.employeebankaccounthistoryqueryret.ObjectFactory();
        EmployeeBankAccountHistoryQueryRet employeeBankAccountHistoryQueryRet;

        if (ebaHistoryQueryCollection != null && ebaHistoryQueryCollection.size() > 0) {
            for (Iterator iterator = ebaHistoryQueryCollection.iterator(); iterator.hasNext();) {
                com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount employeeBankAccount = (com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount) iterator.next();

                employeeBankAccountHistoryQueryRet =
                        ebaHistoryQueryRetObjectFactory.createEmployeeBankAccountHistoryQueryRet();

                employeeBankAccountHistoryQueryRet.setSourceSystemCd
                        (employeeBankAccount.getEmployee().getCompany().getSourceSystemCd().toString());
                employeeBankAccountHistoryQueryRet.setCompanyID
                        (employeeBankAccount.getEmployee().getCompany().getSourceCompanyId());
                employeeBankAccountHistoryQueryRet.setEmployeeID
                        (employeeBankAccount.getEmployee().getSourceEmployeeId());
                employeeBankAccountHistoryQueryRet.setEmployeeBankAccountID
                        (employeeBankAccount.getSourceBankAccountId());
                employeeBankAccountHistoryQueryRet.setEmployeeBankAccountStatusCd
                        (DDCodeToPSP.getQBOEBankAccountStatus(employeeBankAccount.getStatusCd()));
                employeeBankAccountHistoryQueryRet.setUserId(employeeBankAccount.getCreatorId());

                EmployeeInfo ee = DDCommon.build_EmployeeInfo(employeeBankAccount.getEmployee());
                employeeBankAccountHistoryQueryRet.setEmployeeInfo(ee);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(employeeBankAccount.getCreatedDate().toLocal().getTimeInMilliseconds());
                employeeBankAccountHistoryQueryRet.setDate(calendar);

                employeeBankAccountHistoryQueryRet.setBankAccount
                        (DDCommon.build_BankAccount(employeeBankAccount.getBankAccount()));

                employeeBankAccountHistoryQueryRs.getEmployeeBankAccountHistoryQueryRet().add
                        (employeeBankAccountHistoryQueryRet);
            }
        }

    }

}
