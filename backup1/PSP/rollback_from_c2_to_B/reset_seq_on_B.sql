spool RESET_SEQUENCES_on_B
set echo on feedback on timing on
-- Setting Tag 2 to disable DDL replication on C2
exec dbms_streams.set_tag (hextoraw(2));

 alter sequence pspadm.psid_co RESTART START WITH 508013543;
 alter sequence pspadm.psid_ms RESTART START WITH 428002335;
 alter sequence pspadm.psid_pa RESTART START WITH 342010748;
 alter sequence pspadm.psid_nd RESTART START WITH 438000805;
 alter sequence pspadm.psid_hi RESTART START WITH 715002687;
 alter sequence pspadm.seq_txn_token_nbr RESTART START WITH 7216495;
 alter sequence pspadm.psid_nv RESTART START WITH 632006667;
 alter sequence pspadm.psid_vt RESTART START WITH 350001388;
 alter sequence pspadm.psid_nm RESTART START WITH 535003029;
 alter sequence pspadm.psid_de RESTART START WITH 310001530;
 alter sequence pspadm.psid_mt RESTART START WITH 530002077;
 alter sequence pspadm.psid_in RESTART START WITH 318003519;
 alter sequence pspadm.seq_eftps_file_sequence RESTART START WITH 176451;
 alter sequence pspadm.psid_md RESTART START WITH 324010079;
 alter sequence pspadm.psid_ks RESTART START WITH 420003207;
 alter sequence pspadm.psid_ca RESTART START WITH 606121778;
 alter sequence pspadm.psid_ak RESTART START WITH 702002048;
 alter sequence pspadm.psid_id RESTART START WITH 516003587;
 alter sequence pspadm.psid_sc RESTART START WITH 345005434;
 alter sequence pspadm.seq_ee_calculation_token RESTART START WITH 20577159;
 alter sequence pspadm.psid_wv RESTART START WITH 353001552;
 alter sequence pspadm.seq_asst_usage_billing_token RESTART START WITH 129972;
 alter sequence pspadm.psid_ct RESTART START WITH 309005572;
 alter sequence pspadm.psid_tx RESTART START WITH 448038278;
 alter sequence pspadm.psid_ar RESTART START WITH 405002402;
 alter sequence pspadm.psid_ma RESTART START WITH 325008178;
 alter sequence pspadm.psid_az RESTART START WITH 504012037;
 alter sequence pspadm.psid_ky RESTART START WITH 321003106;
 alter sequence pspadm.psid_ok RESTART START WITH 440003953;
 alter sequence pspadm.psid_nc RESTART START WITH 337012616;
 alter sequence pspadm.seq_subscription_number RESTART START WITH 10009914;
 alter sequence pspadm.seq_401k_signup_batch_id RESTART START WITH 557;
 alter sequence pspadm.seq_ee_pitem_calc_token RESTART START WITH 1280144;
 alter sequence pspadm.seq_atf_batch_id_nbr RESTART START WITH 10035;
 alter sequence pspadm.psid_ri RESTART START WITH 344000986;
 alter sequence pspadm.psid_ga RESTART START WITH 313013616;
 alter sequence pspadm.psid_tn RESTART START WITH 347000670;
 alter sequence pspadm.seq_trace_number RESTART START WITH 2128686582468;
 alter sequence pspadm.psid_wa RESTART START WITH 652013936;
 alter sequence pspadm.psid_oh RESTART START WITH 339008660;
 alter sequence pspadm.psid_ia RESTART START WITH 419002576;
 alter sequence pspadm.seq_ach_file_ctr RESTART START WITH 27;
 alter sequence pspadm.psid_al RESTART START WITH 401004974;
 alter sequence pspadm.psid_wi RESTART START WITH 454005837;
 alter sequence pspadm.psid_mi RESTART START WITH 326009944;
 alter sequence pspadm.seq_401k_upload_batch_id RESTART START WITH 1320;
 alter sequence pspadm.psid_wy RESTART START WITH 555001156;
 alter sequence pspadm.psid_ny RESTART START WITH 336017860;
 alter sequence pspadm.seq_qbdt_source_company_id RESTART START WITH 109046253;
 alter sequence pspadm.psid_me RESTART START WITH 323001629;
 alter sequence pspadm.psid_fl RESTART START WITH 312032562;
 alter sequence pspadm.seq_eftps_payment_sequence RESTART START WITH 18952815;
 alter sequence pspadm.psid_nh RESTART START WITH 333002484;
 alter sequence pspadm.seq_usage_billing_token RESTART START WITH 128344409;
 alter sequence pspadm.psid_dc RESTART START WITH 311001627;
 alter sequence pspadm.seq_eftps_segment_sequence RESTART START WITH 376818;
 alter sequence pspadm.psid_or RESTART START WITH 641008495;
 alter sequence pspadm.psid_il RESTART START WITH 417015228;
 alter sequence pspadm.seq_gems_upload_batch_id RESTART START WITH 38003;
 alter sequence pspadm.psid_la RESTART START WITH 422005509;
 alter sequence pspadm.psid_nj RESTART START WITH 334009272;
 alter sequence pspadm.seq_trace_nbr RESTART START WITH 16520000000004;
 alter sequence pspadm.psid_ne RESTART START WITH 431001987;
 alter sequence pspadm.psid_mn RESTART START WITH 427007155;
 alter sequence pspadm.psid_ut RESTART START WITH 549006042;
 alter sequence pspadm.psid_sd RESTART START WITH 446000916;
 alter sequence pspadm.psid_mo RESTART START WITH 429006076;
 alter sequence pspadm.seq_transaction_number RESTART START WITH 418247249;
 alter sequence pspadm.psid_va RESTART START WITH 351014102;
 alter sequence pspadm.psid_default RESTART START WITH 999120847;


spool off
