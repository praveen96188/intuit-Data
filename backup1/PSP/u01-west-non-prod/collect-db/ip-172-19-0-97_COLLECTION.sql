
REM Palisade DB Collection Script
REM Use SQL*PLUS to connect to the database (locally or remotely) with SYS or SYSTEM user, having privileges:
REM CREATE SESSION, SELECT ANY TABLE, SELECT ANY DICTIONARY
REM tables used in queries: APPS.FND_PRODUCT_GROUPS, CMPINSTALLATION_V, DBA_ADVISOR_TASKS, DBA_AWS, DBA_CUBES, DBA_ENCRYPTED_COLUMNS, DBA_FEATURE_USAGE_STATISTICS, DBA_FLASHBACK_ARCHIVE,
REM DBA_FLASHBACK_ARCHIVE_TABLES, DBA_FLASHBACK_ARCHIVE_TS, DBA_LOB_PARTITIONS, DBA_LOB_SUBPARTITIONS, DBA_LOBS, DBA_OBJECT_TABLES, DBA_OBJECTS, DBA_SQL_PROFILES, DBA_SQLSET, DBA_TAB_PARTITIONS,
REM DBA_TAB_SUBPARTITIONS, DBA_TABLES, DBA_TABLESPACES, DBA_USERS, DMSYS.DM$MODEL, DMSYS.DM$OBJECT, DMSYS.DM$P_MODEL, DVSYS.DBA_DV_REALM, GV$IM_SEGMENT, GV$INSTANCE, GV$PARAMETER, LBACSYS.LBAC$POLT, MDSYS.SDO_GEOM_METADATA_TABLE, ODM.ODM_MINING_MODEL, ODM_DOCUMENT,
REM ODM_RECORD, OLAPSYS.DBA$OLAP_CUBES, SYS.DBA_MINING_MODELS, SYS.MODEL$, SYSMAN.EM_PLUGIN_VERSION, SYSMAN.MGMT_ADMIN_LICENSES, SYSMAN.MGMT_FU_LICENSE_MAP, SYSMAN.MGMT_FU_REGISTRATIONS, SYSMAN.MGMT_FU_STATISTICS, SYSMAN.MGMT_LICENSE_CONFIRMATION, SYSMAN.MGMT_LICENSE_DEFINITIONS,
REM SYSMAN.MGMT_LICENSED_TARGETS, SYSMAN.MGMT_LICENSE_USAGE_HISTORY, SYSMAN.MGMT_LICENSE_USAGE_LOG, SYSMAN.MGMT_LICENSES, SYSMAN.MGMT_PLUGIN_COMP_INFO, SYSMAN.MGMT_TARGET_TYPES, SYSMAN.MGMT_TARGETS, SYSMAN.MGMT$AVAILABILITY_HISTORY, SYSMAN.MGMT$CURR_DEPLOYED_PLUGIN_AGT, SYSMAN.MGMT$TARGET,
REM V$ARCHIVE_DEST_STATUS, V$BLOCK_CHANGE_TRACKING, V$CONTAINERS, V$DATABASE, V$INSTANCE, V$OPTION, V$PARAMETER, V$VERSION

REM TERMS FOR PALISADE COMPLIANCE DATA COLLECTION TOOL ("TOOL")
REM This Agreement supplements the Master Agreement in place between your company/organization and Palisade Compliance. If no Master Agreement is in place, the terms of this agreement govern your use of the Palisade Tool. By your use of the Tool, you indicate your acceptance of these terms and your agreement, as an authorized representative of your company or organization (if being acquired for use by an entity) or as an individual, to comply with the following license terms that apply to the Tool. If you are not willing to be bound by these terms, do not indicate your acceptance and do not download, install, or use the Tool.
REM
REM This data collection Tool is not Software or a Deliverable within the meaning of any contract that may be in place between Your organization and Palisade. This Tool is not commercially available, and is a part of the data intake process for use of Palisade services.
REM License Agreement
REM
REM PLEASE SCROLL DOWN AND READ ALL OF THE FOLLOWING TERMS AND CONDITIONS OF THIS LICENSE AGREEMENT ("Agreement") CAREFULLY. THIS AGREEMENT IS A LEGALLY BINDING CONTRACT BETWEEN YOU AND PALISADE COMPLIANCE, LLC THAT SETS FORTH THE TERMS AND CONDITIONS THAT GOVERN YOUR USE OF THE TOOL.
REM
REM YOU MUST ACCEPT AND ABIDE BY THESE TERMS AND CONDITIONS AS PRESENTED TO YOU - ANY CHANGES, ADDITIONS OR DELETIONS BY YOU TO THESE TERMS AND CONDITIONS WILL NOT BE ACCEPTED BY US AND WILL NOT BE PART OF THIS AGREEMENT.
REM
REM Definitions
REM "We," "Us," and "Our" refers to Palisade Compliance, LLC. "Palisade" refers to Palisade Compliance, LLC, Palisade Compliance LTD and their affiliates.
REM
REM "You" and "Your" refers to the individual or entity that wishes to use the Tool (as defined below) provided by Palisade.
REM
REM "Tool" refers to the tool(s), script(s) and/or product(s) and any applicable documentation provided to You by Palisade which You wish to access and use to measure Your usage of separately-licensed third-party software.
REM
REM Rights Granted
REM We grant You a non-exclusive, non-transferable limited right to use the Tool, subject to the terms of this Agreement, for the limited purpose of measuring Your usage of separately-licensed third-party software as a current client of Palisade. During the term of your Agreement with Palisade, You may allow Your agents and contractors (including, without limitation, outsourcers) to use the Tool for this purpose and You are responsible for their compliance with this Agreement in such use. You (including Your agents, contractors and/or outsourcers) may not use the Tool for any other purpose or beyond the end date of your Agreement with Palisade.
REM
REM Ownership and Restrictions
REM Palisade retains all ownership and intellectual property rights to the Tool. The Tool may be installed on one or more servers; provided, however, that You may only make one copy of the Tool for backup or archival purposes.
REM
REM You may not:
REM - use the Tool for Your own internal data processing or for any commercial or production purposes, or use the Tool for any purpose except the purpose stated herein;
REM - remove or modify any Tool markings or any notice of Palisade's proprietary rights;
REM - make the Tool available in any manner to any third party for use in the third party's business operations, without Our prior written consent;
REM - use the Tool to provide third party training or rent or lease the Tool or use the Tool for commercial time sharing or service bureau use;
REM - assign this Agreement or give or transfer the Tool or an interest in them to another individual or entity;
REM - cause or permit reverse engineering (unless required by law for interoperability), disassembly or decompilation of the Tool (the foregoing prohibition includes but is not limited to review of data structures or similar materials produced by Tool);
REM - use any Palisade name, trademark or logo without Our prior written consent .
REM
REM Disclaimer of Warranty
REM PALISADE DOES NOT GUARANTEE THAT THE TOOL WILL PERFORM ERROR-FREE OR UNINTERRUPTED. TO THE EXTENT NOT PROHIBITED BY LAW, THE TOOL ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND AND THERE ARE NO WARRANTIES, EXPRESS OR IMPLIED, OR CONDITIONS, INCLUDING WITHOUT LIMITATION, WARRANTIES OR CONDITIONS OF MERCHANTABILITY, NONINFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE, THAT APPLY TO THE TOOL.
REM
REM No Right to Technical Support
REM You acknowledge and agree that Palisade's technical support organization will not provide You with technical support for the Tool licensed under this Agreement.
REM
REM End of Agreement
REM You may terminate this Agreement by destroying all copies of the Tool. We have the right to terminate Your right to use the Tool at any time upon notice to You, in which case You shall destroy all copies of the Tool.
REM
REM Entire Agreement
REM You agree that this Agreement is the complete agreement for the Tool and supersedes all prior or contemporaneous agreements or representations, written or oral, regarding such Tool. If any term of this Agreement is found to be invalid or unenforceable, the remaining provisions will remain effective and such term shall be replaced with a term consistent with the purpose and intent of this Agreement.
REM
REM Limitation of Liability
REM IN NO EVENT SHALL PALISADE BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, PUNITIVE OR CONSEQUENTIAL DAMAGES, OR ANY LOSS OF PROFITS, REVENUE, DATA OR DATA USE, INCURRED BY YOU OR ANY THIRD PARTY. PALISADE'S ENTIRE LIABILITY FOR DAMAGES ARISING OUT OF OR RELATED TO THIS AGREEMENT, WHETHER IN CONTRACT OR TORT OR OTHERWISE, SHALL IN NO EVENT EXCEED ONE THOUSAND U.S. DOLLARS (U.S. $1,000).
REM
REM Export
REM Export laws and regulations of the United States and any other relevant local export laws and regulations may apply to the Tool. You agree that such export control laws govern Your use of the Tool (including technical data) provided under this Agreement, and You agree to comply with all such export laws and regulations (including "deemed export" and "deemed re-export" regulations). You agree that no data, information, and/or Tool (or direct product thereof) will be exported, directly or indirectly, in violation of any export laws, nor will they be used for any purpose prohibited by these laws including, without limitation, nuclear, chemical, or biological weapons proliferation, or development of missile technology.
REM
REM Other
REM 1. This Agreement is governed by the substantive and procedural laws of the State of New Jersey, USA. You and We agree to submit to the exclusive jurisdiction of, and venue in, the courts of Morris County, New Jersey in any dispute arising out of or relating to this Agreement.
REM
REM 2. You may not assign this Agreement or give or transfer the Tool or an interest in them to another individual or entity. If You grant a security interest in the Tool, the secured party has no right to use or transfer the Tool.
REM
REM 3. Except for actions for breach of Palisade's proprietary rights, no action, regardless of form, arising out of or relating to this Agreement may be brought by either party more than two years after the cause of action has accrued.
REM
REM 4. The relationship between You and Us is that of licensee/licensor. Nothing in this Agreement shall be construed to create a partnership, joint venture, agency, or employment relationship between the parties. The parties agree that they are acting solely as independent contractors hereunder and agree that the parties have no fiduciary duty to one another or any other special or implied duties that are not expressly stated herein. Neither party has any authority to act as agent for, or to incur any obligations on behalf of or in the name of the other.
REM
REM 5. This Agreement may not be modified and the rights and restrictions may not be altered or waived except in a writing signed by authorized representatives of You and of Us.
REM
REM 6. Any notice required under this Agreement shall be provided to the other party in writing.
REM
REM 7. Palisade Compliance highly recommends that customers execute the collect_db.sql script on one or more "non-production" environment(s) before executing on any "production" environments.

