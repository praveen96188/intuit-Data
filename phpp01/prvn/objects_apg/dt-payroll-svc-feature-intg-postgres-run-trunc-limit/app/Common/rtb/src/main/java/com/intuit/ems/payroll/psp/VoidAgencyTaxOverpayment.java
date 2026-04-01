package com.intuit.ems.payroll.psp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.FinancialTransactionState;
import com.intuit.sbd.payroll.psp.domain.LedgerAccount;
import com.intuit.sbd.payroll.psp.domain.LedgerAccountCode;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * 
 * @author kmuthurangam on 10/03/2016
 */
public class VoidAgencyTaxOverpayment {

	private static final String FILE_NAME_COMMAND = "-file";
	private static final String EXCLUDE_FILE_NAME_COMMAND = "-exclude-file";
	private static final String COMMIT_COMMAND = "-commit";

	private File mFileName;
	private File mExcludeFileName;
	private boolean mCommit;

	public static void main(String[] args) {

		try {
			VoidAgencyTaxOverpayment agentTaxOverpayment = new VoidAgencyTaxOverpayment();
			agentTaxOverpayment.parseArgs(args);
			List<String> records = FileUtils.readLines(agentTaxOverpayment.getFileName());
			List<String> sourceCompanyIds = FileUtils.readLines(agentTaxOverpayment.getExcludeFileName());
			agentTaxOverpayment.processCompanies(records, sourceCompanyIds);
		} catch (IOException exception) {
			System.out.println("ERROR: Error in main");
			exception.printStackTrace();
			System.exit(-1);
		}

	}

	public File getFileName() {
		return mFileName;
	}

	public File getExcludeFileName() {
		return mExcludeFileName;
	}

	private void parseArgs(String[] args) {

		final String usage = "VoidAgentTaxOverpayment -file=FullPathOfFile -exclude-file=FullPathOfFile -commit=[true|false]";

		for (String arg : args) {
			String[] argParts = arg.split("=");
			if (argParts.length == 2) {
				if (argParts[0].equals(FILE_NAME_COMMAND)) {
					mFileName = new File(argParts[1]);
				} else if (argParts[0].equals(EXCLUDE_FILE_NAME_COMMAND)) {
					mExcludeFileName = new File(argParts[1]);
				} else if (argParts[0].equals(COMMIT_COMMAND)) {
					mCommit = Boolean.valueOf(argParts[1]);
				}
			} else {
				print("ERROR: Invalid Argument, Usage - " + usage);
				throw new RuntimeException("Invalid argument: " + arg);
			}
		}

		if (mFileName == null || mExcludeFileName == null) {
			print("ERROR: Invalid parameters - Must provide filename. Usage " + usage);
			System.exit(-1);
		}
	}

