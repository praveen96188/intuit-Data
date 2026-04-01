package com.intuit.sbd.payroll.psp.adapters.mobile.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 @author Jeff Jones
 */

public class CompanyFinder {

    public static Company findCompany(String pEin){
        //Get Termed Accounts
        Expression<CompanyService> query =
                new Query<CompanyService>()
                       .Where(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.Terminated)
                              .And(CompanyService.Company().FedTaxId().equalTo(pEin)
                              .And(CompanyService.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))))
                       .OrderBy(CompanyService.Company().SignUpDate().Descending())
                       .LimitResults(0, 1);

        DomainEntitySet<CompanyService> companyServices = Application.find(CompanyService.class, query);

        if (!companyServices.isEmpty()) {
            return companyServices.get(0).getCompany();
        }

        //Get Active Accounts
        query = new Query<CompanyService>()
                   .Where(CompanyService.StatusCd().notIn(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled)
                          .And(CompanyService.Company().FedTaxId().equalTo(pEin)
                          .And(CompanyService.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))))
                   .OrderBy(CompanyService.Company().SignUpDate().Descending())
                   .LimitResults(0, 1);

        companyServices = Application.find(CompanyService.class, query);

        if (!companyServices.isEmpty()) {
            return companyServices.get(0).getCompany();
        }

        //Get Cancelled Accounts
        query = new Query<CompanyService>()
                   .Where(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.Cancelled)
                          .And(CompanyService.Company().FedTaxId().equalTo(pEin)
                          .And(CompanyService.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))))
                   .OrderBy(CompanyService.Company().SignUpDate().Descending())
                   .LimitResults(0, 1);

        companyServices = Application.find(CompanyService.class, query);

        if (!companyServices.isEmpty()) {
            return companyServices.get(0).getCompany();
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
