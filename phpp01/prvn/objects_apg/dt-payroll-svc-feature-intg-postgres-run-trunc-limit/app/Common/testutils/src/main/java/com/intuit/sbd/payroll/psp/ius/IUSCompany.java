package com.intuit.sbd.payroll.psp.ius;

import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.platform.integration.ius.common.types.Realm;
import com.intuit.platform.integration.ius.common.types.User;

public class IUSCompany {

    private User user;

    private IAMTicket iamTicket;

    private Realm realm;

    private Grant grant;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public IAMTicket getIamTicket() {
        return iamTicket;
    }

    public void setIamTicket(IAMTicket iamTicket) {
        this.iamTicket = iamTicket;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public Grant getGrant() {
        return grant;
    }

    public void setGrant(Grant grant) {
        this.grant = grant;
    }
}
