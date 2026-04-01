<?xml version="1.0" encoding="UTF-8" ?>
<stylesheet version="1.0" xmlns="http://www.w3.org/1999/XSL/Transform">
<output method="text"/>

<template match="AgencyRules">// Generated from xml, do not edit

package com.intuit.payroll.agency.dao.generated;

import com.intuit.payroll.agency.impl.*;
import com.intuit.payroll.agency.dao.*;
import com.intuit.payroll.agency.dao.mnemonics.MnemonicPeriod;

public class <value-of select="FileName"/> implements IDataSource{

	private IDataStore dataStore;

	public <value-of select="FileName"/> () {
	}

	public void addToDataStore(IDataStore ds) {
       	this.dataStore = ds;
	
		createJurs ();
		createAgencies ();
		createTemplates ();
		createHolidayGroups ();
		createFormTemplateGroups ();
		createFormTemplates () ;
		createEnrollmentGroups ();
		
<if test="EFERules/DueDatePadding/TimeZone">
		EfeRules efeRules = new EfeRules ();
		efeRules.setTimeZone("<value-of select="EFERules/DueDatePadding/TimeZone"/>");
		efeRules.setTimeDue("<value-of select="EFERules/DueDatePadding/TimeDue"/>");
		efeRules.setDaysOffsetFromAgencyDueDate(<value-of select="EFERules/DueDatePadding/DaysOffsetFromAgencyDueDate"/>);
//		efeRules.setSubmitMethodType("<value-of select="EFERules/DueDatePadding/SubmitMethodType"/>");
		dataStore.setEfeRules (efeRules);
</if>
		
		dataStore.setVersion ("<value-of select="VersionNumber"/>");
	}

	private void createJurs () 
	{
<apply-templates select="Jurisdiction/JurisdictionID"/>
	}

	private void createAgencies () 
	{
<apply-templates select="Jurisdiction/Agency/AgencyID"/>
	}

	private void createTemplates () 
	{
<apply-templates select="Jurisdiction/Agency/PaymentTemplate/PaymentTemplateID"/>
	}

	private void createHolidayGroups () 
	{
<apply-templates select="HolidayGroup/HolidayGroupID"/>
	}

	private void createFormTemplateGroups () 
	{
<apply-templates select="Jurisdiction/Agency/FormGroup/FormGroupID"/>
	}

	private void createFormTemplates () 
	{
<apply-templates select="Jurisdiction/Agency/FormTemplate/FormTemplateID"/>
	}

	private void createEnrollmentGroups () 
	{
<apply-templates select="EnrollmentGroup/EnrollmentGroupID"/>
	}


<apply-templates select="Jurisdiction"/>

<apply-templates select="Jurisdiction/Agency"/>

<apply-templates select="Jurisdiction/Agency/PaymentTemplate"/>

<apply-templates select="HolidayGroup"/>

<apply-templates select="Jurisdiction/Agency/FormGroup"/>

<apply-templates select="Jurisdiction/Agency/FormTemplate"/>

<apply-templates select="EnrollmentGroup"/>


}
</template>

<template match="PaymentTemplateID">		dataStore.addPaymentTemplate(createPaymentTemplate<value-of select="translate(current(),'-','_')"/>());
</template>

<template match="AgencyID">		dataStore.addAgency (createAgency<value-of select="translate(current(),'-','_')"/>());
</template>

<template match="JurisdictionID">		dataStore.addJurisdiction (createJur<value-of select="."/>());
</template>

<template match="HolidayGroupID">		dataStore.addHolidayGroup (createHolidayGroup<value-of select="translate(current(),'-','_')"/>());
</template>

<template match="FormGroupID">		dataStore.addFormTemplateGroup (createFormTemplateGroup<value-of select="translate(current(),'-','_')"/>());
</template>

<template match="FormTemplateID">		dataStore.addFormTemplate (createFormTemplate<value-of select="translate(current(),'-','_')"/>());
</template>

