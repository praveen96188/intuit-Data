[+] void DoAttachFile(Window browser,String fieldXPath, String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] windowItem.SetText (value)
	[ ] windowItem.TypeKeys("<Tab 2>", 0)
	[ ] Window windowItem2 = getObject(browser, ".//PushButton[@caption='Open' and @windowid='1']")
	[ ] windowItem2.Click()
[+] void DoChangeFocus(Window browser,String fieldXPath, String value null)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] windowItem.ChangeFocus()
[+] void DoVerifyDataGridRowCount(Window browser,String fieldXPath, String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] NUMBER rowCount= Val(value)
	[+] windowItem.VerifyProperties ({...})
		[ ] ""
		[+] {...}
			[ ] {"numRows",             rowCount}
	[ ] 
[+] void DoChange(Window browser,String fieldXPath, String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] windowItem.Change(value)
[+] void DoClickGridColumn(Window browser,String fieldXPath, String value)
	[ ]  Window windowItem = getObject(browser, fieldXPath)
	[ ] INTEGER inValue = Val(value)
	[+] if (! inValue == null)
		[ ] Print("ColumnValue :" + "{inValue}")
	[ ] windowItem.HeaderClick(inValue)
[+] void DoClickButton(Window browser,String fieldXPath, String value null)
	[+] // if value != null
		[ ] // fieldXPath = StrTran (fieldXPath, "<VALUE>", value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] Print(fieldXPath)
	[ ] windowItem.Click(1,5, 5)
[+] void DoClick(Window browser,String fieldXPath, String value null)
	[+] // if value != null
		[ ] // fieldXPath = StrTran (fieldXPath, "<VALUE>", value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] Print(fieldXPath)
	[ ] windowItem.Click()
[+] void CloseBrowser()
	[ ] InternetExplorer.Close()
	[ ] SYS_Execute("taskkill /F /IM iexplore.exe")
[+] void DoCloseBrowser(Window browser null,String fieldXPath null, String value null)
	[ ] CloseBrowser()
[+] void DoFocusOnApp(Window browser null,String fieldXPath null, String value null)
	[ ] InternetExplorer.Control3.Control1.FlexApplication1.Click(1, 10, 10)
[+] void DoInput(Window browser,String fieldXPath, String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] windowItem.Input(value)
[+] void DoOpen(Window browser,String fieldXPath, String value null)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] windowItem.Open()
[+] // void DoOpenGridPopUpMenu(Window browser,String fieldXPath, String value)
		[ ] // LIST OF WINDOW actionButtons = browser.findAll(".//FlexPopUpButton[@caption='payrollActionButton' and @className='psp.sap.view.PayrollActionPopUpMenuButton' and @windowid='payrollActionButton']")
		[ ] // actionButtons[Val(value)].Open()
[+] void DoRefreshPage(Window browser null,String fieldXPath null , String value null)
	[ ] InternetExplorer.SetActive()
	[ ] DoFocusOnApp(null,null,null)
	[ ] InternetExplorer.Control3.Control1.FlexApplication1.TypeKeys("<F9>")
	[ ] sleep(1)
[+] void DoRunCommand(Window browser,String fieldXPath null, String value)
	[ ] Integer iReturn = SYS_Execute (value)
[+] void DoScrollToTopOfObj(Window browser,String fieldXPath, String value null)
	[ ] String XPathOfObj = GetField (fieldXPath, "|", 1)
	[ ] String XPathOfCanvas = GetField (fieldXPath, "|", 2)
	[ ] Window objWin = getObject(browser, XPathOfObj)
	[ ] int yPositionOfObj = objWin.GetProperty("y")
	[ ] Window canvasWin = getObject(browser, XPathOfCanvas)
	[ ] canvasWin.Scroll(yPositionOfObj, SCROLL_VERTICAL, THUMB_POSITION)
[+] void DoSelect(Window browser,String fieldXPath, String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] windowItem.Select(value)
[+] void DoSleep(STRING value)
	[ ] NUMBER num= Val(value)
	[+] if(num>0)
		[ ] sleep(num)
