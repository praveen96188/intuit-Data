DECLARE

	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_OWNERSHIP_TYPE';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_OWNERSHIP_TYPE" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TEMP_PSP_OWNERSHIP_TYPE
(
  OWNERSHIP_TYPE_SEQ       VARCHAR2(255 CHAR) NOT NULL,
  OWNERSHIP                VARCHAR2(255 CHAR) NOT NULL,
  VERSION                 NUMBER(19)    NOT NULL
)

/
--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('58c940d0-0229-45d6-bc5e-a691z939178a','Sole Proprietorship', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('598c41df-090f-4feb-9800-b116d9f3cded','Partnership', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('4fa8c115-1ec5-4246-b5fa-db13166eb819','Limited Liability Corp', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
     VALUES ('ab6b0811-23d1-417b-a411-d025590262b5','Corporation', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('f1a35cee-0e97-4743-8724-20450eb3e587','Non-Profit Organization', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('26f9d500-c7bc-4129-afaf-309cfeea560f','Private Limited Company', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('fc08f7ca-e390-4877-b465-af34124b9ef4','Public Limited Company', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('634050bc-8f5b-4133-b9a1-24b0ff6c6740','Limited Liability Partnership', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
    VALUES ('81d686bc-1b17-4509-90bf-89b7f948d85e','Foreign Company', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('8b4c1862-0509-474e-969c-80d2e95cf5f2','Registered Charity', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('c0614694-3044-42a1-a43c-76cf0a91a1f5','Unregistered Charity', 0)
/
INSERT INTO TEMP_PSP_OWNERSHIP_TYPE (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
	 VALUES ('1ca2cc2c-6188-44de-b8e1-b3d0a5f88963','Clubs And Societies', 0)
/
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------
-- todo deleting a method that is in use could be problematic

DELETE FROM PSP_OWNERSHIP_TYPE
WHERE OWNERSHIP_TYPE_SEQ NOT IN (SELECT OWNERSHIP_TYPE_SEQ FROM TEMP_PSP_OWNERSHIP_TYPE)

/

INSERT INTO PSP_OWNERSHIP_TYPE
   (OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION)
SELECT
  OWNERSHIP_TYPE_SEQ, OWNERSHIP, VERSION
FROM
   TEMP_PSP_OWNERSHIP_TYPE tt
WHERE
   tt.OWNERSHIP_TYPE_SEQ NOT IN (SELECT OWNERSHIP_TYPE_SEQ FROM PSP_OWNERSHIP_TYPE)

/

UPDATE PSP_OWNERSHIP_TYPE rt
SET (rt.VERSION, rt.OWNERSHIP) =
(SELECT tt.VERSION, tt.OWNERSHIP
FROM TEMP_PSP_OWNERSHIP_TYPE tt WHERE tt.OWNERSHIP_TYPE_SEQ = rt.OWNERSHIP_TYPE_SEQ)
 WHERE rt.OWNERSHIP_TYPE_SEQ IN (SELECT OWNERSHIP_TYPE_SEQ FROM TEMP_PSP_OWNERSHIP_TYPE)
/

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_OWNERSHIP_TYPE

/
COMMIT