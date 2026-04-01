-- ------------ Write CREATE-TYPE-stage scripts -----------

CREATE TYPE ibobadm_pds.prc_remove_company$columnnameslist$c AS (
column_value CHARACTER VARYING(50)
);

CREATE TYPE ibobadm_pds.prc_remove_company$parentcolumnnames$c AS (
column_value CHARACTER VARYING(50)
);

CREATE TYPE ibobadm_pds.prc_remove_company$parentrec$r AS (
tablename CHARACTER VARYING(50),
uniqueid CHARACTER VARYING(1020)
);

CREATE TYPE ibobadm_pds.prc_remove_company$uniqueidcollection$c AS (
column_value CHARACTER VARYING(1020)
);

