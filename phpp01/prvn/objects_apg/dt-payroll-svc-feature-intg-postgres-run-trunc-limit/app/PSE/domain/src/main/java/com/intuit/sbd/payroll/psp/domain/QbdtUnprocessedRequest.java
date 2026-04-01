package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class QbdtUnprocessedRequest extends BaseQbdtUnprocessedRequest {

	/**
	 * Default constructor.
	 */
	public QbdtUnprocessedRequest()
	{
		super();
	}

    public static DomainEntitySet<QbdtUnprocessedRequest> findUnprocessedRequests(Company pCompany, boolean eagerLoadTransmission, QbdtRequestStatus... pQbdtRequestStatuses) {
        Criterion<QbdtUnprocessedRequest> where = QbdtUnprocessedRequest.Company().equalTo(pCompany)
                .And(QbdtUnprocessedRequest.Status().in(pQbdtRequestStatuses));
            return Application.find(QbdtUnprocessedRequest.class, new Query<QbdtUnprocessedRequest>()
                .Where(where));
    }

    public static DomainEntitySet<QbdtUnprocessedRequest> findUnprocessedRequests(int batchSize, QbdtRequestStatus... pQbdtRequestStatuses) {
        String[] paramNames = new String[1];
        paramNames[0] = "statuses";

        Object[] paramValues = new Object[1];
        paramValues[0] = pQbdtRequestStatuses;

        return Application.findByNamedQuery("findUnprocessedRequestsForCompaniesNotInError", paramNames, paramValues, 0, batchSize);
    }

    public static QbdtUnprocessedRequest findUnprocessedRequest(SpcfUniqueId pId) {
        Expression<QbdtUnprocessedRequest> query = new Query<QbdtUnprocessedRequest>()
                .Where(QbdtUnprocessedRequest.Id().equalTo(pId));
        DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests = Application.find(QbdtUnprocessedRequest.class, query);
        if (qbdtUnprocessedRequests.size() == 0) {
            return null;
        } else {
            return qbdtUnprocessedRequests.get(0);
        }
    }

    public static DomainEntitySet<QbdtUnprocessedRequest> findUnprocessedRequests(Company pCompany,SpcfCalendar pFromDate, SpcfCalendar pToDate, QbdtRequestStatus... pQbdtRequestStatuses) {
        Criterion<QbdtUnprocessedRequest> where = QbdtUnprocessedRequest.Company().equalTo(pCompany)
                                                                        .And(QbdtUnprocessedRequest.Status().in(pQbdtRequestStatuses))
                                                                        .And(QbdtUnprocessedRequest.CreatedDate().between(pFromDate,pToDate));
        return Application.find(QbdtUnprocessedRequest.class, new Query<QbdtUnprocessedRequest>()
                .Where(where)
                .OrderBy(QbdtUnprocessedRequest.CreatedDate().Descending()));
    }

    public static void clearUnprocessedRequest(Company company, String errorMsg, QbdtRequestStatus... pQbdtRequestStatuses) {

        DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests = QbdtUnprocessedRequest.findUnprocessedRequests(company, false, pQbdtRequestStatuses);

        for (QbdtUnprocessedRequest qbdtUnprocessedRequest : qbdtUnprocessedRequests) {
            if ((qbdtUnprocessedRequest.getStatus().equals(com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus.Error))) {
                qbdtUnprocessedRequest.setStatus(com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus.Processed);
            } else {
                qbdtUnprocessedRequest.setStatus(com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus.Processed);
                qbdtUnprocessedRequest.setErrorMessage(errorMsg);
            }
            Application.save(qbdtUnprocessedRequest);
        }
    }

}