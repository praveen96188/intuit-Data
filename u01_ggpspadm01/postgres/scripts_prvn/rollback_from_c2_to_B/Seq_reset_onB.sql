spool RESET_SEQUENCES_on_B
set echo on feedback on timing on
-- Setting Tag 2 to disable DDL replication on C2
exec dbms_streams.set_tag (hextoraw(2));

 alter sequence pspadm.psid_oh RESTART START WITH 339008673;
 alter sequence pspadm.psid_az RESTART START WITH 504012055;
 alter sequence pspadm.seq_eftps_file_sequence RESTART START WITH 189526;
 alter sequence pspadm.seq_ee_calculation_token RESTART START WITH 21340427;
 alter sequence pspadm.seq_asst_usage_billing_token RESTART START WITH 190212;
 alter sequence pspadm.psid_ok RESTART START WITH 440003961;
 alter sequence pspadm.seq_trace_number RESTART START WITH 2128863982502;
 alter sequence pspadm.psid_nj RESTART START WITH 334009283;
 alter sequence pspadm.psid_nh RESTART START WITH 333002486;
 alter sequence pspadm.psid_ca RESTART START WITH 606121850;
 alter sequence pspadm.psid_va RESTART START WITH 351014119;
 alter sequence pspadm.psid_nc RESTART START WITH 337012628;
 alter sequence pspadm.seq_transaction_number RESTART START WITH 484280111;
 alter sequence pspadm.psid_mo RESTART START WITH 429006084;
 alter sequence pspadm.psid_ar RESTART START WITH 405002408;
 alter sequence pspadm.psid_ut RESTART START WITH 549006044;
 alter sequence pspadm.psid_sc RESTART START WITH 345005441;
 alter sequence pspadm.psid_md RESTART START WITH 324010087;
 alter sequence pspadm.seq_eftps_payment_sequence RESTART START WITH 19650783;
 alter sequence pspadm.psid_nv RESTART START WITH 632006671;
 alter sequence pspadm.psid_mn RESTART START WITH 427007157;
 alter sequence pspadm.psid_il RESTART START WITH 417015237;
 alter sequence pspadm.psid_tx RESTART START WITH 448038332;
 alter sequence pspadm.seq_atf_batch_id_nbr RESTART START WITH 10001;
 alter sequence pspadm.psid_ia RESTART START WITH 419002578;
 alter sequence pspadm.psid_co RESTART START WITH 508013563;
 alter sequence pspadm.psid_pa RESTART START WITH 342010763;
 alter sequence pspadm.psid_wv RESTART START WITH 353001552;
 alter sequence pspadm.seq_ach_file_ctr RESTART START WITH 23;
 alter sequence pspadm.seq_subscription_number RESTART START WITH 9855841;
 alter sequence pspadm.psid_vt RESTART START WITH 350001387;
 alter sequence pspadm.psid_in RESTART START WITH 318003517;
 alter sequence pspadm.psid_ma RESTART START WITH 325008178;
 alter sequence pspadm.psid_id RESTART START WITH 516003593;
 alter sequence pspadm.psid_fl RESTART START WITH 312032607;
 alter sequence pspadm.psid_ky RESTART START WITH 321003106;
 alter sequence pspadm.psid_ak RESTART START WITH 702002046;
 alter sequence pspadm.psid_or RESTART START WITH 641008501;
 alter sequence pspadm.psid_la RESTART START WITH 422005522;
 alter sequence pspadm.psid_ms RESTART START WITH 428002334;
 alter sequence pspadm.psid_ri RESTART START WITH 344000985;
 alter sequence pspadm.psid_hi RESTART START WITH 715002686;
 alter sequence pspadm.seq_txn_token_nbr RESTART START WITH 7546455;
 alter sequence pspadm.seq_qbdt_source_company_id RESTART START WITH 108954322;
 alter sequence pspadm.psid_al RESTART START WITH 401004976;
 alter sequence pspadm.seq_gems_upload_batch_id RESTART START WITH 39847;
 alter sequence pspadm.psid_wi RESTART START WITH 454005839;
 alter sequence pspadm.psid_default RESTART START WITH 999004505;
 alter sequence pspadm.psid_tn RESTART START WITH 347000677;
 alter sequence pspadm.psid_ct RESTART START WITH 309005574;
 alter sequence pspadm.psid_de RESTART START WITH 310001531;
 alter sequence pspadm.seq_ee_pitem_calc_token RESTART START WITH 1280142;
 alter sequence pspadm.psid_mi RESTART START WITH 326009956;
 alter sequence pspadm.psid_ny RESTART START WITH 336017875;
 alter sequence pspadm.psid_wy RESTART START WITH 555001156;
 alter sequence pspadm.psid_nd RESTART START WITH 438000803;
 alter sequence pspadm.psid_me RESTART START WITH 323001629;
 alter sequence pspadm.psid_wa RESTART START WITH 652013956;
 alter sequence pspadm.seq_usage_billing_token RESTART START WITH 138070633;
 alter sequence pspadm.psid_sd RESTART START WITH 446000920;
 alter sequence pspadm.psid_dc RESTART START WITH 311001627;
 alter sequence pspadm.seq_401k_signup_batch_id RESTART START WITH 555;
 alter sequence pspadm.psid_nm RESTART START WITH 535003033;
 alter sequence pspadm.seq_eftps_segment_sequence RESTART START WITH 393812;
 alter sequence pspadm.psid_ne RESTART START WITH 431001991;
 alter sequence pspadm.psid_mt RESTART START WITH 530002080;
 alter sequence pspadm.seq_401k_upload_batch_id RESTART START WITH 1318;
 alter sequence pspadm.psid_ga RESTART START WITH 313013628;
 alter sequence pspadm.psid_ks RESTART START WITH 420003208;
 alter sequence pspadm.seq_trace_nbr RESTART START WITH 16368000000002;


spool off
