UPDATE PSP_PAYMENT_TEMPLATE pt 
SET SUPPORT_START_DATE = null,
    NO_CALCULATION = 0
;

update psp_payment_template
set support_start_date = timestamp '1999-01-01 08:00:00'
where payment_template_cd in
('IRS-940-PAYMENT',
 'IRS-941-PAYMENT',
 'AR-941M-PAYMENT',
 'AZ-A1-PAYMENT',
 'CA-PITSDI-PAYMENT',
 'CO-DR1094-PAYMENT',
 'MN-MW1-PAYMENT',
 'MT-MW1-PAYMENT',
 'NC-NC5P-PAYMENT',
 'OH-IT501-PAYMENT',
 'OR-OTCWH-PAYMENT',
 'OR-STTV-PAYMENT',
 'WA-PFML-PAYMENT',
 'SC-WH1601-PAYMENT',
 'AK-AKNS-PAYMENT',
 'AL-CR4UI-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'AL-CR4WH-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'AR-209B-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'AZ-UC018-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'CA-UIETT-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'CO-UITR1-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'CT-2MAG-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'CT-CTWH-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'DC-FR900-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'DC-UC30-PAYMENT',
 'DC-PFL-PAYMENT',
 'DE-DES-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'DE-UC8-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'FL-UCT6-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'GA-DOL4-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'GA-GAV-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'HI-UCB6-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'HI-VP1-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'IA-44105-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'IA-600103-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
 'ID-020-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'ID-910-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'IL-501-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'IL-UI340-PAYMENT', 
 'KS-KCNS100-PAYMENT',
 'KS-KW5-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'KY-K1-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
 'KY-UI3-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'LA-ES61-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'LA-L1-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
 'MA-1700HI-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
 'MA-M941-PAYMENT',
 'MA-PFML-PAYMENT',
 'MD-DLLR-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'MD-MW506-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'ME-900ME-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'ME-941C1ME-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
 'MI-MW106-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'MI-UIA1020-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
 'MN-DEED1-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'MO-941-PAYMENT',
 'MO-MODES-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'MS-M89-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'MS-UI23-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'MT-UI5-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'NC-101-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'ND-306-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
 'ND-SFN41263-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
 'NE-941N-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'NE-UI11T-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'NH-DES200-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
 'NJ-NJ927PUI-PAYMENT',
 'NJ-NJ927PWH-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
 'NM-CRS1-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'NM-ES903A-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
 'NM-WC1-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'NV-NUCS4072-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
 'NY-1MN-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'NY-45MN-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'NY-MTA305-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
 'OH-JFS20127-PAYMENT',
 'OK-OES3-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'OK-OW9A-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'OR-OTCUI-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'PA-501-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'PA-UC2-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'RI-941-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'RI-TX17-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'SC-UCE120-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
 'SD-UID21-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'TN-LB0456-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
 'TX-C3V-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'UT-F3-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
 'UT-TC96-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'VA-FC20-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'VA-VA15-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'VT-C101-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'VT-WH433-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'WA-F5208-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'WI-UCT101-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
 'WI-WT6-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
 'WV-A154-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
 'WV-IT101-PAYMENT',                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
 'WY-WYO056-PAYMENT',
 'CT-PFML-PAYMENT',
 'WA-CARES-PAYMENT',
 'CO-FAMLI-PAYMENT',
 'OR-PFMSL-PAYMENT',
 'DE-PFML-PAYMENT')
;

update psp_payment_template
set processing_start_date = timestamp '2011-04-01 00:00:00'
WHERE PAYMENT_TEMPLATE_CD  IN ('IRS-940-PAYMENT', 'IRS-941-PAYMENT')
;

update psp_payment_template
set processing_start_date = timestamp '2011-10-01 00:00:00'
where payment_template_cd in
('AR-941M-PAYMENT',
 'AZ-A1-PAYMENT',
 'CA-PITSDI-PAYMENT',
 'CO-DR1094-PAYMENT',
 'MN-MW1-PAYMENT',
 'MT-MW1-PAYMENT',
 'NC-NC5P-PAYMENT',
 'OH-IT501-PAYMENT',
 'OR-OTCWH-PAYMENT',
 'SC-WH1601-PAYMENT')
 ;

