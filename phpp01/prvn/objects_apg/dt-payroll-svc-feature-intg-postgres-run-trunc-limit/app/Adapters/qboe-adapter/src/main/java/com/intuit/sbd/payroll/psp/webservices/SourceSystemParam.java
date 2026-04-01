/*********************************************************************************
 * Copyright Statement: CONFIDENTIAL - Copyright 2004 Intuit Inc.
 * This material contains certain trade secrets and confidential and proprietary
 * information of Intuit Inc. Use, reproduction, disclosure and distribution by any
 * means are prohibited, except pursuant to a written license from Intuit Inc. Use of
 * copyright notice is precautionary and does not imply publication or disclosure.
 *********************************************************************************/
package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.api.dtos.SourcePayrollParameterDTO;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.sourcesystemparaminfo.SourceSystemParamInfo;
import intuit.osp.pse.dd.wsapi.xsd.sourcesystemparamquery.SourceSystemParamQuery;
import intuit.osp.pse.dd.wsapi.xsd.sourcesystemparamqueryrs.SourceSystemParamQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.sourcesystemparamret.SourceSystemParamRet;
import intuit.osp.pse.dd.wsapi.xsd.sourcesystemparamupdate.SourceSystemParamUpdate;
import intuit.osp.pse.dd.wsapi.xsd.sourcesystemparamupdaters.SourceSystemParamUpdateRs;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Vector;


/**
 * <p/>
 * File: $Id: //psp/dev/Adapters/QBOE/src/com/intuit/sbd/payroll/psp/webservices/SourceSystemParam.java#1 $
 * <p/>
 * Class: intuit.osp.pse.dd.wsimpl.SourceSystemParam
 * @author rshenderovsky, kpaul
 */
public class SourceSystemParam extends WS {
    private static SpcfLogger logger = Application.getLogger(SourceSystemParam.class);
    public static final String SERVICE_NAME = "SourceSystemParam";

    public interface Operations {
        static final String QUERY = "query";
        static final String UPDATE = "update";
    }

