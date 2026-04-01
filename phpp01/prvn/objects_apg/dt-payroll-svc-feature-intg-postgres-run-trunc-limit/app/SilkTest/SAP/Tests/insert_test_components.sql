insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
1,
'Company Search Page',
'Company Search Page'
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
2,
'Company Info Tab',
'Company Info'
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
3,
'Login Page',
'Login screen'
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
4,
'Default Settings Page',
Null
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
5,
'Main Page',
'Main SAP page user is taken to when logged in'
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
6,
'Activations Queue Page',
Null
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
7,
'Activations Tab',
Null
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
25,
'Bank Returns',
Null
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
26,
'Payrolls Tab',
'The Payrolls Tab Pages'
);

insert into UI_SECTION
(
UI_SECTION_GSEQ,
UI_SECTION_ID,
DESCRIPTION
)
values
(
27,
'Payroll Transaction List',
'The page which appears when we click  on the "View Transactions" on any transaction in the PayrollTab Main Page.'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
1,
'CompanySearch_LegalNameSearchCriteria',
'Company legal name serch field on company search screen',
1,
'.//FlexTextArea[@windowid=''companyLegalNameTextInput'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
2,
'CompanySearch_LegalNameSearchButton',
'Company legal name serch button on company search screen',
1,
'.//FlexButton[@windowid=''searchLegalNameButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
27,
'LoginPage_LoginUserName',
'Login page username field.',
3,
'.//FlexTextArea[@windowid=''username'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
28,
'LoginPage_LoginPassword',
'Login page password',
3,
'.//FlexTextArea[@windowid=''password'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
29,
'LoginPage_LoginButton',
'Login page login button',
3,
'.//FlexButton[@windowid=''loginButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
30,
'CompanySearch_ResultList',
'First company in search list.',
1,
'.//FlexButton[@automationIndex=''legalName:<ITEM_NUMBER_HERE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
31,
'ActivationsQueueTab',
'Activations Queue',
5,
'.//FlexButton[@caption=''Activations'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
32,
'ActivationsQueue_CompanyLink',
'Company Name link on Activations queue page',
6,
'.//FlexButton[@caption=''companyLink_<VALUE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
33,
'ActivationsTab_TitleBar(LegalNameAddress)_Label',
'Validation that Business Legal Name and Address Legal Name Address Section header is correct',
7,
'.//FlexLabel[@caption=''Business Legal Name and Address'' and @windowid=''titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
51,
'Logout',
'Logout from any screen',
Null,
'.//FlexButton[@caption=''Logout'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
52,
'ActivationsQueue_TableResults',
'Provides Access to Table on the Activations Queue Page',
6,
'.//FlexDataGrid[@windowid=''results'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
53,
'ActivationsTab_Attach_LPOA',
'Click Attach button for Form 8655',
Null,
'.//FlexButton[@caption=''attachButton_LPOA'' and @windowid=''attachButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
56,
'ActivationsTab_Attach_8655',
'Attach button for Form 8655',
7,
'.//FlexButton[@caption=''attachButton_8655'' and @windowid=''attachButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
57,
'ActivationsTab_Attach_FileName',
'Filename to attach',
7,
'.//ComboBox[@windowid=''1148'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
58,
'ActivationsTab_Attach_OpenButton',
'Open Button to attach file',
7,
'.//PushButton[@caption=''Open'' and @windowid=''1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
59,
'ActivationsTab_FormStatus_LPOA',
'Status Link for LPOA form',
Null,
'.//FlexButton[@caption=''statusButton_LPOA'' and @windowid=''statusButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
60,
'ActivationsTab_FormStatus_8655',
'Form Status link for 8655 form',
7,
'.//FlexButton[@caption=''statusButton_8655'' and @windowid=''statusButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
74,
'ActivationsQueue_PageDate',
'Date on Activations Queue Page',
6,
'.//FlexLabel[@windowid=''_ActivationsQueueView_Label2'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
75,
'ActivationsQueue_PageTitleandUser',
'Page Title with Rep name on Activations Queue Page',
6,
'.//FlexLabel[@windowid=''_ActivationsQueueView_Label1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
76,
'ActivationsQueue_FilterDropDown',
'Drop down menu for selecting filter on Activations Queue Page',
6,
'.//FlexComboBox[@caption=''searchTypeCombo'' and @windowid=''searchTypeCombo'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
91,
'PageHeaderValidation',
'Validates the Header on a page',
Null,
'.//FlexLabel[@windowid=''headerLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
92,
'ActivationsQueue_ActivationsTableHeader',
'Validates the name of the table header for Activations Queue',
6,
'.//FlexLabel[@windowid=''_ActivationsQueueView_Label3'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
93,
'ActivationsQueue_TableColumnHeaders',
'Validates the Column name and order on the activations Queue page',
6,
'.//FlexDataGrid[@windowid=''results'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
111,
'ActivationsTab_8655Button',
Null,
7,
'.//FlexButton[@caption=''fullLabelButton_8655'' and @windowid=''fullLabelButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
112,
'ActivationsTab_8655DeleteButton',
Null,
7,
'.//FlexButton[@caption=''deleteButton_8655'' and @windowid=''deleteButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
113,
'Activationstab_LPOAButton',
Null,
7,
'.//FlexButton[@caption=''fullLabelButton_LPOA'' and @windowid=''fullLabelButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
114,
'ActivationsTab_LPOADeleteButton',
Null,
7,
'.//FlexButton[@caption=''deleteButton_LPOA'' and @windowid=''deleteButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
115,
'ActivationsTab_ScrolltoTax Forms',
Null,
7,
'.//FlexBox[@caption=''taxForms'' and @className=''psp.sap.view.controls.Expander'' and @windowid=''taxForms'']|.//FlexCanvas[@caption=''Activations View'' and @className=''psp.sap.view.SinglePartPageView'' and @label=''Activations View'' and @windowid=''actionsView'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
131,
'ActivationsQueue_SortbyColumnHeader',
'Resorts the Activations Queue Results table by clicking on header',
6,
'.//FlexDataGrid[@className=''psp.sap.view.controls.SAPDataGrid'' and @caption=''queuePagingGrid'' and @windowid=''queuePagingGrid'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
132,
'ActivationsTab_PageTitle(Header)_Label',
'Title of Activations Tab Screen',
7,
'.//FlexLabel[@caption=''Activations View'' and @windowid=''headerLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
133,
'ActivationsTab_AssignedTo(Header)_Value',
'AssignedTo Value in the Header Area',
7,
'.//FlexLabel[@windowid=''assignedToLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
134,
'ActivationsTab_ShowCheckList(Header)_Button',
'Show CheckList button',
7,
'.//FlexButton[@windowid=''showCheckListButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
135,
'ActivationsTab_TitleBar_Label',
'Title Bar Label Validation',
7,
'.//FlexLabel[@windowid=''titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
136,
'ActivationsTab_LegalName(LegalNameAddress)_Text',
'LegalName Value of the Company',
7,
'.//FlexLabel[@windowid=''legalNameText'' and @automationIndex=''index:35'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
137,
'ActivationsTab_BusinessName(LegalNameAddress)_Text',
'Business Name Value of the Company',
7,
'.//FlexLabel[@windowid=''businessNameText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
138,
'ActivationsTab_EIN(LegalNameAddress)_Text',
'EIN Value of the Company',
7,
'.//FlexLabel[@windowid=''einText'' and @automationIndex=''index:42'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
139,
'ActivationsTab_PSID(LegalNameAddress)_Text',
'PSID Value of the Company',
7,
'.//FlexLabel[@windowid=''psidText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
140,
'ActivationsTab_LegalAddress(LegalNameAddress)_Text',
'Legal Address for the Company',
7,
'.//FlexLabel[@windowid=''legalAddressText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
141,
'ActivationsTab_ServiceSignUpDate(LegalNameAddress)_Text',
'ServiceSignUpDate for the Company',
7,
'.//FlexLabel[@windowid=''serviceSignUpDateLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
142,
'ActivationsTab_ActivationDate(LegalNameAddress)_Text',
'Activation Date for the Company',
7,
'.//FlexLabel[@windowid=''activationDateLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
143,
'ActivationsQueue_CompanyLink2',
Null,
6,
'.//FlexButton[@caption=''<VALUE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
144,
'ActivationsTab_PayrollAdmin_JobTitle',
'Payroll Admin job title',
7,
'.//FlexLabel[@caption=''contactJobTitleInput_PayrollAdmin'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
145,
'ActivationsTab_PayrollAdmin_Phone',
'Payroll Admin''s phone number',
7,
'.//FlexLabel[@caption=''contactPhoneInput_PayrollAdmin'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
146,
'ActivationsTab_PayrollAdmin_Email',
'Payroll Admin''s email address',
7,
'.//FlexLabel[@caption=''contactEmailInput_PayrollAdmin'' ]'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
147,
'ActivationsTab_PrimaryPrincipal_JobTitle',
'Primary Principal Job Title',
7,
'.//FlexLabel[@caption=''contactJobTitleInput_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
148,
'ActivationsTab_PrimaryPrincipal_Phone',
'Primary Principal''s Phone',
7,
'.//FlexLabel[@caption=''contactPhoneInput_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
149,
'ActivationsTab_PrimaryPrincipal_Email',
'Primary Principal''s email address',
7,
'.//FlexLabel[@caption=''contactEmailInput_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
150,
'ActivationsTab_ContactInformation_Label',
'The Contact Information section label',
7,
'.//FlexLabel[@caption=''Contact Information'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
151,
'ActivationsTab_PayrollAdmin_Header',
'The header for the Payroll Admin role.',
7,
'.//FlexLabel[@caption=''contactRoleNameLabel_PayrollAdmin'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
152,
'ActivationsTab_PayrollAdmin_Name',
'The Payroll Admin''s name.',
7,
'.//FlexLabel[@caption=''contactNameInput_PayrollAdmin'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
153,
'ActivationsTab_PrimaryPrincipal_Header',
'The header for the Primary Principal role.',
7,
'.//FlexLabel[@caption=''contactRoleNameLabel_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
154,
'ActivationsTab_PrimaryPrincipal_Name',
'The name of the Primary Principal.',
7,
'.//FlexLabel[@caption=''contactNameInput_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
155,
'ActivationsTab_SecondaryPrincipal_Header',
'The header for the Secondary Principal role.',
7,
'.//FlexLabel[@caption=''contactRoleNameLabel_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
156,
'ActivationsTab_SecondaryPrincipal_Name',
'The name of the Secondary Principal.',
7,
'.//FlexLabel[@caption=''contactNameInput_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
157,
'ActivationsTab_SecondaryPrincipal_JobTitle',
'The job title for the secondary principal.',
7,
'.//FlexLabel[@caption=''contactJobTitleInput_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
158,
'ActivationsTab_SecondaryPrincipal_Phone',
'The phone number for the Secondary Principal.',
7,
'.//FlexLabel[@caption=''contactPhoneInput_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
159,
'ActivationsTab_SecondaryPrincipal_Email',
'The email of the secondary principal.',
7,
'.//FlexLabel[@caption=''contactEmailInput_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
160,
'CompanyInformation_LegalInformation_Label',
'Label for the Legal Information section',
2,
'.//FlexLabel[@caption=''Legal Information'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
171,
'ActivationsTab_EIN(Header)_Text',
'Text Adjacent to EIN Label in the header of the Activations Tab',
7,
'.//FlexLabel[@windowid=''einText'' and @automationIndex=''index:65'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
172,
'ActivationsTab_LegalName(Header)_Text',
'Text adjacent to Legal Name Label in the Header Area of the Activations Tab',
7,
'.//FlexLabel[@windowid=''legalNameText'' and @automationIndex=''index:20'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
173,
'ActivationsTab_LegalName(Header)_Label',
'The Legal Name label in the header area of the activations tab',
7,
'.//FlexLabel[@caption=''Legal Name:'' and @automationIndex=''index:18'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
174,
'ActivationsTab_CompanyID(Header)_Label',
'The CompanyID label in the header area',
7,
'.//FlexLabel[@caption=''Company ID:''] '
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
175,
'ActivationsTab_EIN(Header)_Label',
'The EIN Label in the header area',
7,
'.//FlexLabel[@caption=''EIN:'' and @automationIndex=''index:63'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
176,
'ActivationsTab_SourceSystem(Header)_Label',
'The Source System Label in the Header Area',
7,
'.//FlexLabel[@caption=''Source System:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
177,
'ActivationsTab_DDStatus(Header)_Label',
'The DDStatus label in the Header area',
7,
'.//FlexLabel[@caption=''DD Status:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
178,
'ActivationsTab_SubStatus(Header)_Label',
'The Sub Status label in the header area',
7,
'.//FlexLabel[@caption=''Sub Status:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
179,
'ActivationsTab_Current(Header)_Label',
'The Current Label in the Header area',
7,
'.//FlexLabel[@caption=''Current'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
180,
'ActivationsTab_BalanceDue(Header)_Label',
'The Balance Due label in the header area',
7,
'.//FlexLabel[@caption=''Balance Due:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
181,
'ActivationsTab_OverDDLimit_Current(Header)_Label',
'The OverDDLimit label in the header area',
7,
'.//FlexLabel[@caption=''Over DD Limit:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
182,
'ActivationsTab_Strikes(Header)_Label',
'The Strikes Label in the Header Area',
7,
'.//FlexLabel[@caption=''Strikes:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
183,
'ActivationsTab_History(Header)_Label',
'The History label in the Header Area',
7,
'.//FlexLabel[@caption=''History'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
184,
'ActivationsTab_Payrolls(Header)_Label',
'The Payrolls Label in the Header Area',
7,
'.//FlexLabel[@caption=''Payrolls:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
185,
'ActivationsTab_OverDDLimit_History(Header)_Label',
'The OverDDLimit Label under History in the Header Area',
7,
'.//FlexLabel[@caption=''Over DD Limit:'' and @automationIndex=''index:58'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
186,
'ActivationsTab_Returns(Header)_Label',
'The Returns Label in the Header Area',
7,
'.//FlexLabel[@caption=''Returns:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
187,
'ActivationsTab_CompanyID(Header)_Text',
'The CompanyID Value in the header Area',
7,
'.//FlexLabel[@windowid=''companyIdText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
188,
'ActivationsTab_SourceSystem(Header)_Text',
'The Source System value in the header area',
7,
'.//FlexLabel[@windowid=''sourceSystemText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
189,
'ActivationsTab_DDStatus(Header)_Text',
'The DDStatus Value in the Header Area',
7,
'.//FlexLabel[@windowid=''ddStatusLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
190,
'ActivationsTab_BalanceDue(Header)_Text',
'The BalanceDue Value in the Header Area',
7,
'.//FlexLabel[@windowid=''companyBalanceLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
191,
'ActivationsTab_OverDDLimit_Current(Header)_Text',
'The OverDD Limit Value under Current  in the Header Area',
7,
'.//FlexLabel[@windowid=''consecutiveLimiViolationCountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
192,
'ActivationsTab_Strikes(Header)_Text',
'The Strikes Value in the Header Area',
7,
'.//FlexLabel[@windowid=''strikeCountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
193,
'ActivationsTab_Payrolls(Header)_Text',
'The Payrolls Value in the Header Section',
7,
'.//FlexLabel[@windowid=''payrollRunCountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
194,
'ActivationsTab_Returns(Header)_Text',
'The Returns Value in the Header Area',
7,
'.//FlexLabel[@windowid=''bankReturnCountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
195,
'ActivationsTab_ShowGeminiView(Header)_Button',
'The ShowGeminiView Button in the Header Area',
7,
'.//FlexButton[@caption=''Show Gemini View'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
196,
'ActivationsTab_Edit(Header)_Link',
'The Edit Link in the Header Area',
7,
'.//FlexButton[@className=''mx.controls.LinkButton'' and @caption=''Edit'' and @windowid=''editButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
197,
'ActivationsTab_CompanyTab(Header)_TabNavigator',
'The Company Tab which shows the entire Activations Tab Screen',
7,
'.//FlexTabNavigator[@caption=''companyTabs'' and @windowid=''companyTabs'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
198,
'ActivationsTab_AssignedTo(Header)_Label',
'AssignedTo Label in the Header Area',
7,
'.//FlexLabel[@caption=''Assigned to:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
199,
'ActivationsTab_Employees(PayrollInformation)_Label',
'The Employees Label under payroll Information section',
7,
'.//FlexLabel[@caption=''Employees:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
200,
'ActivationsTab_Employees(PayrollInformation)_Text',
'The Employees count text under payroll Information section',
7,
'.//FlexLabel[@windowid=''eeCountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
201,
'ActivationsTab_PayrollFrequency(PayrollInformation)_Label',
'The PayrollFrequency Label under payroll Information section',
7,
'.//FlexLabel[@caption=''Payroll Frequency:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
202,
'ActivationsTab_PayrollFrequency(PayrollInformation)_Text',
'The PayrollFrequency Text under payroll Information section',
7,
'.//FlexLabel[@windowid=''payrollFrequencyLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
203,
'ActivationsTab_Priors(PayrollInformation)_Label',
'The Prior label in the Payroll Information section',
7,
'.//FlexLabel[@caption=''Priors?'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
204,
'ActivationsTab_Priors(PayrollInformation)_Text',
'The Prior Text in the Payroll Information section',
7,
'.//FlexLabel[@windowid=''priorsLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
205,
'ActivationsTab_Benefits(PayrollInformation)_Label',
'The Benefits label in the Payroll Information section',
7,
'.//FlexLabel[@caption=''Benefits?'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
206,
'ActivationsTab_Benefits(PayrollInformation)_Text',
'The Benefits text in the Payroll Information section',
7,
'.//FlexLabel[@windowid=''benefitsLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
207,
'ActivationsTab_BAType(PayrollInformation)_Label',
'The BankAccount Type label in the Payroll Information section',
7,
'.//FlexLabel[@caption=''Bank Account Type:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
208,
'ActivationsTab_BAType(PayrollInformation)_Text',
'The BankAccount Type text in the Payroll Information section',
7,
'.//FlexLabel[@windowid=''accountTypeLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
209,
'ActivationsTab_LegalName(LegalNameAddress)_Label',
'Legal Name Label in the Section',
7,
'.//FlexLabel[@caption=''Legal Name:'' and @windowid=''_CompanyDisplayLegalInfoView_Label1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
210,
'ActivationsTab_BALegalName(PayrollInformation)_Label',
'The BankAccount legalname label in the Payroll Information section',
7,
'.//FlexLabel[@caption=''Bank Legal Name:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
211,
'ActivationsTab_BALegalName(PayrollInformation)_Text',
'The BankAccount legalname text in the Payroll Information section',
7,
'.//FlexLabel[@windowid=''bankNameLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
212,
'ActivationsTab_BusinessName(LegalNameAddress)_Label',
'Business Name Label in the Section',
7,
'.//FlexLabel[@caption=''Business Name:'' and @windowid=''_CompanyDisplayLegalInfoView_Label2'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
213,
'ActivationsTab_EIN(LegalNameAddress)_Label',
'EIN Label in the section',
7,
'.//FlexLabel[@caption=''EIN:'' and @windowid=''_CompanyDisplayLegalInfoView_Label3'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
214,
'ActivationsTab_RoutingNumber(PayrollInformation)_Label',
'The Routing Number Label under Payroll Information section',
7,
'.//FlexLabel[@caption=''Routing Number:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
215,
'ActivationsTab_PSID(LegalNameAddress)_Label',
'PSID Label in the section',
7,
'.//FlexLabel[@caption=''PSID:'' and @windowid=''_CompanyDisplayLegalInfoView_Label4'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
216,
'ActivationsTab_RoutingNumber(PayrollInformation)_Text',
'The Routing Number Text under PayrollInformation Section',
7,
'.//FlexLabel[@windowid=''routingNumberLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
217,
'ActivationsTab_ServiceSignUp(LegalNameAddress)_Label',
'ServiceSignUp Label in the section',
7,
'.//FlexLabel[@caption=''Service Sign-up:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
218,
'ActivationsTab_ActivationDate(LegalNameAddress)_Label',
'ActivationDate Label in the section',
7,
'.//FlexLabel[@caption=''Activation Date:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
219,
'ActivationsTab_AccountNumber(PayrollInformation)_Label',
'The Account Number Label under PayrollInformation Section',
7,
'.//FlexLabel[@caption=''Account Number:'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
220,
'ActivationsTab_LegalAddress(LegalNameAddress)_Label',
'Legal Address Label in the section',
7,
'.//FlexLabel[@caption=''Legal Address:'' and @windowid=''_CompanyDisplayLegalInfoView_Label5'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
221,
'ActivationsTab_AccountNumber(PayrollInformation)_Text',
'The Account Number text under PayrollInformation Section',
7,
'.//FlexLabel[@windowid=''accountNumberLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
222,
'ActivationsTab_TitleBar(PayrollInformation)_Label',
'The PayrollInformation Title Bar Label',
7,
'.//FlexLabel[@caption=''Payroll Information'' and @windowid=''titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
223,
'ActivationsTab_PayrollInfo(PayrollInformation)_Label',
'The PayrollInfo Label in the Section',
7,
'.//FlexLabel[@caption=''Payroll Info'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
224,
'ActivationsTab_PayrollBAInfo(PayrollInformation)_Label',
'The PayrollBankAccountInfo Label in the Section',
7,
'.//FlexLabel[@caption=''Payroll Bank Account Info'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
225,
'ActivationsTab_ScrolltoEmployeeInfo',
Null,
7,
'.//FlexBox[@label=''Employees'' and @className=''psp.sap.view.EmployeeInfoView'' and @caption=''Employees'' and @windowid=''employeeList''] | .//FlexCanvas[@caption=''Activations View'' and @className=''psp.sap.view.SinglePartPageView'' and @label=''Activations View'' and @windowid=''actionsView'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
226,
'ActivationsTab_Edit(TaxInfo)_Button',
'The Edit Button in the Tax Information Section',
7,
'.//FlexButton[@className=''mx.controls.Button'' and @caption=''Edit'' and @windowid=''editButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
227,
'ActivationsTab_TitleBar(TaxInfo)_Label',
'The Tax Information Title Bar Label',
7,
'.//FlexLabel[@caption=''Tax Information'' and @windowid=''titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
228,
'ActivationsTab_TaxInfoData(TaxInfo)_DataGrid',
'TaxInfoData DataGrid in the TaxInfo Section',
7,
'.//FlexDataGrid[@caption=''taxInfoData'' and @windowid=''taxInfoData'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
229,
'ActivationsTab_TitleBar(Forms)_Label',
'The Title Bar Label for the Forms Section',
7,
'.//FlexLabel[@caption=''Forms'' and @windowid=''titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
230,
'ActivationsTab_Forms(Forms)_Label',
'The Forms label in the Section',
7,
'.//FlexLabel[@caption=''Forms'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
231,
'ActivationsTab_FormTitle(Forms)_Label',
'The FormTitle Label in the section',
7,
'.//FlexLabel[@caption=''Form Title'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
232,
'ActivationsTab_Status(Forms)_Label',
'The Status Label in the Section',
7,
'.//FlexLabel[@caption=''Status'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
233,
'ActivationsTab_8655(Forms)_Button',
'The 8655 Button in the Section',
7,
'.//FlexButton[@caption=''fullLabelButton_8655'' and @windowid=''fullLabelButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
234,
'ActivationsTab_LPOA(Forms)_Button',
'The LPOA Button in the Section',
7,
'.//FlexButton[@caption=''fullLabelButton_LPOA'' and @windowid=''fullLabelButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
235,
'ActivationsTab_Attach8655(Forms)_Button',
'The Attach8655 Button in the Section',
7,
'.//FlexButton[@caption=''attachButton_8655'' and @windowid=''attachButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
236,
'ActivationsTab_Delete8655(Forms)_Button',
'The Delete8655 Button in the Section',
7,
'.//FlexButton[@caption=''deleteButton_8655'' and @windowid=''deleteButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
237,
'ActivationsTab_AttachLPOA(Forms)_Button',
'The AttachLPOA Button in the Section',
7,
'.//FlexButton[@caption=''attachButton_LPOA'' and @windowid=''attachButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
238,
'ActivationsTab_DeleteLPOA(Forms)_Button',
'The DeleteLPOA Button in the Section',
7,
'.//FlexButton[@caption=''deleteButton_LPOA'' and @windowid=''deleteButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
239,
'ActivationsTab_TitleBar(EmployeeInfo)_Label',
'The Employee Info Title Bar Label',
7,
'.//FlexLabel[@caption=''Employee Info'' and @windowid=''titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
240,
'ActivationsQueue_ShowFilter',
Null,
6,
'.//FlexComboBox[@caption=''searchTypeCombo'' and @windowid=''searchTypeCombo'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
241,
'CompanyInformation_LegalInformation_LegalName',
'The legal name data field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''legalNameText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
242,
'CompanyInformation_LegalInformation_BusinessName',
'The business name data field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''businessNameText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
243,
'CompanyInformation_LegalInformation_EIN',
'The EIN data field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''einText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
244,
'CompanyInformation_LegalInformation_PSID',
'The PSID data field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''psidText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
245,
'CompanyInformation_LegalInformation_LegalAddress',
'The legal address data field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''legalAddressText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
246,
'CompanyInformation_InfoLink',
'The Info Link to get to the Company Information Page',
5,
'.//FlexButton[@caption=''Info'' and @className=''mx.controls.LinkButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
247,
'ActivationsTab_EmployeeGrid(EmployeeInfo)_DataGrid',
'The DataGrid in the EmployeeInfo Section',
7,
'.//FlexDataGrid[@caption=''employeesGrid'' and @windowid=''employeesGrid'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
248,
'ActivationsTab_Menu(Header)_LinkBar',
'The Link Bar which is at the top of the screen i.e which contains "Company Search" "Status Search" "Bank returns" etc as Link Buttons',
7,
'.//FlexLinkBar[@caption=''menuLinkBar'' and @windowid=''menuLinkBar'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
249,
'ActivationsTab_TopicNavigator(Header)_LinkBar',
'The Link Bar which is at the top of the screen i.e which contains "Info" "Payrolls" "Banks" etc as Link Buttons',
7,
'.//FlexLinkBar[@caption=''topicNavigationMenu'' and @windowid=''topicNavigationMenu'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
250,
'ActivationsTab_Activations(Header)_LinkButton',
'The Activations Button which is present on the Link Bar which is on the left side of the page',
7,
'.//FlexButton[@caption=''Activations'' and @automationIndex=''index:5'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
251,
'ActivationsTab_OpenCompanies(Header)_LinkButton',
'The "OpenCompanies" LinkButton which is present on the LinkBar which is at the top of the page',
7,
'.//FlexButton[@caption=''Open Companies'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
252,
'CompanyInformation_LegalInformation_LegalNameLabel',
'The legal name label field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''_CompanyDisplayLegalInfoView_Label1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
253,
'CompanyInformation_LegalInformation_BusinessNameLabel',
'The business name label field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''_CompanyDisplayLegalInfoView_Label2'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
254,
'CompanyInformation_LegalInformation_EINLabel',
'The EIN label field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''_CompanyDisplayLegalInfoView_Label3'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
255,
'CompanyInformation_LegalInformation_PSIDLabel',
'The PSID label field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''_CompanyDisplayLegalInfoView_Label4'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
256,
'CompanyInformation_LegalInformation_LegalAddressLabel',
'The legal address label field on the Company Information section in the Legal Information section.',
2,
'.//FlexLabel[@windowid=''_CompanyDisplayLegalInfoView_Label5'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
257,
'CompanyInformation_ContactInformation_NameLabel',
'The name label field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''_CompanyDisplayContactInfoView_Label1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
258,
'CompanyInformation_ContactInformation_Name',
'The name data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''nameText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
259,
'CompanyInformation_ContactInformation_JobTitleLabel',
'The job title label field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''jobTitleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
260,
'CompanyInformation_ContactInformation_JobTitle',
'The job title data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''jobTitleText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
261,
'CompanyInformation_ContactInformation_FaxLabel',
'The fax label field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''faxLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
262,
'CompanyInformation_ContactInformation_Fax',
'The fax data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''faxNumberText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
263,
'CompanyInformation_ContactInformation_PhoneLabel',
'The phone label field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''phoneLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
264,
'CompanyInformation_ContactInformation_Phone',
'The phone data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''phoneNumberText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
265,
'CompanyInformation_ContactInformation_EmailLabel',
'The email label field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''emailLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
266,
'CompanyInformation_ContactInformation_Email',
'The email data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''emailText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
267,
'CompanyInformation_ContactInformation_AddressLabel',
'The address label field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''addressLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
268,
'CompanyInformation_ContactInformation_AddressLine1',
'The line 1 of the address data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''addressLine1Text'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
269,
'CompanyInformation_ContactInformation_AddressLine2',
'The line 2 of the address data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''addressLine2Text'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
270,
'CompanyInformation_ContactInformation_AddressLine3',
'The line 3 of the address data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''addressLine3Text'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
271,
'CompanyInformation_ContactInformation_AddressLine4',
'The line 4 of the address data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''cityStateAndZipText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
272,
'CompanyInformation_ContactInformation_AddressZipExtension',
'The Zip Code Extension of the address data field on the Company Information section in the Contact Information section.',
2,
'.//FlexLabel[@windowid=''zipCodeExtensionText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
273,
'CompanyInformation_DDSubStatus_Status',
'The Status data field on the Company Information section in the DD Sub. Status section.',
2,
'.//FlexLabel[@windowid=''_CompanyDisplaySubscriptionStatus_DirectDepositStatusLabel1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
274,
'CompanyInformation_DDLimits_PayrollLimitAmount',
'The payroll limit amount data field on the Company Information section in the DD Sub. Status section.',
2,
'.//FlexLabel[@windowid=''defaultPayrollLimitAmountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
275,
Null,
Null,
2,
'.//FlexLabel[@windowid=''defaultEmployeeLimitAmountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
276,
'CompanyInformation_DDLimits_EmployeeLimitAmount',
'The employee limit amount data field on the Company Information section in the DD Sub. Status section.',
2,
'.//FlexLabel[@windowid=''defaultEmployeeLimitAmountLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
277,
'CompanyInformation_AchFundingModel_FundingModelName',
'The funding model name data field on the Company Information section in the ACH Funding Model section.',
2,
'.//FlexLabel[@windowid=''fundingModelNameText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
278,
'CompanyInformation_Strikes_StrikeCount',
'The strike count data field on the Company Information section in the Strike section.',
2,
'.//FlexLabel[@windowid=''strikeCountText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
279,
'CompanyInformation_ResponseTokens_ResponseTokenLabel',
'The response token label field on the Company Information section in the Response Token section.',
2,
'.//FlexLabel[@windowid=''txResponseTokenLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
280,
'CompanyInformation_ResponseTokens_ResponseToken',
'The response token data field on the Company Information section in the Response Token section.',
2,
'.//FlexLabel[@windowid=''currentTokenLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
281,
'CompanyInformation_Strikes_ManageLink',
'The manage link on the Company Information section in the Strike section.',
2,
'.//FlexButton[@windowid=''strikeButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
282,
'ActivationsQueue_Notes_NoNotesButton',
Null,
6,
'.//FlexButton[@caption=''hasNoNotes_<VALUE>'' and @windowid=''_ActivationsQueueView_inlineComponent3_ActionLink1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
283,
'ActivationsQueue_Alert_NoAlert',
Null,
6,
'.//FlexButton[@caption=''alertNoError_<VALUE>'' and @windowid=''_ActivationsQueueView_inlineComponent4_ActionLink1'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
284,
'ActivationsQueue_Notes_Text',
Null,
6,
'.//FlexTextArea[@caption=''noteTextArea'' and @windowid=''noteTextArea'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
285,
'ActivationsQueue_Notes_SaveButton',
Null,
6,
'.//FlexButton[@caption=''Save'' and @windowid=''saveButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
286,
'ActivationsQueue_Notes_CloseButton',
Null,
6,
'.//FlexButton[@caption=''index:14'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
287,
'BankReturns',
Null,
5,
'.//FlexButton[@caption=''Bank Returns'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
288,
'ActivationsTab_PayrollAdmin_JobTitleLabel',
'The label for the Payroll Admin''s Job Title on the Activations Tab.',
7,
'.//FlexLabel[@caption=''jobTitleLabel_PayrollAdmin'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
289,
'ActivationsTab_PayrollAdmin_PhoneLabel',
'The label for the Payroll Admin''s Phone on the Activations Tab.',
7,
'.//FlexLabel[@caption=''contactPhoneLabel_PayrollAdmin'' and @windowid=''contactPhoneLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
290,
'ActivationsTab_PayrollAdmin_EmailLabel',
'The label for the Payroll Admin''s Email on the Activations Tab.',
7,
'.//FlexLabel[@caption=''contactEmailLabel_PayrollAdmin'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
291,
'ActivationsTab_PrimaryPrincipal_JobTitleLabel',
'The label for the Primary Principal''s Job Title on the Activations Tab.',
7,
'.//FlexLabel[@caption=''jobTitleLabel_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
292,
'ActivationsTab_PrimaryPrincipal_PhoneLabel',
'The label for the Primary Principal''s Phone on the Activations Tab.',
7,
'.//FlexLabel[@caption=''contactPhoneLabel_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
293,
'ActivationsTab_PrimaryPrincipal_EmailLabel',
'The label for the Primary Principal''s Email on the Activations Tab.',
7,
'.//FlexLabel[@caption=''contactEmailLabel_PrimaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
294,
'ActivationsTab_SecondaryPrincipal_JobTitleLabel',
Null,
7,
'.//FlexLabel[@caption=''jobTitleLabel_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
295,
'ActivationsTab_SecondaryPrincipal_PhoneLabel',
'The label for the Secondary Principal''s Phone on the Activations Tab.',
7,
'.//FlexLabel[@caption=''contactPhoneLabel_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
296,
'ActivationsTab_SecondaryPrincipal_EmailLabel',
'The label for the Secondary Principal''s Email on the Activations Tab.',
7,
'.//FlexLabel[@caption=''contactEmailLabel_SecondaryPrincipal'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
297,
'ActivationTab_NoNotesButton',
Null,
Null,
Null
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
298,
'ActivationsQueue_Notes_HasNotesButton',
Null,
6,
'.//FlexButton[@caption=''hasNotes_<VALUE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
299,
'ActivationsQueue_NotesWindow_TabNavigator',
'Tab navigator on the notes window.',
6,
'.//FlexTabNavigator[@windowid=''tabNavigator'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
300,
'ActivationsQueue_Notes_NoteTextArea',
Null,
6,
'.//FlexTextArea[@windowid=''noteTextArea'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
301,
'CompanySearchPopup_DropDown',
'The Search Type Drop Down in the Search Page Popup after logging in as an "ADMIN" user.',
1,
'.//FlexMenu[@caption=''index:0'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
302,
'CompanySearchPopup_TextArea',
'Company Search Text Input in the Search page after logging in as ADMIN.',
1,
'.//FlexTextArea[@caption=''searchInput_searchFieldAdvanced'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
303,
'CompanySearchPopup_PopupButton',
'Search Types Button on the Search Page after logging in as ADMIN',
1,
'.//FlexPopUpButton[@caption=''searchTypes_searchFieldAdvanced'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
304,
'CompanySearchPopup_CompanyLink',
'The Company Name link on the Search Page after logging in as the "ADMIN" user',
1,
'.//FlexButton[@automationIndex=''legalName:<VALUE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
306,
'CompanyNavigationMenu_Button',
'The Buttons on Navigation Menu on  the Company page. "Info","Payrolls","Activations" etc... ',
5,
'.//FlexButton[@caption=''<VALUE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
307,
'ActionsColumn_PopupButton',
'The Drop Down in the Actions Column in which we can select which page to go to.',
26,
'.//FlexBox[@automationIndex=''actionCollection:<VALUE>'']<SEPARATOR>.//FlexPopUpButton[@caption=''payrollActionButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
308,
'ActionsColumn_DropDown',
'The Drop Down in the Actions Column in the Main Payrolls Tab page',
26,
'.//FlexMenu[@caption=''index:0'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
309,
'Payroll_Transactions_HeaderLabel',
'The Header label on the payroll transactions page',
27,
'.//FlexLabel[@caption=''<VALUE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
310,
'Payroll_Transactions_Payroll_Button',
'The Payroll Button in the header portion',
27,
'.//FlexButton[@caption=''Payroll'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
311,
'Payroll_Transactions_PayrollTransactionList_Button',
'The "Payroll Transaction List" Button in the Header portion of the page',
27,
'.//FlexButton[@caption=''Payroll Transaction List'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
312,
'CompanySearchButton_SearchPage',
'The search button on the Search page that comes up after LOGIN',
1,
'.//FlexPopUpButton[@caption=''searchTypes_searchFieldAdvanced'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
313,
'Payroll_Transactions_EmployerTransaction_Box',
'The Employer Transaction flex box Panel',
27,
'.//FlexBox[@caption=''employerTransactionsPanel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
314,
'Payroll_Transactions_IntuitTransaction_Box',
'The Intuit Transaction flex box Panel',
27,
'.//FlexBox[@caption=''intuitTransactionsPanel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
315,
'Payroll_Transactions_EmployeeTransaction_Box',
'The Employee Transaction flex box Panel',
27,
'.//FlexBox[@caption=''employeeTransactionsPanel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
316,
'Payroll_Transactions_AgencyTransaction_Box',
'The Agency Transaction flex box Panel',
27,
'.//FlexBox[@caption=''agencyTransactionsPanel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
317,
'Payroll_Transactions_EmployerTransaction_Grid',
'The Employer Transaction Grid',
27,
'.//FlexDataGrid[@caption=''erTxnsData'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
318,
'Payroll_Transactions_EmployeeTransaction_Grid',
'The Employee Transaction Grid',
27,
'.//FlexDataGrid[@caption=''eePaychecksData'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
319,
'Payroll_Transactions_AgencyTransaction_Grid',
'The Agency Transaction Grid',
27,
'.//FlexDataGrid[@caption=''agencyTxnsData'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
320,
'Payroll_Transactions_EmployerTransactions_ViewHistory_ListLabel',
'The ViewHistory List label in the Employer Transactions Data grid',
27,
'.//FlexBox[@caption=''employerTransactionsPanel'']<SEPARATOR>.//FlexListLabel[@caption=''View History'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
321,
'Payroll_Transactions_AgencyTransactions_ViewHistory_ListLabel',
'The ViewHistory List label in the AgencyTransactions Data grid',
27,
'.//FlexBox[@caption=''agencyTransactionsPanel'']<SEPARATOR>.//FlexListLabel[@caption=''View History'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
322,
'Payroll_Transactions_EmployeeTransactions_DirectDeposit_Label',
'The Direct Deposit label in the Employee Transactions Data grid',
27,
'.//FlexBox[@caption=''employeeTransactionsPanel'']<SEPARATOR>.//FlexLabel[@caption=''Direct Deposits_titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
323,
'Payroll_Transactions_EmployeeTransactions_PrintedCheks_Label',
'The Printed checks label in the Employee Transactions Data grid',
27,
'.//FlexBox[@caption=''employeeTransactionsPanel'']<SEPARATOR>.//FlexLabel[@caption=''Printed Checks_titleLabel'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
324,
'Payroll_Transactions_IntuitTransactions_NoIntuitTxns_Label',
'The No intuit Transactions label in the Intuit Transactions Panel',
27,
'.//FlexBox[@caption=''intuitTransactionsPanel'']<SEPARATOR>.//FlexLabel[@windowid=''noIntuitTxnsText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
325,
'Payroll_Transactions_EmployeeTransactions_NoDDTxns_Label',
'The No Direct Deposit Transactions label in the Employee Transactions Panel',
27,
'.//FlexBox[@caption=''employeeTransactionsPanel'']<SEPARATOR>.//FlexLabel[@windowid=''noDDTxnsText'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
326,
'Payroll_Transactions_TransactionsGrid_Label',
'The Title for each type of Payroll Transactions Grid. For example Employee Transactions, Employer Transactions, Intuit Transactions, Agency Transactions',
27,
'.//FlexLabel[@caption=''<VALUE>'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
337,
'Payroll_Transactions_EmployerTransactions_PopupButton',
'The Popup Button in the Employer Transactions Grid',
27,
'.//FlexPopUpButton[@caption=''erTxnsActionButton'']'
);

