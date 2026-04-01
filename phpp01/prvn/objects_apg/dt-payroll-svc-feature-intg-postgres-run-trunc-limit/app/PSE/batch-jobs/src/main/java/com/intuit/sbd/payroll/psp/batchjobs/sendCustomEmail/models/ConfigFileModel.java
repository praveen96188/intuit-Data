package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ConfigFileModel {
    private String workflowName;
    private String relativeDir;
    // 24hr format. Eg: 09:00, 14:30, 23:59
    private String scheduledTime;
    private boolean overrideScheduleTime;
}
