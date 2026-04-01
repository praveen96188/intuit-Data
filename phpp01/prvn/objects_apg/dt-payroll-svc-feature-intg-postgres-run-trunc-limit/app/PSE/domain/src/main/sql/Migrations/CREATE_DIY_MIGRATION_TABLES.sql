--
-- These are all the supporting tables and associated table objects
-- for the DIY migration.
--

-- custom code for DIY

CREATE SEQUENCE SEQ_GSEQ_FOR_JAVA
  START WITH 1000
  MAXVALUE 999999999999999999999999999
  MINVALUE 1
  NOCYCLE
  CACHE 20
  NOORDER;


-- these tables are the migration queue.

CREATE TABLE COMPANY_MIGRATION (
       Company_Migration_Gseq NUMBER(15) NOT NULL,
       Migration_State_CD   VARCHAR2(10) NULL,
       Source_DB_Company_ID VARCHAR2(255) NULL,
       Target_DB_Company_ID VARCHAR2(255) NULL,
       Company_Legal_Name   VARCHAR2(80) NULL,
       DD_Service_CD        VARCHAR2(10) NULL
                                   CONSTRAINT DD_SERVICE_VAL4
                                          CHECK (DD_Service_CD IN ('QBOE', 'DIY')),
       Migration_Phase_ID   VARCHAR2(10) NULL,
       Migration_Scheduled_Date DATE NULL,
       Migration_Actual_Date DATE NULL,
       Z_Ins_Dttm           DATE NULL,
       Z_Ins_UserID         VARCHAR(30) NULL,
       Z_Upd_Dttm           DATE NULL,
       Z_Upd_UserID         VARCHAR(30) NULL,
       Z_Upd_Gseq           NUMBER(15) NULL
);

COMMENT ON TABLE COMPANY_MIGRATION IS 'This table maintains the state of a company being migrated from DIY on the AS/400 to PSP.  It also maintains the validation history for IOP companies.';
CREATE INDEX XIF1COMPANY_MIGRATION ON COMPANY_MIGRATION
(
       Migration_State_CD             ASC
);

ALTER TABLE COMPANY_MIGRATION
       ADD  ( CONSTRAINT XPKCOMPANY_MIGRATION PRIMARY KEY (
              Company_Migration_Gseq) ) ;


CREATE TABLE MIGRATION_EVENT_LOG (
       Migration_Event_Log_Gseq INTEGER NOT NULL,
       Company_Migration_Gseq NUMBER(15) NULL,
       Event_Log_Type_CD    VARCHAR2(10) NULL
                                   CONSTRAINT Event_Log_Type_VAL5
                                          CHECK (Event_Log_Type_CD IN ('ERR', 'INFO', 'SUM', 'WARN')),
       Event_Param_CD       VARCHAR2(20) NULL,
       Event_Param_Value    VARCHAR2(1000) NULL,
       Z_Ins_Dttm           DATE NULL,
       Z_Ins_UserID         VARCHAR(30) NULL
);

COMMENT ON TABLE MIGRATION_EVENT_LOG IS 'When Things happen during the migration save the details.  The type of events could be errors, informational or summary';
CREATE INDEX XIF1MIGRATION_EVENT_LOG ON MIGRATION_EVENT_LOG
(
       Company_Migration_Gseq         ASC
);

ALTER TABLE MIGRATION_EVENT_LOG
       ADD  ( CONSTRAINT XPKMIGRATION_EVENT_LOG PRIMARY KEY (
              Migration_Event_Log_Gseq) ) ;


CREATE TABLE MIGRATION_STATE_HIST (
       Migration_State_Hist_Gseq INTEGER NOT NULL,
       Company_Migration_Gseq NUMBER(15) NULL,
       Migration_State_CD   VARCHAR2(10) NULL,
       Z_Ins_Dttm           DATE NULL,
       Z_Ins_UserID         VARCHAR(30) NULL
);

