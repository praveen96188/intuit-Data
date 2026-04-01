package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.address;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.DataUtilities;

import java.util.ArrayList;

/**
 * Factory that creates an AddressDTO containing all required QBDTWS information
 */
public class AddressFactory {

    public static AddressDTO getAddress(){
        AddressDTO address = new AddressDTO();
        DataUtilities du = new DataUtilities();

        //Build an ArrayList of state and zip information
        ArrayList<StateZipData> stateZipList = new ArrayList<StateZipData>();
        ArrayList<StateZipData> stateZipData = AddressChoices.getChoices();

        for (StateZipData aStateZipData : stateZipData) {
            for (int x = 0; x < aStateZipData.getMultiplier(); x++) {
                stateZipList.add(aStateZipData);
            }
        }
        //Select a random State/Zip combo
        int selected = (int) ((stateZipList.size()-1) * Math.random()) + 1;

        //Populate the DTO
        address.setState(stateZipList.get(selected).getState());
        address.setZipCode(stateZipList.get(selected).getZip());
        address.setCity(du.getRandomCity());
        address.setAddress1(du.getRandomStreetAddress());

        return address;
    }
}