<template match="EnrollmentGroupID">		dataStore.addEnrollmentGroup (createEnrollmentGroup<value-of select="translate(current(),'-','_')"/>());
</template>


<template match="Jurisdiction">
	private Jurisdiction createJur<value-of select="JurisdictionID"/> ()
	{
		Jurisdiction result = new Jurisdiction();
		result.setJurisdictionID("<value-of select="JurisdictionID"/>");
<if test="IsObsolete">
		result.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
		result.setDescription("<value-of select="UIString"/>");
		result.setStateID("<value-of select="StateID"/>");
		return result;
	}
</template>

<template match="Agency">
	private Agency createAgency<value-of select="translate(AgencyID,'-','_')"/> ()
	{
		Agency result = new Agency();
		result.setAgencyID("<value-of select="AgencyID"/>");
        result.setAgencyAbbrev("<value-of select="AgencyAbbrev"/>");
<if test="IsObsolete">
		result.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
		result.setDescription("<value-of select="UIString"/>");
<if test="Name">
		result.setName("<value-of select="Name"/>");
</if>
		result.setJurisdictionID("<value-of select="../JurisdictionID"/>");
		return result;
	}
</template>

<template match="PaymentTemplate">
	private PaymentTemplateData createPaymentTemplate<value-of select="translate (PaymentTemplateID,'-','_')"/> () 
	{
		PaymentTemplateData result = new PaymentTemplateData ();
		result.setPaymentTemplateID("<value-of select="PaymentTemplateID"/>");
		result.setPaymentTemplateAbbrev("<value-of select="PaymentTemplateAbbrev"/>");
		result.setAgencyID("<value-of select="../AgencyID"/>");
<if test="IsObsolete">
		result.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
		result.setDescription("<value-of select="UIString"/>");
		result.setLongDescription("<value-of select="UIDescription"/>");
		result.setDefaultPaymentFrequencyID("<value-of select="DefaultPaymentFrequency"/>");
		result.setDefaultSubmitMethodID("<value-of select="DefaultSubmitMethod"/>");
		result.setRollDueDateOnHoliday("<value-of select="RollDueDateOnHoliday"/>");
		result.setRollDueDateOnWeekend("<value-of select="RollDueDateOnWeekend"/>");
		result.setHolidayGroupID("<value-of select="HolidayGroupID"/>");

<if test="WageRounding">
		result.setWageRounding("<value-of select="WageRounding"/>");
</if>
<if test="PaymentAmountRounding">
		result.setPaymentAmountRounding("<value-of select="PaymentAmountRounding"/>");
</if>
<if test="ReconciliationFrequency">
		result.setReconciliationFrequency("<value-of select="ReconciliationFrequency"/>");
</if>
<if test="UsesFrequencyOf">
		result.setUsesFrequencyOf("<value-of select="UsesFrequencyOf"/>");
</if>
<if test="PaymentMaySpanReconciliationPeriods">
		result.setPaymentMaySpanReconciliationPeriods("<value-of select="PaymentMaySpanReconciliationPeriods"/>");
</if>
//nyi	SharesFrequenciesWith
//nyi	MustSubmitWithForm

		FrequencyData freq;
		MnemonicPeriod period;
		PaymentPeriod emergencyPeriod;
		SubmitMethodData method;
		PaymentReason paymentReason;
		UpperLimit upperLimit;
        AgencyFormat agencyFormat;

<for-each select="AgencyFormat">
		agencyFormat = new AgencyFormat ();
		agencyFormat.setFormat("<value-of select="Format"/>");
        result.addAgencyFormat (agencyFormat);
</for-each>

    <for-each select="SubmitMethod">
		method = new SubmitMethodData ();
		method.setSubmitMethodType("<value-of select="SubmitMethodType"/>");
		method.setTemplateID("<value-of select="../PaymentTemplateID"/>");
		method.setIsObsolete("<value-of select="IsObsolete"/>");
		method.setDescription("<value-of select="UIString"/>");
		method.setEnrollmentGroupID("<value-of select="EnrollmentGroupID"/>");
		method.setPaymentsRequireBankAccountInfo("<value-of select="PaymentsRequireBankAccountInfo"/>");
		method.setDefaultPaymentReasonCode("<value-of select="DefaultPaymentReasonCode"/>");
<if test="AllowNegativeLineItems">
		method.setIsNegativeLineItemAllowed("<value-of select="AllowNegativeLineItems"/>");
</if>
<if test="SendByOffset">
		method.setSendByOffset( <value-of select="SendByOffset"/>);
</if>
<if test="ReleaseStatus">
		method.setReleaseStatus("<value-of select="ReleaseStatus"/>");
</if>
<if test="HowIsDueDateDefined">
		method.setHowIsDueDateDefined("<value-of select="HowIsDueDateDefined"/>");
</if>

	<for-each select="PaymentReason">
		paymentReason = new PaymentReason();
		paymentReason.setPaymentReasonCode("<value-of select="PaymentReasonCode"/>");
<if test="IsObsolete">
		paymentReason.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
		paymentReason.setDescription("<value-of select="UIString"/>");
		paymentReason.setIsTaxItemAllowed("<value-of select="PayItemAllowed"/>");
		paymentReason.setIsPenaltyAllowed("<value-of select="PenaltyAllowed"/>");
		paymentReason.setIsInterestAllowed("<value-of select="InterestAllowed"/>");
		paymentReason.setIsOtherExpenseAllowed("<value-of select="OtherExpenseAllowed"/>");
		method.addPaymentReason(paymentReason);
	</for-each>
<if test="NegativeOKForTaxID">
	<for-each select="NegativeOKForTaxID">
		method.addNegativeOKForTaxID("<value-of select="."/>");
	</for-each>
</if>
<if test="Settlement">
		SettlementData settlementData = new SettlementData();
		settlementData.setUIString("<value-of select="Settlement/UIString"/>");
		settlementData.setMinOffset("<value-of select="Settlement/MinOffset"/>");
		settlementData.setMaxOffset("<value-of select="Settlement/MaxOffset"/>");
	<if test="Settlement/ThisMonth">
		settlementData.setThisMonth("<value-of select="Settlement/ThisMonth"/>");
	</if>
	<if test="Settlement/ThisQuarterOrFirstMonthOfNextQuarter">
		settlementData.setThisQuarterOrFirstMonthOfNextQuarter("<value-of select="Settlement/ThisQuarterOrFirstMonthOfNextQuarter"/>");
	</if>
		method.addSettlementData(settlementData);
</if>
			
		result.addSubmitMethod (method);
</for-each>

<for-each select="PaymentFrequency">
		freq = new FrequencyData ();
		freq.setFrequencyID("<value-of select="PaymentFrequencyID"/>");
		freq.setName("<value-of select="UIString"/>");
		freq.setDescription("<value-of select="UIDescription"/>");
		freq.setTemplateID("<value-of select="../PaymentTemplateID"/>");
		freq.setIsObsolete("<value-of select="IsObsolete"/>");
<if test="DisallowYearCrossing">
		freq.setDisallowYearCrossing( <value-of select="DisallowYearCrossing"/>);
</if>
<if test="DisallowQuarterCrossing">
		freq.setDisallowQuarterCrossing( <value-of select="DisallowQuarterCrossing"/>);
</if>
<if test="DisallowMonthCrossing">
        freq.setDisallowMonthCrossing( <value-of select="DisallowMonthCrossing"/>);
</if>
<if test="AddHolidayAllowanceToDueDate">
		freq.setAddHolidayAllowanceToDueDate( <value-of select="AddHolidayAllowanceToDueDate"/>);
</if>
<if test="ZeroPaymentRequired">
		freq.setZeroPaymentRequired( <value-of select="ZeroPaymentRequired"/>);
</if>
<if test="ZeroPaymentMethod">
//		freq.setZeroPaymentMethod( <value-of select="ZeroPaymentMethod"/>);
</if>
<if test="AccrualFrequency">
//		freq.setAccrualFrequency( <value-of select="AccrualFrequency"/>);
</if>
<if test="SplitPeriodCrossingPaymentsDueTogether">
//		freq.setSplitPeriodCrossingPaymentsDueTogether( <value-of select="SplitPeriodCrossingPaymentsDueTogether"/>);
</if>
		result.addPaymentFrequency (freq);
	<for-each select="MnemonicDepositPeriod">
		period = new MnemonicPeriod ();
		period.uiString = "<value-of select="UIString"/>";
		period.start = "<value-of select="PeriodStart"/>";
		period.end = "<value-of select="PeriodEnd"/>";
		period.dueOn = "<value-of select="DueOn"/>";
		freq.addPeriod (period);
	</for-each>
	<for-each select="UpperLimit">
		upperLimit = new UpperLimit ();
		upperLimit.amount = "<value-of select="Amount"/>";
		upperLimit.rollOverFrequency = "<value-of select="RollOverFrequency"/>";
		upperLimit.permanentPaymentFrequency = "<value-of select="PermanentPaymentFrequency"/>";
		freq.addUpperLimit (upperLimit);
	</for-each>
	<for-each select="EmergencyDateOverride">
		emergencyPeriod = new PaymentPeriod();
		emergencyPeriod.setFromAccrualDate("<value-of select="PeriodStart"/>");
		emergencyPeriod.setToAccrualDate("<value-of select="PeriodEnd"/>");
		emergencyPeriod.setDueDate("<value-of select="DueOn"/>");
		emergencyPeriod.setUIString("<value-of select="UIString"/>");
		emergencyPeriod.parseUIString();
		freq.addEmergencyDateOverride (emergencyPeriod);
	</for-each>
</for-each>
		LawData law;
<for-each select="Law">
		law = new LawData();
		law.setLawID (<value-of select="LawID"/>);
		law.setDescription ("<value-of select="Description"/>");
		law.setLawAbbrev ("<value-of select="LawAbbrev"/>");
		result.addLaw (law);
</for-each>
		
		return result;		
	}
