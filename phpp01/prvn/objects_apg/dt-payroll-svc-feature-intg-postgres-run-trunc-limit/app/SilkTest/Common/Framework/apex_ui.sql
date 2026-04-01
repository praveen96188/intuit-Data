set define off
set verify off
set serveroutput on size 1000000
set feedback off
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
begin wwv_flow.g_import_in_progress := true; end; 
/
 
 
--application/set_environment
prompt  APPLICATION 107 - SILKTEST_UI
--
-- Application Export:
--   Application:     107
--   Name:            SILKTEST_UI
--   Date and Time:   08:32 Tuesday June 30, 2009
--   Exported By:     MAIN
--   Flashback:       0
--   Export Type:     Application Export
--   Version: 3.0.1.00.08
 
-- Import:
--   Using application builder
--   or
--   Using SQL*Plus as the Oracle user FLOWS_030000 or as the owner (parsing schema) of the application.
 
-- Application Statistics:
--   Pages:                15
--     Items:              35
--     Computations:        0
--     Validations:         0
--     Processes:          52
--     Regions:            27
--     Buttons:            43
--   Shared Components
--     Breadcrumbs:         4
--        Entries           7
--     Items:               1
--     Computations:        0
--     Processes:           0
--     Parent Tabs:         1
--     Tab Sets:            1
--        Tabs:            10
--     NavBars:             1
--     Lists:               0
--     Shortcuts:           2
--     Themes:              1
--     Templates:
--        Page:             9
--        List:            14
--        Report:           7
--        Label:            5
--        Region:          21
--     Messages:            0
--     Build Options:       0
 
 
--       AAAA       PPPPP   EEEEEE  XX      XX
--      AA  AA      PP  PP  EE       XX    XX
--     AA    AA     PP  PP  EE        XX  XX
--    AAAAAAAAAA    PPPPP   EEEE       XXXX
--   AA        AA   PP      EE        XX  XX
--  AA          AA  PP      EE       XX    XX
--  AA          AA  PP      EEEEEE  XX      XX
prompt  Set Credentials...
 
begin
 
  -- Assumes you are running the script connected to SQL*Plus as the Oracle user FLOWS_030000 or as the owner (parsing schema) of the application.
  wwv_flow_api.set_security_group_id(p_security_group_id=>1465209823845563);
 
end;
/

begin wwv_flow.g_import_in_progress := true; end;
/
begin 

select value into wwv_flow_api.g_nls_numeric_chars from nls_session_parameters where parameter='NLS_NUMERIC_CHARACTERS';

end;

/
begin execute immediate 'alter session set nls_numeric_characters=''.,''';

end;

/
begin wwv_flow.g_browser_language := 'en-us'; end;
/
prompt  Check Compatibility...
 
begin
 
-- This date identifies the minimum version required to import this file.
wwv_flow_api.set_version(p_version_yyyy_mm_dd=>'2007.01.08');
 
end;
/

prompt  Set Application ID...
 
begin
 
   -- SET APPLICATION ID
   wwv_flow.g_flow_id := 107;
   wwv_flow_api.g_id_offset := 0;
null;
 
end;
/

--application/delete_application
 
begin
 
   -- Remove Application
wwv_flow_api.remove_flow(107);
 
end;
/

 
begin
 
wwv_flow_audit.remove_audit_trail(107);
null;
 
end;
/

--application/create_application
 
begin
 
wwv_flow_api.create_flow(
  p_id    => 107,
  p_display_id=> 107,
  p_owner => 'MAIN',
  p_name  => 'SILKTEST_UI',
  p_alias => 'F102112',
  p_page_view_logging => 'YES',
  p_default_page_template=> 9687725257914124 + wwv_flow_api.g_id_offset,
  p_printer_friendly_template=> 9688149371914126 + wwv_flow_api.g_id_offset,
  p_default_region_template=> 9689839472914128 + wwv_flow_api.g_id_offset,
  p_error_template    => 9687725257914124 + wwv_flow_api.g_id_offset,
  p_checksum_salt_last_reset => '20090612051525',
  p_home_link         => 'f?p=&APP_ID.:1:&SESSION.',
  p_box_width         => '98%',
  p_flow_language     => 'en-us',
  p_flow_language_derived_from=> 'FLOW_PRIMARY_LANGUAGE',
  p_flow_image_prefix => '/i/',
  p_documentation_banner=> '',
  p_authentication    => 'CUSTOM2',
  p_login_url         => '',
  p_logout_url        => 'wwv_flow_custom_auth_std.logout?p_this_flow=&APP_ID.&amp;p_next_flow_page_sess=&APP_ID.:1',
  p_application_tab_set=> 1,
  p_public_url_prefix => '',
  p_public_user       => '',
  p_dbauth_url_prefix => '',
  p_proxy_server      => '',
  p_cust_authentication_process=> '.'||to_char(9694545616914145 + wwv_flow_api.g_id_offset)||'.',
  p_cust_authentication_page=> '',
  p_custom_auth_login_url=> '',
  p_flow_version      => 'release 1.0',
  p_flow_status       => 'AVAILABLE_W_EDIT_LINK',
  p_flow_unavailable_text=> '',
  p_build_status      => 'RUN_AND_BUILD',
  p_exact_substitutions_only=> 'Y',
  p_vpd               => '',
  p_theme_id => 2,
  p_default_label_template => 9693128915914134 + wwv_flow_api.g_id_offset,
  p_default_report_template => 9692549154914132 + wwv_flow_api.g_id_offset,
  p_default_list_template => 9691831233914131 + wwv_flow_api.g_id_offset,
  p_default_menu_template => 9693645199914134 + wwv_flow_api.g_id_offset,
  p_default_button_template => 9688636459914126 + wwv_flow_api.g_id_offset,
  p_default_chart_template => 9689047344914126 + wwv_flow_api.g_id_offset,
  p_default_form_template => 9689133428914128 + wwv_flow_api.g_id_offset,
  p_default_wizard_template => 9688926422914126 + wwv_flow_api.g_id_offset,
  p_default_tabform_template => 9689839472914128 + wwv_flow_api.g_id_offset,
  p_default_reportr_template   =>9689839472914128 + wwv_flow_api.g_id_offset,
  p_default_menur_template => 9688830199914126 + wwv_flow_api.g_id_offset,
  p_default_listr_template => 9689839472914128 + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'MAIN',
  p_last_upd_yyyymmddhh24miss=> '20090612051525',
  p_required_roles=> wwv_flow_utilities.string_to_table2(''));
 
 
end;
/

prompt  ...authorization schemes
--
 
begin
 
null;
 
end;
/

--application/shared_components/navigation/navigation_bar
prompt  ...navigation bar entries
--
 
begin
 
wwv_flow_api.create_icon_bar_item(
  p_id             => 9694827741914146 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_icon_sequence  => 200,
  p_icon_image     => '',
  p_icon_subtext   => 'Logout',
  p_icon_target    => '&LOGOUT_URL.',
  p_icon_image_alt => 'Logout',
  p_icon_height    => 32,
  p_icon_width     => 32,
  p_icon_height2   => 24,
  p_icon_width2    => 24,
  p_icon_bar_disp_cond      => '',
  p_icon_bar_disp_cond_type => 'CURRENT_LOOK_IS_1',
  p_begins_on_new_line=> '',
  p_cell_colspan      => 1,
  p_onclick=> '',
  p_icon_bar_comment=> '');
 
 
end;
/

prompt  ...application processes
--
prompt  ...application items
--
--application/shared_components/logic/application_items/fsp_after_login_url
 
begin
 
wwv_flow_api.create_flow_item(
  p_id=> 9700324182915399 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'FSP_AFTER_LOGIN_URL',
  p_data_type=> 'VARCHAR',
  p_is_persistent=> 'Y',
  p_protection_level=> '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_item_comment=> '');
 
null;
 
end;
/

prompt  ...application level computations
--
 
begin
 
null;
 
end;
/

prompt  ...Application Tabs
--
 
begin
 
--application/shared_components/navigation/tabs/standard/t_start_page
wwv_flow_api.create_tab (
  p_id=> 10626049404624198 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 10,
  p_tab_name=> 'T_START_PAGE',
  p_tab_text => 'Landing Page',
  p_tab_step => 1,
  p_tab_also_current_for_pages => '',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/t_test_case
wwv_flow_api.create_tab (
  p_id=> 8966316644390886 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 20,
  p_tab_name=> 'T_TEST_CASE',
  p_tab_text => 'Test Case',
  p_tab_step => 11,
  p_tab_also_current_for_pages => '11,12',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/t_test_group
wwv_flow_api.create_tab (
  p_id=> 9041406388754385 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 30,
  p_tab_name=> 'T_TEST_GROUP',
  p_tab_text => 'Test Group',
  p_tab_step => 13,
  p_tab_also_current_for_pages => '13,14',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/data_driven
wwv_flow_api.create_tab (
  p_id=> 9453913663297724 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 40,
  p_tab_name=> 'DATA_DRIVEN',
  p_tab_text => 'Data-Driven',
  p_tab_step => 15,
  p_tab_also_current_for_pages => '15,16,19',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/t_uiobect
wwv_flow_api.create_tab (
  p_id=> 10548024511218285 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 50,
  p_tab_name=> 'T_UIOBECT',
  p_tab_text => 'UI Object',
  p_tab_step => 3,
  p_tab_also_current_for_pages => '',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/t_testoperation
wwv_flow_api.create_tab (
  p_id=> 10549238363222334 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 60,
  p_tab_name=> 'T_TESTOPERATION',
  p_tab_text => 'Test Operation',
  p_tab_step => 6,
  p_tab_also_current_for_pages => '6',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/t_uisection
wwv_flow_api.create_tab (
  p_id=> 10550250484225785 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 70,
  p_tab_name=> 'T_UISECTION',
  p_tab_text => 'UI Section',
  p_tab_step => 8,
  p_tab_also_current_for_pages => '8',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/t_analysis
wwv_flow_api.create_tab (
  p_id=> 10612041837013864 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 80,
  p_tab_name=> 'T_ANALYSIS',
  p_tab_text => 'Analysis',
  p_tab_step => 2,
  p_tab_also_current_for_pages => '',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/t_soa_test
wwv_flow_api.create_tab (
  p_id=> 8828713375904377 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 90,
  p_tab_name=> 'T_SOA TEST',
  p_tab_text => 'SOA Test',
  p_tab_step => 4,
  p_tab_also_current_for_pages => '',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
--application/shared_components/navigation/tabs/standard/functional_area
wwv_flow_api.create_tab (
  p_id=> 9137202713511088 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_tab_sequence=> 110,
  p_tab_name=> 'FUNCTIONAL_AREA',
  p_tab_text => 'Functional Area',
  p_tab_step => 5,
  p_tab_also_current_for_pages => '',
  p_tab_parent_tabset=>'TEST_CASES',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment  => '');
 
 
end;
/

prompt  ...Application Parent Tabs
--
 
begin
 
--application/shared_components/navigation/tabs/parent/t_test_cases
wwv_flow_api.create_toplevel_tab (
  p_id=> 10152349029870032 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASES',
  p_tab_sequence=> 20,
  p_tab_name  => 'T_TEST_CASES',
  p_tab_text  => 'TEST_CASES',
  p_tab_target=> 'f?p=&APP_ID.:50:&SESSION.:',
  p_current_on_tabset=> 'TEST_CASE',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_tab_comment=> '');
 
 
end;
/

prompt  ...Shared Lists of values
--
--application/shared_components/user_interface/lov/dd_test_case_ids
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 10416849737645645 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'DD_TEST_CASE_IDS',
  p_lov_query=> 'select test_case_id,test_case_gseq'||chr(10)||
'from test_case'||chr(10)||
'where data_driven=''Y'''||chr(10)||
'');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/dd_test_case_list
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9535504328735485 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'DD_TEST_CASE_LIST',
  p_lov_query=> 'SELECT DD_TEST_CASE.DD_TEST_CASE_ID r,DD_TEST_CASE.DD_TEST_CASE_GSEQ d'||chr(10)||
'FROM DD_TEST_CASE');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/functional_area_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9132698591488816 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'FUNCTIONAL_AREA_LOV',
  p_lov_query=> 'SELECT FUNCTIONAL_AREA_ID d,FUNCTIONAL_AREA_GSEQ r'||chr(10)||
'FROM FUNCTIONAL_AREA'||chr(10)||
'ORDER BY FUNCTIONAL_AREA_ID'||chr(10)||
'');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/not_runnable_test_case_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9151317233790608 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'NOT_RUNNABLE_TEST_CASE_LOV',
  p_lov_query=> 'SELECT TEST_CASE_ID d,TEST_CASE_GSEQ r'||chr(10)||
'FROM TEST_CASE'||chr(10)||
'WHERE TEST_CASE.TEST_CASE_TYPE_GSEQ IN ('||chr(10)||
'   SELECT TEST_CASE_TYPE_GSEQ'||chr(10)||
'   FROM TEST_CASE_TYPE'||chr(10)||
'   WHERE TEST_CASE_TYPE_ID != ''EXECUTABLE'''||chr(10)||
')'||chr(10)||
'ORDER BY TEST_CASE_ID');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/operation_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9795037769788826 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'OPERATION_LOV',
  p_lov_query=> 'select test_operation_id,test_operation_gseq'||chr(10)||
'from test_operation'||chr(10)||
'order by 1');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/runnable_test_case_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9150696108784530 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'RUNNABLE_TEST_CASE_LOV',
  p_lov_query=> 'SELECT TEST_CASE_ID d,TEST_CASE_GSEQ r'||chr(10)||
'FROM TEST_CASE'||chr(10)||
'WHERE TEST_CASE.TEST_CASE_TYPE_GSEQ IN ('||chr(10)||
'   SELECT TEST_CASE_TYPE_GSEQ'||chr(10)||
'   FROM TEST_CASE_TYPE'||chr(10)||
'   WHERE TEST_CASE_TYPE_ID = ''RUNNABLE_TEST_CASE'''||chr(10)||
')'||chr(10)||
'ORDER BY TEST_CASE_ID');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/soa_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 10629654674442237 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'SOA_LOV',
  p_lov_query=> 'select soa_test_id,soa_test_gseq'||chr(10)||
'from soa_test'||chr(10)||
'order by soa_test_id');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/test_case_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9796833182806504 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'TEST_CASE_LOV',
  p_lov_query=> 'select test_case_id,test_case_gseq'||chr(10)||
'from test_case'||chr(10)||
'order by 1');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/test_case_type_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9166800629004958 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'TEST_CASE_TYPE_LOV',
  p_lov_query=> 'SELECT TEST_CASE_TYPE_ID d,TEST_CASE_TYPE_GSEQ r'||chr(10)||
'FROM TEST_CASE_TYPE'||chr(10)||
'ORDER BY TEST_CASE_TYPE_ID');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/test_group_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9064007162029335 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'TEST_GROUP_LOV',
  p_lov_query=> 'select test_group_id,test_group_gseq'||chr(10)||
'from   test_group'||chr(10)||
'order by test_group_id');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/test_type_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 8973112286422252 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'TEST_TYPE_LOV',
  p_lov_query=> 'select test_type_id,test_type_gseq'||chr(10)||
'from   test_type'||chr(10)||
'order by test_type_id'||chr(10)||
'');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/ui_object_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9795652314793079 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'UI_OBJECT_LOV',
  p_lov_query=> 'select ui_object_id,ui_object_gseq'||chr(10)||
'from ui_object'||chr(10)||
'order by 1');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/ui_section_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 9796246211800728 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'UI_SECTION_LOV',
  p_lov_query=> 'select ui_section_id,ui_section_gseq'||chr(10)||
'from ui_section'||chr(10)||
'order by 1');
 
null;
 
end;
/

--application/shared_components/user_interface/lov/yn_lov
 
begin
 
wwv_flow_api.create_list_of_values (
  p_id       => 10209627754158692 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_lov_name => 'YN_LOV',
  p_lov_query=> '.'||to_char(10209627754158692 + wwv_flow_api.g_id_offset)||'.');
 
null;
 
end;
/

 
begin
 
wwv_flow_api.create_static_lov_data (
  p_id=>10209949011158695 + wwv_flow_api.g_id_offset,
  p_lov_id=>10209627754158692 + wwv_flow_api.g_id_offset,
  p_lov_disp_sequence=>1,
  p_lov_disp_value=>'YES',
  p_lov_return_value=>'Y',
  p_lov_data_comment=> '');
 
null;
 
end;
/

 
begin
 
wwv_flow_api.create_static_lov_data (
  p_id=>10210135327158696 + wwv_flow_api.g_id_offset,
  p_lov_id=>10209627754158692 + wwv_flow_api.g_id_offset,
  p_lov_disp_sequence=>2,
  p_lov_disp_value=>'NO',
  p_lov_return_value=>'N',
  p_lov_data_comment=> '');
 
null;
 
end;
/

prompt  ...Application Trees
--
--application/pages/page_groups
prompt  ...page groups
--
 
begin
 
null;
 
end;
/

--application/comments
prompt  ...comments: requires application express 2.2 or higher
--
 
--application/pages/page_00001
prompt  ...PAGE 1: MENU_PAGE
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph := null;
wwv_flow_api.create_page(
  p_id     => 1,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'MENU_PAGE',
  p_alias  => 'MENU_PAGE',
  p_step_title=> 'MENU_PAGE',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => '',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'OBE',
  p_last_upd_yyyymmddhh24miss => '20090313171216',
  p_page_comment  => '');
 
end;
 
end;
/

 
begin
 
null;
 
end;
/

 
begin
 
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 1
--
 
begin
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00002
prompt  ...PAGE 2: ANALYSIS
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph := null;
wwv_flow_api.create_page(
  p_id     => 2,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'ANALYSIS',
  p_alias  => 'ANALYSIS',
  p_step_title=> 'ANALYSIS',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => '',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'OBE',
  p_last_upd_yyyymmddhh24miss => '20090316145606',
  p_page_comment  => '');
 
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 10612845375018170 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 2,
  p_plug_name=> 'Search',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select test_case_id,step_number,test_case_step.ui_object_gseq,test_case_step.value'||chr(10)||
'from test_case,test_case_step,test_operation,ui_object'||chr(10)||
'where test_case_step.test_case_gseq=test_case.test_case_gseq(+)'||chr(10)||
'and test_case_step.test_operation_gseq=test_operation.test_operation_gseq(+)'||chr(10)||
'and test_case_step.ui_object_gseq=ui_object.ui_object_gseq(+)'||chr(10)||
'and ui_object.ui_object_gseq=:P2_UI_OBJECT'||chr(10)||
'order by test_ca';

s:=s||'se_id,step_number'||chr(10)||
'';

wwv_flow_api.create_report_region (
  p_id=> 10615933310118760 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 2,
  p_name=> 'ALANYSIS_REPORT',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 20,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'QUERY_COLUMNS',
  p_query_num_rows=> '15',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> ' - ',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'no data found',
  p_query_num_rows_type=> 'NEXT_PREVIOUS_LINKS',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'BOTTOM_RIGHT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 10616236946118782 + wwv_flow_api.g_id_offset,
  p_region_id=> 10615933310118760 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_ID',
  p_column_display_sequence=> 1,
  p_column_heading=> 'Test Case Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'WITHOUT_MODIFICATION',
  p_pk_col_source=> s,
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 10616354956118785 + wwv_flow_api.g_id_offset,
  p_region_id=> 10615933310118760 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'STEP_NUMBER',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Step Number',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'WITHOUT_MODIFICATION',
  p_pk_col_source=> s,
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 10616447243118785 + wwv_flow_api.g_id_offset,
  p_region_id=> 10615933310118760 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'UI_OBJECT_GSEQ',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Ui Object Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT_FROM_LOV',
  p_named_lov=>9795652314793079 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 10616538192118785 + wwv_flow_api.g_id_offset,
  p_region_id=> 10615933310118760 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'VALUE',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Value',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'WITHOUT_MODIFICATION',
  p_pk_col_source=> s,
  p_column_comment=>'');
end;
/
 
begin
 
null;
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>10619837752452665 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 2,
  p_branch_action=> 'f?p=&FLOW_ID.:2:&SESSION.',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 99,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>10618243867434032 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 2,
  p_name=>'P2_UI_OBJECT',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 10612845375018170+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Ui Object',
  p_display_as=> 'COMBOBOX',
  p_named_lov=> 'UI_OBJECT_LOV',
  p_lov => 'select ui_object_id,ui_object_gseq'||chr(10)||
'from ui_object'||chr(10)||
'order by 1',
  p_lov_columns=> 1,
  p_lov_display_null=> 'YES',
  p_lov_translated=> 'N',
  p_lov_null_text=>'',
  p_lov_null_value=> '',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>10619642744452665 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 2,
  p_name=>'P2_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 10612845375018170+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 2
--
 
begin
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00003
prompt  ...PAGE 3: UI_OBJECT_UPDATE
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 3,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'UI_OBJECT_UPDATE',
  p_alias  => 'UI_OBJECT_UPDATE',
  p_step_title=> 'UI_OBJECT_UPDATE',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090514120321',
  p_page_comment  => '');
 
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>3,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 8789407920029439 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 3,
  p_plug_name=> 'Search',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"UI_OBJECT_GSEQ",'||chr(10)||
'"UI_OBJECT_ID",'||chr(10)||
'"DESCRIPTION",'||chr(10)||
'"UI_SECTION_GSEQ",'||chr(10)||
'"XPATH"'||chr(10)||
'from "#OWNER#"."UI_OBJECT"'||chr(10)||
'where upper(ui_object_id) like upper(:P3_UI_OBJECT_FILTER)'||chr(10)||
'and ((UI_SECTION_GSEQ=:P3_UI_SECTION_FILTER) '||chr(10)||
'      OR (UI_SECTION_GSEQ > DECODE(:P3_UI_SECTION_FILTER,-1,0,10000000)))'||chr(10)||
'AND upper(XPATH) LIKE upper(:P3_XPATH_FILTER)'||chr(10)||
'';

wwv_flow_api.create_report_region (
  p_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 3,
  p_name=> 'Tabular Form',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '10',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8798926035060939 + wwv_flow_api.g_id_offset,
  p_region_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8797013028060925 + wwv_flow_api.g_id_offset,
  p_region_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'UI_OBJECT_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Ui Object Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'UI_OBJECT',
  p_ref_column_name=> 'UI_OBJECT_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8797097094060927 + wwv_flow_api.g_id_offset,
  p_region_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'UI_OBJECT_ID',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Ui Object Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '60',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'UI_OBJECT',
  p_ref_column_name=> 'UI_OBJECT_ID',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8797211898060927 + wwv_flow_api.g_id_offset,
  p_region_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '40',
  p_column_height=> '2',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'UI_OBJECT',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8797310553060927 + wwv_flow_api.g_id_offset,
  p_region_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'UI_SECTION_GSEQ',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Ui Section',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>9796246211800728 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'UI_OBJECT',
  p_ref_column_name=> 'UI_SECTION_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8797412386060927 + wwv_flow_api.g_id_offset,
  p_region_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 6,
  p_form_element_id=> null,
  p_column_alias=> 'XPATH',
  p_column_display_sequence=> 6,
  p_column_heading=> 'Xpath',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '40',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'UI_OBJECT',
  p_ref_column_name=> 'XPATH',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 8797596314060930 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 3,
  p_button_sequence=> 10,
  p_button_plug_id => 8796718702060922+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:1:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8797802945060930 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 3,
  p_button_sequence=> 20,
  p_button_plug_id => 8796718702060922+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8797705635060930 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 3,
  p_button_sequence=> 30,
  p_button_plug_id => 8796718702060922+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8877922198233383 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 3,
  p_button_sequence=> 40,
  p_button_plug_id => 8796718702060922+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD_A_ROW',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add A Row',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>8799021075060939 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 3,
  p_branch_action=> 'f?p=&APP_ID.:3:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>3812348305084702 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 3,
  p_name=>'P3_UI_SECTION_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 30,
  p_item_plug_id => 8789407920029439+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Ui Section Filter',
  p_source_type=> 'STATIC',
  p_display_as=> 'COMBOBOX',
  p_named_lov=> 'UI_SECTION_LOV',
  p_lov => 'select ui_section_id,ui_section_gseq'||chr(10)||
'from ui_section'||chr(10)||
'order by 1',
  p_lov_columns=> 1,
  p_lov_display_null=> 'YES',
  p_lov_translated=> 'N',
  p_lov_null_text=>' ',
  p_lov_null_value=> '-1',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>4033129997040959 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 3,
  p_name=>'P3_XPATH_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 40,
  p_item_plug_id => 8789407920029439+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default => '%',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Xpath Filter',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>8791819479042296 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 3,
  p_name=>'P3_UI_OBJECT_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 8789407920029439+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default => '%',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Ui Object Id',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>8861615899051708 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 3,
  p_name=>'P3_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 8789407920029439+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:UI_OBJECT:UI_OBJECT_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8798499329060938 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 3,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8797705635060930 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:UI_OBJECT:UI_OBJECT_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8798719455060939 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 3,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 8869413353117253 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 3,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'ADD_ROW',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_when_button_id=>8877922198233383 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 3
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8801118833070424 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'UI_OBJECT_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8801225308070424 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'UI_OBJECT_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8801312214070424 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8801422454070424 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'UI_SECTION_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8801498496070424 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8796718702060922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 5,
  p_query_column_name=> 'XPATH',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00004
prompt  ...PAGE 4: SOA_TEST
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 4,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'SOA_TEST',
  p_alias  => 'SOA_TEST_PAGE',
  p_step_title=> 'SOA_TEST',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090512114404',
  p_page_comment  => '');
 
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>4,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 8829513550906072 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 4,
  p_plug_name=> 'SEARCH',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"SOA_TEST_GSEQ",'||chr(10)||
