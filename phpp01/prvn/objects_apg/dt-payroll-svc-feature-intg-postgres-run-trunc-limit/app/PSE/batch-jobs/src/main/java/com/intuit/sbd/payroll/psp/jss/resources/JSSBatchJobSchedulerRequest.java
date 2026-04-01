package com.intuit.sbd.payroll.psp.jss.resources;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class JSSBatchJobSchedulerRequest{
    private String[] arg;

    public String[] getArg() {
        return arg;
    }
    public void setArg(String[] arg) {
        this.arg = arg;
    }

    private String jobname;
    private String timerExpression;

    public String getTimerExpression() {
        return timerExpression;
    }

    public void setTimerExpression(String timerExpression) {
        this.timerExpression = timerExpression;
    }

    public String getJobname() {
        return jobname;
    }
    public void setJobname(String jobname) {
        this.jobname = jobname;
    }
}
