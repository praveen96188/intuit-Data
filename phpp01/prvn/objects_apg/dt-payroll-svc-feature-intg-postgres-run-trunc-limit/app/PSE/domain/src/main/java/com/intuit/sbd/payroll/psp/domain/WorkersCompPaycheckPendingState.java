package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Hand-written business logic
 */
public class WorkersCompPaycheckPendingState extends BaseWorkersCompPaycheckPendingState {

	private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompPaycheckPendingState.class);

	/**
	 * Default constructor.
	 */
	public WorkersCompPaycheckPendingState()
	{
		super();
	}
	public static List<Set<SpcfUniqueId>> getCompaniesWithPendingPaychecks(String queryName) {

		List<Set<SpcfUniqueId>> batches = new ArrayList<Set<SpcfUniqueId>>();

		// Find pending paychecks count by company
		ArrayList<Object[]> results =
				Application.executeNamedQuery(queryName, null, null);
		Map<SpcfUniqueId, Number> pendingPaycheckCountByCompany = new HashMap<SpcfUniqueId, Number>();
		if (results != null) {
			for (Object[] result : results) {
				pendingPaycheckCountByCompany.put((SpcfUniqueId) result[0], (Number) result[1]);
			}
		}

		if (pendingPaycheckCountByCompany != null && pendingPaycheckCountByCompany.size() > 0) {
			batches = split(
					pendingPaycheckCountByCompany,
					Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "wc_sftp_server_companies_batchSize")),
					Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "wc_sftp_server_paychecks_batchSize")));
		}

		return batches;
	}

	public static <T> List<Set<T>> split(Map<T, Number> countByObj, int maxObjCountInBatch, int maxTotalCountInBatch) {
		List<Set<T>> batches = new ArrayList<Set<T>>();
		if (countByObj != null && countByObj.size() > 0) {
			Set<T> batch = new HashSet<T>();
			batches.add(batch);
			int batchCount = 0;
			for (T t : countByObj.keySet()) {
				int objCount = countByObj.get(t).intValue();
				if ((batchCount + objCount) <= maxTotalCountInBatch && batch.size() < maxObjCountInBatch) {
					batch.add(t);
					batchCount += objCount;
				} else {
					if (batch.size() > 0) {
						batch = new HashSet<T>();
						batches.add(batch);
					}
					batch.add(t);
					batchCount = objCount;
				}
			}
		}
		return batches;
	}
	public static List<WorkersCompPaycheckPendingState> getPendingPaychecks(Set<SpcfUniqueId> companyIds,SpcfCalendar currentDate, List<com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckStateCode> subsCode) {
		List<SpcfUniqueId> pendingPaychecks=null;
		List<WorkersCompPaycheckPendingState> pendingPaychecksList=null;
		try {

			pendingPaychecks =
					Application.executeNamedQuery("findWCPendingPaychecksByNextCompanies",
							new String[]{"companyIds","currentDate","subsCode"},
							new Object[]{companyIds,currentDate,subsCode},
							0,
							Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "wc_sftp_server_paychecks_batchSize")));
			if(CollectionUtils.isEmpty(pendingPaychecks))
			{
				logger.info("No pending paychecks exist for company"+companyIds);
				return null;
			}
			Set<WorkersCompPaycheckPendingState> paychecks = Application.find(WorkersCompPaycheckPendingState.class,
					new Query<WorkersCompPaycheckPendingState>().Where(WorkersCompPaycheckPendingState.Id().in(pendingPaychecks.toArray( new SpcfUniqueId[pendingPaychecks.size()])))
							.OrderBy(WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().Company())
							.EagerLoad(WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().Company().equalTo(WorkersCompPaycheckPendingState.WorkersCompPaycheck().Company()))
									.EagerLoad(WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().CompensationSet().Filter().Company().equalTo(
									WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().Company()
							))
							.EagerLoad(WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().DeductionSet().Filter().Company().equalTo(
									WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().Company()
							)).EagerLoad(WorkersCompPaycheckPendingState.WorkersCompPaycheck(),
									WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().Company(),
									WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().PayrollRun(),
									WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().SourceEmployee(),
									WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck().Company().LegalAddress())).toNative();
			pendingPaychecksList=new ArrayList<>();
			pendingPaychecksList.addAll(paychecks);
		} finally {
			return pendingPaychecksList;
		}
	}
}