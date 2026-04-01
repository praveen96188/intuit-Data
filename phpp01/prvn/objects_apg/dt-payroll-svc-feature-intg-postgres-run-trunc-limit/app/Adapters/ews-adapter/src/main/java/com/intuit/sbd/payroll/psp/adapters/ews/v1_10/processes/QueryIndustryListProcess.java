package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryIndustryList;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryIndustryListResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.IndustryType;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.Collection;
import java.util.List;

/**
 * Created by suganyas315 on 5/12/15.
 */
public class QueryIndustryListProcess extends BaseProcess {

    private EwsQueryIndustryList mRequest;
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(QueryServiceKeyProcess.class);
    }

    public QueryIndustryListProcess(EwsQueryIndustryList pRequest) {
        mRequest = pRequest;
        logger.info("Processing Get IndustryType request");
    }


    @Override
    public EwsQueryIndustryListResponse execute() {
        EwsQueryIndustryListResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            response = process();
            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsQueryIndustryListResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsQueryIndustryListResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        /*Since its a get request call to get the list of industries from Metadata table,
        we dont need any validation */
    }

    @Override
    protected EwsQueryIndustryListResponse process() throws Exception {
        EwsQueryIndustryListResponse response = new EwsQueryIndustryListResponse();
        List<String> industryTypes = IndustryType.getAllIndustryTypes();
        response.getIndustryType().addAll(industryTypes);
        return response;
    }
}
