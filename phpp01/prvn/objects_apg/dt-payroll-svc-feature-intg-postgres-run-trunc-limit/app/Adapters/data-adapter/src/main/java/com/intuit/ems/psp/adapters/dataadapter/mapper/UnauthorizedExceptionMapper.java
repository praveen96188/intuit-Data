package com.intuit.ems.psp.adapters.dataadapter.mapper;

import com.intuit.ems.psp.adapters.dataadapter.exception.UnauthorizedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by charithah418 on 11/11/15.
 */

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    public Response toResponse(UnauthorizedException e) {
        return Response.status(Response.Status.UNAUTHORIZED).entity(e).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