insert into UI_OBJECT
(
UI_OBJECT_GSEQ,
UI_OBJECT_ID,
DESCRIPTION,
UI_SECTION_GSEQ,
XPATH
)
values
(
338,
'Payroll_Transaction_AgencyTransactions_PopupButton',
'The Popup Button in the Agency Transactions Grid',
27,
'.//FlexPopUpButton[@caption=''agencyTxnsActionButton'']'
);

insert into TEST_CASE_TYPE
(
TEST_CASE_TYPE_GSEQ,
TEST_CASE_TYPE_ID,
DESCRIPTION
)
values
(
1,
'REUSEABLE_COMPONENT',
'Non-Runnable Test Case'
);

insert into TEST_CASE_TYPE
(
TEST_CASE_TYPE_GSEQ,
TEST_CASE_TYPE_ID,
DESCRIPTION
)
values
(
2,
'RUNNABLE_TEST_CASE',
'Runnable Test Case'
);

insert into TEST_CASE_TYPE
(
TEST_CASE_TYPE_GSEQ,
TEST_CASE_TYPE_ID,
DESCRIPTION
)
values
(
3,
'DATA_DRIVEN_TEST_CASE',
'Data-Driven Test Case'
);

insert into TEST_TYPE
(
TEST_TYPE_GSEQ,
TEST_TYPE_ID
)
values
(
1,
'COMPONENT'
);

