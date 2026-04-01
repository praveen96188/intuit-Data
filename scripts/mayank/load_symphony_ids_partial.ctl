load data
infile '/u01/scripts/mayank/PSP_SymphonyUsage_20220228225626-decrypt-partial.csv'
into table mchoubey.psp_symphony_usage_partial
fields terminated by ','
(Groupid,SiteGeneratorPortalproduct,EventCode,Quantity,Timestamp)