define SCRIPT_VERSION=1.4.10
alter session set NLS_DATE_FORMAT='YYYY-MM-DD_HH24:MI:SS';

SET ECHO OFF
SET DEFINE ON
SET TERMOUT ON
SET TIMING OFF
SET TAB OFF
SET TRIMOUT ON
SET TRIMSPOOL ON
SET SERVEROUTPUT ON
SET VERIFY OFF
SET PAUSE OFF
SET FEEDBACK OFF
SET PAGESIZE 5000
SET LINESIZE 1000
SET MARKUP HTML OFF
SET COLSEP ' '

define DATE_START=
define INSTANCE_NAME=XINST
define HOST_NAME=XHOST
define OUTPUT_PATH=
define CON_NAME=
define CDB_NAME=
define MARKER=**

col COL1 new_val DATE_START
col COL2 new_val INSTANCE_NAME
col COL3 new_val HOST_NAME
col COL4 new_val OUTPUT_PATH
col COL5 new_val CON_NAME
col COL6 new_val CDB_NAME
col COL7 new_val DATE_START_FORMATTED

select SYSDATE COL1 from dual;
select to_char(SYSDATE, 'YYYYMMDDHH24MISS') COL7 from dual;
select instance_name COL2, host_name COL3 FROM v$instance;
select '&&INSTANCE_NAME' || decode(VALUE, 'TRUE', '~' || replace(sys_context('USERENV', 'CON_NAME'), '$', '_'), '') COL2 from V$PARAMETER where name = 'enable_pluggable_database';
select '&&HOST_NAME.--&&INSTANCE_NAME.--' || to_char(to_date('&&DATE_START', 'YYYY-MM-DD_HH24:MI:SS'), 'YYYY-MM-DD_HH24-MI') COL4 from dual;
select '' COL5 from dual;
select sys_context('USERENV', 'CON_NAME') COL5 from dual;
select '' COL6 from dual;
select sys_context('USERENV', 'CDB_NAME') COL6 from dual;

SPOOL &&OUTPUT_PATH._db.csv
PROMPT HOST_NAME: &&HOST_NAME.
PROMPT INSTANCE_NAME: &&INSTANCE_NAME.
SHOW USER

PROMPT
PROMPT Palisade DB collection script version &&SCRIPT_VERSION
PROMPT
PROMPT Pluggable Database (only in 12c)
PROMPT CON_NAME: &&CON_NAME.
PROMPT CDB_NAME: &&CDB_NAME.

PROMPT
PROMPT === Check privileges
PROMPT Each table used in queries is checked to see if proper access is granted to current user
PROMPT It is acceptable to see errors related to missing table (script is not locked to DB version) - check for further errors on each Option/Pack
DECLARE
c integer;
t varchar2(100);
type array_t is varray(100) of varchar2(100);
array array_t := array_t('APPS.FND_NODES', 'APPS.FND_PRODUCT_GROUPS', 'CMPINSTALLATION_V', 'DBA_ADVISOR_TASKS', 'DBA_AWS', 'DBA_CUBES', 'DBA_ENCRYPTED_COLUMNS',
'DBA_FEATURE_USAGE_STATISTICS', 'DBA_FLASHBACK_ARCHIVE', 'DBA_FLASHBACK_ARCHIVE_TABLES', 'DBA_FLASHBACK_ARCHIVE_TS',
'DBA_LOB_PARTITIONS', 'DBA_LOB_SUBPARTITIONS', 'DBA_LOBS', 'DBA_OBJECT_TABLES', 'DBA_OBJECTS', 'DBA_SQL_PROFILES', 'DBA_SQLSET',
'DBA_TAB_PARTITIONS', 'DBA_TAB_SUBPARTITIONS', 'DBA_TABLES', 'DBA_TABLESPACES', 'DBA_USERS', 'DMSYS.DM$MODEL', 'DMSYS.DM$OBJECT',
'DMSYS.DM$P_MODEL', 'DVSYS.DBA_DV_REALM', 'GV$IM_SEGMENT', 'GV$INSTANCE', 'GV$PARAMETER', 'LBACSYS.LBAC$POLT', 'MDSYS.SDO_GEOM_METADATA_TABLE',
'ODM.ODM_MINING_MODEL', 'ODM_DOCUMENT', 'ODM_RECORD', 'OLAPSYS.DBA$OLAP_CUBES', 'SYS.DBA_MINING_MODELS', 'SYS.MODEL$', 'SYSMAN.MGMT_ADMIN_LICENSES',
'SYSMAN.MGMT_FU_REGISTRATIONS', 'SYSMAN.MGMT_FU_STATISTICS', 'SYSMAN.MGMT_LICENSE_CONFIRMATION', 'SYSMAN.MGMT_LICENSE_DEFINITIONS', 'SYSMAN.MGMT_LICENSED_TARGETS',
'SYSMAN.MGMT_LICENSES', 'SYSMAN.MGMT_TARGET_TYPES', 'SYSMAN.MGMT_TARGETS', 'V$ARCHIVE_DEST_STATUS', 'V$BLOCK_CHANGE_TRACKING',
'V$CONTAINERS', 'V$DATABASE', 'V$INSTANCE', 'V$OPTION', 'V$PARAMETER', 'V$VERSION');
q varchar2(5000);
res integer;

BEGIN
c := dbms_sql.open_cursor;
for i in 1..array.count loop
t := array(i);
q := 'select * from '||t;
begin
dbms_sql.parse(c, q, dbms_sql.native);
res:=dbms_sql.execute(c);
/*if dbms_sql.fetch_rows(c) > 0 then
dbms_output.put_line ('CHECKED TABLE '''||t|| ''' : OK');
end if;*/
EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN CHECKING TABLE ''' || t || ''' : ' || SQLERRM);
end;
end loop;
dbms_sql.close_cursor (c);
END;
/

