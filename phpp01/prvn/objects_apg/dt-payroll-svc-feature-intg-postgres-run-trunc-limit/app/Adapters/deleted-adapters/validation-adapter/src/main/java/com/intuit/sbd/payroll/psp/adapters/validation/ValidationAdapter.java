package com.intuit.sbd.payroll.psp.adapters.validation;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.validation.wsdto.ParameterWSDTO;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameter;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.Fee;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.util.List;
import java.util.ArrayList;

/**
 @author Jeff Jones
 */

@WebService()
public class ValidationAdapter {

    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(ValidationAdapter.class);
    }

    @WebMethod()
    public List<ParameterWSDTO> querySystemParameters() throws Exception {
        List<ParameterWSDTO> parameterList = new ArrayList<ParameterWSDTO>();
        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<SystemParameter> SystemParameters = PayrollServices.entityFinder.find(SystemParameter.class);
            for (SystemParameter systemParameter : SystemParameters) {
                ParameterWSDTO parameterWSDTO = new ParameterWSDTO();

                parameterWSDTO.setCode(systemParameter.getSystemParameterCd());
                parameterWSDTO.setValue(systemParameter.getSystemParameterValue());

                parameterList.add(parameterWSDTO);
            }
        } catch (Exception e) {
            logger.error(e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return parameterList;
    }

    @WebMethod()
    public List<ParameterWSDTO> querySourcePayrollParameters(@WebParam(name = "sourceSystemCD")String pSourceSystemCD) throws Exception {
        List<ParameterWSDTO> parameterList = new ArrayList<ParameterWSDTO>();

        if (pSourceSystemCD == null || pSourceSystemCD.length() == 0) {
            throw new RuntimeException(
                    "Invalid source system code '" + pSourceSystemCD + "'.");
        }

        try {
            PayrollServices.beginUnitOfWork();

            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf(pSourceSystemCD);

            DomainEntitySet<SourcePayrollParameter> sourcePayrollParameters =
                    PayrollServices.entityFinder.find(SourcePayrollParameter.class);
            for (SourcePayrollParameter sourcePayrollParameter : sourcePayrollParameters) {
                if (sourcePayrollParameter.getSourceSystemCd().equals(sourceSystemCode)) {
                    ParameterWSDTO parameterWSDTO = new ParameterWSDTO();

                    parameterWSDTO.setCode(sourcePayrollParameter.getParameterCd().toString());
                    parameterWSDTO.setValue(sourcePayrollParameter.getParameterValue());

                    parameterList.add(parameterWSDTO);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return parameterList;
    }

    @WebMethod()
    public List<ParameterWSDTO> queryFees() throws Exception {
        List<ParameterWSDTO> parameterList = new ArrayList<ParameterWSDTO>();

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Fee> fees =
                    PayrollServices.entityFinder.find(Fee.class);
            for (Fee fee : fees) {
                ParameterWSDTO parameterWSDTO = new ParameterWSDTO();

                parameterWSDTO.setCode(fee.getFeeCd().toString());
                parameterWSDTO.setValue(fee.getAmount().toString());

                parameterList.add(parameterWSDTO);
            }
        } catch (Exception e) {
            logger.error(e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return parameterList;
    }
}