update psp_payment_template
set processing_start_date = timestamp '2012-01-01 00:00:00'
where payment_template_cd in
('AK-AKNS-PAYMENT',
 'AL-CR4UI-PAYMENT',
 'AL-CR4WH-PAYMENT',
 'AR-209B-PAYMENT',
 'AZ-UC018-PAYMENT',
 'CA-UIETT-PAYMENT',
 'CO-UITR1-PAYMENT',
 'CT-2MAG-PAYMENT',
 'CT-CTWH-PAYMENT',
 'DC-FR900-PAYMENT',
 'DC-UC30-PAYMENT',
 'DE-DES-PAYMENT',
 'DE-UC8-PAYMENT',
 'FL-UCT6-PAYMENT',
 'GA-DOL4-PAYMENT',
 'GA-GAV-PAYMENT',
 'HI-UCB6-PAYMENT',
 'HI-VP1-PAYMENT',
 'IA-44105-PAYMENT',
 'IA-600103-PAYMENT',
 'ID-020-PAYMENT',
 'ID-910-PAYMENT',
 'IL-501-PAYMENT',
 'IL-UI340-PAYMENT',
 'KS-KCNS100-PAYMENT',
 'KS-KW5-PAYMENT',
 'KY-K1-PAYMENT',
 'KY-UI3-PAYMENT',
 'LA-ES61-PAYMENT',
 'LA-L1-PAYMENT',
 'MA-1700HI-PAYMENT',
 'MA-M941-PAYMENT',
 'MD-DLLR-PAYMENT',
 'MD-MW506-PAYMENT',
 'ME-900ME-PAYMENT',
 'ME-941C1ME-PAYMENT',
 'MI-MW106-PAYMENT',
 'MI-UIA1020-PAYMENT',
 'MN-DEED1-PAYMENT',
 'MO-941-PAYMENT',
 'MO-MODES-PAYMENT',
 'MS-M89-PAYMENT',
 'MS-UI23-PAYMENT',
 'MT-UI5-PAYMENT',
 'NC-101-PAYMENT',
 'ND-306-PAYMENT',
 'ND-SFN41263-PAYMENT',
 'NE-941N-PAYMENT',
 'NE-UI11T-PAYMENT',
 'NH-DES200-PAYMENT',
 'NJ-NJ927PUI-PAYMENT',
 'NJ-NJ927PWH-PAYMENT',
 'NM-CRS1-PAYMENT',
 'NM-ES903A-PAYMENT',
 'NM-WC1-PAYMENT',
 'NV-NUCS4072-PAYMENT',
 'NY-1MN-PAYMENT',
 'NY-45MN-PAYMENT',
 'NY-MTA305-PAYMENT',
 'OH-JFS20127-PAYMENT',
 'OK-OES3-PAYMENT',
 'OK-OW9A-PAYMENT',
 'OR-OTCUI-PAYMENT',
 'PA-501-PAYMENT',
 'PA-UC2-PAYMENT',
 'RI-941-PAYMENT',
 'RI-TX17-PAYMENT',
 'SC-UCE120-PAYMENT',
 'SD-UID21-PAYMENT',
 'TN-LB0456-PAYMENT',
 'TX-C3V-PAYMENT',
 'UT-F3-PAYMENT',
 'UT-TC96-PAYMENT',
 'VA-FC20-PAYMENT',
 'VA-VA15-PAYMENT',
 'VT-C101-PAYMENT',
 'VT-WH433-PAYMENT',
 'WA-F5208-PAYMENT',
 'WI-UCT101-PAYMENT',
 'WI-WT6-PAYMENT',
 'WV-A154-PAYMENT',
 'WV-IT101-PAYMENT',
 'WY-WYO056-PAYMENT')
;

update psp_payment_template
set processing_start_date = timestamp '2018-04-01 00:00:00'
where payment_template_cd in
(
'OR-STTV-PAYMENT'
)
;

update psp_payment_template
set processing_start_date = timestamp '2019-01-01 00:00:00'
where payment_template_cd in
(
'WA-PFML-PAYMENT'
)
;


update psp_payment_template
set processing_start_date = timestamp '2019-04-01 00:00:00'
where payment_template_cd in
(
'DC-PFL-PAYMENT'
)
;


