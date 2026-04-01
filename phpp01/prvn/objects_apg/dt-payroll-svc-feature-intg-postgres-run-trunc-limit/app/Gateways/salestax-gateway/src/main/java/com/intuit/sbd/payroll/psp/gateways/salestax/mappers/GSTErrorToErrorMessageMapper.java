package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.GSTError;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GSTErrorToErrorMessageMapper extends BeanMapper<GSTError, ErrorMessage> {
    @Override
    public ErrorMessage mapToTarget(GSTError gstError, Class<ErrorMessage> errorMessageClass) {
        if(Objects.isNull(gstError)) {
            throw new RuntimeException("GSTError is null, can not map to ErrorMessage");
        }
        ErrorMessage pspSalesTaxErrorMessage = new ErrorMessage();
        pspSalesTaxErrorMessage.setErrorCode(gstError.getCode());
        pspSalesTaxErrorMessage.setErrorDescription(gstError.getDetail());

        return pspSalesTaxErrorMessage;
    }
}
