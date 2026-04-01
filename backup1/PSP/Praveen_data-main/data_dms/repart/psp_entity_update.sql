alter table pspadm.psp_entity_update detach PARTITION pspadm.psp_entity_update_m122023;
drop table pspadm.pspadm.psp_entity_update_m122023;


CREATE TABLE pspadm.psp_entity_update_m012024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2024-02-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m022024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-02-01 00:00:00') TO ('2024-03-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m032024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-03-01 00:00:00') TO ('2024-04-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m042024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-04-01 00:00:00') TO ('2024-05-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m052024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-05-01 00:00:00') TO ('2024-06-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m062024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-06-01 00:00:00') TO ('2024-07-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m072024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-07-01 00:00:00') TO ('2024-08-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m082024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-08-01 00:00:00') TO ('2024-09-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m092024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-09-01 00:00:00') TO ('2024-10-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m102024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-10-01 00:00:00') TO ('2024-11-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m112024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-11-01 00:00:00') TO ('2024-12-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m122024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-12-01 00:00:00') TO ('2025-01-01 00:00:00');




--2025
CREATE TABLE pspadm.psp_entity_update_m012025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2025-02-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m022025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-02-01 00:00:00') TO ('2025-03-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m032025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-03-01 00:00:00') TO ('2025-04-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m042025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-04-01 00:00:00') TO ('2025-05-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m052025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-05-01 00:00:00') TO ('2025-06-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m062025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-06-01 00:00:00') TO ('2025-07-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m072025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-07-01 00:00:00') TO ('2025-08-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m082025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-08-01 00:00:00') TO ('2025-09-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m092025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-09-01 00:00:00') TO ('2025-10-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m102025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-10-01 00:00:00') TO ('2025-11-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m112025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-11-01 00:00:00') TO ('2025-12-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m122025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-12-01 00:00:00') TO ('2026-01-01 00:00:00');

--2026

CREATE TABLE pspadm.psp_entity_update_m012026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m022026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m032026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m042026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m052026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m062026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m072026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m082026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m092026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m102026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m112026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m122026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-12-01 00:00:00') TO (MAXVALUE);


--2024
ALTER TABLE pspadm.psp_entity_update_m122024 ADD CONSTRAINT psp_entity_update_m122023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m012024 ADD CONSTRAINT psp_entity_update_m012024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022024 ADD CONSTRAINT psp_entity_update_m022024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032024 ADD CONSTRAINT psp_entity_update_m032024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042024 ADD CONSTRAINT psp_entity_update_m042024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m052024 ADD CONSTRAINT psp_entity_update_m052024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m062024 ADD CONSTRAINT psp_entity_update_m062024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m072024 ADD CONSTRAINT psp_entity_update_m072024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m082024 ADD CONSTRAINT psp_entity_update_m082024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m092024 ADD CONSTRAINT psp_entity_update_m092024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m102024 ADD CONSTRAINT psp_entity_update_m102024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m112024 ADD CONSTRAINT psp_entity_update_m112024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122024 ADD CONSTRAINT psp_entity_update_m122024_pk PRIMARY KEY (entity_update_seq, realm_id);

--2025
ALTER TABLE pspadm.psp_entity_update_m012025 ADD CONSTRAINT psp_entity_update_m012025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022025 ADD CONSTRAINT psp_entity_update_m022025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032025 ADD CONSTRAINT psp_entity_update_m032025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042025 ADD CONSTRAINT psp_entity_update_m042025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m052025 ADD CONSTRAINT psp_entity_update_m052025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m062025 ADD CONSTRAINT psp_entity_update_m062025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m072025 ADD CONSTRAINT psp_entity_update_m072025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m082025 ADD CONSTRAINT psp_entity_update_m082025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m092025 ADD CONSTRAINT psp_entity_update_m092025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m102025 ADD CONSTRAINT psp_entity_update_m102025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m112025 ADD CONSTRAINT psp_entity_update_m112025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122025 ADD CONSTRAINT psp_entity_update_m122025_pk PRIMARY KEY (entity_update_seq, realm_id);

