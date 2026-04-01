package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.address;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: msalayko-admin Date: Apr 26, 2010 Time: 10:35:53 AM To change this template use File
 * | Settings | File Templates.
 */
public class AddressChoices {

    public static ArrayList<StateZipData> getChoices(){

        return new ArrayList<StateZipData>(){
            {
                add(new StateZipData("AL", "35236",1));   add(new StateZipData("GA", "30006",1));   add(new StateZipData("MD", "21401",1));   add(new StateZipData("NJ", "07105",1));   add(new StateZipData("RI", "02906",1));
                add(new StateZipData("AK", "99509",1));   add(new StateZipData("HI", "96813",1));   add(new StateZipData("MA", "02120",1));   add(new StateZipData("NM", "87106",1));   add(new StateZipData("SC", "29204",1));
                add(new StateZipData("AZ", "85011",1));   add(new StateZipData("ID", "83301",1));   add(new StateZipData("MI", "48104",1));   add(new StateZipData("NM", "87106",1));   add(new StateZipData("SD", "57104",1));
                add(new StateZipData("AR", "72204",1));   add(new StateZipData("IL", "60621",1));   add(new StateZipData("MN", "55104",1));   add(new StateZipData("NM", "87106",1));   add(new StateZipData("TN", "37207",1));
                add(new StateZipData("CA", "96161",3));   add(new StateZipData("IN", "46202",1));   add(new StateZipData("MS", "39203",1));   add(new StateZipData("NC", "27406",1));   add(new StateZipData("TX", "57211",1));
                add(new StateZipData("CO", "80301",1));   add(new StateZipData("IA", "50316",1));   add(new StateZipData("MO", "63107",1));   add(new StateZipData("ND", "58102",1));   add(new StateZipData("UT", "84106",1));
                add(new StateZipData("CT", "06058",1));   add(new StateZipData("KS", "66103",1));   add(new StateZipData("MT", "59105",1));   add(new StateZipData("OH", "43701",1));   add(new StateZipData("VT", "05401",1));
                add(new StateZipData("DE", "19801",1));   add(new StateZipData("KY", "40206",1));   add(new StateZipData("NE", "68101",1));   add(new StateZipData("OK", "73101",1));   add(new StateZipData("VA", "22032",1));
                add(new StateZipData("DC", "20001",1));   add(new StateZipData("LA", "70126",1));   add(new StateZipData("NV", "89502",1));   add(new StateZipData("OR", "97045",1));   add(new StateZipData("WA", "98006",1));
                add(new StateZipData("FL", "32114",1));   add(new StateZipData("ME", "04102",1));   add(new StateZipData("NH", "03801",1));   add(new StateZipData("PA", "15204",1));   add(new StateZipData("WV", "25311",1));
                add(new StateZipData("WI", "53209",1));   add(new StateZipData("WY", "82071",1));
            }
        };
    }

}
