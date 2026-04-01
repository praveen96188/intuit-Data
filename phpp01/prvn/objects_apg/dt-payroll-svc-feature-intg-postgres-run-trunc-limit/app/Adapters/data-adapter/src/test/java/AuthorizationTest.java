
import com.intuit.ems.psp.adapters.dataadapter.exception.UnauthorizedException;
import com.intuit.ems.psp.adapters.dataadapter.filter.RestAuthenticationFilter;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.sun.jersey.core.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.Configuration;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by charithah418 on 11/16/15.
 */
public class AuthorizationTest {


    @Before
    public void startup() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.beforeEachTest();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void testGeneratingHmac() throws SignatureException,NoSuchAlgorithmException,InvalidKeyException{
        String key = "377f93e5649c31635191c0e22258f5a5744698d3";
        String value = "0001";
        String hmac512 = "66342e0996cf43bb0ce92a3d1e603f084c99ad58068bfb4752224b1e6ab930820759ff32da890ed82b8be9f6f24531452226691c9bc15850c3fe4ff3373577bf";
        assertEquals(hmac512, RestAuthenticationFilter.calculateHMAC(value, key));

    }

    //Test the filter
    @Test
    public void testAuthorizationOfAuthRoleApi() {
        String APP_SIGN_SHA512 = "57e2afc724200c29d82aac9019fa35517ba8d79f79538d3dc16a4b3983bea430396557a091c9d653de28ae4b78f7862674043ed0c88d4470ecb189f4b5cbdf7a";
        //create a contianer request
        RestAuthenticationFilter authenticationFilter = new RestAuthenticationFilter();

        //generating HMAC
        String Signature = authenticationFilter.generateHMACSignature("6F1C874F6BD54F1F8345E0CB421BA5BB", "122420150030", "GET", RestAuthenticationFilter.SIGNATURE_GEN_ALGORITHM.SHA512);

        assertEquals("Signature Generation Problem", Signature, APP_SIGN_SHA512);

        //DELETE call

        APP_SIGN_SHA512 = "74d06b6f1e2e5134b0d76730d3b014fddccbfbc5c4c7376b4e3ce4d60b1d05f66a796ee4bef0d79ab1fd64e19b0e3ec426180e1cc849eb166961d63110672390";

        Signature = authenticationFilter.generateHMACSignature("6F1C874F6BD54F1F8345E0CB421BA5BB", "122420150030", "DELETE", RestAuthenticationFilter.SIGNATURE_GEN_ALGORITHM.SHA512);

        assertEquals("Signature Generation Problem", Signature, APP_SIGN_SHA512);

    }

    @Test
    public void  testValidateAuthenticationHeaderAndGetAppId() {

        String authorization = "HMAC ";
        String httpVerb = "GET";

        try{
             RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("App Id or Secret Corrupt Encoded AppId_AppSignature =" ,e.getMessage());
        }

        authorization = "HMAC";
        httpVerb = "GET";

        //It will try to decode HMAC to unicode
        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("App Id or Secret Corrupt Encoded AppId_AppSignature =HMAC" ,e.getMessage());
        }

        //Enum exception caught
        authorization = "HMAC:";
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("HMAC: Authentication Scheme not Supported" ,e.getMessage());
        }

        authorization = "HMA";
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("HMA Authentication Scheme not Supported" ,e.getMessage());
        }


        authorization = "Basic";
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("Basic Authentication Scheme not Supported" ,e.getMessage());
        }

        authorization = "Basic hiqefjeklfjkejfkejfkejfkjejfekljf";
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("Basic Authentication Scheme not Supported" ,e.getMessage());
        }

        authorization = "HMAChiqefjeklfjkejfkejfkejfkjejfekljf";
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("HMAChiqefjeklfjkejfkejfkejfkjejfekljf Authentication Scheme not Supported" ,e.getMessage());
        }

        authorization = "HMAC hiqefjeklfjkejfkejfkejfkjejfekljf";
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("App Id or Secret Corrupt Encoded AppId_AppSignature =hiqefjeklfjkejfkejfkejfkjejfekljf" ,e.getMessage());
        }

        String appidappSignString = "133:4243";
        String appIdAppSignature = new String(Base64.encode(appidappSignString.getBytes()));
        authorization = "HMAC "+appIdAppSignature;
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("Access Denied App_Id = 133 App_Signature =4243" ,e.getMessage());
        }

        appidappSignString = "133:";
        appIdAppSignature = new String(Base64.encode(appidappSignString.getBytes()));
        authorization = "HMAC "+appIdAppSignature;
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("App Id or Secret Corrupt Encoded AppId_AppSignature =MTMzOg==" ,e.getMessage());
        }

        appidappSignString = ":12349438kjker";
        appIdAppSignature = new String(Base64.encode(appidappSignString.getBytes()));
        authorization = "HMAC "+appIdAppSignature;
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("App Id or Secret Corrupt Encoded AppId_AppSignature =OjEyMzQ5NDM4a2prZXI=" ,e.getMessage());
        }

        appidappSignString = ":";
        appIdAppSignature = new String(Base64.encode(appidappSignString.getBytes()));
        authorization = "HMAC "+appIdAppSignature;
        httpVerb = "GET";

        try{
            RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization,httpVerb);
        }catch (UnauthorizedException e){
            assertEquals("App Id or Secret Corrupt Encoded AppId_AppSignature =Og==" ,e.getMessage());
       }

        //No Exception thrown

        String appid = "6F1C874F6BD54F1F8345E0CB421BA5BB";
        SpcfCalendar timeStamp = PSPDate.getPSPTime();
        String timestamp = timeStamp.format(RestAuthenticationFilter.TIME_STAMP_FORMAT);

        String hmacSignature = RestAuthenticationFilter.generateHMACSignature(appid, timestamp, "GET", RestAuthenticationFilter.SIGNATURE_GEN_ALGORITHM.SHA512);

        appidappSignString = appid + ":" + hmacSignature;

        appIdAppSignature = new String(Base64.encode(appidappSignString.getBytes()));
        authorization = "HMAC "+appIdAppSignature;
        httpVerb = "GET";
        assertEquals(appid, RestAuthenticationFilter.validateAuthenticationHeaderAndGetAppId(authorization, httpVerb));
      }

      @Test
      public void testRoleListForProdAndQA(){
         Set<String> roleList = RestAuthenticationFilter.getRoleList("6F1C874F6BD54F1F8345E0CB421BA5BB");
         assertEquals("[OIMAuthClient]",roleList.toString());
      }
}
