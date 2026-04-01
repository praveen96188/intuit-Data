[ ] 
[-] // *** DATA DRIVEN ASSISTANT Section (!! DO NOT REMOVE !!) ***
	[ ] use "datadrivetc.inc"
	[ ] use "frame.inc"
	[ ] use "IntuitMethods.inc"
	[ ] 
	[ ] // *** DSN ***
	[ ] String database_username = GetCommonParam( "Database","DBUsername")
	[ ] String database_password = GetCommonParam( "Database","DBPassword")
	[ ] String database_dsn = GetCommonParam( "Database","DBDataSourceName")
	[ ] 
	[ ] STRING gsDSNConnect = "DSN="+database_dsn+";UID="+database_username+";PWD="+database_password+";"
	[ ] 
	[ ] // *** Global record for each testcase ***
	[ ] 
	[-] type REC_DATALIST_DD_RunDDTest is record
		[ ] REC_SILK_TEST_TABLE recSILK_TEST_TABLE  //SILK_TEST_TABLE, 
	[ ] 
	[ ] // *** Global record for each Table ***
	[ ] 
	[-] type REC_SILK_TEST_TABLE is record
		[ ] STRING TEST_CASE_ID  //TEST_CASE_ID, 
		[ ] REAL OVERALL_STEP_NUMBER //OVERALL_STEP_NUMBER,
		[ ] REAL TEST_CASE_STEP_NUM  //TEST_CASE_STEP_NUM, 
		[ ] REAL TEST_GROUP_STEP_NUM  //TEST_GROUP_STEP_NUM, 
		[ ] STRING UI_OBJECT_ID  //UI_OBJECT_ID, 
		[ ] STRING VALUE  //VALUE, 
		[ ] STRING TEST_OPERATION_ID  //TEST_OPERATION_ID, 
		[ ] STRING XPATH  //XPATH, 
		[ ] STRING TEST_TYPE_ID  //TEST_TYPE_ID, 
		[ ] STRING TEST_GROUP_ID  //TEST_GROUP_ID, 
		[ ] STRING FUNCTIONAL_AREA_ID  //FUNCTIONAL_AREA_ID, 
		[ ] STRING UI_SECTION_ID  //UI_SECTION_ID, 
		[ ] STRING STEP_TYPE  //STEP_TYPE, 
		[ ] STRING STEP_TYPE  //VERIFIED, 
	[ ] 
	[ ] // *** Global record containing sample data for each table ***
	[ ] // *** Used when running a testcase with 'Use Sample Data from Script' checked ***
	[ ] 
	[-] REC_SILK_TEST_TABLE grTest_SILK_TEST_TABLE = {...}
		[ ] NULL // TEST_CASE_ID
		[ ] NULL // OVERALL_STEP_NUMBER
		[ ] NULL // TEST_CASE_STEP_NUM
		[ ] NULL // TEST_GROUP_STEP_NUM
		[ ] NULL // UI_OBJECT_ID
		[ ] NULL // VALUE
		[ ] NULL // TEST_OPERATION_ID
		[ ] NULL // XPATH
		[ ] NULL // TEST_TYPE_ID
		[ ] NULL // TEST_GROUP_ID
		[ ] "Launcing IE" // FUNCTIONAL_AREA_ID
		[ ] NULL // UI_SECTION_ID
		[ ] NULL // STEP_TYPE
		[ ] NULL // VERIFIED
	[ ] // *** End of DATA DRIVEN ASSISTANT Section ***
	[ ] 
[-] testcase DD_RunDDTest (REC_DATALIST_DD_RunDDTest rData) appstate none
		[ ] String operation = rData.recSILK_TEST_TABLE.TEST_OPERATION_ID
		[ ] String xPath = rData.recSILK_TEST_TABLE.XPATH
		[ ] String value = rData.recSILK_TEST_TABLE.VALUE
		[ ] String testType = "COMPONENT"
		[ ] // Sleep(1)
		[ ] Number testCaseStepNum = rData.recSILK_TEST_TABLE.TEST_CASE_STEP_NUM
		[ ] String testCaseId = rData.recSILK_TEST_TABLE.TEST_CASE_ID
		[ ] 
		[ ] ProcessStep(operation,xPath,value,testType,testCaseStepNum,testCaseId)
	[ ] 