insert into FUNCTIONAL_AREA
(
FUNCTIONAL_AREA_GSEQ,
FUNCTIONAL_AREA_ID,
DESCRIPTION
)
values
(
2,
'Activations',
'Activations'
);

insert into FUNCTIONAL_AREA
(
FUNCTIONAL_AREA_GSEQ,
FUNCTIONAL_AREA_ID,
DESCRIPTION
)
values
(
3,
'Payroll',
'Payroll'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
2,
'Input',
'Enter input data in field.',
'value contains <value> to be inputted'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
3,
'ChangeFocus',
'Change focus to the field specified.',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
4,
'Type',
'Type the keys provided.',
'Type() the <value> provided.'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
5,
'CustomSelectCompany',
'Select the QB company on the company search page.',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
6,
'VerifyText',
'Select the QB company on the company search page.',
'Verify <field> contains <value>'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
7,
'RunCommand',
'Run the DOS command specified.',
'Run command specified in <value>.'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
8,
'RefreshPage',
'Simulate F9 keypess to refresh data.',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
9,
'CloseBrowser',
'Close IE.',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
10,
'VerifyEnabled',
'Test if button is enabled or disabled based on value passed in.',
'Value should be TRUE or FALSE based on if button is enabled.'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
14,
'SOATest',
'Run the SOA test specified.',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
15,
'Click',
'Perform a click',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
16,
'VerifyFocus',
'Verify specified ui field has focus.',
'Verify <field> has focus.'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
17,
'Select',
'Select item',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
18,
'VerifyDateToday',
'Calculate today''s date and verify that Date displayed is today',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
19,
'AttachFile',
'Enter Filename and tab to Open and press enter',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
20,
'VerifyListLabels',
'Verify that the column headers are spelt correctly and displayed in the correct order',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
37,
'SortbyListLabel',
'Resort Data Grid by clicking on List Label',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
38,
'ClickButton',
'Perform click on a button',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
39,
'FocusonApp',
Null,
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
40,
'ScrollToTopOfObj',
'Scroll to location on page',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
57,
'VerifyExists',
'Verifies is an XPath exists on the page',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
58,
'VerifyButtonLabel',
'Checks the Label on the Button',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
59,
'Sleep',
'Sleep for the certain amount of time',
'Value entered should be a valid number'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
77,
'VerifySelectedChild',
'Checks the Selected Child in  a Tab Navigator Object',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
78,
'ClickGridColumn',
'Click a Column Label of a Data Grid so that the rows get sorted',
'Pass the Column ID for the Data Grid'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
79,
'VerifyDataGridContents',
'Compares the returned data grid contents against value',
'Value entered should be the expected contents of datagrid'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
80,
'Change',
'Calls the Change().  I currently have only seen this used on tab navigators.',
Null
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
81,
'SetFocus',
'Calls the DoSetFocus(). Sets the focus to a particular control.',
'Especially useful when we need to simulate keystrokes to enter a value in a popup window.'
);

