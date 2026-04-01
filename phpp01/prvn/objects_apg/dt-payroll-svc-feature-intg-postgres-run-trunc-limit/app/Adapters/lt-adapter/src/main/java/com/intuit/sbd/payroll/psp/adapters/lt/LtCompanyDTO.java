package com.intuit.sbd.payroll.psp.adapters.lt;

import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Mar 28, 2008
 * Time: 2:43:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class LtCompanyDTO {
        public String companyId;
        public String fein;
        public String companyName;
        public String bankAccount;
        public SourceSystemCode sourceSystemId;
        public long token;
        public long cloudToken;
}
