-----------------------------------------------psp_entity_update-----------------------------------------------

--DDL--
\timing
set search_path to pspadm;

--m12-2026

ALTER TABLE pspadm.psp_entity_update DETACH PARTITION pspadm.psp_entity_update_m122026;

ALTER TABLE pspadm.psp_entity_update ATTACH PARTITION pspadm.psp_entity_update_m122026
FOR VALUES FROM ('2026-12-01 00:00:00') TO ('2027-01-01 00:00:00');



--m1--2027

CREATE TABLE pspadm.psp_entity_update_m012027
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2027-02-01 00:00:00');



--m2--2027

CREATE TABLE pspadm.psp_entity_update_m022027
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-02-01 00:00:00') TO ('2027-03-01 00:00:00');


--m3-2027

CREATE TABLE pspadm.psp_entity_update_m032027 
        PARTITION OF pspadm.psp_entity_update 
        FOR VALUES FROM ('2027-03-01 00:00:00') TO ('2027-04-01 00:00:00');

--m4-2027

CREATE TABLE pspadm.psp_entity_update_m042027 
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-04-01 00:00:00') TO ('2027-05-01 00:00:00');


--m5-2027

CREATE TABLE pspadm.psp_entity_update_m052027 
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-05-01 00:00:00') TO ('2027-06-01 00:00:00');


--m6-2027

CREATE TABLE pspadm.psp_entity_update_m062027
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-06-01 00:00:00') TO ('2027-07-01 00:00:00');


--m7-2027

CREATE TABLE pspadm.psp_entity_update_m072027
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-07-01 00:00:00') TO ('2027-08-01 00:00:00');


--m8-2027

CREATE TABLE pspadm.psp_entity_update_m082027 
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-08-01 00:00:00') TO ('2027-09-01 00:00:00');

--m9-2027

CREATE TABLE pspadm.psp_entity_update_m092027  
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-09-01 00:00:00') TO ('2027-10-01 00:00:00');


--m10-2027

CREATE TABLE pspadm.psp_entity_update_m102027 
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2027-11-01 00:00:00');

--m11-2027

CREATE TABLE pspadm.psp_entity_update_m112027 
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-11-01 00:00:00') TO ('2027-12-01 00:00:00');

--m12-2027

CREATE TABLE pspadm.psp_entity_update_m122027 
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2027-12-01 00:00:00') TO ('2028-01-01 00:00:00');

--m1-2028

CREATE TABLE pspadm.psp_entity_update_m012028 
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2028-01-01 00:00:00') TO (MAXVALUE);

--CONSTRAINTS--

ALTER TABLE pspadm.psp_entity_update_m012027 ADD CONSTRAINT pspadm.psp_entity_update_m012027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022027 ADD CONSTRAINT pspadm.psp_entity_update_m022027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032027 ADD CONSTRAINT pspadm.psp_entity_update_m032027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042027 ADD CONSTRAINT pspadm.psp_entity_update_m042027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m052027 ADD CONSTRAINT pspadm.psp_entity_update_m052027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m062027 ADD CONSTRAINT pspadm.psp_entity_update_m062027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m072027 ADD CONSTRAINT pspadm.psp_entity_update_m072027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m082027 ADD CONSTRAINT pspadm.psp_entity_update_m082027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m092027 ADD CONSTRAINT pspadm.psp_entity_update_m092027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m102027 ADD CONSTRAINT pspadm.psp_entity_update_m102027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m112027 ADD CONSTRAINT pspadm.psp_entity_update_m112027_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122027 ADD CONSTRAINT pspadm.psp_entity_update_m122027_pk PRIMARY KEY (entity_update_seq, realm_id);

ALTER TABLE pspadm.psp_entity_update_m012028 ADD CONSTRAINT pspadm.psp_entity_update_m012028_pk PRIMARY KEY (entity_update_seq, realm_id);
--Index--

--2027
ALTER INDEX pspadm.psp_entity_update_m012027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m012027;
ALTER INDEX pspadm.psp_entity_update_m022027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m022027;
ALTER INDEX pspadm.psp_entity_update_m032027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m032027;
ALTER INDEX pspadm.psp_entity_update_m042027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m042027;
ALTER INDEX pspadm.psp_entity_update_m052027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m052027;
ALTER INDEX pspadm.psp_entity_update_m062027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m062027;
ALTER INDEX pspadm.psp_entity_update_m072027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m072027;
ALTER INDEX pspadm.psp_entity_update_m082027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m082027;
ALTER INDEX pspadm.psp_entity_update_m092027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m092027;
ALTER INDEX pspadm.psp_entity_update_m102027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m102027;
ALTER INDEX pspadm.psp_entity_update_m112027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m112027;
ALTER INDEX pspadm.psp_entity_update_m122027_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m122027;


ALTER INDEX pspadm.psp_entity_update_m012027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m012027;
ALTER INDEX pspadm.psp_entity_update_m022027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m022027;
ALTER INDEX pspadm.psp_entity_update_m032027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m032027;
ALTER INDEX pspadm.psp_entity_update_m042027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m042027;
ALTER INDEX pspadm.psp_entity_update_m052027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m052027;
ALTER INDEX pspadm.psp_entity_update_m062027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m062027;
ALTER INDEX pspadm.psp_entity_update_m072027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m072027;
ALTER INDEX pspadm.psp_entity_update_m082027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m082027;
ALTER INDEX pspadm.psp_entity_update_m092027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m092027;
ALTER INDEX pspadm.psp_entity_update_m102027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m102027;
ALTER INDEX pspadm.psp_entity_update_m112027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m112027;
ALTER INDEX pspadm.psp_entity_update_m122027_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m122027;

--2028
ALTER INDEX pspadm.psp_entity_update_m012028_created_date_idx RENAME TO pspadm.idx_entityupdate_crdate_m012028;

ALTER INDEX pspadm.psp_entity_update_m012028_modified_date_idx RENAME TO pspadm.idx_ent_upd_mod_date_m012028;

--attach--
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m012027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m022027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m032027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m042027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m052027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m062027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m072027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m082027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m092027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m102027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m112027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m122027;
alter index pspadm.idx_entityupdate_crdate attach partition pspadm.idx_entityupdate_crdate_m012028;

alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m012027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m022027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m032027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m042027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m052027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m062027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m072027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m082027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m092027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m102027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m112027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m122027;
alter index pspadm.idx_ent_upd_mod_date attach partition pspadm.idx_ent_upd_mod_date_m012028;
__________________________________________________________________________________________________________________________________