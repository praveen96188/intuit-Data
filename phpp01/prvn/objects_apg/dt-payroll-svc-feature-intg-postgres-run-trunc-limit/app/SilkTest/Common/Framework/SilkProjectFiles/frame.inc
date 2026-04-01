[ ] const wMainWindow = InternetExplorer
[ ] 
[-] window MainWin InternetExplorer
	[+]  multitag TAG_IS_OPEN_AGENT
		[ ]  "https:??localhost:8443?SAP?SAPApp.swf*"
		[ ]  "*Microsoft Internet Explorer"
	[ ] 
	[ ] // There is a bug in SilkTest where you can't record test cases and utilize the CTL-ALT when 
	[ ] //     frame.inc loads from parameter files.
	[ ] // So if you are recording a test case locally and want to be able to use CTL-ATL to see what
	[ ] //     verificaiton options are available on the screen, then:
	[ ] //     COMMENT the "TEST LOCAL COMMENT SECTION" section and
	[ ] //     UNCOMMENT the "TEST LOCAL UNCOMMENT SECTION" section.
	[ ] 
	[ ] ////////// TEST LOCAL COMMENT SECTION - BEGIN (Start comments here) //////////
	[ ] String SilkTestDir = GetCommonParam( "General","SilkTestDir")
	[ ] String CurrentRelease = GetCommonParam( "General","PSPRelease")
	[ ] String  RootPSPDir = GetCommonParam( "General","RootPSPDir")
	[ ] String WorkingDir = GetCommonParam("General","WorkingDir")
	[ ] // Clear out any test case failure inficators in case a run was killed in the middle.
	[ ] String fullPathWorkDirStr = RootPSPDir+CurrentRelease+"\"+SilkTestDir+"\"+WorkingDir+"\"
	[ ] // The command line used to invoke the application
	[ ] String SAPBaseURL = GetEnvSpecificParam( "General","SAPBaseURL")
	[ ] String internetExplorerEXE = GetClientSpecificParam( "General","InternetExplorerFullPathEXE")
	[ ] ////////// TEST LOCAL COMMENT SECTION - END (Finsih comments here) //////////
	[ ] 
	[ ] ////////// TEST LOCAL UNCOMMENT SECTION - BEGIN (Start uncommenting here) //////////
	[ ] // String fullPathWorkDirStr = "C:\dev\PSP\main\SilkTest\WorkingDir"
	[ ] // String SAPBaseURL = "https://localhost:8443/"
	[ ] // String internetExplorerEXE = "C:\Program Files\Internet Explorer\IEXPLORE.EXE"
	[ ] ////////// TEST LOCAL UNCOMMENT SECTION - END (Finish uncommenting here) //////////
	[ ] 
	[ ] const sCmdLine = """"+internetExplorerEXE+""" "+SAPBaseURL+"SAP/SAPApp.swf"
	[ ] //const sCmdLine = """C:\Program Files\Internet Explorer\IEXPLORE.EXE"" "+SAPBaseURL+"SAP/SAPApp.swf"
	[ ] 
	[ ] // The working directory of the application when it is invoked
	[ ] const sDir = fullPathWorkDirStr
	[+] CustomWin Control3
		[ ] tag "[Control]#3"
		[-] CustomWin Control1
			[ ] tag "[Control]#1"
			[-] FlexApplication FlexApplication1
				[-] multitag "&className=SAPApp"
					[ ] "#1"
				[-] FlexBox OpenCompanies
					[-] multitag "&label=Open Companies&className=psp.sap.view.DDCompanyExplorerView"
						[ ] "Open Companies"
						[ ] "#29"
						[ ] "$companyExplorerView"
					[-] FlexBox GeminiCompany123456789
						[-] multitag "&label=Gemini Company (123456789)&className=psp.sap.view.DDCompanyInspectorView"
							[ ] "Gemini Company (123456789)"
							[ ] "#2"
						[-] FlexDividedBox ContentArea
							[-] multitag "&className=psp.sap.view.GradientVDividedBox"
								[ ] "contentArea"
								[ ] "#96"
								[ ] "$contentArea"
							[-] FlexCanvas ActivationsTab
								[-] multitag "&label=Activations Tab&className=psp.sap.view.SinglePartPageView"
									[ ] "Activations Tab"
									[ ] "#6"
									[ ] "$_DDCompanyInspectorView_SinglePartPageView5"
								[+] FlexBox ActivationsView
									[-] multitag "&label=Activations View&className=psp.sap.view.ActivationsView"
										[ ] "Activations View"
										[ ] "#9"
										[ ] "$_ActivationsTabView_ActivationsView1"
									[-] FlexButton ShowChecklist
										[-] multitag "&className=mx.controls.Button"
											[ ] "Show Checklist"
											[ ] "#13"
											[ ] "$showCheckListButton"
				[+] FlexTitleWindow FullModal
					[+] multitag "&className=psp.sap.view.controls.SizeableTitleWindow"
						[ ] "fullModal"
						[ ] "#35"
						[ ] "$fullModal"
					[+] FlexBox ChecklistGroupItemRowChec
						[-] multitag "&label=ChecklistGroupItemRow&className=psp.sap.view.ActivationsChecklistGroupItemView"
							[ ] "?ChecklistGroupItemRow? ? ChecklistGroupItemRow"
							[ ] "#17"
							[ ] "$_ActivationsChecklistView_ActivationsChecklistGroupItemView1"
						[-] FlexImage _ActivationsChecklistGroupItem
							[-] multitag "&className=mx.controls.Image"
								[ ] "_ActivationsChecklistGroupItemView_Image1"
								[ ] "#4"
								[ ] "$_ActivationsChecklistGroupItemView_Image1"
					[+] FlexBox ChecklistGroupItemRowChec1
						[-] multitag "&label=ChecklistGroupItemRow&className=psp.sap.view.ActivationsChecklistGroupItemView"
							[ ] "?ChecklistGroupItemRow? ? ChecklistGroupItemRow"
							[ ] "#7"
							[ ] "$_ActivationsChecklistView_ActivationsChecklistGroupItemView1"
						[-] FlexImage _ActivationsChecklistGroupItem
							[-] multitag "&className=mx.controls.Image"
								[ ] "_ActivationsChecklistGroupItemView_Image1"
								[ ] "#4"
								[ ] "$_ActivationsChecklistGroupItemView_Image1"
					[+] FlexComboBox ChecklistItem_RAFEnrollment
						[-] multitag "&className=mx.controls.ComboBox"
							[ ] "ChecklistItem_RAF Enrollment"
							[ ] "$checklistCombo"
					[+] FlexComboBox ChecklistItem_TaxAudit
						[-] multitag "&className=mx.controls.ComboBox"
							[ ] "ChecklistItem_Tax Audit"
							[ ] "$checklistCombo"
					[-] FlexButton ActivationsCancelButton
						[-] multitag "&className=mx.controls.Button"
							[ ] "activationsCancelButton"
							[ ] "#46"
							[ ] "$cancelButton"
