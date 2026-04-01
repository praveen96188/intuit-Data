package com.intuit.ems.psp.adapters.dataadapter.helper;

import com.intuit.ems.psp.adapters.dataadapter.dto.AuthRole;
import com.intuit.ems.psp.adapters.dataadapter.dto.AuthUser;
import com.intuit.ems.psp.adapters.dataadapter.exception.BadRequestException;
import com.intuit.ems.psp.adapters.dataadapter.exception.DataNotFoundException;
import com.intuit.ems.psp.adapters.dataadapter.mapper.AuthRoleToDTOMapper;
import com.intuit.ems.psp.adapters.dataadapter.mapper.AuthUserToDTOMapper;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.util.AsyncMailSender;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ajhawar on 10/22/2015.
 */
public class AuthUserHelper {
    private static SpcfLogger logger = PayrollServices.getLogger(AuthUserHelper.class);
    private static final String AUTH_USER_NOT_FOUND_ERROR_MSG = "AuthUser not found";
    private static final String AUTH_USER_NOT_DELETED = "AuthUser could not be deleted";
    private static final String AUTH_USER_ROLE_NOT_DELETED = "AuthRole could not be deleted";
    private static final String AUTH_USER_ROLE_NOT_FOUND = "AuthRole not found";
    private static final String AUTH_USER_SEQUENCE_ID_INCORRECT = "AuthUser SequenceId incorrect";

    public static List<AuthUser> getAuthUsers() {
        DomainEntitySet<com.intuit.sbd.payroll.psp.domain.AuthUser> authUserSet = Application.find(com.intuit.sbd.payroll.psp.domain.AuthUser.class);
        List<AuthUser> authUserList = new ArrayList<AuthUser>();
        for (com.intuit.sbd.payroll.psp.domain.AuthUser authUser : authUserSet) {
            authUserList.add(AuthUserToDTOMapper.mapToDTO(authUser));
        }
        return authUserList;
    }

    public static AuthUser getUserBySequenceId(String seq) throws DataNotFoundException, BadRequestException {
        AuthUser authUser;
        SpcfUniqueId authUserSpcfUniqueId;
        try {
            authUserSpcfUniqueId = SpcfUniqueIdImpl.createInstance(seq);
        } catch (SpcfIllegalArgumentException e) {
            logger.warn(e);
            throw new BadRequestException(AUTH_USER_SEQUENCE_ID_INCORRECT);
        }
        com.intuit.sbd.payroll.psp.domain.AuthUser domainAuthUser = Application.findById(com.intuit.sbd.payroll.psp.domain.AuthUser.class, authUserSpcfUniqueId);
        if (domainAuthUser != null) {
            authUser = AuthUserToDTOMapper.mapToDTO(domainAuthUser);
            logger.info("Successfully sending response for GET AuthUser by sequence id ");
            return authUser;
        }
        throw new DataNotFoundException(AUTH_USER_NOT_FOUND_ERROR_MSG);
    }

    public static AuthUser getUserByCorpId(String pCorpId) throws DataNotFoundException {
        AuthUser authUser;
        com.intuit.sbd.payroll.psp.domain.AuthUser domainAuthUser = com.intuit.sbd.payroll.psp.domain.AuthUser.findUser(pCorpId);
        if (domainAuthUser != null) {
            authUser = AuthUserToDTOMapper.mapToDTO(domainAuthUser);
            logger.info("Successfully sending response for GET AuthUser by corp id ");
            return authUser;
        }
        throw new DataNotFoundException(AUTH_USER_NOT_FOUND_ERROR_MSG);
    }

    public static List<AuthUser> getUserByLastModified(Long lastModTime) {
        List<AuthUser> authUserList = new ArrayList<AuthUser>();
        //todo - NULL check on lastModTime
        SpcfCalendar spcfCalendar = new SpcfCalendarImpl(lastModTime);
        Criterion<com.intuit.sbd.payroll.psp.DomainEntity> where = com.intuit.sbd.payroll.psp.domain.AuthUser.ModifiedDate().greaterOrEqualThan(spcfCalendar);
        DomainEntitySet<com.intuit.sbd.payroll.psp.domain.AuthUser> authUserSet = Application.find(com.intuit.sbd.payroll.psp.domain.AuthUser.class, where);
        for (com.intuit.sbd.payroll.psp.domain.AuthUser authUser : authUserSet) {
            authUserList.add(AuthUserToDTOMapper.mapToDTO(authUser));
        }
        return authUserList;
    }

    public static void deleteUserByCorpId(String pCorpId) throws DataNotFoundException, BadRequestException {
        AuthUser authUser = getUserByCorpId(pCorpId);
        deleteUser(authUser);
        logger.info("AuthUser with corpid " + authUser.getCorpId() + " deleted ");
        sendMail(authUser, EmailCriteria.AUTH_USER_DELETED);

    }

