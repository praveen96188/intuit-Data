load data
infile '/tmp/wc_company_temp.csv'
into table pspadm.psp_wc_company_temp
fields terminated by ',' optionally enclosed by '"'
(
PSP_COMPANY_ID,
STATUS,
SUBSCRIPTION_PROVIDER_ID,
INTEGRATION_ID,
BUSINESS_NAME,
Subscription_provider_name,
Status_Name
)