[ ] 
[-] String GetCommonParam(String paramGroup,String paramName)
	[ ] HINIFILE hIni = IniFileOpen ("..\..\..\QATestConfigurationFiles\client_config\Common.ini")
	[ ] String sValue = IniFileGetValue (hIni, paramGroup,paramName)
	[ ] IniFileClose (hIni)
	[ ] return sValue
[ ] 
[-] String GetClientSpecificParam(String paramGroup,String paramName)
	[ ] LIST OF STRING args = GetArgs()
	[ ] String configFilePath = "..\..\..\QATestConfigurationFiles\client_config\"
	[ ] String filename
	[-] if (ListCount (args) < 2)
		[ ] filename = "Standard.ini"
	[-] else
		[ ] filename = args[2]
	[ ] HINIFILE hIni = IniFileOpen (configFilePath + filename)
	[ ] String sValue = IniFileGetValue (hIni, paramGroup,paramName)
	[ ] IniFileClose (hIni)
	[ ] return sValue
[ ] 
[-] String GetEnvSpecificParam(String paramGroup,String paramName)
	[ ] LIST OF STRING args = GetArgs()
	[ ] String configFilePath = "..\..\..\QATestConfigurationFiles\server_config\"
	[ ] String filename
	[-] if (ListCount (args) == 0)
		[ ] filename = "Local.ini"
	[-] else
		[ ] filename = args[1]
	[ ] HINIFILE hIni = IniFileOpen (configFilePath + filename)
	[ ] String sValue = IniFileGetValue (hIni, paramGroup,paramName)
	[ ] IniFileClose (hIni)
	[ ] 
	[ ] return sValue
	[ ] 
	[ ] 
