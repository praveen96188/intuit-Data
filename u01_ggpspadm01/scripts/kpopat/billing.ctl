load data
infile ‘/home/oracle/Oct5billing.txt’
append into table SBG_BILLING.PRICEMIGRATIONCAMPAIGN
fields terminated by ‘,’
optionally enclosed by ‘“’ TRAILING NULLCOLS
(rid “sbg_billing.pricemigrationcampaign_seq.nextval”, campaignId, scheduleId, companyId, baseURL, stepNumber, eventStatus, eventPlannedDate date “mm/dd/yy” ,eventExecutedDate date “mm/dd/yy” nullif eventExecutedDate=‘null’, billingPlanName, bucketId, discountId, currentBillingCode, targetBillingCode, sku, nextChargeDate date “mm/dd/yy” nullif nextChargeDate=‘null’ , clusterId, cancelled, notes)
