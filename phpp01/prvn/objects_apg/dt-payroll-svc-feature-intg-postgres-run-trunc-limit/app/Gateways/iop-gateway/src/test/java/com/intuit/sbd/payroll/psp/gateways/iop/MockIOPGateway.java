package com.intuit.sbd.payroll.psp.gateways.iop;

import com.intuit.onlinepayroll.webservices.v1.*;
import com.intuit.sbd.payroll.psp.gateways.iop.IOPGateway;
import com.intuit.sbd.payroll.psp.gateways.iop.exceptions.ServiceUnavailableException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jeff Jones
 */
public class MockIOPGateway extends IOPGateway {

    private static Map<Integer, PayrollCompanyModel> companyIdToPayrollCompanyModelMap;
    private static Map<Integer, ContractorPaymentCompanyModel> companyIdToContractorPaymentCompanyModelMap;

    static {
        companyIdToPayrollCompanyModelMap = new HashMap<Integer, PayrollCompanyModel>();
        companyIdToContractorPaymentCompanyModelMap = new HashMap<Integer, ContractorPaymentCompanyModel>();
    }

    public MockIOPGateway() throws Throwable {}

    @Override
    public QName getQName() {
        return  super.getQName();
    }

    @Override
    public List<Integer> getCompaniesWithPayrollActivity(SpcfCalendar pStart, SpcfCalendar pEnd) throws ServiceUnavailableException {
        return new ArrayList<Integer>(companyIdToPayrollCompanyModelMap.keySet());
    }

    @Override
    public PayrollCompanyModel getPaychecksEmployeesCompanyDetails(Long pCompanyId, SpcfCalendar pStart, SpcfCalendar pEnd) throws ServiceUnavailableException {
        return companyIdToPayrollCompanyModelMap.remove(Integer.valueOf(pCompanyId.toString()));
    }
    
    public static void addPayrollCompanyModel(Integer pCompanyId, PayrollCompanyModel pPayrollCompanyModel) {
        companyIdToPayrollCompanyModelMap.put(pCompanyId, pPayrollCompanyModel);
    }

    @Override
    public List<Integer> getCompaniesWithContractorPaymentActivity(SpcfCalendar pStart, SpcfCalendar pEnd) throws ServiceUnavailableException {
        return new ArrayList<Integer>(companyIdToContractorPaymentCompanyModelMap.keySet());
    }

    @Override
    public ContractorPaymentCompanyModel getContractorPaymentCompanyModel(Long pCompanyId, SpcfCalendar pStart, SpcfCalendar pEnd) throws ServiceUnavailableException {
        return companyIdToContractorPaymentCompanyModelMap.remove(Integer.valueOf(pCompanyId.toString()));
    }

    public static void addContractorPaymentCompanyModel(Integer pCompanyId, ContractorPaymentCompanyModel pContractorPaymentCompanyModel) {
        companyIdToContractorPaymentCompanyModelMap.put(pCompanyId, pContractorPaymentCompanyModel);
    }
}
