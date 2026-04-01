package com.intuit.sbd.payroll.psp.adapters.brm.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 9/17/12
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement()
@XmlType(name = "GetUsageBillingDetailRequest")
public class GetUsageBillingDetailRequest {
    private String ein = null;
    private String billDate = null;
    private Boolean viewAll = false;
    private String companyId = null;

    @XmlElement(name = "EIN", required = true)
    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }

    @XmlElement(name = "BillDate", required = true)
    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String pBillDate) {
        billDate = pBillDate;
    }

    @XmlElement(name = "ViewAll", required = false)
    public Boolean getViewAll() {
        return viewAll;
    }

    public void setViewAll(Boolean pViewAll) {
        viewAll = pViewAll;
    }

    @XmlElement(name = "CompanyID", required = false)
    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String pcompanyId) { companyId = pcompanyId; }
}
