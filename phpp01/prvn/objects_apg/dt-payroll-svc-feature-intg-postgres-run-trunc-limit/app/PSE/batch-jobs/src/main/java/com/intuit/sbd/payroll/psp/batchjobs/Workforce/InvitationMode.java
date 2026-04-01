package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

public enum InvitationMode {

    FreshInvites("FreshInvites","BulkWorkforceInviteProcessor"),
    ReEngageERInvited("ReEngageERInvited","BulkWorkforceInviteProcessor_2ER"),
    ReEngageAutoInvited("ReEngageAutoInvited","BulkWorkforceInviteProcessor_2Auto"),
    NewInvitesOnEmployeeAdd("NewInvitesOnEmployeeAdd", "BulkWorkforceInviteProcessor_NewEE");

    private String invitationMode;
    private String invitationSource;

    InvitationMode(String invitationMode, String invitationSource) {
        this.invitationMode = invitationMode;
        this.invitationSource = invitationSource;
    }

    public String getInvitationModeAsString() {
        return this.invitationMode;
    }
    public String getInvitationSource() {return this.invitationSource;}

    @Override
    public String toString(){
        return invitationMode;
    }


}