    public static void deleteUserBySeqId(String pSeqId) throws DataNotFoundException, BadRequestException {
        AuthUser authUser = getUserBySequenceId(pSeqId);
        deleteUser(authUser);
        logger.info("AuthUser with corpid " + authUser.getCorpId() + " deleted ");
        sendMail(authUser, EmailCriteria.AUTH_USER_DELETED);
    }

    public static void deleteUser(AuthUser pAuthUser) throws BadRequestException {

        ProcessResult<com.intuit.sbd.payroll.psp.domain.AuthUser> processResult;
        processResult = PayrollServices.userManager.deleteUser(pAuthUser.getCorpId());
        if (!processResult.isSuccess()) {

            throw new BadRequestException(AUTH_USER_NOT_DELETED);

        }

    }

    /**
     * @param pAuthUser
     * @param pEmailCriteria
     */
    public static void sendMail(AuthUser pAuthUser, EmailCriteria pEmailCriteria) {
        String subject = null;
        StringBuilder body = new StringBuilder();
        subject = "PSP DataAdapter Alert - AuthUser Deleted";
        body.append("AuthUser with CorpId " + pAuthUser.getCorpId() + " has been deleted, details below:");
        body.append("\r\n\r\n");
        try {
            String mailServer = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_mail_server");
            String fromAddress = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_auth_service_informed_email_list");
            String toAddress = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_auth_service_informed_email_list");
            logger.info("Sending transaction alert for authUser=" + pAuthUser.getCorpId());
            if (StringUtils.isNotEmpty(toAddress)) {
                body.append("AuthUser FirstName: " + pAuthUser.getFirstName());
                body.append("\r\n");
                body.append("AuthUser LastName: " + pAuthUser.getLastName());
                body.append("\r\n");
                body.append("AuthUser CorpId: " + pAuthUser.getCorpId());
                body.append("\r\n");
                body.append("AuthUser SequenceId: " + pAuthUser.getAuthUserSeq());
                body.append("\r\n");
                body.append("Deleted At: " + (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss aa zzz")).format(new Date()));
                body.append("\r\n\r\n");
                MailSender.sendEmailAsync(mailServer,
                                          fromAddress,
                                          toAddress,
                                          subject,
                                          body.toString());
                logger.info("Successfully scheduled alert for email criteria " + pEmailCriteria + " for authUser=" + pAuthUser.getCorpId() + " to=" + toAddress);
            } else {
                logger.warn("Unable to send alert email for delete call made for authUser=" + pAuthUser.getCorpId() + " to address is empty");
            }

        } catch (Throwable ex) {

            logger.info("Received exception while sending alert email, system will ignore and continue processing", ex);
        }
    }

    private enum EmailCriteria {
        AUTH_USER_DELETED;
    }

    public static void deleteUserRoleByCorpId(String pCorpId, String pRoleId) throws DataNotFoundException, BadRequestException {
        com.intuit.sbd.payroll.psp.domain.AuthRole domainAuthRole = com.intuit.sbd.payroll.psp.domain.AuthRole.findRole(pRoleId);
        if (domainAuthRole == null) {
            throw new BadRequestException(AUTH_USER_ROLE_NOT_FOUND);
        }
        AuthUser authUser = getUserByCorpId(pCorpId);
        AuthRole authRoleDTO = AuthRoleToDTOMapper.mapToDTO(domainAuthRole);
        List<AuthRole> authRoleList = authUser.getAuthRoles();
        //Check if User has the role that is to be removed
        if (!authRoleList.contains(authRoleDTO)) {
            throw new BadRequestException(AUTH_USER_ROLE_NOT_FOUND);
        } else if (authRoleList.size() == 1) { //Check if User has only one role
            deleteUserByCorpId(pCorpId);
        } else {//Remove role from the user
            authRoleList.remove(authRoleList.indexOf(authRoleDTO));
            List<String> roleIds = new ArrayList<String>();
            for (AuthRole authRole : authRoleList) {
                roleIds.add(authRole.getRoleId());
            }
            ProcessResult<com.intuit.sbd.payroll.psp.domain.AuthUser> processResult;
            processResult = PayrollServices.userManager.updateUser(authUser.getAuthUserSeq(), authUser.getCorpId(), roleIds, authUser.getFirstName(), authUser.getLastName());
            if (!processResult.isSuccess()) {

                throw new BadRequestException(AUTH_USER_ROLE_NOT_DELETED);
            }
        }
        logger.info("AuthUser with corpid " + authUser.getCorpId() + " role with " + pRoleId + " deleted ");
    }
}


