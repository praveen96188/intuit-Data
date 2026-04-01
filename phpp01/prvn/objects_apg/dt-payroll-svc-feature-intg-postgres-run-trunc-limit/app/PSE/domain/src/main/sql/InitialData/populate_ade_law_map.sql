DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_ADE_LAW_MAP';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_ADE_LAW_MAP" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_ADE_LAW_MAP
(
  ADE_LAW_MAP_ID  VARCHAR2(50 CHAR)                 NOT NULL,
  VERSION         NUMBER(19)                        NOT NULL,
  REALM_ID        NUMBER(19)                        DEFAULT -1                    NOT NULL,
  ADE_NAME        VARCHAR2(255 CHAR),
  LAW_FK          VARCHAR2(255 CHAR)                NOT NULL,
  ADE_LAW_MAP_FK  VARCHAR2(255 CHAR)
)
/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

-- Arizona - ADE will convert Agency provided JTT code (ex: 'Y', 'N', etc) to the appropriate rate.
insert into temp_ade_law_map values ('f3700136-b27a-4000-84aa-2e2134248f7d', 0, -1, 'uiRate', '86', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4001-84aa-2e2134248f7d', 0, -1, 'JTT', '179', null)
/

-- Arkansas
insert into temp_ade_law_map values ('f3700136-b27a-4000-84aa-2e213424af7d', 0, -1, 'uiRate', '85', null)
/

-- California
insert into temp_ade_law_map values ('f3700136-b27a-4002-84aa-2e2134248f7d', 0, -1, 'uiRate', '87', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4003-84aa-2e2134248f7d', 0, -1, 'Employment Training Tax', '142', null)
/

-- Colorado
insert into temp_ade_law_map values ('f3700136-b27a-4004-84aa-2e2134248f7d', 0, -1, 'uiRate', '88', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4005-84aa-2e2134248f7d', 0, -1, 'Bond Rate', '153', null)
/

-- Connecticut
insert into temp_ade_law_map values ('f3700136-b27a-4704-84aa-2e2134248f7d', 0, -1, 'uiRate', '89', null)
/

-- Florida
insert into temp_ade_law_map values ('f3700136-b27a-4204-84aa-2e2134248f7d', 0, -1, 'uiRate', '92', null)
/

-- Georgia
insert into temp_ade_law_map values ('f3700136-b27a-4104-84aa-2e2134248f7d', 0, -1, 'uiRate', '93', null)
/

-- Hawaii
insert into temp_ade_law_map values ('f3700136-b27a-4304-84aa-2e2134248f7d', 0, -1, 'uiRate', '94', null)
/

-- Idaho
insert into temp_ade_law_map values ('f3e00136-b27a-4205-84aa-2e2134248f7d', 0, -1, 'uiRate', '96', null)
/

-- Illinois
insert into temp_ade_law_map values ('f3700136-b27a-4205-84aa-2e2134248f7d', 0, -1, 'uiRate', '97', null)
/

-- Kansas
insert into temp_ade_law_map values ('f3700136-bb7a-4205-84aa-2e2134248f7d', 0, -1, 'uiRate', '99', null)
/

-- Kentucky
insert into temp_ade_law_map values ('f3a00436-b27a-420c-84aa-2e2137e48f7d', 0, -1, 'uiRate', '100', null)
/

-- Louisiana
insert into temp_ade_law_map values ('f3700136-b27a-4205-84aa-2e1134248f7d', 0, -1, 'uiRate', '101', null)
/
-- LA Social Charge Tax is not currently defined by ADE.


-- Maine
insert into temp_ade_law_map values ('f3760136-b27a-4205-84a7-2e2134248f7d', 0, -1, 'uiRate', '104', null)
/
insert into temp_ade_law_map values ('f3760136-b27a-4205-84a8-2e2134248f7d', 0, -1, 'Competitive Skills Training Fund Tax Rate', '189', null)
/
-- ME Surtax is not currently defined by ADE.


-- Maryland
insert into temp_ade_law_map values ('f3760136-b27a-4205-84aa-2e2134248f7d', 0, -1, 'uiRate', '103', null)
/

-- Massachusetts
insert into temp_ade_law_map values ('f3700136-b27a-4006-84aa-2e2134248f7d', 0, -1, 'uiRate', '102', null)
/

-- Michigan
insert into temp_ade_law_map values ('f3700136-b27a-4106-84aa-2e2134248f7d', 0, -1, 'uiRate', '105', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4206-84aa-2e2134248f7d', 0, -1, 'Obligation Assessment', '201', null)
/

-- Minnesota
insert into temp_ade_law_map values ('f3700136-b27a-4306-84aa-2e2134248f7d', 0, -1, 'uiRate', '106', null)
/

-- Mississippi
insert into temp_ade_law_map values ('f3700136-b27a-4406-84aa-2e2134248f7d', 0, -1, 'uiRate', '108', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4207-84aa-2e2134248f7d', 0, -1, 'WET Rate', '187', null)
/

-- Missouri
insert into temp_ade_law_map values ('f3700436-b27a-4406-84aa-2e2134248f7d', 0, -1, 'uiRate', '107', null)
/

-- Montana
insert into temp_ade_law_map values ('f3700136-b27a-4506-84aa-2e2134248f7d', 0, -1, 'uiRate', '109', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4507-84aa-2e2134248f7d', 0, -1, 'ST_AdministrativeFundTaxRate', '147', null)
/

-- Nebraska
insert into temp_ade_law_map values ('f3700136-b27a-4506-84aa-2e213c240f7d', 0, -1, 'uiRate', '112', null)
/
-- NE SUIT Tax is not currently defined by ADE nor required by the state.  Allow it to default to 0.


-- Nevada
insert into temp_ade_law_map values ('f3700136-b27a-4506-84aa-2e213c248f7d', 0, -1, 'uiRate', '116', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4507-84aa-2e213d248f7d', 0, -1, 'CEP Rate', '159', null)
/

-- New Hampshire
insert into temp_ade_law_map values ('f3700136-b2ea-4008-84aa-2e2134248f7d', 0, -1, 'uiRate', '113', null)
/
insert into temp_ade_law_map values ('f3700136-b2fa-4008-84aa-2e2134248f7d', 0, -1, 'Administrative Tax Rate', '185', null)
/

-- New Jersey
insert into temp_ade_law_map values ('f3700136-b27a-4008-84aa-2e2134248f7d', 0, -1, 'uiRate', '114', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-4009-84aa-2e2134248f7d', 0, -1, 'Disability Rate', '73', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-400a-84aa-2e2134248f7d', 0, -1, 'Workforce Rate', '167', null)
/
insert into temp_ade_law_map values ('f3700136-b37a-400a-84aa-2e2134248f7d', 0, -1, 'Healthcare Rate', '169', null)
/
insert into temp_ade_law_map values ('f3700136-b47a-400a-84aa-2e2134248f7d', 0, -1, 'FLI Rate', '194', null)
/

-- New Mexico
insert into temp_ade_law_map values ('f3700136-b27a-4018-84aa-2e2134248f7d', 0, -1, 'uiRate', '115', null)
/
-- NM Trust Fund is not defined in ADE nor required by the state.  Allow it to default to 0.


-- New York
insert into temp_ade_law_map values ('f3700136-b27a-400c-84aa-2e2134248f7d', 0, -1, 'uiRate', '117', null)
/

-- North Carolina
insert into temp_ade_law_map values ('f3700136-b27a-420c-84aa-2e2134248f7d', 0, -1, 'uiRate', '110', null)
/

-- North Dakota
insert into temp_ade_law_map values ('f3700436-b27a-420c-84aa-2e2134e4ef7d', 0, -1, 'uiRate', '111', null)
/

-- Ohio
insert into temp_ade_law_map values ('f3700136-b27a-420c-84aa-2e2134e48f7d', 0, -1, 'uiRate', '118', null)
/

-- Oklahoma
insert into temp_ade_law_map values ('f3700436-b27a-420c-84aa-2e2e34e48f7d', 0, -1, 'uiRate', '119', null)
/

-- Oregon
insert into temp_ade_law_map values ('f3700436-b27a-420c-84aa-2e2134e48f7d', 0, -1, 'uiRate', '120', null)
/

-- Pennsylvania
insert into temp_ade_law_map values ('f3707436-b27a-420c-84aa-2e2134e48f7d', 0, -1, 'uiRate', '121', null)
/

-- Rhode Island
insert into temp_ade_law_map values ('f3b00436-b27a-420c-84aa-2e2134e48f7d', 0, -1, 'uiRate', '123', null)
/

-- South Carolina
insert into temp_ade_law_map values ('f3700136-b27a-420c-84aa-2e213a248f7d', 0, -1, 'uiRate', '124', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-420c-84aa-2e213b248f7d', 0, -1, 'Interest Surcharge and Contingency assessment', '161', null)
/

-- South Dakota
insert into temp_ade_law_map values ('f3700136-b27a-410c-84aa-2e2134248f7d', 0, -1, 'uiRate', '125', null)
/

-- Texas
insert into temp_ade_law_map values ('f3700136-b27a-400b-84aa-2e2134248f7d', 0, -1, 'uiRate', '127', null)
/

-- Tennessee
insert into temp_ade_law_map values ('f3700136-b27a-420b-84aa-2e2134248f7d', 0, -1, 'uiRate', '126', null)
/


-- Utah
insert into temp_ade_law_map values ('f3700136-b27a-440b-84aa-2e2134248f7d', 0, -1, 'uiRate', '128', null)
/

-- Vermont
insert into temp_ade_law_map values ('f3700136-b27a-401f-84aa-2e2134248f7d', 0, -1, 'uiRate', '130', null)
/

-- Virginia
insert into temp_ade_law_map values ('f3700136-b27a-400f-84aa-2e2134248f7d', 0, -1, 'uiRate', '129', null)
/

-- Washington
insert into temp_ade_law_map values ('f3700136-b27a-400d-84aa-2e2134248f7d', 0, -1, 'uiRate', '131', null)
/
insert into temp_ade_law_map values ('f3700136-b27a-400e-84aa-2e2134248f7d', 0, -1, 'EAF Annual Rate', '164', null)
/

-- West Virginia
insert into temp_ade_law_map values ('f3600436-b27a-420c-84aa-2e2134e48f7d', 0, -1, 'uiRate', '133', null)
/

-- Wisconsin
insert into temp_ade_law_map values ('f3a00436-b27a-420c-84aa-2e2134e48f7d', 0, -1, 'uiRate', '132', null)
/

--------------------------------------------------------
-- Synchronize temp table and real table by           --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO psp_ade_law_map
   (ade_law_map_id, version, realm_id, ade_name, law_fk, ade_law_map_fk)
SELECT 
   ade_law_map_id, version, realm_id, ade_name, law_fk, ade_law_map_fk
FROM 
   temp_ade_law_map tmp
WHERE 
   tmp.ade_law_map_id NOT IN (SELECT ade_law_map_id FROM psp_ade_law_map)
/

DELETE FROM psp_ade_law_map
WHERE ade_law_map_id NOT IN (SELECT ade_law_map_id FROM temp_ade_law_map)
/

UPDATE psp_ade_law_map alm
SET  (version, realm_id, ade_name, law_fk, ade_law_map_fk) =
(SELECT version, realm_id, ade_name, law_fk, ade_law_map_fk
 FROM temp_ade_law_map temp WHERE temp.ade_law_map_id = alm.ade_law_map_id)
/

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE temp_ade_law_map

/
COMMIT
