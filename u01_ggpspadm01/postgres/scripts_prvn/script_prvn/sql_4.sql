\timing
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
                     where ((this_.TRANSMISSION_IDENTIFIER = '924652f7-4bc4-4b18-afa8-b2ab1b8cc847' and this_.COMPANY_ID = '9791cd0d-aa61-4a88-9a1e-fa8f630536cc')) ;


/* update com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission */ update ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION set VERSION=2,
 REQUEST_DOCUMENT=(select request_document from ibobadm.PSP_SOURCE_SYSTEM_TRANSMISSION where created_date >= date '2023-12-01' and SOURCE_SYSTEM_TRANSMISSION_SEQ='ccbda1c2-9105-4020-80bd-ce8d847323a4')
 where  created_date >= date '2023-12-01' and SOURCE_SYSTEM_TRANSMISSION_SEQ='86f9cca0-7d46-455c-8e5d-fe7f27b4bf64' and VERSION=2;
