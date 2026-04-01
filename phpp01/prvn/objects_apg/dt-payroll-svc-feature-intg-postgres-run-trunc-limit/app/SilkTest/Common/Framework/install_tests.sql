prompt Running setup_tests.sql
@setup_framework.sql
-- Remove trigger so we don't populate SILK_TEST_TABLE each time we insert.
prompt Running insert_tests.sql
@..\..\SAP\Tests\insert_test_components.sql
@..\..\SAP\Tests\insert_tests.sql
-- Reset all of the sequences to start with the NEXT ID in each table.
prompt reset_sequence_values.sql
@reset_sequence_values.sql
prompt pk_silk.pks
@pk_silk.pks
/
prompt pk_silk.pkb
@pk_silk.pkb
/
prompt exec pk_silk.populate_test_list()
exec pk_silk.populate_test_list()
@add_post_install_triggers.sql
commit
/
exit