insert into TEST_OPERATION
(
TEST_OPERATION_GSEQ,
TEST_OPERATION_ID,
DESCRIPTION,
USAGE_NOTES
)
values
(
82,
'Open',
'Calls DoOpen(). It just expands the Drop Down associated to a  FlexPopupButton Object.',
Null
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
4,
'Delete_Company',
'DeleteCompanies.tst',
'Deletes a company'
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
24,
'CreateERDDDBReturn_Resolved',
'QBOE-ERDDB_Resolved.tst',
'Setup QBOE ERDDB Resolved.'
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
25,
'AddGeminiCompany_AssignAgent',
'GeminiAddCompany.tst',
'Setup Gemini Company only'
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
26,
'AddGeminiCACompany_AssignAgent',
'GeminiAddCompany_CA.tst',
'Setup Gemini Company with CA and Assign to Agent'
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
45,
'Add4GeminiCompaniesforScreenValidation',
'GeminiAdd_4_Companies.tst',
'Setup 4 Gemini Companies in different states'
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
65,
'GeminiAddCompanyWithActiveBA_CA',
'GeminiAddCompanyWithActiveBA_CA.tst',
'Setup Gemini Company with CA ,Active BA  and assign to an agent'
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
66,
'GeminiAdd_2_Companies_Emp_CA',
'GeminiAdd_2_Companies_Emp_CA.tst',
'Setup 2 Gemini Companies with different states'
);

insert into SOA_TEST
(
SOA_TEST_GSEQ,
SOA_TEST_ID,
FILENAME,
DESCRIPTION
)
values
(
67,
'Gemini_Multiple_Payrolls_CA_Diff_Emp',
'Gemini_Multiple_Payrolls_CA_Diff_Emp.tst',
'Setup Company with Employees and create some paychecks'
);

