package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class PstubAddress extends BasePstubAddress {

	/**
	 * Default constructor.
	 */
	public PstubAddress()
	{
		super();
	}

    public static PstubAddress createPstubAddress(String pLine1, String pLine2, String pLine3, String pLine4, String pLine5) {
        PstubAddress pstubAddress = new PstubAddress();
        pstubAddress.setLine1(pLine1);
        pstubAddress.setLine2(pLine2);
        pstubAddress.setLine3(pLine3);
        pstubAddress.setLine4(pLine4);
        pstubAddress.setLine5(pLine5);
        Application.save(pstubAddress);
        return pstubAddress;
    }
}