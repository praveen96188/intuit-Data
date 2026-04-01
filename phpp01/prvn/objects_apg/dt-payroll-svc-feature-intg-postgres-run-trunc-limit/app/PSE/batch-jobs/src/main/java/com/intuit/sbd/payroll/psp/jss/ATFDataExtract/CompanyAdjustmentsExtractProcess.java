package com.intuit.sbd.payroll.psp.jss.ATFDataExtract;

import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;

import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 28, 2009
 * Time: 3:36:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyAdjustmentsExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID ="CO_ADJ";

    public CompanyAdjustmentsExtractProcess(String[] pArguments) {
        super(pArguments);
        init();
    }

    public CompanyAdjustmentsExtractProcess(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        init();
    }
    
    private void init(){
    	mProcessCode = REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.CompanyAdjustmentsInfo;
    }

    public void execute() {
        super.execute();
    }

    /**
     * This method writes the actual company info details.
     * @param pPW
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {
//        pPW.println("Test File");
//        mRecordCount++;
    }

    @Override
    protected void writeData(PgpWriter pPW) throws Throwable {
        pPW.write("Test File");
        mRecordCount++;
    }
}