package com.paycycle.model;


//import com.paycycle.addon.workcomp.WorkersCompMgr;
//import com.paycycle.auth.AuthMgr;
//import com.paycycle.billing.Billing;
//import com.paycycle.billing.BillingConstants;
//import com.paycycle.billing.BillingMgr;
//import com.paycycle.billing.CompanyBillingInformation;
//import com.paycycle.biz.AccountAccessEvent;
//import com.paycycle.biz.Address;
//import com.paycycle.biz.BankInfo;
//import com.paycycle.biz.Company;
//import com.paycycle.biz.CompanyInfoEvent;
//import com.paycycle.biz.CompanyStatus;
//import com.paycycle.biz.Contractor;
//import com.paycycle.biz.Department;
//import com.paycycle.biz.Employee;
//import com.paycycle.biz.EmployeeBank;
//import com.paycycle.biz.EmployeeTaxSetup;
//import com.paycycle.biz.Feature;
//import com.paycycle.biz.FeatureSet;
//import com.paycycle.biz.FeatureSet.FeatureSetType;
//import com.paycycle.biz.FeatureSetAddOn;
//import com.paycycle.biz.PartnerMgr;
//import com.paycycle.biz.Person;
//import com.paycycle.biz.WorkLocation;
//import com.paycycle.biz.entities.EmailAddr;
//import com.paycycle.biz.entities.EmailType;
//import com.paycycle.biz.entities.EmployeeJobInfo;
//import com.paycycle.biz.entities.IM;
//import com.paycycle.biz.entities.IMType;
//import com.paycycle.biz.entities.PersonContact;
//import com.paycycle.biz.entities.Phone;
//import com.paycycle.biz.entities.PhoneType;
//import com.paycycle.boamm.BOAVerifyAccount;
//import com.paycycle.cs.ClientTicket;
//import com.paycycle.customfields.entities.CustomField;
//import com.paycycle.customfields.entities.CustomFieldLabel;
//import com.paycycle.customfields.entities.PersonCustomField;
//import com.paycycle.data.BasicItemMgr;
//import com.paycycle.data.KeyedRecord;
//import com.paycycle.data.Transaction;
//import com.paycycle.email.CompanyMail;
//import com.paycycle.email.MailInfo;
//import com.paycycle.email.MailManager;
//import com.paycycle.form.FilingType;
//import com.paycycle.marketing.OfferMgr;
//import com.paycycle.model.BankAccountModel.BankAccountType;
//import com.paycycle.model.CompanyDeductionModel.ContributionType;
//import com.paycycle.model.CompanyModel.CompanyType;
//import com.paycycle.model.CompanyModel.EmployeeCountRange;
//import com.paycycle.model.EmployeeDeductionModel.AmountType;
//import com.paycycle.model.EmployeeDeductionModel.DeductionType;
//import com.paycycle.model.EmployeeDeductionModel.GarnishmentWeighting;
//import com.paycycle.model.EmployeeModel.Gender;
//import com.paycycle.model.EmployeeModel.OregonTransitTax;
//import com.paycycle.model.EmployeeModel.PayMethod;
//import com.paycycle.model.EmployeeModel.PayRateType;
//import com.paycycle.model.EmployeeModel.SalaryFrequency;
//import com.paycycle.model.PayScheduleModel.PayFrequency;
//import com.paycycle.model.PtoPolicyModel.PtoAccrualFrequency;
//import com.paycycle.model.PtoPolicyModel.PtoCategoryModel;
//import com.paycycle.payment.CompanySchedule;
//import com.paycycle.payment.Schedule;
//import com.paycycle.payment.TaxAdjustment;
//import com.paycycle.payment.TaxPayment;
//import com.paycycle.payment.TaxPaymentDetail;
//import com.paycycle.payroll.CompanyDeduction;
//import com.paycycle.payroll.CompanyTaxRate;
//import com.paycycle.payroll.CompanyWCClass;
//import com.paycycle.payroll.CompanyWCRate;
//import com.paycycle.payroll.CompanyWageItem;
//import com.paycycle.payroll.ContributionDetail;
//import com.paycycle.payroll.DeductionCategory;
//import com.paycycle.payroll.DeductionDetail;
//import com.paycycle.payroll.DeductionItem;
//import com.paycycle.payroll.EmployeeDeduction;
//import com.paycycle.payroll.EmployeeLocalTaxItem;
//import com.paycycle.payroll.EmployeeWCClass;
//import com.paycycle.payroll.EmployeeWageItem;
//import com.paycycle.payroll.EmployerContribution;
//import com.paycycle.payroll.FederalTaxLevyGarnishment;
//import com.paycycle.payroll.Garnishment;
//import com.paycycle.payroll.ItemMgr;
//import com.paycycle.payroll.OtherGarnishment;
//import com.paycycle.payroll.OtherPaycheckDetail;
//import com.paycycle.payroll.PaySchedule;
//import com.paycycle.payroll.Paycheck;
//import com.paycycle.payroll.StateRegulationMgr;
//import com.paycycle.payroll.SupportGarnishment;
//import com.paycycle.payroll.TaxDetail;
//import com.paycycle.payroll.TaxGroup;
//import com.paycycle.payroll.TaxItem;
//import com.paycycle.payroll.TaxRateConstants;
//import com.paycycle.payroll.WCConstants;
//import com.paycycle.payroll.WageDetail;
//import com.paycycle.payroll.WageItem;
//import com.paycycle.payroll.WageItemID;
//import com.paycycle.pto.PtoCategory;
//import com.paycycle.pto.PtoConstants;
//import com.paycycle.pto.PtoHours;
//import com.paycycle.pto.PtoPolicy;
//import com.paycycle.security.EmployeeSecret;
//import com.paycycle.security.Encryption;
//import com.paycycle.sso.SSOManager;
//import com.paycycle.taglib.BasicInputTag;
//import com.paycycle.taglib.Option;
//import com.paycycle.tx.ClientTicketTx;
//import com.paycycle.tx.ContractorTx;
//import com.paycycle.tx.EmployeeTx;
//import com.paycycle.user.Login;
//import com.paycycle.user.Role;
//import com.paycycle.user.RoleID;
//import com.paycycle.user.User;
//import com.paycycle.user.UserException;
//import com.paycycle.user.UserType;
//import com.paycycle.util.AppMgr;

import com.paycycle.util.DateUtil;

import java.util.Date;


//import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.Iterator;

//import java.util.List;
//import java.util.Map;
//import java.util.Vector;
public class ModelHelper {
    //	static public MathContext mathContext = new MathContext(10);
    //	static public BigDecimal doubleToBigDecimal(double value) {
    //		return new BigDecimal(value, mathContext);
    //	}
    //
    //	static public int getEmployeeModelIndex(Employee employee) {
    //		return Integer.parseInt(employee.getNamedStatus("MODEL_INDEX", String.valueOf(employee.getId())));
    //	}
    //
    //    static public Date toDate(DateTimeModel modelTime) {
    //        if (modelTime == null) {
    //            return null;
    //        }
    //        return DateUtil.createDateTime(modelTime.year, modelTime.month, modelTime.day, modelTime.hour + (modelTime.ampm == DateTimeModel.AmPm.PM ? 12 : 0), modelTime.minute, modelTime.second);
    //    }
    static public Date toDate(DateModel modelDate) {
        if (modelDate == null) {
            return null;
        }

        return DateUtil.createDate(modelDate.year, modelDate.month - 1, modelDate.day);
    }

