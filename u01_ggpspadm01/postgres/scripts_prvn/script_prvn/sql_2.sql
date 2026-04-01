SELECT c.relname, count(*) AS buffers FROM pg_buffercache b INNER JOIN pg_class c ON b.relfilenode = pg_relation_filenode(c.oid) AND b.reldatabase IN (0, (SELECT oid FROM pg_database WHERE datname = current_database())) GROUP BY c.relname ORDER BY 2 DESC LIMIT 5;                     

/* update com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission */ update ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION set VERSION=2, REQUEST_DOCUMENT=(select request_document from ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION where created_date >= date '2023-10-01' and SOURCE_SYSTEM_TRANSMISSION_SEQ='b4a4e123-d60a-4011-8dc0-6b567f8f1a86') 
 where  created_date >= date '2023-10-01' and SOURCE_SYSTEM_TRANSMISSION_SEQ='0ed22609-dd10-4db8-9528-0eefaf5b1bb5' and VERSION=2;

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
                     where ((this_.TRANSMISSION_IDENTIFIER = '6b666954-89a8-481d-b34c-564f010f8d83' and this_.COMPANY_ID = 'bdb8993d-4558-4636-805b-8c9f6f6de5fb') and this_.CREATED_DATE > date '2023-10-01');