--2026
ALTER TABLE pspadm.psp_entity_update_m012026 ADD CONSTRAINT psp_entity_update_m012026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022026 ADD CONSTRAINT psp_entity_update_m022026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032026 ADD CONSTRAINT psp_entity_update_m032026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042026 ADD CONSTRAINT psp_entity_update_m042026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m052026 ADD CONSTRAINT psp_entity_update_m052026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m062026 ADD CONSTRAINT psp_entity_update_m062026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m072026 ADD CONSTRAINT psp_entity_update_m072026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m082026 ADD CONSTRAINT psp_entity_update_m082026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m092026 ADD CONSTRAINT psp_entity_update_m092026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m102026 ADD CONSTRAINT psp_entity_update_m102026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m112026 ADD CONSTRAINT psp_entity_update_m112026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122026 ADD CONSTRAINT psp_entity_update_m122026_pk PRIMARY KEY (entity_update_seq, realm_id);






select 'creating index on psp_entity_update' as status ;
create index concurrently  idx_entityupdate_crdate_m122023 on pspadm.psp_entity_update_m122023 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m012024 on pspadm.psp_entity_update_m012024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m022024 on pspadm.psp_entity_update_m022024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m032024 on pspadm.psp_entity_update_m032024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m042024 on pspadm.psp_entity_update_m042024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m052024 on pspadm.psp_entity_update_m052024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m062024 on pspadm.psp_entity_update_m062024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m072024 on pspadm.psp_entity_update_m072024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m082024 on pspadm.psp_entity_update_m082024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m092024 on pspadm.psp_entity_update_m092024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m102024 on pspadm.psp_entity_update_m102024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m112024 on pspadm.psp_entity_update_m112024 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m122024 on pspadm.psp_entity_update_m122024 USING BTREE (created_date );

create index concurrently  idx_entityupdate_crdate_m012025 on pspadm.psp_entity_update_m012025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m022025 on pspadm.psp_entity_update_m022025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m032025 on pspadm.psp_entity_update_m032025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m042025 on pspadm.psp_entity_update_m042025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m052025 on pspadm.psp_entity_update_m052025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m062025 on pspadm.psp_entity_update_m062025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m072025 on pspadm.psp_entity_update_m072025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m082025 on pspadm.psp_entity_update_m082025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m092025 on pspadm.psp_entity_update_m092025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m102025 on pspadm.psp_entity_update_m102025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m112025 on pspadm.psp_entity_update_m112025 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m122025 on pspadm.psp_entity_update_m122025 USING BTREE (created_date );

create index concurrently  idx_entityupdate_crdate_m012026 on pspadm.psp_entity_update_m012026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m022026 on pspadm.psp_entity_update_m022026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m032026 on pspadm.psp_entity_update_m032026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m042026 on pspadm.psp_entity_update_m042026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m052026 on pspadm.psp_entity_update_m052026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m062026 on pspadm.psp_entity_update_m062026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m072026 on pspadm.psp_entity_update_m072026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m082026 on pspadm.psp_entity_update_m082026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m092026 on pspadm.psp_entity_update_m092026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m102026 on pspadm.psp_entity_update_m102026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m112026 on pspadm.psp_entity_update_m112026 USING BTREE (created_date );
create index concurrently  idx_entityupdate_crdate_m122026 on pspadm.psp_entity_update_m122026 USING BTREE (created_date );

create index concurrently  idx_ent_upd_mod_date_m122023 on pspadm.psp_entity_update_m122023 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m012024 on pspadm.psp_entity_update_m012024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m022024 on pspadm.psp_entity_update_m022024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m032024 on pspadm.psp_entity_update_m032024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m042024 on pspadm.psp_entity_update_m042024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m052024 on pspadm.psp_entity_update_m052024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m062024 on pspadm.psp_entity_update_m062024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m072024 on pspadm.psp_entity_update_m072024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m082024 on pspadm.psp_entity_update_m082024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m092024 on pspadm.psp_entity_update_m092024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m102024 on pspadm.psp_entity_update_m102024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m112024 on pspadm.psp_entity_update_m112024 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m122024 on pspadm.psp_entity_update_m122024 USING BTREE (modified_date );

create index concurrently  idx_ent_upd_mod_date_m012025 on pspadm.psp_entity_update_m012025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m022025 on pspadm.psp_entity_update_m022025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m032025 on pspadm.psp_entity_update_m032025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m042025 on pspadm.psp_entity_update_m042025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m052025 on pspadm.psp_entity_update_m052025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m062025 on pspadm.psp_entity_update_m062025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m072025 on pspadm.psp_entity_update_m072025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m082025 on pspadm.psp_entity_update_m082025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m092025 on pspadm.psp_entity_update_m092025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m102025 on pspadm.psp_entity_update_m102025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m112025 on pspadm.psp_entity_update_m112025 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m122025 on pspadm.psp_entity_update_m122025 USING BTREE (modified_date );

