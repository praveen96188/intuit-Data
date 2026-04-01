DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_AGENCY';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_AGENCY" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_AGENCY
(
  AGENCY_ID                  VARCHAR2(10 CHAR)  NOT NULL,
  VERSION                    NUMBER(19)         NOT NULL,
  REALM_ID                   NUMBER(19)         DEFAULT -1,
  NAME                       VARCHAR2(300 CHAR),
  DEFAULT_R_A_A_FORM         VARCHAR2(255 CHAR),
  A_C_H_ENROLLMENT_REQUIRED  NUMBER(1),
  R_A_A_ENROLLMENT_REQUIRED  NUMBER(1),
  R_A_F_ENROLLMENT_REQUIRED  NUMBER(1),
  AGENCY_SUPPORTED           NUMBER(1)
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_AGENCY (AGENCY_ID, NAME, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, VERSION,RFNDS_INTUIT_FOR_RETURNED_PMT,AGENCY_SUPPORTED) VALUES ( 
'IRS', 'Internal Revenue Service', 'Federal8655', 0, 1, 1, 0,1,1)
/
INSERT INTO TEMP_PSP_AGENCY (AGENCY_ID, NAME, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, VERSION,RFNDS_INTUIT_FOR_RETURNED_PMT,AGENCY_SUPPORTED) VALUES ( 
'SSA', 'Social Security Administration', null, 0, 0, 0, 0,0,1)
/
INSERT INTO TEMP_PSP_AGENCY (AGENCY_ID, NAME, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, VERSION,RFNDS_INTUIT_FOR_RETURNED_PMT,AGENCY_SUPPORTED) VALUES ( 
'CAEDD', 'CA Employment Development Dept', 'LPOA', 1, 1, 0, 0,0,1)
/
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_AGENCY
   (AGENCY_ID, NAME, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, VERSION, AGENCY_SUPPORTED)
SELECT 
   AGENCY_ID, NAME, DEFAULT_R_A_A_FORM, A_C_H_ENROLLMENT_REQUIRED, R_A_A_ENROLLMENT_REQUIRED, R_A_F_ENROLLMENT_REQUIRED, VERSION, AGENCY_SUPPORTED
FROM 
   TEMP_PSP_AGENCY tae
WHERE 
   tae.AGENCY_ID NOT IN (SELECT AGENCY_ID FROM PSP_AGENCY)

/

DELETE FROM PSP_AGENCY
WHERE AGENCY_ID NOT IN (SELECT AGENCY_ID FROM TEMP_PSP_AGENCY) 

/

UPDATE PSP_AGENCY ae
SET  (ae.AGENCY_ID, ae.NAME, ae.VERSION) =
(SELECT  tae.AGENCY_ID, tae.NAME, tae.VERSION
 FROM TEMP_PSP_AGENCY tae WHERE tae.AGENCY_ID =ae.AGENCY_ID)
/

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_AGENCY

/
COMMIT
 

 