update psp_payment_template
set processing_start_date = timestamp '2019-07-01 00:00:00'
where payment_template_cd in
(
'MA-PFML-PAYMENT'
)
;

update psp_payment_template
set processing_start_date = timestamp '2020-01-01 00:00:00'
where payment_template_cd in
(
'CT-PFML-PAYMENT'
)
;

update psp_payment_template
set processing_start_date = timestamp '2022-01-01 00:00:00'
where payment_template_cd in
(
'WA-CARES-PAYMENT'
)
;
update psp_payment_template
set processing_start_date = timestamp '2023-01-01 00:00:00'
where payment_template_cd in
('CO-FAMLI-PAYMENT',
'OR-PFMSL-PAYMENT')
;
update psp_payment_template
set processing_start_date = timestamp '2023-01-01 00:00:00'
where payment_template_cd in
      ('DE-PFML-PAYMENT')
;
COMMIT
;
-- CREATE NO CALC PAYMENT TEMPLATES - USED FOR LAWS THAT ARE NOT BEING PAID AND THEREFORE HAVE NO
-- RULES ASSOCIATED WITH THEM.
DROP TABLE IF EXISTS TMP_PSP_PAYMENT_TEMPLATE CASCADE

;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TMP_PSP_PAYMENT_TEMPLATE (LIKE PSP_PAYMENT_TEMPLATE INCLUDING ALL)

