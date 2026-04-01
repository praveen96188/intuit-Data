DECLARE
	table_exists PLS_INTEGER;
BEGIN
	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_AUTH_DOMAIN';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_AUTH_DOMAIN" CASCADE CONSTRAINTS';
	END IF;

END;
/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_AUTH_DOMAIN
(
	"DOMAIN_ID" VARCHAR2(255 CHAR) NOT NULL, PRIMARY KEY (DOMAIN_ID)
	, "VERSION" NUMBER(19,0) NOT NULL
	, "DESCRIPTION" VARCHAR2(4000 CHAR)
	, "NAME" VARCHAR2(4000 CHAR)
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_AUTH_DOMAIN ( DOMAIN_ID, VERSION, NAME, DESCRIPTION) VALUES (
'DDUI', 0, 'dd ui', 'This is the dd ui rep app')
/

INSERT INTO TEMP_PSP_AUTH_DOMAIN ( DOMAIN_ID, VERSION, NAME, DESCRIPTION) VALUES (
'AUTH', 0, 'Auth Tool', 'Tool used to add and edit users and roles')
/

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_AUTH_DOMAIN
   (DOMAIN_ID, VERSION, NAME, DESCRIPTION)
SELECT
    DOMAIN_ID, VERSION, NAME, DESCRIPTION
FROM
   TEMP_PSP_AUTH_DOMAIN tt
WHERE
   tt.DOMAIN_ID NOT IN (SELECT DOMAIN_ID FROM PSP_AUTH_DOMAIN)

/

DELETE FROM PSP_AUTH_DOMAIN
WHERE DOMAIN_ID NOT IN (SELECT DOMAIN_ID FROM TEMP_PSP_AUTH_DOMAIN)

/

UPDATE PSP_AUTH_DOMAIN rt
SET (rt.VERSION, rt.NAME, rt.DESCRIPTION) =
(SELECT tt.VERSION, tt.NAME, tt.DESCRIPTION
 FROM TEMP_PSP_AUTH_DOMAIN tt WHERE tt.DOMAIN_ID = rt.DOMAIN_ID)
/

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_AUTH_DOMAIN

/
COMMIT