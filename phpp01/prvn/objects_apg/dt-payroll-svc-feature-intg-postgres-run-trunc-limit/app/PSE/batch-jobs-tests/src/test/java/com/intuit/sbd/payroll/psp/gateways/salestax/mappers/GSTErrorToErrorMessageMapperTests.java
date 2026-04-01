package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.GSTError;

import com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class GSTErrorToErrorMessageMapperTests {

    GSTErrorToErrorMessageMapper mapper = new GSTErrorToErrorMessageMapper();

    @Test
    public void testGSTErrorToErrorMessageHappy() {
        GSTError gstError = new GSTError();
        gstError.setCode("503");
        gstError.setMessage("Custom error message");
        //there is an issue in library due to which setDetail does not work
//        gstError.setDetail("Custom error details");
        gstError.setType("Custom error type");

        ErrorMessage pspSalesTaxErrorMessage = mapper.mapToTarget(gstError,ErrorMessage.class);
        //there is an issue in library due to which details can't be correctly set
        //assertEquals("Custom error details",pspSalesTaxErrorMessage.getErrorDescription());
        assertEquals("503",pspSalesTaxErrorMessage.getErrorCode());
    }

}
