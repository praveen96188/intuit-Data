DROP TABLE IF EXISTS TEMP_USER_PREFERENCE CASCADE
;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_USER_PREFERENCE (LIKE PSP_USER_PREFERENCE INCLUDING ALL)

;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

--Expanders
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Company Recent Events', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Company Legal Information', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Company Contact Information', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Display Subscription Status', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Direct Deposit Limits', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Edit Funding Model', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Strikes Summary', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Quickbooks Information', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Agreement Summary', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Sales Tax Summary', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Company PIN', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Edit Tokens', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Company Debug Logging', 0, 'true')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'expand_on_activate_Price Type', 0, 'true')
;
--Other
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'display_inline_settings', 0, 'false')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'company_event_log_columns', 0, '0101')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'initial_page', 0, 'Company Search')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'search_psp_as400', 0, 'false')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'include_as400_events', 0, 'false')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'show_cancelled_financial_transactions', 0, 'false')
;
INSERT INTO TEMP_USER_PREFERENCE ( KEY, VERSION, DEFAULT_VALUE) VALUES (
'include_possible_backdate_years', 0, 'false')
;


--;-------------------------------------------------------

INSERT INTO PSP_USER_PREFERENCE
   (KEY, VERSION, DEFAULT_VALUE)
SELECT
    KEY, VERSION, DEFAULT_VALUE
FROM
   TEMP_USER_PREFERENCE tt
WHERE
   tt.KEY NOT IN (SELECT KEY FROM PSP_USER_PREFERENCE)

;

DELETE FROM PSP_USER_PREFERENCE
WHERE KEY NOT IN (SELECT KEY FROM TEMP_USER_PREFERENCE)

;

UPDATE PSP_USER_PREFERENCE rt
SET (VERSION, KEY, DEFAULT_VALUE) =
(SELECT tt.VERSION, tt.KEY, tt.DEFAULT_VALUE
 FROM TEMP_USER_PREFERENCE tt WHERE tt.KEY = rt.KEY)
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_USER_PREFERENCE

;
COMMIT
;
