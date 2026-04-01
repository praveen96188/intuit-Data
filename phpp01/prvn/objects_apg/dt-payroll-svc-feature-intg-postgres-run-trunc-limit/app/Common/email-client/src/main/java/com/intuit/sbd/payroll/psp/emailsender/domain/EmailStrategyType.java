package com.intuit.sbd.payroll.psp.emailsender.domain;

public enum EmailStrategyType {
    SendGrid,
    SendGridWithAttachments,
    ExactTarget,
    OINP,
    OINPBulkKafka,
    OINPWithAttachments
}
