package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;

/**
 * Hand-written business logic
 */
public class EmployeeUsage extends BaseEmployeeUsage {

	private EmployeeUsageFoundCode employeeUsageFoundCode;

	private DomainEntitySet<EmployeeUsage> openBillEmployeeUsages = new DomainEntitySet<>();

	private DomainEntitySet<EmployeeUsage> closedBillEmployeeUsages = new DomainEntitySet<>();

	private DomainEntitySet<Bill> openBills = new DomainEntitySet<>();

	private DomainEntitySet<Bill> closedBills = new DomainEntitySet<>();

	/**
	 * Default constructor.
	 */
	public EmployeeUsage() {
		super();
	}

	public static EmployeeUsage findEmployeeUsage(UsagePeriod pUsagePeriod, String pEmployeeId) {
		EmployeeUsage foundEmployeeUsage = null;

		NaturalKey naturalKey = new NaturalKey(EmployeeUsage.class, pUsagePeriod.getId(), pEmployeeId);
		SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

		if (primaryKey != null) {
			foundEmployeeUsage = Application.findById(EmployeeUsage.class, primaryKey);
		} else {
			DomainEntitySet<EmployeeUsage> employeeUsages = Application.find(EmployeeUsage.class, EmployeeUsage.UsagePeriod().equalTo(pUsagePeriod).And(EmployeeUsage.SourceEmployeeId().equalTo(pEmployeeId)));

			if (employeeUsages.size() > 1) {
				throw new RuntimeException("Query for employee usages by bill usage period" + pUsagePeriod + " and id " + pEmployeeId + " did not return 0 or 1 results as expected");
			}

			if (!employeeUsages.isEmpty()) {
				foundEmployeeUsage = employeeUsages.get(0);
				Application.getSessionCache().addPrimaryKey(naturalKey, foundEmployeeUsage.getId());
			}
		}

		return foundEmployeeUsage;
	}

    public static DomainEntitySet<EmployeeUsage>  findEmployeeUsages(UsagePeriod pUsagePeriod) {
        return Application.find(EmployeeUsage.class, new Query<EmployeeUsage>().Where(EmployeeUsage.UsagePeriod().equalTo(pUsagePeriod))
                                                                                       .EagerLoad(EmployeeUsage.PaycheckUsageSet()));
    }

	public static EmployeeUsage createEmployeeUsage(UsagePeriod pUsagePeriod, String pEmployeeId, String pEmployeeName, String pEmployeeRecordNumber) {
		EmployeeUsage createdEmployeeUsage = new EmployeeUsage();

		createdEmployeeUsage.setUsagePeriod(pUsagePeriod);
		createdEmployeeUsage.setEmployeeName(pEmployeeName);
		createdEmployeeUsage.setSourceEmployeeId(pEmployeeId);
		createdEmployeeUsage.setEmployeeRecordNumber(pEmployeeRecordNumber);
		createdEmployeeUsage.setUsageCount(0);

		Application.save(createdEmployeeUsage);

		NaturalKey naturalKey = new NaturalKey(EmployeeUsage.class, pUsagePeriod.getId(), pEmployeeId);
		Application.getSessionCache().addPrimaryKey(naturalKey, createdEmployeeUsage.getId());

		return createdEmployeeUsage;
	}

	public static EmployeeUsage findOrCreateEmployeeUsage(UsagePeriod pUsagePeriod, String pEmployeeId, String pEmployeeName, String pEmployeeRecordNumber) {
		EmployeeUsage aEmployeeUsage = findEmployeeUsage(pUsagePeriod, pEmployeeId);
		if (aEmployeeUsage == null) {
			aEmployeeUsage = createEmployeeUsage(pUsagePeriod, pEmployeeId, pEmployeeName, pEmployeeRecordNumber);
			aEmployeeUsage.associateExistingEmployeeUsages();
		}
		return aEmployeeUsage;
	}

	public static DomainEntitySet<EmployeeUsage> findEmployeeUsageByFedTaxIdAndListId(String fedTaxId, UsagePeriod usagePeriod, String sourceEmployeeId) {

		List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, fedTaxId);

