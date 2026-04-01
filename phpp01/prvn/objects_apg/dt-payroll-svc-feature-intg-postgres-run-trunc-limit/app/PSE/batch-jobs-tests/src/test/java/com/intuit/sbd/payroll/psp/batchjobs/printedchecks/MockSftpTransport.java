package com.intuit.sbd.payroll.psp.batchjobs.printedchecks;

import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.jcraft.jsch.ChannelSftp;

public class MockSftpTransport extends Transporter {
    private static ChannelSftp mockChannelSftp;

    public MockSftpTransport(String pHost, String pUser, String passKey, boolean useKeyAuth) {
        super(pHost, pUser, passKey, useKeyAuth);
    }

    public static void setMockChannelSftp(ChannelSftp mockChannelSftp) {
        MockSftpTransport.mockChannelSftp = mockChannelSftp;
    }

    @Override
    public void connect() {
        // no op
    }

    @Override
    public ChannelSftp getChannelSftp() {
        return mockChannelSftp;
    }
}