COMMENT ON TABLE MIGRATION_STATE_HIST IS 'The history of state changes during a migration.';
CREATE INDEX XIF1MIGRATION_STATE_HIST ON MIGRATION_STATE_HIST
(
       Company_Migration_Gseq         ASC
);

CREATE INDEX XIF4MIGRATION_STATE_HIST ON MIGRATION_STATE_HIST
(
       Migration_State_CD             ASC
);

ALTER TABLE MIGRATION_STATE_HIST
       ADD  ( CONSTRAINT XPKMIGRATION_STATE_HIST PRIMARY KEY (
              Migration_State_Hist_Gseq) ) ;


CREATE TABLE MIGRATION_STATE_VAL (
       Migration_State_CD   VARCHAR2(10) NOT NULL,
       Name                 VARCHAR(80) NOT NULL,
       Descr                VARCHAR(160) NULL,
       Z_Ins_Dttm           DATE NULL,
       Z_Ins_UserID         VARCHAR(30) NULL
);

COMMENT ON TABLE MIGRATION_STATE_VAL IS 'The various states (e.g. state engine) a migration could pass through.';

ALTER TABLE MIGRATION_STATE_VAL
       ADD  ( CONSTRAINT XPKMIGRATION_STATE_VAL PRIMARY KEY (
              Migration_State_CD) ) ;

ALTER TABLE COMPANY_MIGRATION
       ADD  ( CONSTRAINT FKMIGRATION_STATE_VALCOMPANY_M
              FOREIGN KEY (Migration_State_CD)
                             REFERENCES MIGRATION_STATE_VAL ) ;

ALTER TABLE MIGRATION_EVENT_LOG
       ADD  ( CONSTRAINT FKCOMPANY_MIGRATIONMIGRATION_E
              FOREIGN KEY (Company_Migration_Gseq)
                             REFERENCES COMPANY_MIGRATION ) ;

ALTER TABLE MIGRATION_STATE_HIST
       ADD  ( CONSTRAINT FKMIGRATION_STATE_VALMIGRATION
              FOREIGN KEY (Migration_State_CD)
                             REFERENCES MIGRATION_STATE_VAL ) ;

ALTER TABLE MIGRATION_STATE_HIST
       ADD  ( CONSTRAINT FKCOMPANY_MIGRATIONMIGRATION_S
              FOREIGN KEY (Company_Migration_Gseq)
                             REFERENCES COMPANY_MIGRATION ) ;


PROMPT Creating sequence SEQ_COMPANY_MIGRATION
CREATE SEQUENCE  SEQ_COMPANY_MIGRATION
INCREMENT BY 1
START WITH 1000
CACHE 20;
 
PROMPT Creating sequence SEQ_MIGRATION_EVENT_LOG
CREATE SEQUENCE  SEQ_MIGRATION_EVENT_LOG
INCREMENT BY 1
START WITH 1000
CACHE 20;
 
PROMPT Creating sequence SEQ_MIGRATION_STATE_HIST
CREATE SEQUENCE  SEQ_MIGRATION_STATE_HIST
INCREMENT BY 1
START WITH 1000
CACHE 20;


PROMPT Creating audit trigger TR_BIUD_MIGRATION_STATE_HIST
CREATE OR REPLACE TRIGGER TR_BIUD_MIGRATION_STATE_HIST
BEFORE INSERT ON MIGRATION_STATE_HIST
FOR EACH ROW
BEGIN
  :NEW.Z_Ins_Dttm   := SYSDATE;
  :NEW.Z_Ins_UserID := USER;
END;
/
SHOW ERRORS;

PROMPT Creating audit trigger TR_BIUD_MIGRATION_STATE_VAL
CREATE OR REPLACE TRIGGER TR_BIUD_MIGRATION_STATE_VAL
BEFORE INSERT ON MIGRATION_STATE_VAL
FOR EACH ROW
BEGIN
  :NEW.Z_Ins_Dttm   := SYSDATE;
  :NEW.Z_Ins_UserID := USER;