'"SOA_TEST_ID",'||chr(10)||
'"FILENAME",'||chr(10)||
'"DESCRIPTION"'||chr(10)||
'from "#OWNER#"."SOA_TEST"'||chr(10)||
'where upper(SOA_TEST_ID) like upper(:P4_SOA_TEST_FILTER)'||chr(10)||
''||chr(10)||
'';

wwv_flow_api.create_report_region (
  p_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 4,
  p_name=> 'Tabular Form',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8833001511911456 + wwv_flow_api.g_id_offset,
  p_region_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8831307046911450 + wwv_flow_api.g_id_offset,
  p_region_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'SOA_TEST_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Soa Test Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'SOA_TEST',
  p_ref_column_name=> 'SOA_TEST_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8831419342911450 + wwv_flow_api.g_id_offset,
  p_region_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'SOA_TEST_ID',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Soa Test Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '50',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'SOA_TEST',
  p_ref_column_name=> 'SOA_TEST_ID',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8831513085911450 + wwv_flow_api.g_id_offset,
  p_region_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'FILENAME',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Filename',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '50',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'SOA_TEST',
  p_ref_column_name=> 'FILENAME',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8831597213911450 + wwv_flow_api.g_id_offset,
  p_region_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '40',
  p_column_height=> '2',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'SOA_TEST',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 8831816948911453 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 4,
  p_button_sequence=> 30,
  p_button_plug_id => 8830999137911447+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'REGION_TEMPLATE_CHANGE',
  p_button_alignment=> 'RIGHT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8831697494911453 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 4,
  p_button_sequence=> 10,
  p_button_plug_id => 8830999137911447+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'REGION_TEMPLATE_CLOSE',
  p_button_alignment=> 'RIGHT',
  p_button_redirect_url=> 'f?p=&APP_ID.:1:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8832021629911453 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 4,
  p_button_sequence=> 40,
  p_button_plug_id => 8830999137911447+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row',
  p_button_position=> 'REGION_TEMPLATE_CREATE2',
  p_button_alignment=> 'RIGHT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8831908490911453 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 4,
  p_button_sequence=> 20,
  p_button_plug_id => 8830999137911447+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'REGION_TEMPLATE_DELETE',
  p_button_alignment=> 'RIGHT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>8849323634192469 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 4,
  p_branch_action=> 'f?p=&FLOW_ID.:4:&SESSION.',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 99,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>8836010069103872 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 4,
  p_name=>'P4_SOA_TEST_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 8829513550906072+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Soa Test Filter',
  p_source=>'%',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>8854903377243866 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 4,
  p_name=>'P4_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 8829513550906072+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:SOA_TEST:SOA_TEST_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8832625343911455 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 4,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8831816948911453 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:SOA_TEST:SOA_TEST_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8832821505911455 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 4,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:SOA_TEST:SOA_TEST_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8833097473911456 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 4,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8832021629911453 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 8833306262911456 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 4,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>8832021629911453 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 4
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8837496826118892 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'SOA_TEST_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8837618304118892 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'SOA_TEST_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8837725364118892 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'FILENAME',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8837800458118892 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8830999137911447 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00005
prompt  ...PAGE 5: Update FUNCTIONAL_AREA
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h:=h||'No help is available for this page.';

ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 5,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'Update FUNCTIONAL_AREA',
  p_step_title=> 'Update FUNCTIONAL_AREA',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => ' ',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090512132454',
  p_page_comment  => '');
 
wwv_flow_api.set_page_help_text(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>5,p_text=>h);
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>5,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"FUNCTIONAL_AREA_GSEQ",'||chr(10)||
'"FUNCTIONAL_AREA_ID",'||chr(10)||
'"DESCRIPTION"'||chr(10)||
'from "#OWNER#"."FUNCTIONAL_AREA"'||chr(10)||
'';

wwv_flow_api.create_report_region (
  p_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 5,
  p_name=> 'Tabular Form',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9139324952511113 + wwv_flow_api.g_id_offset,
  p_region_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9137603443511097 + wwv_flow_api.g_id_offset,
  p_region_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'FUNCTIONAL_AREA_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Functional Area Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'FUNCTIONAL_AREA',
  p_ref_column_name=> 'FUNCTIONAL_AREA_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9137814667511097 + wwv_flow_api.g_id_offset,
  p_region_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '50',
  p_column_height=> '2',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'FUNCTIONAL_AREA',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9143713295118881 + wwv_flow_api.g_id_offset,
  p_region_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'FUNCTIONAL_AREA_ID',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Functional Set Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '50',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 9137895153511102 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 5,
  p_button_sequence=> 10,
  p_button_plug_id => 9137296222511089+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:5:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9138115537511102 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 5,
  p_button_sequence=> 20,
  p_button_plug_id => 9137296222511089+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9138015251511102 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 5,
  p_button_sequence=> 30,
  p_button_plug_id => 9137296222511089+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9138207972511102 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 5,
  p_button_sequence=> 40,
  p_button_plug_id => 9137296222511089+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>9139804324511113 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 5,
  p_branch_action=> 'f?p=&APP_ID.:5:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:FUNCTIONAL_AREA:FUNCTIONAL_AREA_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9138914202511108 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 5,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9138015251511102 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:FUNCTIONAL_AREA:FUNCTIONAL_AREA_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9139126805511111 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 5,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:FUNCTIONAL_AREA:FUNCTIONAL_AREA_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9139405775511113 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 5,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9138207972511102 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 9139623002511113 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 5,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>9138207972511102 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 5
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9140419285513752 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'FUNCTIONAL_AREA_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9143499815118880 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'FUNCTIONAL_AREA_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9140622718513752 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9137296222511089 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00006
prompt  ...PAGE 6: TEST_OPERATION
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 6,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'TEST_OPERATION',
  p_alias  => 'TEST_OPERATION',
  p_step_title=> 'TEST_OPERATION',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090512132337',
  p_page_comment  => '');
 
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>6,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 8910219296293131 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 6,
  p_plug_name=> 'SEARCH',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"TEST_OPERATION_GSEQ",'||chr(10)||
'"TEST_OPERATION_ID",'||chr(10)||
'"DESCRIPTION",'||chr(10)||
'"USAGE_NOTES"'||chr(10)||
'from "#OWNER#"."TEST_OPERATION"'||chr(10)||
'WHERE upper(TEST_OPERATION_ID) LIKE upper(:P6_TEST_OPERATION_FILTER)';

wwv_flow_api.create_report_region (
  p_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 6,
  p_name=> 'Tabular Form',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8913923234299921 + wwv_flow_api.g_id_offset,
  p_region_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8911998773299914 + wwv_flow_api.g_id_offset,
  p_region_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_OPERATION_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Test Operation Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_OPERATION',
  p_ref_column_name=> 'TEST_OPERATION_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8912115754299914 + wwv_flow_api.g_id_offset,
  p_region_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_OPERATION_ID',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Test Operation Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '40',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_OPERATION',
  p_ref_column_name=> 'TEST_OPERATION_ID',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8912225362299914 + wwv_flow_api.g_id_offset,
  p_region_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '40',
  p_column_height=> '2',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'TEST_OPERATION',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8912315477299914 + wwv_flow_api.g_id_offset,
  p_region_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'USAGE_NOTES',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Usage Notes',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '60',
  p_column_height=> '2',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'TEST_OPERATION',
  p_ref_column_name=> 'USAGE_NOTES',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 8912498865299917 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 6,
  p_button_sequence=> 10,
  p_button_plug_id => 8911710077299911+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:6:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8912698249299917 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 6,
  p_button_sequence=> 20,
  p_button_plug_id => 8911710077299911+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8912615921299917 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 6,
  p_button_sequence=> 30,
  p_button_plug_id => 8911710077299911+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8912796932299917 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 6,
  p_button_sequence=> 40,
  p_button_plug_id => 8911710077299911+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>8914394779299921 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 6,
  p_branch_action=> 'f?p=&APP_ID.:6:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9353720132022685 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 6,
  p_name=>'P6_TEST_OPERATION_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 8910219296293131+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default => '%',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Test Operation Filter',
  p_source=>'%',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9354325326024199 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 6,
  p_name=>'P6_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 8910219296293131+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_OPERATION:TEST_OPERATION_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8913505061299919 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 6,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8912615921299917 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_OPERATION:TEST_OPERATION_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8913697618299919 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 6,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_OPERATION:TEST_OPERATION_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8914022575299921 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 6,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8912796932299917 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 8914202816299921 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 6,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>8912796932299917 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 6
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9354706757028238 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'TEST_OPERATION_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9354796392028239 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'TEST_OPERATION_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9354916216028239 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9355017420028239 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8911710077299911 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'USAGE_NOTES',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00008
prompt  ...PAGE 8: UI_SECTION
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 8,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'UI_SECTION',
  p_alias  => 'UI_SECTION',
  p_step_title=> 'UI_SECTION',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090512132420',
  p_page_comment  => '');
 
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>8,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 8928426185342338 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 8,
  p_plug_name=> 'SEARCH',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"UI_SECTION_GSEQ",'||chr(10)||
'"UI_SECTION_ID",'||chr(10)||
'"DESCRIPTION"'||chr(10)||
'from "#OWNER#"."UI_SECTION"'||chr(10)||
'WHERE upper(UI_SECTION_ID) LIKE upper(:P8_UI_SECTION_FILTER)';

wwv_flow_api.create_report_region (
  p_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 8,
  p_name=> 'Tabular Form',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8931995275346935 + wwv_flow_api.g_id_offset,
  p_region_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8930295716346925 + wwv_flow_api.g_id_offset,
  p_region_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'UI_SECTION_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Ui Section Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'UI_SECTION',
  p_ref_column_name=> 'UI_SECTION_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8930404326346925 + wwv_flow_api.g_id_offset,
  p_region_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'UI_SECTION_ID',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Ui Section Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '50',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'UI_SECTION',
  p_ref_column_name=> 'UI_SECTION_ID',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8930496524346925 + wwv_flow_api.g_id_offset,
  p_region_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '60',
  p_column_height=> '2',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'UI_SECTION',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 8930600670346927 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 8,
  p_button_sequence=> 10,
  p_button_plug_id => 8930009001346922+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:8:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8930808295346927 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 8,
  p_button_sequence=> 20,
  p_button_plug_id => 8930009001346922+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8930711431346927 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 8,
  p_button_sequence=> 30,
  p_button_plug_id => 8930009001346922+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8930904093346927 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 8,
  p_button_sequence=> 40,
  p_button_plug_id => 8930009001346922+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>8932520776346936 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 8,
  p_branch_action=> 'f?p=&APP_ID.:8:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9350796011949471 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 8,
  p_name=>'P8_UI_SECTION_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 8928426185342338+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default => '%',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Ui Section Filter',
  p_source=>'%',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9351403976951755 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 8,
  p_name=>'P8_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 8928426185342338+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:UI_SECTION:UI_SECTION_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8931602940346928 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 8,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8930711431346927 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:UI_SECTION:UI_SECTION_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8931806532346930 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 8,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:UI_SECTION:UI_SECTION_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8932119454346935 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 8,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8930904093346927 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 8932303723346936 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 8,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>8930904093346927 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 8
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9352321292956769 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'UI_SECTION_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9352419516956769 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'UI_SECTION_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9352523141956769 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8930009001346922 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00011
prompt  ...PAGE 11: TEST_CASE
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 11,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'TEST_CASE',
  p_alias  => 'TEST_CASE',
  p_step_title=> 'TEST_CASE',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090512131905',
  p_page_comment  => '');
 
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>11,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 3901136285982352 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 11,
  p_plug_name=> 'Breadcrumb',
  p_region_name=>'',
  p_plug_template=> 9688830199914126+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 1,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'REGION_POSITION_01',
  p_plug_source=> s,
  p_plug_source_type=> 'M'|| to_char(9695933642914154 + wwv_flow_api.g_id_offset),
  p_menu_template_id=> 9693645199914134+ wwv_flow_api.g_id_offset,
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'COLON_DELMITED_LIST',
  p_plug_query_row_count_max => 500,
  p_plug_display_condition_type => '',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 4612360534858335 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 11,
  p_plug_name=> 'Copy Test Case',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 25,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 8967121589396524 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 11,
  p_plug_name=> 'Search',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'SELECT'||chr(10)||
'"TEST_CASE_GSEQ",'||chr(10)||
'"TEST_CASE_ID" TEST_CASE_LINK,'||chr(10)||
'"TEST_CASE_ID",'||chr(10)||
'"DESCRIPTION",'||chr(10)||
'"FUNCTIONAL_AREA_GSEQ",'||chr(10)||
'"TEST_CASE_TYPE_GSEQ",'||chr(10)||
'TEST_TYPE_GSEQ'||chr(10)||
'FROM "#OWNER#"."TEST_CASE"'||chr(10)||
'WHERE upper(TEST_CASE_ID) LIKE upper(:P11_TEST_CASE_FILTER)'||chr(10)||
'ORDER BY TEST_CASE_ID';

