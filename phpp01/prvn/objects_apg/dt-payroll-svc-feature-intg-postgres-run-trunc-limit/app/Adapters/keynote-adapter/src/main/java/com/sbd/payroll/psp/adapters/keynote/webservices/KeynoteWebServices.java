package com.sbd.payroll.psp.adapters.keynote.webservices;

/**
 * User: ihannur
 * Date: 11/28/12
 * Time: 10:31 AM
 */

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.CommonValidations;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.ErrorMessageList;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sbd.payroll.psp.adapters.keynote.dtos.KeynoteResponse;
import org.hibernate.Query;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * WebServices for keynote validation
 */
@WebService()
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public class KeynoteWebServices {
    private static final SpcfLogger logger = PayrollServices.getLogger(KeynoteWebServices.class);

    @WebMethod
    public KeynoteResponse KeynoteValidation(@WebParam(name = "KeynoteRequest") Request keynoteRequest) {
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.QBDTWSAdapter));

        KeynoteResponse response = new KeynoteResponse();
        response.setStatus(-1);

        String companyId = null;
        try {
            if (keynoteRequest == null) {
                response.getProcessingMessagesList().add(ErrorMessageList.invalidArgument("KeynoteRequest"));
                return response;
            }

            // validate the company, PIN and token (basic required message properties)
            PayrollServices.beginUnitOfWork();
            CommonValidations.validateCompanyPin(keynoteRequest, response);

            if (response.getProcessingMessagesList().size() != 0) {
                return response;
            }

            Query query = Application.getHibernateSession().createSQLQuery("select " +
                    Application.getTruncFunctionString("CURRENT_TIMESTAMP") +
                    " from dual");

            String value = query.list().get(0).toString();
            response.setValue(value);
            response.setStatus(0);

            PayrollServices.rollbackUnitOfWork();

        } catch (Throwable t) {
            logger.error("KeynoteValidation exception for PSID: " + companyId, t);
            response.getProcessingMessagesList().add(ErrorMessageList.unexpectedError());
            response.setValue(t.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

}