;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('AK-NOCALC','NOCALC', 'AK-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('AL-NOCALC','NOCALC', 'AL-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('AZ-NOCALC','NOCALC', 'AZ-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('CA-NOCALC','NOCALC', 'CA-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('CO-NOCALC','NOCALC', 'CO-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('DC-NOCALC','NOCALC', 'DC-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('FL-NOCALC','NOCALC', 'FL-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('GA-NOCALC','NOCALC', 'GA-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('HI-NOCALC', 'NOCALC',  'HI-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('IL-NOCALC','NOCALC', 'IL-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('KY-NOCALC', 'NOCALC',  'KY-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('LA-NOCALC','NOCALC', 'LA-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('MA-NOCALC', 'NOCALC',  'MA-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('MD-NOCALC', 'NOCALC',  'MD-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('ME-NOCALC', 'NOCALC',  'ME-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('MI-NOCALC', 'NOCALC',  'MI-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('MO-NOCALC', 'NOCALC',  'MO-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('MT-NOCALC', 'NOCALC',  'MT-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('NC-NOCALC', 'NOCALC',  'NC-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('NH-NOCALC', 'NOCALC',  'NH-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('NM-NOCALC', 'NOCALC',  'NM-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('NV-NOCALC', 'NOCALC',  'NV-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('NY-NOCALC', 'NOCALC',  'NY-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('OK-NOCALC', 'NOCALC',  'OK-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('OR-NOCALC', 'NOCALC',  'OR-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('PR-NOCALC', 'NOCALC',  'PR-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('RI-NOCALC', 'NOCALC',  'RI-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('SD-NOCALC', 'NOCALC',  'SD-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('TN-NOCALC', 'NOCALC',  'TN-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('TX-NOCALC', 'NOCALC',  'TX-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('VA-NOCALC', 'NOCALC',  'VA-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('VT-NOCALC', 'NOCALC',  'VT-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('WA-NOCALC', 'NOCALC',  'WA-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('WY-NOCALC', 'NOCALC',  'WY-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('177-NOCALC', 'NOCALC',  '177-NOCALC', 0, 1)
;
INSERT INTO TMP_PSP_PAYMENT_TEMPLATE (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV,VERSION, NO_CALCULATION)
VALUES ('9-NOCALC', 'NOCALC',  '9-NOCALC', 0, 1)
;
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_PAYMENT_TEMPLATE
   (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV, NO_CALCULATION, VERSION)
SELECT
    PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV, NO_CALCULATION, 0 
FROM
   TMP_PSP_PAYMENT_TEMPLATE tt
WHERE
   tt.PAYMENT_TEMPLATE_CD NOT IN (SELECT PAYMENT_TEMPLATE_CD FROM PSP_PAYMENT_TEMPLATE)

;

UPDATE PSP_PAYMENT_TEMPLATE rt
SET (PAYMENT_TEMPLATE_CD, AGENCY_FK, PAYMENT_TEMPLATE_ABBREV, NO_CALCULATION) =
(SELECT  tt.PAYMENT_TEMPLATE_CD, tt.AGENCY_FK, tt.PAYMENT_TEMPLATE_ABBREV, tt.NO_CALCULATION
 FROM TMP_PSP_PAYMENT_TEMPLATE tt WHERE tt.PAYMENT_TEMPLATE_CD = rt.PAYMENT_TEMPLATE_CD)
 WHERE rt.PAYMENT_TEMPLATE_CD IN (SELECT PAYMENT_TEMPLATE_CD FROM TMP_PSP_PAYMENT_TEMPLATE)
;



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TMP_PSP_PAYMENT_TEMPLATE

;
COMMIT
;
-- DEFAULT_DEPOSIT_FREQUENCY

---  Weekly
UPDATE PSP_PAYMENT_TEMPLATE rt 
SET DEFAULT_DEPOSIT_FREQUENCY ='WEEKLY'
WHERE PAYMENT_TEMPLATE_CD IN 
('CT-CTWH-PAYMENT',
 'NJ-NJ500-PAYMENT',
 'NJ-NJ500UI-PAYMENT',
 'NJ-NJ927PWH-PAYMENT')
;
--- Semiweekly
UPDATE PSP_PAYMENT_TEMPLATE rt 
SET DEFAULT_DEPOSIT_FREQUENCY ='SEMIWEEKLY'
WHERE PAYMENT_TEMPLATE_CD IN 
('IRS-941-PAYMENT', 
 'AZ-A1-PAYMENT',
 'CA-PITSDI-PAYMENT',
 'MN-MW1-PAYMENT',
 'MN_UI',
 'MN_WH',
 'OR-OTCWH-PAYMENT',
 'SC-WH1601-PAYMENT',
 'VA-VA15-PAYMENT')
;

--- Monthly
UPDATE PSP_PAYMENT_TEMPLATE rt 
SET DEFAULT_DEPOSIT_FREQUENCY ='MONTHLY'
WHERE PAYMENT_TEMPLATE_CD IN 
('AL-CR4WH-PAYMENT',
 'AR-941M-PAYMENT',
 'CO-DR1094-PAYMENT',
 'DC-FR900-PAYMENT',
 'DE-DES-PAYMENT',
 'GA-GAV-PAYMENT',
 'HI-VP1-PAYMENT',
 'IA-44105-PAYMENT',
 'ID-910-PAYMENT',
 'IL-501-PAYMENT',
 'IN-WH1-PAYMENT',
 'MA-M941-PAYMENT',
 'MI-MW106-PAYMENT',
 'MO-941-PAYMENT',
 'MS-M89-PAYMENT',
 'MT-MW1-PAYMENT',
 'NC-NC5P-PAYMENT',
 'NE-941N-PAYMENT',
 'NM-CRS1-PAYMENT',
 'OH-IT501-PAYMENT',
 'OH-SD101-PAYMENT',
 'OK-OW9A-PAYMENT',
 'RI-941-PAYMENT',
 'UT-TC96-PAYMENT',
 'VT-WH433-PAYMENT',
 'WV-IT101-PAYMENT')
;

--- Semimonthly
UPDATE PSP_PAYMENT_TEMPLATE rt 
SET DEFAULT_DEPOSIT_FREQUENCY ='SEMIMONTHLY'
WHERE PAYMENT_TEMPLATE_CD IN 
('KS-CNS100-PAYMENT',
 'KS-KW5-PAYMENT',
 'LA-L1-PAYMENT',
 'PA-501-PAYMENT',
 'WI-WT6-PAYMENT')
;

--- TwiceMonthly
UPDATE PSP_PAYMENT_TEMPLATE rt 
SET DEFAULT_DEPOSIT_FREQUENCY ='TWICEMONTHLY'
WHERE PAYMENT_TEMPLATE_CD IN 
('KY-K1-PAYMENT')
;

--- Quarterly
UPDATE PSP_PAYMENT_TEMPLATE rt 
SET DEFAULT_DEPOSIT_FREQUENCY ='QUARTERLY'
WHERE PAYMENT_TEMPLATE_CD IN 
('ME-900ME-PAYMENT',
 'ME-941C1ME-PAYMENT',
 'ND-306-PAYMENT',
 'ND-SFN41263-PAYMENT',
 'NV-NUCS4072-PAYMENT',
 'NY-1MN-PAYMENT',
 'NY-45MN-PAYMENT',
 'NY-SDI-PAYMENT',
 'NY-MTA305-PAYMENT',
 'AK-AKNS-PAYMENT',
 'FL-UCT6-PAYMENT',
 'IRS-940-PAYMENT',
 'NH-DES200-PAYMENT',
 'SD-UID21-PAYMENT',
 'TN-LB0456-PAYMENT',
 'TX-C3V-PAYMENT',
 'WA-F5208-PAYMENT',
 'WY-WYO056-PAYMENT',
 'AL-CR4UI-PAYMENT',
 'AR-209B-PAYMENT',
 'AZ-UC018-PAYMENT',
 'CA-UIETT-PAYMENT',
 'CO-UITR1-PAYMENT',
 'CT-2MAG-PAYMENT',
 'DC-UC30-PAYMENT',
 'DC-PFL-PAYMENT',
 'DE-UC8-PAYMENT',
 'GA-DOL4-PAYMENT',
 'HI-UCB6-PAYMENT',
 'IA-600103-PAYMENT',
 'ID-020-PAYMENT',
 'IL-UI340-PAYMENT',
 'IN-UC1-PAYMENT',
 'KS-KCNS100-PAYMENT',
 'KY-UI3-PAYMENT',
 'LA-ES61-PAYMENT',
 'MA-1700HI-PAYMENT',
 'MA-PFML-PAYMENT',
 'MD-DLLR-PAYMENT',
 'MI-UIA1020-PAYMENT',
 'MN-DEED1-PAYMENT',
 'MO-MODES-PAYMENT',
 'MS-UI23-PAYMENT',
 'MT-UI5-PAYMENT',
 'NC-101-PAYMENT',
 'NE-UI11T-PAYMENT',
 'NJ-NJ927PUI-PAYMENT',
 'NM-ES903A-PAYMENT',
 'NM-WC1-PAYMENT',
 'OH-JFS66111-PAYMENT',
 'OH-JFS20127-PAYMENT',
 'OK-OES3-PAYMENT',
 'OR-OTCUI-PAYMENT',
 'OR-STTV-PAYMENT',
 'WA-PFML-PAYMENT',
 'PA-UC2-PAYMENT',
 'RI-TX17-PAYMENT',
 'SC-UCE120-PAYMENT',
 'UT-F3-PAYMENT',
 'VA-FC20-PAYMENT',
 'VT-C101-PAYMENT',
 'WI-UCT101-PAYMENT',
 'WV-A154-PAYMENT',
 'CT-PFML-PAYMENT',
 'WA-CARES-PAYMENT',
 'CO-FAMLI-PAYMENT',
 'OR-PFMSL-PAYMENT',
 'DE-PFML-PAYMENT'
 )
 ;

 --- 3D (ACCELERATED)
UPDATE PSP_PAYMENT_TEMPLATE rt 
SET DEFAULT_DEPOSIT_FREQUENCY ='ACCELERATED'
WHERE PAYMENT_TEMPLATE_CD IN 
('MD-MW506-PAYMENT')
 ;
 
-- NON_MODIFIABLE_FREQUENCY
UPDATE PSP_PAYMENT_TEMPLATE rt
SET NON_MODIFIABLE_FREQUENCY = '0'
;
UPDATE PSP_PAYMENT_TEMPLATE rt
SET NON_MODIFIABLE_FREQUENCY = '1'
WHERE PAYMENT_TEMPLATE_CD IN ('CA-UIETT-PAYMENT', 'IRS-940-PAYMENT')
;

-- PRIOR_QTR_ADJ_REQ_AMENDMENT
UPDATE PSP_PAYMENT_TEMPLATE rt
SET PRIOR_QTR_ADJ_REQ_AMENDMENT = '0'
;
UPDATE PSP_PAYMENT_TEMPLATE rt
SET PRIOR_QTR_ADJ_REQ_AMENDMENT = '1'
WHERE PAYMENT_TEMPLATE_CD IN ('IRS-941-PAYMENT', 'CA-PITSDI-PAYMENT')
;

--AGENCY_REFUNDS_QUARTERLY
UPDATE PSP_PAYMENT_TEMPLATE rt
SET AGENCY_REFUNDS_QUARTERLY = '0'
;
UPDATE PSP_PAYMENT_TEMPLATE rt
SET AGENCY_REFUNDS_QUARTERLY = '1'
WHERE PAYMENT_TEMPLATE_CD IN ('IRS-941-PAYMENT')
;

-- TXP_RECORD_CLASS

UPDATE PSP_PAYMENT_TEMPLATE 
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_'|| substr(PAYMENT_TEMPLATE_CD, 1, 2)||'_WH' ,
    CATEGORY = 'Withholding' 
WHERE PAYMENT_TEMPLATE_CD in (
'KS-KW5-PAYMENT',
'OK-OW9A-PAYMENT',
'GA-GAV-PAYMENT',
'WV-IT101-PAYMENT',
'LA-L1-PAYMENT',
'SC-WH1601-PAYMENT',
'PA-501-PAYMENT',
'NC-NC5P-PAYMENT',
'NY-1MN-PAYMENT',
'ND-306-PAYMENT',
'ID-910-PAYMENT',
'MD-MW506-PAYMENT',
'MA-M941-PAYMENT',
'WI-WT6-PAYMENT',
'IL-501-PAYMENT',
'NE-941N-PAYMENT',
'NJ-NJ927PWH-PAYMENT',
'MO-941-PAYMENT',
'IA-44105-PAYMENT',
'UT-TC96-PAYMENT',
'AL-CR4WH-PAYMENT',
'CT-CTWH-PAYMENT',
'KY-K1-PAYMENT',
'HI-VP1-PAYMENT',
'ME-900ME-PAYMENT',
'CO-DR1094-PAYMENT',
'AZ-A1-PAYMENT',
'VA-VA15-PAYMENT',
'VT-WH433-PAYMENT',
'DE-DES-PAYMENT',
'DC-FR900-PAYMENT',
'OH-IT501-PAYMENT',
'RI-941-PAYMENT',
'CA-PITSDI-PAYMENT',
'OR-OTCWH-PAYMENT',
'MN-MW1-PAYMENT',
'MI-MW106-PAYMENT',
'MT-MW1-PAYMENT',
'AR-941M-PAYMENT',
'NM-CRS1-PAYMENT',
'MS-M89-PAYMENT')
;
UPDATE PSP_PAYMENT_TEMPLATE 
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_'|| substr(PAYMENT_TEMPLATE_CD, 1, 2)||'_UI',
    CATEGORY = 'SUI' 
WHERE PAYMENT_TEMPLATE_CD in 
('AL-CR4UI-PAYMENT',
 'AZ-UC018-PAYMENT', 
 'CA-UIETT-PAYMENT', 
 'FL-UCT6-PAYMENT', 
 'IL-UI340-PAYMENT', 
 'IA-600103-PAYMENT',
 'ID-020-PAYMENT',
 'VT-C101-PAYMENT',
 'KS-KCNS100-PAYMENT',
 'ME-941C1ME-PAYMENT', 
 'MD-DLLR-PAYMENT', 
 'MA-1700HI-PAYMENT', 
 'MN-DEED1-PAYMENT', 
 'NE-UI11T-PAYMENT', 
 'NV-NUCS4072-PAYMENT',
 'NJ-NJ927PUI-PAYMENT',
 'NC-101-PAYMENT',
 'ND-SFN41263-PAYMENT', 
 'OR-OTCUI-PAYMENT',
 'PA-UC2-PAYMENT',
 'TN-LB0456-PAYMENT', 
 'TX-C3V-PAYMENT', 
 'WI-UCT101-PAYMENT',
 'LA-ES61-PAYMENT',
 'GA-DOL4-PAYMENT',
 'MT-UI5-PAYMENT',
 'WA-PFML-PAYMENT',
 'SC-UCE120-PAYMENT',
 'MO-MODES-PAYMENT')
;
UPDATE PSP_PAYMENT_TEMPLATE
SET CATEGORY = 'SUI'
WHERE PAYMENT_TEMPLATE_CD IN
('AL-CR4UI-PAYMENT', 
 'AK-AKNS-PAYMENT', 
 'AZ-UC018-PAYMENT', 
 'AR-209B-PAYMENT', 
 'CA-UIETT-PAYMENT', 
 'CO-UITR1-PAYMENT', 
 'CT-2MAG-PAYMENT', 
 'DE-UC8-PAYMENT', 
 'DC-UC30-PAYMENT', 
 'FL-UCT6-PAYMENT', 
 'GA-DOL4-PAYMENT', 
 'HI-UCB6-PAYMENT', 
 'ID-020-PAYMENT', 
 'IL-UI340-PAYMENT', 
 'IA-600103-PAYMENT', 
 'KS-KCNS100-PAYMENT', 
 'KY-UI3-PAYMENT', 
 'LA-ES61-PAYMENT', 
 'ME-941C1ME-PAYMENT', 
 'MD-DLLR-PAYMENT', 
 'MA-1700HI-PAYMENT', 
 'MI-UIA1020-PAYMENT', 
 'MN-DEED1-PAYMENT', 
 'MS-UI23-PAYMENT', 
 'MO-MODES-PAYMENT', 
 'MT-UI5-PAYMENT', 
 'NE-UI11T-PAYMENT', 
 'NV-NUCS4072-PAYMENT', 
 'NH-DES200-PAYMENT', 
 'NJ-NJ927PUI-PAYMENT', 
 'NM-ES903A-PAYMENT', 
 'NM-WC1-PAYMENT', 
 'NY-45MN-PAYMENT', 
 'NC-101-PAYMENT', 
 'ND-SFN41263-PAYMENT', 
 'OH-JFS20127-PAYMENT', 
 'OK-OES3-PAYMENT', 
 'OR-OTCUI-PAYMENT',
 'RI-TX17-PAYMENT', 
 'SC-UCE120-PAYMENT', 
 'SD-UID21-PAYMENT', 
 'TN-LB0456-PAYMENT', 
 'TX-C3V-PAYMENT', 
 'UT-F3-PAYMENT', 
 'VT-C101-PAYMENT', 
 'VA-FC20-PAYMENT', 
 'WV-A154-PAYMENT', 
 'WA-F5208-PAYMENT', 
 'WI-UCT101-PAYMENT', 
 'WY-WYO056-PAYMENT',
 'CT-PFML-PAYMENT',
 'CT-2MAG-PAYMENT',
 'OR-PFMSL-PAYMENT',
 'DE-PFML-PAYMENT')
;
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_NY_MCT',
    CATEGORY = 'Withholding'  
WHERE PAYMENT_TEMPLATE_CD in ('NY-MTA305-PAYMENT')
;
--PSP-18543 start
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_MA_PFML',
    CATEGORY = 'Withholding'
WHERE PAYMENT_TEMPLATE_CD in ('MA-PFML-PAYMENT')
;
--PSP-18543 END
--PSP-19068 start
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_OR_STTV'
WHERE PAYMENT_TEMPLATE_CD in ('OR-STTV-PAYMENT')
;
--PSP-19068 END
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_OR_PFMSL'
WHERE PAYMENT_TEMPLATE_CD in ('OR-PFMSL-PAYMENT')
;
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_CT_PFML'
WHERE PAYMENT_TEMPLATE_CD in ('CT-PFML-PAYMENT')
;
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_CT_UI'
WHERE PAYMENT_TEMPLATE_CD in ('CT-2MAG-PAYMENT')
;
--PSP-25221
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_CO_FAMLI'
WHERE PAYMENT_TEMPLATE_CD in ('CO-FAMLI-PAYMENT')
;
--PSP-27235
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_WA_CARES'
WHERE PAYMENT_TEMPLATE_CD in ('WA-CARES-PAYMENT')
;
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_WV_UI'
WHERE PAYMENT_TEMPLATE_CD in ('WV-A154-PAYMENT')
;
UPDATE PSP_PAYMENT_TEMPLATE
SET TXP_RECORD_CLASS='com.intuit.sbd.payroll.psp.agency.ach.txp.Txp_CO_UI'
WHERE PAYMENT_TEMPLATE_CD in ('CO-UITR1-PAYMENT')
;
COMMIT;
;