		String[] paramNames = {"fedTaxIdEncList", "startDate", "endDate", "sourceEmployeeId"};
		Object[] paramValues = {fedTaxIdEncList, usagePeriod.getStartDate(), usagePeriod.getEndDate(), sourceEmployeeId};

		return Application.findByNamedQuery("findEmployeeUsageByFedTaxIdAndSourceEmployeeId", paramNames, paramValues);
	}

	public static DomainEntitySet<EmployeeUsage> findOtherEmployeeUsageByFedTaxIdAndListId(String fedTaxId, UsagePeriod usagePeriod, String sourceEmployeeId, SpcfUniqueId employeeUsageId) {
		List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, fedTaxId);

		String[] paramNames = {"fedTaxIdEncList", "startDate", "endDate", "sourceEmployeeId", "employeeUsageId"};
		Object[] paramValues = {fedTaxIdEncList, usagePeriod.getStartDate(), usagePeriod.getEndDate(), sourceEmployeeId, employeeUsageId};

		return Application.findByNamedQuery("findOtherEmployeeUsageByFedTaxIdAndSourceEmployeeId", paramNames, paramValues);
	}

	public static DomainEntitySet<EmployeeUsage> findEmployeeUsageByFedTaxIdAndEmployeeName(String fedTaxId, UsagePeriod usagePeriod, String firstName, String lastName) {

		List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, fedTaxId);

		firstName = StringUtils.defaultString(firstName);
		lastName = StringUtils.defaultString(lastName);
		String[] paramNames = {"fedTaxIdEncList", "startDate", "endDate", "firstName", "lastName"};
		Object[] paramValues = {fedTaxIdEncList, usagePeriod.getStartDate(), usagePeriod.getEndDate(), firstName.trim().toLowerCase(), lastName.trim().toLowerCase()};

		return Application.findByNamedQuery("findEmployeeUsageByFedTaxIdAndEmployeeName", paramNames, paramValues);
	}

	public static DomainEntitySet<EmployeeUsage> findOtherEmployeeUsageByFedTaxIdAndEmployeeName(String fedTaxId, UsagePeriod usagePeriod, String firstName, String lastName, SpcfUniqueId employeeUsageId) {

		List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, fedTaxId);

		firstName = StringUtils.defaultString(firstName);
		lastName = StringUtils.defaultString(lastName);
		String[] paramNames = {"fedTaxIdEncList", "startDate", "endDate", "firstName", "lastName", "employeeUsageId"};
		Object[] paramValues = {fedTaxIdEncList, usagePeriod.getStartDate(), usagePeriod.getEndDate(), firstName.trim().toLowerCase(), lastName.trim().toLowerCase(), employeeUsageId};

		return Application.findByNamedQuery("findOtherEmployeeUsageByFedTaxIdAndEmployeeName", paramNames, paramValues);
	}

	public static boolean isAllEmployeeUsagesOfSameCompany(DomainEntitySet<EmployeeUsage> employeeUsages){
		String sourceCompanyId = null;
		for (EmployeeUsage employeeUsage: employeeUsages) {
			if(sourceCompanyId == null){
				sourceCompanyId = employeeUsage.getUsagePeriod().getCompanyUsage().getSourceCompanyId();
				continue;
			}

			if(!sourceCompanyId.equals(employeeUsage.getUsagePeriod().getCompanyUsage().getSourceCompanyId())){
				return false;
			}
		}
		return true;
	}

	public EmployeeUsageFoundCode getEmployeeUsageFoundCode() {
		return employeeUsageFoundCode;
	}

	public void setEmployeeUsageFoundCode(EmployeeUsageFoundCode employeeUsageFoundCode) {
		this.employeeUsageFoundCode = employeeUsageFoundCode;
	}

	public DomainEntitySet<EmployeeUsage> getOpenBillEmployeeUsages() {
		return openBillEmployeeUsages;
	}

	public void addOpenBillEmployeeUsage(EmployeeUsage employeeUsage) {
		this.openBillEmployeeUsages.add(employeeUsage);
	}

	public DomainEntitySet<EmployeeUsage> getClosedBillEmployeeUsages() {
		return closedBillEmployeeUsages;
	}

	public void addClosedBillEmployeeUsage(EmployeeUsage employeeUsage) {
		this.closedBillEmployeeUsages.add(employeeUsage);
	}

	public Pair<EmployeeUsageFoundCode, DomainEntitySet<EmployeeUsage>> findExistingEmployeeUsages() {
		String sourceCompanyId = getUsagePeriod().getCompanyUsage().getSourceCompanyId();

		Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

		DomainEntitySet<EmployeeUsage> employeeUsages = findEmployeeUsageByFedTaxIdAndListId(company.getFedTaxId(), getUsagePeriod(), getSourceEmployeeId());

		if (employeeUsages.isNotEmpty()) {
			return Pair.of(EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID, employeeUsages);
		}

		Employee employee = Employee.findEmployeeByQBListId(company, getSourceEmployeeId());

		employeeUsages = findEmployeeUsageByFedTaxIdAndEmployeeName(company.getFedTaxId(), getUsagePeriod(), employee.getFirstName(), employee.getLastName());

		if (employeeUsages.isNotEmpty()) {
			return Pair.of(EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_NAME, employeeUsages);
		}

		return null;
	}

	public Pair<EmployeeUsageFoundCode, DomainEntitySet<EmployeeUsage>> findOtherEmployeeUsages() {
		String sourceCompanyId = getUsagePeriod().getCompanyUsage().getSourceCompanyId();

		Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

		DomainEntitySet<EmployeeUsage> employeeUsages = findOtherEmployeeUsageByFedTaxIdAndListId(company.getFedTaxId(), getUsagePeriod(), getSourceEmployeeId(), getId());

		if (employeeUsages.isNotEmpty()) {
			return Pair.of(EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID, employeeUsages);
		}

		Employee employee = Employee.findEmployeeByQBListId(company, getSourceEmployeeId());

		if(Objects.isNull(employee)){
			return null;
		}

		employeeUsages = findOtherEmployeeUsageByFedTaxIdAndEmployeeName(company.getFedTaxId(), getUsagePeriod(), employee.getFirstName(), employee.getLastName(), getId());

		if (employeeUsages.isNotEmpty()) {
			return Pair.of(EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_NAME, employeeUsages);
		}

		return null;
	}

	public void associateExistingEmployeeUsages(){
		Pair<EmployeeUsageFoundCode, DomainEntitySet<EmployeeUsage>> existingEmployeeUsagePair = findExistingEmployeeUsages();

		if(Objects.isNull(existingEmployeeUsagePair)){
			return;
		}

		setEmployeeUsageFoundCode(existingEmployeeUsagePair.getLeft());

		for(EmployeeUsage employeeUsage: existingEmployeeUsagePair.getRight()){

			employeeUsage.groupAndAssociateBillsByStatus();

			if(employeeUsage.isAnyBillOpen()){
				addOpenBillEmployeeUsage(employeeUsage);
			} else {
				addClosedBillEmployeeUsage(employeeUsage);
			}
		}
	}

	public int increaseUsageCount() {
		setUsageCount(getUsageCount() + 1);
		return getUsageCount();
	}

	public int decreaseUsageCount() {
		int oldCount = getUsageCount();
		if (oldCount > 0) {
			setUsageCount(oldCount - 1);
		}
		return getUsageCount();
	}

	public DomainEntitySet<Bill> getOpenBills() {
		return openBills;
	}

	public DomainEntitySet<Bill> getClosedBills() {
		return closedBills;
	}

	public void addOpenBill(Bill bill){
		this.openBills.add(bill);
	}

	public void addClosedBill(Bill bill){
		this.closedBills.add(bill);
	}

	public boolean isAnyBillOpen() {
		return getOpenBills().isNotEmpty();
	}

	public boolean containsMultipleOpenBills() {
		return getOpenBills().size() > 1;
	}

	public boolean containsOpenAndClosedBill(){
		return getOpenBills().isNotEmpty() && getClosedBills().isNotEmpty();
	}

	public void groupAndAssociateBillsByStatus() {
		DomainEntitySet<PaycheckUsage> paycheckUsageCollection = getPaycheckUsageCollection();
		if(paycheckUsageCollection.isEmpty()){
			return;
		}

		for (PaycheckUsage paycheckUsage: paycheckUsageCollection) {
			Bill bill = paycheckUsage.getBill();
			if(bill.getClosed()){
				addClosedBill(bill);
			} else {
				addOpenBill(bill);
			}

		}
	}

	public boolean isUsageTransfer(){
		DomainEntitySet<PaycheckUsage> paycheckUsageCollection = getPaycheckUsageCollection();
		if(paycheckUsageCollection.isEmpty()){
			return false;
		}

		for(PaycheckUsage paycheckUsage : paycheckUsageCollection){
			if(paycheckUsage.getReasonForFreeCharge() == ReasonForFreeChargeCode.UsageTransfer){
				return true;
			}
		}

		return false;
	}

	public DomainEntitySet<PaycheckUsage> getAlreadyBilledPaychecks() {
		DomainEntitySet<PaycheckUsage> alreadyBilledPaycheckUsageCollection = new DomainEntitySet<PaycheckUsage>();

		for(PaycheckUsage paycheckUsage :  getPaycheckUsageCollection()){
			if(paycheckUsage.getReasonForFreeCharge() != ReasonForFreeChargeCode.AlreadyBilled){
				continue;
			}
			alreadyBilledPaycheckUsageCollection.add(paycheckUsage);
		}
		return alreadyBilledPaycheckUsageCollection;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("EmployeeUsage")
				.append("  Id=").append(getId())
				.append("  SourceCompanyId=").append(getUsagePeriod().getCompanyUsage().getSourceCompanyId())
				.append("  SourceEmployeeId=").append(getSourceEmployeeId())
				.append("  UsagePeriodStartDate=").append(getUsagePeriod().getStartDate())
				.append("  UsagePeriodEndDate=").append(getUsagePeriod().getEndDate());
		return builder.toString();
	}

	public enum EmployeeUsageFoundCode {
		MATCHES_FED_TAX_ID_AND_LIST_ID("Employee with same Fed Tax Id and List Id found"),
		MATCHES_FED_TAX_ID_AND_NAME("Employee with same Fed Tax Id and Name Found");

		private String reason;

		EmployeeUsageFoundCode(String reason){
			this.reason = reason;
		}

		public String getReason(){
			return this.reason;
		}
	}

	public enum OpenBillEmployeeUsageCode {
		USAGE_ALREADY_TRANSFERRED,
		ELIGIBLE_FOR_PAYCHECK_USAGE_TRANSFER,
		ELIGIBLE_FOR_EMPLOYEE_USAGE_TRANSFER
	}

	/* These code are applicable only if any existing employee usages exists for the processing employee usage */
	public enum UsageBillingEventCode {
		MULTIPLE_EMPLOYEE_USAGES_FOUND_FOR_CLOSED_BILL,
		FOUND_MULIPLE_OPEN_EMPLOYEES_USAGES_FOR_USAGE_TRANSFER,
		FOUND_MULIPLE_OPEN_EMPLOYEES_USAGES_ACROSS_COMPANIES,
		USAGE_BILLING_WAIVED_OFF_ON_ALREADY_BILLED_USAGE,
		NO_WAIVE_OFF_BECAUSE_USAGE_NOT_ALREADY_BILLED,
		USAGE_TRANSFERRED,
		EMPLOYEE_USAGE_ALREADY_TRANSFERRED,
		EMPLOYEE_USAGE_IS_ALREADY_ZERO,
		FOUND_MULTIPLE_OPEN_BILLS_FOR_SAME_EMPLOYEE,
		FOUND_BOTH_OPEN_AND_CLOSED_BILL_FOR_SAME_EMPLOYEE,
		FOUND_MULTIPLE_ALREADY_BILLED_EMPLOYEE_USAGES
	}
}
