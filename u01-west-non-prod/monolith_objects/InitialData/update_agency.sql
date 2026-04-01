DROP TABLE IF EXISTS TMP_PSP_AGENCY CASCADE

;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TMP_PSP_AGENCY (LIKE PSP_AGENCY INCLUDING ALL)

;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TMP_PSP_AGENCY (AGENCY_ID, NAME, AGENCY_ABBREV, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED,
                        R_A_F_ENROLLMENT_REQUIRED, RFNDS_INTUIT_FOR_RETURNED_PMT, AGENCY_SUPPORTED,NO_CALCULATION, VERSION) 
 VALUES ( 'NOCALC',  'No Calculation Agency', 'NOCALC',null, 0, 0, 0, 0, 0, 1, 0)
;
COMMIT
;
 
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_AGENCY
   (AGENCY_ID, NAME, AGENCY_ABBREV, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, RFNDS_INTUIT_FOR_RETURNED_PMT, AGENCY_SUPPORTED, NO_CALCULATION, VERSION)
SELECT
    AGENCY_ID, NAME, AGENCY_ABBREV, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, RFNDS_INTUIT_FOR_RETURNED_PMT, AGENCY_SUPPORTED, NO_CALCULATION, VERSION 
FROM
   TMP_PSP_AGENCY tt
WHERE
   tt.AGENCY_ID NOT IN (SELECT AGENCY_ID FROM PSP_AGENCY)

;

UPDATE PSP_AGENCY rt
SET (AGENCY_ID, NAME, AGENCY_ABBREV, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, RFNDS_INTUIT_FOR_RETURNED_PMT, AGENCY_SUPPORTED, NO_CALCULATION) =
(SELECT  tt.AGENCY_ID, tt.NAME, tt.AGENCY_ABBREV, tt.DEFAULT_R_A_A_FORM, tt.A_C_H_ENROLLMENT_REQUIRED, tt.R_A_A_ENROLLMENT_REQUIRED, tt.R_A_F_ENROLLMENT_REQUIRED, tt.RFNDS_INTUIT_FOR_RETURNED_PMT, tt.AGENCY_SUPPORTED, tt.NO_CALCULATION
 FROM TMP_PSP_AGENCY tt WHERE tt.AGENCY_ID = rt.AGENCY_ID)
 WHERE rt.AGENCY_ID IN (SELECT AGENCY_ID FROM TMP_PSP_AGENCY)
;

update psp_agency
set agency_supported = 1
where agency_id in
(select agency_fk
 from psp_payment_template
where support_start_date is not null)
and agency_supported != 1
;

update psp_agency
set agency_supported = 0
where agency_id not in
(select agency_fk
 from psp_payment_template
where support_start_date is not null)
and agency_supported != 0
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TMP_PSP_AGENCY

;
COMMIT
 
