/* update com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission */ update ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION set VERSION=2, REQUEST_DOCUMENT=(select request_document from ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION where created_date >= date '2023-09-01' and SOURCE_SYSTEM_TRANSMISSION_SEQ='db16dd26-83b1-45a0-bbb3-ac5d640bee0c')  
 where  created_date >= date '2023-09-01' and SOURCE_SYSTEM_TRANSMISSION_SEQ='e10d5345-e47d-403d-a782-a272e2c247f2' and VERSION=2;

 /* criteria query */ select this_.SOURCE_SYSTEM_TRANSMISSION_SEQ as source_s1_252_0_,
                            this_.VERSION                        as version2_252_0_,
                            this_.CREATOR_ID                     as creator_3_252_0_,
                            this_.CREATED_DATE                   as created_4_252_0_,
                            this_.MODIFIER_ID                    as modifier5_252_0_,
                            this_.MODIFIED_DATE                  as modified6_252_0_,
                            this_.REALM_ID                       as realm_id7_252_0_,
                            this_.HOST                           as host8_252_0_,
                            this_.FROM_SOURCE_SYSTEM             as from_sou9_252_0_,
                            this_.FINALIZE_DATE_TIME             as finaliz10_252_0_,
                            this_.REQUEST_TOKEN                  as request11_252_0_,
                            this_.RESPONSE_TOKEN                 as respons12_252_0_,
                            this_.TYPE                           as type15_252_0_,
                            this_.INITIALIZE_DATE_TIME           as initial16_252_0_,
                            this_.DESCRIPTION                    as descrip17_252_0_,
                            this_.TO_SOURCE_SYSTEM               as to_sour18_252_0_,
                            this_.TRANSMISSION_IDENTIFIER        as transmi19_252_0_,
                            this_.I_P_ADDRESS                    as i_p_add20_252_0_,
                            this_.APPLICATION_VERSION            as applica21_252_0_,
                            this_.APPLICATION_ID                 as applica22_252_0_,
                            this_.TAX_TABLE_ID                   as tax_tab23_252_0_,
                            this_.COMPANY_ID                     as company24_252_0_
                     from ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION this_
                     where ((this_.TRANSMISSION_IDENTIFIER = '692f0dae-279d-4304-bc87-f6fac47c62fd' and this_.COMPANY_ID = 'c4c46b58-5c22-48c2-b062-4a12a4ea161a') and this_.CREATED_DATE > date '2023-09-01');

SELECT c.relname, count(*) AS buffers FROM pg_buffercache b INNER JOIN pg_class c ON b.relfilenode = pg_relation_filenode(c.oid) AND b.reldatabase IN (0, (SELECT oid FROM pg_database WHERE datname = current_database())) GROUP BY c.relname ORDER BY 2 DESC LIMIT 5;

