spool RESET_SEQUENCES_on_B
set echo on feedback on timing on


-- Setting Tag 2 to disable DDL replication on C2
exec dbms_streams.set_tag (hextoraw(2));
 alter sequence pspadm.seq_gems_upload_batch_id RESTART start with 38001;
alter sequence pspadm.psid_nh RESTART start WITH 333002482;
alter sequence pspadm.psid_wa RESTART start WITH 652013934;
alter sequence pspadm.psid_sd RESTART start WITH 446000914;
alter sequence pspadm.psid_pa RESTART start WITH 342010746;
alter sequence pspadm.psid_la RESTART start WITH 422005507;
alter sequence pspadm.psid_mo RESTART start WITH 429006074;
alter sequence pspadm.psid_me RESTART start WITH 323001627;
alter sequence pspadm.seq_txn_token_nbr RESTART start WITH 7216421;
alter sequence pspadm.seq_transaction_number RESTART start WITH 417928553;
alter sequence pspadm.psid_ne RESTART start WITH 431001985;
alter sequence pspadm.psid_nc RESTART start WITH 337012614;
alter sequence pspadm.psid_nv RESTART start WITH 632006665;
alter sequence pspadm.psid_wy RESTART start WITH 555001154;
alter sequence pspadm.psid_tx RESTART start WITH 448038276;
alter sequence pspadm.psid_az RESTART start WITH 504012035;
alter sequence pspadm.seq_subscription_number RESTART start WITH 10009408;
alter sequence pspadm.psid_wi RESTART start WITH 454005835;
alter sequence pspadm.psid_tn RESTART start WITH 347000668;
alter sequence pspadm.psid_ms RESTART start WITH 428002333;
alter sequence pspadm.psid_ga RESTART start WITH 313013614;
alter sequence pspadm.seq_trace_number RESTART start WITH 2128686561809;
alter sequence pspadm.psid_mt RESTART start WITH 530002075;
alter sequence pspadm.psid_nm RESTART start WITH 535003027;
alter sequence pspadm.psid_in RESTART start WITH 318003517;
alter sequence pspadm.psid_ak RESTART start WITH 702002046;
alter sequence pspadm.psid_ct RESTART start WITH 309005570;
alter sequence pspadm.psid_ia RESTART start WITH 419002574;
alter sequence pspadm.psid_md RESTART start WITH 324010077;
alter sequence pspadm.seq_eftps_file_sequence RESTART start WITH 176383;
alter sequence pspadm.psid_dc RESTART start WITH 311001625;
alter sequence pspadm.seq_usage_billing_token RESTART start WITH 128344274;
alter sequence pspadm.psid_de RESTART start WITH 310001528;
alter sequence pspadm.seq_atf_batch_id_nbr RESTART start WITH 10033;
alter sequence pspadm.psid_ma RESTART start WITH 325008176;
alter sequence pspadm.psid_wv RESTART start WITH 353001550;
alter sequence pspadm.psid_or RESTART start WITH 641008493;
alter sequence pspadm.psid_ny RESTART start WITH 336017858;
alter sequence pspadm.psid_mi RESTART start WITH 326009942;
alter sequence pspadm.seq_ach_file_ctr RESTART start WITH 27;
alter sequence pspadm.psid_co RESTART start WITH 508013541;
alter sequence pspadm.psid_hi RESTART start WITH 715002685;
alter sequence pspadm.seq_ee_pitem_calc_token RESTART start WITH 1280142;
alter sequence pspadm.psid_vt RESTART start WITH 350001386;
alter sequence pspadm.psid_fl RESTART start WITH 312032560;
alter sequence pspadm.psid_ky RESTART start WITH 321003104;
alter sequence pspadm.psid_ut RESTART start WITH 549006040;
alter sequence pspadm.psid_id RESTART start WITH 516003585;
alter sequence pspadm.psid_nj RESTART start WITH 334009270;
alter sequence pspadm.psid_nd RESTART start WITH 438000803;
alter sequence pspadm.seq_eftps_payment_sequence RESTART start WITH 18952813;
alter sequence pspadm.psid_default RESTART start WITH 999120648;
alter sequence pspadm.psid_ca RESTART start WITH 606121776;
alter sequence pspadm.psid_ok RESTART start WITH 440003951;
alter sequence pspadm.seq_ee_calculation_token RESTART start WITH 20576979;
alter sequence pspadm.seq_eftps_segment_sequence RESTART start WITH 376749;
alter sequence pspadm.psid_mn RESTART start WITH 427007153;
alter sequence pspadm.psid_ar RESTART start WITH 405002400;
alter sequence pspadm.seq_qbdt_source_company_id RESTART start WITH 109045747;
alter sequence pspadm.seq_401k_signup_batch_id RESTART start WITH 555;
alter sequence pspadm.psid_il RESTART start WITH 417015226;
alter sequence pspadm.seq_trace_nbr RESTART start WITH 16520000000002;
alter sequence pspadm.psid_ri RESTART start WITH 344000984;
alter sequence pspadm.psid_sc RESTART start WITH 345005432;
alter sequence pspadm.seq_401k_upload_batch_id RESTART start WITH 1318;
alter sequence pspadm.seq_asst_usage_billing_token RESTART start WITH 129970;
alter sequence pspadm.psid_va RESTART start WITH 351014100;
alter sequence pspadm.psid_al RESTART start WITH 401004972;
alter sequence pspadm.psid_ks RESTART start WITH 420003205;
alter sequence pspadm.psid_oh RESTART start WITH 339008658;

spool off;

