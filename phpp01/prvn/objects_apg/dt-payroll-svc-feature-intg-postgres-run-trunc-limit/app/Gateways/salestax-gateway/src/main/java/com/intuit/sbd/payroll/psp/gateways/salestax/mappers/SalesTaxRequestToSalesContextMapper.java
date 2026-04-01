package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.SalesBusiness;
import com.intuit.gst.SalesContext;
import com.intuit.gst.SalesParty;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.springframework.stereotype.Component;

@Component
public class SalesTaxRequestToSalesContextMapper extends BeanMapper<SalesTaxRequest, SalesContext> {


    private final String pspSalesOrganization = "PSP";
    private final String constantTransactionType = "Sale";

    @Override
    public SalesContext mapToTarget(SalesTaxRequest pspSalesTaxRequest, Class<SalesContext> salesContextClass) {
        SalesContext gstSalesContext = new SalesContext();

        //todo- check if this is what it needs to be set for all the requests, and is it even required
        gstSalesContext.setBizProcessType("Quotation");

        //todo- check if this is what it needs to be set for all the requests, and is it even required
        gstSalesContext.setChannel("Web");

        gstSalesContext.setSalesOrganization(pspSalesOrganization);

        //todo - check if default mapper is sufficient
        SalesParty salesParty = getDefaultSalesParty();
        gstSalesContext.setSalesParty(salesParty);

        //todo - check if this can be something else in any case
        gstSalesContext.setTransactionType(constantTransactionType);
        gstSalesContext.setTransactionSubtype("");

        return gstSalesContext;
    }


    private SalesParty getDefaultSalesParty() {
        SalesParty defaultSalesParty = new SalesParty();
        defaultSalesParty.setSalesBusiness(getSalesBusinessForDefaultSalesParty());
        return defaultSalesParty;
    }

    public SalesBusiness getSalesBusinessForDefaultSalesParty() {
        SalesBusiness salesBusiness = new SalesBusiness();
        salesBusiness.setName("eBiz");
        salesBusiness.setCompanyId("503");
        return salesBusiness;
    }
}
