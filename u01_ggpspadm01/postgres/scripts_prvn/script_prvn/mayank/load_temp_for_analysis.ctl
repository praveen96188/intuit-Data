load data
infile '/u01/scripts/mayank/Chase-Customer-Accounts-Wallet-IDs-Sheet1.csv'
into table mchoubey.temp_for_analysis
fields terminated by ','
(realm_id,
wallet_entry_id,
original_asset_id,
account_id,
provider_group,
channel_type_name)