	private void processCompanies(List<String> records, List<String> sourceCompanyIds) throws IOException {

		print("INFO: Starting Void Agency Tax Overpayment with filename - %s, exclude-file - %s and commit - %s",
				mFileName, mExcludeFileName, mCommit);

		ExecutorService executor = Executors.newFixedThreadPool(10);
		CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(executor);

		int processedRecords = 0;
		for (int i = 0; i < records.size(); i++) {
			String record = records.get(i);
			final ATRRecord agencyTaxRefund = validateAndCreateATRRecord(record);
			if (agencyTaxRefund == null) {
				print("ERROR: %s,%s,%s", record, "No", "Invalid data");
				continue;
			}

			if (sourceCompanyIds.contains(agencyTaxRefund.getSourceCompanyId())) {
				print("WARN: %s,%s,%s", record, "No", "Excluding company from processing");
				continue;
			}

			if (agencyTaxRefund.equals(SpcfMoney.ZERO)) {
				print("WARN: %s,%s%s", record, "Yes", "Ledger already balanced (0.00)");
				continue;
			}

			processedRecords++;
			completionService.submit(new ATOProcessor(agencyTaxRefund));
		}

		for (int i = 0; i < processedRecords; i++) {
			try {
				completionService.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		print("INFO: Process completed. Total number of companies available - %s. Total number of companies processed - %s.",
				records.size(), processedRecords);

		ThreadingUtils.shutdownAndAwaitTermination(executor);
	}

	private void processCompany(ATRRecord agencyTaxRefund) {

		PayrollServices.setCurrentPrincipal(SystemPrincipal.TESBatchJob);

		PayrollServices.beginUnitOfWork();

		List<FinancialTransactionRecord> agentTaxOverpaymentFinancialTxns = findATOs(agencyTaxRefund);

		voidATO(agentTaxOverpaymentFinancialTxns);

		if (mCommit) {
			PayrollServices.commitUnitOfWork();
		} else {
			PayrollServices.rollbackUnitOfWork();
		}
	}

	private List<FinancialTransactionRecord> findATOs(ATRRecord atrRecord) {
		List<FinancialTransactionRecord> agentTaxOverpaymentFinancialTxns = new ArrayList<FinancialTransactionRecord>();

		Company company = Company.findCompany(atrRecord.getSourceCompanyId(), SourceSystemCode.QBDT);

		if (company == null) {
			print("INFO: %s,%s", atrRecord, "No", "Company not valid");
			return agentTaxOverpaymentFinancialTxns;
		}

		SpcfDecimal agencyTaxRefundAmount = LedgerAccount.getLedgerAccountBalance(company,
				LedgerAccountCode.AgencyTaxRefund);

		SpcfDecimal agencyTaxRefundMoney = atrRecord.getBalanceAmount();

		if (!agencyTaxRefundAmount.equals(agencyTaxRefundMoney)) {
			print("INFO: %s,%s,%s", atrRecord, "No",
					String.format(
							"Agency Tax Overpayment already voided. Calculated Agency Tax Refund (%s) is not equal to Given Agency Tax Refund (%s) ",
							agencyTaxRefundAmount, agencyTaxRefundMoney));
			return agentTaxOverpaymentFinancialTxns;
		}

		List<Object[]> txnList = FinancialTransaction.findFinancialTransactionsWithCreditDebitCode(company, null,
				LedgerAccountCode.AgencyTaxRefund);

		SpcfCalendar agencyTaxRefundPaycheckQuarter = CalendarUtils.getFirstDayOfQuarter(atrRecord.getYear(),
				atrRecord.getQuarter());

		Map<ATRRecordUniqueId, SpcfMoney> agencyTaxOverpaymentMap = new HashMap<ATRRecordUniqueId, SpcfMoney>();

		SpcfMoney agencyTaxOverpaymentAmount = new SpcfMoney();

		for (Object[] finTxnComboObject : txnList) {
			FinancialTransaction financialTransaction = (FinancialTransaction) finTxnComboObject[0];
			Boolean isCredit = finTxnComboObject[1] != null && "C".equals(finTxnComboObject[1]);

			PayrollRun payrollRun = financialTransaction.getPayrollRun();
			if (payrollRun == null) {
				continue;
			}

			String paymentTemplateCode = financialTransaction.getLaw().getPaymentTemplate().getPaymentTemplateCd();
			if (!paymentTemplateCode.equals(atrRecord.getPaymentTemplate())) {
				continue;
			}

			SpcfCalendar paycheckDate = financialTransaction.getPayrollRun().getPaycheckDate();
			SpcfCalendar firstDayOfPaycheckQuarter = CalendarUtils.getFirstDayOfQuarter(paycheckDate);
			if (!firstDayOfPaycheckQuarter.equals(agencyTaxRefundPaycheckQuarter)) {
				continue;
			}

			FinancialTransactionState currentFinancialTransactionState = financialTransaction
					.getCurrentFinancialTransactionState();
			if (currentFinancialTransactionState != null) {
				TransactionStateCode transactionStateCode = currentFinancialTransactionState.getTransactionState()
						.getTransactionStateCd();
				if (transactionStateCode == TransactionStateCode.Voided
						|| transactionStateCode == TransactionStateCode.Cancelled
						|| transactionStateCode == TransactionStateCode.Returned) {
					continue;
				}
			}

			ATRRecordUniqueId unique = new ATRRecordUniqueId(paymentTemplateCode, firstDayOfPaycheckQuarter);

			agencyTaxOverpaymentAmount = agencyTaxOverpaymentMap.get(unique);
			if (agencyTaxOverpaymentAmount == null) {
				agencyTaxOverpaymentAmount = new SpcfMoney();
			}

			if (isCredit) {
				agencyTaxOverpaymentAmount = (SpcfMoney) agencyTaxOverpaymentAmount
						.add(financialTransaction.getFinancialTransactionAmount());
			} else {
				agencyTaxOverpaymentAmount = (SpcfMoney) agencyTaxOverpaymentAmount
						.subtract(financialTransaction.getFinancialTransactionAmount());
			}
			if (!agencyTaxOverpaymentAmount.equals(SpcfMoney.ZERO)) {
				agencyTaxOverpaymentMap.put(unique, agencyTaxOverpaymentAmount);
			} else {
				agencyTaxOverpaymentMap.remove(unique);
			}

			String transactionType = (isCredit ? "C" : "D");

			agentTaxOverpaymentFinancialTxns.add(new FinancialTransactionRecord(financialTransaction.getId(),
					financialTransaction.getFinancialTransactionAmount(), transactionType));

			// print("AgencyTaxOverpayment Map (%s)", agencyTaxOverpaymentMap);
		}

		boolean patternMatches = agencyTaxOverpaymentAmount.equals(agencyTaxRefundMoney);

		if (agencyTaxOverpaymentAmount.negate().equals(agencyTaxRefundMoney)) {
			print("INFO: %s,%s,%s", atrRecord, (patternMatches ? "Yes" : "No"),
					"Agency Tax Overpayment already voided");
		} else {
			print("INFO: %s,%s,%s", atrRecord, (patternMatches ? "Yes" : "No"), agentTaxOverpaymentFinancialTxns);
		}
		return agentTaxOverpaymentFinancialTxns;
	}

	private void voidATO(List<FinancialTransactionRecord> agencyTaxOverpaymentFinancialTxns) {
		for (FinancialTransactionRecord financialTransactionRecord : agencyTaxOverpaymentFinancialTxns) {
			FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class,
					financialTransactionRecord.getId());
			if (financialTransaction != null) {
				financialTransaction.updateFinancialTransactionState(TransactionStateCode.Voided);
			}
		}
	}

	private ATRRecord validateAndCreateATRRecord(String line) {
		String[] columns = line.split(",");
		if (columns.length != 4) {
			return null;
		}
		String sourceCompanyId = columns[0];
		String paymentTemplateCode = columns[1];
		String yearAndQuarter = columns[2];
		String agencyTaxRefundAmount = columns[3];
		String[] yearAndQuater = yearAndQuarter.split("-");

		if (yearAndQuater.length != 2) {
			return null;
		}

		int year = Integer.valueOf(yearAndQuater[0]);
		int quarter = Integer.valueOf(yearAndQuater[1]);

		if (year >= 2015) {
			return null;
		}

		ATRRecord agencyTaxRefund = new ATRRecord(sourceCompanyId, paymentTemplateCode, quarter, year,
				new SpcfMoney(agencyTaxRefundAmount));
		return agencyTaxRefund;
	}

	private synchronized void print(String format, Object... args) {
		String message = String.format(format, args);
		System.out.println(message);
	}

	public class ATOProcessor implements Callable<Integer> {

		private ATRRecord agencyTaxRefund;

		public ATOProcessor(ATRRecord agencyTaxRefund) {
			super();
			this.agencyTaxRefund = agencyTaxRefund;
		}

		@Override
		public Integer call() throws Exception {
			processCompany(agencyTaxRefund);
			return null;
		}

	}

	public class ATRRecord {

		private String sourceCompanyId;
		private String paymentTemplate;
		private int quarter;
		private int year;
		private SpcfMoney balanceAmount;

		public ATRRecord(String sourceCompanyId, String paymentTemplate, int quarter, int year,
				SpcfMoney balanceAmount) {
			super();
			this.sourceCompanyId = sourceCompanyId;
			this.paymentTemplate = paymentTemplate;
			this.quarter = quarter;
			this.year = year;
			this.balanceAmount = balanceAmount;
		}

		public String getSourceCompanyId() {
			return sourceCompanyId;
		}

		public String getPaymentTemplate() {
			return paymentTemplate;
		}

		public int getQuarter() {
			return quarter;
		}

		public int getYear() {
			return year;
		}

		public SpcfMoney getBalanceAmount() {
			return balanceAmount;
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(sourceCompanyId).append(",");
			buffer.append(paymentTemplate).append(",");
			buffer.append(year).append("-").append(quarter).append(",");
			buffer.append(balanceAmount);
			return buffer.toString();
		}

	}

	public class ATRRecordUniqueId {

		private String paymentTemplate;
		private SpcfCalendar quarterStartDate;

		public ATRRecordUniqueId(String paymentTemplate, SpcfCalendar quarterStartDate) {
			super();
			this.paymentTemplate = paymentTemplate;
			this.quarterStartDate = quarterStartDate;
		}

		public String getPaymentTemplate() {
			return paymentTemplate;
		}

		public SpcfCalendar getQuarterStartDate() {
			return quarterStartDate;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((paymentTemplate == null) ? 0 : paymentTemplate.hashCode());
			result = prime * result + ((quarterStartDate == null) ? 0 : quarterStartDate.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ATRRecordUniqueId other = (ATRRecordUniqueId) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (paymentTemplate == null) {
				if (other.paymentTemplate != null)
					return false;
			} else if (!paymentTemplate.equals(other.paymentTemplate))
				return false;
			if (quarterStartDate == null) {
				if (other.quarterStartDate != null)
					return false;
			} else if (!quarterStartDate.equals(other.quarterStartDate))
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Unique [paymentTemplate=").append(paymentTemplate).append(", quarterStartDate=")
					.append(quarterStartDate).append("]");
			return builder.toString();
		}

		private VoidAgencyTaxOverpayment getOuterType() {
			return VoidAgencyTaxOverpayment.this;
		}
	}

	public class FinancialTransactionRecord {

		private SpcfUniqueId id;
		private SpcfMoney financialTransactionAmount;
		private String transactionType;

		public FinancialTransactionRecord(SpcfUniqueId id, SpcfMoney financialTransactionAmount,
				String transactionType) {
			super();
			this.id = id;
			this.financialTransactionAmount = financialTransactionAmount;
			this.transactionType = transactionType;
		}

		public SpcfUniqueId getId() {
			return id;
		}

		public SpcfMoney getFinancialTransactionAmount() {
			return financialTransactionAmount;
		}

		public String getTransactionType() {
			return transactionType;
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(id).append("#");
			buffer.append(financialTransactionAmount).append("#");
			buffer.append(transactionType);
			return buffer.toString();
		}

	}

}
