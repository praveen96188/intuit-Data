load data
infile '/u01/scripts/mayank/PSP_SymphonyUsage_20220228225626-decrypt.csv'
into table mchoubey.psp_symphony_usage
fields terminated by ','
(Groupid,SiteGeneratorPortalproduct,EventCode,Quantity,Timestamp)
