DROP TABLE IF EXISTS TEMP_PSP_PAYROLL_FREQUENCY CASCADE

;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_PAYROLL_FREQUENCY (LIKE PSP_PAYROLL_FREQUENCY INCLUDING ALL)

;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'1', 'Annually', 'Once a year',0)
; 
INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'2', 'SemiAnnually', 'Twice a year',0)
; 
INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'4', 'Quarterly', 'Once a quarter',0)
; 
INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'12', 'Monthly', 'Once a month',0)
; 
INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'24', 'SemiMonthly', 'Twice a month',0)
; 
INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'26', 'BiWeekly', 'Once every other week',0)
; 
INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'52', 'Weekly', 'Once a week',0)
; 
INSERT INTO TEMP_PSP_PAYROLL_FREQUENCY ( PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION ) VALUES ( 
'260', 'daily-misc.', 'Daily or miscellaneous',0)
;
 
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_PAYROLL_FREQUENCY
   (PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION )
SELECT 
   PAYROLL_FREQ_CD, NAME, DESCRIPTION, VERSION
FROM 
   TEMP_PSP_PAYROLL_FREQUENCY tt 
WHERE 
   tt.PAYROLL_FREQ_CD NOT IN (SELECT PAYROLL_FREQ_CD FROM PSP_PAYROLL_FREQUENCY)

;
DELETE FROM PSP_PAYROLL_FREQUENCY
WHERE PAYROLL_FREQ_CD NOT IN (SELECT PAYROLL_FREQ_CD FROM TEMP_PSP_PAYROLL_FREQUENCY) 

;

UPDATE PSP_PAYROLL_FREQUENCY rt
SET ( NAME, DESCRIPTION, VERSION) =
(SELECT   tt.NAME, tt.DESCRIPTION, tt.VERSION
 FROM TEMP_PSP_PAYROLL_FREQUENCY tt WHERE tt.PAYROLL_FREQ_CD = rt.PAYROLL_FREQ_CD)
;



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_PAYROLL_FREQUENCY

;
COMMIT

  

 
