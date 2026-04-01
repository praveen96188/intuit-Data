package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.common.wsf.server.WSServerContext;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountadd.CompanyBankAccountAdd;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountaddrs.CompanyBankAccountAddRs;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountdeactivate.CompanyBankAccountDeactivate;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountdeactivaters.CompanyBankAccountDeactivateRs;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountquery.CompanyBankAccountQuery;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountqueryrs.CompanyBankAccountQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountret.CompanyBankAccountRet;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountstatushistoryquery.CompanyBankAccountStatusHistoryQuery;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountstatushistoryqueryrs.CompanyBankAccountStatusHistoryQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountstatushistoryret.CompanyBankAccountStatusHistoryRet;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountupdate.CompanyBankAccountUpdate;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountupdaters.CompanyBankAccountUpdateRs;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountverify.CompanyBankAccountVerify;
import intuit.osp.pse.dd.wsapi.xsd.companybankaccountverifyrs.CompanyBankAccountVerifyRs;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.util.*;


/**
 * User: mvillani
 * Date: Aug 6, 2007
 * Time: 1:53:03 PM
 */
public final class CompanyBankAccount extends WS {

    private static SpcfLogger logger = Application.getLogger(CompanyBankAccount.class);

    public static final String SERVICE_NAME = "CompanyBankAccount";
    public static final String COMPANYBANKACCOUNT_ALREADY_EXISTS = "162";


    WSServerContext context = null;
    CompanyBankAccountAddRs companyBankAccountAddRs = null;
    CompanyBankAccountUpdateRs companyBankAccountUpdateRs = null;
    CompanyBankAccountVerifyRs companyBankAccountVerifyRs = null;
    CompanyBankAccountDeactivateRs companyBankAccountDeactivateRs = null;

    public Element add(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"121","5001","255","142","137","138","125","169", "177","162","1061"};

        try {
            PayrollServices.beginUnitOfWork();
            //Get the incoming companyBankAccountDTO
            context = new WSServerContext("CompanyBankAccount", "add");

            CompanyBankAccountAdd companyBankAccountAddXML = (CompanyBankAccountAdd) context.translateInputElement(requestDoc);

            // Instantiate PSP DTO
            CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();

            //Populate attributes of the company bank account Add  DTO we'll persist from the incoming DD DTO
            populateCompanyBankAccountFromDTO(companyBankAccountAddXML, companyBankAccountDTO);

            //Execute the process flow for adding a new company bank account, and gather the processResult and newly created company bank account afterwards
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult =
                    PayrollServices.companyManager.addCompanyBankAccount(
                            SourceSystemCode.valueOf(companyBankAccountAddXML.getSourceSystemCd()),
                            companyBankAccountAddXML.getCompanyID(), companyBankAccountDTO, true, true);

            //Need to get the company bank account from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
            com.intuit.sbd.payroll.psp.domain.CompanyBankAccount domainCompanyBankAccount = processResult.getResult();
            com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                    companyBankAccountAddXML.getCompanyID(),
                    SourceSystemCode.valueOf(companyBankAccountAddXML.getSourceSystemCd()));

            //Build the response to give back to the client
            companyBankAccountAddRs = (CompanyBankAccountAddRs) context.getOutputDTO();
            CompanyBankAccountRet companyBankAccountRet = companyBankAccountAddRs.getCompanyBankAccountRet();

            if (processResult.isSuccess()) {
                build_CompanyBankAccountRet(domainCompanyBankAccount, companyBankAccountRet);
                companyBankAccountAddRs.setCompanyBankAccountRet(companyBankAccountRet);
                PayrollServices.commitUnitOfWork();
            } else {
                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
                // Check for message 162 - Company Bank Account already exists and change the level from ERROR to WARNING
                if (processResult.getMessages().size() == 1 && processResult.getMessages().get(0).getMessageCode().equals(COMPANYBANKACCOUNT_ALREADY_EXISTS)) {
                    build_CompanyBankAccountRet(domainCompanyBankAccount, companyBankAccountRet);
                    processResult.getMessages().get(0).setLevel(MessageInfo.MessageLevel.WARNING);
                } else {
                    companyBankAccountAddRs.setCompanyBankAccountRet(null);
                }
                PayrollServices.rollbackUnitOfWork();
            }

            companyBankAccountAddRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            returnDoc = context.translateOutputDTO();

        } catch (WSValidationException e) {
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


    private void populateCompanyBankAccountFromDTO(CompanyBankAccountAdd pCompanyBankAccountAddXML, CompanyBankAccountDTO pCompanyBankAccountDTO) throws Exception {
        if (pCompanyBankAccountDTO != null) {
            build_CompanyBankAccountAddDTO(pCompanyBankAccountAddXML, pCompanyBankAccountDTO);
        } else {
            String args[] = {"CompanyBankAccountAdd", "build_CompanyBankAccountAddRs"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    private void build_CompanyBankAccountAddDTO(CompanyBankAccountAdd pCompanyBankAccountAddXML, CompanyBankAccountDTO pCompanyBankAccountDTO) throws Exception {
        if (pCompanyBankAccountAddXML != null) {
            pCompanyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccountAddXML.getCompanyBankAccountID());

            // Builds a Bank Account DTO and assign it to the CompanyBankAccount DTO
            pCompanyBankAccountDTO.setBankAccountDTO(DDCommon.build_BankAccountDTO(pCompanyBankAccountAddXML.getBankAccount()));
        } else {
            String args[] = {"CompanyBankAccountAdd", "build_CompanyDO"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }


    public Element update(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"5001","255","142","137","138","125","169","177","186","170","202"};

        try {
            PayrollServices.beginUnitOfWork();
            //Get the incoming companyBankAccountDTO
            context = new WSServerContext("CompanyBankAccount", "update");

            CompanyBankAccountUpdate companyBankAccountUpdateXML = (CompanyBankAccountUpdate) context.translateInputElement(requestDoc);

            // Instantiate PSP DTO
            CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();

            //Populate attributes of the company bank account DO we'll persist from the incoming company DTO
            populateUpdateCompanyBankAccountFromDTO(companyBankAccountUpdateXML, companyBankAccountDTO);

            //Execute the prcoess flow for updating a company bank account
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult =
                    PayrollServices.companyManager.updateCompanyBankAccount(
                            SourceSystemCode.valueOf(companyBankAccountUpdateXML.getSourceSystemCd()),
                            companyBankAccountUpdateXML.getCompanyID(), companyBankAccountDTO);

            //Need to get the company bank account from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
            com.intuit.sbd.payroll.psp.domain.CompanyBankAccount domainCompanyBankAccount = processResult.getResult();

            //Build the response to give back to the client
            companyBankAccountUpdateRs = (CompanyBankAccountUpdateRs) context.getOutputDTO();
            CompanyBankAccountRet companyBankAccountRet = companyBankAccountUpdateRs.getCompanyBankAccountRet();

            if (processResult.isSuccess()) {
                build_CompanyBankAccountRet(domainCompanyBankAccount, companyBankAccountRet);
                companyBankAccountUpdateRs.setCompanyBankAccountRet(companyBankAccountRet);
                PayrollServices.commitUnitOfWork();
            } else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        companyBankAccountUpdateXML.getCompanyID(),
                        SourceSystemCode.valueOf(companyBankAccountUpdateXML.getSourceSystemCd()));
                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
                
                companyBankAccountUpdateRs.setCompanyBankAccountRet(null);
                PayrollServices.rollbackUnitOfWork();
            }

            companyBankAccountUpdateRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();

        } catch (WSValidationException e) {
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

    private void populateUpdateCompanyBankAccountFromDTO(CompanyBankAccountUpdate pCompanyBankAccountUpdateXML, CompanyBankAccountDTO pDomainCompanyBankAccountDTO) throws Exception {
        if (pCompanyBankAccountUpdateXML != null) {
            build_UpdateCompanyBankAccountDO(pCompanyBankAccountUpdateXML, pDomainCompanyBankAccountDTO);
        } else {
            String args[] = {"CompanyBankAccountUpdate", "build_CompanyBankAccountUpdateRs"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    private void build_UpdateCompanyBankAccountDO(CompanyBankAccountUpdate pCompanyBankAccountUpdateXML, CompanyBankAccountDTO pCompanyBankAccountDTO) throws Exception {
        if (pCompanyBankAccountUpdateXML != null) {
            pCompanyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccountUpdateXML.getCompanyBankAccountID());

            // Builds a Bank Account DTO and assign it to the CompanyBankAccount DTO
            pCompanyBankAccountDTO.setBankAccountDTO(DDCommon.build_BankAccountDTO(pCompanyBankAccountUpdateXML.getBankAccount()));
        } else {
            String args[] = {"CompanyBankAccountUpdate", "build_UpdateCompanyBankAccountDO"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }


    public Element verify(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"137","138","125","169","177","170","250","188","189","197","205","208","204","190"};

        try {
            PayrollServices.beginUnitOfWork();
            Collection transactionAmountsToVerify = null;

            //Get the incoming companyBankAccountDTO
            context = new WSServerContext("CompanyBankAccount", "verify");

            CompanyBankAccountVerify companyBankAccountVerify = (CompanyBankAccountVerify) context.translateInputElement(requestDoc);

            List<BigDecimal> verifyTxns = companyBankAccountVerify.getVerifyTxAmt();
            SpcfMoney txnToVerify1 = SpcfUtils.convertToSpcfMoney(verifyTxns.get(0));
            SpcfMoney txnToVerify2 = SpcfUtils.convertToSpcfMoney(verifyTxns.get(1));

            //Execute the prcoess flow for verifying the transaction amounts, and gather the processResult and newly created company bank account afterwards
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(
                    SourceSystemCode.valueOf(companyBankAccountVerify.getSourceSystemCd()),
                    companyBankAccountVerify.getCompanyID(),
                    companyBankAccountVerify.getCompanyBankAccountID(), txnToVerify1, txnToVerify2, false);

            //Need to get the company bank account from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
            com.intuit.sbd.payroll.psp.domain.CompanyBankAccount domainCompanyBankAccount = processResult.getResult();

            //Build the response to give back to the client
            companyBankAccountVerifyRs = (CompanyBankAccountVerifyRs) context.getOutputDTO();
            CompanyBankAccountRet companyBankAccountRet = companyBankAccountVerifyRs.getCompanyBankAccountRet();

            if (processResult.isSuccess()) {
                build_CompanyBankAccountRet(domainCompanyBankAccount, companyBankAccountRet);
                companyBankAccountVerifyRs.setCompanyBankAccountRet(companyBankAccountRet);
                PayrollServices.commitUnitOfWork();
            } else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        companyBankAccountVerify.getCompanyID(),
                        SourceSystemCode.valueOf(companyBankAccountVerify.getSourceSystemCd()));
                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);

                companyBankAccountVerifyRs.setCompanyBankAccountRet(null);
                PayrollServices.commitUnitOfWork();
            }

            companyBankAccountVerifyRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();

        } catch (WSValidationException e) {
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

    public Element deactivate(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"137","138","125","169","217","170","218","219","226","177"};
        try {
            PayrollServices.beginUnitOfWork();
            //Get the incoming companyBankAccountDTO
            context = new WSServerContext("CompanyBankAccount", "deactivate");
            CompanyBankAccountDeactivate companyBankAccountDeactivate = (CompanyBankAccountDeactivate) context.translateInputElement(requestDoc);

            //Execute the prcoess flow for verifying the transaction amounts, and gather the processResult and newly created company bank account afterwards
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult =
                    PayrollServices.companyManager.deactivateCompanyBankAccount(
                            SourceSystemCode.valueOf(companyBankAccountDeactivate.getSourceSystemCd()), 
                            companyBankAccountDeactivate.getCompanyID(),
                            companyBankAccountDeactivate.getCompanyBankAccountID(),
                            false, false);

            //Need to get the company bank account from the process flow because SPCF follows JSR-220, which states that the original Entity is not modified
            com.intuit.sbd.payroll.psp.domain.CompanyBankAccount domainCompanyBankAccount = processResult.getResult();

            //Build the response to give back to the client
            companyBankAccountDeactivateRs = (CompanyBankAccountDeactivateRs) context.getOutputDTO();
            CompanyBankAccountRet companyBankAccountRet = companyBankAccountDeactivateRs.getCompanyBankAccountRet();

            if (processResult.isSuccess()) {
                build_CompanyBankAccountRet(domainCompanyBankAccount, companyBankAccountRet);
                companyBankAccountDeactivateRs.setCompanyBankAccountRet(companyBankAccountRet);
                PayrollServices.commitUnitOfWork();
            } else {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                            companyBankAccountDeactivate.getCompanyID(),
                            SourceSystemCode.valueOf(companyBankAccountDeactivate.getSourceSystemCd()));

                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
                companyBankAccountDeactivateRs.setCompanyBankAccountRet(null);
                PayrollServices.rollbackUnitOfWork();
            }

            companyBankAccountDeactivateRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();

        } catch (WSValidationException e) {
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


    protected void build_CompanyBankAccountRet(com.intuit.sbd.payroll.psp.domain.CompanyBankAccount pCompanyBankAccountDO, CompanyBankAccountRet pCompanyBankAccountRet) throws Exception {

        if (pCompanyBankAccountDO != null) {
            pCompanyBankAccountRet.setCompanyBankAccountID(pCompanyBankAccountDO.getSourceBankAccountId());
            if (pCompanyBankAccountDO.getStatusCd() != null) {
                String compBankAccountAPIStatus = DDCodeToPSP.getQBOEBankAccountStatus(pCompanyBankAccountDO.getStatusCd());
                pCompanyBankAccountRet.setCompanyBankAccountStatusCd(compBankAccountAPIStatus);
            }
            Company company = pCompanyBankAccountDO.getCompany();
            pCompanyBankAccountRet.setCompanyID(company.getSourceCompanyId());
            pCompanyBankAccountRet.setSourceSystemCd(
                                pCompanyBankAccountDO.getCompany().getSourceSystemCd().toString());
            long verificationRetryCount = (long) pCompanyBankAccountDO.getVerifyRetryCount();
            pCompanyBankAccountRet.setVerificationRetryCount((int) verificationRetryCount);
            pCompanyBankAccountRet.setBankAccount(DDCommon.build_BankAccount(pCompanyBankAccountDO.getBankAccount()));

        } else {
            String args[] = {"CompanyBankAccount", "build_CompanyBankAccountRet"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
    }

    /**
     * This method processes Company Bank Account Query request.
     *
     * @param request
     * @return
     * @throws intuit.osp.common.wsf.base.WSException
     *
     */
    public Element query(Element request) throws WSException {
        String[] expectedErrorCodes = {"169"};
        try {
            intuit.osp.common.wsf.server.WSServerContext context = new WSServerContext("CompanyBankAccount", "query");
            CompanyBankAccountQuery queryRequest = (CompanyBankAccountQuery) context.translateInputElement(request);
            CompanyBankAccountQueryRs queryResponse = (CompanyBankAccountQueryRs) context.getOutputDTO();

            doQuery(queryRequest, queryResponse, expectedErrorCodes);

            Element responseDoc = context.translateOutputDTO();
            return responseDoc;
        } catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void doQuery(
            CompanyBankAccountQuery pRequest,
            CompanyBankAccountQueryRs pResponse,
            String[] pExpectedErrorCodes) throws JAXBException, Exception {
        ProcessResult result = new ProcessResult();

        PayrollServices.beginUnitOfWork();
        try {
            String srcSystemCd = pRequest.getSourceSystemCd();
            String srcCompanyId = pRequest.getCompanyID();
            List<String> targetStatusCodes = pRequest.getCompanyBankAccountStatusCd();

            // make sure the company exists
            com.intuit.sbd.payroll.psp.domain.Company domainCompany =
                    Company.findCompany(srcCompanyId, SourceSystemCode.valueOf(srcSystemCd));
            if (domainCompany == null) // if no such Company
            {
                result.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, srcCompanyId, srcSystemCd, srcCompanyId);
                logger.warn(result.getMessages().get(result.getMessages().size() - 1).getMessage());
            } else // Company exists
            {
                // get the CompanyBankAccount objects for this company by explicit query to make order
                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> allCBAs = com.intuit.sbd.payroll.psp.domain.CompanyBankAccount.findCompanyBankAccounts(domainCompany);
//                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> allCBAs = domainCompany.getCompanyBankAccountCollection();
                if (allCBAs != null) {
                    intuit.osp.pse.dd.wsapi.xsd.companybankaccountret.ObjectFactory factory =
                            new intuit.osp.pse.dd.wsapi.xsd.companybankaccountret.ObjectFactory();

                    // scan the list, looking for any that match the optional status criteria
                    for (com.intuit.sbd.payroll.psp.domain.CompanyBankAccount domainCBA : allCBAs) {
                        String mappedStatusCd = DDCodeToPSP.getQBOEBankAccountStatus(domainCBA.getStatusCd());

                        boolean noStatusCriteria = (targetStatusCodes == null || targetStatusCodes.size() == 0);
                        if (noStatusCriteria || targetStatusCodes.contains(mappedStatusCd)) {
                            CompanyBankAccountRet xmlCBA = factory.createCompanyBankAccountRet();
                            build_CompanyBankAccountRet(domainCBA, xmlCBA);
                            pResponse.getCompanyBankAccountRet().add(xmlCBA);
                        }
                    }
                }

                if (logger.isInfoEnabled()) {
                    String statuses = "";
                    if (targetStatusCodes != null) {
                        statuses = " and statuses ( ";
                        for (String stat : targetStatusCodes) {
                            statuses += stat + " ";
                        }
                        statuses += ")";
                    }
                    logger.info("search for " + srcSystemCd + ":" + srcCompanyId + statuses +
                            " returns " + pResponse.getCompanyBankAccountRet().size() + " results");
                }
            }

            pResponse.setResponseStatus(DDCommon.build_ResponseStatus(result, pExpectedErrorCodes));

        } finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    /**
     * The logic relies on the fact that all records are sorted ascending.
     * This way the initial record is always based on the company bank account found,
     * which provides the starting status value.
     * At the end, the resulting list of Retsis simply reversed.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element queryStatusHistory(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"169","170"};

        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext wsServerContext = new WSServerContext(CompanyBankAccount.SERVICE_NAME, CompanyBankAccount.Operations.QUERY_STATUS_HISTORY);
            CompanyBankAccountStatusHistoryQuery companyBankAccountStatusHistoryQuery =
                    (CompanyBankAccountStatusHistoryQuery) wsServerContext.translateInputElement(requestDocument);

            CompanyBankAccountStatusHistoryQueryRs companyBankAccountStatusHistoryQueryRs = (CompanyBankAccountStatusHistoryQueryRs) wsServerContext.getOutputDTO();
            ProcessResult processResult = new ProcessResult();

            DomainEntitySet<PropertyAudit> propertyAuditCollection = null;
            
            if (companyBankAccountStatusHistoryQuery != null) {
                String sourceSystemCode = companyBankAccountStatusHistoryQuery.getSourceSystemCd();
                String sourceCompanyId = companyBankAccountStatusHistoryQuery.getCompanyID();
                String sourceCompanyBankAccountId = companyBankAccountStatusHistoryQuery.getCompanyBankAccountID();

                com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));

                if (company == null) {
                    processResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCode, sourceCompanyId);
                } else {
                    DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> companyBankAccounts =
                        com.intuit.sbd.payroll.psp.domain.CompanyBankAccount.findCompanyBankAccountsIncludingExpired(company, sourceCompanyBankAccountId);

                    if (companyBankAccounts == null || companyBankAccounts.size() == 0) {
                        /*
                         * per security advisory do not log bank id
                         * CompanyBankAccount.logger.info("Company Bank Account Business Object with source system id "
                         *      + sourceSystemCode + " , source company id " + sourceCompanyId
                         *       + ", and source company bank account id " + sourceCompanyBankAccountId + " does not exist");
                         */
                        processResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount, sourceCompanyBankAccountId,
                                sourceCompanyBankAccountId, sourceSystemCode, sourceCompanyId);
                    } else {

                        // Get a collection of Audit Columns.
                        propertyAuditCollection =
                                PropertyAudit.findCompanyBankAccountPropertyAudits(company, sourceCompanyBankAccountId);
                    }


                    List retList = companyBankAccountStatusHistoryQueryRs.getCompanyBankAccountStatusHistoryRet();
                    intuit.osp.pse.dd.wsapi.xsd.companybankaccountstatushistoryret.ObjectFactory objectFactory =
                            new intuit.osp.pse.dd.wsapi.xsd.companybankaccountstatushistoryret.ObjectFactory();
                    CompanyBankAccountStatusHistoryRet companyBankAccountStatusHistoryRet = null;

                    String statusCode = null, lastStatusCode = "", userId = null, statusCodeString = null;
                    Date changeDate = null;
                    Calendar calendar = null;

                    if (propertyAuditCollection != null && propertyAuditCollection.size() > 0) {
                        // Get the very first status from the first property audit's old value
                        companyBankAccountStatusHistoryRet =
                                        createCBAStatusReturn (sourceSystemCode, sourceCompanyId,
                                                sourceCompanyBankAccountId, propertyAuditCollection.get(0),
                                                propertyAuditCollection.get(0).getOldPropertyValue());
                        retList.add(companyBankAccountStatusHistoryRet);

                        for (PropertyAudit propertyAudit : propertyAuditCollection) {

                            if ((statusCode = propertyAudit.getNewPropertyValue()) == null) {
                                statusCode = "";
                            }

                            if (!statusCode.equals(lastStatusCode)) {
                                companyBankAccountStatusHistoryRet =
                                        createCBAStatusReturn (sourceSystemCode, sourceCompanyId,
                                                sourceCompanyBankAccountId, propertyAudit, propertyAudit.getNewPropertyValue());
                                retList.add(companyBankAccountStatusHistoryRet);

                                lastStatusCode = statusCode;
                            }
                        }

                    }
                    Collections.reverse(retList);
                }
            }

            companyBankAccountStatusHistoryQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            CompanyBankAccount.logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            CompanyBankAccount.logger.error(exception.getMessage(), exception);
            throw new WSException(DDCommon.pse_Error, exception);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnDoc;
    }

    private CompanyBankAccountStatusHistoryRet createCBAStatusReturn(String pSourceSystemCode,
                                                                     String pSourceCompanyId,
                                                                     String pSourceCompanyBankAccountId,
                                                                     PropertyAudit pPropertyAudit,
                                                                     String pStatusCode) throws Exception {

        intuit.osp.pse.dd.wsapi.xsd.companybankaccountstatushistoryret.ObjectFactory objectFactory =
                            new intuit.osp.pse.dd.wsapi.xsd.companybankaccountstatushistoryret.ObjectFactory();
        CompanyBankAccountStatusHistoryRet companyBankAccountStatusHistoryRet = objectFactory.createCompanyBankAccountStatusHistoryRet();
        Date changeDate = CalendarUtils.convertToDate(pPropertyAudit.getAuditDate().toLocal());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(changeDate);
        companyBankAccountStatusHistoryRet.setDateTime(calendar);
        String statusCode = pStatusCode, userId = null, statusCodeString = null;
        if (statusCode == null) {
            statusCode = "";
        }
        BankAccountStatus bca = BankAccountStatus.valueOf(statusCode);
        companyBankAccountStatusHistoryRet.setCompanyBankAccountStatusCd(DDCodeToPSP.getQBOEBankAccountStatus(bca));
        statusCodeString = "Active";
        if (BankAccountStatus.Inactive.toString().equals(statusCode)) {
            statusCodeString = "Inactive";
        } else if (BankAccountStatus.PendingVerification.toString().equals(statusCode)) {
            statusCodeString = "Pending Verification";
        }

        companyBankAccountStatusHistoryRet.setCompanyBankAccountStatusName(statusCodeString);
        userId = pPropertyAudit.getUserId();
        if (userId == null) {
            userId = "";
        }
        companyBankAccountStatusHistoryRet.setUserID(userId);
        companyBankAccountStatusHistoryRet.setSourceSystemCd(pSourceSystemCode);
        companyBankAccountStatusHistoryRet.setCompanyID(pSourceCompanyId);
        companyBankAccountStatusHistoryRet.setCompanyBankAccountID(pSourceCompanyBankAccountId);

        return companyBankAccountStatusHistoryRet;
    }

    /**
     * Interface to store names of Company Bank Account operations.
     */
    public interface Operations {
        public static final String QUERY_STATUS_HISTORY = "queryStatusHistory";
        public static final String ADD = "add";
        public static final String DEACTIVATE = "deactivate";
        public static final String QUERY = "query";
        public static final String UPDATE = "update";
        public static final String VERIFY = "verify";
    }
}

