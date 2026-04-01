package com.intuit.ems.psp.adapters.dataadapter.mapper;

import com.intuit.ems.psp.adapters.dataadapter.exception.BadRequestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by ajhawar on 1/3/2016.
 */
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    public Response toResponse(BadRequestException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
    }

}
