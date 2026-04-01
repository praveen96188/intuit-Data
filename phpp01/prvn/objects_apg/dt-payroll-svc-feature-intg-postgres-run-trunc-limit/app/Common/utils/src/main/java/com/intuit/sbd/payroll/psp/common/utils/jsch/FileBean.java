package com.intuit.sbd.payroll.psp.common.utils.jsch;

/**
 * Created by RVL on 7/3/2017.
 * FilBean for JSCH Connected & Disconnected Events
 */
public class FileBean {

    private String mPath;
    private String mFilename;
    private long mFilesize;
    private String mTime;

    public FileBean() {}

    public FileBean(String pPath, String pFilename, long pFilesize, String pTime) {
        this.mPath = pPath;
        this.mFilename = pFilename;
        this.mFilesize = pFilesize;
        this.mTime = pTime;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String pPath) {
        this.mPath = pPath;
    }

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String pFilename) {
        this.mFilename = pFilename;
    }

    public long getFilesize() {
        return mFilesize;
    }

    public void setFilesize(long pFilesize) {
        this.mFilesize = pFilesize;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String pTime) {
        this.mTime = pTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(", mFilename=");
        builder.append(mFilename);
        builder.append(", mPath=");
        builder.append(mPath);
        builder.append(", mFilesize=");
        builder.append(mFilesize);
        builder.append(", mTime=");
        builder.append(mTime);
        builder.append("]");
        return builder.toString();
    }

}
