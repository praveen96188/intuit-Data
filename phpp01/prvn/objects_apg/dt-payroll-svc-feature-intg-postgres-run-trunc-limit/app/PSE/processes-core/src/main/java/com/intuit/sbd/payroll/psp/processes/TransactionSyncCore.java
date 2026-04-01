/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * This process allows a client to submit a transaction token to the system in
 * order to obtain a list of transactions that needs to be sync'd up with the
 * client
 * 
 * @author Sean Barenz
 */
public class TransactionSyncCore extends Process implements IProcess {
	private DomainEntitySet<TransactionResponse> transactionResponses;

	private SourceSystemCode sourceSystemCd;
	private String sourceCompanyId;
	private Long token;

	Company company;


	/**
	 * Constructor method for Transaction Sync Core
	 * 
	 * @param pSourceSystemCd Source System Code
	 * @param pSourceCompanyId Source Company ID
	 * @param pToken Token ID used for the sync
	 */
	public TransactionSyncCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, Long pToken) {
		sourceSystemCd = pSourceSystemCd;
		sourceCompanyId = pSourceCompanyId;
		token = pToken;
	}

	/**
	 * Obtains a collection of one or more transaction responses
	 * 
	 * @return Collection of TransactionResponses to sync back to the client
	 */
	public DomainEntitySet<TransactionResponse> getTransactionResponses() {
		return transactionResponses;
	}

	/**
	 * Obtains any transaction responses from the database. If there were no
	 * responses returned, then an info message is added to the process result
	 * and returned to the client
	 * 
	 * @return ProcessResult - empty collection if successful. An info message
	 *         is provided if no transaction resposnes were found
	 */
	public ProcessResult process() {
		ProcessResult processResult = new ProcessResult();
		transactionResponses = TransactionResponse.findTransactionResponses(company, token);

		// If the resposne size is zero, send an info alert back to the client
		if (transactionResponses.size() == 0) {
			String tokenString = Long.toString(token);
			processResult.getMessages().NoTransactionResponsesFound(EntityName.TransactionResponse,
					tokenString, sourceSystemCd.toString(), sourceCompanyId, tokenString);
		}
		return processResult;
	}

	/**
	 * Validates the client input for transaction sync.<br/>
	 * 
	 * Return an error if the company does not exist
	 * 
	 * @return ProcessResult collection containing any validation errors.
	 *         Otherwise returns an empty process collection on success
	 */
	public ProcessResult validate() {
		ProcessResult validationResult = new ProcessResult();

		// Validate Source System Cd
		if (sourceSystemCd == null) {
			validationResult.getMessages().SourceSystemCdNotSpecified(EntityName.Company,
					sourceCompanyId);
			return validationResult;
		}

		// Validate Source CompanyId
		if (sourceCompanyId == null) {
			validationResult.getMessages().CompanyIdNotSpecified(EntityName.Company,
					sourceCompanyId);
			return validationResult;
		}

		// Validate the company exists
		company = Company.findCompany(sourceCompanyId, sourceSystemCd);
		if (company == null) {
			validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
					sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (! company.isAllowedCapability(SystemCapabilityCode.SynchronizeAccount)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(), SystemCapabilityCode.SynchronizeAccount.toString());
            return validationResult;
        }

		return validationResult;
	}
}