END;
/
SHOW ERRORS;

PROMPT Creating audit trigger TR_BIUD_MIGRATION_EVENT_LOG
CREATE OR REPLACE TRIGGER TR_BIUD_MIGRATION_EVENT_LOG
BEFORE INSERT ON MIGRATION_EVENT_LOG
FOR EACH ROW
BEGIN
  :NEW.Z_Ins_Dttm   := SYSDATE;
  :NEW.Z_Ins_UserID := USER;
END;
/
SHOW ERRORS;

PROMPT Creating audit trigger TR_BIUD_COMPANY_MIGRATION
CREATE OR REPLACE TRIGGER TR_BIUD_COMPANY_MIGRATION
BEFORE INSERT OR UPDATE ON COMPANY_MIGRATION
FOR EACH ROW
BEGIN
    IF (INSERTING) THEN
        :NEW.Z_Ins_Dttm   := SYSDATE;
        :NEW.Z_Ins_UserID := USER;
        :NEW.Z_Upd_Dttm   := NULL;
        :NEW.Z_Upd_gSeq   := 1;
    ELSIF (UPDATING) THEN
        IF (:NEW.Z_Upd_gSeq < :OLD.Z_Upd_gSeq) THEN
            RAISE_APPLICATION_ERROR( -20050, 'Attempt to update stale record', TRUE);
        END IF;
        :NEW.Z_Upd_Dttm   := SYSDATE;
        :NEW.Z_Upd_UserID := USER;
        :NEW.Z_Upd_gSeq   := :OLD.Z_Upd_gSeq + 1;
    END IF;
END;
/
SHOW ERRORS;

PROMPT Creating state history trigger TRC_SET_MIGRATION_STATE_CHANGE
CREATE OR REPLACE TRIGGER TRC_SET_MIGRATION_STATE_CHANGE
AFTER INSERT OR UPDATE ON COMPANY_MIGRATION FOR EACH ROW
BEGIN
    IF (INSERTING) THEN
      INSERT INTO MIGRATION_STATE_HIST (
        MIGRATION_STATE_HIST_GSEQ, 
        COMPANY_MIGRATION_GSEQ, 
        MIGRATION_STATE_CD
      ) 
      VALUES (
        SEQ_MIGRATION_STATE_HIST.NEXTVAL,
        :NEW.COMPANY_MIGRATION_GSEQ,
        :NEW.MIGRATION_STATE_CD
      );    
    ELSIF (UPDATING) THEN
      IF (:OLD.MIGRATION_STATE_CD <> :NEW.MIGRATION_STATE_CD) THEN
        INSERT INTO MIGRATION_STATE_HIST (
          MIGRATION_STATE_HIST_GSEQ, 
          COMPANY_MIGRATION_GSEQ, 
          MIGRATION_STATE_CD
        ) 
        VALUES (
          SEQ_MIGRATION_STATE_HIST.NEXTVAL,
          :NEW.COMPANY_MIGRATION_GSEQ,
          :NEW.MIGRATION_STATE_CD
        );  
      END IF;  
    END IF;
END;
/
SHOW ERRORS;


-- added local payroll cache for performance enhancement

CREATE TABLE TEMP_CACHE_DXCHKXREF (
       XREF_USERID          NUMBER NOT NULL,
       XREF_TRACE_NUMBER    NUMBER NOT NULL,
       XREF_PAYCHKID        NUMBER NOT NULL,
       XREF_DD_NUMBER       NUMBER NULL,
       XREF_DD_NUMBER2      NUMBER NULL,
       XREF_DD_NUMBER3      NUMBER NULL,
       XREF_DD_NUMBER4      NUMBER NULL,
       Created_Date         DATE NULL,
       Creator_ID           VARCHAR2(30) NULL
);

