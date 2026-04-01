package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 3, 2008
 * Time: 11:40:05 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ISalesTaxGateway {
    public SalesTaxResponse send(SalesTaxRequest pSalesTaxRequest) ;
}
