package com.intuit.sbd.payroll.psp.gateways.iop;

import com.intuit.onlinepayroll.webservices.v1.ContractorPaymentCompanyModel;
import com.intuit.onlinepayroll.webservices.v1.PayrollCompanyModel;
import com.intuit.sbd.payroll.psp.gateways.iop.exceptions.ServiceUnavailableException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Jeff Jones
 */
public interface IIOPGateway {
    public QName getQName();       

    public List<Integer> getCompaniesWithPayrollActivity(SpcfCalendar pStart,
                                                         SpcfCalendar pEnd) throws Exception;

    public PayrollCompanyModel getPaychecksEmployeesCompanyDetails(Long pCompanyId,
                                                                   SpcfCalendar pStart,
                                                                   SpcfCalendar pEnd) throws Exception;

    public List<Integer> getCompaniesWithContractorPaymentActivity(SpcfCalendar pStart,
                                                         SpcfCalendar pEnd) throws Exception;

    public ContractorPaymentCompanyModel getContractorPaymentCompanyModel(Long pCompanyId,
                                                                   SpcfCalendar pStart,
                                                                   SpcfCalendar pEnd) throws Exception;
}