SET HEADING ON
PROMPT
PROMPT === Databases, Instances, Users (ref: V$DATABASE, GV$INSTANCE, DBA_USERS)
PROMPT ==Databases
col platform_name format a30
col db_unique_name format a20
select DBID, NAME, to_char(CREATED,'YYYY_MM_DD') as CREATED, OPEN_MODE, DATABASE_ROLE, DATAGUARD_BROKER, PLATFORM_NAME, DB_UNIQUE_NAME, FS_FAILOVER_STATUS,
to_char(CONTROLFILE_CREATED,'YYYY_MM_DD') CONTROLFILE_CREATED, LOG_MODE, SWITCHOVER_STATUS, SUPPLEMENTAL_LOG_DATA_MIN, FORCE_LOGGING from V$DATABASE;
PROMPT
PROMPT ==Instances
col host_name format a50
col startup_time format a15
select INST_ID, INSTANCE_NAME, HOST_NAME, VERSION, to_char(STARTUP_TIME,'YYYY_MM_DD') as STARTUP_TIME, STATUS, PARALLEL, DATABASE_STATUS, INSTANCE_ROLE
from GV$INSTANCE order by INSTANCE_NAME;

PROMPT
PROMPT ==Containers 12c
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATABASES';
feature_detail varchar2(100) := 'CONTAINERS12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where upper(substr(object_name,1,100)) in (''V$CONTAINERS'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select CON_ID||'',''||NAME||'',''||DBID||'',''||OPEN_MODE||'',''||to_char(OPEN_TIME,''YYYY_MM_DD'')
from V$CONTAINERS order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT ==PDBs 12c
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATABASES';
feature_detail varchar2(100) := 'PDBs12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where upper(substr(object_name,1,100)) in (''DBA_PDBS'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select PDB_ID||'',''||PDB_NAME||'',''||DBID||'',''||STATUS
from DBA_PDBS order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT ==Dataguard Config
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATAGUARD_CONFIG';
feature_detail varchar2(100) := 'DB_UNIQUE_NAME';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where upper(substr(object_name,1,100)) in (''V$DATAGUARD_CONFIG'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select DB_UNIQUE_NAME
from V$DATAGUARD_CONFIG order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT ==Patches
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATABASES';
feature_detail varchar2(100) := 'Patches';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where upper(substr(object_name,1,100)) in (''REGISTRY$HISTORY'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select to_char(ACTION_TIME,''YYYY_MM_DD'')||'',''||ACTION||'',''||NAMESPACE||'',''||VERSION||'',''||COMMENTS
from SYS.REGISTRY$HISTORY order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT ==Patches12c
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATABASES';
feature_detail varchar2(100) := 'Patches12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where upper(substr(object_name,1,100)) in (''DBA_REGISTRY_SQLPATCH'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select to_char(ACTION_TIME,''YYYY_MM_DD'')||'',''||ACTION||'',''||PATCH_ID||'',''||STATUS||'',''||DESCRIPTION
from SYS.DBA_REGISTRY_SQLPATCH order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT ==DBA Registry
col comp_name format a40
col schema format a40
select MODIFIED,substr(COMP_NAME,1,40) as COMP_NAME,schema,VERSION,STATUS from DBA_REGISTRY order by modified desc;
PROMPT
PROMPT ==Users
col username format a30
col profile format a20
select distinct USERNAME, to_char(CREATED,'YYYY_MM_DD') created,
to_char(EXPIRY_DATE,'YYYY_MM_DD') expiry_date, replace(ACCOUNT_STATUS,' ','') status
from DBA_USERS
where lower(ACCOUNT_STATUS) not like '%locked%'
order by 1;

SET HEADING OFF
PROMPT
select distinct '&&MARKER.,GV$INSTANCE,'||
INST_ID||','||INSTANCE_NAME||','||HOST_NAME||','||VERSION||','||STARTUP_TIME||','||STATUS||','||PARALLEL||','||DATABASE_STATUS||','||INSTANCE_ROLE||','
from GV$INSTANCE order by 1;
select distinct '&&MARKER.,V$DATABASE,'||
NAME|| ','||DB_UNIQUE_NAME||','||OPEN_MODE||',"'||DATABASE_ROLE||'","'||REMOTE_ARCHIVE||'","'||
DATAGUARD_BROKER||'","'||GUARD_STATUS||'","'||to_char(CREATED,'YYYY_MM_DD')||','||to_char(CONTROLFILE_CREATED,'YYYY_MM_DD')||','
from V$DATABASE order by 1;

PROMPT
PROMPT === Version, Parameters, DBA_FEATURE_USAGE_STATISTICS, Options (filtered) (ref: V$VERSION, V$PARAMETER, GV$PARAMETER, DBA_FEATURE_USAGE_STATISTICS, V$OPTION)
select '&&MARKER.,V$VERSION,"'||replace(replace(replace(BANNER,'Oracle Database ',''),'Release ',''),' - Production','')||'",'
from V$VERSION
where lower(banner) not like '%nlsrtl%'
and lower(banner) not like 'core%'
and lower(banner) not like 'pl/sql%';
PROMPT ==========================================================
select distinct '&&MARKER.,V$PARAMETER,'||NAME||','||VALUE||','||decode(isdefault,'TRUE','DEFAULT','NOT DEFAULT')||','
from V$PARAMETER
where lower(NAME) like '%fal%'
or lower(NAME) like '%compatible%'
or lower(NAME) like '%cluster%'
or lower(NAME) like '%ddl%'
or lower(NAME) like '%goldengate%'
or lower(NAME) like '%inmemory%'
or lower(NAME) like '%unique%'
or lower(NAME) like '%management%pack%access%'
or lower(NAME) like '%pluggable%'
or lower(NAME) like '%ddl%logging%'
or lower(NAME) like '%standby%'
or lower(NAME) like '%dictionary%accessibility%'
or lower(NAME) like '%log%archive%'
or lower(NAME) like '%heat%map%'
order by 1;
PROMPT ==========================================================
select distinct '&&MARKER.,GV$PARAMETER,'||INST_ID||','||NAME||','||VALUE||','||decode(isdefault,'TRUE','DEFAULT','NOT DEFAULT')||','
from GV$PARAMETER
where lower(NAME) like '%fal%'
or lower(NAME) like '%compatible%'
or lower(NAME) like '%cluster%'
or lower(NAME) like '%ddl%'
or lower(NAME) like '%goldengate%'
or lower(NAME) like '%inmemory%'
or lower(NAME) like '%unique%'
or lower(NAME) like '%management%pack%access%'
or lower(NAME) like '%pluggable%'
or lower(NAME) like '%ddl%logging%'
or lower(NAME) like '%standby%'
or lower(NAME) like '%dictionary%accessibility%'
or lower(NAME) like '%log%archive%'
or lower(NAME) like '%heat%map%'
order by 1;

PROMPT ==========================================================
select distinct '&&MARKER.,DBA_FEATURE_USAGE_STATISTICS,"'||
NAME||'","''","'||CURRENTLY_USED||'",'||DETECTED_USAGES||','||to_char(LAST_USAGE_DATE,'YYYY_MM_DD')||','||to_char(FIRST_USAGE_DATE,'YYYY_MM_DD')||','||DBID||','||AUX_COUNT||','
from DBA_FEATURE_USAGE_STATISTICS
where detected_usages > 0
order by 1;
PROMPT ==========================================================
select distinct '&&MARKER.,DBA_FEATURE_USAGE_STATISTICS,"'||
NAME||'","'||replace(replace(replace(to_char(substr(FEATURE_INFO, 1, 1000)), chr(10), ' '), chr(13), ' '),'"','''')||'","'||CURRENTLY_USED||'",'||DETECTED_USAGES||','||to_char(LAST_USAGE_DATE,'YYYY_MM_DD')||','||to_char(FIRST_USAGE_DATE,'YYYY_MM_DD')||','||DBID||','
from DBA_FEATURE_USAGE_STATISTICS
where feature_info is not null
order by 1;
PROMPT ==========================================================
select '&&MARKER.,V$OPTION,,,,,,,,,"'||PARAMETER||'","'||VALUE||'",'
from V$OPTION
order by 1;

PROMPT
PROMPT === Option Partitioning (ref: DBA_OBJECTS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'PARTITIONING';
feature_detail varchar2(100) := 'PARTITIONED_SEGMENTS';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct OWNER||'',''||OBJECT_TYPE||'',''||substr(OBJECT_NAME,1,1000)||'',''||to_char(min(CREATED),''YYYY_MM_DD'')||'',''||to_char(min(LAST_DDL_TIME),''YYYY_MM_DD'')
from DBA_OBJECTS where OBJECT_TYPE LIKE ''%PARTITION%''
and OWNER not in (''ADMIN'',''ADMINISTRATOR'',''ANONYMOUS'',''SH'',''SYS'',''SYSADM'',''SYSADMIN'',''SYSMAN'',''SYSTEM'')
and OBJECT_NAME not like ''AW$%''
group by OWNER, OBJECT_TYPE, OBJECT_NAME
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Multitenant (ref: V$CONTAINERS, V$DATABASE)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'MULTITENANT';
feature_detail varchar2(100) := 'V$CONTAINERS';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where upper(substr(object_name,1,100)) in (''V$CONTAINERS'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct b.CDB||'',''||a.NAME||'',''||a.OPEN_MODE||'',''||to_char(a.OPEN_TIME,''YYYY_MM_DD'')||'',''||a.CON_ID
from V$CONTAINERS a, V$DATABASE b
where a.con_id not in (0, 1, 2)
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Active Data Guard (ref: V$ARCHIVE_DEST_STATUS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ACTIVE_DATA_GUARD';
feature_detail varchar2(100) := 'V$ARCHIVE_DEST_STATUS';

BEGIN
c := dbms_sql.open_cursor;
q := 'select a.DEST_ID||'',''||a.DEST_NAME||'',''||a.DB_UNIQUE_NAME||'',''||a.STATUS||'',''||a.TYPE||'',''||a.DATABASE_MODE||'',''||a.RECOVERY_MODE||'',''||a.PROTECTION_MODE||'',"''||a.DESTINATION||''",''
from V$ARCHIVE_DEST_STATUS a
where a.TYPE!=''LOCAL''
order by a.DEST_ID';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Active Data Guard (ref: V$BLOCK_CHANGE_TRACKING, V$DATABASE)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ACTIVE_DATA_GUARD';
feature_detail varchar2(100) := 'V$BLOCK_CHANGE_TRACKING';

BEGIN
c := dbms_sql.open_cursor;
q := 'select b.DATABASE_ROLE||'',''||a.STATUS||'',''||a.FILENAME||'',''||a.BYTES
from V$BLOCK_CHANGE_TRACKING a, V$DATABASE b
where upper(b.DATABASE_ROLE) like ''PHYSICAL STANDBY''
and upper(a.STATUS) = ''ENABLED''';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option OLAP (ref: OLAPSYS.DBA$OLAP_CUBES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OLAP';
feature_detail varchar2(100) := 'OLAPSYS.DBA$OLAP_CUBES';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''OLAPSYS'' and upper(substr(object_name,1,100)) = ''DBA$OLAP_CUBES''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct OWNER||'',''||CUBE_NAME||'',''||DISPLAY_NAME
from OLAPSYS.DBA$OLAP_CUBES
where OWNER not in (''SH'',''SYS'')';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option OLAP (ref: DBA_CUBES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OLAP';
feature_detail varchar2(100) := 'DBA_CUBES';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct OWNER||'',''||CUBE_NAME||'',''||AW_NAME
from DBA_CUBES
where OWNER not in (''SH'',''SYS'')';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option OLAP (ref: DBA_AWS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OLAP';
feature_detail varchar2(100) := 'ANALYTIC_WORKSPACES';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct OWNER||'',''||AW_NAME||'',''||PAGESPACES
from DBA_AWS
where OWNER not in (''SH'',''SYS'')';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Data Mining (ref: DMSYS.DM$OBJECT)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATA_MINING';
feature_detail varchar2(100) := '10g1.DM$OBJECT';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''DMSYS'' and upper(substr(object_name,1,100)) = ''DM$OBJECT''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select count(*) from DMSYS.DM$OBJECT';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 1000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Data Mining (ref: DMSYS.DM$MODEL)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATA_MINING';
feature_detail varchar2(100) := '10g1.DM$MODEL';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''DMSYS'' and upper(substr(object_name,1,100)) = ''DM$MODEL''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select count(*) from DMSYS.DM$MODEL';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Data Mining (ref: DMSYS.DM$P_MODEL)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATA_MINING';
feature_detail varchar2(100) := '102.DM$P_MODEL';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''DMSYS'' and upper(substr(object_name,1,100)) = ''DM$P_MODEL''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select count(*) from DMSYS.DM$P_MODEL';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Data Mining (ref: SYS.MODEL$)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATA_MINING';
feature_detail varchar2(100) := '11g.SYS.MODEL$';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYS'' and upper(substr(object_name,1,100)) = ''MODEL$''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select count(*) from SYS.MODEL$';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Data Mining (ref: SYS.DBA_MINING_MODELS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATA_MINING';
feature_detail varchar2(100) := '11g+.DBA_MINING_MODELS';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYS'' and upper(substr(object_name,1,100)) = ''DBA_MINING_MODELS''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct MODEL_NAME||'',''||to_char(CREATION_DATE,''YYYY_MM_DD'')
from SYS.DBA_MINING_MODELS order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Label Security (ref: LBACSYS.LBAC$POLT)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'LABEL_SECURITY';
feature_detail varchar2(100) := 'LBAC$POLT_COUNT';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''LBACSYS'' and upper(substr(object_name,1,100)) = ''LBAC$POLT''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select count(*) from LBACSYS.LBAC$POLT
where OWNER <> ''SA_DEMO''';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Security (ref: DBA_TABLESPACES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_SECURITY';
feature_detail varchar2(100) := 'TABLESPACE_ENCRYPTION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct tablespace_name||'',''||encrypted
from DBA_TABLESPACES
where encrypted =''YES''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Security (ref: DBA_LOBS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_SECURITY';
feature_detail varchar2(100) := 'SECUREFILES_ENCRYPTION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct owner||'',''||table_name||'',''||column_name
from dba_lobs
where encrypt not in (''NO'', ''NONE'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Security (ref: DBA_LOB_PARTITIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_SECURITY';
feature_detail varchar2(100) := 'SECUREFILES_ENCRYPTION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct table_owner||'',''||table_name||'',''||column_name
from dba_lob_partitions
where encrypt not in (''NO'', ''NONE'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Security (ref: DBA_LOB_SUBPARTITIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_SECURITY';
feature_detail varchar2(100) := 'SECUREFILES_ENCRYPTION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct table_owner||'',''||table_name||'',''||column_name
from dba_lob_subpartitions
where encrypt not in (''NO'', ''NONE'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Security (ref: DBA_ENCRYPTED_COLUMNS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_SECURITY';
feature_detail varchar2(100) := 'COLUMN_ENCRYPTION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct owner||'',''||table_name||'',''||column_name
from DBA_ENCRYPTED_COLUMNS
where (OWNER,TABLE_NAME,COLUMN_NAME) not in
(select OWNER,TABLE_NAME,COLUMN_NAME from DBA_LOBS)
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_TABLES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'TABLE_COMPRESSION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct owner||'',''||table_name
from dba_tables
where compress_for in (''FOR ALL OPERATIONS'', ''OLTP'', ''ADVANCED'')
and owner not in (''SYSMAN'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_TAB_PARTITIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'TABLE_COMPRESSION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct table_owner||'',''||table_name
from dba_tab_partitions
where compress_for in (''FOR ALL OPERATIONS'', ''OLTP'', ''ADVANCED'')
and table_owner not in (''SYSMAN'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_TAB_SUBPARTITIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'TABLE_COMPRESSION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct table_owner||'',''||table_name
from dba_tab_subpartitions
where compress_for in (''FOR ALL OPERATIONS'', ''OLTP'', ''ADVANCED'')
and table_owner not in (''SYSMAN'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_LOBS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'SECUREFILES_COMPRESSION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct owner||'',''||table_name||'',''||column_name
from dba_lobs
where compression not in (''NO'', ''NONE'') or deduplication not in (''NO'', ''NONE'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_LOB_PARTITIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'SECUREFILES_COMPRESSION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct table_owner||'',''||table_name||'',''||column_name
from dba_lob_partitions
where compression not in (''NO'', ''NONE'') or deduplication not in (''NO'', ''NONE'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_LOB_SUBPARTITIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'SECUREFILES_COMPRESSION';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct table_owner||'',''||table_name||'',''||column_name
from dba_lob_subpartitions
where compression not in (''NO'', ''NONE'') or deduplication not in (''NO'', ''NONE'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_FLASHBACK_ARCHIVE, DBA_FLASHBACK_ARCHIVE_TS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'DBA_FLASHBACK_ARCHIVE';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct a.FLASHBACK_ARCHIVE_NAME||'',''||b.TABLESPACE_NAME||'',''||to_char(a.CREATE_TIME,''YYYY_MM_DD'')||'',''||to_char(a.LAST_PURGE_TIME,''YYYY_MM_DD'')||'',''||a.STATUS
from DBA_FLASHBACK_ARCHIVE a
left join DBA_FLASHBACK_ARCHIVE_TS b on a.FLASHBACK_ARCHIVE# = b.FLASHBACK_ARCHIVE#
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Advanced Compression (ref: DBA_FLASHBACK_ARCHIVE_TABLES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'ADVANCED_COMPRESSION';
feature_detail varchar2(100) := 'DBA_FLASHBACK_ARCHIVE_TABLES';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct FLASHBACK_ARCHIVE_NAME||'',''||OWNER_NAME||'',''||TABLE_NAME||'',''||ARCHIVE_TABLE_NAME
from DBA_FLASHBACK_ARCHIVE_TABLES
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Options Database Vault, Audit Vault (ref: DVSYS.DBA_DV_REALM)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATABASE_VAULT';
feature_detail varchar2(100) := 'DVSYS.DBA_DV_REALM';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''DVSYS'' and upper(object_name) = ''DBA_DV_REALM''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct NAME||'',''||DESCRIPTION||'',''||ENABLED
from DVSYS.DBA_DV_REALM a
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Options Database Vault, Audit Vault (ref: DVSYS.DBA_DV_REALM_AUTH)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATABASE_VAULT';
feature_detail varchar2(100) := 'DVSYS.DBA_DV_REALM_AUTH';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''DVSYS'' and upper(object_name) = ''DBA_DV_REALM_AUTH''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select REALM_NAME||'',''||count(*)
from DVSYS.DBA_DV_REALM_AUTH a
where REALM_NAME in (''Oracle Database Vault'')
group by REALM_NAME
having count(*) > 0
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Options Database Vault (ref: DBA_USERS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DATABASE_VAULT';
feature_detail varchar2(100) := 'DVSYS_SCHEMA';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct username
from dba_users
where upper(username) in (''DVSYS'',''DVF'')';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Options Audit Vault (ref: DBA_USERS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'AUDIT_VAULT';
feature_detail varchar2(100) := 'AVSYS_SCHEMA';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct username
from dba_users
where upper(username) in (''AVSYS'')';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option In-Memory Database (ref: GV$IM_SEGMENTS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DB_IN_MEMORY';
feature_detail varchar2(100) := 'GV$IM_SEGMENTS';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where upper(substr(object_name,1,100)) = ''GV$IM_SEGMENTS''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select a.OWNER||'',''||a.SEGMENT_NAME||'',''||a.PARTITION_NAME
from GV$IM_SEGMENTS a
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option In-Memory Database (ref: DBA_TABLES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DB_IN_MEMORY';
feature_detail varchar2(100) := 'INMEMORY_ENABLED_TABLES';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct table_name)
from ALL_TAB_COLUMNS where upper(table_name) = ''DBA_TABLES'' and upper(column_name) = ''INMEMORY''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct table_name
from dba_tables
where upper(inmemory) in (''ENABLED'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option In-Memory Database (ref: DBA_TAB_PARTITIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DB_IN_MEMORY';
feature_detail varchar2(100) := 'INMEMORY_ENABLED_TABLES';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct table_name)
from ALL_TAB_COLUMNS where upper(table_name) = ''DBA_TAB_PARTITIONS'' and upper(column_name) = ''INMEMORY''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct table_name
from dba_tab_partitions
where upper(inmemory) in (''ENABLED'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option In-Memory Database (ref: DBA_OBJECT_TABLES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'DB_IN_MEMORY';
feature_detail varchar2(100) := 'INMEMORY_ENABLED_TABLES';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct table_name)
from ALL_TAB_COLUMNS where upper(table_name) = ''DBA_OBJECT_TABLES'' and upper(column_name) = ''INMEMORY''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct table_name
from dba_object_tables
where upper(inmemory) in (''ENABLED'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Option Content and Records Database (ref: DBA_USERS, ODM_DOCUMENT, ODM_RECORD)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'CONTENT_AND_RECORDS';
feature_detail varchar2(100) := 'ODM_DOCUMENT';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct username)
from DBA_USERS
where upper(USERNAME)=''CONTENT''';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
dbms_output.put_line ('&&MARKER.,'||feature||',CONTENT_SCHEMA,1,CONTENT,');
q := 'select count(*) from odm_document';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
q := 'select count(*) from odm_record';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM License Confirmation (ref: DBA_TABLES, SYSMAN.MGMT_LICENSE_CONFIRMATION, SYSMAN.MGMT_TARGETS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'MGMT_LICENSE_CONFIRMATION';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_LICENSE_CONFIRMATION'',''MGMT_TARGETS'')';
ntables := '2';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select a.confirmation||'',''||to_char(a.confirmed_time,''YYYY_MM_DD'')||'',''||b.target_name||'',''||b.target_type
from SYSMAN.MGMT_LICENSE_CONFIRMATION a
left join SYSMAN.MGMT_TARGETS b on a.target_guid = b.target_guid
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Access (ref: DBA_TABLES, SYSMAN.MGMT_LICENSE_DEFINITIONS, SYSMAN.MGMT_ADMIN_LICENSES, SYSMAN.MGMT_LICENSES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'MGMT_ADMIN_LICENSES';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_LICENSE_DEFINITIONS'',''MGMT_ADMIN_LICENSES'',''MGMT_LICENSES'')';
ntables := '3';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select * from (select distinct b.pack_name||'',''||a.pack_display_label||'',''||c.pack_access_agreed||'',''||a.target_type
from SYSMAN.MGMT_LICENSE_DEFINITIONS a
left join SYSMAN.MGMT_ADMIN_LICENSES b on a.pack_label = b.pack_name,
(select decode(count(*), 0, ''NO'', ''YES'') as PACK_ACCESS_AGREED
from SYSMAN.MGMT_LICENSES where upper(I_AGREE)=''YES'') c
where PACK_ACCESS_AGREED = ''YES'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Target Databases (ref: SYSMAN.MGMT_TARGETS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'MGMT_TARGETS';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_TARGETS'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select HOST_NAME||'',''||TARGET_NAME||'',''||to_char(LOAD_TIMESTAMP,''YYYY_MM_DD'')||'',''||to_char(LAST_LOAD_TIME,''YYYY_MM_DD'')||'',''||TARGET_TYPE
from SYSMAN.MGMT_TARGETS
where TARGET_TYPE like ''%database%'' or TARGET_TYPE like ''%pdb%''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Grid - Packs Agreed 11g+ (ref: DBA_TABLES, SYSMAN.MGMT_TARGETS, SYSMAN.MGMT_TARGET_TYPES, SYSMAN.MGMT_LICENSE_DEFINITIONS, SYSMAN.MGMT_LICENSED_TARGETS, SYSMAN.MGMT_LICENSE_CONFIRMATION)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'GRID_PACKS_AGREED11g+';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_TARGETS'',''MGMT_TARGET_TYPES'',''MGMT_LICENSE_DEFINITIONS'',''MGMT_LICENSED_TARGETS'',''MGMT_LICENSE_CONFIRMATION'')';
ntables := '5';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select mtt.type_display_name||'',''||mtg.host_name||'',''||mtg.target_name||'',''||mld.pack_display_label||'',''||
decode(mlc.target_guid, null, ''NO'', ''YES'')||'',''||to_char(mlc.confirmed_time,''YYYY_MM_DD'')||'',''||mtg.target_type
from SYSMAN.MGMT_TARGETS mtg
left join SYSMAN.MGMT_TARGET_TYPES mtt on mtg.target_type = mtt.target_type
join SYSMAN.MGMT_LICENSE_DEFINITIONS mld on mtg.target_type = mld.target_type
left join SYSMAN.MGMT_LICENSE_CONFIRMATION mlc on mtg.target_guid = mlc.target_guid
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Cloud Control - Packs Agreed 13c (ref: DBA_TABLES, SYSMAN.MGMT_TARGETS, SYSMAN.MGMT_TARGET_TYPES, SYSMAN.MGMT_LICENSE_DEFINITIONS, SYSMAN.MGMT_LICENSED_TARGETS, SYSMAN.MGMT_LICENSE_CONFIRMATION)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'CLOUD_CONTROL_PACKS_AGREED13c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_TARGETS'',''MGMT_TARGET_TYPES'',''MGMT_LICENSE_DEFINITIONS'',''MGMT_LICENSED_TARGETS'',''MGMT_LICENSE_CONFIRMATION'')';
ntables := '5';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'SELECT mtt.type_display_name||'',''||atrg.host_name||'',''||atrg.target_guid||'',''||atrg.target_name||'',''||mld.pack_display_label||'',''
||decode(mlc.target_guid, NULL, ''NO'', ''YES'')||'',''||to_char(mlc.confirmed_time,''YYYY_MM_DD'')||'',''||atrg.target_type
FROM
(SELECT decode(mtgd.target_name, mtg.target_name, mtg.target_guid, mtgd.target_guid) AS target_guid, mtg.target_name,
mtg.target_type, mtg.host_name, mlt.pack_name
FROM sysman.mgmt_targets mtg
JOIN sysman.mgmt_licensed_targets mlt ON mlt.target_guid=mtg.target_guid
JOIN sysman.mgmt_targets mtgd ON mlt.from_target_guid=mtgd.target_guid) atrg
LEFT JOIN (SELECT DISTINCT pack_label, pack_display_label FROM sysman.mgmt_license_definitions) mld ON mld.pack_label = atrg.pack_name
LEFT JOIN sysman.mgmt_target_types mtt ON mtt.target_type = atrg.target_type
LEFT JOIN sysman.mgmt_license_confirmation mlc ON mlc.target_guid = atrg.target_guid
ORDER BY 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Cloud Control - Packs Usage Detais 13c (ref: SYSMAN.MGMT_TARGETS, SYSMAN.MGMT_TARGET_TYPES, SYSMAN.MGMT_LICENSE_DEFINITIONS, SYSMAN.MGMT_LICENSE_USAGE_LOG, SYSMAN.MGMT_LICENSE_USAGE_HISTORY)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'CLOUD_CONTROL_PACKS_USAGE_DETAILS13c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_TARGETS'',''MGMT_TARGET_TYPES'',''MGMT_LICENSE_USAGE_LOG'',''MGMT_LICENSE_USAGE_HISTORY'')';
ntables := '4';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'SELECT mtt.type_display_name||'',''||mtg.host_name||'',''||mtg.target_name||'',''||to_char(mtg.load_timestamp,''YYYY/MM/DD'')
||'',''||mtg.manage_status||'',''||mtg.is_active||'',''||mtg.is_ready||'',''||mtg.is_cloud_service||'',''||mlu.pack_label||'',''||mld.pack_display_label
||'',''||mlu.src_id||'',''||mlu.src_type||'',''||mlu.t_cnt||'',''||to_char(mlu.last_used_timestamp,''YYYY/MM/DD'')
FROM sysman.mgmt_targets mtg
LEFT JOIN sysman.mgmt_target_types mtt ON mtt.target_type = mtg.target_type
LEFT JOIN (SELECT pack_label, target_type, target_name, src_id, src_type, to_char(SUM(to_number(COUNT))) AS t_cnt, MAX(last_used_timestamp) AS last_used_timestamp FROM
(SELECT pack_label, target_type, target_name, src_id, src_type, COUNT, last_used_timestamp
FROM sysman.mgmt_license_usage_history WHERE target_type IS NOT NULL AND PACK_LABEL is not null
UNION
SELECT pack_label, target_type, target_name, src_id, src_type, COUNT, last_used_timestamp
FROM sysman.mgmt_license_usage_log WHERE target_type IS NOT NULL AND PACK_LABEL is not null)
GROUP BY pack_label, target_type, target_name, src_id, src_type) mlu ON (mlu.target_name = mtg.target_name AND mlu.target_type = mtg.target_type)
LEFT JOIN (SELECT DISTINCT pack_label,pack_display_label FROM sysman.mgmt_license_definitions) mld ON mld.pack_label = mlu.pack_label
ORDER BY 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Cloud Control - Packs Usage 13c (ref: DBA_TABLES, SYSMAN.MGMT_FU_REGISTRATIONS, SYSMAN.MGMT_FU_STATISTICS, SYSMAN.MGMT_FU_LICENSE_MAP, SYSMAN.MGMT_LICENSE_DEFINITIONS, SYSMAN.MGMT$TARGET)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'CLOUD_CONTROL_PACKS_USAGE13c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_FU_REGISTRATIONS'',''MGMT_FU_STATISTICS'',''MGMT_FU_LICENSE_MAP'',''MGMT_LICENSE_DEFINITIONS'',''MGMT$TARGET'')';
ntables := '5';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select fs.feature_id||'',''||fr.feature_name||'',''||nvl(flm.pack_label,ldd.pack_label)||'',''||nvl(ld.pack_display_label,ldd.pack_display_label)
||'',''||fs.target_guid||'',''|| t.target_name||'',''||t.target_type||'',''||fs.version||'',''||fs.isused||'',''||fs.detected_samples
||'',''||to_char(fs.last_usage_date,''YYYY/MM/DD'')||'',''||to_char(fs.first_sample_date,''YYYY/MM/DD'')||'',''||to_char(fs.last_sample_date,''YYYY/MM/DD'')||'',''||fs.error_count
from SYSMAN.mgmt_fu_statistics fs
left join SYSMAN.mgmt_fu_registrations fr
on fr.feature_id = fs.feature_id
left join (select target_guid, target_name, display_name, target_type, type_display_name, last_load_time_utc, creation_date from sysman.mgmt$target) t
on t.target_guid = fs.target_guid
left join sysman.mgmt_fu_license_map flm
on flm.feature_id = fs.feature_id and flm.target_type = t.target_type
left join sysman.mgmt_license_definitions ld
on ld.pack_label = flm.pack_label and ld.target_type = t.target_type
left join (select pack_display_label, pack_label, target_type from sysman.mgmt_license_definitions) ldd
on ldd.pack_display_label = fr.feature_name and ldd.target_type = t.target_type
order by 1 asc';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/


PROMPT
PROMPT === OEM Cloud Control - Target Status 13c (ref: SYSMAN.MGMT_TARGETS, SYSMAN.MGMT$AVAILABILITY_HISTORY)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'CLOUD_CONTROL_TARGET_STATUS13c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_TARGETS'',''MGMT$AVAILABILITY_HISTORY'')';
ntables := '2';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'SELECT tsh.target_name||'',''||tsh.target_type||'',''||tsh.target_guid||'',''||to_char(T.load_timestamp,''YYYY/MM/DD'')||'',''||tsh.start_ts||'',''||tsh.end_ts||'',''||tsh.availability_status
FROM
(SELECT target_name, target_type
, target_guid, to_char(start_timestamp,''YYYY/MM/DD HH24:MI:SS'') AS start_ts, to_char(end_timestamp,''YYYY/MM/DD HH24:MI:SS'') AS end_ts, availability_status
, RANK() OVER (PARTITION BY target_name ORDER BY start_timestamp DESC) AS target_rank
FROM sysman.mgmt$availability_history WHERE (nvl(end_timestamp,sysdate) - start_timestamp) * 24 * 60 * 60 > 180) tsh
JOIN sysman.mgmt_targets T ON T.target_guid = tsh.target_guid
WHERE tsh.target_rank <= 2
ORDER BY 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Cloud Control - Plugins 13c (ref: SYSMAN.EM_PLUGIN_VERSION, SYSMAN.MGMT$CURR_DEPLOYED_PLUGIN_AGT, SYSMAN.MGMT_PLUGIN_COMP_INFO)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'PLUGINS13c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''EM_PLUGIN_VERSION'',''MGMT$CURR_DEPLOYED_PLUGIN_AGT'',''MGMT_PLUGIN_COMP_INFO'')';
ntables := '3';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'SELECT distinct pv.CATEGORY||'',''||pv.display_name||'',''||pv.plugin_id
||'',''||pv.plugin_version_id||'',''||pv.VERSION||'',''||TO_CHAR(pv.release_date,''YYYY/MM/DD'')
||'',''||TO_CHAR(pv.last_update_date,''YYYY/MM/DD'')||'',''||TO_CHAR(pci.update_timestamp,''YYYY/MM/DD'')||'',''||pag.agent_name||'',''||pag.deployed_date
FROM sysman.em_plugin_version pv
LEFT JOIN (select distinct plugin_id, version, update_timestamp from sysman.mgmt_plugin_comp_info where component_name like ''%_MDS'') pci
ON pv.plugin_id=pci.plugin_id AND pv.VERSION=pci.VERSION
LEFT JOIN (SELECT plugin_id, VERSION, agent_name, deployed_date FROM sysman.mgmt$curr_deployed_plugin_agt WHERE content_type=''Plugin'') pag
ON pv.plugin_id=pag.plugin_id AND pv.VERSION=pag.VERSION
ORDER BY 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Usage 12c (ref: DBA_TABLES, SYSMAN.MGMT_FU_REGISTRATIONS, SYSMAN.MGMT_FU_STATISTICS, SYSMAN.MGMT_TARGETS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'PACKS_USAGE12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_FU_REGISTRATIONS'',''MGMT_FU_STATISTICS'',''MGMT_TARGETS'')';
ntables := '3';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select * from (
select reg.feature_name||'',''||tgts.target_name||'',''||tgts.display_name||'',''||tgts.type_display_name||'',''||tgts.host_name||'',''||
DECODE(stat.isused, 1, ''TRUE'', ''FALSE'')||'',''||stat.detected_samples||'',''||to_char(stat.last_usage_date,''YYYY_MM_DD'')||'',''||to_char(stat.first_sample_date,''YYYY_MM_DD'')
from SYSMAN.mgmt_fu_registrations reg,
SYSMAN.mgmt_fu_statistics stat,
SYSMAN.mgmt_targets tgts
where (stat.isused = 1 or stat.detected_samples > 0)
and stat.target_guid = tgts.target_guid
and reg.feature_id = stat.feature_id
and reg.collection_mode = 2
and (tgts.target_type like ''%database%'' or tgts.target_type like ''%pdb%'')
order by 1
)';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Feature Usage 12c (ref: DBA_TABLES, SYSMAN.MGMT_FU_REGISTRATIONS, SYSMAN.MGMT_FU_STATISTICS, SYSMAN.MGMT_TARGETS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'PACKS_FEATURE_USAGE12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_FU_REGISTRATIONS'',''MGMT_FU_STATISTICS'',''MGMT_TARGETS'',''MGMT_FU_LICENSE_MAP'')';
ntables := '4';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select * from (
select reg.feature_name||'',''||lmap.pack_label||'',''||tgts.target_name||'',''||tgts.display_name||'',''||tgts.type_display_name||'',''||tgts.host_name||'',''||
DECODE(stat.isused, 1, ''TRUE'', ''FALSE'')||'',''||freg.feature_name||'',''||DECODE(fstat.isused, 1, ''TRUE'', ''FALSE'')||'',''||
fstat.detected_samples||'',''||to_char(fstat.last_usage_date,''YYYY_MM_DD'')||'',''||to_char(fstat.first_sample_date,''YYYY_MM_DD'')
from SYSMAN.mgmt_fu_registrations reg,
SYSMAN.mgmt_fu_statistics stat,
SYSMAN.mgmt_targets tgts,
SYSMAN.mgmt_fu_registrations freg,
SYSMAN.mgmt_fu_statistics fstat,
SYSMAN.mgmt_fu_license_map lmap
where (stat.isused = 1 or stat.detected_samples > 0 or fstat.isused = 1 or fstat.detected_samples > 0)
and stat.target_guid = tgts.target_guid
and reg.feature_id = stat.feature_id
and reg.collection_mode = 2
and lmap.pack_id = reg.feature_id
and lmap.feature_id = freg.feature_id
and freg.feature_id = fstat.feature_id
and fstat.target_guid = tgts.target_guid
and (tgts.target_type like ''%database%'' or tgts.target_type like ''%pdb%'')
order by 1
)';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Change Management Usage 12c (ref: SYSMAN.MGMT_CM_COMPARISON_VERSIONS, SYSMAN.MGMT_CM_COMPARISONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'CHANGE_MGMT_USAGE12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_CM_COMPARISON_VERSIONS'',''MGMT_CM_COMPARISONS'')';
ntables := '2';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct cc.comparison_owner||'',''||cc.description||'',''||to_char(ccv.comparison_time,''YYYY_MM_DD'')||'',''||tgts.target_name||'',''||tgts.target_type
from SYSMAN.MGMT_CM_COMPARISON_VERSIONS ccv
join SYSMAN.MGMT_CM_COMPARISONS cc on ccv.comparison_guid = cc.comparison_guid
left join SYSMAN.MGMT_JOB_TARGET jt on cc.job_id = jt.job_id
left join SYSMAN.MGMT_TARGETS tgts on jt.target_guid = tgts.target_guid
where tgts.target_type like ''%database%'' or tgts.target_type like ''%pdb%''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Blackout Usage 12c (ref: SYSMAN.MGMT_BLACKOUT_HISTORY)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'BLACKOUT_USAGE12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_BLACKOUT_HISTORY'',''MGMT_BLACKOUT_TARGET_DETAILS'',''MGMT_TARGETS'')';
ntables := '3';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct mbh.occurrence_number||'',''||to_char(mbh.start_time,''YYYY_MM_DD'')||'',''||to_char(mbh.end_time,''YYYY_MM_DD'')||'',''||tgts.target_name||'',''||tgts.target_type
from SYSMAN.MGMT_BLACKOUT_HISTORY mbh
join SYSMAN.MGMT_BLACKOUT_TARGET_DETAILS mbtd on mbh.blackout_guid = mbtd.blackout_guid
join SYSMAN.MGMT_TARGETS tgts on mbtd.target_guid = tgts.target_guid
where tgts.target_type like ''%database%'' or tgts.target_type like ''%pdb%''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Configuration Management Usage 12c (ref: SYSMAN.MGMT$TARGET_POLICIES, SYSMAN.MGMT$TARGET_POLICY_EVAL_SUMM)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'CONFIGURATION_MGMT_USAGE12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT$TARGET_POLICIES'',''MGMT$TARGET_POLICY_EVAL_SUMM'')';
ntables := '2';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct tp.target_name||'',''||tp.target_type||'',''||tp.policy_name||'',''||tp.is_enabled||'',''||tpe.last_evaluation_date
from SYSMAN.MGMT$TARGET_POLICIES tp
left join SYSMAN.MGMT$TARGET_POLICY_EVAL_SUMM tpe on tp.policy_guid = tpe.policy_guid and tp.target_guid = tpe.target_guid
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Data Masking Usage 12c (ref: SYSMAN.MGMT_DM_SS_COLUMNS, SYSMAN.MGMT_DM_JOB_EXECUTIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'DATA_MASKING_USAGE12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_DM_SS_COLUMNS'',''MGMT_DM_JOB_EXECUTIONS'')';
ntables := '2';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct dc.table_schema||'',''||dc.table_name||'',''||to_char(dje.submission_ts,''YYYY_MM_DD'')||'',''||tgts.target_name||'',''||tgts.target_type
from SYSMAN.MGMT_DM_SS_COLUMNS dc
left join SYSMAN.MGMT_DM_JOB_EXECUTIONS dje on dc.ss_guid = dje.ss_guid
left join SYSMAN.MGMT_JOB_HISTORY jh on dje.execution_id = jh.execution_id
left join SYSMAN.MGMT_JOB_TARGET jt on jh.job_id = jt.job_id
left join SYSMAN.MGMT_TARGETS tgts on jt.target_guid = tgts.target_guid
where tgts.target_type like ''%database%'' or tgts.target_type like ''%pdb%''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Data Masking Usage 13c (ref: SYSMAN.MGMT_DM_SS_COLUMNS, SYSMAN.MGMT_DM_JOB_EXECUTIONS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'DATA_MASKING_USAGE13c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_DM_SS_COLUMNS'',''MGMT_DM_JOB_EXECUTIONS'')';
ntables := '2';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct dc.object_schema||'',''||dc.object_name||'',''||to_char(dje.submission_ts,''YYYY_MM_DD'')||'',''||tgts.target_name||'',''||tgts.target_type
from SYSMAN.MGMT_DM_SS_COLUMNS dc
left join SYSMAN.MGMT_DM_JOB_EXECUTIONS dje on dc.ss_guid = dje.ss_guid
left join SYSMAN.MGMT_JOB_HISTORY jh on dje.execution_id = jh.execution_id
left join SYSMAN.MGMT_JOB_TARGET jt on jh.job_id = jt.job_id
left join SYSMAN.MGMT_TARGETS tgts on jt.target_guid = tgts.target_guid
where tgts.target_type like ''%database%'' or tgts.target_type like ''%pdb%''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === OEM Packs Real Application Testing Usage 12c (ref: SYSMAN.MGMT_JOB, SYSMAN.MGMT_JOB_TARGET, SYSMAN.MGMT_WORKLOAD_CAPTURES, SYSMAN.MGMT_WORKLOAD_REPLAYS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'REAL_APPLICATION_TESTING12c';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from ALL_OBJECTS where owner = ''SYSMAN'' and upper(substr(object_name,1,100)) in (''MGMT_JOB'',''MGMT_JOB_TARGET'',''MGMT_WORKLOAD_CAPTURES'',''MGMT_WORKLOAD_REPLAYS'')';
ntables := '4';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct mj.job_type||'',''||mj.job_name||'',''||mj.job_description||'',''||tgts.target_name||'',''||tgts.target_type||'',''||wc.capture_name||'',''||
to_char(wc.capture_start,''YYYY_MM_DD'')||'',''||wr.replay_name||'',''||to_char(wr.replay_start,''YYYY_MM_DD'')
from SYSMAN.MGMT_JOB mj
left join SYSMAN.MGMT_JOB_TARGET jt on mj.job_id = jt.job_id
left join SYSMAN.MGMT_TARGETS tgts on jt.target_guid = tgts.target_guid
left join SYSMAN.MGMT_WORKLOAD_CAPTURES wc on tgts.target_guid = wc.target_guid
left join SYSMAN.MGMT_WORKLOAD_REPLAYS wr on tgts.target_guid = wr.target_guid
where (lower(mj.job_name) like ''%capture%''
or lower(mj.job_name) like ''%replay%'')
and (tgts.target_type like ''%database%'' or tgts.target_type like ''%pdb%'')
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Tuning Pack (SQL Profiles) (ref: DBA_SQL_PROFILES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'SQL_PROFILES';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct ''"''||NAME||''","''||substr(DESCRIPTION,1,100)||''",''||TYPE||'',''||STATUS||'',''||to_char(LAST_MODIFIED,''YYYY_MM_DD'')||'',''||to_char(CREATED,''YYYY_MM_DD'')
from DBA_SQL_PROFILES
where lower(STATUS) = ''enabled''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Tuning Pack (Advisor Tasks) (ref: DBA_ADVISOR_TASKS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'SQL_ADVISOR';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct ''"''||OWNER||''","''||TASK_NAME||''","''||substr(DESCRIPTION,1,100)||''","''||STATUS||''",''||to_char(LAST_MODIFIED,''YYYY_MM_DD'')||'',''||to_char(CREATED,''YYYY_MM_DD'')||''","''||ADVISOR_NAME
from DBA_ADVISOR_TASKS
where lower(ADVISOR_NAME) like (''sql%advisor'')
and upper(OWNER) not like ''%SYS%''
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Tuning Pack (SQL Set) (ref: DBA_SQLSET)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'DBA_SQLSET';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct ''"''||NAME||''","''||OWNER||''",''||to_char(LAST_MODIFIED,''YYYY_MM_DD'')||'',''||to_char(CREATED,''YYYY_MM_DD'')||''","''||substr(DESCRIPTION,1,100)||''","''||STATEMENT_COUNT
from DBA_SQLSET
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Real Application Testing (Workload Captures) (ref: DBA_WORKLOAD_CAPTURES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'OEM';
feature_detail varchar2(100) := 'DBA_WORKLOAD_CAPTURES';

BEGIN
c := dbms_sql.open_cursor;
q := 'select distinct ''"''||DBNAME||''","''||STATUS||''",''||to_char(START_TIME,''YYYY_MM_DD'')
from DBA_WORKLOAD_CAPTURES
order by 1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === E-Business Suite Release Version (ref: DBA_OBJECTS, DBA_USERS, APPS.FND_PRODUCT_GROUPS)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'EBS';
feature_detail varchar2(100) := 'EBS_RELEASE';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from DBA_OBJECTS a
join DBA_USERS b on a.OWNER = B.USERNAME
where a.OBJECT_NAME in (''FND_PRODUCT_GROUPS'')
and a.OBJECT_TYPE in (''TABLE'', ''SYNONYM'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select distinct release_name||'',''||applications_system_name
from APPS.FND_PRODUCT_GROUPS';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === E-Business Suite Nodes (ref: DBA_TABLES, APPS.FND_NODES)
DECLARE
c integer;
q varchar2(5000);
res integer;
v_c1 varchar2(5000);
feature varchar2(100) := 'EBS';
feature_detail varchar2(100) := 'EBS_NODES';
ntables varchar2(1);

BEGIN
c := dbms_sql.open_cursor;
q := 'select count(distinct object_name)
from DBA_OBJECTS a
join DBA_USERS b on a.OWNER = B.USERNAME
where a.OBJECT_NAME in (''FND_NODES'')
and a.OBJECT_TYPE in (''TABLE'', ''SYNONYM'')';
ntables := '1';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
if v_c1 >= ntables then
q := 'select node_name||'',''||webhost||'',''||decode(support_db,''Y'',''DB'','''')||'',''||decode(support_cp,''Y'',''CP'','''')
||'',''||decode(support_admin,''Y'',''ADMIN'','''')||'',''||decode(support_web,''Y'',''WEB'','''')
||'',''||decode(support_forms,''Y'',''FORMS'','''')||'',''||host||'',''||domain
from APPS.FND_NODES where node_name!=''AUTHENTICATION''';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
end if;
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT === Oracle Warehouse Builder Repositories (ref: DBA_TABLES, CMPINSTALLATION_V)
DECLARE
o integer;
c integer;
q varchar2(5000);
res integer;
sch varchar2(5000);
v_c1 varchar2(5000);
feature varchar2(100) := 'OWB';
feature_detail varchar2(100) := 'OWB_REPOSITORIES';

BEGIN
o := dbms_sql.open_cursor;
c := dbms_sql.open_cursor;
q := 'select distinct owner
from ALL_OBJECTS where upper(substr(object_name,1,100)) in (''CMPSYSCLASSES'')';
dbms_sql.parse(o, q, dbms_sql.native);
dbms_sql.define_column(o, 1, sch, 5000);
res:=dbms_sql.execute(o);
loop
if dbms_sql.fetch_rows(o) > 0 then
dbms_sql.column_value (o, 1, sch);
if sch = '' or sch is null then
exit;
else
dbms_output.put_line ('Detected owner: '||sch);
q := 'select installedversion from '|| sch || '.cmpinstallation_v where lower(name) = ''oracle warehouse builder''';
dbms_sql.parse(c, q, dbms_sql.native);
dbms_sql.define_column(c, 1, v_c1, 5000);
res:=dbms_sql.execute(c);
loop
if dbms_sql.fetch_rows(c) > 0 then
dbms_sql.column_value (c, 1, v_c1);
dbms_output.put_line ('&&MARKER.,'||feature||','||feature_detail||','||v_c1||',');
else
exit;
end if;
end loop;
end if;
else
exit;
end if;
end loop;
dbms_sql.close_cursor (o);
dbms_sql.close_cursor (c);

EXCEPTION
WHEN OTHERS THEN
dbms_output.put_line ('ERROR WHEN COLLECTING '||feature||' : ' || SQLERRM);
dbms_sql.close_cursor (c);

END;
/

PROMPT
PROMPT PALDB_SCRIPT_START_TIMESTAMP:&&DATE_START_FORMATTED.
select 'PALDB_SCRIPT_END_TIMESTAMP:'||to_char(SYSDATE, 'YYYYMMDDHH24MISS') from dual;
PROMPT
PROMPT EOF
SPOOL OFF

