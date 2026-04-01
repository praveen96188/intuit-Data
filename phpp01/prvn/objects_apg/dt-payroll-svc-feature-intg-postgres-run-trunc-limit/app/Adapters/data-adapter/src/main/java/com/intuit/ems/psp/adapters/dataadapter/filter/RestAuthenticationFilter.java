package com.intuit.ems.psp.adapters.dataadapter.filter;

import com.intuit.ems.psp.adapters.dataadapter.exception.UnauthorizedException;
import com.intuit.ems.psp.adapters.dataadapter.helper.AuthorizationHelper;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.String;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;


/**
 * Created by charithah418 on 11/10/15.
 * the public access key or api key (aka username) - app-id ;
 * the client calculated signature - app-secret ;
 */
public class RestAuthenticationFilter implements ContainerRequestFilter {

    private static final String AUTHORIZATION_PROPERTY = "Authorization";

    private static enum AUTHENTICATION_SCHEMES {Basic, OAuth, HMAC}

    public static enum SIGNATURE_GEN_ALGORITHM {SHA512}

    private static final String ACCESS_DENIED = "Access Denied";
    private static final String CORRUPT_CREDENTIALS = "App Id or Secret Corrupt";
    private static final String NON_SUPPORTED_AUTHENTICATION_SCHEMES = "Authentication Scheme not Supported";
    private static final int SIGNATURE_TIMEOUT_MINUTES = 1;
    public static final String TIME_STAMP_FORMAT = "MMddyyyyHHmm";
    private static final SpcfLogger logger = PayrollServices.getLogger(RestAuthenticationFilter.class);

    public ContainerRequest filter(ContainerRequest pContainerRequest) {

        final MultivaluedMap<String, String> requestHeaders = pContainerRequest.getRequestHeaders();
        final String authorization = requestHeaders.getFirst(AUTHORIZATION_PROPERTY);

        if (StringUtils.isBlank(authorization)) {
            logger.warn("Code:Authorization "+ AUTHORIZATION_PROPERTY+" = blank" + ACCESS_DENIED);
            throw new UnauthorizedException(ACCESS_DENIED);
        }
        String appId = validateAuthenticationHeaderAndGetAppId(authorization, pContainerRequest.getMethod());
        Set<String> roleSet = RestAuthenticationFilter.getRoleList(appId);
        AuthorizationHelper authorizationHelper = new AuthorizationHelper(roleSet, appId, true);
        pContainerRequest.setSecurityContext(authorizationHelper);

        return pContainerRequest;
    }

    private static Boolean isValidAuthenticationSchemeEnum(String SelectedAuthenticationScheme){
       for(AUTHENTICATION_SCHEMES authentication_scheme : AUTHENTICATION_SCHEMES.values()){
            if(authentication_scheme.name().equals(SelectedAuthenticationScheme)){
                return true;
            }
        }
       return false;
    }

    public static String validateAuthenticationHeaderAndGetAppId(String authorization, String httpVerb) throws UnauthorizedException{
        String authenticationScheme = authorization.split(" ")[0];

        if(!RestAuthenticationFilter.isValidAuthenticationSchemeEnum(authenticationScheme)){
            logger.warn("Authorization scheme = "+ authenticationScheme + " is " + NON_SUPPORTED_AUTHENTICATION_SCHEMES);
            throw new UnauthorizedException( authenticationScheme + " " + NON_SUPPORTED_AUTHENTICATION_SCHEMES);
        }

        switch (AUTHENTICATION_SCHEMES.valueOf(authenticationScheme)) {
            case HMAC:
                String encodedAppIdAppSignature = null;
                String appIdAppSignature;
                StringTokenizer tokenizer;
                String appId = null;
                String appSignature = null;
                try {
                    encodedAppIdAppSignature = authorization.replaceFirst(AUTHENTICATION_SCHEMES.HMAC+" ", "");
                    appIdAppSignature = new String(Base64.decode(encodedAppIdAppSignature.getBytes()));
                    tokenizer = new StringTokenizer(appIdAppSignature, ":");
                    appId = tokenizer.nextToken();
                    appSignature = tokenizer.nextToken();

                    if (!validateHMACSignature(appId, appSignature, httpVerb)) {
                        logger.warn("Authorization =  " + ACCESS_DENIED);
                        throw new UnauthorizedException( ACCESS_DENIED + " App_Id = " + appId + " App_Signature =" + appSignature);
                    }

                    return appId;

                }catch (NoSuchElementException exception) {
                    logger.warn("Encoded AppId_AppSignature ="+ encodedAppIdAppSignature);
                    logger.warn(CORRUPT_CREDENTIALS + " App_Id = "+appId+" App_Signature ="+appSignature  );
                    throw new UnauthorizedException(CORRUPT_CREDENTIALS + " Encoded AppId_AppSignature ="+ encodedAppIdAppSignature );
                }
            default:
                logger.warn("Authorization scheme = "+ authenticationScheme + " is " + NON_SUPPORTED_AUTHENTICATION_SCHEMES);
                throw new UnauthorizedException( authenticationScheme+" " + NON_SUPPORTED_AUTHENTICATION_SCHEMES);
        }
    }

    public static Boolean validateHMACSignature(String appId, String appSignature, String httpVerb) {
            SpcfCalendar timeStamp = PSPDate.getPSPTime();
            String fnTimeStamp;
            final String APP_ID = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_dataadapter_oim_app_id");

            for (int count = 0; count <= SIGNATURE_TIMEOUT_MINUTES; count++) {

                timeStamp.addMinutes(-count);
                fnTimeStamp = timeStamp.format(TIME_STAMP_FORMAT);
                String APP_SIGNATURE = generateHMACSignature(APP_ID, fnTimeStamp, httpVerb, SIGNATURE_GEN_ALGORITHM.SHA512);

                if (appId.equals(APP_ID) && appSignature.equals(APP_SIGNATURE)) {
                    return true;
                }
            }
            return false;
    }

    public static Set<String> getRoleList(String appId){
        Set<String> roles = new HashSet<String>();
        String rolesList = ConfigurationManager.getSettingValue(ConfigurationModule.Common, appId + "_roles");
        if (rolesList.trim().length() == 0) {
            throw new UnauthorizedException("Roles List Empty");
        }
        Set<String> roleList = new HashSet<String>(Arrays.asList(rolesList.split(",")));
        roles.addAll(roleList);
        return roles;
    }

    public static String generateHMACSignature(String appId, String timeStamp, String httpVerb, SIGNATURE_GEN_ALGORITHM signatureGenAlgorithm) {
        final String APP_SECRET = ConfigurationManager.getSettingValue(ConfigurationModule.Common, appId + "_secret");
        String appSignature = httpVerb + appId + timeStamp;
        switch (signatureGenAlgorithm) {
            case SHA512:
                try {
                    appSignature = calculateHMAC(appSignature, APP_SECRET);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    logger.warn("Authorization = Failed Unsupported Algorithm ");
                } catch (SignatureException e) {
                    e.printStackTrace();
                    logger.warn("Authorization = Failed Secret Key Corrupt ");
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                    logger.warn("Authorization = Failed Invalid Secret Key ");
                }
        }
        return appSignature;
    }


    public static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String calculateHMAC(String data, String key) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        final String HMAC_SHA512_ALGORITHM = "HmacSHA512";
        SecretKeySpec signingKey=new SecretKeySpec(key.getBytes(), HMAC_SHA512_ALGORITHM);
        Mac mac=Mac.getInstance(HMAC_SHA512_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }

}

