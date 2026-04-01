package com.intuit.ems.psp.adapters.dataadapter.mapper;

import com.intuit.ems.psp.adapters.dataadapter.exception.DataNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by ajhawar on 10/21/2015.
 */
@Provider
public class DataNotFoundExceptionMapper implements ExceptionMapper<DataNotFoundException> {

    public Response toResponse(DataNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(e).build();
    }

}
