package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Feb 11, 2010
 * Time: 2:58:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThirdParty401kEmployeeInfoWSDTO {
    public Date birthDate;
    public boolean isHighlyCompensatedEmployee;
    public boolean isFamilyMember;
    public BigDecimal ownershipPercentage;
}