</template>

<template match="HolidayGroup">
	private HolidayGroup createHolidayGroup<value-of select="HolidayGroupID"/> ()
	{
		HolidayGroup result = new HolidayGroup();
		result.setHolidayGroupID("<value-of select="HolidayGroupID"/>");
		result.setExtendsHolidayGroupID("<value-of select="ExtendsHolidayGroupID"/>");
		AgencyHoliday holiday;
	<for-each select="Holiday">
		holiday = new AgencyHoliday();
		holiday.setHolidayDate("<value-of select="Date"/>");
		holiday.setUIString("<value-of select="UIString"/>");
		result.addHoliday(holiday);
	</for-each>
		return result;
	}
</template>

<template match="FormTemplate">
	private FormTemplateData createFormTemplate<value-of select="translate (FormTemplateID,'-','_')"/> () 
	{
		FormTemplateData result = new FormTemplateData();
		result.setFormTemplateID("<value-of select="FormTemplateID"/>");
		result.setAgencyID("<value-of select="../AgencyID"/>");
<if test="IsObsolete">
		result.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
		result.setName("<value-of select="UIString"/>");
		result.setDescription("<value-of select="UIDescription"/>");
		result.setIsRequired("<value-of select="IsRequired"/>");
<if test="FormGroupID">
		result.setRulesFormTemplateGroupID("<value-of select="FormGroupID"/>");
</if>
		result.setFormID("<value-of select="FormID"/>");
<if test="DefaultSubmitMethod">
		result.setDefaultSubmitMethodID("<value-of select="DefaultSubmitMethod"/>");
</if>
//nyi		result.setEPayersMustFile("<value-of select="EPayersMustFile"/>");
<if test="AssociatedPaymentTemplateID">
//		result.setAssociatedPaymentTemplateID("<value-of select="AssociatedPaymentTemplateID"/>");
</if>
<if test="SameFrequencyAsPayment">
//		result.setSameFrequencyAsPayment("<value-of select="SameFrequencyAsPayment"/>");
</if>
<if test="FormGoodForYear">
//nyi		result.setFormGoodForYear("<value-of select="FormGoodForYear"/>");
</if>
<if test="RollDueDateOnHoliday">
//nyi		result.setRollDueDateOnHoliday("<value-of select="RollDueDateOnHoliday"/>");
</if>
<if test="RollDueDateOnWeekend">
//nyi		result.setRollDueDateOnWeekend("<value-of select="RollDueDateOnWeekend"/>");
</if>
<if test="HolidayGroupID">
//nyi		result.setHolidayGroupID("<value-of select="HolidayGroupID"/>");
</if>

		FrequencyData freq;
		MnemonicPeriod period;
		LawData law;
<for-each select="Law">
		law = new LawData();
		law.setLawID (<value-of select="LawID"/>);
		law.setDescription ("<value-of select="Description"/>");
		result.addLaw (law);
</for-each>

<for-each select="FormFrequency">
		freq = new FrequencyData ();
		freq.setFrequencyID("<value-of select="FormFrequencyID"/>");
		freq.setName("<value-of select="UIString"/>");
		freq.setDescription("<value-of select="UIDescription"/>");
		freq.setTemplateID("<value-of select="../FormTemplateID"/>");
<if test="IsObsolete">
		freq.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
<if test="AddHolidayAllowanceToDueDate">
		freq.setAddHolidayAllowanceToDueDate( <value-of select="AddHolidayAllowanceToDueDate"/>);
</if>
		result.addFrequency (freq);
	<for-each select="MnemonicFilingPeriod">
		period = new MnemonicPeriod ();
		period.uiString = "<value-of select="UIString"/>";
		period.start = "<value-of select="PeriodStart"/>";
		period.end = "<value-of select="PeriodEnd"/>";
		period.dueOn = "<value-of select="DueOn"/>";
		freq.addPeriod (period);
	</for-each>
</for-each>

		SubmitMethodData method = new SubmitMethodData();
<for-each select="SubmitMethod">
		method = new SubmitMethodData ();
		method.setSubmitMethodType("<value-of select="SubmitMethodType"/>");
		method.setTemplateID("<value-of select="../FormTemplateID"/>");
<if test="IsObsolete">
		method.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
<if test="UIString">
		method.setDescription("<value-of select="UIString"/>");
</if>
		method.setEnrollmentGroupID("<value-of select="EnrollmentGroupID"/>");
<if test="SendByOffset">
		method.setSendByOffset( <value-of select="SendByOffset"/>);
</if>
<if test="ReleaseStatus">
		method.setReleaseStatus("<value-of select="ReleaseStatus"/>");
</if>
			
		result.addSubmitMethod (method);
</for-each>
		return result;
	}
