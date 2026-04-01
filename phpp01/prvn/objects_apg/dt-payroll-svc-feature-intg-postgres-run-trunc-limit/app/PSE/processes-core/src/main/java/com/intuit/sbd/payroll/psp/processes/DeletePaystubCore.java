package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Paystub;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primarySpecific.logging.SpcfLoggerImpl;
import sun.util.logging.resources.logging;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class DeletePaystubCore extends Process implements IProcess {
    private Company mCompany = null;
    private String[] paramNames = {"company"};


    private static final SpcfLogger logger = Application.getLogger(DeletePaystubCore.class);
    private static final String mInnerQueryPaystubs = "( select distinct ps from com.intuit.sbd.payroll.psp.domain.Paystub ps" +
                                                      " where ps.Paycheck.Company  = :company )";


    public DeletePaystubCore(Company pCompany) {
        mCompany = pCompany;
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        if (mCompany == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, null, "Company is not found");
            return validationResult;
        }

        //validate the they having required services should be active on VMP service.
        if(!mCompany.hasService(ServiceCode.ViewMyPaycheck)){
           logger.error("Company : " + mCompany.getSourceCompanyId() + "not on VMP");
           validationResult.getMessages().InvalidValue(EntityName.Company, null, "Company is not on VMP service");
           return validationResult;
        }

        Long countOfPaystubs = getCountOfPaystubs();

        if(countOfPaystubs <= 0){
            logger.error("Deleting Number of Paystubs, PaystubCount := " + countOfPaystubs);
            validationResult.getMessages().InvalidValue(EntityName.Company, null, "Company doesn't have VMP data to delete ");
            return validationResult;
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        logger.info("Deleting Number of Paystubs, PaystubCount := " + countOfPaystubs);

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        /* delete Pstub related data in order */

        logger.info("Deleting Paystub Pay Item...");
        deletePaystubPayItem();

        logger.info("Deleting Paystub Msg...");
        deletePaystubMsg();

        logger.info("Deleting Paystub DD Item...");
        deletePaystubDDItem();

        logger.info("Deleting Paystub Paid Timeoff Item...");
        deletePaystubPaidTimeoffItem();

        logger.info("Deleting Paystub...");
        deletePaystub();

        return processResult;
    }

    /**
     *
     * @return Count of Paystub entries for a company
     */

    private Long getCountOfPaystubs(){

        StringBuilder builder = new StringBuilder();
        Object[] paramValues = {mCompany};

        builder.append("select count(*) from com.intuit.sbd.payroll.psp.domain.Paystub ps ")
               .append("where ps.Paycheck.Company  = :company");

        List<Long> paystubCount = Application.executeHQLQuery(builder.toString(), paramNames, paramValues);

        if(paystubCount == null && paystubCount.isEmpty()){
            return 0L;
        }

        return paystubCount.get(0);
    }

    private void deletePaystubPayItem(){
        StringBuilder builder = new StringBuilder();
        Object[] paramValues = {mCompany};

        builder.append("delete from com.intuit.sbd.payroll.psp.domain.PstubPayItem ppi ")
               .append("where  ppi.Paystub in " )
               .append(mInnerQueryPaystubs);

        Application.executeHQLUpdate(builder.toString(), paramNames, paramValues);
     }

    private void deletePaystubMsg(){
        StringBuilder builder = new StringBuilder();
        Object[] paramValues = {mCompany};

        builder.append("delete from com.intuit.sbd.payroll.psp.domain.PstubMsg pm ")
               .append("where pm.Paystub in " )
               .append(mInnerQueryPaystubs);

        Application.executeHQLUpdate(builder.toString(),paramNames,paramValues);
    }


    private void deletePaystubDDItem(){

        StringBuilder builder = new StringBuilder();
        Object[] paramValues = {mCompany};

        builder.append("delete from com.intuit.sbd.payroll.psp.domain.PstubDDItem pddi ")
               .append("where  pddi.Paystub in " )
               .append(mInnerQueryPaystubs);

        Application.executeHQLUpdate(builder.toString(),paramNames,paramValues);
    }

    private void deletePaystubPaidTimeoffItem(){

        StringBuilder builder = new StringBuilder();
        Object[] paramValues = {mCompany};

        builder.append("delete from com.intuit.sbd.payroll.psp.domain.PstubPaidTimeoffItem ppti ")
               .append("where  ppti.Paystub in " )
               .append(mInnerQueryPaystubs);

         Application.executeHQLUpdate(builder.toString(),paramNames,paramValues);

    }

    private void deletePaystub(){

        StringBuilder builder = new StringBuilder();
        Object[] paramValues = {mCompany};

        builder.append("delete from com.intuit.sbd.payroll.psp.domain.Paystub ps ")
               .append("where  ps in " )
               .append(mInnerQueryPaystubs);

        Application.executeHQLUpdate(builder.toString(),paramNames,paramValues);
    }
}