    public Element query(Element requestDoc) throws WSException {
        Element responseDoc;

        try {
            // Get a server context for this request
            WSServerContext context = new WSServerContext(SERVICE_NAME, Operations.QUERY);

            // Translate the incoming xml request document into its associated jaxb object
            SourceSystemParamQuery requestXml = (SourceSystemParamQuery) context.translateInputElement(requestDoc);

            // Retrieve the (empty) response jaxb object from the context
            SourceSystemParamQueryRs responseXml = (SourceSystemParamQueryRs) context.getOutputDTO();

            PayrollServices.beginUnitOfWork();
            try {
                // Get the domain object for the given source payroll system
                SourceSystemCode sourceSystemCd = SourceSystemCode.valueOf(requestXml.getSourceSystemCd());

                // Retrieve the entire list of source payroll parameters for the given source payroll system
/*                DomainEntitySet<SourcePayrollParameter> sourceParams = PayrollServices.entityFinder.find(SourcePayrollParameter.class,
                        SourcePayrollParameter.SourceSystemCd().equalTo(sourceSystemCd));*/

        Expression<SourcePayrollParameter> query =
                new Query<SourcePayrollParameter>()
                        .Where(SourcePayrollParameter.SourceSystemCd().equalTo(sourceSystemCd))
                        .OrderBy(SourcePayrollParameter.ParameterCd());


        DomainEntitySet<SourcePayrollParameter> sourceParams = Application.find(SourcePayrollParameter.class, query);

                // Build the response to return to the client
                buildResponse(responseXml.getSourceSystemParamRet(), sourceParams);

                PayrollServices.commitUnitOfWork();
            } catch (Exception e) {
                PayrollServices.rollbackUnitOfWork();
                throw e;
            }

            responseXml.setResponseStatus(DDCommon.SUCCESS);

            responseDoc = context.translateOutputDTO();
        } catch (WSValidationException e) {
            logger.info(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return responseDoc;
    }

	public Element update(Element requestDoc) throws WSException {
        Element responseDoc;
        String[] expectedErrorCodes = {"270", "137", "125", "11"};

		try {
            ProcessResult<DomainEntitySet<SourcePayrollParameter>> processResult =
                    new ProcessResult<DomainEntitySet<SourcePayrollParameter>>();

            // Get a server context for this request
            WSServerContext context = new WSServerContext(SERVICE_NAME, Operations.UPDATE);

            // Translate the incoming xml request document into its associated jaxb object
            SourceSystemParamUpdate requestXml = (SourceSystemParamUpdate) context.translateInputElement(requestDoc);

            // Retrieve the (empty) response jaxb object from the context
            SourceSystemParamUpdateRs responseXml = (SourceSystemParamUpdateRs) context.getOutputDTO();

            // Create a list of DTO objects to be populated by the request list
            List<SourcePayrollParameterDTO> dtoList =
                    new Vector<SourcePayrollParameterDTO>(requestXml.getSourceSystemParamInfo().size());

            // Convert the request jaxb object list into a dto list for use by the core process
            processResult.merge(buildRequestDTOList(requestXml, dtoList));

            // if the conversion was successful, proceed with the update
            if (processResult.isSuccess()) {
                PayrollServices.beginUnitOfWork();
                try {
                    // Call the psp api (core process) to update the source payroll parameters
                    // (don't merge here since the 'result' won't be part of the merge)
                    processResult = PayrollServices.payrollManager.
                        updateSourcePayrollParameter(SourceSystemCode.valueOf(requestXml.getSourceSystemCd()), dtoList);

                    PayrollServices.commitUnitOfWork();
                } catch (Exception e) {
                    PayrollServices.rollbackUnitOfWork();
                    throw e;
                }

                // Build the response to return to the client
                if (processResult.isSuccess()) {
                    buildResponse(responseXml.getSourceSystemParamRet(), processResult.getResult());
                } else {
                    responseXml.setSourceSystemParamRet(null);
                }
            }

            responseXml.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            responseDoc = context.translateOutputDTO();
        } catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return responseDoc;
    }

    private static void buildResponse(SourceSystemParamRet pSourceSystemParameRet,
                                         DomainEntitySet<SourcePayrollParameter> pSourceParams) throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.sourcesystemparaminfo.ObjectFactory sourceParamInfoObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.sourcesystemparaminfo.ObjectFactory();
        SourceSystemParamInfo sourceSystemParamInfo;
        SourcePayrollParameterCode paramCode;

        for (SourcePayrollParameter param : pSourceParams) {
            paramCode = SourcePayrollParameterCode.valueOf(param.getParameterCd().toString());
            sourceSystemParamInfo = sourceParamInfoObjectFactory.createSourceSystemParamInfo();
            sourceSystemParamInfo.setParamName(param.getName());
            sourceSystemParamInfo.setParamDesc(param.getDescription());
            sourceSystemParamInfo.setParamCd(SourcePayrollParameterCodeEnum.toQBOECode(paramCode));
            sourceSystemParamInfo.setParamValue(param.getParameterValue());
            pSourceSystemParameRet.getSourceSystemParamInfo().add(sourceSystemParamInfo);
        }
	}

	private static ProcessResult buildRequestDTOList(SourceSystemParamUpdate request,
                                                     List<SourcePayrollParameterDTO> dtoList) {
        ProcessResult processResult = new ProcessResult();
        SourceSystemParamInfo paramInfo;
        SourcePayrollParameterCodeEnum paramCode;

        for (Object param : request.getSourceSystemParamInfo()) {
            paramInfo = (SourceSystemParamInfo) param;

            try {
                paramCode = SourcePayrollParameterCodeEnum.valueOf(paramInfo.getParamCd());

                dtoList.add(new SourcePayrollParameterDTO(
                        SourceSystemCode.valueOf(request.getSourceSystemCd()),
                        paramInfo.getParamName(),
                        paramInfo.getParamDesc(),
                        paramCode.toPspCode(),
                        paramInfo.getParamValue()));
            } catch (IllegalArgumentException e) {
                processResult.getMessages().SourcePayrollParameterDoesNotExist(
                        EntityName.SourcePayrollParameter,
                        request.getSourceSystemCd(),
                        request.getSourceSystemCd(),
                        paramInfo.getParamCd());
            }
        }

        return processResult;
    }
}