wwv_flow_api.create_report_region (
  p_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 11,
  p_name=> 'Test Cases',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8970897525406441 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8968899978406425 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Test Case Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE',
  p_ref_column_name=> 'TEST_CASE_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9172001497995446 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_LINK',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Test Case Link',
  p_column_link=>'f?p=&APP_ID.:12:&SESSION.::&DEBUG.::P12_TEST_CASE_GSEQ:#TEST_CASE_GSEQ#',
  p_column_linktext=>'#TEST_CASE_LINK#',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'WITHOUT_MODIFICATION',
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8969005600406427 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_ID',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Test Case Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '40',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE',
  p_ref_column_name=> 'TEST_CASE_ID',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8969199020406427 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 7,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '40',
  p_column_height=> '2',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'TEST_CASE',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9134520853500088 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 6,
  p_form_element_id=> null,
  p_column_alias=> 'FUNCTIONAL_AREA_GSEQ',
  p_column_display_sequence=> 8,
  p_column_heading=> 'Feature Set',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>9132698591488816 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'YES',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9165704808994442 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 7,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_TYPE_GSEQ',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Test Case Type',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>9166800629004958 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_column_default=> '1',
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9404413480562135 + wwv_flow_api.g_id_offset,
  p_region_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 8,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_TYPE_GSEQ',
  p_column_display_sequence=> 6,
  p_column_heading=> 'Test Type',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>8973112286422252 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'YES',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 4614577980872908 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 11,
  p_button_sequence=> 50,
  p_button_plug_id => 4612360534858335+wwv_flow_api.g_id_offset,
  p_button_name    => 'CREATE_COPY',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Create Copy',
  p_button_position=> 'BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8969509314406430 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 11,
  p_button_sequence=> 10,
  p_button_plug_id => 8968623105406416+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:11:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8969713314406430 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 11,
  p_button_sequence=> 20,
  p_button_plug_id => 8968623105406416+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8969601792406430 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 11,
  p_button_sequence=> 30,
  p_button_plug_id => 8968623105406416+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8969811815406430 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 11,
  p_button_sequence=> 40,
  p_button_plug_id => 8968623105406416+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>8971405243406441 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 11,
  p_branch_action=> 'f?p=&APP_ID.:11:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>4613456163866603 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 11,
  p_name=>'P11_TEST_CASE_COPY',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 30,
  p_item_plug_id => 4612360534858335+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Test Case Copy',
  p_source_type=> 'STATIC',
  p_display_as=> 'POPUP_KEY_LOV',
  p_named_lov=> 'TEST_CASE_LOV',
  p_lov => 'select test_case_id,test_case_gseq'||chr(10)||
'from test_case'||chr(10)||
'order by 1',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9347708909934219 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 11,
  p_name=>'P11_TEST_CASE_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 8967121589396524+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default => '%',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Test Case Filter',
  p_source=>'%',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9348316528936469 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 11,
  p_name=>'P11_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 8967121589396524+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_CASE:TEST_CASE_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8970524848406435 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 11,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8969601792406430 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_CASE:TEST_CASE_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8970707088406439 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 11,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_CASE:TEST_CASE_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8971009429406441 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 11,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8969811815406430 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 8971202861406441 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 11,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>8969811815406430 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'PK_SILK.CREATE_TEST_CASE_COPY(:P11_TEST_CASE_COPY)';

wwv_flow_api.create_page_process(
  p_id     => 4616170859031629 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 11,
  p_process_sequence=> 50,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'PLSQL',
  p_process_name=> 'CREATE COPY',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_when_button_id=>4614577980872908 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'PK_SILK.UPDATE_TG_FOR_TC_CHANGES()';

wwv_flow_api.create_page_process(
  p_id     => 4631573337836700 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 11,
  p_process_sequence=> 60,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'PLSQL',
  p_process_name=> 'PROCESS_TG_FOR_IUD_TC',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 11
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8974510779488042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'TEST_CASE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9171495653995425 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'TEST_CASE_LINK',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8974620608488042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'TEST_CASE_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8974814116488042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9134402972500078 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 5,
  p_query_column_name=> 'FUNCTIONAL_AREA_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9165611980994439 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 6,
  p_query_column_name=> 'TEST_CASE_TYPE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9404305892562133 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8968623105406416 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 7,
  p_query_column_name=> 'TEST_TYPE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00012
prompt  ...PAGE 12: Update TEST_CASE_STEP
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h:=h||'No help is available for this page.';

ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 12,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'Update TEST_CASE_STEP',
  p_step_title=> 'Update TEST_CASE_STEP',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => ' ',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'MAIN',
  p_last_upd_yyyymmddhh24miss => '20090612051525',
  p_page_comment  => '');
 
wwv_flow_api.set_page_help_text(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>12,p_text=>h);
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>12,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 3898826570828067 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 12,
  p_plug_name=> 'Breadcrumb',
  p_region_name=>'',
  p_plug_template=> 9688830199914126+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 1,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'REGION_POSITION_01',
  p_plug_source=> s,
  p_plug_source_type=> 'M'|| to_char(3897055398817528 + wwv_flow_api.g_id_offset),
  p_menu_template_id=> 9693645199914134+ wwv_flow_api.g_id_offset,
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'COLON_DELMITED_LIST',
  p_plug_query_row_count_max => 500,
  p_plug_display_condition_type => '',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'TEST_CASE_STEP.TEST_CASE_STEP_GSEQ,'||chr(10)||
'TEST_CASE_STEP.TEST_CASE_GSEQ,'||chr(10)||
'TEST_CASE_STEP.STEP_NUMBER,'||chr(10)||
'TEST_CASE_STEP.UI_OBJECT_GSEQ,'||chr(10)||
'TEST_CASE_STEP.VALUE,'||chr(10)||
'TEST_CASE_STEP.SOA_TEST_GSEQ,'||chr(10)||
'TEST_CASE_STEP.TEST_OPERATION_GSEQ,'||chr(10)||
'TEST_CASE_STEP.TEST_CASE_REF_GSEQ,'||chr(10)||
'TEST_CASE_STEP.DATA_DRIVEN'||chr(10)||
'from TEST_CASE_STEP'||chr(10)||
'where TEST_CASE_STEP.TEST_CASE_GSEQ=:P12_TEST_CASE_GSEQ'||chr(10)||
'order by step_number'||chr(10)||
'';

wwv_flow_api.create_report_region (
  p_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 12,
  p_name=> 'Test Case: &P12_TEST_CASE_ID.',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8980696864568338 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8978500163568331 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_STEP_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Test Case Step Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE_STEP',
  p_ref_column_name=> 'TEST_CASE_STEP_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8978594333568333 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_GSEQ',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Test Case',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_column_default=> 'TO_NUMBER(:P12_TEST_CASE_GSEQ)',
  p_column_default_type=> 'FUNCTION',
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE_STEP',
  p_ref_column_name=> 'TEST_CASE_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8978712318568333 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'STEP_NUMBER',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Step Number',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_column_default=> 'TO_NUMBER(:P12_STEP_TO_INSERT_AT)',
  p_column_default_type=> 'FUNCTION',
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE_STEP',
  p_ref_column_name=> 'STEP_NUMBER',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8978796459568333 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'UI_OBJECT_GSEQ',
  p_column_display_sequence=> 6,
  p_column_heading=> 'Ui Object',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>9795652314793079 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE_STEP',
  p_ref_column_name=> 'UI_OBJECT_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8978897941568333 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 6,
  p_form_element_id=> null,
  p_column_alias=> 'VALUE',
  p_column_display_sequence=> 7,
  p_column_heading=> 'Value',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE_STEP',
  p_ref_column_name=> 'VALUE',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8979014524568333 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 7,
  p_form_element_id=> null,
  p_column_alias=> 'SOA_TEST_GSEQ',
  p_column_display_sequence=> 8,
  p_column_heading=> 'Soa Test',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>10629654674442237 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE_STEP',
  p_ref_column_name=> 'SOA_TEST_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 8979112059568333 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 8,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_OPERATION_GSEQ',
  p_column_display_sequence=> 9,
  p_column_heading=> 'Test Operation',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>9795037769788826 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_CASE_STEP',
  p_ref_column_name=> 'TEST_OPERATION_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9003223589826799 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 9,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_REF_GSEQ',
  p_column_display_sequence=> 10,
  p_column_heading=> 'Test Case Reference',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>9796833182806504 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9457501820398481 + wwv_flow_api.g_id_offset,
  p_region_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 10,
  p_form_element_id=> null,
  p_column_alias=> 'DATA_DRIVEN',
  p_column_display_sequence=> 5,
  p_column_heading=> 'DD',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>10209627754158692 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_column_default=> '''N''',
  p_column_default_type=> 'FUNCTION',
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 8979621350568333 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 12,
  p_button_sequence=> 10,
  p_button_plug_id => 8978200891568327+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row (At)',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8979311482568333 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 12,
  p_button_sequence=> 20,
  p_button_plug_id => 8978200891568327+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:11:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8979516924568333 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 12,
  p_button_sequence=> 30,
  p_button_plug_id => 8978200891568327+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 8979397112568333 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 12,
  p_button_sequence=> 40,
  p_button_plug_id => 8978200891568327+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>8981224897568339 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 12,
  p_branch_action=> 'f?p=&APP_ID.:12:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>8985518678660641 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 12,
  p_name=>'P12_TEST_CASE_GSEQ',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 8978200891568327+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 1,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9021622570992900 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 12,
  p_name=>'P12_TEST_CASE_ID',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 8978200891568327+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source=>'select test_case_id'||chr(10)||
'from test_case'||chr(10)||
'where test_case_gseq=:P12_TEST_CASE_GSEQ',
  p_source_type=> 'QUERY',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 1,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9227622750734369 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 12,
  p_name=>'P12_STEP_TO_INSERT_AT',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 40,
  p_item_plug_id => 8978200891568327+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Step to Add Row At ',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 1,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'NO',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT-TOP',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_CASE_STEP:TEST_CASE_STEP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8980311401568336 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 12,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8979397112568333 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_CASE_STEP:TEST_CASE_STEP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8980515220568338 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 12,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_CASE_STEP:TEST_CASE_STEP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 8980817806568338 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 12,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>8979621350568333 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 8981009999568339 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 12,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>8979621350568333 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'select test_case_id'||chr(10)||
'into :P12_TEST_CASE_ID'||chr(10)||
'from test_case'||chr(10)||
'where test_case_gseq=:P12_TEST_CASE_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9027613052028025 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 12,
  p_process_sequence=> 50,
  p_process_point=> 'BEFORE_HEADER',
  p_process_type=> 'PLSQL',
  p_process_name=> 'SET_TEST_CASE_ID',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'PK_SILK.INSERT_TEST_CASE_STEP_AT(TO_NUMBER(:P12_TEST_CASE_GSEQ),TO_NUMBER(:P12_STEP_TO_INSERT_AT))';

wwv_flow_api.create_page_process(
  p_id     => 9228718295761460 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 12,
  p_process_sequence=> 70,
  p_process_point=> 'ON_SUBMIT_BEFORE_COMPUTATION',
  p_process_type=> 'PLSQL',
  p_process_name=> 'INSERT_STEP_AT_PROCESS',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_when_button_id=>8979621350568333 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 12
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8988297945683041 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'TEST_CASE_STEP_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8988426053683042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'TEST_CASE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8988506393683042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'STEP_NUMBER',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8988625541683042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'UI_OBJECT_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8988694980683042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 5,
  p_query_column_name=> 'VALUE',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8988798155683042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 6,
  p_query_column_name=> 'SOA_TEST_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 8988904655683042 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 7,
  p_query_column_name=> 'TEST_OPERATION_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9003108123826796 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 8,
  p_query_column_name=> 'TEST_CASE_REF_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9457405838398480 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 8978200891568327 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 9,
  p_query_column_name=> 'DATA_DRIVEN',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00013
prompt  ...PAGE 13: TEST_GROUP
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 13,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'TEST_GROUP',
  p_alias  => 'TEST_GROUP',
  p_step_title=> 'TEST_GROUP',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090518115513',
  p_page_is_public_y_n=> 'N',
  p_page_comment  => '');
 
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>13,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 4068144582576382 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 13,
  p_plug_name=> 'Breadcrumb',
  p_region_name=>'',
  p_plug_template=> 9688830199914126+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 1,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'REGION_POSITION_01',
  p_plug_source=> s,
  p_plug_source_type=> 'M'|| to_char(9695933642914154 + wwv_flow_api.g_id_offset),
  p_menu_template_id=> 9693645199914134+ wwv_flow_api.g_id_offset,
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'COLON_DELMITED_LIST',
  p_plug_query_row_count_max => 500,
  p_plug_display_condition_type => '',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 9042211673756233 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 13,
  p_plug_name=> 'Search',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"TEST_GROUP_GSEQ",'||chr(10)||
'"TEST_GROUP_ID" TEST_GROUP_LINK,'||chr(10)||
'"TEST_GROUP_ID",'||chr(10)||
'"DESCRIPTION"'||chr(10)||
'from "#OWNER#"."TEST_GROUP"'||chr(10)||
'where upper(TEST_GROUP_ID) like upper(:P13_TEST_GROUP_FILTER)'||chr(10)||
'';

wwv_flow_api.create_report_region (
  p_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 13,
  p_name=> 'Test Groups',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9046295942762491 + wwv_flow_api.g_id_offset,
  p_region_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9055006323991752 + wwv_flow_api.g_id_offset,
  p_region_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_GROUP_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Test Group',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9279202869315539 + wwv_flow_api.g_id_offset,
  p_region_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_GROUP_LINK',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Test Group Link',
  p_column_link=>'f?p=&APP_ID.:14:&SESSION.::&DEBUG.::P14_TEST_GROUP_GSEQ:#TEST_GROUP_GSEQ#',
  p_column_linktext=>'#TEST_GROUP_LINK#',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'WITHOUT_MODIFICATION',
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9055122080991753 + wwv_flow_api.g_id_offset,
  p_region_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_GROUP_ID',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Test Group ID',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '50',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9044417451762458 + wwv_flow_api.g_id_offset,
  p_region_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXTAREA',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '50',
  p_column_height=> '1',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_GROUP',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 9044920810762464 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 13,
  p_button_sequence=> 10,
  p_button_plug_id => 9043900376762449+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:13:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9045122408762464 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 13,
  p_button_sequence=> 20,
  p_button_plug_id => 9043900376762449+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9045012614762464 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 13,
  p_button_sequence=> 30,
  p_button_plug_id => 9043900376762449+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9045200655762464 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 13,
  p_button_sequence=> 40,
  p_button_plug_id => 9043900376762449+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>9046799406762492 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 13,
  p_branch_action=> 'f?p=&APP_ID.:13:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9340919551880510 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 13,
  p_name=>'P13_TEST_GROUP_FILTER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 9042211673756233+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default => '%',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Test Group Filter',
  p_source=>'%',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9346301852922728 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 13,
  p_name=>'P13_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 9042211673756233+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_GROUP:TEST_GROUP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9045898519762480 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 13,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9045012614762464 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_GROUP:TEST_GROUP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9046118505762491 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 13,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_GROUP:TEST_GROUP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9046424578762492 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 13,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9045200655762464 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 9046613882762492 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 13,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>9045200655762464 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 13
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9054608022991747 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'TEST_GROUP_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9278825725315531 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'TEST_GROUP_LINK',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9054718703991747 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'TEST_GROUP_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9050015575941992 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9043900376762449 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00014
prompt  ...PAGE 14: Update TEST_GROUP_ITEM
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h:=h||'No help is available for this page.';

ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 14,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'Update TEST_GROUP_ITEM',
  p_step_title=> 'Update TEST_GROUP_ITEM',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => ' ',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090518115539',
  p_page_comment  => '');
 
wwv_flow_api.set_page_help_text(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>14,p_text=>h);
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>14,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 4069453586579029 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 14,
  p_plug_name=> 'Breadcrumb',
  p_region_name=>'',
  p_plug_template=> 9688830199914126+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 1,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'REGION_POSITION_01',
  p_plug_source=> s,
  p_plug_source_type=> 'M'|| to_char(9695933642914154 + wwv_flow_api.g_id_offset),
  p_menu_template_id=> 9693645199914134+ wwv_flow_api.g_id_offset,
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'COLON_DELMITED_LIST',
  p_plug_query_row_count_max => 500,
  p_plug_display_condition_type => '',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"TEST_GROUP_ITEM_GSEQ",'||chr(10)||
'"TEST_GROUP_GSEQ",'||chr(10)||
'"TEST_CASE_GSEQ",'||chr(10)||
'"STEP_NUMBER",'||chr(10)||
'"SETUP_STEP",'||chr(10)||
'"VERIFIED"'||chr(10)||
'from "#OWNER#"."TEST_GROUP_ITEM"'||chr(10)||
'where TEST_GROUP_GSEQ =:P14_TEST_GROUP_GSEQ'||chr(10)||
'order by step_number';

wwv_flow_api.create_report_region (
  p_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 14,
  p_name=> 'Test Group &P14_TEST_GROUP_ID.',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '20',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'TOP_AND_BOTTOM_LEFT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9060717043003358 + wwv_flow_api.g_id_offset,
  p_region_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9058619541003352 + wwv_flow_api.g_id_offset,
  p_region_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_GROUP_ITEM_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Test Group Item Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_GROUP_ITEM',
  p_ref_column_name=> 'TEST_GROUP_ITEM_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9058796000003353 + wwv_flow_api.g_id_offset,
  p_region_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_GROUP_GSEQ',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Test Group',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_named_lov=>9064007162029335 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_column_default=> 'TO_NUMBER(:P14_TEST_GROUP_GSEQ)',
  p_column_default_type=> 'FUNCTION',
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_GROUP_ITEM',
  p_ref_column_name=> 'TEST_GROUP_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9058921411003353 + wwv_flow_api.g_id_offset,
  p_region_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_GSEQ',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Test Case',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>9796833182806504 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_GROUP_ITEM',
  p_ref_column_name=> 'TEST_CASE_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9059003928003353 + wwv_flow_api.g_id_offset,
  p_region_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'STEP_NUMBER',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Step Number',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_column_default=> 'TO_NUMBER(:P14_STEP_TO_INSERT_AT)',
  p_column_default_type=> 'FUNCTION',
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'TEST_GROUP_ITEM',
  p_ref_column_name=> 'STEP_NUMBER',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9059118808003353 + wwv_flow_api.g_id_offset,
  p_region_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 6,
  p_form_element_id=> null,
  p_column_alias=> 'SETUP_STEP',
  p_column_display_sequence=> 6,
  p_column_heading=> 'Setup Step',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>10209627754158692 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_column_width=> '1',
  p_pk_col_source=> s,
  p_column_default=> '''N''',
  p_column_default_type=> 'FUNCTION',
  p_lov_display_extra=> 'YES',
  p_ref_table_name=> 'TEST_GROUP_ITEM',
  p_ref_column_name=> 'SETUP_STEP',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 4833971244974381 + wwv_flow_api.g_id_offset,
  p_region_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 7,
  p_form_element_id=> null,
  p_column_alias=> 'VERIFIED',
  p_column_display_sequence=> 7,
  p_column_heading=> 'Verified',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'SELECT_LIST_FROM_LOV',
  p_named_lov=>10209627754158692 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_column_default=> '''N''',
  p_column_default_type=> 'FUNCTION',
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 9059610819003353 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 14,
  p_button_sequence=> 10,
  p_button_plug_id => 9058310545003350+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row (At)',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9059311553003353 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 14,
  p_button_sequence=> 20,
  p_button_plug_id => 9058310545003350+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'f?p=&APP_ID.:13:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9059501822003353 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 14,
  p_button_sequence=> 30,
  p_button_plug_id => 9058310545003350+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9059406375003353 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 14,
  p_button_sequence=> 40,
  p_button_plug_id => 9058310545003350+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'TOP_AND_BOTTOM',
  p_button_alignment=> 'LEFT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>9061214941003358 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 14,
  p_branch_action=> 'f?p=&APP_ID.:14:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9068103493113427 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 14,
  p_name=>'P14_TEST_GROUP_GSEQ',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 9058310545003350+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 1,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9073221548147021 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 14,
  p_name=>'P14_TEST_GROUP_ID',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 9058310545003350+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 1,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9244008579957363 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 14,
  p_name=>'P14_STEP_TO_INSERT_AT',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 40,
  p_item_plug_id => 9058310545003350+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Step to Add Row At ',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 1,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'NO',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT-BOTTOM',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_lov_display_extra=>'NO',
  p_protection_level => 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_GROUP_ITEM:TEST_GROUP_ITEM_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9060309545003355 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 14,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9059406375003353 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_GROUP_ITEM:TEST_GROUP_ITEM_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9060509647003358 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 14,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:TEST_GROUP_ITEM:TEST_GROUP_ITEM_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9060811435003358 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 14,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9059610819003353 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 9060996043003358 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 14,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>9059610819003353 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'PK_SILK.INSERT_TEST_GROUP_ITEM_AT(TO_NUMBER(:P14_TEST_GROUP_GSEQ),TO_NUMBER(:P14_STEP_TO_INSERT_AT))';

wwv_flow_api.create_page_process(
  p_id     => 9243119876951124 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 14,
  p_process_sequence=> 50,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'PLSQL',
  p_process_name=> 'INSERT_STEP_AT_PROCESS',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_when_button_id=>9059610819003353 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'PK_SILK.RESET_TEST_GROUP_ITEM_NUMBERS(TO_NUMBER(:P14_TEST_GROUP_GSEQ))';

wwv_flow_api.create_page_process(
  p_id     => 9245699491973667 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 14,
  p_process_sequence=> 60,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'PLSQL',
  p_process_name=> 'RESET_TC_STEPS_DELETE',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_when_button_id=>9059501822003353 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'select test_group_id'||chr(10)||
'into :P14_TEST_GROUP_ID'||chr(10)||
'from test_group'||chr(10)||
'where test_group_gseq=:P14_TEST_GROUP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9069800984131558 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 14,
  p_process_sequence=> 50,
  p_process_point=> 'BEFORE_HEADER',
  p_process_type=> 'PLSQL',
  p_process_name=> 'SET_TEST_GROUP_ID',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 14
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9062007031019831 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'TEST_GROUP_ITEM_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9062123640019833 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'TEST_GROUP_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9062204189019833 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'TEST_CASE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9062316805019833 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'STEP_NUMBER',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9062426195019833 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 5,
  p_query_column_name=> 'SETUP_STEP',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 4833880765974380 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9058310545003350 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 6,
  p_query_column_name=> 'VERIFIED',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00015
prompt  ...PAGE 15: DD Test Cases
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h:=h||'No help is available for this page.';

ph := null;
wwv_flow_api.create_page(
  p_id     => 15,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'DD Test Cases',
  p_step_title=> 'DD Test Cases',
  p_step_sub_title => 'Report 1',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'AUTO_FIRST_ITEM',
  p_help_text => ' ',
  p_html_page_header => '',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'JOE',
  p_last_upd_yyyymmddhh24miss => '20090413135819',
  p_page_is_public_y_n=> 'N',
  p_page_comment  => '');
 
wwv_flow_api.set_page_help_text(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>15,p_text=>h);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'SELECT TEST_CASE_GSEQ,TEST_CASE_ID'||chr(10)||
'FROM TEST_CASE,TEST_CASE_TYPE'||chr(10)||
'WHERE TEST_CASE.TEST_CASE_TYPE_GSEQ=TEST_CASE_TYPE.TEST_CASE_TYPE_GSEQ'||chr(10)||
'AND TEST_CASE_TYPE.TEST_CASE_TYPE_ID=''DATA_DRIVEN_TEST_CASE''';

wwv_flow_api.create_report_region (
  p_id=> 9454002102297730 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 15,
  p_name=> 'List of Data-Driven Test Cases',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 10,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'QUERY_COLUMNS',
  p_query_num_rows=> '15',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'BOTTOM_RIGHT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9454320422297767 + wwv_flow_api.g_id_offset,
  p_region_id=> 9454002102297730 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_GSEQ',
  p_column_display_sequence=> 1,
  p_column_heading=> 'Test Case Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9454406716297772 + wwv_flow_api.g_id_offset,
  p_region_id=> 9454002102297730 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_ID',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Test Case Id',
  p_column_link=>'f?p=&APP_ID.:16:&SESSION.::&DEBUG.::P16_TEST_CASE_GSEQ:#TEST_CASE_GSEQ#',
  p_column_linktext=>'#TEST_CASE_ID#',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'WITHOUT_MODIFICATION',
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
 
begin
 
null;
 
end;
/

 
begin
 
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 15
--
 
begin
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00016
prompt  ...PAGE 16: Update DD_TEST_CASE
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h:=h||'No help is available for this page.';

ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 16,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'Update DD_TEST_CASE',
  p_step_title=> 'Update DD_TEST_CASE',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => ' ',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'REBECCA',
  p_last_upd_yyyymmddhh24miss => '20090423124456',
  p_page_comment  => '');
 
wwv_flow_api.set_page_help_text(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>16,p_text=>h);
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>16,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'"DD_TEST_CASE_GSEQ",'||chr(10)||
'"DD_TEST_CASE_ID" DD_TEST_CASE_ID_LINK,'||chr(10)||
'"DD_TEST_CASE_ID",'||chr(10)||
'"TEST_CASE_GSEQ",'||chr(10)||
'"DD_TEST_NUMBER",'||chr(10)||
'"DESCRIPTION"'||chr(10)||
'from "#OWNER#"."DD_TEST_CASE"'||chr(10)||
'where TEST_CASE_GSEQ=:P16_TEST_CASE_GSEQ'||chr(10)||
'ORDER BY DD_TEST_NUMBER';

wwv_flow_api.create_report_region (
  p_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 16,
  p_name=> 'Data-Driven Test Cases',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '10',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'BOTTOM_RIGHT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9497003758969888 + wwv_flow_api.g_id_offset,
  p_region_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9495095982969866 + wwv_flow_api.g_id_offset,
  p_region_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'DD_TEST_CASE_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Dd Test Case Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE',
  p_ref_column_name=> 'DD_TEST_CASE_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9502895327987646 + wwv_flow_api.g_id_offset,
  p_region_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'DD_TEST_CASE_ID_LINK',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Dd Test Case Id Link',
  p_column_link=>'f?p=&APP_ID.:19:&SESSION.::&DEBUG.::P19_DD_TEST_CASE_GSEQ,P19_DD_TEST_CASE_NUMBER:#DD_TEST_CASE_GSEQ#,#DD_TEST_NUMBER#',
  p_column_linktext=>'#DD_TEST_CASE_ID_LINK#',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'WITHOUT_MODIFICATION',
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9495202739969867 + wwv_flow_api.g_id_offset,
  p_region_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'DD_TEST_CASE_ID',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Dd Test Case Id',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE',
  p_ref_column_name=> 'DD_TEST_CASE_ID',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9495302241969867 + wwv_flow_api.g_id_offset,
  p_region_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_GSEQ',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Test Case Gseq',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_column_default=> 'P16_TEST_CASE_GSEQ',
  p_column_default_type=> 'ITEM',
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE',
  p_ref_column_name=> 'TEST_CASE_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9495398030969867 + wwv_flow_api.g_id_offset,
  p_region_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 6,
  p_form_element_id=> null,
  p_column_alias=> 'DD_TEST_NUMBER',
  p_column_display_sequence=> 6,
  p_column_heading=> 'Dd Test Number',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE',
  p_ref_column_name=> 'DD_TEST_NUMBER',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9495512074969867 + wwv_flow_api.g_id_offset,
  p_region_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 7,
  p_form_element_id=> null,
  p_column_alias=> 'DESCRIPTION',
  p_column_display_sequence=> 7,
  p_column_heading=> 'Description',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE',
  p_ref_column_name=> 'DESCRIPTION',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 9540997821089667 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 16,
  p_plug_name=> 'CREATE_DD_TEST',
  p_region_name=>'',
  p_plug_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_display_error_message=> '#SQLERRM#',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'QUERY_COLUMNS',
  p_plug_query_num_rows_type => 'NEXT_PREVIOUS_LINKS',
  p_plug_query_row_count_max => 500,
  p_plug_query_show_nulls_as => ' - ',
  p_plug_display_condition_type => '',
  p_pagination_display_position=>'BOTTOM_RIGHT',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 9495701273969869 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 16,
  p_button_sequence=> 30,
  p_button_plug_id => 9494818386969861+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'REGION_TEMPLATE_CHANGE',
  p_button_alignment=> 'RIGHT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9495607646969869 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 16,
  p_button_sequence=> 10,
  p_button_plug_id => 9494818386969861+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'REGION_TEMPLATE_CLOSE',
  p_button_alignment=> 'RIGHT',
  p_button_redirect_url=> 'f?p=&APP_ID.:15:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9495895071969869 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 16,
  p_button_sequence=> 40,
  p_button_plug_id => 9494818386969861+wwv_flow_api.g_id_offset,
  p_button_name    => 'ADD',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Add Row',
  p_button_position=> 'REGION_TEMPLATE_CREATE2',
  p_button_alignment=> 'RIGHT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9495797912969869 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 16,
  p_button_sequence=> 20,
  p_button_plug_id => 9494818386969861+wwv_flow_api.g_id_offset,
  p_button_name    => 'MULTI_ROW_DELETE',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Delete',
  p_button_position=> 'REGION_TEMPLATE_DELETE',
  p_button_alignment=> 'RIGHT',
  p_button_redirect_url=> 'javascript:confirmDelete(htmldb_delete_message,''MULTI_ROW_DELETE'');',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>9497516526969889 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 16,
  p_branch_action=> 'f?p=&APP_ID.:16:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9498311377971192 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 16,
  p_name=>'P16_TEST_CASE_GSEQ',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 9494818386969861+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9498914840972191 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 16,
  p_name=>'P16_TEST_CASE_ID',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 9494818386969861+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9541995182098349 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 16,
  p_name=>'P16_ADD_DD_TEST_CASE_ID',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 30,
  p_item_plug_id => 9540997821089667+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Add Dd Test Case Id',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9542816306104467 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 16,
  p_name=>'P16_ADD_DD_TEST_DESCRIPTION',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 40,
  p_item_plug_id => 9540997821089667+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_prompt=>'Add Dd Test Description',
  p_source_type=> 'STATIC',
  p_display_as=> 'TEXT',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 30,
  p_cMaxlength=> 2000,
  p_cHeight=> 5,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9544209165111888 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 16,
  p_name=>'P16_GO',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 50,
  p_item_plug_id => 9540997821089667+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'NO',
  p_item_default => 'Go',
  p_prompt=>'Go',
  p_source=>'Go',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_button_image => 'go.gif',
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'N',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:DD_TEST_CASE:DD_TEST_CASE_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9496621736969886 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 16,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9495701273969869 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:DD_TEST_CASE:DD_TEST_CASE_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9496824435969888 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 16,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_DELETE',
  p_process_name=> 'ApplyMRD',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process delete.',
  p_process_when=>'MULTI_ROW_DELETE',
  p_process_when_type=>'REQUEST_EQUALS_CONDITION',
  p_process_success_message=> '#MRD_COUNT# row(s) deleted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:DD_TEST_CASE:DD_TEST_CASE_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9497102443969888 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 16,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9495895071969869 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'1';

wwv_flow_api.create_page_process(
  p_id     => 9497323839969888 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 16,
  p_process_sequence=> 40,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'ADD_ROWS_TO_TABULAR_FORM',
  p_process_name=> 'AddRows',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to add rows.',
  p_process_when_button_id=>9495895071969869 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'PK_SILK.DD_STEPS_ADD_NEW_STEPS_SET(:P16_ADD_DD_TEST_CASE_ID,:P16_TEST_CASE_GSEQ,:P16_ADD_DD_TEST_DESCRIPTION);';

wwv_flow_api.create_page_process(
  p_id     => 9540111570961164 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 16,
  p_process_sequence=> 50,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'PLSQL',
  p_process_name=> 'PROCESS_SUBMIT',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_when_button_id=>9544209165111888 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 16
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9499322805974485 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'DD_TEST_CASE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9502413712987636 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'DD_TEST_CASE_ID_LINK',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9499409411974485 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'DD_TEST_CASE_ID',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9499526053974485 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'TEST_CASE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9499599713974485 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 5,
  p_query_column_name=> 'DD_TEST_NUMBER',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9499697247974485 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9494818386969861 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 6,
  p_query_column_name=> 'DESCRIPTION',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00019
prompt  ...PAGE 19: Update DD_TEST_CASE_STEP
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h:=h||'No help is available for this page.';

ph:=ph||'<script language="JavaScript" type="text/javascript">'||chr(10)||
'<!--'||chr(10)||
''||chr(10)||
' htmldb_delete_message=''"DELETE_CONFIRM_MSG"'';'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>';

wwv_flow_api.create_page(
  p_id     => 19,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'TEST_CASE',
  p_name   => 'Update DD_TEST_CASE_STEP',
  p_step_title=> 'Update DD_TEST_CASE_STEP',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'NO_FIRST_ITEM',
  p_help_text => ' ',
  p_html_page_header => ' ',
  p_step_template => '',
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'JOE',
  p_last_upd_yyyymmddhh24miss => '20090416151728',
  p_page_comment  => '');
 
wwv_flow_api.set_page_help_text(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>19,p_text=>h);
wwv_flow_api.set_html_page_header(p_flow_id=>wwv_flow.g_flow_id,p_flow_step_id=>19,p_text=>ph);
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s:=s||'select '||chr(10)||
'DD_TEST_CASE_STEP.DD_TEST_CASE_STEP_GSEQ,'||chr(10)||
'DD_TEST_CASE_STEP.DD_TEST_CASE_GSEQ,'||chr(10)||
'DD_TEST_CASE_STEP.TEST_CASE_STEP_GSEQ,'||chr(10)||
'TEST_CASE_STEP.UI_OBJECT_GSEQ,'||chr(10)||
'TEST_CASE_STEP.TEST_OPERATION_GSEQ,'||chr(10)||
'DD_TEST_CASE_STEP.VALUE'||chr(10)||
'from DD_TEST_CASE_STEP,TEST_CASE_STEP,DD_TEST_CASE'||chr(10)||
'where DD_TEST_CASE.DD_TEST_CASE_GSEQ=:P19_DD_TEST_CASE_GSEQ'||chr(10)||
'and DD_TEST_CASE.DD_TEST_NUMBER=:P19_DD_TEST_CASE_NUMBER'||chr(10)||
'and DD_TEST_CAS';

s:=s||'E.DD_TEST_CASE_GSEQ=DD_TEST_CASE_STEP.DD_TEST_CASE_GSEQ'||chr(10)||
'and DD_TEST_CASE_STEP.TEST_CASE_STEP_GSEQ=TEST_CASE_STEP.TEST_CASE_STEP_GSEQ'||chr(10)||
''||chr(10)||
'';

wwv_flow_api.create_report_region (
  p_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 19,
  p_name=> 'Data-Driven Test Case Steps',
  p_region_name=>'',
  p_template=> 9689839472914128+ wwv_flow_api.g_id_offset,
  p_display_sequence=> 15,
  p_display_column=> 1,
  p_display_point=> 'AFTER_SHOW_ITEMS',
  p_source=> s,
  p_source_type=> 'UPDATABLE_SQL_QUERY',
  p_display_error_message=> '#SQLERRM#',
  p_customized=> '0',
  p_translate_title=> 'Y',
  p_query_row_template=> 9692549154914132+ wwv_flow_api.g_id_offset,
  p_query_headings_type=> 'COLON_DELMITED_LIST',
  p_query_num_rows=> '10',
  p_query_options=> 'DERIVED_REPORT_COLUMNS',
  p_query_show_nulls_as=> '(null)',
  p_query_break_cols=> '0',
  p_query_no_data_found=> 'No data found.',
  p_query_num_rows_type=> 'ROW_RANGES_IN_SELECT_LIST',
  p_query_row_count_max=> '500',
  p_pagination_display_position=> 'BOTTOM_RIGHT',
  p_csv_output=> 'N',
  p_sort_null=> 'F',
  p_query_asc_image=> 'arrow_down_gray_dark.gif',
  p_query_asc_image_attr=> 'width="13" height="12" alt=""',
  p_query_desc_image=> 'arrow_up_gray_dark.gif',
  p_query_desc_image_attr=> 'width="13" height="12" alt=""',
  p_plug_query_strip_html=> 'Y',
  p_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9507910664995071 + wwv_flow_api.g_id_offset,
  p_region_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 1,
  p_form_element_id=> null,
  p_column_alias=> 'CHECK$01',
  p_column_display_sequence=> 1,
  p_column_heading=> '&nbsp;',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'CHECKBOX',
  p_pk_col_source=> s,
  p_derived_column=> 'Y',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9505926395995064 + wwv_flow_api.g_id_offset,
  p_region_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 2,
  p_form_element_id=> null,
  p_column_alias=> 'DD_TEST_CASE_STEP_GSEQ',
  p_column_display_sequence=> 2,
  p_column_heading=> 'Dd Test Case Step',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_column_width=> '16',
  p_pk_col_source_type=> 'T',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE_STEP',
  p_ref_column_name=> 'DD_TEST_CASE_STEP_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9515508978323150 + wwv_flow_api.g_id_offset,
  p_region_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 3,
  p_form_element_id=> null,
  p_column_alias=> 'DD_TEST_CASE_GSEQ',
  p_column_display_sequence=> 5,
  p_column_heading=> 'Dd Test Case',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT_FROM_LOV',
  p_named_lov=>9535504328735485 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_column_default=> 'P19_DD_TEST_CASE_GSEQ',
  p_column_default_type=> 'ITEM',
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9506011114995064 + wwv_flow_api.g_id_offset,
  p_region_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 4,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_CASE_STEP_GSEQ',
  p_column_display_sequence=> 3,
  p_column_heading=> 'Test Case Step',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'LEFT',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'Y',
  p_display_as=>'HIDDEN',
  p_lov_show_nulls=> 'NO',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE_STEP',
  p_ref_column_name=> 'TEST_CASE_STEP_GSEQ',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9522214541378750 + wwv_flow_api.g_id_offset,
  p_region_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 5,
  p_form_element_id=> null,
  p_column_alias=> 'UI_OBJECT_GSEQ',
  p_column_display_sequence=> 6,
  p_column_heading=> 'Ui Object',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT_FROM_LOV',
  p_named_lov=>9795652314793079 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9522307479378750 + wwv_flow_api.g_id_offset,
  p_region_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 6,
  p_form_element_id=> null,
  p_column_alias=> 'TEST_OPERATION_GSEQ',
  p_column_display_sequence=> 7,
  p_column_heading=> 'Test Operation',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT_FROM_LOV',
  p_named_lov=>9795037769788826 + wwv_flow_api.g_id_offset,
  p_lov_show_nulls=> 'NO',
  p_pk_col_source=> s,
  p_lov_display_extra=> 'YES',
  p_column_comment=>'');
end;
/
declare
  s varchar2(32767) := null;
begin
s := null;
wwv_flow_api.create_report_columns (
  p_id=> 9506412531995066 + wwv_flow_api.g_id_offset,
  p_region_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_query_column_id=> 7,
  p_form_element_id=> null,
  p_column_alias=> 'VALUE',
  p_column_display_sequence=> 4,
  p_column_heading=> 'Value',
  p_column_alignment=>'LEFT',
  p_heading_alignment=>'CENTER',
  p_default_sort_column_sequence=>0,
  p_disable_sort_column=>'Y',
  p_sum_column=> 'N',
  p_hidden_column=> 'N',
  p_display_as=>'TEXT',
  p_column_width=> '16',
  p_pk_col_source=> s,
  p_ref_schema=> 'JOE',
  p_ref_table_name=> 'DD_TEST_CASE_STEP',
  p_ref_column_name=> 'VALUE',
  p_column_comment=>'');
end;
/
 
begin
 
wwv_flow_api.create_page_button(
  p_id             => 9506626548995066 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 19,
  p_button_sequence=> 30,
  p_button_plug_id => 9505600547995064+wwv_flow_api.g_id_offset,
  p_button_name    => 'SUBMIT',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Submit',
  p_button_position=> 'REGION_TEMPLATE_CHANGE',
  p_button_alignment=> 'RIGHT',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
wwv_flow_api.create_page_button(
  p_id             => 9506497636995066 + wwv_flow_api.g_id_offset,
  p_flow_id        => wwv_flow.g_flow_id,
  p_flow_step_id   => 19,
  p_button_sequence=> 10,
  p_button_plug_id => 9505600547995064+wwv_flow_api.g_id_offset,
  p_button_name    => 'CANCEL',
  p_button_image   => 'template:'||to_char(9688636459914126+wwv_flow_api.g_id_offset),
  p_button_image_alt=> 'Cancel',
  p_button_position=> 'REGION_TEMPLATE_CLOSE',
  p_button_alignment=> 'RIGHT',
  p_button_redirect_url=> 'f?p=&APP_ID.:16:&SESSION.::&DEBUG.:::',
  p_required_patch => null + wwv_flow_api.g_id_offset);
 
 
end;
/

 
begin
 
wwv_flow_api.create_page_branch(
  p_id=>9508394684995071 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 19,
  p_branch_action=> 'f?p=&APP_ID.:19:&SESSION.&success_msg=#SUCCESS_MSG#',
  p_branch_point=> 'AFTER_PROCESSING',
  p_branch_type=> 'REDIRECT_URL',
  p_branch_sequence=> 1,
  p_branch_comment=> '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9509203112997152 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 19,
  p_name=>'P19_TEST_CASE_GSEQ',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 9505600547995064+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9509809000998856 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 19,
  p_name=>'P19_TEST_CASE_ID',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 9505600547995064+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9510725277003577 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 19,
  p_name=>'P19_DD_TEST_CASE_GSEQ',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 30,
  p_item_plug_id => 9505600547995064+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9511300474005838 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 19,
  p_name=>'P19_DD_TEST_CASE_NUMBER',
  p_data_type=> 'VARCHAR',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 40,
  p_item_plug_id => 9505600547995064+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> 'YES',
  p_item_default_type => 'STATIC_TEXT_WITH_SUBSTITUTIONS',
  p_source_type=> 'STATIC',
  p_display_as=> 'HIDDEN',
  p_lov_columns=> 1,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> 2000,
  p_cHeight=> null,
  p_cAttributes=> 'nowrap="nowrap"',
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'#OWNER#:DD_TEST_CASE_STEP:DD_TEST_CASE_STEP_GSEQ';

wwv_flow_api.create_page_process(
  p_id     => 9507507713995069 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 19,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'MULTI_ROW_UPDATE',
  p_process_name=> 'ApplyMRU',
  p_process_sql_clob => p, 
  p_process_error_message=> 'Unable to process update.',
  p_process_when_button_id=>9506626548995066 + wwv_flow_api.g_id_offset,
  p_process_success_message=> '#MRU_COUNT# row(s) updated, #MRI_COUNT# row(s) inserted.',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 19
--
 
begin
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9515117957323149 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 1,
  p_query_column_name=> 'DD_TEST_CASE_STEP_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9515211069323149 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 2,
  p_query_column_name=> 'DD_TEST_CASE_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9515319496323149 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 3,
  p_query_column_name=> 'TEST_CASE_STEP_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9521917069378747 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 4,
  p_query_column_name=> 'UI_OBJECT_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9522018094378747 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 5,
  p_query_column_name=> 'TEST_OPERATION_GSEQ',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
wwv_flow_api.create_region_rpt_cols (
  p_id     => 9515411862323149 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_plug_id=> 9505600547995064 + wwv_flow_api.g_id_offset,
  p_column_sequence=> 6,
  p_query_column_name=> 'VALUE',
  p_display_as=> 'TEXT',
  p_column_comment=> '');
 
null;
end;
null;
 
end;
/

 
--application/pages/page_00101
prompt  ...PAGE 101: Login
--
 
begin
 
declare
    h varchar2(32767) := null;
    ph varchar2(32767) := null;
begin
h := null;
ph := null;
wwv_flow_api.create_page(
  p_id     => 101,
  p_flow_id=> wwv_flow.g_flow_id,
  p_tab_set=> 'T_ADMIN',
  p_name   => 'Login',
  p_alias  => 'LOGIN',
  p_step_title=> 'Login',
  p_step_sub_title_type => 'TEXT_WITH_SUBSTITUTIONS',
  p_first_item=> 'AUTO_FIRST_ITEM',
  p_help_text => '',
  p_html_page_header => '',
  p_step_template => 9688054572914126+ wwv_flow_api.g_id_offset,
  p_required_patch=> null + wwv_flow_api.g_id_offset,
  p_last_updated_by => 'DEV1',
  p_last_upd_yyyymmddhh24miss => '20090307064018',
  p_page_is_public_y_n=> 'N',
  p_page_comment  => '');
 
end;
 
end;
/

declare
  s varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
s := null;
wwv_flow_api.create_page_plug (
  p_id=> 9695147893914148 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_page_id=> 101,
  p_plug_name=> 'Login',
  p_region_name=>'',
  p_plug_template=> 0,
  p_plug_display_sequence=> 10,
  p_plug_display_column=> 1,
  p_plug_display_point=> 'AFTER_SHOW_ITEMS',
  p_plug_source=> s,
  p_plug_source_type=> 'STATIC_TEXT',
  p_plug_query_row_template=> 1,
  p_plug_query_headings_type=> 'COLON_DELMITED_LIST',
  p_plug_query_row_count_max => 500,
  p_plug_display_condition_type => '',
  p_plug_caching=> 'NOT_CACHED',
  p_required_patch=> '' + wwv_flow_api.g_id_offset,
  p_plug_comment=> '');
end;
/
 
begin
 
null;
 
end;
/

 
begin
 
null;
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9695239899914151 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 101,
  p_name=>'P101_USERNAME',
  p_data_type=> '',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 10,
  p_item_plug_id => 9695147893914148+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> '',
  p_prompt=>'User Name',
  p_display_as=> 'TEXT',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 40,
  p_cMaxlength=> 100,
  p_cHeight=> null,
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 2,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9695323604914151 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 101,
  p_name=>'P101_PASSWORD',
  p_data_type=> '',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 20,
  p_item_plug_id => 9695147893914148+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> '',
  p_prompt=>'Password',
  p_display_as=> 'PASSWORD_WITH_ENTER_SUBMIT',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> 40,
  p_cMaxlength=> 100,
  p_cHeight=> null,
  p_begin_on_new_line => 'YES',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'RIGHT',
  p_field_alignment  => 'LEFT',
  p_field_template => 9693128915914134+wwv_flow_api.g_id_offset,
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

declare
    h varchar2(32767) := null;
begin
wwv_flow_api.create_page_item(
  p_id=>9695450405914151 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id=> 101,
  p_name=>'P101_LOGIN',
  p_data_type=> '',
  p_accept_processing=> 'REPLACE_EXISTING',
  p_item_sequence=> 30,
  p_item_plug_id => 9695147893914148+wwv_flow_api.g_id_offset,
  p_use_cache_before_default=> '',
  p_item_default => 'Login',
  p_prompt=>'Login',
  p_source=>'LOGIN',
  p_source_type=> 'STATIC',
  p_display_as=> 'BUTTON',
  p_lov_columns=> null,
  p_lov_display_null=> 'NO',
  p_lov_translated=> 'N',
  p_cSize=> null,
  p_cMaxlength=> null,
  p_cHeight=> null,
  p_tag_attributes  => 'template:'||to_char(9688636459914126 + wwv_flow_api.g_id_offset),
  p_begin_on_new_line => 'NO',
  p_begin_on_new_field=> 'YES',
  p_colspan => 1,
  p_rowspan => 1,
  p_label_alignment  => 'LEFT',
  p_field_alignment  => 'LEFT',
  p_is_persistent=> 'Y',
  p_item_comment => '');
 
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'begin'||chr(10)||
'owa_util.mime_header(''text/html'', FALSE);'||chr(10)||
'owa_cookie.send('||chr(10)||
'    name=>''LOGIN_USERNAME_COOKIE'','||chr(10)||
'    value=>lower(:P101_USERNAME));'||chr(10)||
'exception when others then null;'||chr(10)||
'end;';

wwv_flow_api.create_page_process(
  p_id     => 9695643159914154 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 101,
  p_process_sequence=> 10,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'PLSQL',
  p_process_name=> 'Set Username Cookie',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'wwv_flow_custom_auth_std.login('||chr(10)||
'    P_UNAME       => :P101_USERNAME,'||chr(10)||
'    P_PASSWORD    => :P101_PASSWORD,'||chr(10)||
'    P_SESSION_ID  => v(''APP_SESSION''),'||chr(10)||
'    P_FLOW_PAGE   => :APP_ID||'':52'''||chr(10)||
'    );';

wwv_flow_api.create_page_process(
  p_id     => 9695538892914151 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 101,
  p_process_sequence=> 20,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'PLSQL',
  p_process_name=> 'Login',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'101';

wwv_flow_api.create_page_process(
  p_id     => 9695842756914154 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 101,
  p_process_sequence=> 30,
  p_process_point=> 'AFTER_SUBMIT',
  p_process_type=> 'CLEAR_CACHE_FOR_PAGES',
  p_process_name=> 'Clear Page(s) Cache',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
declare
  p varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
p:=p||'declare'||chr(10)||
'    v varchar2(255) := null;'||chr(10)||
'    c owa_cookie.cookie;'||chr(10)||
'begin'||chr(10)||
'   c := owa_cookie.get(''LOGIN_USERNAME_COOKIE'');'||chr(10)||
'   :P101_USERNAME := c.vals(1);'||chr(10)||
'exception when others then null;'||chr(10)||
'end;';

wwv_flow_api.create_page_process(
  p_id     => 9695736270914154 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_flow_step_id => 101,
  p_process_sequence=> 10,
  p_process_point=> 'BEFORE_HEADER',
  p_process_type=> 'PLSQL',
  p_process_name=> 'Get Username Cookie',
  p_process_sql_clob => p, 
  p_process_error_message=> '',
  p_process_success_message=> '',
  p_process_is_stateful_y_n=>'N',
  p_required_patch=>null + wwv_flow_api.g_id_offset,
  p_process_comment=>'');
end;
null;
 
end;
/

 
begin
 
---------------------------------------
-- ...updatable report columns for page 101
--
 
begin
 
null;
end;
null;
 
end;
/

prompt  ...lists
--
--application/shared_components/navigation/breadcrumbs
prompt  ...breadcrumbs
--
 
begin
 
wwv_flow_api.create_menu (
  p_id=> 3897055398817528 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'TEST_CASE_BREADCRUMB');
 
wwv_flow_api.create_menu_option (
  p_id=>3897430941819928 + wwv_flow_api.g_id_offset,
  p_menu_id=>3897055398817528 + wwv_flow_api.g_id_offset,
  p_parent_id=>0,
  p_option_sequence=>10,
  p_short_name=>'TEST CASE',
  p_long_name=>'',
  p_link=>'f?p=&APP_ID.:11:&SESSION.::&DEBUG.:::',
  p_page_id=>11,
  p_also_current_for_pages=> '');
 
wwv_flow_api.create_menu_option (
  p_id=>3897839945822543 + wwv_flow_api.g_id_offset,
  p_menu_id=>3897055398817528 + wwv_flow_api.g_id_offset,
  p_parent_id=>3897430941819928 + wwv_flow_api.g_id_offset,
  p_option_sequence=>10,
  p_short_name=>'TEST CASE STEPS',
  p_long_name=>'',
  p_link=>'f?p=&APP_ID.:11:&SESSION.::&DEBUG.:::',
  p_page_id=>12,
  p_also_current_for_pages=> '');
 
null;
 
end;
/

 
begin
 
wwv_flow_api.create_menu (
  p_id=> 4064948822558724 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'TEST_GROUP_BREADCRUMB');
 
wwv_flow_api.create_menu_option (
  p_id=>4065632331563386 + wwv_flow_api.g_id_offset,
  p_menu_id=>4064948822558724 + wwv_flow_api.g_id_offset,
  p_parent_id=>0,
  p_option_sequence=>10,
  p_short_name=>'TEST_GROUP',
  p_long_name=>'',
  p_link=>'f?p=&APP_ID.:13:&SESSION.::&DEBUG.:::',
  p_page_id=>13,
  p_also_current_for_pages=> '');
 
wwv_flow_api.create_menu_option (
  p_id=>4066042374566375 + wwv_flow_api.g_id_offset,
  p_menu_id=>4064948822558724 + wwv_flow_api.g_id_offset,
  p_parent_id=>4065632331563386 + wwv_flow_api.g_id_offset,
  p_option_sequence=>10,
  p_short_name=>'TEST_GROUP_ITEM',
  p_long_name=>'',
  p_link=>'f?p=&APP_ID.:13:&SESSION.::&DEBUG.:::',
  p_page_id=>14,
  p_also_current_for_pages=> '');
 
null;
 
end;
/

 
begin
 
wwv_flow_api.create_menu (
  p_id=> 8995910159743353 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'TEST_CASE_TO_TEST_CASE_STEP');
 
null;
 
end;
/

 
begin
 
wwv_flow_api.create_menu (
  p_id=> 9695933642914154 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> ' Breadcrumb');
 
wwv_flow_api.create_menu_option (
  p_id=>3901444482982353 + wwv_flow_api.g_id_offset,
  p_menu_id=>9695933642914154 + wwv_flow_api.g_id_offset,
  p_parent_id=>null,
  p_option_sequence=>10,
  p_short_name=>'TEST_CASE',
  p_long_name=>'',
  p_link=>'f?p=&FLOW_ID.:11:&SESSION.',
  p_page_id=>11,
  p_also_current_for_pages=> '');
 
wwv_flow_api.create_menu_option (
  p_id=>4068438414576391 + wwv_flow_api.g_id_offset,
  p_menu_id=>9695933642914154 + wwv_flow_api.g_id_offset,
  p_parent_id=>null,
  p_option_sequence=>10,
  p_short_name=>'TEST_GROUP',
  p_long_name=>'',
  p_link=>'f?p=&FLOW_ID.:13:&SESSION.',
  p_page_id=>13,
  p_also_current_for_pages=> '');
 
wwv_flow_api.create_menu_option (
  p_id=>4069732807579029 + wwv_flow_api.g_id_offset,
  p_menu_id=>9695933642914154 + wwv_flow_api.g_id_offset,
  p_parent_id=>4068438414576391 + wwv_flow_api.g_id_offset,
  p_option_sequence=>10,
  p_short_name=>' > TEST GROUP ITEM',
  p_long_name=>'',
  p_link=>'f?p=&APP_ID.:13:&SESSION.::&DEBUG.:::',
  p_page_id=>14,
  p_also_current_for_pages=> '');
 
null;
 
end;
/

prompt  ...page templates for application: 107
--
--application/shared_components/user_interface/templates/page/popup
prompt  ......Page template 9687546688914124
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'#FORM_CLOSE#</body>'||chr(10)||
'</html>';

c3:=c3||'<table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top"><div class="t2messages">#SUCCESS_MESSAGE##NOTIFICATION_MESSAGE#</div>#BOX_BODY##REGION_POSITION_01##REGION_POSITION_02##REGION_POSITION_04##REGION_POSITION_05##REGION_POSITION_06##REGION_POSITION_07##REGION_POSITION_08#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_template(
  p_id=> 9687546688914124 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'Popup',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>',
  p_current_tab=> '',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_theme_id  => 2,
  p_theme_class_id => 4,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/no_tabs
prompt  ......Page template 9687634566914124
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-0.png"/></td>'||chr(10)||
'<td width="100%" class="t2BottomBarCenter"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-1.png"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-3.png" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<div class="t2footer"><table width';

c2:=c2||'="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="bottom" class="t2user">&APP_USER.</td>'||chr(10)||
'<td valign="bottom" class="t2copy"><!-- Copyright --><span class="t2Customize">#CUSTOMIZE#</span></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></div>'||chr(10)||
'<br />'||chr(10)||
'#REGION_POSITION_05#'||chr(10)||
'#FORM_CLOSE# '||chr(10)||
'</body>'||chr(10)||
'</html>';

c3:=c3||'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top" class="t2Logo">#LOGO##REGION_POSITION_06#</td>'||chr(10)||
'<td valign="top" width="100%">#REGION_POSITION_07#</td>'||chr(10)||
'<td valign="top" align="right">#NAVIGATION_BAR##REGION_POSITION_08#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="" height="70%">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2topbar';

c3:=c3||'01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-0.png" width="7" height="35" /><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="25" height="1" /></td>'||chr(10)||
'<td class="t2topbar05"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-3.png" width="21" height="35" /></td>'||chr(10)||
'<td class="t2topbar05" width="100%"><br /></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topba';

c3:=c3||'r-0-6.png" width="8" height="35" /></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td rowspan="2" valign="top"><br/></td>'||chr(10)||
'<td colspan="3" class="t2breadcrumbholder">#REGION_POSITION_01#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2body" valign="top" width="100%" colspan="3" valign="top" height="100%"><table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top"><div class="t2messages">#GLOBAL_NO';

c3:=c3||'TIFICATION##SUCCESS_MESSAGE##NOTIFICATION_MESSAGE#</div>#BOX_BODY##REGION_POSITION_02##REGION_POSITION_04#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_template(
  p_id=> 9687634566914124 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'No Tabs',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>'||chr(10)||
'',
  p_current_tab=> '',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>'||chr(10)||
'',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_breadcrumb_def_reg_pos => 'REGION_POSITION_01',
  p_theme_id  => 2,
  p_theme_class_id => 3,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/one_level_tabs
prompt  ......Page template 9687725257914124
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-0.png"/></td>'||chr(10)||
'<td width="100%" class="t2BottomBarCenter"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-1.png"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-3.png" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<div class="t2footer"><table width';

c2:=c2||'="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="bottom" class="t2user">&APP_USER.</td>'||chr(10)||
'<td valign="bottom" class="t2copy"><!-- Copyright --><span class="t2Customize">#CUSTOMIZE#</span></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></div>'||chr(10)||
'<br />'||chr(10)||
'#REGION_POSITION_05#'||chr(10)||
'#FORM_CLOSE# '||chr(10)||
'</body>'||chr(10)||
'</html>';

c3:=c3||'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top" class="t2Logo">#LOGO##REGION_POSITION_06#</td>'||chr(10)||
'<td valign="top" width="100%">#REGION_POSITION_07#</td>'||chr(10)||
'<td valign="top" align="right">#NAVIGATION_BAR##REGION_POSITION_08#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%"><br /></td>'||chr(10)||
'<td';

c3:=c3||' class="t2tabholder" valign="bottom"><table width="100%" cellpadding="0" cellspacing="0" border="0" summary=""><tr><td><br /></td>#TAB_CELLS#</tr></table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" height="70%" cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2topbar01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-0.png" width="7" height="35" /><img alt="" src="#IMAGE';

c3:=c3||'_PREFIX#themes/theme_2/1px_trans.gif" width="25" height="1" /></td>'||chr(10)||
'<td class="t2topbar05"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-3.png" width="21" height="35" /></td>'||chr(10)||
'<td class="t2topbar05" width="100%"><br /></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-6.png" width="8" height="35" /></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td rowspan="2" valign="top"><br/></td>'||chr(10)||
'<td colspan="3" class';

c3:=c3||'="t2breadcrumbholder" height="20">#REGION_POSITION_01#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2body" valign="top" width="100%" colspan="3" height="100%"><table summary="" cellpadding="0" width="100%" height="70%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top"><div class="t2messages">#GLOBAL_NOTIFICATION##SUCCESS_MESSAGE##NOTIFICATION_MESSAGE#</div>#BOX_BODY##REGION_POSITION_02##REGION_POSITIO';

c3:=c3||'N_04#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_template(
  p_id=> 9687725257914124 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'One Level Tabs',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>'||chr(10)||
'',
  p_current_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_left.png" border="0" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOn">#TAB_LABEL##TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_right.png" border="0" alt="" /></td>',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_left.png" border="0" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOff"><a href="#TAB_LINK#">#TAB_LABEL#</a>#TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_right.png" border="0" alt="" /></td>',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>'||chr(10)||
'',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_breadcrumb_def_reg_pos => 'REGION_POSITION_01',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '19');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/no_tabs_with_side_bar
prompt  ......Page template 9687852870914124
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-0.png"/></td>'||chr(10)||
'<td width="100%" class="t2BottomBarCenter"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-1.png"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-3.png" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<div class="t2footer"><table width';

c2:=c2||'="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="bottom" class="t2user">&APP_USER.</td>'||chr(10)||
'<td valign="bottom" class="t2copy"><!-- Copyright --><span class="t2Customize">#CUSTOMIZE#</span></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></div>'||chr(10)||
'<br />'||chr(10)||
'#REGION_POSITION_05#'||chr(10)||
'#FORM_CLOSE# '||chr(10)||
'</body>'||chr(10)||
'</html>';

c3:=c3||'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top" class="t2Logo">#LOGO##REGION_POSITION_06#</td>'||chr(10)||
'<td valign="top" width="100%">#REGION_POSITION_07#</td>'||chr(10)||
'<td valign="top" align="right">#NAVIGATION_BAR##REGION_POSITION_08#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="" height="70%">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2topbar';

c3:=c3||'01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-0.png" width="7" height="35" /><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="25" height="1" /></td>'||chr(10)||
'<td class="t2topbar05"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-3.png" width="21" height="35" /></td>'||chr(10)||
'<td class="t2topbar05" width="100%"><br /></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topba';

c3:=c3||'r-0-6.png" width="8" height="35" /></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td rowspan="2" class="t2sidebar" valign="top">#REGION_POSITION_02#</td>'||chr(10)||
'<td colspan="3" class="t2breadcrumbholder" height="20">#REGION_POSITION_01#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2body" valign="top" width="100%" colspan="3" height="100%"><table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top"><d';

c3:=c3||'iv class="t2messages">#GLOBAL_NOTIFICATION##SUCCESS_MESSAGE##NOTIFICATION_MESSAGE#</div>#BOX_BODY##REGION_POSITION_04#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_template(
  p_id=> 9687852870914124 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'No Tabs with Side Bar',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>'||chr(10)||
'',
  p_current_tab=> '',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_sidebar_def_reg_pos => 'REGION_POSITION_02',
  p_breadcrumb_def_reg_pos => 'REGION_POSITION_01',
  p_theme_id  => 2,
  p_theme_class_id => 17,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/two_level_tabs
prompt  ......Page template 9687933748914126
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-0.png"/></td>'||chr(10)||
'<td width="100%" class="t2BottomBarCenter"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-1.png"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-3.png" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<div class="t2footer"><table width';

c2:=c2||'="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="bottom" class="t2user">&APP_USER.</td>'||chr(10)||
'<td valign="bottom" class="t2copy"><!-- Copyright --><span class="t2Customize">#CUSTOMIZE#</span></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></div>'||chr(10)||
'<br />'||chr(10)||
'#REGION_POSITION_05#'||chr(10)||
'#FORM_CLOSE# '||chr(10)||
'</body>'||chr(10)||
'</html>';

c3:=c3||'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top" class="t2Logo">#LOGO##REGION_POSITION_06#</td>'||chr(10)||
'<td valign="top" width="100%">#REGION_POSITION_07#</td>'||chr(10)||
'<td valign="top" align="right">#NAVIGATION_BAR##REGION_POSITION_08#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%"><br /></td>'||chr(10)||
'#PA';

c3:=c3||'RENT_TAB_CELLS#'||chr(10)||
'<td width="6"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="6" height="1" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="" height="70%">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2topbar01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-0.png" width="7" height="35" /><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" wi';

c3:=c3||'dth="25" height="1" /></td>'||chr(10)||
'<td class="t2topbar05"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-3.png" width="21" height="35" /></td>'||chr(10)||
'<td class="t2topbar05" width="100%" valign="bottom"><table cellpadding="0" border="0" cellspacing="0" summary=""><tr><td><br /></td>#TAB_CELLS#</tr></table></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-6.png" width="8" height="35" /></td';

c3:=c3||'>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td rowspan="2" class="t2sidebar" valign="top"><br /></td>'||chr(10)||
'<td colspan="3" class="t2breadcrumbholder">#REGION_POSITION_01#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2body" valign="top" width="100%" colspan="3" height="100%"><table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top"><div class="t2messages">#GLOBAL_NOTIFICATION##SUCCESS_MESSAGE##NOTI';

c3:=c3||'FICATION_MESSAGE#</div>#BOX_BODY##REGION_POSITION_02##REGION_POSITION_04#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_template(
  p_id=> 9687933748914126 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'Two Level Tabs',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>',
  p_current_tab=> '<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_on_left.png" alt="" /></td>'||chr(10)||
'<td class="t2subtabcenterOn">#TAB_LABEL##TAB_INLINE_EDIT#</td>'||chr(10)||
'<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_on_right.png" alt="" /></td>',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_off_left.png" alt="" /></td>'||chr(10)||
'<td class="t2subtabcenterOff"><a href="#TAB_LINK#">#TAB_LABEL#</a>#TAB_INLINE_EDIT#</td>'||chr(10)||
'<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_off_right.png" alt="" /></td>',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_left.png" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOn">#TAB_LABEL##TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_right.png" alt="" /></td>'||chr(10)||
'',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_left.png" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOff"><a href="#TAB_LINK#">#TAB_LABEL#</a>#TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_right.png" alt="" /></td>',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_sidebar_def_reg_pos => 'REGION_POSITION_03',
  p_breadcrumb_def_reg_pos => 'REGION_POSITION_01',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/login
prompt  ......Page template 9688054572914126
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'  </body>'||chr(10)||
'</html>'||chr(10)||
'';

c3:=c3||'<div class="t2messages">#NOTIFICATION_MESSAGE#</div>'||chr(10)||
'<table cellpadding="0" border="0" cellspacing="0" summary="" style="margin-top:100px;" align="center">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_';

c3:=c3||'PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2ReportsRegion">'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BOX_BODY##REGION_POSITION_01##REGION_POSITI';

c3:=c3||'ON_02##REGION_POSITION_03##REGION_POSITION_04##REGION_POSITION_05##REGION_POSITION_06##REGION_POSITION_07##REGION_POSITION_08#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><i';

c3:=c3||'mg alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'#FORM_CLOSE#'||chr(10)||
''||chr(10)||
'';

wwv_flow_api.create_template(
  p_id=> 9688054572914126 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'Login',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>'||chr(10)||
'',
  p_current_tab=> '',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>'||chr(10)||
''||chr(10)||
'',
  p_navigation_bar=> '',
  p_navbar_entry=> '',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_theme_id  => 2,
  p_theme_class_id => 6,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '18');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/printer_friendly
prompt  ......Page template 9688149371914126
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'#FORM_CLOSE#'||chr(10)||
'</body>'||chr(10)||
'</html>';

c3:=c3||'<table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top">#LOGO##REGION_POSITION_06#</td>'||chr(10)||
'<td width="100%" valign="top">#REGION_POSITION_07#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_08#</td>'||chr(10)||
'</table>'||chr(10)||
'<table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top">'||chr(10)||
'<div style="border:1px solid black;">#SUCCESS_MESSAG';

c3:=c3||'E##NOTIFICATION_MESSAGE#</div>'||chr(10)||
'#BOX_BODY##REGION_POSITION_04#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'#REGION_POSITION_05#';

wwv_flow_api.create_template(
  p_id=> 9688149371914126 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'Printer Friendly',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>'||chr(10)||
'',
  p_current_tab=> '',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_theme_id  => 2,
  p_theme_class_id => 5,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '3');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/two_level_tabs_with_side_bar
prompt  ......Page template 9688225756914126
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-0.png"/></td>'||chr(10)||
'<td width="100%" class="t2BottomBarCenter"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-1.png"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-3.png" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<div class="t2footer"><table width';

c2:=c2||'="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="bottom" class="t2user">&APP_USER.</td>'||chr(10)||
'<td valign="bottom" class="t2copy"><!-- Copyright --><span class="t2Customize">#CUSTOMIZE#</span></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></div>'||chr(10)||
'<br />'||chr(10)||
'#FORM_CLOSE# '||chr(10)||
'</body>'||chr(10)||
'</html>';

c3:=c3||'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top" class="t2Logo">#LOGO##REGION_POSITION_06#</td>'||chr(10)||
'<td valign="top" width="100%">#REGION_POSITION_07#</td>'||chr(10)||
'<td valign="top" align="right">#NAVIGATION_BAR##REGION_POSITION_08#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%"><br /></td>'||chr(10)||
'<td';

c3:=c3||' class="t2tabholder" valign="bottom"><table width="100%" cellpadding="0" cellspacing="0" border="0" summary=""><tr>#PARENT_TAB_CELLS#</tr></table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="" height="70%">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2topbar01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-0.png" width="7" height="35" /><img alt="" src="#IMAGE_PREFIX#';

c3:=c3||'themes/theme_2/1px_trans.gif" width="25" height="1" /></td>'||chr(10)||
'<td  class="t2topbar05"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-3.png" width="21" height="35" /></td>'||chr(10)||
'<td class="t2topbar05" width="100%" valign="bottom"><table cellpadding="0" border="0" cellspacing="0" summary=""><tr><td><br /></td>#TAB_CELLS#</tr></table></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-6.';

c3:=c3||'png" width="8" height="35" /></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td rowspan="2" class="t2sidebar" valign="top">#REGION_POSITION_02#</td>'||chr(10)||
'<td colspan="3" class="t2breadcrumbholder">#REGION_POSITION_01#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td align="left" class="t2body" valign="top" width="100%" colspan="3" height="100%"><table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top"><div cl';

c3:=c3||'ass="t2messages">#GLOBAL_NOTIFICATION##SUCCESS_MESSAGE##NOTIFICATION_MESSAGE#</div>#BOX_BODY##REGION_POSITION_04##REGION_POSITION_05##REGION_POSITION_06##REGION_POSITION_07##REGION_POSITION_08#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_template(
  p_id=> 9688225756914126 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'Two Level Tabs with Side Bar',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>'||chr(10)||
'',
  p_current_tab=> '<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_on_left.png" alt="" /></td>'||chr(10)||
'<td class="t2subtabcenterOn">#TAB_LABEL##TAB_INLINE_EDIT#</td>'||chr(10)||
'<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_on_right.png" alt="" /></td>',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_off_left.png" alt="" /></td>'||chr(10)||
'<td class="t2subtabcenterOff"><a href="#TAB_LINK#">#TAB_LABEL#</a>#TAB_INLINE_EDIT#</td>'||chr(10)||
'<td valign="bottom"><img src="#IMAGE_PREFIX#themes/theme_2/subtab_off_right.png" alt="" /></td>',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_left.png" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOn">#TAB_LABEL##TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_right.png" alt="" /></td>'||chr(10)||
'',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_left.png" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOff"><a href="#TAB_LINK#">#TAB_LABEL#</a>#TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_right.png" alt="" /></td>',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>'||chr(10)||
'',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_sidebar_def_reg_pos => 'REGION_POSITION_02',
  p_breadcrumb_def_reg_pos => 'REGION_POSITION_01',
  p_theme_id  => 2,
  p_theme_class_id => 18,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '');
end;
 
null;
 
end;
/

--application/shared_components/user_interface/templates/page/one_level_tabs_with_side_bar
prompt  ......Page template 9688334692914126
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'<html lang="&BROWSER_LANGUAGE.">'||chr(10)||
'<head>'||chr(10)||
'<title>#TITLE#</title>'||chr(10)||
'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V3.css" type="text/css" />'||chr(10)||
'#HEAD#'||chr(10)||
'</head>'||chr(10)||
'<body #ONLOAD#>#FORM_OPEN#';

c2:=c2||'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-0.png"/></td>'||chr(10)||
'<td width="100%" class="t2BottomBarCenter"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-1.png"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/bottom_bar-0-3.png" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<div class="t2footer"><table width';

c2:=c2||'="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="bottom" class="t2user">&APP_USER.</td>'||chr(10)||
'<td valign="bottom" class="t2copy"><!-- Copyright --><span class="t2Customize">#CUSTOMIZE#</span></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></div>'||chr(10)||
'<br />'||chr(10)||
'#REGION_POSITION_05#'||chr(10)||
'#FORM_CLOSE# '||chr(10)||
'</body>'||chr(10)||
'</html>';

c3:=c3||'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top" class="t2Logo">#LOGO##REGION_POSITION_06#</td>'||chr(10)||
'<td valign="top" width="100%">#REGION_POSITION_07#</td>'||chr(10)||
'<td valign="top" align="right">#NAVIGATION_BAR##REGION_POSITION_08#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%"><br /></td>'||chr(10)||
'<td';

c3:=c3||' class="t2tabholder" valign="bottom"><table width="100%" cellpadding="0" cellspacing="0" border="0" summary=""><tr><td><br /></td>#TAB_CELLS#</tr></table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'<table width="100%" cellpadding="0" border="0" cellspacing="0" summary="" height="70%">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2topbar01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-0.png" width="7" height="35" /><img alt="" src="#IMAGE';

c3:=c3||'_PREFIX#themes/theme_2/1px_trans.gif" width="25" height="1" /></td>'||chr(10)||
'<td class="t2topbar05"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-3.png" width="21" height="35" /></td>'||chr(10)||
'<td class="t2topbar05" width="100%"><br /></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/topbar-0-6.png" width="8" height="35" /></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td rowspan="2" class="t2sidebar" valign="top">#REGION_POSITI';

c3:=c3||'ON_02#</td>'||chr(10)||
'<td colspan="3" class="t2breadcrumbholder" height="20">#REGION_POSITION_01#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2body" valign="top" width="100%" colspan="3" height="100%"><table summary="" cellpadding="0" width="100%" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td width="100%" valign="top"><div class="t2messages">#GLOBAL_NOTIFICATION##SUCCESS_MESSAGE##NOTIFICATION_MESSAGE#</div>#BOX_BODY##REGION_POSITIO';

c3:=c3||'N_04#</td>'||chr(10)||
'<td valign="top">#REGION_POSITION_03#<br /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_template(
  p_id=> 9688334692914126 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'One Level Tabs with Side Bar',
  p_body_title=> '',
  p_header_template=> c1,
  p_box=> c3,
  p_footer_template=> c2,
  p_success_message=> '<div class="t2success">#SUCCESS_MESSAGE#</div>'||chr(10)||
'',
  p_current_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_left.png" border="0" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOn">#TAB_LABEL##TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_on_right.png" border="0" alt="" /></td>',
  p_current_tab_font_attr=> '',
  p_non_current_tab=> '<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_left.png" border="0" alt="" /></td>'||chr(10)||
'<td class="t2tabcenterOff"><a href="#TAB_LINK#">#TAB_LABEL#</a>#TAB_INLINE_EDIT#</td>'||chr(10)||
'<td><img src="#IMAGE_PREFIX#themes/theme_2/tab_off_right.png" border="0" alt="" /></td>',
  p_non_current_tab_font_attr => '',
  p_top_current_tab=> '',
  p_top_current_tab_font_attr => '',
  p_top_non_curr_tab=> '',
  p_top_non_curr_tab_font_attr=> '',
  p_current_image_tab=> '',
  p_non_current_image_tab=> '',
  p_notification_message=> '<div class="t2notification">#MESSAGE#</div>'||chr(10)||
'',
  p_navigation_bar=> '<div class="t2NavigationBar">#BAR_BODY#</div>',
  p_navbar_entry=> '<a href="#LINK#" class="t2navbar">#TEXT#</a>',
  p_app_tab_before_tabs=>'',
  p_app_tab_current_tab=>'',
  p_app_tab_non_current_tab=>'',
  p_app_tab_after_tabs=>'',
  p_region_table_cattributes=> ' summary="" cellpadding="0" border="0" cellspacing="0" width="100%"',
  p_sidebar_def_reg_pos => 'REGION_POSITION_02',
  p_breadcrumb_def_reg_pos => 'REGION_POSITION_01',
  p_theme_id  => 2,
  p_theme_class_id => 16,
  p_required_patch   => null + wwv_flow_api.g_id_offset,
  p_translate_this_template => 'N',
  p_template_comment => '');
end;
 
null;
 
end;
/

prompt  ...button templates
--
--application/shared_components/user_interface/templates/button/button_alternative_1
prompt  ......Button Template 9688446068914126
declare
  t varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
t:=t||'<table class="t2ButtonAlternative1" cellspacing="0" cellpadding="0" border="0"  summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2L"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_alt1_left.gif" alt="" /></a></td>'||chr(10)||
'<td class="t2C"><a href="#LINK#">#LABEL#</a></td>'||chr(10)||
'<td class="t2R"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_alt1_right.gif" alt="" /></a></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_button_templates (
  p_id=>9688446068914126 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_template=>t,
  p_template_name=> 'Button, Alternative 1',
  p_translate_this_template => 'N',
  p_theme_id  => 2,
  p_theme_class_id => 4,
  p_template_comment       => '');
end;
/
--application/shared_components/user_interface/templates/button/button_alternative_3
prompt  ......Button Template 9688539538914126
declare
  t varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
t:=t||'<table class="t2ButtonAlternative3" cellspacing="0" cellpadding="0" border="0"  summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2L"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_alt3_left.gif" alt="" /></a></td>'||chr(10)||
'<td class="t2C"><a href="#LINK#">#LABEL#</a></td>'||chr(10)||
'<td class="t2R"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_alt3_right.gif" alt="" /></a></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_button_templates (
  p_id=>9688539538914126 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_template=>t,
  p_template_name=> 'Button, Alternative 3',
  p_translate_this_template => 'N',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_template_comment       => '');
end;
/
--application/shared_components/user_interface/templates/button/button
prompt  ......Button Template 9688636459914126
declare
  t varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
t:=t||'<table class="t2Button" cellspacing="0" cellpadding="0" border="0"  summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2L"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_left.gif" alt="" /></a></td>'||chr(10)||
'<td class="t2C"><a href="#LINK#">#LABEL#</a></td>'||chr(10)||
'<td class="t2R"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_right.gif" alt="" /></a></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_button_templates (
  p_id=>9688636459914126 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_template=>t,
  p_template_name=> 'Button',
  p_translate_this_template => 'N',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_template_comment       => '');
end;
/
--application/shared_components/user_interface/templates/button/button_alternative_2
prompt  ......Button Template 9688748977914126
declare
  t varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
t:=t||'<table class="t2ButtonAlternative2" cellspacing="0" cellpadding="0" border="0"  summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2L"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_alt2_left.gif" alt="" /></a></td>'||chr(10)||
'<td class="t2C"><a href="#LINK#">#LABEL#</a></td>'||chr(10)||
'<td class="t2R"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/theme_2/button_alt2_right.gif" alt="" /></a></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

wwv_flow_api.create_button_templates (
  p_id=>9688748977914126 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_template=>t,
  p_template_name=> 'Button, Alternative 2',
  p_translate_this_template => 'N',
  p_theme_id  => 2,
  p_theme_class_id => 5,
  p_template_comment       => '');
end;
/
---------------------------------------
prompt  ...region templates
--
--application/shared_components/user_interface/templates/region/breadcrumb_region
prompt  ......region template 9688830199914126
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<div class="t2BreadcrumbRegion"  id="#REGION_ID#">#BODY#</div>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9688830199914126 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Breadcrumb Region',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 6,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9688830199914126 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/wizard_region
prompt  ......region template 9688926422914126
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2WizardRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELET';

t:=t||'E##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><i';

t:=t||'mg alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9688926422914126 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Wizard Region',
  p_plug_table_bgcolor     => '#f7f7e7',
  p_theme_id  => 2,
  p_theme_class_id => 12,
  p_plug_heading_bgcolor => '#f7f7e7',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9688926422914126 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/chart_region
prompt  ......region template 9689047344914126
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2ChartRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELETE';

t:=t||'##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2Body" valign="top" colspan="2">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt=';

t:=t||'"" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689047344914126 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Chart Region',
  p_plug_table_bgcolor     => '#FFFFFF',
  p_theme_id  => 2,
  p_theme_class_id => 30,
  p_plug_heading_bgcolor => '#FFFFFF',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689047344914126 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/form_region
prompt  ......region template 9689133428914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2FormRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELETE#';

t:=t||'#EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BODY#<img src="#IMAGE_PREFIX#/themes/theme_2/1px_trans.gif" height="1" width="582" style="display:block;" alt="" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt=""';

t:=t||' src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689133428914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Form Region',
  p_plug_table_bgcolor     => '#FFFFFF',
  p_theme_id  => 2,
  p_theme_class_id => 8,
  p_plug_heading_bgcolor => '#FFFFFF',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689133428914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/sidebar_region
prompt  ......region template 9689233237914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2SidebarRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td clas';

t:=t||'s="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width';

t:=t||'="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689233237914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Sidebar Region',
  p_plug_table_bgcolor     => '#F7F7E7',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_plug_heading_bgcolor => '#F7F7E7',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689233237914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/hide_and_show_region
prompt  ......region template 9689336685914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2HideandShowRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#<a style="margin-left:5px;" href="javascript:hideShow(''region#REGION_SEQUENCE_ID#'',''shIMG#R';

t:=t||'EGION_SEQUENCE_ID#'',''#IMAGE_PREFIX#themes/theme_2/showhide_hidden.gif'',''#IMAGE_PREFIX#themes/theme_2/showhide_show.gif'');" class="t2HideandShowRegionLink"><img src="#IMAGE_PREFIX#themes/theme_2/showhide_hidden.gif" '||chr(10)||
'  id="shIMG#REGION_SEQUENCE_ID#" alt="" /></a></td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##';

t:=t||'COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2"><div class="t2Hide" id="region#REGION_SEQUENCE_ID#">#BODY#</div></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="';

t:=t||'t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689336685914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Hide and Show Region',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689336685914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/wizard_region_with_icon
prompt  ......region template 9689446796914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2WizardRegionwithIcon">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEX';

t:=t||'T##DELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2"><table summary="" cellpadding="0" cellspacing="0" border="0">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top"><img src="#IMAGE_PREFIX#themes/theme_2/wizard_icon.gif" alt=""/></td>'||chr(10)||
'<td width="100%" valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt=""';

t:=t||' src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>';

t:=t||''||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689446796914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Wizard Region with Icon',
  p_plug_table_bgcolor     => '#f7f7e7',
  p_theme_id  => 2,
  p_theme_class_id => 20,
  p_plug_heading_bgcolor => '#f7f7e7',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689446796914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/navigation_region
prompt  ......region template 9689541665914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<div class="t2NavigationRegion" id="#REGION_ID#">#BODY#</div>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689541665914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Navigation Region',
  p_plug_table_bgcolor     => '#F7F7E7',
  p_theme_id  => 2,
  p_theme_class_id => 5,
  p_plug_heading_bgcolor => '#F7F7E7',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689541665914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/button_region_with_title
prompt  ......region template 9689639546914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2ButtonRegionwithTitle">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2ButtonHolder" valign="top">#CLOSE#&nbsp;&nbsp;&nbsp;#PREV';

t:=t||'IOUS##NEXT##DELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#<img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" style="display:block;" width="582" height="1" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" wi';

t:=t||'dth="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>#BODY#';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689639546914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Button Region with Title',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 4,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689639546914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/button_region_without_title
prompt  ......region template 9689735579914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="600" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" heig';

t:=t||'ht="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2ButtonRegionwithoutTitle">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2ButtonHolder" valign="top">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COP';

t:=t||'Y##HELP#<img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" style="display:block;" width="582" height="1" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt=""';

t:=t||' src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>#BODY#';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689735579914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Button Region without Title',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 17,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689735579914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/reports_region
prompt  ......region template 9689839472914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2ReportsRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELE';

t:=t||'TE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><';

t:=t||'img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689839472914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Reports Region',
  p_plug_table_bgcolor     => '#FFFFFF',
  p_theme_id  => 2,
  p_theme_class_id => 9,
  p_plug_heading_bgcolor => '#FFFFFF',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689839472914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/borderless_region
prompt  ......region template 9689928989914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2BorderlessRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##D';

t:=t||'ELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21';

t:=t||'"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9689928989914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Borderless Region',
  p_plug_table_bgcolor     => '#FFFFFF',
  p_theme_id  => 2,
  p_theme_class_id => 7,
  p_plug_heading_bgcolor => '#FFFFFF',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9689928989914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/report_list
prompt  ......region template 9690040370914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody">'||chr(10)||
''||chr(10)||
'<table border="0" cellspacing="0" cellpadding="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2GCCHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2GCCHeader" align="right" valign="bottom"><br /></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2GCCBody" colspan="2" val';

t:=t||'ign="top"><table cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top"><img src="#IMAGE_PREFIX#themes/theme_2/report.gif" /></td>'||chr(10)||
'<td valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
''||chr(10)||
'</td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/l';

t:=t||'eft_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690040370914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Report List',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 29,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690040370914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/reports_region_alternative_1
prompt  ......region template 9690132158914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/grey-region-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2greyround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/grey-region-0-3.png" width="9"';

t:=t||' height="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2greyround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2greyroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2ReportsRegionAlternative1">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp';

t:=t||';#PREVIOUS##NEXT##DELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2greyround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/grey-region-2-0.png" width="9" height="9"/></td>'||chr(10)||
'';

t:=t||'<td class="t2greyround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/grey-region-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690132158914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Reports Region, Alternative 1',
  p_plug_table_bgcolor     => '#FFFFFF',
  p_theme_id  => 2,
  p_theme_class_id => 10,
  p_plug_heading_bgcolor => '#FFFFFF',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690132158914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/sidebar_region_alternative_1
prompt  ......region template 9690237635914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/grey-region-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2greyround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/grey-region-0-3.png" width="9"';

t:=t||' height="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2greyround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2greyroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2SidebarRegionAlternative1">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'';

t:=t||'</table></td>'||chr(10)||
'<td class="t2greyround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/grey-region-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2greyround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/them';

t:=t||'e_2/grey-region-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690237635914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Sidebar Region, Alternative 1',
  p_plug_table_bgcolor     => '#F7F7E7',
  p_theme_id  => 2,
  p_theme_class_id => 3,
  p_plug_heading_bgcolor => '#F7F7E7',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690237635914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/navigation_region_alternative_1
prompt  ......region template 9690342620914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table border="0" cellspacing="0" cellpadding="0" summary="" class="t2NavigationRegionAlternative1" id="#REGION_ID#">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690342620914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Navigation Region, Alternative 1',
  p_plug_table_bgcolor     => '#F7F7E7',
  p_theme_id  => 2,
  p_theme_class_id => 16,
  p_plug_heading_bgcolor => '#F7F7E7',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690342620914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/region_without_title
prompt  ......region template 9690425840914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2RegionwithoutTitle">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HE';

t:=t||'LP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" wid';

t:=t||'th="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690425840914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Region without Title',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 11,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690425840914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/reports_region_100_width
prompt  ......region template 9690526601914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" width="100%" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" wid';

t:=t||'th="9" height="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody" width="100%"><table border="0" cellspacing="0" cellpadding="0" summary="" width="100%" class="t2ReportsRegion100Width">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom"';

t:=t||'>#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DELETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="';

t:=t||'9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690526601914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Reports Region 100% Width',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 13,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => 'Outline, tab-title, 100%');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690526601914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/bracketed_region
prompt  ......region template 9690640278914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2BracketedRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2ButtonHolder" valign="bottom">#CLOSE#&nbsp;&nbsp;&nbsp;#PREVIOUS##NEXT##DE';

t:=t||'LETE##EDIT##CHANGE##CREATE##CREATE2##EXPAND##COPY##HELP#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top" colspan="2">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"';

t:=t||'><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690640278914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Bracketed Region',
  p_plug_table_bgcolor     => '#FFFFFF',
  p_theme_id  => 2,
  p_theme_class_id => 18,
  p_plug_heading_bgcolor => '#FFFFFF',
  p_plug_font_size => '-1',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690640278914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/chart_list
prompt  ......region template 9690726207914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2GCCHeader" valign="bottom">#TITLE#</td>'||chr(10)||
'<td class="t2GCCHeader" align="right" valign="bottom"><br /></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2GCCBody" colspan="2" valig';

t:=t||'n="top"><table cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td valign="top"><img src="#IMAGE_PREFIX#themes/theme_2/chart.gif" alt="" /></td>'||chr(10)||
'<td valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2';

t:=t||'/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690726207914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Chart List',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 29,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690726207914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/region/region_without_buttons_and_titles
prompt  ......region template 9690846171914128
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_length number := 1;
begin
t:=t||'<table cellpadding="0" border="0" cellspacing="0" summary="" id="#REGION_ID#" class="htmldbRegion">'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-0.png" width="9" height="11"/></td>'||chr(10)||
'<td class="t2stdround01"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-0-3.png" width="9" height';

t:=t||'="11"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2stdround10"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td class="t2stroundbody"><table border="0" cellspacing="0" cellpadding="0" summary="" class="t2RegionwithoutButtonsandTitles">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2RegionBody" valign="top">#BODY#</td>'||chr(10)||
'</tr>'||chr(10)||
'</table></td>'||chr(10)||
'<td class="t2stdround13"><img alt="" src="#IMAGE_PREFIX#the';

t:=t||'mes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-0.png" width="9" height="9"/></td>'||chr(10)||
'<td class="t2stdround21"><img alt="" src="#IMAGE_PREFIX#themes/theme_2/1px_trans.gif" width="1" height="1"/></td>'||chr(10)||
'<td><img alt="" src="#IMAGE_PREFIX#themes/theme_2/left_nav-2-3.png" width="9" height="9"/></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2 := null;
wwv_flow_api.create_plug_template (
  p_id       => 9690846171914128 + wwv_flow_api.g_id_offset,
  p_flow_id  => wwv_flow.g_flow_id,
  p_template => t,
  p_page_plug_template_name=> 'Region without Buttons and Titles',
  p_plug_table_bgcolor     => '',
  p_theme_id  => 2,
  p_theme_class_id => 19,
  p_plug_heading_bgcolor => '',
  p_plug_font_size => '',
  p_translate_this_template => 'N',
  p_template_comment       => '');
end;
null;
 
end;
/

 
begin
 
declare
    t2 varchar2(32767) := null;
begin
t2 := null;
wwv_flow_api.set_plug_template_tab_attr (
  p_id=> 9690846171914128 + wwv_flow_api.g_id_offset,
  p_form_table_attr=> t2 );
exception when others then null;
end;
null;
 
end;
/

prompt  ...List Templates
--
--application/shared_components/user_interface/templates/list/dhtml_list_image_with_sublist
prompt  ......list template 9690952576914129
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<div class="dhtmlMenuItem"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/generic_list.gif" #IMAGE_ATTR# /></a><img src="#IMAGE_PREFIX#themes/generic_nochild.gif" width="22" height="75" alt="" /><a href="#LINK#" class="dhtmlBottom">#TEXT#</a></div>';

t2:=t2||'<div class="dhtmlMenuItem"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/generic_list.gif" #IMAGE_ATTR# alt="" /></a><img src="#IMAGE_PREFIX#themes/generic_nochild.gif" width="22" height="75" alt="" /><a href="#LINK#" class="dhtmlBottom">#TEXT#</a></div>';

t3:=t3||'<li class="dhtmlMenuSep"><img src="#IMAGE_PREFIX#themes/theme_13/1px_trans.gif"  width="1" height="1" alt=""  class="dhtmlMenuSep" /></li>';

t4:=t4||'<li><a href="#LINK#" class="dhtmlSubMenuN" onmouseover="dhtml_CloseAllSubMenusL(this)">#TEXT#</a></li>';

t5:=t5||'<div class="dhtmlMenuItem"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/generic_list.gif" #IMAGE_ATTR# alt="" /></a><img src="#IMAGE_PREFIX#themes/generic_open.gif" width="22" height="75" class="dhtmlMenu" onclick="app_AppMenuMultiOpenBottom(this,''#LIST_ITEM_ID#'',false)" alt="" /><a href="#LINK#" class="dhtmlBottom">#TEXT#</a></div>';

t6:=t6||'<div class="dhtmlMenuItem"><a href="#LINK#"><img src="#IMAGE_PREFIX#themes/generic_list.gif" #IMAGE_ATTR# alt="" /></a><img src="#IMAGE_PREFIX#themes/generic_open.gif" width="22" height="75" class="dhtmlMenu" onclick="app_AppMenuMultiOpenBottom(this,''#LIST_ITEM_ID#'',false)" alt="" /><a href="#LINK#" class="dhtmlBottom">#TEXT#</a></div>';

t7:=t7||'<li class="dhtmlSubMenuS"><a href="#LINK#" class="dhtmlSubMenuS" onmouseover="dhtml_MenuOpen(this,''#LIST_ITEM_ID#'',true,''Left'')"><span style="float:left;">#TEXT#</span><img class="t13MIMG" src="#IMAGE_PREFIX#menu_open_right2.gif" alt="" /></a></li>';

t8:=t8||'<li class="dhtmlSubMenuS"><a href="#LINK#" class="dhtmlSubMenuS" onmouseover="dhtml_MenuOpen(this,''#LIST_ITEM_ID#'',true,''Left'')"><span style="float:left;">#TEXT#</span><img class="t13MIMG" src="#IMAGE_PREFIX#menu_open_right2.gif" alt="" /></a></li>';

wwv_flow_api.create_list_template (
  p_id=>9690952576914129 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'DHTML List (Image) with Sublist',
  p_theme_id  => 2,
  p_theme_class_id => 21,
  p_list_template_before_rows=>'<div class="dhtmlMenuLG">',
  p_list_template_after_rows=>'</div><br style="clear:both;"/><br style="clear:both;"/>',
  p_before_sub_list=>'<ul id="#PARENT_LIST_ITEM_ID#" htmldb:listlevel="#LEVEL#" class="dhtmlSubMenu2" style="display:none;"><li class="dhtmlSubMenuP" onmouseover="dhtml_CloseAllSubMenusL(this)">#PARENT_TEXT#</li>',
  p_after_sub_list=>'</ul>',
  p_sub_list_item_current=> t3,
  p_sub_list_item_noncurrent=> t4,
  p_item_templ_curr_w_child=> t5,
  p_item_templ_noncurr_w_child=> t6,
  p_sub_templ_curr_w_child=> t7,
  p_sub_templ_noncurr_w_child=> t8,
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/tree_list
prompt  ......list template 9691035850914129
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<li><a href="#LINK#">#TEXT#</a></li>';

t2:=t2||'<li><a href="#LINK#">#TEXT#</a></li>';

t3:=t3||'<li><a href="#LINK#">#TEXT#</a></li>';

t4:=t4||'<li><a href="#LINK#">#TEXT#</a></li>';

t5:=t5||'<li><a href="#LINK#">#TEXT#</a></li>';

t6:=t6||'<li><a href="#LINK#">#TEXT#</a></li>';

t7:=t7||'<li><a href="#LINK#">#TEXT#</a></li>';

t8:=t8||'<li><a href="#LINK#">#TEXT#</a></li>';

wwv_flow_api.create_list_template (
  p_id=>9691035850914129 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Tree List',
  p_theme_id  => 2,
  p_theme_class_id => 23,
  p_list_template_before_rows=>'<ul class="htmlTree">',
  p_list_template_after_rows=>'</ul><br style="clear:both;"/><br style="clear:both;"/>',
  p_before_sub_list=>'<ul id="#PARENT_LIST_ITEM_ID#" htmldb:listlevel="#LEVEL#">',
  p_after_sub_list=>'</ul>',
  p_sub_list_item_current=> t3,
  p_sub_list_item_noncurrent=> t4,
  p_item_templ_curr_w_child=> t5,
  p_item_templ_noncurr_w_child=> t6,
  p_sub_templ_curr_w_child=> t7,
  p_sub_templ_noncurr_w_child=> t8,
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/dhtml_tree
prompt  ......list template 9691124013914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<li><img src="#IMAGE_PREFIX#themes/theme_13/node.gif" align="middle" alt="" /><a href="#LINK#">#TEXT#</a></li>';

t2:=t2||'<li><img src="#IMAGE_PREFIX#themes/theme_13/node.gif" align="middle"  alt="" /><a href="#LINK#">#TEXT#</a></li>';

t3:=t3||'<li><img src="#IMAGE_PREFIX#themes/theme_13/node.gif" align="middle"  alt="" /><a href="#LINK#">#TEXT#</a></li>';

t4:=t4||'<li><img src="#IMAGE_PREFIX#themes/theme_13/node.gif"  align="middle" alt="" /><a href="#LINK#">#TEXT#</a></li>';

t5:=t5||'<li><img src="#IMAGE_PREFIX#themes/theme_13/plus.gif" align="middle"  onclick="htmldb_ToggleWithImage(this,''#LIST_ITEM_ID#'')" class="pseudoButtonInactive" alt="" /><a href="#LINK#">#TEXT#</a></li>';

t6:=t6||'<li><img src="#IMAGE_PREFIX#themes/theme_13/plus.gif" align="middle"  onclick="htmldb_ToggleWithImage(this,''#LIST_ITEM_ID#'')" class="pseudoButtonInactive" alt="" /><a href="#LINK#">#TEXT#</a></li>';

t7:=t7||'<li><img src="#IMAGE_PREFIX#themes/theme_13/plus.gif" onclick="htmldb_ToggleWithImage(this,''#LIST_ITEM_ID#'')" align="middle" class="pseudoButtonInactive" alt="" /><a href="#LINK#">#TEXT#</a></li>';

t8:=t8||'<li><img src="#IMAGE_PREFIX#themes/theme_13/plus.gif" onclick="htmldb_ToggleWithImage(this,''#LIST_ITEM_ID#'')" align="middle" class="pseudoButtonInactive" alt="" /><a href="#LINK#">#TEXT#</a></li>';

wwv_flow_api.create_list_template (
  p_id=>9691124013914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'DHTML Tree',
  p_theme_id  => 2,
  p_theme_class_id => 22,
  p_list_template_before_rows=>'<ul class="dhtmlTree">',
  p_list_template_after_rows=>'</ul><br style="clear:both;"/><br style="clear:both;"/>',
  p_before_sub_list=>'<ul id="#PARENT_LIST_ITEM_ID#" htmldb:listlevel="#LEVEL#" style="display:none;" class="dhtmlTree">',
  p_after_sub_list=>'</ul>',
  p_sub_list_item_current=> t3,
  p_sub_list_item_noncurrent=> t4,
  p_item_templ_curr_w_child=> t5,
  p_item_templ_noncurr_w_child=> t6,
  p_sub_templ_curr_w_child=> t7,
  p_sub_templ_noncurr_w_child=> t8,
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/dhtml_menu_with_sublist
prompt  ......list template 9691225626914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<li class="dhtmlMenuItem"><a href="#LINK#">#TEXT#</a></li>';

t2:=t2||'<li class="dhtmlMenuItem"><a href="#LINK#">#TEXT#</a></li>';

t3:=t3||'<li class="dhtmlMenuSep2"><img src="#IMAGE_PREFIX#themes/theme_13/1px_trans.gif"  width="1" height="1" alt="" class="dhtmlMenuSep2" /></li>';

t4:=t4||'<li><a href="#LINK#" class="dhtmlSubMenuN" onmouseover="dhtml_CloseAllSubMenusL(this)">#TEXT#</a></li>';

t5:=t5||'<li class="dhtmlMenuItem1"><a href="#LINK#">#TEXT#</a><img src="#IMAGE_PREFIX#themes/theme_13/menu_small.gif" alt="Expand" onclick="app_AppMenuMultiOpenBottom2(this,''#LIST_ITEM_ID#'',false)" /></li>';

t6:=t6||'<li class="dhtmlMenuItem1"><a href="#LINK#">#TEXT#</a><img src="#IMAGE_PREFIX#themes/theme_13/menu_small.gif" alt="Expand" onclick="app_AppMenuMultiOpenBottom2(this,''#LIST_ITEM_ID#'',false)" /></li>';

t7:=t7||'<li class="dhtmlSubMenuS"><a href="#LINK#" class="dhtmlSubMenuS" onmouseover="dhtml_MenuOpen(this,''#LIST_ITEM_ID#'',true,''Left'')"><span style="float:left;">#TEXT#</span><img class="t13MIMG" src="#IMAGE_PREFIX#menu_open_right2.gif" alt="" /></a></li>';

t8:=t8||'<li class="dhtmlSubMenuS"><a href="#LINK#" class="dhtmlSubMenuS" onmouseover="dhtml_MenuOpen(this,''#LIST_ITEM_ID#'',true,''Left'')"><span style="float:left;">#TEXT#</span><img class="t13MIMG" src="#IMAGE_PREFIX#menu_open_right2.gif" alt="" /></a></li>';

wwv_flow_api.create_list_template (
  p_id=>9691225626914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'DHTML Menu with Sublist',
  p_theme_id  => 2,
  p_theme_class_id => 20,
  p_list_template_before_rows=>'<ul class="dhtmlMenuLG2">',
  p_list_template_after_rows=>'</ul><br style="clear:both;"/><br style="clear:both;"/>',
  p_before_sub_list=>'<ul id="#PARENT_LIST_ITEM_ID#" htmldb:listlevel="#LEVEL#" class="dhtmlSubMenu2" style="display:none;">',
  p_after_sub_list=>'</ul>',
  p_sub_list_item_current=> t3,
  p_sub_list_item_noncurrent=> t4,
  p_item_templ_curr_w_child=> t5,
  p_item_templ_noncurr_w_child=> t6,
  p_sub_templ_curr_w_child=> t7,
  p_sub_templ_noncurr_w_child=> t8,
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/vertical_images_list
prompt  ......list template 9691326900914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<tr><td class="t2current"><img src="#IMAGE_PREFIX##IMAGE#" #IMAGE_ATTR# alt="" /><br />#TEXT#</td></tr>';

t2:=t2||'<tr><td><a href="#LINK#"><img src="#IMAGE_PREFIX##IMAGE#" #IMAGE_ATTR# alt="" /><br />#TEXT#</a></td></tr>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9691326900914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Vertical Images List',
  p_theme_id  => 2,
  p_theme_class_id => 5,
  p_list_template_before_rows=>'<table cellpadding="0" cellspacing="0" border="0" summary="0" class="t2VerticalImagesList">',
  p_list_template_after_rows=>'</table>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/tabbed_navigation_list
prompt  ......list template 9691448064914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<li><a class="t2current" href="#LINK#">#TEXT#</a></li>';

t2:=t2||'<li><a href="#LINK#">#TEXT#</a></li>'||chr(10)||
'';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9691448064914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Tabbed Navigation List',
  p_theme_id  => 2,
  p_theme_class_id => 7,
  p_list_template_before_rows=>'<ul class="t2TabbedNavigationList">',
  p_list_template_after_rows=>'</ul>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/wizard_progress_list
prompt  ......list template 9691546480914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<tr><td class="t2current">#TEXT#<br /><img src="#IMAGE_PREFIX#themes/theme_2/arrow_down.gif" alt="Down" /></td></tr>';

t2:=t2||'<tr><td>#TEXT#<br /><img src="#IMAGE_PREFIX#themes/theme_2/arrow_down.gif" alt="Down" /></td></tr>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9691546480914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Wizard Progress List',
  p_theme_id  => 2,
  p_theme_class_id => 17,
  p_list_template_before_rows=>'<table cellpadding="0" border="0" cellspacing="0" summary="" class="t2WizardProgressList">',
  p_list_template_after_rows=>'<tr><td>&DONE.</td></tr>'||chr(10)||
'</table>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/vertical_ordered_list
prompt  ......list template 9691645025914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<li class="t2current"><a href="#LINK#">#TEXT#</a></li>';

t2:=t2||'<li><a href="#LINK#">#TEXT#</a></li>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9691645025914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Vertical Ordered List',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_list_template_before_rows=>'<ol class="t2VerticalOrderedList">',
  p_list_template_after_rows=>'</ol>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/vertical_unordered_list_without_bullet
prompt  ......list template 9691752641914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<li class="t2current">#TEXT#</li>';

t2:=t2||'<li><a href="#LINK#">#TEXT#</a></li>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9691752641914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Vertical Unordered List without Bullet',
  p_theme_id  => 2,
  p_theme_class_id => 18,
  p_list_template_before_rows=>'<ul class="t2VerticalUnorderedListwithoutBullet">',
  p_list_template_after_rows=>'</ul>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/vertical_unordered_list_with_bullet
prompt  ......list template 9691831233914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<li class="t2current">#TEXT#</li>'||chr(10)||
'';

t2:=t2||'<li><a href="#LINK#">#TEXT#</a></li>'||chr(10)||
'';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9691831233914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Vertical Unordered List with Bullet',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_list_template_before_rows=>'<ul class="t2VerticalUnorderedListwithBullet">',
  p_list_template_after_rows=>'</ul>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/horizontal_images_with_label_list
prompt  ......list template 9691954897914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<td class="t2current"><img src="#IMAGE_PREFIX##IMAGE#" #IMAGE_ATTR# alt="" /><br />#TEXT#</td>';

t2:=t2||'<td><a href="#LINK#"><img src="#IMAGE_PREFIX##IMAGE#" #IMAGE_ATTR# alt="" /></a><br /><a href="#LINK#">#TEXT#</a></td>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9691954897914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Horizontal Images with Label List',
  p_theme_id  => 2,
  p_theme_class_id => 4,
  p_list_template_before_rows=>'<table cellpadding="0" border="0" cellspacing="0" summary="" class="t2HorizontalImageswithLabelList"><tr>',
  p_list_template_after_rows=>'</tr></table>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/horizontal_links_list
prompt  ......list template 9692055484914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<a href="#LINK#" class="t2current">#TEXT#</a>';

t2:=t2||'<a href="#LINK#">#TEXT#</a>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9692055484914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Horizontal Links List',
  p_theme_id  => 2,
  p_theme_class_id => 3,
  p_list_template_before_rows=>'<div class="t2HorizontalLinksList">',
  p_list_template_after_rows=>'</div>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/button_list
prompt  ......list template 9692135941914131
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<table class="t2ButtonList" cellspacing="0" cellpadding="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2L"><img src="#IMAGE_PREFIX#/themes/theme_2/button_list_left.png" alt="" /></td>'||chr(10)||
'<td class="t2C"><a href="#LINK#">#TEXT#</a></td>'||chr(10)||
'<td class="t2R"><img src="#IMAGE_PREFIX#/themes/theme_2/button_list_right.png" alt="" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t2:=t2||'<table class="t2ButtonList" cellspacing="0" cellpadding="0" border="0" summary="">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2L"><img src="#IMAGE_PREFIX#/themes/theme_2/button_list_left.png" alt="" /></td>'||chr(10)||
'<td class="t2NC"><a href="#LINK#">#TEXT#</a></td>'||chr(10)||
'<td class="t2R"><img src="#IMAGE_PREFIX#/themes/theme_2/button_list_right.png" alt="" /></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9692135941914131 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Button List',
  p_theme_id  => 2,
  p_theme_class_id => 6,
  p_list_template_before_rows=>'<div class="t2ButtonList">',
  p_list_template_after_rows=>'</div>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/list/vertical_sidebar_list
prompt  ......list template 9692227823914132
 
begin
 
declare
  t varchar2(32767) := null;
  t2 varchar2(32767) := null;
  t3 varchar2(32767) := null;
  t4 varchar2(32767) := null;
  t5 varchar2(32767) := null;
  t6 varchar2(32767) := null;
  t7 varchar2(32767) := null;
  t8 varchar2(32767) := null;
  l_clob clob;
  l_clob2 clob;
  l_clob3 clob;
  l_clob4 clob;
  l_clob5 clob;
  l_clob6 clob;
  l_clob7 clob;
  l_clob8 clob;
  l_length number := 1;
begin
t:=t||'<a href="#LINK#" class="t2navcurrent">#TEXT#</a>';

t2:=t2||'<a href="#LINK#" class="t2nav">#TEXT#</a>';

t3 := null;
t4 := null;
t5 := null;
t6 := null;
t7 := null;
t8 := null;
wwv_flow_api.create_list_template (
  p_id=>9692227823914132 + wwv_flow_api.g_id_offset,
  p_flow_id=>wwv_flow.g_flow_id,
  p_list_template_current=>t,
  p_list_template_noncurrent=> t2,
  p_list_template_name=>'Vertical Sidebar List',
  p_theme_id  => 2,
  p_theme_class_id => 19,
  p_list_template_before_rows=>'<div class="t2VerticalSidebarList">',
  p_list_template_after_rows=>'</div>',
  p_translate_this_template => 'N',
  p_list_template_comment=>'');
end;
null;
 
end;
/

prompt  ...report templates
--
--application/shared_components/user_interface/templates/report/horizontal_border
prompt  ......report template 9692347767914132
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  c4 varchar2(32767) := null;
begin
c1:=c1||'<td#ALIGNMENT# headers="#COLUMN_HEADER_NAME#" class="t2data">#COLUMN_VALUE#</td>';

c2 := null;
c3 := null;
c4 := null;
wwv_flow_api.create_row_template (
  p_id=> 9692347767914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_row_template_name=> 'Horizontal Border',
  p_row_template1=> c1,
  p_row_template_condition1=> '',
  p_row_template2=> c2,
  p_row_template_condition2=> '',
  p_row_template3=> c3,
  p_row_template_condition3=> '',
  p_row_template4=> c4,
  p_row_template_condition4=> '',
  p_row_template_before_rows=>'<table cellpadding="0" border="0" cellspacing="0" summary="">#TOP_PAGINATION#'||chr(10)||
'<tr>'||chr(10)||
'<td><table cellpadding="0" border="0" cellspacing="0" summary="" class="t2HorizontalBorder">',
  p_row_template_after_rows =>'</table><div class="t2CVS">#EXTERNAL_LINK##CSV_LINK#</div></td>'||chr(10)||
'</tr>#PAGINATION#'||chr(10)||
'</table>',
  p_row_template_table_attr =>'OMIT',
  p_row_template_type =>'GENERIC_COLUMNS',
  p_column_heading_template =>'<th class="t2header"  id="#COLUMN_HEADER_NAME#" #ALIGNMENT#>#COLUMN_HEADER#</th>',
  p_row_template_display_cond1=>'0',
  p_row_template_display_cond2=>'0',
  p_row_template_display_cond3=>'0',
  p_row_template_display_cond4=>'0',
  p_next_page_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_page_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS#</a>',
  p_next_set_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT_SET#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_set_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS_SET#</a>',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_translate_this_template => 'N',
  p_row_template_comment=> '');
end;
null;
 
end;
/

 
begin
 
begin
wwv_flow_api.create_row_template_patch (
  p_id => 9692347767914132 + wwv_flow_api.g_id_offset,
  p_row_template_before_first =>'<tr #HIGHLIGHT_ROW#>',
  p_row_template_after_last =>'</tr>');
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/report/standard_ppr
prompt  ......report template 9692434075914132
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  c4 varchar2(32767) := null;
begin
c1:=c1||'<td#ALIGNMENT# headers="#COLUMN_HEADER_NAME#" class="t2data">#COLUMN_VALUE#</td>';

c2 := null;
c3 := null;
c4 := null;
wwv_flow_api.create_row_template (
  p_id=> 9692434075914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_row_template_name=> 'Standard (PPR)',
  p_row_template1=> c1,
  p_row_template_condition1=> '',
  p_row_template2=> c2,
  p_row_template_condition2=> '',
  p_row_template3=> c3,
  p_row_template_condition3=> '',
  p_row_template4=> c4,
  p_row_template_condition4=> '',
  p_row_template_before_rows=>'<div id="report#REGION_ID#"><htmldb:#REGION_ID#><table cellpadding="0" border="0" cellspacing="0" summary="">#TOP_PAGINATION#'||chr(10)||
'<tr>'||chr(10)||
'<td><table cellpadding="0" border="0" cellspacing="0" summary="" class="t2standard">',
  p_row_template_after_rows =>'</table><div class="t2CVS">#EXTERNAL_LINK##CSV_LINK#</div></td>'||chr(10)||
'</tr>'||chr(10)||
'#PAGINATION#'||chr(10)||
'</table><script language=JavaScript type=text/javascript>'||chr(10)||
'<!--'||chr(10)||
'init_htmlPPRReport(''#REGION_ID#'');'||chr(10)||
''||chr(10)||
'//-->'||chr(10)||
'</script>'||chr(10)||
'</htmldb:#REGION_ID#>'||chr(10)||
'</div>',
  p_row_template_table_attr =>'OMIT',
  p_row_template_type =>'GENERIC_COLUMNS',
  p_column_heading_template =>'<th#ALIGNMENT# id="#COLUMN_HEADER_NAME#" class="t2header">#COLUMN_HEADER#</th>',
  p_row_template_display_cond1=>'0',
  p_row_template_display_cond2=>'0',
  p_row_template_display_cond3=>'0',
  p_row_template_display_cond4=>'0',
  p_next_page_template=>'<a href="javascript:html_PPR_Report_Page(this,''#REGION_ID#'',''#LINK#'')" class="t2pagination">#PAGINATION_NEXT#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_page_template=>'<a href="javascript:html_PPR_Report_Page(this,''#REGION_ID#'',''#LINK#'')" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS#</a>',
  p_next_set_template=>'<a href="javascript:html_PPR_Report_Page(this,''#REGION_ID#'',''#LINK#'')" class="t2pagination">#PAGINATION_NEXT_SET#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_set_template=>'<a href="javascript:html_PPR_Report_Page(this,''#REGION_ID#'',''#LINK#'')" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS_SET#</a>',
  p_theme_id  => 2,
  p_theme_class_id => 7,
  p_translate_this_template => 'N',
  p_row_template_comment=> '');
end;
null;
 
end;
/

 
begin
 
begin
wwv_flow_api.create_row_template_patch (
  p_id => 9692434075914132 + wwv_flow_api.g_id_offset,
  p_row_template_before_first =>'<tr #HIGHLIGHT_ROW#>',
  p_row_template_after_last =>'</tr>');
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/report/standard
prompt  ......report template 9692549154914132
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  c4 varchar2(32767) := null;
begin
c1:=c1||'<td#ALIGNMENT# headers="#COLUMN_HEADER_NAME#" class="t2data">#COLUMN_VALUE#</td>';

c2 := null;
c3 := null;
c4 := null;
wwv_flow_api.create_row_template (
  p_id=> 9692549154914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_row_template_name=> 'Standard',
  p_row_template1=> c1,
  p_row_template_condition1=> '',
  p_row_template2=> c2,
  p_row_template_condition2=> '',
  p_row_template3=> c3,
  p_row_template_condition3=> '',
  p_row_template4=> c4,
  p_row_template_condition4=> '',
  p_row_template_before_rows=>'<table cellpadding="0" border="0" cellspacing="0" summary="">#TOP_PAGINATION#'||chr(10)||
'<tr>'||chr(10)||
'<td><table cellpadding="0" border="0" cellspacing="0" summary="" class="t2standard">',
  p_row_template_after_rows =>'</table><div class="t2CVS">#EXTERNAL_LINK##CSV_LINK#</div></td>'||chr(10)||
'</tr>'||chr(10)||
'#PAGINATION#'||chr(10)||
'</table>',
  p_row_template_table_attr =>'OMIT',
  p_row_template_type =>'GENERIC_COLUMNS',
  p_column_heading_template =>'<th#ALIGNMENT# id="#COLUMN_HEADER_NAME#" class="t2header">#COLUMN_HEADER#</th>',
  p_row_template_display_cond1=>'0',
  p_row_template_display_cond2=>'0',
  p_row_template_display_cond3=>'0',
  p_row_template_display_cond4=>'0',
  p_next_page_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_page_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS#</a>',
  p_next_set_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT_SET#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_set_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS_SET#</a>',
  p_theme_id  => 2,
  p_theme_class_id => 4,
  p_translate_this_template => 'N',
  p_row_template_comment=> '');
end;
null;
 
end;
/

 
begin
 
begin
wwv_flow_api.create_row_template_patch (
  p_id => 9692549154914132 + wwv_flow_api.g_id_offset,
  p_row_template_before_first =>'<tr #HIGHLIGHT_ROW#>',
  p_row_template_after_last =>'</tr>');
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/report/value_attribute_pairs
prompt  ......report template 9692634220914132
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  c4 varchar2(32767) := null;
begin
c1:=c1||'<tr><th class="t2header">#COLUMN_HEADER#</th><td class="t2data">#COLUMN_VALUE#</td></tr>';

c2 := null;
c3 := null;
c4 := null;
wwv_flow_api.create_row_template (
  p_id=> 9692634220914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_row_template_name=> 'Value Attribute Pairs',
  p_row_template1=> c1,
  p_row_template_condition1=> '',
  p_row_template2=> c2,
  p_row_template_condition2=> '',
  p_row_template3=> c3,
  p_row_template_condition3=> '',
  p_row_template4=> c4,
  p_row_template_condition4=> '',
  p_row_template_before_rows=>'<table cellpadding="0" border="0" cellspacing="0" summary="">'||chr(10)||
'#TOP_PAGINATION#'||chr(10)||
'<tr><td><table cellpadding="0" cellspacing="0" border="0" summary="" class="t2ValueAttributePairs">',
  p_row_template_after_rows =>'</table><div class="t2CVS">#EXTERNAL_LINK##CSV_LINK#</div></td></tr>#PAGINATION#</table>',
  p_row_template_table_attr =>'OMIT',
  p_row_template_type =>'GENERIC_COLUMNS',
  p_column_heading_template =>'',
  p_row_template_display_cond1=>'0',
  p_row_template_display_cond2=>'0',
  p_row_template_display_cond3=>'NOT_CONDITIONAL',
  p_row_template_display_cond4=>'0',
  p_next_page_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_page_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS#</a>',
  p_next_set_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT_SET#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_set_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS_SET#</a>',
  p_theme_id  => 2,
  p_theme_class_id => 6,
  p_translate_this_template => 'N',
  p_row_template_comment=> '');
end;
null;
 
end;
/

 
begin
 
begin
wwv_flow_api.create_row_template_patch (
  p_id => 9692634220914132 + wwv_flow_api.g_id_offset,
  p_row_template_before_first =>'OMIT',
  p_row_template_after_last =>'<tr><td colspan="2" class="t2seperate"><hr /></td></tr>');
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/report/standard_alternating_row_colors
prompt  ......report template 9692751229914132
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  c4 varchar2(32767) := null;
begin
c1:=c1||'<td#ALIGNMENT# headers="#COLUMN_HEADER_NAME#" class="t2data">#COLUMN_VALUE#</td>';

c2:=c2||'<td#ALIGNMENT# headers="#COLUMN_HEADER_NAME#" class="t2dataalt">#COLUMN_VALUE#</td>';

c3 := null;
c4 := null;
wwv_flow_api.create_row_template (
  p_id=> 9692751229914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_row_template_name=> 'Standard, Alternating Row Colors',
  p_row_template1=> c1,
  p_row_template_condition1=> '',
  p_row_template2=> c2,
  p_row_template_condition2=> '',
  p_row_template3=> c3,
  p_row_template_condition3=> '',
  p_row_template4=> c4,
  p_row_template_condition4=> '',
  p_row_template_before_rows=>'<table cellpadding="0" border="0" cellspacing="0" summary="">#TOP_PAGINATION#'||chr(10)||
'<tr>'||chr(10)||
'<td><table cellpadding="0" border="0" cellspacing="0" summary="" class="t2standardalternatingrowcolors">',
  p_row_template_after_rows =>'</table><div class="t2CVS">#EXTERNAL_LINK##CSV_LINK#</div></td>'||chr(10)||
'</tr>#PAGINATION#'||chr(10)||
'</table>',
  p_row_template_table_attr =>'OMIT',
  p_row_template_type =>'GENERIC_COLUMNS',
  p_column_heading_template =>'<th#ALIGNMENT# id="#COLUMN_HEADER_NAME#" class="t2header">#COLUMN_HEADER#</th>',
  p_row_template_display_cond1=>'EVEN_ROW_NUMBERS',
  p_row_template_display_cond2=>'ODD_ROW_NUMBERS',
  p_row_template_display_cond3=>'0',
  p_row_template_display_cond4=>'EVEN_ROW_NUMBERS',
  p_next_page_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_page_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS#</a>',
  p_next_set_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT_SET#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_set_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS_SET#</a>',
  p_theme_id  => 2,
  p_theme_class_id => 5,
  p_translate_this_template => 'N',
  p_row_template_comment=> '');
end;
null;
 
end;
/

 
begin
 
begin
wwv_flow_api.create_row_template_patch (
  p_id => 9692751229914132 + wwv_flow_api.g_id_offset,
  p_row_template_before_first =>'<tr #HIGHLIGHT_ROW#>',
  p_row_template_after_last =>'</tr>');
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/report/borderless
prompt  ......report template 9692835615914132
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  c4 varchar2(32767) := null;
begin
c1:=c1||'<td#ALIGNMENT# headers="#COLUMN_HEADER_NAME#" class="t2data">#COLUMN_VALUE#</td>';

c2 := null;
c3 := null;
c4 := null;
wwv_flow_api.create_row_template (
  p_id=> 9692835615914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_row_template_name=> 'Borderless',
  p_row_template1=> c1,
  p_row_template_condition1=> '',
  p_row_template2=> c2,
  p_row_template_condition2=> '',
  p_row_template3=> c3,
  p_row_template_condition3=> '',
  p_row_template4=> c4,
  p_row_template_condition4=> '',
  p_row_template_before_rows=>'<table cellpadding="0" border="0" cellspacing="0" summary="">#TOP_PAGINATION#'||chr(10)||
'<tr>'||chr(10)||
'<td><table cellpadding="0" border="0" cellspacing="0" summary="" class="t2borderless">'||chr(10)||
''||chr(10)||
'',
  p_row_template_after_rows =>'</table><div class="t2CVS">#EXTERNAL_LINK##CSV_LINK#</div></td>'||chr(10)||
'</tr>'||chr(10)||
'#PAGINATION#'||chr(10)||
'</table>',
  p_row_template_table_attr =>'OMIT',
  p_row_template_type =>'GENERIC_COLUMNS',
  p_column_heading_template =>'<th#ALIGNMENT# id="#COLUMN_HEADER_NAME#" class="t2header">#COLUMN_HEADER#</th>',
  p_row_template_display_cond1=>'0',
  p_row_template_display_cond2=>'0',
  p_row_template_display_cond3=>'0',
  p_row_template_display_cond4=>'0',
  p_next_page_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>'||chr(10)||
'',
  p_previous_page_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS#</a>',
  p_next_set_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT_SET#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_set_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS_SET#</a>',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_translate_this_template => 'N',
  p_row_template_comment=> '');
end;
null;
 
end;
/

 
begin
 
begin
wwv_flow_api.create_row_template_patch (
  p_id => 9692835615914132 + wwv_flow_api.g_id_offset,
  p_row_template_before_first =>'<tr #HIGHLIGHT_ROW#>',
  p_row_template_after_last =>'</tr>');
exception when others then null;
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/report/one_column_unordered_list
prompt  ......report template 9692938422914132
 
begin
 
declare
  c1 varchar2(32767) := null;
  c2 varchar2(32767) := null;
  c3 varchar2(32767) := null;
  c4 varchar2(32767) := null;
begin
c1:=c1||'<li>#COLUMN_VALUE#</li>';

c2 := null;
c3 := null;
c4 := null;
wwv_flow_api.create_row_template (
  p_id=> 9692938422914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_row_template_name=> 'One Column Unordered List',
  p_row_template1=> c1,
  p_row_template_condition1=> '',
  p_row_template2=> c2,
  p_row_template_condition2=> '',
  p_row_template3=> c3,
  p_row_template_condition3=> '',
  p_row_template4=> c4,
  p_row_template_condition4=> '',
  p_row_template_before_rows=>'<table cellpadding="0" cellspacing="0" border="0" summary="">'||chr(10)||
'#TOP_PAGINATION#'||chr(10)||
'<tr><td><ul class="t2OneColumnUnorderedList">',
  p_row_template_after_rows =>'</ul><div class="t2CVS">#EXTERNAL_LINK##CSV_LINK#</div></td></tr>#PAGINATION#'||chr(10)||
'</table>',
  p_row_template_table_attr =>'OMIT',
  p_row_template_type =>'GENERIC_COLUMNS',
  p_column_heading_template =>'',
  p_row_template_display_cond1=>'0',
  p_row_template_display_cond2=>'0',
  p_row_template_display_cond3=>'NOT_CONDITIONAL',
  p_row_template_display_cond4=>'0',
  p_next_page_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_page_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS#</a>',
  p_next_set_template=>'<a href="#LINK#" class="t2pagination">#PAGINATION_NEXT_SET#<img src="#IMAGE_PREFIX#themes/theme_2/paginate_next.gif" alt="Next"></a>',
  p_previous_set_template=>'<a href="#LINK#" class="t2pagination"><img src="#IMAGE_PREFIX#themes/theme_2/paginate_prev.gif" alt="Previous">#PAGINATION_PREVIOUS_SET#</a>',
  p_theme_id  => 2,
  p_theme_class_id => 3,
  p_translate_this_template => 'N',
  p_row_template_comment=> '');
end;
null;
 
end;
/

 
begin
 
begin
wwv_flow_api.create_row_template_patch (
  p_id => 9692938422914132 + wwv_flow_api.g_id_offset,
  p_row_template_before_first =>'OMIT',
  p_row_template_after_last =>'OMIT');
exception when others then null;
end;
null;
 
end;
/

prompt  ...label templates
--
--application/shared_components/user_interface/templates/label/optional
prompt  ......label template 9693052936914132
 
begin
 
begin
wwv_flow_api.create_field_template (
  p_id=> 9693052936914132 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_template_name=>'Optional',
  p_template_body1=>'<label for="#CURRENT_ITEM_NAME#" tabindex="999"><span class="t2Optional">',
  p_template_body2=>'</span></label>',
  p_on_error_before_label=>'<div class="t2InlineError">',
  p_on_error_after_label=>'<br/>#ERROR_MESSAGE#</div>',
  p_theme_id  => 2,
  p_theme_class_id => 3,
  p_translate_this_template=> 'N',
  p_template_comment=> '');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/label/optional_with_help
prompt  ......label template 9693128915914134
 
begin
 
begin
wwv_flow_api.create_field_template (
  p_id=> 9693128915914134 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_template_name=>'Optional with Help',
  p_template_body1=>'<label for="#CURRENT_ITEM_NAME#" tabindex="999"><a class="t2OptionalwithHelp" href="javascript:popupFieldHelp(''#CURRENT_ITEM_ID#'',''&SESSION.'')" tabindex="999">',
  p_template_body2=>'</a></label>',
  p_on_error_before_label=>'<div class="t2InlineError">',
  p_on_error_after_label=>'<br/>#ERROR_MESSAGE#</div>',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_translate_this_template=> 'N',
  p_template_comment=> '');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/label/no_label
prompt  ......label template 9693237156914134
 
begin
 
begin
wwv_flow_api.create_field_template (
  p_id=> 9693237156914134 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_template_name=>'No Label',
  p_template_body1=>'<span class="t2NoLabel">',
  p_template_body2=>'</span>',
  p_on_error_before_label=>'<div class="t2InlineError">',
  p_on_error_after_label=>'<br/>#ERROR_MESSAGE#</div>',
  p_theme_id  => 2,
  p_theme_class_id => 13,
  p_translate_this_template=> 'N',
  p_template_comment=> '');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/label/required_with_help
prompt  ......label template 9693346138914134
 
begin
 
begin
wwv_flow_api.create_field_template (
  p_id=> 9693346138914134 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_template_name=>'Required with help',
  p_template_body1=>'<label for="#CURRENT_ITEM_NAME#"><img src="#IMAGE_PREFIX#themes/theme_2/required.gif" alt="Required Field Icon" style="margin-right:5px;"/><a class="t2RequiredwithHelp" href="javascript:popupFieldHelp(''#CURRENT_ITEM_ID#'',''&SESSION.'')">',
  p_template_body2=>'</a></label>',
  p_on_error_before_label=>'<div class="t2InlineError">',
  p_on_error_after_label=>'<br/>#ERROR_MESSAGE#</div>',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_translate_this_template=> 'N',
  p_template_comment=> '');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/label/required
prompt  ......label template 9693439408914134
 
begin
 
begin
wwv_flow_api.create_field_template (
  p_id=> 9693439408914134 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_template_name=>'Required',
  p_template_body1=>'<label for="#CURRENT_ITEM_NAME#" tabindex="999"><span class="t2Required"><img src="#IMAGE_PREFIX#themes/theme_2/required.gif" alt="Required Field Icon" style="margin-right:5px;" />',
  p_template_body2=>'</span></label>',
  p_on_error_before_label=>'<div class="t2InlineError">',
  p_on_error_after_label=>'<br/>#ERROR_MESSAGE#</div>',
  p_theme_id  => 2,
  p_theme_class_id => 4,
  p_translate_this_template=> 'N',
  p_template_comment=> '');
end;
null;
 
end;
/

prompt  ...breadcrumb templates
--
--application/shared_components/user_interface/templates/breadcrumb/hierarchical_menu
prompt  ......template 9693537879914134
 
begin
 
begin
wwv_flow_api.create_menu_template (
  p_id=> 9693537879914134 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=>'Hierarchical Menu',
  p_before_first=>'<ul class="t2HierarchicalMenu">',
  p_current_page_option=>'<li class="t2current">#NAME#</li>',
  p_non_current_page_option=>'<li><a href="#LINK#">#NAME#</a></li>',
  p_menu_link_attributes=>'',
  p_between_levels=>'',
  p_after_last=>'</ul>',
  p_max_levels=>11,
  p_start_with_node=>'CHILD_MENU',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_translate_this_template => 'N',
  p_template_comments=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/breadcrumb/breadcrumb_menu
prompt  ......template 9693645199914134
 
begin
 
begin
wwv_flow_api.create_menu_template (
  p_id=> 9693645199914134 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=>'Breadcrumb Menu',
  p_before_first=>'<div class="t2BreadcrumbMenu">',
  p_current_page_option=>'<span class="t2current">#NAME#</span>',
  p_non_current_page_option=>'<a href="#LINK#">#NAME#</a>',
  p_menu_link_attributes=>'',
  p_between_levels=>'&nbsp;&gt;&nbsp;',
  p_after_last=>'</div>',
  p_max_levels=>12,
  p_start_with_node=>'PARENT_TO_LEAF',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_translate_this_template => 'N',
  p_template_comments=>'');
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/popuplov
prompt  ...popup list of values templates
--
prompt  ......template 9694337914914135
 
begin
 
begin
wwv_flow_api.create_popup_lov_template (
  p_id=> 9694337914914135 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_popup_icon=>'#IMAGE_PREFIX#list.gif',
  p_popup_icon_attr=>'width="13" height="13" alt="Popup Lov"',
  p_popup_icon2=>'',
  p_popup_icon_attr2=>'',
  p_page_name=>'winlov',
  p_page_title=>'Search Dialog',
  p_page_html_head=>'<link rel="stylesheet" href="#IMAGE_PREFIX#themes/theme_2/theme_V2.css" type="text/css" />'||chr(10)||
'',
  p_page_body_attr=>'onload="first_field()" style="margin:0;"',
  p_before_field_text=>'<div class="t2PopupHead">',
  p_page_heading_text=>'',
  p_page_footer_text =>'',
  p_filter_width     =>'20',
  p_filter_max_width =>'100',
  p_filter_text_attr =>'',
  p_find_button_text =>'Search',
  p_find_button_image=>'',
  p_find_button_attr =>'',
  p_close_button_text=>'Close',
  p_close_button_image=>'',
  p_close_button_attr=>'',
  p_next_button_text =>'Next >',
  p_next_button_image=>'',
  p_next_button_attr =>'',
  p_prev_button_text =>'< Previous',
  p_prev_button_image=>'',
  p_prev_button_attr =>'',
  p_after_field_text=>'</div>',
  p_scrollbars=>'1',
  p_resizable=>'1',
  p_width =>'400',
  p_height=>'450',
  p_result_row_x_of_y=>'<br /><div style="padding:2px; font-size:8pt;">Row(s) #FIRST_ROW# - #LAST_ROW#</div>',
  p_result_rows_per_pg=>500,
  p_before_result_set=>'<div class="t2PopupBody">',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_translate_this_template => 'N',
  p_after_result_set   =>'</div>');
end;
null;
 
end;
/

prompt  ...calendar templates
--
--application/shared_components/user_interface/templates/calendar/calendar
prompt  ......template 9693737945914134
 
begin
 
begin
wwv_flow_api.create_calendar_template(
  p_id=> 9693737945914134 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_cal_template_name=>'Calendar',
  p_translate_this_template=> 'Y',
  p_day_of_week_format=> '<th class="t2DayOfWeek">#IDAY#</th>',
  p_month_title_format=> '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2CalendarHolder"> '||chr(10)||
' <tr>'||chr(10)||
'   <td class="t2MonthTitle">#IMONTH# #YYYY#</td>'||chr(10)||
' </tr>'||chr(10)||
' <tr>'||chr(10)||
' <td>',
  p_month_open_format=> '<table border="0" cellpadding="0" cellspacing="2" summary="0" class="t2Calendar">',
  p_month_close_format=> '</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'',
  p_day_title_format=> '<div class="t2DayTitle">#DD#</div>',
  p_day_open_format=> '<td class="t2Day" valign="top">',
  p_day_close_format=> '</td>',
  p_today_open_format=> '<td valign="top" class="t2Today">',
  p_weekend_title_format=> '<div class="t2WeekendDayTitle">#DD#</div>',
  p_weekend_open_format => '<td valign="top" class="t2WeekendDay">',
  p_weekend_close_format => '</td>',
  p_nonday_title_format => '<div class="t2NonDayTitle">#DD#</div>',
  p_nonday_open_format => '<td class="t2NonDay" valign="top">',
  p_nonday_close_format => '</td>',
  p_week_title_format => '',
  p_week_open_format => '<tr>',
  p_week_close_format => '</tr> ',
  p_daily_title_format => '<th width="14%" class="calheader">#IDAY#</th>',
  p_daily_open_format => '<tr>',
  p_daily_close_format => '</tr>',
  p_weekly_title_format => '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2WeekCalendarHolder">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2MonthTitle" id="test">#WTITLE#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td>',
  p_weekly_day_of_week_format => '<th class="t2DayOfWeek">#IDAY#<br>#MM#/#DD#</th>',
  p_weekly_month_open_format => '<table border="0" cellpadding="0" cellspacing="2" summary="0" class="t2WeekCalendar">',
  p_weekly_month_close_format => '</table></td></tr></table>',
  p_weekly_day_title_format => '',
  p_weekly_day_open_format => '<td class="t2Day" valign="top">',
  p_weekly_day_close_format => '<br /></td>',
  p_weekly_today_open_format => '<td class="t2Today" valign="top">',
  p_weekly_weekend_title_format => '',
  p_weekly_weekend_open_format => '<td valign="top" class="t2NonDay">',
  p_weekly_weekend_close_format => '<br /></td>',
  p_weekly_time_open_format => '<th class="t2Hour">',
  p_weekly_time_close_format => '<br /></th>',
  p_weekly_time_title_format => '#TIME#',
  p_weekly_hour_open_format => '<tr>',
  p_weekly_hour_close_format => '</tr>',
  p_daily_day_of_week_format => '<th class="t2DayOfWeek">#IDAY# #DD#/#MM#</th>',
  p_daily_month_title_format => '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2DayCalendarHolder"> <tr> <td class="t2MonthTitle">#IMONTH# #DD#, #YYYY#</td> </tr> <tr> <td>'||chr(10)||
'',
  p_daily_month_open_format => '<table border="0" cellpadding="2" cellspacing="2" summary="0" class="t2DayCalendar">',
  p_daily_month_close_format => '</table></td> </tr> </table>',
  p_daily_day_title_format => '',
  p_daily_day_open_format => '<td valign="top" class="t2Day">',
  p_daily_day_close_format => '<br /></td>',
  p_daily_today_open_format => '<td valign="top" class="t2Today">',
  p_daily_time_open_format => '<th class="t2Hour">',
  p_daily_time_close_format => '<br /></th>',
  p_daily_time_title_format => '#TIME#',
  p_daily_hour_open_format => '<tr>',
  p_daily_hour_close_format => '</tr>',
  p_theme_id  => 2,
  p_theme_class_id => 1,
  p_reference_id=> null);
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/calendar/small_calendar
prompt  ......template 9693923380914135
 
begin
 
begin
wwv_flow_api.create_calendar_template(
  p_id=> 9693923380914135 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_cal_template_name=>'Small Calendar',
  p_translate_this_template=> 'Y',
  p_day_of_week_format=> '<th class="t2DayOfWeek" height="12">#DY#</th>',
  p_month_title_format=> '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2SmallCalendarHolder"> '||chr(10)||
' <tr>'||chr(10)||
'   <td class="t2MonthTitle">#IMONTH# #YYYY#</td>'||chr(10)||
' </tr>'||chr(10)||
' <tr>'||chr(10)||
' <td>',
  p_month_open_format=> '<table border="0" cellpadding="0" cellspacing="2" summary="0" class="t2SmallCalendar">',
  p_month_close_format=> '</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'',
  p_day_title_format=> '<div class="t2DayTitle">#DD#</div>',
  p_day_open_format=> '<td class="t2Day" valign="top">',
  p_day_close_format=> '</td>',
  p_today_open_format=> '<td valign="top" class="t2Today">',
  p_weekend_title_format=> '<div class="t2WeekendDayTitle">#DD#</div>',
  p_weekend_open_format => '<td valign="top" class="t2WeekendDay">',
  p_weekend_close_format => '</td>',
  p_nonday_title_format => '<div class="t2NonDayTitle">#DD#</div>',
  p_nonday_open_format => '<td class="t2NonDay" valign="top">',
  p_nonday_close_format => '</td>',
  p_week_title_format => '',
  p_week_open_format => '<tr>',
  p_week_close_format => '</tr> ',
  p_daily_title_format => '<th width="14%" class="calheader">#IDAY#</th>',
  p_daily_open_format => '<tr>',
  p_daily_close_format => '</tr>',
  p_weekly_title_format => '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2SmallWeekCalendarHolder">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2MonthTitle" id="test">#WTITLE#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td>',
  p_weekly_day_of_week_format => '<th class="t2DayOfWeek">#IDAY#<br />#MM#/#DD#</th>',
  p_weekly_month_open_format => '<table border="0" cellpadding="0" cellspacing="2" summary="0" class="t2SmallWeekCalendar">',
  p_weekly_month_close_format => '</table></td></tr></table>',
  p_weekly_day_title_format => '',
  p_weekly_day_open_format => '<td class="t2Day" valign="top">',
  p_weekly_day_close_format => '<br /></td>',
  p_weekly_today_open_format => '<td class="t2Today" valign="top">',
  p_weekly_weekend_title_format => '',
  p_weekly_weekend_open_format => '<td valign="top" class="t2NonDay">',
  p_weekly_weekend_close_format => '<br /></td>',
  p_weekly_time_open_format => '<th class="t2Hour">',
  p_weekly_time_close_format => '<br /></th>',
  p_weekly_time_title_format => '#TIME#',
  p_weekly_hour_open_format => '<tr>',
  p_weekly_hour_close_format => '</tr>',
  p_daily_day_of_week_format => '<th class="t2DayOfWeek">#IDAY# #DD#/#MM#</th>',
  p_daily_month_title_format => '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2SmallDayCalendarHolder"> <tr> <td class="t2MonthTitle">#IMONTH# #DD#, #YYYY#</td> </tr><tr><td>'||chr(10)||
'',
  p_daily_month_open_format => '<table border="0" cellpadding="2" cellspacing="2" summary="0" class="t2SmallDayCalendar">',
  p_daily_month_close_format => '</table></td></tr></table>',
  p_daily_day_title_format => '',
  p_daily_day_open_format => '<td valign="top" class="t2Day">',
  p_daily_day_close_format => '<br /></td>',
  p_daily_today_open_format => '<td valign="top" class="t2Today">',
  p_daily_time_open_format => '<th class="t2Hour">',
  p_daily_time_close_format => '<br /></th>',
  p_daily_time_title_format => '#TIME#',
  p_daily_hour_open_format => '<tr>',
  p_daily_hour_close_format => '</tr>',
  p_theme_id  => 2,
  p_theme_class_id => 3,
  p_reference_id=> null);
end;
null;
 
end;
/

--application/shared_components/user_interface/templates/calendar/calendar_alternative_1
prompt  ......template 9694145970914135
 
begin
 
begin
wwv_flow_api.create_calendar_template(
  p_id=> 9694145970914135 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_cal_template_name=>'Calendar, Alternative 1',
  p_translate_this_template=> 'Y',
  p_day_of_week_format=> '<th class="t2DayOfWeek">#IDAY#</th>',
  p_month_title_format=> '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2CalendarAlternative1Holder"> '||chr(10)||
' <tr>'||chr(10)||
'   <td class="t2MonthTitle">#IMONTH# #YYYY#</td>'||chr(10)||
' </tr>'||chr(10)||
' <tr>'||chr(10)||
' <td>',
  p_month_open_format=> '<table border="0" cellpadding="0" cellspacing="1" summary="0" class="t2CalendarAlternative1">',
  p_month_close_format=> '</table></td>'||chr(10)||
'</tr>'||chr(10)||
'</table>'||chr(10)||
'',
  p_day_title_format=> '<div class="t2DayTitle">#DD#</div>',
  p_day_open_format=> '<td class="t2Day" valign="top">',
  p_day_close_format=> '</td>',
  p_today_open_format=> '<td valign="top" class="t2Today">',
  p_weekend_title_format=> '<div class="t2WeekendDayTitle">#DD#</div>',
  p_weekend_open_format => '<td valign="top" class="t2WeekendDay">',
  p_weekend_close_format => '</td>',
  p_nonday_title_format => '<div class="t2NonDayTitle">#DD#</div>',
  p_nonday_open_format => '<td class="t2NonDay" valign="top">',
  p_nonday_close_format => '</td>',
  p_week_title_format => '',
  p_week_open_format => '<tr>',
  p_week_close_format => '</tr> ',
  p_daily_title_format => '<th width="14%" class="calheader">#IDAY#</th>',
  p_daily_open_format => '<tr>',
  p_daily_close_format => '</tr>',
  p_weekly_title_format => '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2WeekCalendarAlternative1Holder">'||chr(10)||
'<tr>'||chr(10)||
'<td class="t2MonthTitle" id="test">#WTITLE#</td>'||chr(10)||
'</tr>'||chr(10)||
'<tr>'||chr(10)||
'<td>',
  p_weekly_day_of_week_format => '<th class="t2DayOfWeek">#IDAY#<br>#MM#/#DD#</th>',
  p_weekly_month_open_format => '<table border="0" cellpadding="0" cellspacing="1" summary="0" class="t2WeekCalendarAlternative1">',
  p_weekly_month_close_format => '</table></td></tr></table>',
  p_weekly_day_title_format => '',
  p_weekly_day_open_format => '<td class="t2Day" valign="top">',
  p_weekly_day_close_format => '<br /></td>',
  p_weekly_today_open_format => '<td class="t2Today" valign="top">',
  p_weekly_weekend_title_format => '',
  p_weekly_weekend_open_format => '<td valign="top" class="t2NonDay">',
  p_weekly_weekend_close_format => '<br /></td>',
  p_weekly_time_open_format => '<th class="t2Hour">',
  p_weekly_time_close_format => '<br /></th>',
  p_weekly_time_title_format => '#TIME#',
  p_weekly_hour_open_format => '<tr>',
  p_weekly_hour_close_format => '</tr>',
  p_daily_day_of_week_format => '<th class="t2DayOfWeek">#IDAY# #DD#/#MM#</th>',
  p_daily_month_title_format => '<table cellspacing="0" cellpadding="0" border="0" summary="" class="t2DayCalendarAlternative1Holder"> <tr><td class="t2MonthTitle">#IMONTH# #DD#, #YYYY#</td></tr><tr><td>'||chr(10)||
'',
  p_daily_month_open_format => '<table border="0" cellpadding="2" cellspacing="1" summary="0" class="t2DayCalendarAlternative1">',
  p_daily_month_close_format => '</table></td> </tr> </table>',
  p_daily_day_title_format => '',
  p_daily_day_open_format => '<td valign="top" class="t2Day">',
  p_daily_day_close_format => '<br /></td>',
  p_daily_today_open_format => '<td valign="top" class="t2Today">',
  p_daily_time_open_format => '<th class="t2Hour">',
  p_daily_time_close_format => '<br /></th>',
  p_daily_time_title_format => '#TIME#',
  p_daily_hour_open_format => '<tr>',
  p_daily_hour_close_format => '</tr>',
  p_theme_id  => 2,
  p_theme_class_id => 2,
  p_reference_id=> null);
end;
null;
 
end;
/

prompt  ...application themes
--
prompt  ......theme 9694424796914137
--application/shared_components/user_interface/themes/blue_and_tan
begin
wwv_flow_api.create_theme (
  p_id =>9694424796914137 + wwv_flow_api.g_id_offset,
  p_flow_id =>wwv_flow.g_flow_id,
  p_theme_id  => 2,
  p_theme_name=>'Blue and Tan',
  p_default_page_template=>9687725257914124 + wwv_flow_api.g_id_offset,
  p_error_template=>9687725257914124 + wwv_flow_api.g_id_offset,
  p_printer_friendly_template=>9688149371914126 + wwv_flow_api.g_id_offset,
  p_breadcrumb_display_point=>'REGION_POSITION_01',
  p_sidebar_display_point=>'REGION_POSITION_02',
  p_login_template=>9688054572914126 + wwv_flow_api.g_id_offset,
  p_default_button_template=>9688636459914126 + wwv_flow_api.g_id_offset,
  p_default_region_template=>9689839472914128 + wwv_flow_api.g_id_offset,
  p_default_chart_template =>9689047344914126 + wwv_flow_api.g_id_offset,
  p_default_form_template  =>9689133428914128 + wwv_flow_api.g_id_offset,
  p_default_reportr_template   =>9689839472914128 + wwv_flow_api.g_id_offset,
  p_default_tabform_template  =>9689839472914128 + wwv_flow_api.g_id_offset,
  p_default_wizard_template   =>9688926422914126 + wwv_flow_api.g_id_offset,
  p_default_menur_template    =>9688830199914126 + wwv_flow_api.g_id_offset,
  p_default_listr_template    =>9689839472914128 + wwv_flow_api.g_id_offset,
  p_default_report_template   =>9692549154914132 + wwv_flow_api.g_id_offset,
  p_default_label_template    =>9693128915914134 + wwv_flow_api.g_id_offset,
  p_default_menu_template     =>9693645199914134 + wwv_flow_api.g_id_offset,
  p_default_calendar_template =>9693737945914134 + wwv_flow_api.g_id_offset,
  p_default_list_template     =>9691831233914131 + wwv_flow_api.g_id_offset,
  p_default_option_label      =>9693128915914134 + wwv_flow_api.g_id_offset,
  p_default_required_label    =>9693346138914134 + wwv_flow_api.g_id_offset);
end;
/
 
prompt  ...build options used by application 107
--
 
begin
 
null;
 
end;
/

--application/shared_components/globalization/messages
prompt  ...messages used by application: 107
--
--application/shared_components/globalization/language
prompt  ...Language Maps for Application 107
--
 
begin
 
null;
 
end;
/

prompt  ...Shortcuts
--
--application/shared_components/user_interface/shortcuts/delete_confirm_msg
 
begin
 
declare
  c1 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'Would you like to perform this delete action?';

wwv_flow_api.create_shortcut (
 p_id=> 9697348438914301 + wwv_flow_api.g_id_offset,
 p_flow_id=> wwv_flow.g_flow_id,
 p_shortcut_name=> 'DELETE_CONFIRM_MSG',
 p_shortcut_type=> 'TEXT_ESCAPE_JS',
 p_shortcut=> c1);
end;
null;
 
end;
/

--application/shared_components/user_interface/shortcuts/ok_to_get_next_prev_pk_value
 
begin
 
declare
  c1 varchar2(32767) := null;
  l_clob clob;
  l_length number := 1;
begin
c1:=c1||'Are you sure you want to leave this page without saving?';

wwv_flow_api.create_shortcut (
 p_id=> 10027148980790160 + wwv_flow_api.g_id_offset,
 p_flow_id=> wwv_flow.g_flow_id,
 p_shortcut_name=> 'OK_TO_GET_NEXT_PREV_PK_VALUE',
 p_shortcut_type=> 'TEXT_ESCAPE_JS',
 p_shortcut=> c1);
end;
null;
 
end;
/

prompt  ...web services (9iR2 or better)
--
prompt  ...shared queries
--
prompt  ...report layouts
--
prompt  ...authentication schemes
--
--application/shared_components/security/authentication/html_db
prompt  ......scheme 9694545616914145
 
begin
 
declare
  s1 varchar2(32767) := null;
  s2 varchar2(32767) := null;
  s3 varchar2(32767) := null;
  s4 varchar2(32767) := null;
  s5 varchar2(32767) := null;
begin
s1 := null;
s2 := null;
s3 := null;
s4:=s4||'-BUILTIN-';

s5 := null;
wwv_flow_api.create_auth_setup (
  p_id=> 9694545616914145 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'HTML DB',
  p_description=>'Use internal Application Express account credentials and login page in this application.',
  p_page_sentry_function=> s1,
  p_sess_verify_function=> s2,
  p_pre_auth_process=> s3,
  p_auth_function=> s4,
  p_post_auth_process=> s5,
  p_invalid_session_page=>'101',
  p_invalid_session_url=>'',
  p_cookie_name=>'',
  p_cookie_path=>'',
  p_cookie_domain=>'',
  p_ldap_host=>'',
  p_ldap_port=>'',
  p_ldap_string=>'',
  p_attribute_01=>'',
  p_attribute_02=>'wwv_flow_custom_auth_std.logout?p_this_flow=&APP_ID.&amp;p_next_flow_page_sess=&APP_ID.:1',
  p_attribute_03=>'',
  p_attribute_04=>'',
  p_attribute_05=>'',
  p_attribute_06=>'',
  p_attribute_07=>'',
  p_attribute_08=>'',
  p_required_patch=>'');
end;
null;
 
end;
/

--application/shared_components/security/authentication/database
prompt  ......scheme 9694652996914146
 
begin
 
declare
  s1 varchar2(32767) := null;
  s2 varchar2(32767) := null;
  s3 varchar2(32767) := null;
  s4 varchar2(32767) := null;
  s5 varchar2(32767) := null;
begin
s1:=s1||'-DATABASE-';

s2 := null;
s3 := null;
s4 := null;
s5 := null;
wwv_flow_api.create_auth_setup (
  p_id=> 9694652996914146 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'DATABASE',
  p_description=>'Use database authentication (user identified by DAD).',
  p_page_sentry_function=> s1,
  p_sess_verify_function=> s2,
  p_pre_auth_process=> s3,
  p_auth_function=> s4,
  p_post_auth_process=> s5,
  p_invalid_session_page=>'',
  p_invalid_session_url=>'',
  p_cookie_name=>'',
  p_cookie_path=>'',
  p_cookie_domain=>'',
  p_ldap_host=>'',
  p_ldap_port=>'',
  p_ldap_string=>'',
  p_attribute_01=>'',
  p_attribute_02=>'',
  p_attribute_03=>'',
  p_attribute_04=>'',
  p_attribute_05=>'',
  p_attribute_06=>'',
  p_attribute_07=>'',
  p_attribute_08=>'',
  p_required_patch=>'');
end;
null;
 
end;
/

--application/shared_components/security/authentication/database_account
prompt  ......scheme 9694747902914146
 
begin
 
declare
  s1 varchar2(32767) := null;
  s2 varchar2(32767) := null;
  s3 varchar2(32767) := null;
  s4 varchar2(32767) := null;
  s5 varchar2(32767) := null;
begin
s1 := null;
s2 := null;
s3 := null;
s4:=s4||'return false; end;--';

s5 := null;
wwv_flow_api.create_auth_setup (
  p_id=> 9694747902914146 + wwv_flow_api.g_id_offset,
  p_flow_id=> wwv_flow.g_flow_id,
  p_name=> 'DATABASE ACCOUNT',
  p_description=>'Use database account credentials.',
  p_page_sentry_function=> s1,
  p_sess_verify_function=> s2,
  p_pre_auth_process=> s3,
  p_auth_function=> s4,
  p_post_auth_process=> s5,
  p_invalid_session_page=>'101',
  p_invalid_session_url=>'',
  p_cookie_name=>'',
  p_cookie_path=>'',
  p_cookie_domain=>'',
  p_ldap_host=>'',
  p_ldap_port=>'',
  p_ldap_string=>'',
  p_attribute_01=>'',
  p_attribute_02=>'wwv_flow_custom_auth_std.logout?p_this_flow=&APP_ID.&amp;p_next_flow_page_sess=&APP_ID.:1',
  p_attribute_03=>'',
  p_attribute_04=>'',
  p_attribute_05=>'',
  p_attribute_06=>'',
  p_attribute_07=>'',
  p_attribute_08=>'',
  p_required_patch=>'');
end;
null;
 
end;
/

--application/end_environment
commit;
commit;
begin 
execute immediate 'alter session set nls_numeric_characters='''||wwv_flow_api.g_nls_numeric_chars||'''';
end;
/
set verify on
set feedback on
prompt  ...done
