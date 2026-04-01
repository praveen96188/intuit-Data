package com.intuit.ems.psp.adapters.dataadapter.mapper;

import com.intuit.ems.psp.adapters.dataadapter.exception.MethodNotAllowed;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by ajhawar on 10/22/2015.
 */
@Provider
public class MethodNotAllowedMapper implements ExceptionMapper<MethodNotAllowed> {
    private static final int METHOD_NOT_ALLOWED = 405;
    public Response toResponse(MethodNotAllowed pMethodNotAllowed) {
        return Response.status(METHOD_NOT_ALLOWED).entity(pMethodNotAllowed).build();
    }

}
