package com.intuit.ems.psp.adapters.dataadapter.service;

import com.intuit.ems.psp.adapters.dataadapter.dto.AuthUser;
import com.intuit.ems.psp.adapters.dataadapter.exception.BadRequestException;
import com.intuit.ems.psp.adapters.dataadapter.exception.DataNotFoundException;
import com.intuit.ems.psp.adapters.dataadapter.exception.MethodNotAllowed;
import com.intuit.ems.psp.adapters.dataadapter.helper.AuthUserHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


/**
 * Created by ajhawar on 10/19/2015.
 */

@Path(("/authusers"))
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class AuthUserResource {
    private static SpcfLogger logger = PayrollServices.getLogger(AuthUserResource.class);


    @RolesAllowed({"OIMAuthClient"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AuthUser> getAuthUsers() {
        long startTime = System.currentTimeMillis();
        PayrollServices.beginUnitOfWork();
        List<AuthUser> authUserList = AuthUserHelper.getAuthUsers();
        PayrollServices.rollbackUnitOfWork();
        logger.info("Total Number of AuthUsers returned in the response : " + authUserList.size());
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.info("Time taken to complete the GET all AuthUsers request :" + totalTime);
        return authUserList;
    }

    @RolesAllowed({"OIMAuthClient"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/corpid/{authUserCorp}")
    public AuthUser getAuthUserByCorpId(@PathParam("authUserCorp") String corpId) throws DataNotFoundException {
        long startTime = System.currentTimeMillis();
        AuthUser authUser = null;
        try {
            PayrollServices.beginUnitOfWork();
            authUser = AuthUserHelper.getUserByCorpId(corpId);
        } catch (DataNotFoundException exception) {
            logger.warn(exception);
            throw exception;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to complete the GET authuser by corp id request :" + (endTime - startTime));
        }
        return authUser;
    }

    @RolesAllowed({"OIMAuthClient"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/sequence/{authUserSeq}")
    public AuthUser getAuthUserBySequenceId(@PathParam("authUserSeq") String authUserSeq) throws DataNotFoundException, BadRequestException {
        long startTime = System.currentTimeMillis();
        AuthUser authUser = null;
        try {
            PayrollServices.beginUnitOfWork();
            authUser = AuthUserHelper.getUserBySequenceId(authUserSeq);
        } catch (DataNotFoundException exception) {
            logger.warn(exception);
            throw exception;
        } catch (BadRequestException exception) {
            logger.warn(exception);
            throw exception;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to complete the GET authuser by sequence id request :" + (endTime - startTime));
        }
        return authUser;
    }

    @RolesAllowed({"OIMAuthClient"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/lastmodified/{authUserLastModified}")
    public List<AuthUser> getAuthUsersByLastModified(@PathParam("authUserLastModified") long authUserLastModified) {
        long startTime = System.currentTimeMillis();
        PayrollServices.beginUnitOfWork();
        List<AuthUser> authUserList = AuthUserHelper.getUserByLastModified(authUserLastModified);
        PayrollServices.rollbackUnitOfWork();
        long endTime = System.currentTimeMillis();
        logger.info("Total number of GET all authusers based on last modified time is :" + authUserList.size());
        logger.info("Time taken to complete the GET all authusers request by lastmodified time :" + (endTime - startTime));
        return authUserList;
    }

    @RolesAllowed({"OIMAuthClient"})
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/corpid/{authUserCorpId}")
    public Response deleteAuthUserByCorpId(@PathParam("authUserCorpId") String authUserCorpId) throws DataNotFoundException, BadRequestException{
        long startTime = System.currentTimeMillis();
        try {
            PayrollServices.beginUnitOfWork();
            AuthUserHelper.deleteUserByCorpId(authUserCorpId);
            PayrollServices.commitUnitOfWork();
        } catch (DataNotFoundException exception) {
            logger.warn(exception);
            throw exception;
        }  catch (BadRequestException exception) {
            logger.warn(exception);
            throw exception;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to complete the Delete authuser request :" + (endTime - startTime));
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @RolesAllowed({"OIMAuthClient"})
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/sequence/{authUserSeq}")
    public Response deleteAuthUserBySeqId(@PathParam("authUserSeq") String authUserSeq) throws DataNotFoundException, BadRequestException{
        long startTime = System.currentTimeMillis();
        try {
            PayrollServices.beginUnitOfWork();
            AuthUserHelper.deleteUserBySeqId(authUserSeq);
            PayrollServices.commitUnitOfWork();
        } catch (DataNotFoundException exception) {
            logger.warn(exception);
            throw exception;
        } catch (BadRequestException exception) {
            logger.warn(exception);
            throw exception;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to complete the Delete authuser request :" + (endTime - startTime));
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @RolesAllowed({"OIMAuthClient"})
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/corpid/{authUserCorpId}/role/{roleId}")
    public Response deleteAuthUserRoleByCorpId(@PathParam("authUserCorpId") String authUserCorpId,@PathParam("roleId") String roleId) throws DataNotFoundException, BadRequestException{
        long startTime = System.currentTimeMillis();
        try {
            PayrollServices.beginUnitOfWork();
            AuthUserHelper.deleteUserRoleByCorpId(authUserCorpId, roleId);
            PayrollServices.commitUnitOfWork();
        } catch (DataNotFoundException exception) {
            logger.warn(exception);
            throw exception;
        } catch (BadRequestException e) {
            logger.warn(e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to complete the Delete role of the authuser request :" + (endTime - startTime));
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @RolesAllowed({"OIMAuthClient"})
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postToAuthUsers() {
        throw new MethodNotAllowed("Not Allowed");
    }

    @RolesAllowed({"OIMAuthClient"})
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putToAuthUsers() {
        throw new MethodNotAllowed("Not Allowed");
    }
}
