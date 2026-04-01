package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.SourcePayrollParameterDTO;
import com.intuit.sbd.payroll.psp.cache.DirtyCheckProcessCache;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameter;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameterCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

import java.util.List;

/**
 *
 * User: kpaul
 * Date: Nov 27, 2007
 * Time: 10:43:36 AM

 */
public class UpdateSourcePayrollParameterCore extends Process implements IProcess {
    SourceSystemCode sourceSystemCd = null;
    List<SourcePayrollParameterDTO> dtoParamList = null;

    public UpdateSourcePayrollParameterCore(SourceSystemCode pSourceSystemCd, List<SourcePayrollParameterDTO> pParams) {
        sourceSystemCd = pSourceSystemCd;
        dtoParamList = pParams;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // verify the source system code isn't null or enpty
        if (sourceSystemCd == null) {
            validationResult.getMessages().SourceSystemCdNotSpecified(EntityName.SourcePayrollParameter, null);
            return validationResult;
        }

        // verify the source system parameters are valid
        for (SourcePayrollParameterDTO dto : dtoParamList) {
            validationResult.merge(dto.validate());
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        SourcePayrollParameter param;

        for (SourcePayrollParameterDTO dto : dtoParamList) {
            //get the _cached_ object
            param = SourcePayrollParameter.findSourcePayrollParameter(sourceSystemCd,
                    SourcePayrollParameterCode.valueOf(dto.getParameterCd().toString()));

            //update the _cached_ object
            param.setParameterValue(dto.getParameterValue());

            //we must also get the persistent object
            param = Application.findById(SourcePayrollParameter.class, param.getId());

            //and update that
            param.setParameterValue(dto.getParameterValue());

            Application.save(param);

        }

        // invalidate caches
        DirtyCheckProcessCache.updateDBCacheTokenValue();

        Expression<SourcePayrollParameter> query =
                new Query<SourcePayrollParameter>()
                        .Where(SourcePayrollParameter.SourceSystemCd().equalTo(sourceSystemCd))
                        .OrderBy(SourcePayrollParameter.ParameterCd());


        DomainEntitySet<SourcePayrollParameter> sourceParams = Application.find(SourcePayrollParameter.class, query);

        processResult.setResult(sourceParams);
        return processResult;
    }
}
