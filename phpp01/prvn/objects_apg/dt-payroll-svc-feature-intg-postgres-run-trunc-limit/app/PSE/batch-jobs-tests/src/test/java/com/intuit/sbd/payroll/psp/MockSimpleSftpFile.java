package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchListener;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;

import java.io.File;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 15, 2011
 * Time: 2:46:02 PM
 */
public class MockSimpleSftpFile extends Transporter {

    private JSchListener mListener;

    public MockSimpleSftpFile(String pHost, String pUsername, String passKey, boolean useKeyAuth) {
        super(pHost, pUsername, passKey, useKeyAuth);
    }

    @Override
    public void connect() {
        // no op
    }

    @Override
    public void changeLocalDir(String pDir) {
        // no op
    }

    @Override
    public void changeRemoteDir(String pDir) {
        // no op
    }

    @Override
    public void addListener(JSchListener pListener) {
        mListener = pListener;
    }

    @Override
    public void uploadFile(String pFile) {
        try {
            mListener.upload(new FileBean("", pFile, 0, "0"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadFiles(Collection<String> pFileList, int pMillisDelayBetweenFiles) {
        try {
            super.uploadFiles(pFileList, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadFile(String pLocalFile, String pRemoteFile) {
        mListener.upload(new FileBean(pRemoteFile, pLocalFile, 0, "0"));
    }

    @Override
    public File downloadFile(String pFile) {
        try {
            mListener.download(new FileBean("", pFile, 0, "0"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new File("test");
    }

    @Override
    public void deleteRemoteFile(String pFile) {
        try {
            mListener.download(new FileBean("", pFile, 0, "0"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
