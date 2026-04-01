package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;


import static org.junit.Assert.*;

public class SalesTaxRequestLineToGSTLineTotalMapperTests {

    private SalesTaxRequestLineToGSTLineTotalMapper mapperUnderTest
            = new SalesTaxRequestLineToGSTLineTotalMapper();

    @Test
    public void TestHappyPath() {

        SalesTaxRequestLine pspSalesTaxRequestLine = new SalesTaxRequestLine();
        pspSalesTaxRequestLine.setAmount(new BigDecimal("12.3"));

        LineTotal resultLineTotal = mapperUnderTest.mapToTarget(pspSalesTaxRequestLine,LineTotal.class);
        assertEquals(12.3,resultLineTotal.getValue(),0.01);

    }
}