[-] void DoSOATest(Window browser null,String fieldXPath null, String value)
	[-] if(!MatchStr("*.tst*", value))
		[ ] SYS_Execute("C:\Dev\PSP\main\SilkTest\Common\amfCurlRequest.bat " + """" + value  + """")
	[-] else
		[ ] // Sets iHandle = to the window handle for the desktop
		[ ] int iHandle = Desktop.GetHandle()
		[ ] // The operation that is to be carried out. Open will execute the file with the associated action or application
		[ ] string lpOperation = "open"
		[ ] // The file on which this operation will be carried out
		[ ] String SOABATFile = GetCommonParam( "SOATests","SOABATFile")
		[ ] 
		[ ] String SOADirectory = GetCommonParam( "SOATests","SOAPATH")
		[ ] 
		[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
		[ ] String CurrentEnv = GetEnvSpecificParam( "General","Env")
		[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
		[ ] String  SOAUIAutoFileFolder = GetCommonParam( "SOATests","SOAUIAutoFileFolder")
		[ ] 
		[ ] // Any additional parameters to the commandline
		[ ] string lpParameters = RootPSPDir+CurrentRelease+"\"+SOAUIAutoFileFolder+"\"+ value +" "+CurrentRelease+" "+CurrentEnv+""
		[ ] 
		[ ] // The working directory in which this will be run
		[ ] String  SOABATFileDir = GetCommonParam( "SOATests","SOABATFileDir")
		[ ] string lpDirectory = RootPSPDir+CurrentRelease+"\"+SOABATFileDir+"\"
		[ ] // a flag that determines how the command will be shown
		[ ] int iShowCmd = 5
		[ ] 
		[ ] // Create a marker file that the SOA test runner will delete when it is done.
		[ ] HANDLE hFile
		[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
		[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
		[ ] String markerFilenameFullPath = RootPSPDir+CurrentRelease+"\"+SOABATFileDir+"\test_running.txt"
		[ ] hFile = FileOpen (markerFilenameFullPath, FM_WRITE, NULL)
		[ ] FileClose(hFile)
		[ ] 
		[ ] // Shell execute w (W stands for wide byte) launches the CMD window to run the shell script.  It does not wait for the ensuing call it to finish.
		[ ] ShellExecuteW (iHandle,lpOperation,SOABATFile,lpParameters,lpDirectory,iShowCmd)
		[ ] // Wait for marker file to be removed before 
		[ ] int cnt = 0
		[ ] boolean done = false
		[-] do
			[-] while (!done)
				[ ] cnt = cnt + 1
				[ ] sleep(0.1)
				[+] if (!SYS_FileExists(markerFilenameFullPath))
					[ ] done=true
				[+] if (cnt == 5000)
					[ ] raise -1,"SOA test " + value + " never finished."
		[-] except
			[ ] Print("Finished test")
[+] void DoType(Window browser,String fieldXPath,String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] windowItem.Type(value)
[+] void DoVerifyDateToday(Window browser,String fieldXPath, String value)
	[ ] DateTime currDttm = GetDateTime()
	[ ] String formatDate = FormatDateTime (currDttm, "mm/dd/yyyy")
	[ ] String newValue = value + formatDate
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[+] windowItem.VerifyProperties ({...})
			[ ] ""
			[+] {...}
				[ ] {"text",                newValue}
[+] void DoVerifyListLabels(Window browser,String fieldXPath, String value)
	[ ] LIST of STRING lsIndex
	[ ] STRING sIndexValue = "" ,sSegment = "SampleValue",sChanged
	[ ] int b = 1,c
	[ ] value = (StrTran(value,"<nl>",Chr(13)))
	[+] while (sSegment != "")
		[ ] sSegment = GetField(value,"|",b)
		[ ] b++
		[ ] sSegment=Trim(sSegment)
		[+] if(sSegment != "")
			[ ] ListAppend(lsIndex,sSegment)
	[+] for c=1 to ListCount(lsIndex)
		[ ] sIndexValue = sIndexValue + "[{lsIndex[c]}]"
		[+] if(c != ListCount(lsIndex))
			[ ] sIndexValue = sIndexValue + " | "
		[+] else
			[ ] Print(sIndexValue)
			[ ] break
	[+] for c=1 to ListCount(lsIndex)
		[ ] sChanged = StrTran(sIndexValue,lsIndex[c],"*{lsIndex[c]}*")
		[ ] Print(sChanged)
		[ ] window lsWin = getObject(browser,".//FlexListLabel[@automationIndex='{sChanged}']")
		[+] if(lsWin.GetProperty("automationValue") == lsIndex[c])
			[ ] Print("There is a DataGrid Column with name "  + "{lsIndex[c]}")
		[+] else
			[ ] Print("There is a no DataGrid Column with name "  + "{lsIndex[c]}")
[+] void DoVerifyButtonLabel(Window browser,String fieldXPath,String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[+] windowItem.VerifyProperties ({...})
		[ ] ""
		[+] {...}
			[ ] {"label",                 value}
[+] void DoVerifyDataGridContents(Window browser,String fieldXPath, String value)
	[ ] list of list of string allRows
	[ ] STRING actRows = "Sample Value"
	[ ] Window dataGrid = getObject(browser, fieldXPath)
	[ ] allRows = dataGrid.GetValues(0,-1)
	[ ] INTEGER c = 1
	[ ] 
	[+] if(MatchStr("*"{"{*",value) && MatchStr("*}}*",value))
		[ ] value = GetField(value,"}}",1)
		[ ] value = GetField(value,""{"{",2)
	[ ] 
	[+] while(actRows != "")
		[ ] actRows = GetField(value,"}, "{",c)
		[ ] Print(actRows)
		[+] if(actRows != "")
			[+] if (! MatchStr ("*{actRows}*", "{allRows}"))
					[ ] print("Displayed: " + "{allRows}")
					[ ] print("Expected : " + "{value}")
					[ ] RaiseError(100,"Contents of table do not match expected value. "  +  "  Expected Values: " + value +  " Returned Results: " + "{allRows}")
		[ ] c++
[+] void DoVerifyBoxContents(Window browser,String fieldXPath, String value)
	[ ] list of list of string allRows
	[ ] Window Box = getObject(browser, fieldXPath)
	[ ] allRows = Box.GetValues(0,-1)
	[ ] 
	[+] if(MatchStr("*"{"{*",value) && MatchStr("*}}*",value))
		[ ] value = GetField(value,"}}",1)
		[ ] value = GetField(value,""{"{",2)
	[ ] 
	[+] if (! MatchStr ("*{value}*", "{allRows}"))
			[ ] print("Displayed: " + "{allRows}")
			[ ] print("Expected : " + "{value}")
			[ ] RaiseError(100,"Contents of table do not match expected value. "  +  "  Expected Values: " + value +  " Returned Results: " + "{allRows}")
[+] void DoVerifyDataGridSpecificRows(Window browser,String fieldXPath, String value)
	[ ] list of list of string allVisibableRows
	[ ] Window dataGrid = getObject(browser, fieldXPath)
	[ ] int startRow = Val(GetField(value,"<FIRSTROWNUM>",1))
	[ ] String stringAfterStartRowNum = GetField(value,"<FIRSTROWNUM>",2)
	[ ] int lastRow = Val(GetField(stringAfterStartRowNum,"<LASTROWNUM>",1))
	[ ] // Move the final string after the last row num back into the value string
	[ ] value = GetField(stringAfterStartRowNum,"<LASTROWNUM>",2)
	[ ] allVisibableRows = dataGrid.GetValues(startRow,lastRow)
	[+] if (! MatchStr (value, "{allVisibableRows}"))
			[ ] print("Displayed: " + "{allVisibableRows}")
			[ ] print("Expected : " + "{value}")
			[ ] RaiseError(100,"Contents of table do not match expected value. "  +  "  Expected Values: " + value +  " Returned Results: " + "{allVisibableRows}")
[+] void DoVerifyExists(Window browser,String fieldXPath,String value)
	[+] if value=="TRUE"
		[ ] findAnItem(browser,fieldXPath)
	[+] else
		[ ] Print ("Value is false")
		[-] do
			[ ] findAnItem(browser,fieldXPath)
		[-] except
			[ ] Print ("XPath does not exist.")
[+] void DoVerifyText(Window browser,String fieldXPath,String value null)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[ ] Print(windowItem.Text)
	[-] windowItem.VerifyProperties ({...})
		[ ] ""
		[-] {...}
			[ ] {"text",                 value}
[+] void DoVerifyEnabled(Window browser,String fieldXPath,String value)
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[+] if value=="TRUE"
		[+] windowItem.VerifyProperties ({...})
			[ ] ""
			[+] {...}
				[ ] {"enabled",                 TRUE}
	[+] else
		[ ] Print ("Value is false")
		[+] windowItem.VerifyProperties ({...})
			[ ] ""
			[+] {...}
				[ ] {"enabled",                 FALSE}
[+] void DoVerifyColor(Window browser,String fieldXPath,String value)
	[ ] String color = ""
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[+] if value=="GREEN"
		[+] windowItem.VerifyProperties ({...})
			[ ] ""
			[+] {...}
				[ ] {"color",                [LIST OF INTEGER] {0, 153, 0}}
	[+] else
		[+] if value=="RED"
			[+] windowItem.VerifyProperties ({...})
				[ ] ""
				[+] {...}
					[ ] {"color",                [LIST OF INTEGER] {255, 0, 0}}
		[+] else
			[ ] Verify(TRUE,FALSE,"Color '" + value + "' not recognized.")
	[ ] 
[+] void DoVerifySelectedChild(Window browser,String fieldXPath,String value null)
	[+] if(value==null)
		[ ] value = ""
	[ ] Window windowItem = getObject(browser, fieldXPath)
	[+] windowItem.VerifyProperties ({...})
		[ ] ""
		[+] {...}
			[ ] {"selectedChild",                 value}
[+] dll "shell32"
	[ ] int ShellExecuteW (int hwnd, String lpOperation, String lpFile, string lpParameters,String lpDirectory, int nShowCmd)
[ ] 
[+] void DoTestCaseBegin(Window browser null,String fieldXPath null, String value null, String testType)
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
	[ ] 
	[ ] // Clear out any test case failure inficators in case a run was killed in the middle.
	[ ] String testCaseResultFileStr = RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\testcase_fail.txt"
	[+] if (SYS_FileExists(testCaseResultFileStr))
		[ ] SYS_RemoveFile(testCaseResultFileStr)
	[ ] 
	[ ] HFILE FileHandle
	[ ] String SilkTestResultDir = GetCommonParam( "General","SilkTestResultDir")
	[ ] FileHandle = FileOpen (SilkTestResultDir + testType+".xml", FM_APPEND)
	[ ] FileWriteLine (FileHandle, "   <TEST_CASE>")
	[ ] FileWriteLine (FileHandle, "      <TEST_CASE_ID>"+escapeSpecialXMLChars(value)+"</TEST_CASE_ID>")
	[ ] FileClose (FileHandle)
	[ ] 
	[ ] HFILE StartTestCaseFileHandle = FileOpen (RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\testcase_start.txt", FM_WRITE)
	[ ] 
	[ ] DateTime currDttm = GetDateTime()
	[ ] String currDttmStr = "{currDttm}"
	[ ] FileWriteLine (StartTestCaseFileHandle, currDttmStr)
	[ ] FileClose(StartTestCaseFileHandle)
	[ ] 
[+] void DoTestGroupBegin(Window browser null,String fieldXPath null, String value null, String testType)
	[ ] HFILE FileHandle
	[ ] String SilkTestResultDir = GetCommonParam( "General","SilkTestResultDir")
	[ ] FileHandle = FileOpen (SilkTestResultDir+testType+".xml", FM_APPEND)
	[ ] FileWriteLine (FileHandle, "   <TEST_GROUP>")
	[ ] FileWriteLine (FileHandle, "      <TEST_GROUP_ID>"+escapeSpecialXMLChars(value)+"</TEST_GROUP_ID>")
	[ ] FileClose (FileHandle)
	[ ] 
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
	[ ] HFILE StartTestGroupFileHandle = FileOpen (RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\test_group_start.txt", FM_WRITE)
	[ ] 
	[ ] DateTime currDttm = GetDateTime()
	[ ] String currDttmStr = "{currDttm}"
	[ ] FileWriteLine (StartTestGroupFileHandle, currDttmStr)
	[ ] FileClose(StartTestGroupFileHandle)
[+] void DoTestCaseEnd(Window browser null,String fieldXPath null, String value null, String testType)
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
	[ ] 
	[ ] HFILE StartTestCaseFileHandle = FileOpen (RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\testcase_start.txt", FM_READ)
	[ ] String startDttmStr 
	[ ] FileReadLine (StartTestCaseFileHandle,startDttmStr)
	[ ] FileClose(StartTestCaseFileHandle)
	[ ] 
	[ ] // Find out if the test case was successful by looking for an error file.
	[ ] String testCaseResult
	[ ] String testCaseResultFileStr = RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\testcase_fail.txt"
	[+] if (SYS_FileExists(testCaseResultFileStr))
		[ ] testCaseResult = "FAILED"
		[ ] SYS_RemoveFile(testCaseResultFileStr)
	[+] else
		[ ] testCaseResult = "SUCCESS"
	[ ] 
	[ ] DateTime currDttm = GetDateTime()
	[ ] DateTime startDttm = startDttmStr
	[ ] String totalTimeStr = GetTimeStrDiffInSeconds(startDttm,currDttm)
	[ ] HFILE FileHandle
	[ ] String SilkTestResultDir = GetCommonParam( "General","SilkTestResultDir")
	[ ] FileHandle = FileOpen (SilkTestResultDir+testType+".xml", FM_APPEND)
	[ ] FileWriteLine (FileHandle, "         <TEST_CASE_RESULT>" + testCaseResult + "</TEST_CASE_RESULT>")
	[ ] FileWriteLine (FileHandle, "         <TEST_CASE_TOTAL_TIME_IN_SECS>" + totalTimeStr + "</TEST_CASE_TOTAL_TIME_IN_SECS>")
	[ ] FileWriteLine (FileHandle, "   </TEST_CASE>")
	[ ] FileClose (FileHandle)
[+] void DoTestGroupEnd(Window browser null,String fieldXPath null, String value null, String testType)
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
	[ ] 
	[ ] HFILE StartTestGroupFileHandle = FileOpen (RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\test_group_start.txt", FM_READ)
	[ ] String startDttmStr 
	[ ] FileReadLine (StartTestGroupFileHandle,startDttmStr)
	[ ] FileClose(StartTestGroupFileHandle)
	[ ] 
	[ ] DateTime currDttm = GetDateTime()
	[ ] DateTime startDttm = startDttmStr
	[ ] String totalTimeStr = GetTimeStrDiffInSeconds(startDttm,currDttm)
	[ ] 
	[ ] HFILE FileHandle
	[ ] String SilkTestResultDir = GetCommonParam( "General","SilkTestResultDir")
	[ ] FileHandle = FileOpen (SilkTestResultDir+testType+".xml", FM_APPEND)
	[ ] FileWriteLine (FileHandle, "         <TEST_GROUP_TOTAL_TIME_IN_SECS>" + totalTimeStr + "</TEST_GROUP_TOTAL_TIME_IN_SECS>")
	[ ] FileWriteLine (FileHandle, "   </TEST_GROUP>")
	[ ] FileClose (FileHandle)
[+] void DoTestTypeBegin(String testType)
	[ ] HFILE FileHandle
	[ ] String SilkTestResultDir = GetCommonParam( "General","SilkTestResultDir")
	[ ] Print(SilkTestResultDir+testType+".xml")
	[ ] FileHandle = FileOpen (SilkTestResultDir+testType+".xml", FM_WRITE)
	[ ] FileWriteLine (FileHandle, "   <TEST_GROUPS>")
	[ ] FileWriteLine (FileHandle, "      <TEST_TYPE>"+escapeSpecialXMLChars(testType)+"</TEST_TYPE>")
	[ ] FileClose (FileHandle)
	[ ] 
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
	[ ] HFILE StartTestTypeFileHandle = FileOpen (RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\testType_start.txt", FM_WRITE)
	[ ] 
	[ ] DateTime currDttm = GetDateTime()
	[ ] String currDttmStr = "{currDttm}"
	[ ] FileWriteLine (StartTestTypeFileHandle, currDttmStr)
	[ ] FileClose(StartTestTypeFileHandle)
	[ ] 
[+] void DoTestTypeEnd(String testType)
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
	[ ] HFILE StartTestTypeFileHandle = FileOpen (RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\testType_start.txt", FM_READ)
	[ ] String startDttmStr 
	[ ] FileReadLine (StartTestTypeFileHandle,startDttmStr)
	[ ] FileClose(StartTestTypeFileHandle)
	[ ] 
	[ ] DateTime currDttm = GetDateTime()
	[ ] DateTime startDttm = startDttmStr
	[ ] String totalTimeStr = GetTimeStrDiffInSeconds(startDttm,currDttm)
	[ ] 
	[ ] HFILE FileHandle
	[ ] String SilkTestResultDir = GetCommonParam( "General","SilkTestResultDir")
	[ ] FileHandle = FileOpen (SilkTestResultDir+testType+".xml", FM_APPEND)
	[ ] FileWriteLine (FileHandle, "      <TEST_TYPE_TOTAL_TIME_IN_SECONDS>"+ totalTimeStr +"</TEST_TYPE_TOTAL_TIME_IN_SECONDS>")
	[ ] FileWriteLine (FileHandle, "   </TEST_GROUPS>")
	[ ] FileClose (FileHandle)
[+] void writeTestCaseSuccess(String operation null, String stepStr, String testCaseId, String timeSpentInSeconds,String testType)
	[ ] writeTestCaseResult("SUCCESS",operation,stepStr,testCaseId,timeSpentInSeconds,testType,null)
[+] void writeTestCaseResult(String successStr, String operation null, String stepStr,String testCaseId, String timeSpentInSeconds,String testType,String errStr NULL)
	[ ] HFILE FileHandle
	[ ] // Open file, append line, and close
	[ ] String SilkTestResultDir = GetCommonParam( "General","SilkTestResultDir")
	[ ] FileHandle = FileOpen (SilkTestResultDir+testType+".xml", FM_APPEND)
	[ ] FileWriteLine (FileHandle, "      <TEST_STEP>")
	[ ] FileWriteLine (FileHandle, "         <TEST_STEP_RESULT>" + escapeSpecialXMLChars(successStr) + "</TEST_STEP_RESULT>")
	[ ] FileWriteLine (FileHandle, "         <OPERATION>" + escapeSpecialXMLChars(operation) + "</OPERATION>")
	[ ] FileWriteLine (FileHandle, "         <STEP>" + escapeSpecialXMLChars(stepStr) + "</STEP>")
	[ ] FileWriteLine (FileHandle, "         <STEP_TIME_IN_MILLISECONDS>" + timeSpentInSeconds + "</STEP_TIME_IN_MILLISECONDS>")
	[+] if errStr != NULL
		[ ] FileWriteLine (FileHandle, "         <ERROR>" + escapeSpecialXMLChars(errStr) + "</ERROR>")
	[ ] FileWriteLine (FileHandle, "      </TEST_STEP>")
	[ ] FileClose (FileHandle)
[+] void writeTestCaseFailure(String operation, String stepStr, String testCaseId null,String timeSpentInSeconds,String testType)
	[ ] LogError ("ERROR")
	[ ] Print ("------------------------------------")
	[ ] String errStr
	[ ] CALL Call
	[ ] LIST OF CALL lCall
	[ ] lCall = ExceptCalls ()
	[ ] errStr = ExceptData ()
	[+] for each Call in lCall
		[ ] errStr = errStr + "Module: {Call.sModule}, Function: {Call.sFunction}, Line: {Call.iLine}" + chr(10)
	[ ] Print (errStr)
	[ ] Print ("------------------------------------")
	[ ] 
	[ ] String WorkingDir = GetCommonParam( "General","WorkingDir")
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String CurrentEnv = GetEnvSpecificParam( "General","Env")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] 
	[ ] // Create a file to incidate that the test case has failed.
	[ ] HFILE ErrorTestCaseFileHandle = FileOpen (RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\testcase_fail.txt", FM_WRITE)
	[ ] FileWriteLine (ErrorTestCaseFileHandle, stepStr)
	[ ] FileClose(ErrorTestCaseFileHandle)
	[ ] 
	[ ] writeTestCaseResult("FAILED",operation,stepStr,testCaseId,timeSpentInSeconds,testType,errStr)
[+] String escapeSpecialXMLChars(String inputDataStr)
	[ ] String alteredDataStr = inputDataStr
	[ ] alteredDataStr = StrTran (alteredDataStr, "&","&amp;")
	[ ] alteredDataStr = StrTran (alteredDataStr,"""","&quot;")
	[ ] alteredDataStr = StrTran (alteredDataStr, "'","&apos;")
	[ ] alteredDataStr = StrTran (alteredDataStr,"<","&lt;")
	[ ] alteredDataStr = StrTran (alteredDataStr,">","&gt;")
	[ ] return alteredDataStr
[+] String GetTimeStrDiffInSeconds(DateTime starting, DateTime ending)
	[ ] int iHours,iMinutes, iSeconds, iMicrosecs
	[ ] DiffDateTime(ending,starting,iHours,iMinutes, iSeconds, iMicrosecs)
	[ ] int totalTimeInSecs = iHours * 60 * 60 + iMinutes * 60 + iSeconds
	[ ] return Str(totalTimeInSecs)
[+] void ProcessStep(String operation,String xPath null,String value null,String testType,Number testCaseStepNum null,String testCaseId null)
	[ ] Window browser = InternetExplorer
	[ ] Boolean foundOper = false
	[ ] 
	[ ] HTIMER TotalTimer
	[ ] TotalTimer = TimerCreate ()
	[ ] 
	[ ] TimerStart (TotalTimer)
	[ ] DateTime beginTime
	[ ] STRING sOverallTime
	[+] if (value != null)
		[ ] value = StrTran (value,"<NEW_LINE>",chr(13))
	[ ] String VALUE_SEPARATOR = "<VALUE_SEPARATOR>"
	[ ] String VALUE_REPLACE_TAG = "<VALUE>"
	[ ] String origValue = value
	[ ] String origXPath = xPath
	[ ] // First we replace all of the <VALUE> items in XPATH with each 
	[+] if (xPath != null)
		[ ] boolean xpathHasValueTag = true
		[ ] // Lets replace all of the <VALUE> items in the XPATH with the items in the VALUE
		[+] while xpathHasValueTag
			[+] if (GetField(xPath,VALUE_REPLACE_TAG,2)=="")
				[ ] xpathHasValueTag = false
			[-] else
				[ ] // If value is null and we have found a <VALUE> in the Xpath, raise an error.
				[+] if (value==null)
					[ ] RaiseError (100,"The {VALUE_REPLACE_TAG} entries in the xpath ({origXPath}) outnumber the {VALUE_SEPARATOR} entries the value ({origValue}).")
				[ ] String stringBeforeVALUEInXPath = GetField(xPath,VALUE_REPLACE_TAG,1)
				[ ] // If the value has a VALUE_SEPARATOR, pull it out
				[ ] // If not there, then use the entire value
				[ ] String valueStr
				[+] if (MatchStr("*{VALUE_SEPARATOR}*",value))
					[ ] valueStr = GetField(value,VALUE_SEPARATOR,1)
					[ ] value = SubStr(value,Len(valueStr+VALUE_SEPARATOR)+1)
				[+] else
					[ ] valueStr = value
					[ ] value=null
				[ ] // Overwrite the <VALUE> in XPATH with the value string from VALUE
				[ ] xPath = stringBeforeVALUEInXPath + valueStr + SubStr(xPath,Len(stringBeforeVALUEInXPath + VALUE_REPLACE_TAG)+1)
	[+] if (xPath != null)
		[+] if (MatchStr("*{VALUE_REPLACE_TAG}*",xPath))
			[ ] RaiseError (101,"The replacement tags do not match for your value  ({origValue}) and your xpath ({origXPath}) entry.")
	[+] if (value != null)
		[ ] value = (StrTran(value,"<nl>",Chr(13)))
		[+] if (MatchStr("*{VALUE_SEPARATOR}*",value))
			[ ] RaiseError (101,"The replacement tags do not match for your value  ({origValue}) and your xpath ({origXPath}) entry.")
	[-] do
		[+] if (value==null)
			[ ] Print("Operation " +operation)
		[+] else
			[ ] Print("Operation " +operation + " wtih value '" + value + "'")
		[+] if (MatchStr(operation,"TestCaseBegin"))
			[ ] DoTestCaseBegin(browser,xPath,value,testType)
			[ ] return
		[+] if (MatchStr(operation,"TestCaseEnd"))
			[ ] DoTestCaseEnd(browser,xPath,value,testType)
			[ ] return
		[+] if (MatchStr(operation,"TestGroupBegin"))
			[ ] DoTestGroupBegin(browser,xPath,value,testType)
			[ ] return
		[+] if (MatchStr(operation,"TestGroupEnd"))
			[ ] DoTestGroupEnd(browser,xPath,value,testType)
			[ ] return
		[+] if (MatchStr(operation,"Input"))
			[ ] DoInput(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"Open"))
			[ ] DoOpen(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"AttachFile"))
			[ ] DoAttachFile(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"Select"))
			[ ] DoSelect(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"Click"))
			[ ] DoClick(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"ClickButton"))
			[ ] DoClickButton(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"ClickGridColumn"))
			[ ] DoClickGridColumn(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"Sleep"))
			[ ] DoSleep(value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"ScrollToTopOfObj"))
			[ ] DoScrollToTopOfObj(browser, xPath, value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"ChangeFocus"))
			[ ] DoChangeFocus(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyDataGridRowCount"))
			[ ] DoVerifyDataGridRowCount(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"Change"))
			[ ] sleep(1)
			[ ] DoChange(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"Type"))
			[ ] DoType(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyButtonLabel"))
			[ ] DoVerifyButtonLabel(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyDataGridContents"))
			[ ] DoVerifyDataGridContents(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyBoxContents"))
			[ ] DoVerifyBoxContents(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyDataGridSpecificRows"))
			[ ] DoVerifyDataGridSpecificRows(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifySelectedChild"))
			[ ] DoVerifySelectedChild(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyText"))
			[ ] DoVerifyText(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyDateToday"))
			[ ] DoVerifyDateToday(browser,xPath, value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyExists"))
			[ ] Print("value *** " +value + " ***")
			[ ] Print("xPath " +xPath)
			[ ] DoVerifyExists(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyListLabels"))
			[ ] DoVerifyListLabels(browser,xPath, value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyColor"))
			[ ] DoVerifyColor(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"RunCommand"))
			[ ] DoRunCommand(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"RefreshPage"))
			[ ] DoRefreshPage(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"CloseBrowser"))
			[ ] DoCloseBrowser(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"VerifyEnabled"))
			[ ] Print("value *** " +value + " ***")
			[ ] Print("xPath " +xPath)
			[ ] DoVerifyEnabled(browser,xPath,value)
			[ ] foundOper=true
		[+] if (MatchStr(operation,"SOATest"))
			[ ] DoSOATest(browser,xPath,value)
			[ ] foundOper=true
		[+] if foundOper
			[ ] TimerStop (TotalTimer)
			[ ] sOverallTime = TimerStr (TotalTimer)
			[ ] TimerDestroy (TotalTimer)
			[ ] writeTestCaseSuccess(operation,Str(testCaseStepNum),testCaseId,sOverallTime,testType)
		[+] else
			[ ] Verify(TRUE,FALSE,"Operation '" + operation + "' operation not recognized.")
	[+] except
		[ ] TimerStop (TotalTimer)
		[ ] sOverallTime = TimerStr(TotalTimer)
		[ ] TimerDestroy (TotalTimer)
		[ ] String stepNumStr
		[+] if testCaseStepNum != null
			[ ] stepNumStr = Str(testCaseStepNum)
		[+] else
			[ ] stepNumStr =  "none"
		[ ] writeTestCaseFailure(operation,stepNumStr,testCaseId,sOverallTime,testType)
[+] void CloseIE()
	[ ] SYS_Execute("taskkill /F /IM iexplore.exe")
[+] Window findAnItem(Window browser,String fieldXPath)
	[ ] Window page
	[ ] int cnt = 0
	[ ] boolean pageFound = false
	[+] while (cnt < 5 && !pageFound)
		[+] do
			[ ] //page = browser.find(".//FlexBox[@caption='pageVBox' and @className='psp.sap.view.PageVBox' and @windowid='pageVBox']")
			[ ] page = browser.find(fieldXPath)
			[ ] pageFound = true
		[+] except
			[ ] pageFound = false
			[ ] cnt++
			[ ] Print("Delay {cnt} on fieldXPath")
			[ ] Sleep(1)
	[ ] // If the page isn't found, try one last time.  
	[ ] // Chances are it won't be found and the error message is not caught and will be raised and provide the details on the failure.
	[+] if !pageFound
		[ ] page = browser.find(fieldXPath)
	[ ] return page
[+] Window getObject(Window browser,String fieldXPath)
	[ ] int  cnt = 1
	[ ] boolean foundItem = true
	[ ] Window currObj = null
	[+] while foundItem
		[ ] String objStr = GetField(fieldXPath,"<XPATH_SEPARATOR>",cnt)
		[+] if (objStr != "")
			[+] if currObj == null
				[ ] currObj = findAnItem(browser,objStr)
			[+] else
				[ ] currObj = findAnItem(currObj,objStr)
			[ ] cnt++
			[ ] foundItem=true
		[+] else
			[ ] foundItem=false
	[+] if currObj == null
		[ ] currObj = findAnItem(browser,fieldXPath)
	[ ] return currObj
	[ ] 
[ ] 
[ ] builtin void Start (STRING sCmdLine, STRING sDir optional, STRING sStartupTag optional, NUMBER nTimeout optional)
[ ] 
[-] void DoSAPDataloader(String dataloaderName)
	[ ] Start ("C:\Program Files\Internet Explorer\IEXPLORE.EXE https://localhost:8443/SAP/SAPApp.swf", "D:\dev\PSP\main\SilkTest\WorkingDir", "InternetExplorer")