    //
    //	static public DateModel toDateModel(Date date) {
    //		if (date == null) {
    //			return null;
    //		}
    //		DateModel model = new DateModel();
    //		model.day = DateUtil.getDay(date);
    //		model.year = DateUtil.getYear(date);
    //		model.month = DateUtil.getMonth1Base(date);
    //		return model;
    //	}
    //
    //    static public DateTimeModel toDateTimeModel(Date date) {
    //        DateModel dateModel = toDateModel(date);
    //
    //        if (dateModel == null) {
    //            return null;
    //        }
    //
    //        DateTimeModel result = new DateTimeModel();
    //        result.year = dateModel.year;
    //        result.day = dateModel.day;
    //        result.month = dateModel.month;
    //
    //        result.hour = DateUtil.getDatePart(date, Calendar.HOUR);
    //        result.minute = DateUtil.getDatePart(date, Calendar.MINUTE);
    //        result.second = DateUtil.getDatePart(date, Calendar.SECOND);
    //        result.ampm = DateUtil.getDatePart(date, Calendar.AM_PM) == 0 ? DateTimeModel.AmPm.AM : DateTimeModel.AmPm.PM;
    //
    //        return result;
    //    }
    //
    //	static public PayScheduleModel payScheduleToModel(PaySchedule schedule) {
    //		if (schedule == null) {
    //			return null;
    //		}
    //		PayScheduleModel model = new PayScheduleModel();
    //        populateModelFromKeyedRecord(schedule, model);
    //
    //		model.payFrequency  = PayFrequency.fromInt(schedule.getPayFrequency());
    //		model.nextPayDay = toDateModel(schedule.getNextPayDay());
    //		Period payPeriod =schedule.getPeriodForNextPayDay(schedule.getNextPayDay());
    //		model.periodEndDate = toDateModel(payPeriod.getEndDate());
    //		model.periodStartDate = toDateModel(payPeriod.getStartDate());
    //		model.useAsDefault = schedule.getCompany().getDefaultPaySchedule() == schedule.getId();
    //		model.description = schedule.getDescription();
    //		String arrearsType = schedule.getArrearsType();
    //		if ((schedule.getPayFrequency() == 15 || schedule.getPayFrequency() == 30) && Helper.isNotEmpty(arrearsType) &&
    //			(arrearsType.equals("P") || arrearsType.equals("S") || arrearsType.equals("N") || arrearsType.equals("R"))) {
    //			model.isCustom = true;
    //		} else {
    //			model.isCustom = false;
    //		}
    //
    //		// These are relevant for Semi-Monthly and Monthly when custom
    //		model.firstPayDay1 = PayScheduleModel.DayOfMonth.parseInt(schedule.getPayDayOfMonth());
    //		if (arrearsType.equals("R")) {
    //			model.arrearsType1 = PayScheduleModel.ArrearsType.daysBeforePayDay;
    //			model.arrearsDayOfTheMonth1 = PayScheduleModel.DayOfMonth.parseInt(1);
    //			model.arrearsDayOfTheMonthWhichMonth1 = PayScheduleModel.WhichMonthType.Same;
    //			model.arrearsDaysBeforePayDay1 = schedule.getDaysInArrears();
    //		} else {
    //			model.arrearsType1 = PayScheduleModel.ArrearsType.dayOfTheMonth;
    //			model.arrearsDayOfTheMonth1 = PayScheduleModel.DayOfMonth.parseInt(schedule.getDaysInArrears());
    //			if (arrearsType.equals("P")) {
    //				model.arrearsDayOfTheMonthWhichMonth1 = PayScheduleModel.WhichMonthType.Previous;
    //			} else if (arrearsType.equals("S")) {
    //				model.arrearsDayOfTheMonthWhichMonth1 = PayScheduleModel.WhichMonthType.Same;
    //			} else if (arrearsType.equals("N")) {
    //				model.arrearsDayOfTheMonthWhichMonth1 = PayScheduleModel.WhichMonthType.Next;
    //			}
    //			model.arrearsDaysBeforePayDay1 = 0;
    //		}
    //
    //		// These are relevant for Semi-Monthly when custom
    //		String arrearsType2 = schedule.getArrearsType2();
    //		model.firstPayDay2 = PayScheduleModel.DayOfMonth.parseInt(schedule.getPayDayOfMonth2());
    //		if (arrearsType2.equals("R")) {
    //			model.arrearsType2 = PayScheduleModel.ArrearsType.daysBeforePayDay;
    //			model.arrearsDayOfTheMonth2 = PayScheduleModel.DayOfMonth.parseInt(16);
    //			model.arrearsDayOfTheMonthWhichMonth2 = PayScheduleModel.WhichMonthType.Same;
    //			model.arrearsDaysBeforePayDay2 = schedule.getDaysInArrears2();
    //		} else {
    //			model.arrearsType2 = PayScheduleModel.ArrearsType.dayOfTheMonth;
    //			model.arrearsDayOfTheMonth2 = PayScheduleModel.DayOfMonth.parseInt(schedule.getDaysInArrears2());
    //			if (arrearsType.equals("P")) {
    //				model.arrearsDayOfTheMonthWhichMonth2 = PayScheduleModel.WhichMonthType.Previous;
    //			} else if (arrearsType.equals("S")) {
    //				model.arrearsDayOfTheMonthWhichMonth2 = PayScheduleModel.WhichMonthType.Same;
    //			} else if (arrearsType.equals("N")) {
    //				model.arrearsDayOfTheMonthWhichMonth2 = PayScheduleModel.WhichMonthType.Next;
    //			}
    //			model.arrearsDaysBeforePayDay2 = 0;
    //		}
    //
    //		return model;
    //	}
    //
    //	static public PeriodModel periodToModel(Period period) {
    //		PeriodModel pm = new PeriodModel();
    //		pm.periodStartDate = toDateModel(period.getStartDate());
    //		pm.periodEndDate = toDateModel(period.getEndDate());
    //		return pm;
    //	}
    //
    //	static public Period toPeriod(PeriodModel periodModel) {
    //		Date startDate = toDate(periodModel.periodStartDate);
    //		Date endDate = toDate(periodModel.periodEndDate);
    //		return new Period(startDate, endDate);
    //	}
    //
    //
    //	static public WorkLocationModel workLocationToWorkLocationModel(WorkLocation workLocation) {
    //		AddressModel addressModel = addressToAddressModel(workLocation);
    //		WorkLocationModel model = new WorkLocationModel(addressModel);
    //        populateModelFromKeyedRecord(workLocation, model);
    //		model.active = workLocation.isActive();
    //		model.canDelete = workLocation.getEmployees(false).size() == 0;
    //
    //        if (Helper.isNotEmpty(workLocation.getState())) {
    //            if (workLocation.getState().equalsIgnoreCase("MN") || workLocation.getState().equalsIgnoreCase("MA") || workLocation.getState().equalsIgnoreCase("IA"))
    //                model.unitNumber = workLocation.getUnitNumber();
    //        }
    //		return model;
    //	}
    //
    //	static public AddressModel addressToAddressModel(Address address) {
    //        return addressToAddressModel(address, false);
    //    }
    //
    //	static public AddressModel addressToAddressModel(Address address, boolean isTaxAddress) {
    //		if (address == null) {
    //			return null;
    //		}
    //
    //		AddressModel model = new AddressModel();
    //        populateModelFromKeyedRecord(address, model);
    //		model.address1 = address.getAddress1();
    //		model.address2 = address.getAddress2();
    //		model.city = address.getCity();
    //		model.state = address.getState();
    //		model.county = address.getCounty();
    //		model.zip = address.getZipCode();
    //        model.setIsTaxAddress(isTaxAddress);
    //		Address taxAddress = address.getTaxAddress();
    //		if ( taxAddress != address) {
    //			model.setTaxAddress(addressToAddressModel(taxAddress, true));
    //		} else if (model.taxAddress == null && !model.getIsTaxAddress()) {
    //			model.taxAddress = new AddressModel(model);
    //		}
    //
    //		return model;
    //	}
    //
    //	static public Address initializeAddress(Address address, AddressModel model, boolean allowRelaxingOfValidation) {
    //		if (model != null && model.getState() != null) {
    //			address.initialize(model.getAddress1()==null ? "" : model.getAddress1(),
    //					model.getAddress2()== null ? "" : model.getAddress2(),
    //					model.getCity()==null ? "" : model.getCity(),
    //					model.getCounty()== null ? "" : model.getCounty(),
    //					model.getState(),
    //					model.getZip()==null ? "" : model.getZip());
    //
    //            try {
    //                address.validate();
    //            } catch (RuntimeException ex) {
    //                if (!allowRelaxingOfValidation || model.getValidateAddress()) {
    //                    throw ex;
    //                }
    //            }
    //
    //        }
    //
    //		return address;
    //	}
    //
    //	static public PrimaryContactModel userToPrimaryContactModel(User user, PrimaryContactModel existingPrimaryContactModel) {
    //		if (user == null) {
    //			return existingPrimaryContactModel;
    //		}
    //
    //		PrimaryContactModel model = new PrimaryContactModel();
    //        populateModelFromKeyedRecord(user, model);
    //		model.eMailAddress = isNull(existingPrimaryContactModel.eMailAddress, user.getEmailAddress());
    //		model.eMailAddress2 = isNull(existingPrimaryContactModel.eMailAddress2, user.getEmailAddress());
    //		model.firstName = isNull(existingPrimaryContactModel.firstName, user.getFirstName());
    //		model.lastName = isNull(existingPrimaryContactModel.lastName, user.getLastName());
    //		model.middleInitial = isNull(existingPrimaryContactModel.middleInitial, user.getMiddleInitial());
    //		model.secretQuestionsAnswered = null;
    //		if (user.getLogin()!=null){
    //			model.secretQuestionsAnswered = user.getLogin().getSecretQuestions().size() > 0;
    //		}
    //		model.secretAnswers = null;
    //		model.secretQuestions = existingPrimaryContactModel.secretQuestions;
    //		model.userID = isNull(existingPrimaryContactModel.userID, user.getLogin() == null ? null : user.getLogin().getUserId());
    //
    //		return model;
    //	}
    //
    //	static public Boolean isNullCompanyStatus(Company company, String statusKey, boolean replacementValue) {
    //		String value = company.getStatus().getStatus(statusKey, null);
    //		return value == null ? replacementValue : Boolean.parseBoolean(value);
    //	}
    //
    //	static public <T> T isNull(T object, T replacement) {
    //		return object == null ? replacement : object;
    //	}
    //
    //    static public CompanyScheduleModel scheduleToScheduleModel(CompanySchedule schedule) {
    //        if (schedule == null) {
    //            return null;
    //        }
    //
    //        CompanyScheduleModel model = new CompanyScheduleModel();
    //        model.scheduleId = schedule.getSchedule().getId();
    //        model.effectiveDate = schedule.getEffectiveDate();
    //        model.endDate = schedule.getEndDate();
    //        if (schedule.getFilingType() != null) {
    //            model.filingType = CompanyScheduleModel.FilingType.fromLegacyType((int) schedule.getFilingType().getId());
    //        }
    //
    //        return model;
    //    }
    //
    //    static public CompanyStateTaxSetupModel getStateTaxSetupModel(List<CompanyStateTaxSetupModel> stateModels, String state, boolean addToStateModelList) {
    //        for (CompanyStateTaxSetupModel stateModel : stateModels) {
    //            if (stateModel.state.equals(state)) {
    //                return stateModel;
    //            }
    //        }
    //
    //        if (addToStateModelList) {
    //            CompanyStateTaxSetupModel stateModel = new CompanyStateTaxSetupModel();
    //            stateModel.state = state;
    //            stateModels.add(stateModel);
    //            return stateModel;
    //        }
    //
    //        return null;
    //    }
    //
    //    static public CompanyTaxRateModel companyTaxRateToCompanyTaxRateModel(CompanyTaxRate taxRate) {
    //        if (taxRate == null) {
    //            return null;
    //        }
    //
    //        CompanyTaxRateModel model = new CompanyTaxRateModel();
    //        model.rate = taxRate.getRate();
    //        model.state = taxRate.getState();
    //        model.effectiveDate = taxRate.getEffectiveDate();
    //        model.endDate = taxRate.getEndDate();
    //        TaxItem taxItem = ItemMgr.getTaxItemByTaxRateType(null, taxRate.getRateType(), Helper.getGeocodeFromJurisdiction(model.state));
    //        model.taxItemId = taxItem == null ? -1 : taxItem.getId();
    //
    //        return model;
    //    }
    //
    //    static public CompanyTaxSetupModel companyTaxSetupToCompanyTaxSetupModel(Company company) {
    //        if (company == null) {
    //            return null;
    //        }
    //
    //        CompanyTaxSetupModel model = new CompanyTaxSetupModel();
    //        model.federalEIN = company.getFederalEIN();
    //        model.isFilingAddressSameAsBusinessAddress = company.isFilingAddressSameAsBizAddress();
    //
    //        if (!model.isFilingAddressSameAsBusinessAddress) {
    //            model.filingAddress = addressToAddressModel(company.getFilingAddress());
    //        }
    //
    //        model.filingName = company.getFilingName();
    //        model.filingType = company.getCompanyFilingType();
    //
    //        Map<String, List<CompanyScheduleModel>> scheduleMap = new Hashtable<String, List<CompanyScheduleModel>>();
    //        for (CompanySchedule schedule : company.getCompanyPaymentSchedules()) {
    //            String jurisdiction = schedule.getSchedule().getTaxPaymentGroup().getJurisdiction();
    //            List<CompanyScheduleModel> currentList = scheduleMap.get(jurisdiction);
    //            if (currentList == null) {
    //                currentList = new Vector<CompanyScheduleModel>();
    //            }
    //            currentList.add(scheduleToScheduleModel(schedule));
    //            scheduleMap.put(jurisdiction, currentList);
    //        }
    //
    //        Map<String, List<CompanyTaxRateModel>> taxRateMap = new Hashtable<String, List<CompanyTaxRateModel>>();
    //        for (CompanyTaxRate companyTaxRate : company.getTaxRates()) {
    //            String state = companyTaxRate.getState();
    //            List<CompanyTaxRateModel> currentList = taxRateMap.get(state);
    //            if (currentList == null) {
    //                currentList = new Vector<CompanyTaxRateModel>();
    //            }
    //            currentList.add(companyTaxRateToCompanyTaxRateModel(companyTaxRate));
    //            taxRateMap.put(state, currentList);
    //        }
    //
    //        List<String> states = company.getAllStatesNeededForTaxSetup();
    //        List<CompanyStateTaxSetupModel> stateTaxSetupModels = new Vector<CompanyStateTaxSetupModel>();
    //        for (String state : states) {
    //            CompanyStateTaxSetupModel stateModel = new CompanyStateTaxSetupModel();
    //            stateModel.state = state;
    //            if (Helper.isNotEmpty(company.getEIN(state))) {
    //                stateModel.ein1 = company.getEIN(state);
    //            }
    //
    //            if (Helper.isNotEmpty(company.getEIN2(state))) {
    //                stateModel.ein2 = company.getEIN2(state);
    //            }
    //            stateTaxSetupModels.add(stateModel);
    //        }
    //
    //        for (String key : scheduleMap.keySet()) {
    //            CompanyScheduleModel[] scheduleModels = scheduleMap.get(key).toArray(new CompanyScheduleModel[] {});
    //            if (key.equals("FD")) {
    //                model.federalDepositSchedules = scheduleModels;
    //            } else {
    //                CompanyStateTaxSetupModel stateModel = getStateTaxSetupModel(stateTaxSetupModels, key, true);
    //                stateModel.stateSchedules = scheduleModels;
    //            }
    //        }
    //
    //        for (String key : taxRateMap.keySet()) {
    //            CompanyTaxRateModel[] taxRateModels = taxRateMap.get(key).toArray(new CompanyTaxRateModel[] {});
    //            CompanyStateTaxSetupModel stateModel = getStateTaxSetupModel(stateTaxSetupModels, key, true);
    //            stateModel.stateTaxRates = taxRateModels;
    //        }
    //
    //        model.stateTaxSetup = stateTaxSetupModels.toArray(new CompanyStateTaxSetupModel[] {});
    //
    //        return model;
    //    }
    //
    //	static public CompanyModel companyToCompanyModel(Company company, CompanyModel existingCompanyModel) {
    //		if (company == null) {
    //			return existingCompanyModel;
    //		}
    //
    //		CompanyModel model = new CompanyModel();
    //
    //		populateModelFromKeyedRecord(company, model);
    //
    //		model.acceptedCustomerServiceAgreement = isNull(existingCompanyModel.acceptedCustomerServiceAgreement, company.isContractReceived());
    //		model.businessAddress = addressToAddressModel(company.getAddress());
    //		String businessName = company.getBusinessName();
    //		model.businessName = isNull(existingCompanyModel.businessName, Helper.isEmpty(businessName, null));
    //		model.businessType = isNull(existingCompanyModel.businessType, company.getIndustry());
    //		model.clearTextSourceCode = isNull(model.clearTextSourceCode, company.getSourceCode());
    //		model.signupDate = company.getSignupDate();
    //
    //		if (existingCompanyModel.isAccountantClient == null) {
    //			model.isAccountantClient = company.getProvider() != null;
    //		}
    //
    //		if (existingCompanyModel.companyType == null) {
    //			if (company.isHousehold()) {
    //				model.companyType = CompanyType.Household;
    //			} else if (company.isSBA()) {
    //				model.companyType = CompanyType.SmallBusinessClient;
    //			} else if (company.isProvider()) {
    //				model.companyType = CompanyType.Accountant;
    //			} else {
    //				model.companyType = CompanyType.SmallBusiness;
    //			}
    //		} else {
    //			model.companyType = existingCompanyModel.companyType;
    //		}
    //
    //		model.fax = isNull(existingCompanyModel.fax, company.getUser().getFaxNumber());
    //		model.featureSetType = isNull(existingCompanyModel.featureSetType, company.getFeatureSet().getType());
    //		model.featureSetIsLocked = (company.getEmployees() != null && company.getEmployees().size() > 0);
    //		model.homePhone = isNull(existingCompanyModel.homePhone, company.getUser().getHomePhone());
    //		model.mobilePhone = isNull(existingCompanyModel.mobilePhone, company.getUser().getMobilePhone());
    //        model.formUsageDate = company.getFormUsageDate();
    //		model.marketingCode = isNull(existingCompanyModel.marketingCode, company.getMarketingCode());
    //		model.needsToAcceptCustomerServiceAgreement = isNull(model.needsToAcceptCustomerServiceAgreement, company.getPartner().isCustomerServiceAgreementRequired() && !company.isContractReceived());
    //		if (existingCompanyModel.offerCodeEncrypted == null) {
    //			model.offerCodeEncrypted = company.getOfferCodeId() == 0 ? null : (OfferMgr.getOffer(company.getOfferCodeId()) != null ? OfferMgr.getOffer(company.getOfferCodeId()).getCode() : null);
    //			if (model.offerCodeEncrypted != null) {
    //				model.offerCodeEncrypted = Encryption.encryptCode(model.offerCodeEncrypted);
    //			}
    //		}
    //		else {
    //			model.offerCodeEncrypted = existingCompanyModel.offerCodeEncrypted;
    //		}
    //		model.phone = isNull(existingCompanyModel.phone, company.getUser().getWorkPhone());
    //		model.phoneExtension = isNull(existingCompanyModel.phoneExtension, company.getUser().getWorkExtension());
    //		model.primaryContact = userToPrimaryContactModel(company.getUser(), model.primaryContact);
    //		model.referrer = isNull(existingCompanyModel.referrer, company.getReferrer());
    //		model.visitorId = isNull(existingCompanyModel.visitorId, company.getInitialVisitorId());
    //
    //		// Initial interview questions
    //		model.directDeposit = isNull(existingCompanyModel.directDeposit,
    //				!company.getStatus().hasStatus(CompanyStatus.INITIALINTERVIEW_DIRECTDEPOSIT)?null:company.getStatus().getStatusAsBoolean(CompanyStatus.INITIALINTERVIEW_DIRECTDEPOSIT, null));
    //		model.directDepositPaperCheckWhileWaiting = isNull(existingCompanyModel.directDepositPaperCheckWhileWaiting,
    //				!company.getStatus().hasStatus(CompanyStatus.INITIALINTERVIEW_DIRECTDEPOSITPAPERCHECKWHILEWAITING)?Boolean.TRUE:company.getStatus().getStatusAsBoolean(CompanyStatus.INITIALINTERVIEW_DIRECTDEPOSITPAPERCHECKWHILEWAITING, "true"));
    //		// bug fix 24912 - default to - I'll write or print paper checks until direct deposit is ready (recommended)
    //		model.havePaidTimeOff = isNull(existingCompanyModel.havePaidTimeOff,
    //				!company.getStatus().hasStatus(CompanyStatus.INITIALINTERVIEW_HAVEPAIDTIMEOFF)?null:company.getStatus().getStatusAsBoolean(CompanyStatus.INITIALINTERVIEW_HAVEPAIDTIMEOFF, null));
    //		model.moreThanOneWorkLocation = isNull(existingCompanyModel.moreThanOneWorkLocation,
    //				!company.getStatus().hasStatus(CompanyStatus.INITIALINTERVIEW_MORETHANONEWORKLOCATION)?null:company.getStatus().getStatusAsBoolean(CompanyStatus.INITIALINTERVIEW_MORETHANONEWORKLOCATION, null));
    //		model.startPayrollThisQtr = isNull(existingCompanyModel.startPayrollThisQtr,
    //				!company.getStatus().hasStatus(CompanyStatus.PCHISTORYSTARTTHISQTR)?null:company.getStatus().getStatusAsBoolean(CompanyStatus.PCHISTORYSTARTTHISQTR, null));
    //		model.havePaidEmployeesThisYear = isNull(existingCompanyModel.havePaidEmployeesThisYear,
    //				!company.getStatus().hasStatus(CompanyStatus.PCHISTORYPAYROLLTHISYEAR)?null:company.getStatus().getStatusAsBoolean(CompanyStatus.PCHISTORYPAYROLLTHISYEAR, null));
    //		model.haveContractor = isNull(existingCompanyModel.haveContractor,
    //				!company.getStatus().hasStatus(CompanyStatus.INITIALINTERVIEW_HAVECONTRACTOR)?null:company.getStatus().getStatusAsBoolean(CompanyStatus.INITIALINTERVIEW_HAVECONTRACTOR, null));
    //        model.haveTimeTracking =isNull(existingCompanyModel.haveTimeTracking,
    //				!company.getStatus().hasStatus(CompanyStatus.INITIALINTERVIEW_TIMETRACKING)?null:company.getStatus().getStatusAsBoolean(CompanyStatus.INITIALINTERVIEW_TIMETRACKING, null));
    //
    //        model.showTimeTracking = true;
    //		model.showWorkLocation = true;
    //		model.showContractor= true;
    //		model.showDirectDeposit = true;
    //		model.showHistory = true;
    //		model.showFUD = true;
    //		if (FeatureSet.FeatureSetType.Basic  == model.featureSetType
    //			|| FeatureSet.FeatureSetType.Payday == model.featureSetType
    //			|| FeatureSet.FeatureSetType.Money == model.featureSetType
    //			|| CompanyModel.CompanyType.Household == model.companyType) {
    //			model.showWorkLocation = false;
    //			model.showContractor = false;
    //			model.moreThanOneWorkLocation = false;
    //		}
    //        if(company.isSBA() ||
    //    			!FeatureSetAddOn.AddOnType.TimeTracking.allowFeatureSetAddOn(company.getFeatureSet(), company.getPartner())){
    //                    model.showTimeTracking = false;
    //        }
    //		if (FeatureSet.FeatureSetType.Payday == model.featureSetType
    //			|| FeatureSet.FeatureSetType.Money == model.featureSetType) {
    //			model.showDirectDeposit = false;
    //		}
    //
    //		if (CompanyModel.CompanyType.Household == model.companyType) {
    //			model.showHistory = false;
    //			model.showFUD = false;
    //		}
    //
    //		boolean hasEmployees = company.getEmployees(true, false) != null && company.getEmployees(true, false).size() > 0;
    //
    //		if (company.hasPaidEmployee()) {
    //			// This happens when the user completed setup before Apollo went live.  In
    //			// that case, we need to default the flags because they would not have been
    //			// set in the db and will still be null now - THESE WILL LIKELY GET
    //			// OVERWRITTEN BELOW with the correct value..
    //			model.startPayrollThisQtr = true;
    //			model.haveContractor = false;
    //			model.havePaidEmployeesThisYear = false;
    //			model.startPayrollThisQtr = false;
    //			model.directDeposit = false;
    //			model.havePaidTimeOff = true;
    //			model.moreThanOneWorkLocation = false;
    //		}
    //
    //		//For QBOSSO partner, we allow state to be editable if there isn't
    //		//any setup info for state tax yet. Once this code is tested throughtly
    //		//in qbo/payroll integration project, we can enable this logic for other
    //		//partners
    //		if(company.getPartner() != null && company.getPartner().isQBOSSOPartner()) {
    //			model.stateEditIsLocked = company.hasAnyStateTaxSetupInfo();
    //		} else {
    //			// Flag for whether we allow the state to be changed (lockState = true means no).
    //			model.stateEditIsLocked = (company.getAddress() != null);
    //		}
    //
    //		if( company.getFormUsageDate()!=null) {
    //			Date thisQtr = DateUtil.getFirstDayOfQuarter(DateUtil.today());
    //			if( company.getFormUsageDate().after(thisQtr) ) {
    //				// FUD says they are starting next quarter.
    //				model.startPayrollThisQtr = false;
    //			} else {
    //				// FUD says they are starting in a prior quarter.
    //				model.startPayrollThisQtr = true;
    //			}
    //		}
    //
    //		if (company.getContractors().size() > 0) {
    //			model.haveContractor = true;
    //		}
    //
    //		model.contractorsOnly = isNull(existingCompanyModel.contractorsOnly, Boolean.parseBoolean(company.getStatus().getStatus(CompanyStatus.INITIALINTERVIEW_CONTRACTORSONLY, null)));
    //		if ((company.getEmployees() != null && company.getEmployees().size() > 0) || (model.haveContractor != null && !model.haveContractor)) {
    //			model.contractorsOnly = false;
    //		}
    //
    //		// Flag for whether we allow the user to still select "contractors only" option.
    //		model.contractorsOnlyEditIsLocked = (company.getEmployees() != null && company.getEmployees().size() > 0);
    //
    //		model.editWorkLocationAllowed = company.hasFeature(Feature.FullMultipleState);
    //
    //		if (company.hasHistoricalPaycheck()) {
    //			model.havePaidEmployeesThisYear = true;
    //			model.startPayrollThisQtr = true;
    //		}
    //
    //		if (company.isDirectDepositing() && hasEmployees) {
    //			model.directDeposit = true;
    //			model.directDepositPaperCheckWhileWaiting = true;
    //		}
    //
    //		if (company.haveAssignedPaidTimeOffToEmployees())
    //		{
    //			model.haveAssignedPaidTimeOffToEmployees = true;
    //			model.havePaidTimeOff = true;
    //		}
    //
    //		if (company.getWorkLocationList().size() > 1 || model.featureSetType == FeatureSetType.Sample) {
    //			model.moreThanOneWorkLocation = true;
    //		}
    //
    //		if (model.featureSetType == FeatureSetType.Basic) {
    //			model.moreThanOneWorkLocation = false;
    //		}
    //
    //		model.havePriorQuarterPayrolls = company.getStatus().getStatusAsBoolean(CompanyStatus.PCHISTORYPRIORQPAYROLLS, null);
    //		model.haveCurrentQuarterPayrolls = company.getStatus().getStatusAsBoolean(CompanyStatus.PCHISTORYCURRQPAYROLLS, null);
    //		if( company.getPaycheckHistory()!=null ) {
    //			model.convertFromCode = company.getPaycheckHistory().getConvertFromCode();
    //		}
    //
    //		String currentRangeString = company.getStatus().getStatus(CompanyStatus.INITIALINTERVIEW_EMPLOYEECOUNT, null);
    //		model.employeeCount = currentRangeString == null ? null : EmployeeCountRange.valueOf(currentRangeString);
    //
    //		model.workLocations = new WorkLocationModel[company.getWorkLocationList().getAll().size()];
    //		for (int index = 0; index < company.getWorkLocationList().getAll().size(); index++) {
    //			model.workLocations[index] = workLocationToWorkLocationModel(company.getWorkLocationList().getAll().get(index));
    //		}
    //
    //
    //		model.companyDeductions = new CompanyDeductionModel[company.getDeductions().size()];
    //		for (int index = 0; index < model.companyDeductions.length; index++) {
    //			model.companyDeductions[index] = companyDeductionToCompanyDeductionModel(company.getDeductions().get(index));
    //		}
    //
    //		model.companyWageItems = new CompanyWageItemModel[company.getWageItems().size()];
    //		for (int index = 0; index < model.companyWageItems.length; index++) {
    //			model.companyWageItems[index] = companyWageItemToCompanyWageItemModel(company.getWageItems().get(index));
    //		}
    //
    //		model.companyWageItems = new CompanyWageItemModel[company.getWageItems().size()];
    //		for (int index = 0; index < model.companyWageItems.length; index++) {
    //			model.companyWageItems[index] = companyWageItemToCompanyWageItemModel(company.getWageItems().get(index));
    //		}
    //
    //		List<PtoPolicy> ptoPolicies = company.getPtoPolicies();
    //		model.ptoPolicies = new PtoPolicyModel[ptoPolicies.size()];
    //		for (int index = 0; index < model.ptoPolicies.length; index++) {
    //			model.ptoPolicies[index] = ptoPolicyToPtoPolicyModel(ptoPolicies.get(index));
    //		}
    //
    //		List<Department> depts = new ArrayList<Department>();
    //		for(Department dept : company.getDepartments() ){
    //            Department newDepartment = dept.shallowCopy();
    //            newDepartment.company = null;
    //			depts.add(newDepartment);
    //		}
    //		model.departments = depts.toArray(new Department[depts.size()]);
    //
    //		//Add custom field labels;
    //		List<CustomFieldLabel> labels = company.getCustomFieldLabels();
    //		if(labels != null) {
    //			List<CustomFieldLabel> modelLabels = new ArrayList<CustomFieldLabel>();
    //			for(CustomFieldLabel label : labels ){
    //                CustomFieldLabel newLabel = label.shallowCopy();
    //                newLabel.setCompany(null);
    //				modelLabels.add(newLabel);
    //			}
    //			Collections.sort(modelLabels);
    //			model.customFieldLabels = modelLabels.toArray(new CustomFieldLabel[modelLabels.size()]);
    //			ServiceLayerHelper.getCustomFieldMgrInstance().setCanDeleteFlags(model.customFieldLabels);
    //		}
    //
    //
    //            model.miscAttributes = new HashMap<String, String>();
    //            model.isActive = company.isActive();
    //            model.billingActivationDate = company.getBillingActivationDate();
    //            model.billingInformation = new BillingInformationModel();
    //            CompanyBillingInformation companyBillingInfo = company.getCompanyBillingInformation();
    //            if(companyBillingInfo != null) {
    //                model.billingInformation.billingExpirationDate = companyBillingInfo.getBillingExpirationDate();
    //                model.billingInformation.nextBillingDate = companyBillingInfo.getNextBillingDate();
    //                if (companyBillingInfo.getBilling() != null){
    //                    Billing billing = companyBillingInfo.getBilling();
    //                    model.billingInformation.billingId = billing.getId();
    //                    model.billingInformation.name = billing.getName();
    //                    model.billingInformation.isFree = billing.getIsFree();
    //                    model.billingInformation.isObsolete = billing.getIsObsolete();
    //                    model.billingInformation.discountRate = billing.getDiscountRate();
    //                    model.billingInformation.footNote1 = billing.getFootNote();
    //                    model.billingInformation.footNote2 = billing.getFootNote2();
    //                    model.billingInformation.billingTerms = BillingMgr.getBillingDescription(company);
    //                    model.billingInformation.address = addressToAddressModel(company.getBillingAddress());
    //                    model.billingInformation.shortDesc = billing.getBillingDescription(BillingConstants.XML_SHORT_DESC);
    //                    model.billingInformation.adShortDesc = billing.getBillingDescription(BillingConstants.XML_AD_SHORT_DESC);
    //                    model.billingInformation.longDesc = billing.getBillingDescription(BillingConstants.XML_LONG_DESC);
    //                    model.billingInformation.adLongDesc = billing.getBillingDescription(BillingConstants.XML_AD_LONG_DESC);
    //                    model.billingInformation.adBulletDesc = billing.getBillingDescription(BillingConstants.XML_AD_BULLET_DESC);
    //                    model.billingInformation.clientDesc = billing.getBillingDescription(BillingConstants.XML_CLIENT_DESC);
    //                }
    //                if (companyBillingInfo.getNextBilling() != null) {
    //                    model.billingInformation.nextBillingId = companyBillingInfo.getNextBilling().getId();
    //                }
    //            }
    //
    //        //CCard billing information conversation, //DONOT port the clear text credit card number
    //        model.isBillingAddressBizAddress = company.isBillingAddressSameAsBizAddress();
    //        if(company.getCCard() != null)
    //            model.cCard = company.getCCard().shallowCopy();
    //
    //
    //        model.companyTaxSetup = companyTaxSetupToCompanyTaxSetupModel(company);
    //
    //        model.ssoFederatedID = companySsoExternalIDToSsoFederatedId(company);
    //
    //        model.initialSetupCompleted = company.getStatus().getInitialSetupCompleted();
    //
    //        // If this is an integrated partner, also get the class preferences.
    //        if (company.getPartner().hasPartnerDataIntegration()) {
    //        	model.useClasses = isNull(existingCompanyModel.useClasses, !company.getStatus().hasStatus(CompanyStatus.USE_CLASSES) ? null : company.getStatus().getStatusAsBoolean(CompanyStatus.USE_CLASSES, null));
    //        	model.warnIfNoClass = isNull(existingCompanyModel.warnIfNoClass, !company.getStatus().hasStatus(CompanyStatus.WARN_IF_NO_CLASS) ? null : company.getStatus().getStatusAsBoolean(CompanyStatus.WARN_IF_NO_CLASS, null));
    //        }
    //
    //        model.isOnHold = company.isFundsHold();
    //        model.companyExternalId = Long.valueOf(company.getExternalID(false));
    //
    //        model.pcCalcVersion2 = company.getStatus().getPCCalcVersion2();
    //
    //		List<CompanyWorkersCompensationClassModel> workersCompClassModels = new ArrayList<CompanyWorkersCompensationClassModel>();
    //        List<CompanyWCClass> companyWorkersCompClasses = company.getWCClassesSorted();
    //        for (CompanyWCClass companyWCClass : companyWorkersCompClasses) {
    //            if (companyWCClass.getActive() != 0 && !companyWCClass.getClassCode().equalsIgnoreCase(WCConstants.WC_EXEMPT_CLASS_CODE)) {
    //				workersCompClassModels.add(companyWCClassToCompanyWorkersCompensationClassModel(companyWCClass));
    //            }
    //		}
    //		model.workersCompensationClasses = workersCompClassModels.toArray(new CompanyWorkersCompensationClassModel[] {});
    //
    //        return model;
    //	}
    //
    //	static public String companySsoExternalIDToSsoFederatedId(Company company) {
    //		String federatedID = company.getSsoExternalID();
    //		if (federatedID != null) {
    //			federatedID = federatedID.substring(0, federatedID.indexOf(SSOManager.EXTERNAL_ID_DELIM));
    //		}
    //		return federatedID;
    //
    //	}
    //
    //	static public BankAccountType stringToBankAccountType(String accountType) {
    //		return accountType.equals("S") ? BankAccountType.Savings : BankAccountType.Checking;
    //	}
    //	static public BankAccountModel employeeBankToBankAccountModel(EmployeeBank bank, Employee employee, boolean isBank2) {
    //		if (bank == null || Helper.isEmpty(bank.getRoutingNumber()) || Helper.isEmpty(bank.getAccountNumber())) {
    //			return null;
    //		}
    //
    //		BankAccountModel model = new BankAccountModel();
    //		model.bankRoutingNumberSensitized = bank.getRoutingNumber();
    //		model.accountNumberSensitized = bank.getAccountNumber();
    //		model.bankAccountType = stringToBankAccountType(bank.getAccountType());
    //		EmployeeSecret secret = employee.getMasterSecret();
    //		FeatureSet fs = employee.getWorkCompany().getFeatureSet();
    //                String routingNumber="", accountNumber="";
    //                if (!isBank2) {
    //                    routingNumber = secret.getRoutingNumber();
    //                    accountNumber = secret.getAccountNumber();
    //                }
    //                else {
    //                    routingNumber = secret.getRoutingNumber2();
    //                    accountNumber = secret.getAccountNumber2();
    //                }
    //                try {
    //                    model.bankName = BankInfo.validate(routingNumber, accountNumber, "", fs).getName();
    //                }
    //                catch (UserException userExp) {
    //                    // BankInfo validation might fail due to invalid routing number stored in EmployeeBank.
    //                    // This data non-integrity is the result of we don't clean up employee's reference to the deleted routing number when updating GRoutingInfo table
    //                    // catch that 'Bank.validate.badRoutingNumber' exception and ignore it:
    //                    if (!userExp.getMessage().contains(AppMgr.getUserMsg("Bank.validate.badRoutingNumber", new Object[] { routingNumber })))
    //                        throw new UserException(userExp.getMessage());
    //                }
    //
    //		return model;
    //	}
    //
    //	static public EmployeeTaxSetupModel employeeTaxSetupToEmployeeTaxSetupModel(EmployeeTaxSetup employeeTaxSetup) {
    //		if (employeeTaxSetup == null || !employeeTaxSetup.isReady()) {
    //			return null;
    //		}
    //
    //		EmployeeTaxSetupModel model = new EmployeeTaxSetupModel();
    //		model.additionalWithholding = doubleToBigDecimal(employeeTaxSetup.getExtraTaxAmount());
    //		model.allowanceAmount = doubleToBigDecimal(employeeTaxSetup.getExemptionAmount());
    //		model.allowances = employeeTaxSetup.getExemptions();
    //		model.filingStatus = employeeTaxSetup.getFilingStatus();
    //
    //		return model;
    //	}
    //
    //	static public EmployeeModel employeeToEmployeeModel(Employee employee) {
    //		if (employee == null) {
    //			return new EmployeeModel();
    //		}
    //
    //		EmployeeModel model = new EmployeeModel();
    //
    //		userToUserModel(employee, model);
    //
    ////		personToPersonModel(employee, model);
    //
    //		populateModelFromKeyedRecord(employee, model);
    //
    //		model.allowPaystubAccess = employee.hasRole(RoleID.EMPLOYEE);
    //		model.birthDate = toDateModel(employee.getBirthDate());
    //		model.payMethod = PayMethod.fromPayMethodConstant(employee.getPayMethod());
    //		model.directDepositAccount1 = employeeBankToBankAccountModel(employee.getBank(), employee, false);
    //		model.directDepositAccount2 = employeeBankToBankAccountModel(employee.getBank2(), employee, true);
    //
    //		// Convert to employmentStatus in employee
    //		EmploymentStatus employmentStatus = employee.getEmploymentStatus();
    //		if (employmentStatus == null) {
    //			// No saved employment status in DB yet - default from isActive and isNewHire.
    //			if (employee.getHomeAddress() == null) {
    //				model.employmentStatus = EmploymentStatus.Active;
    //			} else {
    //				model.employmentStatus = employee.isNewHire() ? EmploymentStatus.Active :
    //										 employee.isActive() ? EmploymentStatus.Active : EmploymentStatus.Terminated;
    //			}
    //		} else {
    //			model.employmentStatus = employee.getEmploymentStatus();
    //		}
    //		model.isActive = employee.isActive();
    //		model.isActiveForAccounting = employee.getIsActiveForAccounting();
    //
    //		// JobInfo fields.
    //		model.filedNewHireForm = !employee.isNewHire();
    //		model.filedI9Form = employee.getI9FilingStatus();
    //		model.hireDate = toDateModel(employee.getHireDate());
    //		if(employmentStatus != null && employmentStatus == EmploymentStatus.Terminated){
    //                    model.terminationDate = toDateModel(employee.getDeactivationDate());
    //                }
    //		EmployeeJobInfo jobInfo = employee.getJobInfo();
    //		if (jobInfo != null) {
    //			model.badge = jobInfo.getBadge();
    //			model.jobCategory = jobInfo.getJobCategory();
    //
    //			if(employmentStatus != null && (employmentStatus == EmploymentStatus.PaidLeave ||
    //							employmentStatus == EmploymentStatus.UnpaidLeave)){
    //				model.leaveStartDate = ModelHelper.toDateModel(jobInfo.getLeaveStartDate());
    //				model.leaveEndDate = ModelHelper.toDateModel(jobInfo.getLeaveEndDate());
    //				model.leaveReason = jobInfo.getLeaveReason();
    //				model.leaveReasonOther = jobInfo.getLeaveReasonOther();
    //			}else if(employmentStatus != null && employmentStatus == EmploymentStatus.Terminated){
    //				model.terminationReason = jobInfo.getTerminationReason();
    //				model.terminationReasonOther = jobInfo.getTerminationReasonOther();
    //				// As of 05/22/09, we are going to be consistent from database to UI by making everything eligibleForRehire, so we
    //				// have changed the previous revision where this line was model.eligibleForRehire = !jobInfo.getEligibleForRehire();
    //				model.eligibleForRehire = jobInfo.getEligibleForRehire();
    //			}
    //		}
    //
    //		//For manager all we need is an id for the UI. But since UI is coded to work with BaseModel, we just create a base model and
    //		//set its id.
    //		BaseModel managerModel = new BaseModel();
    //		managerModel.setId(employee.getManager() != null ? employee.getManager().getId() : -1);
    //		model.manager = managerModel;
    //
    //		// Determine if it's ok to display the BoA Outside DD choice in the paymethods setup
    //		// and also possibly translate the "real" PayMethods type to the BoaODD type.
    //		model.displayBoaODDChoice = false;
    //		if (PartnerMgr.getPartner(employee.getCompany()).isOutsideDirectDepositAllowed()) {
    //			FeatureSet fs = employee.getCompany().getFeatureSet();
    //			if (fs.hasFeature(Feature.PlusBehavior)) {
    //				model.displayBoaODDChoice = true;
    //				if (BOAVerifyAccount.isBoaOutsideDirectDepositHirelingForUI(employee)) {
    //					model.payMethod = PayMethod.DirectDepositBoaODD;
    //				}
    //			}
    //		}
    //
    //		// For reasons I do not know, with two direct deposit accounts or a direct deposit plus check,
    //		// the first and second accounts are swapped. The attempt here is to hide this craziness from the model
    //		// and only have it happen in the toplink layer.
    //		if (model.payMethod == PayMethod.DirectDeposit2Accounts || model.payMethod == PayMethod.DirectDepositPlusCheck) {
    //			BankAccountModel temp = model.directDepositAccount2;
    //			model.directDepositAccount2 = model.directDepositAccount1;
    //			model.directDepositAccount1 = temp;
    //		}
    //		model.directDepositAmount = doubleToBigDecimal(employee.getDepositValue());
    //		model.setJobTitle(employee.getJobTitle());
    //		model.setEmailAddress(employee.getEmailAddress());
    //		model.federalTaxSetup = employeeTaxSetupToEmployeeTaxSetupModel(employee.getFederalEmployeeTaxSetup());
    //		model.residenceTaxSetup = employeeTaxSetupToEmployeeTaxSetupModel(employee.getResidenceStateEmployeeTaxSetup());
    //		model.workTaxSetup = employeeTaxSetupToEmployeeTaxSetupModel(employee.getWorkStateEmployeeTaxSetup());
    //		int nonResidentCertificateState = employee.getNonResidentCertificateState();
    //		model.hasNonResidentCertificate =  nonResidentCertificateState == Employee.NONRESIDENTCERTIFICATE_NOTDEFINED ? null :
    //			(nonResidentCertificateState == Employee.NONRESIDENTCERTIFICATE_YES ? true : false);
    //
    //		model.firstName = employee.getFirstName();
    //		model.lastName = employee.getLastName();
    //		model.futaExemptReason = Helper.isEmpty(employee.getFUTAExemptCode()) ? null : Integer.parseInt(employee.getFUTAExemptCode());
    //		model.gender = employee.getGender() == null ? null : (employee.getGender().equalsIgnoreCase("F") ? Gender.Female : Gender.Male);
    ////		model.hasAnyRole = employee.getRoles().size() > 0;
    ////		model.hasAccessRole = employee.hasAccessRole();
    ////		model.isProviderAdmin = employee.isProviderAdmin();
    //
    //		model.setHomeAddress(addressToAddressModel(employee.getHomeAddress()));
    //
    //		model.index = getEmployeeModelIndex(employee);
    //		model.isAEICCalculated = employee.isAEICCalculated();
    //		model.isCorporateOfficer = employee.getIsCorporateOfficer();
    //		model.isHIRETaxCreditExempt = employee.getIsHIRETaxCreditExempt();
    //		model.requiresResidentLocalTaxAdditionalAmount = false;
    //		model.requiresWorkLocalTaxAdditionalAmount = false;
    //		List<LocalTaxItemModel> localTaxes;
    //        if (employee.getWorkTaxGeocode()!=null) {
    //            localTaxes = getLocalTaxes(employee.getWorkCompany(), employee, model);
    //            if (localTaxes.size() > 0) {
    //                model.localTaxItems = localTaxes.toArray(new LocalTaxItemModel[0]);
    //            }
    //        }
    //		model.hasOregonTransitTax = false;
    //		if (employee.getWorkAddress() != null) {
    //            if (employee.getWorkState().equals("OR")){
    //			    model.hasOregonTransitTax = true;
    //                if (employee.specialStateTax1Applies()) {
    //                    model.oregonTransitTax = OregonTransitTax.LTDTransitTax;
    //                } else if (employee.specialStateTax2Applies()) {
    //                    model.oregonTransitTax = OregonTransitTax.TriMetTransitTax;
    //                } else {
    //                    model.oregonTransitTax = OregonTransitTax.None;
    //                }
    //            }
    //            else if (employee.getWorkState().equals("MN") || employee.getWorkState().equals("MA") || employee.getWorkState().equals("IA")) {
    //                model.unitNumber = employee.getUnitNumber();
    //            }
    //		}
    //
    //        model.defaultUnitNumber = Helper.isEmpty(employee.getNamedStatus(Employee.UNIT_NUMBER, null));
    ////		model.canHaveLocalTaxes = model.localTaxItems != null && model.localTaxItems.length > 0;
    //        boolean fullMultistateFeature = (employee.getCompany().hasFeature(Feature.FullMultipleState));
    //		model.canHaveLocalTaxes = (employee.getWorkState()!=null && StateRegulationMgr.get(employee.getWorkState(), DateUtil.today()).hasLocalTax()) ||
    //                      fullMultistateFeature && (employee.getResidenceState()!=null && StateRegulationMgr.get(employee.getResidenceState(), DateUtil.today()).hasLocalTax());
    //		model.setMiddleInitial(employee.getMiddleInitial());
    //		model.occupationalTitleOrCode = employee.getNamedStatus("OccupationalTitleOrCode", "");
    //		model.commuterOnlyOptOut = ("true".equals(employee.getNamedStatus("commuterOnlyOptOut", ""))) ? true : false;
    //		model.payRateType = employee.getPayRate().isHourly() ? PayRateType.Hourly :
    //			employee.getPayRate().isCommissionOnly() ? PayRateType.CommissionOnly :
    //			employee.getPayRate().isSalary() ? PayRateType.Salary : null;
    //		if (employee.getPaySchedule() == null) {
    //			long defaultPayScheduleId = employee.getWorkCompany().getDefaultPaySchedule();
    //			if (defaultPayScheduleId > 0) {
    //				PaySchedule defaultPaySchedule = Helper.<PaySchedule>readObject(PaySchedule.class, defaultPayScheduleId);
    //				model.paySchedule = payScheduleToModel(defaultPaySchedule);
    //			}
    //		} else {
    //			model.paySchedule = payScheduleToModel(employee.getPaySchedule());
    //		}
    //
    //		List<WageItem> wageItems = Helper.readAllObjects(WageItem.class, "select * from GWageItems where display = 1 order by displayorder");
    //		List<CompanyWageItem> coWageItems = employee.getCompany().getSortedCompanyWageItems();
    //
    //		// Create a hashmap of active company wage items (with name)
    //		HashMap payTypesUsedByEEs = new HashMap();
    //		for (Employee ee : employee.getCompany().getEmployees()) {
    //			for (EmployeeWageItem ew : ee.getEmployeeWageItems()) {
    //				if (ew.getIsActive()) {
    //					// Hashmap is CompanyWageItem id, CompanyWageItem name
    //					payTypesUsedByEEs.put(new Long(ew.getCompanyWageItem().getId()), ew.getCompanyWageItem().getName());
    //				}
    //			}
    //		}
    //		// Create an list of wage types available to an employee (include those in use)
    //		List<PayTypeModel> payTypes = new ArrayList<PayTypeModel>();
    //		int countOfActiveAdvancedPayTypes = 0;
    //		for (WageItem wageItem : wageItems) {
    //			if (wageItem.display(employee.getCompany(), employee.getWorkState())) {
    //				// For hourly wage items, make all existing on company available
    //				if (wageItem.isHourly()) {
    //					for (CompanyWageItem coWageItem : coWageItems) {
    //						if (coWageItem.isHourly()) {
    //							// Add this item to the payTypes list
    //							PayTypeModel payType = companyWageItemToPayTypeModel(coWageItem, employee.getEmployeeWageItems());
    //
    //							if (payTypesUsedByEEs.get(new Long(coWageItem.getId())) != null) {
    //								payType.isAdvanced = false;
    //							}
    //							payTypes.add(payType);
    //							if (payType.isAdvanced && payType.isActive) {
    //								countOfActiveAdvancedPayTypes++;
    //							}
    //						}
    //					}
    //				}
    //				else {
    //					// Add a payType based on the WageItem
    //					PayTypeModel payType = wageItemToPayTypeModel(wageItem, employee.getEmployeeWageItems());
    //
    //					// if wage is in use, it is NOT advanced
    //					if (Helper.isNotEmpty(coWageItems)) {
    //					for (CompanyWageItem coWageItem : coWageItems) {
    //						if (coWageItem.getWageItem() == wageItem) {
    //							payType.isAdvanced = false;
    //							break;
    //						}
    //					}
    //					}
    //					payTypes.add(payType);
    //					if (payType.isAdvanced && payType.isActive) {
    //						countOfActiveAdvancedPayTypes++;
    //					}
    //				}
    //			}
    //		}
    //
    //		model.payTypes = payTypes.toArray(new PayTypeModel[payTypes.size()]);
    //		model.showAllAdvancedPayTypes = countOfActiveAdvancedPayTypes > 2;
    //
    //		model.payRateType = employee.getPayRate().isHourly() ? PayRateType.Hourly :
    //			employee.getPayRate().isCommissionOnly() ? PayRateType.CommissionOnly :
    //			employee.getPayRate().isSalary() ? PayRateType.Salary : null;
    //
    //
    //        // PAYTYPES NEW
    //		model.isTaxEmployerPaid = false; //default to false initially
    //        List<EmployeeWageItemModel> employeeWageItemModels = new ArrayList<EmployeeWageItemModel>();
    //		for (EmployeeWageItem employeeWageItem : employee.getEmployeeNonHourlyWageItems()) {
    //			employeeWageItemModels.add(employeeWageItemToEmployeeWageItemModel(employeeWageItem));
    //			if (employeeWageItem.getCompanyWageItem().getWageItem().getWageItemId() == WageItemID.REIMBURSED_EE_TAXES)
    //				model.isTaxEmployerPaid = employeeWageItem.getIsActive();
    //		}
    //		model.allWages = employeeWageItemModels.toArray(new EmployeeWageItemModel[0]);
    //
    //		// Create array of hourly wage items
    //        List<EmployeeWageItemModel> hourlyEmployeeWageItemModels = new ArrayList<EmployeeWageItemModel>();
    //		for (EmployeeWageItem employeeWageItem : employee.getEmployeeHourlyWageItems()) {
    //			hourlyEmployeeWageItemModels.add(employeeWageItemToEmployeeWageItemModel(employeeWageItem));
    //		}
    //		model.hourlyWages = hourlyEmployeeWageItemModels.toArray(new EmployeeWageItemModel[0]);
    //
    //		setSalaryInfo(model, employee);
    //		model.sickPtoPolicy = ptoPolicyToPtoPolicyModel(employee.getPtoPolicy(PtoConstants.SICK_PAY));
    //		model.vacationPtoPolicy = ptoPolicyToPtoPolicyModel(employee.getPtoPolicy(PtoConstants.VACATION_PAY));
    //		PtoCategory sickCategory = Helper.<PtoCategory>readObject(PtoCategory.class, PtoConstants.SICK_PAY);
    //		PtoHours sickHours = employee.getPtoHours(sickCategory);
    //		model.sickBalance = sickHours == null ? null : sickHours.getHoursAvailable();
    //		PtoCategory vacationCategory = Helper.<PtoCategory>readObject(PtoCategory.class, PtoConstants.VACATION_PAY);
    //		PtoHours vacationHours = employee.getPtoHours(vacationCategory);
    //		model.vacationBalance =  vacationHours == null ? null : vacationHours.getHoursAvailable();
    ////		model.socialSecurityNumberSensitized = employee.getSocialSecurityNumber();
    ////		model.socialSecurityNumber = employee.getSocialSecurityNumberSecret();
    //
    //		if (employee.getWorkLocation() != null && employee.getHomeAddress() != null) {
    //			List<TaxGroup> taxGroups = employee.getPossibleTaxExemptionGroups();
    //			List<TaxGroupModel> taxGroupModels = new Vector<TaxGroupModel>();
    //			Company eeCompany = employee.getCompany();
    //			for (TaxGroup taxGroup : taxGroups) {
    //				 if(!(eeCompany.isCompany501c3() && (taxGroup.getId() == 1)) && !(taxGroup.getId() == 15)) {
    //					taxGroupModels.add(ModelHelper.taxGroupToTaxGroupModel(taxGroup, employee.isTaxExemptionGroupExempt(taxGroup)));
    //				}
    //			}
    //			model.taxExemptions = taxGroupModels.toArray(new TaxGroupModel[taxGroupModels.size()]);
    //		}
    //		model.workLocation = workLocationToWorkLocationModel(employee.getWorkLocation());
    //
    //		List<EmployeeDeductionModel> employeeDeductionModels = new ArrayList<EmployeeDeductionModel>();
    //		for (EmployeeDeduction employeeDeduction : employee.getDeductions()) {
    //			employeeDeductionModels.add(employeeDeductionToEmployeeDeductionModel(employeeDeduction));
    //		}
    //		for (EmployerContribution employerContribution : employee.getEmployerContributions()) {
    //			employeeDeductionModels.add(employerContributionToEmployeeDeductionModel(employerContribution));
    //		}
    //		model.deductions = employeeDeductionModels.toArray(new EmployeeDeductionModel[0]);
    //		model.garnishmentWeighting = employee.getGarnishmentWeightingChoiceValue();
    //
    //		model.sickPtoPolicy = ptoPolicyToPtoPolicyModel(employee.getPtoPolicy(PtoConstants.SICK_PAY));
    //		model.vacationPtoPolicy = ptoPolicyToPtoPolicyModel(employee.getPtoPolicy(PtoConstants.VACATION_PAY));
    //
    //		// This is WALI code.
    //		List<EmployeeWorkersCompensationClassModel> employeeWorkersCompensationClassModels = new ArrayList<EmployeeWorkersCompensationClassModel>();
    //		for (EmployeeWCClass employeeWCClass : employee.getWCClasses()) {
    //			employeeWorkersCompensationClassModels.add(employeeWCClassToEmployeeWorkersCompensationClassModel(employeeWCClass));
    //		}
    //		model.employeeWorkersCompensationClasses = employeeWorkersCompensationClassModels.toArray(new EmployeeWorkersCompensationClassModel[0]);
    //		model.currentEmployeeWorkersCompensation = employeeWCClassToEmployeeWorkersCompensationClassModel(employee.getWCClass(new Date()));
    //
    //		// This is for standard Workers Compensation (non-WALI).
    //		model.workersComp = getWorkersCompModel(employee.getCompany(), employee.getWorkAddress() == null ? 0 : employee.getWorkAddress().getId());
    //		model.workersComp.workersCompClass = employee.getClassName();
    //
    //		model.workLocalTaxExtraAmount = doubleToBigDecimal(employee.getLocalWorkExtraTaxAmount());
    //		model.residentLocalTaxExtraAmount = doubleToBigDecimal(employee.getLocalResExtraTaxAmount());
    //
    //		model.department = employee.getDepartment();
    //
    //        model.timeTracking = employee.getTimeTracking();
    //
    //        model.hasTimeTracking = employee.hasTimeTracking();
    //
    //        model.notes = employee.getNotes() == null ? null : new String(employee.getNotes());
    //
    //		return model;
    //	}
    //
    //    private static <T> T[] cleansePersonReference(List<T> contacts) {
    //        List<PersonContact> result = new Vector<PersonContact>();
    //        Class componentType = null;
    //
    //        for (T contact : contacts) {
    //            PersonContact newContact = null;
    //            if (contact instanceof IM) {
    //                componentType = IM.class;
    //                newContact = ((IM) contact).shallowCopy();
    //            } else if (contact instanceof Phone) {
    //                componentType = Phone.class;
    //                newContact = ((Phone) contact).shallowCopy();
    //            } else if (contact instanceof EmailAddr) {
    //                componentType = EmailAddr.class;
    //                newContact = ((EmailAddr) contact).shallowCopy();
    //            }
    //            // PD-9694 - personModel should not have reference to toplink Person
    //            newContact.setPerson(null);
    //            result.add(newContact);
    //        }
    //
    //        return (T[]) result.toArray((T[]) Array.newInstance(componentType, 1));
    //    }
    //
    //	/**
    //	 * Transfers User fields to UserModel.
    //	 * @param user
    //	 * @param model
    //	 */
    //	public static void userToUserModel(User user, UserModel model) {
    //
    //		personToPersonModel(user, model);
    //
    //		populateModelFromKeyedRecord(user, model);
    //
    //		// Moved from EmployeeModel
    //		model.hasAnyRole = user.getRoles().size() > 0;
    //		model.hasAccessRole = user.hasAccessRole();
    //		model.isProviderAdmin = user.isProviderAdmin();
    //
    //		// Initialize SSN for employees differently than for contractors
    //		if (user.getUserType().equals(UserType.EMPLOYEE)) {
    //			if (Helper.isNotEmpty(user.toEmployee())) {
    //				model.socialSecurityNumberSensitized = user.toEmployee().getSocialSecurityNumber();
    //				model.socialSecurityNumber = user.toEmployee().getSocialSecurityNumberSecret();
    //			}
    //		}
    //		else if (user.getUserType().equals(UserType.CONTRACTOR)) {
    //			Company co = user.getCompany();
    //			if (Helper.isNotEmpty(co) && Helper.isNotEmpty(co.getContractorByContact(user.getId()))) {
    //				model.socialSecurityNumberSensitized = co.getContractorByContact(user.getId()).getClearSSNStripped();
    //				model.socialSecurityNumber = co.getContractorByContact(user.getId()).getClearTIN();
    //			}
    //		}
    //	}
    //
    //    /**
    //	 * Transfers Person fields to PersonModel.
    //	 * @param person
    //	 * @param model
    //	 */
    //	public static void personToPersonModel(Person person, PersonModel model) {
    //		int size;
    //		populateModelFromKeyedRecord(person, model);
    //		model.setFirstName(person.getFirstName());
    //		model.setLastName(person.getLastName());
    //		model.setMiddleInitial(person.getMiddleInitial());
    //		model.setHomeAddress(addressToAddressModel(person.getHomeAddress()));
    //		model.setEmailAddress(person.getEmailAddress());
    //		model.setJobTitle(person.getJobTitle());
    //		model.setRace(person.getRace());
    //
    //		size = person.getPhones().size();
    //		if (size > 0) {
    //            model.setPhones((Phone[]) cleansePersonReference(person.getPhones()));
    //        }
    //		size = person.getEmailAddresses().size();
    //		if (size > 0) {
    //			model.setEmails((EmailAddr[]) cleansePersonReference(person.getEmailAddresses()));
    //        }
    //		size = person.getIMs().size();
    //		if (size > 0) {
    //			model.setIms((IM[]) cleansePersonReference(person.getIMs()));
    //        }
    //
    //		size = person.getEmergencyContact2() == null ? (person.getEmergencyContact1() == null ? 0 : 1) : 2;
    //		EmergencyContactModel[] emergencyContacts = new EmergencyContactModel[size];
    //		if(person.getEmergencyContact1() != null){
    //			EmergencyContactModel ec1Model = new EmergencyContactModel();
    //			personToEmergencyContactModel(person.getEmergencyContact1(), ec1Model);
    //			emergencyContacts[0] = ec1Model;
    //		}
    //		if(person.getEmergencyContact2() != null){
    //			EmergencyContactModel ec2Model = new EmergencyContactModel();
    //			personToEmergencyContactModel(person.getEmergencyContact2(), ec2Model);
    //			emergencyContacts[1] = ec2Model;
    //		}
    //		if (size > 0)
    //			model.setEmergencyContacts(emergencyContacts);
    //
    //		size = person.getCustomFields().size();
    //		if (size > 0){
    //			List<CustomField> customFields = new ArrayList<CustomField>();
    //			for(CustomField cf : person.getCustomFields()){
    //                CustomField newCustomField = cf.shallowCopy();
    //                newCustomField.getLabel().setCompany(null);
    //				customFields.add(newCustomField);
    //			}
    //			model.personCustomFields = customFields.toArray(new PersonCustomField[size]);
    //		}
    //
    //	}
    //
    //	/**
    //	 * Transfers Person fields to EmergencyContactModel.
    //	 * @param person
    //	 * @param model
    //	 */
    //	private static void personToEmergencyContactModel(Person person, EmergencyContactModel model) {
    //		populateModelFromKeyedRecord(person, model);
    //		model.setFirstName(person.getFirstName());
    //		model.setLastName(person.getLastName());
    //		model.setMiddleInitial(person.getMiddleInitial());
    //        if(person.getPhones() != null && person.getPhones().size()>0){
    //            model.setPhones((Phone[])cleansePersonReference(person.getPhones()));
    //        }
    //        if(person.getEmailAddresses() != null && person.getEmailAddresses().size()>0){
    //            model.setEmails((EmailAddr[]) cleansePersonReference(person.getEmailAddresses()));
    //        }
    //	}
    //
    //	/**
    //	 * Gets the COmpany;s WorkersCompModel for a given work location id.  This
    //	 * tells us if workers comp should be shown for the given work location.
    //	 * The helpId is dynamic as is whether to show the promo text.
    //	 *
    //	 * @param co				The company.
    //	 * @param workLocationId	The id of the work location.
    //	 *
    //	 * @return A WorkersCompModel object containing Worker's Comp info for the work location.
    //	 */
    //	static public WorkersCompModel getWorkersCompModel(Company co, long workLocationId) {
    //		WorkersCompModel workersCompModel = new WorkersCompModel();
    //		String state = workLocationId <= 0 ? co.getPrimaryWorkLocation().getState() : co.getWorkLocation(workLocationId).getState();
    //		workersCompModel.showWorkersComp = !StateRegulationMgr.get(state, new Date()).isDisplayTaxRate(TaxRateConstants.WCRATE);
    //		workersCompModel.showWorkersCompPromo = workersCompModel.showWorkersComp && WorkersCompMgr.ranFirstPayroll(co) && WorkersCompMgr.isEligibleForWCAndMaybeInterested(co) && !WorkersCompMgr.isEnrolledInWC(co);
    //		workersCompModel.workersCompHelpId = StateRegulationMgr.get(state, DateUtil.today()).getStringWithDefault("EmployeeClassFAQID",WorkersCompMgr.getFAQId(co));
    //		return workersCompModel;
    //	}
    //
    //	static public EmployeeDeductionModel employerContributionToEmployeeDeductionModel(EmployerContribution employerContribution) {
    //		if (employerContribution == null) {
    //			return null;
    //		}
    //
    //		EmployeeDeductionModel model = new EmployeeDeductionModel();
    //        populateModelFromKeyedRecord(employerContribution, model);
    //		model.companyDeduction = companyDeductionToCompanyDeductionModel(employerContribution.getCompanyDeduction());
    //		model.amount = doubleToBigDecimal(employerContribution.getAmount());
    //		model.amountType = AmountType.fromLegacyValue(employerContribution.getAmountType());
    //		model.amountForDisplay = model.amountType == AmountType.Dollar ? StringUtil.format(model.amount, "dollar") : (StringUtil.format(model.amount, "#####0.0") + "%");
    //		model.maxAmount = doubleToBigDecimal(employerContribution.getMaxAmount());
    //		model.maxAmountForDisplay = model.maxAmount.compareTo(BigDecimal.ZERO) == 0 ? "" : StringUtil.format(model.maxAmount, "dollar");
    //		model.isActive = employerContribution.isActive();
    //		model.isGarnishment = false;
    //		model.deductionType = DeductionType.EmployerContribution;
    //		model.companyDeduction.isEditMaxAmount = true;
    //
    //		return model;
    //	}
    //
    //	static public EmployeeDeductionModel employeeDeductionToEmployeeDeductionModel(EmployeeDeduction employeeDeduction) {
    //		if (employeeDeduction == null) {
    //			return null;
    //		}
    //
    //		EmployeeDeductionModel model = new EmployeeDeductionModel();
    //        populateModelFromKeyedRecord(employeeDeduction, model);
    //
    //		model.companyDeduction = companyDeductionToCompanyDeductionModel(employeeDeduction.getCompanyDeduction());
    //		if (employeeDeduction.getCompanyDeduction().getDeductionItem().getCategory().getId() != DeductionCategory.GARNISHMENTS) {
    //			model.amount = doubleToBigDecimal(employeeDeduction.getAmount());
    //			model.amountType = AmountType.fromLegacyValue(employeeDeduction.getAmountType());
    //			model.amountForDisplay = model.amountType == AmountType.Dollar ? StringUtil.format (model.amount, "dollar") : StringUtil.format (model.amount, "#####0.0") + "%";
    //			model.deductionType = DeductionType.EmployeeDeduction;
    //			model.isActive = employeeDeduction.isActive();
    //			model.isGarnishment = false;
    //			model.maxAmount = doubleToBigDecimal(employeeDeduction.getMaxAmount(DateUtil.today()));
    //			model.maxAmountForDisplay = model.maxAmount.compareTo(BigDecimal.ZERO) == 0 ? "" : StringUtil.format(model.maxAmount, "dollar");
    //		} else {
    //			model.exemptDeductionAdjustment = doubleToBigDecimal(((Garnishment) employeeDeduction.getCompanyDeduction()).getExemptAmountAdjustment());
    //			model.isGarnishment = true;
    //			model.isActive = employeeDeduction.isActive();
    //			model.weighting = GarnishmentWeighting.fromLegacyValue(((Garnishment) employeeDeduction.getCompanyDeduction()).getGarnishmentWeighting());
    //			if (employeeDeduction.getCompanyDeduction().getDeductionItem().getId() == DeductionItem.FEDERAL_TAX_LEVY) {
    //				FederalTaxLevyGarnishment garnishment = (FederalTaxLevyGarnishment) employeeDeduction.getCompanyDeduction();
    //				model.amountExempt = doubleToBigDecimal(garnishment.getAmountExempt());
    //				model.amountForDisplay = StringUtil.format(model.amountExempt, "dollar");
    //				model.deductionType = DeductionType.GarnishmentTaxLevy;
    //			} else if (employeeDeduction.getCompanyDeduction().getDeductionItem().getId() == DeductionItem.SUPPORT_GARNISHMENT) {
    //				SupportGarnishment garnishment = (SupportGarnishment) employeeDeduction.getCompanyDeduction();
    //				model.amountRequested = doubleToBigDecimal(garnishment.getAmountRequested());
    //				model.amountForDisplay = StringUtil.format(model.amountRequested, "dollar");
    //				model.maxPercent = doubleToBigDecimal(garnishment.getMaxPercent());
    //				model.maxAmountForDisplay = model.maxPercent.compareTo(BigDecimal.ZERO) == 0 ? "" : StringUtil.format(model.maxPercent, "#####0.0") + "%";
    //				model.deductionType = DeductionType.GarnishmentChildSpousalSupport;
    //			} else if (employeeDeduction.getCompanyDeduction().getDeductionItem().getId() == DeductionItem.OTHER_GARNISHMENT) {
    //				OtherGarnishment garnishment = (OtherGarnishment) employeeDeduction.getCompanyDeduction();
    //				model.totalAmountOwed = doubleToBigDecimal(garnishment.getTotalAmountOwed());
    //				model.otherGarnishmentAmountType = AmountType.fromLegacyValue(garnishment.getOtherGarnishmentAmountType());
    //				model.otherGarnishmentAmount = doubleToBigDecimal(garnishment.getOtherGarnishmentAmount());
    //				model.alternateGarnishmentCap = (garnishment.getAlternateGarnishmentCap() > 0) ? doubleToBigDecimal(garnishment.getAlternateGarnishmentCap()) : null;
    //				model.deductionType = DeductionType.GarnishmentOther;
    //				model.amountForDisplay = model.otherGarnishmentAmountType == AmountType.Dollar ? StringUtil.format (model.otherGarnishmentAmount, "dollar") : StringUtil.format (model.otherGarnishmentAmount, "#####0.0") + "%";
    //				model.maxAmountForDisplay = garnishment.getAlternateGarnishmentCap() <= 0 ? "" : StringUtil.format(model.alternateGarnishmentCap, "dollar");
    //			}
    //		}
    //
    //
    //		return model;
    //	}
    //
    //	static public CompanyDeductionModel companyDeductionToCompanyDeductionModel(CompanyDeduction companyDeduction) {
    //		if (companyDeduction == null) {
    //			return null;
    //		}
    //
    //		CompanyDeductionModel model = new CompanyDeductionModel();
    //        populateModelFromKeyedRecord(companyDeduction, model);
    //		model.contributionType = ContributionType.fromLegacyValue(companyDeduction.getDeductionItem().getContributionType());
    //		model.deductionItemId = companyDeduction.getDeductionItem().getId();
    //		model.deductionItemDecription = companyDeduction.getDeductionItem().getDescription();
    //		model.description = companyDeduction.getDescription();
    //		model.isEditSetup = companyDeduction.getDeductionItem().editEmployeeSetup();
    //		model.isEditMaxAmount = companyDeduction.isEditMaxAmount();
    //		model.isMaxAmountOptional = companyDeduction.getDeductionItem().isGlobalMaxOptional(DateUtil.today());
    //		model.maxAmount = companyDeduction.getMaxAmount(DateUtil.today());
    //		model.isGarnishment = companyDeduction.getDeductionItem().isGarnishment();
    //		model.isAssigned = companyDeduction.isAssigned();
    //		model.categoryId = companyDeduction.getDeductionItem().getCategory().getId();
    //
    //		return model;
    //	}
    //
    //	static public TaxGroupModel taxGroupToTaxGroupModel(TaxGroup group, boolean isActive) {
    //		if (group == null) {
    //			return null;
    //		}
    //
    //		TaxGroupModel model = new TaxGroupModel();
    //        populateModelFromKeyedRecord(group, model);
    //		model.description = (group.getJurisdiction().equals("FD") ? "" : (group.getJurisdiction() + " ")) +group.getDescription();
    //		model.isActive = isActive;
    //		model.jurisdiction = group.getJurisdiction();
    //
    //		return model;
    //	}
    //
    //	static public PtoPolicyModel ptoPolicyToPtoPolicyModel(PtoPolicy policy) {
    //		if (policy == null) {
    //			return null;
    //		}
    //
    //		PtoPolicyModel model = new PtoPolicyModel();
    //        populateModelFromKeyedRecord(policy, model);
    //		model.category = PtoCategoryModel.fromCategoryId(policy.getCategory().getId());
    //		model.description = policy.getDescription();
    //		model.accrualFrequency = PtoAccrualFrequency.fromConstant(policy.getAccrualFrequency());
    //		model.accrualMaximum = doubleToBigDecimal(policy.getAccrualMaximum());
    //		model.accrualRate = doubleToBigDecimal(policy.getAccrualRate());
    //
    //		return model;
    //	}
    //
    //	static public PayTypeModel wageItemToPayTypeModel(WageItem wageItem, List<EmployeeWageItem> employeeWageItems) {
    //		PayTypeModel model = new PayTypeModel();
    //
    //		model.wageItemId = wageItem.getWageItemId();
    //		model.wageItemName = wageItem.getName();
    //		model.hasRecurringAmount = wageItem.isRecurring();
    //		model.isAdvanced = !wageItem.isBasic();
    //		model.isActive = false;
    //		model.isFringe = wageItem.isFringeBenefit();
    //		model.wageItemExplanation = wageItem.getExplanation();
    //
    //		// Locate the corresponding EmployeeWageItem for additional data on the EmployeeWageItem
    //		for (EmployeeWageItem employeeWageItem : employeeWageItems) {
    //			if (employeeWageItem.getWageItem().getWageItemId() == model.wageItemId) {
    //				model.recurringAmount = employeeWageItem.getRecurringAmount();
    //				model.isActive = employeeWageItem.getIsActive();
    //                model.employeeWageItemId = employeeWageItem.getId();
    //				break;
    //			}
    //		}
    //
    //		return model;
    //	}
    //
    //	static public PayTypeModel companyWageItemToPayTypeModel(CompanyWageItem coWageItem, List<EmployeeWageItem> employeeWageItems) {
    //		PayTypeModel model = new PayTypeModel();
    //
    //		model.wageItemId = coWageItem.getWageItem().getWageItemId();
    //		model.wageItemName = coWageItem.getName();
    //		model.hasRecurringAmount = coWageItem.getWageItem().isRecurring();
    //		model.isAdvanced = !coWageItem.getWageItem().isBasic();
    //		model.isActive = false;
    //		model.isFringe = coWageItem.getWageItem().isFringeBenefit();
    //		model.wageItemExplanation = coWageItem.getWageItem().getExplanation();
    //
    //		// Locate the corresponding EmployeeWageItem for additional data on the EmployeeWageItem
    //		for (EmployeeWageItem employeeWageItem : employeeWageItems) {
    //			if (employeeWageItem.getCompanyWageItem() == coWageItem) {
    //				model.recurringAmount = employeeWageItem.getRecurringAmount();
    //				model.isActive = employeeWageItem.getIsActive();
    //                model.employeeWageItemId = employeeWageItem.getId();
    //				break;
    //			}
    //		}
    //
    //		return model;
    //	}
    //
    //    static public CompanyWageItemModel companyWageItemToCompanyWageItemModel(CompanyWageItem coWageItem) {
    //        CompanyWageItemModel model = new CompanyWageItemModel();
    //        populateModelFromKeyedRecord(coWageItem, model);
    //
    //        model.isActive = coWageItem.getActive();
    //        model.name = coWageItem.getName();
    //        model.permanentName = coWageItem.getPermanentName();
    //        model.isNonCash = coWageItem.isNonCash();
    //        model.rate = coWageItem.getRate();
    //        model.displayOrder = coWageItem.getDisplayOrder();
    //        model.setId(coWageItem.getId());
    //        model.wageItemId = coWageItem.getWageItem().getWageItemId();
    //        model.isPaidByHours = coWageItem.getWageItem().isPaidByHours();
    //        model.payDayName = coWageItem.getPayDayName();
    //
    //        return model;
    //    }
    //
    //    static public EmployeeWageItemModel employeeWageItemToEmployeeWageItemModel(EmployeeWageItem eeWageItem) {
    //        EmployeeWageItemModel model = new EmployeeWageItemModel();
    //        populateModelFromKeyedRecord(eeWageItem, model);
    //
    //        model.companyWageItem = companyWageItemToCompanyWageItemModel(eeWageItem.getCompanyWageItem());
    //        model.isActive = eeWageItem.getActive();
    //        model.isBaseRate = eeWageItem.isBaseRate();
    //        model.isHourly = eeWageItem.getCompanyWageItem().isHourly();
    //        model.isNonCash = eeWageItem.isNonCash();
    //        model.recurringAmount = eeWageItem.getRecurringAmount();
    //        model.rate = eeWageItem.getRate();
    //        model.details = eeWageItem.getSalaryDetails();
    //        model.setId(eeWageItem.getId());
    //
    //        return model;
    //    }
    //
    //    static public EmployeeWageItemModel getEmployeeWageItemModel(EmployeeWageItemModel[] wageArray, WageItemID wageItemId) {
    //		// Find employee wage item model
    //		EmployeeWageItemModel employeeWageModel = null;
    //		if (Helper.isNotEmpty(wageArray)) {
    //			for (EmployeeWageItemModel eeWageModel : wageArray) {
    //				// Use wageItemId where ever set
    //				if ((Helper.isNotEmpty(eeWageModel.companyWageItem) && Helper.isNotEmpty(eeWageModel.companyWageItem.wageItemId) && eeWageModel.companyWageItem.wageItemId == wageItemId) ||
    //				    (Helper.isNotEmpty(eeWageModel.wageItemId) && eeWageModel.wageItemId == wageItemId)) {
    //					return eeWageModel;
    //				}
    //			}
    //		}
    //		return null;
    //    }
    //
    //    static public EmployeeWageItemModel getHourlyEmployeeWageItemModel(EmployeeWageItemModel[] wageArray, EmployeeModel employeeModel, boolean isBaseRate) {
    //		// Find hourly employee wage item model
    //		EmployeeWageItemModel employeeWageModel = null;
    //		if (employeeModel != null && wageArray != null) {
    //			for (EmployeeWageItemModel eeWageModel : wageArray) {
    //				if (/*eeWageModel.isActive && */eeWageModel.isHourly) {
    //					if ((isBaseRate && eeWageModel.isBaseRate) ||
    //					    (!isBaseRate && !eeWageModel.isBaseRate)){
    //						return eeWageModel;
    //					}
    //				}
    //			}
    //		}
    //		return null;
    //    }
    //
    //    static public CompanyWageItemModel getCompanyWageItemModel(CompanyModel companyModel, WageItemID wageItemId) {
    //		// Find company wage item model
    //		CompanyWageItemModel companyWageModel = null;
    //		if (companyModel != null && companyModel.companyWageItems != null) {
    //			for (CompanyWageItemModel coWageModel : companyModel.companyWageItems) {
    //				if (coWageModel.wageItemId == wageItemId) {
    //					companyWageModel = coWageModel;
    //					return coWageModel;
    //				}
    //			}
    //		}
    //		return null;
    //    }
    //
    //    static public CompanyWageItemModel getCompanyWageItemModel(CompanyModel companyModel, String permanentName) {
    //		// Find company wage item model
    //		CompanyWageItemModel companyWageModel = null;
    //		if (companyModel != null && companyModel.companyWageItems != null) {
    //			for (CompanyWageItemModel coWageModel : companyModel.companyWageItems) {
    //				if (coWageModel.permanentName != null && coWageModel.permanentName.equalsIgnoreCase(permanentName)) {
    //					companyWageModel = coWageModel;
    //					return coWageModel;
    //				}
    //			}
    //		}
    //		return null;
    //    }
    //
    //    static public CompanyWageItemModel getCompanyWageItemModel(CompanyModel companyModel, long id) {
    //		// Find company wage item model
    //		CompanyWageItemModel companyWageModel = null;
    //		if (companyModel != null && companyModel.companyWageItems != null) {
    //			for (CompanyWageItemModel coWageModel : companyModel.companyWageItems) {
    //				if (coWageModel.getId() == id) {
    //					companyWageModel = coWageModel;
    //					return coWageModel;
    //				}
    //			}
    //		}
    //		return null;
    //    }
    //
    //    static public CompanyWageItemModel getCompanyWageItemModel(CompanyModel companyModel, CompanyWageItemModel wageModel) {
    //		// Find company wage item model
    //    	if (wageModel != null) {
    //			long id = wageModel.getId();
    //			if (companyModel != null && companyModel.companyWageItems != null) {
    //				for (CompanyWageItemModel coWageModel : companyModel.companyWageItems) {
    //					// Use id if non-zero
    //					if (id > 0) {
    //						if (coWageModel.getId() == id) {
    //							return coWageModel;
    //						}
    //					}
    //					else if (wageModel.permanentName != null || wageModel.permanentName != "") {
    //						if (wageModel.permanentName.equalsIgnoreCase(coWageModel.permanentName)) {
    //							return coWageModel;
    //						}
    //					}
    //				}
    //			}
    //    	}
    //		return null;
    //    }
    //
    //
    //	static public String getHourlyBaseRateName(EmployeeModel eeModel) {
    //		return getHourlyRateName(eeModel, 0);
    //	}
    //
    //	static public String getHourlyRate2Name(EmployeeModel eeModel) {
    //		return getHourlyRateName(eeModel, 1);
    //	}
    //
    //	static public String getHourlyRateName(EmployeeModel eeModel, int index) {
    //		String name = null;
    //		if (eeModel.hourlyWages != null) {
    //			for(EmployeeWageItemModel wage: eeModel.hourlyWages) {
    //				if (wage.isHourly && wage.isBaseRate && wage.isActive && index == 0) {
    //					name = wage.companyWageItem.name;
    //					break;
    //				}
    //				if (wage.isHourly && !wage.isBaseRate && wage.isActive && index == 1) {
    //					name = wage.companyWageItem.name;
    //					break;
    //				}
    //			}
    //		}
    //		return name;
    //	}
    //
    //	static public BigDecimal getHourlyBaseRate(EmployeeModel eeModel) {
    //		return getHourlyRate(eeModel, 0);
    //	}
    //
    //	static public BigDecimal getHourlyRate2(EmployeeModel eeModel) {
    //		return getHourlyRate(eeModel, 1);
    //	}
    //
    //	static public BigDecimal getHourlyRate(EmployeeModel eeModel, int index) {
    //		BigDecimal rate = null;
    //		if (eeModel.hourlyWages != null) {
    //			for(EmployeeWageItemModel wage: eeModel.hourlyWages) {
    //				if (wage.isHourly && wage.isBaseRate && wage.isActive && index == 0) {
    //					rate = wage.rate;
    //					break;
    //				}
    //				if (wage.isHourly && !wage.isBaseRate && wage.isActive && index == 1) {
    //					rate = wage.rate;
    //					break;
    //				}
    //			}
    //		}
    //		return rate;
    //	}
    //
    //	static public void setSalaryInfo(EmployeeModel model, Employee emp) {
    //		model.salaryRate = emp.getPayRate().getSalaryRate();
    //		model.salaryDaysPerWeek = emp.getPayRate().getSalaryDaysPerWeek();
    //		model.salaryFrequency = SalaryFrequency.fromSalaryFrequencyInteger(emp.getPayRate().getSalaryFrequency());
    //		model.salaryHoursPerDay = emp.getPayRate().getSalaryHoursPerDay();
    //	}
    //
    //	static public void resetSalaryInfo(EmployeeModel model, Employee emp) {
    ////		emp.setSalaryDefaults();
    //		emp.setSalary(null);
    //		setSalaryInfo(model, emp);
    //		// Ensure model is updated
    //		model.setVersion(-1L);
    //	}
    //
    //    static public LocalTaxItemModel localTaxItemToLocalTaxItemModel(EmployeeLocalTaxItem localTaxItem) {
    //		if (localTaxItem == null) {
    //			return null;
    //		}
    //
    //		LocalTaxItemModel model = new LocalTaxItemModel();
    //        populateModelFromKeyedRecord(localTaxItem.getLocalTaxItem(), model);
    //		model.taxItemId = localTaxItem.getLocalTaxItem().getTaxItem().getId();
    //		model.taxType = localTaxItem.getTaxType() == TaxItem.TAXTYPE_RESIDENCEBASED ? TaxType.Residence : TaxType.Work;
    //		model.name = localTaxItem.getLocalTaxItem().getTaxItem().getName();
    //
    //		return model;
    //	}
    //
    //	static public PaycheckWagesModel wageDetailToPaycheckWagesModel(WageDetail wageDetail) {
    //		PaycheckWagesModel model = new PaycheckWagesModel();
    //		if (Helper.isNotEmpty(wageDetail)) {
    //			model.wageAmount = TypeUtil.toMoney(wageDetail.getWageAmount());
    //			model.ytdAmount = TypeUtil.toMoney(wageDetail.getYtdAmount());
    //			model.hours = wageDetail.getHours();
    //			model.rate = wageDetail.getRate();
    //			model.wageItemId = wageDetail.getWageItemId();
    //			model.description = wageDetail.getWageItem().getPayDayName();
    //			model.employeeWageItemId = Helper.isNotEmpty(wageDetail.getEmployeeWageItem()) ? wageDetail.getEmployeeWageItem().getId() : null;
    //			model.wagePermanentName = Helper.isNotEmpty(wageDetail.getEmployeeWageItem()) ? wageDetail.getEmployeeWageItem().getPermanentName() : null;
    //			model.isActive = Helper.isNotEmpty(wageDetail.getEmployeeWageItem()) ? wageDetail.getEmployeeWageItem().getActive() : null;
    //		}
    //		return model;
    //	}
    //
    //	static public PaycheckTaxesModel taxDetailToPaycheckTaxesModel(TaxDetail taxDetail) {
    //		PaycheckTaxesModel model = new PaycheckTaxesModel();
    //		if (Helper.isNotEmpty(taxDetail)) {
    //			model.taxType = PaycheckTaxesModel.PaycheckTaxType.fromLegacy(taxDetail.getTaxType());
    //	        model.isEmployer = taxDetail.getTaxItem().isEmployer();
    //			model.taxAmount = TypeUtil.toMoney(taxDetail.getTaxAmount());
    //			model.wageBasis = TypeUtil.toMoney(taxDetail.getWageBasis());
    //			model.incomeSubjectToTax = TypeUtil.toMoney(taxDetail.getIncomeSubjectToTax());
    //			model.qtdAmount = TypeUtil.toMoney(taxDetail.getQtdAmount());
    //			model.qtdWageBasis = TypeUtil.toMoney(taxDetail.getQtdWageBasis());
    //			model.ytdAmount = TypeUtil.toMoney(taxDetail.getYtdAmount());
    //			model.ytdWageBasis = TypeUtil.toMoney(taxDetail.getYtdWageBasis());
    //			model.qtdIncomeSubjectToTax = TypeUtil.toMoney(taxDetail.getQtdIncomeSubjectToTax());
    //			model.ytdIncomeSubjectToTax = TypeUtil.toMoney(taxDetail.getYtdIncomeSubjectToTax());
    //			model.taxItemId = taxDetail.getTaxItemId();
    //			model.description = taxDetail.getTaxItem().getName();
    //		}
    //		return model;
    //	}
    //
    //	static public PaycheckDeductionModel deductionDetailToPaycheckDeductionModel(DeductionDetail deductionDetail) {
    //		PaycheckDeductionModel model = new PaycheckDeductionModel();
    //		if (Helper.isNotEmpty(deductionDetail)) {
    //			model.employeeDeduction = employeeDeductionToEmployeeDeductionModel(deductionDetail.getEmployeeDeduction());
    //			model.amount = TypeUtil.toMoney(deductionDetail.getAmount());
    //			model.ytdAmount = TypeUtil.toMoney(deductionDetail.getYtdAmount());
    //			model.isPreTax = deductionDetail.isPreTax();
    //		}
    //		return model;
    //	}
    //
    //	static public PaycheckContributionModel contributionDetailToPaycheckContributionModel(ContributionDetail contributionDetail) {
    //		PaycheckContributionModel model = new PaycheckContributionModel();
    //		if (Helper.isNotEmpty(contributionDetail)) {
    //			model.employerContribution = employerContributionToEmployeeDeductionModel(contributionDetail.getEmployerContribution());
    //		    model.amount = TypeUtil.toMoney(contributionDetail.getAmount());
    //		    model.ytdAmount = TypeUtil.toMoney(contributionDetail.getYtdAmount());
    //		}
    //		return model;
    //	}
    //
    //	static public PaycheckOtherDetailsModel otherPaycheckDetailToPaycheckOtherDetailsModel(OtherPaycheckDetail otherDetail) {
    //		PaycheckOtherDetailsModel model = new PaycheckOtherDetailsModel();
    //		if (Helper.isNotEmpty(otherDetail)) {
    //			model.itemId = otherDetail.getItemId();
    //			model.taxAmount = TypeUtil.toMoney(otherDetail.getTaxAmount());
    //			model.type = otherDetail.getType();
    //			model.wageBasis = TypeUtil.toMoney(otherDetail.getWageBasis());
    //		}
    //		return model;
    //	}
    //
    //    static public Long getEmployeeWageItemId(WageItemID wageItemId, Employee employee) {
    //        for (EmployeeWageItem employeeWageItem : employee.getEmployeeWageItems()) {
    //            if (employeeWageItem.getWageItem().getWageItemId() == wageItemId) {
    //                return employeeWageItem.getId();
    //            }
    //        }
    //
    //        return null;
    //    }
    //
    //	static public PaycheckModel paycheckToPaycheckModel(Paycheck paycheck) {
    //        return paycheckToPaycheckModel(paycheck, true);
    //    }
    //
    //	static public PaycheckModel paycheckToPaycheckModel(Paycheck paycheck, boolean populateDetails) {
    //		PaycheckModel model = new PaycheckModel();
    //
    //        populateModelFromKeyedRecord(paycheck, model);
    //	    model.checkDate = paycheck.getCheckDate();
    //	    model.periodStartDate = paycheck.getPeriodStartDate();
    //	    model.periodEndDate = paycheck.getPeriodEndDate();
    //	    model.netCheckAmount = paycheck.getNetCheckAmount();
    //	    model.grossAmount = paycheck.getGrossAmount();
    //	    model.totalHours = paycheck.getTotalHours();
    //	    model.taxesWH = paycheck.getTaxesWithheld();
    //	    model.deductionsWH = paycheck.getDeductionsWithheld();
    //	    model.taxesEmployer = paycheck.getTaxesEmployer();
    //	    model.employerContributions = paycheck.getEmployerContributions();
    //	    model.totalCostFringeBenefits = paycheck.getTotalCostFringeBenefits();
    //	    model.createTimeStamp = paycheck.getCreateTimeStamp();
    //	    model.ddStatus = paycheck.getDDStatus();
    //	    model.ddType = paycheck.getDDType();
    //	    model.isApproved = paycheck.isApproved();
    //	    model.isHistory = paycheck.isHistory();
    //	    model.txType = PaycheckModel.TxType.fromTxTypeString(paycheck.getTxType());
    //	    model.checkAmount = paycheck.getCheckAmount();
    //	    model.ddAmount = paycheck.getDDAmount();
    //	    model.ddAmount2 = paycheck.getDDAmount2();
    //        model.createTimeStamp = paycheck.getCreateTimeStamp();
    //	    model.approvalDate = paycheck.getApprovalDate();
    //        model.employee = new EmployeeModel();
    //        model.employee.setId(paycheck.getEmployee().getId());
    //
    //	    model.externalID = paycheck.getExternalID(false);
    //	    model.memo = paycheck.getMemo();
    //
    //        if (populateDetails) {
    //            List<WageDetail> wageDetails = paycheck.getWageDetails();
    //            model.wageDetails = new PaycheckWagesModel[wageDetails.size()];
    //            for (int ix=0; ix<wageDetails.size(); ix++ ) {
    //                model.wageDetails[ix] = wageDetailToPaycheckWagesModel((WageDetail)wageDetails.get(ix));
    //            }
    //
    //            List<DeductionDetail> deductionDetails = paycheck.getDeductionDetails();
    //            model.deductionDetails = new PaycheckDeductionModel[deductionDetails.size()];
    //            for (int ix=0; ix<deductionDetails.size(); ix++ ) {
    //                model.deductionDetails[ix] = deductionDetailToPaycheckDeductionModel((DeductionDetail)deductionDetails.get(ix));
    //            }
    //
    //            List<TaxDetail> taxDetails = paycheck.getTaxDetails();
    //            model.taxDetails = new PaycheckTaxesModel[taxDetails.size()];
    //            for (int ix=0; ix<taxDetails.size(); ix++ ) {
    //                model.taxDetails[ix] = taxDetailToPaycheckTaxesModel(taxDetails.get(ix));
    //            }
    //
    //            List<ContributionDetail> contributionDetails = paycheck.getContributionDetails();
    //            model.contributionDetails = new PaycheckContributionModel[contributionDetails.size()];
    //            for (int ix=0; ix<contributionDetails.size(); ix++ ) {
    //                model.contributionDetails[ix] = contributionDetailToPaycheckContributionModel((ContributionDetail)contributionDetails.get(ix));
    //            }
    //
    //            List<OtherPaycheckDetail> otherDetails = paycheck.getOtherDetails();
    //            model.otherDetails = new PaycheckOtherDetailsModel[otherDetails.size()];
    //            for (int ix=0; ix<otherDetails.size(); ix++ ) {
    //                model.otherDetails[ix] = otherPaycheckDetailToPaycheckOtherDetailsModel((OtherPaycheckDetail)otherDetails.get(ix));
    //            }
    //        }
    //
    //	    return model;
    //	}
    //
    //        /**
    //         * convert tax payment object to a tax payment model (used in web svcs)
    //         * @param payment
    //         * @return
    //         */
    //	static public TaxPaymentModel taxPaymentToTaxPaymentModel(TaxPayment payment) {
    //            TaxPaymentModel model = new TaxPaymentModel();
    //            populateModelFromKeyedRecord(payment, model);
    //
    //            model.txType = TaxPaymentModel.PaymentTxType.fromTxTypeString(payment.getType());
    //            model.netCheckAmount = payment.getNetCheckAmount();
    //            model.periodEndDate = payment.getPeriodEndDate();
    //            model.periodStartDate = payment.getPeriodStartDate();
    //            model.voucherNumber = payment.getVoucherNumber();
    //
    //            model.agencyDelinquentDate = payment.getAgencyDelinquentDate();
    //            model.delinquentDate = payment.getDelinquentDate();
    //            model.approvalDate = payment.getApprovalDate();
    //            model.checkDate = payment.getCheckDate();
    //            model.createTimeStamp = payment.getCreateTimeStamp();
    //
    //            model.paymentGroupName = payment.getTaxPaymentGroup().getDescription();
    //
    //            model.isApproved = new Boolean(payment.isApproved());
    //            model.isHistory = new Boolean(payment.isHistory());
    //            model.isUserCreated = new Boolean(payment.isUserCreated());
    //
    //            if(model.checkDate == null)
    //            	model.withdrawalDate = DateUtil.getPrevBusinessDay(model.agencyDelinquentDate, payment.getTaxPaymentGroup().getEPayLeadTimeDays());
    //            else
    //            	model.withdrawalDate = DateUtil.getPrevBusinessDay(model.checkDate, payment.getTaxPaymentGroup().getEPayLeadTimeDays());
    //
    //            Schedule sched = payment.getSchedule();
    //            if (null != sched) {
    //                model.scheduleId = new Long(sched.getId());
    //            }
    //
    //            FilingType filing = payment.getFilingType();
    //            if (null != filing) {
    //                model.filingType = CompanyScheduleModel.FilingType.fromLegacyType((int) filing.getId());
    //            }
    //
    //            if(payment.getEPayStatus() != null)
    //            	model.ePayStatus = payment.getEPayStatus().toInt();
    //
    //
    //            // todo - need to do the epay stuff
    //            //     Long epayeeId;
    //            //     Integer ePayStatus;
    //            //     Long achProcessorId;
    //
    //            // details are in 2 collections, but they are all basically the same
    //            //    but different types and constructor sets different values
    //            // TaxAdjustment is a subclass of TaxPaymentDetail
    //            if (payment.getPaymentDetails() != null) {
    //                List<TaxPaymentDetail> detailsList = payment.getPaymentDetails();
    //                TaxPaymentDetailModel[] models = new TaxPaymentDetailModel[detailsList.size()];
    //                model.paymentDetails = models;
    //                int i=0;
    //                for (TaxPaymentDetail d: detailsList) {
    //                    TaxPaymentDetailModel detailModel = new TaxPaymentDetailModel();
    //                    paymentDetailToPaymentDetailModel(d, detailModel);
    //                    models[i++] = detailModel;
    //                }
    //            }
    //            if (payment.getAdjustments() != null) {
    //                List<TaxAdjustment> detailsList = payment.getAdjustments();
    //                TaxAdjustmentModel[] models = new TaxAdjustmentModel[detailsList.size()];
    //                model.taxAdjustments = models;
    //                int i=0;
    //                for (TaxAdjustment d: detailsList) {
    //                    TaxAdjustmentModel detailModel = new TaxAdjustmentModel();
    //                    paymentDetailToPaymentDetailModel(d, detailModel);
    //                    models[i++] = detailModel;
    //                }
    //            }
    //
    //            return model;
    //        }
    //
    //        /**
    //         * put info from the detail into the model
    //         * ridiculous, the name is longer than the method but it's all we need
    //         * @param detail - the payment detail to copy from
    //         * @param model - destination, will be filled with info from the detail
    //         */
    //	static public void paymentDetailToPaymentDetailModel(TaxPaymentDetail detail, TaxPaymentDetailModel model)  {
    //            model.taxAmount = detail.getTaxAmount();
    //            model.taxItemId = detail.getTaxItem().getId();
    //        }
    //
    //	static public Option[] getEnum(Class clazz) {
    //		try {
    //			List<Option> list = new ArrayList<Option>();
    //			for (Field field : clazz.getDeclaredFields()) {
    //				if (field.isEnumConstant()) {
    //					Enum e = (Enum) field.get(null);
    //					list.add(new Option(e.name(), e.toString()));
    //				}
    //			}
    //
    //			return list.toArray(new Option[list.size()]);
    //		} catch (SecurityException e) {
    //		} catch (IllegalArgumentException e) {
    //		} catch (IllegalAccessException e) {
    //		}
    //
    //		return null;
    //	}
    //
    //	static public boolean isBaseType(Class clazz) {
    //		return Number.class.isAssignableFrom(clazz) || Enum.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    //	}
    //
    //	static public <T> T requestToModel(Class clazz, HttpServletRequest request) {
    //		Object object = requestToModel(clazz, request, null, true);
    //		return (T) object;
    //	}
    //
    //	static public <T> T requestToModel(Class clazz, HttpServletRequest request, String name, boolean recurse) {
    //		String escapedName = name == null ? null : BasicInputTag.escapeName(name);
    //		String value = escapedName == null ? null : request.getParameter(escapedName);
    //		try {
    //			T newInstance = null;
    //			if (isBaseType(clazz)) {
    //				if (value != null) {
    //					if (BigDecimal.class.isAssignableFrom(clazz)) {
    //						newInstance = (T) new BigDecimal(value);
    //					} else if (Boolean.class.isAssignableFrom(clazz)) {
    //						newInstance = (T) new Boolean(!value.equalsIgnoreCase("false"));
    //					} else {
    //						newInstance = (T) clazz.getMethod("valueOf", String.class.isAssignableFrom(clazz) ? Object.class : String.class).invoke(null, value);
    //					}
    //				} else if (Boolean.class.isAssignableFrom(clazz) && request.getParameter(escapedName+"_checkbox_present") != null) {
    //					newInstance = (T) new Boolean(false);
    //				}
    //            } else if (clazz.isPrimitive() && clazz.getName().equals("long")) {
    //                if (value != null) {
    //                    newInstance = (T) new Long(value);
    //                }
    //            } else if (clazz.isPrimitive() && clazz.getName().equals("int")) {
    //                if (value != null) {
    //                    newInstance = (T) new Integer(value);
    //                }
    //			} else if (DateModel.class.isAssignableFrom(clazz)) {
    //				if (value != null) {
    //					DateModel newDate = toDateModel(DateUtil.parseDate(value));
    //					newInstance = (T) newDate;
    //				}
    //			} else {
    //				if (Object[].class.isAssignableFrom(clazz)) {
    //					Map<Integer,T> objects = new HashMap<Integer,T>();
    //					for (Object parameter : request.getParameterMap().keySet()) {
    //						String parameterString = (String) parameter;
    //						if (parameterString.startsWith(escapedName+BasicInputTag.SQUARE_BRACKET_OPEN)) {
    //							int index = Integer.parseInt(parameterString.substring(parameterString.indexOf(BasicInputTag.SQUARE_BRACKET_OPEN,escapedName.length())+BasicInputTag.SQUARE_BRACKET_OPEN.length(),parameterString.indexOf(BasicInputTag.SQUARE_BRACKET_CLOSE,escapedName.length())));
    //							if (!objects.containsKey(index)) {
    //								objects.put(index, (T) requestToModel(clazz.getComponentType(), request, name+"["+index+"]", true));
    //							}
    //						}
    //					}
    //					if (objects.size() > 0) {
    //						Object[] array = (Object[]) Array.newInstance(clazz.getComponentType(), objects.size());
    //						for (int index = 0; index < objects.size(); index++) {
    //							array[index] = objects.get(index);
    //						}
    //						newInstance = (T) array;
    //					} else {
    //						newInstance = null;
    //					}
    //				} else {
    //					newInstance = (T) clazz.newInstance();
    //					boolean foundNonNullField = false;
    //					for (FieldOrMethod field : FieldOrMethod.getAllFields(clazz)) {
    //						if (recurse || field.getType() != clazz) {
    //							Object fieldValue = requestToModel(field.getType(), request, (name == null ? "" : (name+"."))+field.getName(), field.getType() != clazz);
    //							if (fieldValue != null) {
    //								field.set(newInstance, fieldValue);
    //								foundNonNullField = true;
    //							}
    //						}
    //					}
    //					if (!foundNonNullField && escapedName != null && value != null && BaseModel.class.isAssignableFrom(clazz)) {
    //						newInstance = (T) clazz.newInstance();
    //						((BaseModel) newInstance).setId(Long.parseLong(value));
    //					} else {
    //						newInstance = foundNonNullField ? newInstance : null;
    //					}
    //				}
    //			}
    //			return newInstance;
    //		} catch (NumberFormatException e) {
    //		} catch (IllegalArgumentException e) {
    //		} catch (SecurityException e) {
    //		} catch (InvocationTargetException e) {
    //		} catch (NoSuchMethodException e) {
    //		} catch (InstantiationException e) {
    //		} catch (IllegalAccessException e) {
    //		} catch (ParseException e) {
    //		}
    //
    //		return null;
    //	}
    //
    //	public static List<LocalTaxItemModel> getLocalTaxes(Company company, Employee employee, EmployeeModel employeeModel) {
    //		List<LocalTaxItemModel> returnValue = new Vector<LocalTaxItemModel>();
    //		employeeModel.isLocalTaxAdditionalAmountAbsolute = false;
    //		if (employee.getAddress() == null || employee.getWorkLocation() == null) {
    //			return returnValue;
    //		}
    //
    //	    boolean isBasicCommuter = employee.commutesToAnotherState() &&  !(company.hasFeature(Feature.FullMultipleState));
    //		List<Map<String, Object>> localTaxMaps = EmployeeTx.getLocalTaxes(company, false, isBasicCommuter, employee);
    //		for (Map<String, Object> localTaxMap : localTaxMaps) {
    //			LocalTaxItemModel model = new LocalTaxItemModel();
    //			model.taxItemId = (Long) localTaxMap.get(EmployeeTx.ID_KEY);
    //			model.isActive = (Boolean) localTaxMap.get(EmployeeTx.VALUE_KEY);
    //			model.name = (String) localTaxMap.get(EmployeeTx.NAME_KEY);
    //			TaxItem item = BasicItemMgr.<TaxItem>getItem(TaxItem.class, model.taxItemId);
    //			model.taxType = TaxType.localTaxTypeToLocalTaxTypeModel((String) localTaxMap.get(EmployeeTx.PREFIX_KEY));
    //			model.isNotApplicable = false;
    //
    //			if (item.getId() == TaxItem.NY_YONKERS_LOCAL_ITEMID || item.getId() == TaxItem.NY_NYC_LOCAL_ITEMID) {
    //                // For NY state, if filing status is DNW, don't allow user to select NY local tax
    //                boolean useResTaxSetup = employeeModel.getHomeAddress().getState().equals("NY");
    //                if (!useResTaxSetup && employeeModel.workLocation != null) // employeeModel.workLocation might be null
    //                    useResTaxSetup = !employeeModel.workLocation.getState().equals("NY");
    //                EmployeeTaxSetupModel taxSetupModel = useResTaxSetup ? employeeModel.residenceTaxSetup : employeeModel.workTaxSetup;
    //                model.isNotApplicable = taxSetupModel != null && taxSetupModel.filingStatus == EmployeeTaxSetup.FSTAT_DONOTWITHHOLD;
    //                employeeModel.isLocalTaxAdditionalAmountAbsolute = model.isNotApplicable;
    //
    //                if (model.taxType == TaxType.Residence || model.taxType == TaxType.ResidenceAndWork) {
    //					employeeModel.requiresResidentLocalTaxAdditionalAmount = true;
    //					employeeModel.labelForResidentLocalTaxAdditionalAmount = item.getName();
    //				} else if (model.taxType == TaxType.Work) {
    //					employeeModel.requiresWorkLocalTaxAdditionalAmount = true;
    //					employeeModel.labelForWorkLocalTaxAdditionalAmount = item.getName();
    //				}
    //			}
    //			returnValue.add(model);
    //		}
    //		return returnValue;
    //	}
    //
    //	static public DeductionItemModel deductionItemToDeductionItemModel(DeductionItem dedItem) {
    //		if (dedItem == null) {
    //			return null;
    //		}
    //
    //		Date effectiveDate = DateUtil.today();
    //		DeductionItemModel model = new DeductionItemModel();
    //		model.isEditMaxAmount = dedItem.isEditGlobalMaxAmount(effectiveDate);
    //		model.isMaxAmountOptional = dedItem.isGlobalMaxOptional(effectiveDate);
    //		model.maxAmount = dedItem.getGlobalMaxAmount(effectiveDate);
    //		model.isEditSetup = dedItem.editEmployeeSetup();
    //
    //		return model;
    //	}
    //
    //	static public EmployeeWorkersCompensationClassModel employeeWCClassToEmployeeWorkersCompensationClassModel(EmployeeWCClass employeeWCClass) {
    //		if (employeeWCClass == null) {
    //			return null;
    //		}
    //
    //		EmployeeWorkersCompensationClassModel model = new EmployeeWorkersCompensationClassModel();
    //		model.companyWorkersCompensationClassModel = companyWCClassToCompanyWorkersCompensationClassModel(employeeWCClass.getCompanyWCClass());
    //		model.effectiveDate = toDateModel(employeeWCClass.getEffectiveDate());
    //
    //		return model;
    //	}
    //
    //	static public CompanyWorkersCompensationClassModel companyWCClassToCompanyWorkersCompensationClassModel(CompanyWCClass companyWCClass) {
    //		if (companyWCClass == null) {
    //			return null;
    //		}
    //
    //		CompanyWorkersCompensationClassModel model = new CompanyWorkersCompensationClassModel();
    //        populateModelFromKeyedRecord(companyWCClass, model);
    //		model.classCode = companyWCClass.getClassCode();
    //		model.subClassCode = companyWCClass.getSubClassCode();
    //		model.natureOfWork = companyWCClass.getNatureOfWork();
    //		model.active = companyWCClass.getActive()==1 ? Boolean.TRUE : Boolean.FALSE;
    //
    //		List<CompanyWorkersCompensationRateModel> companyWorkersCompensationRateModels = new ArrayList<CompanyWorkersCompensationRateModel>();
    //		for (CompanyWCRate companyWCRate: companyWCClass.getCompanyWCRates()) {
    //			companyWorkersCompensationRateModels.add(companyWCRateToCompanyWorkersCompensationRateModel(companyWCRate));
    //		}
    //		model.companyWorkersCompensationRateModels = companyWorkersCompensationRateModels.toArray(new CompanyWorkersCompensationRateModel[0]);
    //
    //		return model;
    //	}
    //	static public CompanyWorkersCompensationRateModel companyWCRateToCompanyWorkersCompensationRateModel(CompanyWCRate companyWCRate) {
    //		if (companyWCRate == null) {
    //			return null;
    //		}
    //
    //		CompanyWorkersCompensationRateModel model = new CompanyWorkersCompensationRateModel();
    //		model.effectiveDate = toDateModel(companyWCRate.getEffectiveDate());
    //		model.endDate = toDateModel(companyWCRate.getEndDate());
    //		model.compositeRate = companyWCRate.getCompositeRate();
    //		model.payrollDeduction = companyWCRate.getPayrollDeduction();
    //
    //		return model;
    //	}
    //
    //	static public void initializeUser(User user, UserModel model, Transaction tx, Company company, boolean secure) {
    //		if (model != null) {
    //			// Save contractor SSN (from ContractorTx().updateSocialSecurityNumber(parameters))
    //			if (user.getUserType() == UserType.CONTRACTOR) {
    //		        Contractor conCopy = company.getContractorByContact(user.getId());
    //		        String plainTextSSN = model.socialSecurityNumber;
    //		        String savedSSN = conCopy.getClearSSN();
    //		        // if sensitized value edited i.e if only last four digits changed throw error.
    //		        if(StringUtil.isSensitized(plainTextSSN) && (Helper.isEmpty(savedSSN))) {
    //					throw new UserException("Please enter the Social Security Number as xxx-xx-xxxx.");
    //				}
    //		        // Change any spaces to dashes to enforce uniform format
    //		        if(!StringUtil.isSensitized(plainTextSSN)) {
    //					plainTextSSN = plainTextSSN.replace(' ', '-');
    //				}
    //		        plainTextSSN = Validator.processSSN(plainTextSSN);
    //		        Validator.validateSSN(plainTextSSN);
    //		        conCopy.setTIN(plainTextSSN);
    //		        conCopy.sensitize();
    //			}
    //		}
    //	}
    //
    //
    //	static public void initializePerson(Person person, PersonModel model, Transaction tx, Company company, boolean secure) {
    //		if (model != null) {
    //			SetupWebServiceHelper.updateSimpleField(person, "setFirstName", model.getFirstName());
    //			SetupWebServiceHelper.updateSimpleField(person, "setLastName", model.getLastName());
    //			SetupWebServiceHelper.updateSimpleField(person, "setMiddleInitial", model.getMiddleInitial());
    //
    //			if (model.getHomeAddress()!= null && (model.getHomeAddress().getState() != null)) {
    //				Address homeAddress = person.getHomeAddress();
    //				homeAddress = homeAddress == null ? tx.<Address>registerNewObject(new Address()) : tx.<Address>registerExitingObject(homeAddress);
    //				ModelHelper.initializeAddress(homeAddress, model.getHomeAddress(), secure);
    //				person.setHomeAddress(homeAddress);
    //			}
    //
    //			if (model.getHomeAddress() != null && model.getHomeAddress().getTaxAddress() != null && model.getHomeAddress().getUpdateTaxAddress()!= null && model.getHomeAddress().getUpdateTaxAddress()) {
    //				Address taxAddress = person.getHomeAddress().getRawTaxAddress();
    //                                if (taxAddress == null)
    //                                    model.getHomeAddress().setState(person.getHomeAddress().getState());
    //                                taxAddress = taxAddress == null ? tx.<Address>registerNewObject(new Address()) : tx.<Address>registerExitingObject(taxAddress);
    //				model.getHomeAddress().getTaxAddress().setAddress1(taxAddress.getAddress1() != null ? taxAddress.getAddress1() : "AddressOne");
    //				model.getHomeAddress().getTaxAddress().setAddress2(taxAddress.getAddress2() != null ? taxAddress.getAddress2() : "AddressTwo");
    //				model.getHomeAddress().getTaxAddress().setState(taxAddress.getState() != null ? taxAddress.getState() : model.getHomeAddress().getState());
    //				ModelHelper.initializeAddress(taxAddress, model.getHomeAddress().getTaxAddress(), secure);
    //				person.getHomeAddress().setTaxAddress(taxAddress);
    //			}
    //
    //			SetupWebServiceHelper.updateSimpleField(person, "setJobTitle", model.getJobTitle());
    //
    //			if(Helper.isNotEmpty(model.getRace())){
    //				person.setRace(model.getRace());
    //			}
    //
    //			//How do we delete emergency contacts??
    //			EmergencyContactModel[] emergencyContacts = model.getEmergencyContacts();
    //			if(emergencyContacts != null){
    //				if(emergencyContacts.length >=1 && emergencyContacts[0] != null){
    //					if(emergencyContacts[0].delete || (Helper.isEmpty(emergencyContacts[0].firstName) && Helper.isEmpty(emergencyContacts[0].lastName))){
    //						//Delete ec.
    //						person.setEmergencyContact1(null);
    //					}else{
    //						//Initialize and update.
    //						Person emergencyContact = person.getEmergencyContact1();
    //						emergencyContact = emergencyContact == null ? tx.<Person>registerNewObject(new Person()) : tx.<Person>registerExitingObject(emergencyContact);
    //						ModelHelper.initializeEmergencyContact(emergencyContact, emergencyContacts[0]);
    //						person.setEmergencyContact1(emergencyContact);
    //					}
    //				}
    //
    //				if(emergencyContacts.length >=2 && emergencyContacts[1] != null){
    //					if(emergencyContacts[1].delete || (Helper.isEmpty(emergencyContacts[1].firstName) && Helper.isEmpty(emergencyContacts[1].lastName))){
    //						//Delete ec.
    //						person.setEmergencyContact2(null);
    //					}else{
    //						Person emergencyContact = person.getEmergencyContact2();
    //						emergencyContact = emergencyContact == null ? tx.<Person>registerNewObject(new Person()) : tx.<Person>registerExitingObject(emergencyContact);
    //						ModelHelper.initializeEmergencyContact(emergencyContact, emergencyContacts[1]);
    //						person.setEmergencyContact2(emergencyContact);
    //					}
    //				}
    //			}
    //
    //
    //			//Update phone numbers, emails, ims, custom fields.
    //			if(model.getPhones() != null){
    //				List<Phone> phones = new ArrayList<Phone>(person.getPhones());
    //				List<Phone> addedPhones = new ArrayList<Phone>();
    //				for(Phone phone : model.getPhones()){
    //					if(phone != null && phone.getDelete()!= null && phone.getDelete()){
    //						deletePhone(phones, phone.getId());
    //					}else if (phone != null && phone.getId() > 0) {
    //						//update phone. May delete the phone, if this new phone number is blank.
    //						updatePhone(phones, phone);
    //					}else if (phone != null && phone.getId() <= 0 && Helper.isNotEmpty(phone.getPhoneNumber())) {
    //						//add phone only if new number is not blank.
    //						//in case of new phones, go through existing phones and just update it if that type phone is found.
    //						//if not, add this new phone number.
    //						boolean found = false;
    //						for(Phone existingPhone : phones){
    //							if(existingPhone.getPhoneType() == phone.getPhoneType()){
    //								found = true;
    //								existingPhone.setPhoneNumber(phone.getPhoneNumber());
    //								break;
    //							}
    //						}
    //						if(!found){
    //							phone.setPerson(person);
    //							addedPhones.add(phone);
    //						}
    //					}
    //				}
    //				phones.addAll(addedPhones);
    //				validatePhones(phones);
    //				person.setPhones(phones);
    //			}
    //
    //
    //
    //			//Emails
    //			if(model.getEmails() != null){
    //				List<EmailAddr> emails = new ArrayList<EmailAddr>(person.getEmailAddresses());
    //				List<EmailAddr> addedEmails = new ArrayList<EmailAddr>();
    //				for(EmailAddr emailAddress : model.getEmails()){
    //					if(emailAddress != null && emailAddress.getDelete()!= null && emailAddress.getDelete()){
    //						deleteEmail(emails, emailAddress.getId());
    //					}else if (emailAddress != null && emailAddress.getId() > 0) {
    //						//update emailaddress. May delete the email, if this new email address is blank.
    //						updateEmail(emails, emailAddress);
    //					}else if (emailAddress != null && emailAddress.getId() <=  0 && Helper.isNotEmpty(emailAddress.getEmail())) {
    //						//add email only if the new email is not blank.
    //						//in case of new emails go through existing emails and just update it if that type email address is found.
    //						//if not, add this new email address.
    //						boolean found = false;
    //						for(EmailAddr emailAddr : emails){
    //							if(emailAddr.getEmailType() == emailAddress.getEmailType()){
    //								found = true;
    //								emailAddr.setEmail(emailAddress.getEmail());
    //								break;
    //							}
    //						}
    //						if(!found){
    //							emailAddress.setPerson(person);
    //							addedEmails.add(emailAddress);
    //						}
    //					}
    //				}
    //				emails.addAll(addedEmails);
    //				validateEmails(emails);
    //				person.setEmailAddresses(emails);
    //			}
    //
    //			//IMs
    //			if(model.getIms() != null){
    //				List<IM> ims = new ArrayList<IM>(person.getIMs());
    //				List<IM> addedIMs = new ArrayList<IM>();
    //				for(IM im : model.getIms()){
    //					if(im != null && im.getDelete()!= null && im.getDelete()){
    //						deleteIM(ims, im.getId());
    //					}else if (im != null && im.getId() > 0) {
    //						updateIM(ims, im);
    //					}else if (im != null && im.getId() <=  0 && Helper.isNotEmpty(im.getIM())) {
    //						//add IM, only if new im is not blank.
    //						//in case of new ims, go through existing ims and just update it if that type im is found.
    //						//if not, add this new im.
    //						boolean found = false;
    //						for(IM existingIm: ims){
    //							if(existingIm.getIMType() == im.getIMType()){
    //								found = true;
    //								existingIm.setIM(im.getIM());
    //								break;
    //							}
    //						}
    //						if(!found){
    //							im.setPerson(person);
    //							addedIMs.add(im);
    //						}
    //					}
    //				}
    //				ims.addAll(addedIMs);
    //				validateIMs(ims);
    //				person.setIMs(ims);
    //			}
    //
    //			if(model.getPersonCustomFields() != null){
    //				for(CustomField field : model.getPersonCustomFields()){
    //					if(field != null && Helper.isNotEmpty(field.getLabel()) ){
    //						CustomFieldLabel label = company.getCustomFieldLabel(field.getLabel().getId());
    //						if(label != null){
    //							if(Helper.isNotEmpty(field.getValue())){
    //								person.setCustomField(label, field.getValue());
    //							}else{
    //								//remove custom field.
    //								person.removeCustomField(label.getId());
    //							}
    //						}
    //					}
    //				}
    //			}
    //
    //			//After all this. just overwrite work email address.
    //			if(Helper.isNotEmpty(model.getEmailAddress())){
    //				person.setEmailAddress(model.getEmailAddress());
    //			}
    //		}
    //	}
    //
    //	private static void validateIMs(List<IM> ims) {
    //		//Make sure no two ims have same type set.
    ////		HashMap<IMType,Integer> typeCountMap = new HashMap<IMType, Integer>();
    //		for(IM im : ims){
    //			IMType imtType = im.getIMType();
    //			if(imtType == null){
    //				throw new UserException("Invalid IM Type for IM '" + im.getIM() + "'");
    //			}
    ////			Integer typeCount = typeCountMap.get(imtType);
    ////			if(typeCount == null){
    ////				typeCountMap.put(imtType, 1);
    ////			}else{
    ////				typeCountMap.put(imtType, typeCount+1);
    ////			}
    //		}
    ////		for(IMType imType : typeCountMap.keySet()){
    ////			Integer typeCount = typeCountMap.get(imType);
    ////			if(typeCount != null && typeCount > 1){
    ////				throw new UserException("You have more than 1 IM of type '" + imType + "'");
    ////			}
    ////		}
    //	}
    //
    //	private static void validateEmails(List<EmailAddr> emails) {
    //		//Make sure no two emails have same type set.
    //		HashMap<EmailType,Integer> typeCountMap = new HashMap<EmailType, Integer>();
    //		for(EmailAddr email : emails){
    //			String emailAddress = email.getEmail();
    //			if(!Validator.validateEmail(emailAddress)){
    //				throw new UserException("Email Address '" + emailAddress + "' is not in valid format");
    //			}
    //			EmailType emailType = email.getEmailType();
    //			if(emailType == null){
    //				throw new UserException("Invalid Email Type for email '" + emailAddress + "'");
    //			}
    //
    //			Integer typeCount = typeCountMap.get(emailType);
    //			if(typeCount == null){
    //				typeCountMap.put(emailType, 1);
    //			}else{
    //				typeCountMap.put(emailType, typeCount+1);
    //			}
    //		}
    //
    //		for(EmailType emailType : typeCountMap.keySet()){
    //			Integer typeCount = typeCountMap.get(emailType);
    //			if(typeCount != null && typeCount > 1){
    //				throw new UserException("You have more than 1 email address of type '" + emailType + "'");
    //			}
    //		}
    //	}
    //
    //	private static void validatePhones(List<Phone> phones) {
    //		//Make sure no two phones have same type set.
    //		HashMap<PhoneType,Integer> typeCountMap = new HashMap<PhoneType, Integer>();
    //		for(Phone phone : phones){
    //			String phoneNumber = phone.getPhoneNumber();
    //			Validator.validatePhoneNumber(phoneNumber);
    //			PhoneType phoneType = phone.getPhoneType();
    //			if(phoneType == null){
    //				throw new UserException("Invalid Phone Type for phone '" + phoneNumber + "'");
    //			}
    //			Integer typeCount = typeCountMap.get(phoneType);
    //			if(typeCount == null){
    //				typeCountMap.put(phoneType, 1);
    //			}else{
    //				typeCountMap.put(phoneType, typeCount+1);
    //			}
    //		}
    //
    //		for(PhoneType phoneType : typeCountMap.keySet()){
    //			Integer typeCount = typeCountMap.get(phoneType);
    //			if(typeCount != null && typeCount > 1){
    //				throw new UserException("You have more than 1 phone of type '" + phoneType + "'");
    //			}
    //		}
    //	}
    //
    //
    //	static public void initializeEmergencyContact(Person person, EmergencyContactModel model) {
    //		if (model != null) {
    //
    //			SetupWebServiceHelper.updateSimpleField(person, "setFirstName", model.getFirstName());
    //			SetupWebServiceHelper.updateSimpleField(person, "setLastName", model.getLastName());
    //			SetupWebServiceHelper.updateSimpleField(person, "setMiddleInitial", model.getMiddleInitial());
    //
    //			//Update phone numbers
    //			if(model.getPhones() != null){
    //				List<Phone> origPhones = new ArrayList<Phone>(person.getPhones());
    //				List<Phone> addedPhones = new ArrayList<Phone>();
    //				for(Phone phone : model.getPhones()){
    //					if(phone != null && phone.getDelete()!= null && phone.getDelete()){
    //						deletePhone(origPhones, phone.getId());
    //					}else if (phone != null && phone.getId() > 0) {
    //						//update phone. May delete the phone, if this new phone number is blank.
    //						updatePhone(origPhones, phone);
    //					}else if (phone != null && phone.getId() <= 0 && Helper.isNotEmpty(phone.getPhoneNumber())) {
    //						//add phone only if new number is not blank.
    //						phone.setPerson(person);
    //						addedPhones.add(phone);
    //					}
    //				}
    //				origPhones.addAll(addedPhones);
    //				person.setPhones(origPhones);
    //			}
    //
    //			//Update emails
    //			if(model.getEmails() != null){
    //				List<EmailAddr> origEmails = new ArrayList<EmailAddr>(person.getEmailAddresses());
    //				List<EmailAddr> addedEmails = new ArrayList<EmailAddr>();
    //				for(EmailAddr emailAddress : model.getEmails()){
    //					if(emailAddress != null && emailAddress.getDelete()!= null && emailAddress.getDelete()){
    //						deleteEmail(origEmails, emailAddress.getId());
    //					}else if (emailAddress != null && emailAddress.getId() > 0) {
    //						//update emailaddress. May delete the email, if this new email address is blank.
    //						updateEmail(origEmails, emailAddress);
    //					}else if (emailAddress != null && emailAddress.getId() <=  0 && Helper.isNotEmpty(emailAddress.getEmail())) {
    //						//add email only if the new email is not blank.
    //						emailAddress.setPerson(person);
    //						addedEmails.add(emailAddress);
    //					}
    //				}
    //				origEmails.addAll(addedEmails);
    //				person.setEmailAddresses(origEmails);
    //			}
    //		}
    //	}
    //
    //	private static void deletePhone(List<Phone> phones, long id){
    //		for (Iterator iterator = phones.iterator(); iterator.hasNext();) {
    //			Phone phone = (Phone) iterator.next();
    //			if(phone.getId() == id){
    //				phones.remove(phone);
    //				break;
    //			}
    //		}
    //	}
    //
    //	private static void updatePhone(List<Phone> phones, Phone newPhone){
    //		for (Iterator iterator = phones.iterator(); iterator.hasNext();) {
    //			Phone phone = (Phone) iterator.next();
    //			if(phone.getId() == newPhone.getId()){
    //				if(Helper.isEmpty(newPhone.getPhoneNumber())){
    //					phones.remove(phone);
    //				}else{
    //					phone.setPhoneNumber(newPhone.getPhoneNumber());
    //					phone.setExtension(newPhone.getExtension());
    //					phone.setPhoneType(newPhone.getPhoneType());
    //					phone.setDisplayOrder(newPhone.getDisplayOrder());
    //				}
    //				break;
    //			}
    //		}
    //	}
    //
    //	private static void deleteEmail(List<EmailAddr> emails, long id){
    //		for (Iterator iterator = emails.iterator(); iterator.hasNext();) {
    //			EmailAddr emailAddress = (EmailAddr) iterator.next();
    //			if(emailAddress.getId() == id){
    //				emails.remove(emailAddress);
    //				break;
    //			}
    //		}
    //	}
    //
    //	private static void updateEmail(List<EmailAddr> emails, EmailAddr newEmail){
    //		for (Iterator iterator = emails.iterator(); iterator.hasNext();) {
    //			EmailAddr emailAddress = (EmailAddr) iterator.next();
    //			if(emailAddress.getId() == newEmail.getId()){
    //				if(Helper.isEmpty(newEmail.getEmail())){
    //					emails.remove(emailAddress);
    //				}else{
    //					emailAddress.setEmail(newEmail.getEmail());
    //					emailAddress.setEmailType(newEmail.getEmailType());
    //					emailAddress.setDisplayOrder(newEmail.getDisplayOrder());
    //				}
    //				break;
    //			}
    //		}
    //	}
    //
    //	private static void deleteIM(List<IM> ims, long id){
    //		for (Iterator iterator = ims.iterator(); iterator.hasNext();) {
    //			IM im = (IM) iterator.next();
    //			if(im.getId() == id){
    //				ims.remove(im);
    //				break;
    //			}
    //		}
    //	}
    //
    //	private static void updateIM(List<IM> ims, IM newIM){
    //		for (Iterator iterator = ims.iterator(); iterator.hasNext();) {
    //			IM im = (IM) iterator.next();
    //			if(im.getId() == newIM.getId()){
    //				if(Helper.isEmpty(newIM.getIM())){
    //					ims.remove(im);
    //				}else{
    //					im.setIM(newIM.getIM());
    //					im.setIMType(newIM.getIMType());
    //					im.setDisplayOrder(newIM.getDisplayOrder());
    //				}
    //				break;
    //			}
    //		}
    //	}
    //
    //	public static boolean isHRISActive(EmploymentStatus empStatus){
    //		return empStatus != null && (empStatus == EmploymentStatus.Active || empStatus == EmploymentStatus.PaidLeave|| empStatus == EmploymentStatus.UnpaidLeave);
    //	}
    //
    //	public static boolean isPayActive(EmploymentStatus empStatus){
    //		return empStatus != null && (empStatus == EmploymentStatus.Active || empStatus == EmploymentStatus.PaidLeave);
    //	}
    //
    //        public static boolean IsUpdateLocalTaxWithDifferentGeocode(Address address, AddressModel model)
    //        {
    //            // get address geocode:
    //            String addressGeocode = "";
    //            if (address != null) {
    //                try {
    //                    addressGeocode = address.validate();
    //                }
    //                catch (Exception e)
    //                {}
    //            }
    //            // get model geocode:
    //            String modelGeocode = "";
    //            if (model != null) {
    //                Address modelAddress = new Address( address.getAddress1() == null ? "" : address.getAddress1(),
    //                                                    address.getAddress2() == null ? "" : address.getAddress2(),
    //                                                    model.getCity() == null ? "" : model.getCity(),
    //                                                    model.getCounty() == null ? "" : model.getCounty(),
    //                                                    address.getState(),
    //                                                    model.getZip() == null ? "" : model.getZip());
    //                try {
    //                    modelGeocode = modelAddress.validate();
    //                }
    //                catch (Exception e)
    //                {}
    //            }
    //
    //            if (!modelGeocode.equals(addressGeocode))
    //                return true;
    //
    //            return false;
    //        }
    //
    //        public static boolean isSavingLocalTaxRequired(Address address, AddressModel model)
    //        {
    //           // get address geocode:
    //            String addressGeocode = "";
    //            if (address != null) {
    //                try {
    //                    addressGeocode = address.validate();
    //                }
    //                catch (Exception e)
    //                {}
    //            }
    //            // get model geocode:
    //            String modelGeocode = "";
    //            if (model != null) {
    //                Address modelAddress = new Address( model.getAddress1() == null ? "" : model.getAddress1(),
    //                                                    model.getAddress2() == null ? "" : model.getAddress2(),
    //                                                    model.getCity() == null ? "" : model.getCity(),
    //                                                    model.getCounty() == null ? "" : model.getCounty(),
    //                                                    model.getState() == null ? "" : model.getState(),
    //                                                    model.getZip() == null ? "" : model.getZip());
    //                try {
    //                    modelGeocode = modelAddress.validate();
    //                }
    //                catch (Exception e)
    //                {}
    //            }
    //            if (address != null && !modelGeocode.equals(addressGeocode)) {
    //                Vector addressLocalTaxes = null, modelLocalTaxes = null;
    //                if (!addressGeocode.equals(""))
    //                    addressLocalTaxes = ItemMgr.getTaxItems(addressGeocode, ItemMgr.COUNTYLOCALLEVEL);
    //                if (!modelGeocode.equals(""))
    //                    modelLocalTaxes = ItemMgr.getTaxItems(modelGeocode, ItemMgr.COUNTYLOCALLEVEL);
    //                if ((addressLocalTaxes != null && addressLocalTaxes.size() > 0) || (modelLocalTaxes != null && modelLocalTaxes.size() > 0))
    //                    return true;
    //            }
    //
    //            return false;
    //        }
    //
    //        public static boolean isSavingLocalTaxRequired(Address address, Address newAddress)
    //        {
    //            // get current address geocode:
    //            String addressGeocode = "";
    //            if (address != null) {
    //                try {
    //                    addressGeocode = address.validate();
    //                }
    //                catch (Exception e)
    //                {}
    //            }
    //            // get new address geocode:
    //            String newGeocode = "";
    //            if (newAddress != null) {
    //                try {
    //                    newGeocode = newAddress.validate();
    //                }
    //                catch (Exception e)
    //                {}
    //            }
    //
    //            if (address != null && !newGeocode.equals(addressGeocode)) {
    //                Vector addressLocalTaxes = null, newLocalTaxes = null;
    //                if (!addressGeocode.equals(""))
    //                    addressLocalTaxes = ItemMgr.getTaxItems(addressGeocode, ItemMgr.COUNTYLOCALLEVEL);
    //                if (!newGeocode.equals(""))
    //                    newLocalTaxes = ItemMgr.getTaxItems(newGeocode, ItemMgr.COUNTYLOCALLEVEL);
    //                if ((addressLocalTaxes != null && addressLocalTaxes.size() > 0) || (newLocalTaxes != null && newLocalTaxes.size() > 0))
    //                    return true;
    //            }
    //
    //            return false;
    //        }
    //
    //    public static void populateModelFromKeyedRecord( KeyedRecord keyedRecord, BaseModel baseModel ) {
    //        if( baseModel == null || keyedRecord == null ) {
    //            return;
    //        }
    //        baseModel.setId(keyedRecord.getId());
    //        baseModel.setVersion(keyedRecord.getVersion());
    //        baseModel.setExternalPartnerDataId(keyedRecord.getExternalPartnerDataId());
    //    }
    //
    //    // used in paychecks and payments, in case we have the pay item id but not the tax item id
    //    // so if we are creating a deduction for the local tax, we need to use the name for the description
    //    // throws exception if not found or no mapping
    //    public static String findClassicLocalTaxName(Long classicCompanyId, Long classicPayrollItemId) {
    //        String sql = "select ClassicTaxName from ClassicLocalAndCustomTaxMap where ClassicCompanyId=#P1 and ClassicPayrollItemId=#P2" ;
    //        List<Map<String, Object>> results = Helper.executeDataReadQuery(sql,classicCompanyId,classicPayrollItemId);
    //        if (results.isEmpty()) {
    //           throw new UserException("No mapping for payroll item ID " + classicPayrollItemId.toString());
    //        }
    //        Map<String, Object> row = results.get(0);   // there could only be one result
    //        return (String) row.get("ClassicTaxName");
    //    }
    //
    //    // just a class to allow us to return all the local tax info for txns
    //    public static class ClassicLocalTaxSplit {
    //        public Integer taxItemId1;
    //        public Integer taxItemId2;
    //        // if taxitemid2 is not null, use these 2 rates to split amounts in existing txns
    //        // such as if rate1 is 1.5 and rate2 is .5 then amounts will be split 75% and 25%
    //        public BigDecimal taxRate1;
    //        public BigDecimal taxRate2;
    //        public BigDecimal amount1;
    //        public BigDecimal amount2;
    //        public String classicTaxName;
    //        public Long classicPayrollItemId;
    //        public String classicTaxRate;
    //        public String classicRateType;
    //        public boolean isCompanyPaid;
    //
    //        public ClassicLocalTaxSplit(Integer id1, Integer id2, BigDecimal rate1, BigDecimal rate2,
    //                        Long payrollItemId, String classicName, String classicRate, String rateType,
    //                        boolean companyPaid) {
    //            taxItemId1 = id1;
    //            taxItemId2 = id2;
    //            taxRate1 = rate1;
    //            taxRate2 = rate2;
    //            amount1 = null;
    //            amount2 = null;
    //            classicTaxName = classicName;
    //            classicPayrollItemId = payrollItemId;
    //            classicTaxRate = classicRate;
    //            classicRateType = rateType;
    //            isCompanyPaid = companyPaid;  // not exactly right but this is true for company paid custom local
    //        }
    //        public void splitTaxAmount(BigDecimal totalAmount )
    //        {
    //            // here is where we try to split this amount for the 2 tax items
    //            if (taxItemId2 == null) {
    //                amount1 = totalAmount;
    //                amount2 = null;
    //            } else {
    //                double total = this.taxRate1.add(taxRate2).doubleValue();
    //                // what percent of the amounts should be on tax 2 (remainder to tax 1)
    //                double dblTaxRate2 = this.taxRate2.doubleValue();
    //                double percentOther = 100.0*dblTaxRate2/total;
    //                double roundAmount = 0.5;
    //                if (totalAmount.signum() < 0) {
    //                    roundAmount = -0.5;
    //                }
    //                amount2 = new BigDecimal((percentOther*totalAmount.doubleValue())+roundAmount);
    //                // just make sure we have dollars and cents, no partial pennies.
    //                amount2 = new BigDecimal(amount2.intValue()).movePointLeft(2);
    //                amount1 = totalAmount.subtract(amount2);
    //            }
    //        }
    //
    //    }
    //
    //    // used in paychecks and payments, in case we have the pay item id but not the tax item id
    //    // so we do the mapping here.
    //    // throws exception if not found or no mapping
    //    // returns obj with tax item id null, if it should become a deduction/contribution
    //    public static ClassicLocalTaxSplit findClassicLocalTax(Long classicCompanyId, Long classicPayrollItemId) {
    //        String sql = "select TaxItemId, TaxItemId2, TaxRate1, TaxRate2, ClassicTaxName, ClassicRate, ClassicRateType, mappedStatus, ClassicDetailType from ClassicLocalAndCustomTaxMap where ClassicCompanyId=#P1 and ClassicPayrollItemId=#P2" ;
    //        List<Map<String, Object>> results = Helper.executeDataReadQuery(sql,classicCompanyId,classicPayrollItemId);
    //        if (results.isEmpty()) {
    //           throw new UserException("No mapping for payroll item ID " + classicPayrollItemId.toString());
    //        }
    //        Map<String, Object> row = results.get(0);   // there could only be one result
    //        Integer taxID = row.get("TaxItemId") == null ? null :(Integer) row.get("TaxItemId");
    //        String status = (String) row.get("mappedStatus");
    //        boolean isCompanyPaid = false;
    //        int detailType = ((Integer) row.get("ClassicDetailType")).intValue();
    //        if ((detailType == 1000017) || (detailType == 197)) {
    //            isCompanyPaid = true; // this is company paid tax, make company contribution
    //        }
    //        String classicName = (String) row.get("ClassicTaxName");
    //        if (taxID == null) {
    //            // special case for mapping to deduction is status of n/a
    //            if ((null != status) && status.equalsIgnoreCase("n/a")) {
    //                // return obj with null for the tax ID but setting correct for company paid
    //                return new ClassicLocalTaxSplit(null, null, null, null,
    //                        classicPayrollItemId, classicName, null, null,
    //                        isCompanyPaid);
    //            } else {
    //                throw new UserException("No mapping for payroll item ID " + classicPayrollItemId.toString());
    //            }
    //        }
    //        Integer taxID2 = row.get("TaxItemId2") == null ? null :(Integer) row.get("TaxItemId2");
    //        BigDecimal rate1 = row.get("TaxRate1") == null ? null :(BigDecimal) row.get("TaxRate1");
    //        BigDecimal rate2 = row.get("TaxRate2") == null ? null :(BigDecimal) row.get("TaxRate2");
    //        String classicRate = (String) row.get("ClassicRate");
    //        String classicRateType = (String) row.get("ClassicRateType");
    //        return new ClassicLocalTaxSplit(taxID, taxID2, rate1, rate2,
    //                classicPayrollItemId, classicName, classicRate, classicRateType,
    //                isCompanyPaid);
    //    }
    //
    //    // This method will create a deduction model from what qbo thought was going to be a local tax
    //    // so what we have is a classic payroll item id, no company or employee deduction id
    //    // including creating a new company and employee item if needed
    //    public static EmployeeDeductionModel createEmployeeDeductionModel( Company company, Employee employee, Long classicPayrollItemId) {
    //        if (null == classicPayrollItemId) {
    //            return null;
    //        }
    //        EmployeeDeductionModel eeDedModel= new EmployeeDeductionModel();
    //        CompanyDeductionModel coDedModel = new CompanyDeductionModel();
    //        eeDedModel.deductionType = EmployeeDeductionModel.DeductionType.EmployeeDeduction;
    //        eeDedModel.companyDeduction  = coDedModel;
    //
    //        coDedModel.description = ModelHelper.findClassicLocalTaxName(new Long(company.getExternalPartnerDataId()), classicPayrollItemId);
    //
    //        // see if there is already a company ded model
    //        CompanyDeduction companyDeduction = company.getDeduction(coDedModel.description);
    //        if (null == companyDeduction) {
    //        // if not, create it now
    //            coDedModel.deductionItemId = new Long(21);  // this is a vanilla deduction
    //
    //            companyDeduction = SetupWebServiceHelper.updateCompanyDeduction(coDedModel, company);
    //            eeDedModel.isActive = true;
    //            eeDedModel.amountType = EmployeeDeductionModel.AmountType.Dollar;
    //            eeDedModel.amount = BigDecimal.ZERO;
    //            eeDedModel.setExternalPartnerDataId(classicPayrollItemId.toString());
    //        } else {
    //            coDedModel.setId(companyDeduction.getId());
    //        }
    //
    //        return eeDedModel;
    //    }
    //
    //
    //    // This method will create and save an employee deduction, from what qbo thought was going to be a local tax
    //    //    if the company deduction does not exist, this method will also add the company deduction
    //    // so what we have is a classic payroll item id, no company or employee deduction id
    //    public static void addEmployeeDeduction( Company company, Employee employee, Long classicPayrollItemId, boolean isActive) {
    //        if (null == classicPayrollItemId) {
    //            return;
    //        }
    //        Long classicCompanyId = new Long(company.getExternalPartnerDataId());
    //
    //        // we have to get the tax name, rate and max too
    //        String sql = "select ClassicWageCap,ClassicRate,ClassicRateType,ClassicTaxName from ClassicLocalAndCustomTaxMap where ClassicCompanyId=#P1 and ClassicPayrollItemId=#P2" ;
    //        List<Map<String, Object>> results = Helper.executeDataReadQuery(sql,classicCompanyId,classicPayrollItemId);
    //        if (results.isEmpty()) {
    //           throw new UserException("No mapping for payroll item ID " + classicPayrollItemId.toString());
    //        }
    //        Map<String, Object> row = results.get(0);   // there could only be one result
    //
    //        String classicName = (String) row.get("ClassicTaxName");
    //        String temp = (String) row.get("ClassicRate");
    //        double rate = 0.0;
    //        if (null != temp) {
    //            rate = Double.parseDouble(temp);
    //        }
    //        String dollarOrPct = (String) row.get("ClassicRateType");
    //        int amountType = CompanyDeduction.AMT_TYPE_DOLLAR;
    //        if ((null != dollarOrPct) && !dollarOrPct.equalsIgnoreCase("Dollar")) {
    //            amountType = CompanyDeduction.AMT_TYPE_PERCENT;
    //        }
    //        temp = (String) row.get("ClassicWageCap");
    //        double max = 0.0;
    //        if (null != temp) {
    //            max = Double.parseDouble(temp);
    //        }
    //
    //        // see if there is already a company deduction for this one
    //        CompanyDeduction companyDeduction = company.getDeduction(classicName);
    //        if (null == companyDeduction) {
    //        // if not, create it now
    //            CompanyDeductionModel coDedModel = new CompanyDeductionModel();
    //            coDedModel.description = classicName;
    //            coDedModel.deductionItemId = new Long(21);  // this is a vanilla deduction
    //            companyDeduction = SetupWebServiceHelper.updateCompanyDeduction(coDedModel, company);
    //        }
    //        // and now add the employee deduction that uses the company deduction
    //        employee.addDeduction(companyDeduction, isActive, rate, amountType, max, classicPayrollItemId.toString());
    //    }
    //
    //	public static EmployeeWageItemModel validateEmployeeWageItemModel(EmployeeWageItemModel eeWageModel) {
    //
    //		// EmployeeWageItemModel must have a valid companyWageItemModel and a non-zero rate
    //		if (eeWageModel != null && eeWageModel.companyWageItem != null) {
    //			if (validateCompanyWageItemModel(eeWageModel.companyWageItem) != null) {
    //				return eeWageModel;
    //			}
    //		}
    //		// otherwise invalid EmployeeWageItem object
    //		return null;
    //	}
    //
    //	public static CompanyWageItemModel validateCompanyWageItemModel(CompanyWageItemModel coWageModel) {
    //
    //		if (coWageModel != null) {
    //			if (Helper.isNotEmpty(coWageModel.permanentName) || coWageModel.getId() > 0) {
    //				return coWageModel;
    //			}
    //		}
    //		// otherwise invalid CompanyWageItem object
    //		return null;
    //	}
    //
    //	public static EmployeeWageItemModel[] removeItem(EmployeeWageItemModel[] array,
    //								EmployeeWageItemModel item) {
    //
    //		if (item != null && array != null && Helper.isNotEmpty(array)) {
    //			List<EmployeeWageItemModel> mylist = new ArrayList<EmployeeWageItemModel>();
    //
    //			for (int i=0; i<array.length; i++) {
    //				if(item.equals(array[i])) {
    //					continue;
    //				}
    //				// else add EmployeeWageItemModel
    //				mylist.add(array[i]);
    //			}
    //			array = mylist.toArray(new EmployeeWageItemModel[0]);
    //		}
    //		return array;
    //	}
    //
}
