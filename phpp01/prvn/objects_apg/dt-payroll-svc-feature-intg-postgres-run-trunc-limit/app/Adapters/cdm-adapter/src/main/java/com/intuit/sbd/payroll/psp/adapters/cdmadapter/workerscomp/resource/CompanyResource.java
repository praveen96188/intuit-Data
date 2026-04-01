package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.resource;

/**
 * Created with IntelliJ IDEA.
 * User: afroza786
 * Date: 7/3/13
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.CompanyDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.CompanyListDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.manager.WorkersCompManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("v1/workerscomp")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CompanyResource {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(CompanyResource.class);

    @GET
    @Path("/company/psid/{psid}")
    public Response getCompanyByPSID(@PathParam("psid") String psid) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("PSID: " + psid);
            }
            if (StringUtils.isBlank(psid)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            WorkersCompManager manager = new WorkersCompManager();
            CompanyDTO dto = manager.getCompanyByPSID(psid);
            if (dto != null) {
                return Response.ok(dto).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception ex) {
            logger.error("Error while returning company info for psid " + psid, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/company/ein/{ein}/subscription/{subsNo}")
    public Response getCompanyByEINAndSubsNum(@PathParam("ein") String ein, @PathParam("subsNo") String subsNo) {
        CompanyDTO dto=null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("subsNo: "+subsNo);
            }
            if (StringUtils.isBlank(subsNo)|| StringUtils.isBlank(ein)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            WorkersCompManager manager = new WorkersCompManager();
             dto = manager.getCompanyByEINAndSubsNum(ein, subsNo);

            if (dto != null) {
                logger.debug("PSID : "+dto.getPsid());
                return Response.ok(dto).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception ex) {
            logger.error("Error while returning company info for psid/subsno " + dto.getPsid() + "/" + subsNo, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/companylist/ein/{ein}/subscription/{subsNo}")
    public Response getCompaniesByEINAndSubsNum(@PathParam("ein") String ein, @PathParam("subsNo") String subsNo){

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("subsNo: "+subsNo );
            }
            if (StringUtils.isBlank(subsNo)|| StringUtils.isBlank(ein)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            WorkersCompManager manager = new WorkersCompManager();
            CompanyListDTO dto = manager.getCompaniesByEINAndSubsNum(ein, subsNo);
            if (dto != null) {
                return Response.ok(dto).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception ex){
            logger.error("Error while returning company list for ein/subsno " + CdmHelper.maskValue(ein) + "/" + subsNo, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/companylist/ein/{ein}")
    public Response getCompaniesByEIN(@PathParam("ein") String ein){
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("ein: " + CdmHelper.maskValue(ein));
            }
            if (StringUtils.isBlank(ein)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            WorkersCompManager manager = new WorkersCompManager();
            CompanyListDTO dto = manager.getCompaniesByEIN(ein);
            if (dto != null) {
                return Response.ok(dto).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception ex){
            logger.error("Error while returning companies for ein: " + CdmHelper.maskValue(ein),ex );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
