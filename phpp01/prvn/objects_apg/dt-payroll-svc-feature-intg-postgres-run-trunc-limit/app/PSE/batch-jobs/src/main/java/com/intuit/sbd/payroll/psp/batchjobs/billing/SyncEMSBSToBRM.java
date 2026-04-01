package com.intuit.sbd.payroll.psp.batchjobs.billing;

import com.intuit.ems.payroll.psp.gateway.brm.BRMGatewayFactory;
import com.intuit.ems.payroll.psp.gateway.brm.CreateServiceResponse;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.Bill;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.ems.payroll.psp.gateway.brm.BRMGateway;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/23/12
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SyncEMSBSToBRM {
	private static SpcfLogger logger = Application.getLogger(SyncEMSBSToBRM.class);

	public void sync() {
		try {
			StopWatch sw = new StopWatch().start();

            SpcfCalendar billDate = PSPDate.getPSPTime();
            billDate.addDays(1);

            Application.beginUnitOfWork();
            List<SpcfUniqueId> billIdsToSynch = Bill.findOpenBillsOnDate(billDate);
            Application.rollbackUnitOfWork();

			updateBRM(billIdsToSynch);

			sw.stop();
			logger.info("completed EMSBS to BRM sync number of bills " + billIdsToSynch.size() + "     " +
					            "duration: " + sw.getElapsedTimeString());
		} catch (Throwable t) {
			// no db actions if anything goes wrong unexpectedly
			logger.error("failed to sync EMSBS to BRM", t);
		} finally {
            Application.rollbackUnitOfWork();
        }
	}

	protected void updateBRM(List<SpcfUniqueId> pBillIdsToSynch) {
		int processors = Runtime.getRuntime().availableProcessors();
		int threadCount = processors * 2;
		ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
		CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

		for (SpcfUniqueId billId : pBillIdsToSynch) {
			completionService.submit(new BRMUpdater(billId));
		}

		for (int i = 0; i < pBillIdsToSynch.size(); i++) {
			try {
				completionService.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Throwable t) {
				// should not get runtime error here, but in case of the unexpected errors, swallow it and let other threads go
				logger.info("failed to sync EMSBS to BRM", t);
			}
		}
	}

	protected class BRMUpdater implements Callable<Integer> {
		private SpcfUniqueId mBillId;

		BRMUpdater(SpcfUniqueId pBillId) {
			mBillId = pBillId;
		}

        public Integer call() throws Exception {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EMSBSToBRMDataSyncBatchJob));
            try {
                Application.beginUnitOfWork();
                Bill bill = Application.findById(Bill.class, mBillId);
                int delta = bill.getUsageCount() - bill.getSynchedCount();
                if (delta != 0) {
                    StopWatch sw = new StopWatch().start();

                    // call BRM web service to send delta
                    try {
                        BRMGateway brmGateway = BRMGatewayFactory.createInstance();
                        brmGateway.createUsage(bill.getCompanyUsage().getLicenseId(), bill.getCompanyUsage().getEntitlementId(), bill.getBillDate(), delta);
                        bill.setSynchedCount(bill.getUsageCount());
                    } finally {
                        bill.setClosed(true);
                        Application.save(bill);
                        Application.commitUnitOfWork();
                        sw.stop();
                        logger.info("completed EMSBS to BRM sync on bill " + mBillId + "     " +
                                            "duration: " + sw.getElapsedTimeString());
                    }
                }

                return delta;
            } catch (Throwable e) {
                logger.error("failed to the synch process with BRM. Bill id: " + mBillId, e);
            } finally {
                Application.rollbackUnitOfWork();
            }

            return 0;
        }
    }
}

