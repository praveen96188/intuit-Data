load data
infile '/u01/scripts/mayank/psp_property_audit_oos.csv'
into table mchoubey.psp_pa
fields terminated by ','
(PROPERTY_AUDIT_SEQ, REALM_ID)