</template>		

<template match="FormGroup">
	private FormTemplateGroup createFormTemplateGroup<value-of select="translate (FormGroupID,'-','_')"/> () 
	{
		FormTemplateGroup result = new FormTemplateGroup();
		result.setFormTemplateGroupID("<value-of select="FormGroupID"/>");
<if test="IsObsolete">
		result.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
		result.setShortDescription("<value-of select="UIString"/>");
		result.setLongDescription("<value-of select="UIDescription"/>");
		return result;
	}
</template>

<template match="EnrollmentGroup">
	private EnrollmentGroupData createEnrollmentGroup<value-of select="translate (EnrollmentGroupID,'-','_')"/> () 
	{
		EnrollmentGroupData result = new EnrollmentGroupData();
		CredentialFormLogin login;
		CredentialFormHeader header;
		CredentialFormInputField inputField;
		CredentialFormFooter footer;
		
		result.setEnrollmentGroupID("<value-of select="EnrollmentGroupID"/>");
<if test="IsObsolete">
		result.setIsObsolete("<value-of select="IsObsolete"/>");
</if>
		result.setUIString("<value-of select="UIString"/>");
		result.setUIDescription("<value-of select="UIDescription"/>");
		result.setEnrollmentRequired("<value-of select="EnrollmentRequired"/>");
		result.setEnrollViaWebsite("<value-of select="EnrollViaWebsite"/>");

<if test="WebSiteEnrollmentURL">
		result.setWebSiteEnrollmentURL("<value-of select="WebSiteEnrollmentURL"/>");
</if>
<if test="WebSiteInstructionsFileName">
		result.setWebSiteInstructions("<value-of select="WebSiteInstructionsFileName"/>");
</if>

<if test="EnrollmentCredentialData">
		CredentialData ecd = new CredentialData();
<if test="EnrollmentCredentialData/Window-Title">
		ecd.setWindowTitle("<value-of select="EnrollmentCredentialData/Window-Title"/>");
</if>
<if test="EnrollmentCredentialData/Header">
		header = new CredentialFormHeader();
	<if test="EnrollmentCredentialData/Header/TitleLabel">
		header.setTitleLabel("<value-of select="EnrollmentCredentialData/Header/TitleLabel"/>");
	</if>
	<if test="EnrollmentCredentialData/Header/AgencyLabel">
		header.setAgencyLabel("<value-of select="EnrollmentCredentialData/Header/AgencyLabel"/>");
	</if>
	<if test="EnrollmentCredentialData/Header/PeriodLabel">
		header.setPeriodLabel("<value-of select="EnrollmentCredentialData/Header/PeriodLabel"/>");
	</if>
	<if test="EnrollmentCredentialData/Header/AmountLabel">
		header.setAmountLabel("<value-of select="EnrollmentCredentialData/Header/AmountLabel"/>");
	</if>
	<if test="EnrollmentCredentialData/Header/SettlementDateLabel">
		header.setSettlementDateLabel("<value-of select="EnrollmentCredentialData/Header/SettlementDateLabel"/>");
	</if>
	<if test="EnrollmentCredentialData/Header/ReasonLabel">
		header.setReasonLabel("<value-of select="EnrollmentCredentialData/Header/ReasonLabel"/>");
	</if>
		ecd.setHeader(header);
</if>

		login = new CredentialFormLogin();
<if test="EnrollmentCredentialData/Login/TitleLabel">
		login.setTitleLabel("<value-of select="EnrollmentCredentialData/Login/TitleLabel"/>");
</if>
		login.setLoginText("<value-of select="EnrollmentCredentialData/Login/LoginText"/>");
<if test="EnrollmentCredentialData/Login/EnrollText">
		login.setEnrollText("<value-of select="EnrollmentCredentialData/Login/EnrollText"/>");
</if>

<if test="EnrollmentCredentialData/Login/InputField">
<for-each select="EnrollmentCredentialData/Login/InputField">
		inputField = new CredentialFormInputField();
		inputField.setFieldID("<value-of select="FieldID"/>");
<if test="FieldType">
		inputField.setFieldType("<value-of select="FieldType"/>");
</if>
<if test="PossibleFieldValues">
	<for-each select="PossibleFieldValues/Value">
		inputField.addPossibleFieldValue("<value-of select="."/>");
	</for-each>
</if>
<if test="MinLength">
		inputField.setMinimumLength(<value-of select="MinLength"/>);
</if>
<if test="Length">
		inputField.setLength(<value-of select="Length"/>);
</if>
		inputField.setLabel("<value-of select="Label"/>");
<if test="IsObscure">
		inputField.setIsObscure("<value-of select="IsObscure"/>");
</if>
<if test="NumericOnly">
		inputField.setIsNumericOnly("<value-of select="NumericOnly"/>");
</if>
		inputField.setIsRequired("<value-of select="IsRequired"/>");

		login.addInputField(inputField);
</for-each>
</if>
	
		ecd.setLogin(login);

		footer = new CredentialFormFooter();
		footer.setAuthorizeText("<value-of select="EnrollmentCredentialData/Footer/AuthorizeText"/>");
		ecd.setFooter(footer);
		
		result.setEnrollmentCredentialData(ecd);
</if>


<if test="SubmissionCredentialData">
		CredentialData scd = new CredentialData();
<if test="SubmissionCredentialData/Window-Title">
		scd.setWindowTitle("<value-of select="SubmissionCredentialData/Window-Title"/>");
</if>
<if test="SubmissionCredentialData/Header">
		header = new CredentialFormHeader();
	<if test="SubmissionCredentialData/Header/TitleLabel">
		header.setTitleLabel("<value-of select="SubmissionCredentialData/Header/TitleLabel"/>");
	</if>
	<if test="SubmissionCredentialData/Header/AgencyLabel">
		header.setAgencyLabel("<value-of select="SubmissionCredentialData/Header/AgencyLabel"/>");
	</if>
	<if test="SubmissionCredentialData/Header/PeriodLabel">
		header.setPeriodLabel("<value-of select="SubmissionCredentialData/Header/PeriodLabel"/>");
	</if>
	<if test="SubmissionCredentialData/Header/AmountLabel">
		header.setAmountLabel("<value-of select="SubmissionCredentialData/Header/AmountLabel"/>");
	</if>
	<if test="SubmissionCredentialData/Header/SettlementDateLabel">
		header.setSettlementDateLabel("<value-of select="SubmissionCredentialData/Header/SettlementDateLabel"/>");
	</if>
	<if test="SubmissionCredentialData/Header/ReasonLabel">
		header.setReasonLabel("<value-of select="SubmissionCredentialData/Header/ReasonLabel"/>");
	</if>
		scd.setHeader(header);
</if>

		login = new CredentialFormLogin();
<if test="SubmissionCredentialData/Login/TitleLabel">
		login.setTitleLabel("<value-of select="SubmissionCredentialData/Login/TitleLabel"/>");
</if>
		login.setLoginText("<value-of select="SubmissionCredentialData/Login/LoginText"/>");
<if test="SubmissionCredentialData/Login/EnrollText">
		login.setEnrollText("<value-of select="SubmissionCredentialData/Login/EnrollText"/>");
</if>

<if test="SubmissionCredentialData/Login/InputField">
<for-each select="SubmissionCredentialData/Login/InputField">
		inputField = new CredentialFormInputField();
		inputField.setFieldID("<value-of select="FieldID"/>");
<if test="FieldType">
		inputField.setFieldType("<value-of select="FieldType"/>");
</if>
<if test="PossibleFieldValues">
	<for-each select="PossibleFieldValues/Value"> 
		inputField.addPossibleFieldValue("<value-of select="."/>");
	</for-each>
</if>
<if test="MinLength">
		inputField.setMinimumLength(<value-of select="MinLength"/>);
</if>
<if test="Length">
		inputField.setLength(<value-of select="Length"/>);
</if>
		inputField.setLabel("<value-of select="Label"/>");
<if test="IsObscure">
		inputField.setIsObscure("<value-of select="IsObscure"/>");
</if>
<if test="NumericOnly">
		inputField.setIsNumericOnly("<value-of select="NumericOnly"/>");
</if>
		inputField.setIsRequired("<value-of select="IsRequired"/>");

		login.addInputField(inputField);
</for-each>
</if>
	
		scd.setLogin(login);

		footer = new CredentialFormFooter();
		footer.setAuthorizeText("<value-of select="translate (SubmissionCredentialData/Footer/AuthorizeText, '&quot;', '' )" />");
		scd.setFooter(footer);
		
		result.setCredentialData(scd);
</if>

<if test="EnrollmentMethod">
// nyi enrollment method
</if>
		
		return result;
	}
</template>

</stylesheet>