COMMENT ON TABLE TEMP_CACHE_DXCHKXREF IS 'This table is the cross reference between DIY paychecks and the OFX as found on the AS400 in the DXCHKXREF file.';

ALTER TABLE TEMP_CACHE_DXCHKXREF
       ADD  ( CONSTRAINT XPKTEMP_CACHE_DXCHKXREF PRIMARY KEY (
              XREF_USERID, XREF_TRACE_NUMBER, XREF_PAYCHKID) ) ;


CREATE TABLE TEMP_CACHE_IQACH (
       ACH_USERID           NUMBER NOT NULL,
       ACH_TRACE_NUMBER     NUMBER NOT NULL,
       ACH_LIABCHK          NUMBER NULL,
       ACH_DTPAYCHKS        NUMBER NULL,
       ACH_DDAMT            NUMBER NULL,
       ACH_TIMESTAMP        NUMBER NULL,
       ACH_DD_FEE           NUMBER NULL,
       ACH_EMPLOYEE_FEE     NUMBER NULL,
       ACH_SALES_TAX        NUMBER NULL,
       Created_Date         DATE NULL,
       Creator_ID           VARCHAR2(30) NULL
);

COMMENT ON TABLE TEMP_CACHE_IQACH IS 'This table has the DIY payroll runs as found on the AS400 in the IQACH table.';

ALTER TABLE TEMP_CACHE_IQACH
       ADD  ( CONSTRAINT XPKTEMP_CACHE_IQACH PRIMARY KEY (ACH_USERID, 
              ACH_TRACE_NUMBER) ) ;


CREATE TABLE TEMP_CACHE_IQACHDD (
       ACHD_USERID          NUMBER NOT NULL,
       ACHD_TRACE_NUMBER    NUMBER NOT NULL,
       ACHD_SUBNUM          NUMBER NOT NULL,
       ACHD_EMPNAME         VARCHAR2(200) NULL,
       ACHD_ACCTID          VARCHAR2(100) NULL,
       ACHD_ACCTTYPE        VARCHAR2(100) NULL,
       ACHD_BANKID          VARCHAR2(100) NULL,
       ACHD_DTPAYCHKS       NUMBER NULL,
       ACHD_AMT             NUMBER NULL,
       Created_Date         DATE NULL,
       Creator_ID           VARCHAR2(30) NULL
);

COMMENT ON TABLE TEMP_CACHE_IQACHDD IS 'This table contains the DIY paychecks as found on the AS400 in the IQACHDD file.';

ALTER TABLE TEMP_CACHE_IQACHDD
       ADD  ( CONSTRAINT XPKTEMP_CACHE_IQACHDD PRIMARY KEY (
              ACHD_USERID, ACHD_TRACE_NUMBER, ACHD_SUBNUM) ) ;


CREATE OR REPLACE TRIGGER TR_BIUD_TEMP_CACHE_DXCHKXREF
BEFORE INSERT ON TEMP_CACHE_DXCHKXREF
FOR EACH ROW
BEGIN
  :NEW.Created_Date := SYSDATE;
  :NEW.Creator_ID   := USER;
END;
/
SHOW ERRORS;

CREATE OR REPLACE TRIGGER TR_BIUD_TEMP_CACHE_IQACH
BEFORE INSERT ON TEMP_CACHE_IQACH
FOR EACH ROW
BEGIN
  :NEW.Created_Date := SYSDATE;
  :NEW.Creator_ID   := USER;
END;
/
SHOW ERRORS;

CREATE OR REPLACE TRIGGER TR_BIUD_TEMP_CACHE_IQACHDD
BEFORE INSERT ON TEMP_CACHE_IQACHDD
FOR EACH ROW
BEGIN
  :NEW.Created_Date := SYSDATE;
  :NEW.Creator_ID   := USER;
END;
/
SHOW ERRORS;

