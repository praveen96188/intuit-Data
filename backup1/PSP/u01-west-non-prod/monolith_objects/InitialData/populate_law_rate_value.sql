DROP TABLE IF EXISTS TEMP_PSP_LAW_RATE_VALUE CASCADE

;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_LAW_RATE_VALUE (LIKE PSP_LAW_RATE_VALUE INCLUDING ALL)
;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

-- Arkansas
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65900-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00800, 145)
;

-- Arizona
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65901-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00000, 179)
;
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65902-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00100, 179)
;

-- California
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65901-9ec8-4117-9b77-1cd4e90c237a', 0, -1, 0.00000, 142)
;
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65902-9ec8-4117-9b77-1cd4f90c237a', 0, -1, 0.00100, 142)
;

-- Colorado (TODO: TLD - Details from Bonnie shows as always 0.00%, but database data shows otherwise.
-- insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
--  'e3d65903-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00000, 153);
--;

-- Georgia
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65904-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00000, 154)
;
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65905-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00060, 154)
;

-- Hawaii
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65906-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00000, 155)
;
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65907-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00010, 155)
;

-- Massachusetts
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65908-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00056, 150)
;

-- Maine
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65909-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00070, 189)
;

-- Minnesota
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d6590a-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00100, 158)
;

-- Mississippi

-- New Jersey (Value from ADE is 0.001175, but should be rounded to 0.00118)
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d6590d-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00118, 167)
;

-- Nevada
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d6590e-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00000, 159)
;
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d6590f-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00050, 159)
;

-- New York
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65910-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00075, 152)
;

-- Rhode Island
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65911-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00210, 160)
;
-- Not sure which law pertains to the Fixed 1.2%.  Law 72 (RI State Disability Insurance) seems to match the data in PSP.
-- insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
--  'e3d65912-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.01200, 72)
--;

-- South Carolina.  Since the Contingency Assessment and Interest Surcharge are combined and stored in the Contingency
-- Assessment, we can't have a fixed value for the Contingency Assessment.
-- insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
--  'e3d65913-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00060, 161)
--;

-- Texas (Since this surcharge is combined with the base rate, 0.0% will need to be valid.
-- insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
--   'e3d65914-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00100, 163)
-- ;

-- Washington
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65915-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00020, 164)
;
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65916-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00030, 164)
;

--Maine UPAF
insert into temp_psp_law_rate_value (law_rate_value_id, version, realm_id, rate, law_fk) values (
  'e3d65918-9ec8-4117-9b77-1cdcd90c237a', 0, -1, 0.00150, 219)
;

--------------------------------------------------------
-- Synchronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO psp_law_rate_value
   (law_rate_value_id, version, realm_id, rate, law_fk)
SELECT 
    law_rate_value_id, version, realm_id, rate, law_fk
FROM 
   temp_psp_law_rate_value tmp
WHERE 
   tmp.law_rate_value_id NOT IN (SELECT law_rate_value_id FROM psp_law_rate_value)
;

DELETE FROM psp_law_rate_value
WHERE law_rate_value_id NOT IN (SELECT law_rate_value_id FROM temp_psp_law_rate_value)
;

UPDATE psp_law_rate_value lrv
SET  (version, realm_id, rate, law_fk) =
(SELECT version, realm_id, rate, law_fk
 FROM temp_psp_law_rate_value temp WHERE temp.law_rate_value_id = lrv.law_rate_value_id)
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE temp_psp_law_rate_value

;
COMMIT
 

 