create index concurrently  idx_ent_upd_mod_date_m012026 on pspadm.psp_entity_update_m012026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m022026 on pspadm.psp_entity_update_m022026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m032026 on pspadm.psp_entity_update_m032026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m042026 on pspadm.psp_entity_update_m042026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m052026 on pspadm.psp_entity_update_m052026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m062026 on pspadm.psp_entity_update_m062026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m072026 on pspadm.psp_entity_update_m072026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m082026 on pspadm.psp_entity_update_m082026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m092026 on pspadm.psp_entity_update_m092026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m102026 on pspadm.psp_entity_update_m102026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m112026 on pspadm.psp_entity_update_m112026 USING BTREE (modified_date );
create index concurrently  idx_ent_upd_mod_date_m122026 on pspadm.psp_entity_update_m122026 USING BTREE (modified_date );
--psp_entity_update

alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m122023 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m012024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m022024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m032024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m042024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m052024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m062024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m072024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m082024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m092024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m102024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m112024 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m122024 ;

alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m012025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m022025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m032025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m042025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m052025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m062025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m072025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m082025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m092025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m102025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m112025 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m122025 ;

alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m012026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m022026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m032026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m042026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m052026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m062026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m072026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m082026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m092026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m102026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m112026 ;
alter index idx_entityupdate_crdate attach partition  pspadm.idx_entityupdate_crdate_m122026 ;

alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122023 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112024 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122024 ;

alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112025 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122025 ;

alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m012026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m022026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m032026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m042026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m052026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m062026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m072026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m082026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m092026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m102026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m112026 ;
alter index idx_ent_upd_mod_date attach partition  pspadm.idx_ent_upd_mod_date_m122026 ;



ALTER TABLE pspadm.psp_entity_update_m012024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m022024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m032024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m042024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m052024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m062024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m072024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m082024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m092024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m102024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m112024 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m122024 REPLICA IDENTITY FULL;

ALTER TABLE pspadm.psp_entity_update_m012025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m022025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m032025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m042025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m052025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m062025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m072025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m082025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m092025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m102025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m112025 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m122025 REPLICA IDENTITY FULL;

ALTER TABLE pspadm.psp_entity_update_m012026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m022026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m032026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m042026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m052026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m062026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m072026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m082026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m092026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m102026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m112026 REPLICA IDENTITY FULL;
ALTER TABLE pspadm.psp_entity_update_m122026 REPLICA IDENTITY FULL;


--not required
alter index psp_entity_update_m122023_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122023;
alter index psp_entity_update_m012024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m012024;
alter index psp_entity_update_m012025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m012025;
alter index psp_entity_update_m012026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m012026;
alter index psp_entity_update_m022024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m022024;
alter index psp_entity_update_m022025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m022025;
alter index psp_entity_update_m022026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m022026;
alter index psp_entity_update_m032024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m032024;
alter index psp_entity_update_m032025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m032025;
alter index psp_entity_update_m032026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m032026;
alter index psp_entity_update_m042024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m042024;
alter index psp_entity_update_m042025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m042025;
alter index psp_entity_update_m042026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m042026;
alter index psp_entity_update_m052024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m052024;
alter index psp_entity_update_m052025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m052025;
alter index psp_entity_update_m052026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m052026;
alter index psp_entity_update_m062024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m062024;
alter index psp_entity_update_m062025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m062025;
alter index psp_entity_update_m062026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m062026;
alter index psp_entity_update_m072024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m072024;
alter index psp_entity_update_m072025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m072025;
alter index psp_entity_update_m072026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m072026;
alter index psp_entity_update_m082024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m082024;
alter index psp_entity_update_m082025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m082025;
alter index psp_entity_update_m082026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m082026;
alter index psp_entity_update_m092024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m092024;
alter index psp_entity_update_m092025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m092025;
alter index psp_entity_update_m092026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m092026;
alter index psp_entity_update_m102024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m102024;
alter index psp_entity_update_m102025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m102025;
alter index psp_entity_update_m102026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m102026;
alter index psp_entity_update_m112024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m112024;
alter index psp_entity_update_m112025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m112025;
alter index psp_entity_update_m112026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m112026;
alter index psp_entity_update_m122023_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122023;
alter index psp_entity_update_m122024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122024;
alter index psp_entity_update_m122025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122025;
alter index psp_entity_update_m122026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122026;