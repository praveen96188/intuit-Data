The following distribution contains is a SOAP ui project that demonstrates 
how to send an email using the Notification Service API via SOAPUI tool.

This directory where this SOAPUI project file is unpacked is referred to in this document as [SOAPUI_PROJECT_DIR]

Specific information for you:

  1. Your SOAPUI (referred to in this document as [SOAPUI_PROJECT_FILE]) project file is: 

  PSP-soapui-project.xml


  2. The SOAPUI project (referred to in this document as [SOAPUI_PROJECT_NAME]) is called: 

  PSP
 
  3. Your unique senderID (discussed below) is

  PSP



This soapUI project requires SOAPUI 2.0.2 or higher.
SOAPUI is available from:  http://soapui.org/
 

To use this project:
1. Start SOAPUI
2. Go to File->Import Project
3. and select the [SOAPUI_PROJECT_FILE]

Next configure your certificate that soapUI will use
1. Go to File->Preferences->SSL Settings
2. In the KeyStore entry, click Browse and go to [SOAPUI_PROJECT_DIR]/cert/[CERTNAME]/[CERTNAME].jks,
3. For keyStore Password, enter "intuit" for all pre-production environment certificate.
4. Click ok.


Now, navigate to the sample SOAPUI project

In the Workspace tab, you should see a project [SOAPUI_PROJECT_NAME]
This represents an example XML SOAP request to send an email

1. Select [SOAPUI_PROJECT_NAME] and in the lower left hand corner, click on the "Test properties" tab
2. The test properties tab shows you two properties. You normally shouldn't have to change this, 
but for informational purposes:
a) env.url: refers to the NotificationService URL. It should refer to the E2E environment
b) senderID: is a uniqueID we assigned to you to send notification, as listed above. Please ensure all
   requests you send includes this senderID in the message. The example soapUI project refers to this variable
   inside the XML request.


1. Go to [SOAPUI_PROJECT_NAME]->NotificationSoapBinding->Send->Request 1. 
This represents an example request to send an email
a)  <SenderId> identifies who you are. It leverages the senderID variable set above.
b)  <ContentId> identifies the email content you are sending. In exact target terms, this
    is the name of the TriggerSend definition setup to send a specific email template
c)  <TemplateProperties> in the <ContentProfile> specifies name/value pairs that apply 
    to email that all receipients of this request will receive. In this example, the "OfferingName"
    attribute refers to the name of the product and will appear in the email sent to all specified recipient
    in this request
d)  <Destination> is one or more elements, each representing 1 email recipient
e)  <Format> specifies whether the email should be sent as HTML or Text
f)  <TemplateProperties> inside the Destination specifies attribute name/value pairs that apply specifically
    to the recipient. For example, the FullName attribute represents the first and last name of the
    recipient.


--------------------------------------

Corresponding Setup In Exact Target

An email creator account has been setup for you in ExactTarget
Please go to http://email.exacttarget.com/ to login


1) Your email login is: pspemailcreator@intuit.com
2) Your initial password is $PSP2008. 


Please change this password as soon as possible by going to 
Settings at the top right corner and clicking the edit link.


The following example email setup has been created in your account
1) First, we have defined two template attributes for you: OfferingName and FullName
To add/update template attributes
  a) Go to Subscribers in the lower left hand corner
  b) Click on Profile Management in the left sidebar

2) An HTML based email called TEST_EMAIL_HTML has been created for you.
To add/update/view email
  a) Click on the "Content" in the lower left hand corner
  b) Click on "my emails"
  c) The email contains two template attributes: OfferingName in the subject, and FullName in the body.
  d) Attributes are designed as %%Name%%. 
     If there are special characters in the attribute, it should be designed with brackets such as
     %%[Offering_Name]%%

3) For exact target to send off this email template, you need to create a "trigger send".
One has already been created for you.
  a) Click on Interations in the lower left hand corner
  b) Click on Messages->Email->Triggered in the left sidebar
  c) You will see a list of trigger send definitions
  d) One called TEST_EMAIL_HTML has been created to send the TEST_EMAIL_HTML email. 
     Click on the definition
  e) The External Key (TEST_EMAIL_HTML) is the identifier you specify in the Send API request to 
     send this email. For simplicity, we set the Trigger Send's name, the external key, and the email template
     name to be the same (TEST_EMAIL_HTML)


If there are any questions, please contact the Notification Service Team for assistance.



