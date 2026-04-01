package com.intuit.sbd.payroll.psp.adapters.qbdt.socket;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTProcessResult;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTRequestProcessor;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 5/25/12
 * Time: 7:51 AM
 */
public class StaleObjectExceptionSocketManager implements ISocketManager {
    public String processRequest(String pRequest) throws SocketException, RequestException, SocketClosedException {
        // use a transaction thread to update the companies information and cause a stale object exception
        QBDTProcessResult<String> QBDTProcessResult = QBDTRequestProcessor.retrieveCompanyPSIDFromRequestString(pRequest);
        final String finalPsid = QBDTProcessResult.getResult();
        TransactionThread<ProcessResult> thread = new TransactionThread<ProcessResult>() {
            public ProcessResult transaction() {
                Company company = Company.findCompany(finalPsid, SourceSystemCode.QBDT);
                company.setCurrentToken(8);
                Application.save(company);
                return new ProcessResult();
            }
        };
        PayrollServices.executeTransactionThread(thread);

        return QBDTTestHelper.SUCCESSFUL_OFX_RESPONSE;
    }

    public void close() throws SocketClosingException {
    }

    public void open(String pHost, int pPort, int pSocketTimeout) throws SocketConnectionException {
    }

    public void open(String pHost, int pPort, int pConnectionTimeout, int pSocketTimeout) throws SocketConnectionException {
    }
}
