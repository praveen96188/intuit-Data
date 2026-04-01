package com.intuit.sbd.payroll.psp.iam;

import com.intuit.cto.auth.utils.AuthHeaderUtils;
import com.intuit.platform.jsk.security.iam.authn.IntuitTicketAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerErrorException;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

import static com.intuit.cto.auth.utils.AuthHeaderUtils.INTUIT_TOKEN;
import static com.intuit.cto.auth.utils.AuthHeaderUtils.INTUIT_USERID;

@Slf4j
public class HeaderUtils {
    public static String fetchAuthId(String authorization){

        Map<String, String> map = AuthHeaderUtils.splitAuthorizationFields(HeaderUtils.class.getCanonicalName(), authorization);
        if (!isAuthenticationAttempt(map)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "DIS Adapter: Authorization Header is not valid. ");
        }
        String authId = map.get(INTUIT_USERID);
        log.info("AuthId: " + authId);
        return Objects.nonNull(authId) ? authId : null;
    }

    public static boolean isAuthenticationAttempt(@Nonnull Map<String, String> authorizationFields) {
        return authorizationFields != null
                && authorizationFields.get(INTUIT_TOKEN) != null
                && authorizationFields.get(INTUIT_USERID) != null;
    }

    public static boolean isOfflineTicket(){
        IntuitTicketAuthentication intuitTicketAuthentication = (IntuitTicketAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (intuitTicketAuthentication == null) {
            log.error("IntuitTicketAuthentication object is not exists in the Context");
            throw new ServerErrorException("Server Error occurred",new IllegalArgumentException("IntuitTicketAuthentication cannot be null"));
        }
        return intuitTicketAuthentication.getIAMTicket().isOffline();
    }
}
