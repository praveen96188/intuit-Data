package com.intuit.ems.psp.adapters.dataadapter.service;

import com.intuit.ems.psp.adapters.dataadapter.dto.AuthRole;
import com.intuit.ems.psp.adapters.dataadapter.exception.MethodNotAllowed;
import com.intuit.ems.psp.adapters.dataadapter.helper.AuthRoleHelper;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by ajhawar on 10/20/2015.
 */
@Path(("/authroles"))
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class AuthRoleResource {
    public static final SpcfLogger logger = Application.getLogger(AuthRoleResource.class);

    @RolesAllowed({"OIMAuthClient"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AuthRole> getRoles() {
        long startTime = System.currentTimeMillis();
        PayrollServices.beginUnitOfWork();
        List<AuthRole> authRoleList = AuthRoleHelper.getAuthRoles();
        PayrollServices.rollbackUnitOfWork();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.info("Number of Auth Roles returned :" + authRoleList.size());
        logger.info("Time taken to complete the GET all AuthRoles request :" + totalTime);
        return authRoleList;
    }

    @RolesAllowed({"OIMAuthClient"})
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAuthRoles() {
        throw new MethodNotAllowed("Not Allowed");
    }

    @RolesAllowed({"OIMAuthClient"})
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postToAuthRoles() {
        throw new MethodNotAllowed("Not Allowed");
    }

    @RolesAllowed({"OIMAuthClient"})
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putToAuthRoles() {
        throw new MethodNotAllowed("Not Allowed");
    }
}
