package com.intuit.ems.psp.adapters.dataadapter.mapper;

import javax.validation.ValidationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created with IntelliJ IDEA.
 * User: sshetty
 * Date: 9/11/15
 * Time: 8:12 AM
 * To change this template use File | Settings | File Templates.
 */
@Provider
public class DefaultExceptionMapper
        implements ExceptionMapper<Throwable> {

    public Response toResponse(Throwable exception) {
        Throwable badRequestException
                = getBadRequestException(exception);
        if (badRequestException != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(badRequestException.getMessage())
                           .build();
        }
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException)exception)
                    .getResponse();
        }
        return Response.serverError()
                       .entity(exception.getMessage())
                       .build();
    }

    private Throwable getBadRequestException(
            Throwable exception) {
        if (exception instanceof ValidationException) {
            return exception;
        }
        Throwable cause = exception.getCause();
        if (cause != null && cause != exception) {
            Throwable result = getBadRequestException(cause);
            if (result != null) {
                return result;
            }
        }
        if (exception instanceof IllegalArgumentException) {
            return exception;
        }

        return null;
    }

}