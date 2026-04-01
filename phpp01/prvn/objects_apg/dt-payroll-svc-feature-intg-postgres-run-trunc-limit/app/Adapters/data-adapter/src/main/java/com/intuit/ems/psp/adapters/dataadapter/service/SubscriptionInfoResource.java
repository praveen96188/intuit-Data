package com.intuit.ems.psp.adapters.dataadapter.service;

import com.intuit.ems.psp.adapters.dataadapter.dto.PayrollStatus;
import com.intuit.ems.psp.adapters.dataadapter.dto.RequestHeader;
import com.intuit.ems.psp.adapters.dataadapter.helper.LicenseHelper;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import javax.validation.*;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: sshetty
 * Date: 7/20/15
 */

@Path(("/ratable"))
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SubscriptionInfoResource {

    public static final String INTUIT_TID = "intuit_tid";
    private static SpcfLogger logger = PayrollServices.getLogger(SubscriptionInfoResource.class);

    @POST
    @Path("/v1/licenseInfo")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getQbLicenseInfo(@HeaderParam(INTUIT_TID) String pIntuitTid, @Valid RequestHeader pRequestHeader) throws Exception {

        try {
            logger.info("Request received from QB for " + INTUIT_TID + "=" + pIntuitTid +
                                ",SKU=" + pRequestHeader.getSKU() +
                                ",MajorVersion=" + pRequestHeader.getMajorVersion() + ",MinorVersion: " + pRequestHeader.getMinorVersion() +
                                ",Source=" + pRequestHeader.getSource());

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<RequestHeader>> constraintViolations = validator.validate(pRequestHeader);

            if (constraintViolations != null && constraintViolations.size() > 0) {
                Iterator<ConstraintViolation<RequestHeader>> iter = constraintViolations.iterator();
                StringBuilder sb = new StringBuilder();
                if(iter.hasNext()) {
                    ConstraintViolation firstViolation = iter.next();
                    String firstErrorMessage = firstViolation.getPropertyPath() + " " + firstViolation.getMessage();
                    sb.append(firstErrorMessage + "\n");
                    while (iter.hasNext()) {
                        ConstraintViolation violation = iter.next();
                        sb.append(violation.getPropertyPath() + " " + violation.getMessage() + "\n");
                    }
                    logger.error("Constraint Violations: " + sb.toString());
                    throw new ValidationException(firstErrorMessage);
                }
            }
            Pattern qbLicensePattern = Pattern.compile(RequestHeader.VALID_QBLICENSE_PATTERN);
            Matcher qbLicensePatternMatcher = qbLicensePattern.matcher(pRequestHeader.getQBLicense());
            if (!qbLicensePatternMatcher.matches()) {
                throw new ValidationException("QBLicense not matching the criteria of XXXX-XXXX-XXXX-XXX.");
            }

            PayrollStatus responseInfo = null;
            if (!LicenseHelper.isQBVersionSupported(pRequestHeader.getMajorVersion())) {
                responseInfo = new PayrollStatus();
                responseInfo.setStatus("Disabled");
            } else {
                responseInfo = new LicenseHelper().findEntitlementByQbLicense(pRequestHeader.getQBLicense());

            }
            responseInfo.setQBLicense(pRequestHeader.getQBLicense());

            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK);
            responseBuilder.header(INTUIT_TID, pIntuitTid);

            responseBuilder.entity(responseInfo);

            logger.info("Response from PSP for " + INTUIT_TID + "=" + pIntuitTid +
                                ", SKU=" + responseInfo.getStatus() +
                                ", EndDate=" + responseInfo.getEndDate());
            return responseBuilder.build();
        } catch (Exception e) {
            logger.error("Exception Thrown:", e);
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();

        }
    }

}
