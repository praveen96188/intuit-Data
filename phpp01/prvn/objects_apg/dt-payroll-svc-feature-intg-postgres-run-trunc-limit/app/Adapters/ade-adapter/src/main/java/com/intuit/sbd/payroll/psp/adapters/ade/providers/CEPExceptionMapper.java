package com.intuit.sbd.payroll.psp.adapters.ade.providers;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 4/12/14
 * Time: 7:30 AM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.ems.cep.api.exception.ServiceResultException;
import com.intuit.ems.cep.api.messages.Message;
import com.intuit.ems.cep.api.messages.MessageCode;
import com.intuit.ems.cep.api.messages.MessageLevel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.schema.platform.common.error.v2.Error;
import com.intuit.schema.platform.common.error.v2.ErrorType;
import com.intuit.schema.platform.common.error.v2.Errors;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.jersey.api.ParamException;
import org.codehaus.jackson.map.JsonMappingException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * ExceptionMapper to handle all the exceptions thrown by the resources and convert them to Response object.
 * <p/>
 * Provider is Jersey
 */
@SuppressWarnings("UnusedDeclaration")
@Provider
public class CEPExceptionMapper implements ExceptionMapper<Throwable> {

    private SpcfLogger logger = Application.getLogger(CEPExceptionMapper.class);

    public Response toResponse(Throwable exception) {
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.BAD_REQUEST);
        Errors errors = new Errors();
        if (exception instanceof ParamException) {
            ParamException paramException = (ParamException) exception;
            String message = "Error parsing, parameter=" + paramException.getParameterName() + " message=" + paramException.getMessage();
            Error error = new Error();
            error.setMessage(message);
            errors.getError().add(error);
            responseBuilder.entity(errors);
        } else if (exception instanceof WebApplicationException) {
            WebApplicationException webApplicationException = (WebApplicationException) exception;
            int responseStatusCode = webApplicationException.getResponse().getStatus();
            Response.Status responseStatus = Response.Status.fromStatusCode(responseStatusCode);
            responseBuilder.status(responseStatusCode);
            Error error = new Error();
            error.setCode(MessageCode.UnexpectedException.getMessageCode());
            if (responseStatus != null) {
                error.setMessage(responseStatus.getReasonPhrase());
            }
            errors.getError().add(error);
            responseBuilder.entity(errors);
        } else if (exception instanceof ServiceResultException) {
            for (Message message : ((ServiceResultException) exception).getServiceResult().getMessages(MessageLevel.ERROR)) {
                Error error = new Error();
                error.setCode(message.getMessageCode());
                error.setType(ErrorType.CLIENT);
                error.setMessage(message.getMessage());
                errors.getError().add(error);

                if (MessageCode.EntityDoesNotExist.getMessageCode().equalsIgnoreCase(message.getMessageCode()) ||
                        MessageCode.EntitiesDoNotExist.getMessageCode().equalsIgnoreCase(message.getMessageCode()) ||
                        MessageCode.NotImplemented.getMessageCode().equalsIgnoreCase(message.getMessageCode())) {
                    responseBuilder = Response.status(Response.Status.NOT_FOUND);

                } else if (MessageCode.UnexpectedException.getMessageCode().equalsIgnoreCase(message.getMessageCode())) {
                    error.setType(ErrorType.SERVER);
                    responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
            responseBuilder.entity(errors);
        } else if (exception instanceof JsonMappingException) {
            Error error = new Error();
            error.setCode(MessageCode.InvalidValue.getMessageCode());
            error.setMessage("Invalid request");
            error.setDetail(exception.getMessage());
            errors.getError().add(error);
            responseBuilder.entity(errors);
        } else {
            responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
            Error error = new Error();
            error.setCode(MessageCode.UnexpectedException.getMessageCode());
            error.setType(ErrorType.SERVER);

            String message = "Unexpected Exception";
            if (exception.getMessage() != null)
                message = exception.getMessage();
            error.setMessage(message);
            error.setDetail("Internal server error");

            errors.getError().add(error);
            responseBuilder.entity(errors);
        }
        Response response = responseBuilder.build();

        if (response.getStatus() >= 500) {
            logger.error("CEP Exception", exception);
            logger.error("CEP Response Code:" + response.getStatus());
            logger.error("CEP Error Response: " + JacksonContextResolver.getInstance().serialize(errors));
        }

        return response;
    }
}

