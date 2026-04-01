package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: jbhatt
 * Date: Oct 12, 2007
 * Time: 9:25:55 AM
 * To change this template use File | Settings | File Templates.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;

import java.util.ArrayList;
import java.util.Random;

public class AddressDTOTests {
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String state;
    private String zipCode;
    private String zipCodeExtension;
    private String country;
    
    @Test
    public void testTooLongAddressLine1(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        addressLine1 = getSampleString(81);
        addressDTO.setAddressLine1(addressLine1);

        assertTrue(compareMessageStrings("AddressLine1 has invalid value", addressDTO));
    }

    @Test
     public void testTooShortAddressLine1(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        addressLine1 = "";
        addressDTO.setAddressLine1(addressLine1);

        assertTrue(compareMessageStrings("AddressLine1 has invalid value", addressDTO));
    }

     @Test
     public void testInvalidAddressLine2(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        addressLine2 = getSampleString(81);
        addressDTO.setAddressLine2(addressLine2);

        assertTrue(compareMessageStrings("AddressLine2 has invalid value", addressDTO));
    }

    @Test
     public void testInvalidAddressLine3(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        addressLine3 = getSampleString(81);
        addressDTO.setAddressLine3(addressLine3);

        assertTrue(compareMessageStrings("AddressLine3 has invalid value", addressDTO));
    }

    @Test
    public void testTooLongCity(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        city = getSampleString(256);
        addressDTO.setCity(city);

        assertTrue(compareMessageStrings("City has invalid value", addressDTO));
    }

    @Test
    public void testTooShortCity(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        city = "";
        addressDTO.setCity(city);

        assertTrue(compareMessageStrings("City has invalid value", addressDTO));
    }

    @Test
    public void testTooLongState(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        state = getSampleString(22);
        addressDTO.setState(state);

        assertTrue(compareMessageStrings("State has invalid value", addressDTO));
    }

    @Test
    public void testTooShortState(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        state = "";
        addressDTO.setState(state);

        assertTrue(compareMessageStrings("State has invalid value", addressDTO));
    }

    @Test
    public void testTooLongZipCode(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        zipCode = getSampleString(14);
        addressDTO.setZipCode(zipCode);

        assertTrue(compareMessageStrings("ZipCode has invalid value", addressDTO));
    }

    @Test
    public void testTooShortZipCode(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        zipCode = "";
        addressDTO.setZipCode(zipCode);

        assertTrue(compareMessageStrings("ZipCode has invalid value", addressDTO));
    }

    @Test
    public void testInvalidZipCodeExtension(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        zipCodeExtension = getSampleString(22);
        addressDTO.setZipCodeExtension(zipCodeExtension);

        assertTrue(compareMessageStrings("ZipCodeExtension has invalid value", addressDTO));
    }

    @Test
    public void testInvalidCountry(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        //invalid entry
        country = getSampleString(256);
        addressDTO.setCountry(country);

        assertTrue(compareMessageStrings("Country has invalid value", addressDTO));
    }

    @Test
    public void testAllCorrect(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        assertFalse(compareMessageStrings("AddressLine1 has invalid value", addressDTO));
        assertFalse(compareMessageStrings("AddressLine2 has invalid value", addressDTO));
        assertFalse(compareMessageStrings("AddressLine3 has invalid value", addressDTO));
        assertFalse(compareMessageStrings("City has invalid value", addressDTO));
        assertFalse(compareMessageStrings("State has invalid value", addressDTO));
        assertFalse(compareMessageStrings("ZipCode has invalid value", addressDTO));
        assertFalse(compareMessageStrings("ZipCodeExtension has invalid value", addressDTO));
        assertFalse(compareMessageStrings("Country has invalid value", addressDTO));
    }

    @Test
    public void testGetters(){

        AddressDTO addressDTO = new AddressDTO();
        getSampleAddress(addressDTO);

        assertEquals("345 Valencia Street", addressDTO.getAddressLine1());
        assertEquals("Apt # 123", addressDTO.getAddressLine2());
        assertEquals("Room # 2", addressDTO.getAddressLine3());
        assertEquals("Reno", addressDTO.getCity());
        assertEquals("Nevada", addressDTO.getState());
        assertEquals("89509", addressDTO.getZipCode());
        assertEquals("3434", addressDTO.getZipCodeExtension());
        assertEquals("USA", addressDTO.getCountry());
    }


    private boolean compareMessageStrings(String expectedMessage, AddressDTO addressDTO){

        ProcessResult pr = addressDTO.validateAddressDTO();
        ArrayList<Message> messages = pr.getMessages();
        boolean found = false;

        for(int i = 0; i < messages.size(); i ++) {
            Message m = messages.get(i);
            if(m.getMessage().equals(expectedMessage))
                found = true;
        }
        return found;
    }

    private void getSampleAddress(AddressDTO addressDTO){

        addressLine1 = "345 Valencia Street";
        addressLine2 = "Apt # 123";
        addressLine3 = "Room # 2";
        city = "Reno";
        state = "Nevada";
        zipCode = "89509";
        zipCodeExtension = "3434";
        country = "USA";

        addressDTO.setAddressLine1(addressLine1);
        addressDTO.setAddressLine2(addressLine2);
        addressDTO.setAddressLine3(addressLine3);
        addressDTO.setCity(city);
        addressDTO.setState(state);
        addressDTO.setZipCode(zipCode);
        addressDTO.setZipCodeExtension(zipCodeExtension);
        addressDTO.setCountry(country);
    }

    private String getSampleString(int length){

        char b[] = new char[length];
        for (int i=0; i<length; i++){
            b[i] = (char)rand('a', 'z');
        }

        return new String(b);        
    }

    private static int rand(int lo, int hi){

         Random rn = new Random();

            int n = hi - lo + 1;
            int i = rn.nextInt() % n;
            if (i < 0)
                    i = -i;
            return lo + i;
    }
}
